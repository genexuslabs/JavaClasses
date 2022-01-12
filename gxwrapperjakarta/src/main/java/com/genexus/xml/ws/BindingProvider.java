package com.genexus.xml.ws;

import java.util.List;

public class BindingProvider {
	jakarta.xml.ws.BindingProvider bindingProvider;

	public BindingProvider(Object object) {
		bindingProvider = (jakarta.xml.ws.BindingProvider)object;
	}

	public static String getENDPOINT_ADDRESS_PROPERTY() {
		return jakarta.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
	}

	public jakarta.xml.ws.soap.SOAPBinding getBinding() {
		return (jakarta.xml.ws.soap.SOAPBinding)bindingProvider.getBinding();
	}

	public java.util.Map<String,Object> getRequestContext() {
		return bindingProvider.getRequestContext();
	}

	public void addHandlerChain(Object object) {
		List<jakarta.xml.ws.handler.Handler> handlerList = bindingProvider.getBinding().getHandlerChain();
		handlerList.add((jakarta.xml.ws.handler.Handler)object);
		bindingProvider.getBinding().setHandlerChain(handlerList);
	}

	public jakarta.xml.ws.BindingProvider getWrappedClass() {
		return bindingProvider;
	}

	public Class getBindingClass() {
		try {
			return Class.forName("jakarta.xml.ws.BindingProvider");
		}
		catch (Exception e) {
			return null;
		}
	}
}
