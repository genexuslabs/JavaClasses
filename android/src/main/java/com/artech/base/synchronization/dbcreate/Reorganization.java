package com.artech.base.synchronization.dbcreate;
import com.genexus.GXReorganization;
import com.genexus.ModelContext;

public final  class Reorganization extends GXReorganization
{
   public static void main( String args[] )
   {
      new Reorganization().executeReorg(args, true);
   }

   public Reorganization( )
   {
      super(reorg.class);
   }

   public String getPackageDir( )
   {
      return "" ;
   }

   public void init( )
   {
   }

   public void execute( )
   {
      context = new ModelContext(getClass());
      new reorg( getHandle()).execute();
   }

}

