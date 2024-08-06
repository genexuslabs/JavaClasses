package com.genexus.genexusserverlessapi;

import com.genexus.GxUserType;
import com.genexus.ModelContext;

import java.util.HashMap;

public final class SdtEventCustomPayload_CustomPayloadItem extends GxUserType {
	public SdtEventCustomPayload_CustomPayloadItem() {
		this(new ModelContext(SdtEventCustomPayload_CustomPayloadItem.class));
	}

	public SdtEventCustomPayload_CustomPayloadItem(ModelContext context) {
		super(context, "SdtEventCustomPayload_CustomPayloadItem");
	}

	public SdtEventCustomPayload_CustomPayloadItem(int remoteHandle,
												   ModelContext context) {
		super(remoteHandle, context, "SdtEventCustomPayload_CustomPayloadItem");
	}

	public SdtEventCustomPayload_CustomPayloadItem(StructSdtEventCustomPayload_CustomPayloadItem struct) {
		this();
		setStruct(struct);
	}

	private static HashMap mapper = new HashMap();

	static {
	}

	public String getJsonMap(String value) {
		return (String) mapper.get(value);
	}

	public String getgxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid() {
		return gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid;
	}

	public void setgxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid(String value) {
		gxTv_SdtEventCustomPayload_CustomPayloadItem_N = (byte) (0);
		gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid = value;
	}

	public String getgxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue() {
		return gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue;
	}

	public void setgxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue(String value) {
		gxTv_SdtEventCustomPayload_CustomPayloadItem_N = (byte) (0);
		gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue = value;
	}

	public void initialize(int remoteHandle) {
		initialize();
	}

	public void initialize() {
		gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid = "";
		gxTv_SdtEventCustomPayload_CustomPayloadItem_N = (byte) (1);
		gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue = "";
		sTagName = "";
	}

	public byte isNull() {
		return gxTv_SdtEventCustomPayload_CustomPayloadItem_N;
	}

	public com.genexus.genexusserverlessapi.SdtEventCustomPayload_CustomPayloadItem Clone() {
		return (com.genexus.genexusserverlessapi.SdtEventCustomPayload_CustomPayloadItem) (clone());
	}

	public void setStruct(StructSdtEventCustomPayload_CustomPayloadItem struct) {
		setgxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid(struct.getPropertyid());
		setgxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue(struct.getPropertyvalue());
	}

	@SuppressWarnings("unchecked")
	public StructSdtEventCustomPayload_CustomPayloadItem getStruct() {
		StructSdtEventCustomPayload_CustomPayloadItem struct = new StructSdtEventCustomPayload_CustomPayloadItem();
		struct.setPropertyid(getgxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid());
		struct.setPropertyvalue(getgxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue());
		return struct;
	}

	protected byte gxTv_SdtEventCustomPayload_CustomPayloadItem_N;
	protected short readOk;
	protected short nOutParmCount;
	protected String gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue;
	protected String sTagName;
	protected boolean readElement;
	protected boolean formatError;
	protected String gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid;
}

