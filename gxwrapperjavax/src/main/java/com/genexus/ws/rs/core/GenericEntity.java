package com.genexus.ws.rs.core;

public class GenericEntity<T> extends javax.ws.rs.core.GenericEntity<T>{

	public GenericEntity(T entity) {
		super(entity);
	}
}
