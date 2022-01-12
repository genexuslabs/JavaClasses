package com.genexus;

import org.apache.logging.log4j.Logger;
import java.util.jar.*;

public class Version
{
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(Version.class);
	
	public static final String getFullVersion()
	{
		String version = "";
		try {
			String path = com.genexus.Application.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			String decodedPath = java.net.URLDecoder.decode(path, "UTF-8");
			JarInputStream jarStream = new JarInputStream(new java.io.FileInputStream(decodedPath));
			Manifest mf = jarStream.getManifest();
			Attributes attributes = mf.getMainAttributes();
			version = attributes.getValue("Build-Label");
		}
		catch (Exception e) {
			log.debug("Could not get Build-Label information");
		}

		return version;
	}

	public static void main(String arg[])
	{
		System.out.println("Using GeneXus Standard Classes version " + getFullVersion());
	}
}
