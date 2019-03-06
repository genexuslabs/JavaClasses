package com.genexus.sap;

import java.util.Hashtable;
import java.util.Properties;

import com.genexus.diagnostics.Log;
import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;

public class DestinationProvider implements DestinationDataProvider 
{
	
	private static DestinationProvider _instance;
	public static DestinationProvider Instance()
	{
		if (_instance == null)
		{
			_instance = new DestinationProvider();
		}
		return _instance;
		
	}
	
	private String SAP_SERVER = "SAP_SERVER";
	private Properties connectionProperties;
	private Hashtable<String, Properties> connectionList = new Hashtable<String, Properties>(); 
	private DestinationDataEventListener eventListener;
	
	/*@Override*/
	public Properties getDestinationProperties(String sessionName)
	{
		if (sessionName != null)
		{			
			Properties val = connectionList.get(sessionName);
			if (val != null)
			{			
				return val;
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}
	
	/*@Override*/
	public boolean supportsEvents()
	{
		return true;
		
	}
	
	/*@Override*/
	public void setDestinationDataEventListener(DestinationDataEventListener eventListener)
	{	
		this.eventListener = eventListener;
	}
	
	public void removeConnectionProperties(String sessionName)
	{
		if (eventListener != null)
		{
			eventListener.deleted(sessionName);
		}
		connectionProperties = null;
		connectionList.remove(sessionName);
	}

	public void setConnectionProperties( String sessionName, Properties properties)
	{
		if ( sessionName == null || sessionName.equals(""))
		{
			sessionName = SAP_SERVER;
			
		}
		if (!Environment.isDestinationDataProviderRegistered()) 
		{
			Environment.registerDestinationDataProvider(this);
		}
		if (properties == null)
		{			
			if (eventListener != null)
			{
				eventListener.deleted( sessionName);
			}
			connectionProperties = null;
			connectionList.remove( sessionName);
		}
		else
		{
			Log.info("GX SAP - Setting Properties : " + sessionName + " total : " + Integer.toString(connectionList.size()));
			connectionProperties = properties;
			connectionList.put( sessionName, connectionProperties);
			if (eventListener != null)
			{
				eventListener.updated(sessionName);
			}
		}				
	}
}