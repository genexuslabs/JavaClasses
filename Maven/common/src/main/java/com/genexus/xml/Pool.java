package com.genexus.xml;

import java.util.Vector;
import java.util.Stack;

public class Pool
{
	Vector usedObjects;
	Stack  freeObjects;
	
	Pool()
	{
		usedObjects = new Vector();
		freeObjects = new Stack();
	}
	
	Object getFree()
	{
		if (!freeObjects.empty())
		{
			return freeObjects.pop();
				
		}
		else return null;
		
	}
	void add(Object element)
	{
		usedObjects.addElement(element);
	}
	
	void free(Object element)
	{
		freeObjects.push(element);
	}
	
}
