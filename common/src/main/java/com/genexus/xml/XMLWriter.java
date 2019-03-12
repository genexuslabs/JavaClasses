
package com.genexus.xml;

import java.io.*;
import com.genexus.util.NameValuePair;
import com.genexus.CommonUtil;
import java.util.*;

import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;


public class XMLWriter
{
	private static final byte GXSUCCESS = 0;
	private static final byte GXFAIL    = 1;
	private static final byte GXOUTPUT_ERROR = 2;

	private String sCurrValue;
	private String sCurrPrefix;

	private Stack 		stack 		= new Stack();
	private Vector 		attributes 	= new Vector();
	private Hashtable 	namespaces 	= new Hashtable();
	private StringWriter	sWriter = null;

	private int inValue = 0;
	private boolean bOpenValue;
	private boolean bOpenElementHeader;
	private String 	indentChar 		= "\t";
	private String 	errDescription 	= "";
	private short   indentation 	= 1;
	private int 	errCode			= 0;
	private boolean openedWriter = true;
	private boolean simpleElement = false;
	private boolean textFlag = false;
	
	private String encoding = "";
	private Writer out = null;
	private OutputStream stream;

	private int entities = 0;

	public byte getUseentities()
	{
		return (byte) entities;	
	}

	public void setUseentities(int entities)
	{
		this.entities = entities;
	}
		
	public String getEncoding()
	{
		return encoding;
	}
	
	public void setEncoding(String enc)
	{
		encoding = CommonUtil.normalizeEncodingName(enc);
	}
	
	private void checkWriter()
	{
		if (out == null && stream != null)
		{
			try
			{
				out = new OutputStreamWriter(stream, encoding);
			}
			catch (UnsupportedEncodingException e)
			{
				out = new OutputStreamWriter(stream);
			}
		}
	}
	
	public byte xmlEnd()
	{
		while (!stack.empty())
			writeEndElement();
		
		try
		{
			if	(out != null)
			{
				out.close();		
				out = null;
			}
		}
		catch (IOException e)
		{
		}

		return GXSUCCESS;
	}

	public byte xmlStartWeb(String sFileName)
	{
		sWriter = null;
		return GXSUCCESS;
	}

	public byte xmlStart(String sFileName)
	{
		sWriter = null;
		stack.removeAllElements();
		xmlEnd();
		textFlag = false;
		try
		{
			if (sFileName.trim().length() > 0)
				//out = new PrintWriter(new BufferedWriter(new FileWriter(sFileName)));
				stream = new BufferedOutputStream( new FileOutputStream(sFileName));
			else
				//out = new PrintWriter(System.out);
				stream = new FileOutputStream (FileDescriptor.out);
		}
		catch (IOException e)
		{
			return GXFAIL;
		}

		return GXSUCCESS;
	}

	
	public byte openToString()
	{
		sWriter = new StringWriter();
		stack.removeAllElements();
		xmlEnd();
		textFlag = false;
		out = new PrintWriter(sWriter);
		return GXSUCCESS;
	}
	
	public String getResultingString()
	{
		if (sWriter == null)
			return "";
		else
			return sWriter.toString();
	}
	
	public void setOutputStream(OutputStream oStream)
	{
		xmlEnd();
		stream = oStream;
	}
	
	public void setWriter(Writer writer)
	{
		out = writer;
	}

	public byte xmlBeginElement(String sLevelName)
	{
		checkWriter();
		CloseElementHeaderAndValue();
		return OpenElementHeader(sLevelName, "") ? GXSUCCESS:GXFAIL;
	}

	public byte xmlEndElement()
	{
		checkWriter();
		CloseElementHeaderAndValue();

		if (stack.empty())
		{
			errCode = GXFAIL;
			errDescription = "Stack underflow";
			return GXFAIL;
		}
		
		
		NameValuePair node = (NameValuePair) stack.pop();
		
		printIndentation();

		String prefix = (String) namespaces.get(node.value);
		if	(prefix == null)
			prefix = "";
		
		try
		{
			if (StringUtils.isNotEmpty(prefix))
				out.write("</" + prefix + ":" + node.name + ">\n"); 
			else
				out.write("</" + node.name + ">\n"); 
		}
		catch (NullPointerException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		catch (IOException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		inValue = 0;
		return (byte)errCode;
	}

	public byte xmlText(String sTag)
	{
		checkWriter();
		CloseElementHeaderAndValue();
		try
		{
			out.write(CommonUtil.quoteString(sTag, entities != 0, false));
		}
		catch (NullPointerException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		catch (IOException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		return (byte)errCode;
	}

	public byte xmlRaw(String sTag)
	{
		checkWriter();
		CloseElementHeaderAndValue();
		try
		{
			out.write(sTag);
			textFlag = true;
		}
		catch (NullPointerException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		catch (IOException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		return (byte)errCode;
	}

	public byte xmlValue(String sTag, java.util.Date sValue)
	{
		xmlBeginElement(sTag);
		return OpenValue(sValue.toString()) ? GXSUCCESS:GXFAIL;
	}

	public byte xmlValue(String sTag, String sValue)
	{	
		xmlBeginElement(sTag);
		return OpenValue(sValue) ? GXSUCCESS:GXFAIL;
	}

	public byte xmlValue(String sTag, long nValue)
	{	
		xmlBeginElement(sTag);
		return OpenValue(Long.toString(nValue)) ? GXSUCCESS:GXFAIL;
	}

	public byte xmlValue(String sTag, double dValue)
	{	
		xmlBeginElement(sTag);
		return OpenValue(Double.toString(dValue)) ? GXSUCCESS:GXFAIL;
	}
	
	public byte xmlValue(String sTag, BigDecimal dValue)
	{
		return xmlValue(sTag, dValue.doubleValue());
	}

	public byte xmlAtt(String sName, String sValue)
	{
		checkWriter();
		try
		{
			out.write(" " + sName + "=\"" + sValue + "\"");
		}
		catch (NullPointerException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		catch (IOException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		return (byte)errCode;
	}

	public byte xmlAtt(String sName, long nValue)
	{
		checkWriter();
		try
		{
			out.write(" " + sName + "=\"" + nValue+ "\"");
		}
		catch (NullPointerException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		catch (IOException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		return (byte)errCode;
	}
	
	public byte xmlAtt(String sName, double nValue)
	{
		checkWriter();
		try
		{
			out.write(" " + sName + "=\"" + nValue + "\"");
		}
		catch (NullPointerException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		catch (IOException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		return (byte)errCode;		
	}

	public byte xmlAtt(String sName, BigDecimal dValue)
	{
		return xmlAtt(sName, dValue.doubleValue()); 
	}
	
	private boolean OpenValue(String sValue)
	{
		bOpenValue = true;
		sCurrValue = sValue;

		return true;
	}

	private boolean OpenElementHeader(String sLevelName, String sLevelURI)
	{
		CloseElementHeaderAndValue();

		stack.push(new NameValuePair(sLevelName, sLevelURI));
		bOpenElementHeader = true;

		return true;
	}

	private int getCurrentLevel()
	{
		return stack.size();
	}


	private void printIndentation()
	{
		printIndentation(getCurrentLevel());
	}
	
	private void printIndentation(int level)
	{
		try
		{
			if (inValue == 2)
			{
				return;
			}			
			if (inValue == 1)
			{
				inValue = 2;
			}
			if (textFlag) out.write("\n");
			textFlag = false;
			if	(indentation != 0)
			{
				if (level > 0)
					out.write(CommonUtil.replicate(indentChar, level * indentation));
			}
		}
		catch (NullPointerException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		catch (IOException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
	}

	private boolean CloseElementHeaderAndValue()
	{
		if (!bOpenElementHeader)
			return false;

		NameValuePair node = (NameValuePair) stack.peek();
		String prefix = (String) namespaces.get(node.value);
		
		if	(node.value.length() != 0)
		{					
			if	( 	prefix == null || (sCurrPrefix.length() > 0) && !(prefix.equals(sCurrPrefix))	)
			{
				prefix = sCurrPrefix;
				writeAttribute("xmlns" + ((prefix.length() > 0)? (":" + prefix):""), node.value);
			}
		}
		if	(prefix == null)
			prefix = "";

		
		printIndentation(getCurrentLevel() - 1);
		
		try
		{
			out.write("<");
			if	(prefix.length() > 0)
			{
				out.write(prefix);
				out.write(":");
			}
			out.write(node.name);

			for (Enumeration en = attributes.elements(); en.hasMoreElements(); )
			{
				NameValuePair vp = (NameValuePair) en.nextElement();

				out.write(" " + vp.name + "=\"" + CommonUtil.quoteString(vp.value, entities != 0, true) + "\"");
			}
			attributes.removeAllElements();

			if (bOpenValue)
			{
				bOpenValue = false;

				if (IsBlank(sCurrValue))
				{
					out.write("/>\n");
				}
				else
				{
					out.write(">");
					out.write(CommonUtil.quoteString(sCurrValue, entities != 0, false));
					out.write("</");

					if	(prefix.length() > 0)
					{
						out.write(prefix);
						out.write(":");
					}
					out.write(node.name);
					out.write(">\n");
				}

				stack.pop();
			}
			else
			{
				if (inValue == 0)
					out.write(">\n");
				else
					out.write(">");
			}

			bOpenElementHeader = false;
		}
		catch (NullPointerException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		catch (IOException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		return true;
	}

	private boolean IsBlank(String s)
	{
		return (s == null || s.trim().length() == 0);
	}
		
	private StringBuffer openRequestEncoding = null;
	public byte openRequest(com.genexus.internet.HttpClient resp)
	{
		openRequestEncoding = new StringBuffer();
		StringWriter writer = new StringWriter();
		out = new PrintWriter(writer);		
		resp.addStringWriter(writer, openRequestEncoding);

		return GXSUCCESS;
	}

	public byte openResponse(com.genexus.com.IHttpResponse resp)
	{
		sWriter = null;
		stack.removeAllElements();
		xmlEnd();
		textFlag = false;
		try
		{	
			stream = resp.getOutputStream();
		}
		catch (IOException e)
		{
			return GXFAIL;
		}

		return GXSUCCESS;
	}
	
	public byte openResponse(OutputStream s)
	{
		sWriter = null;
		stack.removeAllElements();
		xmlEnd();
		textFlag = false;
		setOutputStream(s);
		return GXSUCCESS;
	}

	public byte openResponse(PrintWriter writer)
	{
		this.out = writer;
		return GXSUCCESS;
	}
	public byte openResponse(String sFileName)
	{
		return GXSUCCESS;
	}

	// Nueva API

	// Properties
	public void setIndentChar(String indentChar)
	{
		this.indentChar = indentChar;
	}

	public String getIndentChar()
	{
		return indentChar;
	}

	public void setIndentation(int indentation)
	{
		this.indentation = (short) indentation;
	}

	public short getIndentation()
	{
		return indentation;
	}

	public short getErrCode()
	{
		return (short) errCode;
	}
	
	public String getErrDescription()
	{
		return errDescription;
	}

	public byte openURL(String sFileName)
	{
		return (byte) xmlStart(sFileName);
	}

	public byte writeStartElement(String sLevelName)
	{
		return xmlBeginElement(sLevelName);
	}
	
	public void writeNSStartElement(String sLevelName)
	{
		writeNSStartElement(sLevelName, "", "");
	}

	public void writeNSStartElement(String sLevelName, String sPrefix, String sURI)
	{
		checkWriter();
		CloseElementHeaderAndValue();
		OpenElementHeader(sLevelName, sURI);
		sCurrPrefix = sPrefix;
	}

	public byte writeEndElement()
	{
		return xmlEndElement();
	}

	public void writeComment(String comment)
	{
		checkWriter();
		CloseElementHeaderAndValue();
		printIndentation();
		try
		{
  			out.write("<!--" + comment + "-->\n");
		}
		catch (NullPointerException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		catch (IOException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
	}

	public void writeProcessingInstruction(String pi)
	{
		checkWriter();
		CloseElementHeaderAndValue();
		printIndentation();
		try
		{
  			out.write("<?" + pi + "?>\n");
		}
		catch (NullPointerException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		catch (IOException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
	}

	public void writeProcessingInstruction(String pi, String value)
	{
		checkWriter();
		CloseElementHeaderAndValue();
		printIndentation();
		try
		{
  			out.write("<?" + pi + " " + value + "?>\n");
		}
		catch (NullPointerException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		catch (IOException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
	}

	public void writeDocType(String name)
	{
		writeDocType(name, "");
	}

	public void writeDocType(String name, String subset)
	{
		checkWriter();
		CloseElementHeaderAndValue();
		try
		{
			if (subset.trim().isEmpty())
				out.write("<!DOCTYPE " + name.trim() + ">\n");
			else
				out.write("<!DOCTYPE " + name.trim() + " [" + subset + "]>\n");
		}
		catch (NullPointerException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		catch (IOException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
	}

	public void writeDocTypeSystem(String name, String uri)
	{
		writeDocTypeSystem(name, uri, "");
	}

	public void writeDocTypeSystem(String name, String uri, String subset)
	{
		checkWriter();
		CloseElementHeaderAndValue();
		try
		{
			if (subset.trim().isEmpty())
				out.write("<!DOCTYPE " + name.trim() + " SYSTEM " + " \"" + uri + "\">\n");
			else
				out.write("<!DOCTYPE " + name.trim() + " SYSTEM " + " \"" + uri + "\" [" + subset + "]>\n");
		}
		catch (NullPointerException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		catch (IOException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
	}

	public void writeDocTypePublic(String name, String pubid, String uri)
	{
		writeDocTypePublic(name, pubid, uri, "");
	}

	public void writeDocTypePublic(String name, String pubid, String uri, String subset)
	{
		checkWriter();
		CloseElementHeaderAndValue();
		try
		{
			if (subset.trim().isEmpty())
				out.write("<!DOCTYPE " + name.trim() + " PUBLIC \"" + pubid + "\" \"" + uri + "\">\n");
			else
				out.write("<!DOCTYPE " + name.trim() + " PUBLIC \"" + pubid + "\" \"" + uri + "\" [" + subset + "]>\n");
		}
		catch (NullPointerException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		catch (IOException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
	}

	public void writeEntityReference(String value)
	{
		checkWriter();
		CloseElementHeaderAndValue();
		try
		{
  			out.write("&" + value.trim() + ";");
		}
		catch (NullPointerException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		catch (IOException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
	}

	public void writeCData(String data)
	{
		checkWriter();
		CloseElementHeaderAndValue();
		printIndentation();
		try
		{
  			out.write("<![CDATA[" + data + "]]>\n");
		}
		catch (NullPointerException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		catch (IOException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
	}

	public byte writeElement(String sLevelName)
	{
		return writeElement(sLevelName, "");
	}

	public byte writeElement(String sName, String sValue)
	{		
		CloseElementHeaderAndValue();
		simpleElement = true;
		xmlValue(sName, sValue);
		return 0;
	}
	
	public byte writeElement(String sName, long sValue)
	{
		return writeElement(sName, Long.toString(sValue));
	}

	public byte writeElement(String sName, double sValue)
	{
		return writeElement(sName, Double.toString(sValue));
	}
	
	public byte writeElement(String sName, BigDecimal dValue)
	{
		return writeElement(sName, dValue.doubleValue());
	}

	public void writeNSElement(String sLevelName)
	{
		writeNSElement(sLevelName, "", "");
	}

	public void writeNSElement(String sLevelName, String sURI)
	{
		writeNSElement(sLevelName, sURI, "");
	}

	public void writeNSElement(String sLevelName, String sURI, String sValue)
	{
		writeNSStartElement(sLevelName, "", sURI);
		OpenValue(sValue);
	}

	public byte writeAttribute(String name, String value)
	{
		if (bOpenElementHeader || bOpenValue)
		{
			attributes.addElement(new NameValuePair(name, value));

			String prefix ;
			if	( (prefix = getNamespace(name)) != null)
			{
				namespaces.put(value.trim(), prefix);
			}
		}
		else
		{
			errCode = GXFAIL;
		}

		return (byte) errCode;
	}

	public String getNamespace(String nm)
	{
		nm = nm.trim();
		if	(nm.startsWith("xmlns:"))
		{
			return nm.substring(6);
		}

		return null;
	}

	public byte writeAttribute(String sName, long sValue)
	{
		return writeAttribute(sName, Long.toString(sValue));
	}

	public byte writeAttribute(String sName, double sValue)
	{
		return writeAttribute(sName, Double.toString(sValue));
	}
	
	public byte writeAttribute(String sName, BigDecimal dValue)
	{
		return writeAttribute(sName, dValue.doubleValue());
	}

	public byte writeText(String sTag)
	{
		inValue = 1;		
		CloseElementHeaderAndValue();
		return xmlText(sTag);
	}

	public byte writeRawText(String sTag)
	{
		CloseElementHeaderAndValue();
		return xmlRaw(sTag);
	}

	public void writeStartDocument()
	{
		writeStartDocument(encoding, 0);
	}

	public void writeStartDocument(String encod)
	{
		writeStartDocument(encod, 0);
	}

	public void writeStartDocument(String encod, int standalone)
	{		
		setEncoding(encod);
		if(openRequestEncoding != null)
		{ //@hack: para setear el encoding en el caso de usar openRequest
			openRequestEncoding.setLength(0);
			openRequestEncoding.append(encoding);			
		}
		checkWriter();
		try
		{
			String cannonicalName;
			try
			{
				CommonUtil.normalizeSupportedEncodingName(encod);
				cannonicalName = CommonUtil.ianaEncodingName(encod.trim());
			}catch(Throwable e2) 
			{
				 cannonicalName = CommonUtil.normalizeEncodingName(encod); //si java no soporta ese encoding entonces se graba con el default
			;}

			// @hack: Tenemos que escribir el header pero SIN usar ningun encoding, incluyendo
			// el que est� por default, porque puede codificar distinto al UTF, por lo tanto
			// iteramos y escribimos directamente cada byte
			// Nota: no podemos hacer un "xxx".getBytes(), porque parece que utiliza el 
			// encoding por defecto, que dijimos que tampoco podemos usar
			String temp = "<?xml version=\"1.0\" encoding=\"" + cannonicalName + "\""  + ((standalone != 0)? " standalone=\"yes\"":"") + "?>\n";
			if(stream != null)
			{
				for(int i = 0; i < temp.length(); i++)
				{
					stream.write((byte)temp.charAt(i));
				}
			}
			else
			{
				for(int i = 0; i < temp.length(); i++)
				{
					out.write((byte)temp.charAt(i));
				}
			}
		}
		catch (NullPointerException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
		catch (IOException e)
		{
			errCode = GXOUTPUT_ERROR;
		}
	}

	public byte close()
	{
		xmlEnd();

		return GXSUCCESS;
	}
	
}

