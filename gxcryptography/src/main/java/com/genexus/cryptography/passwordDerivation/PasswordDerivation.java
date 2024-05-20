package com.genexus.cryptography.passwordDerivation;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.generators.BCrypt;
import org.bouncycastle.crypto.generators.SCrypt;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Base64;

import com.genexus.cryptography.commons.PasswordDerivationObject;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.utils.SecurityUtils;

/**
 * @author sgrampone
 *
 */
public class PasswordDerivation extends PasswordDerivationObject {

	/**
	 * PasswordDerivation class constructor
	 */
	public PasswordDerivation() {
		super();
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/

	/**
	 * @param password
	 *            String UTF-8 to hash
	 * @param salt
	 *            String UTF-8 to use as salt
	 * @param CPUCost
	 *            CPUCost must be larger than 1, a power of 2 and less than 2^(128 *
	 *            blockSize / 8)
	 * @param blockSize
	 *            The blockSize must be >= 1
	 * @param parallelization
	 *            Parallelization must be a positive integer less than or equal to
	 *            Integer.MAX_VALUE / (128 * blockSize * 8)
	 * @param keyLenght
	 *            fixed key length
	 */
	public String doGenerateSCrypt(String password, String salt, int CPUCost, int blockSize, int parallelization,
								   int keyLenght) {
		this.error.cleanError();

		/*******INPUT VERIFICATION - BEGIN*******/
		SecurityUtils.validateStringInput("password", password, this.error);
		SecurityUtils.validateStringInput("salt", salt, this.error);
		if (keyLenght == 0) {this.error.setError("PD005", "Parameter keyLenght cannot be 0");}
		if(this.hasError()) { return "";};
		/*******INPUT VERIFICATION - END*******/

		EncodingUtil eu = new EncodingUtil();
		byte[] bytePassword = eu.getBytes(password);
		if(eu.hasError()) { this.error = eu.getError(); }

		byte[] byteSalt = SecurityUtils.hexaToByte(salt, this.error);
		if(this.hasError()) {return "";}

		byte[] encryptedBytes = null;
		try {
			encryptedBytes = SCrypt.generate(bytePassword, byteSalt, CPUCost, blockSize,
				parallelization, keyLenght);
		}catch(Exception e)
		{
			this.error.setError("PD001", e.getMessage());
			return "";
		}
		return Strings.fromByteArray(Base64.encode(encryptedBytes));

	}

	/**
	 *
	 * Calculates SCrypt digest with arbitrary fixed parameters: CPUCost (N) = 16384
	 * blockSize (r) = 8 parallelization (p) = 1 keyLenght = 256
	 *
	 * @param password
	 *            String UTF-8 to hash
	 * @param salt
	 *            String UTF-8 to use as salt
	 */
	public String doGenerateDefaultSCrypt(String password, String salt) {

		/*******INPUT VERIFICATION - BEGIN*******/
		SecurityUtils.validateStringInput("password", password, this.error);
		SecurityUtils.validateStringInput("salt", salt, this.error);
		if(this.hasError()) { return "";};
		/*******INPUT VERIFICATION - END*******/

		int N = 16384;
		int r = 8;
		int p = 1;
		int keyLenght = 256;
		return doGenerateSCrypt(password, salt, N, r, p, keyLenght);
	}

	/**
	 * @param password
	 *            String UTF-8 to hash. the password bytes (up to 72 bytes) to use
	 *            for this invocation.
	 * @param salt
	 *            String hexa to salt. The salt lenght must be 128 bits
	 * @param cost
	 *            The cost of the bcrypt function grows as 2^cost. Legal values are
	 *            4..31 inclusive.
	 * @return String Base64 hashed password to store
	 */
	public String doGenerateBcrypt(String password, String salt, int cost) {
		this.error.cleanError();

		/*******INPUT VERIFICATION - BEGIN*******/
		SecurityUtils.validateStringInput("password", password, this.error);
		SecurityUtils.validateStringInput("salt", salt, this.error);
		if(this.hasError()) { return "";};
		/*******INPUT VERIFICATION - END*******/

		EncodingUtil eu = new EncodingUtil();
		byte[] bytePassword = eu.getBytes(password);
		if(eu.hasError()) {this.error = eu.getError(); }

		byte[] byteSalt = SecurityUtils.hexaToByte(salt, this.error);
		if(this.hasError()) { return ""; }

		byte[] encryptedBytes = null;

		try {
			encryptedBytes = BCrypt.generate(bytePassword, byteSalt, cost);
		}catch(Exception e)
		{
			this.error.setError("PD002", e.getMessage());
			return "";
		}

		return Strings.fromByteArray(Base64.encode(encryptedBytes));

	}

	/**
	 * Calculates Bcrypt digest with arbitrary fixed cost parameter: cost = 6
	 *
	 * @param password
	 *            String UTF-8 to hash. the password bytes (up to 72 bytes) to use
	 *            for this invocation.
	 * @param salt
	 *            String UTF-8 to salt. The salt lenght must be 128 bits
	 * @return String Base64 hashed password to store
	 */
	public String doGenerateDefaultBcrypt(String password, String salt) {

		/*******INPUT VERIFICATION - BEGIN*******/
		SecurityUtils.validateStringInput("password", password, this.error);
		SecurityUtils.validateStringInput("salt", salt, this.error);
		if(this.hasError()) { return "";};
		/*******INPUT VERIFICATION - END*******/

		int cost = 6;
		return doGenerateBcrypt(password, salt, cost);
	}

	public String doGenerateArgon2(String argon2Version10, String argon2HashType, int iterations, int memory,
								   int parallelism, String password, String salt, int hashLength) {
		this.error.cleanError();

		/*******INPUT VERIFICATION - BEGIN*******/
		SecurityUtils.validateStringInput("argon2Version10", argon2Version10, this.error);
		SecurityUtils.validateStringInput("argon2HashType", argon2HashType, this.error);
		SecurityUtils.validateStringInput("password", password, this.error);
		SecurityUtils.validateStringInput("salt", salt, this.error);
		if(hashLength == 0) {this.error.setError("PD006", "Parameter hashLength cannot be 0");}
		if(this.hasError()) { return "";};
		/*******INPUT VERIFICATION - END*******/

		Argon2Version ver_aux = Argon2Version.getArgon2Version(argon2Version10, this.error);
		int version = Argon2Version.getVersionParameter(ver_aux, this.error);
		Argon2HashType hash_aux = Argon2HashType.getArgon2HashType(argon2HashType, this.error);
		int hashType = Argon2HashType.getArgon2Parameter(hash_aux, this.error);
		EncodingUtil eu = new EncodingUtil();
		byte[] bytePass = eu.getBytes(password);
		if(eu.hasError()) { this.error = eu.getError(); }
		byte[] byteSalt = SecurityUtils.hexaToByte(salt, this.error);
		if(this.hasError()) { return ""; }

		Argon2Parameters.Builder builder = new Argon2Parameters.Builder(hashType).withVersion(version)
			.withIterations(iterations).withMemoryPowOfTwo(memory).withParallelism(parallelism)
			.withSalt(byteSalt);

		Argon2BytesGenerator dig = new Argon2BytesGenerator();
		byte[] res = new byte[hashLength];

		try {
			dig.init(builder.build());
			dig.generateBytes(bytePass, res);
		}catch(Exception e)
		{
			this.error.setError("PD003", e.getMessage());
			return "";
		}

		return Strings.fromByteArray(Base64.encode(res));

	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/
}
