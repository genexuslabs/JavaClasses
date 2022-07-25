package com.unittest.eventdriven.dummy;

import com.genexus.GXProcedure;
import com.genexus.ModelContext;

public final class handlerruntimeexception extends GXProcedure {
	public handlerruntimeexception(int remoteHandle) {
		super(remoteHandle, new ModelContext(handlerruntimeexception.class), "");
	}

	public handlerruntimeexception(int remoteHandle, ModelContext context) {
		super(remoteHandle, context, "");
	}


	public void execute(String s, com.genexus.genexusserverlessapi.SdtEventMessageResponse[] aP1) {
		execute_int(s, aP1);
	}

	private void execute_int(String s, com.genexus.genexusserverlessapi.SdtEventMessageResponse[] aP1) {
		handlerruntimeexception.this.aP1 = aP1;
		initialize();
		/* GeneXus formulas */
		/* Output device settings */
		privateExecute();
	}

	private void privateExecute() {
		System.out.println("START Schedule Event received");
		throw new RuntimeException("Handler error");
	}

	protected void cleanup() {
		this.aP1[0] = handlerruntimeexception.this.AV9EventMessageResponse;
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

