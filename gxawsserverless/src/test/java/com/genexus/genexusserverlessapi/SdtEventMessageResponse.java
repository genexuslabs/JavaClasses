package com.genexus.genexusserverlessapi;

import com.genexus.*;
import com.genexus.*;
import com.genexus.xml.*;
import com.genexus.search.*;
import com.genexus.webpanels.*;

import java.util.*;

public final class SdtEventMessageResponse extends GxUserType {
	public SdtEventMessageResponse() {
		this(new ModelContext(SdtEventMessageResponse.class));
	}

	public SdtEventMessageResponse(ModelContext context) {
		super(context, "SdtEventMessageResponse");
	}

	public SdtEventMessageResponse(int remoteHandle,
								   ModelContext context) {
		super(remoteHandle, context, "SdtEventMessageResponse");
	}

	public SdtEventMessageResponse(StructSdtEventMessageResponse struct) {
		this();
		setStruct(struct);
	}

	private static java.util.HashMap mapper = new java.util.HashMap();

	static {
	}

	public String getJsonMap(String value) {
		return (String) mapper.get(value);
	}

	public void tojson() {
		tojson(true);
	}

	public void tojson(boolean includeState) {
		tojson(includeState, true);
	}

	public void tojson(boolean includeState,
					   boolean includeNonInitialized) {
		AddObjectProperty("Handled", gxTv_SdtEventMessageResponse_Handled, false, false);
		AddObjectProperty("ErrorMessage", gxTv_SdtEventMessageResponse_Errormessage, false, false);
	}

	public boolean getgxTv_SdtEventMessageResponse_Handled() {
		return gxTv_SdtEventMessageResponse_Handled;
	}

	public void setgxTv_SdtEventMessageResponse_Handled(boolean value) {
		gxTv_SdtEventMessageResponse_N = (byte) (0);
		gxTv_SdtEventMessageResponse_Handled = value;
	}

	public String getgxTv_SdtEventMessageResponse_Errormessage() {
		return gxTv_SdtEventMessageResponse_Errormessage;
	}

	public void setgxTv_SdtEventMessageResponse_Errormessage(String value) {
		gxTv_SdtEventMessageResponse_N = (byte) (0);
		gxTv_SdtEventMessageResponse_Errormessage = value;
	}

	public void initialize(int remoteHandle) {
		initialize();
	}

	public void initialize() {
		gxTv_SdtEventMessageResponse_N = (byte) (1);
		gxTv_SdtEventMessageResponse_Errormessage = "";
		sTagName = "";
	}

	public byte isNull() {
		return gxTv_SdtEventMessageResponse_N;
	}

	public com.genexus.genexusserverlessapi.SdtEventMessageResponse Clone() {
		return (com.genexus.genexusserverlessapi.SdtEventMessageResponse) (clone());
	}

	public void setStruct(com.genexus.genexusserverlessapi.StructSdtEventMessageResponse struct) {
		setgxTv_SdtEventMessageResponse_Handled(struct.getHandled());
		setgxTv_SdtEventMessageResponse_Errormessage(struct.getErrormessage());
	}

	@SuppressWarnings("unchecked")
	public com.genexus.genexusserverlessapi.StructSdtEventMessageResponse getStruct() {
		com.genexus.genexusserverlessapi.StructSdtEventMessageResponse struct = new com.genexus.genexusserverlessapi.StructSdtEventMessageResponse();
		struct.setHandled(getgxTv_SdtEventMessageResponse_Handled());
		struct.setErrormessage(getgxTv_SdtEventMessageResponse_Errormessage());
		return struct;
	}

	protected byte gxTv_SdtEventMessageResponse_N;
	protected short readOk;
	protected short nOutParmCount;
	protected String sTagName;
	protected boolean gxTv_SdtEventMessageResponse_Handled;
	protected boolean readElement;
	protected boolean formatError;
	protected String gxTv_SdtEventMessageResponse_Errormessage;
}

