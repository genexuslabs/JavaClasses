package com.genexus.webpanels.gridstate ;

public final  class StructSdtGridState_InputValuesItem implements Cloneable, java.io.Serializable {
	public StructSdtGridState_InputValuesItem() {
		gxTv_SdtGridState_InputValuesItem_Name = "";
		gxTv_SdtGridState_InputValuesItem_Value = "";
	}

	public Object clone() {
		Object cloned = null;
		try {
			cloned = super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return cloned;
	}

	public String getName() {
		return gxTv_SdtGridState_InputValuesItem_Name;
	}

	public void setName(String value) {
		gxTv_SdtGridState_InputValuesItem_Name = value;
	}

	public String getValue() {
		return gxTv_SdtGridState_InputValuesItem_Value;
	}

	public void setValue(String value) {
		gxTv_SdtGridState_InputValuesItem_Value = value;
	}

	protected String gxTv_SdtGridState_InputValuesItem_Name;
	protected String gxTv_SdtGridState_InputValuesItem_Value;
}

