// $Log: XSLT.java,v $
// Revision 1.2  2005/05/10 18:58:56  gusbro
// Release inicial
//
//

package com.genexus.util;
import java.io.*;

public class XSLT
{
	/** Aplica un xslt a un xml a partir de 2 strings
	 * @parm xml String conteniendo el xml
	 * @parm xsltFile String con el nombre del archivo que contiene el xslt
	 * @return String con la aplicacion del xslt al xml
	 */
	public static String XSLTApply(String xml, String xsltFile)
	{
		try
		{
			javax.xml.transform.TransformerFactory transformerFactory = javax.xml.transform.TransformerFactory.newInstance();
			javax.xml.transform.Transformer transformer = transformerFactory.newTransformer(new javax.xml.transform.stream.StreamSource(xsltFile));
			StringWriter writer = new StringWriter();
			transformer.transform(new javax.xml.transform.stream.StreamSource(new StringReader(xml)), new javax.xml.transform.stream.StreamResult(new BufferedWriter(writer)));
			writer.close();
			return writer.toString();
		}catch(java.lang.NoClassDefFoundError cnfe)
		{
			System.err.println("Xalan was not found in classpath: " + cnfe.toString());
		}catch(Exception e)
		{
			 System.err.println(e.toString());
		}
		return "";
	}
	
	/** Aplica un xslt a un xml a partir de 2 files
	 * @parm xmlFile String conteniendo el nombre del archivo que tiene el xml
	 * @parm xslt String conteniendo el nombre del archivo que tiene el xslt
	 * @return String con la aplicacion del xslt al xml
	 */
	public static String XSLTApplyFromFiles(String xmlFile, String xsltFile) throws Exception
	{
		javax.xml.transform.TransformerFactory transformerFactory = javax.xml.transform.TransformerFactory.newInstance();
		javax.xml.transform.Transformer transformer = transformerFactory.newTransformer(new javax.xml.transform.stream.StreamSource(xsltFile));
		StringWriter writer = new StringWriter();
		transformer.transform(new javax.xml.transform.stream.StreamSource(xmlFile), new javax.xml.transform.stream.StreamResult(new BufferedWriter(writer)));
		writer.close();
		return writer.toString();
	}
}

