package com.genexus.servlet.http;

public interface ICookie {

	boolean getSecure();
	String getName();
	String getValue();
	void setSecure(boolean secure);
	void setPath(String path);
	void setMaxAge(int maxAge);
	void setDomain(String domain);
	void setHttpOnly(boolean httpOnly);
}
