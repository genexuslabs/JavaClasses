package com.genexus.totp;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@SuppressWarnings("unused")
public class TOTPAuthenticator {

	private static final Logger logger = LogManager.getLogger(TOTPAuthenticator.class);

	public static String GenerateKey(int keyLength) {
		logger.debug("GenerateKey");
		SecretGenerator secretGenerator = new DefaultSecretGenerator(keyLength * 8);
		String str = secretGenerator.generate();
		try {
			return str.substring(0, keyLength);
		} catch (Exception e) {
			logger.error("GenerateKey", e);
			return str;
		}
	}

	public static String GenerateQRData(String accountName, String secretKey, String appName, String algorithm, int digits, int period) {
		logger.debug("GenerateQRData");
		HashingAlgorithm hashAlg;
		if (algorithm.equalsIgnoreCase("SHA512")) {
			hashAlg = HashingAlgorithm.SHA512;
		} else if (algorithm.equalsIgnoreCase("SHA256")) {
			hashAlg = HashingAlgorithm.SHA256;
		} else {
			hashAlg = HashingAlgorithm.SHA1;
		}

		QrData data = new QrData.Builder()
			.label(accountName)
			.secret(secretKey)
			.issuer(appName)
			.algorithm(hashAlg)
			.digits(digits)
			.period(period)
			.build();


		QrGenerator generator = new ZxingPngQrGenerator();
		try {
			return getDataUriForImage(generator.generate(data), generator.getImageMimeType());
		} catch (Exception e) {
			logger.error("GenerateQRData", e);
			return null;
		}
	}

	public static boolean VerifyTOTPCode(String secretKey, String code, String algorithm, int digits, int period) {
		logger.debug("VerifyTOTPCode");
		HashingAlgorithm hashAlg;
		if (algorithm.equalsIgnoreCase("SHA512")) {
			hashAlg = HashingAlgorithm.SHA512;
		} else if (algorithm.equalsIgnoreCase("SHA256")) {
			hashAlg = HashingAlgorithm.SHA256;
		} else {
			hashAlg = HashingAlgorithm.SHA1;
		}

		TimeProvider timeProvider = new SystemTimeProvider();

		CodeGenerator codeGenerator = new DefaultCodeGenerator(hashAlg, digits);
		DefaultCodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

		// sets the time period for codes to be valid for to X seconds
		verifier.setTimePeriod(period);

		// allow codes valid for 1 time periods before/after to pass as valid
		verifier.setAllowedTimePeriodDiscrepancy(0);

		// secret = the shared secret for the user
		// code = the code submitted by the user
		return verifier.isValidCode(secretKey, code);
	}
}


