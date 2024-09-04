package com.genexus.common.interfaces;

import HTTPClient.HTTPConnection;
import HTTPClient.ProtocolNotSuppException;

public interface IExtensionHTTPConnection {

	void createHttpConnectionFromApplet(Object applet, HTTPConnection httpConnection) throws ProtocolNotSuppException;

}
