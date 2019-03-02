package com.genexus.ws;

import java.util.Set;
import java.util.Collections;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

public class GXHandlerChain implements SOAPHandler<SOAPMessageContext>
{
	public static final ILogger logger = LogManager.getLogger(GXHandlerChain.class);
	public static final String GX_SOAP_BODY = "GXSoapBody";
	
  public Set<QName> getHeaders()
  {
    return Collections.emptySet();
  }
  
  public boolean handleMessage(SOAPMessageContext messageContext)
  {
			Boolean outboundProperty = (Boolean) messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
  		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();;
  		try
  		{
  			messageContext.getMessage().writeTo(out);
  			String messageBody = new String(out.toByteArray(), "utf-8"); 
				if (Boolean.FALSE.equals(outboundProperty)) 
				{  				
  				messageContext.put(GX_SOAP_BODY, messageBody);
  				messageContext.setScope(GX_SOAP_BODY, MessageContext.Scope.APPLICATION);
  			}
  			
				logger.debug(messageBody);
  		}
			catch (Exception e) 
			{
					logger.error("Exception in handler: ", e);
			}				
      return true;
  }
  
  public boolean handleFault(SOAPMessageContext messageContext)
  {
    return true;
  }
  
  public void close(MessageContext messageContext)
  {
  }
}