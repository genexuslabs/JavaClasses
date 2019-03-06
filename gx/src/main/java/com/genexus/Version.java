package com.genexus;
public class Version
{
        public static final String version = "'1.0'";
        public static final String nameSpace = "Genexus X Evolution 4 Java Applications";
	
	public static final String getFullVersion()
	{
                return version;
	}

	public static void main(String arg[])
	{
		System.out.println("Using GeneXus Standard Classes version " + getFullVersion());
	}
}
