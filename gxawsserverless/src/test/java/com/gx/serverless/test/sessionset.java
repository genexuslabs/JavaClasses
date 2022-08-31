package com.gx.serverless.test;

import com.genexus.*;

public final  class sessionset extends GXProcedure
{
   public sessionset( int remoteHandle )
   {
      super( remoteHandle , new ModelContext( sessionset.class ), "" );
   }

   public sessionset( int remoteHandle ,
                      ModelContext context )
   {
      super( remoteHandle , context, "" );
   }

   public void execute( String aP0 ,
                        String aP1 )
   {
      execute_int(aP0, aP1);
   }

   private void execute_int( String aP0 ,
                             String aP1 )
   {
      sessionset.this.AV8SessionName = aP0;
      sessionset.this.AV9SessionValue = aP1;
      initialize();
      /* GeneXus formulas */
      /* Output device settings */
      privateExecute();
   }

   private void privateExecute( )
   {
      AV10WebSession.setValue(AV8SessionName, AV9SessionValue);
      cleanup();
   }

   protected void cleanup( )
   {
      CloseOpenCursors();
      exitApp();
   }

   protected void CloseOpenCursors( )
   {
   }

   /* Aggregate/select formulas */
   public void initialize( )
   {
      AV10WebSession = httpContext.getWebSession();
      /* GeneXus formulas. */
      Gx_err = (short)(0) ;
   }

   private short Gx_err ;
   private String AV8SessionName ;
   private String AV9SessionValue ;
   private com.genexus.webpanels.WebSession AV10WebSession ;
}

