package com.genexus.webpanels.gridstate ;
public final  class StructSdtGridState implements Cloneable, java.io.Serializable {
	public StructSdtGridState() {
		gxTv_SdtGridState_Inputvalues_N = (byte) (1);
	}

	public Object clone() {
		Object cloned = null;
		try {
			cloned = super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return cloned;
	}

	public int getCurrentpage() {
		return gxTv_SdtGridState_Currentpage;
	}

	public void setCurrentpage(int value) {
		gxTv_SdtGridState_Currentpage = value;
	}

	public short getOrderedby() {
		return gxTv_SdtGridState_Orderedby;
	}

	public void setOrderedby(short value) {
		gxTv_SdtGridState_Orderedby = value;
	}

	public java.util.Vector getInputvalues() {
		return gxTv_SdtGridState_Inputvalues;
	}

	public void setInputvalues(java.util.Vector value) {
		gxTv_SdtGridState_Inputvalues_N = (byte) (0);
		gxTv_SdtGridState_Inputvalues = value;
	}

	protected byte gxTv_SdtGridState_Inputvalues_N;
	protected short gxTv_SdtGridState_Orderedby;
	protected int gxTv_SdtGridState_Currentpage;
	protected java.util.Vector gxTv_SdtGridState_Inputvalues = null;
}

