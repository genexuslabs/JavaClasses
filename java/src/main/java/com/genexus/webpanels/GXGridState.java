/*
               File: GXGridState
        Description: GridState
             Author: GeneXus Java Generator version 15_0_10-122595
       Generated on: April 5, 2019 14:6:9.39
       Program type: Callable routine
          Main DBMS: SQL Server
*/
package com.genexus.webpanels ;
import com.genexus.*;
import com.genexus.xml.*;
import com.genexus.search.*;
import com.genexus.webpanels.*;
import com.genexus.internet.*;
import java.util.*;
import java.util.concurrent.Callable;

public final class GXGridState extends GXXMLSerializable implements Cloneable, java.io.Serializable
{
   String gridName;
   Runnable varsFromState;
   Runnable varsToState;
   HttpContext httpContext;

   public GXGridState( )
   {
      this(  new ModelContext(GXGridState.class));
   }

   public GXGridState( ModelContext context )
   {
      super( context, "GXGridState");
   }

   public GXGridState( int remoteHandle ,  ModelContext context )
   {
      super( remoteHandle, context, "GXGridState");
   }

   public GXGridState( int remoteHandle, com.genexus.common.classes.AbstractModelContext context )
   {
      super( remoteHandle, (ModelContext)context, "GXGridState");
   }

   public GXGridState(ModelContext context, String gridName, String programName, Runnable varsFromState, Runnable varsToState)
   {
      this(context);
      this.httpContext = context.getHttpContext();
      this.gridName = programName + "_" + gridName + "_GridState";
      this.varsFromState = varsFromState;
      this.varsToState = varsToState;
   }

	public String filterValues(int idx)
	{
		return getgxTv_GXGridState_Filtervalues().elementAt(idx-1).getgxTv_GXGridState_FilterValue_Value();
	}
	public void clearFilterValues()
	{
		getgxTv_GXGridState_Filtervalues().clear();
	}
	public void addFilterValue(String value)
	{
		GXGridState_FilterValue GridStateFilterValue = new GXGridState_FilterValue(remoteHandle, context);
      GridStateFilterValue.setgxTv_GXGridState_FilterValue_Value( value );
      getgxTv_GXGridState_Filtervalues().add(GridStateFilterValue, 0);
	}

	public void saveGridState()
	{
      WebSession session = httpContext.getWebSession();
		fromJSonString(session.getValue(gridName));
      varsToState.run();
		session.setValue(gridName, toJSonString());
	}

	public void loadGridState()
	{
      HttpRequest httpRequest = httpContext.getHttpRequest();
      WebSession session = httpContext.getWebSession();
      if ( GXutil.strcmp(httpRequest.getMethod(), "GET") == 0 )
		{
			fromJSonString(session.getValue(gridName));
			varsFromState.run();
		}
	}
   public int getFiltercount()
   {
      return getgxTv_GXGridState_Filtervalues().size();
   }

   public int getCurrentpage()
   {
         return gxTv_GXGridState_Currentpage; 
   }
   public void setCurrentpage(int value)
   {
      gxTv_GXGridState_Currentpage = value;
   }

   public String getJsonMap( String value )
   {
      return value;
   }

   public void tojson( )
   {
      tojson( true) ;
   }

   public void tojson( boolean includeState )
   {
      tojson( includeState, true) ;
   }

   public void tojson( boolean includeState ,
                       boolean includeNonInitialized )
   {
      AddObjectProperty("CurrentPage", gxTv_GXGridState_Currentpage, false, false);
      if ( gxTv_GXGridState_Filtervalues != null )
      {
         AddObjectProperty("FilterValues", gxTv_GXGridState_Filtervalues, false, false);
      }
   }

   public int getgxTv_GXGridState_Currentpage( )
   {
      return gxTv_GXGridState_Currentpage ;
   }

   public void setgxTv_GXGridState_Currentpage( int value )
   {
      gxTv_GXGridState_Currentpage = value ;
   }

   public GXBaseCollection<GXGridState_FilterValue> getgxTv_GXGridState_Filtervalues( )
   {
      if ( gxTv_GXGridState_Filtervalues == null )
      {
         gxTv_GXGridState_Filtervalues = new GXBaseCollection<GXGridState_FilterValue>(GXGridState_FilterValue.class, "GXGridState.FilterValue", "", remoteHandle);
      }
      gxTv_GXGridState_Filtervalues_N = (byte)(0) ;
      return gxTv_GXGridState_Filtervalues ;
   }

   public void setgxTv_GXGridState_Filtervalues( GXBaseCollection<GXGridState_FilterValue> value )
   {
      gxTv_GXGridState_Filtervalues_N = (byte)(0) ;
      gxTv_GXGridState_Filtervalues = value ;
   }

   public void setgxTv_GXGridState_Filtervalues_SetNull( )
   {
      gxTv_GXGridState_Filtervalues_N = (byte)(1) ;
      gxTv_GXGridState_Filtervalues = null ;
   }

   public boolean getgxTv_GXGridState_Filtervalues_IsNull( )
   {
      if ( gxTv_GXGridState_Filtervalues == null )
      {
         return true ;
      }
      return false ;
   }

   public byte getgxTv_GXGridState_Filtervalues_N( )
   {
      return gxTv_GXGridState_Filtervalues_N ;
   }

   public void initialize( int remoteHandle )
   {
      initialize( ) ;
   }

   public void initialize( )
   {
      gxTv_GXGridState_Filtervalues_N = (byte)(1) ;
   }

   public GXGridState Clone( )
   {
      return (GXGridState)(clone()) ;
   }

   protected byte gxTv_GXGridState_Filtervalues_N ;
   protected int gxTv_GXGridState_Currentpage ;
   protected GXBaseCollection<GXGridState_FilterValue> gxTv_GXGridState_Filtervalues=null ;
}

