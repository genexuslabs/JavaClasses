package com.genexus.xml.ws.handler;

import com.genexus.xml.ws.WebServiceContext;

public class MessageContext {
	jakarta.xml.ws.handler.MessageContext messageContext;

	public MessageContext(WebServiceContext webServiceContext) {
		this.messageContext = webServiceContext.getMessageContext();
	}

	public Object get(Object key) {
		return messageContext.get(key);
	}

	public String getSERVLET_REQUEST(){
		return jakarta.xml.ws.handler.MessageContext.SERVLET_REQUEST;
	}

	public String getSERVLET_RESPONSE(){
		return jakarta.xml.ws.handler.MessageContext.SERVLET_RESPONSE;
	}

	public String getSERVLET_CONTEXT(){
		return jakarta.xml.ws.handler.MessageContext.SERVLET_CONTEXT;
	}

}
