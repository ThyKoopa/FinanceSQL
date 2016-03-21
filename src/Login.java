import java.util.ArrayList;

public class Login {
	private int id;
	private String user;
	private ArrayList<String> categories;
	private ArrayList<FinanceEntry> entries;
	
	public Login(String user)
	{
		this.setUser(user);
		setCat(new ArrayList<String>());
	}
	
	public ArrayList<String> getCatArray()
	{
		return categories;
	}
	
	public int getCatNum(String str)
	{
		return categories.indexOf(str);
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setCat(ArrayList<String> categories) {
		this.categories = categories;
	}
	
	public String[] getCat()
	{
		String[] cat = new String[categories.size()+1];
		for(int x = 0 ; x < categories.size() ; x++)
		{
			cat[x] = categories.get(x);
		}
		cat[cat.length-1] = "Add Category";
		return cat;
	}
	
	public String getCat(int catNum)
	{
		return categories.get(catNum);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ArrayList<FinanceEntry> getEntries() {
		return entries;
	}

	public void setEntries(ArrayList<FinanceEntry> entries) {
		this.entries = entries;
	}
}
