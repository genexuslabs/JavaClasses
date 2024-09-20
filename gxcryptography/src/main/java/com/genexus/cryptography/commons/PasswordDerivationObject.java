package com.genexus.cryptography.commons;

import com.genexus.securityapicommons.commons.SecurityAPIObject;

public abstract class PasswordDerivationObject extends SecurityAPIObject {

	public PasswordDerivationObject() {
		super();
	}

	public abstract String doGenerateSCrypt(String password, String salt, int CPUCost, int blockSize,
											int parallelization, int keyLenght);

	public abstract String doGenerateDefaultSCrypt(String password, String salt);

	public abstract String doGenerateBcrypt(String password, String salt, int cost);

	public abstract String doGenerateDefaultBcrypt(String password, String salt);
}
