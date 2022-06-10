package com.genexusmessaging.genexusmessagingqueue.simplequeue;

import com.genexus.*;

public class SdtMessageResult extends GxUserType {
	protected byte gxTv_SdtMessageResult_N;
	protected byte gxTv_SdtMessageResult_Messageattributes_N;
	protected String gxTv_SdtMessageResult_Messageid;
	protected String gxTv_SdtMessageResult_Servermessageid;
	protected String gxTv_SdtMessageResult_Messagehandleid;
	protected String gxTv_SdtMessageResult_Messagestatus;
	protected GXBaseCollection<com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessageProperty> gxTv_SdtMessageResult_Messageattributes_aux;
	protected GXBaseCollection<com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessageProperty> gxTv_SdtMessageResult_Messageattributes = null;

	public SdtMessageResult( )
	{
		this(  new ModelContext(SdtMessageResult.class));
	}

	public SdtMessageResult( ModelContext context )
	{
		super( context, "SdtMessageResult");
	}

	public String getgxTv_SdtMessageResult_Messageid() {
		return gxTv_SdtMessageResult_Messageid;
	}

	public void setgxTv_SdtMessageResult_Messageid(String value) {
		gxTv_SdtMessageResult_N = (byte) (0);
		gxTv_SdtMessageResult_Messageid = value;
	}

	public String getgxTv_SdtMessageResult_Servermessageid() {
		return gxTv_SdtMessageResult_Servermessageid;
	}

	public void setgxTv_SdtMessageResult_Servermessageid(String value) {
		gxTv_SdtMessageResult_N = (byte) (0);
		gxTv_SdtMessageResult_Servermessageid = value;
	}

	public String getgxTv_SdtMessageResult_Messagehandleid() {
		return gxTv_SdtMessageResult_Messagehandleid;
	}

	public void setgxTv_SdtMessageResult_Messagehandleid(String value) {
		gxTv_SdtMessageResult_N = (byte) (0);
		gxTv_SdtMessageResult_Messagehandleid = value;
	}

	public String getgxTv_SdtMessageResult_Messagestatus() {
		return gxTv_SdtMessageResult_Messagestatus;
	}

	public void setgxTv_SdtMessageResult_Messagestatus(String value) {
		gxTv_SdtMessageResult_N = (byte) (0);
		gxTv_SdtMessageResult_Messagestatus = value;
	}

	public GXBaseCollection<com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessageProperty> getgxTv_SdtMessageResult_Messageattributes() {
		if (gxTv_SdtMessageResult_Messageattributes == null) {
			gxTv_SdtMessageResult_Messageattributes = new GXBaseCollection<com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessageProperty>(com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessageProperty.class, "MessageProperty", "GeneXusMessaging", remoteHandle);
		}
		gxTv_SdtMessageResult_Messageattributes_N = (byte) (0);
		gxTv_SdtMessageResult_N = (byte) (0);
		return gxTv_SdtMessageResult_Messageattributes;
	}

	public void setgxTv_SdtMessageResult_Messageattributes(GXBaseCollection<com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessageProperty> value) {
		gxTv_SdtMessageResult_Messageattributes_N = (byte) (0);
		gxTv_SdtMessageResult_N = (byte) (0);
		gxTv_SdtMessageResult_Messageattributes = value;
	}

	@Override
	public void initialize() {

	}

	@Override
	public String getJsonMap(String value) {
		return null;
	}
}

