package com.genexus.servlet;

import java.io.InputStream;

public class ServletInputStream implements IServletInputStream{
	javax.servlet.ServletInputStream is;

	public ServletInputStream(javax.servlet.ServletInputStream is) {
		this.is = is;
	}

	public InputStream getInputStream() {
		return is;
	}
}
