package com.genexus.genexusserverlessapi;

import com.genexus.GXBaseCollection;
import com.genexus.GXutil;
import com.genexus.GxUserType;
import com.genexus.ModelContext;

import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

public final class SdtEventMessage extends GxUserType {
	public SdtEventMessage() {
		this(new ModelContext(SdtEventMessage.class));
	}

	public SdtEventMessage(ModelContext context) {
		super(context, "SdtEventMessage");
	}

	public SdtEventMessage(int remoteHandle,
						   ModelContext context) {
		super(remoteHandle, context, "SdtEventMessage");
	}

	public SdtEventMessage(StructSdtEventMessage struct) {
		this();
		setStruct(struct);
	}

	private static HashMap mapper = new HashMap();

	public String getJsonMap(String value) {
		return (String) mapper.get(value);
	}

	public String getgxTv_SdtEventMessage_Eventmessageid() {
		return gxTv_SdtEventMessage_Eventmessageid;
	}

	public void setgxTv_SdtEventMessage_Eventmessageid(String value) {
		gxTv_SdtEventMessage_N = (byte) (0);
		gxTv_SdtEventMessage_Eventmessageid = value;
	}

	public Date getgxTv_SdtEventMessage_Eventmessagedate() {
		return gxTv_SdtEventMessage_Eventmessagedate;
	}

	public void setgxTv_SdtEventMessage_Eventmessagedate(Date value) {
		gxTv_SdtEventMessage_Eventmessagedate_N = (byte) (0);
		gxTv_SdtEventMessage_N = (byte) (0);
		gxTv_SdtEventMessage_Eventmessagedate = value;
	}

	public String getgxTv_SdtEventMessage_Eventmessagesourcetype() {
		return gxTv_SdtEventMessage_Eventmessagesourcetype;
	}

	public void setgxTv_SdtEventMessage_Eventmessagesourcetype(String value) {
		gxTv_SdtEventMessage_N = (byte) (0);
		gxTv_SdtEventMessage_Eventmessagesourcetype = value;
	}

	public String getgxTv_SdtEventMessage_Eventmessagedata() {
		return gxTv_SdtEventMessage_Eventmessagedata;
	}

	public void setgxTv_SdtEventMessage_Eventmessagedata(String value) {
		gxTv_SdtEventMessage_N = (byte) (0);
		gxTv_SdtEventMessage_Eventmessagedata = value;
	}

	public String getgxTv_SdtEventMessage_Eventmessageversion() {
		return gxTv_SdtEventMessage_Eventmessageversion;
	}

	public void setgxTv_SdtEventMessage_Eventmessageversion(String value) {
		gxTv_SdtEventMessage_N = (byte) (0);
		gxTv_SdtEventMessage_Eventmessageversion = value;
	}

	public GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventCustomPayload_CustomPayloadItem> getgxTv_SdtEventMessage_Eventmessagecustompayload() {
		if (gxTv_SdtEventMessage_Eventmessagecustompayload == null) {
			gxTv_SdtEventMessage_Eventmessagecustompayload = new GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventCustomPayload_CustomPayloadItem>(com.genexus.genexusserverlessapi.SdtEventCustomPayload_CustomPayloadItem.class, "CustomPayloadItem", "ServerlessAPI", remoteHandle);
		}
		gxTv_SdtEventMessage_Eventmessagecustompayload_N = (byte) (0);
		gxTv_SdtEventMessage_N = (byte) (0);
		return gxTv_SdtEventMessage_Eventmessagecustompayload;
	}

	public void setgxTv_SdtEventMessage_Eventmessagecustompayload(GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventCustomPayload_CustomPayloadItem> value) {
		gxTv_SdtEventMessage_Eventmessagecustompayload_N = (byte) (0);
		gxTv_SdtEventMessage_N = (byte) (0);
		gxTv_SdtEventMessage_Eventmessagecustompayload = value;
	}

	public void setgxTv_SdtEventMessage_Eventmessagecustompayload_SetNull() {
		gxTv_SdtEventMessage_Eventmessagecustompayload_N = (byte) (1);
		gxTv_SdtEventMessage_Eventmessagecustompayload = null;
	}

	public boolean getgxTv_SdtEventMessage_Eventmessagecustompayload_IsNull() {
		if (gxTv_SdtEventMessage_Eventmessagecustompayload == null) {
			return true;
		}
		return false;
	}

	public byte getgxTv_SdtEventMessage_Eventmessagecustompayload_N() {
		return gxTv_SdtEventMessage_Eventmessagecustompayload_N;
	}

	public void initialize(int remoteHandle) {
		initialize();
	}

	public void initialize() {
		gxTv_SdtEventMessage_Eventmessageid = "";
		gxTv_SdtEventMessage_N = (byte) (1);
		gxTv_SdtEventMessage_Eventmessagedate = GXutil.resetTime(GXutil.nullDate());
		gxTv_SdtEventMessage_Eventmessagedate_N = (byte) (1);
		gxTv_SdtEventMessage_Eventmessagesourcetype = "";
		gxTv_SdtEventMessage_Eventmessagedata = "";
		gxTv_SdtEventMessage_Eventmessageversion = "";
		gxTv_SdtEventMessage_Eventmessagecustompayload_N = (byte) (1);
		sTagName = "";
		sDateCnv = "";
		sNumToPad = "";
		datetime_STZ = GXutil.resetTime(GXutil.nullDate());
	}

	public byte isNull() {
		return gxTv_SdtEventMessage_N;
	}

	public com.genexus.genexusserverlessapi.SdtEventMessage Clone() {
		return (com.genexus.genexusserverlessapi.SdtEventMessage) (clone());
	}

	public void setStruct(StructSdtEventMessage struct) {
		setgxTv_SdtEventMessage_Eventmessageid(struct.getEventmessageid());
		if (struct.gxTv_SdtEventMessage_Eventmessagedate_N == 0) {
			setgxTv_SdtEventMessage_Eventmessagedate(struct.getEventmessagedate());
		}
		setgxTv_SdtEventMessage_Eventmessagesourcetype(struct.getEventmessagesourcetype());
		setgxTv_SdtEventMessage_Eventmessagedata(struct.getEventmessagedata());
		setgxTv_SdtEventMessage_Eventmessageversion(struct.getEventmessageversion());
		GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventCustomPayload_CustomPayloadItem> gxTv_SdtEventMessage_Eventmessagecustompayload_aux = new GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventCustomPayload_CustomPayloadItem>(com.genexus.genexusserverlessapi.SdtEventCustomPayload_CustomPayloadItem.class, "CustomPayloadItem", "ServerlessAPI", remoteHandle);
		Vector<com.genexus.genexusserverlessapi.StructSdtEventCustomPayload_CustomPayloadItem> gxTv_SdtEventMessage_Eventmessagecustompayload_aux1 = struct.getEventmessagecustompayload();
		if (gxTv_SdtEventMessage_Eventmessagecustompayload_aux1 != null) {
			for (int i = 0; i < gxTv_SdtEventMessage_Eventmessagecustompayload_aux1.size(); i++) {
				gxTv_SdtEventMessage_Eventmessagecustompayload_aux.add(new com.genexus.genexusserverlessapi.SdtEventCustomPayload_CustomPayloadItem(gxTv_SdtEventMessage_Eventmessagecustompayload_aux1.elementAt(i)));
			}
		}
		setgxTv_SdtEventMessage_Eventmessagecustompayload(gxTv_SdtEventMessage_Eventmessagecustompayload_aux);
	}

	@SuppressWarnings("unchecked")
	public StructSdtEventMessage getStruct() {
		StructSdtEventMessage struct = new StructSdtEventMessage();
		struct.setEventmessageid(getgxTv_SdtEventMessage_Eventmessageid());
		if (gxTv_SdtEventMessage_Eventmessagedate_N == 0) {
			struct.setEventmessagedate(getgxTv_SdtEventMessage_Eventmessagedate());
		}
		struct.setEventmessagesourcetype(getgxTv_SdtEventMessage_Eventmessagesourcetype());
		struct.setEventmessagedata(getgxTv_SdtEventMessage_Eventmessagedata());
		struct.setEventmessageversion(getgxTv_SdtEventMessage_Eventmessageversion());
		struct.setEventmessagecustompayload(getgxTv_SdtEventMessage_Eventmessagecustompayload().getStruct());
		return struct;
	}

	protected byte gxTv_SdtEventMessage_N;
	protected byte gxTv_SdtEventMessage_Eventmessagedate_N;
	protected byte gxTv_SdtEventMessage_Eventmessagecustompayload_N;
	protected short readOk;
	protected short nOutParmCount;
	protected String sTagName;
	protected String sDateCnv;
	protected String sNumToPad;
	protected Date gxTv_SdtEventMessage_Eventmessagedate;
	protected Date datetime_STZ;
	protected boolean readElement;
	protected boolean formatError;
	protected String gxTv_SdtEventMessage_Eventmessagedata;
	protected String gxTv_SdtEventMessage_Eventmessageid;
	protected String gxTv_SdtEventMessage_Eventmessagesourcetype;
	protected String gxTv_SdtEventMessage_Eventmessageversion;
	protected GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventCustomPayload_CustomPayloadItem> gxTv_SdtEventMessage_Eventmessagecustompayload_aux;
	protected GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventCustomPayload_CustomPayloadItem> gxTv_SdtEventMessage_Eventmessagecustompayload = null;
}

