package com.genexus.sap;

import java.util.Properties;
import com.genexus.ModelContext;
import com.genexus.diagnostics.Log;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoContext;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;

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

	private String sapGUI = "";
	private Integer errorCode = 0;
	private String errorMessage = "";
	private String language = "";
	private String poolCapacity = "10";
	private String peekLimit = "10";
	private String registrationCount = "3";
	private String programID = "";
	private String serverName = "";
	private String repositoryName = "";

	private ModelContext _context = null;
	
	Properties connectionProperties=null;
	DocumentReceiver documentReceiver=null;
	DocumentSender documentSender=null;


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

	public void ConnectSession(String destination, String scope)
	{
		Log.info("GX SAP - Connecting to Destination" +  destination);
		ConnectInternal(destination, destination, scope);		
	}
	
	public void ConnectInternal(String session, String sessionName, String scope)
	{	
		errorCode = 0;
		errorMessage = "";	
		_context.setContextProperty("SAPSessionName", session);
		_context.setContextProperty("SAPSessionScope", scope);
		try
		{
			JCoDestination destination = null;
			if ( scope == null ||  scope.length() == 0 )
				destination = JCoDestinationManager.getDestination(session);
			else	
				destination = JCoDestinationManager.getDestination(session, scope);
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

	public void Connect()
	{
		errorCode = 0;
		errorMessage = "";
		connectionProperties = new Properties();

		ServerConnection connection = new ServerConnection(this, _context);
		connection.connect(connectionProperties);
	}
	
	public void DocumentReceiverStart()
	{	
		documentReceiver = new DocumentReceiver(serverName, repositoryName, _context);
		documentReceiver.start();		
	}
	
	public void DocumentReceiverStop() 
	{
		documentReceiver.stop();
	}

	public void DocumentSenderStart() 
	{
		documentSender = new DocumentSender(serverName, repositoryName, _context);
		documentSender.start();		
	}

	public void DocumentSenderStop() 
	{
		documentSender.stop();
	}

	public void TransactionBegin()
	{	
	
		Object destinationObj = _context.getContextProperty("SAPSessionName");	
		Object scopeObj = _context.getContextProperty("SAPSessionScope");	
		String destinationName = "";
		String scopeName = "";
		if (destinationObj !=null)
		{
			try
			{					
				destinationName = (String)destinationObj;
				scopeName  = (String)scopeObj;
				Log.info("GX SAP - Begin Transaction " +  destinationName);
				JCoDestination destination = null;
				if (scopeName.length() == 0) 
					destination = JCoDestinationManager.getDestination(destinationName);		
				else 	
					destination = JCoDestinationManager.getDestination(destinationName, scopeName);		
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
		Object scopeObject = _context.getContextProperty("SAPSessionScope");
		String destinationName = "";
		String scopeName = "";
		if (destinationObj !=null)
		{
			try
			{
				JCoDestination destination = null;
				destinationName = (String)destinationObj;						
				if ( scopeObject == null ||  ((String)scopeObject).length() == 0 ) {
					destination = JCoDestinationManager.getDestination(destinationName);
				}
				else {
					scopeName = (String)scopeObject;
					destination = JCoDestinationManager.getDestination(destinationName, scopeName);	
				}
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

	public String getRegistrationCount()
	{
		return registrationCount;
	}

	public String getProgramID()
	{
		return programID;
	}

	public String getServerName()
	{
		return serverName;
	}
	
	public String getRepositoryName()
	{
		return repositoryName;
	}
	
	public String getRepositoryName()
	{
		return repositoryName;
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
		
	public void setRegistrationCount(String value)
	{
		registrationCount = value;
	}

	public void setProgramID(String value)
	{
		programID = value;
	}

	public void setServerName(String value)
	{
		serverName = value;
	}

	public void setRepositoryName(String value)
	{
		repositoryName = value;
	}
}