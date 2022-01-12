package com.genexus.webpanels;
import com.genexus.internet.HttpContext;

public class GXOAuthAccessTokenDummy extends GXWebObjectStub
{   
    protected void doExecute(HttpContext context) throws Exception
    {	        
    }	
	protected boolean IntegratedSecurityEnabled( )
   {
      return false;
   }	

   protected int IntegratedSecurityLevel( )
   {
      return 0;
   }   
   
   protected String IntegratedSecurityPermissionPrefix( )
   {
      return "";
   }

   protected String EncryptURLParameters() {return "NO";};
   
   protected void init(HttpContext context )
   {
   }      
}
