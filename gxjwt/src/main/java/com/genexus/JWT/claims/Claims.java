package com.genexus.JWT.claims;

import com.genexus.securityapicommons.commons.Error;
import com.genexus.securityapicommons.utils.SecurityUtils;

import java.util.ArrayList;
import java.util.List;

public class Claims {

	private List<Claim> claims;

	public Claims() {
		claims = new ArrayList<Claim>();
	}

	public boolean setClaim(String key, Object value, Error error) {
		Claim claim = new Claim(key, value);
		claims.add(claim);
		return true;
	}

	public List<Claim> getAllClaims() {
		return claims;
	}

	@SuppressWarnings("unused")
	public Object getClaimValue(String key, Error error) {
		for (Claim claim : claims) {
			if (SecurityUtils.compareStrings(key, claim.getKey())) {
				return claim.getValue();
			}
		}
		error.setError("CLA01", String.format("Could not find a claim with %s key value", key));
		return "";
	}

	public boolean isEmpty() {
		return claims.isEmpty();
	}
}
