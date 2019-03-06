package com.genexus;

public interface IHttpContext {

	String getStaticContentBase();

	String convertURL(String string);

	String getLanguage();

	String getDefaultPath();

	String getHeader(String string);

	String getClientId();

}
