package com.genexus.servlet;

import com.genexus.servlet.http.HttpServletResponse;
import com.genexus.servlet.http.IHttpServletResponse;

public class ServletResponse implements IServletResponse{
	private javax.servlet.ServletResponse resp;

	public ServletResponse(javax.servlet.ServletResponse resp) {
		this.resp = resp;
	}

	public javax.servlet.ServletResponse getWrappedClass() {
		return resp;
	}

	public boolean isHttpServletResponse()
	{
		return resp instanceof javax.servlet.http.HttpServletResponse;
	}

	public IHttpServletResponse getHttpServletResponse()
	{
		if (resp instanceof javax.servlet.http.HttpServletResponse) {
			return new HttpServletResponse((javax.servlet.http.HttpServletResponse)resp);
		}
		return null;
	}
	public boolean isCommitted() {
		return resp.isCommitted();
	}
}
