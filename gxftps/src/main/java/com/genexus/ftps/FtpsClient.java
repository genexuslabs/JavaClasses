package com.genexus.ftps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Objects;

import javax.net.ssl.X509TrustManager;

import com.genexus.ftps.utils.FtpEncoding;
import com.genexus.ftps.utils.FtpEncryptionMode;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;

import com.genexus.commons.ftps.FtpsClientObject;
import com.genexus.ftps.utils.FtpConnectionMode;
import com.genexus.securityapicommons.utils.ExtensionsWhiteList;
import com.genexus.securityapicommons.utils.SecurityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings({"unused", "LoggingSimilarMessage"})
public class FtpsClient extends FtpsClientObject {

	private FTPSClient client;
	private String pwd;
	private ExtensionsWhiteList whiteList;
	private static final Logger logger = LogManager.getLogger(FtpsClient.class);

	public FtpsClient() {
		super();
		this.client = null;
		this.whiteList = null;
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/
	public boolean connect(FtpsOptions options) {
		logger.debug("connect");
		if (options.hasError()) {
			this.error = options.getError();
			return false;
		}
		if (SecurityUtils.compareStrings("", options.getHost()) || SecurityUtils.compareStrings("", options.getUser())
			|| SecurityUtils.compareStrings("", options.getPassword())) {
			this.error.setError("FS001", "Empty connection data");
			logger.error("connect - Empty connection data");
			return false;
		}

		setProtocol(options);

		if (!SecurityUtils.compareStrings("", options.getTrustStorePath())) {
			if (!setCertificateValidation(options)) {
				return false;
			}
		}
		try {
			this.client.connect(options.getHost(), options.getPort());
			if (options.getForceEncryption()) {
				client.execPBSZ(0);
				client.execPROT("P");
			}
			if (!FTPReply.isPositiveCompletion(this.client.getReplyCode())) {
				this.client.disconnect();
				this.error.setError("FS008", "Connection error");
				logger.error("connect - Connection error");
				return false;
			}
			boolean login = this.client.login(options.getUser(), options.getPassword());
			if (!login) {
				this.error.setError("FS0016", "Login error");
				logger.error("connect - Login error");
				return false;
			}
			setEncoding(options);
			setConnectionMode(options);

		} catch (IOException e) {
			this.error.setError("FS002", "Login error " + e.getMessage());
			logger.error("connect", e);
			this.client = null;
			return false;
		}
		if (!this.client.isConnected()) {
			this.error.setError("FS009", "Connection error");
			logger.error("connect - Connection error");
			return false;
		}
		this.whiteList = options.getWhiteList();
		return true;
	}

	public boolean put(String localPath, String remoteDir) {
		logger.debug("put");
		if (this.whiteList != null) {
			if (!this.whiteList.isValid(localPath)) {
				this.error.setError("WL001", "Invalid file extension");
				logger.error("put - Invalid file extension");
				return false;
			}
		}

		if (this.client == null || !this.client.isConnected()
			|| !FTPReply.isPositiveCompletion(this.client.getReplyCode())) {
			this.error.setError("FS003", "The connection is invalid, reconnect");
			logger.error("put - The connection is invalid, reconnect");
			return false;
		}
		boolean dirchange = false;
		try {
			if (!isSameDir(remoteDir, this.client.printWorkingDirectory())) {
				dirchange = this.client.changeWorkingDirectory(remoteDir);
				this.pwd = remoteDir;
			}

		} catch (IOException e2) {
			this.error.setError("FS013", "Error changing directory " + e2.getMessage());
			logger.error("put - Error changing directory ", e2);
			return false;
		}
		if (!dirchange) {
			this.error.setError("FS014",
				"Reply code: " + this.client.getReplyCode() + " Reply String: " + this.client.getReplyString());
			logger.error("Reply code: " + this.client.getReplyCode() + " Reply String: " + this.client.getReplyString());

			return false;
		}
		InputStream input;
		try {
			input = new FileInputStream(localPath);
		} catch (FileNotFoundException e1) {
			this.error.setError("FS011", "Could not load local file " + e1.getMessage());
			logger.error("put - Could not load local file ", e1);
			return false;
		}
		String remoteFileName = getFileName(localPath);
		boolean isStored = false;
		try {

			isStored = this.client.storeFile(remoteFileName, input);
			if (!isStored) {
				this.error.setError("FS012",
					"Reply code: " + this.client.getReplyCode() + " Reply String: " + this.client.getReplyString());
				logger.error("Reply code: " + this.client.getReplyCode() + " Reply String: " + this.client.getReplyString());
			}
		} catch (IOException e) {
			this.error.setError("FS004", "Error uploading file to server " + e.getMessage());
			logger.error("put - Error uploading file to server ", e );
			return false;
		}
		return isStored;
	}

	public boolean get(String remoteFilePath, String localDir) {
		logger.debug("get");
		if (this.whiteList != null) {
			if (!this.whiteList.isValid(remoteFilePath)) {
				this.error.setError("WL002", "Invalid file extension");
				logger.error("get - Invalid file extension");
				return false;
			}
		}
		if (this.client == null || !this.client.isConnected()) {
			this.error.setError("FS010", "The connection is invalid, reconnect");
			logger.error("get - The connection is invalid, reconnect");
			return false;
		}
		boolean dirchange = true;
		try {
			if (!isSameDir(getDirectory(remoteFilePath), this.client.printWorkingDirectory())) {
				dirchange = this.client.changeWorkingDirectory(getDirectory(remoteFilePath));
				this.pwd = getDirectory(remoteFilePath);
			}

		} catch (IOException e2) {
			this.error.setError("FS013", "Error changing directory " + e2.getMessage());
			logger.error("put - Error changing directory ", e2);
			return false;
		}
		if (!dirchange) {
			this.error.setError("FS016",
				"Reply code: " + this.client.getReplyCode() + " Reply String: " + this.client.getReplyString());
			logger.error("Reply code: " + this.client.getReplyCode() + " Reply String: " + this.client.getReplyString());
			return false;
		}
		InputStream is = null;
		try {
			is = this.client.retrieveFileStream(getFileName(remoteFilePath));

		} catch (IOException e) {
			this.error.setError("FS005", "Error retrieving file " + e.getMessage());
			logger.error("put - Error retrieving file ", e);
			return false;
		}
		if (is == null) {
			this.error.setError("FS007", "Could not retrieve file");
			logger.error("put - Could not retrieve file");
			return false;
		}
		File file = new File(addFileName(remoteFilePath, localDir));
		try {
			copyInputStreamToFile(is, file);
		} catch (IOException e) {
			this.error.setError("FS017", "Error reading stream");
			logger.error("put - Error reading stream", e);
			return false;
		}

		return true;

	}

	public boolean rm(String remoteFilePath) {
		logger.debug("rm");
		if (this.client == null || !this.client.isConnected()) {
			this.error.setError("FS019", "The connection is invalid, reconnect");
			logger.error("rm - The connection is invalid, reconnect");
			return false;
		}
		boolean dirchange = true;
		try {
			if (!isSameDir(getDirectory(remoteFilePath), this.client.printWorkingDirectory())) {
				dirchange = this.client.changeWorkingDirectory(getDirectory(remoteFilePath));
				this.pwd = getDirectory(remoteFilePath);
			}

		} catch (IOException e2) {
			this.error.setError("FS020", "Error changing directory " + e2.getMessage());
			logger.error("rm - Error changing directory ", e2);
			return false;
		}
		if (!dirchange) {
			this.error.setError("FS021",
				"Reply code: " + this.client.getReplyCode() + " Reply String: " + this.client.getReplyString());
			logger.error("Reply code: " + this.client.getReplyCode() + " Reply String: " + this.client.getReplyString());
			return false;
		}
		try {
			return this.client.deleteFile(remoteFilePath);
		} catch (Exception e) {
			this.error.setError("FS022", "Error retrieving file " + e.getMessage());
			logger.error("rm - Error retrieving file ", e);
			return false;
		}

	}

	public void disconnect() {
		logger.debug("disconnect");
		if (this.client != null && this.client.isConnected()) {
			try {
				this.client.logout();
				this.client.disconnect();
			} catch (IOException ignored) {
			}
		}
	}

	public String getWorkingDirectory() {
		logger.debug("getWorkingDirectory");
		if (this.client == null || !this.client.isConnected()) {
			this.error.setError("FS007", "The connection is invalid, reconnect");
			logger.error("getWorkingDirectory - The connection is invalid, reconnect");
			return "";
		}

		try {
			String pwd = this.client.printWorkingDirectory();
			return pwd==null? this.pwd: pwd;
		} catch (IOException e) {
			this.error.setError("FS006", "Could not obtain working directory, try reconnect");
			logger.error("getWorkingDirectory - Could not obtain working directory, try reconnect", e);
			return "";
		}
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	private String addFileName(String originPath, String dir) {
		logger.debug("addFileName");
		Path path = Paths.get(originPath);
		Path fileName = path.getFileName();
		if (SecurityUtils.compareStrings("", dir)) {
			return fileName.toString();
		}
		return dir.contains("/") ? dir + "/" + fileName.toString(): dir + "\\" + fileName.toString();
	}

	private String getFileName(String originPath) {
		Path path = Paths.get(originPath);
		Path fileName = path.getFileName();
		return fileName.toString();
	}

	private String getDirectory(String filePath) {
		logger.debug("getDirectory");
		Path path = Paths.get(filePath);
		try {
			return path.getParent().toString();
		} catch (java.lang.NullPointerException e) {
			logger.error("getDirectory", e);
			return "";
		}
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean isSameDir(String path1, String path2) {
		Path path11 = Paths.get(path1);
		Path path22 = Paths.get(path2);
		return path11.compareTo(path22) == 0;
	}

	private static void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {

		try (FileOutputStream outputStream = new FileOutputStream(file)) {

			int read;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}

		}

	}

	private void setEncoding(FtpsOptions options) throws IOException {
		if (Objects.requireNonNull(options.getFtpEncoding()) == FtpEncoding.ASCII) {
			this.client.setFileType(FTP.ASCII_FILE_TYPE);
		} else {
			this.client.setFileType(FTP.BINARY_FILE_TYPE);
		}
	}

	private void setConnectionMode(FtpsOptions options) {
		FtpConnectionMode mode = options.getFtpConnectionMode();
		if (Objects.requireNonNull(mode) == FtpConnectionMode.ACTIVE) {
			this.client.enterLocalActiveMode();
		} else {
			this.client.enterLocalPassiveMode();
		}
	}

	private void setProtocol(FtpsOptions options) {
		switch (options.getFtpsProtocol()) {
			case TLS1_1:
				this.client = new FTPSClient("TLSv1.1", isImplicit(options));
				break;
			case TLS1_2:
				this.client = new FTPSClient("TLSv1.2", isImplicit(options));
				break;
			case SSLv2:
				this.client = new FTPSClient("SSLv2", isImplicit(options));
				break;
			case SSLv3:
				this.client = new FTPSClient("SSLv3", isImplicit(options));
				break;
			default:
				this.client = new FTPSClient("TLS", isImplicit(options));
		}
	}

	private boolean isImplicit(FtpsOptions options) {
		return Objects.requireNonNull(options.getFtpEncryptionMode()) == FtpEncryptionMode.IMPLICIT;
	}

	private boolean setCertificateValidation(FtpsOptions options) {
		X509TrustManager trustManager = null;
		KeyStore keyStore = null;
		InputStream in = null;
		try {
			in = SecurityUtils.inputFileToStream(options.getTrustStorePath());
			keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(in, options.getTrustStorePassword().toCharArray());
			trustManager = TrustManagerUtils.getDefaultTrustManager(keyStore);
		} catch (IOException | GeneralSecurityException e) {
			this.error.setError("FS017", "Could not load trust store");
			return false;
		}
		if (trustManager == null) {
			this.error.setError("FS018", "Could not load trust store");
			return false;
		}
		client.setTrustManager(trustManager);
		return true;
	}

}
