package bgu.spl.app.microservices;

import bgu.spl.app.Logger;
import bgu.spl.app.messages.PurchaseOrderRequest;
import bgu.spl.app.messages.RestockRequest;
import bgu.spl.app.messages.TerminateBroadcast;
import bgu.spl.app.messages.TickBroadcast;
import bgu.spl.app.passive.BuyResult;
import bgu.spl.app.passive.Receipt;
import bgu.spl.app.passive.Store;
import bgu.spl.mics.MicroService;

public class SellingService extends MicroService {
	private int currentTick;

	public SellingService(String name) {
		super(name);
		this.currentTick = 0;
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TerminateBroadcast.class, terminateBroadcast -> {
			terminate();
		});
		
		subscribeBroadcast(TickBroadcast.class, tickBroadcast -> {
			currentTick = tickBroadcast.getCurrentTick();
        });
		subscribeRequest(PurchaseOrderRequest.class, purchaseOrderRequest -> {
			BuyResult buyResult = Store.getInstance().take(purchaseOrderRequest.getShoeType(), purchaseOrderRequest.isOnlyDiscount());
			if (buyResult == BuyResult.REGULAR_PRICE || buyResult == BuyResult.DISCOUNTED_PRICE){
				Receipt receipt = new Receipt(getName(),
						                      purchaseOrderRequest.getCustomer(), 
						                      purchaseOrderRequest.getShoeType(), 
						                      buyResult == BuyResult.DISCOUNTED_PRICE, 
						                      currentTick, 
						                      currentTick, 
						                      1);
				Logger.getInstance().log(getName() + " sold a pair of " + purchaseOrderRequest.getShoeType() + " to " + purchaseOrderRequest.getCustomer() +
						                 (buyResult == BuyResult.DISCOUNTED_PRICE ? " - Discount given" : " - Regular price"));
				Store.getInstance().file(receipt);
				complete(purchaseOrderRequest, receipt);
			}
			else if (buyResult == BuyResult.NOT_ON_DISCOUNT){
				Logger.getInstance().log(getName() + " sold a pair of " + purchaseOrderRequest.getShoeType() + " to " + purchaseOrderRequest.getCustomer() +
						                 " - Regular price");
				complete(purchaseOrderRequest, null);
			}
			else { // Not in stock
				final int requestTick = currentTick;
				Logger.getInstance().log(getName() + " sent a restock request for a pair of " + purchaseOrderRequest.getShoeType());
				boolean success = sendRequest(new RestockRequest(purchaseOrderRequest.getShoeType()), restockSuccess -> {
					if (restockSuccess) {
						boolean discount = Store.getInstance().decreaseDiscountedAmount(purchaseOrderRequest.getShoeType());
						
						Receipt receipt = new Receipt(getName(),
								                      purchaseOrderRequest.getCustomer(), 
								                      purchaseOrderRequest.getShoeType(), 
								                      discount, 
								                      requestTick, 
								                      currentTick, 
								                      1);
						Logger.getInstance().log(getName() + " received a pair of " + purchaseOrderRequest.getShoeType() +
								                 " (restock succeeded) and sold a pair of " + purchaseOrderRequest.getShoeType() + " to " +
								                 purchaseOrderRequest.getCustomer() +
								                 (discount ? " - Discount given" : " - Regular price"));
						Store.getInstance().file(receipt);
						complete(purchaseOrderRequest, receipt);
					}
	            });
				
				if (!success) { // No service subscribed to RestockRequest
					complete(purchaseOrderRequest, null);
				}
			}
        });
		

	}

}
