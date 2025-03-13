package com.genexus.saml20.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

public class Encoding {

	private static final Logger logger = LogManager.getLogger(Encoding.class);

	public static String delfateAndEncodeXmlParameter(String parm) {
		logger.trace("delfateAndEncodeXmlParameter");

		try {
			ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
			Deflater deflater = new Deflater(Deflater.DEFLATED, true);
			DeflaterOutputStream deflaterStream = new DeflaterOutputStream(bytesOut, deflater);
			deflaterStream.write(parm.getBytes(StandardCharsets.UTF_8));
			deflaterStream.finish();

			String base64 = Base64.toBase64String(bytesOut.toByteArray());
			logger.debug(MessageFormat.format("Base64: {0}", base64));
			return URLEncoder.encode(base64, StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			logger.error("delfateAndEncodeXmlParameter", e);
			return "";
		}
	}

	public static String decodeAndInflateXmlParameter(String parm) {
		logger.trace("decodeAndInflateXmlParameter");
		try {
			String base64 = URLDecoder.decode(parm, StandardCharsets.UTF_8.name());
			byte[] bytes = Base64.decode(base64);
			byte[] uncompressedData = new byte[4096];
			Inflater inflater = new Inflater(true);
			inflater.setInput(bytes);
			int len = inflater.inflate(uncompressedData);
			inflater.end();
			return new String(uncompressedData, 0, len, StandardCharsets.UTF_8);
		} catch (Exception e) {
			logger.error("decodeAndInflateXmlParameter", e);
			return "";
		}
	}

	public static String documentToString(Document doc) {
		logger.trace("documentToString");
		try (StringWriter writer = new StringWriter()) {
			DOMSource domSource = new DOMSource(doc);
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			return writer.toString();
		} catch (Exception e) {
			logger.error("documentToString", e);
			return null;
		}
	}
}
