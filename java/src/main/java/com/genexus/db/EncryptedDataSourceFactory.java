package com.genexus.db;

import com.genexus.specific.java.Connect;
import com.genexus.util.IniFile;
import org.apache.commons.dbcp2.BasicDataSourceFactory;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

public class EncryptedDataSourceFactory implements ObjectFactory {

	private final static String USERNAME = "username";
	private final static String PASSWORD = "password";

	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
		if (obj instanceof Reference) {
			Reference ref = (Reference) obj;
			Properties properties = new Properties();

			Enumeration<javax.naming.RefAddr> addresses = ref.getAll();
			while (addresses.hasMoreElements()) {
				javax.naming.RefAddr addr = addresses.nextElement();
				properties.setProperty(addr.getType(), (String) addr.getContent());
			}

			IniFile config;
			try {
				Class<?> gxCfg = Class.forName(properties.getProperty("gxcfg"));
				Connect.init();
				config = com.genexus.ConfigFileFinder.getConfigFile(null, "client.cfg", gxCfg);
			}
			catch (Exception e) {
				System.out.println("ERROR com.genexus.db.EncryptedDataSourceFactory - Could not found gxcfg Class");
				return null;
			}

			String encryptedUsername = properties.getProperty(USERNAME);
			properties.setProperty(USERNAME, config.decryptValue(encryptedUsername, "USER_ID"));

			String encryptedPassword = properties.getProperty(PASSWORD);
			properties.setProperty(PASSWORD, config.decryptValue(encryptedPassword, "USER_PASSWORD"));

			return BasicDataSourceFactory.createDataSource(properties);
		}
		return null;
	}
}