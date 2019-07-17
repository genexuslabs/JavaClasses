/*
               File: Test
        Description: Test
             Author: GeneXus Java Generator version 15_0_11-123400
       Generated on: July 19, 2018 16:59:50.49
       Program type: Callable routine
          Main DBMS: SQL Server
*/
package com.gx.serverless.test;
import com.gx.serverless.*;
import com.genexus.*;

public final  class test extends GXProcedure
{
   public test( int remoteHandle )
   {
      super( remoteHandle , new ModelContext( test.class ), "" );
   }

   public test( int remoteHandle ,
                ModelContext context )
   {
      super( remoteHandle , context, "" );
   }

   public SdtItem executeUdp(short aP0 )
   {
      test.this.AV5ItemNumber = aP0;
      test.this.aP1 = aP1;
      test.this.aP1 = new SdtItem[] {new SdtItem()};
      initialize();
      privateExecute();
      return aP1[0];
   }

   public void execute( short aP0 ,
                        SdtItem[] aP1 )
   {
      execute_int(aP0, aP1);
   }

   private void execute_int( short aP0 ,
                             SdtItem[] aP1 )
   {
      test.this.AV5ItemNumber = aP0;
      test.this.aP1 = aP1;
      initialize();
      /* GeneXus formulas */
      /* Output device settings */
      privateExecute();
   }

   private void privateExecute( )
   {
      GXt_int1 = AV6i ;
      GXv_int2[0] = GXt_int1 ;
      new sethttpresponseheaders(remoteHandle, context).execute( GXv_int2) ;
      test.this.GXt_int1 = GXv_int2[0] ;
      AV6i = GXt_int1 ;
      Gxm1item.setgxTv_SdtItem_Itemid( AV5ItemNumber );
      Gxm1item.setgxTv_SdtItem_Itemname( GXutil.format( "%1 Item", GXutil.trim( GXutil.str( AV5ItemNumber, 4, 0)), "", "", "", "", "", "", "", "") );
      cleanup();
   }

   protected void cleanup( )
   {
      this.aP1[0] = test.this.Gxm1item;
      CloseOpenCursors();
      exitApplication();
   }

   protected void CloseOpenCursors( )
   {
   }

   /* Aggregate/select formulas */
   public void initialize( )
   {
      Gxm1item = new SdtItem(remoteHandle, context);
      GXv_int2 = new short [1] ;
      /* GeneXus formulas. */
      Gx_err = (short)(0) ;
   }

   private short AV5ItemNumber ;
   private short AV6i ;
   private short GXt_int1 ;
   private short GXv_int2[] ;
   private short Gx_err ;
   private SdtItem[] aP1 ;
   private SdtItem Gxm1item ;
}

