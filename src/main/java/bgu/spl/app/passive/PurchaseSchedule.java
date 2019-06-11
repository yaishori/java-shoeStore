package bgu.spl.app.passive;

public class PurchaseSchedule implements Comparable<PurchaseSchedule> {
	private String shoeType;
	private int tick;
	
	public PurchaseSchedule(String shoeType, int tick) {
		this.shoeType = shoeType;
		this.tick = tick;
	}

	public String getShoeType() {
		return shoeType;
	}

	public int getTick() {
		return tick;
	}

	@Override
	public int compareTo(PurchaseSchedule otherPurchaseSchedule ) {
		return tick - otherPurchaseSchedule.tick;
	}
}
