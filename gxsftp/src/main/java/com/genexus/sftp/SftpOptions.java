package com.genexus.sftp;

import com.genexus.securityapicommons.commons.SecurityAPIObject;
import com.genexus.securityapicommons.utils.ExtensionsWhiteList;
import com.genexus.securityapicommons.utils.SecurityUtils;

@SuppressWarnings("unused")
public class SftpOptions extends SecurityAPIObject {

	private String host;
	private int port;
	private String user;
	private String password;
	private String keyPath;
	private String keyPassword;
	private boolean allowHostKeyChecking;
	private String knownHostsPath;
	private ExtensionsWhiteList whiteList;

	public SftpOptions() {
		this.host = "";
		this.port = 22;
		this.user = "";
		this.password = "";
		this.keyPath = "";
		this.keyPassword = "";
		this.allowHostKeyChecking = true;
		this.knownHostsPath = "";
		this.whiteList = null;
	}

	public void setUser(String value) {
		this.user = value.trim();
	}

	public String getUser() {
		return this.user;
	}

	public void setPassword(String value) {
		this.password = value.trim();
	}

	public String getPassword() {
		return this.password;
	}

	public void setKeyPath(String value) {

		if (!(SecurityUtils.extensionIs(value.trim(), ".key") || SecurityUtils.extensionIs(value.trim(), ".pem")
			|| SecurityUtils.extensionIs(value.trim(), ""))) {
			this.error.setError("OP001",
				"Private key must be base64 encoded file (Valid extensions: .pem, .key, empty)");
		} else {

			this.keyPath = value.trim();
		}
	}

	public String getKeyPath() {
		return this.keyPath;
	}

	public void setHost(String value) {
		this.host = value.trim();
	}

	public String getHost() {
		return this.host;
	}

	public void setPort(int value) {
		this.port = value;
	}

	public int getPort() {
		return this.port;
	}

	public void setAllowHostKeyChecking(boolean value) {
		this.allowHostKeyChecking = value;
	}

	public boolean getAllowHostKeyChecking() {
		return this.allowHostKeyChecking;
	}

	public String getKeyPassword() {
		return this.keyPassword;
	}

	public void setKeyPassword(String value) {
		this.keyPassword = value.trim();
	}

	public String getKnownHostsPath() {
		return this.knownHostsPath;
	}

	public void setKnownHostsPath(String value) {
		if (!SecurityUtils.extensionIs(value.trim(), "")) {
			this.error.setError("OP002", "No extension is allowed for known_hosts file");
		} else {
			this.knownHostsPath = value.trim();
		}
	}

	public void setWhiteList(ExtensionsWhiteList value) {
		this.whiteList = value;
	}

	public ExtensionsWhiteList getWhiteList() {
		return this.whiteList;
	}
}
