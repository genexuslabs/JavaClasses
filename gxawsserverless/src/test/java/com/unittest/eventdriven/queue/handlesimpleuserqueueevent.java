package com.unittest.eventdriven.queue ;
import com.unittest.*;
import java.sql.*;
import com.genexus.db.*;
import com.genexus.*;
import com.genexus.search.*;

public final  class handlesimpleuserqueueevent extends GXProcedure
{
	public handlesimpleuserqueueevent( int remoteHandle )
	{
		super( remoteHandle , new ModelContext( handlesimpleuserqueueevent.class ), "" );
	}

	public handlesimpleuserqueueevent( int remoteHandle ,
									   ModelContext context )
	{
		super( remoteHandle , context, "" );
	}

	@SuppressWarnings("unchecked")
	public com.genexus.genexusserverlessapi.SdtEventMessageResponse executeUdp( com.genexus.genexusserverlessapi.SdtEventMessages aP0 )
	{
		handlesimpleuserqueueevent.this.aP1 = new com.genexus.genexusserverlessapi.SdtEventMessageResponse[] {new com.genexus.genexusserverlessapi.SdtEventMessageResponse()};
		execute_int(aP0, aP1);
		return aP1[0];
	}

	public void execute( com.genexus.genexusserverlessapi.SdtEventMessages aP0 ,
						 com.genexus.genexusserverlessapi.SdtEventMessageResponse[] aP1 )
	{
		execute_int(aP0, aP1);
	}

	private void execute_int( com.genexus.genexusserverlessapi.SdtEventMessages aP0 ,
							  com.genexus.genexusserverlessapi.SdtEventMessageResponse[] aP1 )
	{
		handlesimpleuserqueueevent.this.AV8EventMessages = aP0;
		handlesimpleuserqueueevent.this.aP1 = aP1;
		initialize();
		/* GeneXus formulas */
		/* Output device settings */
		privateExecute();
	}

	private void privateExecute( )
	{
		System.out.println("START Queue Event received") ;
		System.out.println(AV8EventMessages.toJSonString(false, true)) ;

		AV16GXV1 = 1 ;
		while ( AV16GXV1 <= AV8EventMessages.getgxTv_SdtEventMessages_Eventmessage().size() )
		{
			AV10EventMessage = (com.genexus.genexusserverlessapi.SdtEventMessage)((com.genexus.genexusserverlessapi.SdtEventMessage)AV8EventMessages.getgxTv_SdtEventMessages_Eventmessage().elementAt(-1+AV16GXV1));
			System.out.println("Processing: "+AV10EventMessage.toJSonString(false, true)) ;
			AV9EventMessageResponse.setgxTv_SdtEventMessageResponse_Handled( AV11UserSDT.fromJSonString(AV10EventMessage.getgxTv_SdtEventMessage_Eventmessagedata(), AV12OutMessages) );
			if ( ! AV9EventMessageResponse.getgxTv_SdtEventMessageResponse_Handled() )
			{
				System.out.println("EventMessageData could not be parsed: "+AV10EventMessage.getgxTv_SdtEventMessage_Eventmessagedata()) ;
				System.out.println(AV12OutMessages.toJSonString(false)) ;
				AV9EventMessageResponse.setgxTv_SdtEventMessageResponse_Errormessage( AV12OutMessages.toJSonString(false) );
				returnInSub = true;
				cleanup();
				if (true) return;
			}
			System.out.println("UserSDT Processed OK: "+AV11UserSDT.toJSonString(false, true)); ;
			AV16GXV1 = (int)(AV16GXV1+1) ;
		}
		System.out.println("END Queue Event received") ;
		cleanup();
	}

	protected void cleanup( )
	{
		this.aP1[0] = handlesimpleuserqueueevent.this.AV9EventMessageResponse;
		CloseOpenCursors();
		exitApp();
	}

	protected void CloseOpenCursors( )
	{
	}

	/* Aggregate/select formulas */
	public void initialize( )
	{
		AV9EventMessageResponse = new com.genexus.genexusserverlessapi.SdtEventMessageResponse(remoteHandle, context);
		AV15Pgmname = "" ;
		AV10EventMessage = new com.genexus.genexusserverlessapi.SdtEventMessage(remoteHandle, context);
		AV12OutMessages = new GXBaseCollection<com.genexus.SdtMessages_Message>(com.genexus.SdtMessages_Message.class, "Message", "GeneXus", remoteHandle);
		AV11UserSDT = new com.unittest.eventdriven.SdtUser(remoteHandle);
		AV15Pgmname = "EventDriven.Queue.HandleSimpleUserQueueEvent" ;
		/* GeneXus formulas. */
		AV15Pgmname = "EventDriven.Queue.HandleSimpleUserQueueEvent" ;
		Gx_err = (short)(0) ;
	}

	private short Gx_err ;
	private int AV16GXV1 ;
	private String AV15Pgmname ;
	private boolean returnInSub ;
	private com.unittest.eventdriven.SdtUser AV11UserSDT ;
	private com.genexus.genexusserverlessapi.SdtEventMessageResponse[] aP1 ;
	private GXBaseCollection<com.genexus.SdtMessages_Message> AV12OutMessages ;
	private com.genexus.genexusserverlessapi.SdtEventMessages AV8EventMessages ;
	private com.genexus.genexusserverlessapi.SdtEventMessage AV10EventMessage ;
	private com.genexus.genexusserverlessapi.SdtEventMessageResponse AV9EventMessageResponse ;
}

