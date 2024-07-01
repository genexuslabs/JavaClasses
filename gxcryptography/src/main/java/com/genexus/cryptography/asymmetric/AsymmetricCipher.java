package com.genexus.cryptography.asymmetric;

import java.io.UnsupportedEncodingException;

import org.bouncycastle.cert.X509CertificateHolder;
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

/**
 * @author sgrampone
 *
 */
public class AsymmetricCipher extends AsymmetricCipherObject {

	/**
	 * AsymmetricCipher class constructor
	 */
	public AsymmetricCipher() {
		super();
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/

	@Override
	public String doEncrypt_WithPrivateKey(String hashAlgorithm, String asymmetricEncryptionPadding, PrivateKeyManager key, String plainText) {

		this.error.cleanError();
		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateObjectInput("hashAlgorithm", hashAlgorithm, this.error);
		SecurityUtils.validateStringInput("asymmetricEncryptionPadding", asymmetricEncryptionPadding, this.error);
		SecurityUtils.validateStringInput("plainText", plainText, this.error);
		SecurityUtils.validateObjectInput("key", key, this.error);
		if (this.hasError()) {
			return "";
		}

		/******* INPUT VERIFICATION - END *******/

		return doEncryptInternal(hashAlgorithm, asymmetricEncryptionPadding, key, true, plainText, false);
	}

	@Override
	public String doEncrypt_WithPublicKey(String hashAlgorithm, String asymmetricEncryptionPadding, PublicKey key, String plainText) {

		this.error.cleanError();
		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateObjectInput("hashAlgorithm", hashAlgorithm, this.error);
		SecurityUtils.validateStringInput("asymmetricEncryptionPadding", asymmetricEncryptionPadding, this.error);
		SecurityUtils.validateStringInput("plainText", plainText, this.error);
		SecurityUtils.validateObjectInput("key", key, this.error);
		if (this.hasError()) {
			return "";
		}

		/******* INPUT VERIFICATION - END *******/

		return doEncryptInternal(hashAlgorithm, asymmetricEncryptionPadding, key, false, plainText, true);
	}

	@Override
	public String doEncrypt_WithCertificate(String hashAlgorithm, String asymmetricEncryptionPadding, Certificate certificate, String plainText) {

		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateObjectInput("hashAlgorithm", hashAlgorithm, this.error);
		SecurityUtils.validateStringInput("asymmetricEncryptionPadding", asymmetricEncryptionPadding, this.error);
		SecurityUtils.validateStringInput("plainText", plainText, this.error);
		SecurityUtils.validateObjectInput("certificate", certificate, this.error);
		if (this.hasError()) {
			return "";
		}

		/******* INPUT VERIFICATION - END *******/

		return doEncryptInternal(hashAlgorithm, asymmetricEncryptionPadding, certificate, false, plainText, false);
	}


	@Override
	public String doDecrypt_WithPrivateKey(String hashAlgorithm, String asymmetricEncryptionPadding, PrivateKeyManager key, String encryptedInput) {

		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateObjectInput("hashAlgorithm", hashAlgorithm, this.error);
		SecurityUtils.validateStringInput("asymmetricEncryptionPadding", asymmetricEncryptionPadding, this.error);
		SecurityUtils.validateStringInput("encryptedInput", encryptedInput, this.error);
		SecurityUtils.validateObjectInput("key", key, this.error);
		if (this.hasError()) {
			return "";
		}

		/******* INPUT VERIFICATION - END *******/

		return doDecryptInternal(hashAlgorithm, asymmetricEncryptionPadding, key, true, encryptedInput, false);
	}

	@Override
	public String doDecrypt_WithPublicKey(String hashAlgorithm, String asymmetricEncryptionPadding, PublicKey key, String encryptedInput) {

		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateObjectInput("hashAlgorithm", hashAlgorithm, this.error);
		SecurityUtils.validateStringInput("asymmetricEncryptionPadding", asymmetricEncryptionPadding, this.error);
		SecurityUtils.validateStringInput("encryptedInput", encryptedInput, this.error);
		SecurityUtils.validateObjectInput("key", key, this.error);
		if (this.hasError()) {
			return "";
		}

		/******* INPUT VERIFICATION - END *******/

		return doDecryptInternal(hashAlgorithm, asymmetricEncryptionPadding, key, false, encryptedInput, true);
	}

	@Override
	public String doDecrypt_WithCertificate(String hashAlgorithm, String asymmetricEncryptionPadding, Certificate certificate, String encryptedInput) {

		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateObjectInput("hashAlgorithm", hashAlgorithm, this.error);
		SecurityUtils.validateStringInput("asymmetricEncryptionPadding", asymmetricEncryptionPadding, this.error);
		SecurityUtils.validateStringInput("encryptedInput", encryptedInput, this.error);
		SecurityUtils.validateObjectInput("certificate", certificate, this.error);
		if (this.hasError()) {
			return "";
		}

		/******* INPUT VERIFICATION - END *******/

		return doDecryptInternal(hashAlgorithm, asymmetricEncryptionPadding, certificate, false, encryptedInput, false);
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	/**
	 * @param asymmetricEncryptionAlgorithm
	 *            String AsymmetricEncryptionAlgorithm enum, algorithm name
	 * @param hashAlgorithm
	 *            String HashAlgorithm enum, algorithm name
	 * @param asymmetricEncryptionPadding
	 *            String AsymmetricEncryptionPadding enum, padding name
	 * @param keyPath
	 *            String path to key/certificate
	 * @param isPrivate
	 *            boolean true if key is provate, false if it is public
	 * @param alias
	 *            String keystore/certificate pkcs12 format alias
	 * @param password
	 *            Srting keysore/certificate pkcs12 format alias
	 * @param plainText
	 *            String UTF-8 to encrypt
	 * @return String Base64 encrypted plainText text
	 */
	private String doEncryptInternal(String hashAlgorithm, String asymmetricEncryptionPadding, Key key, boolean isPrivate,
									 String plainText, boolean isPublicKey) {
		error.cleanError();
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
		} catch (InvalidCipherTextException e) {
			this.error.setError("AE036", "Algoritmo inválido" + algorithm);
			//e.printStackTrace();
			return "";
		}
	}

	/**
	 * @param asymmetricEncryptionAlgorithm
	 *            String AsymmetricEncryptionAlgorithm enum, algorithm name
	 * @param hashAlgorithm
	 *            String HashAlgorithm enum, algorithm name
	 * @param asymmetricEncryptionPadding
	 *            String AsymmetricEncryptionPadding enum, padding name
	 * @param keyPath
	 *            String path to key/certificate
	 * @param isPrivate
	 *            boolean true if key is provate, false if it is public
	 * @param alias
	 *            String keystore/certificate pkcs12 format alias
	 * @param password
	 *            Srting keysore/certificate pkcs12 format alias
	 * @param encryptedInput
	 *            String Base64 to decrypt
	 * @return String UTF-8 decypted encryptedInput text
	 */
	private String doDecryptInternal(String hashAlgorithm, String asymmetricEncryptionPadding, Key key, boolean isPrivate,
									 String encryptedInput, boolean isPublicKey) {
		this.error.cleanError();
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
		} catch (InvalidCipherTextException | UnsupportedEncodingException e) {
			this.error.setError("AE039", "Algoritmo inválido" + algorithm);
			//e.printStackTrace();
			return "";
		}
	}

	/**
	 * @param asymmetricEncryptionAlgorithm
	 *            AsymmetricEncryptionAlgorithm enum, algorithm name
	 * @param hashAlgorithm
	 *            HashAlgorithm enum, algorithm name
	 * @param asymmetricEncryptionPadding
	 *            AsymmetricEncryptionPadding enum, padding name
	 * @param asymmetricKeyParameter
	 *            AsymmetricKeyParameter with loaded key for specified algorithm
	 * @param encryptedInput
	 *            String Base64 to decrypt
	 * @return String UTF-8 decypted encryptedInput text
	 * @throws InvalidCipherTextException
	 * @throws UnsupportedEncodingException
	 */
	private String doDecyrpt(AsymmetricEncryptionAlgorithm asymmetricEncryptionAlgorithm, HashAlgorithm hashAlgorithm,
							 AsymmetricEncryptionPadding asymmetricEncryptionPadding, AsymmetricKeyParameter asymmetricKeyParameter,
							 String encryptedInput) throws InvalidCipherTextException, UnsupportedEncodingException {
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

	/**
	 * @param asymmetricEncryptionAlgorithm
	 *            AsymmetricEncryptionAlgorithm enum, algorithm name
	 * @param hashAlgorithm
	 *            HashAlgorithm enum, algorithm name
	 * @param asymmetricEncryptionPadding
	 *            AsymmetricEncryptionPadding enum, padding name
	 * @param asymmetricKeyParameter
	 *            AsymmetricKeyParameter with loaded key for specified algorithm
	 * @param encryptedInput
	 *            String Base64 to decrypt
	 * @returnString Base64 encrypted encryptedInput text
	 * @throws InvalidCipherTextException
	 */
	private String doEncrypt(AsymmetricEncryptionAlgorithm asymmetricEncryptionAlgorithm, HashAlgorithm hashAlgorithm,
							 AsymmetricEncryptionPadding asymmetricEncryptionPadding, AsymmetricKeyParameter asymmetricKeyParameter,
							 String plainText) throws InvalidCipherTextException {
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

	/**
	 * @param asymmetricEncryptionAlgorithm
	 *            AsymmetricEncryptionAlgorithm enum, algorithm name
	 * @return AsymmetricBlockCipher Engine for the specified algorithm
	 */
	private AsymmetricBlockCipher getEngine(AsymmetricEncryptionAlgorithm asymmetricEncryptionAlgorithm) {

		switch (asymmetricEncryptionAlgorithm) {
			case RSA:
				return new RSAEngine();
			default:
				this.error.setError("AE042", "Unrecognized algorithm");
				return null;
		}

	}

	/**
	 * @param hashAlgorithm
	 *            HashAlgorithm enum, algorithm name
	 * @return Digest Engine for the specified algorithm
	 */
	private Digest getDigest(HashAlgorithm hashAlgorithm) {
		Hashing hash = new Hashing();
		Digest digest = hash.createHash(hashAlgorithm);
		if (digest == null) {
			this.error.setError("AE043", "Unrecognized HashAlgorithm");
			return null;
		}
		return digest;
	}

	/**
	 * @param asymBlockCipher
	 *            AsymmetricBlockCipher enum, algorithm name
	 * @param hash
	 *            Digest Engine for hashing
	 * @param asymmetricEncryptionPadding
	 *            AsymmetricEncryptionPadding enum, padding name
	 * @return AsymmetricBlockCipher Engine specific for the algoritm, hash and
	 *         padding
	 */
	private AsymmetricBlockCipher getPadding(AsymmetricBlockCipher asymBlockCipher, Digest hash,
											 AsymmetricEncryptionPadding asymmetricEncryptionPadding) {
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
				return null;
		}
	}

}
