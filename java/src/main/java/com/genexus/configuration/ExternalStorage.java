package com.genexus.configuration;

import com.genexus.Application;
import com.genexus.GXBaseCollection;
import com.genexus.GXutil;
import com.genexus.SdtMessages_Message;
import com.genexus.common.interfaces.SpecificImplementation;
import com.genexus.db.driver.ExternalProvider;
import com.genexus.util.Encryption;
import com.genexus.util.GXProperties;
import com.genexus.util.GXProperty;
import com.genexus.util.GXService;
import com.genexus.util.GXServices;
import com.genexus.util.GXStorageProvider;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;

public class ExternalStorage extends GXStorageProvider {

	private GXService providerService;

	public static ILogger logger = LogManager.getLogger(Application.class);
	private static String key = SpecificImplementation.Application.getModelContext().getServerKey();

	public ExternalStorage()
	{
		providerService = Application.getGXServices().get(GXServices.STORAGE_APISERVICE);
		if (providerService == null)
		{
			providerService = Application.getGXServices().get(GXServices.STORAGE_SERVICE);
		}
	}

	public boolean create(String name, GXProperties properties, GXStorageProvider[] storageProvider, GXBaseCollection<SdtMessages_Message>[] messages)
	{
		storageProvider[0] = null;

		if (isNullOrEmpty(name))
		{
			GXutil.ErrorToMessages("Unsupported", "Provider name cannot be empty", messages[0]);
			return false;
		}

		try
		{
			if (providerService == null || !providerService.getName().equals(name))
			{
				providerService = new GXService();
				providerService.setType(GXServices.STORAGE_SERVICE);
				providerService.setName(name);
				providerService.setAllowMultiple(false);
				providerService.setAllowOverrideWithEnvVarSettings(false);
				providerService.setProperties(new GXProperties());
			}

			preprocess(name, properties);

			GXProperty prop = properties.first();
			while(!properties.eof())
			{
				providerService.getProperties().set(prop.name, prop.value);
				prop = properties.next();
			}

			String classFullName = providerService.getClassName();
			logger.debug("Loading storage provider: " + classFullName);
			final Class<?> providerClass = Class.forName(classFullName);
			this.provider = (ExternalProvider) providerClass.getConstructor(GXService.class).newInstance(providerService);
		}
		catch (final Exception ex)
		{
			logger.error("Couldn't connect to external storage provider. ", ex.getMessage(), ex);
			storageMessages(ex, messages[0]);
			return false;
		}

		storageProvider[0] = this;
		return true;
	}

	public boolean connect(String profileName, GXProperties properties, GXStorageProvider[] storageProvider, GXBaseCollection<SdtMessages_Message>[] messages)
	{
		if (providerService != null)
		{
			if (profileName.trim().equalsIgnoreCase("default"))
			{
				profileName = providerService.getName();
			}
			return create(profileName, properties, storageProvider, messages);
		}
		storageMessages(new RuntimeException("Provider cannot be local"), messages[0]);
		return false;
	}

	private void preprocess(String name, GXProperties properties)
	{
		String className = null;

		switch(name)
		{

			case "AMAZONS3":
				className = "com.genexus.db.driver.ExternalProviderS3";
				setDefaultProperty(properties, "STORAGE_PROVIDER_REGION", "us-east-1");
				setDefaultProperty(properties, "STORAGE_ENDPOINT", "s3.amazonaws.com");
				setEncryptProperty(properties, "STORAGE_PROVIDER_ACCESSKEYID");
				setEncryptProperty(properties, "STORAGE_PROVIDER_SECRETACCESSKEY");
				setEncryptProperty(properties, "BUCKET_NAME");
				break;

			case "AZURESTORAGE":
				className = "com.genexus.db.driver.ExternalProviderAzureStorage";
				setEncryptProperty(properties, "PUBLIC_CONTAINER_NAME");
				setEncryptProperty(properties, "PRIVATE_CONTAINER_NAME");
				setEncryptProperty(properties, "ACCOUNT_NAME");
				setEncryptProperty(properties, "ACCESS_KEY");
				break;

			case "BOX":
				className = "com.genexus.db.driver.ExternalProviderBox";
				break;

			case "GOOGLE":
				className = "com.genexus.db.driver.ExternalProviderGoogle";
				setEncryptProperty(properties, "KEY");
				setEncryptProperty(properties, "BUCKET_NAME");
				break;

			case "IBMCOS":
				className = "com.genexus.db.driver.ExternalProviderIBM";
				setEncryptProperty(properties, "STORAGE_PROVIDER_ACCESS_KEY");
				setEncryptProperty(properties, "STORAGE_PROVIDER_SECRET_KEY");
				setEncryptProperty(properties, "BUCKET_NAME");
				break;

			case "OPENSTACKSTORAGE":
				className = "com.genexus.db.driver.ExternalProviderOpenStack";
				setEncryptProperty(properties, "BUCKET_NAME");
				setEncryptProperty(properties, "STORAGE_PROVIDER_USER");
				setEncryptProperty(properties, "STORAGE_PROVIDER_PASSWORD");
				break;

			default:
				throw new RuntimeException(String.format("Provider %s is not supported", name));

		}
		
		if (isNullOrEmpty(providerService.getClassName()) || !providerService.getClassName().equals(className))
		{
			providerService.setClassName(className);
		}
	}

	private void setDefaultProperty(GXProperties properties, String prop, String value)
	{
		if (!properties.containsKey(prop))
			properties.set(prop, value);
	}

	private void setEncryptProperty(GXProperties properties, String prop)
	{
		String value = properties.get(prop);
		if (isNullOrEmpty(value))
			value = "";
		value = Encryption.addchecksum(value, Encryption.getCheckSumLength());
		value = Encryption.encrypt64(value, key);
		properties.set(prop, value);
	}

	private boolean isNullOrEmpty(String value)
	{
		return value == null || value.trim().length() == 0;
	}

}

/*
	TODO:
	+ Provider's libraries (jar) must be manually copied from '{gx}/Services/Storage/{provider}/services/*.jar' to '{webapp}/web/services'
	+ Every library (jar) must be manually added to Classpath property
*/