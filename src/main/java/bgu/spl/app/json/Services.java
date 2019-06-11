package bgu.spl.app.json;

import java.util.List;

public class Services {
	private Time time;
	private Manager manager;
	private int factories;
	private int sellers;
	private List<Customer> customers;
	
	public Time getTime() {
		return time;
	}
	
	public Manager getManager() {
		return manager;
	}
	
	public int getFactories() {
		return factories;
	}
	
	public int getSellers() {
		return sellers;
	}
	
	public List<Customer> getCustomers() {
		return customers;
	}
}
