package com.genexus.xml;

public interface IXMLReader {

	void openFromString(String sXML);

	short read();

	void close();

	short getErrCode();

	String getErrDescription();

	String readRawXML();

	String getLocalName();

	int getAttributeCount();

	String getAttributeLocalName(int gxi);

	String getAttributeByIndex(int gxi);

	Object getValue();

	String getName();

	short getNodeType();

	short getIsSimple();

	void open(String absoluteName);

	short readType(int i, String string);

	String getAttributeByName(String string);

}
