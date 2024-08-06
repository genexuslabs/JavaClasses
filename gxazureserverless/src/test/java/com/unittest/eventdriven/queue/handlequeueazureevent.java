package com.unittest.eventdriven.queue;

import com.genexus.GXProcedure;
import com.genexus.GXutil;
import com.genexus.ModelContext;
import com.genexus.genexusserverlessapi.SdtEventMessageResponse;

public final class handlequeueazureevent extends GXProcedure {
	public handlequeueazureevent(int remoteHandle) {
		super(remoteHandle, new ModelContext(handlequeueazureevent.class), "");
	}

	public handlequeueazureevent(int remoteHandle, ModelContext context) {
		super(remoteHandle, context, "");
	}


	public void execute(String aP0, SdtEventMessageResponse[] aP1) {
		execute_int(aP0, aP1);
	}

	private void execute_int(String aP0, SdtEventMessageResponse[] aP1) {
		handlequeueazureevent.this.AV13RAWMessage = aP0;
		handlequeueazureevent.this.aP1 = aP1;
		initialize();
		/* GeneXus formulas */
		/* Output device settings */
		privateExecute();
	}

	protected void privateExecute() {
		System.out.println("START Queue Event received");
		System.out.println("Raw message: " +AV13RAWMessage);
		System.out.println("END Queue Event received");
		AV9EventMessageResponse.setgxTv_SdtEventMessageResponse_Handlefailure(!AV13RAWMessage.startsWith("Test message"));
		cleanup();
	}

	protected void cleanup() {
		this.aP1[0] = handlequeueazureevent.this.AV9EventMessageResponse;
		CloseOpenCursors();
		exitApp();
	}

	protected void CloseOpenCursors() {
	}

	/* Aggregate/select formulas */
	public void initialize() {
		AV9EventMessageResponse = new SdtEventMessageResponse(remoteHandle, context);
		AV16Pgmname = "";
		AV16Pgmname = "EventDriven.Queue.HandleQueueAzureEvent";
		/* GeneXus formulas. */
		AV16Pgmname = "EventDriven.Queue.HandleQueueAzureEvent";
		Gx_err = (short) (0);
	}

	private short Gx_err;
	private String AV13RAWMessage;
	private String AV16Pgmname;
	private SdtEventMessageResponse[] aP1;
	private SdtEventMessageResponse AV9EventMessageResponse;
}

