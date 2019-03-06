package com.genexus;

public class ToolsPrograms
{
	static String packagePath = "";
	
	public static void main(String arg[])
	{
		if (arg.length > 1)
			packagePath = arg[1];
		new ToolsPrograms().execute(arg[0]);
	}
	
	private void execute(String arg)
	{
		try
		{
			if (!packagePath.equals(""))
			{
				if	(!packagePath.endsWith("."))
				{
					packagePath += ".";
				}
				Preferences.defaultResourceClass = Class.forName(packagePath + "GXcfg");
				Application.init(Class.forName(packagePath + "GXcfg")); 
			}
			else
			{
				Application.init(Class.forName("GXcfg")); 
			}
			Application.executingGeneratorTool = true;
		}
		catch(Throwable ee)
		{ 
			System.out.println(ee.toString());
		}
		
		if (arg.equals("CreateStoredProcedures"))
		{
			String prgName = "crtjdbccalls" ;
			try 
			{
				Application.init(Class.forName(GXutil.getClassName(prgName.toLowerCase())));
			} 
			catch (ClassNotFoundException e)
			{
				System.out.println(e.toString());
			}
			com.genexus.db.DynamicExecute.dynamicExecute(-1, prgName);			
		}
		
		if (arg.equals("RebuildRedundancy"))
		{
			com.genexus.db.DynamicExecute.dynamicExecute(-1, "gxlred");
		}
	}
}
