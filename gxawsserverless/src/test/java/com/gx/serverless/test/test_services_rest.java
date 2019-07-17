/*
               File: test_services_rest
        Description: Test
             Author: GeneXus Java Generator version 15_0_11-123400
       Generated on: July 19, 2018 16:10:34.6
       Program type: Callable routine
          Main DBMS: SQL Server
*/
package com.gx.serverless.test;
import com.gx.serverless.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.*;
import javax.servlet.*;

import com.genexus.*;

@Path("/Test")
public final  class test_services_rest extends GxRestService
{
   @Context
   private ServletContext myContext;
   @Context
   private javax.servlet.http.HttpServletRequest myServletRequest;
   @Context
   private javax.servlet.http.HttpServletResponse myServletResponse;
   @GET
   @Produces({MediaType.APPLICATION_JSON + ";charset=UTF-8"})
   public Response execute( @QueryParam("Itemnumber") @DefaultValue("5") String sAV5ItemNumber )
   {
      super.init( "GET" , myServletRequest, myServletResponse, myContext);
      ApplicationContext.getInstance().setServletEngineDefaultPath(myContext.getRealPath("/"));
      Response.ResponseBuilder builder = null;
      if ( ! processHeaders("test",myServletRequest,myServletResponse) )
      {
         builder = Response.notModified();
         cleanup();
         return builder.build();
      }
      short AV5ItemNumber ;
      AV5ItemNumber = (short)(GXutil.lval( sAV5ItemNumber)) ;
      SdtItem data ;
      try
      {
         test worker = new test(remoteHandle, context) ;
         data = worker.executeUdp(AV5ItemNumber );
         builder = Response.ok(new SdtItem_RESTInterface(data));
         cleanup();
         return builder.build() ;
      }
      catch ( Exception e )
      {
         webException(e);
         cleanup();
         SetError("500", e.getMessage());
         builder = Response.status(500);
         builder.entity(errorJson.toString());
         String path = System.getenv("LAMBDA_TASK_ROOT");
         return builder.build() ;
      }
   }

   @POST
   @Path("{sA1UserId}")
   @Consumes({MediaType.APPLICATION_JSON})
   @Produces({MediaType.APPLICATION_JSON + ";charset=UTF-8"})
   public Response Insert( @PathParam("sA1UserId") short sA1UserId ,
                           @QueryParam("check") @DefaultValue("false") boolean gxcheck ,
                           @QueryParam("insertorupdate") @DefaultValue("false") boolean gxinsertorupdate ,
                           SdtItem_RESTInterface entity)
   {
      super.init( "GET" , myServletRequest, myServletResponse, myContext);
      ApplicationContext.getInstance().setServletEngineDefaultPath(myContext.getRealPath("/"));
      Response.ResponseBuilder builder = null;
      if ( ! processHeaders("test",myServletRequest,myServletResponse) )
      {
         builder = Response.notModified();
         cleanup();
         return builder.build();
      }
      SdtItem data ;
      try
      {
         test worker = new test(remoteHandle, context) ;
         data = worker.executeUdp(sA1UserId);
         builder = Response.ok(new SdtItem_RESTInterface(data));
         cleanup();
         return builder.build() ;
      }
      catch ( Exception e )
      {
         webException(e);
         cleanup();
         SetError("500", e.getMessage());
         builder = Response.status(500);
         builder.entity(errorJson.toString());
         return builder.build() ;
      }
   }
   @GET
   @Path("{sA1UserId}")
   @Produces({MediaType.APPLICATION_JSON + ";charset=UTF-8"})
   public Response Load( @PathParam("sA1UserId") short sA1UserId )
   {
      super.init( "GET" , myServletRequest, myServletResponse, myContext);
      ApplicationContext.getInstance().setServletEngineDefaultPath(myContext.getRealPath("/"));
      Response.ResponseBuilder builder = null;
      if ( ! processHeaders("test",myServletRequest,myServletResponse) )
      {
         builder = Response.notModified();
         cleanup();
         return builder.build();
      }
      SdtItem data ;
      try
      {
         test worker = new test(remoteHandle, context) ;
         data = worker.executeUdp(sA1UserId);
         builder = Response.ok(new SdtItem_RESTInterface(data));
         cleanup();
         return builder.build() ;
      }
      catch ( Exception e )
      {
         webException(e);
         cleanup();
         SetError("500", e.getMessage());
         builder = Response.status(500);
         builder.entity(errorJson.toString());
         return builder.build() ;
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

