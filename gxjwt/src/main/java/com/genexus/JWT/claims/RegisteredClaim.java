package com.genexus.JWT.claims;

import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.interfaces.Verification;
import com.genexus.securityapicommons.commons.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@SuppressWarnings("LoggingSimilarMessage")
public enum RegisteredClaim {
	iss, exp, sub, aud, nbf, iat, jti,
	;

	private static final Logger logger = LogManager.getLogger(RegisteredClaim.class);

	@SuppressWarnings("unused")
	public static String valueOf(RegisteredClaim registeredClaim, Error error) {
		switch (registeredClaim) {
			case iss:
				return "iss";
			case exp:
				return "exp";
			case sub:
				return "sub";
			case aud:
				return "aud";
			case nbf:
				return "nbf";
			case iat:
				return "iat";
			case jti:
				return "jti";
			default:
				error.setError("RCL01", "Unknown registered Claim");
				logger.error("Unknown registered Claim");
				return "Unknown registered claim";

		}
	}

	public static RegisteredClaim getRegisteredClaim(String registeredClaim, Error error) {
		switch (registeredClaim.trim()) {
			case "iss":
				return RegisteredClaim.iss;
			case "exp":
				return RegisteredClaim.exp;
			case "sub":
				return RegisteredClaim.sub;
			case "aud":
				return RegisteredClaim.aud;
			case "nbf":
				return RegisteredClaim.nbf;
			case "iat":
				return RegisteredClaim.iat;
			case "jti":
				return RegisteredClaim.jti;
			default:
				error.setError("RCL02", "Unknown registered Claim");
				logger.error("Unknown registered Claim");
				return null;
		}
	}

	public static boolean exists(String value) {
		switch (value.trim()) {
			case "iss":
			case "exp":
			case "sub":
			case "aud":
			case "nbf":
			case "iat":
			case "jti":
				return true;
			default:
				return false;
		}
	}

	public static boolean isTimeValidatingClaim(String claimKey) {
		switch (claimKey.trim()) {
			case "iat":
			case "exp":
			case "nbf":
				return true;
			default:
				return false;
		}
	}

	public static Verification getVerificationWithClaim(String registeredClaimKey, String registeredClaimValue,
														long registeredClaimCustomTime, Verification verification, Error error) {
		RegisteredClaim regClaim = getRegisteredClaim(registeredClaimKey, error);
		if (error.existsError()) {
			return null;
		} else {
			return getVerificationWithClaim(regClaim, registeredClaimValue, registeredClaimCustomTime, verification,
				error);
		}
	}

	public static Verification getVerificationWithClaim(RegisteredClaim registeredClaimKey, String registeredClaimValue,
														long registeredClaimCustomTime, Verification verification, Error error) {

		switch (registeredClaimKey) {
			case iss:
				verification.withIssuer(registeredClaimValue);
				break;
			case exp:
				if (registeredClaimCustomTime != 0) {
					verification.acceptExpiresAt(registeredClaimCustomTime);
				}
				break;
			case sub:
				verification.withSubject(registeredClaimValue);
				break;
			case aud:
				verification.withAudience(registeredClaimValue);
				break;
			case nbf:
				if (registeredClaimCustomTime != 0) {
					verification.acceptNotBefore(registeredClaimCustomTime);
				}
				break;
			case iat:
				if (registeredClaimCustomTime != 0) {
					verification.acceptIssuedAt(registeredClaimCustomTime);
				}
				break;
			case jti:
				verification.withJWTId(registeredClaimValue);
				break;
			default:
				error.setError("RCL03", "Unknown registered claim");
				return null;
		}
		return verification;
	}

	public static void getBuilderWithClaim(String registeredClaimKey, String registeredClaimValue,
										   Builder tokenBuilder, Error error) {
		RegisteredClaim regClaim = getRegisteredClaim(registeredClaimKey, error);
		getBuilderWithClaim(regClaim, registeredClaimValue, tokenBuilder, error);
	}

	private static boolean isANumber(String value) {
		return value.matches("-?\\d+");
	}

	public static void getBuilderWithClaim(RegisteredClaim registeredClaimKey, String registeredClaimValue,
										   Builder tokenBuilder, Error error) {
		logger.debug("getBuilderWithClaim");

		try {
			switch (registeredClaimKey) {
				case iss:
					tokenBuilder.withIssuer(registeredClaimValue);
					break;
				case exp:
					tokenBuilder.withExpiresAt(solveDate(registeredClaimValue));
					break;
				case sub:
					tokenBuilder.withSubject(registeredClaimValue);
					break;
				case aud:
					tokenBuilder.withAudience(registeredClaimValue);
					break;
				case nbf:
					tokenBuilder.withNotBefore(solveDate(registeredClaimValue));
					break;
				case iat:
					tokenBuilder.withIssuedAt(solveDate(registeredClaimValue));
					break;
				case jti:
					tokenBuilder.withJWTId(registeredClaimValue);
					break;
				default:
					error.setError("RCL12", "Unknown registered claim");
			}
		} catch (Exception e) {
			error.setError("RCL11", e.getMessage());
			logger.error("getBuilderWithClaim", e);
		}
	}

	private static Date solveDate(String value) throws ParseException {
		logger.debug("solveDate");
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return !isANumber(value) ? dateFormat.parse(value) : new Date(Long.parseLong(value) * 1000);
	}

}
