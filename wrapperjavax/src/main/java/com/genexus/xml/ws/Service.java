package com.genexus.xml.ws;

import java.net.URL;
import javax.xml.namespace.QName;

public class Service {
	javax.xml.ws.Service service;

	public Service(URL url, QName qname) {
		service = javax.xml.ws.Service.create(url, qname);
	}

	public javax.xml.ws.Service getWrappedClass() {
		return service;
	}
}
