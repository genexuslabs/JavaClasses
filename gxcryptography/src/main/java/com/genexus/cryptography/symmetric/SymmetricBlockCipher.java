package com.genexus.cryptography.symmetric;

import com.genexus.cryptography.commons.SymmetricBlockCipherObject;
import com.genexus.cryptography.symmetric.utils.SymmetricBlockAlgorithm;
import com.genexus.cryptography.symmetric.utils.SymmetricBlockMode;
import com.genexus.cryptography.symmetric.utils.SymmetricBlockPadding;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.utils.SecurityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.*;
import org.bouncycastle.crypto.modes.*;
import org.bouncycastle.crypto.paddings.*;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Base64;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class SymmetricBlockCipher extends SymmetricBlockCipherObject {

	private static final Logger logger = LogManager.getLogger(SymmetricBlockCipher.class);

	public SymmetricBlockCipher() {
		super();
	}

	public String doAEADEncrypt(String symmetricBlockAlgorithm, String symmetricBlockMode, String key, int macSize,
								String nonce, String plainText) {
		logger.debug("doAEADEncrypt");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doAEADEncrypt", "symmetricBlockAlgorithm", symmetricBlockAlgorithm, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doAEADEncrypt", "symmetricBlockMode", symmetricBlockMode, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doAEADEncrypt", "key", key, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doAEADEncrypt", "nonce", nonce, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doAEADEncrypt", "plainText", plainText, this.error);
		if (this.hasError()) {
			return "";
		}
		;
		// INPUT VERIFICATION - END

		EncodingUtil eu = new EncodingUtil();
		byte[] txtBytes = eu.getBytes(plainText);
		if (eu.hasError()) {
			this.error = eu.getError();
		}
		if (this.hasError()) {
			return "";
		}

		byte[] encryptedBytes = setUp(symmetricBlockAlgorithm, symmetricBlockMode, null, nonce, key, txtBytes, macSize,
			true, true, false, null, null);
		if (this.hasError()) {
			return "";
		}

		return Strings.fromByteArray(Base64.encode(encryptedBytes)).trim();

	}

	public String doAEADDecrypt(String symmetricBlockAlgorithm, String symmetricBlockMode, String key, int macSize,
								String nonce, String encryptedInput) {
		logger.debug("doAEADDecrypt");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doAEADDecrypt", "symmetricBlockAlgorithm", symmetricBlockAlgorithm, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doAEADDecrypt", "symmetricBlockMode", symmetricBlockMode, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doAEADDecrypt", "key", key, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doAEADDecrypt", "nonce", nonce, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doAEADDecrypt", "encryptedInput", encryptedInput, this.error);
		if (this.hasError()) {
			return "";
		}
		;
		// INPUT VERIFICATION - END

		byte[] input = null;
		try {
			input = Base64.decode(encryptedInput);
		} catch (Exception e) {
			this.error.setError("SB001", e.getMessage());
			return "";
		}

		byte[] decryptedBytes = setUp(symmetricBlockAlgorithm, symmetricBlockMode, null, nonce, key, input, macSize,
			false, true, false, null, null);
		if (this.hasError()) {
			return "";
		}

		EncodingUtil eu = new EncodingUtil();
		String result = eu.getString(decryptedBytes);
		if (eu.hasError()) {
			this.error = eu.getError();
			return "";
		}
		return result.trim();
	}

	public String doEncrypt(String symmetricBlockAlgorithm, String symmetricBlockMode, String symmetricBlockPadding,
							String key, String IV, String plainText) {
		logger.debug("doEncrypt");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doEncrypt", "symmetricBlockAlgorithm", symmetricBlockAlgorithm, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doEncrypt", "symmetricBlockMode", symmetricBlockMode, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doEncrypt", "symmetricBlockPadding", symmetricBlockPadding, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doEncrypt", "key", key, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doEncrypt", "IV", IV, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doEncrypt", "plainText", plainText, this.error);
		if (this.hasError()) {
			return "";
		}
		;
		// INPUT VERIFICATION - END

		EncodingUtil eu = new EncodingUtil();
		byte[] inputBytes = eu.getBytes(plainText);
		if (eu.hasError()) {
			this.error = eu.getError();
			return "";
		}

		byte[] encryptedBytes = setUp(symmetricBlockAlgorithm, symmetricBlockMode, symmetricBlockPadding, IV, key,
			inputBytes, 0, true, false, false, null, null);
		if (this.hasError()) {
			return "";
		}

		return Strings.fromByteArray(Base64.encode(encryptedBytes)).trim();
	}

	public String doDecrypt(String symmetricBlockAlgorithm, String symmetricBlockMode, String symmetricBlockPadding,
							String key, String IV, String encryptedInput) {
		logger.debug("doDecrypt");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doDecrypt", "symmetricBlockAlgorithm", symmetricBlockAlgorithm, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doDecrypt", "symmetricBlockMode", symmetricBlockMode, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doDecrypt", "symmetricBlockPadding", symmetricBlockPadding, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doDecrypt", "key", key, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doDecrypt", "IV", IV, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doDecrypt", "encryptedInput", encryptedInput, this.error);
		if (this.hasError()) {
			return "";
		}
		;
		// INPUT VERIFICATION - END

		byte[] input = null;
		try {
			input = Base64.decode(encryptedInput);
		} catch (Exception e) {
			this.error.setError("SB002", e.getMessage());
			return "";
		}

		byte[] decryptedBytes = setUp(symmetricBlockAlgorithm, symmetricBlockMode, symmetricBlockPadding, IV, key,
			input, 0, false, false, false, null, null);
		if (this.hasError()) {
			return "";
		}

		EncodingUtil eu = new EncodingUtil();
		String result = eu.getString(decryptedBytes);
		if (eu.hasError()) {
			this.error = eu.getError();
			return "";
		}
		return result.trim();

	}

	public boolean doAEADEncryptFile(String symmetricBlockAlgorithm, String symmetricBlockMode, String key, int macSize,
									 String nonce, String pathInputFile, String pathOutputFile) {
		logger.debug("doAEADEncryptFile");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doAEADEncryptFile", "symmetricBlockAlgorithm", symmetricBlockAlgorithm, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doAEADEncryptFile", "symmetricBlockMode", symmetricBlockMode, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doAEADEncryptFile", "key", key, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doAEADEncryptFile", "nonce", nonce, this.error);
		if (this.hasError()) {
			return false;
		}
		;
		// INPUT VERIFICATION - END

		return setUpFile(symmetricBlockAlgorithm, symmetricBlockMode, null, nonce, key, pathInputFile, pathOutputFile,
			macSize, true, true);
	}

	public boolean doAEADDecryptFile(String symmetricBlockAlgorithm, String symmetricBlockMode, String key, int macSize,
									 String nonce, String pathInputFile, String pathOutputFile) {
		logger.debug("doAEADDecryptFile");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doAEADDecryptFile", "symmetricBlockAlgorithm", symmetricBlockAlgorithm, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doAEADDecryptFile", "symmetricBlockMode", symmetricBlockMode, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doAEADDecryptFile", "key", key, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doAEADDecryptFile", "nonce", nonce, this.error);
		if (this.hasError()) {
			return false;
		}
		;
		// INPUT VERIFICATION - END

		return setUpFile(symmetricBlockAlgorithm, symmetricBlockMode, null, nonce, key, pathInputFile, pathOutputFile,
			macSize, false, true);
	}

	public boolean doEncryptFile(String symmetricBlockAlgorithm, String symmetricBlockMode,
								 String symmetricBlockPadding, String key, String IV, String pathInputFile, String pathOutputFile) {
		logger.debug("doEncryptFile");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doEncryptFile", "symmetricBlockAlgorithm", symmetricBlockAlgorithm, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doEncryptFile", "symmetricBlockMode", symmetricBlockMode, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doEncryptFile", "symmetricBlockPadding", symmetricBlockPadding, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doEncryptFile", "key", key, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doEncryptFile", "IV", IV, this.error);
		if (this.hasError()) {
			return false;
		}
		;
		// INPUT VERIFICATION - END

		return setUpFile(symmetricBlockAlgorithm, symmetricBlockMode, symmetricBlockPadding, IV, key, pathInputFile,
			pathOutputFile, 0, true, false);
	}

	public boolean doDecryptFile(String symmetricBlockAlgorithm, String symmetricBlockMode,
								 String symmetricBlockPadding, String key, String IV, String pathInputFile, String pathOutputFile) {
		logger.debug("doDecryptFile");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doDecryptFile", "symmetricBlockAlgorithm", symmetricBlockAlgorithm, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doDecryptFile", "symmetricBlockMode", symmetricBlockMode, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doDecryptFile", "symmetricBlockPadding", symmetricBlockPadding, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doDecryptFile", "key", key, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "doDecryptFile", "IV", IV, this.error);
		if (this.hasError()) {
			return false;
		}
		;
		// INPUT VERIFICATION - END

		return setUpFile(symmetricBlockAlgorithm, symmetricBlockMode, symmetricBlockPadding, IV, key, pathInputFile,
			pathOutputFile, 0, false, false);
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	private BufferedBlockCipher getCipher(SymmetricBlockAlgorithm algorithm, SymmetricBlockMode mode,
										  SymmetricBlockPadding padding) {
		BlockCipher engine = getCipherEngine(algorithm);
		BlockCipherPadding paddingCipher = getPadding(padding);
		BlockCipher bc;
		if (mode != SymmetricBlockMode.ECB) {
			bc = getCipherMode(engine, mode);
		} else {
			bc = engine;
		}
		// si el padding es WITHCTS el paddingCipher es null
		if (usesCTS(mode, padding)) {
			return new CTSBlockCipher(bc); // no usa el paddingCipher que es el null
		}
		if (padding == SymmetricBlockPadding.NOPADDING) {
			return new BufferedBlockCipher(bc);
		} else {
			return new PaddedBufferedBlockCipher(bc, paddingCipher);
		}

	}

	private boolean usesCTS(SymmetricBlockMode mode, SymmetricBlockPadding padding) {
		return mode == SymmetricBlockMode.CTS || padding == SymmetricBlockPadding.WITHCTS;
	}

	public BlockCipher getCipherEngine(SymmetricBlockAlgorithm algorithm) {
		logger.debug("getCipherEngine");
		switch (algorithm) {
			case AES:
				return new AESEngine();
			case BLOWFISH:
				return new BlowfishEngine();
			case CAMELLIA:
				return new CamelliaEngine();
			case CAST5:
				return new CAST5Engine();
			case CAST6:
				return new CAST6Engine();
			case DES:
				return new DESEngine();
			case TRIPLEDES:
				return new DESedeEngine();
			case DSTU7624_128:
				return new DSTU7624Engine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.DSTU7624_128, this.error));
			case DSTU7624_256:
				return new DSTU7624Engine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.DSTU7624_256, this.error));
			case DSTU7624_512:
				return new DSTU7624Engine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.DSTU7624_512, this.error));
			case GOST28147:
				return new GOST28147Engine();
			case NOEKEON:
				return new NoekeonEngine();
			case RC2:
				return new RC2Engine();
			case RC532:
				return new RC532Engine();
			case RC564:
				return new RC564Engine();
			case RC6:
				return new RC6Engine();
			case RIJNDAEL_128:
				return new RijndaelEngine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.RIJNDAEL_128, this.error));
			case RIJNDAEL_160:
				return new RijndaelEngine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.RIJNDAEL_160, this.error));
			case RIJNDAEL_192:
				return new RijndaelEngine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.RIJNDAEL_192, this.error));
			case RIJNDAEL_224:
				return new RijndaelEngine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.RIJNDAEL_224, this.error));
			case RIJNDAEL_256:
				return new RijndaelEngine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.RIJNDAEL_256, this.error));
			case SEED:
				return new SEEDEngine();
			case SERPENT:
				return new SerpentEngine();
			case SKIPJACK:
				return new SkipjackEngine();
			case SM4:
				return new SM4Engine();
			case TEA:
				return new TEAEngine();
			case THREEFISH_256:
				return new ThreefishEngine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.THREEFISH_256, this.error));
			case THREEFISH_512:
				return new ThreefishEngine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.THREEFISH_512, this.error));
			case THREEFISH_1024:
				return new ThreefishEngine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.THREEFISH_1024, this.error));
			case TWOFISH:
				return new TwofishEngine();
			case XTEA:
				return new XTEAEngine();
			default:
				this.error.setError("SB003", "Unrecognized symmetric block algoritm");
				logger.error("Unrecognized symmetric block algoritm");
				return null;
		}
	}

	private BlockCipherPadding getPadding(SymmetricBlockPadding padding) {
		logger.debug("getPadding");
		switch (padding) {
			case ISO10126D2PADDING:
				return new ISO10126d2Padding();
			case PKCS7PADDING:
				return new PKCS7Padding();
			case X923PADDING:
				return new X923Padding();
			case ISO7816D4PADDING:
				return new ISO7816d4Padding();
			case ZEROBYTEPADDING:
				return new ZeroBytePadding();
			case WITHCTS:
			case NOPADDING:
			default:
				this.error.setError("SB004", "Unrecognized symmetric block padding.");
				logger.error("Unrecognized symmetric block padding.");
				return null;
		}
	}

	private AEADBlockCipher getAEADCipherMode(BlockCipher blockCipher, SymmetricBlockMode mode) {
		logger.debug("getAEADCipherMode");
		switch (mode) {
			case AEAD_CCM:
				return new CCMBlockCipher(blockCipher);
			case AEAD_EAX:
				return new EAXBlockCipher(blockCipher);
			case AEAD_GCM:
				return new GCMBlockCipher(blockCipher);
			case AEAD_KCCM:
				return new KCCMBlockCipher(blockCipher);
			default:
				this.error.setError("SB005", "Unrecognized symmetric AEAD mode");
				logger.error("Unrecognized symmetric AEAD mode");
				return null;
		}
	}

	private BlockCipher getCipherMode(BlockCipher blockCipher, SymmetricBlockMode mode) {
		logger.debug("getCipherMode");
		switch (mode) {
			case ECB:
			case NONE:
				return blockCipher;
			case CBC:
			case CTS:
				return new CBCBlockCipher(blockCipher);
			case CFB:
				return new CFBBlockCipher(blockCipher, blockCipher.getBlockSize());
			case CTR:
			case SIC:
				return new SICBlockCipher(blockCipher);
			case GOFB:
				return new GOFBBlockCipher(blockCipher);
			case OFB:
				return new OFBBlockCipher(blockCipher, blockCipher.getBlockSize());
			case OPENPGPCFB:
				return new OpenPGPCFBBlockCipher(blockCipher);
			default:
				this.error.setError("SB006", "Unrecognized symmetric block mode");
				logger.error("Unrecognized symmetric block mode");
				return blockCipher;
		}
	}

	private byte[] setUp(String symmetricBlockAlgorithm, String symmetricBlockMode, String symmetricBlockPadding,
						 String nonce, String key, byte[] input, int macSize, boolean toEncrypt, boolean isAEAD, boolean isFile,
						 String pathInput, String pathOutput) {
		SymmetricBlockAlgorithm algorithm = SymmetricBlockAlgorithm.getSymmetricBlockAlgorithm(symmetricBlockAlgorithm,
			this.error);
		SymmetricBlockMode mode = SymmetricBlockMode.getSymmetricBlockMode(symmetricBlockMode, this.error);

		SymmetricBlockPadding padding = !isAEAD ? SymmetricBlockPadding.getSymmetricBlockPadding(symmetricBlockPadding, this.error) : null;

		byte[] nonceBytes = SecurityUtils.hexaToByte(nonce, this.error);
		byte[] keyBytes = SecurityUtils.hexaToByte(key, this.error);

		if (this.hasError()) {
			return null;
		}

		return isAEAD
			? encryptAEAD(algorithm, mode, keyBytes, nonceBytes, input, macSize, toEncrypt, isFile, pathInput,
			pathOutput)
			: encrypt(algorithm, mode, padding, keyBytes, nonceBytes, input, toEncrypt, isFile, pathInput,
			pathOutput);

	}

	private byte[] encryptAEAD(SymmetricBlockAlgorithm algorithm, SymmetricBlockMode mode, byte[] key, byte[] nonce,
							   byte[] txt, int macSize, boolean toEncrypt, boolean isFile, String pathInput, String pathOutput) {
		logger.debug("encryptAEAD");
		BlockCipher engine = getCipherEngine(algorithm);
		AEADBlockCipher bbc = getAEADCipherMode(engine, mode);
		if (this.hasError()) {
			return null;
		}

		KeyParameter keyParam = new KeyParameter(key);
		AEADParameters AEADparams = new AEADParameters(keyParam, macSize, nonce);

		try {
			bbc.init(toEncrypt, AEADparams);
		} catch (Exception e) {
			this.error.setError("SB007", e.getMessage());
			logger.error("encryptAEAD", e);
			return null;
		}

		if (isFile) {
			try {
				byte[] inBuffer = new byte[1024];
				byte[] outBuffer = new byte[bbc.getUnderlyingCipher().getBlockSize() + bbc.getOutputSize(inBuffer.length)];
				int inCount = 0;
				int outCount = 0;
				try (FileInputStream inputStream = new FileInputStream(pathInput)) {
					try (FileOutputStream outputStream = new FileOutputStream(pathOutput)) {
						while ((inCount = inputStream.read(inBuffer, 0, inBuffer.length)) > 0) {
							outCount = bbc.processBytes(inBuffer, 0, inCount, outBuffer, 0);
							outputStream.write(outBuffer, 0, outCount);
						}
						outCount = bbc.doFinal(outBuffer, 0);

						outputStream.write(outBuffer, 0, outCount);
					}
				}
			} catch (Exception e) {
				this.error.setError("SB012", e.getMessage());
				logger.error("encryptAEAD", e);
				return null;
			}
			return new byte[1];
		} else {

			byte[] outputBytes = new byte[bbc.getOutputSize(txt.length)];
			try {

				int length = bbc.processBytes(txt, 0, txt.length, outputBytes, 0);
				bbc.doFinal(outputBytes, length);
				return outputBytes;
			} catch (Exception e) {
				this.error.setError("SB008", e.getMessage());
				logger.error("encryptAEAD", e);
				return null;
			}
		}
	}

	private byte[] encrypt(SymmetricBlockAlgorithm algorithm, SymmetricBlockMode mode, SymmetricBlockPadding padding,
						   byte[] key, byte[] iv, byte[] input, boolean toEncrypt, boolean isFile, String pathInput,
						   String pathOutput) {
		logger.debug("encrypt");
		if (padding == null) {
			return null;
		}

		BufferedBlockCipher bbc = getCipher(algorithm, mode, padding);
		KeyParameter keyParam = new KeyParameter(key);
		if (this.hasError()) {
			return null;
		}

		try {
			if (SymmetricBlockMode.ECB != mode && SymmetricBlockMode.OPENPGPCFB != mode) {
				ParametersWithIV keyParamWithIV = new ParametersWithIV(keyParam, iv);
				bbc.init(toEncrypt, keyParamWithIV);
			} else {
				bbc.init(toEncrypt, keyParam);
			}
		} catch (Exception e) {
			this.error.setError("SB009", e.getMessage());
			logger.error("encrypt", e);
			return null;
		}

		if (isFile) {
			try {
				byte[] inBuffer = new byte[1024];
				byte[] outBuffer = new byte[bbc.getBlockSize() + bbc.getOutputSize(inBuffer.length)];
				int inCount = 0;
				int outCount = 0;
				try (FileInputStream inputStream = new FileInputStream(pathInput)) {
					try (FileOutputStream outputStream = new FileOutputStream(pathOutput)) {
						while ((inCount = inputStream.read(inBuffer, 0, inBuffer.length)) > 0) {
							outCount = bbc.processBytes(inBuffer, 0, inCount, outBuffer, 0);
							outputStream.write(outBuffer, 0, outCount);
						}
						outCount = bbc.doFinal(outBuffer, 0);

						outputStream.write(outBuffer, 0, outCount);
					}
				}
			} catch (Exception e) {
				this.error.setError("SB012", e.getMessage());
				logger.error("encrypt", e);
				return null;
			}
			return new byte[1];
		} else {
			byte[] outputBytes = new byte[bbc.getOutputSize(input.length)];
			try {
				int length = bbc.processBytes(input, 0, input.length, outputBytes, 0);
				bbc.doFinal(outputBytes, length);
				return outputBytes;
			} catch (Exception e) {
				this.error.setError("SB010", e.getMessage());
				logger.error("encrypt", e);
				return null;
			}
		}
	}

	private boolean setUpFile(String symmetricBlockAlgorithm, String symmetricBlockMode, String symmetricBlockPadding,
							  String nonce, String key, String pathInput, String pathOutput, int macSize, boolean toEncrypt,
							  boolean isAEAD) {
		logger.debug("setUpFile");

		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "setUpFile", "pathInputFile", pathInput, this.error);
		SecurityUtils.validateStringInput(String.valueOf(SymmetricBlockCipher.class), "setUpFile", "pathOutputFile", pathOutput, this.error);
		if (this.hasError()) {
			return false;
		}
		;
		// INPUT VERIFICATION - END

		byte[] output = setUp(symmetricBlockAlgorithm, symmetricBlockMode, symmetricBlockPadding, nonce, key, null,
			macSize, toEncrypt, isAEAD, true, pathInput, pathOutput);
		return output != null;
	}
}
