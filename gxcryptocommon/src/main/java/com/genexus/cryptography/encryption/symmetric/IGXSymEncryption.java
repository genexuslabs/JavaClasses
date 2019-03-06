package com.genexus.cryptography.encryption.symmetric;

import com.genexus.cryptography.exception.AlgorithmNotSupportedException;
import com.genexus.cryptography.exception.EncryptionException;
import com.genexus.cryptography.exception.InvalidKeyLengthException;

public interface IGXSymEncryption {

	String encrypt(String text) throws EncryptionException;

	String decrypt(String text) throws EncryptionException;

	String getIV();

	void setIV(String iv);

	String getKey();

	void setKey(String Key);

	int getKeySize();

	void setKeySize(int keySize) throws InvalidKeyLengthException, AlgorithmNotSupportedException;

	int getBlockSize();

	void setBlockSize(int blockSize);
}
