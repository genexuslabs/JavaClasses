package com.genexus.genexusserverlessapi;

import com.genexus.GXSimpleCollection;
import com.genexus.GXutil;
import com.genexus.Globals;
import com.genexus.GxUserType;
import com.genexus.ModelContext;
import com.genexus.xml.XMLReader;
import com.genexus.xml.XMLWriter;
import java.util.HashMap;

public final class SdtEventMessagesList extends GxUserType {
	private static HashMap mapper = new HashMap();
	protected byte gxTv_SdtEventMessagesList_Items_N;
	protected byte sdtIsNull;
	protected short readOk;
	protected short nOutParmCount;
	protected String sTagName;
	protected boolean readElement;
	protected boolean formatError;
	protected GXSimpleCollection<String> gxTv_SdtEventMessagesList_Items;

	public SdtEventMessagesList() {
		this(new ModelContext(SdtEventMessagesList.class));
	}

	public SdtEventMessagesList(ModelContext context) {
		super(context, "SdtEventMessagesList");
		this.gxTv_SdtEventMessagesList_Items = null;
	}

	public SdtEventMessagesList(int remoteHandle, ModelContext context) {
		super(remoteHandle, context, "SdtEventMessagesList");
		this.gxTv_SdtEventMessagesList_Items = null;
	}

	public SdtEventMessagesList(StructSdtEventMessagesList struct) {
		this();
		this.setStruct(struct);
	}

	public String getJsonMap(String value) {
		return (String)mapper.get(value);
	}

	public short readxml(XMLReader oReader, String sName) {
		short GXSoapError = 1;
		this.formatError = false;
		this.sTagName = oReader.getName();
		if (oReader.getIsSimple() == 0) {
			GXSoapError = oReader.read();
			this.nOutParmCount = 0;

			while(true) {
				do {
					if (GXutil.strcmp(oReader.getName(), this.sTagName) == 0 && oReader.getNodeType() != 1 || GXSoapError <= 0) {
						return GXSoapError;
					}

					this.readOk = 0;
					this.readElement = false;
					if (GXutil.strcmp2(oReader.getLocalName(), "items")) {
						if (this.gxTv_SdtEventMessagesList_Items == null) {
							this.gxTv_SdtEventMessagesList_Items = new GXSimpleCollection(String.class, "internal", "");
						}

						if (oReader.getIsSimple() == 0) {
							GXSoapError = this.gxTv_SdtEventMessagesList_Items.readxmlcollection(oReader, "items", "Item");
						}

						this.readElement = true;
						if (GXSoapError > 0) {
							this.readOk = 1;
						}

						if (GXutil.strcmp2(oReader.getLocalName(), "items")) {
							GXSoapError = oReader.read();
						}
					}

					if (!this.readElement) {
						this.readOk = 1;
						GXSoapError = oReader.read();
					}

					++this.nOutParmCount;
				} while(this.readOk != 0 && !this.formatError);

				StringBuilder var10000 = new StringBuilder();
				Globals var10002 = this.context.globals;
				var10002.sSOAPErrMsg = var10000.append(var10002.sSOAPErrMsg).append("Error reading ").append(this.sTagName).append(GXutil.newLine()).toString();
				var10000 = new StringBuilder();
				var10002 = this.context.globals;
				var10002.sSOAPErrMsg = var10000.append(var10002.sSOAPErrMsg).append("Message: ").append(oReader.readRawXML()).toString();
				GXSoapError = (short)(this.nOutParmCount * -1);
			}
		} else {
			return GXSoapError;
		}
	}

	public void writexml(XMLWriter oWriter, String sName, String sNameSpace) {
		this.writexml(oWriter, sName, sNameSpace, true);
	}

	public void writexml(XMLWriter oWriter, String sName, String sNameSpace, boolean sIncludeState) {
		if (GXutil.strcmp("", sName) == 0) {
			sName = "EventMessagesList";
		}

		if (GXutil.strcmp("", sNameSpace) == 0) {
			sNameSpace = "ServerlessAPI";
		}

		oWriter.writeStartElement(sName);
		if (GXutil.strcmp(GXutil.left(sNameSpace, 10), "[*:nosend]") != 0) {
			oWriter.writeAttribute("xmlns", sNameSpace);
		} else {
			sNameSpace = GXutil.right(sNameSpace, GXutil.len(sNameSpace) - 10);
		}

		if (this.gxTv_SdtEventMessagesList_Items != null) {
			String sNameSpace1;
			if (GXutil.strcmp(sNameSpace, "ServerlessAPI") == 0) {
				sNameSpace1 = "[*:nosend]ServerlessAPI";
			} else {
				sNameSpace1 = "ServerlessAPI";
			}

			this.gxTv_SdtEventMessagesList_Items.writexmlcollection(oWriter, "items", sNameSpace1, "Item", sNameSpace1);
		}

		oWriter.writeEndElement();
	}

	public void tojson() {
		this.tojson(true);
	}

	public void tojson(boolean includeState) {
		this.tojson(includeState, true);
	}

	public void tojson(boolean includeState, boolean includeNonInitialized) {
		if (this.gxTv_SdtEventMessagesList_Items != null) {
			this.AddObjectProperty("items", this.gxTv_SdtEventMessagesList_Items, false, false);
		}

	}

	public GXSimpleCollection<String> getgxTv_SdtEventMessagesList_Items() {
		if (this.gxTv_SdtEventMessagesList_Items == null) {
			this.gxTv_SdtEventMessagesList_Items = new GXSimpleCollection(String.class, "internal", "");
		}

		this.gxTv_SdtEventMessagesList_Items_N = 0;
		this.sdtIsNull = 0;
		return this.gxTv_SdtEventMessagesList_Items;
	}

	public void setgxTv_SdtEventMessagesList_Items(GXSimpleCollection<String> value) {
		this.gxTv_SdtEventMessagesList_Items_N = 0;
		this.sdtIsNull = 0;
		this.gxTv_SdtEventMessagesList_Items = value;
	}

	public void setgxTv_SdtEventMessagesList_Items_SetNull() {
		this.gxTv_SdtEventMessagesList_Items_N = 1;
		this.gxTv_SdtEventMessagesList_Items = null;
	}

	public boolean getgxTv_SdtEventMessagesList_Items_IsNull() {
		return this.gxTv_SdtEventMessagesList_Items == null;
	}

	public byte getgxTv_SdtEventMessagesList_Items_N() {
		return this.gxTv_SdtEventMessagesList_Items_N;
	}

	public void initialize(int remoteHandle) {
		this.initialize();
	}

	public void initialize() {
		this.gxTv_SdtEventMessagesList_Items_N = 1;
		this.sdtIsNull = 1;
		this.sTagName = "";
	}

	public byte isNull() {
		return this.sdtIsNull;
	}

	public SdtEventMessagesList Clone() {
		return (SdtEventMessagesList)((SdtEventMessagesList)this.clone());
	}

	public void setStruct(StructSdtEventMessagesList struct) {
		if (struct != null) {
			this.setgxTv_SdtEventMessagesList_Items(new GXSimpleCollection(String.class, "internal", "", struct.getItems()));
		}

	}

	public StructSdtEventMessagesList getStruct() {
		StructSdtEventMessagesList struct = new StructSdtEventMessagesList();
		struct.setItems(this.getgxTv_SdtEventMessagesList_Items().getStruct());
		return struct;
	}
}

