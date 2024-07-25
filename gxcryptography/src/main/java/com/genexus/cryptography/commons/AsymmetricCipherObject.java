package com.genexus.cryptography.commons;

import com.genexus.securityapicommons.commons.Certificate;
import com.genexus.securityapicommons.commons.PublicKey;
import com.genexus.securityapicommons.commons.SecurityAPIObject;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;

/**
 * @author sgrampone
 *
 */
public abstract class AsymmetricCipherObject extends SecurityAPIObject {

	/**
	 * AsymmetricCipherObject constructor
	 */
	public AsymmetricCipherObject() {
		super();
	}

	public abstract String doEncrypt_WithPrivateKey(String hashAlgorithm, String asymmetricEncryptionPadding,
													PrivateKeyManager key, String plainText);

	public abstract String doEncrypt_WithPublicKey(String hashAlgorithm, String asymmetricEncryptionPadding,
												   PublicKey key, String plainText);

	public abstract String doEncrypt_WithCertificate(String hashAlgorithm, String asymmetricEncryptionPadding,
													 Certificate certificate, String plainText);

	public abstract String doDecrypt_WithPrivateKey(String hashAlgorithm, String asymmetricEncryptionPadding,
													PrivateKeyManager key, String encryptedInput);

	public abstract String doDecrypt_WithPublicKey(String hashAlgorithm, String asymmetricEncryptionPadding,
												   PublicKey key, String encryptedInput);

	public abstract String doDecrypt_WithCertificate(String hashAlgorithm, String asymmetricEncryptionPadding,
													 Certificate certificate, String encryptedInput);

}
