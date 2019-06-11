package bgu.spl.mics.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;
import bgu.spl.mics.RequestCompleted;

public class MessageBusImpl implements MessageBus {
	
	private List<MicroService> microServices;
	private Map<MicroService, List<Message>> microServiceMessageQueues;
	private Map<Class<? extends Message>, List<MicroService>> messageSubscriptions;
	private Map<Class<? extends Request>, Integer> requestNextMicroServiceIndices;
	private Map<Request, MicroService> sentRequestsMicroServices;
	
	private static class SingletonHolder {
        private static MessageBusImpl instance = new MessageBusImpl();
    }
	
    private MessageBusImpl() {
    	microServices = new ArrayList<MicroService>();
    	microServiceMessageQueues = new HashMap<MicroService, List<Message>>();
    	messageSubscriptions = new HashMap<Class<? extends Message>, List<MicroService>>();
    	requestNextMicroServiceIndices = new HashMap<Class<? extends Request>, Integer>();
    	sentRequestsMicroServices = new HashMap<Request, MicroService>();
    }
    
    public static MessageBusImpl getInstance() {
        return SingletonHolder.instance;
    }

	@Override
	public void subscribeRequest(Class<? extends Request> type, MicroService m) {
		synchronized(messageSubscriptions) {
			if (!messageSubscriptions.containsKey(type)) {
				messageSubscriptions.put(type, new ArrayList<MicroService>());
				requestNextMicroServiceIndices.put(type, 0);
			}
			
			messageSubscriptions.get(type).add(m);
		}
	}
	
	public Map<Class<? extends Message>, List<MicroService>> getMessageSubscriptions() {
		return messageSubscriptions;
	}
	
	public Map<Class<? extends Request>, Integer> getRequestNextMicroServiceIndices() {
		return requestNextMicroServiceIndices;
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		synchronized(messageSubscriptions) {
			if (!messageSubscriptions.containsKey(type)) {
				messageSubscriptions.put(type, new ArrayList<MicroService>());
			}
			
			messageSubscriptions.get(type).add(m);
		}
	}

	@Override
	public <T> void complete(Request<T> r, T result) {
		RequestCompleted<T> requestCompleted = new RequestCompleted<T>(r, result);
		
		MicroService sender = null;
		
		synchronized(sentRequestsMicroServices) {
			sender = sentRequestsMicroServices.get(r);
			sentRequestsMicroServices.remove(r);
		}
		synchronized(microServiceMessageQueues) {
			// If the sender unregistered, its queue was deleted before it received the requestCompleted message
			if (microServiceMessageQueues.containsKey(sender)) {
				microServiceMessageQueues.get(sender).add(requestCompleted);
			}
		}
		synchronized(sender) {
			sender.notifyAll();
		}
	}
	
	public Map<Request, MicroService> getSentRequestsMicroServices() {
		return sentRequestsMicroServices;
	}
	
	Map<MicroService, List<Message>> getMicroServiceMessageQueues() {
		return microServiceMessageQueues;
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		synchronized(messageSubscriptions) {
			if (messageSubscriptions.containsKey(b.getClass())) {
				for (MicroService microService : messageSubscriptions.get(b.getClass())) {
					synchronized(microServiceMessageQueues) {
						microServiceMessageQueues.get(microService).add(b);
					}
					synchronized(microService) {
						microService.notifyAll();
					}
				}
			}
		}
	}

	@Override
	public boolean sendRequest(Request<?> r, MicroService requester) {
		synchronized(messageSubscriptions) {
			if (messageSubscriptions.size() == 0 || !messageSubscriptions.containsKey(r.getClass())) {
				return false;
			}
			
			List<MicroService> subscriptionMicroServices = messageSubscriptions.get(r.getClass());
			if (subscriptionMicroServices.isEmpty()) {
				return false;
			}
			
			int nextMicroServiceIndex = requestNextMicroServiceIndices.get(r.getClass());
			
			if (nextMicroServiceIndex >= subscriptionMicroServices.size()) {
				nextMicroServiceIndex = 0;
			}
			
			int firstMicroServiceIndex = nextMicroServiceIndex;
			int microServiceIndex;
			MicroService microService;
			for (int i = firstMicroServiceIndex; i < firstMicroServiceIndex + subscriptionMicroServices.size(); i++) {
				microServiceIndex = i % subscriptionMicroServices.size();
				microService = subscriptionMicroServices.get(microServiceIndex);
				if (microService == requester) {
					continue;
				}
				
				synchronized(sentRequestsMicroServices) {
					sentRequestsMicroServices.put(r, requester);
				}
				
				nextMicroServiceIndex = (microServiceIndex + 1) % subscriptionMicroServices.size();
				requestNextMicroServiceIndices.put(r.getClass(), nextMicroServiceIndex);
				synchronized(microServiceMessageQueues) {
					microServiceMessageQueues.get(microService).add(r);
				}
				synchronized(microService) {
					microService.notifyAll();
				}
				
				return true;
			}
			return false;
		}
	}

	@Override
	public void register(MicroService m) {
		synchronized(microServices) {
			microServices.add(m);
			
			synchronized(microServiceMessageQueues) {
				microServiceMessageQueues.put(m, new ArrayList<Message>());
			}
		}
	}

	@Override
	public void unregister(MicroService m) {
		synchronized(microServices) {
			microServices.remove(m);
			
			synchronized(microServiceMessageQueues) {
				microServiceMessageQueues.remove(m);
			}
		}
		
		synchronized(messageSubscriptions) {
			List<MicroService> subscriptionMicroServices;
			
			for (Class<? extends Message> messageClass : messageSubscriptions.keySet()) {
				subscriptionMicroServices = messageSubscriptions.get(messageClass);
				
				if (Request.class.isAssignableFrom(messageClass)) { // Request subscriptions
					int indexOfRemovedMicroService = subscriptionMicroServices.indexOf(m);
					if (indexOfRemovedMicroService == -1) {
						continue;
					}
					subscriptionMicroServices.remove(m);
					int nextMicroServiceIndex = requestNextMicroServiceIndices.get(messageClass);
					
					// Keep the Round-Robin order:
					if (nextMicroServiceIndex > indexOfRemovedMicroService) {
						nextMicroServiceIndex--;
					}
					else if (nextMicroServiceIndex == indexOfRemovedMicroService && nextMicroServiceIndex == subscriptionMicroServices.size()) {
						nextMicroServiceIndex = 0;
					}
					
					requestNextMicroServiceIndices.put((Class<? extends Request>) messageClass, nextMicroServiceIndex);
				}
				else { // Broadcast subscriptions
					subscriptionMicroServices.remove(m);
				}
			}
		}	
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		List<Message> queue = microServiceMessageQueues.get(m);
		
		while (queue.size() == 0) {
			synchronized(m) {
				m.wait();
			}
		}
		
		Message message = queue.get(0);
		queue.remove(0);
		
		return message;
	}

}
