package com.genexus;

import java.util.Vector;

public class Store
{	
	static public Vector getPurchasedProducts()
	{
		GxUnknownObjectCollection result = new GxUnknownObjectCollection();
		result.fromJSonString(ModelContext.getModelContext().getHttpContext().getHeader("Purchased-Products"));
		return result;
	}
	
	static public boolean isEnabled(String productId)
	{
		Vector products = getPurchasedProducts();
		for(int i=0; i< products.size(); i++) 
		{
			if(((String)products.get(i)).toLowerCase().equals(productId.toLowerCase()))
			{
				return true;
			}
		}
		return false;
	}	
}
	
	