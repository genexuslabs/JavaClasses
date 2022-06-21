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

	public void Load(UUID AV4UserId) {
		IGxSilentTrn obj;
		obj = getTransaction();
		obj.LoadKey(new Object[]{AV4UserId});
	}

	public void LoadStrParms(String sAV4UserId) {
		UUID AV4UserId;
		AV4UserId = GXutil.strToGuid(sAV4UserId);
		Load(AV4UserId);
	}

	public Object[][] GetBCKey() {
		return (Object[][]) (new Object[][]{new Object[]{"UserId", UUID.class}});
	}

	public com.genexus.util.GXProperties getMetadata() {
		com.genexus.util.GXProperties metadata = new com.genexus.util.GXProperties();
		metadata.set("Name", "EventDriven\\User");
		metadata.set("BT", "User");
		metadata.set("PK", "[ \"UserId\" ]");
		metadata.set("PKAssigned", "[ \"UserId\" ]");
		metadata.set("AllowInsert", "True");
		metadata.set("AllowUpdate", "True");
		metadata.set("AllowDelete", "True");
		return metadata;
	}

	public short readxml(XMLReader oReader,
						 String sName) {
		short GXSoapError = 1;
		formatError = false;
		sTagName = oReader.getName();
		if (oReader.getIsSimple() == 0) {
			GXSoapError = oReader.read();
			nOutParmCount = (short) (0);
			while (((GXutil.strcmp(oReader.getName(), sTagName) != 0) || (oReader.getNodeType() == 1)) && (GXSoapError > 0)) {
				readOk = (short) (0);
				readElement = false;
				if (GXutil.strcmp2(oReader.getLocalName(), "UserId")) {
					gxTv_SdtUser_Userid = GXutil.strToGuid(oReader.getValue());
					readElement = true;
					if (GXSoapError > 0) {
						readOk = (short) (1);
					}
					GXSoapError = oReader.read();
				}
				if (GXutil.strcmp2(oReader.getLocalName(), "UserName")) {
					gxTv_SdtUser_Username = oReader.getValue();
					readElement = true;
					if (GXSoapError > 0) {
						readOk = (short) (1);
					}
					GXSoapError = oReader.read();
				}
				if (GXutil.strcmp2(oReader.getLocalName(), "UserRegisteredDateTime")) {
					if ((GXutil.strcmp(oReader.getValue(), "") == 0) || (oReader.existsAttribute("xsi:nil") == 1)) {
						gxTv_SdtUser_Userregistereddatetime = GXutil.resetTime(GXutil.nullDate());
					} else {
						gxTv_SdtUser_Userregistereddatetime = localUtil.ymdhmsToT((short) (DecimalUtil.decToDouble(CommonUtil.decimalVal(GXutil.substring(oReader.getValue(), 1, 4), "."))), (byte) (DecimalUtil.decToDouble(CommonUtil.decimalVal(GXutil.substring(oReader.getValue(), 6, 2), "."))), (byte) (DecimalUtil.decToDouble(CommonUtil.decimalVal(GXutil.substring(oReader.getValue(), 9, 2), "."))), (byte) (DecimalUtil.decToDouble(CommonUtil.decimalVal(GXutil.substring(oReader.getValue(), 12, 2), "."))), (byte) (DecimalUtil.decToDouble(CommonUtil.decimalVal(GXutil.substring(oReader.getValue(), 15, 2), "."))), (byte) (DecimalUtil.decToDouble(CommonUtil.decimalVal(GXutil.substring(oReader.getValue(), 18, 2), "."))));
					}
					readElement = true;
					if (GXSoapError > 0) {
						readOk = (short) (1);
					}
					GXSoapError = oReader.read();
				}
				if (GXutil.strcmp2(oReader.getLocalName(), "UserEventMessageData")) {
					gxTv_SdtUser_Usereventmessagedata = oReader.getValue();
					readElement = true;
					if (GXSoapError > 0) {
						readOk = (short) (1);
					}
					GXSoapError = oReader.read();
				}
				if (GXutil.strcmp2(oReader.getLocalName(), "Mode")) {
					gxTv_SdtUser_Mode = oReader.getValue();
					readElement = true;
					if (GXSoapError > 0) {
						readOk = (short) (1);
					}
					GXSoapError = oReader.read();
				}
				if (GXutil.strcmp2(oReader.getLocalName(), "Initialized")) {
					gxTv_SdtUser_Initialized = (short) (getnumericvalue(oReader.getValue()));
					readElement = true;
					if (GXSoapError > 0) {
						readOk = (short) (1);
					}
					GXSoapError = oReader.read();
				}
				if (GXutil.strcmp2(oReader.getLocalName(), "UserId_Z")) {
					gxTv_SdtUser_Userid_Z = GXutil.strToGuid(oReader.getValue());
					readElement = true;
					if (GXSoapError > 0) {
						readOk = (short) (1);
					}
					GXSoapError = oReader.read();
				}
				if (GXutil.strcmp2(oReader.getLocalName(), "UserName_Z")) {
					gxTv_SdtUser_Username_Z = oReader.getValue();
					readElement = true;
					if (GXSoapError > 0) {
						readOk = (short) (1);
					}
					GXSoapError = oReader.read();
				}
				if (GXutil.strcmp2(oReader.getLocalName(), "UserRegisteredDateTime_Z")) {
					if ((GXutil.strcmp(oReader.getValue(), "") == 0) || (oReader.existsAttribute("xsi:nil") == 1)) {
						gxTv_SdtUser_Userregistereddatetime_Z = GXutil.resetTime(GXutil.nullDate());
					} else {
						gxTv_SdtUser_Userregistereddatetime_Z = localUtil.ymdhmsToT((short) (DecimalUtil.decToDouble(CommonUtil.decimalVal(GXutil.substring(oReader.getValue(), 1, 4), "."))), (byte) (DecimalUtil.decToDouble(CommonUtil.decimalVal(GXutil.substring(oReader.getValue(), 6, 2), "."))), (byte) (DecimalUtil.decToDouble(CommonUtil.decimalVal(GXutil.substring(oReader.getValue(), 9, 2), "."))), (byte) (DecimalUtil.decToDouble(CommonUtil.decimalVal(GXutil.substring(oReader.getValue(), 12, 2), "."))), (byte) (DecimalUtil.decToDouble(CommonUtil.decimalVal(GXutil.substring(oReader.getValue(), 15, 2), "."))), (byte) (DecimalUtil.decToDouble(CommonUtil.decimalVal(GXutil.substring(oReader.getValue(), 18, 2), "."))));
					}
					readElement = true;
					if (GXSoapError > 0) {
						readOk = (short) (1);
					}
					GXSoapError = oReader.read();
				}
				if (!readElement) {
					readOk = (short) (1);
					GXSoapError = oReader.read();
				}
				nOutParmCount = (short) (nOutParmCount + 1);
				if ((readOk == 0) || formatError) {
					context.globals.sSOAPErrMsg += "Error reading " + sTagName + GXutil.newLine();
					context.globals.sSOAPErrMsg += "Message: " + oReader.readRawXML();
					GXSoapError = (short) (nOutParmCount * -1);
				}
			}
		}
		return GXSoapError;
	}

	public void writexml(XMLWriter oWriter,
						 String sName,
						 String sNameSpace) {
		writexml(oWriter, sName, sNameSpace, true);
	}

	public void writexml(XMLWriter oWriter,
						 String sName,
						 String sNameSpace,
						 boolean sIncludeState) {
		if ((GXutil.strcmp("", sName) == 0)) {
			sName = "User";
		}
		if ((GXutil.strcmp("", sNameSpace) == 0)) {
			sNameSpace = "Issue95858";
		}
		oWriter.writeStartElement(sName);
		if (GXutil.strcmp(GXutil.left(sNameSpace, 10), "[*:nosend]") != 0) {
			oWriter.writeAttribute("xmlns", sNameSpace);
		} else {
			sNameSpace = GXutil.right(sNameSpace, GXutil.len(sNameSpace) - 10);
		}
		oWriter.writeElement("UserId", gxTv_SdtUser_Userid.toString());
		if (GXutil.strcmp(sNameSpace, "Issue95858") != 0) {
			oWriter.writeAttribute("xmlns", "Issue95858");
		}
		oWriter.writeElement("UserName", gxTv_SdtUser_Username);
		if (GXutil.strcmp(sNameSpace, "Issue95858") != 0) {
			oWriter.writeAttribute("xmlns", "Issue95858");
		}
		sDateCnv = "";
		sNumToPad = GXutil.trim(GXutil.str(GXutil.year(gxTv_SdtUser_Userregistereddatetime), 10, 0));
		sDateCnv += GXutil.substring("0000", 1, 4 - GXutil.len(sNumToPad)) + sNumToPad;
		sDateCnv += "-";
		sNumToPad = GXutil.trim(GXutil.str(GXutil.month(gxTv_SdtUser_Userregistereddatetime), 10, 0));
		sDateCnv += GXutil.substring("00", 1, 2 - GXutil.len(sNumToPad)) + sNumToPad;
		sDateCnv += "-";
		sNumToPad = GXutil.trim(GXutil.str(GXutil.day(gxTv_SdtUser_Userregistereddatetime), 10, 0));
		sDateCnv += GXutil.substring("00", 1, 2 - GXutil.len(sNumToPad)) + sNumToPad;
		sDateCnv += "T";
		sNumToPad = GXutil.trim(GXutil.str(GXutil.hour(gxTv_SdtUser_Userregistereddatetime), 10, 0));
		sDateCnv += GXutil.substring("00", 1, 2 - GXutil.len(sNumToPad)) + sNumToPad;
		sDateCnv += ":";
		sNumToPad = GXutil.trim(GXutil.str(GXutil.minute(gxTv_SdtUser_Userregistereddatetime), 10, 0));
		sDateCnv += GXutil.substring("00", 1, 2 - GXutil.len(sNumToPad)) + sNumToPad;
		sDateCnv += ":";
		sNumToPad = GXutil.trim(GXutil.str(GXutil.second(gxTv_SdtUser_Userregistereddatetime), 10, 0));
		sDateCnv += GXutil.substring("00", 1, 2 - GXutil.len(sNumToPad)) + sNumToPad;
		oWriter.writeElement("UserRegisteredDateTime", sDateCnv);
		if (GXutil.strcmp(sNameSpace, "Issue95858") != 0) {
			oWriter.writeAttribute("xmlns", "Issue95858");
		}
		oWriter.writeElement("UserEventMessageData", gxTv_SdtUser_Usereventmessagedata);
		if (GXutil.strcmp(sNameSpace, "Issue95858") != 0) {
			oWriter.writeAttribute("xmlns", "Issue95858");
		}
		if (sIncludeState) {
			oWriter.writeElement("Mode", gxTv_SdtUser_Mode);
			if (GXutil.strcmp(sNameSpace, "Issue95858") != 0) {
				oWriter.writeAttribute("xmlns", "Issue95858");
			}
			oWriter.writeElement("Initialized", GXutil.trim(GXutil.str(gxTv_SdtUser_Initialized, 4, 0)));
			if (GXutil.strcmp(sNameSpace, "Issue95858") != 0) {
				oWriter.writeAttribute("xmlns", "Issue95858");
			}
			oWriter.writeElement("UserId_Z", gxTv_SdtUser_Userid_Z.toString());
			if (GXutil.strcmp(sNameSpace, "Issue95858") != 0) {
				oWriter.writeAttribute("xmlns", "Issue95858");
			}
			oWriter.writeElement("UserName_Z", gxTv_SdtUser_Username_Z);
			if (GXutil.strcmp(sNameSpace, "Issue95858") != 0) {
				oWriter.writeAttribute("xmlns", "Issue95858");
			}
			sDateCnv = "";
			sNumToPad = GXutil.trim(GXutil.str(GXutil.year(gxTv_SdtUser_Userregistereddatetime_Z), 10, 0));
			sDateCnv += GXutil.substring("0000", 1, 4 - GXutil.len(sNumToPad)) + sNumToPad;
			sDateCnv += "-";
			sNumToPad = GXutil.trim(GXutil.str(GXutil.month(gxTv_SdtUser_Userregistereddatetime_Z), 10, 0));
			sDateCnv += GXutil.substring("00", 1, 2 - GXutil.len(sNumToPad)) + sNumToPad;
			sDateCnv += "-";
			sNumToPad = GXutil.trim(GXutil.str(GXutil.day(gxTv_SdtUser_Userregistereddatetime_Z), 10, 0));
			sDateCnv += GXutil.substring("00", 1, 2 - GXutil.len(sNumToPad)) + sNumToPad;
			sDateCnv += "T";
			sNumToPad = GXutil.trim(GXutil.str(GXutil.hour(gxTv_SdtUser_Userregistereddatetime_Z), 10, 0));
			sDateCnv += GXutil.substring("00", 1, 2 - GXutil.len(sNumToPad)) + sNumToPad;
			sDateCnv += ":";
			sNumToPad = GXutil.trim(GXutil.str(GXutil.minute(gxTv_SdtUser_Userregistereddatetime_Z), 10, 0));
			sDateCnv += GXutil.substring("00", 1, 2 - GXutil.len(sNumToPad)) + sNumToPad;
			sDateCnv += ":";
			sNumToPad = GXutil.trim(GXutil.str(GXutil.second(gxTv_SdtUser_Userregistereddatetime_Z), 10, 0));
			sDateCnv += GXutil.substring("00", 1, 2 - GXutil.len(sNumToPad)) + sNumToPad;
			oWriter.writeElement("UserRegisteredDateTime_Z", sDateCnv);
			if (GXutil.strcmp(sNameSpace, "Issue95858") != 0) {
				oWriter.writeAttribute("xmlns", "Issue95858");
			}
		}
		oWriter.writeEndElement();
	}

	public long getnumericvalue(String value) {
		if (GXutil.notNumeric(value)) {
			formatError = true;
		}
		return GXutil.lval(value);
	}

	public void tojson() {
		tojson(true);
	}

	public void tojson(boolean includeState) {
		tojson(includeState, true);
	}

	public void tojson(boolean includeState,
					   boolean includeNonInitialized) {
		AddObjectProperty("UserId", gxTv_SdtUser_Userid, false, includeNonInitialized);
		AddObjectProperty("UserName", gxTv_SdtUser_Username, false, includeNonInitialized);
		datetime_STZ = gxTv_SdtUser_Userregistereddatetime;
		sDateCnv = "";
		sNumToPad = GXutil.trim(GXutil.str(GXutil.year(datetime_STZ), 10, 0));
		sDateCnv += GXutil.substring("0000", 1, 4 - GXutil.len(sNumToPad)) + sNumToPad;
		sDateCnv += "-";
		sNumToPad = GXutil.trim(GXutil.str(GXutil.month(datetime_STZ), 10, 0));
		sDateCnv += GXutil.substring("00", 1, 2 - GXutil.len(sNumToPad)) + sNumToPad;
		sDateCnv += "-";
		sNumToPad = GXutil.trim(GXutil.str(GXutil.day(datetime_STZ), 10, 0));
		sDateCnv += GXutil.substring("00", 1, 2 - GXutil.len(sNumToPad)) + sNumToPad;
		sDateCnv += "T";
		sNumToPad = GXutil.trim(GXutil.str(GXutil.hour(datetime_STZ), 10, 0));
		sDateCnv += GXutil.substring("00", 1, 2 - GXutil.len(sNumToPad)) + sNumToPad;
		sDateCnv += ":";
		sNumToPad = GXutil.trim(GXutil.str(GXutil.minute(datetime_STZ), 10, 0));
		sDateCnv += GXutil.substring("00", 1, 2 - GXutil.len(sNumToPad)) + sNumToPad;
		sDateCnv += ":";
		sNumToPad = GXutil.trim(GXutil.str(GXutil.second(datetime_STZ), 10, 0));
		sDateCnv += GXutil.substring("00", 1, 2 - GXutil.len(sNumToPad)) + sNumToPad;
		AddObjectProperty("UserRegisteredDateTime", sDateCnv, false, includeNonInitialized);
		AddObjectProperty("UserEventMessageData", gxTv_SdtUser_Usereventmessagedata, false, includeNonInitialized);
		if (includeState) {
			AddObjectProperty("Mode", gxTv_SdtUser_Mode, false, includeNonInitialized);
			AddObjectProperty("Initialized", gxTv_SdtUser_Initialized, false, includeNonInitialized);
			AddObjectProperty("UserId_Z", gxTv_SdtUser_Userid_Z, false, includeNonInitialized);
			AddObjectProperty("UserName_Z", gxTv_SdtUser_Username_Z, false, includeNonInitialized);
			datetime_STZ = gxTv_SdtUser_Userregistereddatetime_Z;
			sDateCnv = "";
			sNumToPad = GXutil.trim(GXutil.str(GXutil.year(datetime_STZ), 10, 0));
			sDateCnv += GXutil.substring("0000", 1, 4 - GXutil.len(sNumToPad)) + sNumToPad;
			sDateCnv += "-";
			sNumToPad = GXutil.trim(GXutil.str(GXutil.month(datetime_STZ), 10, 0));
			sDateCnv += GXutil.substring("00", 1, 2 - GXutil.len(sNumToPad)) + sNumToPad;
			sDateCnv += "-";
			sNumToPad = GXutil.trim(GXutil.str(GXutil.day(datetime_STZ), 10, 0));
			sDateCnv += GXutil.substring("00", 1, 2 - GXutil.len(sNumToPad)) + sNumToPad;
			sDateCnv += "T";
			sNumToPad = GXutil.trim(GXutil.str(GXutil.hour(datetime_STZ), 10, 0));
			sDateCnv += GXutil.substring("00", 1, 2 - GXutil.len(sNumToPad)) + sNumToPad;
			sDateCnv += ":";
			sNumToPad = GXutil.trim(GXutil.str(GXutil.minute(datetime_STZ), 10, 0));
			sDateCnv += GXutil.substring("00", 1, 2 - GXutil.len(sNumToPad)) + sNumToPad;
			sDateCnv += ":";
			sNumToPad = GXutil.trim(GXutil.str(GXutil.second(datetime_STZ), 10, 0));
			sDateCnv += GXutil.substring("00", 1, 2 - GXutil.len(sNumToPad)) + sNumToPad;
			AddObjectProperty("UserRegisteredDateTime_Z", sDateCnv, false, includeNonInitialized);
		}
	}

	public void updateDirties(SdtUser sdt) {
		if (sdt.IsDirty("UserId")) {
			gxTv_SdtUser_N = (byte) (0);
			gxTv_SdtUser_Userid = sdt.getgxTv_SdtUser_Userid();
		}
		if (sdt.IsDirty("UserName")) {
			gxTv_SdtUser_N = (byte) (0);
			gxTv_SdtUser_Username = sdt.getgxTv_SdtUser_Username();
		}
		if (sdt.IsDirty("UserRegisteredDateTime")) {
			gxTv_SdtUser_N = (byte) (0);
			gxTv_SdtUser_Userregistereddatetime = sdt.getgxTv_SdtUser_Userregistereddatetime();
		}
		if (sdt.IsDirty("UserEventMessageData")) {
			gxTv_SdtUser_N = (byte) (0);
			gxTv_SdtUser_Usereventmessagedata = sdt.getgxTv_SdtUser_Usereventmessagedata();
		}
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
	private short readOk;
	private short nOutParmCount;
	private String gxTv_SdtUser_Username;
	private String gxTv_SdtUser_Mode;
	private String gxTv_SdtUser_Username_Z;
	private String sTagName;
	private String sDateCnv;
	private String sNumToPad;
	private Date gxTv_SdtUser_Userregistereddatetime;
	private Date gxTv_SdtUser_Userregistereddatetime_Z;
	private Date datetime_STZ;
	private boolean readElement;
	private boolean formatError;
	private String gxTv_SdtUser_Usereventmessagedata;
	private UUID AV4UserId;
	private UUID gxTv_SdtUser_Userid;
	private UUID gxTv_SdtUser_Userid_Z;
}

