package com.genexus.cryptography.encryption.asymmetric;

import com.genexus.cryptography.exception.EncryptionException;
import com.genexus.cryptography.exception.PrivateKeyNotFoundException;
import com.genexus.cryptography.exception.PublicKeyNotFoundException;

public interface IGXAsymEncryption {
	// / <exception cref="EncryptionException">Unknown error
	// occured.</exception>
	// / <exception cref="CertificateNotLoadedException">Certificate has not
	// been loaded</exception>
	String encrypt(String data) throws PublicKeyNotFoundException, EncryptionException;

	// / <exception cref="EncryptionException">Unknown error
	// occured.</exception>
	// / <exception cref="CertificateNotLoadedException">Certificate has not
	// been loaded.</exception>
	// / <exception cref="PrivateKeyNotFoundException">The certificate specified
	// does not contain private key needed to decrypt.</exception>
	String decrypt(String data) throws PrivateKeyNotFoundException, EncryptionException;
}