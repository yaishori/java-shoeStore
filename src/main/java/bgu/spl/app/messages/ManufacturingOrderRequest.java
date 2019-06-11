package bgu.spl.app.messages;

import bgu.spl.app.passive.Receipt;
import bgu.spl.mics.Request;

public class ManufacturingOrderRequest implements Request<Receipt> {
	private String shoeType;
	private int amount;
	
	public ManufacturingOrderRequest(String shoeType, int amount) {
		this.shoeType = shoeType;
		this.amount = amount;
	}
	
	public String getShoeType() {
		return shoeType;
	}
	
	public int getAmount() {
		return amount;
	}
}
