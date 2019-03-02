package com.genexus.gxoffice.ooffice;

import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.connection.XConnection;
import com.sun.star.connection.XConnector;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XComponent;

/*
 * Created on 01/08/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author dvillagra
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

class OOfficeConnectionManager
{
    private static boolean connected=false;
  
    private static XDesktop xDesktop = null;
    private static XMultiServiceFactory xMSF = null;    
    private static XBridge bridge = null ;   
        

    public static boolean isConnected()
    {    	
        return connected;
    }
    
    public static XMultiServiceFactory getXMultiServiceFactory()
    {    	
    	return xMSF;
    }
    
    public static XFrame getCurrentXFrame()
    {
    	if (connected){    		
    		return xDesktop.getCurrentFrame();    		
    	}    		    	
    	else{
    		return null;
    	}    	
    }
    
    
    public static XDesktop getDesktop()
    {
    	return xDesktop;     
    }        
    
    public static boolean connect(String host, int port)   
    {
    	
    if (connected)
    {
    	return true;
    }

    Object oDesktop = null;
    XComponentContext xRemoteContext = null;
    XComponentLoader officeComponentLoader = null;
    XMultiComponentFactory xRemoteServiceManager = null;
    
    //String sConnectionString = "socket,host=%host%,port=%port%";
	String sConnectionString = "socket,host=" + host + ",port=" + String.valueOf(port);
    //sConnectionString = sConnectionString.replace("%host%", host);
	//sConnectionString = sConnectionString.replace("%port%", String.valueOf(port));

    try {    	
    	
    	xRemoteContext = com.sun.star.comp.helper.Bootstrap.createInitialComponentContext(null);         
                        
        Object xObj = xRemoteContext.getServiceManager().createInstanceWithContext(
                "com.sun.star.connection.Connector", xRemoteContext);
        
        XConnector xConnector = (XConnector)
                UnoRuntime.queryInterface(XConnector.class, xObj);        
        
        XConnection connection = xConnector.connect(sConnectionString);
        
        if (connection == null)
        {
			System.err.println("GXOffice Error: Connection is null.");
        	return false;
        }                
        
        xObj = xRemoteContext.getServiceManager().createInstanceWithContext(
                "com.sun.star.bridge.BridgeFactory", xRemoteContext);
        
        XBridgeFactory xBridgeFactory = (XBridgeFactory) UnoRuntime.queryInterface(
                XBridgeFactory.class , xObj );
        
        if (xBridgeFactory== null)
        {
        	System.err.println("GXOffice Error: bridge factory is null.");        	
        	return false;
        }            
        
        // this is the bridge that will dispose        
        bridge = xBridgeFactory.createBridge("" , "urp", connection , null);
        
        XComponent xComponent = (XComponent) UnoRuntime.queryInterface(
                XComponent.class, bridge );
        
        // get the remote instance                         
        xObj = bridge.getInstance("StarOffice.ServiceManager");
        
        // Query the initial object for its main factory interface
        xRemoteServiceManager = ( XMultiComponentFactory )
                UnoRuntime.queryInterface( XMultiComponentFactory.class, xObj );
        
        // Query the for MultiServiceFactory interface        
        xMSF = (XMultiServiceFactory)
        UnoRuntime.queryInterface( XMultiServiceFactory.class, xRemoteServiceManager );
        
       // retrieve the component context (it's not yet exported from the office)
       // Query for the XPropertySet interface.
       XPropertySet xProperySet = ( XPropertySet )
                UnoRuntime.queryInterface( XPropertySet.class, xRemoteServiceManager );
            
       // Get the default context from the office server.
       Object oDefaultContext =
                xProperySet.getPropertyValue( "DefaultContext" );
            
       // Query for the interface XComponentContext.
       XComponentContext xOfficeComponentContext =
                ( XComponentContext ) UnoRuntime.queryInterface(
                    XComponentContext.class, oDefaultContext );
       
       // create the desktop service
       oDesktop = xRemoteServiceManager.createInstanceWithContext(
        "com.sun.star.frame.Desktop", xOfficeComponentContext );

       // get the desktop interface
       xDesktop = (com.sun.star.frame.XDesktop) UnoRuntime.queryInterface(
            com.sun.star.frame.XDesktop.class, oDesktop);


        officeComponentLoader = ( XComponentLoader )
			UnoRuntime.queryInterface( XComponentLoader.class, oDesktop );

       if (officeComponentLoader != null)
       {
       		connected = true;
       }
       else
       {
       		connected = false;
       }       
    }
    catch( Exception e ) {
        xRemoteContext = null;
        e.printStackTrace();
        connected = false;
    }    
    return connected;    
    }

public static boolean release()
	{
		try
		{
	        XComponent xcomponent = ( XComponent ) UnoRuntime.queryInterface( XComponent.class,bridge );
	        // Closing the bridge
	        xcomponent.dispose();
	        connected = false;
		}catch (Exception e)
		{
			return false;
		}
		return true;
	}
}