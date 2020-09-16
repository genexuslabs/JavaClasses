package com.artech.base.services;

import java.io.InputStream;
import java.util.Hashtable;

import com.artech.synchronization.ISynchronizationHelper;


public interface IContext {

	int getResource(String data, String namespace);
	int getDataImageResourceId(String imageUri);
	
	InputStream getResourceStream(String data, String namespace);
	InputStream openRawResource(int id);
	
	void saveMinorVersion(String prefName, long minorVersion);
	long getMinorVersion(String prefName, long def);

	InputStream openFileInput(String dataFile);

	String getFilesBlobsApplicationDirectory();
	String getFilesSubApplicationDirectory(String directoryName);
	
	/* Deprecated */
	IPropertiesObject getEmptyPropertiesObject();

	IEntity createEntity(String module, String name, IEntity parent);
	IEntity createEntity(String module, String name, IEntity parent, IEntityList collection);
	IEntityList createEntityList();
	
    IPropertiesObject createPropertyObject();
    IPropertiesObject runGxObjectFromProcedure(String objectToCall, IPropertiesObject parameters);

	IAndroidSession getAndroidSession();
	
	/* path in android */
	String getDataBaseFilePath();
	String getDataBaseSyncFilePath();
	String getDataBaseSyncHashesFilePath();

	String getApplicationDataPath();
	String getTemporaryFilesPath();
	String getExternalFilesPath();
	
	boolean getUseUtcConversion();
	
	public int getRemoteHandle();
	
	public String makeImagePath(String imagePartialPath);
	public String getBaseImagesUri();
	// Url 
	public String getRootUri();
	
	public void addSDHeaders(String host, String baseUrl, Hashtable<String, String> headersToSend);
	
	public boolean getSynchronizerSavePendingEvents();

	/**
	 * Gets the name of the GeneXus language that is currently being used.
	 */
	public String getLanguageName();
	
	ISynchronizationHelper getSynchronizationHelper();
	
	public IBluetoothPrinter getBluetoothPrinter(); 
	
}
