package com.genexus.cloud.serverless.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.genexus.GxUserType;
import com.genexus.ModelContext;

import java.lang.reflect.Constructor;


public class LambdaSQSHandler extends LambdaBaseHandler implements RequestHandler<SQSEvent, Void> {

	public LambdaSQSHandler() throws Exception {
		super();
	}

	@Override
	public Void handleRequest(SQSEvent sqsEvent, Context context)  {
		logger.debug("handleRequest start");

		for(SQSMessage msg : sqsEvent.getRecords()){
			logger.debug(String.format("Processing sqsEvent Message: %s", msg.getMessageId()));

			try {
				ModelContext modelContext = new ModelContext( entryPointClass );
				Object[] parameters = new Object[2];

				Class<?> eventMessagesClass = Class.forName(MESSAGE_INPUT_CLASS_NAME);
				Constructor constructor = eventMessagesClass.getConstructor(ModelContext.class);
				GxUserType inputMessage = (GxUserType) constructor.newInstance(modelContext);

				com.genexus.db.DynamicExecute.dynamicExecute(modelContext, -1, entryPointClass, entryPointClass.getName(), parameters);
			}
			catch (Exception e) {
				logger.error("HandleRequest failed: " + entryPointClass.getName(), e);
			}

		}
		return null;
	}
}

