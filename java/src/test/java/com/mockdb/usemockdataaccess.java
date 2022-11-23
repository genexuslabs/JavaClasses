package com.mockdb ;
import com.genexus.*;
import com.genexus.sampleapp.GXcfg;

public final  class usemockdataaccess extends GXProcedure
{
   public static void main( String args[] )
   {
      Application.init(GXcfg.class);
      usemockdataaccess pgm = new usemockdataaccess (-1);
      Application.realMainProgram = pgm;
      pgm.executeCmdLine(args);
      GXRuntime.exit( );
   }

   public void executeCmdLine( String args[] )
   {

      execute();
   }

   public usemockdataaccess( )
   {
      super( -1 , new ModelContext( usemockdataaccess.class ), "" );
      Application.init(GXcfg.class);
   }

   public usemockdataaccess( int remoteHandle )
   {
      super( remoteHandle , new ModelContext( usemockdataaccess.class ), "" );
   }

   public usemockdataaccess( int remoteHandle ,
                             ModelContext context )
   {
      super( remoteHandle , context, "" );
   }

   public void execute( )
   {
      execute_int();
   }

   private void execute_int( )
   {
      initialize();
      /* GeneXus formulas */
      /* Output device settings */
      new com.mockdb.ausemockdataaccess(remoteHandle, context).execute(  );
      cleanup();
   }

   protected void cleanup( )
   {
      CloseOpenCursors();
      Application.cleanup(context, this, remoteHandle);
   }

   protected void CloseOpenCursors( )
   {
   }

   /* Aggregate/select formulas */
   public void initialize( )
   {
      /* GeneXus formulas. */
      Gx_err = (short)(0) ;
   }

   private short Gx_err ;
}

