package com.genexus.securityapicommons.commons;

import com.genexus.securityapicommons.utils.SecurityUtils;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.RSAKey;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class PublicKey extends Key {

	protected SubjectPublicKeyInfo subjectPublicKeyInfo;
	private static final Logger logger = LogManager.getLogger(PublicKey.class);

	public PublicKey() {
		super();
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/
	@Override
	public boolean load(String path) {
		logger.debug("load");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(SubjectPublicKeyInfo.class), "load", "path", path, this.error);

		if (!isPkcs8(path)) {
			this.error.setError("PU001", "Public key should be loaded from a .pem or .key file");
			logger.error("load - Public key should be loaded from a .pem or .key file");
			return false;
		}
		// INPUT VERIFICATION - END

		try {
			return loadPublicKeyFromFile(path);
		} catch (Exception e) {
			this.error.setError("PU002", e.getMessage());
			logger.error("load", e);
			return false;
		}
	}

	@Override
	public boolean fromBase64(String base64Data) {
		this.error.cleanError();
		this.subjectPublicKeyInfo = null;
		logger.debug("fromBase64");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(SubjectPublicKeyInfo.class), "fromBase64", "base64Data", base64Data, this.error);
		if (this.hasError()) {
			return false;
		}

		// INPUT VERIFICATION - END

		try {
			byte[] dataBuffer = Base64.decode(base64Data);
			this.subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(ASN1Sequence.getInstance(dataBuffer));
			setAlgorithm();
		} catch (Exception e) {
			this.error.setError("PU003", e.getMessage());
			logger.error("fromBase64", e);
		}
		return this.subjectPublicKeyInfo != null;
	}

	@Override
	public String toBase64() {
		this.error.cleanError();
		logger.debug("toBase64");
		if (this.subjectPublicKeyInfo == null) {
			this.error.setError("PU004", "Not loaded key");
			logger.error("toBase64 - Not loaded key");
			return "";
		}

		try {
			return new String(Base64.encode(this.subjectPublicKeyInfo.getEncoded()));
		} catch (Exception e) {
			this.error.setError("PU005", e.getMessage());
			logger.error("toBase64", e);
			return "";
		}
	}

	public boolean fromJwks(String jwks, String kid) {
		this.error.cleanError();
		logger.debug("fromJwks");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(SubjectPublicKeyInfo.class), "fromJwks", "jwks", jwks, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SubjectPublicKeyInfo.class), "fromJwks", "kid", kid, this.error);
		if (this.hasError()) {
			return false;
		}

		// INPUT VERIFICATION - END

		try {
			String b64 = fromJson(jwks, kid);
			return this.fromBase64(b64);
		} catch (Exception e) {
			this.error.setError("PU013", e.getMessage());
			logger.error("fromJwks", e);
		}
		return this.subjectPublicKeyInfo != null;
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	private String fromJson(String jwks, String kid) {
		this.error.cleanError();
		logger.debug("fromJson");
		try {
			return convert(JWKSet.parse(jwks).getKeyByKeyId(kid));
		} catch (Exception e) {
			this.error.setError("PU012", e.getMessage());
			logger.error("fromJson", e);
			return "";
		}
	}

	private String convert(JWK jwk) {
		logger.debug("convert");
		try {
			if (!jwk.getKeyType().equals(KeyType.RSA)) {
				throw new IllegalArgumentException("JWK must be an RSA public key");
			}

			RSAKey key = jwk.toRSAKey();
			RSAPublicKey rsaPublicKey = key.toRSAPublicKey();
			RSAPublicKeySpec spec = new RSAPublicKeySpec(rsaPublicKey.getModulus(), rsaPublicKey.getPublicExponent());
			KeyFactory f = KeyFactory.getInstance("RSA");
			java.security.PublicKey pub = f.generatePublic(spec);
			byte[] data = pub.getEncoded();
			return new String(java.util.Base64.getEncoder().encode(data));
		} catch (Exception e) {
			this.error.setError("PU011", e.getMessage());
			logger.error("convert", e);
			return "";
		}
	}

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
		logger.debug("getAsymmetricKeyParameter");
		try {
			return PublicKeyFactory.createKey(this.subjectPublicKeyInfo);
		} catch (Exception e) {
			this.error.setError("PU006", e.getMessage());
			logger.error("getAsymmetricKeyParameter", e);
			return null;
		}
	}

	public java.security.PublicKey getPublicKey() {
		logger.debug("getPublicKey");
		try {
			KeyFactory kf = SecurityUtils.getKeyFactory(this.algorithm);
			X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(this.subjectPublicKeyInfo.getEncoded());
			return kf.generatePublic(encodedKeySpec);
		} catch (Exception e) {
			this.error.setError("PU007", e.getMessage());
			logger.error("getPublicKey", e);
			return null;
		}
	}

	private boolean loadPublicKeyFromFile(String path) throws IOException {
		logger.debug("loadPublicKeyFromFile");
		try (FileReader privateKeyReader = new FileReader(path)) {
			try (PEMParser parser = new PEMParser(privateKeyReader)) {
				Object obj;
				obj = parser.readObject();
				if (obj instanceof PrivateKeyInfo) {
					this.error.setError("PU008", "The file contains a private key");
					logger.error("loadPublicKeyFromFile - The file contains a private key");
				}
				if (obj instanceof SubjectPublicKeyInfo) {
					this.subjectPublicKeyInfo = (SubjectPublicKeyInfo) obj;
					setAlgorithm();
				}
				if (obj instanceof PEMKeyPair) {
					PEMKeyPair keypair = (PEMKeyPair) obj;
					this.subjectPublicKeyInfo = keypair.getPublicKeyInfo();
					setAlgorithm();
				}
				if (obj instanceof ECPublicKeyParameters) {
					ECPublicKeyParameters ecParms = (ECPublicKeyParameters) obj;
					this.subjectPublicKeyInfo = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(ecParms);
					setAlgorithm();
				}
				if (obj instanceof RSAKeyParameters) {
					RSAKeyParameters rsaParms = (RSAKeyParameters) obj;
					this.subjectPublicKeyInfo = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(rsaParms);
					setAlgorithm();
				}
				if (obj instanceof X509CertificateHolder) {
					this.error.setError("PU009", "This file contains a certificate, use the Certificate object instead");
					logger.error("loadPublicKeyFromFile - This file contains a certificate, use the Certificate object instead");
				}
			}
		} catch (Exception e) {
			this.error.setError("PU010", e.getMessage());
			logger.error("loadPublicKeyFromFile", e);
		}
		return this.subjectPublicKeyInfo != null;
	}

	private boolean isPkcs8(String path) {
		for (String s : pkcs8_extensions) {
			if (FilenameUtils.getExtension(path).equals(s)) {
				return true;
			}
		}
		return false;
	}

}
