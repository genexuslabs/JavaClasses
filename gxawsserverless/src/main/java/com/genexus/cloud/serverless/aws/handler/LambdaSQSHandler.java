package com.genexus.cloud.serverless.aws.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.cloud.serverless.*;
import com.genexus.cloud.serverless.aws.EventMessageProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class LambdaSQSHandler extends LambdaBaseHandler implements RequestHandler<SQSEvent, SQSBatchResponse> {

	public LambdaSQSHandler() throws Exception {
		super();
	}

	public LambdaSQSHandler(String className) throws Exception {
		super(className);
	}

	//https://docs.aws.amazon.com/en_gb/lambda/latest/dg/with-sqs.html
	@Override
	public SQSBatchResponse handleRequest(SQSEvent sqsEvent, Context context) {
		int msgCount = (int) sqsEvent.getRecords().stream().count();

		logger.debug("handleRequest started with #sqsItems: " + msgCount);

		EventMessages msgs = new EventMessages();

		for (SQSMessage sqsItem : sqsEvent.getRecords()) {
			logger.debug(String.format("Processing sqsEvent Message: %s", sqsItem.getMessageId()));
			EventMessage msg = new EventMessage();
			msg.setMessageId(sqsItem.getMessageId());
			msg.setMessageSourceType(EventMessageSourceType.QueueMessage);
			msg.setMessageDate(new Date());
			msg.setMessageData(sqsItem.getBody());
			List<EventMessageProperty> msgAtts = msg.getMessageProperties();

			for (Map.Entry<String, String> entry : sqsItem.getAttributes().entrySet()) {
				msgAtts.add(new EventMessageProperty(entry.getKey(), entry.getValue()));
			}
			for (Map.Entry<String, SQSEvent.MessageAttribute> entry : sqsItem.getMessageAttributes().entrySet()) {
				msgAtts.add(new EventMessageProperty(entry.getKey(), entry.getValue().getStringValue()));
			}
			msgAtts.add(new EventMessageProperty("eventSource", sqsItem.getEventSource()));
			msgAtts.add(new EventMessageProperty("eventSourceARN", sqsItem.getEventSourceArn()));
			msgAtts.add(new EventMessageProperty("awsRegion", sqsItem.getAwsRegion()));
			msgAtts.add(new EventMessageProperty("receiptHandle", sqsItem.getReceiptHandle()));
			msgAtts.add(new EventMessageProperty("md5Body", sqsItem.getMd5OfBody()));

			msgs.add(msg);
		}

		boolean wasHandled = false;
		String errorMessage;

		try {
			EventMessageResponse response = dispatchEventMessages(msgs, Helper.toJSONString(sqsEvent));
			wasHandled = response.isHandled();
			errorMessage = response.getErrorMessage();
		} catch (Exception e) {
			errorMessage = "HandleRequest execution error";
			logger.error(errorMessage, e);
		}

		if (!wasHandled) {
			logger.error(String.format("Messages were not handled. Marking all SQS Events as failed: %s", errorMessage));
			List<SQSBatchResponse.BatchItemFailure> failures = new ArrayList<>();
			//Assume all batch failed
			for (SQSMessage sqsItem : sqsEvent.getRecords()) {
				failures.add(new SQSBatchResponse.BatchItemFailure(sqsItem.getMessageId()));
			}
			return new SQSBatchResponse(failures);
		} else {
			logger.info("Event Message processed successfully");
		}

		return null;
	}
}

