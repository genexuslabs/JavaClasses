package com.genexus.ws.rs.core;

public interface IResponseBuilder {
	Object build();
	void type(String type);
	void entity(Object entity);
	IResponseBuilder entityWrapped(Object entity);
	void header(String header, Object object);
}
