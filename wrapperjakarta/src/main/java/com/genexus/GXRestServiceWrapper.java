package com.genexus;

import com.genexus.servlet.ServletContext;
import com.genexus.servlet.http.HttpServletRequest;
import com.genexus.servlet.http.HttpServletResponse;
import com.genexus.ws.rs.core.UriInfo;
import com.genexus.servlet.IServletContext;
import com.genexus.servlet.http.IHttpServletRequest;
import com.genexus.servlet.http.IHttpServletResponse;
import com.genexus.ws.rs.core.IUriInfo;

import jakarta.ws.rs.core.Context;
import org.springframework.beans.factory.annotation.Autowired;

public class GXRestServiceWrapper {

	@Context
	private jakarta.ws.rs.core.UriInfo uriInfo;
	@Context
	@Autowired
	private jakarta.servlet.ServletContext myContext;
	@Context
	@Autowired
	private jakarta.servlet.http.HttpServletRequest myServletRequest;
	@Context
	@Autowired
	private jakarta.servlet.http.HttpServletResponse myServletResponse;

	protected IHttpServletRequest myServletRequestWrapper;
	protected IHttpServletResponse myServletResponseWrapper;
	protected IServletContext myContextWrapper;
	protected IUriInfo myUriInfoWrapper;

	protected void initWrappedVars() {
		myServletRequestWrapper = new HttpServletRequest(myServletRequest);
		myServletResponseWrapper = new HttpServletResponse(myServletResponse);
		myContextWrapper = new ServletContext(myContext);
		myUriInfoWrapper = new UriInfo(uriInfo);
	}
}
