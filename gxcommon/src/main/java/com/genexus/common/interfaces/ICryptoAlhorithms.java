package com.genexus.common.interfaces;

import java.security.InvalidKeyException;

public interface ICryptoAlhorithms {

	Object twoFish_BlockDecrypt(byte[] input, int i, Object key);

	Object twoFish_BlockEncrypt(byte[] input_copy, int i, Object key);

	Object twoFish_makeKey(byte[] convertKey) throws InvalidKeyException;

	Object rijndael_makeKey(byte[] keyBytes) throws InvalidKeyException;

	int getRijndael_AlgorithmBLOCK_SIZE();

	void rijndael_BlockDecrypt(byte[] encryBytes, byte[] decryBytes, int blockStart, Object objKey);

	void rijndael_BlockEncrypt(byte[] decryBytes, byte[] encryBytes, int blockStart, Object objKey);

}
