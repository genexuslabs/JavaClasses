package com.unittest.eventdriven;

import com.unittest.*;
import com.genexus.*;
import com.genexus.xml.*;
import com.genexus.search.*;
import com.genexus.webpanels.*;

import java.util.*;

public final class SdtUser extends GxSilentTrnSdt {
	public SdtUser(int remoteHandle) {
		this(remoteHandle, new ModelContext(SdtUser.class));
	}

	public SdtUser(int remoteHandle,
				   ModelContext context) {
		super(remoteHandle, context, "SdtUser");
		initialize();
	}

	public SdtUser(int remoteHandle,
				   StructSdtUser struct) {
		this(remoteHandle);
		setStruct(struct);
	}

	private static HashMap mapper = new HashMap();

	static {
	}

	public String getJsonMap(String value) {
		return (String) mapper.get(value);
	}

	public UUID getgxTv_SdtUser_Userid() {
		return gxTv_SdtUser_Userid;
	}

	public void setgxTv_SdtUser_Userid(UUID value) {
		gxTv_SdtUser_N = (byte) (0);
		if (!(gxTv_SdtUser_Userid.equals(value))) {
			gxTv_SdtUser_Mode = "INS";
			this.setgxTv_SdtUser_Userid_Z_SetNull();
			this.setgxTv_SdtUser_Username_Z_SetNull();
			this.setgxTv_SdtUser_Userregistereddatetime_Z_SetNull();
		}
		SetDirty("Userid");
		gxTv_SdtUser_Userid = value;
	}

	public String getgxTv_SdtUser_Username() {
		return gxTv_SdtUser_Username;
	}

	public void setgxTv_SdtUser_Username(String value) {
		gxTv_SdtUser_N = (byte) (0);
		SetDirty("Username");
		gxTv_SdtUser_Username = value;
	}

	public Date getgxTv_SdtUser_Userregistereddatetime() {
		return gxTv_SdtUser_Userregistereddatetime;
	}

	public void setgxTv_SdtUser_Userregistereddatetime(Date value) {
		gxTv_SdtUser_N = (byte) (0);
		SetDirty("Userregistereddatetime");
		gxTv_SdtUser_Userregistereddatetime = value;
	}

	public String getgxTv_SdtUser_Usereventmessagedata() {
		return gxTv_SdtUser_Usereventmessagedata;
	}

	public void setgxTv_SdtUser_Usereventmessagedata(String value) {
		gxTv_SdtUser_N = (byte) (0);
		SetDirty("Usereventmessagedata");
		gxTv_SdtUser_Usereventmessagedata = value;
	}

	public String getgxTv_SdtUser_Mode() {
		return gxTv_SdtUser_Mode;
	}

	public void setgxTv_SdtUser_Mode(String value) {
		gxTv_SdtUser_N = (byte) (0);
		SetDirty("Mode");
		gxTv_SdtUser_Mode = value;
	}

	public void setgxTv_SdtUser_Mode_SetNull() {
		gxTv_SdtUser_Mode = "";
		SetDirty("Mode");
	}

	public boolean getgxTv_SdtUser_Mode_IsNull() {
		return false;
	}

	public short getgxTv_SdtUser_Initialized() {
		return gxTv_SdtUser_Initialized;
	}

	public void setgxTv_SdtUser_Initialized(short value) {
		gxTv_SdtUser_N = (byte) (0);
		SetDirty("Initialized");
		gxTv_SdtUser_Initialized = value;
	}

	public void setgxTv_SdtUser_Initialized_SetNull() {
		gxTv_SdtUser_Initialized = (short) (0);
		SetDirty("Initialized");
	}

	public boolean getgxTv_SdtUser_Initialized_IsNull() {
		return false;
	}

	public UUID getgxTv_SdtUser_Userid_Z() {
		return gxTv_SdtUser_Userid_Z;
	}

	public void setgxTv_SdtUser_Userid_Z(UUID value) {
		gxTv_SdtUser_N = (byte) (0);
		SetDirty("Userid_Z");
		gxTv_SdtUser_Userid_Z = value;
	}

	public void setgxTv_SdtUser_Userid_Z_SetNull() {
		gxTv_SdtUser_Userid_Z = UUID.fromString("00000000-0000-0000-0000-000000000000");
		SetDirty("Userid_Z");
	}

	public boolean getgxTv_SdtUser_Userid_Z_IsNull() {
		return false;
	}

	public String getgxTv_SdtUser_Username_Z() {
		return gxTv_SdtUser_Username_Z;
	}

	public void setgxTv_SdtUser_Username_Z(String value) {
		gxTv_SdtUser_N = (byte) (0);
		SetDirty("Username_Z");
		gxTv_SdtUser_Username_Z = value;
	}

	public void setgxTv_SdtUser_Username_Z_SetNull() {
		gxTv_SdtUser_Username_Z = "";
		SetDirty("Username_Z");
	}

	public boolean getgxTv_SdtUser_Username_Z_IsNull() {
		return false;
	}

	public Date getgxTv_SdtUser_Userregistereddatetime_Z() {
		return gxTv_SdtUser_Userregistereddatetime_Z;
	}

	public void setgxTv_SdtUser_Userregistereddatetime_Z(Date value) {
		gxTv_SdtUser_N = (byte) (0);
		SetDirty("Userregistereddatetime_Z");
		gxTv_SdtUser_Userregistereddatetime_Z = value;
	}

	public void setgxTv_SdtUser_Userregistereddatetime_Z_SetNull() {
		gxTv_SdtUser_Userregistereddatetime_Z = GXutil.resetTime(GXutil.nullDate());
		SetDirty("Userregistereddatetime_Z");
	}

	public boolean getgxTv_SdtUser_Userregistereddatetime_Z_IsNull() {
		return false;
	}


	public void initialize() {
		gxTv_SdtUser_Userid = UUID.fromString("00000000-0000-0000-0000-000000000000");
		gxTv_SdtUser_N = (byte) (1);
		gxTv_SdtUser_Username = "";
		gxTv_SdtUser_Userregistereddatetime = GXutil.resetTime(GXutil.nullDate());
		gxTv_SdtUser_Usereventmessagedata = "";
		gxTv_SdtUser_Mode = "";
		gxTv_SdtUser_Userid_Z = UUID.fromString("00000000-0000-0000-0000-000000000000");
		gxTv_SdtUser_Username_Z = "";
		gxTv_SdtUser_Userregistereddatetime_Z = GXutil.resetTime(GXutil.nullDate());
		sTagName = "";
		sDateCnv = "";
		sNumToPad = "";
		datetime_STZ = GXutil.resetTime(GXutil.nullDate());
	}

	public byte isNull() {
		return gxTv_SdtUser_N;
	}


	public void setStruct(com.unittest.eventdriven.StructSdtUser struct) {
		setgxTv_SdtUser_Userid(struct.getUserid());
		setgxTv_SdtUser_Username(struct.getUsername());
		setgxTv_SdtUser_Userregistereddatetime(struct.getUserregistereddatetime());
		setgxTv_SdtUser_Usereventmessagedata(struct.getUsereventmessagedata());
		setgxTv_SdtUser_Mode(struct.getMode());
		setgxTv_SdtUser_Initialized(struct.getInitialized());
		setgxTv_SdtUser_Userid_Z(struct.getUserid_Z());
		setgxTv_SdtUser_Username_Z(struct.getUsername_Z());
		setgxTv_SdtUser_Userregistereddatetime_Z(struct.getUserregistereddatetime_Z());
	}

	@SuppressWarnings("unchecked")
	public com.unittest.eventdriven.StructSdtUser getStruct() {
		com.unittest.eventdriven.StructSdtUser struct = new com.unittest.eventdriven.StructSdtUser();
		struct.setUserid(getgxTv_SdtUser_Userid());
		struct.setUsername(getgxTv_SdtUser_Username());
		struct.setUserregistereddatetime(getgxTv_SdtUser_Userregistereddatetime());
		struct.setUsereventmessagedata(getgxTv_SdtUser_Usereventmessagedata());
		struct.setMode(getgxTv_SdtUser_Mode());
		struct.setInitialized(getgxTv_SdtUser_Initialized());
		struct.setUserid_Z(getgxTv_SdtUser_Userid_Z());
		struct.setUsername_Z(getgxTv_SdtUser_Username_Z());
		struct.setUserregistereddatetime_Z(getgxTv_SdtUser_Userregistereddatetime_Z());
		return struct;
	}

	private byte gxTv_SdtUser_N;
	private short gxTv_SdtUser_Initialized;
	private String gxTv_SdtUser_Username;
	private String gxTv_SdtUser_Mode;
	private String gxTv_SdtUser_Username_Z;
	private String sTagName;
	private String sDateCnv;
	private String sNumToPad;
	private Date gxTv_SdtUser_Userregistereddatetime;
	private Date gxTv_SdtUser_Userregistereddatetime_Z;
	private Date datetime_STZ;
	private String gxTv_SdtUser_Usereventmessagedata;
	private UUID gxTv_SdtUser_Userid;
	private UUID gxTv_SdtUser_Userid_Z;
}

