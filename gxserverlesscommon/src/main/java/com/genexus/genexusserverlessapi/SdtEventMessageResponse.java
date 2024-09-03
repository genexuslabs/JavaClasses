package com.genexus.genexusserverlessapi;

import com.genexus.GXutil;
import com.genexus.GxUserType;
import com.genexus.ModelContext;
import com.genexus.xml.XMLReader;
import com.genexus.xml.XMLWriter;

import java.util.HashMap;

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

   private static HashMap mapper = new HashMap();
   static
   {
   }

   public String getJsonMap( String value )
   {
      return (String) mapper.get(value);
   }

   public short readxml( XMLReader oReader ,
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
            if ( GXutil.strcmp2( oReader.getLocalName(), "HandleFailure") )
            {
               gxTv_SdtEventMessageResponse_Handlefailure = (boolean)((((GXutil.strcmp(oReader.getValue(), "true")==0)||(GXutil.strcmp(oReader.getValue(), "1")==0) ? 1 : 0)==0)?false:true) ;
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

   public void writexml( XMLWriter oWriter ,
                         String sName ,
                         String sNameSpace )
   {
      writexml(oWriter, sName, sNameSpace, true);
   }

   public void writexml( XMLWriter oWriter ,
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
      oWriter.writeElement("HandleFailure", GXutil.booltostr( gxTv_SdtEventMessageResponse_Handlefailure));
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
      AddObjectProperty("HandleFailure", gxTv_SdtEventMessageResponse_Handlefailure, false, false);
      AddObjectProperty("ErrorMessage", gxTv_SdtEventMessageResponse_Errormessage, false, false);
   }

   public boolean getgxTv_SdtEventMessageResponse_Handlefailure( )
   {
      return gxTv_SdtEventMessageResponse_Handlefailure ;
   }

   public void setgxTv_SdtEventMessageResponse_Handlefailure( boolean value )
   {
      gxTv_SdtEventMessageResponse_N = (byte)(0) ;
      gxTv_SdtEventMessageResponse_Handlefailure = value ;
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

   public void setStruct( StructSdtEventMessageResponse struct )
   {
      if ( struct != null )
      {
         setgxTv_SdtEventMessageResponse_Handlefailure(struct.getHandlefailure());
         setgxTv_SdtEventMessageResponse_Errormessage(struct.getErrormessage());
      }
   }

   @SuppressWarnings("unchecked")
   public StructSdtEventMessageResponse getStruct( )
   {
      StructSdtEventMessageResponse struct = new StructSdtEventMessageResponse ();
      struct.setHandlefailure(getgxTv_SdtEventMessageResponse_Handlefailure());
      struct.setErrormessage(getgxTv_SdtEventMessageResponse_Errormessage());
      return struct ;
   }

   protected byte gxTv_SdtEventMessageResponse_N ;
   protected short readOk ;
   protected short nOutParmCount ;
   protected String sTagName ;
   protected boolean gxTv_SdtEventMessageResponse_Handlefailure ;
   protected boolean readElement ;
   protected boolean formatError ;
   protected String gxTv_SdtEventMessageResponse_Errormessage ;
}

