package com.genexus.securityapicommons.keys;

import com.genexus.securityapicommons.utils.SecurityUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder;
import org.bouncycastle.util.encoders.Base64;

import javax.crypto.EncryptedPrivateKeyInfo;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;


public class PrivateKeyManager extends com.genexus.securityapicommons.commons.PrivateKey {

	private static final Logger logger = LogManager.getLogger(PrivateKeyManager.class);

	private PrivateKeyInfo privateKeyInfo;
	private boolean hasPrivateKey;
	private String encryptionPassword;

	public PrivateKeyManager() {
		super();
		this.hasPrivateKey = false;
		this.encryptionPassword = null;
	}


	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/
	@Override
	public boolean load(String privateKeyPath) {
		this.error.cleanError();
		logger.debug("load");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(PrivateKeyManager.class), "load", "path", privateKeyPath, this.error);
		if (this.hasError()) {
			return false;
		}

		// INPUT VERIFICATION - END

		boolean result = loadPKCS12(privateKeyPath, "", "");
		setAlgorithm();
		return result;
	}

	@Override
	public boolean loadEncrypted(String privateKeyPath, String encryptionPassword) {
		logger.debug("loadEncrypted");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(PrivateKeyManager.class), "loadEncrypted", "path", privateKeyPath, this.error);
		SecurityUtils.validateStringInput(String.valueOf(PrivateKeyManager.class), "loadEncrypted", "password", encryptionPassword, this.error);
		if (this.hasError()) {
			return false;
		}

		// INPUT VERIFICATION - END

		this.encryptionPassword = encryptionPassword;
		boolean result = loadPKCS12(privateKeyPath, "", "");
		setAlgorithm();
		return result;

	}

	@Override
	public boolean loadPKCS12(String privateKeyPath, String alias, String password) {
		logger.debug("loadPKCS12");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(PrivateKeyManager.class), "loadPKCS12", "path", privateKeyPath, this.error);
		if (this.hasError()) {
			return false;
		}

		// INPUT VERIFICATION - END


		try {
			boolean result = loadKeyFromFile(privateKeyPath, alias, password);
			setAlgorithm();
			return !this.hasError() && result;
		} catch (Exception e) {
			this.error.setError("PK001", e.getMessage());
			logger.error("loadPKCS12", e);
			return false;
		}
	}

	@Override
	public boolean fromBase64(String base64) {
		logger.debug("fromBase64");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(PrivateKeyManager.class), "fromBase64", "base64", base64, this.error);
		if (this.hasError()) {
			return false;
		}

		// INPUT VERIFICATION - END
		try {
			byte[] keyBytes = Base64.decode(base64);
			try (ASN1InputStream istream = new ASN1InputStream(keyBytes)) {
				ASN1Sequence seq = (ASN1Sequence) istream.readObject();
				this.privateKeyInfo = PrivateKeyInfo.getInstance(seq);
				setAlgorithm();
				return this.privateKeyInfo != null;
			}
		} catch (Exception e) {
			this.error.setError("PK004", e.getMessage());
			logger.error("fromBase64", e);
			return false;
		}
	}

	@Override
	public String toBase64() {
		this.error.cleanError();
		logger.debug("toBase64");
		if (this.hasPrivateKey) {
			try {
				return Base64.toBase64String(this.privateKeyInfo.getEncoded());
			} catch (Exception e) {
				this.error.setError("PK003", e.getMessage());
				logger.error("toBase64", e);
				return "";
			}
		}
		this.error.setError("PK0016", "No private key loaded");
		logger.error("No private key loaded");
		return "";
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	public PrivateKey getPrivateKey() {
		logger.debug("getPrivateKey");
		try {
			KeyFactory kf = SecurityUtils.getKeyFactory(this.getAlgorithm());
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(this.privateKeyInfo.getEncoded());
			return kf.generatePrivate(keySpec);
		} catch (Exception e) {
			this.error.setError("PK005", e.getMessage());
			logger.error("getPrivateKey", e);
			return null;
		}
	}

	@Override
	public AsymmetricKeyParameter getAsymmetricKeyParameter() {
		logger.debug("getAsymmetricKeyParameter");
		try {
			return PrivateKeyFactory.createKey(this.privateKeyInfo);
		} catch (Exception e) {
			this.error.setError("PK006", e.getMessage());
			logger.error("getAsymmetricKeyParameter", e);
			return null;
		}
	}


	public boolean hasPrivateKey() {
		return this.hasPrivateKey;
	}

	private boolean loadKeyFromFile(String path, String alias, String password) {
		logger.debug("loadPrivateKeyFromFile");
		this.privateKeyInfo = null;
		for (String n : pkcs8_extensions) {
			if (FilenameUtils.getExtension(path).equals(n)) {
				return loadPrivateKeyFromPkcs8File(path);
			}
		}
		for (String s : pkcs12_extensions) {
			if (FilenameUtils.getExtension(path).equals(s)) {
				return loadPrivateKeyFromPKCS12File(path, alias, password);
			}
		}

		this.error.setError("PK007", "Error loading private key, invalid extension");
		logger.error("Error loading private key, invalid extension");
		this.hasPrivateKey = false;
		return false;
	}

	private boolean loadPrivateKeyFromPKCS12File(String path, String alias, String password) {
		logger.debug("loadPrivateKeyFromPKCS12File");
		if (password == null) {
			this.error.setError("PK008", "The password is required for PKCS12 keys");
			logger.error("The password is required for PKCS12 keys");
			return false;
		}
		try (InputStream in = new DataInputStream(Files.newInputStream(new File(path).toPath()))) {
			KeyStore ks = KeyStore.getInstance("PKCS12");
			ks.load(in, password.toCharArray());
			if (alias.isEmpty()) {
				alias = ks.aliases().nextElement();
			}
			if (ks.getKey(alias, password.toCharArray()) != null) {
				PrivateKey privateKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
				this.privateKeyInfo = PrivateKeyInfo.getInstance(privateKey.getEncoded());
			}
		} catch (Exception e) {
			this.error.setError("PK008", e.getMessage());
			logger.error("loadPrivateKeyFromPKCS12File", e);
		}
		return this.privateKeyInfo != null;
	}

	private boolean loadPrivateKeyFromPkcs8File(String path) {
		logger.debug("loadPrivateKeyFromPEMFile");
		try (FileReader privateKeyReader = new FileReader(path)) {
			try (PEMParser parser = new PEMParser(privateKeyReader)) {
				Object obj;
				obj = parser.readObject();
				if (obj instanceof EncryptedPrivateKeyInfo || obj instanceof PKCS8EncryptedPrivateKeyInfo) {
					Security.addProvider(new BouncyCastleProvider());
					PKCS8EncryptedPrivateKeyInfo encPrivKeyInfo = (PKCS8EncryptedPrivateKeyInfo) obj;
					InputDecryptorProvider pkcs8Prov = new JcePKCSPBEInputDecryptorProviderBuilder().setProvider("BC")
						.build(this.encryptionPassword.toCharArray());
					this.privateKeyInfo = encPrivKeyInfo.decryptPrivateKeyInfo(pkcs8Prov);
				} else if (obj instanceof PrivateKeyInfo) {
					this.privateKeyInfo = (PrivateKeyInfo) obj;
				} else if (obj instanceof PEMKeyPair) {
					PEMKeyPair pemKeyPair = (PEMKeyPair) obj;
					this.privateKeyInfo = pemKeyPair.getPrivateKeyInfo();
				} else if (obj instanceof SubjectPublicKeyInfo || obj instanceof X509CertificateHolder) {
					this.error.setError("PK009", "The file contains a public key");
					logger.error("The file contains a public key");
				}
			}
		} catch (Exception e) {
			this.error.setError("PK010", e.getMessage());
			logger.error("loadPrivateKeyFromPEMFile", e);
		}
		return this.privateKeyInfo != null;
	}

	@Override
	protected void setAlgorithm() {
		logger.debug("setAlgorithm");
		if (this.privateKeyInfo == null) {
			logger.debug("setAlgorithm this.privateKeyInfo = null");
			return;
		}
		this.hasPrivateKey = true;
		String alg = this.privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm().getId();
		switch (alg) {
			case "1.2.840.113549.1.1.1":
				this.algorithm = "RSA";
				break;
			case "1.2.840.10045.2.1":
				this.algorithm = "ECDSA";
				break;
		}
	}

}
