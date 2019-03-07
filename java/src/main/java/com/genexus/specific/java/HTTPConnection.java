package com.genexus.specific.java;

import java.applet.Applet;

import com.genexus.common.interfaces.IExtensionHTTPConnection;

import HTTPClient.ProtocolNotSuppException;

@SuppressWarnings("deprecation")
public class HTTPConnection implements IExtensionHTTPConnection {

	

	@Override
	public void createHttpConnectionFromApplet(Object obj, HTTPClient.HTTPConnection httpConnection) throws ProtocolNotSuppException {
		Applet applet = (Applet) obj;
		httpConnection.setInfo(applet.getCodeBase().getProtocol(),
			     applet.getCodeBase().getHost(),
			     applet.getCodeBase().getPort(), null, -1);
		
	}

}
