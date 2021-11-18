/*
               File: Test
        Description: Test
             Author: GeneXus Java Generator version 15_0_11-123400
       Generated on: July 19, 2018 16:59:50.49
       Program type: Callable routine
          Main DBMS: SQL Server
*/
package com.gx.serverless.test;

import com.genexus.GXProcedure;
import com.genexus.GXutil;
import com.genexus.ModelContext;

public final  class receivenumber extends GXProcedure
{
   public receivenumber(int remoteHandle )
   {
      super( remoteHandle , new ModelContext( receivenumber.class ), "" );
   }

   public receivenumber(int remoteHandle ,
						ModelContext context )
   {
      super( remoteHandle , context, "" );
   }

   public SdtItem executeUdp(short aP0 )
   {
      receivenumber.this.AV5ItemNumber = aP0;
      receivenumber.this.aP1 = aP1;
      receivenumber.this.aP1 = new SdtItem[] {new SdtItem()};
      initialize();
      privateExecute();
      return aP1[0];
   }

   public void execute( short aP0 )
   {
      execute_int(aP0);
   }

   private void execute_int( short aP0 )
   {
      receivenumber.this.AV5ItemNumber = aP0;
      receivenumber.this.aP1 = aP1;
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
      receivenumber.this.GXt_int1 = GXv_int2[0] ;
      AV6i = GXt_int1 ;

   }

   protected void cleanup( )
   {

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

