package com.genexus.servlet;

import com.genexus.servlet.http.IHttpServletRequest;
import com.genexus.servlet.http.IHttpServletResponse;

public interface IRequestDispatcher {
	void forward(IHttpServletRequest req, IHttpServletResponse resp) throws Exception;

	void forward(IServletRequest req, IServletResponse resp) throws Exception;
}
