package bgu.spl.app.passive;

public class DiscountSchedule implements Comparable<DiscountSchedule> {
	private String shoeType;
	private int tick;
	private int amount;
	
	public DiscountSchedule(String shoeType, int tick, int amount) {
		this.shoeType = shoeType;
		this.tick = tick;
		this.amount = amount;
	}
	
	public String getShoeType() {
		return shoeType;
	}
	
	public int getTick() {
		return tick;
	}
	
	public int getAmount() {
		return amount;
	}

	@Override
	public int compareTo(DiscountSchedule otherDiscountSchedule) {
		return tick - otherDiscountSchedule.tick;
	}
}
