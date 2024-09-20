package com.genexus.cryptography.commons;

import com.genexus.cryptography.asymmetric.utils.SignatureStandardOptions;
import com.genexus.securityapicommons.commons.SecurityAPIObject;

public abstract class StandardSignerObject extends SecurityAPIObject {

	public StandardSignerObject() {
		super();
	}

	public abstract String sign(String plainText, SignatureStandardOptions options);

	public abstract boolean verify(String signed, String plainText, SignatureStandardOptions options);
}
