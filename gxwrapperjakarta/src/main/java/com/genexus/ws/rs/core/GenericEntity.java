package com.genexus.ws.rs.core;

public class GenericEntity<T> extends jakarta.ws.rs.core.GenericEntity<T>{

	public GenericEntity(T entity) {
		super(entity);
	}
}
