package com.genexusmessaging.genexusmessagingqueue.simplequeue;

import com.genexus.*;

public class SdtMessage extends GxUserType {
	protected byte gxTv_SdtMessage_N;
	protected byte gxTv_SdtMessage_Messageattributes_N;
	protected String sTagName;
	protected String gxTv_SdtMessage_Messagebody;
	protected String gxTv_SdtMessage_Messageid;
	protected String gxTv_SdtMessage_Messagehandleid;
	protected GXBaseCollection<com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessageProperty> gxTv_SdtMessage_Messageattributes_aux;
	protected GXBaseCollection<com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessageProperty> gxTv_SdtMessage_Messageattributes = null;


	public SdtMessage( )
	{
		this(  new ModelContext(SdtMessage.class));
	}

	public SdtMessage( ModelContext context )
	{
		super( context, "SdtMessage");
	}

	public String getgxTv_SdtMessage_Messageid() {
		return gxTv_SdtMessage_Messageid;
	}

	public void setgxTv_SdtMessage_Messageid(String value) {
		gxTv_SdtMessage_N = (byte) (0);
		gxTv_SdtMessage_Messageid = value;
	}

	public String getgxTv_SdtMessage_Messagebody() {
		return gxTv_SdtMessage_Messagebody;
	}

	public void setgxTv_SdtMessage_Messagebody(String value) {
		gxTv_SdtMessage_N = (byte) (0);
		gxTv_SdtMessage_Messagebody = value;
	}

	public String getgxTv_SdtMessage_Messagehandleid() {
		return gxTv_SdtMessage_Messagehandleid;
	}

	public void setgxTv_SdtMessage_Messagehandleid(String value) {
		gxTv_SdtMessage_N = (byte) (0);
		gxTv_SdtMessage_Messagehandleid = value;
	}

	public GXBaseCollection<com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessageProperty> getgxTv_SdtMessage_Messageattributes() {
		if (gxTv_SdtMessage_Messageattributes == null) {
			gxTv_SdtMessage_Messageattributes = new GXBaseCollection<com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessageProperty>(com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessageProperty.class, "MessageProperty", "GeneXusMessaging", remoteHandle);
		}
		gxTv_SdtMessage_Messageattributes_N = (byte) (0);
		gxTv_SdtMessage_N = (byte) (0);
		return gxTv_SdtMessage_Messageattributes;
	}

	public void setgxTv_SdtMessage_Messageattributes(GXBaseCollection<com.genexusmessaging.genexusmessagingqueue.simplequeue.SdtMessageProperty> value) {
		gxTv_SdtMessage_Messageattributes_N = (byte) (0);
		gxTv_SdtMessage_N = (byte) (0);
		gxTv_SdtMessage_Messageattributes = value;
	}


	public void initialize() {
		gxTv_SdtMessage_Messageid = "";
		gxTv_SdtMessage_N = (byte) (1);
		gxTv_SdtMessage_Messagebody = "";
		gxTv_SdtMessage_Messagehandleid = "";
		gxTv_SdtMessage_Messageattributes_N = (byte) (1);
		sTagName = "";
	}

	@Override
	public String getJsonMap(String value) {
		return null;
	}
}

