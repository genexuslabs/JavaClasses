package com.genexus.sap;


import com.sap.conn.jco.JCoContext;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoFunctionTemplate;
import com.sap.conn.jco.ext.*;
import java.util.Properties;
import com.genexus.ModelContext;
import com.genexus.diagnostics.Log;

public class SessionManager
{

	private String msHost = "";
	private String msServ = "";
	private String group = "";
	private String sapRouter = "";
	private String gatewayHost = "";
	private String gatewayService = "";
	private String port = "";
	private String userName = "";
	private String password = "";
	private String instanceNumber = "";
	private String appServer = "";
	private String routerString = "";
	private String clientNumber = "";
	private String systemId = "";
	private String sessionName = "";
	private String hashedSession = "";
	private String sapGUI = "";
	private Integer errorCode = 0;
	private String errorMessage = "";
	private String language = "";
	private String poolCapacity = "10";
	private String peekLimit = "10";
	private ModelContext _context = null;
	
	DestinationProvider destinationProvider = null;
	Properties connectionProperties=null;
	
	public SessionManager(ModelContext context)
	{		
		_context = context;
	}
	
	public void Disconnect()
	{
		errorCode = 0;
		errorMessage = "";
		//if (destinationProvider == null) {
		//	destinationProvider =  DestinationProvider.Instance();
		//}
		//destinationProvider.removeConnectionProperties(sessionName);
		//destinationProvider.setConnectionProperties( sessionName,  connectionProperties);
		//_context.setContextProperty("SAPSessionName", "");
	}

	public void Connect()
	{
		errorCode = 0;
		errorMessage = "";
		connectionProperties = new Properties();
		if (destinationProvider == null) {
			destinationProvider =  DestinationProvider.Instance();
		}

		if (msHost == null || msHost.equals("")) {
			connectionProperties.setProperty(DestinationDataProvider.JCO_ASHOST, routerString + appServer);
		}
		else {
			if (port == null || port.equals("")) {
				connectionProperties.setProperty(DestinationDataProvider.JCO_MSHOST, msHost);
				connectionProperties.setProperty(DestinationDataProvider.JCO_MSSERV, msServ);
			}
			else {
				connectionProperties.setProperty(DestinationDataProvider.JCO_MSHOST, msHost + ":" + port);
				if (!msServ.equals(""))				
				{
					connectionProperties.setProperty(DestinationDataProvider.JCO_MSSERV, msServ + ":" + port);
				}
			}
			connectionProperties.setProperty(DestinationDataProvider.JCO_GWHOST, gatewayHost);
			connectionProperties.setProperty(DestinationDataProvider.JCO_GWSERV, gatewayService);
			connectionProperties.setProperty(DestinationDataProvider.JCO_R3NAME, systemId);
			connectionProperties.setProperty(DestinationDataProvider.JCO_SAPROUTER, sapRouter);
			connectionProperties.setProperty(DestinationDataProvider.JCO_GROUP, group);

		}
		connectionProperties.setProperty(DestinationDataProvider.JCO_SYSNR, instanceNumber);
		connectionProperties.setProperty(DestinationDataProvider.JCO_CLIENT, clientNumber);
		connectionProperties.setProperty(DestinationDataProvider.JCO_USER, userName);
		connectionProperties.setProperty(DestinationDataProvider.JCO_PASSWD, password);

		connectionProperties.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, poolCapacity);
        connectionProperties.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, peekLimit);

		if ( ! language.equals(""))
		{
			connectionProperties.setProperty(DestinationDataProvider.JCO_LANG, language);
		}
		
		// Hash session name
		hashedSession = sessionName + "H" + Integer.toString(connectionProperties.values().toString().hashCode()).replace("-","0");
		
		Log.info("GX SAP - Connecting " +  sessionName);
		destinationProvider.setConnectionProperties( hashedSession, connectionProperties);
		
		_context.setContextProperty("SAPSessionName", hashedSession);
		try
		{
			JCoDestination destination = JCoDestinationManager.getDestination(hashedSession);
			destination.ping();
		
		}
		catch (AbapException ex)
		{
    		errorCode = ex.getGroup();
			errorMessage = ex.toString();            
        	Log.warning("GX SAP - Error Connecting " +  sessionName + " " + ex.toString()) ;
		
		}
		catch (JCoException ex)
        {
			if(ex.getGroup() == JCoException.JCO_ERROR_INTERNAL)
			{
				Log.error("GX SAP - Error Connecting " +  sessionName + " " + ex.toString()) ;
				throw new RuntimeException(ex.toString());    				
			}		
			errorCode = ex.getGroup();
			errorMessage = ex.toString();            
			Log.warning("GX SAP - Error Connecting " +  sessionName + " " + ex.toString()) ;
        }
		catch (Exception ex)
        {
			Log.error("GX SAP - Error Connecting " +  sessionName + " " + ex.toString()) ;
            throw new RuntimeException(ex.toString());    
        }
	}
	
	public void TransactionBegin()
	{	
	
		Object destinationObj = _context.getContextProperty("SAPSessionName");	
		String destinationName = "";
		if (destinationObj !=null)
		{
			try
			{					
				destinationName = (String)destinationObj;
				Log.info("GX SAP - Begin Transaction " +  destinationName);
				JCoDestination destination = JCoDestinationManager.getDestination(destinationName);		
				JCoContext.begin(destination);	
			}
			catch (JCoException e)
            {				
                throw new RuntimeException(e.toString());    
            }
		}
	}
	
	public void TransactionCommit()
	{
		Object destinationObj = _context.getContextProperty("SAPSessionName");	
		String destinationName = "";
		if (destinationObj !=null)
		{
			try
			{
				destinationName = (String)destinationObj;		
				JCoDestination destination = JCoDestinationManager.getDestination(destinationName);
				Log.info("GX SAP - Commit Transaction " +  destinationName);	
				JCoFunction commitFnc = destination.getRepository().getFunction("BAPI_TRANSACTION_COMMIT");                    
				commitFnc.execute(destination);
				JCoContext.end(destination);
			}
			 catch (JCoException e)
            {
                throw new RuntimeException(e.toString());    
            }
		}
	}
	
	public void Save()
	{
		
	}
	
	public void Load()
	{
		
	}
	/* Getters */
	public String getPort()
	{
		return port;
	}
	public String getMessageHost( )
	{
		return msHost;
	}
	public String getMessageSrv( )
	{
		return msServ;
	}	
	public String getSAPRouter()
	{
		return sapRouter;
	}
	public String getGatewayHost( )
	{
		return gatewayHost;
	}
	
	public String getGatewaySrv( )
	{
		return gatewayService;
	}

	public String getGroup( )
	{
		return group;
	}

	public String getUserName( )
   	{
		return userName;
    }
	
	public String getPassword()
    {
		return password;
    }
	
	public String getInstanceNumber( )
    {
		return instanceNumber;
    }
	
	public String getAppServer( )
	{
		return appServer;
    }
	
	public String getRouterString()
    {
		return routerString;
    }
	
	public String getClientNumber( )
    {
		return clientNumber;
    }
				
	public String getSystemId()
    {
		return systemId;
    }
	
	public String getSessionName( )
    {
		return sessionName;
    }
	
	public String getSAPGUI()
    {
		return sapGUI;
    }
	
	public String getPeekLimit()
    {
		return peekLimit;
    }
	
	public String getPoolCapacity()
    {
		return poolCapacity;
    }
	
	public String getLanguage()
	{
		return language;
	}
	
	public Integer getErrorCode()
	{
		return errorCode;
	}
	
	public String getErrorMessage()
	{
		return errorMessage;
	}
	
	/* Setters */

	public void setPort( String value )
	{
		port = value;
	}

	public void setMessageHost( String value )
	{
		msHost = value;
	}

	public void setMessageSrv( String value )	
	{
		msServ = value;
	}	
	
	public void setSAPRouter( String value)
	{
		sapRouter = value;
	}
	public void setGatewayHost( String value)
	{
		gatewayHost = value;
	}

	public void setGatewaySrv( String value)
	{
		gatewayService = value;
	}

	public void setGroup( String value )
	{
		group = value;
	}
	
	public void setUserName( String value )
	{
		userName = value;
	}
	
	public void setPassword( String value )
    	{
		password = value;
    	}
	
	public void setInstanceNumber( String value )
    	{
		instanceNumber = value;
    	}
	
	public void setAppServer( String value )
    	{
		appServer = value;
    	}
	
	public void setRouterString( String value )
    	{
		routerString = value;
   	}
	
	public void setClientNumber( String value )
    	{
		clientNumber = value;
    	}
		
	public void setSystemId( String value )
    	{
		systemId = value;
    	}
	
	public void setSessionName( String value )
   	{
		sessionName = value;
    }
	
	public void setSAPGUI( String value )
	{
		sapGUI = value;
	}
				
	public void setPeekLimit( String value )
	{
		peekLimit = value;
	}

	public void setPoolCapacity( String value )
	{
		poolCapacity = value;
	}

	public void setLanguage(String value)	
	{
		language = value;	
	}
	
	public void setErrorCode( Integer value )
    	{
		errorCode = value;
    	}
	
	public void setErrorMessage( String value )
    	{
		errorMessage = value;
    	}

}