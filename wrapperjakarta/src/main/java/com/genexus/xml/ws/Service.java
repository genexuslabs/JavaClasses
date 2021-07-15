package com.genexus.xml.ws;

import java.net.URL;
import javax.xml.namespace.QName;

public class Service {
	jakarta.xml.ws.Service service;

	public Service(URL url, QName qname) {
		service = jakarta.xml.ws.Service.create(url, qname);
	}

	public jakarta.xml.ws.Service getWrappedClass() {
		return service;
	}
}
