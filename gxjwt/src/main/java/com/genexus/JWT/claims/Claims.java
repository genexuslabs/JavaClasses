package com.genexus.JWT.claims;

import java.util.ArrayList;
import java.util.List;

import com.genexus.securityapicommons.commons.Error;
import com.genexus.securityapicommons.utils.SecurityUtils;

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

	public Object getClaimValue(String key, Error error) {
		for (int i = 0; i < claims.size(); i++) {
			if (SecurityUtils.compareStrings(key, claims.get(i).getKey())) {
				return claims.get(i).getValue();
			}
		}
		error.setError("CLA01", String.format("Could not find a claim with %s key value", key));
		return "";
	}

	public boolean isEmpty() {
		if (claims.size() == 0) {
			return true;
		} else {
			return false;

		}
	}
}
