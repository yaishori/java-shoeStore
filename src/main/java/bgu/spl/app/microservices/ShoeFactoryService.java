package bgu.spl.app.microservices;

import java.util.ArrayList;
import java.util.List;

import bgu.spl.app.Logger;
import bgu.spl.app.messages.ManufacturingOrderRequest;
import bgu.spl.app.messages.TerminateBroadcast;
import bgu.spl.app.messages.TickBroadcast;
import bgu.spl.app.passive.ManufacturingProgress;
import bgu.spl.app.passive.Receipt;
import bgu.spl.mics.MicroService;

public class ShoeFactoryService extends MicroService {
	private List<ManufacturingProgress> manufacturingProgresses;
	private int currentTick;

	public ShoeFactoryService(String name) {
		super(name);
		manufacturingProgresses = new ArrayList<ManufacturingProgress>();
		this.currentTick = 0;
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TerminateBroadcast.class, terminateBroadcast -> {
			terminate();
		});
		
		subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
			currentTick = tickBroadcast.getCurrentTick();
			synchronized(manufacturingProgresses){
				if (!manufacturingProgresses.isEmpty()) {
					boolean manufacture = false;
					
					ManufacturingProgress manufacturingProgress = manufacturingProgresses.get(0);
					if (manufacturingProgress.getRemainingShoesToManufacture() == 0) {
						Receipt receipt = new Receipt(getName(),
								                      "store",
								                      manufacturingProgress.getManufacturingOrderRequest().getShoeType(),
								                      false,
								                      currentTick,
								                      manufacturingProgress.getOrderRequestTick(),
								                      manufacturingProgress.getManufacturingOrderRequest().getAmount());
						Logger.getInstance().log(getName() + " finished manufacturing " + manufacturingProgress.getManufacturingOrderRequest().getAmount() +
						                         " pairs of " + manufacturingProgress.getManufacturingOrderRequest().getShoeType() + ".");
						complete(manufacturingProgress.getManufacturingOrderRequest(), receipt);
						manufacturingProgresses.remove(manufacturingProgress);
						
						if (!manufacturingProgresses.isEmpty()) {
							manufacturingProgress = manufacturingProgresses.get(0);
							manufacture = true;
						}
					}
					else {
						manufacture = true;
					}
					
					if (manufacture) {
						manufacturingProgress.decreaseRemainingShoesToManufacture();
						Logger.getInstance().log(getName() + " manufactured one pair of " + manufacturingProgress.getManufacturingOrderRequest().getShoeType() + ".");
					}
				}
			}
        });
		subscribeRequest(ManufacturingOrderRequest.class, manufacturingOrderRequest -> {
			ManufacturingProgress manufacturingProgress	= new ManufacturingProgress(manufacturingOrderRequest, 
					                                                                manufacturingOrderRequest.getAmount(),
					                                                                currentTick);
			synchronized(manufacturingProgresses) {
				manufacturingProgresses.add(manufacturingProgress);
			}
		});
	}
}
