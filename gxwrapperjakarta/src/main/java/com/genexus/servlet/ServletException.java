package com.genexus.servlet;

public class ServletException extends jakarta.servlet.ServletException{

	public ServletException(String message){
		super(message);
	}

	public ServletException(String message, RuntimeException e){
		super(message, e);
	}
	public ServletException(Throwable e){
		super(e);
	}
}
