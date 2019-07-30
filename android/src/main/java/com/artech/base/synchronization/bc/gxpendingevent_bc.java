/*
               File: gxpendingevent_bc
        Description: Gx Pending Event
             Author: GeneXus Android Generator version 10_4_0-89519
       Generated on: March 18, 2015 13:55:8.2
       Program type: Callable routine
          Main DBMS: sqlite
*/

package com.artech.base.synchronization.bc;

import java.sql.SQLException;

import com.genexus.Application;
import com.genexus.CommonUtil;
import com.genexus.IGxSilentTrn;
import com.genexus.ModelContext;
import com.genexus.ModelContext;
import com.genexus.db.Cursor;
import com.genexus.db.DataStoreHelperBase;
import com.genexus.db.DataStoreProvider;
import com.genexus.db.ForEachCursor;
import com.genexus.db.IDataStoreProvider;
import com.genexus.db.IFieldGetter;
import com.genexus.db.IFieldSetter;
import com.genexus.db.ILocalDataStoreHelper;
import com.genexus.db.UpdateCursor;
import com.genexus.search.GXContentInfo;

public final  class gxpendingevent_bc extends com.genexus.dummy.GXSDPanel implements IGxSilentTrn
{
   public gxpendingevent_bc( int remoteHandle )
   {
      super( remoteHandle , new ModelContext( gxpendingevent_bc.class ));
   }

   public gxpendingevent_bc( int remoteHandle ,
                             ModelContext context )
   {
      super( remoteHandle , context);
   }

   public void inittrn( )
   {
   }

   public void getInsDefault( )
   {
      readRow011( ) ;
      standaloneNotModal( ) ;
      initializeNonKey011( ) ;
      standaloneModal( ) ;
      addRow011( ) ;
      Gx_mode = "INS" ;
   }

   public void afterTrn( )
   {
      if ( trnEnded == 1 )
      {
         trnEnded = 0 ;
         standaloneNotModal( ) ;
         standaloneModal( ) ;
         if ( ( CommonUtil.strcmp(Gx_mode, "INS") == 0 )  )
         {
            Z1PendingEventId = A1PendingEventId ;
            SetMode( "UPD") ;
         }
      }
   }

   public String toString( )
   {
      return "" ;
   }

   public boolean Reindex( )
   {
      return true ;
   }

   public void confirm_010( )
   {
      beforeValidate011( ) ;
      if ( AnyError == 0 )
      {
         if ( CommonUtil.strcmp(Gx_mode, "DLT") == 0 )
         {
            onDeleteControls011( ) ;
         }
         else
         {
            checkExtendedTable011( ) ;
            if ( AnyError == 0 )
            {
            }
            closeExtendedTableCursors011( ) ;
         }
      }
      if ( AnyError == 0 )
      {
      }
   }

   public void zm011( int GX_JID )
   {
      if ( ( GX_JID == 5 ) || ( GX_JID == 0 ) )
      {
         Z2PendingEventTimestamp = A2PendingEventTimestamp ;
         Z3PendingEventBC = A3PendingEventBC ;
         Z4PendingEventAction = A4PendingEventAction ;
         Z6PendingEventStatus = A6PendingEventStatus ;
      }
      if ( GX_JID == -5 )
      {
         Z1PendingEventId = A1PendingEventId ;
         Z2PendingEventTimestamp = A2PendingEventTimestamp ;
         Z3PendingEventBC = A3PendingEventBC ;
         Z4PendingEventAction = A4PendingEventAction ;
         Z6PendingEventStatus = A6PendingEventStatus ;
      }
   }

   public void standaloneNotModal( )
   {
   }

   public void standaloneModal( )
   {
      if ( ( CommonUtil.strcmp(Gx_mode, "INS") == 0 )  && java.util.UUID.fromString("00000000-0000-0000-0000-000000000000").equals(A1PendingEventId) )
      {
         A1PendingEventId = java.util.UUID.randomUUID( ) ;
      }
      if ( ( CommonUtil.strcmp(Gx_mode, "INS") == 0 ) && ( Gx_BScreen == 0 ) )
      {
      }
   }

   public void load011( )
   {
      /* Using cursor BC00013 */
      pr_default.execute(1, new Object[] {A1PendingEventId});
      if ( (pr_default.getStatus(1) != 101) )
      {
         RcdFound1 = (short)(1) ;
         A2PendingEventTimestamp = BC00013_A2PendingEventTimestamp[0] ;
         A3PendingEventBC = BC00013_A3PendingEventBC[0] ;
         A4PendingEventAction = BC00013_A4PendingEventAction[0] ;
         A5PendingEventData = BC00013_A5PendingEventData[0] ;
         A6PendingEventStatus = BC00013_A6PendingEventStatus[0] ;
         A7PendingEventErrors = BC00013_A7PendingEventErrors[0] ;
         A8PendingEventExtras = BC00013_A8PendingEventExtras[0] ;
         A9PendingEventFiles = BC00013_A9PendingEventFiles[0] ;
         zm011( -5) ;
      }
      pr_default.close(1);
      onLoadActions011( ) ;
   }

   public void onLoadActions011( )
   {
   }

   public void checkExtendedTable011( )
   {
      standaloneModal( ) ;
      if ( ! ( ( A4PendingEventAction == 1 ) || ( A4PendingEventAction == 2 ) || ( A4PendingEventAction == 3 ) ) )
      {
         httpContext.GX_msglist.addItem("Field Pending Event Action is out of range", "OutOfRange", 1, "");
         AnyError = (short)(1) ;
      }
      if ( ! ( ( A6PendingEventStatus == 1 ) || ( A6PendingEventStatus == 2 ) || ( A6PendingEventStatus == 3 ) || ( A6PendingEventStatus == 4 ) || ( A6PendingEventStatus == 5 ) || ( A6PendingEventStatus == 6 ) || ( A6PendingEventStatus == 7 ) || ( A6PendingEventStatus == 51 ) ) )
      {
         httpContext.GX_msglist.addItem("Field Pending Event Status is out of range", "OutOfRange", 1, "");
         AnyError = (short)(1) ;
      }
   }

   public void closeExtendedTableCursors011( )
   {
   }

   public void enableDisable( )
   {
   }

   public void getKey011( )
   {
      /* Using cursor BC00014 */
      pr_default.execute(2, new Object[] {A1PendingEventId});
      if ( (pr_default.getStatus(2) != 101) )
      {
         RcdFound1 = (short)(1) ;
      }
      else
      {
         RcdFound1 = (short)(0) ;
      }
      pr_default.close(2);
   }

   public void getByPrimaryKey( )
   {
      /* Using cursor BC00015 */
      pr_default.execute(3, new Object[] {A1PendingEventId});
      if ( (pr_default.getStatus(3) == 103) )
      {
         httpContext.GX_msglist.addItem(localUtil.getMessages().getMessage("GXM_lock", new Object[] {""}), "RecordIsLocked", 1, "");
         AnyError = (short)(1) ;
         return  ;
      }
      if ( (pr_default.getStatus(3) != 101) )
      {
         zm011( 5) ;
         RcdFound1 = (short)(1) ;
         A1PendingEventId = BC00015_A1PendingEventId[0] ;
         A2PendingEventTimestamp = BC00015_A2PendingEventTimestamp[0] ;
         A3PendingEventBC = BC00015_A3PendingEventBC[0] ;
         A4PendingEventAction = BC00015_A4PendingEventAction[0] ;
         A5PendingEventData = BC00015_A5PendingEventData[0] ;
         A6PendingEventStatus = BC00015_A6PendingEventStatus[0] ;
         A7PendingEventErrors = BC00015_A7PendingEventErrors[0] ;
         A8PendingEventExtras = BC00015_A8PendingEventExtras[0] ;
         A9PendingEventFiles = BC00015_A9PendingEventFiles[0] ;
         Z1PendingEventId = A1PendingEventId ;
         sMode1 = Gx_mode ;
         Gx_mode = "DSP" ;
         standaloneModal( ) ;
         load011( ) ;
         if ( AnyError == 1 )
         {
            RcdFound1 = (short)(0) ;
            initializeNonKey011( ) ;
         }
         Gx_mode = sMode1 ;
      }
      else
      {
         RcdFound1 = (short)(0) ;
         initializeNonKey011( ) ;
         sMode1 = Gx_mode ;
         Gx_mode = "DSP" ;
         standaloneModal( ) ;
         Gx_mode = sMode1 ;
      }
      pr_default.close(3);
   }

   public void getEqualNoModal( )
   {
      getKey011( ) ;
      if ( RcdFound1 == 0 )
      {
         Gx_mode = "INS" ;
      }
      else
      {
         Gx_mode = "UPD" ;
      }
      getByPrimaryKey( ) ;
   }

   public void insert_check( )
   {
      confirm_010( ) ;
   }

   public void update_check( )
   {
      insert_check( ) ;
   }

   public void delete_check( )
   {
      insert_check( ) ;
   }

   public void checkOptimisticConcurrency011( )
   {
      if ( CommonUtil.strcmp(Gx_mode, "INS") != 0 )
      {
         /* Using cursor BC00016 */
         pr_default.execute(4, new Object[] {A1PendingEventId});
         if ( (pr_default.getStatus(4) == 103) )
         {
            httpContext.GX_msglist.addItem(localUtil.getMessages().getMessage("GXM_lock", new Object[] {"GxPendingEvent"}), "RecordIsLocked", 1, "");
            AnyError = (short)(1) ;
            return  ;
         }
         if ( (pr_default.getStatus(4) == 101) || !( Z2PendingEventTimestamp.equals( BC00016_A2PendingEventTimestamp[0] ) ) || ( CommonUtil.strcmp(Z3PendingEventBC, BC00016_A3PendingEventBC[0]) != 0 ) || ( Z4PendingEventAction != BC00016_A4PendingEventAction[0] ) || ( Z6PendingEventStatus != BC00016_A6PendingEventStatus[0] ) )
         {
            httpContext.GX_msglist.addItem(localUtil.getMessages().getMessage("GXM_waschg", new Object[] {"GxPendingEvent"}), "RecordWasChanged", 1, "");
            AnyError = (short)(1) ;
            return  ;
         }
      }
   }

   public void insert011( )
   {
      beforeValidate011( ) ;
      if ( AnyError == 0 )
      {
         checkExtendedTable011( ) ;
      }
      if ( AnyError == 0 )
      {
         zm011( 0) ;
         checkOptimisticConcurrency011( ) ;
         if ( AnyError == 0 )
         {
            afterConfirm011( ) ;
              beforeInsert011( ) ;
              /* Using cursor BC00017 */
              pr_default.execute(5, new Object[] {A1PendingEventId, A2PendingEventTimestamp, A3PendingEventBC, Short.valueOf(A4PendingEventAction), A5PendingEventData, Short.valueOf(A6PendingEventStatus), A7PendingEventErrors, A8PendingEventExtras, A9PendingEventFiles});
              if ( (pr_default.getStatus(5) == 1) )
              {
                 httpContext.GX_msglist.addItem(localUtil.getMessages().getMessage("GXM_noupdate"), "DuplicatePrimaryKey", 1, "");
                 AnyError = (short)(1) ;
              }
              httpContext.GX_msglist.addItem(localUtil.getMessages().getMessage("GXM_sucadded"), 0, "", true);
         }
         else
         {
            load011( ) ;
         }
         endLevel011( ) ;
      }
      closeExtendedTableCursors011( ) ;
   }

   public void update011( )
   {
      beforeValidate011( ) ;
      if ( AnyError == 0 )
      {
		 checkExtendedTable011( ) ;
		 checkOptimisticConcurrency011( ) ;
		    afterConfirm011( ) ;
		       beforeUpdate011( ) ;
		          /* Using cursor BC00018 */
		  pr_default.execute(6, new Object[] {A2PendingEventTimestamp, A3PendingEventBC, Short.valueOf(A4PendingEventAction), A5PendingEventData, Short.valueOf(A6PendingEventStatus), A7PendingEventErrors, A8PendingEventExtras, A9PendingEventFiles, A1PendingEventId});
		  if ( (pr_default.getStatus(6) == 103) )
		  {
		     httpContext.GX_msglist.addItem(localUtil.getMessages().getMessage("GXM_lock", new Object[] {"GxPendingEvent"}), "RecordIsLocked", 1, "");
		     AnyError = (short)(1) ;
		  }
		  deferredUpdate011( ) ;
		     /* Start of After( update) rules */
		 /* End of After( update) rules */
		 if ( AnyError == 0 )
		 {
		    getByPrimaryKey( ) ;
		    httpContext.GX_msglist.addItem(localUtil.getMessages().getMessage("GXM_sucupdated"), 0, "", true);
		 }
		 endLevel011( ) ;
      }
      closeExtendedTableCursors011( ) ;
   }

   public void deferredUpdate011( )
   {
   }

   public void delete( )
   {
      Gx_mode = "DLT" ;
      beforeValidate011( ) ;
      if ( AnyError == 0 )
      {
         checkOptimisticConcurrency011( ) ;
      }
      if ( AnyError == 0 )
      {
		 onDeleteControls011( ) ;
		 afterConfirm011( ) ;
	    beforeDelete011( ) ;
		       /* No cascading delete specified. */
		   /* Using cursor BC00019 */
		   pr_default.execute(7, new Object[] {A1PendingEventId});
		      /* Start of After( delete) rules */
		  /* End of After( delete) rules */
		 httpContext.GX_msglist.addItem(localUtil.getMessages().getMessage("GXM_sucdeleted"), 0, "", true);
      }
      sMode1 = Gx_mode ;
      Gx_mode = "DLT" ;
      endLevel011( ) ;
      Gx_mode = sMode1 ;
   }

   public void onDeleteControls011( )
   {
      standaloneModal( ) ;
      /* No delete mode formulas found. */
   }

   public void endLevel011( )
   {
      if ( CommonUtil.strcmp(Gx_mode, "INS") != 0 )
      {
         pr_default.close(4);
      }
      if ( AnyError == 0 )
      {
         beforeComplete011( ) ;
      }
      if ( AnyError == 0 )
      {
         /* After transaction rules */
         /* Execute 'After Trn' event if defined. */
         trnEnded = 1 ;
      }
      else
      {
      }
   }

   public void scanKeyStart011( )
   {
      /* Using cursor BC000110 */
      pr_default.execute(8, new Object[] {A1PendingEventId});
      RcdFound1 = (short)(0) ;
      if ( (pr_default.getStatus(8) != 101) )
      {
         RcdFound1 = (short)(1) ;
         A1PendingEventId = BC000110_A1PendingEventId[0] ;
         A2PendingEventTimestamp = BC000110_A2PendingEventTimestamp[0] ;
         A3PendingEventBC = BC000110_A3PendingEventBC[0] ;
         A4PendingEventAction = BC000110_A4PendingEventAction[0] ;
         A5PendingEventData = BC000110_A5PendingEventData[0] ;
         A6PendingEventStatus = BC000110_A6PendingEventStatus[0] ;
         A7PendingEventErrors = BC000110_A7PendingEventErrors[0] ;
         A8PendingEventExtras = BC000110_A8PendingEventExtras[0] ;
         A9PendingEventFiles = BC000110_A9PendingEventFiles[0] ;
      }
      /* Load Subordinate Levels */
   }

   public void scanKeyNext011( )
   {
      /* Scan next routine */
      pr_default.readNext(8);
      RcdFound1 = (short)(0) ;
      scanKeyLoad011( ) ;
   }

   public void scanKeyLoad011( )
   {
      sMode1 = Gx_mode ;
      Gx_mode = "DSP" ;
      if ( (pr_default.getStatus(8) != 101) )
      {
         RcdFound1 = (short)(1) ;
         A1PendingEventId = BC000110_A1PendingEventId[0] ;
         A2PendingEventTimestamp = BC000110_A2PendingEventTimestamp[0] ;
         A3PendingEventBC = BC000110_A3PendingEventBC[0] ;
         A4PendingEventAction = BC000110_A4PendingEventAction[0] ;
         A5PendingEventData = BC000110_A5PendingEventData[0] ;
         A6PendingEventStatus = BC000110_A6PendingEventStatus[0] ;
         A7PendingEventErrors = BC000110_A7PendingEventErrors[0] ;
         A8PendingEventExtras = BC000110_A8PendingEventExtras[0] ;
         A9PendingEventFiles = BC000110_A9PendingEventFiles[0] ;
      }
      Gx_mode = sMode1 ;
   }

   public void scanKeyEnd011( )
   {
      pr_default.close(8);
   }

   public void afterConfirm011( )
   {
      /* After Confirm Rules */
   }

   public void beforeInsert011( )
   {
      /* Before Insert Rules */
   }

   public void beforeUpdate011( )
   {
      /* Before Update Rules */
   }

   public void beforeDelete011( )
   {
      /* Before Delete Rules */
   }

   public void beforeComplete011( )
   {
      /* Before Complete Rules */
   }

   public void beforeValidate011( )
   {
      /* Before Validate Rules */
   }

   public void disableAttributes011( )
   {
   }

   public void addRow011( )
   {
      VarsToRow1( bcGxPendingEvent) ;
   }

   public void readRow011( )
   {
      RowToVars1( bcGxPendingEvent, 1) ;
   }

   public void initializeNonKey011( )
   {
      A2PendingEventTimestamp = CommonUtil.resetTime( CommonUtil.nullDate() );
      A3PendingEventBC = "" ;
      A4PendingEventAction = (short)(0) ;
      A5PendingEventData = "" ;
      A6PendingEventStatus = (short)(0) ;
      A7PendingEventErrors = "" ;
      A8PendingEventExtras = "" ;
      A9PendingEventFiles = "" ;
   }

   public void initAll011( )
   {
      A1PendingEventId = java.util.UUID.randomUUID( ) ;
      initializeNonKey011( ) ;
   }

   public void standaloneModalInsert( )
   {
   }

   public void VarsToRow1( SdtGxPendingEvent obj1 )
   {
      obj1.setgxTv_SdtGxPendingEvent_Mode( Gx_mode );
      obj1.setgxTv_SdtGxPendingEvent_Pendingeventtimestamp( A2PendingEventTimestamp );
      obj1.setgxTv_SdtGxPendingEvent_Pendingeventbc( A3PendingEventBC );
      obj1.setgxTv_SdtGxPendingEvent_Pendingeventaction( A4PendingEventAction );
      obj1.setgxTv_SdtGxPendingEvent_Pendingeventdata( A5PendingEventData );
      obj1.setgxTv_SdtGxPendingEvent_Pendingeventstatus( A6PendingEventStatus );
      obj1.setgxTv_SdtGxPendingEvent_Pendingeventerrors( A7PendingEventErrors );
      obj1.setgxTv_SdtGxPendingEvent_Pendingeventextras( A8PendingEventExtras );
      obj1.setgxTv_SdtGxPendingEvent_Pendingeventfiles( A9PendingEventFiles );
      obj1.setgxTv_SdtGxPendingEvent_Pendingeventid( A1PendingEventId );
      obj1.setgxTv_SdtGxPendingEvent_Pendingeventid_Z( Z1PendingEventId );
      obj1.setgxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z( Z2PendingEventTimestamp );
      obj1.setgxTv_SdtGxPendingEvent_Pendingeventbc_Z( Z3PendingEventBC );
      obj1.setgxTv_SdtGxPendingEvent_Pendingeventaction_Z( Z4PendingEventAction );
      obj1.setgxTv_SdtGxPendingEvent_Pendingeventstatus_Z( Z6PendingEventStatus );
      obj1.setgxTv_SdtGxPendingEvent_Mode( Gx_mode );
   }

   public void KeyVarsToRow1( SdtGxPendingEvent obj1 )
   {
      obj1.setgxTv_SdtGxPendingEvent_Pendingeventid( A1PendingEventId );
   }

   public void RowToVars1( SdtGxPendingEvent obj1 ,
                           int forceLoad )
   {
      Gx_mode = obj1.getgxTv_SdtGxPendingEvent_Mode() ;
      A2PendingEventTimestamp = obj1.getgxTv_SdtGxPendingEvent_Pendingeventtimestamp() ;
      A3PendingEventBC = obj1.getgxTv_SdtGxPendingEvent_Pendingeventbc() ;
      A4PendingEventAction = obj1.getgxTv_SdtGxPendingEvent_Pendingeventaction() ;
      A5PendingEventData = obj1.getgxTv_SdtGxPendingEvent_Pendingeventdata() ;
      A6PendingEventStatus = obj1.getgxTv_SdtGxPendingEvent_Pendingeventstatus() ;
      A7PendingEventErrors = obj1.getgxTv_SdtGxPendingEvent_Pendingeventerrors() ;
      A8PendingEventExtras = obj1.getgxTv_SdtGxPendingEvent_Pendingeventextras() ;
      A9PendingEventFiles = obj1.getgxTv_SdtGxPendingEvent_Pendingeventfiles() ;
      A1PendingEventId = obj1.getgxTv_SdtGxPendingEvent_Pendingeventid() ;
      Z1PendingEventId = obj1.getgxTv_SdtGxPendingEvent_Pendingeventid_Z() ;
      Z2PendingEventTimestamp = obj1.getgxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z() ;
      Z3PendingEventBC = obj1.getgxTv_SdtGxPendingEvent_Pendingeventbc_Z() ;
      Z4PendingEventAction = obj1.getgxTv_SdtGxPendingEvent_Pendingeventaction_Z() ;
      Z6PendingEventStatus = obj1.getgxTv_SdtGxPendingEvent_Pendingeventstatus_Z() ;
      Gx_mode = obj1.getgxTv_SdtGxPendingEvent_Mode() ;
   }

   public void LoadKey( Object[] obj )
   {
      BackMsgLst = httpContext.GX_msglist ;
      httpContext.GX_msglist = LclMsgLst ;
      A1PendingEventId = (java.util.UUID)getParm(obj,0) ;
      AnyError = (short)(0) ;
      httpContext.GX_msglist.removeAllItems();
      initializeNonKey011( ) ;
      scanKeyStart011( ) ;
      if ( RcdFound1 == 0 )
      {
         Gx_mode = "INS" ;
      }
      else
      {
         Gx_mode = "UPD" ;
         Z1PendingEventId = A1PendingEventId ;
      }
      zm011( -5) ;
      onLoadActions011( ) ;
      addRow011( ) ;
      scanKeyEnd011( ) ;
      if ( RcdFound1 == 0 )
      {
         httpContext.GX_msglist.addItem(localUtil.getMessages().getMessage("GXM_keynfound"), "PrimaryKeyNotFound", 1, "");
         AnyError = (short)(1) ;
      }
      httpContext.GX_msglist = BackMsgLst ;
   }

   public void Load( )
   {
      AnyError = (short)(0) ;
      httpContext.GX_msglist.removeAllItems();
      BackMsgLst = httpContext.GX_msglist ;
      httpContext.GX_msglist = LclMsgLst ;
      RowToVars1( bcGxPendingEvent, 0) ;
      scanKeyStart011( ) ;
      if ( RcdFound1 == 0 )
      {
         Gx_mode = "INS" ;
      }
      else
      {
         Gx_mode = "UPD" ;
         Z1PendingEventId = A1PendingEventId ;
      }
      zm011( -5) ;
      onLoadActions011( ) ;
      addRow011( ) ;
      scanKeyEnd011( ) ;
      if ( RcdFound1 == 0 )
      {
         httpContext.GX_msglist.addItem(localUtil.getMessages().getMessage("GXM_keynfound"), "PrimaryKeyNotFound", 1, "");
         AnyError = (short)(1) ;
      }
      httpContext.GX_msglist = BackMsgLst ;
   }

   public void Save( )
   {
      BackMsgLst = httpContext.GX_msglist ;
      httpContext.GX_msglist = LclMsgLst ;
      AnyError = (short)(0) ;
      httpContext.GX_msglist.removeAllItems();
      RowToVars1( bcGxPendingEvent, 0) ;
      getKey011( ) ;
      if ( RcdFound1 == 1 )
      {
         if ( CommonUtil.strcmp(Gx_mode, "INS") == 0 )
         {
            httpContext.GX_msglist.addItem(localUtil.getMessages().getMessage("GXM_noupdate"), "DuplicatePrimaryKey", 1, "");
            AnyError = (short)(1) ;
         }
         else if ( !( A1PendingEventId.equals( Z1PendingEventId ) ) )
         {
            A1PendingEventId = Z1PendingEventId ;
            httpContext.GX_msglist.addItem(localUtil.getMessages().getMessage("GXM_getbeforeupd"), "CandidateKeyNotFound", 1, "");
            AnyError = (short)(1) ;
         }
         else if ( CommonUtil.strcmp(Gx_mode, "DLT") == 0 )
         {
            delete( ) ;
            afterTrn( ) ;
         }
         else
         {
            Gx_mode = "UPD" ;
            /* Update record */
            update011( ) ;
         }
      }
      else
      {
         if ( CommonUtil.strcmp(Gx_mode, "DLT") == 0 )
         {
            httpContext.GX_msglist.addItem(localUtil.getMessages().getMessage("GXM_getbeforeupd"), "CandidateKeyNotFound", 1, "");
            AnyError = (short)(1) ;
         }
         else
         {
            if ( !( A1PendingEventId.equals( Z1PendingEventId ) ) )
            {
               if ( CommonUtil.strcmp(Gx_mode, "UPD") == 0 )
               {
                  httpContext.GX_msglist.addItem(localUtil.getMessages().getMessage("GXM_getbeforeupd"), "DuplicatePrimaryKey", 1, "");
                  AnyError = (short)(1) ;
               }
               else
               {
                  Gx_mode = "INS" ;
                  /* Insert record */
                  insert011( ) ;
               }
            }
            else
            {
               if ( CommonUtil.strcmp(Gx_mode, "UPD") == 0 )
               {
                  httpContext.GX_msglist.addItem(localUtil.getMessages().getMessage("GXM_recdeleted"), 1, "");
                  AnyError = (short)(1) ;
               }
               else
               {
                  Gx_mode = "INS" ;
                  /* Insert record */
                  insert011( ) ;
               }
            }
         }
      }
      afterTrn( ) ;
      VarsToRow1( bcGxPendingEvent) ;
      httpContext.GX_msglist = BackMsgLst ;
   }

   public boolean Insert( )
   {
      BackMsgLst = httpContext.GX_msglist ;
      httpContext.GX_msglist = LclMsgLst ;
      AnyError = (short)(0) ;
      httpContext.GX_msglist.removeAllItems();
      RowToVars1( bcGxPendingEvent, 0) ;
      /* Insert record */
      insert011( ) ;
      VarsToRow1( bcGxPendingEvent) ;
      httpContext.GX_msglist = BackMsgLst ;
      return (AnyError==0) ;
   }

   public boolean Update( )
   {
      BackMsgLst = httpContext.GX_msglist ;
      httpContext.GX_msglist = LclMsgLst ;
      AnyError = (short)(0) ;
      httpContext.GX_msglist.removeAllItems();
      RowToVars1( bcGxPendingEvent, 0) ;
      if ( CommonUtil.strcmp(Gx_mode, "UPD") == 0 )
      {
         Save( ) ;
      }
      else
      {
         SdtGxPendingEvent auxBC = new SdtGxPendingEvent( remoteHandle, context) ;
         auxBC.Load(A1PendingEventId);
         auxBC.updateDirties(bcGxPendingEvent);
         auxBC.Save();
         afterTrn( ) ;
      }
      VarsToRow1( bcGxPendingEvent) ;
      httpContext.GX_msglist = BackMsgLst ;
      return (AnyError==0) ;
   }

   public boolean InsertOrUpdate( )
   {
      BackMsgLst = httpContext.GX_msglist ;
      httpContext.GX_msglist = LclMsgLst ;
      AnyError = (short)(0) ;
      httpContext.GX_msglist.removeAllItems();
      RowToVars1( bcGxPendingEvent, 0) ;
      Insert( ) ;
      if ( AnyError == 1 )
      {
         Update( ) ;
      }
      VarsToRow1( bcGxPendingEvent) ;
      httpContext.GX_msglist = BackMsgLst ;
      return (AnyError==0) ;
   }

   public void Check( )
   {
      BackMsgLst = httpContext.GX_msglist ;
      httpContext.GX_msglist = LclMsgLst ;
      AnyError = (short)(0) ;
      httpContext.GX_msglist.removeAllItems();
      RowToVars1( bcGxPendingEvent, 0) ;
      getKey011( ) ;
      if ( RcdFound1 == 1 )
      {
         if ( CommonUtil.strcmp(Gx_mode, "INS") == 0 )
         {
            httpContext.GX_msglist.addItem(localUtil.getMessages().getMessage("GXM_noupdate"), "DuplicatePrimaryKey", 1, "");
            AnyError = (short)(1) ;
         }
         else if ( !( A1PendingEventId.equals( Z1PendingEventId ) ) )
         {
            A1PendingEventId = Z1PendingEventId ;
            httpContext.GX_msglist.addItem(localUtil.getMessages().getMessage("GXM_getbeforeupd"), "DuplicatePrimaryKey", 1, "");
            AnyError = (short)(1) ;
         }
         else if ( CommonUtil.strcmp(Gx_mode, "DLT") == 0 )
         {
            delete_check( ) ;
         }
         else
         {
            Gx_mode = "UPD" ;
            update_check( ) ;
         }
      }
      else
      {
         if ( !( A1PendingEventId.equals( Z1PendingEventId ) ) )
         {
            Gx_mode = "INS" ;
            insert_check( ) ;
         }
         else
         {
            if ( CommonUtil.strcmp(Gx_mode, "UPD") == 0 )
            {
               httpContext.GX_msglist.addItem(localUtil.getMessages().getMessage("GXM_recdeleted"), 1, "");
               AnyError = (short)(1) ;
            }
            else
            {
               Gx_mode = "INS" ;
               insert_check( ) ;
            }
         }
      }
      Application.rollback(context, remoteHandle, "DEFAULT", null, "gxpendingevent_bc");
      VarsToRow1( bcGxPendingEvent) ;
      httpContext.GX_msglist = BackMsgLst ;
   }

   public int Errors( )
   {
      if ( AnyError == 0 )
      {
         return 0 ;
      }
      return 1 ;
   }

   public com.genexus.internet.MsgList GetMessages( )
   {
      return LclMsgLst ;
   }

   public String GetMode( )
   {
      Gx_mode = bcGxPendingEvent.getgxTv_SdtGxPendingEvent_Mode() ;
      return Gx_mode ;
   }

   public void SetMode( String lMode )
   {
      Gx_mode = lMode ;
      bcGxPendingEvent.setgxTv_SdtGxPendingEvent_Mode( Gx_mode );
   }

   public void SetSDT( SdtGxPendingEvent sdt ,
                       byte sdtToBc )
   {
      if ( sdt != bcGxPendingEvent )
      {
         bcGxPendingEvent = sdt ;
         if ( CommonUtil.strcmp(bcGxPendingEvent.getgxTv_SdtGxPendingEvent_Mode(), "") == 0 )
         {
            bcGxPendingEvent.setgxTv_SdtGxPendingEvent_Mode( "INS" );
         }
         if ( sdtToBc == 1 )
         {
            VarsToRow1( bcGxPendingEvent) ;
         }
         else
         {
            RowToVars1( bcGxPendingEvent, 1) ;
         }
      }
      else
      {
         if ( CommonUtil.strcmp(bcGxPendingEvent.getgxTv_SdtGxPendingEvent_Mode(), "") == 0 )
         {
            bcGxPendingEvent.setgxTv_SdtGxPendingEvent_Mode( "INS" );
         }
      }
   }

   public void ReloadFromSDT( )
   {
      RowToVars1( bcGxPendingEvent, 1) ;
   }

   public void ForceCommitOnExit()
   {
   }

   public SdtGxPendingEvent getGxPendingEvent_BC( )
   {
      return bcGxPendingEvent ;
   }


   protected void cleanup( )
   {
      super.cleanup();
      CloseOpenCursors();
   }

   protected void CloseOpenCursors( )
   {
   }

   /* Aggregate/select formulas */
   public void initialize( )
   {
      Gx_mode = "" ;
      Z1PendingEventId = java.util.UUID.fromString("00000000-0000-0000-0000-000000000000") ;
      A1PendingEventId = java.util.UUID.fromString("00000000-0000-0000-0000-000000000000") ;
      Z2PendingEventTimestamp = CommonUtil.resetTime( CommonUtil.nullDate() );
      A2PendingEventTimestamp = CommonUtil.resetTime( CommonUtil.nullDate() );
      Z3PendingEventBC = "" ;
      A3PendingEventBC = "" ;
      A5PendingEventData = "" ;
      A7PendingEventErrors = "" ;
      A8PendingEventExtras = "" ;
      A9PendingEventFiles = "" ;
      BC00013_A1PendingEventId = new java.util.UUID[] {java.util.UUID.fromString("00000000-0000-0000-0000-000000000000")} ;
      BC00013_A2PendingEventTimestamp = new java.util.Date[] {CommonUtil.nullDate()} ;
      BC00013_A3PendingEventBC = new String[] {""} ;
      BC00013_A4PendingEventAction = new short[1] ;
      BC00013_A5PendingEventData = new String[] {""} ;
      BC00013_A6PendingEventStatus = new short[1] ;
      BC00013_A7PendingEventErrors = new String[] {""} ;
      BC00013_A8PendingEventExtras = new String[] {""} ;
      BC00013_A9PendingEventFiles = new String[] {""} ;
      BC00014_A1PendingEventId = new java.util.UUID[] {java.util.UUID.fromString("00000000-0000-0000-0000-000000000000")} ;
      BC00015_A1PendingEventId = new java.util.UUID[] {java.util.UUID.fromString("00000000-0000-0000-0000-000000000000")} ;
      BC00015_A2PendingEventTimestamp = new java.util.Date[] {CommonUtil.nullDate()} ;
      BC00015_A3PendingEventBC = new String[] {""} ;
      BC00015_A4PendingEventAction = new short[1] ;
      BC00015_A5PendingEventData = new String[] {""} ;
      BC00015_A6PendingEventStatus = new short[1] ;
      BC00015_A7PendingEventErrors = new String[] {""} ;
      BC00015_A8PendingEventExtras = new String[] {""} ;
      BC00015_A9PendingEventFiles = new String[] {""} ;
      sMode1 = "" ;
      BC00016_A1PendingEventId = new java.util.UUID[] {java.util.UUID.fromString("00000000-0000-0000-0000-000000000000")} ;
      BC00016_A2PendingEventTimestamp = new java.util.Date[] {CommonUtil.nullDate()} ;
      BC00016_A3PendingEventBC = new String[] {""} ;
      BC00016_A4PendingEventAction = new short[1] ;
      BC00016_A5PendingEventData = new String[] {""} ;
      BC00016_A6PendingEventStatus = new short[1] ;
      BC00016_A7PendingEventErrors = new String[] {""} ;
      BC00016_A8PendingEventExtras = new String[] {""} ;
      BC00016_A9PendingEventFiles = new String[] {""} ;
      BC000110_A1PendingEventId = new java.util.UUID[] {java.util.UUID.fromString("00000000-0000-0000-0000-000000000000")} ;
      BC000110_A2PendingEventTimestamp = new java.util.Date[] {CommonUtil.nullDate()} ;
      BC000110_A3PendingEventBC = new String[] {""} ;
      BC000110_A4PendingEventAction = new short[1] ;
      BC000110_A5PendingEventData = new String[] {""} ;
      BC000110_A6PendingEventStatus = new short[1] ;
      BC000110_A7PendingEventErrors = new String[] {""} ;
      BC000110_A8PendingEventExtras = new String[] {""} ;
      BC000110_A9PendingEventFiles = new String[] {""} ;
      BackMsgLst = new com.genexus.internet.MsgList();
      LclMsgLst = new com.genexus.internet.MsgList();
      pr_default = new DataStoreProvider(context, remoteHandle, new gxpendingevent_bc__default(),
         new Object[] {
             new Object[] {
            BC00012_A1PendingEventId, BC00012_A2PendingEventTimestamp, BC00012_A3PendingEventBC, BC00012_A4PendingEventAction, BC00012_A5PendingEventData, BC00012_A6PendingEventStatus, BC00012_A7PendingEventErrors, BC00012_A8PendingEventExtras, BC00012_A9PendingEventFiles
            }
            , new Object[] {
            BC00013_A1PendingEventId, BC00013_A2PendingEventTimestamp, BC00013_A3PendingEventBC, BC00013_A4PendingEventAction, BC00013_A5PendingEventData, BC00013_A6PendingEventStatus, BC00013_A7PendingEventErrors, BC00013_A8PendingEventExtras, BC00013_A9PendingEventFiles
            }
            , new Object[] {
            BC00014_A1PendingEventId
            }
            , new Object[] {
            BC00015_A1PendingEventId, BC00015_A2PendingEventTimestamp, BC00015_A3PendingEventBC, BC00015_A4PendingEventAction, BC00015_A5PendingEventData, BC00015_A6PendingEventStatus, BC00015_A7PendingEventErrors, BC00015_A8PendingEventExtras, BC00015_A9PendingEventFiles
            }
            , new Object[] {
            BC00016_A1PendingEventId, BC00016_A2PendingEventTimestamp, BC00016_A3PendingEventBC, BC00016_A4PendingEventAction, BC00016_A5PendingEventData, BC00016_A6PendingEventStatus, BC00016_A7PendingEventErrors, BC00016_A8PendingEventExtras, BC00016_A9PendingEventFiles
            }
            , new Object[] {
            }
            , new Object[] {
            }
            , new Object[] {
            }
            , new Object[] {
            BC000110_A1PendingEventId, BC000110_A2PendingEventTimestamp, BC000110_A3PendingEventBC, BC000110_A4PendingEventAction, BC000110_A5PendingEventData, BC000110_A6PendingEventStatus, BC000110_A7PendingEventErrors, BC000110_A8PendingEventExtras, BC000110_A9PendingEventFiles
            }
         }
      );
      Z1PendingEventId = java.util.UUID.randomUUID( ) ;
      /* Execute Start event if defined. */
      standaloneNotModal( ) ;
   }

   private byte Gx_BScreen ;
   private short AnyError ;
   private short Z4PendingEventAction ;
   private short A4PendingEventAction ;
   private short Z6PendingEventStatus ;
   private short A6PendingEventStatus ;
   private short RcdFound1 ;
   private int trnEnded ;
   private String Gx_mode ;
   private String sMode1 ;
   private java.util.Date Z2PendingEventTimestamp ;
   private java.util.Date A2PendingEventTimestamp ;
   private String A5PendingEventData ;
   private String A7PendingEventErrors ;
   private String A8PendingEventExtras ;
   private String A9PendingEventFiles ;
   private String Z3PendingEventBC ;
   private String A3PendingEventBC ;
   private java.util.UUID Z1PendingEventId ;
   private java.util.UUID A1PendingEventId ;
   private com.genexus.internet.MsgList BackMsgLst ;
   private com.genexus.internet.MsgList LclMsgLst ;
   private SdtGxPendingEvent bcGxPendingEvent ;
   private IDataStoreProvider pr_default ;
   private java.util.UUID[] BC00013_A1PendingEventId ;
   private java.util.Date[] BC00013_A2PendingEventTimestamp ;
   private String[] BC00013_A3PendingEventBC ;
   private short[] BC00013_A4PendingEventAction ;
   private String[] BC00013_A5PendingEventData ;
   private short[] BC00013_A6PendingEventStatus ;
   private String[] BC00013_A7PendingEventErrors ;
   private String[] BC00013_A8PendingEventExtras ;
   private String[] BC00013_A9PendingEventFiles ;
   private java.util.UUID[] BC00014_A1PendingEventId ;
   private java.util.UUID[] BC00015_A1PendingEventId ;
   private java.util.Date[] BC00015_A2PendingEventTimestamp ;
   private String[] BC00015_A3PendingEventBC ;
   private short[] BC00015_A4PendingEventAction ;
   private String[] BC00015_A5PendingEventData ;
   private short[] BC00015_A6PendingEventStatus ;
   private String[] BC00015_A7PendingEventErrors ;
   private String[] BC00015_A8PendingEventExtras ;
   private String[] BC00015_A9PendingEventFiles ;
   private java.util.UUID[] BC00016_A1PendingEventId ;
   private java.util.Date[] BC00016_A2PendingEventTimestamp ;
   private String[] BC00016_A3PendingEventBC ;
   private short[] BC00016_A4PendingEventAction ;
   private String[] BC00016_A5PendingEventData ;
   private short[] BC00016_A6PendingEventStatus ;
   private String[] BC00016_A7PendingEventErrors ;
   private String[] BC00016_A8PendingEventExtras ;
   private String[] BC00016_A9PendingEventFiles ;
   private java.util.UUID[] BC000110_A1PendingEventId ;
   private java.util.Date[] BC000110_A2PendingEventTimestamp ;
   private String[] BC000110_A3PendingEventBC ;
   private short[] BC000110_A4PendingEventAction ;
   private String[] BC000110_A5PendingEventData ;
   private short[] BC000110_A6PendingEventStatus ;
   private String[] BC000110_A7PendingEventErrors ;
   private String[] BC000110_A8PendingEventExtras ;
   private String[] BC000110_A9PendingEventFiles ;
   private java.util.UUID[] BC00012_A1PendingEventId ;
   private java.util.Date[] BC00012_A2PendingEventTimestamp ;
   private String[] BC00012_A3PendingEventBC ;
   private short[] BC00012_A4PendingEventAction ;
   private String[] BC00012_A5PendingEventData ;
   private short[] BC00012_A6PendingEventStatus ;
   private String[] BC00012_A7PendingEventErrors ;
   private String[] BC00012_A8PendingEventExtras ;
   private String[] BC00012_A9PendingEventFiles ;

    @Override
	public void submit(int submitId, Object[] submitParms, ModelContext ctx) {
	}
	

}

final  class gxpendingevent_bc__default extends DataStoreHelperBase implements ILocalDataStoreHelper
{
   public Cursor[] getCursors( )
   {
      return new Cursor[] {
          new ForEachCursor("BC00012", "SELECT [PendingEventId], [PendingEventTimestamp], [PendingEventBC], [PendingEventAction], [PendingEventData], [PendingEventStatus], [PendingEventErrors], [PendingEventExtras], [PendingEventFiles] FROM [GxPendingEvent] WHERE [PendingEventId] = ? ",true, GX_NOMASK, false, this,1,0,false )
         ,new ForEachCursor("BC00013", "SELECT TM1.[PendingEventId], TM1.[PendingEventTimestamp], TM1.[PendingEventBC], TM1.[PendingEventAction], TM1.[PendingEventData], TM1.[PendingEventStatus], TM1.[PendingEventErrors], TM1.[PendingEventExtras], TM1.[PendingEventFiles] FROM [GxPendingEvent] TM1 WHERE TM1.[PendingEventId] = ? ORDER BY TM1.[PendingEventId] ",true, GX_NOMASK, false, this,100,0,false )
         ,new ForEachCursor("BC00014", "SELECT [PendingEventId] FROM [GxPendingEvent] WHERE [PendingEventId] = ? ",true, GX_NOMASK, false, this,1,0,false )
         ,new ForEachCursor("BC00015", "SELECT [PendingEventId], [PendingEventTimestamp], [PendingEventBC], [PendingEventAction], [PendingEventData], [PendingEventStatus], [PendingEventErrors], [PendingEventExtras], [PendingEventFiles] FROM [GxPendingEvent] WHERE [PendingEventId] = ? ",true, GX_NOMASK, false, this,1,0,false )
         ,new ForEachCursor("BC00016", "SELECT [PendingEventId], [PendingEventTimestamp], [PendingEventBC], [PendingEventAction], [PendingEventData], [PendingEventStatus], [PendingEventErrors], [PendingEventExtras], [PendingEventFiles] FROM [GxPendingEvent] WHERE [PendingEventId] = ? ",true, GX_NOMASK, false, this,1,0,false )
         ,new UpdateCursor("BC00017", "INSERT INTO [GxPendingEvent]([PendingEventId], [PendingEventTimestamp], [PendingEventBC], [PendingEventAction], [PendingEventData], [PendingEventStatus], [PendingEventErrors], [PendingEventExtras], [PendingEventFiles]) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)", GX_NOMASK)
         ,new UpdateCursor("BC00018", "UPDATE [GxPendingEvent] SET [PendingEventTimestamp]=?, [PendingEventBC]=?, [PendingEventAction]=?, [PendingEventData]=?, [PendingEventStatus]=?, [PendingEventErrors]=?, [PendingEventExtras]=?, [PendingEventFiles]=?  WHERE [PendingEventId] = ?", GX_NOMASK)
         ,new UpdateCursor("BC00019", "DELETE FROM [GxPendingEvent]  WHERE [PendingEventId] = ?", GX_NOMASK)
         ,new ForEachCursor("BC000110", "SELECT TM1.[PendingEventId], TM1.[PendingEventTimestamp], TM1.[PendingEventBC], TM1.[PendingEventAction], TM1.[PendingEventData], TM1.[PendingEventStatus], TM1.[PendingEventErrors], TM1.[PendingEventExtras], TM1.[PendingEventFiles] FROM [GxPendingEvent] TM1 WHERE TM1.[PendingEventId] = ? ORDER BY TM1.[PendingEventId] ",true, GX_NOMASK, false, this,100,0,false )
      };
   }

   public void getResults( int cursor ,
                           IFieldGetter rslt ,
                           Object[] buf ) throws SQLException
   {
      switch ( cursor )
      {
            case 0 :
               ((java.util.UUID[]) buf[0])[0] = rslt.getGUID(1) ;
               ((java.util.Date[]) buf[1])[0] = rslt.getGXDateTime(2) ;
               ((String[]) buf[2])[0] = rslt.getVarchar(3) ;
               ((short[]) buf[3])[0] = rslt.getShort(4) ;
               ((String[]) buf[4])[0] = rslt.getLongVarchar(5) ;
               ((short[]) buf[5])[0] = rslt.getShort(6) ;
               ((String[]) buf[6])[0] = rslt.getLongVarchar(7) ;
               ((String[]) buf[7])[0] = rslt.getLongVarchar(8) ;
               ((String[]) buf[8])[0] = rslt.getLongVarchar(9) ;
               return;
            case 1 :
               ((java.util.UUID[]) buf[0])[0] = rslt.getGUID(1) ;
               ((java.util.Date[]) buf[1])[0] = rslt.getGXDateTime(2) ;
               ((String[]) buf[2])[0] = rslt.getVarchar(3) ;
               ((short[]) buf[3])[0] = rslt.getShort(4) ;
               ((String[]) buf[4])[0] = rslt.getLongVarchar(5) ;
               ((short[]) buf[5])[0] = rslt.getShort(6) ;
               ((String[]) buf[6])[0] = rslt.getLongVarchar(7) ;
               ((String[]) buf[7])[0] = rslt.getLongVarchar(8) ;
               ((String[]) buf[8])[0] = rslt.getLongVarchar(9) ;
               return;
            case 2 :
               ((java.util.UUID[]) buf[0])[0] = rslt.getGUID(1) ;
               return;
            case 3 :
               ((java.util.UUID[]) buf[0])[0] = rslt.getGUID(1) ;
               ((java.util.Date[]) buf[1])[0] = rslt.getGXDateTime(2) ;
               ((String[]) buf[2])[0] = rslt.getVarchar(3) ;
               ((short[]) buf[3])[0] = rslt.getShort(4) ;
               ((String[]) buf[4])[0] = rslt.getLongVarchar(5) ;
               ((short[]) buf[5])[0] = rslt.getShort(6) ;
               ((String[]) buf[6])[0] = rslt.getLongVarchar(7) ;
               ((String[]) buf[7])[0] = rslt.getLongVarchar(8) ;
               ((String[]) buf[8])[0] = rslt.getLongVarchar(9) ;
               return;
            case 4 :
               ((java.util.UUID[]) buf[0])[0] = rslt.getGUID(1) ;
               ((java.util.Date[]) buf[1])[0] = rslt.getGXDateTime(2) ;
               ((String[]) buf[2])[0] = rslt.getVarchar(3) ;
               ((short[]) buf[3])[0] = rslt.getShort(4) ;
               ((String[]) buf[4])[0] = rslt.getLongVarchar(5) ;
               ((short[]) buf[5])[0] = rslt.getShort(6) ;
               ((String[]) buf[6])[0] = rslt.getLongVarchar(7) ;
               ((String[]) buf[7])[0] = rslt.getLongVarchar(8) ;
               ((String[]) buf[8])[0] = rslt.getLongVarchar(9) ;
               return;
            case 8 :
               ((java.util.UUID[]) buf[0])[0] = rslt.getGUID(1) ;
               ((java.util.Date[]) buf[1])[0] = rslt.getGXDateTime(2) ;
               ((String[]) buf[2])[0] = rslt.getVarchar(3) ;
               ((short[]) buf[3])[0] = rslt.getShort(4) ;
               ((String[]) buf[4])[0] = rslt.getLongVarchar(5) ;
               ((short[]) buf[5])[0] = rslt.getShort(6) ;
               ((String[]) buf[6])[0] = rslt.getLongVarchar(7) ;
               ((String[]) buf[7])[0] = rslt.getLongVarchar(8) ;
               ((String[]) buf[8])[0] = rslt.getLongVarchar(9) ;
               return;
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
            case 1 :
               stmt.setGUID(1, (java.util.UUID)parms[0]);
               return;
            case 2 :
               stmt.setGUID(1, (java.util.UUID)parms[0]);
               return;
            case 3 :
               stmt.setGUID(1, (java.util.UUID)parms[0]);
               return;
            case 4 :
               stmt.setGUID(1, (java.util.UUID)parms[0]);
               return;
            case 5 :
               stmt.setGUID(1, (java.util.UUID)parms[0]);
               stmt.setDateTime(2, (java.util.Date)parms[1], false);
               stmt.setVarchar(3, (String)parms[2], 128, false);
               stmt.setShort(4, ((Number) parms[3]).shortValue());
               stmt.setLongVarchar(5, (String)parms[4], false);
               stmt.setShort(6, ((Number) parms[5]).shortValue());
               stmt.setLongVarchar(7, (String)parms[6], false);
               stmt.setLongVarchar(8, (String)parms[7], false);
               stmt.setLongVarchar(9, (String)parms[8], false);
               return;
            case 6 :
               stmt.setDateTime(1, (java.util.Date)parms[0], false);
               stmt.setVarchar(2, (String)parms[1], 128, false);
               stmt.setShort(3, ((Number) parms[2]).shortValue());
               stmt.setLongVarchar(4, (String)parms[3], false);
               stmt.setShort(5, ((Number) parms[4]).shortValue());
               stmt.setLongVarchar(6, (String)parms[5], false);
               stmt.setLongVarchar(7, (String)parms[6], false);
               stmt.setLongVarchar(8, (String)parms[7], false);
               stmt.setGUID(9, (java.util.UUID)parms[8]);
               return;
            case 7 :
               stmt.setGUID(1, (java.util.UUID)parms[0]);
               return;
            case 8 :
               stmt.setGUID(1, (java.util.UUID)parms[0]);
               return;
      }
   }

}

