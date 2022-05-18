package com.genexus.genexusserverlessapi;
import com.genexus.*;
import com.genexus.*;
import com.genexus.xml.*;
import com.genexus.search.*;
import com.genexus.webpanels.*;
import java.util.*;

public final  class SdtEventCustomPayload_CustomPayloadItem extends GxUserType
{
   public SdtEventCustomPayload_CustomPayloadItem( )
   {
      this(  new ModelContext(SdtEventCustomPayload_CustomPayloadItem.class));
   }

   public SdtEventCustomPayload_CustomPayloadItem( ModelContext context )
   {
      super( context, "SdtEventCustomPayload_CustomPayloadItem");
   }

   public SdtEventCustomPayload_CustomPayloadItem( int remoteHandle ,
                                                   ModelContext context )
   {
      super( remoteHandle, context, "SdtEventCustomPayload_CustomPayloadItem");
   }

   public SdtEventCustomPayload_CustomPayloadItem( StructSdtEventCustomPayload_CustomPayloadItem struct )
   {
      this();
      setStruct(struct);
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
      short GXSoapError = 1;
      formatError = false ;
      sTagName = oReader.getName() ;
      if ( oReader.getIsSimple() == 0 )
      {
         GXSoapError = oReader.read() ;
         nOutParmCount = (short)(0) ;
         while ( ( ( GXutil.strcmp(oReader.getName(), sTagName) != 0 ) || ( oReader.getNodeType() == 1 ) ) && ( GXSoapError > 0 ) )
         {
            readOk = (short)(0) ;
            readElement = false ;
            if ( GXutil.strcmp2( oReader.getLocalName(), "PropertyId") )
            {
               gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid = oReader.getValue() ;
               readElement = true ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( GXutil.strcmp2( oReader.getLocalName(), "PropertyValue") )
            {
               gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue = oReader.getValue() ;
               readElement = true ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( ! readElement )
            {
               readOk = (short)(1) ;
               GXSoapError = oReader.read() ;
            }
            nOutParmCount = (short)(nOutParmCount+1) ;
            if ( ( readOk == 0 ) || formatError )
            {
               context.globals.sSOAPErrMsg += "Error reading " + sTagName + GXutil.newLine( ) ;
               context.globals.sSOAPErrMsg += "Message: " + oReader.readRawXML() ;
               GXSoapError = (short)(nOutParmCount*-1) ;
            }
         }
      }
      return GXSoapError ;
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
      if ( (GXutil.strcmp("", sName)==0) )
      {
         sName = "EventCustomPayload.CustomPayloadItem" ;
      }
      oWriter.writeStartElement(sName);
      if ( GXutil.strcmp(GXutil.left( sNameSpace, 10), "[*:nosend]") != 0 )
      {
         oWriter.writeAttribute("xmlns", sNameSpace);
      }
      else
      {
         sNameSpace = GXutil.right( sNameSpace, GXutil.len( sNameSpace)-10) ;
      }
      oWriter.writeElement("PropertyId", gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid);
      if ( GXutil.strcmp(sNameSpace, "ServerlessAPI") != 0 )
      {
         oWriter.writeAttribute("xmlns", "ServerlessAPI");
      }
      oWriter.writeElement("PropertyValue", gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue);
      if ( GXutil.strcmp(sNameSpace, "ServerlessAPI") != 0 )
      {
         oWriter.writeAttribute("xmlns", "ServerlessAPI");
      }
      oWriter.writeEndElement();
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
      AddObjectProperty("PropertyId", gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid, false, false);
      AddObjectProperty("PropertyValue", gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue, false, false);
   }

   public String getgxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid( )
   {
      return gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid ;
   }

   public void setgxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid( String value )
   {
      gxTv_SdtEventCustomPayload_CustomPayloadItem_N = (byte)(0) ;
      gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid = value ;
   }

   public String getgxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue( )
   {
      return gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue ;
   }

   public void setgxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue( String value )
   {
      gxTv_SdtEventCustomPayload_CustomPayloadItem_N = (byte)(0) ;
      gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue = value ;
   }

   public void initialize( int remoteHandle )
   {
      initialize( ) ;
   }

   public void initialize( )
   {
      gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid = "" ;
      gxTv_SdtEventCustomPayload_CustomPayloadItem_N = (byte)(1) ;
      gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue = "" ;
      sTagName = "" ;
   }

   public byte isNull( )
   {
      return gxTv_SdtEventCustomPayload_CustomPayloadItem_N ;
   }

   public com.genexus.genexusserverlessapi.SdtEventCustomPayload_CustomPayloadItem Clone( )
   {
      return (com.genexus.genexusserverlessapi.SdtEventCustomPayload_CustomPayloadItem)(clone()) ;
   }

   public void setStruct( com.genexus.genexusserverlessapi.StructSdtEventCustomPayload_CustomPayloadItem struct )
   {
      setgxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid(struct.getPropertyid());
      setgxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue(struct.getPropertyvalue());
   }

   @SuppressWarnings("unchecked")
   public com.genexus.genexusserverlessapi.StructSdtEventCustomPayload_CustomPayloadItem getStruct( )
   {
      com.genexus.genexusserverlessapi.StructSdtEventCustomPayload_CustomPayloadItem struct = new com.genexus.genexusserverlessapi.StructSdtEventCustomPayload_CustomPayloadItem ();
      struct.setPropertyid(getgxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid());
      struct.setPropertyvalue(getgxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue());
      return struct ;
   }

   protected byte gxTv_SdtEventCustomPayload_CustomPayloadItem_N ;
   protected short readOk ;
   protected short nOutParmCount ;
   protected String gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyvalue ;
   protected String sTagName ;
   protected boolean readElement ;
   protected boolean formatError ;
   protected String gxTv_SdtEventCustomPayload_CustomPayloadItem_Propertyid ;
}

