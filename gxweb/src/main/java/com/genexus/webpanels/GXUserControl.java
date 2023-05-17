package com.genexus.webpanels;

import com.genexus.ModelContext;
import com.genexus.internet.HttpAjaxContext;
import com.genexus.usercontrols.UserControlFactoryImpl;
import com.genexus.util.GXMap;

public class GXUserControl 
{
	GXMap propertyBag = new GXMap();
	public void setProperty(String propertyName, Object propertyValue)
	{
		if (propertyName!=null && propertyValue!=null)
		{
			if (propertyValue instanceof String  || propertyValue.getClass().isPrimitive())
			{
				propertyBag.put(propertyName, propertyValue);
			}
			else
			{
				try
				{
					Object struct = propertyValue.getClass().getMethod("getStruct", new Class[]{}).invoke(propertyValue, (Object[])null);
					propertyBag.put(propertyName, struct);
				}
				catch(Exception e)
				{
					propertyBag.put(propertyName, propertyValue);
				}
			}

		}
	}
	public void sendProperty(ModelContext context, String componentPrefix, boolean isMasterPage, String internalName, String propertyName, String propertyValue)
	{
		((HttpAjaxContext)context.getHttpContext()).ajax_rsp_assign_uc_prop(componentPrefix, isMasterPage, internalName, propertyName, propertyValue);
		setProperty(propertyName, propertyValue);
	}
	public void render(ModelContext context, String controlType, String internalName, String htmlId)
	{
		propertyBag.put("ContainerName", htmlId);
		String ucServerContent = UserControlFactoryImpl.getInstance().renderControl(controlType, internalName, propertyBag);
		((HttpAjaxContext)context.getHttpContext()).writeText( String.format("<div class=\"gx_usercontrol\" id=\"%s\">%s</div>", htmlId, ucServerContent));
	}
}
