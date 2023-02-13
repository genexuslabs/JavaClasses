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
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.ServerDataProvider;

public class ServerConnection
{
    ModelContext _context;
    SessionManager _manager;
    DestinationProvider destinationProvider = null;
	private Integer errorCode = 0;
	private String errorMessage = "";

    private String hashedSession = "";

    public ServerConnection(SessionManager manager, ModelContext context)
    {
        _manager = manager;
        _context = context;
    }

    public void connect(Properties connectionProperties)
    {
        
		if (destinationProvider == null) {
			destinationProvider =  DestinationProvider.Instance();
		}

		if (_manager.getMessageHost() == null || _manager.getMessageHost().equals("")) {
			connectionProperties.setProperty(DestinationDataProvider.JCO_ASHOST, _manager.getRouterString() + _manager.getAppServer());
		}
		else {
			if (_manager.getPort() == null || _manager.getPort().equals("")) {
				connectionProperties.setProperty(DestinationDataProvider.JCO_MSHOST, _manager.getMessageHost());
				connectionProperties.setProperty(DestinationDataProvider.JCO_MSSERV, _manager.getMessageSrv());
			}
			else {
				connectionProperties.setProperty(DestinationDataProvider.JCO_MSHOST, _manager.getMessageHost() + ":" + _manager.getPort());
				if (!_manager.getMessageSrv().equals(""))
				{
					connectionProperties.setProperty(DestinationDataProvider.JCO_MSSERV, _manager.getMessageSrv() + ":" + _manager.getPort());
				}
			}
		}

		// Document Transfer server
		//connectionProperties.setProperty(DestinationDataProvider.JCO_GWHOST, gatewayHost);
		//connectionProperties.setProperty(DestinationDataProvider.JCO_GWSERV, gatewayService);
        // Set Destination Properties 

		connectionProperties.setProperty(DestinationDataProvider.JCO_R3NAME, _manager.getSystemId());
		connectionProperties.setProperty(DestinationDataProvider.JCO_SAPROUTER, _manager.getSAPRouter());
		connectionProperties.setProperty(DestinationDataProvider.JCO_GROUP, _manager.getGroup());
		
		connectionProperties.setProperty(ServerDataProvider.JCO_GWHOST, _manager.getGatewayHost());
		connectionProperties.setProperty(ServerDataProvider.JCO_GWSERV, _manager.getGatewaySrv());
		connectionProperties.setProperty(ServerDataProvider.JCO_PROGID, _manager.getProgramID());
		connectionProperties.setProperty(ServerDataProvider.JCO_CONNECTION_COUNT, _manager.getRegistrationCount());
		//
		connectionProperties.setProperty(DestinationDataProvider.JCO_SYSNR, _manager.getInstanceNumber());
		connectionProperties.setProperty(DestinationDataProvider.JCO_CLIENT, _manager.getClientNumber());
		connectionProperties.setProperty(DestinationDataProvider.JCO_USER, _manager.getUserName());
		connectionProperties.setProperty(DestinationDataProvider.JCO_PASSWD, _manager.getPassword());

		connectionProperties.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, _manager.getPoolCapacity());
	        connectionProperties.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, _manager.getPeekLimit());

		if ( ! _manager.getLanguage().equals(""))
		{
			connectionProperties.setProperty(DestinationDataProvider.JCO_LANG, _manager.getLanguage());
		}
		
		// Hash session name
		hashedSession = _manager.getSessionName() + "H" + Integer.toString(connectionProperties.values().toString().hashCode()).replace("-","0");
		
		Log.info("GX SAP - Connecting " +  _manager.getSessionName());
		destinationProvider.setConnectionProperties( hashedSession, connectionProperties);
		destinationProvider.setServerProperties(_manager.getServerName(), connectionProperties);
		
		_context.setContextProperty("SAPSessionName", hashedSession);
		
		_context.setContextProperty("SAPReceiverServerName", _manager.getServerName());

		_context.setContextProperty("SAPReceiverRepositoryName", _manager.getRepositoryName());
		_context.setContextProperty("SAPSenderServerName", _manager.getServerName());
		_context.setContextProperty("SAPSenderRepositoryName", _manager.getRepositoryName());

		try
		{
			JCoDestination destination = JCoDestinationManager.getDestination(hashedSession);
			destination.ping();
		}
		catch (AbapException ex)
		{
	    	errorCode = ex.getGroup();
			errorMessage = ex.toString();            
        	Log.warning("GX SAP - Error Connecting " +  _manager.getSessionName() + " " + ex.toString()) ;		
		}
		catch (JCoException ex)
	        {
			if(ex.getGroup() == JCoException.JCO_ERROR_INTERNAL)
			{
				Log.error("GX SAP - Error Connecting " +  _manager.getSessionName() + " " + ex.toString()) ;
				throw new RuntimeException(ex.toString());    				
			}		
			errorCode = ex.getGroup();
			errorMessage = ex.toString();            
			Log.warning("GX SAP - Error Connecting " +  _manager.getSessionName() + " " + ex.toString()) ;
        	}
		catch (Exception ex)
        	{
			Log.error("GX SAP - Error Connecting " +  _manager.getSessionName() + " " + ex.toString()) ;
			throw new RuntimeException(ex.toString());    
	        }
    }

    public Integer getErrorCode()
    {
        return errorCode;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }
    
}