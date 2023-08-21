package com.genexus.servlet.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import com.genexus.servlet.IServletOutputStream;

public interface IHttpServletResponse {
	void setHeader(String name, String value);
	void setHeader(String name, String value, boolean sanitize);
	void addDateHeader(String name, long date);
	void setDateHeader(String name, long date);
	void addHeader(String name, String value);
	void setStatus(int sc);
	void sendError(int sc) throws IOException, IllegalStateException;
	void sendError(int sc, String msg) throws java.io.IOException;
	void setContentType(String type);
	void reset();
	void flushBuffer() throws IOException;
	PrintWriter getWriter() throws IOException, IllegalStateException, UnsupportedEncodingException;
	boolean isCommitted();
	void addCookie(ICookie cookie);
	void setContentLength(int len);
	Collection<String> getHeaders(String name);
	void setBufferSize(int size);
	IServletOutputStream getOutputStream() throws java.io.IOException;
}
