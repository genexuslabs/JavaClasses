package com.genexus.cryptography.symmetric;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.BlowfishEngine;
import org.bouncycastle.crypto.engines.CAST5Engine;
import org.bouncycastle.crypto.engines.CAST6Engine;
import org.bouncycastle.crypto.engines.CamelliaEngine;
import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.engines.DSTU7624Engine;
import org.bouncycastle.crypto.engines.GOST28147Engine;
import org.bouncycastle.crypto.engines.NoekeonEngine;
import org.bouncycastle.crypto.engines.RC2Engine;
import org.bouncycastle.crypto.engines.RC532Engine;
import org.bouncycastle.crypto.engines.RC564Engine;
import org.bouncycastle.crypto.engines.RC6Engine;
import org.bouncycastle.crypto.engines.RijndaelEngine;
import org.bouncycastle.crypto.engines.SEEDEngine;
import org.bouncycastle.crypto.engines.SM4Engine;
import org.bouncycastle.crypto.engines.SerpentEngine;
import org.bouncycastle.crypto.engines.SkipjackEngine;
import org.bouncycastle.crypto.engines.TEAEngine;
import org.bouncycastle.crypto.engines.ThreefishEngine;
import org.bouncycastle.crypto.engines.TwofishEngine;
import org.bouncycastle.crypto.engines.XTEAEngine;
import org.bouncycastle.crypto.modes.AEADBlockCipher;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.modes.CCMBlockCipher;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.modes.CTSBlockCipher;
import org.bouncycastle.crypto.modes.EAXBlockCipher;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.modes.GOFBBlockCipher;
import org.bouncycastle.crypto.modes.KCCMBlockCipher;
import org.bouncycastle.crypto.modes.OFBBlockCipher;
import org.bouncycastle.crypto.modes.OpenPGPCFBBlockCipher;
import org.bouncycastle.crypto.modes.SICBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.ISO10126d2Padding;
import org.bouncycastle.crypto.paddings.ISO7816d4Padding;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.paddings.X923Padding;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Base64;

import com.genexus.cryptography.commons.SymmetricBlockCipherObject;
import com.genexus.cryptography.symmetric.utils.SymmetricBlockAlgorithm;
import com.genexus.cryptography.symmetric.utils.SymmetricBlockMode;
import com.genexus.cryptography.symmetric.utils.SymmetricBlockPadding;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.utils.SecurityUtils;

/**
 * @author sgrampone
 *
 */
public class SymmetricBlockCipher extends SymmetricBlockCipherObject {

	/**
	 * SymmetricBlockCipher class constructor
	 */
	public SymmetricBlockCipher() {
		super();
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/

	/**
	 * @param symmetricBlockAlgorithm String SymmetricBlockAlgorithm enum, symmetric
	 *                                block algorithm name
	 * @param symmetricBlockMode      String SymmetricBlockModes enum, symmetric
	 *                                block mode name
	 * @param key                     String Hexa key for the algorithm excecution
	 * @param macSize                 int macSize in bits for MAC length for AEAD
	 *                                Encryption algorithm
	 * @param nonce                   String Hexa nonce for MAC length for AEAD
	 *                                Encryption algorithm
	 * @param plainText               String UTF-8 plain text to encrypt
	 * @return String Base64 encrypted text with the given algorithm and parameters
	 */
	public String doAEADEncrypt(String symmetricBlockAlgorithm, String symmetricBlockMode, String key, int macSize,
								String nonce, String plainText) {
		this.error.cleanError();

		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateStringInput("symmetricBlockAlgorithm", symmetricBlockAlgorithm, this.error);
		SecurityUtils.validateStringInput("symmetricBlockMode", symmetricBlockMode, this.error);
		SecurityUtils.validateStringInput("key", key, this.error);
		SecurityUtils.validateStringInput("nonce", nonce, this.error);
		SecurityUtils.validateStringInput("plainText", plainText, this.error);
		if (this.hasError()) {
			return "";
		}
		;
		/******* INPUT VERIFICATION - END *******/

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

	/**
	 * @param symmetricBlockAlgorithm String SymmetricBlockAlgorithm enum, symmetric
	 *                                block algorithm name
	 * @param symmetricBlockMode      String SymmetricBlockModes enum, symmetric
	 *                                block mode name
	 * @param key                     String Hexa key for the algorithm excecution
	 * @param macSize                 int macSize in bits for MAC length for AEAD
	 *                                Encryption algorithm
	 * @param nonce                   String Hexa nonce for MAC length for AEAD
	 *                                Encryption algorithm
	 * @param encryptedInput          String Base64 text to decrypt
	 * @return String plain text UTF-8 with the given algorithm and parameters
	 */
	public String doAEADDecrypt(String symmetricBlockAlgorithm, String symmetricBlockMode, String key, int macSize,
								String nonce, String encryptedInput) {
		this.error.cleanError();

		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateStringInput("symmetricBlockAlgorithm", symmetricBlockAlgorithm, this.error);
		SecurityUtils.validateStringInput("symmetricBlockMode", symmetricBlockMode, this.error);
		SecurityUtils.validateStringInput("key", key, this.error);
		SecurityUtils.validateStringInput("nonce", nonce, this.error);
		SecurityUtils.validateStringInput("encryptedInput", encryptedInput, this.error);
		if (this.hasError()) {
			return "";
		}
		;
		/******* INPUT VERIFICATION - END *******/

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

	/**
	 * @param symmetricBlockAlgorithm String SymmetricBlockAlgorithm enum, symmetric
	 *                                block algorithm name
	 * @param symmetricBlockMode      String SymmetricBlockModes enum, symmetric
	 *                                block mode name
	 * @param symmetricBlockPadding   String SymmetricBlockPadding enum, symmetric
	 *                                block padding name
	 * @param key                     String Hexa key for the algorithm excecution
	 * @param IV                      String IV for the algorithm execution, must be
	 *                                the same length as the blockSize
	 * @param plainText               String UTF-8 plain text to encrypt
	 * @return String Base64 encrypted text with the given algorithm and parameters
	 */
	public String doEncrypt(String symmetricBlockAlgorithm, String symmetricBlockMode, String symmetricBlockPadding,
							String key, String IV, String plainText) {
		this.error.cleanError();

		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateStringInput("symmetricBlockAlgorithm", symmetricBlockAlgorithm, this.error);
		SecurityUtils.validateStringInput("symmetricBlockMode", symmetricBlockMode, this.error);
		SecurityUtils.validateStringInput("symmetricBlockPadding", symmetricBlockPadding, this.error);
		SecurityUtils.validateStringInput("key", key, this.error);
		SecurityUtils.validateStringInput("IV", IV, this.error);
		SecurityUtils.validateStringInput("plainText", plainText, this.error);
		if (this.hasError()) {
			return "";
		}
		;
		/******* INPUT VERIFICATION - END *******/

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

	/**
	 * @param symmetricBlockAlgorithm String SymmetricBlockAlgorithm enum, symmetric
	 *                                block algorithm name
	 * @param symmetricBlockMode      String SymmetricBlockModes enum, symmetric
	 *                                block mode name
	 * @param symmetricBlockPadding   String SymmetricBlockPadding enum, symmetric
	 *                                block padding name
	 * @param key                     String Hexa key for the algorithm excecution
	 * @param IV                      String IV for the algorithm execution, must be
	 *                                the same length as the blockSize
	 * @param encryptedInput          String Base64 text to decrypt
	 * @return String plain text UTF-8 with the given algorithm and parameters
	 */
	public String doDecrypt(String symmetricBlockAlgorithm, String symmetricBlockMode, String symmetricBlockPadding,
							String key, String IV, String encryptedInput) {
		this.error.cleanError();

		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateStringInput("symmetricBlockAlgorithm", symmetricBlockAlgorithm, this.error);
		SecurityUtils.validateStringInput("symmetricBlockMode", symmetricBlockMode, this.error);
		SecurityUtils.validateStringInput("symmetricBlockPadding", symmetricBlockPadding, this.error);
		SecurityUtils.validateStringInput("key", key, this.error);
		SecurityUtils.validateStringInput("IV", IV, this.error);
		SecurityUtils.validateStringInput("encryptedInput", encryptedInput, this.error);
		if (this.hasError()) {
			return "";
		}
		;
		/******* INPUT VERIFICATION - END *******/

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
		this.error.cleanError();

		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateStringInput("symmetricBlockAlgorithm", symmetricBlockAlgorithm, this.error);
		SecurityUtils.validateStringInput("symmetricBlockMode", symmetricBlockMode, this.error);
		SecurityUtils.validateStringInput("key", key, this.error);
		SecurityUtils.validateStringInput("nonce", nonce, this.error);
		if (this.hasError()) {
			return false;
		}
		;
		/******* INPUT VERIFICATION - END *******/

		return setUpFile(symmetricBlockAlgorithm, symmetricBlockMode, null, nonce, key, pathInputFile, pathOutputFile,
			macSize, true, true);
	}

	public boolean doAEADDecryptFile(String symmetricBlockAlgorithm, String symmetricBlockMode, String key, int macSize,
									 String nonce, String pathInputFile, String pathOutputFile) {
		this.error.cleanError();

		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateStringInput("symmetricBlockAlgorithm", symmetricBlockAlgorithm, this.error);
		SecurityUtils.validateStringInput("symmetricBlockMode", symmetricBlockMode, this.error);
		SecurityUtils.validateStringInput("key", key, this.error);
		SecurityUtils.validateStringInput("nonce", nonce, this.error);
		if (this.hasError()) {
			return false;
		}
		;
		/******* INPUT VERIFICATION - END *******/

		return setUpFile(symmetricBlockAlgorithm, symmetricBlockMode, null, nonce, key, pathInputFile, pathOutputFile,
			macSize, false, true);
	}

	public boolean doEncryptFile(String symmetricBlockAlgorithm, String symmetricBlockMode,
								 String symmetricBlockPadding, String key, String IV, String pathInputFile, String pathOutputFile) {
		this.error.cleanError();

		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateStringInput("symmetricBlockAlgorithm", symmetricBlockAlgorithm, this.error);
		SecurityUtils.validateStringInput("symmetricBlockMode", symmetricBlockMode, this.error);
		SecurityUtils.validateStringInput("symmetricBlockPadding", symmetricBlockPadding, this.error);
		SecurityUtils.validateStringInput("key", key, this.error);
		SecurityUtils.validateStringInput("IV", IV, this.error);
		if (this.hasError()) {
			return false;
		}
		;
		/******* INPUT VERIFICATION - END *******/

		return setUpFile(symmetricBlockAlgorithm, symmetricBlockMode, symmetricBlockPadding, IV, key, pathInputFile,
			pathOutputFile, 0, true, false);
	}

	public boolean doDecryptFile(String symmetricBlockAlgorithm, String symmetricBlockMode,
								 String symmetricBlockPadding, String key, String IV, String pathInputFile, String pathOutputFile) {
		this.error.cleanError();
		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateStringInput("symmetricBlockAlgorithm", symmetricBlockAlgorithm, this.error);
		SecurityUtils.validateStringInput("symmetricBlockMode", symmetricBlockMode, this.error);
		SecurityUtils.validateStringInput("symmetricBlockPadding", symmetricBlockPadding, this.error);
		SecurityUtils.validateStringInput("key", key, this.error);
		SecurityUtils.validateStringInput("IV", IV, this.error);
		if (this.hasError()) {
			return false;
		}
		;
		/******* INPUT VERIFICATION - END *******/

		return setUpFile(symmetricBlockAlgorithm, symmetricBlockMode, symmetricBlockPadding, IV, key, pathInputFile,
			pathOutputFile, 0, false, false);
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	/**
	 * Gets the BufferedBlockCipher loaded with Padding, Mode and Engine to Encrypt
	 * with a Symmetric Block Algorithm
	 *
	 * @param algorithm SymmetricBlockAlgorithm enum, algorithm name
	 * @param mode      SymmetricBlockModes enum, mode name
	 * @param padding   SymmetricBlockPadding enum, padding name
	 * @return BufferedBlockCipher loaded with Padding, Mode and Engine to Encrypt
	 *         with a Symmetric Block Algorithm
	 */
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

	/**
	 * @param mode    SymmetricBlockModes enum, mode name
	 * @param padding SymmetricBlockPadding enum, padding name
	 * @return boolean true if it uses CTS
	 */
	private boolean usesCTS(SymmetricBlockMode mode, SymmetricBlockPadding padding) {
		return mode == SymmetricBlockMode.CTS || padding == SymmetricBlockPadding.WITHCTS;
	}

	/**
	 * @param algorithm SymmetricBlockAlgorithm enum, algorithm name
	 * @return BlockCipher with the algorithm Engine
	 */
	public BlockCipher getCipherEngine(SymmetricBlockAlgorithm algorithm) {

		BlockCipher engine = null;

		switch (algorithm) {
			case AES:
				engine = new AESEngine();
				break;
			case BLOWFISH:
				engine = new BlowfishEngine();
				break;
			case CAMELLIA:
				engine = new CamelliaEngine();
				break;
			case CAST5:
				engine = new CAST5Engine();
				break;
			case CAST6:
				engine = new CAST6Engine();
				break;
			case DES:
				engine = new DESEngine();
				break;
			case TRIPLEDES:
				engine = new DESedeEngine();
				break;
			case DSTU7624_128:
				engine = new DSTU7624Engine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.DSTU7624_128, this.error));
				break;
			case DSTU7624_256:
				engine = new DSTU7624Engine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.DSTU7624_256, this.error));
				break;
			case DSTU7624_512:
				engine = new DSTU7624Engine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.DSTU7624_512, this.error));
				break;
			case GOST28147:
				engine = new GOST28147Engine();
				break;
			case NOEKEON:
				engine = new NoekeonEngine();
				break;
			case RC2:
				engine = new RC2Engine();
				break;
			case RC532:
				engine = new RC532Engine();
				break;
			case RC564:
				engine = new RC564Engine();
				break;
			case RC6:
				engine = new RC6Engine();
				break;
			case RIJNDAEL_128:
				engine = new RijndaelEngine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.RIJNDAEL_128, this.error));
				break;
			case RIJNDAEL_160:
				engine = new RijndaelEngine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.RIJNDAEL_160, this.error));
				break;
			case RIJNDAEL_192:
				engine = new RijndaelEngine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.RIJNDAEL_192, this.error));
				break;
			case RIJNDAEL_224:
				engine = new RijndaelEngine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.RIJNDAEL_224, this.error));
				break;
			case RIJNDAEL_256:
				engine = new RijndaelEngine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.RIJNDAEL_256, this.error));
				break;
			case SEED:
				engine = new SEEDEngine();
				break;
			case SERPENT:
				engine = new SerpentEngine();
				break;
			case SKIPJACK:
				engine = new SkipjackEngine();
				break;
			case SM4:
				engine = new SM4Engine();
				break;
			case TEA:
				engine = new TEAEngine();
				break;
			case THREEFISH_256:
				engine = new ThreefishEngine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.THREEFISH_256, this.error));
				break;
			case THREEFISH_512:
				engine = new ThreefishEngine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.THREEFISH_512, this.error));
				break;
			case THREEFISH_1024:
				engine = new ThreefishEngine(
					SymmetricBlockAlgorithm.getBlockSize(SymmetricBlockAlgorithm.THREEFISH_1024, this.error));
				break;
			case TWOFISH:
				engine = new TwofishEngine();
				break;
			case XTEA:
				engine = new XTEAEngine();
				break;
			default:
				this.error.setError("SB003", "Unrecognized symmetric block algoritm");
				break;
		}
		return engine;

	}

	/**
	 * @param padding SymmetricBlockPadding enum, padding name
	 * @return BlockCipherPadding with loaded padding type, if padding is WITHCTS
	 *         returns null
	 */
	private BlockCipherPadding getPadding(SymmetricBlockPadding padding) {

		BlockCipherPadding paddingCipher = null;

		switch (padding) {
			case NOPADDING:
				paddingCipher = null;
				break;
			case ISO10126D2PADDING:
				paddingCipher = new ISO10126d2Padding();
				break;
			case PKCS7PADDING:
				paddingCipher = new PKCS7Padding();
				break;
			case WITHCTS:
				break;
			case X923PADDING:
				paddingCipher = new X923Padding();
			case ISO7816D4PADDING:
				paddingCipher = new ISO7816d4Padding();
				break;
			case ZEROBYTEPADDING:
				paddingCipher = new ZeroBytePadding();
				break;
			default:
				this.error.setError("SB004", "Unrecognized symmetric block padding.");
				break;
		}
		return paddingCipher;
	}

	/**
	 * @param blockCipher BlockCipher engine
	 * @param mode        SymmetricBlockModes enum, symmetric block mode name
	 * @return AEADBlockCipher loaded with a given BlockCipher
	 */
	private AEADBlockCipher getAEADCipherMode(BlockCipher blockCipher, SymmetricBlockMode mode) {

		AEADBlockCipher bc = null;

		switch (mode) {
			case AEAD_CCM:
				bc = new CCMBlockCipher(blockCipher);
				break;
			case AEAD_EAX:
				bc = new EAXBlockCipher(blockCipher);
				break;
			case AEAD_GCM:
				bc = new GCMBlockCipher(blockCipher);
				break;
			case AEAD_KCCM:
				bc = new KCCMBlockCipher(blockCipher);
				break;
			default:
				this.error.setError("SB005", "Unrecognized symmetric AEAD mode");
				break;
		}
		return bc;

	}

	/**
	 * @param blockCipher BlockCipher loaded with the algorithm Engine
	 * @param mode        SymmetricBlockModes enum, mode name
	 * @return BlockCipher with mode loaded
	 */
	private BlockCipher getCipherMode(BlockCipher blockCipher, SymmetricBlockMode mode) {

		BlockCipher bc = null;

		switch (mode) {
			case ECB:
			case NONE:
				bc = blockCipher;
				break;
			case CBC:
				bc = new CBCBlockCipher(blockCipher);
				break;
			case CFB:
				bc = new CFBBlockCipher(blockCipher, blockCipher.getBlockSize());
				break;
			case CTR:
				bc = new SICBlockCipher(blockCipher);
				break;
			case CTS:
				bc = new CBCBlockCipher(blockCipher);
				break;
			case GOFB:
				bc = new GOFBBlockCipher(blockCipher);
				break;
			case OFB:
				bc = new OFBBlockCipher(blockCipher, blockCipher.getBlockSize());
				break;
			case OPENPGPCFB:
				bc = new OpenPGPCFBBlockCipher(blockCipher);
				break;
			case SIC:
				blockCipher = new SICBlockCipher(blockCipher);
				break;

			default:
				this.error.setError("SB006", "Unrecognized symmetric block mode");
				break;

		}
		return bc;
	}

	private byte[] setUp(String symmetricBlockAlgorithm, String symmetricBlockMode, String symmetricBlockPadding,
						 String nonce, String key, byte[] input, int macSize, boolean toEncrypt, boolean isAEAD, boolean isFile,
						 String pathInput, String pathOutput) {
		SymmetricBlockAlgorithm algorithm = SymmetricBlockAlgorithm.getSymmetricBlockAlgorithm(symmetricBlockAlgorithm,
			this.error);
		SymmetricBlockMode mode = SymmetricBlockMode.getSymmetricBlockMode(symmetricBlockMode, this.error);
		SymmetricBlockPadding padding = null;
		if (!isAEAD) {
			padding = SymmetricBlockPadding.getSymmetricBlockPadding(symmetricBlockPadding, this.error);
		}

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
			return null;
		}

		byte[] outputBytes = null;
		if (isFile) {
			try {
				byte[] inBuffer = new byte[1024];
				byte[] outBuffer = new byte[bbc.getOutputSize(1024)];

				outBuffer = new byte[bbc.getUnderlyingCipher().getBlockSize() + bbc.getOutputSize(inBuffer.length)];
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
				return null;
			}
			outputBytes = new byte[1];
		} else {

			outputBytes = new byte[bbc.getOutputSize(txt.length)];
			try {

				int length = bbc.processBytes(txt, 0, txt.length, outputBytes, 0);
				bbc.doFinal(outputBytes, length);
			} catch (Exception e) {
				this.error.setError("SB008", e.getMessage());
				return null;
			}
		}
		return outputBytes;
	}

	private byte[] encrypt(SymmetricBlockAlgorithm algorithm, SymmetricBlockMode mode, SymmetricBlockPadding padding,
						   byte[] key, byte[] iv, byte[] input, boolean toEncrypt, boolean isFile, String pathInput,
						   String pathOutput) {
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
			return null;
		}

		byte[] outputBytes = null;
		if (isFile) {
			try {
				byte[] inBuffer = new byte[1024];
				byte[] outBuffer = new byte[bbc.getOutputSize(1024)];
				outBuffer = new byte[bbc.getBlockSize() + bbc.getOutputSize(inBuffer.length)];
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
				return null;
			}
			outputBytes = new byte[1];
		} else {
			outputBytes = new byte[bbc.getOutputSize(input.length)];
			try {
				int length = bbc.processBytes(input, 0, input.length, outputBytes, 0);
				bbc.doFinal(outputBytes, length);
			} catch (Exception e) {
				this.error.setError("SB010", e.getMessage());
				return null;
			}
		}
		return outputBytes;
	}

	private boolean setUpFile(String symmetricBlockAlgorithm, String symmetricBlockMode, String symmetricBlockPadding,
							  String nonce, String key, String pathInput, String pathOutput, int macSize, boolean toEncrypt,
							  boolean isAEAD) {
		/******* INPUT VERIFICATION - BEGIN *******/
		SecurityUtils.validateStringInput("pathInputFile", pathInput, this.error);
		SecurityUtils.validateStringInput("pathOutputFile", pathOutput, this.error);
		if (this.hasError()) {
			return false;
		}
		;
		/******* INPUT VERIFICATION - END *******/
		byte[] output = setUp(symmetricBlockAlgorithm, symmetricBlockMode, symmetricBlockPadding, nonce, key, null,
			macSize, toEncrypt, isAEAD, true, pathInput, pathOutput);
		return output == null ? false : true;
	}
}
