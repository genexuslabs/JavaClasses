package com.unittest.eventdriven.queue;

import com.genexus.*;

public final class handlesimplesqsevent extends GXProcedure {
	public handlesimplesqsevent(int remoteHandle) {
		super(remoteHandle, new ModelContext(handlesimplesqsevent.class), "");
	}

	public handlesimplesqsevent(int remoteHandle, ModelContext context) {
		super(remoteHandle, context, "");
	}


	public void execute(String aP0, com.genexus.genexusserverlessapi.SdtEventMessageResponse[] aP1) {
		execute_int(aP0, aP1);
	}

	private void execute_int(String aP0, com.genexus.genexusserverlessapi.SdtEventMessageResponse[] aP1) {
		handlesimplesqsevent.this.AV13RAWMessage = aP0;
		handlesimplesqsevent.this.aP1 = aP1;
		initialize();
		/* GeneXus formulas */
		/* Output device settings */
		privateExecute();
	}

	protected void privateExecute() {
		System.out.println("START Queue Event received");
		System.out.println(AV13RAWMessage);
		System.out.println("END Queue Event received");
		System.out.println((boolean) ((GXutil.len(AV13RAWMessage) > 0)));
		AV9EventMessageResponse.setgxTv_SdtEventMessageResponse_Handlefailure(!AV13RAWMessage.startsWith("{\"records\":[{\"messageId\":\"1\",\"receiptHandle\":\"123123\",\"body\":\""));
		cleanup();
	}

	protected void cleanup() {
		this.aP1[0] = handlesimplesqsevent.this.AV9EventMessageResponse;
		CloseOpenCursors();
		exitApp();
	}

	protected void CloseOpenCursors() {
	}

	/* Aggregate/select formulas */
	public void initialize() {
		AV9EventMessageResponse = new com.genexus.genexusserverlessapi.SdtEventMessageResponse(remoteHandle, context);
		AV16Pgmname = "";
		AV16Pgmname = "EventDriven.Queue.HandleSimpleRAWSQSEvent";
		/* GeneXus formulas. */
		AV16Pgmname = "EventDriven.Queue.HandleSimpleRAWSQSEvent";
		Gx_err = (short) (0);
	}

	private short Gx_err;
	private String AV13RAWMessage;
	private String AV16Pgmname;
	private com.genexus.genexusserverlessapi.SdtEventMessageResponse[] aP1;
	private com.genexus.genexusserverlessapi.SdtEventMessageResponse AV9EventMessageResponse;
}

