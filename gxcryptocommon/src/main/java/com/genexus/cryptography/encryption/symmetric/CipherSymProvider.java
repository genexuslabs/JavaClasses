package com.genexus.cryptography.encryption.symmetric;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.genexus.cryptography.Constants;
import com.genexus.cryptography.exception.AlgorithmNotSupportedException;
import com.genexus.cryptography.exception.EncryptionException;
import com.genexus.cryptography.exception.InvalidKeyLengthException;
import com.genexus.util.Base64;
import com.genexus.util.Codecs;

public class CipherSymProvider implements IGXSymEncryption {

	private int _keySize;
	private int _blockSize;
	byte[] _key; // key
	byte[] _iv; // initialization vector
	private Cipher _cipher;
	private String _alg;

	public CipherSymProvider(String algorithm) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyLengthException, AlgorithmNotSupportedException {
		this(algorithm, 0);
	}

	public CipherSymProvider(String algorithm, int keySize) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyLengthException, AlgorithmNotSupportedException {
		String[] algParts = algorithm.split("/");
		_keySize = keySize;
		_alg = algParts[0];
		_cipher = Cipher.getInstance(algorithm); // DES/ECB/PKCS5Padding for
													// SunJCE
		_key = generateKey(keySize);
		// _iv = generateIv();
	}

	public String encrypt(String text) throws EncryptionException {
		try {
			return encryption(Cipher.ENCRYPT_MODE, text.getBytes(Constants.UNICODE));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";
	}

	public String decrypt(String text) throws EncryptionException {
		byte[] data = Base64.decode(text);// Codecs.base64Decode(text.getBytes());			
		return encryption(Cipher.DECRYPT_MODE, data);				
	}

	private String encryption(int mode, byte[] data) throws EncryptionException {
		SecretKeySpec key = new SecretKeySpec(_key, _alg);
		IvParameterSpec ivParameterSpec = null;

		if (_iv != null) {
			ivParameterSpec = new IvParameterSpec(_iv);
		}
		try {
			_cipher.init(mode, key, ivParameterSpec);
			_iv = _cipher.getIV();
		} catch (InvalidAlgorithmParameterException e) {
			throw new EncryptionException("Alghorithm not supported", e);
		} catch (InvalidKeyException e) {
			throw new EncryptionException("Encryption key not valid", e);
		}

		try {
			switch (mode) {
			case Cipher.DECRYPT_MODE:
				return new String(_cipher.doFinal(data), Constants.UNICODE);
			case Cipher.ENCRYPT_MODE:
				return new String(Codecs.base64Encode(_cipher.doFinal(data)), Constants.UNICODE);
			default:
				return "";
			}
		} catch (Exception e) {
			throw new EncryptionException(e.getMessage(), e);
		}	
	}

	private byte[] generateKey(int keysize) throws InvalidKeyLengthException, AlgorithmNotSupportedException {
		KeyGenerator kg = null;
		try {
			kg = KeyGenerator.getInstance(_alg);
			if (keysize > 0) {
				kg.init(keysize / 8);
			}
		} catch (NoSuchAlgorithmException e) {

			throw new AlgorithmNotSupportedException();
		} catch (InvalidParameterException e) {
			throw new InvalidKeyLengthException(e);
		}
		byte[] result = kg.generateKey().getEncoded();
		_keySize = result.length * 8;
		return result;
	}

	public String getIV() {
		return com.genexus.util.Base64.encodeBytes(_iv);
	}

	public void setIV(String iv) {
		_iv = com.genexus.util.Base64.decode(iv);
	}

	public String getKey() {
		return com.genexus.util.Base64.encodeBytes(_key);
	}

	public void setKey(String Key) {
		_key = com.genexus.util.Base64.decode(Key);
	}

	public int getKeySize() {
		return _keySize;
	}

	public void setKeySize(int keySize) throws InvalidKeyLengthException, AlgorithmNotSupportedException {
		_keySize = keySize;
		_key = generateKey(keySize);
	}

	public int getBlockSize() {
		return _cipher.getBlockSize();
	}

	public void setBlockSize(int blockSize) {
		_blockSize = blockSize;
	}

	/*private byte[] generateIv() throws NoSuchAlgorithmException {
		AlgorithmParameters params = _cipher.getParameters();
		byte[] iv = null;
		try {
			iv = params.getParameterSpec(IvParameterSpec.class).getIV();
		} catch (InvalidParameterSpecException e) {

			e.printStackTrace();
		}
		return iv;
	}*/

}
