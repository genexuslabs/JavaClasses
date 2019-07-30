package com.genexus.webpanels.gridstate ;
import com.genexus.GXutil;
import com.genexus.ModelContext;
import com.genexus.xml.*;

public final  class SdtGridState_InputValuesItem extends GXXMLSerializable implements Cloneable, java.io.Serializable {
	public SdtGridState_InputValuesItem() {
		this(new ModelContext(SdtGridState_InputValuesItem.class));
	}

	public SdtGridState_InputValuesItem(ModelContext context) {
		super(context, "SdtGridState_InputValuesItem");
	}

	public SdtGridState_InputValuesItem(int remoteHandle,
										ModelContext context) {
		super(remoteHandle, context, "SdtGridState_InputValuesItem");
	}

	public SdtGridState_InputValuesItem(StructSdtGridState_InputValuesItem struct) {
		this();
		setStruct(struct);
	}

	private static java.util.HashMap mapper = new java.util.HashMap();

	static {
	}

	public String getJsonMap(String value) {
		return (String) mapper.get(value);
	}

	public short readxml(com.genexus.xml.XMLReader oReader,
						 String sName) {
		short GXSoapError = 1;
		formatError = false;
		sTagName = oReader.getName();
		if (oReader.getIsSimple() == 0) {
			GXSoapError = oReader.read();
			nOutParmCount = (short) (0);
			while (((GXutil.strcmp(oReader.getName(), sTagName) != 0) || (oReader.getNodeType() == 1)) && (GXSoapError > 0)) {
				readOk = (short) (0);
				if (GXutil.strcmp2(oReader.getLocalName(), "Name")) {
					gxTv_SdtGridState_InputValuesItem_Name = oReader.getValue();
					readOk = (short) (1);
					GXSoapError = oReader.read();
				}
				if (GXutil.strcmp2(oReader.getLocalName(), "Value")) {
					gxTv_SdtGridState_InputValuesItem_Value = oReader.getValue();
					if (GXSoapError > 0) {
						readOk = (short) (1);
					}
					GXSoapError = oReader.read();
				}
				nOutParmCount = (short) (nOutParmCount + 1);
				if ((readOk == 0) || formatError) {
					context.globals.sSOAPErrMsg = context.globals.sSOAPErrMsg + "Error reading " + sTagName + GXutil.newLine();
					context.globals.sSOAPErrMsg = context.globals.sSOAPErrMsg + "Message: " + oReader.readRawXML();
					GXSoapError = (short) (nOutParmCount * -1);
				}
			}
		}
		return GXSoapError;
	}

	public void writexml(com.genexus.xml.XMLWriter oWriter,
						 String sName,
						 String sNameSpace) {
		writexml(oWriter, sName, sNameSpace, true);
	}

	public void writexml(com.genexus.xml.XMLWriter oWriter,
						 String sName,
						 String sNameSpace,
						 boolean sIncludeState) {
		if ((GXutil.strcmp("", sName) == 0)) {
			sName = "GeneXus\\Common\\GridState.InputValuesItem";
		}
		oWriter.writeStartElement(sName);
		if (GXutil.strcmp(GXutil.left(sNameSpace, 10), "[*:nosend]") != 0) {
			oWriter.writeAttribute("xmlns", sNameSpace);
		} else {
			sNameSpace = GXutil.right(sNameSpace, GXutil.len(sNameSpace) - 10);
		}
		oWriter.writeElement("Name", GXutil.rtrim(gxTv_SdtGridState_InputValuesItem_Name));
		if (GXutil.strcmp(sNameSpace, "GeneXus") != 0) {
			oWriter.writeAttribute("xmlns", "GeneXus");
		}
		oWriter.writeElement("Value", GXutil.rtrim(gxTv_SdtGridState_InputValuesItem_Value));
		if (GXutil.strcmp(sNameSpace, "GeneXus") != 0) {
			oWriter.writeAttribute("xmlns", "GeneXus");
		}
		oWriter.writeEndElement();
	}

	public void tojson() {
		tojson(true);
	}

	public void tojson(boolean includeState) {
		tojson(includeState, true);
	}

	public void tojson(boolean includeState,
					   boolean includeNonInitialized) {
		AddObjectProperty("Name", gxTv_SdtGridState_InputValuesItem_Name, false, false);
		AddObjectProperty("Value", gxTv_SdtGridState_InputValuesItem_Value, false, false);
	}

	public String getgxTv_SdtGridState_InputValuesItem_Name() {
		return gxTv_SdtGridState_InputValuesItem_Name;
	}

	public void setgxTv_SdtGridState_InputValuesItem_Name(String value) {
		gxTv_SdtGridState_InputValuesItem_Name = value;
	}

	public String getgxTv_SdtGridState_InputValuesItem_Value() {
		return gxTv_SdtGridState_InputValuesItem_Value;
	}

	public void setgxTv_SdtGridState_InputValuesItem_Value(String value) {
		gxTv_SdtGridState_InputValuesItem_Value = value;
	}

	public void initialize(int remoteHandle) {
		initialize();
	}

	public void initialize() {
		gxTv_SdtGridState_InputValuesItem_Name = "";
		gxTv_SdtGridState_InputValuesItem_Value = "";
		sTagName = "";
	}

	public SdtGridState_InputValuesItem Clone() {
		return (SdtGridState_InputValuesItem) (clone());
	}

	public void setStruct(StructSdtGridState_InputValuesItem struct) {
		setgxTv_SdtGridState_InputValuesItem_Name(struct.getName());
		setgxTv_SdtGridState_InputValuesItem_Value(struct.getValue());
	}

	public StructSdtGridState_InputValuesItem getStruct() {
		StructSdtGridState_InputValuesItem struct = new StructSdtGridState_InputValuesItem();
		struct.setName(getgxTv_SdtGridState_InputValuesItem_Name());
		struct.setValue(getgxTv_SdtGridState_InputValuesItem_Value());
		return struct;
	}

	protected short readOk;
	protected short nOutParmCount;
	protected String sTagName;
	protected boolean formatError;
	protected String gxTv_SdtGridState_InputValuesItem_Name;
	protected String gxTv_SdtGridState_InputValuesItem_Value;
}

