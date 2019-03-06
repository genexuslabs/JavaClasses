/*
               File: SdtGxSynchroEventSDT_GxSynchroEventSDTItem
        Description: GxSynchroEventSDT
             Author: GeneXus Android Generator version 10_4_0-89519
       Generated on: March 18, 2015 13:55:8.38
       Program type: Callable routine
          Main DBMS: sqlite
*/
package com.artech.base.synchronization.dps;
import com.genexus.*;
import com.genexus.xml.*;
import com.artech.base.services.*;

public final  class SdtGxSynchroEventSDT_GxSynchroEventSDTItem extends GXXMLSerializable implements Cloneable, java.io.Serializable
{
   public SdtGxSynchroEventSDT_GxSynchroEventSDTItem( )
   {
      this(  new ModelContext(SdtGxSynchroEventSDT_GxSynchroEventSDTItem.class));
   }

   public SdtGxSynchroEventSDT_GxSynchroEventSDTItem( ModelContext context )
   {
      super( context, "SdtGxSynchroEventSDT_GxSynchroEventSDTItem");
   }

   public SdtGxSynchroEventSDT_GxSynchroEventSDTItem( int remoteHandle ,
                                                      ModelContext context )
   {
      super( remoteHandle, context, "SdtGxSynchroEventSDT_GxSynchroEventSDTItem");
   }

   private static java.util.HashMap mapper = new java.util.HashMap();
   static
   {
   }

   public String getJsonMap( String value )
   {
      return (String) mapper.get(value);
   }
   
   public void sdttoentity( IEntity androidEntity )
   {
      /*  Save BC members to entity  */
      androidEntity.setProperty("EventId", GXutil.trim( gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventid.toString()));
      androidEntity.setProperty("EventTimestamp", GXutil.timeToCharREST( gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventtimestamp));
      androidEntity.setProperty("EventBC", GXutil.trim( gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventbc));
      androidEntity.setProperty("EventAction", GXutil.trim( GXutil.str( gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventaction, 4, 0)));
      androidEntity.setProperty("EventData", GXutil.trim( gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventdata));
      androidEntity.setProperty("EventStatus", GXutil.trim( GXutil.str( gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventstatus, 4, 0)));
      androidEntity.setProperty("EventErrors", GXutil.trim( gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventerrors));
      androidEntity.setProperty("EventFiles", GXutil.trim( gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventfiles));
   }

   public void entitytosdt( IEntity androidEntity )
   {
      /*  Copy entity values to BC  */
      setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventid( (java.util.UUID) GXutil.strToGuid(androidEntity.optStringProperty("EventId")));
      setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventtimestamp( (java.util.Date) GXutil.charToTimeREST( androidEntity.optStringProperty("EventTimestamp")));
      setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventbc( (String) androidEntity.optStringProperty("EventBC"));
      setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventaction( (short) GXutil.val( androidEntity.optStringProperty("EventAction"), "."));
      setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventdata( (String) androidEntity.optStringProperty("EventData"));
      setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventstatus( (short) GXutil.val( androidEntity.optStringProperty("EventStatus"), "."));
      setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventerrors( (String) androidEntity.optStringProperty("EventErrors"));
      setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventfiles( (String) androidEntity.optStringProperty("EventFiles"));
   }

   public short readxml( com.genexus.xml.XMLReader oReader ,
                         String sName )
   {
      short GXSoapError = 1 ;
      sTagName = oReader.getName() ;
      if ( oReader.getIsSimple() == 0 )
      {
         GXSoapError = oReader.read() ;
         nOutParmCount = (short)(0) ;
         while ( ( ( GXutil.strcmp(oReader.getName(), sTagName) != 0 ) || ( oReader.getNodeType() == 1 ) ) && ( GXSoapError > 0 ) )
         {
            readOk = (short)(0) ;
            if ( GXutil.strcmp2( oReader.getLocalName(), "EventId") )
            {
               gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventid = GXutil.strToGuid(oReader.getValue()) ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( GXutil.strcmp2( oReader.getLocalName(), "EventTimestamp") )
            {
               if ( ( GXutil.strcmp(oReader.getValue(), "0000-00-00T00:00:00") == 0 ) || ( oReader.existsAttribute("xsi:nil") == 1 ) )
               {
                  gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventtimestamp = GXutil.resetTime( GXutil.nullDate() );
               }
               else
               {
                  gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventtimestamp = localUtil.ymdhmsToT( (short)(GXutil.val( GXutil.substring( oReader.getValue(), 1, 4), ".")), (byte)(GXutil.val( GXutil.substring( oReader.getValue(), 6, 2), ".")), (byte)(GXutil.val( GXutil.substring( oReader.getValue(), 9, 2), ".")), (byte)(GXutil.val( GXutil.substring( oReader.getValue(), 12, 2), ".")), (byte)(GXutil.val( GXutil.substring( oReader.getValue(), 15, 2), ".")), (byte)(GXutil.val( GXutil.substring( oReader.getValue(), 18, 2), "."))) ;
               }
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( GXutil.strcmp2( oReader.getLocalName(), "EventBC") )
            {
               gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventbc = oReader.getValue() ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( GXutil.strcmp2( oReader.getLocalName(), "EventAction") )
            {
               gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventaction = (short)(GXutil.lval( oReader.getValue())) ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( GXutil.strcmp2( oReader.getLocalName(), "EventData") )
            {
               gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventdata = oReader.getValue() ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( GXutil.strcmp2( oReader.getLocalName(), "EventStatus") )
            {
               gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventstatus = (short)(GXutil.lval( oReader.getValue())) ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( GXutil.strcmp2( oReader.getLocalName(), "EventErrors") )
            {
               gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventerrors = oReader.getValue() ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( GXutil.strcmp2( oReader.getLocalName(), "EventFiles") )
            {
               gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventfiles = oReader.getValue() ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            nOutParmCount = (short)(nOutParmCount+1) ;
            if ( readOk == 0 )
            {
               context.setSOAPErrMsg(context.getSOAPErrMsg() + "Error reading " + sTagName + GXutil.newLine( ) );
               context.setSOAPErrMsg(context.getSOAPErrMsg() + "Message: " + oReader.readRawXML() );
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
         sName = "GxSynchroEventSDT.GxSynchroEventSDTItem" ;
      }
      if ( (GXutil.strcmp("", sNameSpace)==0) )
      {
         sNameSpace = "PendingEvents" ;
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
      oWriter.writeElement("EventId", GXutil.rtrim( gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventid.toString()));
      if ( GXutil.strcmp(sNameSpace, "PendingEvents") != 0 )
      {
         oWriter.writeAttribute("xmlns", "PendingEvents");
      }
      if ( GXutil.nullDate().equals(gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventtimestamp) )
      {
         oWriter.writeStartElement("EventTimestamp");
         oWriter.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
         oWriter.writeAttribute("xsi:nil", "true");
         oWriter.writeEndElement();
      }
      else
      {
         sDateCnv = "" ;
         sNumToPad = GXutil.trim( GXutil.str( GXutil.year( gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventtimestamp), 10, 0)) ;
         sDateCnv = sDateCnv + GXutil.substring( "0000", 1, 4-GXutil.len( sNumToPad)) + sNumToPad ;
         sDateCnv = sDateCnv + "-" ;
         sNumToPad = GXutil.trim( GXutil.str( GXutil.month( gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventtimestamp), 10, 0)) ;
         sDateCnv = sDateCnv + GXutil.substring( "00", 1, 2-GXutil.len( sNumToPad)) + sNumToPad ;
         sDateCnv = sDateCnv + "-" ;
         sNumToPad = GXutil.trim( GXutil.str( GXutil.day( gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventtimestamp), 10, 0)) ;
         sDateCnv = sDateCnv + GXutil.substring( "00", 1, 2-GXutil.len( sNumToPad)) + sNumToPad ;
         sDateCnv = sDateCnv + "T" ;
         sNumToPad = GXutil.trim( GXutil.str( GXutil.hour( gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventtimestamp), 10, 0)) ;
         sDateCnv = sDateCnv + GXutil.substring( "00", 1, 2-GXutil.len( sNumToPad)) + sNumToPad ;
         sDateCnv = sDateCnv + ":" ;
         sNumToPad = GXutil.trim( GXutil.str( GXutil.minute( gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventtimestamp), 10, 0)) ;
         sDateCnv = sDateCnv + GXutil.substring( "00", 1, 2-GXutil.len( sNumToPad)) + sNumToPad ;
         sDateCnv = sDateCnv + ":" ;
         sNumToPad = GXutil.trim( GXutil.str( GXutil.second( gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventtimestamp), 10, 0)) ;
         sDateCnv = sDateCnv + GXutil.substring( "00", 1, 2-GXutil.len( sNumToPad)) + sNumToPad ;
         oWriter.writeElement("EventTimestamp", sDateCnv);
         if ( GXutil.strcmp(sNameSpace, "PendingEvents") != 0 )
         {
            oWriter.writeAttribute("xmlns", "PendingEvents");
         }
      }
      oWriter.writeElement("EventBC", GXutil.rtrim( gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventbc));
      if ( GXutil.strcmp(sNameSpace, "PendingEvents") != 0 )
      {
         oWriter.writeAttribute("xmlns", "PendingEvents");
      }
      oWriter.writeElement("EventAction", GXutil.trim( GXutil.str( gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventaction, 4, 0)));
      if ( GXutil.strcmp(sNameSpace, "PendingEvents") != 0 )
      {
         oWriter.writeAttribute("xmlns", "PendingEvents");
      }
      oWriter.writeElement("EventData", GXutil.rtrim( gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventdata));
      if ( GXutil.strcmp(sNameSpace, "PendingEvents") != 0 )
      {
         oWriter.writeAttribute("xmlns", "PendingEvents");
      }
      oWriter.writeElement("EventStatus", GXutil.trim( GXutil.str( gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventstatus, 4, 0)));
      if ( GXutil.strcmp(sNameSpace, "PendingEvents") != 0 )
      {
         oWriter.writeAttribute("xmlns", "PendingEvents");
      }
      oWriter.writeElement("EventErrors", GXutil.rtrim( gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventerrors));
      if ( GXutil.strcmp(sNameSpace, "PendingEvents") != 0 )
      {
         oWriter.writeAttribute("xmlns", "PendingEvents");
      }
      oWriter.writeElement("EventFiles", GXutil.rtrim( gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventfiles));
      if ( GXutil.strcmp(sNameSpace, "PendingEvents") != 0 )
      {
         oWriter.writeAttribute("xmlns", "PendingEvents");
      }
      oWriter.writeEndElement();
   }

   public void tojson( )
   {
      tojson( true) ;
   }

   public void tojson( boolean includeState )
   {
      AddObjectProperty("EventId", gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventid, false);
      datetime_STZ = gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventtimestamp ;
      sDateCnv = "" ;
      sNumToPad = GXutil.trim( GXutil.str( GXutil.year( datetime_STZ), 10, 0)) ;
      sDateCnv = sDateCnv + GXutil.substring( "0000", 1, 4-GXutil.len( sNumToPad)) + sNumToPad ;
      sDateCnv = sDateCnv + "-" ;
      sNumToPad = GXutil.trim( GXutil.str( GXutil.month( datetime_STZ), 10, 0)) ;
      sDateCnv = sDateCnv + GXutil.substring( "00", 1, 2-GXutil.len( sNumToPad)) + sNumToPad ;
      sDateCnv = sDateCnv + "-" ;
      sNumToPad = GXutil.trim( GXutil.str( GXutil.day( datetime_STZ), 10, 0)) ;
      sDateCnv = sDateCnv + GXutil.substring( "00", 1, 2-GXutil.len( sNumToPad)) + sNumToPad ;
      sDateCnv = sDateCnv + "T" ;
      sNumToPad = GXutil.trim( GXutil.str( GXutil.hour( datetime_STZ), 10, 0)) ;
      sDateCnv = sDateCnv + GXutil.substring( "00", 1, 2-GXutil.len( sNumToPad)) + sNumToPad ;
      sDateCnv = sDateCnv + ":" ;
      sNumToPad = GXutil.trim( GXutil.str( GXutil.minute( datetime_STZ), 10, 0)) ;
      sDateCnv = sDateCnv + GXutil.substring( "00", 1, 2-GXutil.len( sNumToPad)) + sNumToPad ;
      sDateCnv = sDateCnv + ":" ;
      sNumToPad = GXutil.trim( GXutil.str( GXutil.second( datetime_STZ), 10, 0)) ;
      sDateCnv = sDateCnv + GXutil.substring( "00", 1, 2-GXutil.len( sNumToPad)) + sNumToPad ;
      AddObjectProperty("EventTimestamp", sDateCnv, false);
      AddObjectProperty("EventBC", gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventbc, false);
      AddObjectProperty("EventAction", gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventaction, false);
      AddObjectProperty("EventData", gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventdata, false);
      AddObjectProperty("EventStatus", gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventstatus, false);
      AddObjectProperty("EventErrors", gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventerrors, false);
      AddObjectProperty("EventFiles", gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventfiles, false);
   }

   public java.util.UUID getgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventid( )
   {
      return gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventid ;
   }

   public void setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventid( java.util.UUID value )
   {
      gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventid = value ;
   }

   public java.util.Date getgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventtimestamp( )
   {
      return gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventtimestamp ;
   }

   public void setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventtimestamp( java.util.Date value )
   {
      gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventtimestamp = value ;
   }

   public String getgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventbc( )
   {
      return gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventbc ;
   }

   public void setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventbc( String value )
   {
      gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventbc = value ;
   }

   public short getgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventaction( )
   {
      return gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventaction ;
   }

   public void setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventaction( short value )
   {
      gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventaction = value ;
   }

   public String getgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventdata( )
   {
      return gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventdata ;
   }

   public void setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventdata( String value )
   {
      gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventdata = value ;
   }

   public short getgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventstatus( )
   {
      return gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventstatus ;
   }

   public void setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventstatus( short value )
   {
      gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventstatus = value ;
   }

   public String getgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventerrors( )
   {
      return gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventerrors ;
   }

   public void setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventerrors( String value )
   {
      gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventerrors = value ;
   }

   public String getgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventfiles( )
   {
      return gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventfiles ;
   }

   public void setgxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventfiles( String value )
   {
      gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventfiles = value ;
   }

   public void initialize( int remoteHandle )
   {
      initialize( ) ;
   }

   public void initialize( )
   {
      gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventid = java.util.UUID.fromString("00000000-0000-0000-0000-000000000000") ;
      gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventtimestamp = GXutil.resetTime( GXutil.nullDate() );
      gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventbc = "" ;
      gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventdata = "" ;
      gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventerrors = "" ;
      gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventfiles = "" ;
      sTagName = "" ;
      sDateCnv = "" ;
      sNumToPad = "" ;
      datetime_STZ = GXutil.resetTime( GXutil.nullDate() );
   }

   public SdtGxSynchroEventSDT_GxSynchroEventSDTItem Clone( )
   {
      return (SdtGxSynchroEventSDT_GxSynchroEventSDTItem)(clone()) ;
   }

   protected short gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventaction ;
   protected short gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventstatus ;
   protected short readOk ;
   protected short nOutParmCount ;
   protected String sTagName ;
   protected String sDateCnv ;
   protected String sNumToPad ;
   protected java.util.Date gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventtimestamp ;
   protected java.util.Date datetime_STZ ;
   protected String gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventdata ;
   protected String gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventerrors ;
   protected String gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventfiles ;
   protected String gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventbc ;
   protected java.util.UUID gxTv_SdtGxSynchroEventSDT_GxSynchroEventSDTItem_Eventid ;
}

