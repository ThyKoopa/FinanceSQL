import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.mysql.jdbc.MysqlDataTruncation;

import net.miginfocom.swing.MigLayout;

public class GUI {
	@SuppressWarnings("unused")
	private int	view;
	private Connector	connector;
	private JDialog 	userInput;
	private JFrame 		frame;
	private Login		userData;
	private JPanel 		panel;
	private JTextArea 	output;
	private Dimension 	screen;
	private JTextField		input;
	private JPasswordField	secretInput;
	
	public GUI(Connector conn)
	{
		connector = conn;
		screen = Toolkit.getDefaultToolkit().getScreenSize();
		userInput = new JDialog();
		setUserData(new Login("default"));
		getUserdata();
		setDialog();
	}
	
	public void setOutput(String text, boolean append)
	{
		if(append)
		{
			output.append(text);
		}
		else
		{
			output.setText(text);
		}
	}
	
	void getUserdata()
	{
		String username = userData.getUser();
		Statement st = connector.getStatement();
		try {
			ResultSet rs = st.executeQuery("SELECT categories,id FROM users WHERE user='"+username+"'");			
			rs.next();
			String cat = rs.getString(1);
			//System.out.println(cat);
			Scanner j = new Scanner(cat);
			j.useDelimiter(",");
			ArrayList<String> list = new ArrayList<String>();
			while(j.hasNext())
				list.add(j.next());
			userData.setCat(list);
			userData.setId(rs.getInt(2));
			j.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	private void setDialog()
	{
		userInput.dispose();
		userInput = new JDialog();
		userInput.setSize(175,150);
		userInput.setTitle("Login");
		userInput.setLayout(new MigLayout("fill, wrap 3"));
		userInput.add(new JLabel("Username", SwingConstants.RIGHT),"grow, align right");
		input = new JTextField();
		userInput.add(input, "grow, align center, span 2");
		userInput.add(new JLabel("Password", SwingConstants.RIGHT),"grow, align right");
		secretInput = new JPasswordField();
		userInput.add(secretInput, "grow, align center, span 2");
		JButton confirm = new JButton("Confirm");
			confirm.addActionListener(new ActionConfirm());
			userInput.add(confirm, "grow, align center");
		JButton register = new JButton("Register");
			register.addActionListener(new ActionRegister());
			userInput.add(register, "grow, align center");
		userInput.pack();
		userInput.setLocationRelativeTo(frame);
		userInput.setVisible(true);
	}
	
	class ActionRegister implements ActionListener
	{
		JPasswordField secretInput2;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			userInput.dispose();
			userInput = new JDialog();
			userInput.setTitle("Register");
			userInput.setSize(200,150);
			userInput.setLayout(new MigLayout("fill, wrap 3"));
			userInput.add(new JLabel("Username", SwingConstants.RIGHT), "grow, align right");
			userInput.add(input, "grow, align center, span 2");
			userInput.add(new JLabel("Password", SwingConstants.RIGHT), "grow, align right");
			userInput.add(secretInput, "grow, align center, span 2");
			secretInput2 = new JPasswordField();
			userInput.add(new JLabel("Confirm Password", SwingConstants.RIGHT), "grow, align right");
			userInput.add(secretInput2, "grow, align center, span 2");

			JButton register = new JButton("Register");
				register.addActionListener(new RegisterAction());
			userInput.add(register, "grow, align center");
			JButton cancel = new JButton("Cancel");
				cancel.addActionListener(new ActionCancel());
			userInput.add(cancel, "grow, align center");
			userInput.pack();
			userInput.setLocationRelativeTo(frame);
			userInput.setVisible(true);
		}
		
		class RegisterAction implements ActionListener
		{

			@Override
			public void actionPerformed(ActionEvent e) {
				if(input.getText().length() > 20)
					JOptionPane.showMessageDialog(null,"Password is longer than the 20 character limit", "ERROR",JOptionPane.WARNING_MESSAGE);
				else
					if(!Arrays.toString(secretInput.getPassword()).equals(Arrays.toString(secretInput2.getPassword())))
						JOptionPane.showMessageDialog(null,"Passwords do not match", "ERROR",JOptionPane.WARNING_MESSAGE);
					else
					{
						boolean taken = false;
						try{
							Statement s = connector.getConnection().createStatement();
							ResultSet rs = s.executeQuery("SELECT user FROM users");
							while(rs.next())
								if(rs.getString(1).equalsIgnoreCase(input.getText()))
								{
									JOptionPane.showMessageDialog(null,"Username taken", "ERROR",JOptionPane.WARNING_MESSAGE);
									taken = true;
								}
						}catch(Exception error){
							error.printStackTrace();
						}
						if(!taken)
						{
							try{
								Statement s = connector.getConnection().createStatement();						
								byte[] salt = PasswordHasher.getSalt();
								byte[] encPass = PasswordHasher.hashPass(Arrays.toString(secretInput.getPassword()), salt);
								//Test
								String passer = new String(encPass,StandardCharsets.UTF_8);
								String salter = new String(salt,StandardCharsets.UTF_8);
								byte[] pa = passer.getBytes(StandardCharsets.UTF_8);
								byte[] sa = salter.getBytes(StandardCharsets.UTF_8);
								if(!PasswordHasher.authenticate(Arrays.toString(secretInput.getPassword()),
										pa, sa))
									actionPerformed(e);
								else
								{
									s.execute("INSERT INTO users (user,password,salt,categories) VALUES ('"
												+ input.getText() + "','" + passer + "','" 
												+ salter + "','Food,Entertainment,Gas,Misc.')");
									createTable(s);
									JOptionPane.showMessageDialog(null, "Registration Successful!","Register",JOptionPane.PLAIN_MESSAGE);
									setDialog();
								}
								}catch(Exception error){}
							}
					}
			}
			
			private void createTable(Statement s)
			{
				try {
					ResultSet rs = s.executeQuery("SELECT id FROM USERS");
					rs.last();
					int tableNumber = rs.getInt(1);
					s.execute("CREATE TABLE user" + tableNumber + " ("
							+ "id int not null auto_increment,"
							+ "item tinytext not null,"
							+ "cost double not null,"
							+ "category smallint unsigned not null,"
							+ "desciption text not null,"
							+ "purchase_date date not null,"
							+ "submittion_date timestamp default current_timestamp not null,"
							+ "primary key(id)"
							+ ")");
				} catch (SQLException e) {
				}
				
			}
		}
		}
		
		class ActionCancel implements ActionListener
		{

			@Override
			public void actionPerformed(ActionEvent e) {
				setDialog();
			}
			
		}
	
	class ActionConfirm implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			String username = input.getText();
			char[] password = secretInput.getPassword();
			
			if (checkLogin(username, password))
			{
				JOptionPane.showMessageDialog(null, "Login successful!","Login",JOptionPane.PLAIN_MESSAGE);
				setUserData(new Login(username));
				getUserdata();
				
				userInput.dispose();

				setPanel();
				setFrame();
			}else
				{
					JOptionPane.showMessageDialog(null, "Wrong Username/Password","Login",JOptionPane.WARNING_MESSAGE);
				}
		}	
		
		private boolean checkLogin(String user, char[] pass)
		{
			Statement st = connector.getStatement();
			String salt = null;
			String hash = null;
			try{
				ResultSet rs = st.executeQuery("SELECT salt FROM users WHERE user = '" + user +"'");
				if(rs.next())
					salt = rs.getString(1);
				else
					return false;
				rs = st.executeQuery("SELECT password FROM users WHERE user = '" + user + "'");
				if(rs.next())
					hash = rs.getString(1);
				else
					return false;
				//System.out.println("Salt: " + salt + "\nPass: " + hash);
				//System.out.println("Password: " + user);
				return PasswordHasher.authenticate(Arrays.toString(pass), hash.getBytes(), salt.getBytes());
			}catch(Exception e){System.out.println(e);return false;}
		}
	}
	
	private void setFrame()
	{
		frame = new JFrame();
		frame.setSize((int)screen.getWidth()/2, (int)screen.getHeight()/2);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(3);
		frame.setContentPane(panel);
		frame.setVisible(true);
	}
	
	private void setPanel()
	{
		panel = new JPanel(new MigLayout("fill,wrap 4"));
		panel.setBackground(new Color(125,140,115));
		output = new JTextArea();
			output.setSize((int)screen.getWidth()/2, (int)screen.getHeight()/2);
			output.setEditable(false);
			output.setFocusable(false);
			output.setFont(new java.awt.Font("DialogInput",0,12));
		JScrollPane scrollPane = new JScrollPane(output);
			scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		panel.add(scrollPane, "grow,span 4 9");
		
		JButton buttonOne = new JButton();
			buttonOne.setText("Add Entry");
			buttonOne.addActionListener(new ActionOne());
		panel.add(buttonOne, "grow");
		
		JButton buttonTwo = new JButton();
			buttonTwo.setText("Show All Entries");
			buttonTwo.addActionListener(new ActionTwo());
		panel.add(buttonTwo, "grow");
	
		JButton buttonThree = new JButton();
			buttonThree.setText("Button 3");
			buttonThree.addActionListener(new ActionThree());
		panel.add(buttonThree, "grow");

		JButton buttonFour = new JButton();
			buttonFour.setText("Button 4");
			buttonFour.addActionListener(new ActionFour());
		panel.add(buttonFour, "grow");
	}
	
	private void updateOutput()
	{
		ArrayList<FinanceEntry> entries = userData.getEntries();
		output.setText("Entries:");
		output.append("\n===============================================================================================");
		
		for(FinanceEntry e: entries)
		{
			output.append("\n");
			output.append("Item: " + e.getItem());
			output.append("\nPrice: $" + e.getCost());
			output.append("\nCategory: " + userData.getCat(e.getCategory()));
			output.append("\nDate: " + e.getPurchase() );
			output.append("\nMemo: " + e.getDesc());
			output.append("\nEntry Number: " + e.getId());
			output.append("\nSubmitted: " + e.getSubmittion());
			output.append("\n===============================================================================================");
		}
	}
	
	private void setView(int viewInt){
		view = viewInt;
	}
	
	public Login getUserData() {
		return userData;
	}

	public void setUserData(Login userData) {
		this.userData = userData;
	}

	class ActionOne implements ActionListener{

		JTextField item,cost,date;
		JTextArea description;
		JComboBox<String> category;
		JDialog entryWindow;
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			entryWindow = new JDialog();
			entryWindow.setLayout(new MigLayout("fill,wrap 4"));
			entryWindow.add(new JLabel("Item",SwingConstants.RIGHT),"align right, span 1");
				item = new JTextField();
				entryWindow.add(item,"grow, span 3");
			entryWindow.add(new JLabel("Date Purchased",SwingConstants.RIGHT), "align right, span 1");
				date = new JTextField("mm/dd/yyyy");
				entryWindow.add(date,"grow, span 3");
			entryWindow.add(new JLabel("Cost",SwingConstants.RIGHT),"align right, span 1");
				cost = new JTextField();
				entryWindow.add(cost, "grow, span 3");
			entryWindow.add(new JLabel("Memo",SwingConstants.NORTH_EAST), "align right, span 1 4" );
				description = new JTextArea();
				description.setLineWrap(true);
				description.setWrapStyleWord(true);
				description.setRows(5);
				description.setMinimumSize(new Dimension(150,75));
				entryWindow.add(description, "grow, span 3 4");
			entryWindow.add(new JLabel("Category",SwingConstants.RIGHT ), "align right, span 1");
			category = new JComboBox<String>(userData.getCat());
				getUserdata();
			entryWindow.add(category, "grow, span 3 ");
	        
			JPanel blank = new JPanel();
				entryWindow.add(blank);
			JButton add = new JButton("Confirm");
				add.addActionListener(new AddAction());
			entryWindow.add(add, "align right");
			JButton cancel = new JButton("Cancel");
				cancel.addActionListener(new CancelAdd());
			entryWindow.add(cancel, "align left");
			
			entryWindow.pack();
			entryWindow.setLocationRelativeTo(frame);
			entryWindow.setVisible(true);
		}
		
		class AddAction implements ActionListener
		{
			
			private String check(String n, String d, String m)
			{
				if(n.isEmpty())
				{
					JOptionPane.showMessageDialog(null, "Can't leave 'Name' blank", "ERROR", JOptionPane.ERROR_MESSAGE);
					return "failed";
				}
				
				if(d.isEmpty())
				{
					JOptionPane.showMessageDialog(null, "Can't leave 'Date Purchased' blank", "ERROR", JOptionPane.ERROR_MESSAGE);
					return "failed";
				}
				
				try{
					Scanner j = new Scanner(d);
					j.useDelimiter("/");
					
					int month = j.nextInt();
					int day = j.nextInt();
					int year = j.nextInt();
					
					if((year+"").length() != 4)
					{
						JOptionPane.showMessageDialog(null, "Incorrect date input: " + d +"\nUse format mm/dd/yyyy", "ERROR", JOptionPane.ERROR_MESSAGE);				
						j.close();
						return "failed";
					}
					if(month < 1 || month > 12)
					{
						JOptionPane.showMessageDialog(null, "Invalid month: " + month, "ERROR", JOptionPane.ERROR_MESSAGE);				
						j.close();
						return "failed";
					}
					ArrayList<Integer> thirtyone = new ArrayList<Integer>();
					thirtyone.add(1);
					thirtyone.add(3);
					thirtyone.add(5);
					thirtyone.add(7);
					thirtyone.add(8);
					thirtyone.add(10);
					thirtyone.add(12);
					if(thirtyone.contains(month) && (day < 1 || day > 31))
					{
						JOptionPane.showMessageDialog(null, "Invalid day: " + day, "ERROR", JOptionPane.ERROR_MESSAGE);				
						j.close();
						return "failed";
					}
					else
					if(month == 2 && day < 1 || day > 29)
					{
						JOptionPane.showMessageDialog(null, "Invalid day: " + day, "ERROR", JOptionPane.ERROR_MESSAGE);				
						j.close();
						return "failed";						
					}
					
					if(day < 1 || day > 30)
					{
						JOptionPane.showMessageDialog(null, "Invalid day: " + day, "ERROR", JOptionPane.ERROR_MESSAGE);				
						j.close();
						return "failed";						
					}
					
					if(year > 2016)
					{
						JOptionPane.showMessageDialog(null, "Invalid day: " + day, "ERROR", JOptionPane.ERROR_MESSAGE);				
						j.close();
						return "failed";						
					}
					
					if(j.hasNext())
					{
						JOptionPane.showMessageDialog(null, "Incorrect date input: " + d +"\nUse format mm/dd/yyyy", "ERROR", JOptionPane.ERROR_MESSAGE);				
						j.close();
						return "failed";
					}
					j.close();

					return year +"-"+month+"-"+day;
					
				}catch(Exception e){
					JOptionPane.showMessageDialog(null, "Incorrect date input: " + d +"\nUse format mm/dd/yyyy", "ERROR", JOptionPane.ERROR_MESSAGE);				
					return "failed";
				}
			}

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String name = item.getText();
				String day = date.getText();
				String memo = description.getText();
				String price = cost.getText();
				String cat = (String) category.getSelectedItem();
				double pr = 0;
				
				if(cat.equals("Add Category"))
				{
					String newCat = JOptionPane.showInputDialog("Category Name");
					if(userData.getCatArray().contains(newCat))
					{
						JOptionPane.showMessageDialog(null, "Category " + newCat + " already exists", "ERROR", JOptionPane.ERROR_MESSAGE);
					}
					else
					if(newCat != null && !newCat.isEmpty())
					{
						try{
							Statement st = connector.getStatement();
							ResultSet rs = st.executeQuery("SELECT categories FROM users WHERE id = '" + userData.getId() +"'");
							rs.next();
							String catStr = rs.getString(1) + "," + newCat;
							st.execute("UPDATE users SET categories = '" + catStr + "' WHERE id = '" + userData.getId() + "'");
							getUserdata();
							category.addItem(newCat);
						}catch(Exception e){e.printStackTrace();}
					}
				}
				else
				{
					day = check(name,day,memo);
					while(!price.isEmpty() && price.charAt(0) == '$')
						price = price.substring(1);
					try{
						if(price.isEmpty())
							pr = 1/0;
						pr = Double.valueOf(price);
						Statement s = connector.getStatement();
						s.execute("INSERT INTO user" + userData.getId() + " (item,cost,category,desciption,purchase_date)"
								+ " VALUES ('" + name + "','" + pr + "','" + userData.getCatNum(cat) + "',\"" + memo + "\",'" + day + "')");
					entryWindow.dispose();
					}catch(ArithmeticException e){JOptionPane.showMessageDialog(null, "Incorrect price input: " + price +"\nUse format $xx.xx",null,JOptionPane.ERROR_MESSAGE);}
					catch(MysqlDataTruncation e){}
					catch(Exception e){e.printStackTrace();}
				}
			}
			
		}
		
		class CancelAdd implements ActionListener
		{

			@Override
			public void actionPerformed(ActionEvent e) {
				entryWindow.dispose();
			}
			
		}
		
	}
	
	class ActionTwo implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			try{
				ArrayList<FinanceEntry> entries = new ArrayList<FinanceEntry>();
				Statement st = connector.getStatement();
				ResultSet rs = st.executeQuery("SELECT * from user" + userData.getId());
				while(rs.next())
				{
					entries.add(new FinanceEntry(rs.getInt("id"),rs.getString("item"),rs.getDouble("cost"),
							rs.getInt("category"),rs.getString("desciption"),
							rs.getDate("purchase_date"),rs.getTimestamp("submittion_date")));
				}
				userData.setEntries(entries);
			}catch(Exception e){e.printStackTrace();}
			finally{
				updateOutput();
			}
		}
		
	}
	
	class ActionThree implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			setView(2);
			updateOutput();
		}
		
	}
	
	class ActionFour implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			setView(3);
			updateOutput();
		}
		
	}
}
