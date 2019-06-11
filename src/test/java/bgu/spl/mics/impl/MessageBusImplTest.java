package bgu.spl.mics.impl;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bgu.spl.app.messages.RestockRequest;
import bgu.spl.app.messages.TickBroadcast;
import bgu.spl.mics.Message;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Request;

public class MessageBusImplTest {

	private MessageBusImpl messageBus;
	
	@Before
	public void setUp() throws Exception {
		messageBus = MessageBusImpl.getInstance();
	}

	@After
	public void tearDown() throws Exception {
		messageBus.getMessageSubscriptions().clear();
		messageBus.getRequestNextMicroServiceIndices().clear();
		messageBus.getSentRequestsMicroServices().clear();
		messageBus.getMicroServiceMessageQueues().clear();
	}

	@Test
	public void testGetInstance() {
		MessageBusImpl instance = MessageBusImpl.getInstance();
		
		assertEquals(instance, messageBus);
	}

	@Test
	public void testSubscribeRequest() {
		Map<Class<? extends Message>, List<MicroService>> messageSubscriptions = messageBus.getMessageSubscriptions();
		assertTrue(messageSubscriptions.isEmpty());
		
		MicroService microService = new MicroService("Name") {
			@Override
			protected void initialize() {
			}
		};
		
		messageBus.subscribeRequest(RestockRequest.class, microService);
		
		assertFalse(messageSubscriptions.isEmpty());
		assertTrue(messageSubscriptions.containsKey(RestockRequest.class));
		assertTrue(messageSubscriptions.get(RestockRequest.class).contains(microService));
	}

	@Test
	public void testSubscribeBroadcast() {
		Map<Class<? extends Message>, List<MicroService>> messageSubscriptions = messageBus.getMessageSubscriptions();
		assertTrue(messageSubscriptions.isEmpty());
		
		MicroService microService = new MicroService("Name") {
			@Override
			protected void initialize() {
			}
		};
		
		messageBus.register(microService);
		
		messageBus.subscribeBroadcast(TickBroadcast.class, microService);
		
		assertFalse(messageSubscriptions.isEmpty());
		assertTrue(messageSubscriptions.containsKey(TickBroadcast.class));
		assertTrue(messageSubscriptions.get(TickBroadcast.class).contains(microService));
	}

	@Test
	public void testComplete() {
		MicroService microService = new MicroService("Name") {
			@Override
			protected void initialize() {
			}
		};
		
		MicroService sender = new MicroService("requester") {
			@Override
			protected void initialize() {
			}
		};
		
		messageBus.register(microService);
		messageBus.register(sender);
		
		messageBus.subscribeRequest(RestockRequest.class, microService);
		
		assertTrue(messageBus.getSentRequestsMicroServices().isEmpty());
		
		RestockRequest request = new RestockRequest("Show Type 1");
		
		
		
		messageBus.sendRequest(request, sender);
		
		assertEquals(1, messageBus.getSentRequestsMicroServices().size());
		
		messageBus.complete(request, true);
		
		assertTrue(messageBus.getSentRequestsMicroServices().isEmpty());
		
		assertEquals(1, messageBus.getMicroServiceMessageQueues().get(sender).size());
	}

	@Test
	public void testSendBroadcast() {
		fail("Not yet implemented");
	}

	@Test
	public void testSendRequest() {
		fail("Not yet implemented");
	}

	@Test
	public void testRegister() {
		fail("Not yet implemented");
	}

	@Test
	public void testUnregister() {
		fail("Not yet implemented");
	}

	@Test
	public void testAwaitMessage() {
		fail("Not yet implemented");
	}

}
