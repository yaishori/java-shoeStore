package bgu.spl.app.messages;

import bgu.spl.mics.Request;

public class RestockRequest implements Request<Boolean> {
	private String shoeType;
	
	public RestockRequest(String shoeType) {
		this.shoeType = shoeType;
	}
	
	public String getShoeType() {
		return shoeType;
	}
}
