package com.genexus;

import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.jar.*;

public class Version
{
	private static Logger log = org.apache.logging.log4j.LogManager.getLogger(Version.class);
	
	public static final String getFullVersion()
	{
		String version = "";
		JarInputStream jarStream = null;
		try {
			String path = com.genexus.Application.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			String decodedPath = java.net.URLDecoder.decode(path, "UTF-8");
			jarStream = new JarInputStream(new java.io.FileInputStream(decodedPath));
			Manifest mf = jarStream.getManifest();
			Attributes attributes = mf.getMainAttributes();
			version = attributes.getValue("Build-Label");
		}
		catch (Exception e) {
			log.debug("Could not get Build-Label information");
		} finally {
			try{ if (jarStream != null) jarStream.close(); } catch (IOException ioe) { log.debug("Could not close jar input stream"); }
		}

		return version;
	}

	public static void main(String arg[])
	{
		System.out.println("Using GeneXus Standard Classes version " + getFullVersion());
	}
}
