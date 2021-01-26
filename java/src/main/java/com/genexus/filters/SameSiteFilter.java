package com.genexus.filters;

import com.genexus.Application;
import com.genexus.ApplicationContext;

import java.io.IOException;
import java.util.Collection;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

public class SameSiteFilter implements Filter {
	String sameSiteMode;
	private static final String SameSiteNone = "None";
	private static final String SameSiteLax = "Lax";
	private static final String SameSiteStrict = "Strict";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		chain.doFilter(request, response);
		if (!sameSiteMode.equals("")){
			addSameSiteCookieAttribute((HttpServletResponse) response);
		}
	}
	private void addSameSiteCookieAttribute(HttpServletResponse response) {
		Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);
		boolean firstHeader = true;
		for (String header : headers) {
			if (firstHeader) {
				response.setHeader(HttpHeaders.SET_COOKIE, String.format("%s; %s", header, "SameSite="+sameSiteMode));
				firstHeader = false;
				continue;
			}
			response.addHeader(HttpHeaders.SET_COOKIE, String.format("%s; %s", header, "SameSite="+sameSiteMode));
		}
	}
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String sameSiteModeValue=Application.getClientPreferences().getProperty("SAMESITE_COOKIE", "");
		if (sameSiteModeValue.equals(SameSiteNone) || sameSiteModeValue.equals(SameSiteLax)||sameSiteModeValue.equals(SameSiteStrict)) {
			sameSiteMode = sameSiteModeValue;
		}
	}
	@Override
	public void destroy() {
	}
}