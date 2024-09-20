package com.genexus.securityapicommons.keys;

import com.genexus.securityapicommons.utils.SecurityUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.encoders.Base64;

import java.io.*;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

public class CertificateX509 extends com.genexus.securityapicommons.commons.Certificate {

	private static final Logger logger = LogManager.getLogger(CertificateX509.class);

	private String algorithmWithHash;
	private X509Certificate cert;
	public String issuer;
	public String subject;
	public String serialNumber;
	public String thumbprint;
	public Date notAfter;
	public Date notBefore;
	public int version;

	public CertificateX509() {
		super();
		this.cert = null;
	}

	public X509Certificate Cert() {
		return this.cert;
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/
	@Override
	public boolean load(String path) {
		this.error.cleanError();
		logger.debug("load");
		this.cert = null;
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(CertificateX509.class), "load", "path", path, this.error);
		if (this.hasError()) {
			return false;
		}

		// INPUT VERIFICATION - END

		return loadPKCS12(path, "", "");
	}

	@Override
	public boolean loadPKCS12(String path, String alias, String password) {
		this.error.cleanError();
		logger.debug("loadPKCS12");
		this.cert = null;
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(CertificateX509.class), "loadPKCS12", "path", path, this.error);
		if (this.hasError()) {
			return false;
		}

		// INPUT VERIFICATION - END


		try {
			return loadPublicKeyFromFile(path, alias, password);
		} catch (Exception e) {
			this.error.setError("CE001", e.getMessage());
			logger.error("loadPKCS12", e);
			return false;
		}
	}

	@Override
	public boolean fromBase64(String base64Data) {
		this.error.cleanError();
		logger.debug("fromBase64");
		this.cert = null;
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(CertificateX509.class), "fromBase64", "base64Data", base64Data, this.error);
		if (this.hasError()) {
			return false;
		}

		// INPUT VERIFICATION - END

		try {
			byte[] dataBuffer = Base64.decode(base64Data);
			ByteArrayInputStream bI = new ByteArrayInputStream(dataBuffer);
			CertificateFactory cf = new CertificateFactory();
			this.cert = (X509Certificate) cf.engineGenerateCertificate(bI);
			inicializeParameters();
		} catch (Exception e) {
			this.error.setError("CE002", e.getMessage());
			logger.error("fromBase64", e);
		}
		return this.cert != null;
	}

	@Override
	public String toBase64() {
		this.error.cleanError();
		logger.debug("toBase64");
		if (this.cert == null) {
			this.error.setError("CE003", "Not loaded certificate");
			logger.error("toBase64 - Not loaded certificate");
			return "";
		}

		try {
			return new String(Base64.encode(this.cert.getEncoded()));

		} catch (Exception e) {
			this.error.setError("CE004", e.getMessage());
			logger.error("toBase64", e);
			return "";
		}
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/


	public String getPublicKeyHash() {
		String[] aux = this.algorithmWithHash.toUpperCase().split("WITH");
		if (SecurityUtils.compareStrings(aux[0], "1.2.840.10045.2.1")) {
			return "ECDSA";
		}
		return aux[0];
	}


	private boolean loadPublicKeyFromFile(String path, String alias, String password) {
		logger.debug("loadPublicKeyFromFile");
		for (String s : pkcs8_extensions) {
			if (FilenameUtils.getExtension(path).equals(s)) {
				return loadPublicKeyFromPkcs8File(path);
			}
		}

		for (String s : der_extentions) {
			if (FilenameUtils.getExtension(path).equals(s)) {
				return loadPublicKeyFromDERFile(path);
			}
		}
		for (String s : pkcs12_extensions) {
			if (FilenameUtils.getExtension(path).equals(s)) {
				return loadPublicKeyFromPKCS12File(path, alias, password);
			}
		}

		this.error.setError("CE005", "Error loading certificate");
		logger.error("loadPublicKeyFromFile - Error loading certificate");
		return false;
	}


	private boolean loadPublicKeyFromPKCS12File(String path, String alias, String password) {
		logger.debug("loadPublicKeyFromPKCS12File");
		if (password == null) {
			this.error.setError("CE006", "Password is required for PKCS12 certificates");
			logger.error("loadPublicKeyFromPKCS12File - Password is required for PKCS12 certificates");
			return false;
		}
		try (InputStream in = new DataInputStream(Files.newInputStream(new File(path).toPath()))) {
			KeyStore ks = KeyStore.getInstance("PKCS12");
			ks.load(in, password.toCharArray());
			if (alias.isEmpty()) {
				alias = ks.aliases().nextElement();
			}
			this.cert = (X509Certificate) ks.getCertificate(alias);
			inicializeParameters();
		} catch (Exception e) {
			this.error.setError("CE007", "Path not found.");
			logger.error("loadPublicKeyFromPKCS12File - Path not found.");
		}
		return this.cert != null;
	}


	private boolean loadPublicKeyFromPkcs8File(String path) {
		logger.debug("loadPublicKeyFromPkcs8File");

		try (FileReader privateKeyReader = new FileReader(path)) {
			try (PEMParser parser = new PEMParser(privateKeyReader)) {
				Object obj;
				obj = parser.readObject();
				if (obj instanceof PrivateKeyInfo || obj instanceof PEMKeyPair || obj instanceof SubjectPublicKeyInfo) {
					this.error.setError("CE008", "The file contains a private key");
					logger.error("loadPublicKeyFromPkcs8File - The file contains a private key");
				} else if (obj instanceof X509CertificateHolder) {

					X509CertificateHolder x509 = (X509CertificateHolder) obj;
					CertificateFactory certFactory = new CertificateFactory();
					try (InputStream in = new ByteArrayInputStream(x509.getEncoded())) {
						this.cert = (X509Certificate) certFactory.engineGenerateCertificate(in);
						inicializeParameters();
					}
				}
			}
		} catch (Exception e) {
			this.error.setError("CE008", e.getMessage());
			logger.error("loadPublicKeyFromPkcs8File", e);
		}
		return this.cert != null;
	}


	private boolean loadPublicKeyFromDERFile(String path) {
		logger.debug("loadPublicKeyFromDERFile");
		try (InputStream in = new DataInputStream(Files.newInputStream(new File(path).toPath()))) {
			CertificateFactory cf = new CertificateFactory();
			this.cert = (X509Certificate) cf.engineGenerateCertificate(in);
			inicializeParameters();
		} catch (Exception e) {
			this.error.setError("CE009", e.getMessage());
			logger.error("loadPublicKeyFromDERFile", e);
		}
		return this.cert != null;
	}

	private void inicializeParameters() {
		this.serialNumber = this.cert.getSerialNumber().toString();
		this.subject = this.cert.getSubjectDN().getName();
		this.version = this.cert.getVersion();
		this.issuer = this.cert.getIssuerDN().getName();
		this.thumbprint = "";
		this.notAfter = this.cert.getNotAfter();
		this.notBefore = this.cert.getNotBefore();
		this.algorithmWithHash = this.cert.getSigAlgName();
		extractPublicInfo();
		setAlgorithm();
	}


	private void extractPublicInfo() {
		Certificate cert1 = this.cert;
		PublicKey publicKey = cert1.getPublicKey();
		this.subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
	}
}
