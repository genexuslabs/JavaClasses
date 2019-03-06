package com.genexus.configuration;

import com.genexus.util.GXService;
import com.genexus.util.GXServices;
import com.genexus.util.IniFile;

public class ConfigurationManager {

	public static boolean hasValue(String propName, String fileName){
		String value = getValue(propName, fileName);
		if (value == null)
			return false;

		return !value.isEmpty();
	}

	public static boolean hasValue(String propName){
		return hasValue(propName, "");
	}

	public static String getValue(String propName, String fileName){
		GXServices services = null;
		if (fileName.isEmpty()){
			IniFile iniFile = com.genexus.Preferences.getDefaultPreferences().getIniFile();

			String section = "Client";
			String key = propName;
			if (propName.indexOf(":") > 0)
			{
				section = propName.substring(0, propName.indexOf(":"));
				key = propName.substring(propName.indexOf(":") + 1);
			}

			String propValue = iniFile.getProperty(section, key);

			if (propValue != null && !propValue.isEmpty())
				return propValue;

			services = GXServices.getInstance();
		}
		else{
			services = new GXServices();
			GXServices.loadFromFile("",fileName, services);
		}

		if (services == null)
			return "";

		return getValueFromGXServices(services, propName);
	}

	public static String getValue(String propName){

		return getValue(propName, "");
	}

	private static String getValueFromGXServices(GXServices services, String propName){
		
		String[] tokens = propName.split(":");
		if (tokens.length != 2)
			return null;

		GXService service = services.get(tokens[0]);
		if (service == null)
			return null;

		return service.getProperties().get(tokens[1]);
	}
}