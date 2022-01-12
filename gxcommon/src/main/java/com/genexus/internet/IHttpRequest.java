package com.genexus.internet;

import java.io.IOException;
import java.io.InputStream;

public interface IHttpRequest {

	InputStream getInputStream() throws IOException;

}
