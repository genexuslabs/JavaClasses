package com.genexus.genexusserverlessapi ;
import com.genexus.*;
import com.genexus.*;
import com.genexus.xml.*;
import com.genexus.search.*;
import com.genexus.webpanels.*;
import java.util.*;

public final  class SdtEventMessagesList extends GxUserType
{
   public SdtEventMessagesList( )
   {
      this(  new ModelContext(SdtEventMessagesList.class));
   }

   public SdtEventMessagesList( ModelContext context )
   {
      super( context, "SdtEventMessagesList");
   }

   public SdtEventMessagesList( int remoteHandle ,
                                ModelContext context )
   {
      super( remoteHandle, context, "SdtEventMessagesList");
   }

   public SdtEventMessagesList( StructSdtEventMessagesList struct )
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
            if ( GXutil.strcmp2( oReader.getLocalName(), "items") )
            {
               if ( gxTv_SdtEventMessagesList_Items == null )
               {
                  gxTv_SdtEventMessagesList_Items = new GXSimpleCollection<String>(String.class, "internal", "");
               }
               if ( oReader.getIsSimple() == 0 )
               {
                  GXSoapError = gxTv_SdtEventMessagesList_Items.readxmlcollection(oReader, "items", "Item") ;
               }
               readElement = true ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               if ( GXutil.strcmp2( oReader.getLocalName(), "items") )
               {
                  GXSoapError = oReader.read() ;
               }
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
         sName = "EventMessagesList" ;
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
      if ( gxTv_SdtEventMessagesList_Items != null )
      {
         String sNameSpace1;
         if ( GXutil.strcmp(sNameSpace, "ServerlessAPI") == 0 )
         {
            sNameSpace1 = "[*:nosend]" + "ServerlessAPI" ;
         }
         else
         {
            sNameSpace1 = "ServerlessAPI" ;
         }
         gxTv_SdtEventMessagesList_Items.writexmlcollection(oWriter, "items", sNameSpace1, "Item", sNameSpace1);
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
      if ( gxTv_SdtEventMessagesList_Items != null )
      {
         AddObjectProperty("items", gxTv_SdtEventMessagesList_Items, false, false);
      }
   }

   public GXSimpleCollection<String> getgxTv_SdtEventMessagesList_Items( )
   {
      if ( gxTv_SdtEventMessagesList_Items == null )
      {
         gxTv_SdtEventMessagesList_Items = new GXSimpleCollection<String>(String.class, "internal", "");
      }
      gxTv_SdtEventMessagesList_Items_N = (byte)(0) ;
      sdtIsNull = (byte)(0) ;
      return gxTv_SdtEventMessagesList_Items ;
   }

   public void setgxTv_SdtEventMessagesList_Items( GXSimpleCollection<String> value )
   {
      gxTv_SdtEventMessagesList_Items_N = (byte)(0) ;
      sdtIsNull = (byte)(0) ;
      gxTv_SdtEventMessagesList_Items = value ;
   }

   public void setgxTv_SdtEventMessagesList_Items_SetNull( )
   {
      gxTv_SdtEventMessagesList_Items_N = (byte)(1) ;
      gxTv_SdtEventMessagesList_Items = null ;
   }

   public boolean getgxTv_SdtEventMessagesList_Items_IsNull( )
   {
      if ( gxTv_SdtEventMessagesList_Items == null )
      {
         return true ;
      }
      return false ;
   }

   public byte getgxTv_SdtEventMessagesList_Items_N( )
   {
      return gxTv_SdtEventMessagesList_Items_N ;
   }

   public void initialize( int remoteHandle )
   {
      initialize( ) ;
   }

   public void initialize( )
   {
      gxTv_SdtEventMessagesList_Items_N = (byte)(1) ;
      sdtIsNull = (byte)(1) ;
      sTagName = "" ;
   }

   public byte isNull( )
   {
      return sdtIsNull ;
   }

   public com.genexus.genexusserverlessapi.SdtEventMessagesList Clone( )
   {
      return (com.genexus.genexusserverlessapi.SdtEventMessagesList)(clone()) ;
   }

   public void setStruct( com.genexus.genexusserverlessapi.StructSdtEventMessagesList struct )
   {
      if ( struct != null )
      {
         setgxTv_SdtEventMessagesList_Items(new GXSimpleCollection<String>(String.class, "internal", "", struct.getItems()));
      }
   }

   @SuppressWarnings("unchecked")
   public com.genexus.genexusserverlessapi.StructSdtEventMessagesList getStruct( )
   {
      com.genexus.genexusserverlessapi.StructSdtEventMessagesList struct = new com.genexus.genexusserverlessapi.StructSdtEventMessagesList ();
      struct.setItems(getgxTv_SdtEventMessagesList_Items().getStruct());
      return struct ;
   }

   protected byte gxTv_SdtEventMessagesList_Items_N ;
   protected byte sdtIsNull ;
   protected short readOk ;
   protected short nOutParmCount ;
   protected String sTagName ;
   protected boolean readElement ;
   protected boolean formatError ;
   protected GXSimpleCollection<String> gxTv_SdtEventMessagesList_Items=null ;
}

