package com.genexus.commons;

import com.genexus.securityapicommons.commons.SecurityAPIObject;

public abstract class GUIDObject extends SecurityAPIObject{

	public GUIDObject() {
		super();

	}

	public abstract String generate();
}
