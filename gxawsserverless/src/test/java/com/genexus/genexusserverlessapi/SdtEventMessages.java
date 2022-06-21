package com.genexus.genexusserverlessapi;

import com.genexus.*;
import com.genexus.*;
import com.genexus.xml.*;
import com.genexus.search.*;
import com.genexus.webpanels.*;

import java.util.*;

public final class SdtEventMessages extends GxUserType {
	public SdtEventMessages() {
		this(new ModelContext(SdtEventMessages.class));
	}

	public SdtEventMessages(ModelContext context) {
		super(context, "SdtEventMessages");
	}

	public SdtEventMessages(int remoteHandle,
							ModelContext context) {
		super(remoteHandle, context, "SdtEventMessages");
	}

	public SdtEventMessages(StructSdtEventMessages struct) {
		this();
		setStruct(struct);
	}

	private static java.util.HashMap mapper = new java.util.HashMap();

	static {
	}

	public String getJsonMap(String value) {
		return (String) mapper.get(value);
	}

	public void tojson() {
		tojson(true);
	}

	public void tojson(boolean includeState) {
		tojson(includeState, true);
	}

	public void tojson(boolean includeState,
					   boolean includeNonInitialized) {
		if (gxTv_SdtEventMessages_Eventmessage != null) {
			AddObjectProperty("EventMessage", gxTv_SdtEventMessages_Eventmessage, false, false);
		}
	}

	public GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventMessage> getgxTv_SdtEventMessages_Eventmessage() {
		if (gxTv_SdtEventMessages_Eventmessage == null) {
			gxTv_SdtEventMessages_Eventmessage = new GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventMessage>(com.genexus.genexusserverlessapi.SdtEventMessage.class, "EventMessage", "ServerlessAPI", remoteHandle);
		}
		gxTv_SdtEventMessages_Eventmessage_N = (byte) (0);
		gxTv_SdtEventMessages_N = (byte) (0);
		return gxTv_SdtEventMessages_Eventmessage;
	}

	public void setgxTv_SdtEventMessages_Eventmessage(GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventMessage> value) {
		gxTv_SdtEventMessages_Eventmessage_N = (byte) (0);
		gxTv_SdtEventMessages_N = (byte) (0);
		gxTv_SdtEventMessages_Eventmessage = value;
	}

	public void setgxTv_SdtEventMessages_Eventmessage_SetNull() {
		gxTv_SdtEventMessages_Eventmessage_N = (byte) (1);
		gxTv_SdtEventMessages_Eventmessage = null;
	}

	public boolean getgxTv_SdtEventMessages_Eventmessage_IsNull() {
		if (gxTv_SdtEventMessages_Eventmessage == null) {
			return true;
		}
		return false;
	}

	public byte getgxTv_SdtEventMessages_Eventmessage_N() {
		return gxTv_SdtEventMessages_Eventmessage_N;
	}

	public void initialize(int remoteHandle) {
		initialize();
	}

	public void initialize() {
		gxTv_SdtEventMessages_Eventmessage_N = (byte) (1);
		gxTv_SdtEventMessages_N = (byte) (1);
		sTagName = "";
	}

	public byte isNull() {
		return gxTv_SdtEventMessages_N;
	}

	public com.genexus.genexusserverlessapi.SdtEventMessages Clone() {
		return (com.genexus.genexusserverlessapi.SdtEventMessages) (clone());
	}

	public void setStruct(com.genexus.genexusserverlessapi.StructSdtEventMessages struct) {
		GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventMessage> gxTv_SdtEventMessages_Eventmessage_aux = new GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventMessage>(com.genexus.genexusserverlessapi.SdtEventMessage.class, "EventMessage", "ServerlessAPI", remoteHandle);
		Vector<com.genexus.genexusserverlessapi.StructSdtEventMessage> gxTv_SdtEventMessages_Eventmessage_aux1 = struct.getEventmessage();
		if (gxTv_SdtEventMessages_Eventmessage_aux1 != null) {
			for (int i = 0; i < gxTv_SdtEventMessages_Eventmessage_aux1.size(); i++) {
				gxTv_SdtEventMessages_Eventmessage_aux.add(new com.genexus.genexusserverlessapi.SdtEventMessage(gxTv_SdtEventMessages_Eventmessage_aux1.elementAt(i)));
			}
		}
		setgxTv_SdtEventMessages_Eventmessage(gxTv_SdtEventMessages_Eventmessage_aux);
	}

	@SuppressWarnings("unchecked")
	public com.genexus.genexusserverlessapi.StructSdtEventMessages getStruct() {
		com.genexus.genexusserverlessapi.StructSdtEventMessages struct = new com.genexus.genexusserverlessapi.StructSdtEventMessages();
		struct.setEventmessage(getgxTv_SdtEventMessages_Eventmessage().getStruct());
		return struct;
	}

	protected byte gxTv_SdtEventMessages_Eventmessage_N;
	protected byte gxTv_SdtEventMessages_N;
	protected short readOk;
	protected short nOutParmCount;
	protected String sTagName;
	protected boolean readElement;
	protected boolean formatError;
	protected GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventMessage> gxTv_SdtEventMessages_Eventmessage_aux;
	protected GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventMessage> gxTv_SdtEventMessages_Eventmessage = null;
}

