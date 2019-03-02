package com.genexus.com;

import java.io.IOException;
import java.io.OutputStream;

public interface IHttpResponse {

	OutputStream getOutputStream() throws IOException;

}
