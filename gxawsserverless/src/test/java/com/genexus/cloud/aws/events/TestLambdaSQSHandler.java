package com.genexus.cloud.aws.events;

import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.genexus.cloud.serverless.aws.handler.LambdaSQSHandler;
import com.unittest.eventdriven.queue.handlesimplesqsevent;
import com.unittest.eventdriven.queue.handlesimplesqsevent2;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class TestLambdaSQSHandler {
	private static String SIMPLE_HANDLER = handlesimplesqsevent2.class.getName();
	private static String SIMPLE_RAW_HANDLER = handlesimplesqsevent.class.getName();

	@Test
	public void TestSQSSimpleEvent() throws Exception {
		LambdaSQSHandler sqsHandler = new LambdaSQSHandler(SIMPLE_HANDLER);
		SQSEvent sqsEvent = new SQSEvent();
		createSqsEvent("1", "{\"UserId\":\"d2376a4c-86c3-461f-93cc-1c2e0174222b\", \"UserName\":\"John\"}", sqsEvent);
		SQSBatchResponse response = sqsHandler.handleRequest(sqsEvent, new MockContext());
		Assert.assertNull(response);
	}

	@Test
	public void TestSQSSimpleMultipleEvent() throws Exception {
		LambdaSQSHandler sqsHandler = new LambdaSQSHandler(SIMPLE_HANDLER);
		SQSEvent sqsEvent = new SQSEvent();
		createSqsEvent("1", "{\"UserId\":\"d2376a4c-86c3-461f-93cc-1c2e0174222b\", \"UserName\":\"John\"}", sqsEvent);
		createSqsEvent("2", "{\"UserId\":\"d2996a4c-86c3-461f-93cc-1c2e0174222b\", \"UserName\":\"John2\"}", sqsEvent);
		SQSBatchResponse response = sqsHandler.handleRequest(sqsEvent, new MockContext());
		Assert.assertNull(response);
	}

	@Test
	public void TestSQSRAWEvent() throws Exception {
		LambdaSQSHandler sqsHandler = new LambdaSQSHandler(SIMPLE_RAW_HANDLER);
		SQSEvent sqsEvent = new SQSEvent();
		createSqsEvent("1", "{\"UserId\":\"d2376a4c-86c3-461f-93cc-1c2e0174222b\", \"UserName\":\"John\"}", sqsEvent);
		SQSBatchResponse response = sqsHandler.handleRequest(sqsEvent, new MockContext());
		Assert.assertNull(response);
	}

	@Test
	public void TestSQSSimpleEventError() throws Exception {
		LambdaSQSHandler sqsHandler = new LambdaSQSHandler(SIMPLE_HANDLER);
		String msgId = "1";
		SQSEvent sqsEvent = new SQSEvent();
		createSqsEvent(msgId, "{sadadsa}", sqsEvent);
		SQSBatchResponse response = sqsHandler.handleRequest(sqsEvent, new MockContext());
		Assert.assertNotNull(response);
		Assert.assertEquals(1, response.getBatchItemFailures().size());
		Assert.assertEquals(msgId, response.getBatchItemFailures().get(0).getItemIdentifier());
	}

	@Test
	public void TestSQSSimpleMultipleEventError() throws Exception {
		LambdaSQSHandler sqsHandler = new LambdaSQSHandler(SIMPLE_HANDLER);

		SQSEvent sqsEvent = new SQSEvent();
		createSqsEvent("1", "{aa}", sqsEvent);
		createSqsEvent("2", "{aaaa}", sqsEvent);
		createSqsEvent("3", "{aaaaaaa}", sqsEvent);
		SQSBatchResponse response = sqsHandler.handleRequest(sqsEvent, new MockContext());
		Assert.assertNotNull(response);
		Assert.assertEquals(3, response.getBatchItemFailures().size());
		Assert.assertEquals("1", response.getBatchItemFailures().get(0).getItemIdentifier());
		Assert.assertEquals("2", response.getBatchItemFailures().get(1).getItemIdentifier());
		Assert.assertEquals("3", response.getBatchItemFailures().get(2).getItemIdentifier());
	}

	private void createSqsEvent(String msgId, String body, SQSEvent sqsEvent) {
		if (sqsEvent.getRecords() == null) {
			sqsEvent.setRecords(new ArrayList<>());
		}
		List<SQSEvent.SQSMessage> sqsMessagesList = sqsEvent.getRecords();
		SQSEvent.SQSMessage sqsMessage = new SQSEvent.SQSMessage();
		sqsMessage.setMessageId(msgId);
		sqsMessage.setEventSource("aws:sqs");
		sqsMessage.setAwsRegion("us-east-1");
		sqsMessage.setBody(body);
		sqsMessage.setEventSourceArn("arn:test");
		sqsMessage.setReceiptHandle("123123");
		sqsMessage.setAttributes(new HashMap<>());
		sqsMessage.setMessageAttributes(new HashMap<String, SQSEvent.MessageAttribute>());
		sqsMessagesList.add(sqsMessage);
	}
}
