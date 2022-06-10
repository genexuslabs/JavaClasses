package com.genexusmessaging.genexusmessagingqueue.simplequeue;

import com.genexus.*;
import com.genexus.xml.*;

import java.util.*;

public class SdtMessageOptions extends GxUserType {
	protected byte gxTv_SdtMessageOptions_N;
	protected short gxTv_SdtMessageOptions_Maxnumberofmessages;
	protected int gxTv_SdtMessageOptions_Waittimeout;
	protected int gxTv_SdtMessageOptions_Visibilitytimeout;
	protected int gxTv_SdtMessageOptions_Timetolive;
	protected int gxTv_SdtMessageOptions_Delayseconds;

	protected boolean gxTv_SdtMessageOptions_Deleteconsumedmessages;
	protected boolean gxTv_SdtMessageOptions_Receivemessageattributes;
	protected String gxTv_SdtMessageOptions_Receiverequestattemptid;

	public SdtMessageOptions( )
	{
		this(  new ModelContext(SdtMessageOptions.class));
	}

	public SdtMessageOptions( ModelContext context )
	{
		super( context, "SdtMessageOptions");
	}

	public short getgxTv_SdtMessageOptions_Maxnumberofmessages() {
		return gxTv_SdtMessageOptions_Maxnumberofmessages;
	}

	public void setgxTv_SdtMessageOptions_Maxnumberofmessages(short value) {
		gxTv_SdtMessageOptions_N = (byte) (0);
		gxTv_SdtMessageOptions_Maxnumberofmessages = value;
	}

	public boolean getgxTv_SdtMessageOptions_Deleteconsumedmessages() {
		return gxTv_SdtMessageOptions_Deleteconsumedmessages;
	}

	public void setgxTv_SdtMessageOptions_Deleteconsumedmessages(boolean value) {
		gxTv_SdtMessageOptions_N = (byte) (0);
		gxTv_SdtMessageOptions_Deleteconsumedmessages = value;
	}

	public int getgxTv_SdtMessageOptions_Waittimeout() {
		return gxTv_SdtMessageOptions_Waittimeout;
	}

	public void setgxTv_SdtMessageOptions_Waittimeout(int value) {
		gxTv_SdtMessageOptions_N = (byte) (0);
		gxTv_SdtMessageOptions_Waittimeout = value;
	}

	public int getgxTv_SdtMessageOptions_Visibilitytimeout() {
		return gxTv_SdtMessageOptions_Visibilitytimeout;
	}

	public void setgxTv_SdtMessageOptions_Visibilitytimeout(int value) {
		gxTv_SdtMessageOptions_N = (byte) (0);
		gxTv_SdtMessageOptions_Visibilitytimeout = value;
	}

	public int getgxTv_SdtMessageOptions_Timetolive() {
		return gxTv_SdtMessageOptions_Timetolive;
	}

	public void setgxTv_SdtMessageOptions_Timetolive(int value) {
		gxTv_SdtMessageOptions_N = (byte) (0);
		gxTv_SdtMessageOptions_Timetolive = value;
	}

	public int getgxTv_SdtMessageOptions_Delayseconds() {
		return gxTv_SdtMessageOptions_Delayseconds;
	}

	public void setgxTv_SdtMessageOptions_Delayseconds(int value) {
		gxTv_SdtMessageOptions_N = (byte) (0);
		gxTv_SdtMessageOptions_Delayseconds = value;
	}

	public String getgxTv_SdtMessageOptions_Receiverequestattemptid() {
		return gxTv_SdtMessageOptions_Receiverequestattemptid;
	}

	public void setgxTv_SdtMessageOptions_Receiverequestattemptid(String value) {
		gxTv_SdtMessageOptions_N = (byte) (0);
		gxTv_SdtMessageOptions_Receiverequestattemptid = value;
	}

	public boolean getgxTv_SdtMessageOptions_Receivemessageattributes() {
		return gxTv_SdtMessageOptions_Receivemessageattributes;
	}

	public void setgxTv_SdtMessageOptions_Receivemessageattributes(boolean value) {
		gxTv_SdtMessageOptions_N = (byte) (0);
		gxTv_SdtMessageOptions_Receivemessageattributes = value;
	}

	public void initialize() {
		gxTv_SdtMessageOptions_N = (byte) (1);
		gxTv_SdtMessageOptions_Receiverequestattemptid = "";
	}
	@Override
	public String getJsonMap(String value) {
		return null;
	}
}

