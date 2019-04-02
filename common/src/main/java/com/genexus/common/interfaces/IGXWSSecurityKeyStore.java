package com.genexus.common.interfaces;

public interface IGXWSSecurityKeyStore
{
	String getType();
	void setType(String type);
	String getPassword();
	void setPassword(String password);
	String getSource();
	void setSource(String source);	
}

