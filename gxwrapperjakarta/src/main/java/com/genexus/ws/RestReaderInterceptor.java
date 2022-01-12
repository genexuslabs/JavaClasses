package com.genexus.ws;

import java.io.IOException;
import java.io.InputStream;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;

@Provider
public class RestReaderInterceptor implements ReaderInterceptor {

	@Override
	public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
		InputStream is = context.getInputStream();

		InputStream isBody = com.genexus.WrapperUtils.storeRestRequestBody(is);

		context.setInputStream(isBody);
		return context.proceed();
	}
}
