package com.genexus.genexusserverlessapi;

import com.genexus.*;

public final class StructSdtEventCustomPayload_CustomPayloadItem implements Cloneable, java.io.Serializable {
	public StructSdtEventCustomPayload_CustomPayloadItem() {
		this(-1, new ModelContext(StructSdtEventCustomPayload_CustomPayloadItem.class));
	}

	public StructSdtEventCustomPayload_CustomPayloadItem(int remoteHandle,
														 ModelContext context) {
		gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid = "";
		gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue = "";
	}

	public Object clone() {
		Object cloned = null;
		try {
			cloned = super.clone();
		} catch (CloneNotSupportedException e) {
			;
		}
		return cloned;
	}

	public String getPropertyid() {
		return gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid;
	}

	public void setPropertyid(String value) {
		gxTv_SdtEventCustomPayload_CustomPayloadItem_N = (byte) (0);
		gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid = value;
	}

	public String getPropertyvalue() {
		return gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue;
	}

	public void setPropertyvalue(String value) {
		gxTv_SdtEventCustomPayload_CustomPayloadItem_N = (byte) (0);
		gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue = value;
	}

	protected byte gxTv_SdtEventCustomPayload_CustomPayloadItem_N;
	protected String gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue;
	protected String gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid;
}

