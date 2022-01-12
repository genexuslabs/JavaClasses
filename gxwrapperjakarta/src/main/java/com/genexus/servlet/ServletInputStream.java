package com.genexus.servlet;

import java.io.InputStream;

public class ServletInputStream implements IServletInputStream{
	jakarta.servlet.ServletInputStream is;

	public ServletInputStream(jakarta.servlet.ServletInputStream is) {
		this.is = is;
	}

	public InputStream getInputStream() {
		return is;
	}
}
