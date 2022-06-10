package com.genexus.messaging.queue.aws;

import com.genexus.messaging.queue.IQueue;
import com.genexus.messaging.queue.model.SimpleQueueMessage;
import com.genexus.messaging.queue.model.DeleteMessageResult;
import com.genexus.messaging.queue.model.MessageQueueOptions;
import com.genexus.messaging.queue.model.SendMessageResult;

import com.genexus.services.ServiceConfigurationException;
import com.genexus.services.ServiceSettingsReader;
import com.genexus.util.GXProperties;
import com.genexus.util.GXProperty;
import com.genexus.util.GXService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.*;

public class AWSQueue implements IQueue {
	private static Logger logger = LogManager.getLogger(AWSQueue.class);

	public static String Name = "AWSSQS";

	private SqsClient sqsClient;

	public static String ACCESS_KEY = "ACCESS_KEY";
	public static String SECRET_ACCESS_KEY = "SECRET_KEY";
	public static String REGION = "REGION";
	public static String QUEUE_URL = "QUEUE_URL";

	private String accessKey;
	private String secret;
	private String awsRegion;
	private String queueURL;
	private boolean isFIFO;

	private static String MESSSAGE_GROUP_ID = "MessageGroupId";
	private static String MESSSAGE_DEDUPLICATION_ID = "MessageDeduplicationId";

	public AWSQueue() throws ServiceConfigurationException {
		initialize(new GXService());
	}

	public AWSQueue(GXService service) throws ServiceConfigurationException {
		initialize(service);
	}

	private void initialize(GXService providerService) throws ServiceConfigurationException {
		ServiceSettingsReader serviceSettings = new ServiceSettingsReader("QUEUE", Name, providerService);

		queueURL = serviceSettings.getEncryptedPropertyValue(QUEUE_URL, "");
		accessKey = serviceSettings.getEncryptedPropertyValue(ACCESS_KEY, "");
		secret = serviceSettings.getEncryptedPropertyValue(SECRET_ACCESS_KEY, "");
		awsRegion = serviceSettings.getEncryptedPropertyValue(REGION, "");
		isFIFO = queueURL.endsWith(".fifo");

		boolean bUseIAM = !serviceSettings.getPropertyValue("USE_IAM", "", "").isEmpty() || (accessKey.equals("") && secret.equals(""));

		if (bUseIAM) {
			sqsClient = SqsClient.builder()
				.region(Region.of(awsRegion))
				.credentialsProvider(ProfileCredentialsProvider.create())
				.build();
		} else {
			AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
				accessKey,
				secret);
			sqsClient = SqsClient.builder()
				.region(Region.of(awsRegion))
				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
				.build();
		}
	}

	@Override
	public Integer getQueueLength() {
		String attName = "ApproximateNumberOfMessages";
		GetQueueAttributesRequest request = GetQueueAttributesRequest.builder()
			.queueUrl(queueURL)
			.attributeNamesWithStrings(attName)
			.build();
		GetQueueAttributesResponse response = sqsClient.getQueueAttributes(request);
		String queueLengthS = response.attributesAsStrings().getOrDefault(attName, "0");
		return Integer.parseInt(queueLengthS);
	}

	@Override
	public SendMessageResult sendMessage(SimpleQueueMessage simpleQueueMessage) {
		return sendMessage(simpleQueueMessage, new MessageQueueOptions());
	}

	@Override
	public SendMessageResult sendMessage(SimpleQueueMessage simpleQueueMessage, MessageQueueOptions messageQueueOptions) {
		List<SendMessageResult> result = sendMessagesImpl(Arrays.asList(simpleQueueMessage), messageQueueOptions);
		return result.get(0);
	}

	@Override
	public List<SendMessageResult> sendMessages(List<SimpleQueueMessage> simpleQueueMessages, MessageQueueOptions messageQueueOptions) {
		return sendMessagesImpl(simpleQueueMessages, messageQueueOptions);
	}

	private List<SendMessageResult> sendMessagesImpl(List<SimpleQueueMessage> simpleQueueMessages, MessageQueueOptions messageQueueOptions) {
		List<SendMessageResult> sendMessageResultList = new ArrayList<>();
		List<SendMessageBatchRequestEntry> entryList = new ArrayList<>();

		if (simpleQueueMessages.size() == 0)
			return sendMessageResultList;

		for (SimpleQueueMessage msg : simpleQueueMessages) {
			Map<String, MessageAttributeValue> msgProps = new HashMap<>();
			for (int i = 0; i < msg.getMessageAttributes().count(); i++) {
				GXProperty prop = msg.getMessageAttributes().item(i);
				MessageAttributeValue msgAtt = MessageAttributeValue.builder()
					.stringValue(prop.getValue())
					.dataType("String")
					.build();
				msgProps.put(prop.getKey(), msgAtt);
			}

			SendMessageBatchRequestEntry.Builder entry = SendMessageBatchRequestEntry.builder()
				.messageBody(msg.getMessageBody())
				.id(msg.getMessageId())
				.messageAttributes(msgProps);

			if (isFIFO) {
				String msgDeduplicationId = msg.getMessageAttributes().get(MESSSAGE_DEDUPLICATION_ID);
				String msgGroupId = msg.getMessageAttributes().get(MESSSAGE_GROUP_ID);
				if (msgDeduplicationId != null) {
					entry.messageDeduplicationId(msgDeduplicationId);
				}
				if (msgGroupId != null) {
					entry.messageGroupId(msgGroupId);
				}
			}
			if (messageQueueOptions.getDelaySeconds() > 0) {
				entry.delaySeconds(messageQueueOptions.getDelaySeconds());
			}
			entryList.add(entry.build());
		}

		SendMessageBatchResponse responseBatch = sqsClient.sendMessageBatch(SendMessageBatchRequest.builder()
			.queueUrl(queueURL)
			.entries(entryList)
			.build());

		for (SendMessageBatchResultEntry msg : responseBatch.successful()) {
			sendMessageResultList.add(new SendMessageResult() {{
				setMessageId(msg.id());
				setMessageServerId(msg.messageId());
				setMessageSentStatus(SendMessageResult.SENT);
			}});
		}
		for (BatchResultErrorEntry msg : responseBatch.failed()) {
			logger.error(String.format("SendMessage '%s' was rejected by AWS SQS server. Message: %s", msg.id(), msg.message()));
			sendMessageResultList.add(new SendMessageResult() {{
				setMessageId(msg.id());
				setMessageSentStatus(SendMessageResult.FAILED);
			}});
		}

		return sendMessageResultList;
	}

	@Override
	public List<SimpleQueueMessage> getMessages(MessageQueueOptions messageQueueOptions) {
		List<SimpleQueueMessage> receivedSimpleQueueMessages = new ArrayList<>();

		ReceiveMessageRequest.Builder receiveMessageRequest = ReceiveMessageRequest.builder().queueUrl(queueURL);

		if (messageQueueOptions.getMaxNumberOfMessages() > 0) {
			receiveMessageRequest.maxNumberOfMessages((int) messageQueueOptions.getMaxNumberOfMessages());
		}
		if (messageQueueOptions.getVisibilityTimeout() > 0) {
			receiveMessageRequest.visibilityTimeout(messageQueueOptions.getVisibilityTimeout());
		}
		if (messageQueueOptions.getWaitTimeout() > 0) {
			receiveMessageRequest.waitTimeSeconds(messageQueueOptions.getWaitTimeout());
		}
		if (messageQueueOptions.getReceiveRequestAttemptId().length() > 0) {
			receiveMessageRequest.receiveRequestAttemptId(messageQueueOptions.getReceiveRequestAttemptId());
		}
		if (messageQueueOptions.isReceiveMessageAttributes()) {
			receiveMessageRequest.messageAttributeNames("All");
			receiveMessageRequest.attributeNamesWithStrings("All");

		}

		ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(receiveMessageRequest.build());

		if (receiveMessageResponse.hasMessages()) {
			for (Message m : receiveMessageResponse.messages()) {
				receivedSimpleQueueMessages.add(setupSimpleQueueMessage(m));
			}
		}
		return receivedSimpleQueueMessages;
	}

	private SimpleQueueMessage setupSimpleQueueMessage(Message response) {
		SimpleQueueMessage simpleQueueMessage = new SimpleQueueMessage() {{
			setMessageId(response.messageId());
			setMessageHandleId(response.receiptHandle());
			setMessageBody(response.body());
		}};
		GXProperties messageAtts = simpleQueueMessage.getMessageAttributes();

		messageAtts.add("MD5OfMessageAttributes", response.md5OfMessageAttributes());
		messageAtts.add("MD5OfBody", response.md5OfBody());

		for (Map.Entry<String, String> entry : response.attributesAsStrings().entrySet()) {
			messageAtts.add(entry.getKey(), entry.getValue());
		}

		for (Map.Entry<String, MessageAttributeValue> entry : response.messageAttributes().entrySet()) {
			messageAtts.add(entry.getKey(), entry.getValue().stringValue());
		}

		return simpleQueueMessage;
	}

	@Override
	public DeleteMessageResult deleteMessage(String messageHandleId) {
		if (messageHandleId.length() == 0)
			return new DeleteMessageResult();
		return deleteMessagesImpl(Arrays.asList(messageHandleId)).get(0);
	}

	@Override
	public List<DeleteMessageResult> deleteMessages(List<String> messageHandleIds) {
		return deleteMessagesImpl(messageHandleIds);
	}

	private List<DeleteMessageResult> deleteMessagesImpl(List<String> messageHandleIds) {
		if (messageHandleIds.size() == 0)
			return new ArrayList<>();

		List<DeleteMessageResult> deleteMessageResults = new ArrayList<>();

		List<DeleteMessageBatchRequestEntry> deleteMessageEntries = new ArrayList<>();
		for (String msgId: messageHandleIds) {
			deleteMessageEntries.add(DeleteMessageBatchRequestEntry.builder()
				.id(java.util.UUID.randomUUID().toString())
				.receiptHandle(msgId)
				.build());
		}
		DeleteMessageBatchRequest.Builder deleteMessageRequest = DeleteMessageBatchRequest.builder()
			.queueUrl(queueURL)
			.entries(deleteMessageEntries);

		DeleteMessageBatchResponse deleteMessageBatchResponse = sqsClient.deleteMessageBatch(deleteMessageRequest.build());

		for (DeleteMessageBatchResultEntry msg:deleteMessageBatchResponse.successful()) {
			deleteMessageResults.add(new DeleteMessageResult() {{
				setMessageId(msg.id());
				setMessageDeleteStatus(DeleteMessageResult.DELETED);
			}});
		}

		for (BatchResultErrorEntry msg:deleteMessageBatchResponse.failed()) {
			logger.error(String.format("DeleteMessage '%s' was rejected by AWS SQS server: Message: %s", msg.id(), msg.message()));
			deleteMessageResults.add(new DeleteMessageResult() {{
				setMessageId(msg.id());
				setMessageDeleteStatus(DeleteMessageResult.FAILED);
			}});
		}
		return  deleteMessageResults;
	}

	@Override
	public boolean purge() {
		PurgeQueueRequest purgeQueueRequest = PurgeQueueRequest.builder().queueUrl(queueURL).build();
		try {
			sqsClient.purgeQueue(purgeQueueRequest);
			return true;
		} catch (PurgeQueueInProgressException e) {
			logger.info("Failed to purge queue because a purge queue operation is in progress", e);
		}
		return false;
	}
}
