package com.unittest.eventdriven.cosmosdb;

import com.genexus.GXBaseCollection;
import com.genexus.GXProcedure;
import com.genexus.ModelContext;
import com.genexus.SdtMessages_Message;

public final class handlecosmosdbazure extends GXProcedure {
	public handlecosmosdbazure(int remoteHandle) {
		super(remoteHandle, new ModelContext(handlecosmosdbazure.class), "");
	}

	public handlecosmosdbazure(int remoteHandle,
							   ModelContext context) {
		super(remoteHandle, context, "");
	}

	@SuppressWarnings("unchecked")
	public com.genexus.genexusserverlessapi.SdtEventMessageResponse executeUdp(com.genexus.genexusserverlessapi.SdtEventMessagesList aP0) {
		handlecosmosdbazure.this.aP1 = new com.genexus.genexusserverlessapi.SdtEventMessageResponse[]{new com.genexus.genexusserverlessapi.SdtEventMessageResponse()};
		execute_int(aP0, aP1);
		return aP1[0];
	}

	public void execute(com.genexus.genexusserverlessapi.SdtEventMessagesList aP0,
						com.genexus.genexusserverlessapi.SdtEventMessageResponse[] aP1) {
		execute_int(aP0, aP1);
	}

	private void execute_int(com.genexus.genexusserverlessapi.SdtEventMessagesList aP0,
							 com.genexus.genexusserverlessapi.SdtEventMessageResponse[] aP1) {
		handlecosmosdbazure.this.AV17EventMessagesList = aP0;
		handlecosmosdbazure.this.aP1 = aP1;
		initialize();
		/* GeneXus formulas */
		/* Output device settings */
		privateExecute();
	}

	protected void privateExecute() {
		System.out.println("START CosmosDB Event received");
		System.out.println(AV17EventMessagesList.toJSonString(false, true));
		AV16GXV1 = 1;
		while ( AV16GXV1 <= AV17EventMessagesList.getgxTv_SdtEventMessagesList_Items().size() )
		{
			AV8EventMessage = (String)AV17EventMessagesList.getgxTv_SdtEventMessagesList_Items().elementAt(-1+AV16GXV1) ;
			System.out.println(AV8EventMessage);
			//httpContext.GX_msglist.addItem(AV8EventMessage);

			AV9EventMessageResponse.setgxTv_SdtEventMessageResponse_Handlefailure(!AV11UserSDT.fromJSonString(AV8EventMessage));
			if (AV9EventMessageResponse.getgxTv_SdtEventMessageResponse_Handlefailure()) {
				System.out.println("Event Message Data could not be parsed: " + AV8EventMessage);
				System.out.println(AV12OutMessages.toJSonString(false));
				AV9EventMessageResponse.setgxTv_SdtEventMessageResponse_Errormessage(AV12OutMessages.toJSonString(false));
				returnInSub = true;
				cleanup();
				if (true) return;
			}
			System.out.println("UserSDT Processed OK: " + AV11UserSDT.toJSonString(false, true));
			AV16GXV1 = (int)(AV16GXV1+1) ;
		}

		System.out.println("END CosmosDB Event received");
		cleanup();
	}

	protected void cleanup() {
		this.aP1[0] = handlecosmosdbazure.this.AV9EventMessageResponse;
		CloseOpenCursors();
		exitApp();
	}

	protected void CloseOpenCursors() {
	}

	/* Aggregate/select formulas */
	public void initialize() {
		AV8EventMessage = "";
		AV9EventMessageResponse = new com.genexus.genexusserverlessapi.SdtEventMessageResponse(remoteHandle, context);
		AV15Pgmname = "";
		AV10EventMessage = new com.genexus.genexusserverlessapi.SdtEventMessage(remoteHandle, context);
		AV12OutMessages = new GXBaseCollection<SdtMessages_Message>(SdtMessages_Message.class, "Message", "GeneXus", remoteHandle);
		AV11UserSDT = new com.unittest.eventdriven.SdtUser(remoteHandle);
		AV15Pgmname = "EventDriven.CosmosDB.HandleCosmosDBEvent";
		/* GeneXus formulas. */
		AV15Pgmname = "EventDriven.Queue.CosmosDB.HandleCosmosDBEvent";
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
	private com.genexus.genexusserverlessapi.SdtEventMessagesList AV17EventMessagesList;
	private com.genexus.genexusserverlessapi.SdtEventMessageResponse AV9EventMessageResponse;
	private String AV8EventMessage ;
}

