package com.genexus.gxdynamiccall.test;
import com.genexus.*;

public final  class DynamicCallTestProcedure extends GXProcedure
{
   public DynamicCallTestProcedure( int remoteHandle )
   {
      super( remoteHandle , new ModelContext( DynamicCallTestProcedure.class ), "" );
   }

   public DynamicCallTestProcedure( int remoteHandle ,
                        ModelContext context )
   {
      super( remoteHandle , context, "" );
   }

   @SuppressWarnings("unchecked")
   public String executeUdp( short aP0 ,
                             short aP1 )
   {
      DynamicCallTestProcedure.this.aP2 = new String[] {""};
      execute_int(aP0, aP1, aP2);
      return aP2[0];
   }

   public void execute( short aP0 ,
                        short aP1 ,
                        String[] aP2 )
   {
      execute_int(aP0, aP1, aP2);
   }

   private void execute_int( short aP0 ,
                             short aP1 ,
                             String[] aP2 )
   {
      DynamicCallTestProcedure.this.AV8varchar1 = aP0;
      DynamicCallTestProcedure.this.AV9varchar2 = aP1;
      DynamicCallTestProcedure.this.aP2 = aP2;
      initialize();
      /* GeneXus formulas */
      /* Output device settings */
      privateExecute();
   }

   private void privateExecute( )
   {
      AV11numAux = (short)(AV8varchar1+AV9varchar2) ;
      AV10varchar3 = GXutil.str( AV11numAux, 4, 0) ;
      cleanup();
   }

   protected void cleanup( )
   {
      this.aP2[0] = DynamicCallTestProcedure.this.AV10varchar3;
      CloseOpenCursors();
      exitApp();
   }

   protected void CloseOpenCursors( )
   {
   }

   /* Aggregate/select formulas */
   public void initialize( )
   {
      AV10varchar3 = "" ;
      /* GeneXus formulas. */
      Gx_err = (short)(0) ;
   }

   private short AV8varchar1 ;
   private short AV9varchar2 ;
   private short AV11numAux ;
   private short Gx_err ;
   private String AV10varchar3 ;
   private String[] aP2 ;
}
