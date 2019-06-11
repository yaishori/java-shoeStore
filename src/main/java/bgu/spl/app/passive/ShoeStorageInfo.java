package bgu.spl.app.passive;

public class ShoeStorageInfo {
	private final String shoeType;
	private int amountOnStorage;
	private int discountedAmount;
	
	public ShoeStorageInfo(String shoeType, int amountOnStorage) {
		this.shoeType = shoeType;
		this.amountOnStorage = amountOnStorage;
		this.discountedAmount = 0;
	}

	public String getShoeType() {
		return shoeType;
	}

	public int getAmountOnStorage() {
		return amountOnStorage;
	}

	public int getDiscountedAmount() {
		return discountedAmount;
	}

	public void setAmountOnStorage(int amountOnStorage) {
		this.amountOnStorage = amountOnStorage;
	}

	public void setDiscountedAmount(int discountedAmount) {
		this.discountedAmount = discountedAmount;
	}
	
	public void decreaseAmountOnStorage(int amount) {
		this.amountOnStorage -= amount;
	}

	public void decreaseDiscountedAmount(int amount) {
		this.discountedAmount -= amount;
	}
	
	public void increaseAmountOnStorage(int amount) {
		this.amountOnStorage += amount;
	}

	public void increaseDiscountedAmount(int amount) {
		this.discountedAmount += amount;
	}
	
	
}
