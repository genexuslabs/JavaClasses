package com.genexus.securityapicommons.commons;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.encoders.Base64;

import com.genexus.securityapicommons.utils.SecurityUtils;

public class PublicKey extends Key {

	protected SubjectPublicKeyInfo subjectPublicKeyInfo;

	public PublicKey() {
		super();
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/
	@Override
	public boolean load(String path) {

		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateStringInput("path", path, this.error);
		if (!(SecurityUtils.extensionIs(path, ".pem") || SecurityUtils.extensionIs(path, "key"))) {
			this.error.setError("PU001", "Public key should be loaded from a .pem or .key file");
			return false;
		}
		/******* INPUT VERIFICATION - END *******/
		boolean loaded = false;
		try {
			loaded = loadPublicKeyFromFile(path);
		} catch (Exception e) {
			this.error.setError("PU002", e.getMessage());
			return false;
		}
		return loaded;
	}

	@Override
	public boolean fromBase64(String base64Data) {

		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateStringInput("base64Data", base64Data, this.error);
		if (this.hasError()) {
			return false;
		}

		/******* INPUT VERIFICATION - END *******/

		boolean flag;
		try {
			byte[] dataBuffer = Base64.decode(base64Data);
			this.subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(ASN1Sequence.getInstance(dataBuffer));
			flag = true;
		} catch (Exception e) {
			this.error.setError("PU003", e.getMessage());
			flag = false;
		}
		setAlgorithm();

		return flag;
	}

	@Override
	public String toBase64() {
		if (this.subjectPublicKeyInfo == null) {
			this.error.setError("PU004", "Not loaded key");
			return "";
		}
		String base64Encoded = "";

		try {
			base64Encoded = new String(Base64.encode(this.subjectPublicKeyInfo.getEncoded()));

		} catch (Exception e) {
			this.error.setError("PU005", e.getMessage());
		}

		return base64Encoded;
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	@Override
	protected void setAlgorithm() {
		if (this.subjectPublicKeyInfo == null) {
			return;
		}
		String alg = this.subjectPublicKeyInfo.getAlgorithm().getAlgorithm().getId();
		switch (alg) {
			case "1.2.840.113549.1.1.1":
				this.algorithm = "RSA";
				break;
			case "1.2.840.10045.2.1":
				this.algorithm = "ECDSA";
				break;
		}

	}

	@Override
	public AsymmetricKeyParameter getAsymmetricKeyParameter() {
		AsymmetricKeyParameter akp = null;
		try {
			akp = PublicKeyFactory.createKey(this.subjectPublicKeyInfo);
		} catch (Exception e) {
			this.error.setError("PU006", e.getMessage());
			return null;
		}
		return akp;
	}

	/**
	 * @return PublicKey type for the key type
	 */
	public java.security.PublicKey getPublicKey() {
		java.security.PublicKey pk = null;
		try {
			KeyFactory kf = SecurityUtils.getKeyFactory(this.algorithm);
			X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(this.subjectPublicKeyInfo.getEncoded());
			pk = kf.generatePublic(encodedKeySpec);
		} catch (Exception e) {
			this.error.setError("PU007", e.getMessage());
			return null;
		}
		return pk;
	}

	private boolean loadPublicKeyFromFile(String path) throws IOException {
		boolean flag = false;
		try (FileReader privateKeyReader = new FileReader(new File(path))) {
			try (PEMParser parser = new PEMParser(privateKeyReader)) {
				Object obj;
				obj = parser.readObject();
				if (obj instanceof PrivateKeyInfo) {
					this.error.setError("PU008", "The file contains a private key");
					flag = false;
				}
				if (obj instanceof SubjectPublicKeyInfo) {
					this.subjectPublicKeyInfo = (SubjectPublicKeyInfo) obj;
					setAlgorithm();
					flag = true;
				}
				if (obj instanceof PEMKeyPair) {
					PEMKeyPair keypair = (PEMKeyPair) obj;
					this.subjectPublicKeyInfo = keypair.getPublicKeyInfo();
					setAlgorithm();
					flag = true;
				}
				if (obj instanceof ECPublicKeyParameters) {
					ECPublicKeyParameters ecParms = (ECPublicKeyParameters) obj;
					this.subjectPublicKeyInfo = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(ecParms);
					setAlgorithm();
					flag = true;
				}
				if (obj instanceof RSAKeyParameters) {
					RSAKeyParameters rsaParms = (RSAKeyParameters) obj;
					this.subjectPublicKeyInfo = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(rsaParms);
					setAlgorithm();
					flag = true;
				}
				if (obj instanceof X509CertificateHolder) {
					this.error.setError("PU009", "This file contains a certificate, use the Certificate object instead");
					flag = false;
				}
			}
		}
		if (!flag && !this.hasError()) {
			this.error.setError("PU010", "Error loading public key from file");
		}
		return flag;
	}

}
