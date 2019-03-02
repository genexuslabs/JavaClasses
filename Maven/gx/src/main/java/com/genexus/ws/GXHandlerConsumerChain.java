package com.genexus.ws;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.soap.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import com.genexus.ws.GXWSAddressing;
import com.genexus.ws.GXWSAddressingEndPoint;
import com.genexus.internet.Location;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

public class GXHandlerConsumerChain implements SOAPHandler<SOAPMessageContext>
{
	public static final ILogger logger = LogManager.getLogger(GXHandlerConsumerChain.class);
	private Location location;
	private String soapHeaderRaw;
	
  public Set<QName> getHeaders()
  {
  	final QName securityHeader = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security", "wsse");
  	final QName actionAddressingHeader = new QName("http://schemas.xmlsoap.org/ws/2004/08/addressing", "Action");
  	final QName messageAddressingHeader = new QName("http://schemas.xmlsoap.org/ws/2004/08/addressing", "MessageID");
  	final QName fromAddressingHeader = new QName("http://schemas.xmlsoap.org/ws/2004/08/addressing", "From");
  	final QName toAddressingHeader = new QName("http://schemas.xmlsoap.org/ws/2004/08/addressing", "To");
  	
  	final HashSet headers = new HashSet();
  	headers.add(securityHeader);
  	headers.add(actionAddressingHeader);
  	headers.add(messageAddressingHeader);
  	headers.add(fromAddressingHeader);
  	headers.add(toAddressingHeader);
 
		return headers;
  }
  
  public boolean handleMessage(SOAPMessageContext messageContext)
  {
		Boolean outboundProperty = (Boolean) messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
  		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();;
  		try
  		{
				if (Boolean.TRUE.equals(outboundProperty) && soapHeaderRaw != null)
				{
					Document doc = parseXML(soapHeaderRaw); 
					
					SOAPMessage msg = messageContext.getMessage();
					SOAPEnvelope env = msg.getSOAPPart().getEnvelope();
					SOAPHeader hdr = env.getHeader();
					hdr.detachNode();
      		SOAPHeader sh = env.addHeader();
      		org.w3c.dom.Node node = sh.getOwnerDocument().importNode(doc.getDocumentElement(), true);
      		sh.appendChild(node);
      		msg.saveChanges();
  			}
  			
  	/*		GXWSAddressing wsAddressing = location.getWSAddressing();
			if (Boolean.TRUE.equals(outboundProperty) && soapHeaderRaw == null && !wsAddressing.getMessageID().isEmpty())
			{
				SOAPMessage message = messageContext.getMessage();
				SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
				SOAPHeader header = envelope.getHeader();
				header.addNamespaceDeclaration("wsa", "http://schemas.xmlsoap.org/ws/2004/08/addressing");
				
				//wsa:Action
				if (!wsAddressing.getAction().isEmpty())
				{
					SOAPHeaderElement actionElement = header.addHeaderElement(header.createQName("Action", "wsa"));
					//actionElement.setMustUnderstand(true);
					actionElement.addTextNode(wsAddressing.getAction());
				}
				
				//wsa:MessageID
				if (!wsAddressing.getMessageID().isEmpty())
				{					
					header.addHeaderElement(header.createQName("MessageID", "wsa")).addTextNode(wsAddressing.getMessageID());
				}
				
				//wsa:To
				if (!wsAddressing.getTo().isEmpty())
				{					
					SOAPHeaderElement toElement = header.addHeaderElement(header.createQName("To", "wsa"));
					//toElement.setMustUnderstand(true);
					toElement.addTextNode(wsAddressing.getTo());
				}					
				
				//wsa:From
				if (!wsAddressing.getFrom().getAddress().isEmpty())
				{
					processEndPoint(header, "From", wsAddressing.getFrom());						
				}
				
				//wsa:ReplyTo
				if (!wsAddressing.getReplyTo().getAddress().isEmpty())
				{
					processEndPoint(header, "ReplyTo", wsAddressing.getReplyTo());					
				}					
				
				//wsa:FaultTo
				if (!wsAddressing.getFaultTo().getAddress().isEmpty())
				{	
					processEndPoint(header, "FaultTo", wsAddressing.getFaultTo());				
				}
				
				message.saveChanges();
			}  			
  			
  			messageContext.getMessage().writeTo(out);
  			String messageBody = new String(out.toByteArray(), "utf-8");
				logger.debug(messageBody);
  		*/}
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
  
	public void setSoapheadersraw(Location location, String soapHeaderRaw)
	{
		this.location = location;
		this.soapHeaderRaw = soapHeaderRaw;
	}
	
	private void processEndPoint(SOAPHeader header, String elementName, GXWSAddressingEndPoint endpoint) throws Exception
	{
		//wsa:Address
		SOAPHeaderElement fromElement = header.addHeaderElement(header.createQName(elementName, "wsa"));
		fromElement.addChildElement(header.createQName("Address", "wsa")).addTextNode(endpoint.getAddress());
	
		//wsa:PortType
		if (!endpoint.getPortType().isEmpty())
		{
			fromElement.addChildElement(header.createQName("PortType", "wsa")).addTextNode(endpoint.getPortType());
		}
		
		//wsa:ServiceName
		if (!endpoint.getServiceName().isEmpty())
		{
			fromElement.addChildElement(header.createQName("ServiceName", "wsa")).addTextNode(endpoint.getServiceName());						
		}
	
		//wsa:ReferenceParameters
		if (!endpoint.getParameters().isEmpty())
		{		
			SOAPElement parametersElement = fromElement.addChildElement(header.createQName("ReferenceParameters", "wsa")); 
			Document doc = parseXML(endpoint.getParameters());
			org.w3c.dom.Node importedNode = parametersElement.getOwnerDocument().importNode(doc.getDocumentElement(), true);
			parametersElement.appendChild(importedNode);
		}
		
		//wsa:ReferenceProperties
		if (!endpoint.getProperties().isEmpty())
		{		
			SOAPElement parametersElement = fromElement.addChildElement(header.createQName("ReferenceProperties", "wsa")); 
			Document doc = parseXML(endpoint.getProperties());
			org.w3c.dom.Node importedNode = parametersElement.getOwnerDocument().importNode(doc.getDocumentElement(), true);
			parametersElement.appendChild(importedNode);
		}
	}
	
	
	private Document parseXML(String xml) throws Exception
	{
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		builderFactory.setNamespaceAware(true);
		InputStream stream  = new ByteArrayInputStream(xml.getBytes());
		return builderFactory.newDocumentBuilder().parse(stream);		
	}
}