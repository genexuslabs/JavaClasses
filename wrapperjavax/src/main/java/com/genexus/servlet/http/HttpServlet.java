package com.genexus.servlet.http;

import javax.servlet.ServletException;
import com.genexus.servlet.IServletContext;
import com.genexus.servlet.ServletContext;

public abstract class HttpServlet extends javax.servlet.http.HttpServlet {

	protected abstract void callExecute(String method, IHttpServletRequest req, IHttpServletResponse res) throws ServletException;

	public void doPost(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res) throws ServletException
	{
		IHttpServletRequest gxReq = new HttpServletRequest(req);
		IHttpServletResponse gxResp = new HttpServletResponse(res);
		callExecute("POST", gxReq, gxResp);
	}

	public void doGet(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res) throws ServletException
	{
		IHttpServletRequest gxReq = new HttpServletRequest(req);
		IHttpServletResponse gxResp = new HttpServletResponse(res);
		callExecute("GET", gxReq, gxResp);
	}

	public void doDelete(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res) throws ServletException
	{
		IHttpServletRequest gxReq = new HttpServletRequest(req);
		IHttpServletResponse gxResp = new HttpServletResponse(res);
		callExecute("DELETE", gxReq, gxResp);
	}

	public void doHead(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res) throws ServletException
	{
		IHttpServletRequest gxReq = new HttpServletRequest(req);
		IHttpServletResponse gxResp = new HttpServletResponse(res);
		callExecute("HEAD", gxReq, gxResp);
	}

	public void doOptions(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res) throws ServletException
	{
		IHttpServletRequest gxReq = new HttpServletRequest(req);
		IHttpServletResponse gxResp = new HttpServletResponse(res);
		callExecute("OPTIONS", gxReq, gxResp);
	}

	public void doPut(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res) throws ServletException
	{
		IHttpServletRequest gxReq = new HttpServletRequest(req);
		IHttpServletResponse gxResp = new HttpServletResponse(res);
		callExecute("PUT", gxReq, gxResp);
	}

	public void doTrace(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse res) throws ServletException
	{
		IHttpServletRequest gxReq = new HttpServletRequest(req);
		IHttpServletResponse gxResp = new HttpServletResponse(res);
		callExecute("TRACE", gxReq, gxResp);
	}

	public IServletContext getWrappedServletContext() {
		return new ServletContext(super.getServletContext());
	}
}
