/*
               File: GetPendingEventByTimestamp
        Description: Get Pending Event By Timestamp
             Author: GeneXus Android Generator version 15_0_0-96665
       Generated on: December 11, 2015 14:26:58.89
       Program type: Callable routine
          Main DBMS: sqlite
*/

package com.artech.base.synchronization.dps;

import java.sql.SQLException;

import com.artech.base.services.AndroidContext;
import com.artech.base.services.IEntity;
import com.artech.base.services.IGxProcedure;
import com.artech.base.services.IPropertiesObject;
import com.genexus.CommonUtil;
import com.genexus.GXBaseCollection;
import com.genexus.GXProcedure;
import com.genexus.ModelContext;
import com.genexus.TypeConstants;
import com.genexus.db.Cursor;
import com.genexus.db.DataStoreHelperBase;
import com.genexus.db.DataStoreProvider;
import com.genexus.db.ForEachCursor;
import com.genexus.db.IDataStoreProvider;
import com.genexus.db.IFieldGetter;
import com.genexus.db.IFieldSetter;
import com.genexus.db.ILocalDataStoreHelper;

public final  class getpendingeventbytimestamp extends GXProcedure implements IGxProcedure
{
   public boolean execute( IPropertiesObject androidPropertiesObject )
   {

      short aP0 = 0;
      GXBaseCollection<SdtGxSynchroEventSDT_GxSynchroEventSDTItem>[] aP1 = new GXBaseCollection[] {new GXBaseCollection<SdtGxSynchroEventSDT_GxSynchroEventSDTItem>()};

      try
      {
         aP0 = (short) CommonUtil.val( androidPropertiesObject.optStringProperty("PendingEventStatus"), ".");

         execute(aP0, aP1);

         java.util.LinkedList<IEntity> outObjGxm2rootcol = new java.util.LinkedList<IEntity>();
         if (aP1[0] != null)
         {
            for (int i = 0; i < aP1[0].size(); i++)
            {
               SdtGxSynchroEventSDT_GxSynchroEventSDTItem sdttyped = (SdtGxSynchroEventSDT_GxSynchroEventSDTItem) aP1[0].elementAt(i);
               IEntity objOutElement = AndroidContext.ApplicationContext.createEntity("genexus.sd.synchronization", "SynchronizationEventList.SynchronizationEventListItem", null);
               sdttyped.sdttoentity(objOutElement);
               outObjGxm2rootcol.add(objOutElement);
            }
         }
         androidPropertiesObject.setProperty("ReturnValue",outObjGxm2rootcol);

      }
      catch ( ArrayIndexOutOfBoundsException e )
      {
      }
      return true ;
   }

   public getpendingeventbytimestamp( int remoteHandle )
   {
      super( remoteHandle , new ModelContext( getpendingeventbytimestamp.class ), "" );
   }

   public getpendingeventbytimestamp( int remoteHandle ,
                                      ModelContext context )
   {
      super( remoteHandle , context, "" );
   }

   public GXBaseCollection<SdtGxSynchroEventSDT_GxSynchroEventSDTItem> executeUdp( short aP0 )
   {
      getpendingeventbytimestamp.this.AV5PendingEventStatus = aP0;
      getpendingeventbytimestamp.this.aP1 = new GXBaseCollection[] {new GXBaseCollection<SdtGxSynchroEventSDT_GxSynchroEventSDTItem>()};
      initialize();
      privateExecute();
      return aP1[0];
   }

   public void execute( short aP0 ,
                        GXBaseCollection<SdtGxSynchroEventSDT_GxSynchroEventSDTItem>[] aP1 )
   {
      execute_int(aP0, aP1);
   }

   private void execute_int( short aP0 ,
                             GXBaseCollection<SdtGxSynchroEventSDT_GxSynchroEventSDTItem>[] aP1 )
   {
      getpendingeventbytimestamp.this.AV5PendingEventStatus = aP0;
      getpendingeventbytimestamp.this.aP1 = aP1;
      initialize();
      /* GeneXus formulas */
      Gxm2rootcol = getpendingeventbytimestamp.this.aP1[0];
      /* Output device settings */
      privateExecute();
   }

   private void privateExecute( )
   {
      pr_default.dynParam(0, new Object[]{ new Object[]{
                                           new Short(AV5PendingEventStatus) ,
                                           new Short(A6PendingEventStatus) },
                                           new int[] {
                                           TypeConstants.SHORT, TypeConstants.SHORT
                                           }
      });
      /* Using cursor P00022 */
      pr_default.execute(0, new Object[] {new Short(AV5PendingEventStatus)});
      while ( (pr_default.getStatus(0) != 101) )
      {
         A6PendingEventStatus = P00022_A6PendingEventStatus[0] ;
         A1PendingEventId = P00022_A1PendingEventId[0] ;
         A3PendingEventBC = P00022_A3PendingEventBC[0] ;
         A4PendingEventAction = P00022_A4PendingEventAction[0] ;
         A5PendingEventData = P00022_A5PendingEventData[0] ;
         A7PendingEventErrors = P00022_A7PendingEventErrors[0] ;
         A9PendingEventFiles = P00022_A9PendingEventFiles[0] ;
         A2PendingEventTimestamp = P00022_A2PendingEventTimestamp[0] ;
         Gxm1gxsynchroeventsdt = (SdtGxSynchroEventSDT_GxSynchroEventSDTItem)new SdtGxSynchroEventSDT_GxSynchroEventSDTItem(remoteHandle, context);
         Gxm2rootcol.add(Gxm1gxsynchroeventsdt, 0);
         Gxm1gxsynchroeventsdt.setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventid( A1PendingEventId );
         Gxm1gxsynchroeventsdt.setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventtimestamp( A2PendingEventTimestamp );
         Gxm1gxsynchroeventsdt.setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventbc( A3PendingEventBC );
         Gxm1gxsynchroeventsdt.setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventaction( A4PendingEventAction );
         Gxm1gxsynchroeventsdt.setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventdata( A5PendingEventData );
         Gxm1gxsynchroeventsdt.setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventstatus( A6PendingEventStatus );
         Gxm1gxsynchroeventsdt.setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventerrors( A7PendingEventErrors );
         Gxm1gxsynchroeventsdt.setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventfiles( A9PendingEventFiles );
         pr_default.readNext(0);
      }
      pr_default.close(0);
      cleanup();
   }

   protected void cleanup( )
   {
      this.aP1[0] = getpendingeventbytimestamp.this.Gxm2rootcol;
      CloseOpenCursors();
      exitApplication();
   }

   protected void CloseOpenCursors( )
   {
   }

   /* Aggregate/select formulas */
   public void initialize( )
   {
      Gxm2rootcol = new GXBaseCollection<SdtGxSynchroEventSDT_GxSynchroEventSDTItem>(SdtGxSynchroEventSDT_GxSynchroEventSDTItem.class, "GxSynchroEventSDT.GxSynchroEventSDTItem", "PendingEvents", remoteHandle);
      //Gxm2rootcol = new GxUnknownObjectCollection();

      scmdbuf = "" ;
      P00022_A6PendingEventStatus = new short[1] ;
      P00022_A1PendingEventId = new java.util.UUID[] {java.util.UUID.fromString("00000000-0000-0000-0000-000000000000")} ;
      P00022_A3PendingEventBC = new String[] {""} ;
      P00022_A4PendingEventAction = new short[1] ;
      P00022_A5PendingEventData = new String[] {""} ;
      P00022_A7PendingEventErrors = new String[] {""} ;
      P00022_A9PendingEventFiles = new String[] {""} ;
      P00022_A2PendingEventTimestamp = new java.util.Date[] {CommonUtil.nullDate()} ;
      A1PendingEventId = java.util.UUID.fromString("00000000-0000-0000-0000-000000000000") ;
      A3PendingEventBC = "" ;
      A5PendingEventData = "" ;
      A7PendingEventErrors = "" ;
      A9PendingEventFiles = "" ;
      A2PendingEventTimestamp = CommonUtil.resetTime( CommonUtil.nullDate() );
      Gxm1gxsynchroeventsdt = new SdtGxSynchroEventSDT_GxSynchroEventSDTItem(remoteHandle, context);
      pr_default = new DataStoreProvider(context, remoteHandle, new getpendingeventbytimestamp__default(),
         new Object[] {
             new Object[] {
            P00022_A6PendingEventStatus, P00022_A1PendingEventId, P00022_A3PendingEventBC, P00022_A4PendingEventAction, P00022_A5PendingEventData, P00022_A7PendingEventErrors, P00022_A9PendingEventFiles, P00022_A2PendingEventTimestamp
            }
         }
      );
      /* GeneXus formulas. */
      Gx_err = (short)(0) ;
   }

   private short AV5PendingEventStatus ;
   private short A6PendingEventStatus ;
   private short A4PendingEventAction ;
   private short Gx_err ;
   private String scmdbuf ;
   private java.util.Date A2PendingEventTimestamp ;
   private String A5PendingEventData ;
   private String A7PendingEventErrors ;
   private String A9PendingEventFiles ;
   private String A3PendingEventBC ;
   private java.util.UUID A1PendingEventId ;
   private GXBaseCollection<SdtGxSynchroEventSDT_GxSynchroEventSDTItem>[] aP1 ;
   private IDataStoreProvider pr_default ;
   private short[] P00022_A6PendingEventStatus ;
   private java.util.UUID[] P00022_A1PendingEventId ;
   private String[] P00022_A3PendingEventBC ;
   private short[] P00022_A4PendingEventAction ;
   private String[] P00022_A5PendingEventData ;
   private String[] P00022_A7PendingEventErrors ;
   private String[] P00022_A9PendingEventFiles ;
   private java.util.Date[] P00022_A2PendingEventTimestamp ;
   private GXBaseCollection<SdtGxSynchroEventSDT_GxSynchroEventSDTItem> Gxm2rootcol ;
   private SdtGxSynchroEventSDT_GxSynchroEventSDTItem Gxm1gxsynchroeventsdt ;
}

final  class getpendingeventbytimestamp__default extends DataStoreHelperBase implements ILocalDataStoreHelper
{
   protected Object[] conditional_P00022( ModelContext context ,
                                          int remoteHandle ,
                                          com.genexus.internet.HttpContext httpContext ,
                                          short AV5PendingEventStatus ,
                                          short A6PendingEventStatus )
   {
      String sWhereString = "" ;
      String scmdbuf ;
      byte[] GXv_int1 ;
      GXv_int1 = new byte [1] ;
      Object[] GXv_Object2 ;
      GXv_Object2 = new Object [2] ;
      scmdbuf = "SELECT [PendingEventStatus], [PendingEventId], [PendingEventBC], [PendingEventAction]," ;
      scmdbuf = scmdbuf + " [PendingEventData], [PendingEventErrors], [PendingEventFiles], [PendingEventTimestamp]" ;
      scmdbuf = scmdbuf + " FROM [GxPendingEvent]" ;
      if ( ! (0==AV5PendingEventStatus) )
      {
         if ( CommonUtil.strcmp("", sWhereString) != 0 )
         {
            sWhereString = sWhereString + " and ([PendingEventStatus] = ?)" ;
         }
         else
         {
            sWhereString = sWhereString + " ([PendingEventStatus] = ?)" ;
         }
      }
      else
      {
         GXv_int1[0] = (byte)(1) ;
      }
      if ( CommonUtil.strcmp("", sWhereString) != 0 )
      {
         scmdbuf = scmdbuf + " WHERE " + sWhereString ;
      }
      scmdbuf = scmdbuf + " ORDER BY [PendingEventTimestamp]" ;
      GXv_Object2[0] = scmdbuf ;
      GXv_Object2[1] = GXv_int1 ;
      return GXv_Object2 ;
   }

   public Object [] getDynamicStatement( int cursor ,
                                         ModelContext context ,
                                         int remoteHandle ,
                                         com.genexus.internet.HttpContext httpContext ,
                                         Object [] dynConstraints )
   {
      switch ( cursor )
      {
            case 0 :
                  return conditional_P00022(context, remoteHandle, httpContext, ((Number) dynConstraints[0]).shortValue() , ((Number) dynConstraints[1]).shortValue() );
      }
      return super.getDynamicStatement(cursor, context, remoteHandle, httpContext, dynConstraints);
   }

   public Cursor[] getCursors( )
   {
      return new Cursor[] {
          new ForEachCursor("P00022", "scmdbuf",false, GX_NOMASK + GX_MASKLOOPLOCK, false, this,100,0,false )
      };
   }

   public void getResults( int cursor ,
                           IFieldGetter rslt ,
                           Object[] buf ) throws SQLException
   {
      switch ( cursor )
      {
            case 0 :
               ((short[]) buf[0])[0] = rslt.getShort(1) ;
               ((java.util.UUID[]) buf[1])[0] = rslt.getGUID(2) ;
               ((String[]) buf[2])[0] = rslt.getVarchar(3) ;
               ((short[]) buf[3])[0] = rslt.getShort(4) ;
               ((String[]) buf[4])[0] = rslt.getLongVarchar(5) ;
               ((String[]) buf[5])[0] = rslt.getLongVarchar(6) ;
               ((String[]) buf[6])[0] = rslt.getLongVarchar(7) ;
               ((java.util.Date[]) buf[7])[0] = rslt.getGXDateTime(8) ;
               return;
      }
   }

   public void setParameters( int cursor ,
                              IFieldSetter stmt ,
                              Object[] parms ) throws SQLException
   {
      short sIdx ;
      switch ( cursor )
      {
            case 0 :
               sIdx = (short)(0) ;
               if ( ((Number) parms[0]).byteValue() == 0 )
               {
                  sIdx = (short)(sIdx+1) ;
                  stmt.setShort(sIdx, ((Number) parms[1]).shortValue());
               }
               return;
      }
   }

}

