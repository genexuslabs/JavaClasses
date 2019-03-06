// $Log: GXObjectHelper.java,v $
// Revision 1.1  2001/08/24 21:21:30  gusbro
// Initial revision
//
// Revision 1.1.1.1  2001/08/24 21:21:30  gusbro
// GeneXus Java Olimar
//
package com.genexus;


import com.genexus.common.classes.AbstractModelContext;
import com.genexus.common.classes.AbstractNamespace;
import com.genexus.common.classes.AbstractUserInformation;
import com.genexus.common.interfaces.IClientPreferences;
import com.genexus.common.interfaces.SpecificImplementation;

public class GXObjectHelper
{
	public static AbstractUserInformation getUserInformation(AbstractModelContext context, int remoteHandle)
	{
		AbstractUserInformation ui;

		if (remoteHandle == -1)
		{
			AbstractNamespace ns = SpecificImplementation.Application.createNamespace(context);
			ui = SpecificImplementation.Application.createUserInformation(ns);
		}
		else
		{
			ui = SpecificImplementation.Application.getUserInformation(remoteHandle);
		}

		IClientPreferences preferences = context.getClientPreferences();

		if (context != null &&
			!context.getLanguage().equalsIgnoreCase((String) SpecificImplementation.Application.getProperty("LANG_NAME", "English")))
		{
                    ui.setLocalUtil(context.getLanguageProperty("decimal_point").charAt(0),
                                                    context.getLanguageProperty("date_fmt"),
                                                    context.getLanguageProperty("time_fmt"),
                                                    context.getClientPreferences().getYEAR_LIMIT(),
                                                    context.getLanguageProperty("code"));
		}
		else
		{
                    ui.setLocalUtil(preferences.getDECIMAL_POINT(),
                                                    preferences.getDATE_FMT(),
                                                    preferences.getTIME_FMT(),
                                                    preferences.getYEAR_LIMIT(),
                                                    preferences.getLANGUAGE());
		}
		return ui;
	}
}