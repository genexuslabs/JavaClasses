package com.genexus.JWT.claims;

import com.genexus.securityapicommons.commons.Error;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PrivateClaims extends Claims {

	public PrivateClaims() {
		super();

	}

	public boolean setClaim(String key, String value) {
		return super.setClaim(key, value, new Error());
	}

	public boolean setBooleanClaim(String key, boolean value) {
		return super.setClaim(key, value, new Error());
	}

	public boolean setNumericClaim(String key, int value) {
		return super.setClaim(key, value, new Error());
	}

	public boolean setDateClaim(String key, long value) {
		return super.setClaim(key, value, new Error());
	}

	@SuppressWarnings("unused")
	public boolean setDoubleClaim(String key, double value) {
		return super.setClaim(key, value, new Error());
	}

	public boolean setClaim(String key, PrivateClaims value) {

		return super.setClaim(key, value, new Error());
	}

	public Map<String, Object> getNestedMap() {
		HashMap<String, Object> result = new HashMap<String, Object>();
		for (Claim c : getAllClaims()) {
			if (c.getValue() != null) {
				result.put(c.getKey(), c.getValue());
			} else {
				result.put(c.getKey(), ((PrivateClaims) c.getNestedClaims()).getNestedMap());
			}
		}

		return result;
	}

}
