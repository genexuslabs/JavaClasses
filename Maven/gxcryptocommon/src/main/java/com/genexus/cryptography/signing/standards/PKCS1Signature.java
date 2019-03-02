package com.genexus.cryptography.signing.standards;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.X509Certificate;

import org.apache.commons.codec.binary.Base64;

import com.genexus.cryptography.exception.PrivateKeyNotFoundException;
import com.genexus.cryptography.exception.PublicKeyNotFoundException;
import com.genexus.cryptography.signing.IPkcsSign;

public class PKCS1Signature implements IPkcsSign {

	/**
	 * Signs given document with a given private key.
	 */
	private String _algorithm;

	private X509Certificate _cert;
	private PrivateKey _pKey;


	public PKCS1Signature(String algorithm, X509Certificate cert, PrivateKey key) {
		_algorithm = algorithm;
		_cert = cert;
		_pKey = key;
		initialize();
	}


	private void initialize() {
		
	}
	


	public String sign(byte[] data) throws GeneralSecurityException, PrivateKeyNotFoundException {
		if (_pKey == null) {
			throw new PrivateKeyNotFoundException();
		}
		Signature signatureAlgorithm = Signature.getInstance(_algorithm);
		signatureAlgorithm.initSign(_pKey);
		signatureAlgorithm.update(data);
		byte[] digitalSignature = signatureAlgorithm.sign();
		return Base64.encodeBase64String(digitalSignature);
	}

	public boolean verify(byte[] data, byte[] aSignature) throws GeneralSecurityException, PublicKeyNotFoundException {
		if (_cert == null) {
			throw new PublicKeyNotFoundException();
		}
		PublicKey pKey = _cert.getPublicKey();
		Signature signatureAlgorithm = Signature.getInstance(_algorithm);
		signatureAlgorithm.initVerify(pKey);
		signatureAlgorithm.update(data);
		boolean valid = signatureAlgorithm.verify(aSignature);
		return valid;
	}

	public void setCertificate(X509Certificate cert) {
		this._cert = cert;
	}

	public X509Certificate getCertificate() {
		return _cert;
	}

}
