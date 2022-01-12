package com.genexus.servlet;

import com.genexus.servlet.http.HttpServletResponse;
import com.genexus.servlet.http.IHttpServletResponse;

public class ServletResponse implements IServletResponse{
	private jakarta.servlet.ServletResponse resp;

	public ServletResponse(jakarta.servlet.ServletResponse resp) {
		this.resp = resp;
	}

	public jakarta.servlet.ServletResponse getWrappedClass() {
		return resp;
	}

	public boolean isHttpServletResponse()
	{
		return resp instanceof jakarta.servlet.http.HttpServletResponse;
	}

	public IHttpServletResponse getHttpServletResponse()
	{
		if (resp instanceof jakarta.servlet.http.HttpServletResponse) {
			return new HttpServletResponse((jakarta.servlet.http.HttpServletResponse)resp);
		}
		return null;
	}

	public boolean isCommitted() {
		return resp.isCommitted();
	}
}
