package com.genexus.servlet.http;

import jakarta.servlet.ServletException;
import com.genexus.servlet.IServletContext;
import com.genexus.servlet.ServletContext;

public abstract class HttpServlet extends jakarta.servlet.http.HttpServlet {

	protected abstract void callExecute(String method, IHttpServletRequest req, IHttpServletResponse res) throws ServletException;

	public void doPost(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse res) throws ServletException
	{
		IHttpServletRequest gxReq = new HttpServletRequest(req);
		IHttpServletResponse gxResp = new HttpServletResponse(res);
		callExecute("POST", gxReq, gxResp);
	}

	public void doGet(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse res) throws ServletException
	{
		IHttpServletRequest gxReq = new HttpServletRequest(req);
		IHttpServletResponse gxResp = new HttpServletResponse(res);
		callExecute("GET", gxReq, gxResp);
	}

	public void doDelete(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse res) throws ServletException
	{
		IHttpServletRequest gxReq = new HttpServletRequest(req);
		IHttpServletResponse gxResp = new HttpServletResponse(res);
		callExecute("DELETE", gxReq, gxResp);
	}

	public void doHead(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse res) throws ServletException
	{
		IHttpServletRequest gxReq = new HttpServletRequest(req);
		IHttpServletResponse gxResp = new HttpServletResponse(res);
		callExecute("HEAD", gxReq, gxResp);
	}

	public void doOptions(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse res) throws ServletException
	{
		IHttpServletRequest gxReq = new HttpServletRequest(req);
		IHttpServletResponse gxResp = new HttpServletResponse(res);
		callExecute("OPTIONS", gxReq, gxResp);
	}

	public void doPut(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse res) throws ServletException
	{
		IHttpServletRequest gxReq = new HttpServletRequest(req);
		IHttpServletResponse gxResp = new HttpServletResponse(res);
		callExecute("PUT", gxReq, gxResp);
	}

	public void doTrace(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse res) throws ServletException
	{
		IHttpServletRequest gxReq = new HttpServletRequest(req);
		IHttpServletResponse gxResp = new HttpServletResponse(res);
		callExecute("TRACE", gxReq, gxResp);
	}

	public IServletContext getWrappedServletContext() {
		return new ServletContext(super.getServletContext());
	}
}
