package com.genexus.servlet;

import com.genexus.servlet.http.HttpServletRequest;
import com.genexus.servlet.http.HttpServletResponse;
import com.genexus.servlet.http.IHttpServletRequest;
import com.genexus.servlet.http.IHttpServletResponse;

import java.io.IOException;

public class RequestDispatcher implements IRequestDispatcher{
	jakarta.servlet.RequestDispatcher req;

	public RequestDispatcher(jakarta.servlet.RequestDispatcher req) {
		this.req = req;
	}

	public void forward(IHttpServletRequest req, IHttpServletResponse resp) throws IOException, ServletException{
		try {
			this.req.forward(((HttpServletRequest)req).getWrappedClass(), ((HttpServletResponse)resp).getWrappedClass());
		} catch (jakarta.servlet.ServletException e) {
			throw new ServletException(e.getMessage());
		}
	}

	public void forward(IServletRequest req, IServletResponse resp) throws IOException, ServletException{
		try {
			this.req.forward(((ServletRequest)req).getWrappedClass(), ((ServletResponse)resp).getWrappedClass());
		} catch (jakarta.servlet.ServletException e) {
			throw new ServletException(e.getMessage());
		}
	}
}
