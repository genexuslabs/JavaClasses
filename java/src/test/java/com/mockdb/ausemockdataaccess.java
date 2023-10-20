package com.mockdb ;
import com.genexus.*;
import com.genexus.db.*;
import com.genexus.sampleapp.GXcfg;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;

public final  class ausemockdataaccess extends GXProcedure
{
	private IDataStoreProviderFactory dataStoreProviderFactory;

   public static void main( String args[] )
   {
      Application.init(GXcfg.class);
	  ausemockdataaccess pgm = null;
	  try
	  {
		  Class<?> clazz = Class.forName("com.genexus.performance.DataStoreProviderFactory");
		  Constructor<?> constructor = clazz.getConstructor(String.class);
		  Object instance = constructor.newInstance();

		  pgm = new ausemockdataaccess (-1, (IDataStoreProviderFactory) instance);
	  }
	  catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e)
	  {

	  }
      Application.realMainProgram = pgm;
      pgm.executeCmdLine(args);
      GXRuntime.exit( );
   }

   public void executeCmdLine( String args[] )
   {

      execute();
   }

   public ausemockdataaccess( int remoteHandle , IDataStoreProviderFactory factory)
   {
      super( remoteHandle , new ModelContext( ausemockdataaccess.class ), "");
	  this.dataStoreProviderFactory = factory;
   }

   public ausemockdataaccess( int remoteHandle ,
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
      privateExecute();
   }

   private void privateExecute( )
   {
      /*
         INSERT RECORD ON TABLE Transaction1

      */
      A11Transaction1Id = (short)(1) ;
      A12Transaction1Nombre = "UNO" ;
      /* Using cursor P000R2 */
      pr_default.execute(0, new Object[] {Short.valueOf(A11Transaction1Id), A12Transaction1Nombre});
      Application.getSmartCacheProvider(remoteHandle).setUpdated("Transaction1");
      if ( (pr_default.getStatus(0) == 1) )
      {
         Gx_err = (short)(1) ;
         Gx_emsg = localUtil.getMessages().getMessage("GXM_noupdate") ;
      }
      else
      {
         Gx_err = (short)(0) ;
         Gx_emsg = "" ;
      }
      /* End Insert */
      /* Optimized UPDATE. */
      /* Using cursor P000R3 */
      pr_default.execute(1);
      Application.getSmartCacheProvider(remoteHandle).setUpdated("Transaction1");
      /* End optimized UPDATE. */
      /* Using cursor P000R4 */
      pr_default.execute(2);
      while ( (pr_default.getStatus(2) != 101) )
      {
         A12Transaction1Nombre = P000R4_A12Transaction1Nombre[0] ;
         A11Transaction1Id = P000R4_A11Transaction1Id[0] ;
         AV8Transaction1Nombre = A12Transaction1Nombre ;
         pr_default.readNext(2);
      }
      pr_default.close(2);
      /* Optimized DELETE. */
      /* Using cursor P000R5 */
      pr_default.execute(3);
      Application.getSmartCacheProvider(remoteHandle).setUpdated("Transaction1");
      /* End optimized DELETE. */
      cleanup();
   }

   public static Object refClasses( )
   {
      GXutil.refClasses(usemockdataaccess.class);
      return new GXcfg();
   }

   protected void cleanup( )
   {
      Application.commitDataStores(context, remoteHandle, pr_default, "ausemockdataaccess");
      CloseOpenCursors();
      exitApp();
   }

   protected void CloseOpenCursors( )
   {
   }

   /* Aggregate/select formulas */
   public void initialize( )
   {
      A12Transaction1Nombre = "" ;
      Gx_emsg = "" ;
      scmdbuf = "" ;
      P000R4_A12Transaction1Nombre = new String[] {""} ;
      P000R4_A11Transaction1Id = new short[1] ;
      AV8Transaction1Nombre = "" ;
      pr_default = new DataStoreProvider(context, remoteHandle, new com.mockdb.ausemockdataaccess__default(),
         new Object[] {
             new Object[] {
            }
            , new Object[] {
            }
            , new Object[] {
            P000R4_A12Transaction1Nombre, P000R4_A11Transaction1Id
            }
            , new Object[] {
            }
         },
		  dataStoreProviderFactory
      );
      /* GeneXus formulas. */
      Gx_err = (short)(0) ;
   }

   private short A11Transaction1Id ;
   private short Gx_err ;
   private int GX_INS3 ;
   private String A12Transaction1Nombre ;
   private String Gx_emsg ;
   private String scmdbuf ;
   private String AV8Transaction1Nombre ;
   private IDataStoreProvider pr_default ;
   private String[] P000R4_A12Transaction1Nombre ;
   private short[] P000R4_A11Transaction1Id ;
}

final  class ausemockdataaccess__default extends DataStoreHelperBase implements ILocalDataStoreHelper
{
   public Cursor[] getCursors( )
   {
      return new Cursor[] {
          new UpdateCursor("P000R2", "INSERT INTO [Transaction1]([Transaction1Id], [Transaction1Nombre]) VALUES(?, ?)", GX_NOMASK + GX_MASKLOOPLOCK)
         ,new UpdateCursor("P000R3", "UPDATE [Transaction1] SET [Transaction1Nombre]='UNO UNO'  WHERE [Transaction1Id] = 1", GX_NOMASK + GX_MASKLOOPLOCK)
         ,new ForEachCursor("P000R4", "SELECT [Transaction1Nombre], [Transaction1Id] FROM [Transaction1] ORDER BY [Transaction1Id] ",false, GX_NOMASK + GX_MASKLOOPLOCK, false, this,100, GxCacheFrequency.OFF,false )
         ,new UpdateCursor("P000R5", "DELETE FROM [Transaction1]  WHERE [Transaction1Id] = 1", GX_NOMASK + GX_MASKLOOPLOCK)
      };
   }

   public void getResults( int cursor ,
                           IFieldGetter rslt ,
                           Object[] buf ) throws SQLException
   {
      switch ( cursor )
      {
            case 2 :
               ((String[]) buf[0])[0] = rslt.getString(1, 20);
               ((short[]) buf[1])[0] = rslt.getShort(2);
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
               stmt.setShort(1, ((Number) parms[0]).shortValue());
               stmt.setString(2, (String)parms[1], 20);
               return;
      }
   }

}

