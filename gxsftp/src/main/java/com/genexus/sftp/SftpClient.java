package com.genexus.sftp;

import com.genexus.commons.sftp.SftpClientObject;
import com.genexus.securityapicommons.utils.ExtensionsWhiteList;
import com.genexus.securityapicommons.utils.SecurityUtils;
import com.jcraft.jsch.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@SuppressWarnings({"unused", "LoggingSimilarMessage"})
public class SftpClient extends SftpClientObject {

	private ChannelSftp channel;
	private Session session;
	private ExtensionsWhiteList whiteList;

	private static final Logger logger = LogManager.getLogger(SftpClient.class);

	public SftpClient() {
		super();
		this.channel = null;
		this.session = null;
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/
	public boolean connect(SftpOptions options) {
		logger.debug("connect");
		if (options.hasError()) {
			this.error = options.getError();
			return false;
		}
		boolean useKey = false;
		if (!SecurityUtils.compareStrings("", options.getKeyPath())) {
			useKey = true;
		} else {
			if (SecurityUtils.compareStrings("", options.getUser())
				|| SecurityUtils.compareStrings("", options.getPassword())) {

				this.error.setError("SF001", "Authentication misconfiguration. Missing user or password");
				logger.error("connect - Authentication misconfiguration. Missing user or password");
				return false;
			}
		}
		if (SecurityUtils.compareStrings("", options.getHost())) {
			this.error.setError("SF003", "Empty host");
			logger.error("connect - Empty host");
			return false;
		}
		try {
			this.channel = setupJsch(options, useKey);
			this.channel.connect();
		} catch (JSchException e) {
			this.error.setError("SF004", e.getMessage() + Arrays.toString(e.getStackTrace()));
			logger.error("connect", e);
			return false;
		}
		this.whiteList = options.getWhiteList();
		return true;
	}

	public boolean put(String localPath, String remoteDir) {
		logger.debug("put");
		String rDir = remoteDir;
		if (this.whiteList != null) {
			if (!this.whiteList.isValid(localPath)) {
				this.error.setError("WL001", "Invalid file extension");
				logger.error("put - Invalid file extension");
				return false;
			}
		}
		if (this.channel == null) {
			this.error.setError("SF005", "The channel is invalid, reconnect");
			logger.error("put - The channel is invalid, reconect");
			return false;
		}

		if (remoteDir.length() > 1) {
			if (remoteDir.startsWith("\\") || remoteDir.startsWith("/")) {
				remoteDir = remoteDir.substring(1);
			}
		}
		try {
			this.channel.put(localPath, remoteDir);
		} catch (SftpException e) {
			if (SecurityUtils.compareStrings(rDir, "/") || SecurityUtils.compareStrings(rDir, "\\")) {
				try {
					this.channel.put(localPath, getFileName(localPath));
				} catch (SftpException s) {
					this.error.setError("SF006", s.getMessage());
					logger.error("put", e);
					return false;
				}
			} else {
				this.error.setError("SF006", e.getMessage());
				logger.error("put", e);
				return false;
			}

		}

		return true;
	}

	public boolean rm(String remotePath) {
		logger.debug("rm");
		String rDir = remotePath;
		if (this.channel == null) {
			this.error.setError("SF018", "The channel is invalid, reconnect");
			logger.error("put - The channel is invalid, reconnect");
			return false;
		}

		if (remotePath.length() > 1) {
			if (remotePath.startsWith("\\") || remotePath.startsWith("/")) {
				remotePath = remotePath.substring(1);
			}
		}
		try {
			this.channel.rm(remotePath);
		} catch (SftpException e) {
			if (SecurityUtils.compareStrings(rDir, "/") || SecurityUtils.compareStrings(rDir, "\\")) {
				try {
					this.channel.rm(getFileName(remotePath));
				} catch (SftpException s) {
					this.error.setError("SF019", s.getMessage());
					logger.error("rm", e);
					return false;
				}
			} else {
				this.error.setError("SF020", e.getMessage());
				logger.error("rm", e);
				return false;
			}

		}

		return true;
	}

	public boolean get(String remoteFilePath, String localDir) {
		logger.debug("get");
		if (this.whiteList != null) {
			if (!this.whiteList.isValid(remoteFilePath)) {
				this.error.setError("WL002", "Invalid file extension");
				logger.error("rm - Invalid file extension");
				return false;
			}
		}
		if (this.channel == null) {
			this.error.setError("SF007", "The channel is invalid, reconnect");
			logger.error("rm - The channel is invalid, reconnect");
			return false;
		}
		try {
			this.channel.get(remoteFilePath, localDir);
		} catch (SftpException e) {
			this.error.setError("SF008", e.getMessage());
			logger.error("rm", e);
			return false;
		}
		return true;
	}

	public void disconnect() {
		if (this.channel != null) {
			this.channel.disconnect();
		}
		if (this.session != null) {
			this.session.disconnect();
		}
	}

	public String getWorkingDirectory() {
		logger.debug("getWorkingDirectory");
		if (this.channel != null) {
			try {
				return this.channel.pwd();
			} catch (SftpException e) {
				this.error.setError("SF017", "Could not get working directory, try reconnect");
				logger.error("getWorkingDirectory", e);
				return "";
			}
		}
		return "";
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	private ChannelSftp setupJsch(SftpOptions options, boolean useKey) throws JSchException {
		logger.debug("setupJsch");
		JSch jsch = new JSch();

		if (useKey) {
			jsch.addIdentity(options.getKeyPath(), options.getKeyPassword());

			this.session = jsch.getSession(options.getUser(), options.getHost(), options.getPort());
			if (options.getAllowHostKeyChecking()) {
				if (SecurityUtils.compareStrings("", options.getKnownHostsPath())) {
					this.error.setError("SF009",
						"Options misconfiguration, known_hosts path is empty but host key checking is true");
					logger.error("setupJsch - Options misconfiguration, known_hosts path is empty but host key checking is true");
				}
				jsch.setKnownHosts(options.getKnownHostsPath());
			} else {
				this.session.setConfig("StrictHostKeyChecking", "no");
			}

		} else {
			this.session = jsch.getSession(options.getUser(), options.getHost(), options.getPort());
			this.session.setPassword(options.getPassword());
			this.session.setConfig("StrictHostKeyChecking", "no");
		}
		this.session.connect();
		return (ChannelSftp) this.session.openChannel("sftp");
	}

	private String getFileName(String path) {
		Path p = Paths.get(path);
		return p.getFileName().toString();
	}
}
