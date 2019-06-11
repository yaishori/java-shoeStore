package bgu.spl.app.messages;

import bgu.spl.app.passive.Receipt;
import bgu.spl.mics.Request;

public class PurchaseOrderRequest implements Request<Receipt> {
	private String shoeType;
	private boolean onlyDiscount;
	private String customer;
	
	public PurchaseOrderRequest(String shoeType, boolean onlyDiscount, String customer) {
		this.shoeType = shoeType;
		this.onlyDiscount = onlyDiscount;
		this.customer = customer;
	}

	public String getShoeType() {
		return shoeType;
	}

	public boolean isOnlyDiscount() {
		return onlyDiscount;
	}

	public String getCustomer() {
		return customer;
	}
	
}
