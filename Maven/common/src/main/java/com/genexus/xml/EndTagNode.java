
package com.genexus.xml;

public class EndTagNode extends NamedBasic
{
	public int getNodeType()
	{
		return END_TAG;
	}
	
	EndTagNode(String name, String prefix, String local, String uri)
	{
		super(name, prefix, local, uri);
	}
}
