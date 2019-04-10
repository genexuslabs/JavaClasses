/*
               File: GXGridState_FilterValue
        Description: GridState
             Author: GeneXus Java Generator version 15_0_10-122595
       Generated on: April 5, 2019 14:6:9.43
       Program type: Callable routine
          Main DBMS: SQL Server
*/
package com.genexus.webpanels ;
import com.genexus.*;
import com.genexus.xml.*;
import com.genexus.search.*;
import com.genexus.webpanels.*;
import java.util.*;

public final  class GXGridState_FilterValue extends GXXMLSerializable implements Cloneable, java.io.Serializable
{
   public GXGridState_FilterValue( )
   {
      this(  new ModelContext(GXGridState_FilterValue.class));
   }

   public GXGridState_FilterValue( ModelContext context )
   {
      super( context, "GXGridState_FilterValue");
   }

   public GXGridState_FilterValue( int remoteHandle ,
                                    ModelContext context )
   {
      super( remoteHandle, context, "GXGridState_FilterValue");
   }

   public GXGridState_FilterValue( int remoteHandle ,
                                    com.genexus.common.classes.AbstractModelContext context )
   {
      super( remoteHandle, (ModelContext)context, "GXGridState_FilterValue");
   }


   private static java.util.HashMap mapper = new java.util.HashMap();
   static
   {
   }

   public String getJsonMap( String value )
   {
      return (String) mapper.get(value);
   }

   public short readxml( com.genexus.xml.XMLReader oReader ,
                         String sName )
   {
      return 1 ;
   }

   public void writexml( com.genexus.xml.XMLWriter oWriter ,
                         String sName ,
                         String sNameSpace )
   {
      writexml(oWriter, sName, sNameSpace, true);
   }

   public void writexml( com.genexus.xml.XMLWriter oWriter ,
                         String sName ,
                         String sNameSpace ,
                         boolean sIncludeState )
   {
     
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
      AddObjectProperty("Value", gxTv_GXGridState_FilterValue_Value, false, false);
   }

   public String getgxTv_GXGridState_FilterValue_Value( )
   {
      return gxTv_GXGridState_FilterValue_Value ;
   }

   public void setgxTv_GXGridState_FilterValue_Value( String value )
   {
      gxTv_GXGridState_FilterValue_Value = value ;
   }

   public void initialize( int remoteHandle )
   {
      initialize( ) ;
   }

   public void initialize( )
   {
      gxTv_GXGridState_FilterValue_Value = "" ;
      sTagName = "" ;
   }

   public GXGridState_FilterValue Clone( )
   {
      return (GXGridState_FilterValue)(clone()) ;
   }

   protected short readOk ;
   protected short nOutParmCount ;
   protected String sTagName ;
   protected boolean formatError ;
   protected String gxTv_GXGridState_FilterValue_Value ;
}

