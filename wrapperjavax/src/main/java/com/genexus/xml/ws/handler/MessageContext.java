package com.genexus.xml.ws.handler;

import com.genexus.xml.ws.WebServiceContext;

public class MessageContext {
	javax.xml.ws.handler.MessageContext messageContext;

	public MessageContext(WebServiceContext webServiceContext) {
		this.messageContext = webServiceContext.getMessageContext();
	}

	public Object get(Object key) {
		return messageContext.get(key);
	}

	public String getSERVLET_REQUEST(){
		return javax.xml.ws.handler.MessageContext.SERVLET_REQUEST;
	}

	public String getSERVLET_RESPONSE(){
		return javax.xml.ws.handler.MessageContext.SERVLET_RESPONSE;
	}

	public String getSERVLET_CONTEXT(){
		return javax.xml.ws.handler.MessageContext.SERVLET_CONTEXT;
	}

}
