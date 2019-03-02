package com.genexus;

import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.util.*;		    
import java.util.*;		    
import java.io.*;

public final class GXDomains 
{
	private static IniFile domains;
	static 
	{
		domains = SpecificImplementation.Application.getConfigFile(null, SpecificImplementation.Application.getModelContext().getHttpContext().getDefaultPath() + 
			File.separator + "WEB-INF" + File.separatorChar + "classes" + File.separatorChar + "domains.ini", null);
	}

	public static String enumerationDescription(short domainNumber, String domainValue)
	{
		String domainId = Short.toString(domainNumber);
		return domains.getProperty(domainId, domainValue, "");
	}
	
}
