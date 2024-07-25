package com.genexus.ftps;

import com.genexus.ftps.utils.FtpConnectionMode;
import com.genexus.ftps.utils.FtpEncoding;
import com.genexus.ftps.utils.FtpEncryptionMode;
import com.genexus.ftps.utils.FtpsProtocol;
import com.genexus.securityapicommons.commons.SecurityAPIObject;
import com.genexus.securityapicommons.utils.ExtensionsWhiteList;
import com.genexus.securityapicommons.utils.SecurityUtils;

public class FtpsOptions extends SecurityAPIObject {

	private String host;
	private int port;
	private String user;
	private String password;
	private boolean forceEncryption;
	private FtpConnectionMode connectionMode;
	private FtpEncoding encoding;
	private FtpEncryptionMode encryptionMode;
	private String trustStorePath;
	private String trustStorePassword;
	private FtpsProtocol protocol;
	private ExtensionsWhiteList whiteList;

	public FtpsOptions() {
		this.host = "";
		this.port = 21;
		this.user = "";
		this.password = "";
		this.forceEncryption = true;
		this.connectionMode = FtpConnectionMode.PASSIVE;
		this.encoding = FtpEncoding.BINARY;
		this.encryptionMode = FtpEncryptionMode.EXPLICIT;
		this.trustStorePath = "";
		this.trustStorePassword = "";
		this.protocol = FtpsProtocol.TLS1_2;
		this.whiteList = null;
	}

	public String getHost() {
		return this.host;
	}

	public void setHost(String value) {
		this.host = value.trim();
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int value) {
		this.port = value;
	}

	public String getUser() {
		return this.user.trim();
	}

	public void setUser(String value) {
		this.user = value.trim();
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String value) {
		this.password = value.trim();
	}

	public boolean getForceEncryption() {
		return this.forceEncryption;
	}

	public void setForceEncryption(boolean value) {
		this.forceEncryption = value;
	}

	public void setConnectionMode(String value) {
		this.connectionMode = FtpConnectionMode.getFtpMode(value, this.error);
	}

	public String getConnectionMode() {
		return FtpConnectionMode.valueOf(this.connectionMode, this.error);
	}

	public FtpConnectionMode getFtpConnectionMode() {
		return this.connectionMode;
	}

	public void setEncryptionMode(String value) {
		this.encryptionMode = FtpEncryptionMode.getFtpEncryptionMode(value, this.error);
	}

	public String getEncryptionMode() {
		return FtpEncryptionMode.valueOf(this.encryptionMode, this.error);
	}

	public FtpEncryptionMode getFtpEncryptionMode() {
		return this.encryptionMode;
	}

	public void setEncoding(String value) {
		this.encoding = FtpEncoding.getFtpEncoding(value, this.error);
	}

	public String getEncoding() {
		return FtpEncoding.valueOf(this.encoding, this.error);
	}

	public FtpEncoding getFtpEncoding() {
		return this.encoding;
	}

	public void setTrustStorePath(String value) {
		if (!(SecurityUtils.extensionIs(value, ".pfx") || SecurityUtils.extensionIs(value, ".p12")
			|| SecurityUtils.extensionIs(value, ".jks"))) {
			error.setError("FO001", "Unexpected extension for trust store); valid extensions: .p12 .jks .pfx");
		} else {
			this.trustStorePath = value;
		}
	}

	public String getTrustStorePath() {
		return this.trustStorePath;
	}

	public void setTrustStorePassword(String value) {
		this.trustStorePassword = value;
	}

	public String getTrustStorePassword() {
		return this.trustStorePassword;
	}

	public void setProtocol(String value) {
		this.protocol = FtpsProtocol.getFtpsProtocol(value, this.error);
	}

	public String getProtocol() {
		return FtpsProtocol.valueOf(this.protocol, this.error);
	}

	public FtpsProtocol getFtpsProtocol() {
		return this.protocol;
	}

	public void setWhiteList(ExtensionsWhiteList value) {
		this.whiteList = value;
	}

	public ExtensionsWhiteList getWhiteList() {
		return this.whiteList;
	}
}
