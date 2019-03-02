package com.artech.base.synchronization.dbcreate;

import java.sql.*;
import com.genexus.db.*;
import com.genexus.*;
import com.genexus.util.*;

public final  class reorg extends GXProcedure
{
   public reorg( int remoteHandle )
   {
      super( remoteHandle , new ModelContext( reorg.class ), "" );
   }

   public reorg( int remoteHandle ,
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
      SetCreateDataBase( ) ;
      DBConnectionManager.StartCreateDataBase( ) ;
      CreateDataBase( ) ;
      if ( previousCheck() )
      {
         executeReorganization( ) ;
      }
   }

   private void CreateDataBase( )
   {
      /* Create database is not generated for SQLite databases */
   }

   private void FirstActions( )
   {
      /* Load data into tables. */
   }

   public void CreateGxPendingEvent( ) throws SQLException
   {
      String cmdBuffer ;
      GXReorganization.addMsg( localUtil.getMessages().getMessage("GXM_filecrea", new Object[] {"GxPendingEvent",""}) );
      /* Indices for table GxPendingEvent */
      GXReorganization.addMsg( localUtil.getMessages().getMessage("GXM_creaindx", new Object[] {"IPENDINGEVENT"}) );
      try
      {
         cmdBuffer = " CREATE TABLE [GxPendingEvent] ([PendingEventId] CHAR(36) NOT NULL , [PendingEventTimestamp] ";
         cmdBuffer += "  TEXT NOT NULL , [PendingEventBC] TEXT COLLATE RTRIM NOT NULL , [PendingEventAction] ";
         cmdBuffer += "  INTEGER NOT NULL , [PendingEventData] TEXT COLLATE RTRIM NOT NULL , [PendingEventStatus] ";
         cmdBuffer += "  INTEGER NOT NULL , [PendingEventErrors] TEXT COLLATE RTRIM NOT NULL , [PendingEventExtras] ";
         cmdBuffer += "  TEXT COLLATE RTRIM NOT NULL , [PendingEventFiles] TEXT COLLATE RTRIM NOT NULL , ";
         cmdBuffer += "  PRIMARY KEY([PendingEventId]))  ";
         ExecuteDirectSQL.executeWithThrow(context, remoteHandle, "DEFAULT", cmdBuffer) ;
      }
      catch(SQLException ex)
      {
         cmdBuffer = " DROP TABLE [GxPendingEvent] ";
         ExecuteDirectSQL.executeWithThrow(context, remoteHandle, "DEFAULT", cmdBuffer) ;
         cmdBuffer = " CREATE TABLE [GxPendingEvent] ([PendingEventId] CHAR(36) NOT NULL , [PendingEventTimestamp] ";
         cmdBuffer += "  TEXT NOT NULL , [PendingEventBC] TEXT COLLATE RTRIM NOT NULL , [PendingEventAction] ";
         cmdBuffer += "  INTEGER NOT NULL , [PendingEventData] TEXT COLLATE RTRIM NOT NULL , [PendingEventStatus] ";
         cmdBuffer += "  INTEGER NOT NULL , [PendingEventErrors] TEXT COLLATE RTRIM NOT NULL , [PendingEventExtras] ";
         cmdBuffer += "  TEXT COLLATE RTRIM NOT NULL , [PendingEventFiles] TEXT COLLATE RTRIM NOT NULL , ";
         cmdBuffer += "  PRIMARY KEY([PendingEventId]))  ";
         ExecuteDirectSQL.executeWithThrow(context, remoteHandle, "DEFAULT", cmdBuffer) ;
      }
   }

   private void tablesCount( )
   {
   }

   private boolean previousCheck( )
   {
      return true ;
   }

   private void executeOnlyTablesReorganization( ) throws SQLException
   {
      CreateGxPendingEvent( ) ;
   }

   private void executeOnlyRisReorganization( ) throws SQLException
   {
   }

   private void executeTablesReorganization( )
   {
      try
      {
         executeOnlyTablesReorganization( ) ;
         executeOnlyRisReorganization( ) ;
      }
      catch ( SQLException ex )
      {
         ReorgSubmitThreadPool.setAnError();
         ex.printStackTrace();
      }
   }

   private void executeReorganization( )
   {
      if ( ErrCode == 0 )
      {
         tablesCount( ) ;
         if ( ! GXReorganization.getRecordCount( ) )
         {
            FirstActions( ) ;
            executeTablesReorganization( ) ;
         }
      }
   }

   public void UtilsCleanup( )
   {
      cleanup();
   }

   protected void cleanup( )
   {
      CloseOpenCursors();
   }

   protected void CloseOpenCursors( )
   {
   }

   /* Aggregate/select formulas */
   public void initialize( )
   {
      /* GeneXus formulas. */
   }

   protected short ErrCode ;
}

