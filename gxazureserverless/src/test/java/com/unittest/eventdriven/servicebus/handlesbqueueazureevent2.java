package com.unittest.eventdriven.servicebus;

import com.genexus.GXBaseCollection;
import com.genexus.GXProcedure;
import com.genexus.ModelContext;
import com.genexus.SdtMessages_Message;

public final class handlesbqueueazureevent2 extends GXProcedure {
	public handlesbqueueazureevent2(int remoteHandle) {
		super(remoteHandle, new ModelContext(handlesbqueueazureevent2.class), "");
	}

	public handlesbqueueazureevent2(int remoteHandle,
									ModelContext context) {
		super(remoteHandle, context, "");
	}

	@SuppressWarnings("unchecked")
	public com.genexus.genexusserverlessapi.SdtEventMessageResponse executeUdp(com.genexus.genexusserverlessapi.SdtEventMessages aP0) {
		handlesbqueueazureevent2.this.aP1 = new com.genexus.genexusserverlessapi.SdtEventMessageResponse[]{new com.genexus.genexusserverlessapi.SdtEventMessageResponse()};
		execute_int(aP0, aP1);
		return aP1[0];
	}

	public void execute(com.genexus.genexusserverlessapi.SdtEventMessages aP0,
						com.genexus.genexusserverlessapi.SdtEventMessageResponse[] aP1) {
		execute_int(aP0, aP1);
	}

	private void execute_int(com.genexus.genexusserverlessapi.SdtEventMessages aP0,
							 com.genexus.genexusserverlessapi.SdtEventMessageResponse[] aP1) {
		handlesbqueueazureevent2.this.AV8EventMessages = aP0;
		handlesbqueueazureevent2.this.aP1 = aP1;
		initialize();
		/* GeneXus formulas */
		/* Output device settings */
		privateExecute();
	}

	protected void privateExecute() {
		System.out.println("START Service Bus Event received");

		AV16GXV1 = 1;
		while (AV16GXV1 <= AV8EventMessages.getgxTv_SdtEventMessages_Eventmessage().size()) {
			AV10EventMessage = (com.genexus.genexusserverlessapi.SdtEventMessage) ((com.genexus.genexusserverlessapi.SdtEventMessage) AV8EventMessages.getgxTv_SdtEventMessages_Eventmessage().elementAt(-1 + AV16GXV1));
			System.out.println("MessageData: " + AV10EventMessage.getgxTv_SdtEventMessage_Eventmessagedata());
			System.out.println("MessageId: " + AV10EventMessage.getgxTv_SdtEventMessage_Eventmessageid());
			System.out.println("MessageDate: " + AV10EventMessage.getgxTv_SdtEventMessage_Eventmessagedate());
			AV9EventMessageResponse.setgxTv_SdtEventMessageResponse_Handlefailure(AV10EventMessage.getgxTv_SdtEventMessage_Eventmessagedata() == "");
			if (AV9EventMessageResponse.getgxTv_SdtEventMessageResponse_Handlefailure()) {
				System.out.println("EventMessageData could not be parsed.");
				AV9EventMessageResponse.setgxTv_SdtEventMessageResponse_Errormessage(AV12OutMessages.toJSonString(false));
				returnInSub = true;
				cleanup();
				if (true) return;
			}
			AV16GXV1 = (int) (AV16GXV1 + 1);
		}
		System.out.println("END Service Bus Event received");
		cleanup();
	}

	protected void cleanup() {
		this.aP1[0] = handlesbqueueazureevent2.this.AV9EventMessageResponse;
		CloseOpenCursors();
		exitApp();
	}

	protected void CloseOpenCursors() {
	}

	/* Aggregate/select formulas */
	public void initialize() {
		AV9EventMessageResponse = new com.genexus.genexusserverlessapi.SdtEventMessageResponse(remoteHandle, context);
		AV15Pgmname = "";
		AV10EventMessage = new com.genexus.genexusserverlessapi.SdtEventMessage(remoteHandle, context);
		AV12OutMessages = new GXBaseCollection<SdtMessages_Message>(SdtMessages_Message.class, "Message", "GeneXus", remoteHandle);

		AV15Pgmname = "EventDriven.ServiceBus.HandleSBQueueAzureEvent2";
		/* GeneXus formulas. */
		AV15Pgmname = "EventDriven.ServiceBus.HandleSBQueueAzureEvent2";
		Gx_err = (short) (0);
	}

	private short Gx_err;
	private int AV16GXV1;
	private String AV15Pgmname;
	private boolean returnInSub;
	private com.genexus.genexusserverlessapi.SdtEventMessageResponse[] aP1;
	private GXBaseCollection<SdtMessages_Message> AV12OutMessages;
	private com.genexus.genexusserverlessapi.SdtEventMessages AV8EventMessages;
	private com.genexus.genexusserverlessapi.SdtEventMessage AV10EventMessage;
	private com.genexus.genexusserverlessapi.SdtEventMessageResponse AV9EventMessageResponse;
}

