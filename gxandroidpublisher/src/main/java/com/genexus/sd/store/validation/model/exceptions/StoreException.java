package com.genexus.sd.store.validation.model.exceptions;

public class StoreException extends Exception  {
	public StoreException(String errMessage){
		super(errMessage);
	}
	
	public StoreException(String errMessage, Exception e){
		super(errMessage, e.getCause());
	}
}
