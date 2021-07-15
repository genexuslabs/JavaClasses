package com.genexus.xml.ws;

import java.util.List;

public class BindingProvider {
	javax.xml.ws.BindingProvider bindingProvider;

	public BindingProvider(Object object) {
		bindingProvider = (javax.xml.ws.BindingProvider)object;
	}

	public static String getENDPOINT_ADDRESS_PROPERTY() {
		return javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
	}

	public javax.xml.ws.soap.SOAPBinding getBinding() {
		return (javax.xml.ws.soap.SOAPBinding)bindingProvider.getBinding();
	}

	public java.util.Map<String,Object> getRequestContext() {
		return bindingProvider.getRequestContext();
	}

	public void addHandlerChain(Object object) {
		List<javax.xml.ws.handler.Handler> handlerList = bindingProvider.getBinding().getHandlerChain();
		handlerList.add((javax.xml.ws.handler.Handler)object);
		bindingProvider.getBinding().setHandlerChain(handlerList);
	}

	public javax.xml.ws.BindingProvider getWrappedClass() {
		return bindingProvider;
	}
}
