package bgu.spl.app.microservices;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import bgu.spl.app.Logger;
import bgu.spl.app.messages.NewDiscountBroadcast;
import bgu.spl.app.messages.PurchaseOrderRequest;
import bgu.spl.app.messages.TerminateBroadcast;
import bgu.spl.app.messages.TickBroadcast;
import bgu.spl.app.passive.DiscountSchedule;
import bgu.spl.app.passive.PurchaseSchedule;
import bgu.spl.mics.MicroService;

public class WebsiteClientService extends MicroService {
	
	private List<PurchaseSchedule> purchaseSchedules;
	private Set<String> wishList;
	private int currentPurchaseScheduleIndex;
	private int currentTick;

	public WebsiteClientService(String name, List<PurchaseSchedule> purchaseSchedules, Set<String> wishList) {
		super(name);
		this.purchaseSchedules = purchaseSchedules;
		Collections.sort(this.purchaseSchedules);
		this.wishList = wishList;
		this.currentPurchaseScheduleIndex = 0;
		this.currentTick = 0;
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TerminateBroadcast.class, terminateBroadcast -> {
			terminate();
		});
		
		if(purchaseSchedules.size() == 0) {
			subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
				currentTick = tickBroadcast.getCurrentTick();
			});
		}
		else {
			subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
				currentTick = tickBroadcast.getCurrentTick();
				
				for (;currentPurchaseScheduleIndex < purchaseSchedules.size() && 
					  purchaseSchedules.get(currentPurchaseScheduleIndex).getTick() <= currentTick;
					  currentPurchaseScheduleIndex++) {
					final PurchaseSchedule purchaseSchedule = purchaseSchedules.get(currentPurchaseScheduleIndex);
					
					if(purchaseSchedule.getTick() == currentTick) {
						int index = currentPurchaseScheduleIndex;
						Logger.getInstance().log(getName() + " wants to buy " + purchaseSchedule.getShoeType());
						boolean success = sendRequest(new PurchaseOrderRequest(purchaseSchedule.getShoeType(), false, getName()), receipt -> {
							if (receipt != null && index == purchaseSchedules.size() - 1 && wishList.isEmpty()) {
								terminate();
							}
						});
					}
				}
			});	
		}
		
		subscribeBroadcast(NewDiscountBroadcast.class, newDiscountBroadcast -> {
			if (wishList.contains(newDiscountBroadcast.getShoeType())) {
				boolean success = sendRequest(new PurchaseOrderRequest(newDiscountBroadcast.getShoeType(), true, getName()), receipt -> {
					if (receipt != null) {
						wishList.remove(newDiscountBroadcast.getShoeType());
						if (wishList.isEmpty() && currentPurchaseScheduleIndex == purchaseSchedules.size()) {
							terminate();
						}
					}
				});
			}
		});
	}
}
