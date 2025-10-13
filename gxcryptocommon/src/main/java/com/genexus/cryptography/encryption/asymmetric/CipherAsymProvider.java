package com.genexus.cryptography.encryption.asymmetric;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;

import com.genexus.cryptography.Constants;
import com.genexus.cryptography.Utils;
import com.genexus.cryptography.exception.AlgorithmNotSupportedException;
import com.genexus.cryptography.exception.EncryptionException;
import com.genexus.cryptography.exception.PrivateKeyNotFoundException;
import com.genexus.cryptography.exception.PublicKeyNotFoundException;
import com.genexus.util.Codecs;

public class CipherAsymProvider implements IGXAsymEncryption {

	private String _algorithm; // "RSA/ECB/PKCS1Padding";
	private PublicKey _publicKey;
	private PrivateKey _privateKey;
	private Cipher _cipher;

	public CipherAsymProvider(String algorithm, PublicKey publicKey,
			PrivateKey privateKey) throws AlgorithmNotSupportedException {
		_algorithm = algorithm;
		_publicKey = publicKey;
		_privateKey = privateKey;
		try {
			_cipher = Cipher.getInstance(_algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new AlgorithmNotSupportedException(e);
		} catch (NoSuchPaddingException e) {
			System.err.println(e);
		}
	}

	public String encrypt(String data) throws PublicKeyNotFoundException, EncryptionException {
		if (_publicKey == null) {
			throw new PublicKeyNotFoundException();
		}
		String encryted = "";
		try {
			_cipher.init(Cipher.ENCRYPT_MODE, _publicKey);
			encryted = new String(Base64.encodeBase64(_cipher.doFinal(data
					.getBytes(Constants.UNICODE))));
		} catch (Exception e)
		{
			throw new EncryptionException(encryted, e);
		}
		return encryted;
	}

	public String decrypt(String data) throws PrivateKeyNotFoundException, EncryptionException {
		if (_privateKey == null) {
			throw new PrivateKeyNotFoundException();
		}
		byte[] dataBuffer = null;
		try {
			dataBuffer = Codecs.base64Decode(data.getBytes(Constants.UNICODE));
		} catch (UnsupportedEncodingException e1) {
			Utils.logError(e1);
		}
		String decrypted = "";
		try {
			_cipher.init(Cipher.DECRYPT_MODE, _privateKey);
			decrypted = new String(_cipher.doFinal(dataBuffer),
					Constants.UNICODE);
		} catch (Exception e)
		{
			throw new EncryptionException("Decrypt Error", e);
		}

		return decrypted;
	}

	
}
