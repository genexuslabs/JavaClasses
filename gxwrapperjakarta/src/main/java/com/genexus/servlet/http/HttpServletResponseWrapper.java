package com.genexus.servlet.http;

import com.genexus.servlet.ServletOutputStream;
import com.genexus.servlet.IServletOutputStream;

import java.io.IOException;

public abstract class HttpServletResponseWrapper extends jakarta.servlet.http.HttpServletResponseWrapper implements IHttpServletResponseWrapper{
	public abstract IServletOutputStream getWrapperOutputStream() throws IOException;

	public HttpServletResponseWrapper(IHttpServletResponse response) {
		super(((HttpServletResponse)response).getWrappedClass());
	}

	public void addCookie(ICookie cookie) {
		super.addCookie((Cookie)cookie);
	}

	@Override
	public jakarta.servlet.ServletOutputStream getOutputStream() throws IOException {
		return ((ServletOutputStream) getWrapperOutputStream()).getWrappedClass();
	}

	public IServletOutputStream getWrappedOutputStream() throws IOException {
		return new ServletOutputStream(super.getOutputStream());
	}
}
