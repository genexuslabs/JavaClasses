package com.genexus.cryptography.passwordDerivation;

import com.genexus.cryptography.commons.PasswordDerivationObject;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.utils.SecurityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.generators.BCrypt;
import org.bouncycastle.crypto.generators.SCrypt;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Base64;

public class PasswordDerivation extends PasswordDerivationObject {

	private static final Logger logger = LogManager.getLogger(PasswordDerivation.class);

	public PasswordDerivation() {
		super();
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/

	public String doGenerateSCrypt(String password, String salt, int CPUCost, int blockSize, int parallelization,
								   int keyLenght) {
		this.error.cleanError();
		logger.debug("doGenerateSCrypt");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(PasswordDerivation.class), "doGenerateSCrypt", "password", password, this.error);
		SecurityUtils.validateStringInput(String.valueOf(PasswordDerivation.class), "doGenerateSCrypt", "salt", salt, this.error);
		if (keyLenght == 0) {
			this.error.setError("PD005", "Parameter keyLenght cannot be 0");
			logger.error("Parameter keyLenght cannot be 0");
		}
		if (this.hasError()) {
			return "";
		}
		;
		// INPUT VERIFICATION - END

		EncodingUtil eu = new EncodingUtil();
		byte[] bytePassword = eu.getBytes(password);
		if (eu.hasError()) {
			this.error = eu.getError();
		}

		byte[] byteSalt = SecurityUtils.hexaToByte(salt, this.error);
		if (this.hasError()) {
			return "";
		}

		try {
			return Strings.fromByteArray(Base64.encode(SCrypt.generate(bytePassword, byteSalt, CPUCost, blockSize, parallelization, keyLenght)));
		} catch (Exception e) {
			this.error.setError("PD001", e.getMessage());
			logger.error("doGenerateSCrypt", e);
			return "";
		}

	}

	public String doGenerateDefaultSCrypt(String password, String salt) {
		logger.debug("doGenerateDefaultSCrypt");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(PasswordDerivation.class), "doGenerateDefaultSCrypt", "password", password, this.error);
		SecurityUtils.validateStringInput(String.valueOf(PasswordDerivation.class), "doGenerateDefaultSCrypt", "salt", salt, this.error);
		if (this.hasError()) {
			return "";
		}
		;
		// INPUT VERIFICATION - END

		int N = 16384;
		int r = 8;
		int p = 1;
		int keyLenght = 256;
		return doGenerateSCrypt(password, salt, N, r, p, keyLenght);
	}

	public String doGenerateBcrypt(String password, String salt, int cost) {
		logger.debug("doGenerateBcrypt");
		this.error.cleanError();
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(PasswordDerivation.class), "doGenerateBcrypt", "password", password, this.error);
		SecurityUtils.validateStringInput(String.valueOf(PasswordDerivation.class), "doGenerateBcrypt", "salt", salt, this.error);
		if (this.hasError()) {
			return "";
		}
		;
		// INPUT VERIFICATION - END

		EncodingUtil eu = new EncodingUtil();
		byte[] bytePassword = eu.getBytes(password);
		if (eu.hasError()) {
			this.error = eu.getError();
		}

		byte[] byteSalt = SecurityUtils.hexaToByte(salt, this.error);
		if (this.hasError()) {
			return "";
		}

		try {
			return Strings.fromByteArray(Base64.encode(BCrypt.generate(bytePassword, byteSalt, cost)));
		} catch (Exception e) {
			this.error.setError("PD002", e.getMessage());
			logger.error("doGenerateBcrypt", e);
			return "";
		}

	}

	public String doGenerateDefaultBcrypt(String password, String salt) {
		this.error.cleanError();
		logger.debug("doGenerateDefaultBcrypt");

		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(PasswordDerivation.class), "doGenerateDefaultBcrypt", "password", password, this.error);
		SecurityUtils.validateStringInput(String.valueOf(PasswordDerivation.class), "doGenerateDefaultBcrypt", "salt", salt, this.error);
		if (this.hasError()) {
			return "";
		}
		;
		// INPUT VERIFICATION - END

		int cost = 6;
		return doGenerateBcrypt(password, salt, cost);
	}

	public String doGenerateArgon2(String argon2Version10, String argon2HashType, int iterations, int memory,
								   int parallelism, String password, String salt, int hashLength) {
		this.error.cleanError();
		logger.debug("doGenerateArgon2");

		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(PasswordDerivation.class), "doGenerateArgon2", "argon2Version10", argon2Version10, this.error);
		SecurityUtils.validateStringInput(String.valueOf(PasswordDerivation.class), "doGenerateArgon2", "argon2HashType", argon2HashType, this.error);
		SecurityUtils.validateStringInput(String.valueOf(PasswordDerivation.class), "doGenerateArgon2", "password", password, this.error);
		SecurityUtils.validateStringInput(String.valueOf(PasswordDerivation.class), "doGenerateArgon2", "salt", salt, this.error);
		if (hashLength == 0) {
			this.error.setError("PD006", "Parameter hashLength cannot be 0");
			logger.error("Parameter hashLength cannot be 0");
		}
		if (this.hasError()) {
			return "";
		}
		;
		// INPUT VERIFICATION - END

		Argon2Version ver_aux = Argon2Version.getArgon2Version(argon2Version10, this.error);
		int version = Argon2Version.getVersionParameter(ver_aux, this.error);
		Argon2HashType hash_aux = Argon2HashType.getArgon2HashType(argon2HashType, this.error);
		int hashType = Argon2HashType.getArgon2Parameter(hash_aux, this.error);
		EncodingUtil eu = new EncodingUtil();
		byte[] bytePass = eu.getBytes(password);
		if (eu.hasError()) {
			this.error = eu.getError();
		}
		byte[] byteSalt = SecurityUtils.hexaToByte(salt, this.error);
		if (this.hasError()) {
			return "";
		}

		Argon2Parameters.Builder builder = new Argon2Parameters.Builder(hashType).withVersion(version)
			.withIterations(iterations).withMemoryPowOfTwo(memory).withParallelism(parallelism)
			.withSalt(byteSalt);

		Argon2BytesGenerator dig = new Argon2BytesGenerator();
		byte[] res = new byte[hashLength];

		try {
			dig.init(builder.build());
			dig.generateBytes(bytePass, res);
		} catch (Exception e) {
			this.error.setError("PD003", e.getMessage());
			logger.error("doGenerateArgon2", e);
			return "";
		}

		return Strings.fromByteArray(Base64.encode(res));

	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/
}
