package com.genexus.ws;

import java.util.Set;
import java.util.HashSet;
import java.util.Properties;
import javax.xml.namespace.QName;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import jakarta.xml.soap.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.WSSecEncrypt;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecTimestamp;
import org.w3c.dom.*;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import com.genexus.internet.Location;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.common.interfaces.*;

public class GXHandlerConsumerChain implements SOAPHandler<SOAPMessageContext>
{
	public static final ILogger logger = LogManager.getLogger(GXHandlerConsumerChain.class);
	private Location location;
	private String soapHeaderRaw;

	//        private static final String WSSECURITY_ADDRESSING_URL = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
	private static final String WSSECURITY_ADDRESSING_URL = "http://www.w3.org/2005/08/addressing";

	public Set<QName> getHeaders()
	{
		final QName securityHeader = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security", "wsse");
		final QName actionAddressingHeader = new QName(WSSECURITY_ADDRESSING_URL, "Action");
		final QName messageAddressingHeader = new QName(WSSECURITY_ADDRESSING_URL, "MessageID");
		final QName fromAddressingHeader = new QName(WSSECURITY_ADDRESSING_URL, "From");
		final QName toAddressingHeader = new QName(WSSECURITY_ADDRESSING_URL, "To");

		final HashSet<QName> headers = new HashSet<>();
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
			SOAPMessage message = messageContext.getMessage();
			SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
			SOAPHeader header = envelope.getHeader();

			//soapHeadersRaw
			if (Boolean.TRUE.equals(outboundProperty) && soapHeaderRaw != null)
			{
				Document doc = parseXML(soapHeaderRaw);
				header.detachNode();
				SOAPHeader sh = envelope.addHeader();
				org.w3c.dom.Node node = sh.getOwnerDocument().importNode(doc.getDocumentElement(), true);
				sh.appendChild(node);
				message.saveChanges();
			}

			//ws-addressing
			IGXWSAddressing wsAddressing = location.getWSAddressing();
			if (wsAddressing != null && Boolean.TRUE.equals(outboundProperty) && soapHeaderRaw == null && !wsAddressing.getMessageID().isEmpty())
			{
				header.addNamespaceDeclaration("wsa", WSSECURITY_ADDRESSING_URL);

				//wsa:Action
				if (!wsAddressing.getAction().isEmpty())
				{
					SOAPHeaderElement actionElement = header.addHeaderElement(header.createQName("Action", "wsa"));
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

			//ws-security
			IGXWSSignature wsSignature = null;
			IGXWSEncryption wsEncryption = null;
			int expirationTimeout = 0;
			if (location.getWSSecurity() != null) {
				wsSignature = location.getWSSecurity().getSignature();
				wsEncryption = location.getWSSecurity().getEncryption();
				expirationTimeout = location.getWSSecurity().getExpirationTimeout();
			}

			if (Boolean.TRUE.equals(outboundProperty) && soapHeaderRaw == null && ((wsSignature != null && !wsSignature.getAlias().isEmpty()) || (wsEncryption != null && !wsEncryption.getAlias().isEmpty())))
			{
				Document doc = messageToDocument(messageContext.getMessage());

				//Security header
				WSSecHeader secHeader = new WSSecHeader();
				secHeader.insertSecurityHeader(doc);
				Document signedDoc = null;

				//Signature
				if (!wsSignature.getAlias().isEmpty())
				{
					Properties signatureProperties = new Properties();
					signatureProperties.put("org.apache.ws.security.crypto.merlin.keystore.type", wsSignature.getKeystore().getType());
					signatureProperties.put("org.apache.ws.security.crypto.merlin.keystore.password", wsSignature.getKeystore().getPassword());
					signatureProperties.put("org.apache.ws.security.crypto.merlin.file", wsSignature.getKeystore().getSource());
					Crypto signatureCrypto = CryptoFactory.getInstance(signatureProperties);
					WSSecSignature sign = new WSSecSignature();
					sign.setKeyIdentifierType(wsSignature.getKeyIdentifierType());
					sign.setUserInfo(wsSignature.getAlias(), wsSignature.getKeystore().getPassword());
					signedDoc = sign.build(doc, signatureCrypto, secHeader);

					if (expirationTimeout > 0)
					{
						WSSecTimestamp timestamp = new WSSecTimestamp();
						timestamp.setTimeToLive(expirationTimeout);
						signedDoc = timestamp.build(signedDoc, secHeader);
					}
				}

				//Encryption
				if (!wsEncryption.getAlias().isEmpty())
				{
					Properties encryptionProperties = new Properties();
					encryptionProperties.put("org.apache.ws.security.crypto.merlin.keystore.type", wsEncryption.getKeystore().getType());
					encryptionProperties.put("org.apache.ws.security.crypto.merlin.keystore.password", wsEncryption.getKeystore().getPassword());
					encryptionProperties.put("org.apache.ws.security.crypto.merlin.file", wsEncryption.getKeystore().getSource());
					Crypto encryptionCrypto = CryptoFactory.getInstance(encryptionProperties);
					WSSecEncrypt builder = new WSSecEncrypt();
					builder.setUserInfo(wsEncryption.getAlias(), wsEncryption.getKeystore().getPassword());
					builder.setKeyIdentifierType(wsEncryption.getKeyIdentifierType());
					if (signedDoc == null)
					{
						signedDoc = doc;
					}
					builder.build(signedDoc, encryptionCrypto, secHeader);
				}

				Document securityDoc = doc;
				DOMSource domSource = new DOMSource(securityDoc);
				message.getSOAPPart().setContent(domSource);

				message.saveChanges();
			}

			messageContext.getMessage().writeTo(out);
			String messageBody = new String(out.toByteArray(), "utf-8");
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

	public void setSoapheadersraw(Location location, String soapHeaderRaw)
	{
		this.location = location;
		this.soapHeaderRaw = soapHeaderRaw;
	}

	private void processEndPoint(SOAPHeader header, String elementName, IGXWSAddressingEndPoint endpoint) throws Exception
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

	private Document messageToDocument(SOAPMessage message) throws SOAPException, TransformerException
	{
		Source src = message.getSOAPPart().getContent();
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		DOMResult result = new DOMResult();
		transformer.transform(src, result);
		return (Document) result.getNode();
	}
}