/*
               File: SetHttpResponseHeaders
        Description: Set Http Response Headers
             Author: GeneXus Java Generator version 15_0_11-123400
       Generated on: July 19, 2018 17:0:48.34
       Program type: Callable routine
          Main DBMS: SQL Server
*/
package com.gx.serverless.test;
import com.gx.serverless.*;
import com.genexus.*;

public final  class sethttpresponseheaders extends GXProcedure
{
   public sethttpresponseheaders( int remoteHandle )
   {
      super( remoteHandle , new ModelContext( sethttpresponseheaders.class ), "" );
   }

   public sethttpresponseheaders( int remoteHandle ,
                                  ModelContext context )
   {
      super( remoteHandle , context, "" );
   }

   public short executeUdp( )
   {
      sethttpresponseheaders.this.aP0 = aP0;
      sethttpresponseheaders.this.aP0 = new short[] {0};
      initialize();
      privateExecute();
      return aP0[0];
   }

   public void execute( short[] aP0 )
   {
      execute_int(aP0);
   }

   private void execute_int( short[] aP0 )
   {
      sethttpresponseheaders.this.aP0 = aP0;
      initialize();
      /* GeneXus formulas */
      /* Output device settings */
      privateExecute();
   }

   private void privateExecute( )
   {
      if ( ! httpContext.isAjaxRequest( ) )
      {
         AV9HttpResponse.addHeader("testgx", "genexus");
      }
      AV10Cookie.setName( "gx-test-1" );
      AV10Cookie.setValue( "cookieval" );
      AV9HttpResponse.setCookie(AV10Cookie);
      cleanup();
   }

   protected void cleanup( )
   {
      this.aP0[0] = sethttpresponseheaders.this.AV8i;
      CloseOpenCursors();
      exitApplication();
   }

   protected void CloseOpenCursors( )
   {
   }

   /* Aggregate/select formulas */
   public void initialize( )
   {
      AV9HttpResponse = httpContext.getHttpResponse();
      AV10Cookie = new com.genexus.internet.HttpCookie();
      /* GeneXus formulas. */
      Gx_err = (short)(0) ;
   }

   private short AV8i ;
   private short Gx_err ;
   private com.genexus.internet.HttpCookie AV10Cookie ;
   private short[] aP0 ;
   private com.genexus.internet.HttpResponse AV9HttpResponse ;
}

