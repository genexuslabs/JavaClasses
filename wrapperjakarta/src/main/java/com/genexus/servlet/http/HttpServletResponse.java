package com.genexus.servlet.http;

import com.genexus.CommonUtil;
import com.genexus.servlet.ServletOutputStream;
import com.genexus.servlet.IServletOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

public class HttpServletResponse implements IHttpServletResponse{
	private jakarta.servlet.http.HttpServletResponse resp;

	public HttpServletResponse(jakarta.servlet.http.HttpServletResponse resp) {
		this.resp = resp;
	}

	public HttpServletResponse(Object resp) {
		this.resp = (jakarta.servlet.http.HttpServletResponse)resp;
	}

	public jakarta.servlet.http.HttpServletResponse getWrappedClass() {
		return resp;
	}

	public void setHeader(String name, String value) {
		resp.setHeader(name, CommonUtil.Sanitize(value, CommonUtil.HTTP_HEADER_WHITELIST));
	}

	public void addDateHeader(String name, long date) {
		resp.addDateHeader(name, date);
	}

	public void setDateHeader(String name, long date) {
		resp.setDateHeader(name, date);
	}

	public void addHeader(String name, String value) {
		resp.addHeader(name, value);
	}

	public void setStatus(int sc) {
		resp.setStatus(sc);
	}

	public void sendError(int sc) throws IOException, IllegalStateException {
		resp.sendError(sc);
	}

	public void sendError(int sc, String msg) throws java.io.IOException {
		resp.sendError(sc, msg);
	}

	public void setContentType(String type) {
		resp.setContentType(type);
	}

	public void reset() {
		resp.reset();
	}

	public void flushBuffer() throws IOException{
		resp.flushBuffer();
	}

	public PrintWriter getWriter() throws IOException, IllegalStateException, UnsupportedEncodingException {
		return resp.getWriter();
	}

	public boolean isCommitted() {
		return resp.isCommitted();
	}

	public void addCookie(ICookie cookie) {
		resp.addCookie((Cookie) cookie);
	}

	public void setContentLength(int len) {
		resp.setContentLength(len);
	}

	public Collection<String> getHeaders(String name) {
		return resp.getHeaders(name);
	}

	public void setBufferSize(int size) {
		resp.setBufferSize(size);
	}

	public IServletOutputStream getOutputStream() throws java.io.IOException {
		return new ServletOutputStream(resp.getOutputStream());
	}

	public static int getSC_MOVED_PERMANENTLY() {
		return jakarta.servlet.http.HttpServletResponse.SC_MOVED_TEMPORARILY;
	}

	public static int getSC_MOVED_TEMPORARILY() {
		return jakarta.servlet.http.HttpServletResponse.SC_MOVED_TEMPORARILY;
	}

	public static int getSC_OK() {
		return jakarta.servlet.http.HttpServletResponse.SC_OK;
	}

	public static int getSC_FOUND() {
		return jakarta.servlet.http.HttpServletResponse.SC_FOUND;
	}

	public static int getSC_NOT_MODIFIED() {
		return jakarta.servlet.http.HttpServletResponse.SC_NOT_MODIFIED;
	}

}
