package com.genexus.cryptography.asymmetric;

import com.genexus.cryptography.asymmetric.utils.AsymmetricEncryptionAlgorithm;
import com.genexus.cryptography.asymmetric.utils.AsymmetricEncryptionPadding;
import com.genexus.cryptography.commons.AsymmetricCipherObject;
import com.genexus.cryptography.hash.Hashing;
import com.genexus.cryptography.hash.utils.HashAlgorithm;
import com.genexus.securityapicommons.commons.Certificate;
import com.genexus.securityapicommons.commons.Key;
import com.genexus.securityapicommons.commons.PublicKey;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.securityapicommons.utils.SecurityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.BufferedAsymmetricBlockCipher;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.encodings.ISO9796d1Encoding;
import org.bouncycastle.crypto.encodings.OAEPEncoding;
import org.bouncycastle.crypto.encodings.PKCS1Encoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.util.encoders.Base64;

public class AsymmetricCipher extends AsymmetricCipherObject {

	private static final Logger logger = LogManager.getLogger(AsymmetricCipher.class);

	public AsymmetricCipher() {
		super();
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/

	@Override
	public String doEncrypt_WithPrivateKey(String hashAlgorithm, String asymmetricEncryptionPadding, PrivateKeyManager key, String plainText) {
		logger.debug("doEncrypt_WithPrivateKey");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateObjectInput(String.valueOf(AsymmetricCipher.class), "doEncrypt_WithPrivateKey", "hashAlgorithm", hashAlgorithm, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricCipher.class), "doEncrypt_WithPrivateKey", "asymmetricEncryptionPadding", asymmetricEncryptionPadding, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricCipher.class), "doEncrypt_WithPrivateKey","plainText", plainText, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(AsymmetricCipher.class), "doEncrypt_WithPrivateKey","key", key, this.error);
		if (this.hasError()) {
			return "";
		}

		// INPUT VERIFICATION - END

		return doEncryptInternal(hashAlgorithm, asymmetricEncryptionPadding, key, true, plainText, false);
	}

	@Override
	public String doEncrypt_WithPublicKey(String hashAlgorithm, String asymmetricEncryptionPadding, PublicKey key, String plainText) {
		this.error.cleanError();
		logger.debug("doEncrypt_WithPublicKey");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateObjectInput(String.valueOf(AsymmetricCipher.class), "doEncrypt_WithPublicKey","hashAlgorithm", hashAlgorithm, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricCipher.class), "doEncrypt_WithPublicKey","asymmetricEncryptionPadding", asymmetricEncryptionPadding, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricCipher.class), "doEncrypt_WithPublicKey","plainText", plainText, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(AsymmetricCipher.class), "doEncrypt_WithPublicKey","key", key, this.error);
		if (this.hasError()) {
			return "";
		}

		// INPUT VERIFICATION - END

		return doEncryptInternal(hashAlgorithm, asymmetricEncryptionPadding, key, false, plainText, true);
	}

	@Override
	public String doEncrypt_WithCertificate(String hashAlgorithm, String asymmetricEncryptionPadding, Certificate certificate, String plainText) {
		this.error.cleanError();
		logger.debug("doEncrypt_WithCertificate");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateObjectInput(String.valueOf(AsymmetricCipher.class), "doEncrypt_WithCertificate","hashAlgorithm", hashAlgorithm, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricCipher.class), "doEncrypt_WithCertificate","asymmetricEncryptionPadding", asymmetricEncryptionPadding, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricCipher.class), "doEncrypt_WithCertificate","plainText", plainText, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(AsymmetricCipher.class), "doEncrypt_WithCertificate","certificate", certificate, this.error);
		if (this.hasError()) {
			return "";
		}

		// INPUT VERIFICATION - END

		return doEncryptInternal(hashAlgorithm, asymmetricEncryptionPadding, certificate, false, plainText, false);
	}


	@Override
	public String doDecrypt_WithPrivateKey(String hashAlgorithm, String asymmetricEncryptionPadding, PrivateKeyManager key, String encryptedInput) {
		this.error.cleanError();
		logger.debug("doDecrypt_WithPrivateKey");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateObjectInput(String.valueOf(AsymmetricCipher.class), "doDecrypt_WithPrivateKey","hashAlgorithm", hashAlgorithm, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricCipher.class), "doDecrypt_WithPrivateKey","asymmetricEncryptionPadding", asymmetricEncryptionPadding, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricCipher.class), "doDecrypt_WithPrivateKey","encryptedInput", encryptedInput, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(AsymmetricCipher.class), "doDecrypt_WithPrivateKey","key", key, this.error);
		if (this.hasError()) {
			return "";
		}

		// INPUT VERIFICATION - END

		return doDecryptInternal(hashAlgorithm, asymmetricEncryptionPadding, key, true, encryptedInput, false);
	}

	@Override
	public String doDecrypt_WithPublicKey(String hashAlgorithm, String asymmetricEncryptionPadding, PublicKey key, String encryptedInput) {
		this.error.cleanError();
		logger.debug("doDecrypt_WithPublicKey");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateObjectInput(String.valueOf(AsymmetricCipher.class), "doDecrypt_WithPublicKey","hashAlgorithm", hashAlgorithm, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricCipher.class), "doDecrypt_WithPublicKey","asymmetricEncryptionPadding", asymmetricEncryptionPadding, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricCipher.class), "doDecrypt_WithPublicKey","encryptedInput", encryptedInput, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(AsymmetricCipher.class), "doDecrypt_WithPublicKey","key", key, this.error);
		if (this.hasError()) {
			return "";
		}

		// INPUT VERIFICATION - END

		return doDecryptInternal(hashAlgorithm, asymmetricEncryptionPadding, key, false, encryptedInput, true);
	}

	@Override
	public String doDecrypt_WithCertificate(String hashAlgorithm, String asymmetricEncryptionPadding, Certificate certificate, String encryptedInput) {
		this.error.cleanError();
		logger.debug("doDecrypt_WithCertificate");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateObjectInput(String.valueOf(AsymmetricCipher.class), "doDecrypt_WithCertificate","hashAlgorithm", hashAlgorithm, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricCipher.class), "doDecrypt_WithCertificate","asymmetricEncryptionPadding", asymmetricEncryptionPadding, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricCipher.class), "doDecrypt_WithCertificate","encryptedInput", encryptedInput, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(AsymmetricCipher.class), "doDecrypt_WithCertificate","certificate", certificate, this.error);
		if (this.hasError()) {
			return "";
		}

		// INPUT VERIFICATION - END

		return doDecryptInternal(hashAlgorithm, asymmetricEncryptionPadding, certificate, false, encryptedInput, false);
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/


	private String doEncryptInternal(String hashAlgorithm, String asymmetricEncryptionPadding, Key key, boolean isPrivate,
									 String plainText, boolean isPublicKey) {
		logger.debug("doEncryptInternal");
		HashAlgorithm hash = HashAlgorithm.getHashAlgorithm(hashAlgorithm, this.error);
		AsymmetricEncryptionPadding padding = AsymmetricEncryptionPadding
			.getAsymmetricEncryptionPadding(asymmetricEncryptionPadding, this.error);
		if (this.error.existsError()) {
			return "";
		}

		String asymmetricEncryptionAlgorithm = "";
		AsymmetricKeyParameter asymKey = null;
		if (isPrivate) {
			PrivateKeyManager keyMan = (PrivateKeyManager) key;
			if (!keyMan.hasPrivateKey() || keyMan.hasError()) {
				this.error = keyMan.getError();
				return "";
			}
			asymmetricEncryptionAlgorithm = keyMan.getAlgorithm();

			asymKey = keyMan.getAsymmetricKeyParameter();
			if (keyMan.hasError()) {
				this.error = keyMan.getError();
				return "";
			}
		} else {
			PublicKey cert = isPublicKey ? (PublicKey)key: (CertificateX509) key;
			if (cert.hasError()) {
				this.error = cert.getError();
				return "";
			}
			asymmetricEncryptionAlgorithm = cert.getAlgorithm();
			asymKey = cert.getAsymmetricKeyParameter();
			if (cert.hasError()) {
				this.error = cert.getError();
				return "";
			}
		}

		AsymmetricEncryptionAlgorithm algorithm = AsymmetricEncryptionAlgorithm
			.getAsymmetricEncryptionAlgorithm(asymmetricEncryptionAlgorithm, this.error);
		try {
			this.error.cleanError();
			return doEncrypt(algorithm, hash, padding, asymKey, plainText);
		} catch (Exception e) {
			this.error.setError("AE036", e.getMessage());
			logger.error("doEncryptInternal", e);
			return "";
		}
	}

	private String doDecryptInternal(String hashAlgorithm, String asymmetricEncryptionPadding, Key key, boolean isPrivate,
									 String encryptedInput, boolean isPublicKey) {
		logger.debug("doDecryptInternal");
		HashAlgorithm hash = HashAlgorithm.getHashAlgorithm(hashAlgorithm, this.error);
		AsymmetricEncryptionPadding padding = AsymmetricEncryptionPadding
			.getAsymmetricEncryptionPadding(asymmetricEncryptionPadding, this.error);
		if (this.error.existsError()) {
			return "";
		}

		String asymmetricEncryptionAlgorithm = "";
		AsymmetricKeyParameter asymKey = null;

		if (isPrivate) {
			PrivateKeyManager keyMan = (PrivateKeyManager) key;
			if (!keyMan.hasPrivateKey() || keyMan.hasError()) {
				this.error = keyMan.getError();
				return "";
			}
			asymmetricEncryptionAlgorithm = keyMan.getAlgorithm();

			asymKey = keyMan.getAsymmetricKeyParameter();
			if (keyMan.hasError()) {
				this.error = keyMan.getError();
				return "";
			}
		} else {
			PublicKey cert = isPublicKey ? (PublicKey) key: (CertificateX509) key;
			if (cert.hasError()) {
				this.error = cert.getError();
				return "";
			}
			asymmetricEncryptionAlgorithm = cert.getAlgorithm();
			asymKey = cert.getAsymmetricKeyParameter();
			if (cert.hasError()) {
				this.error = cert.getError();
				return "";
			}
		}

		AsymmetricEncryptionAlgorithm algorithm = AsymmetricEncryptionAlgorithm
			.getAsymmetricEncryptionAlgorithm(asymmetricEncryptionAlgorithm, this.error);
		try {
			this.error.cleanError();
			return doDecyrpt(algorithm, hash, padding, asymKey, encryptedInput);
		} catch (Exception e) {
			this.error.setError("AE039", e.getMessage());
			logger.error("doDecryptInternal", e);
			return "";
		}
	}

	private String doDecyrpt(AsymmetricEncryptionAlgorithm asymmetricEncryptionAlgorithm, HashAlgorithm hashAlgorithm,
							 AsymmetricEncryptionPadding asymmetricEncryptionPadding, AsymmetricKeyParameter asymmetricKeyParameter,
							 String encryptedInput) throws InvalidCipherTextException {
		logger.debug("doDecyrpt");
		AsymmetricBlockCipher asymEngine = getEngine(asymmetricEncryptionAlgorithm);
		Digest hash = getDigest(hashAlgorithm);
		AsymmetricBlockCipher cipher = getPadding(asymEngine, hash, asymmetricEncryptionPadding);
		BufferedAsymmetricBlockCipher bufferedCipher = new BufferedAsymmetricBlockCipher(cipher);
		if (this.error.existsError()) {
			return "";
		}
		bufferedCipher.init(false, asymmetricKeyParameter);
		byte[] inputBytes = Base64.decode(encryptedInput);
		bufferedCipher.processBytes(inputBytes, 0, inputBytes.length);
		byte[] outputBytes = bufferedCipher.doFinal();
		if (outputBytes == null || outputBytes.length == 0) {
			this.error.setError("AE040", "Asymmetric decryption error");
			return "";
		}

		EncodingUtil eu = new EncodingUtil();

		this.error = eu.getError();
		return eu.getString(outputBytes);

	}

	private String doEncrypt(AsymmetricEncryptionAlgorithm asymmetricEncryptionAlgorithm, HashAlgorithm hashAlgorithm,
							 AsymmetricEncryptionPadding asymmetricEncryptionPadding, AsymmetricKeyParameter asymmetricKeyParameter,
							 String plainText) throws InvalidCipherTextException {
		logger.debug("doEncrypt");
		AsymmetricBlockCipher asymEngine = getEngine(asymmetricEncryptionAlgorithm);
		Digest hash = getDigest(hashAlgorithm);
		AsymmetricBlockCipher cipher = getPadding(asymEngine, hash, asymmetricEncryptionPadding);
		BufferedAsymmetricBlockCipher bufferedCipher = new BufferedAsymmetricBlockCipher(cipher);
		if (this.error.existsError()) {
			return "";
		}
		bufferedCipher.init(true, asymmetricKeyParameter);
		EncodingUtil eu = new EncodingUtil();

		byte[] inputBytes = eu.getBytes(plainText);
		if (eu.hasError()) {
			this.error = eu.getError();
			return "";
		}
		bufferedCipher.processBytes(inputBytes, 0, inputBytes.length);
		byte[] outputBytes = bufferedCipher.doFinal();
		if (outputBytes == null || outputBytes.length == 0) {
			this.error.setError("AE041", "Asymmetric encryption error");
			return "";
		}
		return new String(Base64.encode(outputBytes));

	}


	@SuppressWarnings("SwitchStatementWithTooFewBranches")
	private AsymmetricBlockCipher getEngine(AsymmetricEncryptionAlgorithm asymmetricEncryptionAlgorithm) {
		logger.debug("getEngine");
		switch (asymmetricEncryptionAlgorithm) {
			case RSA:
				return new RSAEngine();
			default:
				this.error.setError("AE042", "Unrecognized algorithm");
				logger.error("Unrecognized algorithm");
				return null;
		}

	}

	private Digest getDigest(HashAlgorithm hashAlgorithm) {
		logger.debug("getDigest");
		Hashing hash = new Hashing();
		Digest digest = hash.createHash(hashAlgorithm);
		if (digest == null) {
			this.error.setError("AE043", "Unrecognized HashAlgorithm");
			logger.error("Unrecognized HashAlgorithm");
			return null;
		}
		return digest;
	}


	private AsymmetricBlockCipher getPadding(AsymmetricBlockCipher asymBlockCipher, Digest hash,
											 AsymmetricEncryptionPadding asymmetricEncryptionPadding) {
		logger.debug("getPadding");
		switch (asymmetricEncryptionPadding) {
			case NOPADDING:
				return null;
			case OAEPPADDING:
				if (hash != null) {
					return new OAEPEncoding(asymBlockCipher, hash);
				} else {
					return new OAEPEncoding(asymBlockCipher);
				}
			case PCKS1PADDING:
				return new PKCS1Encoding(asymBlockCipher);
			case ISO97961PADDING:
				return new ISO9796d1Encoding(asymBlockCipher);
			default:
				error.setError("AE044", "Unrecognized AsymmetricEncryptionPadding");
				logger.error("Unrecognized AsymmetricEncryptionPadding");
				return null;
		}
	}

}
