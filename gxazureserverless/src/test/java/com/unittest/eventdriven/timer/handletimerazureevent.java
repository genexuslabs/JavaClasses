package com.unittest.eventdriven.timer;

import com.genexus.GXProcedure;
import com.genexus.GXutil;
import com.genexus.ModelContext;
import com.genexus.genexusserverlessapi.SdtEventMessageResponse;

public final class handletimerazureevent extends GXProcedure {
	public handletimerazureevent(int remoteHandle) {
		super(remoteHandle, new ModelContext(handletimerazureevent.class), "");
	}

	public handletimerazureevent(int remoteHandle, ModelContext context) {
		super(remoteHandle, context, "");
	}


	public void execute(String aP0, SdtEventMessageResponse[] aP1) {
		execute_int(aP0, aP1);
	}

	private void execute_int(String aP0, SdtEventMessageResponse[] aP1) {
		handletimerazureevent.this.AV13RAWMessage = aP0;
		handletimerazureevent.this.aP1 = aP1;
		initialize();
		/* GeneXus formulas */
		/* Output device settings */
		privateExecute();
	}

	protected void privateExecute() {
		System.out.println("START Timer Event received");
		System.out.println("Raw message: " + AV13RAWMessage);
		System.out.println("END Timer Event received");
		AV9EventMessageResponse.setgxTv_SdtEventMessageResponse_Handlefailure(false);
		cleanup();
	}

	protected void cleanup() {
		this.aP1[0] = handletimerazureevent.this.AV9EventMessageResponse;
		CloseOpenCursors();
		exitApp();
	}

	protected void CloseOpenCursors() {
	}

	/* Aggregate/select formulas */
	public void initialize() {
		AV9EventMessageResponse = new SdtEventMessageResponse(remoteHandle, context);
		AV16Pgmname = "";
		AV16Pgmname = "EventDriven.Timer.HandleTimerAzureEvent";
		/* GeneXus formulas. */
		AV16Pgmname = "EventDriven.Timer.HandleTimerAzureEvent";
		Gx_err = (short) (0);
	}

	private short Gx_err;
	private String AV13RAWMessage;
	private String AV16Pgmname;
	private SdtEventMessageResponse[] aP1;
	private SdtEventMessageResponse AV9EventMessageResponse;
}

