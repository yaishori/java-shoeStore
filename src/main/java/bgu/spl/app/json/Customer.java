package bgu.spl.app.json;

import java.util.List;
import java.util.Set;

public class Customer {
	private String name;
	private Set<String> wishList;
	private List<PurchaseInfo> purchaseSchedule;
	
	public String getName() {
		return name;
	}
	
	public Set<String> getWishList() {
		return wishList;
	}
	
	public List<PurchaseInfo> getPurchaseSchedule() {
		return purchaseSchedule;
	}
}
