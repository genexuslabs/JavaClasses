package com.genexus.cryptography.asymmetric;

import com.genexus.cryptography.asymmetric.utils.AsymmetricSigningAlgorithm;
import com.genexus.cryptography.commons.AsymmetricSignerObject;
import com.genexus.cryptography.hash.Hashing;
import com.genexus.cryptography.hash.utils.HashAlgorithm;
import com.genexus.securityapicommons.commons.Key;
import com.genexus.securityapicommons.commons.PrivateKey;
import com.genexus.securityapicommons.commons.PublicKey;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.securityapicommons.utils.SecurityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.util.encoders.Base64;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class AsymmetricSigner extends AsymmetricSignerObject {

	private static final Logger logger = LogManager.getLogger(AsymmetricSigner.class);

	public AsymmetricSigner() {
		super();
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/

	@Override
	public String doSign(PrivateKeyManager key, String hashAlgorithm, String plainText) {
		logger.debug("doSign");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateObjectInput(String.valueOf(AsymmetricSigner.class), "doSign", "key", key, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricSigner.class), "doSign", "hashAlgorithm", hashAlgorithm, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricSigner.class), "doSign", "plainText", plainText, this.error);
		if (this.hasError()) {
			return "";
		}
		;
		// INPUT VERIFICATION - END

		EncodingUtil eu = new EncodingUtil();
		byte[] inputText = eu.getBytes(plainText);
		if (eu.hasError()) {
			this.error = eu.getError();
			return "";
		}

		try (InputStream inputStream = new ByteArrayInputStream(inputText)) {
			return sign(key, hashAlgorithm, inputStream);
		} catch (Exception e) {
			error.setError("AS001", e.getMessage());
			logger.error("doSign", e);
			return "";
		}
	}

	@Override
	public String doSignFile(PrivateKeyManager key, String hashAlgorithm, String path) {
		logger.debug("doSignFile");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateObjectInput(String.valueOf(AsymmetricSigner.class), "doSignFile", "key", key, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricSigner.class), "doSignFile", "hashAlgorithm", hashAlgorithm, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricSigner.class), "doSignFile", "path", path, this.error);
		if (this.hasError()) {
			return "";
		}
		// INPUT VERIFICATION - END

		try (InputStream input = SecurityUtils.getFileStream(path, this.error)) {
			if (this.hasError()) {
				return "";
			}

			return sign(key, hashAlgorithm, input);
		} catch (Exception e) {
			error.setError("AS002", e.getMessage());
			logger.error("doSignFile", e);
			return "";
		}
	}

	@Override
	public boolean doVerify(CertificateX509 cert, String plainText, String signature) {
		logger.debug("doVerify");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateObjectInput(String.valueOf(AsymmetricSigner.class), "doVerify", "cert", cert, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricSigner.class), "doVerify", "plainText", plainText, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricSigner.class), "doVerify", "signature", signature, this.error);
		if (this.hasError()) {
			return false;
		}
		// INPUT VERIFICATION - END

		EncodingUtil eu = new EncodingUtil();
		byte[] inputText = eu.getBytes(plainText);
		if (eu.hasError()) {
			this.error = eu.getError();
			return false;
		}

		try (InputStream inputStream = new ByteArrayInputStream(inputText)) {
			return verify(cert, inputStream, signature, null);
		} catch (Exception e) {
			error.setError("AS003", e.getMessage());
			logger.error("doVerify", e);
			return false;
		}
	}

	@Override
	public boolean doVerifyWithPublicKey(PublicKey key, String plainText, String signature, String hash) {
		logger.debug("doVerifyWithPublicKey");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateObjectInput(String.valueOf(AsymmetricSigner.class), "doVerifyWithPublicKey", "key", key, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricSigner.class), "doVerifyWithPublicKey", "plainText", plainText, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricSigner.class), "doVerifyWithPublicKey", "signature", signature, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricSigner.class), "doVerifyWithPublicKey", "hashAlgorithm", hash, this.error);
		if (this.hasError()) {
			return false;
		}
		// INPUT VERIFICATION - END

		EncodingUtil eu = new EncodingUtil();
		byte[] inputText = eu.getBytes(plainText);
		if (eu.hasError()) {
			this.error = eu.getError();
			return false;
		}

		try (InputStream inputStream = new ByteArrayInputStream(inputText)) {
			return verify(key, inputStream, signature, hash);
		} catch (Exception e) {
			error.setError("AS003", e.getMessage());
			logger.error("doVerifyWithPublicKey", e);
			return false;
		}
	}

	@Override
	public boolean doVerifyFile(CertificateX509 cert, String path, String signature) {
		logger.debug("doVerifyFile");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateObjectInput(String.valueOf(AsymmetricSigner.class), "doVerifyFile", "cert", cert, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricSigner.class), "doVerifyFile", "path", path, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricSigner.class), "doVerifyFile", "signature", signature, this.error);
		if (this.hasError()) {
			return false;
		}
		// INPUT VERIFICATION - END

		try (InputStream input = SecurityUtils.getFileStream(path, this.error)) {
			if (this.hasError()) {
				return false;
			}
			return verify(cert, input, signature, null);
		} catch (Exception e) {
			error.setError("AS004", e.getMessage());
			logger.error("doVerifyFile", e);
			return false;
		}
	}

	@Override
	public boolean doVerifyFileWithPublicKey(PublicKey key, String path, String signature, String hash) {
		logger.debug("doVerifyFileWithPublicKey");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateObjectInput(String.valueOf(AsymmetricSigner.class), "doVerifyFileWithPublicKey", "key", key, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricSigner.class), "doVerifyFileWithPublicKey", "path", path, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricSigner.class), "doVerifyFileWithPublicKey", "signature", signature, this.error);
		SecurityUtils.validateStringInput(String.valueOf(AsymmetricSigner.class), "doVerifyFileWithPublicKey", "hashAlgorithm", hash, this.error);
		if (this.hasError()) {
			return false;
		}
		// INPUT VERIFICATION - END


		try (InputStream input = SecurityUtils.getFileStream(path, this.error)) {
			if (this.hasError()) {
				return false;
			}
			return verify(key, input, signature, hash);
		} catch (Exception e) {
			error.setError("AS004", e.getMessage());
			logger.error("doVerifyFileWithPublicKey", e);
			return false;
		}
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	private String sign(PrivateKey key, String hashAlgorithm, InputStream input) {
		logger.debug("sign");
		PrivateKeyManager keyMan = (PrivateKeyManager) key;
		if (keyMan.hasError()) {
			this.error = keyMan.getError();
			return "";
		}
		AsymmetricSigningAlgorithm asymmetricSigningAlgorithm = AsymmetricSigningAlgorithm
			.getAsymmetricSigningAlgorithm(keyMan.getAlgorithm(), this.error);
		if (this.hasError())
			return "";
		Signer signer = AsymmetricSigningAlgorithm.getSigner(asymmetricSigningAlgorithm, getHash(hashAlgorithm),
			this.error);
		if (this.hasError())
			return "";
		setUpSigner(signer, input, keyMan.getAsymmetricKeyParameter(), true);
		if (this.hasError())
			return "";

		try {
			byte[] outputBytes = signer.generateSignature();
			return Base64.toBase64String(outputBytes);
		} catch (Exception e) {
			error.setError("AS005", e.getMessage());
			logger.error("sign", e);
			return "";
		}
	}

	private boolean verify(Key key, InputStream input, String signature, String hash) {
		logger.debug("verify");
		PublicKey cert = null;
		boolean isKey = false;
		if (hash == null) {
			cert = (CertificateX509) key;
		} else {
			cert = (PublicKey) key;
			isKey = true;
		}
		if (cert.hasError()) {
			this.error = cert.getError();
			return false;
		}
		String hashAlgorithm = "";
		if (isKey) {
			hashAlgorithm = hash;
		} else {
			if (SecurityUtils.compareStrings(((CertificateX509) cert).getPublicKeyHash(), "ECDSA")) {
				hashAlgorithm = "SHA1";
			} else {
				hashAlgorithm = ((CertificateX509) cert).getPublicKeyHash();
			}
		}
		AsymmetricSigningAlgorithm asymmetricSigningAlgorithm = AsymmetricSigningAlgorithm
			.getAsymmetricSigningAlgorithm(cert.getAlgorithm(), this.error);
		if (this.hasError())
			return false;
		Signer signer = AsymmetricSigningAlgorithm.getSigner(asymmetricSigningAlgorithm, getHash(hashAlgorithm),
			this.error);
		if (this.hasError())
			return false;
		setUpSigner(signer, input, cert.getAsymmetricKeyParameter(), false);
		if (this.hasError())
			return false;

		try {
			byte[] signatureBytes = Base64.decode(signature);
			return signer.verifySignature(signatureBytes);
		} catch (Exception e) {
			error.setError("AS007", e.getMessage());
			logger.error("verify", e);
			return false;
		}
	}

	private void setUpSigner(Signer signer, InputStream input, AsymmetricKeyParameter asymmetricKeyParameter,
							 boolean toSign) {
		logger.debug("setUpSigner");
		byte[] buffer = new byte[8192];
		int n;
		try {
			signer.init(toSign, asymmetricKeyParameter);
			while ((n = input.read(buffer)) > 0) {
				signer.update(buffer, 0, n);
			}
		} catch (Exception e) {
			error.setError("AS009", e.getMessage());
			logger.error("setUpSigner", e);
			return;
		}
	}

	private Digest getHash(String hashAlgorithm) {
		HashAlgorithm hash = HashAlgorithm.getHashAlgorithm(hashAlgorithm, this.error);
		if (this.hasError()) {
			return null;
		}
		Hashing hashing = new Hashing();
		Digest digest = hashing.createHash(hash);
		if (hashing.hasError()) {
			this.error = hashing.getError();
			return null;
		}
		return digest;
	}
}
