package com.genexusmessaging.genexusmessagingqueue.simplequeue;

import com.genexus.*;

public class SdtMessageProperty extends GxUserType {
	protected byte gxTv_SdtMessageProperty_N;
	protected String gxTv_SdtMessageProperty_Propertykey;
	protected String gxTv_SdtMessageProperty_Propertyvalue;

	public SdtMessageProperty( )
	{
		this(  new ModelContext(SdtMessageOptions.class));
	}

	public SdtMessageProperty( ModelContext context )
	{
		super( context, "SdtMessageOptions");
	}

	public String getgxTv_SdtMessageProperty_Propertykey() {
		return gxTv_SdtMessageProperty_Propertykey;
	}

	public void setgxTv_SdtMessageProperty_Propertykey(String value) {
		gxTv_SdtMessageProperty_N = (byte) (0);
		gxTv_SdtMessageProperty_Propertykey = value;
	}

	public String getgxTv_SdtMessageProperty_Propertyvalue() {
		return gxTv_SdtMessageProperty_Propertyvalue;
	}

	public void setgxTv_SdtMessageProperty_Propertyvalue(String value) {
		gxTv_SdtMessageProperty_N = (byte) (0);
		gxTv_SdtMessageProperty_Propertyvalue = value;
	}

	public void initialize(int remoteHandle) {
		initialize();
	}

	public void initialize() {
		gxTv_SdtMessageProperty_Propertykey = "";
		gxTv_SdtMessageProperty_N = (byte) (1);
		gxTv_SdtMessageProperty_Propertyvalue = "";
	}

	@Override
	public String getJsonMap(String value) {
		return null;
	}
}

