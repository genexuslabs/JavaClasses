package com.genexus.servlet.http;

import java.io.IOException;
import com.genexus.servlet.IServletOutputStream;

public interface IHttpServletResponseWrapper {

	void addCookie(ICookie cookie);
	IServletOutputStream getWrappedOutputStream() throws IOException;
}
