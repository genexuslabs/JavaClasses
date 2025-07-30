package com.genexus.cryptography.symmetric.utils;

import com.genexus.securityapicommons.commons.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("LoggingSimilarMessage")
public enum SymmetricBlockAlgorithm {

	AES, BLOWFISH, CAMELLIA, CAST5, CAST6, DES, TRIPLEDES, DSTU7624_128, DSTU7624_256, DSTU7624_512, GOST28147, NOEKEON, RC2, RC532, RC564, RC6, RIJNDAEL_128, RIJNDAEL_160, RIJNDAEL_192, RIJNDAEL_224, RIJNDAEL_256, SEED, SERPENT, SKIPJACK, SM4, THREEFISH_256, THREEFISH_512, THREEFISH_1024, TWOFISH, XTEA, TEA, NONE;

	private static final Logger logger = LogManager.getLogger(SymmetricBlockAlgorithm.class);

	public static SymmetricBlockAlgorithm getSymmetricBlockAlgorithm(String symmetricBlockAlgorithm, Error error) {
		if (error == null) return SymmetricBlockAlgorithm.NONE;
		if (symmetricBlockAlgorithm == null) {
			error.setError("SBA05", "Unrecognized SymmetricBlockAlgorithm");
			logger.error("Unrecognized SymmetricBlockAlgorithm");
			return SymmetricBlockAlgorithm.NONE;
		}
		switch (symmetricBlockAlgorithm.toUpperCase().trim()) {
			case "AES":
				return SymmetricBlockAlgorithm.AES;
			case "BLOWFISH":
				return SymmetricBlockAlgorithm.BLOWFISH;
			case "CAMELLIA":
				return SymmetricBlockAlgorithm.CAMELLIA;
			case "CAST5":
				return SymmetricBlockAlgorithm.CAST5;
			case "CAST6":
				return SymmetricBlockAlgorithm.CAST6;
			case "DES":
				return SymmetricBlockAlgorithm.DES;
			case "TRIPLEDES":
				return SymmetricBlockAlgorithm.TRIPLEDES;
			case "DSTU7624_128":
				return SymmetricBlockAlgorithm.DSTU7624_128;
			case "DSTU7624_256":
				return SymmetricBlockAlgorithm.DSTU7624_256;
			case "DSTU7624_512":
				return SymmetricBlockAlgorithm.DSTU7624_512;
			case "GOST28147":
				return SymmetricBlockAlgorithm.GOST28147;
			case "NOEKEON":
				return SymmetricBlockAlgorithm.NOEKEON;
			case "RC2":
				return SymmetricBlockAlgorithm.RC2;
			case "RC6":
				return SymmetricBlockAlgorithm.RC6;
			case "RC532":
				return SymmetricBlockAlgorithm.RC532;
			case "RC564":
				return SymmetricBlockAlgorithm.RC564;
			case "RIJNDAEL_128":
				return SymmetricBlockAlgorithm.RIJNDAEL_128;
			case "RIJNDAEL_160":
				return SymmetricBlockAlgorithm.RIJNDAEL_160;
			case "RIJNDAEL_192":
				return SymmetricBlockAlgorithm.RIJNDAEL_192;
			case "RIJNDAEL_224":
				return SymmetricBlockAlgorithm.RIJNDAEL_224;
			case "RIJNDAEL_256":
				return SymmetricBlockAlgorithm.RIJNDAEL_256;
			case "SEED":
				return SymmetricBlockAlgorithm.SEED;
			case "SERPENT":
				return SymmetricBlockAlgorithm.SERPENT;
			case "SKIPJACK":
				return SymmetricBlockAlgorithm.SKIPJACK;
			case "SM4":
				return SymmetricBlockAlgorithm.SM4;
			case "THREEFISH_256":
				return SymmetricBlockAlgorithm.THREEFISH_256;
			case "THREEFISH_512":
				return SymmetricBlockAlgorithm.THREEFISH_512;
			case "THREEFISH_1024":
				return SymmetricBlockAlgorithm.THREEFISH_1024;
			case "TWOFISH":
				return SymmetricBlockAlgorithm.TWOFISH;
			case "XTEA":
				return SymmetricBlockAlgorithm.XTEA;
			case "TEA":
				return SymmetricBlockAlgorithm.TEA;
			default:
				error.setError("SBA01", "Unrecognized SymmetricBlockAlgorithm");
				logger.error("Unrecognized SymmetricBlockAlgorithm");
				return null;
		}

	}

	public static String valueOf(SymmetricBlockAlgorithm symmetricBlockAlgorithm, Error error) {
		if (error == null) return "SymmetricBlockAlgorithm";

		switch (symmetricBlockAlgorithm) {
			case AES:
				return "AES";
			case BLOWFISH:
				return "BLOWFISH";
			case CAMELLIA:
				return "CAMELLIA";
			case CAST5:
				return "CAST5";
			case CAST6:
				return "CAST6";
			case DES:
				return "DES";
			case TRIPLEDES:
				return "TRIPLEDES";
			case DSTU7624_128:
				return "DSTU7624_128";
			case DSTU7624_256:
				return "DSTU7624_256";
			case DSTU7624_512:
				return "DSTU7624_512";
			case GOST28147:
				return "GOST28147";
			case NOEKEON:
				return "NOEKEON";
			case RC2:
				return "RC2";
			case RC6:
				return "RC6";
			case RC532:
				return "RC532";
			case RC564:
				return "RC564";
			case RIJNDAEL_128:
				return "RIJNDAEL_128";
			case RIJNDAEL_160:
				return "RIJNDAEL_160";
			case RIJNDAEL_192:
				return "RIJNDAEL_192";
			case RIJNDAEL_224:
				return "RIJNDAEL_224";
			case RIJNDAEL_256:
				return "RIJNDAEL_256";
			case SEED:
				return "SEED";
			case SERPENT:
				return "SERPENT";
			case SKIPJACK:
				return "SKIPJACK";
			case SM4:
				return "SM4";
			case THREEFISH_256:
				return "THREEFISH_256";
			case THREEFISH_512:
				return "THREEFISH_512";
			case THREEFISH_1024:
				return "THREEFISH_1024";
			case TWOFISH:
				return "TWOFISH";
			case XTEA:
				return "XTEA";
			case TEA:
				return "TEA";
			default:
				error.setError("SBA02", "Unrecognized SymmetricBlockAlgorithm");
				logger.error("Unrecognized SymmetricBlockAlgorithm");
				return "SymmetricBlockAlgorithm";
		}
	}

	public static int getBlockSize(SymmetricBlockAlgorithm algorithm, Error error) {
		if (error == null) return 0;

		switch (algorithm) {

			case BLOWFISH:
			case CAST5:
			case DES:
			case GOST28147:
			case RC2:
			case RC532:
			case SKIPJACK:
			case XTEA:
			case TEA:
			case TRIPLEDES:
				return 64;
			case AES:
			case CAMELLIA:
			case CAST6:
			case NOEKEON:
			case RC564:
			case RC6:
			case SEED:
			case SERPENT:
			case SM4:
			case TWOFISH:
			case DSTU7624_128:
			case RIJNDAEL_128:
				return 128;
			case RIJNDAEL_160:
				return 160;
			case RIJNDAEL_192:
				return 192;
			case RIJNDAEL_224:
				return 224;
			case DSTU7624_256:
			case RIJNDAEL_256:
			case THREEFISH_256:
				return 256;
			case DSTU7624_512:
			case THREEFISH_512:
				return 512;
			case THREEFISH_1024:
				return 1024;
			default:
				error.setError("SBA03", "Unrecognized SymmetricBlockAlgorithm");
				logger.error("Unrecognized SymmetricBlockAlgorithm");
				return 0;
		}
	}

	protected static int[] getKeySize(SymmetricBlockAlgorithm algorithm, Error error) {
		int[] keySize = new int[3];

		switch (algorithm) {

			case BLOWFISH:
				keySize[0] = 0;
				keySize[1] = 448;
				break;
			case CAMELLIA:
			case SERPENT:
			case TWOFISH:
				keySize[0] = 128;
				keySize[1] = 192;
				keySize[2] = 256;
				break;
			case AES:
			case CAST6:
			case RC6:
			case RIJNDAEL_128:
			case RIJNDAEL_160:
			case RIJNDAEL_192:
			case RIJNDAEL_224:
			case RIJNDAEL_256:
				keySize[0] = 0;
				keySize[1] = 256;
				break;
			case DES:
				keySize[0] = 64;
				break;
			case TRIPLEDES:
				keySize[0] = 128;
				keySize[1] = 192;
				break;
			case DSTU7624_128:
			case DSTU7624_256:
			case DSTU7624_512:
				keySize[0] = 128;
				keySize[1] = 256;
				keySize[2] = 512;
				break;
			case GOST28147:
			case NOEKEON:
			case SEED:
			case SM4:
			case XTEA:
			case TEA:
				keySize[0] = 128;
				break;
			case RC2:
				keySize[0] = 0;
				keySize[1] = 1024;
				break;
			case RC532:
			case RC564:
			case SKIPJACK:
			case CAST5:
				keySize[0] = 0;
				keySize[1] = 128;
				break;
			case THREEFISH_256:
			case THREEFISH_512:
			case THREEFISH_1024:
				keySize[0] = 128;
				keySize[1] = 512;
				keySize[2] = 1024;
				break;
			default:
				error.setError("SBA04", "Unrecognized SymmetricBlockAlgorithm");
				logger.error("Unrecognized SymmetricBlockAlgorithm");
		}
		return keySize;
	}

}
