package com.genexus.cryptography.commons;

import com.genexus.securityapicommons.commons.PublicKey;
import com.genexus.securityapicommons.commons.SecurityAPIObject;
import com.genexus.securityapicommons.keys.CertificateX509;
import com.genexus.securityapicommons.keys.PrivateKeyManager;

public abstract class AsymmetricSignerObject extends SecurityAPIObject {

	public AsymmetricSignerObject() {
		super();
	}

	public abstract String doSign(PrivateKeyManager key, String hashAlgorithm, String plainText);

	public abstract String doSignFile(PrivateKeyManager key, String hashAlgorithm, String path);

	public abstract boolean doVerify(CertificateX509 cert, String plainText, String signature);

	public abstract boolean doVerifyWithPublicKey(PublicKey key, String plainText, String signature, String hash);

	public abstract boolean doVerifyFile(CertificateX509 cert, String path, String signature);

	public abstract boolean doVerifyFileWithPublicKey(PublicKey key, String path, String signature, String hash);

}
