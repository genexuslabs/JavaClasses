package com.unittest.eventdriven.dummy;

import com.genexus.GXProcedure;
import com.genexus.ModelContext;

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

	private void privateExecute() {
		System.out.println("START EventBridge Event received");
		System.out.println("END EventBridge Event received");
		handlesimplesqsevent.this.AV9EventMessageResponse.setgxTv_SdtEventMessageResponse_Handled(true);
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

