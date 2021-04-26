package com.genexus;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.genexus.util.IniFile;
import com.genexus.util.IniFileMultiple;

public class ConfigFileFinder
{
	private static final String CRYPTO_CFG = "crypto.cfg";
	private static final String PROD_ENV_SUFFIX = ".prod";

	public static IniFile getConfigFile(Class resourceClassParm, String fileName, Class defaultResourceClass)
	{
		IniFileStream iniFileStream = new IniFileStream(resourceClassParm, fileName, defaultResourceClass).invoke();
		InputStream is = iniFileStream.getIs();
		InputStream crypto = iniFileStream.getCrypto();

		IniFileStream iniFileStreamProdEnv = new IniFileStream(resourceClassParm, fileName + PROD_ENV_SUFFIX, defaultResourceClass).invoke();
		InputStream isProdEnv = iniFileStreamProdEnv.getIs();

		IniFile iniFile = null;

		try
		{
			iniFile = new IniFileMultiple(is, isProdEnv);
		}
		catch (IOException e)
		{
			if	(ApplicationContext.getInstance().isGXUtility())
			{
				iniFile = new IniFile(fileName);
			}
			else
			{
				String userDir;
				try
				{
					userDir = System.getProperty("user.dir") + "\\";
				}
				catch (SecurityException ex)
				{
					userDir = "";
				}

				throw new InternalError("Can't open " + userDir + fileName + " / " + e.getMessage());
			}
		}

		iniFile.setEncryptionStream(crypto);

		return iniFile;
	}

	private static class IniFileStream {
		private Class resourceClassParm;
		private String fileName;
		private Class defaultResourceClass;
		private InputStream is;
		private InputStream crypto;

		public IniFileStream(Class resourceClassParm, String fileName, Class defaultResourceClass) {
			this.resourceClassParm = resourceClassParm;
			this.fileName = fileName;
			this.defaultResourceClass = defaultResourceClass;
		}

		public InputStream getIs() {
			return is;
		}

		public InputStream getCrypto() {
			return crypto;
		}

		public IniFileStream invoke() {
			is = null;
			crypto = null;

			Class resourceClass = resourceClassParm;
			if (ClientContext.getModelContext() != null)
				resourceClass = ClientContext.getModelContext().getPackageClass();

			if	(is == null && resourceClass != null)
			{
				is = ResourceReader.getResourceAsStream(resourceClass, fileName);
				if	(is != null)
				{
					crypto = ResourceReader.getResourceAsStream(resourceClass, CRYPTO_CFG);
				}
			}

			// This is for GeneXus programs set where is the .cfg file
			if	(is == null && defaultResourceClass != null)
			{
				is = ResourceReader.getResourceAsStream(defaultResourceClass, fileName);
				if	(is != null)
				{
					crypto = ResourceReader.getResourceAsStream(resourceClass, CRYPTO_CFG);
				}
			}

			if	(is == null)
			{
					try
					{
						is = new BufferedInputStream(new FileInputStream(fileName));
						if	(is != null)
						{
							crypto = new BufferedInputStream(new FileInputStream(CRYPTO_CFG));
						}
					}
					catch (FileNotFoundException e)
					{
						try
						{
							is = new BufferedInputStream(new FileInputStream(fileName));
							if	(is != null)
							{
								crypto = new BufferedInputStream(new FileInputStream(CRYPTO_CFG));
							}
						}
						catch (FileNotFoundException e2) { ; }
					}
			}

			if	(is == null)
			{
				if	(ApplicationContext.getInstance().isGXUtility())
				{
					try
					{
						is = new FileInputStream(fileName);
						if	(is != null)
						{
							crypto = new FileInputStream(fileName);
						}
					}
					catch (IOException e)
					{
					}
				}
			}
			return this;
		}
	}
}