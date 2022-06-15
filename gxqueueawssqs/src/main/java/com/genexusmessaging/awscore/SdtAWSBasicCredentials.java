package com.genexusmessaging.awscore;
import com.genexus.*;

public final class SdtAWSBasicCredentials extends GxUserType {
	protected byte gxTv_SdtAWSBasicCredentials_N;
	protected String sTagName;
	protected String gxTv_SdtAWSBasicCredentials_Accesskey;
	protected String gxTv_SdtAWSBasicCredentials_Secretkey;
	protected String gxTv_SdtAWSBasicCredentials_Region;


	public SdtAWSBasicCredentials() {
		this(new ModelContext(SdtAWSBasicCredentials.class));
	}

	public SdtAWSBasicCredentials(ModelContext context) {
		super(context, "SdtAWSBasicCredentials");
	}

	public SdtAWSBasicCredentials(int remoteHandle,
								  ModelContext context) {
		super(remoteHandle, context, "SdtAWSBasicCredentials");
	}


	public String getgxTv_SdtAWSBasicCredentials_Accesskey() {
		return gxTv_SdtAWSBasicCredentials_Accesskey;
	}

	public void setgxTv_SdtAWSBasicCredentials_Accesskey(String value) {
		gxTv_SdtAWSBasicCredentials_N = (byte) (0);
		gxTv_SdtAWSBasicCredentials_Accesskey = value;
	}

	public String getgxTv_SdtAWSBasicCredentials_Secretkey() {
		return gxTv_SdtAWSBasicCredentials_Secretkey;
	}

	public void setgxTv_SdtAWSBasicCredentials_Secretkey(String value) {
		gxTv_SdtAWSBasicCredentials_N = (byte) (0);
		gxTv_SdtAWSBasicCredentials_Secretkey = value;
	}

	public String getgxTv_SdtAWSBasicCredentials_Region() {
		return gxTv_SdtAWSBasicCredentials_Region;
	}

	public void setgxTv_SdtAWSBasicCredentials_Region(String value) {
		gxTv_SdtAWSBasicCredentials_N = (byte) (0);
		gxTv_SdtAWSBasicCredentials_Region = value;
	}

	public void initialize(int remoteHandle) {
		initialize();
	}

	public void initialize() {
		gxTv_SdtAWSBasicCredentials_Accesskey = "";
		gxTv_SdtAWSBasicCredentials_N = (byte) (1);
		gxTv_SdtAWSBasicCredentials_Secretkey = "";
		gxTv_SdtAWSBasicCredentials_Region = "";
		sTagName = "";
	}

	public byte isNull() {
		return gxTv_SdtAWSBasicCredentials_N;
	}

	public SdtAWSBasicCredentials Clone() {
		return (SdtAWSBasicCredentials) (clone());
	}


	@Override
	public String getJsonMap(String value) {
		return null;
	}
}

