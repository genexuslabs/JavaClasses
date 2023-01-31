package com.gx.serverless.test;

import com.genexus.*;
import com.genexus.ws.rs.core.*;

@javax.ws.rs.Path("/SessionSet")
public final  class sessionset_services_rest extends GxRestService
{
   @javax.ws.rs.POST
   @javax.ws.rs.Consumes({javax.ws.rs.core.MediaType.APPLICATION_JSON})
   @javax.ws.rs.Produces({javax.ws.rs.core.MediaType.APPLICATION_JSON + ";charset=UTF-8"})
   public javax.ws.rs.core.Response execute( com.gx.serverless.test.sessionset_RESTInterfaceIN entity ) throws Exception
   {
      super.init( "POST" );
      String AV8SessionName;
      AV8SessionName = entity.getSessionName() ;
      String AV9SessionValue;
      AV9SessionValue = entity.getSessionValue() ;
      if ( ! processHeaders("sessionset",myServletRequestWrapper,myServletResponseWrapper) )
      {
         builder = Response.notModifiedWrapped();
         cleanup();
         return (javax.ws.rs.core.Response) builder.build() ;
      }
      try
      {
		  com.gx.serverless.test.sessionset worker = new com.gx.serverless.test.sessionset(remoteHandle, context);
         worker.execute(AV8SessionName,AV9SessionValue );
         builder = Response.okWrapped();
         cleanup();
         return (javax.ws.rs.core.Response) builder.build() ;
      }
      catch ( Exception e )
      {
         cleanup();
         throw e;
      }
   }

   protected boolean IntegratedSecurityEnabled( )
   {
      return false;
   }

   protected int IntegratedSecurityLevel( )
   {
      return 0;
   }

}

