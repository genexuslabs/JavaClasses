/*
               File: Reorganization
        Description: No description for object
             Author: GeneXus Android Generator version 10_4_0-88937
       Generated on: March 17, 2015 16:37:57.95
       Program type: Callable routine
          Main DBMS: sqlite
*/
package com.artech.base.synchronization.dbcreate;
import com.genexus.*;
import com.genexus.db.*;
import java.sql.*;
import com.artech.base.services.*;

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

