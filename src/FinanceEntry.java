import java.sql.Date;
import java.sql.Timestamp;

/*
   new FinanceEntry(rs.getInt("id"),rs.getString("item"),rs.getDouble("cost"),
							rs.getInt("category"),rs.getString("description"),
							rs.getDate("purchase_date"),rs.getTimestamp("submittion_date"));
*/


public class FinanceEntry implements Comparable<FinanceEntry>
{

	private int id,category;
	private String item,desc;
	private double cost;
	private Date purchase;
	private Timestamp submittion;
	
	public FinanceEntry(int id,String item,double cost, int category,String desc,Date purchase,Timestamp submittion )
	{
		this.id = id;
		this.item = item;
		this.cost = cost;
		this.category = category;
		this.desc = desc;
		this.purchase = purchase;
		this.submittion = submittion;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public Date getPurchase() {
		return purchase;
	}

	public void setPurchase(Date purchase) {
		this.purchase = purchase;
	}

	public Timestamp getSubmittion() {
		return submittion;
	}

	public void setSubmittion(Timestamp submittion) {
		this.submittion = submittion;
	}

	@Override
	public int compareTo(FinanceEntry o) {
		return this.getPurchase().toString().compareTo(o.getPurchase().toString());
	}
	
}
