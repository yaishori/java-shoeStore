package bgu.spl.app.json;

import java.util.List;

public class ConfigurationJson {
	private List<ShoeInfo> initialStorage;
	private Services services;
	
	public List<ShoeInfo> getInitialStorage() {
		return initialStorage;
	}

	public Services getServices() {
		return services;
	}
}
