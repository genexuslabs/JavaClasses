package com.genexus.cryptography.commons;

import com.genexus.securityapicommons.commons.PublicKey;
import com.genexus.securityapicommons.commons.SecurityAPIObject;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;

/**
 * @author sgrampone
 *
 */
public abstract class AsymmetricSignerObject extends SecurityAPIObject {

	/**
	 * AsymmetricSignerObject constructor
	 */
	public AsymmetricSignerObject() {
		super();
	}

	/**
	 * Implements signature calculationwith RSA or ECDSA keys.
	 *
	 * @param path
	 *            String path of the key/certificate file
	 * @param hashAlgorithm
	 *            String HashAlgorithm enum, algorithm name
	 * @param plainText
	 *            String UTF-8 text to sign
	 * @param options
	 *            Options data type to sel alias and pasword for pkcs12 certificate
	 * @return String Base64 signature of plainText text
	 */
	public abstract String doSign(PrivateKeyManager key, String hashAlgorithm, String plainText);

	public abstract String doSignFile(PrivateKeyManager key, String hashAlgorithm, String path);

	/**
	 * Implements signature verification with RSA or ECDSA keys
	 *
	 * @param path
	 *            String path of the key/certificate file
	 * @param plainText
	 *            String UTF-8 signed text
	 * @param signature
	 *            String Base64 signature of plainText
	 * @param options
	 *            Options data type to sel alias and pasword for pkcs12 certificate
	 * @return boolean true if signature is valid for the specified parameters,
	 *         false if it is invalid
	 */
	public abstract boolean doVerify(CertificateX509 cert, String plainText, String signature);

	public abstract boolean doVerifyWithPublicKey(PublicKey key, String plainText, String signature, String hash);

	public abstract boolean doVerifyFile(CertificateX509 cert, String path, String signature);

	public abstract boolean doVerifyFileWithPublicKey(PublicKey key, String path, String signature, String hash);

}
