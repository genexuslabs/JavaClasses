// $Log:
//

package com.genexus.webpanels;

import java.util.Hashtable;

public class BlobsCleaner
{
	private static BlobsCleaner instance = null;
	
	//Blobs archived by websession id
	private Hashtable blobsTable;
	
	private BlobsCleaner()
	{
	}
	
	public static BlobsCleaner getInstance()
	{
		if(instance == null)
		{
			instance = new BlobsCleaner();
		}
		return instance;
	}
	
	public static void endBlobCleaner()
	{
	}
	
	public synchronized void addBlobFile(String filePath)
	{
	}
	
}
