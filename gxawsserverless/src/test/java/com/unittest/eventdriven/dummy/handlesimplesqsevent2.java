package com.unittest.eventdriven.dummy;

import com.genexus.GXBaseCollection;
import com.genexus.GXProcedure;
import com.genexus.ModelContext;
import com.genexus.SdtMessages_Message;

public final class handlesimplesqsevent2 extends GXProcedure {
	public handlesimplesqsevent2(int remoteHandle) {
		super(remoteHandle, new ModelContext(handlesimplesqsevent2.class), "");
	}

	public handlesimplesqsevent2(int remoteHandle,
								 ModelContext context) {
		super(remoteHandle, context, "");
	}

	@SuppressWarnings("unchecked")
	public com.genexus.genexusserverlessapi.SdtEventMessageResponse executeUdp(com.genexus.genexusserverlessapi.SdtEventMessages aP0) {
		handlesimplesqsevent2.this.aP1 = new com.genexus.genexusserverlessapi.SdtEventMessageResponse[]{new com.genexus.genexusserverlessapi.SdtEventMessageResponse()};
		execute_int(aP0, aP1);
		return aP1[0];
	}

	public void execute(com.genexus.genexusserverlessapi.SdtEventMessages aP0,
						com.genexus.genexusserverlessapi.SdtEventMessageResponse[] aP1) {
		execute_int(aP0, aP1);
	}

	private void execute_int(com.genexus.genexusserverlessapi.SdtEventMessages aP0,
							 com.genexus.genexusserverlessapi.SdtEventMessageResponse[] aP1) {
		handlesimplesqsevent2.this.AV8EventMessages = aP0;
		handlesimplesqsevent2.this.aP1 = aP1;
		initialize();
		/* GeneXus formulas */
		/* Output device settings */
		privateExecute();
	}

	private void privateExecute() {
		System.out.println("START EventBridge Event received");
		System.out.println("END EventBridge Event received");
		handlesimplesqsevent2.this.AV9EventMessageResponse.setgxTv_SdtEventMessageResponse_Handled(true);
		cleanup();
	}

	protected void cleanup() {
		this.aP1[0] = handlesimplesqsevent2.this.AV9EventMessageResponse;
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
		AV11UserSDT = new com.unittest.eventdriven.SdtUser(remoteHandle);
		AV15Pgmname = "EventDriven.Queue.HandleSimpleUserQueueEvent";
		/* GeneXus formulas. */
		AV15Pgmname = "EventDriven.Queue.HandleSimpleUserQueueEvent";
		Gx_err = (short) (0);
	}

	private short Gx_err;
	private int AV16GXV1;
	private String AV15Pgmname;
	private boolean returnInSub;
	private com.unittest.eventdriven.SdtUser AV11UserSDT;
	private com.genexus.genexusserverlessapi.SdtEventMessageResponse[] aP1;
	private GXBaseCollection<SdtMessages_Message> AV12OutMessages;
	private com.genexus.genexusserverlessapi.SdtEventMessages AV8EventMessages;
	private com.genexus.genexusserverlessapi.SdtEventMessage AV10EventMessage;
	private com.genexus.genexusserverlessapi.SdtEventMessageResponse AV9EventMessageResponse;
}

