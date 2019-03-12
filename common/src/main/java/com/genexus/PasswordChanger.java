package com.genexus;
import java.io.*;

import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.util.IniFile;
public class PasswordChanger
{
	static String passwordKey = "USER_PASSWORD";
	static String userKey = "USER_ID";

	static String NAMESPACE = "-namespace:";
	static String DATASTORE = "-datastore:";
	static String FILE 		= "-file:";
	static String PASSWORD 	= "-password:";
	static String USER		= "-user:";
	static String HELP1	= "-?";
	static String HELP2	= "-h";

	public static void main(String arg[])
	{
		String file = "client.cfg";
		String namespace = "default";
		String datastore = "DEFAULT";
		String user = null;
		String password = null;

		for (int i = 0; i < arg.length; i++)
		{
			if		(arg[i].toLowerCase().startsWith(NAMESPACE))
			{
				namespace = arg[i].substring(NAMESPACE.length());
			}		
			else if (arg[i].toLowerCase().startsWith(DATASTORE))
			{
				datastore = arg[i].substring(DATASTORE.length());
			}
			else if (arg[i].toLowerCase().startsWith(PASSWORD))
			{
				password = arg[i].substring(PASSWORD.length());
			}
			else if (arg[i].toLowerCase().startsWith(USER))
			{
				user = arg[i].substring(USER.length());
			}
			else if (arg[i].toLowerCase().startsWith(FILE))
			{
				file = arg[i].substring(FILE.length());
			}
			else if (arg[i].toLowerCase().startsWith(HELP1))
			{
				usage();
			}
			else if (arg[i].toLowerCase().startsWith(HELP2))
			{
				usage();
			}
		}

		if	(user == null || password == null)
		{
			System.err.println("You must specify a user name or a password");
			usage();
		}

		if	(!new File(file).exists())
		{
			System.err.println("Can't open " + file);
			System.exit(1);
		}

		IniFile ini = new IniFile(file);
		
		try
		{
			ini.setEncryptionStream(new FileInputStream("crypto.cfg"));
		}
		catch (java.io.IOException e)
		{
			System.out.println("Using default encryption keys...");
		}

		SpecificImplementation.Application.getConfigFile(null, file, null);

		if	(ini.getProperty(namespace + "|" + datastore, passwordKey) == null)
		{
			System.err.println("Invalid .cfg file format: can't find namespace/datastore");
			System.exit(1);
		}

		if	(user != null)
			ini.setPropertyEncrypted(namespace + "|" + datastore, userKey, user);

		if	(password != null)
			ini.setPropertyEncrypted(namespace + "|" + datastore, passwordKey, password);

		ini.save();
	}

	private static void usage()
	{
		System.out.println("\ncom.genexus.PasswordChanger");
		System.out.println("parameters: -file:<filename>");
		System.out.println("            -namespace:<namespace>");
		System.out.println("            -datastore:<datastore>");
		System.out.println("            -user:<user>");
		System.out.println("            -password:<password>");
		System.exit(1);
	}
}