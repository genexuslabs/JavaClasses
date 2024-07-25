package com.genexus.cryptography.asymmetric;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.util.encoders.Base64;

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

public class AsymmetricSigner extends AsymmetricSignerObject {

	/**
	 * AsymmetricSigner class constructor
	 */
	public AsymmetricSigner() {
		super();
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/

	@Override
	public String doSign(PrivateKeyManager key, String hashAlgorithm, String plainText) {
		this.error.cleanError();

		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateObjectInput("key", key, this.error);
		SecurityUtils.validateStringInput("hashAlgorithm", hashAlgorithm, this.error);
		SecurityUtils.validateStringInput("plainText", plainText, this.error);
		if (this.hasError()) {
			return "";
		}
		;
		/******* INPUT VERIFICATION - END *******/

		EncodingUtil eu = new EncodingUtil();
		byte[] inputText = eu.getBytes(plainText);
		if (eu.hasError()) {
			this.error = eu.getError();
			return "";
		}
		String result = "";
		try (InputStream inputStream = new ByteArrayInputStream(inputText)) {
			result = sign(key, hashAlgorithm, inputStream);
		} catch (Exception e) {
			error.setError("AS001", e.getMessage());
		}
		return result;
	}

	@Override
	public String doSignFile(PrivateKeyManager key, String hashAlgorithm, String path) {
		this.error.cleanError();

		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateObjectInput("key", key, this.error);
		SecurityUtils.validateStringInput("hashAlgorithm", hashAlgorithm, this.error);
		SecurityUtils.validateStringInput("path", path, this.error);
		if (this.hasError()) {
			return "";
		}
		/******* INPUT VERIFICATION - END *******/

		String result = "";
		try (InputStream input = SecurityUtils.getFileStream(path, this.error)) {
			if (this.hasError()) {
				return "";
			}

			result = sign(key, hashAlgorithm, input);
		} catch (Exception e) {
			error.setError("AS002", e.getMessage());
		}
		return result;
	}

	@Override
	public boolean doVerify(CertificateX509 cert, String plainText, String signature) {
		this.error.cleanError();

		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateObjectInput("cert", cert, this.error);
		SecurityUtils.validateStringInput("plainText", plainText, this.error);
		SecurityUtils.validateStringInput("signature", signature, this.error);
		if (this.hasError()) {
			return false;
		}
		/******* INPUT VERIFICATION - END *******/

		EncodingUtil eu = new EncodingUtil();
		byte[] inputText = eu.getBytes(plainText);
		if (eu.hasError()) {
			this.error = eu.getError();
			return false;
		}
		boolean result = false;
		try (InputStream inputStream = new ByteArrayInputStream(inputText)) {
			result = verify(cert, inputStream, signature, null);
		} catch (Exception e) {
			error.setError("AS003", e.getMessage());
		}
		return result;
	}

	@Override
	public boolean doVerifyWithPublicKey(PublicKey key, String plainText, String signature, String hash) {
		this.error.cleanError();

		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateObjectInput("key", key, this.error);
		SecurityUtils.validateStringInput("plainText", plainText, this.error);
		SecurityUtils.validateStringInput("signature", signature, this.error);
		SecurityUtils.validateStringInput("hashAlgorithm", hash, this.error);
		if (this.hasError()) {
			return false;
		}
		/******* INPUT VERIFICATION - END *******/

		EncodingUtil eu = new EncodingUtil();
		byte[] inputText = eu.getBytes(plainText);
		if (eu.hasError()) {
			this.error = eu.getError();
			return false;
		}
		boolean result = false;
		try (InputStream inputStream = new ByteArrayInputStream(inputText)) {
			result = verify(key, inputStream, signature, hash);
		} catch (Exception e) {
			error.setError("AS003", e.getMessage());
		}
		return result;
	}

	@Override
	public boolean doVerifyFile(CertificateX509 cert, String path, String signature) {
		this.error.cleanError();

		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateObjectInput("cert", cert, this.error);
		SecurityUtils.validateStringInput("path", path, this.error);
		SecurityUtils.validateStringInput("signature", signature, this.error);
		if (this.hasError()) {
			return false;
		}
		/******* INPUT VERIFICATION - END *******/

		boolean result = false;
		try (InputStream input = SecurityUtils.getFileStream(path, this.error)) {
			if (this.hasError()) {
				return false;
			}
			result = verify(cert, input, signature, null);
		} catch (Exception e) {
			error.setError("AS004", e.getMessage());
		}
		return result;
	}

	@Override
	public boolean doVerifyFileWithPublicKey(PublicKey key, String path, String signature, String hash) {
		this.error.cleanError();

		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateObjectInput("key", key, this.error);
		SecurityUtils.validateStringInput("path", path, this.error);
		SecurityUtils.validateStringInput("signature", signature, this.error);
		SecurityUtils.validateStringInput("hashAlgorithm", hash, this.error);
		if (this.hasError()) {
			return false;
		}
		/******* INPUT VERIFICATION - END *******/

		boolean result = false;
		try (InputStream input = SecurityUtils.getFileStream(path, this.error)) {
			if (this.hasError()) {
				return false;
			}
			result = verify(key, input, signature, hash);
		} catch (Exception e) {
			error.setError("AS004", e.getMessage());
		}
		return result;
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	private String sign(PrivateKey key, String hashAlgorithm, InputStream input) {
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
		byte[] outputBytes = null;
		try {
			outputBytes = signer.generateSignature();
		} catch (Exception e) {
			error.setError("AS005", e.getMessage());
			return "";
		}
		String result = "";
		try {
			result = Base64.toBase64String(outputBytes);
		} catch (Exception e) {
			error.setError("AS006", e.getMessage());
			return "";
		}
		return result;
	}

	private boolean verify(Key key, InputStream input, String signature, String hash) {
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
		byte[] signatureBytes = null;
		try {
			signatureBytes = Base64.decode(signature);
		} catch (Exception e) {
			error.setError("AS007", e.getMessage());
			return false;
		}
		boolean result = false;
		try {
			result = signer.verifySignature(signatureBytes);
		} catch (Exception e) {
			error.setError("AS008", e.getMessage());
			return false;
		}
		return result;

	}

	private void setUpSigner(Signer signer, InputStream input, AsymmetricKeyParameter asymmetricKeyParameter,
							 boolean toSign) {
		try {
			signer.init(toSign, asymmetricKeyParameter);
		} catch (Exception e) {
			error.setError("AS009", e.getMessage());
			return;
		}
		byte[] buffer = new byte[8192];
		int n;
		try {
			while ((n = input.read(buffer)) > 0) {
				signer.update(buffer, 0, n);
			}
		} catch (Exception e) {
			error.setError("AS010", e.getMessage());
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
