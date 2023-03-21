package com.genexus.cryptography;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.genexus.diagnostics.core.ILogger;
import org.w3c.dom.Document;

public class Utils {
	public static void logError(String msg) {
		System.err.println(msg);
	}

	@SuppressWarnings("serial")
	private static Hashtable<String, String> _hashAlgorithms = new Hashtable<String, String>() {
		{
			put("MD5", "MD5");
			put("SHA1", "SHA-1");
			put("SHA256", "SHA-256");
			put("SHA384", "SHA-384");
			put("SHA512", "SHA-512");
                        
                        put("HMACMD5", "HmacMD5");
                        put("HMACSHA1", "HmacSHA1");                        
                        put("HMACSHA256", "HmacSHA256");
                        put("HMACSHA384", "HmacSHA384");
                        put("HMACSHA512", "HmacSHA512");
		}
	};
		
	public static void logError(Exception e) {
		System.err.println(e.getMessage());
	}

	public static void logError(String msg, Exception e) {
		System.err.println(String.format("%s - %s", msg, e.getMessage()));
	}

	public static String mapAlgorithm(String gxAlgorithm, Hashtable<String, String> _hashAlgorithms2) {
		String alg = gxAlgorithm;
		if (_hashAlgorithms2.containsKey(gxAlgorithm)) {
			alg = _hashAlgorithms2.get(gxAlgorithm);
		}
		return alg;
	}

	public static String mapHashAlgorithm(String gxAlgorithm) {
		return mapAlgorithm(gxAlgorithm, _hashAlgorithms);
	}

	public static Document documentFromString(String xml, boolean ignoreWhitespace)
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setIgnoringElementContentWhitespace(ignoreWhitespace);
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder(); 
			return builder.parse(new ByteArrayInputStream(xml.getBytes("UTF8")));
		}
		catch (Exception e)
		{
			logError(e);
		}
		return null;
	}

	public static String serialize(Document doc) throws IOException {
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();				
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer trans = tf.newTransformer();
			trans.setOutputProperty("omit-xml-declaration", "yes");
			trans.transform(new DOMSource(doc), new StreamResult(out));		       
			return new String( out.toByteArray(), "UTF8" );
		}
		catch (Exception e)
		{
			logError(e);
		}
		return "";
	}
    
    public static String toHexString(byte[] byteData){
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

	public enum PKCSStandard {
		PKCS1, PKCS7
	};


}
