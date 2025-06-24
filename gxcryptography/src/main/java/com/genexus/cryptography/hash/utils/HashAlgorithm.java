package com.genexus.cryptography.hash.utils;

import com.genexus.securityapicommons.commons.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum HashAlgorithm {

	NONE, MD5, SHA1, SHA224, SHA256, SHA384, SHA512, BLAKE2B_224, BLAKE2B_256, BLAKE2B_384, BLAKE2B_512, BLAKE2S_128, BLAKE2S_160, BLAKE2S_224, BLAKE2S_256, GOST3411_2012_256, GOST3411_2012_512, GOST3411, KECCAK_224, KECCAK_256, KECCAK_288, KECCAK_384, KECCAK_512, MD2, MD4, RIPEMD128, RIPEMD160, RIPEMD256, RIPEMD320, SHA3_224, SHA3_256, SHA3_384, SHA3_512, SHAKE_128, SHAKE_256, SM3, TIGER, WHIRLPOOL,
	;

	private static final Logger logger = LogManager.getLogger(HashAlgorithm.class);

	public static HashAlgorithm getHashAlgorithm(String hashAlgorithm, Error error) {
		if (error == null) return HashAlgorithm.NONE;
		if (hashAlgorithm == null) {
			error.setError("HAA01", "Unrecognized HashAlgorihm");
			return HashAlgorithm.NONE;
		}
		switch (hashAlgorithm.toUpperCase().trim()) {
			case "MD5":
				return HashAlgorithm.MD5;
			case "SHA1":
				return HashAlgorithm.SHA1;
			case "SHA224":
				return HashAlgorithm.SHA224;
			case "SHA256":
				return HashAlgorithm.SHA256;
			case "SHA384":
				return HashAlgorithm.SHA384;
			case "SHA512":
				return HashAlgorithm.SHA512;
			case "BLAKE2B_224":
				return HashAlgorithm.BLAKE2B_224;
			case "BLAKE2B_256":
				return HashAlgorithm.BLAKE2B_256;
			case "BLAKE2B_384":
				return HashAlgorithm.BLAKE2B_384;
			case "BLAKE2B_512":
				return HashAlgorithm.BLAKE2B_512;
			case "BLAKE2S_128":
				return HashAlgorithm.BLAKE2S_128;
			case "BLAKE2S_160":
				return HashAlgorithm.BLAKE2S_160;
			case "BLAKE2S_224":
				return HashAlgorithm.BLAKE2S_224;
			case "BLAKE2S_256":
				return HashAlgorithm.BLAKE2S_256;
			case "GOST3411_2012_256":
				return HashAlgorithm.GOST3411_2012_256;
			case "GOST3411_2012_512":
				return HashAlgorithm.GOST3411_2012_512;
			case "GOST3411":
				return HashAlgorithm.GOST3411;
			case "KECCAK_224":
				return HashAlgorithm.KECCAK_224;
			case "KECCAK_256":
				return HashAlgorithm.KECCAK_256;
			case "KECCAK_288":
				return HashAlgorithm.KECCAK_288;
			case "KECCAK_384":
				return HashAlgorithm.KECCAK_384;
			case "KECCAK_512":
				return HashAlgorithm.KECCAK_512;
			case "MD2":
				return HashAlgorithm.MD2;
			case "MD4":
				return HashAlgorithm.MD4;
			case "RIPEMD128":
				return HashAlgorithm.RIPEMD128;
			case "RIPEMD160":
				return HashAlgorithm.RIPEMD160;
			case "RIPEMD256":
				return HashAlgorithm.RIPEMD256;
			case "RIPEMD320":
				return HashAlgorithm.RIPEMD320;
			case "SHA3-224":
				return HashAlgorithm.SHA3_224;
			case "SHA3-256":
				return HashAlgorithm.SHA3_256;
			case "SHA3-384":
				return HashAlgorithm.SHA3_384;
			case "SHA3-512":
				return HashAlgorithm.SHA3_512;
			case "SHAKE_128":
				error.setError("HAA04", "Not implemented algorithm SHAKE_128");
				logger.error("Not implemented algorithm SHAKE_128");
				return null;
			case "SHAKE_256":
				error.setError("HAA05", "Not implemented algorithm SHAKE_256");
				logger.error("Not implemented algorithm SHAKE_256");
				return null;
			case "SM3":
				return HashAlgorithm.SM3;
			case "TIGER":
				return HashAlgorithm.TIGER;
			case "WHIRLPOOL":
				return HashAlgorithm.WHIRLPOOL;
			default:
				error.setError("HAA02", "Unrecognized HashAlgorihm");
				logger.error("Unrecognized HashAlgorihm");
				return null;
		}
	}

	public static String valueOf(HashAlgorithm hashAlgorithm, Error error) {
		if (error == null) return "Unrecognized algorithm";
		switch (hashAlgorithm) {
			case MD5:
				return "MD5";
			case SHA1:
				return "SHA1";
			case SHA224:
				return "SHA224";
			case SHA256:
				return "SHA256";
			case SHA384:
				return "SHA384";
			case SHA512:
				return "SHA512";
			case BLAKE2B_224:
				return "BLAKE2B_224";
			case BLAKE2B_256:
				return "BLAKE2B_256";
			case BLAKE2B_384:
				return "BLAKE2B_384";
			case BLAKE2B_512:
				return "BLAKE2B_512";
			case BLAKE2S_128:
				return "BLAKE2S_128";
			case BLAKE2S_160:
				return "BLAKE2S_160";
			case BLAKE2S_224:
				return "BLAKE2S_224";
			case BLAKE2S_256:
				return "BLAKE2S_256";
			case GOST3411_2012_256:
				return "GOST3411_2012_256";
			case GOST3411_2012_512:
				return "GOST3411_2012_512";
			case GOST3411:
				return "GOST3411";
			case KECCAK_224:
				return "KECCAK_224";
			case KECCAK_256:
				return "KECCAK_256";
			case KECCAK_288:
				return "KECCAK_288";
			case KECCAK_384:
				return "KECCAK_384";
			case KECCAK_512:
				return "KECCAK_512";
			case MD2:
				return "MD2";
			case MD4:
				return "MD4";
			case RIPEMD128:
				return "RIPEMD128";
			case RIPEMD160:
				return "RIPEMD160";
			case RIPEMD256:
				return "RIPEMD256";
			case RIPEMD320:
				return "RIPEMD320";
			case SHA3_224:
				return "SHA3_224";
			case SHA3_256:
				return "SHA3_256";
			case SHA3_384:
				return "SHA3_384";
			case SHA3_512:
				return "SHA3_512";
			case SM3:
				return "SM3";
			case TIGER:
				return "TIGER";
			case WHIRLPOOL:
				return "WHIRLPOOL";
			default:
				error.setError("HAA03", "Unrecognized HashAlgorihm");
				logger.error("Unrecognized HashAlgorihm");
				return "Unrecognized algorithm";
		}
	}

}

