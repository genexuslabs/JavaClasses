package com.genexus.JWT.claims;

import com.genexus.securityapicommons.commons.Error;
import com.genexus.securityapicommons.utils.SecurityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("LoggingSimilarMessage")
public final class RegisteredClaims extends Claims {

	private List<Claim> claims;
	private HashMap<String, String> customTimeValidationClaims;

	private static final Logger logger = LogManager.getLogger(RegisteredClaims.class);

	public RegisteredClaims() {
		super();
		customTimeValidationClaims = new HashMap<String, String>();

	}

	@Override
	public boolean setClaim(String key, Object value, Error error) {
		error.setError("RCS01", "Not alllowed data type");
		return false;
	}

	public boolean setClaim(String key, String value, Error error) {
		if (RegisteredClaim.exists(key)) {
			return super.setClaim(key, value, error);
		} else {
			error.setError("RCS02", "Wrong registered key value");
			logger.error("Wrong registered key value");
			return false;
		}
	}

	@SuppressWarnings("UnusedReturnValue")
	public boolean setTimeValidatingClaim(String key, String value, String customValidationSeconds, Error error) {
		if (RegisteredClaim.exists(key) && RegisteredClaim.isTimeValidatingClaim(key)) {
			customTimeValidationClaims.put(key, customValidationSeconds);
			return setClaim(key, value, error);
		} else {
			error.setError("RCS02", "Wrong registered key value");
			logger.error("Wrong registered key value");
			return false;
		}
	}

	public long getClaimCustomValidationTime(String key) {
		if (customTimeValidationClaims.containsKey(key)) {
			try {
				return Long.parseLong(customTimeValidationClaims.get(key));
			} catch (Exception e) {
				logger.error("getClaimCustomValidationTime", e);
				return 0;
			}
		} else {
			return 0;
		}
	}

	public boolean hasCustomValidationClaims() {
		return !customTimeValidationClaims.isEmpty();
	}

	@Override
	public Object getClaimValue(String key, Error error) {
		if (RegisteredClaim.exists(key)) {
			for (Claim claim : claims) {
				if (SecurityUtils.compareStrings(key, claim.getKey())) {
					return claim.getValue();
				}
			}
			error.setError("RCS03", String.format("Could not find a claim with %s key value", key));
			logger.error(String.format("Could not find a claim with %s key value", key));
			return "";
		} else {
			error.setError("RCS02", "Wrong registered key value");
			logger.error("Wrong registered key value");
			return "";
		}
	}

}
