package com.genexus.commons;

import com.genexus.JWT.claims.PrivateClaims;
import com.genexus.securityapicommons.commons.SecurityAPIObject;

public abstract class JWTObject extends SecurityAPIObject {

	public JWTObject() {
		super();

	}

	public abstract String doCreate(String algorithm, PrivateClaims privateClaims, JWTOptions options);

	public abstract boolean doVerify(String token, String expectedAlgorithm, PrivateClaims privateClaims, JWTOptions options);

	public abstract String getPayload(String token);

	public abstract String getHeader(String token);

	public abstract String getTokenID(String token);

}
