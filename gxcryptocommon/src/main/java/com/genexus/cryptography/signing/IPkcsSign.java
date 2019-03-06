package com.genexus.cryptography.signing;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import org.bouncycastle.cms.CMSException;

import com.genexus.cryptography.exception.PrivateKeyNotFoundException;
import com.genexus.cryptography.exception.PublicKeyNotFoundException;
import com.genexus.cryptography.exception.SignatureException;

public interface IPkcsSign {

	String sign(byte[] data) throws GeneralSecurityException, CMSException, IOException, PublicKeyNotFoundException,
			PrivateKeyNotFoundException;

	boolean verify(byte[] data, byte[] aSignature) throws GeneralSecurityException, PublicKeyNotFoundException,
			SignatureException;

	void setCertificate(X509Certificate cert);

	X509Certificate getCertificate();
}
