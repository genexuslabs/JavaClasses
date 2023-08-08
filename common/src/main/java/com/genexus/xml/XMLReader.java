

package com.genexus.xml;

import java.io.*;
import java.util.*;

import com.genexus.ApplicationContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLPullParserConfiguration;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.parsers.StandardParserConfiguration;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.parser.XMLDTDSource;

import com.genexus.util.*;
import com.genexus.ResourceReader;
import com.genexus.internet.IHttpRequest;
import com.genexus.CommonUtil;
import org.springframework.core.io.ClassPathResource;

public class XMLReader implements XMLDocumentHandler, XMLErrorHandler, XMLDTDHandler
{
	/* Constants */
	private final int ERROR_FILE_NOT_FOUND = 1;
	private final int ERROR_TYPE_MISMATCH = 2;
	private final int ERROR_IO = 3;
	private final int ERROR_MEM = 4;
	private final int ERROR_PARSING = 5;
	
	public final int ValidationNone				= 0;
	public final int ValidationAuto				= 1;
	public final int ValidationDTD				= 2;
	public final int ValidationSchema			= 3;
	public final int ValidationXDR				= 4;
	
	/* Data */
	
	//current node
	Node node;
	
    protected StandardParserConfiguration parserConfiguration;
	private NodesQueue nodesQueue;
	private XMLInputSource inputSource;
	private Pool elementsPool = new Pool();
	private Pool endtagsPool = new Pool();
	private Pool valuedNodesPool = new Pool();
	private Pool pinstructionsPool = new Pool();
	Hashtable<String, EntityDeclaration> entities = new Hashtable<>();
	private StringBuffer normalizationBuffer = new StringBuffer();
	
	
	private StringBuffer textBuffer = new StringBuffer(1024);
	private boolean pendingText = false;
	private boolean inCDATA = false;
	
	private int errCode;
	private String errDescription = "";
	private int errLineNumber = 0;
	private int errLinePos = 0;
	private boolean eof;
	
	private int simpleElements = 1;
	private int nextSimpleElements = 1;
	private boolean UTF8NodeEncoding = false;
	private int removeWhiteNodes = 1;
	private int removeWhiteSpaces = 1;
	private int linesNormalization = 1;
	private String documentEncoding = "";
	private int validationType = ValidationNone;
	private int readExternalEntities = 1;
	
	private boolean inDocument = false;

	private InputStream streamToClose;
	
	
	private void reset()
	{
		if (inputSource != null) close();
		eof = false;
		inCDATA = false;
		pendingText = false;
		textBuffer.setLength(0);
		errCode = 0;
		errDescription = "";
		errLineNumber = 0;
		errLinePos = 0;
		nodesQueue.deleteAll();
		inDocument = false;
		node = null;
	}
	
	/*****************************************************************************/
    // ContentHandler methods
    /*****************************************************************************/

    public void startDocument(XMLLocator locator, String encoding, Augmentations a) throws XNIException 
	{
    } 
	public void startDocument(XMLLocator locator, String encoding) throws XNIException 
	{
    } 
	
	public void endDocument(Augmentations a) throws XNIException
	{
	}

	public void endDocument() throws XNIException
	{
	}

    public void startElement(QName qelement, XMLAttributes attrs, Augmentations a) throws XNIException 
	{	
		startElement(qelement, attrs);
    } 

    public void startElement(QName qelement, XMLAttributes attrs) throws XNIException 
	{	
		inDocument = true;
		flushTextBuffer();
		ElementNode element = (ElementNode)elementsPool.getFree();
		if (element == null) 
		{
			element = new ElementNode(qelement.rawname, qelement.prefix, qelement.localpart, qelement.uri, attrs, true);
			elementsPool.add(element);
		}
		else
		{
			element.setName(qelement.rawname);
			element.setPrefix(qelement.prefix);
			element.setLocalName(qelement.localpart);
			element.setNamespaceURI(qelement.uri);
			element.setAttributes(attrs);
			element.setValue("");
		}
		nodesQueue.addElement(element);
    } 

    public void endElement(QName qelement, Augmentations a) throws XNIException
	{
		endElement(qelement);
	}
					   
    public void endElement(QName qelement) throws XNIException
	{
		flushTextBuffer();
		EndTagNode tag = (EndTagNode)endtagsPool.getFree();
		if (tag == null) 
		{
			tag = new EndTagNode(qelement.rawname, qelement.prefix, qelement.localpart, qelement.uri);
		}
		else
		{
			tag.setName(qelement.rawname);
			tag.setPrefix(qelement.prefix);
			tag.setLocalName(qelement.localpart);
			tag.setNamespaceURI(qelement.uri);
		}
		nodesQueue.addElement(tag);
	}
					   
    public void emptyElement(QName qelement, XMLAttributes attrs, Augmentations a) throws XNIException 
	{
		emptyElement(qelement, attrs);
    } 
	
    public void emptyElement(QName qelement, XMLAttributes attrs) throws XNIException 
	{
		startElement(qelement, attrs);
		endElement(qelement);
    } 
	
	public void startPrefixMapping(java.lang.String prefix, java.lang.String uri, Augmentations a) throws XNIException
	{
	}
	
	public void startPrefixMapping(java.lang.String prefix, java.lang.String uri) throws XNIException
	{
	}
							   
	public void endPrefixMapping(java.lang.String prefix, Augmentations a) throws XNIException
	{
	}
	
	public void endPrefixMapping(java.lang.String prefix) throws XNIException
	{
	}
	
	/**
	*	Le saca a un string los 'espacios' de adelante y atras. Como espacios se entienden los
	*	\n \t \r ' ' 
	*/
	private String removeWhiteSpaces(String value)
	{
		// Si empieza con algo y termina con algo <> espacio, me voy rapidito.
		if	( !isSpace(value.charAt(0)) && !isSpace(value.charAt(value.length() - 1) ))
			return value;

		int begin = 0;
		int end   = value.length() - 1;

		// Busca el primer caracter no blanco, y sale
		for (int i = begin; i < value.length(); i++)
		{
			if	(!isSpace(value.charAt(i)))
			{
				begin = i;
				break;
			}
		}

		// Busca el ultimo caracter no blanco, y sale
		for (int i = end; i >= begin; i--)
		{
			if	(!isSpace(value.charAt(i)))
			{
				end = i;
				break;
			}
		}

		return value.substring(begin, end + 1);
	}
	
	private boolean isSpace(char ch) 
	{
		return (ch == ' ' || ch == '\n' || ch == '\t' || ch == '\r');
	}

	private boolean isWhiteSpace(String value)
	{
		for (int i = 0; i < value.length(); i++)
		{
			if	(!isSpace(value.charAt(i)))
			{
				return false;
			}
		}

		return true;
	}	
	
	private void flushTextBuffer()
	{	
		if (pendingText)
		{
			int nodeType;
			String value = textBuffer.toString();
		
			if (inCDATA)
				nodeType = Node.CDATA;
			else
				nodeType = (isWhiteSpace(value)) ? Node.WHITE_SPACE : Node.TEXT;
			
			ValuedNode vnode = (ValuedNode)valuedNodesPool.getFree();
			if (vnode != null)
			{
				vnode.setValue(value);
				vnode.setNodeType(nodeType);
			}
			else
			{
				vnode = new ValuedNode(nodeType, value);
			}
			nodesQueue.addElement(vnode);
			textBuffer.setLength(0);
			pendingText = false;
		}
	}
	
	public void startCDATA(Augmentations a) throws XNIException
	{
		startCDATA();
	}
	
	public void startCDATA() throws XNIException
	{
		flushTextBuffer();
		pendingText = true;
		inCDATA = true;
	}
	
	public void endCDATA(Augmentations a) throws XNIException
	{
		endCDATA();
	}

	public void endCDATA() throws XNIException
	{
		flushTextBuffer();
		inCDATA = false;
	}

    public void characters(XMLString text, Augmentations a) throws XNIException 
	{
		characters(text);
    } 
    
    public void characters(XMLString text) throws XNIException 
	{
		if (!inDocument) return;
		pendingText = true;
		textBuffer.append(text.toString());
    } 
    
    public void ignorableWhitespace(XMLString text, Augmentations a) throws XNIException 
	{
		ignorableWhitespace(text);
    } 
    
    public void ignorableWhitespace(XMLString text) throws XNIException 
	{
		characters(text);
    } 
    
    public void processingInstruction(String target, XMLString data, Augmentations a) throws XNIException 
	{    
		processingInstruction(target, data);
    } 
	
    public void processingInstruction(String target, XMLString data) throws XNIException 
	{    
		flushTextBuffer();
		PInstructionNode pinode = (PInstructionNode)pinstructionsPool.getFree();
		if (pinode == null) 
		{
			pinode = new PInstructionNode(target, data.toString());
		}
		else
		{
			pinode.setName(target);
			pinode.setValue(data.toString());
		}
		nodesQueue.addElement(pinode);
    } 
	
	public void comment(XMLString text, Augmentations a) throws XNIException
	{
		comment(text);
	}
	
	public void comment(XMLString text) throws XNIException
	{
		flushTextBuffer();
		ValuedNode vnode = (ValuedNode)valuedNodesPool.getFree();
		if (vnode != null)
		{
			vnode.setValue(text.toString());
			vnode.setNodeType(Node.COMMENT);
		}
		else
		{
			vnode = new ValuedNode(Node.COMMENT, text.toString());
		}
		nodesQueue.addElement(vnode);
		
	}
	
	public void startEntity(java.lang.String name,
							java.lang.String publicId,
							java.lang.String systemId,
							java.lang.String baseSystemId,
							java.lang.String encoding)
                 throws XNIException
	{
	}
	
	public void endEntity(java.lang.String name) throws XNIException
	{
	}
	
	public void textDecl(java.lang.String version, java.lang.String encoding, Augmentations a) throws XNIException
	{
	}
	
	public void textDecl(java.lang.String version, java.lang.String encoding) throws XNIException
	{
	}
	
	public void xmlDecl(java.lang.String version,
						java.lang.String encoding,
						java.lang.String standalone, Augmentations a)
             throws XNIException
	{
	}
	
	public void xmlDecl(java.lang.String version,
						java.lang.String encoding,
						java.lang.String standalone)
             throws XNIException
	{
	}
	
	public void doctypeDecl(java.lang.String rootElement,
							java.lang.String publicId,
							java.lang.String systemId, Augmentations a)
                 throws XNIException
	{
		doctypeDecl(rootElement, publicId, systemId);
	}
	
	public void doctypeDecl(java.lang.String rootElement,
							java.lang.String publicId,
							java.lang.String systemId)
                 throws XNIException
	{
		flushTextBuffer();
		ValuedNode vnode = (ValuedNode)valuedNodesPool.getFree();
		if (vnode != null)
		{
			vnode.setValue(rootElement);
			vnode.setNodeType(Node.DOCUMENT_TYPE);
		}
		else
		{
			vnode = new ValuedNode(Node.DOCUMENT_TYPE, rootElement);
		}
		nodesQueue.addElement(vnode);
	}
	
	
	
	
    //
    // XMLErrorHandler methods
    //
    public void warning(String domain, String key, XMLParseException ex) throws XNIException 
	{
		errCode = ERROR_PARSING;
		errDescription = ex.getMessage();
		errLineNumber = ex.getLineNumber();
		errLinePos = ex.getColumnNumber();
		throw new XNIException(ex);
    } 

    
    public void error(String domain, String key, XMLParseException ex) throws XNIException 
	{
		errCode = ERROR_PARSING;
		errDescription = ex.getMessage();
		errLineNumber = ex.getLineNumber();
		errLinePos = ex.getColumnNumber();
		throw new XNIException(ex);
    } 

    
    public void fatalError(String domain, String key, XMLParseException ex) throws XNIException 
	{
        errCode = ERROR_PARSING;
		errDescription = ex.getMessage();
		errLineNumber = ex.getLineNumber();
		errLinePos = ex.getColumnNumber();
		throw new XNIException(ex);
    } 

/*****************************************************************************/

	public void attributeDecl(java.lang.String elementName, java.lang.String attributeName, java.lang.String type, java.lang.String[] enumeration, java.lang.String defaultType, XMLString defaultValue)
	{
	}
//	public void attributeDecl(java.lang.String elementName, java.lang.String attributeName, java.lang.String type, java.lang.String[] enumeration, java.lang.String defaultType, XMLString defaultValue, Augmentations a) 
/*	public void attributeDecl(String elementName, String attributeName, String type, String[] enumeration, String defaultType, XMLString defaultValue, Augmentations a) 
	{
	}
*/	
 	 
	public void elementDecl(java.lang.String name, java.lang.String contentModel, Augmentations a) 
	{
	}

	public void elementDecl(java.lang.String name, java.lang.String contentModel) 
	{
	}

	public void endAttlist(Augmentations a) 
	{
	}

	public void endAttlist() 
	{
	}

	public void endConditional(Augmentations a) 
	{
	}

	public void endConditional() 
	{
	}

	public void endDTD(Augmentations a) 
	{
	}

	public void endDTD() 
	{
	}

	public void externalEntityDecl(java.lang.String name, java.lang.String publicId, java.lang.String systemId, java.lang.String baseSystemId, Augmentations a) 
	{
	}

	public void externalEntityDecl(java.lang.String name, java.lang.String publicId, java.lang.String systemId, java.lang.String baseSystemId) 
	{
	}

	public void internalEntityDecl(java.lang.String name, XMLString text, XMLString text2, Augmentations a) 
	{
	}

	public void internalEntityDecl(java.lang.String name, XMLString text, XMLString text2) 
	{
	}


	public void internalEntityDecl(java.lang.String name, XMLString text) 
	{
	}

	public void notationDecl(java.lang.String name, java.lang.String publicId, java.lang.String systemId, Augmentations a) 
	{
	}

	public void notationDecl(java.lang.String name, java.lang.String publicId, java.lang.String systemId) 
	{
	}

	public void startAttlist(java.lang.String elementName, Augmentations a) 
	{
	}

	public void startAttlist(java.lang.String elementName) 
	{
	}

	public void startConditional(short type, Augmentations a) 
	{
	}

	public void startConditional(short type) 
	{
	}

	public void startDTD(XMLLocator locator, Augmentations a) 
	{
	}

	public void startDTD(XMLLocator locator) 
	{
	}

	public void unparsedEntityDecl(java.lang.String name, java.lang.String publicId, java.lang.String systemId, java.lang.String notation) 
	{
		EntityDeclaration entity = new EntityDeclaration(name, publicId, systemId, notation);
		entities.put(name, entity);
	}
	
/*****************************************************************************/
	
	private String unicodeToUTF8(String source)
	{
		
		if (source == null) return source;
			
		int length = source.length();
		char []buf = new char [length * 3];
		int i, j;
		int ch;
		
		for (i = 0, j = 0; i < length; i++)
		{
			ch = source.charAt(i);
			if (ch <= 0x7F)
				buf[j++] = (char)ch;
			else if (ch <= 0x7FF)
			{
				buf[j++] = (char)(0xC0 | (ch >> 6));
				buf[j++] = (char)(0x80 | (ch & 0x3F));
			}
			else
			{
				buf[j++] = (char)(0xE0 | (ch >> 12));
				buf[j++] = (char)(0x80 | ((ch >> 6) & 0x3F));
				buf[j++] = (char)(0x80 | (ch & 0x3F));
			}
		}
		return String.copyValueOf(buf, 0, j);
	}
	
	public short getNodeType()
	{						    
		return (node != null) ? (short) node.getNodeType() : 0;
	}

	public String getName()
	{
		if (node == null) return "";
		return (UTF8NodeEncoding) ? unicodeToUTF8(node.getName()) : node.getName();	
	}

	public String getPrefix()
	{
		if (node == null) return "";
		return (UTF8NodeEncoding) ? unicodeToUTF8(node.getPrefix()) : node.getPrefix();	
	}

	public String getLocalName()
	{
		if (node == null) return "";
		return (UTF8NodeEncoding) ? unicodeToUTF8(node.getLocalName()): node.getLocalName();
		
	}

	public String getNamespaceURI()
	{
		if (node == null) return "";
		return (UTF8NodeEncoding) ? unicodeToUTF8(node.getNamespaceURI()) : node.getNamespaceURI();
	}

	public int getAttributeCount()
	{
		if (node == null) return 0;
		return node.getAttributeCount();
	}

	
	private String normalize_n_to_rn(String s)
	{
		normalizationBuffer.setLength(0);
		normalizationBuffer.append(s);
		int count = 0;
		for (int index = s.indexOf('\n', 0); index >= 0; index = s.indexOf('\n', index + 1))
		{
			normalizationBuffer.insert(index + count++, '\r');
		}
		return normalizationBuffer.toString();
	}
	
	public String getValue()
	{
		if (node == null) return "";
		String value;
		if (removeWhiteSpaces > 0 && getNodeType() == Node.TEXT)
			value = removeWhiteSpaces(node.getValue());
		else
			value = node.getValue();
		if (linesNormalization == 2) 
			value = normalize_n_to_rn(value);
		return (UTF8NodeEncoding) ? unicodeToUTF8(value) : value;
	}

	// --------------------------------- Atributos

	public String getAttributeByIndex(int index)
	{
		if (node == null) return "";
		return (UTF8NodeEncoding) ? unicodeToUTF8(node.getAttributeByIndex(index-1)): node.getAttributeByIndex(index-1);
	}

	public String getAttributeByName(String name)
	{
		if (node == null) return "";
		return (UTF8NodeEncoding) ? unicodeToUTF8(node.getAttributeByName(name)): node.getAttributeByName(name);
	}
	
	public short existsAttribute(String name)
	{
		if (node == null) return 0;
		return (short) node.existsAttribute(name);
	}

	public String getAttributeName(int index)
	{
		if (node == null) return "";
		String value = (UTF8NodeEncoding) ? unicodeToUTF8(node.getAttributeName(index-1)) :  node.getAttributeName(index-1);
		return value == null?"":value;
	}

	public String getAttributePrefix(int index)
	{
		if (node == null) return "";
		String value = (UTF8NodeEncoding) ? unicodeToUTF8(node.getAttributePrefix(index-1)): node.getAttributePrefix(index-1);
		return value == null?"":value;
		
	}
	
	public String getAttributeLocalName(int index)
	{
		if (node == null) return "";
		String value = (UTF8NodeEncoding) ? unicodeToUTF8(node.getAttributeLocalName(index-1)): node.getAttributeLocalName(index-1);
		return value == null?"":value;
		
	}
	
	public String getAttributeURI(int index)
	{
		if (node == null) return "";
		String value = (UTF8NodeEncoding) ? unicodeToUTF8(node.getAttributeURI(index-1)): node.getAttributeURI(index-1);
		return value == null?"":value;
	}
		

	public String getAttEntityValueByName(String name) 
	{
		if (node == null) return "";
		String attvalue = getAttributeByName(name);
		EntityDeclaration declaration = entities.get(attvalue);
		if (declaration == null) return "";
		String value = declaration.getSystemID();
		return (value.length() > 0)? value : declaration.getPublicID();
	}

	public String getAttEntityNotationByName(String name) 
	{
		if (node == null) return "";
		String attvalue = getAttributeByName(name);
		EntityDeclaration declaration = entities.get(attvalue);
		if (declaration == null) return "";
		return declaration.getNotation();
	}

   	public String getAttEntityValueByIndex(int Index)
   	{
		if (node == null) return "";
		String attvalue = getAttributeByIndex(Index);
		EntityDeclaration declaration = entities.get(attvalue);
		if (declaration == null) return "";
		String value = declaration.getSystemID();
		return (value.length() > 0)? value : declaration.getPublicID();
   	}

   	public String getAttEntityNotationByIndex(int Index)
   	{	
		if (node == null) return "";
		String attvalue = getAttributeByIndex(Index);
		EntityDeclaration declaration = entities.get(attvalue);
		if (declaration == null) return "";
		return declaration.getNotation();
   	}
    
	
/*****************************************************************************/
	
	public XMLReader()
	{
		nodesQueue = new NodesQueue();
		inputSource = null;
		streamToClose = null;
		
		parserConfiguration = new StandardParserConfiguration();
		parserConfiguration.setDocumentHandler(this);
		parserConfiguration.setErrorHandler(this);
		parserConfiguration.setDTDHandler(this);
		setReadExternalEntities(0);
		reset();
	}
	
	public void open(String url)
	{
		reset();
		try
		{
			File xmlFile = new File(url);
			if (ApplicationContext.getInstance().isSpringBootApp())
			{
				ClassPathResource resource = new ClassPathResource(url);
				if (resource.exists())
					xmlFile = resource.getFile();
				else
				{
					if (url.startsWith(ApplicationContext.getInstance().getServletEngineDefaultPath()))
					{
						resource = new ClassPathResource(url.replace(ApplicationContext.getInstance().getServletEngineDefaultPath(), ""));
						if (resource.exists())
							xmlFile = resource.getFile();
					}
				}
			}
			inputSource = new XMLInputSource(null, url, null, new FileInputStream(xmlFile), null);
			if (documentEncoding.length() > 0)
				inputSource.setEncoding(CommonUtil.normalizeEncodingName(documentEncoding));
			parserConfiguration.setInputSource(inputSource);
		}
		catch (IOException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
	}

	public void openResource(String url)
	{
		reset();
		InputStream stream = ResourceReader.getFile(url);
		streamToClose = stream;
		try
		{
			if	(stream == null)
			{
				errCode = ERROR_IO;
				errDescription = "File not found: " + url;
				return;
			}

			Reader reader = new BufferedReader(new InputStreamReader(stream));
			if (documentEncoding.length() > 0)
				inputSource = new XMLInputSource(null, null, null, reader, documentEncoding);
			else
				inputSource = new XMLInputSource(null, null, null, reader, null);

			parserConfiguration.setInputSource(inputSource);
		}
		catch (IOException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
	}

	
	public void openFromString(String s)
	{
		reset();
		try
		{
			if (documentEncoding.length() > 0)
				inputSource = new XMLInputSource(null, null, null, new StringReader(s), documentEncoding);
			else
				inputSource = new XMLInputSource(null, null, null, new StringReader(s), null);

			parserConfiguration.setInputSource(inputSource);
		}
		catch (IOException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
	}
	
	public void openResponse(com.genexus.internet.HttpClient client)
	{
		reset();
		try
		{
			InputStream is = client.getInputStream();
			if (documentEncoding.length() > 0)
				inputSource = new XMLInputSource(null, null, null, is, documentEncoding);
			else
				inputSource = new XMLInputSource(null, null, null, is, null);
			parserConfiguration.setInputSource(inputSource);
		}
		catch (IOException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
	}

	public void openRequest(IHttpRequest client)
	{
		reset();
		try
		{
			InputStream is = client.getInputStream();
			if (documentEncoding.length() > 0)
				inputSource = new XMLInputSource(null, null, null, is, documentEncoding);
			else
				inputSource = new XMLInputSource(null, null, null, is, null);
			parserConfiguration.setInputSource(inputSource);
		}
		catch (IOException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
	}
	
	public void close()
	{
		try 
		{
			if (streamToClose != null) streamToClose.close();
			if (inputSource != null)
			{
				Reader reader = inputSource.getCharacterStream();			
				if (reader != null) {
					reader.close();
				}
				else {
					InputStream is = inputSource.getByteStream();
					if (is != null) {
						is.close();
					}
				}
			}
			if (parserConfiguration != null) parserConfiguration.cleanup();
        }
        catch (IOException e) 
		{
            errCode = ERROR_IO;
			errDescription = e.getMessage();
	    }
		inputSource = null;
	}

	@Override
	protected void finalize() {
		this.close();
	}

	public void addSchema(String url, String schema)
	{
		url = url.replaceAll(" ", "%20");
		String externalSchema = schema + " " + url;
		parserConfiguration.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", externalSchema);

	}
	
	public void addSchema(String url)
	{
		addSchema(url, "");
	}
	
	public void setRemoveWhiteNodes(int value)
	{
		this.removeWhiteNodes = value;
	}

	public byte getRemoveWhiteNodes()
	{
		return (byte) removeWhiteNodes;
	}
	
	public void setRemoveWhiteSpaces(int value)
	{
		this.removeWhiteSpaces = value;
	}

	public byte getRemoveWhiteSpaces()
	{
		return (byte) removeWhiteSpaces;
	}
	
	public void setLinesNormalization (int Normalization)
	{
		linesNormalization = Normalization;
		if (linesNormalization == 0)
			parserConfiguration.setFeature("http:www.genexus.com.uy/java/xmlreader/linesNormalization", false);
		else
			parserConfiguration.setFeature("http:www.genexus.com.uy/java/xmlreader/linesNormalization", true);
	}
	
	public short getLinesNormalization ()
	{
		return (short)linesNormalization;
	}
	
	public byte getSimpleElements()
	{
		return (byte)nextSimpleElements;
	}

	public void setSimpleElements(int value)
	{
		nextSimpleElements = value;
	}
	
	public void setDocEncoding(String encoding)
	{
		documentEncoding = encoding;
	}
	
	public void setNodeEncoding(String encoding)
	{
		if (encoding.equals("UTF-8"))
			UTF8NodeEncoding = true;
		else if (encoding.equals("ANSI"))
			UTF8NodeEncoding = false;
	}
	
	private int getNodeExtent()
	{
		int size, i;
		Node currentNode;
		
		size = nodesQueue.size();
		if (size == 0) return 0;
		
		if (simpleElements == 0) return 1;
		
		i = 0;
		currentNode = nodesQueue.elementAt(i);
		if (currentNode.getNodeType() != Node.ELEMENT) return 1;
		
		if (++i >= size) return 0;
		currentNode = nodesQueue.elementAt(i);
		if (currentNode.getNodeType() == Node.END_TAG) 
			return i + 1;
		
		if (currentNode.getNodeType() != Node.TEXT && currentNode.getNodeType() != Node.WHITE_SPACE) 
			return 1;
		
		if (++i >= size) return 0;
		currentNode = nodesQueue.elementAt(i);
		if (currentNode.getNodeType() == Node.END_TAG) 
			return i + 1;
		else
			return 1;
	}
	
	private void fillQueue()
	{
		try
		{
			boolean ret;
			while (errCode == 0 && getNodeExtent() == 0 && (ret = parserConfiguration.parse(false)));
			eof = getNodeExtent() == 0;
		}
		catch (IOException e)
		{
			errCode = ERROR_IO;
			errDescription = e.getMessage();
		}
		catch (XNIException ex)
		{
			if (errCode == 0)
			{
				errCode = ERROR_PARSING;
				errDescription = ex.getMessage();
			}
		}
	}
	
	public short getIsSimple()
	{
		return (getNodeExtent() > 1) ? (short) 1 : 0;
	}
	
	private void deleteFirstNode()
	{
		Node deleted = nodesQueue.elementAt(0);
		switch (deleted.getNodeType())
		{
		case Node.ELEMENT:
			elementsPool.free(deleted);
			break;
		case Node.END_TAG:
			endtagsPool.free(deleted);
			break;
		case Node.PROCESSING_INSTRUCTION:
			pinstructionsPool.free(deleted);
			break;
		case Node.CDATA:
		case Node.COMMENT:
		case Node.TEXT:
		case Node.WHITE_SPACE:
		case Node.DOCUMENT_TYPE:
			valuedNodesPool.free(deleted);
			break;
		}
		nodesQueue.deleteFirst();
	}
	
	public short read()
	{
		short ret;
		
		while ((ret = readOne()) > 0 && ( removeWhiteNodes != 0 && getNodeType() == Node.WHITE_SPACE) );
		return ret;
	}
	
	private short readOne()
	{
		int extent;
		int i, nRet;
		
		if (errCode > 0) return 0;
		
		extent = getNodeExtent();
		
		for (i = 0; i < extent; i++)
			deleteFirstNode();
		
		simpleElements = nextSimpleElements;
		
		fillQueue();
		
		if ((extent = getNodeExtent()) > 0)
		{
			node = nodesQueue.elementAt(0);
			if (extent > 2) 
				node.setValue(nodesQueue.elementAt(1).getValue());
		}
		
		return (extent > 0) ? (short) 1 : 0;
	}
	
	public short readType(int nodeType)
	{
		return readType(nodeType, "");
	}

	public short readType(int nodeType, String name)
	{
		name = name.trim();
		short ret;
		
		while ( (ret = read()) == 1)
		{
			if	( (node.getNodeType() & nodeType) > 0 && (name.length() == 0 || getName().equals(name))) 
			{
				break;
			}
		}
		return ret;
	}
		
	
	public byte getValidationType()
	{
		return (byte)validationType;
		/*
		boolean validation = parserConfiguration.getFeature("http://xml.org/sax/features/validation");
		boolean dynamic = parserConfiguration.getFeature("http://apache.org/xml/features/validation/dynamic");
		if (validation)
		{
			if (dynamic)
				return ValidationAuto;
			else
				return ValidationDTD;
		}
		else return ValidationNone;
		*/
	}

	public void setValidationType(int value)
	{
		validationType = value;
		switch (value)
		{
		case ValidationNone:
			parserConfiguration.setFeature("http://xml.org/sax/features/validation", false);
			break;
		case ValidationAuto:
			parserConfiguration.setFeature("http://xml.org/sax/features/validation", true);
			parserConfiguration.setFeature("http://apache.org/xml/features/validation/dynamic", true);
			break;
		case ValidationDTD:
			parserConfiguration.setFeature("http://apache.org/xml/features/validation/dynamic", false);
			parserConfiguration.setFeature("http://xml.org/sax/features/validation", true);
			break;
		case ValidationSchema:
			parserConfiguration.setFeature("http://apache.org/xml/features/validation/dynamic", false);			
            parserConfiguration.setFeature("http://xml.org/sax/features/validation", true); 
            parserConfiguration.setFeature("http://apache.org/xml/features/validation/schema",true); 
			break;			
		default:
			parserConfiguration.setFeature("http://xml.org/sax/features/validation", false);
			validationType = ValidationNone;
			break;
		}
	}
	
	public byte getReadExternalEntities()
	{
		return (byte)readExternalEntities;
	}

	public void setReadExternalEntities(int value)
	{
		readExternalEntities = value;
		parserConfiguration.setFeature("http://xml.org/sax/features/external-general-entities", value != 0);
		parserConfiguration.setFeature("http://xml.org/sax/features/external-parameter-entities", value != 0);
		try {
		parserConfiguration.setFeature("http://apache.org/xml/features/disallow-doctype-decl", value == 0);
		} catch (XMLConfigurationException e) {
            // componentManager doesn't support this feature,
            // so we won't worry about it. In Android it do not exits.
        }
		parserConfiguration.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", value != 0);
	}
	
								   
	
	private String SkipAndReadRaw(boolean GenerateXML)
	{
		int Level, i;
		String sRet = null;
		String AttName = null;
		int OldRemoveWhiteSpaces = 0;
		
		StringWriter sWriter = null;
		XMLWriter xmlWriter = null;
			
		if (getNodeType() != Node.ELEMENT) return "";

		if (GenerateXML)
		{
			sWriter = new StringWriter();
			xmlWriter = new XMLWriter();
			xmlWriter.setWriter(new PrintWriter(sWriter));
			OldRemoveWhiteSpaces = getRemoveWhiteSpaces();
			this.setRemoveWhiteSpaces(0);
		}


		Level = 0;
		do
		{	
			switch (getNodeType())
			{
			case Node.ELEMENT:

				if (GenerateXML)
				{
					if (getIsSimple() > 0)
						xmlWriter.writeElement(getName(), getValue());
					else
					{
						Level++;
						xmlWriter.writeStartElement(getName());
					}
											
					for (i = 1; xmlWriter.getErrCode() == 0 && (AttName = getAttributeName(i)).length() > 0; i++)
					{
						xmlWriter.writeAttribute(AttName, getAttributeByIndex(i));
					}
				}
				else
				{
					if (! (getIsSimple() > 0)) Level++;
				}
				break;

			case Node.END_TAG:
				if (GenerateXML)
					xmlWriter.writeEndElement();
				Level--;
				break;

			case Node.WHITE_SPACE:
				break;
			case Node.TEXT:
				if (GenerateXML)
				{
					String sValue = getValue();
					if (sValue.startsWith("\r\n"))
						sValue = sValue.substring(2);
					else if (sValue.startsWith("\n"))
						sValue = sValue.substring(1);
					for (i = sValue.length() - 1; i >= 0 && (sValue.charAt(i) == ' ' || sValue.charAt(i) == '\t'); i-- );
					if (i < (sValue.length() - 1))
						xmlWriter.writeText(sValue.substring(0, i));
					else
						xmlWriter.writeText(sValue);
				}
				break;

			case Node.COMMENT:
				if (GenerateXML)
				{
					xmlWriter.writeComment(getValue());
				}
				break;
				
			case Node.CDATA:
				if (GenerateXML)
				{
					xmlWriter.writeCData(getValue());
				}
				break;

			case Node.PROCESSING_INSTRUCTION:
				if (GenerateXML)
				{
					xmlWriter.writeProcessingInstruction(getName(), getValue());
				}
				break;

			}
		}
		while (read() > 0 && Level > 0);


		if (GenerateXML)
		{
			xmlWriter.close();
			if (getErrCode() == 0)
				sRet = sWriter.toString();
			else
				sRet = "";
			try{ sWriter.close(); }catch(Throwable e) { ; }
			if (sRet == null) sRet = "";
			setRemoveWhiteSpaces(OldRemoveWhiteSpaces);
		}
			
		return sRet;
	}

	public void skip()
	{
		SkipAndReadRaw(false);
	}

	public String readRawXML()
	{
		return SkipAndReadRaw(true);
	}
	

	public boolean getEof()
	{
		return eof || errCode > 0;
	}

	public short getErrCode()
	{
		return (short) errCode;
	}
	
	public short getErrLineNumber()
	{
		return (short) errLineNumber;
	}
	
	public short getErrLinePos()
	{
		return (short) errLinePos;
	}

	public String getErrDescription()
	{
		return errDescription;
	}
	
	private void setErrDescription(String s)
	{
		errDescription = s;
		if (s.length() > 0)
			System.err.println(s);
	}
	
	
	public short getElementType()
	{
		return Node.ELEMENT;
	}
	public short getEndTagType()
	{
		return Node.END_TAG;
	}
	public short getTextType()
	{
		return Node.TEXT;
	}
	public short getCommentType()
	{
		return Node.COMMENT;
	}
	public short getWhiteSpaceType()
	{
		return Node.WHITE_SPACE;
	}
	public short getCDataType()
	{
		return Node.CDATA;
	}
	public short getProcessingInstructionType()
	{
		return Node.PROCESSING_INSTRUCTION;
	}
	public short getDoctypeType()
	{
		return Node.DOCUMENT_TYPE;
	}
	
	
	public void startDocument(XMLLocator p1, String p2, NamespaceContext p3, Augmentations p4) { ; }
	public void startGeneralEntity(String p1, XMLResourceIdentifier p2, String p3, Augmentations a) { ; }
	public void endGeneralEntity(String p1, Augmentations a) { ; }	
	public void endExternalSubset(Augmentations a) { ; }	
	public void endParameterEntity(Augmentations a) { ; }	
	public void endParameterEntity(String p1, Augmentations a) { ; }	
	public void externalEntityDecl(String p1, XMLResourceIdentifier p2, Augmentations a) { ; }	
	public void ignoredCharacters(XMLString p1, Augmentations a) { ; }
	public void notationDecl(String p1, XMLResourceIdentifier p2, Augmentations a) { ; }
	public void startExternalSubset(XMLResourceIdentifier p1, Augmentations a) { ; }
	public void startParameterEntity(String p1, XMLResourceIdentifier p2, String p3, Augmentations a) { ; }
	public void unparsedEntityDecl(String p1, XMLResourceIdentifier p2, String p3, Augmentations a) { System.err.println("XMLReader.unparsedEntityDecl"); /* no se si se llama */ }

	public void setDocumentSource(XMLDocumentSource s) 
	{ 
	}
	public XMLDocumentSource getDocumentSource()
	{
		return null;
	}
	
	public void attributeDecl(String elementName, String attributeName,
                          String type, String[] enumeration,
                          String defaultType, XMLString defaultValue,
                          XMLString nonNormalizedDefaultValue, Augmentations augmentations) throws XNIException
	{
	}
	
	private XMLDTDSource xmlDtdSource;
	public void setDTDSource(XMLDTDSource xmlDtdSource)
	{
		this.xmlDtdSource = xmlDtdSource;
	}
	
	public XMLDTDSource getDTDSource()
	{
		return xmlDtdSource;
	}
}
