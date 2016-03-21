import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import net.miginfocom.swing.MigLayout;

public class Connector {
	private Connection 	connection;
	private Statement 	statement;
	private ResultSet 	resultSet;
	private JDialog		dialog;
	private String	ip,port,db,user,pass;
	public Connector()
	{
		ip = "73.76.67.116";
		port = "3737";
		db = "finance";
		user = "finance";
		pass = "csiv";
		setDialog();
		connection = connect();
		
		try {
			statement = connection.createStatement();
		} catch (SQLException e) {e.printStackTrace();}
	} 
	
	private Connection connect()
	{
		try{
			dialog.setVisible(true);
			Connection c = DriverManager.getConnection("jdbc:mysql://" + ip + ":"
					+ port + "/" + db + "?user=" + user + "&password=" + pass +"&useSSL=false");
			dialog.setVisible(false);
			return c;
		}catch(Exception e){
			JOptionPane.showMessageDialog(null, "Can't connect to server.", "ERROR", JOptionPane.WARNING_MESSAGE);
			System.exit(1);
		}
		
		return null;
	}
	
	private void setDialog(){
		dialog = new JDialog();
		dialog.setLocationRelativeTo(null);
		dialog.setSize(175,150);
		JTextPane message = new JTextPane();
			message.setLayout(new MigLayout("fill, wrap 1"));
			message.setText("Connecting to server (" + ip + ")");
			message.setEditable(false);
			SimpleAttributeSet att = new SimpleAttributeSet();
			StyleConstants.setAlignment(att, StyleConstants.ALIGN_CENTER);
			message.setParagraphAttributes(att, true);
		dialog.add(message);
		dialog.pack();
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public Statement getStatement() {
		return statement;
	}

	public void setStatement(Statement statement) {
		this.statement = statement;
	}

	public ResultSet getResultSet() {
		return resultSet;
	}

	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}
}
