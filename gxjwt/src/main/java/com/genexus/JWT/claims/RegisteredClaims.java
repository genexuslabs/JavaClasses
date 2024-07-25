package com.genexus.JWT.claims;

import java.util.HashMap;
import java.util.List;

import com.genexus.securityapicommons.commons.Error;
import com.genexus.securityapicommons.utils.SecurityUtils;

public final class RegisteredClaims extends Claims {

	private List<Claim> claims;
	private HashMap<String, String> customTimeValidationClaims;

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
			return false;
		}
	}

	public boolean setTimeValidatingClaim(String key, String value, String customValidationSeconds, Error error) {
		if (RegisteredClaim.exists(key) && RegisteredClaim.isTimeValidatingClaim(key)) {
			customTimeValidationClaims.put(key, customValidationSeconds);
			return setClaim(key, value, error);
		} else {
			error.setError("RCS02", "Wrong registered key value");
			return false;
		}
	}

	public long getClaimCustomValidationTime(String key) {
		String stringTime = "";

		if (customTimeValidationClaims.containsKey(key)) {
			try {
				stringTime = customTimeValidationClaims.get(key);
			} catch (Exception e) {
				return 0;
			}
		} else {
			return 0;
		}

		return Long.parseLong(stringTime);
	}

	public boolean hasCustomValidationClaims() {
		return customTimeValidationClaims.size() != 0;
	}

	@Override
	public Object getClaimValue(String key, Error error) {
		if (RegisteredClaim.exists(key)) {
			for (int i = 0; i < claims.size(); i++) {
				if (SecurityUtils.compareStrings(key, claims.get(i).getKey())) {
					return claims.get(i).getValue();
				}
			}
			error.setError("RCS03", String.format("Could not find a claim with %s key value", key));
			return "";
		} else {
			error.setError("RCS02", "Wrong registered key value");
			return "";
		}
	}

}
