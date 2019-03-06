package com.genexus.specific.android;

import java.security.InvalidKeyException;

import com.genexus.common.interfaces.ICryptoAlhorithms;
import com.genexus.util.Rijndael_Algorithm;
import com.genexus.util.Twofish_Algorithm;

public class CryptoAlghorithms implements ICryptoAlhorithms {

	@Override
	public Object twoFish_BlockDecrypt(byte[] input, int i, Object key) {

		return Twofish_Algorithm.blockDecrypt(input, i, key);
	}

	@Override
	public Object twoFish_BlockEncrypt(byte[] input_copy, int i, Object key) {

		return Twofish_Algorithm.blockEncrypt(input_copy, i, key);
	}

	@Override
	public Object twoFish_makeKey(byte[] convertKey) throws InvalidKeyException {
		return Twofish_Algorithm.makeKey(convertKey);
	}

	@Override
	public Object rijndael_makeKey(byte[] keyBytes) throws InvalidKeyException {
		return Rijndael_Algorithm.makeKey(keyBytes);
	}

	@Override
	public int getRijndael_AlgorithmBLOCK_SIZE() {
		return Rijndael_Algorithm.BLOCK_SIZE;
	}

	@Override
	public void rijndael_BlockDecrypt(byte[] encryBytes, byte[] decryBytes, int blockStart, Object objKey) {
		Rijndael_Algorithm.blockDecrypt(encryBytes, decryBytes, blockStart, objKey);
	}

	@Override
	public void rijndael_BlockEncrypt(byte[] decryBytes, byte[] encryBytes, int blockStart, Object objKey) {
		Rijndael_Algorithm.blockEncrypt(decryBytes, encryBytes, blockStart, objKey);
	}

}
