
package com.genexus.xml;

import java.util.Vector;

public class NodesQueue
{
	Vector<Node> data;
	int start;
	
	NodesQueue()
	{
		data = new Vector<>();
		start = 0;
	}
	
	public void addElement(Node o)
	{
		data.addElement(o);
	}
	
	public Node elementAt(int index)
	{
		return data.elementAt(start + index);
	}
	
	public int size()
	{
		return data.size() - start;
	}
	
	public void deleteFirst()
	{
		if (++start >= data.size())
		{
			start = 0;
			data.removeAllElements();
		}
	}
	
	public void deleteAll()
	{
		data.removeAllElements();
		start = 0;
	}
}
