package com.genexus.cloud.aws.events;

import com.genexus.cloud.serverless.aws.handler.LambdaEventBridgeHandler;
import com.unittest.eventdriven.dummy.handlerruntimeexception;
import com.unittest.eventdriven.dummy.handlesimplenoparmsevent;
import com.unittest.eventdriven.dummy.handlesimplesqsevent;
import com.unittest.eventdriven.dummy.handlesimplesqsevent2;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestLambdaEventBridgeHandler {
	private static String SIMPLE_HANDLER = handlesimplesqsevent2.class.getName();
	private static String SIMPLE_RAW_HANDLER = handlesimplesqsevent.class.getName();
	private static String SIMPLE_NO_OUTPUT_HANDLER_EXCEPTION = handlerruntimeexception.class.getName();

	private Map<String, Object> createEvent() {
		Map<String, Object> evt = new HashMap<String, Object>();
		evt.put("id", "d043a3b9-7e7f-41d3-9656-6fe970b62888");
		evt.put("time", "2022-05-24T00:43:16Z");
		Map<String, Object> detail = new HashMap<>();
		detail.put("name", "test");
		evt.put("detail", detail);
		return evt;
	}

	@Test
	public void simpleEvent() throws Exception {
		LambdaEventBridgeHandler handler = new LambdaEventBridgeHandler(SIMPLE_HANDLER);
		String result = handler.handleRequest(createEvent(), new MockContext());
		Assert.assertEquals("{\"Handled\":true,\"ErrorMessage\":\"\"}", result);
	}

	@Test
	public void simpleEventRaw() throws Exception {
		LambdaEventBridgeHandler handler = new LambdaEventBridgeHandler(SIMPLE_RAW_HANDLER);
		String result = handler.handleRequest(createEvent(), new MockContext());
		Assert.assertEquals("{\"Handled\":true,\"ErrorMessage\":\"\"}", result);
	}

	@Test
	public void simpleEventNoParms() throws Exception {
		LambdaEventBridgeHandler handler = new LambdaEventBridgeHandler(handlesimplenoparmsevent.class.getName());
		String result = handler.handleRequest(createEvent(), new MockContext());
		Assert.assertEquals("{\"Handled\":true,\"ErrorMessage\":\"\"}", result);
	}


	@Test
	public void handlerExceptionError() throws Exception {
		LambdaEventBridgeHandler handler = new LambdaEventBridgeHandler(SIMPLE_NO_OUTPUT_HANDLER_EXCEPTION);

		Exception thrown = null;
		try {
			String result = handler.handleRequest(createEvent(), new MockContext());
		} catch (Exception e) {
			thrown = e;
		}
		Assert.assertNotNull(thrown);
	}

}
