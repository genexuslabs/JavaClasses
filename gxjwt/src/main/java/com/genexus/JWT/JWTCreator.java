package com.genexus.JWT;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.interfaces.Verification;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genexus.JWT.claims.*;
import com.genexus.JWT.utils.JWTAlgorithm;
import com.genexus.JWT.utils.RevocationList;
import com.genexus.commons.JWTObject;
import com.genexus.commons.JWTOptions;
import com.genexus.securityapicommons.commons.PublicKey;
import com.genexus.securityapicommons.config.EncodingUtil;
import com.genexus.securityapicommons.keys.PrivateKeyManager;
import com.genexus.securityapicommons.utils.SecurityUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JWTCreator extends JWTObject {

	private int counter;

	private static final Logger logger = LogManager.getLogger(JWTCreator.class);

	public JWTCreator() {
		super();
		EncodingUtil eu = new EncodingUtil();
		eu.setEncoding("UTF8");
		this.counter = 0;

	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - BEGIN ********/

	public String doCreate(String algorithm, PrivateClaims privateClaims, JWTOptions options) {
		this.error.cleanError();
		logger.debug("doCreate");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(JWTCreator.class), "doCreate", "algorithm", algorithm, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(JWTCreator.class), "doCreate", "privateClaims", privateClaims, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(JWTCreator.class), "doCreate", "options", options, this.error);
		if (this.hasError()) {
			return "";
		}
		// INPUT VERIFICATION - END
		return create_Aux(algorithm, privateClaims, options, null, true);
	}

	public String doCreateFromJSON(String algorithm, String json, JWTOptions options) {
		this.error.cleanError();
		logger.debug("doCreateFromJSON");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(JWTCreator.class), "doCreateFromJSON", "algorithm", algorithm, this.error);
		SecurityUtils.validateStringInput(String.valueOf(JWTCreator.class), "doCreateFromJSON", "json", json, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(JWTCreator.class), "doCreateFromJSON", "options", options, this.error);
		if (this.hasError()) {
			return "";
		}
		// INPUT VERIFICATION - END
		return create_Aux(algorithm, null, options, json, false);
	}

	public boolean doVerify(String token, String expectedAlgorithm, PrivateClaims privateClaims, JWTOptions options) {
		this.error.cleanError();
		logger.debug("doVerify");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(JWTCreator.class), "doVerify", "token", token, this.error);
		SecurityUtils.validateStringInput(String.valueOf(JWTCreator.class), "doVerify", "expectedAlgorithm", expectedAlgorithm, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(JWTCreator.class), "doVerify", "privateClaims", privateClaims, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(JWTCreator.class), "doVerify", "options", options, this.error);
		if (this.hasError()) {
			return false;
		}
		// INPUT VERIFICATION - END
		return doVerify(token, expectedAlgorithm, privateClaims, options, true, true);
	}

	public boolean doVerifyJustSignature(String token, String expectedAlgorithm, JWTOptions options) {
		this.error.cleanError();
		logger.debug("doVerifyJustSignature");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(JWTCreator.class), "doVerifyJustSignature", "token", token, this.error);
		SecurityUtils.validateStringInput(String.valueOf(JWTCreator.class), "doVerifyJustSignature", "expectedAlgorithm", expectedAlgorithm, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(JWTCreator.class), "doVerifyJustSignature", "options", options, this.error);
		if (this.hasError()) {
			return false;
		}
		// INPUT VERIFICATION - END
		return doVerify(token, expectedAlgorithm, null, options, false, false);
	}

	public boolean doVerifySignature(String token, String expectedAlgorithm, JWTOptions options) {
		this.error.cleanError();
		logger.debug("doVerifySignature");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(JWTCreator.class), "doVerifySignature", "token", token, this.error);
		SecurityUtils.validateStringInput(String.valueOf(JWTCreator.class), "doVerifySignature", "expectedAlgorithm", expectedAlgorithm, this.error);
		SecurityUtils.validateObjectInput(String.valueOf(JWTCreator.class), "doVerifySignature", "options", options, this.error);
		if (this.hasError()) {
			return false;
		}
		// INPUT VERIFICATION - END
		return doVerify(token, expectedAlgorithm, null, options, false, true);
	}

	public String getPayload(String token) {
		this.error.cleanError();
		logger.debug("getPayload");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(JWTCreator.class), "getPayload", "token", token, this.error);
		if (this.hasError()) {
			return "";
		}
		// INPUT VERIFICATION - END
		this.error.cleanError();

		try {
			return getTokenPart(token, "payload");
		} catch (Exception e) {
			this.error.setError("JW001", e.getMessage());
			logger.error("getPayload", e);
			return "";
		}

	}

	public String getHeader(String token) {
		this.error.cleanError();
		logger.debug("getHeader");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(JWTCreator.class), "getHeader", "token", token, this.error);
		if (this.hasError()) {
			return "";
		}
		// INPUT VERIFICATION - END

		try {
			return getTokenPart(token, "header");
		} catch (Exception e) {
			this.error.setError("JW002", e.getMessage());
			logger.error("getHeader", e);
			return "";
		}
	}

	public String getTokenID(String token) {
		this.error.cleanError();
		logger.debug("getTokenID");
		// INPUT VERIFICATION - BEGIN
		SecurityUtils.validateStringInput(String.valueOf(JWTCreator.class), "getTokenID", "token", token, this.error);
		if (this.hasError()) {
			return "";
		}
		// INPUT VERIFICATION - END

		try {

			return getTokenPart(token, "id");
		} catch (Exception e) {
			this.error.setError("JW003", e.getMessage());
			logger.error("getTokenID", e);
			return "";
		}
	}

	/******** EXTERNAL OBJECT PUBLIC METHODS - END ********/

	@SuppressWarnings("unchecked")
	private String create_Aux(String algorithm, PrivateClaims privateClaims, JWTOptions options, String payload,
							  boolean hasClaims) {
		logger.debug("create_Aux");
		if (options == null) {
			this.error.setError("JW004", "Options parameter is null");
			return "";
		}
		JWTAlgorithm alg = JWTAlgorithm.getJWTAlgorithm(algorithm, this.error);
		if (this.hasError()) {
			return "";
		}
		Builder tokenBuilder = JWT.create();
		if (!options.getHeaderParameters().isEmpty()) {
			HeaderParameters parameters = options.getHeaderParameters();
			tokenBuilder.withHeader(parameters.getMap());
		}
		if (hasClaims) {
			if (privateClaims == null) {
				this.error.setError("JW005", "PrivateClaims parameter is null");
				return "";
			}
			doBuildPayload(tokenBuilder, privateClaims, options);
		} else {
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				HashMap<String, Object> map = objectMapper.readValue(payload, HashMap.class);
				tokenBuilder.withPayload(map);
			} catch (Exception e) {
				this.error.setError("JW005", e.getMessage());
				logger.error("create_Aux", e);
				return "";
			}
		}
		if (this.hasError()) {
			return "";
		}
		Algorithm algorithmType = getAlgorithmTypePrivate(alg, options);
		if (this.hasError() || algorithmType == null) {
			return "";
		}
		try {
			return tokenBuilder.sign(algorithmType);
		} catch (Exception e) {
			this.error.setError("JW006", e.getMessage());
			logger.error("create_Aux", e);
			return "";
		}
	}

	private Algorithm getAlgorithmTypePrivate(JWTAlgorithm alg, JWTOptions options) {
		if (JWTAlgorithm.isPrivate(alg)) {
			PrivateKeyManager key = options.getPrivateKey();
			if (key == null) {
				this.error.setError("JW018", "Add the private key using JWTOptions.SetPrivateKey function");
				return null;
			}
			if (key.hasError()) {
				this.error = key.getError();
				return null;

			}
			return JWTAlgorithm.getAsymmetricAlgorithm(alg, key, null, this.error);
		} else {
			return JWTAlgorithm.getSymmetricAlgorithm(alg, options.getSecret(), this.error);
		}
	}

	private Algorithm getAlgorithmTypePublic(JWTAlgorithm alg, JWTOptions options) {
		if (JWTAlgorithm.isPrivate(alg)) {
			PublicKey cert = options.getPublicKey();
			if (cert == null) {
				this.error.setError("JW010", "The public key is null. Load a public key or certificate on JWTOptions object");
				return null;
			}
			if (cert.hasError()) {
				this.error = cert.getError();
				return null;
			}
			return JWTAlgorithm.getAsymmetricAlgorithm(alg, null, cert, this.error);
		} else {
			return JWTAlgorithm.getSymmetricAlgorithm(alg, options.getSecret(), this.error);
		}
	}

	private boolean doVerify(String token, String expectedAlgorithm, PrivateClaims privateClaims, JWTOptions options,
							 boolean verifyClaims, boolean verifyRegClaims) {
		logger.debug("private doVerify");
		if (options == null) {
			this.error.setError("JW007", "Options parameter is null");
			logger.error("doVerify - Options parameter is null");
			return false;
		}
		DecodedJWT decodedJWT = null;
		try {
			decodedJWT = JWT.decode(token);

		} catch (Exception e) {
			this.error.setError("JW008", e.getMessage());
			logger.error("private doVerify", e);
			return false;
		}
		if (isRevoqued(decodedJWT, options)) {
			return false;
		}
		if (verifyClaims) {
			if (!verifyPrivateClaims(decodedJWT, privateClaims, options) || !verifyHeader(decodedJWT, options)) {
				return false;
			}
		}
		String algorithm = decodedJWT.getAlgorithm();
		JWTAlgorithm alg = JWTAlgorithm.getJWTAlgorithm(algorithm, this.error);
		if (this.hasError()) {
			return false;
		}
		JWTAlgorithm expectedJWTAlgorithm = JWTAlgorithm.getJWTAlgorithm(expectedAlgorithm, this.error);
		if (!(alg != null && alg.equals(expectedJWTAlgorithm)) || this.hasError()) {
			this.error.setError("JW009", "Expected algorithm does not match token algorithm");
			logger.error("private doVerify - Expected algorithm does not match token algorithm");
			return false;
		}

		Algorithm algorithmType = getAlgorithmTypePublic(alg, options);
		if (this.hasError() || algorithmType == null) {
			return false;
		}
		Verification verification = buildVerification(JWT.require(algorithmType), options, verifyRegClaims);
		if (this.hasError()) {
			return false;
		}
		try {
			JWTVerifier verifier = verification != null ? verification.build() : null;
			if (verifier != null) {
				verifier.verify(JWT.decode(token));
			}
		} catch (Exception e) {

			error.setError("JW011", e.getMessage());
			logger.error("private doVerify", e);
			return false;
		}
		return true;
	}

	private String getTokenPart(String token, String part) {
		logger.debug("getTokenPart");
		DecodedJWT decodedToken = JWT.decode(token);
		switch (part) {
			case "payload":
				return decodeTokenPart(decodedToken.getPayload());
			case "header":
				return decodeTokenPart(decodedToken.getHeader());
			case "id":
				return decodedToken.getId();
			default:
				error.setError("JW012", "Unknown token segment");
				logger.error("getTokenPart - Unknown token segment");
				return "";
		}
	}

	private String decodeTokenPart(String base64Part) {
		byte[] base64Bytes = Base64.decodeBase64(base64Part);
		EncodingUtil eu = new EncodingUtil();
		String plainTextPart = eu.getString(base64Bytes);
		if (eu.hasError()) {
			this.error = eu.getError();
			return "";
		}
		return plainTextPart;
	}

	private boolean isRevoqued(DecodedJWT decodedJWT, JWTOptions options) {
		RevocationList rList = options.getRevocationList();
		return rList.isInRevocationList(decodedJWT.getId());
	}

	private Verification buildVerification(Verification verification, JWTOptions options, boolean verifyClaims) {
		logger.debug("buildVerification");
		// Adding registered claims
		if (!verifyClaims) {
			return verification;
		}
		if (options.hasRegisteredClaims()) {
			RegisteredClaims registeredClaims = options.getAllRegisteredClaims();
			List<Claim> registeredC = registeredClaims.getAllClaims();
			for (Claim claim : registeredC) {
				if (RegisteredClaim.exists(claim.getKey())) {
					if (!RegisteredClaim.isTimeValidatingClaim(claim.getKey())) {
						verification = RegisteredClaim.getVerificationWithClaim(claim.getKey(),
							(String) claim.getValue(), 0, verification, error);
					} else {
						verification = RegisteredClaim.getVerificationWithClaim(claim.getKey(),
							(String) claim.getValue(),
							registeredClaims.getClaimCustomValidationTime(claim.getKey()),
							verification, error);
					}
					if (this.hasError()) {
						return null;
					}
				} else {
					error.setError("JW013", String.format("%s wrong registered claim key", claim.getKey()));
					logger.error(String.format("buildVerification -  %s wrong registered claim key", claim.getKey()));
					return null;
				}
			}
		}
		return verification;

	}

	private void doBuildPayload(Builder tokenBuilder, PrivateClaims privateClaims, JWTOptions options) {
		logger.debug("doBuildPayload");
		// ****START BUILD PAYLOAD****//
		// Adding private claims
		List<Claim> privateC = privateClaims.getAllClaims();
		for (Claim claim : privateC) {
			try {
				if (claim.getNestedClaims() != null) {
					tokenBuilder.withClaim(claim.getKey(), claim.getNestedClaims().getNestedMap());
				} else {
					Object obj = claim.getValue();
					if (obj instanceof String) {
						tokenBuilder.withClaim(claim.getKey(), (String) claim.getValue());
					} else if (obj instanceof Integer) {
						tokenBuilder.withClaim(claim.getKey(), (int) claim.getValue());
					} else if (obj instanceof Long) {
						tokenBuilder.withClaim(claim.getKey(), (long) claim.getValue());
					} else if (obj instanceof Double) {
						tokenBuilder.withClaim(claim.getKey(), (double) claim.getValue());
					} else if (obj instanceof Boolean) {
						tokenBuilder.withClaim(claim.getKey(), (boolean) claim.getValue());
					} else {
						this.error.setError("JW014", "Unrecognized data type");
						logger.error("doBuildPayload - Unrecognized data type");
					}
				}
			} catch (Exception e) {
				this.error.setError("JW015", e.getMessage());
				logger.error("doBuildPayload", e);
			}
		}
		// Adding public claims
		if (options.hasPublicClaims()) {
			PublicClaims publicClaims = options.getAllPublicClaims();
			List<Claim> publicC = publicClaims.getAllClaims();
			for (Claim claim : publicC) {
				try {
					tokenBuilder.withClaim(claim.getKey(), (String) claim.getValue());
				} catch (Exception e) {
					this.error.setError("JW016", e.getMessage());
					logger.error("doBuildPayload", e);
				}
			}
		}
		// Adding registered claims
		if (options.hasRegisteredClaims()) {
			RegisteredClaims registeredClaims = options.getAllRegisteredClaims();
			List<Claim> registeredC = registeredClaims.getAllClaims();
			for (Claim claim : registeredC) {
				if (RegisteredClaim.exists(claim.getKey())) {
					RegisteredClaim.getBuilderWithClaim(claim.getKey(),
						(String) claim.getValue(), tokenBuilder, this.error);
				}
			}
		}
		// ****END BUILD PAYLOAD****//
	}

	private boolean verifyPrivateClaims(DecodedJWT decodedJWT, PrivateClaims privateClaims, JWTOptions options) {

		logger.debug("verifyPrivateClaims");
		RegisteredClaims registeredClaims = options.getAllRegisteredClaims();
		PublicClaims publicClaims = options.getAllPublicClaims();
		if (privateClaims == null || privateClaims.isEmpty()) {
			return true;
		}
		String base64Part = decodedJWT.getPayload();
		byte[] base64Bytes = Base64.decodeBase64(base64Part);
		EncodingUtil eu = new EncodingUtil();
		String plainTextPart = eu.getString(base64Bytes);
		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper mapper = new ObjectMapper();

		try {
			map = (HashMap<String, Object>) mapper.readValue(plainTextPart, new TypeReference<Map<String, Object>>() {
			});
		} catch (Exception e) {
			this.error.setError("JW018", e.getMessage());
			logger.error("verifyPrivateClaims", e);
			return false;
		}
		this.counter = 0;
		boolean validation = verifyNestedClaims(privateClaims.getNestedMap(), map, registeredClaims, publicClaims);
		int pClaimsCount = countingPrivateClaims(privateClaims.getNestedMap(), 0);
		if (validation && !(this.counter == pClaimsCount)) {
			return false;
		}
		return validation;
	}

	private boolean verifyNestedClaims(Map<String, Object> pclaimMap, Map<String, Object> map,
									   RegisteredClaims registeredClaims, PublicClaims publicClaims) {
		logger.debug("verifyNestedClaims");
		List<String> mapClaimKeyList = new ArrayList<String>(map.keySet());
		for (String mapKey : mapClaimKeyList) {

			if (!isRegistered(mapKey, registeredClaims) && !isPublic(mapKey, publicClaims)) {

				this.counter++;
				if (!pclaimMap.containsKey(mapKey)) {
					return false;
				}

				Object op = pclaimMap.get(mapKey);
				Object ot = map.get(mapKey);

				if (op instanceof String && ot instanceof String) {

					if (!SecurityUtils.compareStrings(((String) op).trim(), ((String) ot).trim())) {
						return false;
					}

				} else if ((op instanceof Integer || op instanceof Long)
					&& (ot instanceof Integer || ot instanceof Long)) {
					if ((convertToLong(op)).compareTo(convertToLong(ot)) != 0) {
						return false;
					}
				} else if ((op instanceof Double && ot instanceof Double)) {
					if ((double) op != (double) ot) {
						return false;
					}
				} else if ((op instanceof Boolean && ot instanceof Boolean)) {
					if (Boolean.compare((boolean) op, (boolean) ot) != 0) {
						return false;
					}
				} else if (op instanceof HashMap && ot instanceof HashMap) {
					@SuppressWarnings("unchecked")
					boolean flag = verifyNestedClaims((HashMap<String, Object>) op, (HashMap<String, Object>) ot,
						registeredClaims, publicClaims);
					if (!flag) {
						return false;
					}
				} else {
					return false;
				}
			}
		}
		return true;
	}

	private boolean isRegistered(String claimKey, RegisteredClaims registeredClaims) {

		List<Claim> registeredClaimsList = registeredClaims.getAllClaims();
		for (Claim s : registeredClaimsList) {
			if (SecurityUtils.compareStrings(s.getKey().trim(), claimKey.trim())) {
				return true;
			}
		}
		return false;
	}

	private boolean isPublic(String claimKey, PublicClaims publicClaims) {
		List<Claim> publicClaimsList = publicClaims.getAllClaims();
		for (Claim s : publicClaimsList) {
			if (SecurityUtils.compareStrings(s.getKey().trim(), claimKey.trim())) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private int countingPrivateClaims(Map<String, Object> map, int counter) {
		List<String> list = new ArrayList<String>(map.keySet());
		for (String s : list) {
			counter++;
			Object obj = map.get(s);
			if (obj instanceof HashMap) {
				counter = countingPrivateClaims((HashMap<String, Object>) obj, counter);
			}
		}
		return counter;
	}

	private boolean verifyHeader(DecodedJWT decodedJWT, JWTOptions options) {

		logger.debug("verifyHeader");
		HeaderParameters parameters = options.getHeaderParameters();
		int claimsNumber = getHeaderClaimsNumber(decodedJWT);

		if (parameters.isEmpty() && claimsNumber == 2) {
			return true;
		}
		if (parameters.isEmpty() && claimsNumber > 2) {
			return false;
		}
		List<String> allParms = parameters.getAll();
		if (allParms.size() + 2 != claimsNumber) {
			return false;
		}
		Map<String, Object> map = parameters.getMap();
		for (String s : allParms) {

			if (decodedJWT.getHeaderClaim(s) == null) {
				return false;
			}
			com.auth0.jwt.interfaces.Claim c = decodedJWT.getHeaderClaim(s);
			try {
				String claimValue = c.asString().trim();
				String optionsValue = ((String) map.get(s)).trim();
				return SecurityUtils.compareStrings(claimValue, optionsValue);
			} catch (NullPointerException e) {
				return false;
			}
		}
		return true;
	}

	private int getHeaderClaimsNumber(DecodedJWT decodedJWT) {
		String base64Part = decodedJWT.getHeader();
		byte[] base64Bytes = Base64.decodeBase64(base64Part);
		EncodingUtil eu = new EncodingUtil();
		String plainTextPart = eu.getString(base64Bytes);
		ObjectMapper mapper = new ObjectMapper();

		try {
			HashMap<String, Object> map = (HashMap<String, Object>) mapper.readValue(plainTextPart, new TypeReference<Map<String, Object>>() {
			});
			return map.size();
		} catch (Exception e) {
			return 0;
		}
	}

	private Long convertToLong(Object o) {
		String stringToConvert = String.valueOf(o);
		return Long.parseLong(stringToConvert);

	}

}