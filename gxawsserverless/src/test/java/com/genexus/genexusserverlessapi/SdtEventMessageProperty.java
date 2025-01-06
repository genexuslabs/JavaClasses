package com.genexus.genexusserverlessapi ;
import com.genexus.*;
import com.genexus.*;
import com.genexus.xml.*;
import com.genexus.search.*;
import com.genexus.webpanels.*;
import java.util.*;

public final  class SdtEventMessageProperty extends GxUserType
{
   public SdtEventMessageProperty( )
   {
      this(  new ModelContext(SdtEventMessageProperty.class));
   }

   public SdtEventMessageProperty( ModelContext context )
   {
      super( context, "SdtEventMessageProperty");
   }

   public SdtEventMessageProperty( int remoteHandle ,
                                   ModelContext context )
   {
      super( remoteHandle, context, "SdtEventMessageProperty");
   }

   public SdtEventMessageProperty( StructSdtEventMessageProperty struct )
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
               gxTv_SdtEventMessageProperty_Propertyid = oReader.getValue() ;
               readElement = true ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( GXutil.strcmp2( oReader.getLocalName(), "PropertyValue") )
            {
               gxTv_SdtEventMessageProperty_Propertyvalue = oReader.getValue() ;
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
         sName = "EventMessageProperty" ;
      }
      if ( (GXutil.strcmp("", sNameSpace)==0) )
      {
         sNameSpace = "ServerlessAPI" ;
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
      oWriter.writeElement("PropertyId", gxTv_SdtEventMessageProperty_Propertyid);
      if ( GXutil.strcmp(sNameSpace, "ServerlessAPI") != 0 )
      {
         oWriter.writeAttribute("xmlns", "ServerlessAPI");
      }
      oWriter.writeElement("PropertyValue", gxTv_SdtEventMessageProperty_Propertyvalue);
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
      AddObjectProperty("PropertyId", gxTv_SdtEventMessageProperty_Propertyid, false, false);
      AddObjectProperty("PropertyValue", gxTv_SdtEventMessageProperty_Propertyvalue, false, false);
   }

   public String getgxTv_SdtEventMessageProperty_Propertyid( )
   {
      return gxTv_SdtEventMessageProperty_Propertyid ;
   }

   public void setgxTv_SdtEventMessageProperty_Propertyid( String value )
   {
      sdtIsNull = (byte)(0) ;
      gxTv_SdtEventMessageProperty_Propertyid = value ;
   }

   public String getgxTv_SdtEventMessageProperty_Propertyvalue( )
   {
      return gxTv_SdtEventMessageProperty_Propertyvalue ;
   }

   public void setgxTv_SdtEventMessageProperty_Propertyvalue( String value )
   {
      sdtIsNull = (byte)(0) ;
      gxTv_SdtEventMessageProperty_Propertyvalue = value ;
   }

   public void initialize( int remoteHandle )
   {
      initialize( ) ;
   }

   public void initialize( )
   {
      gxTv_SdtEventMessageProperty_Propertyid = "" ;
      sdtIsNull = (byte)(1) ;
      gxTv_SdtEventMessageProperty_Propertyvalue = "" ;
      sTagName = "" ;
   }

   public byte isNull( )
   {
      return sdtIsNull ;
   }

   public com.genexus.genexusserverlessapi.SdtEventMessageProperty Clone( )
   {
      return (com.genexus.genexusserverlessapi.SdtEventMessageProperty)(clone()) ;
   }

   public void setStruct( com.genexus.genexusserverlessapi.StructSdtEventMessageProperty struct )
   {
      if ( struct != null )
      {
         setgxTv_SdtEventMessageProperty_Propertyid(struct.getPropertyid());
         setgxTv_SdtEventMessageProperty_Propertyvalue(struct.getPropertyvalue());
      }
   }

   @SuppressWarnings("unchecked")
   public com.genexus.genexusserverlessapi.StructSdtEventMessageProperty getStruct( )
   {
      com.genexus.genexusserverlessapi.StructSdtEventMessageProperty struct = new com.genexus.genexusserverlessapi.StructSdtEventMessageProperty ();
      struct.setPropertyid(getgxTv_SdtEventMessageProperty_Propertyid());
      struct.setPropertyvalue(getgxTv_SdtEventMessageProperty_Propertyvalue());
      return struct ;
   }

   protected byte sdtIsNull ;
   protected short readOk ;
   protected short nOutParmCount ;
   protected String gxTv_SdtEventMessageProperty_Propertyid ;
   protected String gxTv_SdtEventMessageProperty_Propertyvalue ;
   protected String sTagName ;
   protected boolean readElement ;
   protected boolean formatError ;
}

