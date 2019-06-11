package bgu.spl.app.passive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Store {
	private Map<String, ShoeStorageInfo> shoeStorageInfos;
	private List<Receipt> receipts;
	
	private static class SingletonHolder {
        private static Store instance = new Store();
    }
	
    private Store() {
    	this.shoeStorageInfos = new HashMap<String, ShoeStorageInfo>();
    	this.receipts = new ArrayList<Receipt>();
    }
    
    public static Store getInstance() {
        return SingletonHolder.instance;
    }
    
    public void load(ShoeStorageInfo[] storage) {
    	for (ShoeStorageInfo shoeStorageInfo : storage) {
    		shoeStorageInfos.put(shoeStorageInfo.getShoeType(), shoeStorageInfo);
    	}
    }
    
    public void clear() {
    	shoeStorageInfos.clear();
    }
    
    public int getAmount(String shoeType) {
    	if (!shoeStorageInfos.containsKey(shoeType)) {
    		return 0;
    	}
    	
    	return shoeStorageInfos.get(shoeType).getAmountOnStorage();
    }
    
    public int getDiscountedAmount(String shoeType) {
    	if (!shoeStorageInfos.containsKey(shoeType)) {
    		return 0;
    	}
    	
    	return shoeStorageInfos.get(shoeType).getDiscountedAmount();
    }

	public BuyResult take(String shoeType, boolean onlyDiscount) {
		ShoeStorageInfo shoeStorageInfo = shoeStorageInfos.get(shoeType);
		// If the shoe is not on stock, add it with an amount of 0:
		if (shoeStorageInfo == null) {
			synchronized(shoeStorageInfos) {
				shoeStorageInfo = new ShoeStorageInfo(shoeType, 0);
				shoeStorageInfos.put(shoeType, shoeStorageInfo);
			}
		}
		
		synchronized(shoeStorageInfo) {
			if (shoeStorageInfo.getAmountOnStorage() == 0) {
				return BuyResult.NOT_IN_STOCK;
			}
			else if (onlyDiscount && shoeStorageInfo.getDiscountedAmount() == 0) {
				return BuyResult.NOT_ON_DISCOUNT;
			}
			else {
				shoeStorageInfo.decreaseAmountOnStorage(1);
				
				if (shoeStorageInfo.getDiscountedAmount() == 0) {
					return BuyResult.REGULAR_PRICE;
				}
				else {
					shoeStorageInfo.decreaseDiscountedAmount(1);
					
					return BuyResult.DISCOUNTED_PRICE;
				}
			}
		}
    }
	
	public boolean decreaseDiscountedAmount(String shoeType) {
		ShoeStorageInfo shoeStorageInfo = shoeStorageInfos.get(shoeType);
		// If the shoe is not on stock, we return false (no discount):
    	if (shoeStorageInfo == null) {
    		return false;
    	}
		
		synchronized(shoeStorageInfo) {
			if (shoeStorageInfo.getDiscountedAmount() > 0) {
				shoeStorageInfo.decreaseDiscountedAmount(1);
				return true;
			}
			else {
				return false;
			}
		}
	}
    
    public void add(String shoeType, int amount) {
    	ShoeStorageInfo shoeStorageInfo = shoeStorageInfos.get(shoeType);
    	// If the shoe is not on stock, add it with an amount of 0:
		if (shoeStorageInfo == null) {
			synchronized(shoeStorageInfos) {
				shoeStorageInfo = new ShoeStorageInfo(shoeType, 0);
				shoeStorageInfos.put(shoeType, shoeStorageInfo);
			}
		}
    	
		synchronized(shoeStorageInfo){
			shoeStorageInfo.increaseAmountOnStorage(amount);
		}
    }

    public int addDiscount(String shoeType , int amount ) {
    	ShoeStorageInfo shoeStorageInfo = shoeStorageInfos.get(shoeType);
    	// If the shoe is not on stock, we add no discount:
    	if (shoeStorageInfo == null) {
    		return 0;
    	}
    	
		synchronized(shoeStorageInfo){
			int notDiscountedAmount = shoeStorageInfo.getAmountOnStorage() - shoeStorageInfo.getDiscountedAmount();
			
			if (amount > notDiscountedAmount) {
				amount = notDiscountedAmount;
			}
			if (amount > 0) {
				shoeStorageInfo.increaseDiscountedAmount(amount);
			}
		}
		
    	return amount;
    }

    public void file(Receipt receipt) {
    	synchronized(receipts) {
    		receipts.add(receipt);
    	}
    }
    
    public List<Receipt> getReceipts() {
    	return receipts;
    }

    public void print() {
    	ShoeStorageInfo shoeStorageInfo;
    	System.out.println("Shoe Storage Info");
    	System.out.println("-----------------");
    	for (String shoeType : shoeStorageInfos.keySet()){
    		shoeStorageInfo = shoeStorageInfos.get(shoeType);
    		System.out.println("Shoe type:         " + shoeType);
    		System.out.println("Amount:            " + shoeStorageInfo.getAmountOnStorage());
    		System.out.println("Discounted amount: " + shoeStorageInfo.getDiscountedAmount());
    		System.out.println();
    	}
    	System.out.println("Receipts");
    	System.out.println("--------");
    	for(Receipt receipt : receipts){
    		System.out.println("Seller:       " + receipt.getSeller());
    		System.out.println("Customer:     " + receipt.getCustomer());
    		System.out.println("Shoe type:    " + receipt.getShoeType());
    		System.out.println("Discount:     " + receipt.isDiscount());
    		System.out.println("Issued tick:  " + receipt.getIssuedTick());
    		System.out.println("Request tick: " + receipt.getRequestTick());
    		System.out.println("Amount sold:  " + receipt.getAmountsold());
    		System.out.println();
    		
    	}
    	
    	
    }
}
