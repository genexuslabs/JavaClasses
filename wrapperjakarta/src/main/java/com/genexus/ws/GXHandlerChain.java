package com.genexus.ws;

import java.util.Set;
import java.util.Collections;
import javax.xml.namespace.QName;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import com.genexus.servlet.IServletContext;
import com.genexus.servlet.ServletContext;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.specific.java.LogManager;


public class GXHandlerChain implements SOAPHandler<SOAPMessageContext> {
    private static ILogger logger = null;
    public static final String GX_SOAP_BODY = "GXSoapBody";

    public Set<QName> getHeaders() {
        return Collections.emptySet();
    }

    public boolean handleMessage(SOAPMessageContext messageContext) {
        initialize(messageContext);
        Boolean outboundProperty = (Boolean) messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        ;
        try {
			if (logger.isDebugEnabled()) {
				messageContext.getMessage().writeTo(out);
				String messageBody = new String(out.toByteArray(), "utf-8");
				if (Boolean.FALSE.equals(outboundProperty)) {
					messageContext.put(GX_SOAP_BODY, messageBody);
					messageContext.setScope(GX_SOAP_BODY, MessageContext.Scope.APPLICATION);
				}
				logger.debug(messageBody);
			}
        } catch (Exception e) {
            logger.error("Exception in handler: ", e);
        }
        return true;
    }

    private void initialize(SOAPMessageContext messageContext) {
        if (logger == null) {
            IServletContext servletContext = new ServletContext(messageContext.get(MessageContext.SERVLET_CONTEXT));
            logger = LogManager.initialize(servletContext.getRealPath("/"), GXHandlerChain.class);
        }
    }

    public boolean handleFault(SOAPMessageContext messageContext) {
        return true;
    }

    public void close(MessageContext messageContext) {
    }
}