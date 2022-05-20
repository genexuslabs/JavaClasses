package com.unittest.eventdriven;

import com.genexus.*;

public final class StructSdtUser implements Cloneable, java.io.Serializable {
	public StructSdtUser() {
		this(-1, new ModelContext(StructSdtUser.class));
	}

	public StructSdtUser(int remoteHandle,
						 ModelContext context) {
		java.util.Calendar cal = java.util.Calendar.getInstance();
		cal.set(1, 0, 1, 0, 0, 0);
		cal.set(java.util.Calendar.MILLISECOND, 0);
		gxTv_SdtUser_Userid = java.util.UUID.fromString("00000000-0000-0000-0000-000000000000");
		gxTv_SdtUser_Username = "";
		gxTv_SdtUser_Userregistereddatetime = cal.getTime();
		gxTv_SdtUser_Usereventmessagedata = "";
		gxTv_SdtUser_Mode = "";
		gxTv_SdtUser_Userid_Z = java.util.UUID.fromString("00000000-0000-0000-0000-000000000000");
		gxTv_SdtUser_Username_Z = "";
		gxTv_SdtUser_Userregistereddatetime_Z = cal.getTime();
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

	public java.util.UUID getUserid() {
		return gxTv_SdtUser_Userid;
	}

	public void setUserid(java.util.UUID value) {
		gxTv_SdtUser_N = (byte) (0);
		gxTv_SdtUser_Userid = value;
	}

	public String getUsername() {
		return gxTv_SdtUser_Username;
	}

	public void setUsername(String value) {
		gxTv_SdtUser_N = (byte) (0);
		gxTv_SdtUser_Username = value;
	}

	public java.util.Date getUserregistereddatetime() {
		return gxTv_SdtUser_Userregistereddatetime;
	}

	public void setUserregistereddatetime(java.util.Date value) {
		gxTv_SdtUser_N = (byte) (0);
		gxTv_SdtUser_Userregistereddatetime = value;
	}

	public String getUsereventmessagedata() {
		return gxTv_SdtUser_Usereventmessagedata;
	}

	public void setUsereventmessagedata(String value) {
		gxTv_SdtUser_N = (byte) (0);
		gxTv_SdtUser_Usereventmessagedata = value;
	}

	public String getMode() {
		return gxTv_SdtUser_Mode;
	}

	public void setMode(String value) {
		gxTv_SdtUser_N = (byte) (0);
		gxTv_SdtUser_Mode = value;
	}

	public short getInitialized() {
		return gxTv_SdtUser_Initialized;
	}

	public void setInitialized(short value) {
		gxTv_SdtUser_N = (byte) (0);
		gxTv_SdtUser_Initialized = value;
	}

	public java.util.UUID getUserid_Z() {
		return gxTv_SdtUser_Userid_Z;
	}

	public void setUserid_Z(java.util.UUID value) {
		gxTv_SdtUser_N = (byte) (0);
		gxTv_SdtUser_Userid_Z = value;
	}

	public String getUsername_Z() {
		return gxTv_SdtUser_Username_Z;
	}

	public void setUsername_Z(String value) {
		gxTv_SdtUser_N = (byte) (0);
		gxTv_SdtUser_Username_Z = value;
	}

	public java.util.Date getUserregistereddatetime_Z() {
		return gxTv_SdtUser_Userregistereddatetime_Z;
	}

	public void setUserregistereddatetime_Z(java.util.Date value) {
		gxTv_SdtUser_N = (byte) (0);
		gxTv_SdtUser_Userregistereddatetime_Z = value;
	}

	private byte gxTv_SdtUser_N;
	protected short gxTv_SdtUser_Initialized;
	protected String gxTv_SdtUser_Username;
	protected String gxTv_SdtUser_Mode;
	protected String gxTv_SdtUser_Username_Z;
	protected String gxTv_SdtUser_Usereventmessagedata;
	protected java.util.UUID gxTv_SdtUser_Userid;
	protected java.util.UUID gxTv_SdtUser_Userid_Z;
	protected java.util.Date gxTv_SdtUser_Userregistereddatetime;
	protected java.util.Date gxTv_SdtUser_Userregistereddatetime_Z;
}

