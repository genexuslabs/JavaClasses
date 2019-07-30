package com.genexus.webpanels.gridstate ;
import com.genexus.GXBaseCollection;
import com.genexus.GXutil;
import com.genexus.ModelContext;
import com.genexus.xml.*;
import java.util.*;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

public final class SdtGridState extends GXXMLSerializable implements Cloneable, java.io.Serializable {
	public static final ILogger logger = LogManager.getLogger(SdtGridState.class);

	public SdtGridState() {
		this(new ModelContext(SdtGridState.class));
	}

	public SdtGridState(ModelContext context) {
		super(context, "SdtGridState");
	}

	public SdtGridState(int remoteHandle, ModelContext context) {
		super(remoteHandle, context, "SdtGridState");
	}

	public SdtGridState(StructSdtGridState struct) {
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
				if (GXutil.strcmp2(oReader.getLocalName(), "CurrentPage")) {
					gxTv_SdtGridState_Currentpage = (int) (getnumericvalue(oReader));
					readOk = (short) (1);
					GXSoapError = oReader.read();
				}
				if (GXutil.strcmp2(oReader.getLocalName(), "OrderedBy")) {
					gxTv_SdtGridState_Orderedby = (short) (getnumericvalue(oReader));
					if (GXSoapError > 0) {
						readOk = (short) (1);
					}
					GXSoapError = oReader.read();
				}
				if (GXutil.strcmp2(oReader.getLocalName(), "InputValues")) {
					if (gxTv_SdtGridState_Inputvalues == null) {
						gxTv_SdtGridState_Inputvalues = new GXBaseCollection<SdtGridState_InputValuesItem>(SdtGridState_InputValuesItem.class, "GridState.InputValuesItem", "GeneXus", remoteHandle);
					}
					if (oReader.getIsSimple() == 0) {
						GXSoapError = gxTv_SdtGridState_Inputvalues.readxmlcollection(oReader, "InputValues", "InputValuesItem");
					}
					if (GXSoapError > 0) {
						readOk = (short) (1);
					}
					if (GXutil.strcmp2(oReader.getLocalName(), "InputValues")) {
						GXSoapError = oReader.read();
					}
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
			sName = "GridState";
		}
		if ((GXutil.strcmp("", sNameSpace) == 0)) {
			sNameSpace = "GeneXus";
		}
		oWriter.writeStartElement(sName);
		if (GXutil.strcmp(GXutil.left(sNameSpace, 10), "[*:nosend]") != 0) {
			oWriter.writeAttribute("xmlns", sNameSpace);
		} else {
			sNameSpace = GXutil.right(sNameSpace, GXutil.len(sNameSpace) - 10);
		}
		oWriter.writeElement("CurrentPage", GXutil.trim(GXutil.str(gxTv_SdtGridState_Currentpage, 5, 0)));
		if (GXutil.strcmp(sNameSpace, "GeneXus") != 0) {
			oWriter.writeAttribute("xmlns", "GeneXus");
		}
		oWriter.writeElement("OrderedBy", GXutil.trim(GXutil.str(gxTv_SdtGridState_Orderedby, 4, 0)));
		if (GXutil.strcmp(sNameSpace, "GeneXus") != 0) {
			oWriter.writeAttribute("xmlns", "GeneXus");
		}
		if (gxTv_SdtGridState_Inputvalues != null) {
			String sNameSpace1;
			if (GXutil.strcmp(sNameSpace, "GeneXus") == 0) {
				sNameSpace1 = "[*:nosend]" + "GeneXus";
			} else {
				sNameSpace1 = "GeneXus";
			}
			gxTv_SdtGridState_Inputvalues.writexmlcollection(oWriter, "InputValues", sNameSpace1, "InputValuesItem", sNameSpace1);
		}
		oWriter.writeEndElement();
	}

	public long getnumericvalue(com.genexus.xml.XMLReader oReader) {
		if (GXutil.notNumeric(oReader.getValue())) {
			formatError = true;
		}
		return GXutil.lval(oReader.getValue());
	}

	public void tojson() {
		tojson(true);
	}

	public void tojson(boolean includeState) {
		tojson(includeState, true);
	}

	public void tojson(boolean includeState,
					   boolean includeNonInitialized) {
		AddObjectProperty("CurrentPage", gxTv_SdtGridState_Currentpage, false, false);
		AddObjectProperty("OrderedBy", gxTv_SdtGridState_Orderedby, false, false);
		if (gxTv_SdtGridState_Inputvalues != null) {
			AddObjectProperty("InputValues", gxTv_SdtGridState_Inputvalues, false, false);
		}
	}

	public int getgxTv_SdtGridState_Currentpage() {
		return gxTv_SdtGridState_Currentpage;
	}

	public void setgxTv_SdtGridState_Currentpage(int value) {
		gxTv_SdtGridState_Currentpage = value;
	}

	public short getgxTv_SdtGridState_Orderedby() {
		return gxTv_SdtGridState_Orderedby;
	}

	public void setgxTv_SdtGridState_Orderedby(short value) {
		gxTv_SdtGridState_Orderedby = value;
	}

	public GXBaseCollection<SdtGridState_InputValuesItem> getgxTv_SdtGridState_Inputvalues() {
		if (gxTv_SdtGridState_Inputvalues == null) {
			gxTv_SdtGridState_Inputvalues = new GXBaseCollection<SdtGridState_InputValuesItem>(SdtGridState_InputValuesItem.class, "GridState.InputValuesItem", "GeneXus", remoteHandle);
		}
		gxTv_SdtGridState_Inputvalues_N = (byte) (0);
		return gxTv_SdtGridState_Inputvalues;
	}

	public void setgxTv_SdtGridState_Inputvalues(GXBaseCollection<SdtGridState_InputValuesItem> value) {
		gxTv_SdtGridState_Inputvalues_N = (byte) (0);
		gxTv_SdtGridState_Inputvalues = value;
	}

	public void setgxTv_SdtGridState_Inputvalues_SetNull() {
		gxTv_SdtGridState_Inputvalues_N = (byte) (1);
		gxTv_SdtGridState_Inputvalues = null;
	}

	public boolean getgxTv_SdtGridState_Inputvalues_IsNull() {
		return (gxTv_SdtGridState_Inputvalues == null);
	}

	public byte getgxTv_SdtGridState_Inputvalues_N() {
		return gxTv_SdtGridState_Inputvalues_N;
	}

	public void initialize(int remoteHandle) {
		initialize();
	}

	public void initialize() {
		gxTv_SdtGridState_Inputvalues_N = (byte) (1);
		sTagName = "";
	}

	public SdtGridState Clone() {
		return (SdtGridState) (clone());
	}

	public void setStruct(StructSdtGridState struct) {
		setgxTv_SdtGridState_Currentpage(struct.getCurrentpage());
		setgxTv_SdtGridState_Orderedby(struct.getOrderedby());
		GXBaseCollection<SdtGridState_InputValuesItem> gxTv_SdtGridState_Inputvalues_aux = new GXBaseCollection<SdtGridState_InputValuesItem>(SdtGridState_InputValuesItem.class, "GridState.InputValuesItem", "GeneXus", remoteHandle);
		Vector<StructSdtGridState_InputValuesItem> gxTv_SdtGridState_Inputvalues_aux1 = struct.getInputvalues();
		if (gxTv_SdtGridState_Inputvalues_aux1 != null) {
			for (int i = 0; i < gxTv_SdtGridState_Inputvalues_aux1.size(); i++) {
				gxTv_SdtGridState_Inputvalues_aux.add(new SdtGridState_InputValuesItem(gxTv_SdtGridState_Inputvalues_aux1.elementAt(i)));
			}
		}
		setgxTv_SdtGridState_Inputvalues(gxTv_SdtGridState_Inputvalues_aux);
	}

	public StructSdtGridState getStruct() {
		StructSdtGridState struct = new StructSdtGridState();
		struct.setCurrentpage(getgxTv_SdtGridState_Currentpage());
		struct.setOrderedby(getgxTv_SdtGridState_Orderedby());
		struct.setInputvalues(getgxTv_SdtGridState_Inputvalues().getStruct());
		return struct;
	}

	protected byte gxTv_SdtGridState_Inputvalues_N;
	protected short gxTv_SdtGridState_Orderedby;
	protected short readOk;
	protected short nOutParmCount;
	protected int gxTv_SdtGridState_Currentpage;
	protected String sTagName;
	protected boolean formatError;
	protected GXBaseCollection<SdtGridState_InputValuesItem> gxTv_SdtGridState_Inputvalues_aux;
	protected GXBaseCollection<SdtGridState_InputValuesItem> gxTv_SdtGridState_Inputvalues = null;
}

