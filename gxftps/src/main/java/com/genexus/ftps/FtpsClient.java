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

import javax.net.ssl.X509TrustManager;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;

import com.genexus.commons.ftps.FtpsClientObject;
import com.genexus.ftps.utils.FtpConnectionMode;
import com.genexus.securityapicommons.utils.ExtensionsWhiteList;
import com.genexus.securityapicommons.utils.SecurityUtils;

public class FtpsClient extends FtpsClientObject {

	private FTPSClient client;
	private String pwd;
	private ExtensionsWhiteList whiteList;

	public FtpsClient() {
		super();
		this.client = null;
		this.whiteList = null;
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/
	public boolean connect(FtpsOptions options) {
		if (options.hasError()) {
			this.error = options.getError();
			return false;
		}
		if (SecurityUtils.compareStrings("", options.getHost()) || SecurityUtils.compareStrings("", options.getUser())
			|| SecurityUtils.compareStrings("", options.getPassword())) {
			this.error.setError("FS001", "Empty connection data");
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
				return false;
			}
			boolean login = this.client.login(options.getUser(), options.getPassword());
			if (!login) {
				this.error.setError("FS0016", "Login error");
				return false;
			}
			setEncoding(options);
			setConnectionMode(options);

		} catch (IOException e) {
			this.error.setError("FS002", "Login error " + e.getMessage());
			this.client = null;
			return false;
		}
		if (!this.client.isConnected()) {
			this.error.setError("FS009", "Connection error");
			return false;
		}
		this.whiteList = options.getWhiteList();
		return true;
	}

	public boolean put(String localPath, String remoteDir) {
		if (this.whiteList != null) {
			if (!this.whiteList.isValid(localPath)) {
				this.error.setError("WL001", "Invalid file extension");
				return false;
			}
		}

		if (this.client == null || !this.client.isConnected()
			|| !FTPReply.isPositiveCompletion(this.client.getReplyCode())) {
			this.error.setError("FS003", "The connection is invalid, reconect");
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
			return false;
		}
		if (!dirchange) {
			this.error.setError("FS014",
				"Reply code: " + this.client.getReplyCode() + " Reply String: " + this.client.getReplyString());
			return false;
		}
		InputStream input;
		try {
			input = new FileInputStream(new File(localPath));
		} catch (FileNotFoundException e1) {
			this.error.setError("FS011", "Could not load local file " + e1.getMessage());
			return false;
		}
		String remoteFileName = getFileName(localPath);
		boolean isStored = false;
		try {

			isStored = this.client.storeFile(remoteFileName, input);
			if (!isStored) {
				this.error.setError("FS012",
					"Reply code: " + this.client.getReplyCode() + " Reply String: " + this.client.getReplyString());
			}
		} catch (IOException e) {
			this.error.setError("FS004", "Erorr uploading file to server " + e.getMessage());
			return false;
		}
		return isStored;
	}

	public boolean get(String remoteFilePath, String localDir) {
		if (this.whiteList != null) {
			if (!this.whiteList.isValid(remoteFilePath)) {
				this.error.setError("WL002", "Invalid file extension");
				return false;
			}
		}
		if (this.client == null || !this.client.isConnected()) {
			this.error.setError("FS010", "The connection is invalid, reconect");
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
			return false;
		}
		if (!dirchange) {
			this.error.setError("FS016",
				"Reply code: " + this.client.getReplyCode() + " Reply String: " + this.client.getReplyString());
			return false;
		}
		InputStream is = null;
		try {
			is = this.client.retrieveFileStream(getFileName(remoteFilePath));

		} catch (IOException e) {
			this.error.setError("FS005", "Error retrieving file " + e.getMessage());
			return false;
		}
		if (is == null) {
			this.error.setError("FS007", "Could not retrieve file");
			return false;
		}
		File file = new File(addFileName(remoteFilePath, localDir));
		try {
			copyInputStreamToFile(is, file);
		} catch (IOException e) {
			this.error.setError("FS017", "Error reading stream");
			return false;
		}

		return true;

	}

	public boolean rm(String remoteFilePath) {
		if (this.client == null || !this.client.isConnected()) {
			this.error.setError("FS019", "The connection is invalid, reconect");
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
			return false;
		}
		if (!dirchange) {
			this.error.setError("FS021",
				"Reply code: " + this.client.getReplyCode() + " Reply String: " + this.client.getReplyString());
			return false;
		}
		boolean deleted = false;
		try {
			deleted = this.client.deleteFile(remoteFilePath);
		} catch (Exception e) {
			this.error.setError("FS022", "Error retrieving file " + e.getMessage());
			deleted = false;
		}
		return deleted;

	}

	public void disconnect() {
		if (this.client != null && this.client.isConnected()) {
			try {
				this.client.logout();
				this.client.disconnect();
			} catch (IOException e) {
			}
		}
	}

	public String getWorkingDirectory() {
		if (this.client == null || !this.client.isConnected()) {
			this.error.setError("FS007", "The connection is invalid, reconect");
			return "";
		}
		String pwd = "";
		try {
			pwd = this.client.printWorkingDirectory();
		} catch (IOException e) {
			this.error.setError("FS006", "Could not obtain working directory, try reconnect");
			return "";
		}
		if (pwd == null) {
			return this.pwd;
		}
		return pwd;
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	private String addFileName(String originPath, String dir) {

		Path path = Paths.get(originPath);
		Path fileName = path.getFileName();
		if (SecurityUtils.compareStrings("", dir)) {
			return fileName.toString();
		}
		String pathArr = "";
		if (dir.contains("/")) {
			pathArr = dir + "/" + fileName.toString();
		} else {
			pathArr = dir + "\\" + fileName.toString();
		}

		return pathArr;
	}

	private String getFileName(String originPath) {
		Path path = Paths.get(originPath);
		Path fileName = path.getFileName();
		return fileName.toString();
	}

	private String getDirectory(String filePath) {
		Path path = Paths.get(filePath);
		String pathToFile = "";
		try {
			pathToFile = path.getParent().toString();
		} catch (java.lang.NullPointerException e) {
			return "";
		}
		return pathToFile;
	}

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
		switch (options.getFtpEncoding()) {
			case BINARY:
				this.client.setFileType(FTP.BINARY_FILE_TYPE);
				break;
			case ASCII:
				this.client.setFileType(FTP.ASCII_FILE_TYPE);
				break;
			default:
				this.client.setFileType(FTP.BINARY_FILE_TYPE);
		}
	}

	private void setConnectionMode(FtpsOptions options) {
		FtpConnectionMode mode = options.getFtpConnectionMode();
		switch (mode) {
			case ACTIVE:
				this.client.enterLocalActiveMode();
				break;
			case PASSIVE:
				this.client.enterLocalPassiveMode();
				break;
			default:
				this.client.enterLocalPassiveMode();
		}
	}

	private void setProtocol(FtpsOptions options) {
		switch (options.getFtpsProtocol()) {
			case TLS1_0:
				this.client = new FTPSClient("TLS", isImplicit(options));
				break;
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
		switch (options.getFtpEncryptionMode()) {
			case EXPLICIT:
				return false;
			case IMPLICIT:
				return true;
			default:
				return false;
		}
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
