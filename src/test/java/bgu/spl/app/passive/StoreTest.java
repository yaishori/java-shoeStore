package bgu.spl.app.passive;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StoreTest {
	private Store store;

	@Before
	public void setUp() throws Exception {
		store = Store.getInstance();
	}

	@After
	public void tearDown() throws Exception {
		store.clear();
	}

	@Test
	public void testGetInstance() {
		Store instance = Store.getInstance();
		
		assertNotNull(instance);
		assertEquals(store, instance);
	}

	@Test
	public void testLoad() {
		ShoeStorageInfo[] shoeStorageInfos = new ShoeStorageInfo[3];
		shoeStorageInfos[0] = new ShoeStorageInfo("Shoe type 1", 3);
		shoeStorageInfos[1] = new ShoeStorageInfo("Shoe type 2", 5);
		shoeStorageInfos[2] = new ShoeStorageInfo("Shoe type 3", 2);
		
		store.load(shoeStorageInfos);
		
		assertEquals("Wrong initial amount", 3, store.getAmount(shoeStorageInfos[0].getShoeType()));
		assertEquals("Wrong initial amount", 5, store.getAmount(shoeStorageInfos[1].getShoeType()));
		assertEquals("Wrong initial amount", 2, store.getAmount(shoeStorageInfos[2].getShoeType()));
		
		assertEquals("Wrong initial discounted amount", 0, store.getDiscountedAmount(shoeStorageInfos[0].getShoeType()));
		assertEquals("Wrong initial discounted amount", 0, store.getDiscountedAmount(shoeStorageInfos[1].getShoeType()));
		assertEquals("Wrong initial discounted amount", 0, store.getDiscountedAmount(shoeStorageInfos[2].getShoeType()));
	}

	@Test
	public void testTake() {
		assertEquals("Shoe is not in stock", BuyResult.NOT_IN_STOCK, store.take("Shoe type 1", false));
		assertEquals("Shoe is not in stock", BuyResult.NOT_IN_STOCK, store.take("Shoe type 1", true));
		
		ShoeStorageInfo[] shoeStorageInfos = new ShoeStorageInfo[3];
		shoeStorageInfos[0] = new ShoeStorageInfo("Shoe type 1", 10);
		shoeStorageInfos[1] = new ShoeStorageInfo("Shoe type 2", 5);
		shoeStorageInfos[2] = new ShoeStorageInfo("Shoe type 3", 2);
		
		store.load(shoeStorageInfos);
		
		assertEquals("Shoe is not on discount", BuyResult.NOT_ON_DISCOUNT, store.take("Shoe type 1", true));
		assertEquals("Shoe is on stock", BuyResult.REGULAR_PRICE, store.take("Shoe type 1", false));
		
		store.addDiscount("Shoe type 1", 3);
		assertEquals("Shoe is on discount", BuyResult.DISCOUNTED_PRICE, store.take("Shoe type 1", true));
		assertEquals("Shoe is on discount", BuyResult.DISCOUNTED_PRICE, store.take("Shoe type 1", true));
		assertEquals("Shoe is on discount", BuyResult.DISCOUNTED_PRICE, store.take("Shoe type 1", true));
		assertEquals("Shoe is not in stock", BuyResult.NOT_ON_DISCOUNT, store.take("Shoe type 1", true));
	}

	@Test
	public void testDecreaseDiscountedAmount() {
		assertFalse("Shoe is not on discount", store.decreaseDiscountedAmount("Shoe type 1"));
		
		ShoeStorageInfo[] shoeStorageInfos = new ShoeStorageInfo[3];
		shoeStorageInfos[0] = new ShoeStorageInfo("Shoe type 1", 3);
		shoeStorageInfos[1] = new ShoeStorageInfo("Shoe type 2", 5);
		shoeStorageInfos[2] = new ShoeStorageInfo("Shoe type 3", 2);
		
		store.load(shoeStorageInfos);
		
		assertFalse("Shoe is not on discount", store.decreaseDiscountedAmount("Shoe type 1"));
		
		store.addDiscount("Shoe type 1", 1);
		
		assertTrue("Shoe is on discount", store.decreaseDiscountedAmount("Shoe type 1"));
	}

	@Test
	public void testAdd() {
		store.add("Shoe type 1", 3);
		
		assertEquals("Wrong amount", 3, store.getAmount("Shoe type 1"));
	}

	@Test
	public void testAddDiscount() {
		store.add("Shoe type 1", 3);
		
		assertEquals("Wrong initial discounted amount", 0, store.getDiscountedAmount("Shoe type 1"));
		
		store.addDiscount("Shoe type 1", 5);
		
		assertEquals("Wrong discounted amount", 3, store.getDiscountedAmount("Shoe type 1"));
	}

	@Test
	public void testFile() {
		assertTrue("Receipts is not empty", store.getReceipts().isEmpty());
		
		store.file(new Receipt("Seller 1", "Customer 1", "Shoe type 1", true, 1, 1, 1));
		
		assertEquals("Receipts size should be 1", 1, store.getReceipts().size());
		
		store.file(new Receipt("Seller 2", "Customer 2", "Shoe type 2", false, 2, 2, 3));
		
		assertEquals("Receipts size should be 2", 2, store.getReceipts().size());
		
		Receipt receipt = store.getReceipts().get(0);
		
		assertEquals("Seller should be Seller 1", "Seller 1", receipt.getSeller());
		
		receipt = store.getReceipts().get(1);
		
		assertEquals("Issued Tick should be Seller 2", 2, receipt.getIssuedTick());
	}
}
