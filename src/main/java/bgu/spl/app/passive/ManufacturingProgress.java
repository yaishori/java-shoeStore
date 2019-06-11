package bgu.spl.app.passive;

import bgu.spl.app.messages.ManufacturingOrderRequest;

public class ManufacturingProgress {
	private ManufacturingOrderRequest manufacturingOrderRequest;
	private int remainingShoesToManufacture;
	private final int orderRequestTick;
	
	public ManufacturingProgress(ManufacturingOrderRequest manufacturingOrderRequest, int remainingShoesToManufacture, int orderRequestTick) {
		this.manufacturingOrderRequest = manufacturingOrderRequest;
		this.remainingShoesToManufacture = remainingShoesToManufacture;
		this.orderRequestTick = orderRequestTick;
	}

	public int getRemainingShoesToManufacture() {
		return remainingShoesToManufacture;
	}

	public void decreaseRemainingShoesToManufacture() {
		this.remainingShoesToManufacture--;
	}

	public ManufacturingOrderRequest getManufacturingOrderRequest() {
		return manufacturingOrderRequest;
	}

	public int getOrderRequestTick() {
		return orderRequestTick;
	}

}
