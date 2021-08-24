package com.genexus.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

@Provider
public class RestReaderInterceptor implements ReaderInterceptor {

	@Override
	public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
		InputStream is = context.getInputStream();

		String body = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
		requestBodyThreadLocal.set(body);

		InputStream isBody = IOUtils.toInputStream(body, "UTF-8");
		
		context.setInputStream(isBody);
		return context.proceed();
	}

	public static ThreadLocal<String> requestBodyThreadLocal = new ThreadLocal<String>();

}