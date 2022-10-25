package com.genexus;

import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import com.genexus.util.IniFile;
import com.genexus.util.IniFileMultiple;

import java.io.*;

public class ConfigFileFinder {
	public static final ILogger logger = LogManager.getLogger(ConfigFileFinder.class);
	private static final String CRYPTO_CFG = "crypto.cfg";
	private static final String PROD_ENV_SUFFIX = ".prod";
	private static final String DEV_ENV_SUFFIX = ".dev";

	public static IniFile getConfigFile(Class resourceClassParm, String fileName, Class defaultResourceClass) {
		IniFileStream iniFileStream = new IniFileStream(resourceClassParm, fileName, defaultResourceClass).invoke();
		InputStream is = iniFileStream.getIs();
		InputStream crypto = iniFileStream.getCrypto();

		//If client.cfg.dev is present, any other client.cfg.* will not be read
		IniFileStream configurationOverride = new IniFileStream(resourceClassParm, fileName + DEV_ENV_SUFFIX, defaultResourceClass);
		if (configurationOverride == null || configurationOverride.invoke().getIs() == null) {
			configurationOverride = new IniFileStream(resourceClassParm, fileName + PROD_ENV_SUFFIX, defaultResourceClass);
		}
		
		IniFile iniFile = null;

		try {
			iniFile = new IniFileMultiple(is);
			((IniFileMultiple) iniFile).addConfigurationSource(configurationOverride.fileName, configurationOverride.invoke().getIs());
		} catch (IOException e) {
			if (ApplicationContext.getInstance().isGXUtility()) {
				iniFile = new IniFile(fileName);
			} else {
				String userDir;
				try {
					userDir = System.getProperty("user.dir") + "\\";
				} catch (SecurityException ex) {
					userDir = "";
				}

				String errMessage = "Can't open " + userDir + fileName;
				logger.fatal(errMessage, e);
				throw new InternalError(errMessage + " / " + e.getMessage());
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

			if (is == null && resourceClass != null) {
				is = ResourceReader.getResourceAsStream(resourceClass, fileName);
				if (is != null) {
					crypto = ResourceReader.getResourceAsStream(resourceClass, CRYPTO_CFG);
				}
			}

			// This is for GeneXus programs set where is the .cfg file
			if (is == null && defaultResourceClass != null) {
				is = ResourceReader.getResourceAsStream(defaultResourceClass, fileName);
				if (is != null) {
					crypto = ResourceReader.getResourceAsStream(resourceClass, CRYPTO_CFG);
				}
			}

			if (is == null) {
				try {
					is = new BufferedInputStream(new FileInputStream(fileName));
					if (is != null) {
						crypto = new BufferedInputStream(new FileInputStream(CRYPTO_CFG));
					}
				} catch (FileNotFoundException e) {
					try {
						is = new BufferedInputStream(new FileInputStream(fileName));
						if (is != null) {
							crypto = new BufferedInputStream(new FileInputStream(CRYPTO_CFG));
						}
					} catch (FileNotFoundException e2) {
						;
					}
				}
			}

			if (is == null) {
				if (ApplicationContext.getInstance().isGXUtility()) {
					try {
						is = new FileInputStream(fileName);
						if (is != null) {
							crypto = new FileInputStream(fileName);
						}
					} catch (IOException e) {
					}
				}
			}
			return this;
		}
	}
}