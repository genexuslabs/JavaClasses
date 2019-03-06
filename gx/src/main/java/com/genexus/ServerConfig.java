package com.genexus;

import com.genexus.util.GxUtilsLoader;

public class ServerConfig 
{
	public static void execute()
	{
		main(new String[] {});
	}

	public static void main(String arg[])
	{
		ClientContext.setModelContext(new ModelContext(ServerConfig.class));
		ApplicationContext.getInstance().setGXUtility(true);

		String[] fileName = new String[1];

		fileName[0] = (arg.length == 0)?"server.cfg":arg[0];
		
		GxUtilsLoader.runServerConfig(fileName);
	}
}
