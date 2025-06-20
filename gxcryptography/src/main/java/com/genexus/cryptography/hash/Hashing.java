package com.genexus.cryptography.hash;

import com.genexus.cryptography.commons.HashObject;
import com.genexus.cryptography.hash.utils.HashAlgorithm;
import com.genexus.securityapicommons.utils.SecurityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.*;
import org.bouncycastle.util.encoders.Hex;

import java.io.InputStream;

public class Hashing extends HashObject {

	private static final Logger logger = LogManager.getLogger(Hashing.class);

	public Hashing() {
		super();
	}

	public String doHash(String hashAlgorithm, String txtToHash) {
		this.error.cleanError();

		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(Hashing.class), "doHash", "hashAlgorithm", hashAlgorithm, this.error);
		SecurityUtils.validateStringInput(String.valueOf(Hashing.class), "doHash", "txtToHash", txtToHash, this.error);
		if (this.hasError()) {
			return "";
		}
		;
		// INPUT VERIFICATION - END

		HashAlgorithm hashAlgorithmObj = HashAlgorithm.getHashAlgorithm(hashAlgorithm, this.error);
		InputStream input = SecurityUtils.stringToStream(txtToHash, this.error);
		if (this.hasError()) {
			return null;
		}

		byte[] resBytes = calculateHash(hashAlgorithmObj, input);

		return this.hasError() ? "" : Hex.toHexString(resBytes).toUpperCase();
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/


	public byte[] calculateHash(HashAlgorithm hashAlgorithm, InputStream input) {
		logger.debug("calculateHash");
		Digest alg = createHash(hashAlgorithm);
		byte[] buffer = new byte[8192];
		int n;
		byte[] retValue = new byte[alg.getDigestSize()];
		try {
			while ((n = input.read(buffer)) > 0) {
				alg.update(buffer, 0, n);
			}
		} catch (Exception e) {
			this.error.setError("HA001", e.getMessage());
			logger.error("calculateHash", e);
		}
		alg.doFinal(retValue, 0);
		return retValue;
	}

	public Digest createHash(HashAlgorithm hashAlgorithm) {
		switch (hashAlgorithm) {
			case MD5:
				return new MD5Digest();
			case SHA1:
				return new SHA1Digest();
			case SHA224:
				return new SHA224Digest();
			case SHA256:
				return new SHA256Digest();
			case SHA384:
				return new SHA384Digest();
			case SHA512:
				return new SHA512Digest();
			case BLAKE2B_224:
				return new Blake2bDigest(224);
			case BLAKE2B_256:
				return new Blake2bDigest(256);
			case BLAKE2B_384:
				return new Blake2bDigest(384);
			case BLAKE2B_512:
				return new Blake2bDigest(512);
			case BLAKE2S_128:
				return new Blake2sDigest(128);
			case BLAKE2S_160:
				return new Blake2sDigest(160);
			case BLAKE2S_224:
				return new Blake2sDigest(224);
			case BLAKE2S_256:
				return new Blake2sDigest(256);
			case GOST3411_2012_256:
				return new GOST3411_2012_256Digest();
			case GOST3411_2012_512:
				return new GOST3411_2012_512Digest();
			case GOST3411:
				return new GOST3411Digest();
			case KECCAK_224:
				return new KeccakDigest(224);
			case KECCAK_256:
				return new KeccakDigest(256);
			case KECCAK_288:
				return new KeccakDigest(288);
			case KECCAK_384:
				return new KeccakDigest(384);
			case KECCAK_512:
				return new KeccakDigest(512);
			case MD2:
				return new MD2Digest();
			case MD4:
				return new MD4Digest();
			case RIPEMD128:
				return new RIPEMD128Digest();
			case RIPEMD160:
				return new RIPEMD160Digest();
			case RIPEMD256:
				return new RIPEMD256Digest();
			case RIPEMD320:
				return new RIPEMD320Digest();
			case SHA3_224:
				return new SHA3Digest(224);
			case SHA3_256:
				return new SHA3Digest(256);
			case SHA3_384:
				return new SHA3Digest(384);
			case SHA3_512:
				return new SHA3Digest(512);
			case SM3:
				return new SM3Digest();
			case TIGER:
				return new TigerDigest();
			case WHIRLPOOL:
				return new WhirlpoolDigest();
			default:
				this.error.setError("HA002", "Unrecognized HashAlgorithm");
				logger.error("Unrecognized HashAlgorithm");
				return null;
		}
	}

}
