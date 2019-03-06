package com.genexus.usercontrols;
import com.genexus.util.GXMap;

public class UserControlFactoryImpl 
{
	private static UserControlFactoryImpl instance = new UserControlFactoryImpl();
	private GXMap m_Generators = new GXMap();
	
	public static UserControlFactoryImpl getInstance()
	{
		return instance;
	}
	
	public String renderControl(String controlType, String internalName, GXMap propbag)
	{
		UserControlGenerator userControlGenerator = (UserControlGenerator)m_Generators.get(controlType);
		if (userControlGenerator == null)
		{
			userControlGenerator = new UserControlGenerator(controlType);
			m_Generators.put(controlType, userControlGenerator);
		}
		return userControlGenerator.render(internalName, propbag);
	}
}
