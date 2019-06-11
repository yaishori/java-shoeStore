package bgu.spl.app.microservices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bgu.spl.app.Logger;
import bgu.spl.app.messages.ManufacturingOrderRequest;
import bgu.spl.app.messages.NewDiscountBroadcast;
import bgu.spl.app.messages.RestockRequest;
import bgu.spl.app.messages.TerminateBroadcast;
import bgu.spl.app.messages.TickBroadcast;
import bgu.spl.app.passive.DiscountSchedule;
import bgu.spl.app.passive.Store;
import bgu.spl.mics.MicroService;

public class ManagementService extends MicroService {
	private List<DiscountSchedule> discountSchedules;
	private int currentDiscountScheduleIndex;
	private int currentTick;
	
	private Map<String, Map<ManufacturingOrderRequest, List<RestockRequest>>> restockRequests;
	
	public ManagementService(String name, List<DiscountSchedule> discountSchedules) {
		super(name);
		this.discountSchedules = discountSchedules;
		Collections.sort(discountSchedules);
		this.currentDiscountScheduleIndex = 0;
		this.currentTick = 0;
		this.restockRequests = new HashMap<String, Map<ManufacturingOrderRequest, List<RestockRequest>>>();
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TerminateBroadcast.class, terminateBroadcast -> {
			terminate();
		});
		
		initializeDiscountBroadcast();
		
		initializeRestockRequestListener();
	}
	
	private void initializeDiscountBroadcast() {
		if(discountSchedules.size() == 0) {
			subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
				currentTick = tickBroadcast.getCurrentTick();
			});
		}
		else {
			subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
				currentTick = tickBroadcast.getCurrentTick();
				DiscountSchedule discountSchedule;
				for (;currentDiscountScheduleIndex < discountSchedules.size() && 
						discountSchedules.get(currentDiscountScheduleIndex).getTick() <= currentTick;
						currentDiscountScheduleIndex++) {
					discountSchedule = discountSchedules.get(currentDiscountScheduleIndex);
					
					if(discountSchedule.getTick() == currentTick) {
						int amountAdded = Store.getInstance().addDiscount(discountSchedule.getShoeType(), discountSchedule.getAmount());
						
						if (amountAdded > 0) {
							Logger.getInstance().log(getName() + " - Adding discount: " + discountSchedule.getShoeType() +
									                 " - " + amountAdded + " pairs");
						}
						
						sendBroadcast(new NewDiscountBroadcast(discountSchedule.getShoeType(), discountSchedule.getAmount()));
					}
				}
			});	
		}
	}
	
	private void initializeRestockRequestListener() {
		subscribeRequest(RestockRequest.class, restockRequest -> {
			String shoeType = restockRequest.getShoeType();
			boolean needToManufacture = true;
			
			synchronized(restockRequests) {
				Map<ManufacturingOrderRequest, List<RestockRequest>> shoeTypeRestockRequests = restockRequests.get(shoeType);
				List<RestockRequest> requests;
				
				// Check if the factory is already manufacturing this shoe type,
				// and if there are enough shoes, link this request with the manufacturing order request: 
				if (shoeTypeRestockRequests != null) {
					for (ManufacturingOrderRequest manufacturingOrderRequest : shoeTypeRestockRequests.keySet()) {
						requests = shoeTypeRestockRequests.get(manufacturingOrderRequest);
						if (requests.size() < manufacturingOrderRequest.getAmount()) {
							requests.add(restockRequest);
							needToManufacture = false;
							break;
						}
					}
				}
				else {
					shoeTypeRestockRequests = new HashMap<ManufacturingOrderRequest, List<RestockRequest>>();
					restockRequests.put(shoeType, shoeTypeRestockRequests);
				}
				
				requests = new ArrayList<RestockRequest>();
				requests.add(restockRequest);
				
				if (needToManufacture) {
					ManufacturingOrderRequest manufacturingOrderRequest = new ManufacturingOrderRequest(shoeType, (currentTick % 5) + 1);
					shoeTypeRestockRequests.put(manufacturingOrderRequest, requests);
					
					Logger.getInstance().log("Request manufacturing of " + manufacturingOrderRequest.getAmount() + " pairs of " + shoeType );
					sendManufacturingOrderRequest(manufacturingOrderRequest);
				}
				else {
					Logger.getInstance().log("Already manufacturing " + shoeType );
				}
			}
		});
	}
	
	private void sendManufacturingOrderRequest(ManufacturingOrderRequest manufacturingOrderRequest) {
		sendRequest(manufacturingOrderRequest, receipt -> {
			Store.getInstance().file(receipt);
			
			int amount = receipt.getAmountsold();
			int demand = 0;
			synchronized(restockRequests) {
				Map<ManufacturingOrderRequest, List<RestockRequest>> shoeTypeRestockRequests = restockRequests.get(receipt.getShoeType());
				demand = shoeTypeRestockRequests.get(manufacturingOrderRequest).size();
				for (RestockRequest request : shoeTypeRestockRequests.get(manufacturingOrderRequest)) {
					complete(request, true);
				}
				shoeTypeRestockRequests.remove(manufacturingOrderRequest);
			}
			
			if (demand < amount) {
				Store.getInstance().add(receipt.getShoeType(), amount - demand);
			}
		});
	}

}
