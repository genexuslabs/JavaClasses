/*
               File: MarkPendingEventsById
        Description: Mark Pending Events By Id
             Author: GeneXus Android Generator version 10_4_0-89519
       Generated on: March 18, 2015 13:55:8.55
       Program type: Callable routine
          Main DBMS: sqlite
*/

package com.artech.base.synchronization.dps;

import java.sql.SQLException;

import com.artech.base.services.IGxProcedure;
import com.artech.base.services.IPropertiesObject;
import com.genexus.GXProcedure;
import com.genexus.CommonUtil;
import com.genexus.ModelContext;
import com.genexus.db.Cursor;
import com.genexus.db.DataStoreHelperBase;
import com.genexus.db.DataStoreProvider;
import com.genexus.db.IDataStoreProvider;
import com.genexus.db.IFieldGetter;
import com.genexus.db.IFieldSetter;
import com.genexus.db.ILocalDataStoreHelper;
import com.genexus.db.UpdateCursor;

public final  class markpendingeventsbyid extends GXProcedure implements IGxProcedure
{
   public boolean execute( IPropertiesObject androidPropertiesObject )
   {

      java.util.UUID[] aP0 = new java.util.UUID[] {java.util.UUID.fromString("00000000-0000-0000-0000-000000000000")};
      short[] aP1 = new short[] {0};

      try
      {
         aP0[0] = (java.util.UUID) CommonUtil.strToGuid(androidPropertiesObject.optStringProperty("PendingEventId"));
         aP1[0] = (short) CommonUtil.val( androidPropertiesObject.optStringProperty("PendingEventStatus"), ".");

         execute(aP0, aP1);

         androidPropertiesObject.setProperty("PendingEventId", CommonUtil.trim( aP0[0].toString()));
         androidPropertiesObject.setProperty("PendingEventStatus", CommonUtil.trim( com.genexus.GXutil.str( aP1[0], 4, 0)));

      }
      catch ( ArrayIndexOutOfBoundsException e )
      {
      }
      return true ;
   }

   public markpendingeventsbyid( int remoteHandle )
   {
      super( remoteHandle , new ModelContext( markpendingeventsbyid.class ), "" );
   }

   public markpendingeventsbyid( int remoteHandle ,
                                 ModelContext context )
   {
      super( remoteHandle , context, "" );
   }

   public short executeUdp( java.util.UUID[] aP0 )
   {
      markpendingeventsbyid.this.AV9PendingEventId = aP0[0];
      markpendingeventsbyid.this.AV8PendingEventStatus = aP1[0];
      markpendingeventsbyid.this.aP1 = new short[] {0};
      initialize();
      privateExecute();
      return aP1[0];
   }

   public void execute( java.util.UUID[] aP0 ,
                        short[] aP1 )
   {
      execute_int(aP0, aP1);
   }

   private void execute_int( java.util.UUID[] aP0 ,
                             short[] aP1 )
   {
      markpendingeventsbyid.this.AV9PendingEventId = aP0[0];
      this.aP0 = aP0;
      markpendingeventsbyid.this.AV8PendingEventStatus = aP1[0];
      this.aP1 = aP1;
      initialize();
      /* GeneXus formulas */
      /* Output device settings */
      privateExecute();
   }

   private void privateExecute( )
   {
      /* Optimized UPDATE. */
      /* Using cursor P00092 */
      pr_default.execute(0, new Object[] {new Short(AV8PendingEventStatus), AV9PendingEventId});
      /* End optimized UPDATE. */
      cleanup();
   }

   protected void cleanup( )
   {
      this.aP0[0] = markpendingeventsbyid.this.AV9PendingEventId;
      this.aP1[0] = markpendingeventsbyid.this.AV8PendingEventStatus;
      //Application.commit(context, remoteHandle, "DEFAULT", "markpendingeventsbyid");
      CloseOpenCursors();
      exitApplication();
   }

   protected void CloseOpenCursors( )
   {
   }

   /* Aggregate/select formulas */
   public void initialize( )
   {
      pr_default = new DataStoreProvider(context, remoteHandle, new markpendingeventsbyid__default(),
         new Object[] {
             new Object[] {
            }
         }
      );
      /* GeneXus formulas. */
      Gx_err = (short)(0) ;
   }

   private short AV8PendingEventStatus ;
   private short A6PendingEventStatus ;
   private short Gx_err ;
   private java.util.UUID AV9PendingEventId ;
   private java.util.UUID[] aP0 ;
   private short[] aP1 ;
   private IDataStoreProvider pr_default ;
}

final  class markpendingeventsbyid__default extends DataStoreHelperBase implements ILocalDataStoreHelper
{
   public Cursor[] getCursors( )
   {
      return new Cursor[] {
          new UpdateCursor("P00092", "UPDATE [GxPendingEvent] SET [PendingEventStatus]=?  WHERE [PendingEventId] = ?", GX_NOMASK + GX_MASKLOOPLOCK)
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
               stmt.setShort(1, ((Number) parms[0]).shortValue());
               stmt.setGUID(2, (java.util.UUID)parms[1]);
               return;
      }
   }

}

