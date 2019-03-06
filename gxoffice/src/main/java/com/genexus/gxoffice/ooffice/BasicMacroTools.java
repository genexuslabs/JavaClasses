/*
 * Created on 09/08/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.genexus.gxoffice.ooffice;

/**
 * @author dvillagra
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XDispatch;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.frame.DispatchResultState;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.util.URL;
import com.sun.star.util.XURLTransformer;


public class BasicMacroTools {
	
    private final XDispatchProvider mDispProv;
    private final XMultiServiceFactory mMSF;
    private final XURLTransformer mParser;    
    
    public BasicMacroTools(XMultiServiceFactory msf, XFrame xFrame, 
            XComponent xDoc)  {
    	
  		
    		mMSF = msf;            
    		mDispProv = makeDispatchProvider(mMSF, xFrame);    		
    		mParser = makeParser(mMSF);

    		if (mDispProv == null || mParser == null)
    		{
    			System.err.println("GXOffice error: could not initialize BasicMacros.");
    		}
    }
   
    private static XDispatchProvider makeDispatchProvider(XMultiServiceFactory mMSF, 
            XFrame aFrame)
     {
    	try
		{
	    	if (aFrame == null) {
	    		System.err.println("GXOffice Error: Could not create DispatchProvider");
	    	}
    		return (XDispatchProvider) UnoRuntime.queryInterface(XDispatchProvider.class, aFrame);
    		
		} catch (Exception e)
		{
			System.err.println("GXOffice Error: Could not create DispatchProvider");
			return null;
		}   	
    }


    private static XURLTransformer makeParser(XMultiServiceFactory mMSF)
    {
        try {
            return (com.sun.star.util.XURLTransformer) UnoRuntime.queryInterface(
                           XURLTransformer.class, mMSF.createInstance(
                                   "com.sun.star.util.URLTransformer"));
        } catch (Exception e) {
            System.err.print("could not create UTL-Transformer " + e.toString());
            return null;
        }
    }
    
    public boolean runMacro(String MacroName)  {
    	
    	return runFunction("macro://", MacroName);    	
    
    }
    
    public boolean runCommand(String CmdName)  {
    	
        return runFunction(".uno:", CmdName);
        
    }    
    
    private synchronized boolean runFunction(String Type, String MacroCmdName) {
    	
        URL[] aParseURL = new URL[1];
        
        aParseURL[0] = new URL();        
        aParseURL[0].Complete = Type + MacroCmdName;
        
        mParser.parseStrict(aParseURL);

        URL aURL = aParseURL[0];
        
        PropertyValue[] xProps = new PropertyValue[0];
        XDispatch xDispatcher = mDispProv.queryDispatch(aURL, "", 0);  
		
        MacroListener xListener = new MacroListener(this);
        
        com.sun.star.frame.XNotifyingDispatch xNotifyingDispatcher = 
        	(com.sun.star.frame.XNotifyingDispatch)UnoRuntime.queryInterface ( 
        	com.sun.star.frame.XNotifyingDispatch.class , xDispatcher );

        
        if (xDispatcher != null) {            
        	
        	try {
        		
	        	xNotifyingDispatcher.dispatchWithNotification(aURL, xProps, xListener);	        	
        	
	        	while (!xListener.checkFinished(false)) { 
	        		this.wait(1000);
	        	}
        	
	        	if (xListener.getResult() == DispatchResultState.SUCCESS)
	        	{
	        		return true;	
	        	}	        	
        	
        	} catch(Exception e)
			{
        		System.err.println("GXOffice Error: Could not run Macro or Command " + MacroCmdName);
			}
        	
        	return false;
        	
        } else {
            System.err.println("GXOffice Error: Could not run Macro or Command " + MacroCmdName);
            return false;
        }
    }    
}

