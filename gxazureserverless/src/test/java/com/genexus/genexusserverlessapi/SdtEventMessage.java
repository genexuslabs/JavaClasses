package com.genexus.genexusserverlessapi ;
import com.genexus.*;
import com.genexus.*;
import com.genexus.xml.*;
import com.genexus.search.*;
import com.genexus.webpanels.*;
import java.util.*;

public final  class SdtEventMessage extends GxUserType
{
   public SdtEventMessage( )
   {
      this(  new ModelContext(SdtEventMessage.class));
   }

   public SdtEventMessage( ModelContext context )
   {
      super( context, "SdtEventMessage");
   }

   public SdtEventMessage( int remoteHandle ,
                           ModelContext context )
   {
      super( remoteHandle, context, "SdtEventMessage");
   }

   public SdtEventMessage( StructSdtEventMessage struct )
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
            if ( GXutil.strcmp2( oReader.getLocalName(), "EventMessageId") )
            {
               gxTv_SdtEventMessage_Eventmessageid = oReader.getValue() ;
               readElement = true ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( GXutil.strcmp2( oReader.getLocalName(), "EventMessageDate") )
            {
               if ( ( GXutil.strcmp(oReader.getValue(), "") == 0 ) || ( oReader.existsAttribute("xsi:nil") == 1 ) )
               {
                  gxTv_SdtEventMessage_Eventmessagedate = GXutil.resetTime( GXutil.nullDate() );
                  gxTv_SdtEventMessage_Eventmessagedate_N = (byte)(1) ;
               }
               else
               {
                  gxTv_SdtEventMessage_Eventmessagedate_N = (byte)(0) ;
                  gxTv_SdtEventMessage_Eventmessagedate = localUtil.ymdhmsToT( (short)(DecimalUtil.decToDouble(CommonUtil.decimalVal( GXutil.substring( oReader.getValue(), 1, 4), "."))), (byte)(DecimalUtil.decToDouble(CommonUtil.decimalVal( GXutil.substring( oReader.getValue(), 6, 2), "."))), (byte)(DecimalUtil.decToDouble(CommonUtil.decimalVal( GXutil.substring( oReader.getValue(), 9, 2), "."))), (byte)(DecimalUtil.decToDouble(CommonUtil.decimalVal( GXutil.substring( oReader.getValue(), 12, 2), "."))), (byte)(DecimalUtil.decToDouble(CommonUtil.decimalVal( GXutil.substring( oReader.getValue(), 15, 2), "."))), (byte)(DecimalUtil.decToDouble(CommonUtil.decimalVal( GXutil.substring( oReader.getValue(), 18, 2), ".")))) ;
               }
               readElement = true ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( GXutil.strcmp2( oReader.getLocalName(), "EventMessageSourceType") )
            {
               gxTv_SdtEventMessage_Eventmessagesourcetype = oReader.getValue() ;
               readElement = true ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( GXutil.strcmp2( oReader.getLocalName(), "EventMessageData") )
            {
               gxTv_SdtEventMessage_Eventmessagedata = oReader.getValue() ;
               readElement = true ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( GXutil.strcmp2( oReader.getLocalName(), "EventMessageProperties") )
            {
               if ( gxTv_SdtEventMessage_Eventmessageproperties == null )
               {
                  gxTv_SdtEventMessage_Eventmessageproperties = new GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventMessageProperty>(com.genexus.genexusserverlessapi.SdtEventMessageProperty.class, "EventMessageProperty", "ServerlessAPI", remoteHandle);
               }
               if ( oReader.getIsSimple() == 0 )
               {
                  GXSoapError = gxTv_SdtEventMessage_Eventmessageproperties.readxmlcollection(oReader, "EventMessageProperties", "EventMessageProperty") ;
               }
               readElement = true ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               if ( GXutil.strcmp2( oReader.getLocalName(), "EventMessageProperties") )
               {
                  GXSoapError = oReader.read() ;
               }
            }
            if ( GXutil.strcmp2( oReader.getLocalName(), "EventMessageVersion") )
            {
               gxTv_SdtEventMessage_Eventmessageversion = oReader.getValue() ;
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
         sName = "EventMessage" ;
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
      oWriter.writeElement("EventMessageId", gxTv_SdtEventMessage_Eventmessageid);
      if ( GXutil.strcmp(sNameSpace, "ServerlessAPI") != 0 )
      {
         oWriter.writeAttribute("xmlns", "ServerlessAPI");
      }
      if ( GXutil.dateCompare(GXutil.nullDate(), gxTv_SdtEventMessage_Eventmessagedate) && ( gxTv_SdtEventMessage_Eventmessagedate_N == 1 ) )
      {
         oWriter.writeElement("EventMessageDate", "");
         if ( GXutil.strcmp(sNameSpace, "ServerlessAPI") != 0 )
         {
            oWriter.writeAttribute("xmlns", "ServerlessAPI");
         }
      }
      else
      {
         sDateCnv = "" ;
         sNumToPad = GXutil.trim( GXutil.str( GXutil.year( gxTv_SdtEventMessage_Eventmessagedate), 10, 0)) ;
         sDateCnv += GXutil.substring( "0000", 1, 4-GXutil.len( sNumToPad)) + sNumToPad ;
         sDateCnv += "-" ;
         sNumToPad = GXutil.trim( GXutil.str( GXutil.month( gxTv_SdtEventMessage_Eventmessagedate), 10, 0)) ;
         sDateCnv += GXutil.substring( "00", 1, 2-GXutil.len( sNumToPad)) + sNumToPad ;
         sDateCnv += "-" ;
         sNumToPad = GXutil.trim( GXutil.str( GXutil.day( gxTv_SdtEventMessage_Eventmessagedate), 10, 0)) ;
         sDateCnv += GXutil.substring( "00", 1, 2-GXutil.len( sNumToPad)) + sNumToPad ;
         sDateCnv += "T" ;
         sNumToPad = GXutil.trim( GXutil.str( GXutil.hour( gxTv_SdtEventMessage_Eventmessagedate), 10, 0)) ;
         sDateCnv += GXutil.substring( "00", 1, 2-GXutil.len( sNumToPad)) + sNumToPad ;
         sDateCnv += ":" ;
         sNumToPad = GXutil.trim( GXutil.str( GXutil.minute( gxTv_SdtEventMessage_Eventmessagedate), 10, 0)) ;
         sDateCnv += GXutil.substring( "00", 1, 2-GXutil.len( sNumToPad)) + sNumToPad ;
         sDateCnv += ":" ;
         sNumToPad = GXutil.trim( GXutil.str( GXutil.second( gxTv_SdtEventMessage_Eventmessagedate), 10, 0)) ;
         sDateCnv += GXutil.substring( "00", 1, 2-GXutil.len( sNumToPad)) + sNumToPad ;
         oWriter.writeElement("EventMessageDate", sDateCnv);
         if ( GXutil.strcmp(sNameSpace, "ServerlessAPI") != 0 )
         {
            oWriter.writeAttribute("xmlns", "ServerlessAPI");
         }
      }
      oWriter.writeElement("EventMessageSourceType", gxTv_SdtEventMessage_Eventmessagesourcetype);
      if ( GXutil.strcmp(sNameSpace, "ServerlessAPI") != 0 )
      {
         oWriter.writeAttribute("xmlns", "ServerlessAPI");
      }
      oWriter.writeElement("EventMessageData", gxTv_SdtEventMessage_Eventmessagedata);
      if ( GXutil.strcmp(sNameSpace, "ServerlessAPI") != 0 )
      {
         oWriter.writeAttribute("xmlns", "ServerlessAPI");
      }
      if ( gxTv_SdtEventMessage_Eventmessageproperties != null )
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
         gxTv_SdtEventMessage_Eventmessageproperties.writexmlcollection(oWriter, "EventMessageProperties", sNameSpace1, "EventMessageProperty", sNameSpace1);
      }
      oWriter.writeElement("EventMessageVersion", gxTv_SdtEventMessage_Eventmessageversion);
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
      if ( gxTv_SdtEventMessage_Eventmessageid_N != 1 )
      {
         AddObjectProperty("EventMessageId", gxTv_SdtEventMessage_Eventmessageid, false, false);
      }
      if ( gxTv_SdtEventMessage_Eventmessagedate_N != 1 )
      {
         datetime_STZ = gxTv_SdtEventMessage_Eventmessagedate ;
         sDateCnv = "" ;
         sNumToPad = GXutil.trim( GXutil.str( GXutil.year( datetime_STZ), 10, 0)) ;
         sDateCnv += GXutil.substring( "0000", 1, 4-GXutil.len( sNumToPad)) + sNumToPad ;
         sDateCnv += "-" ;
         sNumToPad = GXutil.trim( GXutil.str( GXutil.month( datetime_STZ), 10, 0)) ;
         sDateCnv += GXutil.substring( "00", 1, 2-GXutil.len( sNumToPad)) + sNumToPad ;
         sDateCnv += "-" ;
         sNumToPad = GXutil.trim( GXutil.str( GXutil.day( datetime_STZ), 10, 0)) ;
         sDateCnv += GXutil.substring( "00", 1, 2-GXutil.len( sNumToPad)) + sNumToPad ;
         sDateCnv += "T" ;
         sNumToPad = GXutil.trim( GXutil.str( GXutil.hour( datetime_STZ), 10, 0)) ;
         sDateCnv += GXutil.substring( "00", 1, 2-GXutil.len( sNumToPad)) + sNumToPad ;
         sDateCnv += ":" ;
         sNumToPad = GXutil.trim( GXutil.str( GXutil.minute( datetime_STZ), 10, 0)) ;
         sDateCnv += GXutil.substring( "00", 1, 2-GXutil.len( sNumToPad)) + sNumToPad ;
         sDateCnv += ":" ;
         sNumToPad = GXutil.trim( GXutil.str( GXutil.second( datetime_STZ), 10, 0)) ;
         sDateCnv += GXutil.substring( "00", 1, 2-GXutil.len( sNumToPad)) + sNumToPad ;
         AddObjectProperty("EventMessageDate", sDateCnv, false, false);
      }
      if ( gxTv_SdtEventMessage_Eventmessagesourcetype_N != 1 )
      {
         AddObjectProperty("EventMessageSourceType", gxTv_SdtEventMessage_Eventmessagesourcetype, false, false);
      }
      if ( gxTv_SdtEventMessage_Eventmessagedata_N != 1 )
      {
         AddObjectProperty("EventMessageData", gxTv_SdtEventMessage_Eventmessagedata, false, false);
      }
      if ( gxTv_SdtEventMessage_Eventmessageproperties != null )
      {
         AddObjectProperty("EventMessageProperties", gxTv_SdtEventMessage_Eventmessageproperties, false, false);
      }
      if ( gxTv_SdtEventMessage_Eventmessageversion_N != 1 )
      {
         AddObjectProperty("EventMessageVersion", gxTv_SdtEventMessage_Eventmessageversion, false, false);
      }
   }

   public String getgxTv_SdtEventMessage_Eventmessageid( )
   {
      return gxTv_SdtEventMessage_Eventmessageid ;
   }

   public void setgxTv_SdtEventMessage_Eventmessageid( String value )
   {
      gxTv_SdtEventMessage_Eventmessageid_N = (byte)(0) ;
      sdtIsNull = (byte)(0) ;
      gxTv_SdtEventMessage_Eventmessageid = value ;
   }

   public byte getgxTv_SdtEventMessage_Eventmessageid_N( )
   {
      return gxTv_SdtEventMessage_Eventmessageid_N ;
   }

   public java.util.Date getgxTv_SdtEventMessage_Eventmessagedate( )
   {
      return gxTv_SdtEventMessage_Eventmessagedate ;
   }

   public void setgxTv_SdtEventMessage_Eventmessagedate( java.util.Date value )
   {
      gxTv_SdtEventMessage_Eventmessagedate_N = (byte)(0) ;
      sdtIsNull = (byte)(0) ;
      gxTv_SdtEventMessage_Eventmessagedate = value ;
   }

   public byte getgxTv_SdtEventMessage_Eventmessagedate_N( )
   {
      return gxTv_SdtEventMessage_Eventmessagedate_N ;
   }

   public String getgxTv_SdtEventMessage_Eventmessagesourcetype( )
   {
      return gxTv_SdtEventMessage_Eventmessagesourcetype ;
   }

   public void setgxTv_SdtEventMessage_Eventmessagesourcetype( String value )
   {
      gxTv_SdtEventMessage_Eventmessagesourcetype_N = (byte)(0) ;
      sdtIsNull = (byte)(0) ;
      gxTv_SdtEventMessage_Eventmessagesourcetype = value ;
   }

   public byte getgxTv_SdtEventMessage_Eventmessagesourcetype_N( )
   {
      return gxTv_SdtEventMessage_Eventmessagesourcetype_N ;
   }

   public String getgxTv_SdtEventMessage_Eventmessagedata( )
   {
      return gxTv_SdtEventMessage_Eventmessagedata ;
   }

   public void setgxTv_SdtEventMessage_Eventmessagedata( String value )
   {
      gxTv_SdtEventMessage_Eventmessagedata_N = (byte)(0) ;
      sdtIsNull = (byte)(0) ;
      gxTv_SdtEventMessage_Eventmessagedata = value ;
   }

   public byte getgxTv_SdtEventMessage_Eventmessagedata_N( )
   {
      return gxTv_SdtEventMessage_Eventmessagedata_N ;
   }

   public GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventMessageProperty> getgxTv_SdtEventMessage_Eventmessageproperties( )
   {
      if ( gxTv_SdtEventMessage_Eventmessageproperties == null )
      {
         gxTv_SdtEventMessage_Eventmessageproperties = new GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventMessageProperty>(com.genexus.genexusserverlessapi.SdtEventMessageProperty.class, "EventMessageProperty", "ServerlessAPI", remoteHandle);
      }
      gxTv_SdtEventMessage_Eventmessageproperties_N = (byte)(0) ;
      sdtIsNull = (byte)(0) ;
      return gxTv_SdtEventMessage_Eventmessageproperties ;
   }

   public void setgxTv_SdtEventMessage_Eventmessageproperties( GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventMessageProperty> value )
   {
      gxTv_SdtEventMessage_Eventmessageproperties_N = (byte)(0) ;
      sdtIsNull = (byte)(0) ;
      gxTv_SdtEventMessage_Eventmessageproperties = value ;
   }

   public void setgxTv_SdtEventMessage_Eventmessageproperties_SetNull( )
   {
      gxTv_SdtEventMessage_Eventmessageproperties_N = (byte)(1) ;
      gxTv_SdtEventMessage_Eventmessageproperties = null ;
   }

   public boolean getgxTv_SdtEventMessage_Eventmessageproperties_IsNull( )
   {
      if ( gxTv_SdtEventMessage_Eventmessageproperties == null )
      {
         return true ;
      }
      return false ;
   }

   public byte getgxTv_SdtEventMessage_Eventmessageproperties_N( )
   {
      return gxTv_SdtEventMessage_Eventmessageproperties_N ;
   }

   public String getgxTv_SdtEventMessage_Eventmessageversion( )
   {
      return gxTv_SdtEventMessage_Eventmessageversion ;
   }

   public void setgxTv_SdtEventMessage_Eventmessageversion( String value )
   {
      gxTv_SdtEventMessage_Eventmessageversion_N = (byte)(0) ;
      sdtIsNull = (byte)(0) ;
      gxTv_SdtEventMessage_Eventmessageversion = value ;
   }

   public byte getgxTv_SdtEventMessage_Eventmessageversion_N( )
   {
      return gxTv_SdtEventMessage_Eventmessageversion_N ;
   }

   public void initialize( int remoteHandle )
   {
      initialize( ) ;
   }

   public void initialize( )
   {
      gxTv_SdtEventMessage_Eventmessageid = "" ;
      gxTv_SdtEventMessage_Eventmessageid_N = (byte)(1) ;
      sdtIsNull = (byte)(1) ;
      gxTv_SdtEventMessage_Eventmessagedate = GXutil.resetTime( GXutil.nullDate() );
      gxTv_SdtEventMessage_Eventmessagedate_N = (byte)(1) ;
      gxTv_SdtEventMessage_Eventmessagesourcetype = "" ;
      gxTv_SdtEventMessage_Eventmessagesourcetype_N = (byte)(1) ;
      gxTv_SdtEventMessage_Eventmessagedata = "" ;
      gxTv_SdtEventMessage_Eventmessagedata_N = (byte)(1) ;
      gxTv_SdtEventMessage_Eventmessageproperties_N = (byte)(1) ;
      gxTv_SdtEventMessage_Eventmessageversion = "" ;
      gxTv_SdtEventMessage_Eventmessageversion_N = (byte)(1) ;
      sTagName = "" ;
      sDateCnv = "" ;
      sNumToPad = "" ;
      datetime_STZ = GXutil.resetTime( GXutil.nullDate() );
   }

   public byte isNull( )
   {
      return sdtIsNull ;
   }

   public com.genexus.genexusserverlessapi.SdtEventMessage Clone( )
   {
      return (com.genexus.genexusserverlessapi.SdtEventMessage)(clone()) ;
   }

   public void setStruct( com.genexus.genexusserverlessapi.StructSdtEventMessage struct )
   {
      if ( struct != null )
      {
         setgxTv_SdtEventMessage_Eventmessageid(struct.getEventmessageid());
         if ( struct.gxTv_SdtEventMessage_Eventmessagedate_N == 0 )
         {
            setgxTv_SdtEventMessage_Eventmessagedate(struct.getEventmessagedate());
         }
         setgxTv_SdtEventMessage_Eventmessagesourcetype(struct.getEventmessagesourcetype());
         setgxTv_SdtEventMessage_Eventmessagedata(struct.getEventmessagedata());
         GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventMessageProperty> gxTv_SdtEventMessage_Eventmessageproperties_aux = new GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventMessageProperty>(com.genexus.genexusserverlessapi.SdtEventMessageProperty.class, "EventMessageProperty", "ServerlessAPI", remoteHandle);
         Vector<com.genexus.genexusserverlessapi.StructSdtEventMessageProperty> gxTv_SdtEventMessage_Eventmessageproperties_aux1 = struct.getEventmessageproperties();
         if (gxTv_SdtEventMessage_Eventmessageproperties_aux1 != null)
         {
            for (int i = 0; i < gxTv_SdtEventMessage_Eventmessageproperties_aux1.size(); i++)
            {
               gxTv_SdtEventMessage_Eventmessageproperties_aux.add(new com.genexus.genexusserverlessapi.SdtEventMessageProperty(gxTv_SdtEventMessage_Eventmessageproperties_aux1.elementAt(i)));
            }
         }
         setgxTv_SdtEventMessage_Eventmessageproperties(gxTv_SdtEventMessage_Eventmessageproperties_aux);
         setgxTv_SdtEventMessage_Eventmessageversion(struct.getEventmessageversion());
      }
   }

   @SuppressWarnings("unchecked")
   public com.genexus.genexusserverlessapi.StructSdtEventMessage getStruct( )
   {
      com.genexus.genexusserverlessapi.StructSdtEventMessage struct = new com.genexus.genexusserverlessapi.StructSdtEventMessage ();
      struct.setEventmessageid(getgxTv_SdtEventMessage_Eventmessageid());
      if ( gxTv_SdtEventMessage_Eventmessagedate_N == 0 )
      {
         struct.setEventmessagedate(getgxTv_SdtEventMessage_Eventmessagedate());
      }
      struct.setEventmessagesourcetype(getgxTv_SdtEventMessage_Eventmessagesourcetype());
      struct.setEventmessagedata(getgxTv_SdtEventMessage_Eventmessagedata());
      struct.setEventmessageproperties(getgxTv_SdtEventMessage_Eventmessageproperties().getStruct());
      struct.setEventmessageversion(getgxTv_SdtEventMessage_Eventmessageversion());
      return struct ;
   }

   protected byte gxTv_SdtEventMessage_Eventmessageid_N ;
   protected byte sdtIsNull ;
   protected byte gxTv_SdtEventMessage_Eventmessagedate_N ;
   protected byte gxTv_SdtEventMessage_Eventmessagesourcetype_N ;
   protected byte gxTv_SdtEventMessage_Eventmessagedata_N ;
   protected byte gxTv_SdtEventMessage_Eventmessageproperties_N ;
   protected byte gxTv_SdtEventMessage_Eventmessageversion_N ;
   protected short readOk ;
   protected short nOutParmCount ;
   protected String sTagName ;
   protected String sDateCnv ;
   protected String sNumToPad ;
   protected java.util.Date gxTv_SdtEventMessage_Eventmessagedate ;
   protected java.util.Date datetime_STZ ;
   protected boolean readElement ;
   protected boolean formatError ;
   protected String gxTv_SdtEventMessage_Eventmessagedata ;
   protected String gxTv_SdtEventMessage_Eventmessageid ;
   protected String gxTv_SdtEventMessage_Eventmessagesourcetype ;
   protected String gxTv_SdtEventMessage_Eventmessageversion ;
   protected GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventMessageProperty> gxTv_SdtEventMessage_Eventmessageproperties=null ;
   protected GXBaseCollection<com.genexus.genexusserverlessapi.SdtEventMessageProperty> gxTv_SdtEventMessage_Eventmessageproperties_aux ;
}

