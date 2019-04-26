package com.genexus.webpanels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import com.genexus.internet.HttpContext;
import com.genexus.util.GXFile;

public class BlobsCleaner
{
	private static BlobsCleaner instance = null;
	
	//Blobs archived by websession id
	private Hashtable blobsTable;
	private static Object syncRoot = new Object();
	
	private BlobsCleaner()
	{
		blobsTable = new Hashtable();
	}
	
	public static BlobsCleaner getInstance()
	{
		if(instance == null)
		{
			synchronized(syncRoot) 
			{
				if (instance == null)
				{
					instance = new BlobsCleaner();
				}
			}
		}
		return instance;
	}
	
	public static void endBlobCleaner()
	{
		instance = null;
	}
	
	private synchronized void sessionCreated(String sessionId)
	{
		try
		{
			if(blobsTable.get(sessionId) == null)
			{
				blobsTable.put(sessionId, new ArrayList());
			}
		}
		catch(Throwable e)
		{
			System.err.println("*** ERROR *** Adding session for blobs to be deleted:\n" + e.getMessage());
		}
	}
	
	public synchronized void sessionDestroyed(String sessionId)
	{
		try
		{
			removeSessionFiles(sessionId);
		}
		catch(Throwable e)
		{
			System.err.println("*** ERROR *** Deleting blobs on session destroyed:\n" + e.getMessage());
		}
	}
	
	public synchronized void contextDestroyed()
	{
		try
		{
			removeAllFiles();
		}
		catch(Throwable e)
		{
			System.err.println("*** ERROR *** Deleting blobs on context destroyed:\n" + e.getMessage());
		}
	}
	
	public synchronized void addBlobFile(String filePath)
	{
		if(filePath.trim().equals(""))
			return;
		try
		{
			WebSession session = getCurrentSession();
			if(session != null)
			{
				String sId = session.getId().toString();
				Object obj = blobsTable.get(sId);
				if(obj != null)
				{
					((ArrayList)obj).add(filePath);
				}
			}
		}
		catch(Throwable e)
		{
			System.err.println("*** ERROR *** Adding blob to be deleted:\n" + e.getMessage());
		}
	}
	
	private synchronized void setCurrentSessionInUse(WebSession session)
	{		
		String sId = session.getId().toString();
		sessionCreated(sId);
		session.setAttribute("GX_SESSION_DESTROY_FLAG", "1");		
	}
	
	private WebSession getCurrentSession()
	{
		WebSession session = null;
		com.genexus.internet.HttpContext webContext = (HttpContext) com.genexus.ModelContext.getModelContext().getHttpContext();
		if((webContext != null) && (webContext instanceof com.genexus.webpanels.HttpContextWeb))
		{
			session = webContext.getWebSession();
			setCurrentSessionInUse(session);
		}
		return session;
	}
	
	private void removeSessionFiles(String sessionId)
	{
		Object obj = blobsTable.get(sessionId);
		if(obj != null)
		{
			deleteAllFiles((ArrayList)obj);
			blobsTable.remove(sessionId);
		}
	}
	
	private void removeAllFiles()
	{
		Collection filesLists = blobsTable.values();
		Iterator it = filesLists.iterator();
		while(it.hasNext())
		{
			deleteAllFiles((ArrayList)it.next());
		}
		blobsTable.clear();
	}
	
	private void deleteAllFiles(ArrayList files)
	{
		Iterator it = files.iterator();
		while(it.hasNext())
		{
			GXFile tempFile = new GXFile(it.next().toString());
			if(tempFile.exists())
			{
				try
				{
					tempFile.delete();
				}
				catch(Throwable e)
				{
					System.err.println("*** ERROR *** Deleting file:\n" + tempFile.getAbsolutePath());
				}
			}
		}
		files.clear();
	}
}
