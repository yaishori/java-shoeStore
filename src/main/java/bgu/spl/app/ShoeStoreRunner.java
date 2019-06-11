package bgu.spl.app;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;

import bgu.spl.app.json.ConfigurationJson;
import bgu.spl.app.json.Customer;
import bgu.spl.app.json.DiscountInfo;
import bgu.spl.app.json.PurchaseInfo;
import bgu.spl.app.json.ShoeInfo;
import bgu.spl.app.microservices.ManagementService;
import bgu.spl.app.microservices.SellingService;
import bgu.spl.app.microservices.ShoeFactoryService;
import bgu.spl.app.microservices.TimerService;
import bgu.spl.app.microservices.WebsiteClientService;
import bgu.spl.app.passive.DiscountSchedule;
import bgu.spl.app.passive.PurchaseSchedule;
import bgu.spl.app.passive.ShoeStorageInfo;
import bgu.spl.app.passive.Store;
import bgu.spl.mics.MicroService;

public class ShoeStoreRunner {

	public static void main(String[] args) {
		String jsonFileName = args[0];
		ConfigurationJson configurationJson = readConfigurationJson(jsonFileName);
		
		initializeStoreStorage(configurationJson.getInitialStorage());
		List<MicroService> services = new ArrayList<MicroService>();
		List<Thread> serviceThreads = new ArrayList<Thread>();
		Thread serviceThread;
		List<DiscountSchedule> discountSchedules = new ArrayList<DiscountSchedule>();
		DiscountSchedule discountSchedule;
		for (DiscountInfo discountInfo : configurationJson.getServices().getManager().getDiscountSchedule()) {
			discountSchedule = new DiscountSchedule(discountInfo.getShoeType(), discountInfo.getTick(), discountInfo.getAmount());
			discountSchedules.add(discountSchedule);
		}
		ManagementService manager = new ManagementService("manager", discountSchedules);
		services.add(manager);
		serviceThread = new Thread(manager);
		serviceThreads.add(serviceThread);
		serviceThread.start();
		
		ShoeFactoryService factory;
		for (int i = 1; i <= configurationJson.getServices().getFactories(); i++) {
			factory = new ShoeFactoryService("factory " + i);
			services.add(factory);
			serviceThread = new Thread(factory);
			serviceThreads.add(serviceThread);
			serviceThread.start();
		}
		
		SellingService seller;
		for (int i = 1; i <= configurationJson.getServices().getSellers(); i++) {
			seller = new SellingService("seller " + i);
			services.add(seller);
			serviceThread = new Thread(seller);
			serviceThreads.add(serviceThread);
			serviceThread.start();
		}
		
		WebsiteClientService customer;
		List<PurchaseSchedule> purchaseSchedules;
		PurchaseSchedule purchaseSchedule;
		for (Customer jsonCustomer : configurationJson.getServices().getCustomers()) {
			purchaseSchedules = new ArrayList<PurchaseSchedule>();
			for (PurchaseInfo purchaseInfo : jsonCustomer.getPurchaseSchedule()) {
				purchaseSchedule = new PurchaseSchedule(purchaseInfo.getShoeType(), purchaseInfo.getTick());
				purchaseSchedules.add(purchaseSchedule);
			}
			customer = new WebsiteClientService(jsonCustomer.getName(), purchaseSchedules, jsonCustomer.getWishList());
			services.add(customer);
			serviceThread = new Thread(customer);
			serviceThreads.add(serviceThread);
			serviceThread.start();
		}
		
		Logger.getInstance().log("Waiting for all services except the timer to be initialized.");
		
		for (MicroService service : services) {
			try {
				service.getInitializedSemaphore().acquire();
			}
			catch (InterruptedException exception) {
			}
		}
		
		Logger.getInstance().log("All services except the timer are initialized.");
		
		TimerService timer = new TimerService("timer",
				                              configurationJson.getServices().getTime().getSpeed(),
				                              configurationJson.getServices().getTime().getDuration());
		services.add(timer);
		serviceThread = new Thread(timer);
		serviceThreads.add(serviceThread);
		serviceThread.start();
		
		for (Thread thread : serviceThreads) {
			try {
				thread.join();
			}
			catch (InterruptedException exception) {
			}
		}
		
		Store.getInstance().print();
	}

	private static void initializeStoreStorage(List<ShoeInfo> initialStorage) {
		ShoeStorageInfo[] storage = new ShoeStorageInfo[initialStorage.size()];
		ShoeStorageInfo shoeStorageInfo;
		
		ShoeInfo shoeInfo;
		
		for (int i = 0; i < initialStorage.size(); i++) {
			shoeInfo = initialStorage.get(i);
			shoeStorageInfo = new ShoeStorageInfo(shoeInfo.getShoeType(), shoeInfo.getAmount());
			storage[i] = shoeStorageInfo;
		}
		
		Store.getInstance().load(storage);
	}
	
	private static ConfigurationJson readConfigurationJson(String jsonFileName) {
		String jsonString = readJsonFile(jsonFileName);
		Gson gson = new Gson();
		ConfigurationJson configurationJson = gson.fromJson(jsonString, ConfigurationJson.class);
		
		return configurationJson;
	}

	private static String readJsonFile(String jsonFileName) {
		StringBuffer result = new StringBuffer();
		String line = null;
		BufferedReader bufferedReader = null;

		try {
			bufferedReader = new BufferedReader(new FileReader(jsonFileName));

			while ((line = bufferedReader.readLine()) != null) {
				result.append(line.trim());
			}

			bufferedReader.close();
		}
		catch (FileNotFoundException exception) {
			System.out.println("Unable to open file '" + jsonFileName + "'");
		}
		catch (IOException exception) {
			System.out.println("Error reading file '" + jsonFileName + "'");
		}
		finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException exception) {
				}
			}
		}
		
		return result.toString();
	}
}
