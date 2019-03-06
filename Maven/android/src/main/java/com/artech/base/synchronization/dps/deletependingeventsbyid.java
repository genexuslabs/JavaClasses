/*
               File: DeletePendingEventsById
        Description: Delete Pending Events By Id
             Author: GeneXus Android Generator version 10_4_0-89519
       Generated on: March 18, 2015 13:55:7.70
       Program type: Callable routine
          Main DBMS: sqlite
*/

package com.artech.base.synchronization.dps;

import java.sql.SQLException;

import com.artech.base.services.IGxProcedure;
import com.artech.base.services.IPropertiesObject;
import com.genexus.CommonUtil;
import com.genexus.GXProcedure;
import com.genexus.ModelContext;
import com.genexus.db.Cursor;
import com.genexus.db.DataStoreHelperBase;
import com.genexus.db.DataStoreProvider;
import com.genexus.db.IDataStoreProvider;
import com.genexus.db.IFieldGetter;
import com.genexus.db.IFieldSetter;
import com.genexus.db.ILocalDataStoreHelper;
import com.genexus.db.UpdateCursor;

public final  class deletependingeventsbyid extends GXProcedure implements IGxProcedure
{
   public boolean execute( IPropertiesObject androidPropertiesObject )
   {

      java.util.UUID[] aP0 = new java.util.UUID[] {java.util.UUID.fromString("00000000-0000-0000-0000-000000000000")};

      try
      {
         aP0[0] = (java.util.UUID) CommonUtil.strToGuid(androidPropertiesObject.optStringProperty("PendingEventId"));

         execute(aP0);

         androidPropertiesObject.setProperty("PendingEventId", CommonUtil.trim( aP0[0].toString()));

      }
      catch ( ArrayIndexOutOfBoundsException e )
      {
      }
      return true ;
   }

   public deletependingeventsbyid( int remoteHandle )
   {
      super( remoteHandle , new ModelContext( deletependingeventsbyid.class ), "" );
   }

   public deletependingeventsbyid( int remoteHandle ,
                                   ModelContext context )
   {
      super( remoteHandle , context, "" );
   }

   public java.util.UUID executeUdp( )
   {
      deletependingeventsbyid.this.AV9PendingEventId = aP0[0];
      deletependingeventsbyid.this.aP0 = new java.util.UUID[] {java.util.UUID.fromString("00000000-0000-0000-0000-000000000000")};
      initialize();
      privateExecute();
      return aP0[0];
   }

   public void execute( java.util.UUID[] aP0 )
   {
      execute_int(aP0);
   }

   private void execute_int( java.util.UUID[] aP0 )
   {
      deletependingeventsbyid.this.AV9PendingEventId = aP0[0];
      this.aP0 = aP0;
      initialize();
      /* GeneXus formulas */
      /* Output device settings */
      privateExecute();
   }

   private void privateExecute( )
   {
      /* Optimized DELETE. */
      /* Using cursor P00082 */
      pr_default.execute(0, new Object[] {AV9PendingEventId});
      /* End optimized DELETE. */
      cleanup();
   }

   protected void cleanup( )
   {
      this.aP0[0] = deletependingeventsbyid.this.AV9PendingEventId;
      //Application.commit(context, remoteHandle, "DEFAULT", "deletependingeventsbyid");
      CloseOpenCursors();
      exitApplication();
   }

   protected void CloseOpenCursors( )
   {
   }

   /* Aggregate/select formulas */
   public void initialize( )
   {
      pr_default = new DataStoreProvider(context, remoteHandle, new deletependingeventsbyid__default(),
         new Object[] {
             new Object[] {
            }
         }
      );
      /* GeneXus formulas. */
      Gx_err = (short)(0) ;
   }

   private short Gx_err ;
   private java.util.UUID AV9PendingEventId ;
   private java.util.UUID[] aP0 ;
   private IDataStoreProvider pr_default ;
}

final  class deletependingeventsbyid__default extends DataStoreHelperBase implements ILocalDataStoreHelper
{
   public Cursor[] getCursors( )
   {
      return new Cursor[] {
          new UpdateCursor("P00082", "DELETE FROM [GxPendingEvent]  WHERE [PendingEventId] = ?", GX_NOMASK + GX_MASKLOOPLOCK)
      };
   }

   public void getResults( int cursor ,
                           IFieldGetter rslt ,
                           Object[] buf ) throws SQLException
   {
      switch ( cursor )
      {
      }
   }

   public void setParameters( int cursor ,
                              IFieldSetter stmt ,
                              Object[] parms ) throws SQLException
   {
      switch ( cursor )
      {
            case 0 :
               stmt.setGUID(1, (java.util.UUID)parms[0]);
               return;
      }
   }

}

