package com.genexus.genexusserverlessapi;
import com.genexus.*;
import com.genexus.*;
import com.genexus.xml.*;
import com.genexus.search.*;
import com.genexus.webpanels.*;
import java.util.*;

public final  class SdtEventMessageResponse extends GxUserType
{
   public SdtEventMessageResponse( )
   {
      this(  new ModelContext(SdtEventMessageResponse.class));
   }

   public SdtEventMessageResponse( ModelContext context )
   {
      super( context, "SdtEventMessageResponse");
   }

   public SdtEventMessageResponse( int remoteHandle ,
                                   ModelContext context )
   {
      super( remoteHandle, context, "SdtEventMessageResponse");
   }

   public SdtEventMessageResponse( StructSdtEventMessageResponse struct )
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
            if ( GXutil.strcmp2( oReader.getLocalName(), "Handled") )
            {
               gxTv_SdtEventMessageResponse_Handled = (boolean)((((GXutil.strcmp(oReader.getValue(), "true")==0)||(GXutil.strcmp(oReader.getValue(), "1")==0) ? 1 : 0)==0)?false:true) ;
               readElement = true ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( GXutil.strcmp2( oReader.getLocalName(), "ErrorMessage") )
            {
               gxTv_SdtEventMessageResponse_Errormessage = oReader.getValue() ;
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
         sName = "EventMessageResponse" ;
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
      oWriter.writeElement("Handled", GXutil.booltostr( gxTv_SdtEventMessageResponse_Handled));
      if ( GXutil.strcmp(sNameSpace, "ServerlessAPI") != 0 )
      {
         oWriter.writeAttribute("xmlns", "ServerlessAPI");
      }
      oWriter.writeElement("ErrorMessage", gxTv_SdtEventMessageResponse_Errormessage);
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
      AddObjectProperty("Handled", gxTv_SdtEventMessageResponse_Handled, false, false);
      AddObjectProperty("ErrorMessage", gxTv_SdtEventMessageResponse_Errormessage, false, false);
   }

   public boolean getgxTv_SdtEventMessageResponse_Handled( )
   {
      return gxTv_SdtEventMessageResponse_Handled ;
   }

   public void setgxTv_SdtEventMessageResponse_Handled( boolean value )
   {
      gxTv_SdtEventMessageResponse_N = (byte)(0) ;
      gxTv_SdtEventMessageResponse_Handled = value ;
   }

   public String getgxTv_SdtEventMessageResponse_Errormessage( )
   {
      return gxTv_SdtEventMessageResponse_Errormessage ;
   }

   public void setgxTv_SdtEventMessageResponse_Errormessage( String value )
   {
      gxTv_SdtEventMessageResponse_N = (byte)(0) ;
      gxTv_SdtEventMessageResponse_Errormessage = value ;
   }

   public void initialize( int remoteHandle )
   {
      initialize( ) ;
   }

   public void initialize( )
   {
      gxTv_SdtEventMessageResponse_N = (byte)(1) ;
      gxTv_SdtEventMessageResponse_Errormessage = "" ;
      sTagName = "" ;
   }

   public byte isNull( )
   {
      return gxTv_SdtEventMessageResponse_N ;
   }

   public com.genexus.genexusserverlessapi.SdtEventMessageResponse Clone( )
   {
      return (com.genexus.genexusserverlessapi.SdtEventMessageResponse)(clone()) ;
   }

   public void setStruct( com.genexus.genexusserverlessapi.StructSdtEventMessageResponse struct )
   {
      setgxTv_SdtEventMessageResponse_Handled(struct.getHandled());
      setgxTv_SdtEventMessageResponse_Errormessage(struct.getErrormessage());
   }

   @SuppressWarnings("unchecked")
   public com.genexus.genexusserverlessapi.StructSdtEventMessageResponse getStruct( )
   {
      com.genexus.genexusserverlessapi.StructSdtEventMessageResponse struct = new com.genexus.genexusserverlessapi.StructSdtEventMessageResponse ();
      struct.setHandled(getgxTv_SdtEventMessageResponse_Handled());
      struct.setErrormessage(getgxTv_SdtEventMessageResponse_Errormessage());
      return struct ;
   }

   protected byte gxTv_SdtEventMessageResponse_N ;
   protected short readOk ;
   protected short nOutParmCount ;
   protected String sTagName ;
   protected boolean gxTv_SdtEventMessageResponse_Handled ;
   protected boolean readElement ;
   protected boolean formatError ;
   protected String gxTv_SdtEventMessageResponse_Errormessage ;
}

