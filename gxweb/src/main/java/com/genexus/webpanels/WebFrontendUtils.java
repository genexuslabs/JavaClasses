package com.genexus.webpanels;

import com.genexus.*;

public class WebFrontendUtils
{
	public static GXWebComponent getWebComponent(Class caller, String name, int remoteHandle, com.genexus.ModelContext context)
	{
		try
		{
						//En el caso de en el name venga mas de una vez el packageName.
						if (!context.getPackageName().equals(""))
						{
							String packageWithDot = context.getPackageName() + ".";
							int index = name.lastIndexOf(packageWithDot);
							if (index != -1)
							{
								name = name.substring(index);
							}
						}
						else
						{
							name = name.substring(name.lastIndexOf("/") + 1);
						}
                        Object[] parmsArray = null;
                        int questIdx = name.indexOf("?");
                        int endClass = name.indexOf("_impl");
                        if (questIdx != -1)
                        {
                            String parmsStr = name.substring(questIdx+1, endClass);
                            parmsArray = WebUtils.parmsToObjectArray(context, parmsStr, name);
                            name = name.substring(0, questIdx) + "_impl";
                        }
                        name = CommonUtil.lower(name);
			Class<?> webComponentClass;

			if	(caller.getClassLoader() != null)
			{
				webComponentClass = caller.getClassLoader().loadClass(name);
			}
			else
			{
				webComponentClass = Class.forName(name);
			}
			GXWebComponent newComp = (GXWebComponent) webComponentClass.getConstructor(new Class[] { int.class, ModelContext.class }).newInstance(new Object[] {new Integer(remoteHandle), context});
			newComp.setParms(parmsArray);
            return newComp;
		}
		catch (Exception e)
		{
			return new GXWebComponentNull(remoteHandle, context);
		}
	}
}