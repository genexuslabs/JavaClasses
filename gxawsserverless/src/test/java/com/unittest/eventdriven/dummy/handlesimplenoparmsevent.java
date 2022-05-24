package com.unittest.eventdriven.dummy;

import com.genexus.GXProcedure;
import com.genexus.ModelContext;

public final class handlesimplenoparmsevent extends GXProcedure {
	public handlesimplenoparmsevent(int remoteHandle) {
		super(remoteHandle, new ModelContext(handlesimplenoparmsevent.class), "");
	}

	public handlesimplenoparmsevent(int remoteHandle, ModelContext context) {
		super(remoteHandle, context, "");
	}


	public void execute() {
		execute_int();
	}

	private void execute_int() {
		initialize();
		/* GeneXus formulas */
		/* Output device settings */
		privateExecute();
	}

	private void privateExecute() {
		System.out.println("START EventBridge Event received with no parms");
		System.out.println("END EventBridge Event");
		cleanup();
	}

	protected void cleanup() {
		CloseOpenCursors();
		exitApp();
	}

	protected void CloseOpenCursors() {
	}

	/* Aggregate/select formulas */
	public void initialize() {

	}

}

