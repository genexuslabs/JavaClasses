/*
               File: SdtGxPendingEvent
        Description: Gx Pending Event
             Author: GeneXus Android Generator version 10_4_0-89519
       Generated on: March 18, 2015 13:55:8.19
       Program type: Callable routine
          Main DBMS: sqlite
*/
package com.artech.base.synchronization.bc;
import com.genexus.*;
import com.artech.base.services.*;

public final  class SdtGxPendingEvent extends GxSilentTrnSdt implements Cloneable, java.io.Serializable, IGxBusinessComponent
{
   
	private static final long serialVersionUID = 1L;
	
	public SdtGxPendingEvent( int remoteHandle )
   {
      this( remoteHandle,  new ModelContext(SdtGxPendingEvent.class));
   }

   public SdtGxPendingEvent( int remoteHandle ,
                             ModelContext context )
   {
      super( context, "SdtGxPendingEvent");
      initialize( remoteHandle) ;
   }

   public void Load( java.util.UUID AV1PendingEventId )
   {
      IGxSilentTrn obj ;
      obj = getTransaction() ;
      obj.LoadKey(new Object[] {AV1PendingEventId});
   }

   public Object[][] GetBCKey( )
   {
      return (Object[][])(new Object[][]{new Object[]{"PendingEventId", java.util.UUID.class}}) ;
   }

   public com.genexus.internet.MsgList GetMessagesEx( )
   {
	  IGxSilentTrn trn = getTransaction() ;
	  com.genexus.internet.MsgList msgList = trn.GetMessages() ;
	  return msgList;
   }

   public void sdttoentity( IEntity androidEntity )
   {
      /*  Save BC members to entity  */
      androidEntity.setProperty("PendingEventId", CommonUtil.trim( gxTv_SdtGxPendingEvent_Pendingeventid.toString()));
      androidEntity.setProperty("PendingEventTimestamp", com.genexus.GXutil.timeToCharREST( gxTv_SdtGxPendingEvent_Pendingeventtimestamp));
      androidEntity.setProperty("PendingEventBC", CommonUtil.trim( gxTv_SdtGxPendingEvent_Pendingeventbc));
      androidEntity.setProperty("PendingEventAction", CommonUtil.trim( GXutil.str( gxTv_SdtGxPendingEvent_Pendingeventaction, 4, 0)));
      androidEntity.setProperty("PendingEventData", CommonUtil.trim( gxTv_SdtGxPendingEvent_Pendingeventdata));
      androidEntity.setProperty("PendingEventStatus", CommonUtil.trim( com.genexus.GXutil.str( gxTv_SdtGxPendingEvent_Pendingeventstatus, 4, 0)));
      androidEntity.setProperty("PendingEventErrors", CommonUtil.trim( gxTv_SdtGxPendingEvent_Pendingeventerrors));
      androidEntity.setProperty("PendingEventExtras", CommonUtil.trim( gxTv_SdtGxPendingEvent_Pendingeventextras));
      androidEntity.setProperty("PendingEventFiles", CommonUtil.trim( gxTv_SdtGxPendingEvent_Pendingeventfiles));
   }

   public void entitytosdt( IEntity androidEntity )
   {
      /*  Copy entity values to BC  */
      setgxTv_SdtGxPendingEvent_Pendingeventid( (java.util.UUID) CommonUtil.strToGuid(androidEntity.optStringProperty("PendingEventId")));
      setgxTv_SdtGxPendingEvent_Pendingeventtimestamp( (java.util.Date) com.genexus.GXutil.charToTimeREST( androidEntity.optStringProperty("PendingEventTimestamp")));
      setgxTv_SdtGxPendingEvent_Pendingeventbc( (String) androidEntity.optStringProperty("PendingEventBC"));
      setgxTv_SdtGxPendingEvent_Pendingeventaction( (short) CommonUtil.val( androidEntity.optStringProperty("PendingEventAction"), "."));
      setgxTv_SdtGxPendingEvent_Pendingeventdata( (String) androidEntity.optStringProperty("PendingEventData"));
      setgxTv_SdtGxPendingEvent_Pendingeventstatus( (short) CommonUtil.val( androidEntity.optStringProperty("PendingEventStatus"), "."));
      setgxTv_SdtGxPendingEvent_Pendingeventerrors( (String) androidEntity.optStringProperty("PendingEventErrors"));
      setgxTv_SdtGxPendingEvent_Pendingeventextras( (String) androidEntity.optStringProperty("PendingEventExtras"));
      setgxTv_SdtGxPendingEvent_Pendingeventfiles( (String) androidEntity.optStringProperty("PendingEventFiles"));
   }

   public void initentity( IEntity androidEntity )
   {
      /*  Load BC Defaults  */
      IGxSilentTrn trn = getTransaction() ;
      trn.getInsDefault();
      this.sdttoentity(androidEntity);
   }

   public boolean loadbcfromkey( IEntity androidEntity )
   {
      /*  Read BC keys from entity  */
      java.util.UUID AV1PendingEventId ;
      AV1PendingEventId = CommonUtil.strToGuid(androidEntity.optStringProperty("PendingEventId")) ;
      /*  Load BC from keys  */
      this.Load(AV1PendingEventId);
      /*  Save BC members to entity  */
      this.sdttoentity(androidEntity);
      return (boolean)(this.Success()) ;
   }

   public boolean savebcfromentity( IEntity androidEntity )
   {
      /*  Copy entity values to BC  */
      this.entitytosdt(androidEntity);
      /*  Save BC with entity changes  */
      this.Save();
      /*  Save BC members to entity  */
      this.sdttoentity(androidEntity);
      return (boolean)(this.Success()) ;
   }

   public boolean delete( )
   {
      this.Delete();
      return (boolean)(this.Success()) ;
   }

   public boolean success( )
   {
      return (boolean)(this.Success()) ;
   }

   public com.genexus.internet.MsgList getmessages( )
   {
      IGxSilentTrn trn = getTransaction() ;
      return (com.genexus.internet.MsgList)(trn.GetMessages()) ;
   }

   public String getbcname( )
   {
      return "GxPendingEvent" ;
   }

   public com.genexus.util.GXProperties getMetadata( )
   {
      com.genexus.util.GXProperties metadata = new com.genexus.util.GXProperties() ;
      metadata.set("Name", "GxPendingEvent");
      metadata.set("BT", "GxPendingEvent");
      metadata.set("PK", "[ \"PendingEventId\" ]");
      metadata.set("PKAssigned", "[ \"PendingEventId\" ]");
      metadata.set("AllowInsert", "True");
      metadata.set("AllowUpdate", "True");
      metadata.set("AllowDelete", "True");
      return metadata ;
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
         while ( ( ( CommonUtil.strcmp(oReader.getName(), sTagName) != 0 ) || ( oReader.getNodeType() == 1 ) ) && ( GXSoapError > 0 ) )
         {
            readOk = (short)(0) ;
            if ( CommonUtil.strcmp2( oReader.getLocalName(), "PendingEventId") )
            {
               gxTv_SdtGxPendingEvent_Pendingeventid = CommonUtil.strToGuid(oReader.getValue()) ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( CommonUtil.strcmp2( oReader.getLocalName(), "PendingEventTimestamp") )
            {
               if ( ( CommonUtil.strcmp(oReader.getValue(), "0000-00-00T00:00:00") == 0 ) || ( oReader.existsAttribute("xsi:nil") == 1 ) )
               {
                  gxTv_SdtGxPendingEvent_Pendingeventtimestamp = CommonUtil.resetTime( CommonUtil.nullDate() );
               }
               else
               {
                  gxTv_SdtGxPendingEvent_Pendingeventtimestamp = localUtil.ymdhmsToT( (short)(CommonUtil.val( CommonUtil.substring( oReader.getValue(), 1, 4), ".")), (byte)(CommonUtil.val( CommonUtil.substring( oReader.getValue(), 6, 2), ".")), (byte)(CommonUtil.val( CommonUtil.substring( oReader.getValue(), 9, 2), ".")), (byte)(CommonUtil.val( CommonUtil.substring( oReader.getValue(), 12, 2), ".")), (byte)(CommonUtil.val( CommonUtil.substring( oReader.getValue(), 15, 2), ".")), (byte)(CommonUtil.val( CommonUtil.substring( oReader.getValue(), 18, 2), "."))) ;
               }
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( CommonUtil.strcmp2( oReader.getLocalName(), "PendingEventBC") )
            {
               gxTv_SdtGxPendingEvent_Pendingeventbc = oReader.getValue() ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( CommonUtil.strcmp2( oReader.getLocalName(), "PendingEventAction") )
            {
               gxTv_SdtGxPendingEvent_Pendingeventaction = (short)(CommonUtil.lval( oReader.getValue())) ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( CommonUtil.strcmp2( oReader.getLocalName(), "PendingEventData") )
            {
               gxTv_SdtGxPendingEvent_Pendingeventdata = oReader.getValue() ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( CommonUtil.strcmp2( oReader.getLocalName(), "PendingEventStatus") )
            {
               gxTv_SdtGxPendingEvent_Pendingeventstatus = (short)(CommonUtil.lval( oReader.getValue())) ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( CommonUtil.strcmp2( oReader.getLocalName(), "PendingEventErrors") )
            {
               gxTv_SdtGxPendingEvent_Pendingeventerrors = oReader.getValue() ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( CommonUtil.strcmp2( oReader.getLocalName(), "PendingEventExtras") )
            {
               gxTv_SdtGxPendingEvent_Pendingeventextras = oReader.getValue() ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( CommonUtil.strcmp2( oReader.getLocalName(), "PendingEventFiles") )
            {
               gxTv_SdtGxPendingEvent_Pendingeventfiles = oReader.getValue() ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( CommonUtil.strcmp2( oReader.getLocalName(), "Mode") )
            {
               gxTv_SdtGxPendingEvent_Mode = oReader.getValue() ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( CommonUtil.strcmp2( oReader.getLocalName(), "Initialized") )
            {
               gxTv_SdtGxPendingEvent_Initialized = (short)(CommonUtil.lval( oReader.getValue())) ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( CommonUtil.strcmp2( oReader.getLocalName(), "PendingEventId_Z") )
            {
               gxTv_SdtGxPendingEvent_Pendingeventid_Z = CommonUtil.strToGuid(oReader.getValue()) ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( CommonUtil.strcmp2( oReader.getLocalName(), "PendingEventTimestamp_Z") )
            {
               if ( ( CommonUtil.strcmp(oReader.getValue(), "0000-00-00T00:00:00") == 0 ) || ( oReader.existsAttribute("xsi:nil") == 1 ) )
               {
                  gxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z = CommonUtil.resetTime( CommonUtil.nullDate() );
               }
               else
               {
                  gxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z = localUtil.ymdhmsToT( (short)(CommonUtil.val( CommonUtil.substring( oReader.getValue(), 1, 4), ".")), (byte)(CommonUtil.val( CommonUtil.substring( oReader.getValue(), 6, 2), ".")), (byte)(CommonUtil.val( CommonUtil.substring( oReader.getValue(), 9, 2), ".")), (byte)(CommonUtil.val( CommonUtil.substring( oReader.getValue(), 12, 2), ".")), (byte)(CommonUtil.val( CommonUtil.substring( oReader.getValue(), 15, 2), ".")), (byte)(CommonUtil.val( CommonUtil.substring( oReader.getValue(), 18, 2), "."))) ;
               }
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( CommonUtil.strcmp2( oReader.getLocalName(), "PendingEventBC_Z") )
            {
               gxTv_SdtGxPendingEvent_Pendingeventbc_Z = oReader.getValue() ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( CommonUtil.strcmp2( oReader.getLocalName(), "PendingEventAction_Z") )
            {
               gxTv_SdtGxPendingEvent_Pendingeventaction_Z = (short)(CommonUtil.lval( oReader.getValue())) ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            if ( CommonUtil.strcmp2( oReader.getLocalName(), "PendingEventStatus_Z") )
            {
               gxTv_SdtGxPendingEvent_Pendingeventstatus_Z = (short)(CommonUtil.lval( oReader.getValue())) ;
               if ( GXSoapError > 0 )
               {
                  readOk = (short)(1) ;
               }
               GXSoapError = oReader.read() ;
            }
            nOutParmCount = (short)(nOutParmCount+1) ;
            if ( readOk == 0 )
            {
               context.setSOAPErrMsg(context.getSOAPErrMsg() + "Error reading " + sTagName + CommonUtil.newLine( ) );
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
      if ( (CommonUtil.strcmp("", sName)==0) )
      {
         sName = "GxPendingEvent" ;
      }
      if ( (CommonUtil.strcmp("", sNameSpace)==0) )
      {
         sNameSpace = "PendingEvents" ;
      }
      oWriter.writeStartElement(sName);
      if ( CommonUtil.strcmp(CommonUtil.left( sNameSpace, 10), "[*:nosend]") != 0 )
      {
         oWriter.writeAttribute("xmlns", sNameSpace);
      }
      else
      {
         sNameSpace = CommonUtil.right( sNameSpace, CommonUtil.len( sNameSpace)-10) ;
      }
      oWriter.writeElement("PendingEventId", CommonUtil.rtrim( gxTv_SdtGxPendingEvent_Pendingeventid.toString()));
      if ( CommonUtil.strcmp(sNameSpace, "PendingEvents") != 0 )
      {
         oWriter.writeAttribute("xmlns", "PendingEvents");
      }
      if ( CommonUtil.nullDate().equals(gxTv_SdtGxPendingEvent_Pendingeventtimestamp) )
      {
         oWriter.writeStartElement("PendingEventTimestamp");
         oWriter.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
         oWriter.writeAttribute("xsi:nil", "true");
         oWriter.writeEndElement();
      }
      else
      {
         sDateCnv = "" ;
         sNumToPad = CommonUtil.trim( com.genexus.GXutil.str( CommonUtil.year( gxTv_SdtGxPendingEvent_Pendingeventtimestamp), 10, 0)) ;
         sDateCnv = sDateCnv + CommonUtil.substring( "0000", 1, 4-CommonUtil.len( sNumToPad)) + sNumToPad ;
         sDateCnv = sDateCnv + "-" ;
         sNumToPad = CommonUtil.trim( com.genexus.GXutil.str( CommonUtil.month( gxTv_SdtGxPendingEvent_Pendingeventtimestamp), 10, 0)) ;
         sDateCnv = sDateCnv + CommonUtil.substring( "00", 1, 2-CommonUtil.len( sNumToPad)) + sNumToPad ;
         sDateCnv = sDateCnv + "-" ;
         sNumToPad = CommonUtil.trim( com.genexus.GXutil.str( CommonUtil.day( gxTv_SdtGxPendingEvent_Pendingeventtimestamp), 10, 0)) ;
         sDateCnv = sDateCnv + CommonUtil.substring( "00", 1, 2-CommonUtil.len( sNumToPad)) + sNumToPad ;
         sDateCnv = sDateCnv + "T" ;
         sNumToPad = CommonUtil.trim( com.genexus.GXutil.str( CommonUtil.hour( gxTv_SdtGxPendingEvent_Pendingeventtimestamp), 10, 0)) ;
         sDateCnv = sDateCnv + CommonUtil.substring( "00", 1, 2-CommonUtil.len( sNumToPad)) + sNumToPad ;
         sDateCnv = sDateCnv + ":" ;
         sNumToPad = CommonUtil.trim( com.genexus.GXutil.str( CommonUtil.minute( gxTv_SdtGxPendingEvent_Pendingeventtimestamp), 10, 0)) ;
         sDateCnv = sDateCnv + CommonUtil.substring( "00", 1, 2-CommonUtil.len( sNumToPad)) + sNumToPad ;
         sDateCnv = sDateCnv + ":" ;
         sNumToPad = CommonUtil.trim( com.genexus.GXutil.str( CommonUtil.second( gxTv_SdtGxPendingEvent_Pendingeventtimestamp), 10, 0)) ;
         sDateCnv = sDateCnv + CommonUtil.substring( "00", 1, 2-CommonUtil.len( sNumToPad)) + sNumToPad ;
         oWriter.writeElement("PendingEventTimestamp", sDateCnv);
         if ( CommonUtil.strcmp(sNameSpace, "PendingEvents") != 0 )
         {
            oWriter.writeAttribute("xmlns", "PendingEvents");
         }
      }
      oWriter.writeElement("PendingEventBC", CommonUtil.rtrim( gxTv_SdtGxPendingEvent_Pendingeventbc));
      if ( CommonUtil.strcmp(sNameSpace, "PendingEvents") != 0 )
      {
         oWriter.writeAttribute("xmlns", "PendingEvents");
      }
      oWriter.writeElement("PendingEventAction", CommonUtil.trim( com.genexus.GXutil.str( gxTv_SdtGxPendingEvent_Pendingeventaction, 4, 0)));
      if ( CommonUtil.strcmp(sNameSpace, "PendingEvents") != 0 )
      {
         oWriter.writeAttribute("xmlns", "PendingEvents");
      }
      oWriter.writeElement("PendingEventData", CommonUtil.rtrim( gxTv_SdtGxPendingEvent_Pendingeventdata));
      if ( CommonUtil.strcmp(sNameSpace, "PendingEvents") != 0 )
      {
         oWriter.writeAttribute("xmlns", "PendingEvents");
      }
      oWriter.writeElement("PendingEventStatus", CommonUtil.trim( com.genexus.GXutil.str( gxTv_SdtGxPendingEvent_Pendingeventstatus, 4, 0)));
      if ( CommonUtil.strcmp(sNameSpace, "PendingEvents") != 0 )
      {
         oWriter.writeAttribute("xmlns", "PendingEvents");
      }
      oWriter.writeElement("PendingEventErrors", CommonUtil.rtrim( gxTv_SdtGxPendingEvent_Pendingeventerrors));
      if ( CommonUtil.strcmp(sNameSpace, "PendingEvents") != 0 )
      {
         oWriter.writeAttribute("xmlns", "PendingEvents");
      }
      oWriter.writeElement("PendingEventExtras", CommonUtil.rtrim( gxTv_SdtGxPendingEvent_Pendingeventextras));
      if ( CommonUtil.strcmp(sNameSpace, "PendingEvents") != 0 )
      {
         oWriter.writeAttribute("xmlns", "PendingEvents");
      }
      oWriter.writeElement("PendingEventFiles", CommonUtil.rtrim( gxTv_SdtGxPendingEvent_Pendingeventfiles));
      if ( CommonUtil.strcmp(sNameSpace, "PendingEvents") != 0 )
      {
         oWriter.writeAttribute("xmlns", "PendingEvents");
      }
      if ( sIncludeState )
      {
         oWriter.writeElement("Mode", CommonUtil.rtrim( gxTv_SdtGxPendingEvent_Mode));
         if ( CommonUtil.strcmp(sNameSpace, "PendingEvents") != 0 )
         {
            oWriter.writeAttribute("xmlns", "PendingEvents");
         }
         oWriter.writeElement("Initialized", CommonUtil.trim( com.genexus.GXutil.str( gxTv_SdtGxPendingEvent_Initialized, 4, 0)));
         if ( CommonUtil.strcmp(sNameSpace, "PendingEvents") != 0 )
         {
            oWriter.writeAttribute("xmlns", "PendingEvents");
         }
         oWriter.writeElement("PendingEventId_Z", CommonUtil.rtrim( gxTv_SdtGxPendingEvent_Pendingeventid_Z.toString()));
         if ( CommonUtil.strcmp(sNameSpace, "PendingEvents") != 0 )
         {
            oWriter.writeAttribute("xmlns", "PendingEvents");
         }
         if ( CommonUtil.nullDate().equals(gxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z) )
         {
            oWriter.writeStartElement("PendingEventTimestamp_Z");
            oWriter.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            oWriter.writeAttribute("xsi:nil", "true");
            oWriter.writeEndElement();
         }
         else
         {
            sDateCnv = "" ;
            sNumToPad = CommonUtil.trim( com.genexus.GXutil.str( CommonUtil.year( gxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z), 10, 0)) ;
            sDateCnv = sDateCnv + CommonUtil.substring( "0000", 1, 4-CommonUtil.len( sNumToPad)) + sNumToPad ;
            sDateCnv = sDateCnv + "-" ;
            sNumToPad = CommonUtil.trim( com.genexus.GXutil.str( CommonUtil.month( gxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z), 10, 0)) ;
            sDateCnv = sDateCnv + CommonUtil.substring( "00", 1, 2-CommonUtil.len( sNumToPad)) + sNumToPad ;
            sDateCnv = sDateCnv + "-" ;
            sNumToPad = CommonUtil.trim( com.genexus.GXutil.str( CommonUtil.day( gxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z), 10, 0)) ;
            sDateCnv = sDateCnv + CommonUtil.substring( "00", 1, 2-CommonUtil.len( sNumToPad)) + sNumToPad ;
            sDateCnv = sDateCnv + "T" ;
            sNumToPad = CommonUtil.trim( com.genexus.GXutil.str( CommonUtil.hour( gxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z), 10, 0)) ;
            sDateCnv = sDateCnv + CommonUtil.substring( "00", 1, 2-CommonUtil.len( sNumToPad)) + sNumToPad ;
            sDateCnv = sDateCnv + ":" ;
            sNumToPad = CommonUtil.trim( com.genexus.GXutil.str( CommonUtil.minute( gxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z), 10, 0)) ;
            sDateCnv = sDateCnv + CommonUtil.substring( "00", 1, 2-CommonUtil.len( sNumToPad)) + sNumToPad ;
            sDateCnv = sDateCnv + ":" ;
            sNumToPad = CommonUtil.trim( com.genexus.GXutil.str( CommonUtil.second( gxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z), 10, 0)) ;
            sDateCnv = sDateCnv + CommonUtil.substring( "00", 1, 2-CommonUtil.len( sNumToPad)) + sNumToPad ;
            oWriter.writeElement("PendingEventTimestamp_Z", sDateCnv);
            if ( CommonUtil.strcmp(sNameSpace, "PendingEvents") != 0 )
            {
               oWriter.writeAttribute("xmlns", "PendingEvents");
            }
         }
         oWriter.writeElement("PendingEventBC_Z", CommonUtil.rtrim( gxTv_SdtGxPendingEvent_Pendingeventbc_Z));
         if ( CommonUtil.strcmp(sNameSpace, "PendingEvents") != 0 )
         {
            oWriter.writeAttribute("xmlns", "PendingEvents");
         }
         oWriter.writeElement("PendingEventAction_Z", CommonUtil.trim( com.genexus.GXutil.str( gxTv_SdtGxPendingEvent_Pendingeventaction_Z, 4, 0)));
         if ( CommonUtil.strcmp(sNameSpace, "PendingEvents") != 0 )
         {
            oWriter.writeAttribute("xmlns", "PendingEvents");
         }
         oWriter.writeElement("PendingEventStatus_Z", CommonUtil.trim( com.genexus.GXutil.str( gxTv_SdtGxPendingEvent_Pendingeventstatus_Z, 4, 0)));
         if ( CommonUtil.strcmp(sNameSpace, "PendingEvents") != 0 )
         {
            oWriter.writeAttribute("xmlns", "PendingEvents");
         }
      }
      oWriter.writeEndElement();
   }

   public void tojson( )
   {
      tojson( true) ;
   }

   public void tojson( boolean includeState )
   {
      AddObjectProperty("PendingEventId", gxTv_SdtGxPendingEvent_Pendingeventid, false);
      datetime_STZ = gxTv_SdtGxPendingEvent_Pendingeventtimestamp ;
      sDateCnv = "" ;
      sNumToPad = CommonUtil.trim( com.genexus.GXutil.str( CommonUtil.year( datetime_STZ), 10, 0)) ;
      sDateCnv = sDateCnv + CommonUtil.substring( "0000", 1, 4-CommonUtil.len( sNumToPad)) + sNumToPad ;
      sDateCnv = sDateCnv + "-" ;
      sNumToPad = CommonUtil.trim( com.genexus.GXutil.str( CommonUtil.month( datetime_STZ), 10, 0)) ;
      sDateCnv = sDateCnv + CommonUtil.substring( "00", 1, 2-CommonUtil.len( sNumToPad)) + sNumToPad ;
      sDateCnv = sDateCnv + "-" ;
      sNumToPad = CommonUtil.trim( com.genexus.GXutil.str( CommonUtil.day( datetime_STZ), 10, 0)) ;
      sDateCnv = sDateCnv + CommonUtil.substring( "00", 1, 2-CommonUtil.len( sNumToPad)) + sNumToPad ;
      sDateCnv = sDateCnv + "T" ;
      sNumToPad = CommonUtil.trim( com.genexus.GXutil.str( CommonUtil.hour( datetime_STZ), 10, 0)) ;
      sDateCnv = sDateCnv + CommonUtil.substring( "00", 1, 2-CommonUtil.len( sNumToPad)) + sNumToPad ;
      sDateCnv = sDateCnv + ":" ;
      sNumToPad = CommonUtil.trim( com.genexus.GXutil.str( CommonUtil.minute( datetime_STZ), 10, 0)) ;
      sDateCnv = sDateCnv + CommonUtil.substring( "00", 1, 2-CommonUtil.len( sNumToPad)) + sNumToPad ;
      sDateCnv = sDateCnv + ":" ;
      sNumToPad = CommonUtil.trim( com.genexus.GXutil.str( CommonUtil.second( datetime_STZ), 10, 0)) ;
      sDateCnv = sDateCnv + CommonUtil.substring( "00", 1, 2-CommonUtil.len( sNumToPad)) + sNumToPad ;
      AddObjectProperty("PendingEventTimestamp", sDateCnv, false);
      AddObjectProperty("PendingEventBC", gxTv_SdtGxPendingEvent_Pendingeventbc, false);
      AddObjectProperty("PendingEventAction", gxTv_SdtGxPendingEvent_Pendingeventaction, false);
      AddObjectProperty("PendingEventData", gxTv_SdtGxPendingEvent_Pendingeventdata, false);
      AddObjectProperty("PendingEventStatus", gxTv_SdtGxPendingEvent_Pendingeventstatus, false);
      AddObjectProperty("PendingEventErrors", gxTv_SdtGxPendingEvent_Pendingeventerrors, false);
      AddObjectProperty("PendingEventExtras", gxTv_SdtGxPendingEvent_Pendingeventextras, false);
      AddObjectProperty("PendingEventFiles", gxTv_SdtGxPendingEvent_Pendingeventfiles, false);
      if ( includeState )
      {
         AddObjectProperty("Mode", gxTv_SdtGxPendingEvent_Mode, false);
         AddObjectProperty("Initialized", gxTv_SdtGxPendingEvent_Initialized, false);
         AddObjectProperty("PendingEventId_Z", gxTv_SdtGxPendingEvent_Pendingeventid_Z, false);
         datetime_STZ = gxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z ;
         sDateCnv = "" ;
         sNumToPad = CommonUtil.trim( GXutil.str( CommonUtil.year( datetime_STZ), 10, 0)) ;
         sDateCnv = sDateCnv + CommonUtil.substring( "0000", 1, 4-CommonUtil.len( sNumToPad)) + sNumToPad ;
         sDateCnv = sDateCnv + "-" ;
         sNumToPad = CommonUtil.trim( GXutil.str( CommonUtil.month( datetime_STZ), 10, 0)) ;
         sDateCnv = sDateCnv + CommonUtil.substring( "00", 1, 2-CommonUtil.len( sNumToPad)) + sNumToPad ;
         sDateCnv = sDateCnv + "-" ;
         sNumToPad = CommonUtil.trim( GXutil.str( CommonUtil.day( datetime_STZ), 10, 0)) ;
         sDateCnv = sDateCnv + CommonUtil.substring( "00", 1, 2-CommonUtil.len( sNumToPad)) + sNumToPad ;
         sDateCnv = sDateCnv + "T" ;
         sNumToPad = CommonUtil.trim( GXutil.str( CommonUtil.hour( datetime_STZ), 10, 0)) ;
         sDateCnv = sDateCnv + CommonUtil.substring( "00", 1, 2-CommonUtil.len( sNumToPad)) + sNumToPad ;
         sDateCnv = sDateCnv + ":" ;
         sNumToPad = CommonUtil.trim( GXutil.str( CommonUtil.minute( datetime_STZ), 10, 0)) ;
         sDateCnv = sDateCnv + CommonUtil.substring( "00", 1, 2-CommonUtil.len( sNumToPad)) + sNumToPad ;
         sDateCnv = sDateCnv + ":" ;
         sNumToPad = CommonUtil.trim( GXutil.str( CommonUtil.second( datetime_STZ), 10, 0)) ;
         sDateCnv = sDateCnv + CommonUtil.substring( "00", 1, 2-CommonUtil.len( sNumToPad)) + sNumToPad ;
         AddObjectProperty("PendingEventTimestamp_Z", sDateCnv, false);
         AddObjectProperty("PendingEventBC_Z", gxTv_SdtGxPendingEvent_Pendingeventbc_Z, false);
         AddObjectProperty("PendingEventAction_Z", gxTv_SdtGxPendingEvent_Pendingeventaction_Z, false);
         AddObjectProperty("PendingEventStatus_Z", gxTv_SdtGxPendingEvent_Pendingeventstatus_Z, false);
      }
   }

   public void updateDirties( SdtGxPendingEvent sdt )
   {
      if ( sdt.IsDirty("PendingEventId") )
      {
         gxTv_SdtGxPendingEvent_Pendingeventid = sdt.getgxTv_SdtGxPendingEvent_Pendingeventid() ;
      }
      if ( sdt.IsDirty("PendingEventTimestamp") )
      {
         gxTv_SdtGxPendingEvent_Pendingeventtimestamp = sdt.getgxTv_SdtGxPendingEvent_Pendingeventtimestamp() ;
      }
      if ( sdt.IsDirty("PendingEventBC") )
      {
         gxTv_SdtGxPendingEvent_Pendingeventbc = sdt.getgxTv_SdtGxPendingEvent_Pendingeventbc() ;
      }
      if ( sdt.IsDirty("PendingEventAction") )
      {
         gxTv_SdtGxPendingEvent_Pendingeventaction = sdt.getgxTv_SdtGxPendingEvent_Pendingeventaction() ;
      }
      if ( sdt.IsDirty("PendingEventData") )
      {
         gxTv_SdtGxPendingEvent_Pendingeventdata = sdt.getgxTv_SdtGxPendingEvent_Pendingeventdata() ;
      }
      if ( sdt.IsDirty("PendingEventStatus") )
      {
         gxTv_SdtGxPendingEvent_Pendingeventstatus = sdt.getgxTv_SdtGxPendingEvent_Pendingeventstatus() ;
      }
      if ( sdt.IsDirty("PendingEventErrors") )
      {
         gxTv_SdtGxPendingEvent_Pendingeventerrors = sdt.getgxTv_SdtGxPendingEvent_Pendingeventerrors() ;
      }
      if ( sdt.IsDirty("PendingEventExtras") )
      {
         gxTv_SdtGxPendingEvent_Pendingeventextras = sdt.getgxTv_SdtGxPendingEvent_Pendingeventextras() ;
      }
      if ( sdt.IsDirty("PendingEventFiles") )
      {
         gxTv_SdtGxPendingEvent_Pendingeventfiles = sdt.getgxTv_SdtGxPendingEvent_Pendingeventfiles() ;
      }
   }

   public java.util.UUID getgxTv_SdtGxPendingEvent_Pendingeventid( )
   {
      return gxTv_SdtGxPendingEvent_Pendingeventid ;
   }

   public void setgxTv_SdtGxPendingEvent_Pendingeventid( java.util.UUID value )
   {
      if ( !( gxTv_SdtGxPendingEvent_Pendingeventid.equals( value ) ) )
      {
         gxTv_SdtGxPendingEvent_Mode = "INS" ;
         this.setgxTv_SdtGxPendingEvent_Pendingeventid_Z_SetNull( );
         this.setgxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z_SetNull( );
         this.setgxTv_SdtGxPendingEvent_Pendingeventbc_Z_SetNull( );
         this.setgxTv_SdtGxPendingEvent_Pendingeventaction_Z_SetNull( );
         this.setgxTv_SdtGxPendingEvent_Pendingeventstatus_Z_SetNull( );
      }
      SetDirty("Pendingeventid");
      gxTv_SdtGxPendingEvent_Pendingeventid = value ;
   }

   public java.util.Date getgxTv_SdtGxPendingEvent_Pendingeventtimestamp( )
   {
      return gxTv_SdtGxPendingEvent_Pendingeventtimestamp ;
   }

   public void setgxTv_SdtGxPendingEvent_Pendingeventtimestamp( java.util.Date value )
   {
      SetDirty("Pendingeventtimestamp");
      gxTv_SdtGxPendingEvent_Pendingeventtimestamp = value ;
   }

   public String getgxTv_SdtGxPendingEvent_Pendingeventbc( )
   {
      return gxTv_SdtGxPendingEvent_Pendingeventbc ;
   }

   public void setgxTv_SdtGxPendingEvent_Pendingeventbc( String value )
   {
      SetDirty("Pendingeventbc");
      gxTv_SdtGxPendingEvent_Pendingeventbc = value ;
   }

   public short getgxTv_SdtGxPendingEvent_Pendingeventaction( )
   {
      return gxTv_SdtGxPendingEvent_Pendingeventaction ;
   }

   public void setgxTv_SdtGxPendingEvent_Pendingeventaction( short value )
   {
      SetDirty("Pendingeventaction");
      gxTv_SdtGxPendingEvent_Pendingeventaction = value ;
   }

   public String getgxTv_SdtGxPendingEvent_Pendingeventdata( )
   {
      return gxTv_SdtGxPendingEvent_Pendingeventdata ;
   }

   public void setgxTv_SdtGxPendingEvent_Pendingeventdata( String value )
   {
      SetDirty("Pendingeventdata");
      gxTv_SdtGxPendingEvent_Pendingeventdata = value ;
   }

   public short getgxTv_SdtGxPendingEvent_Pendingeventstatus( )
   {
      return gxTv_SdtGxPendingEvent_Pendingeventstatus ;
   }

   public void setgxTv_SdtGxPendingEvent_Pendingeventstatus( short value )
   {
      SetDirty("Pendingeventstatus");
      gxTv_SdtGxPendingEvent_Pendingeventstatus = value ;
   }

   public String getgxTv_SdtGxPendingEvent_Pendingeventerrors( )
   {
      return gxTv_SdtGxPendingEvent_Pendingeventerrors ;
   }

   public void setgxTv_SdtGxPendingEvent_Pendingeventerrors( String value )
   {
      SetDirty("Pendingeventerrors");
      gxTv_SdtGxPendingEvent_Pendingeventerrors = value ;
   }

   public String getgxTv_SdtGxPendingEvent_Pendingeventextras( )
   {
      return gxTv_SdtGxPendingEvent_Pendingeventextras ;
   }

   public void setgxTv_SdtGxPendingEvent_Pendingeventextras( String value )
   {
      SetDirty("Pendingeventextras");
      gxTv_SdtGxPendingEvent_Pendingeventextras = value ;
   }

   public String getgxTv_SdtGxPendingEvent_Pendingeventfiles( )
   {
      return gxTv_SdtGxPendingEvent_Pendingeventfiles ;
   }

   public void setgxTv_SdtGxPendingEvent_Pendingeventfiles( String value )
   {
      SetDirty("Pendingeventfiles");
      gxTv_SdtGxPendingEvent_Pendingeventfiles = value ;
   }

   public String getgxTv_SdtGxPendingEvent_Mode( )
   {
      return gxTv_SdtGxPendingEvent_Mode ;
   }

   public void setgxTv_SdtGxPendingEvent_Mode( String value )
   {
      SetDirty("Mode");
      gxTv_SdtGxPendingEvent_Mode = value ;
   }

   public void setgxTv_SdtGxPendingEvent_Mode_SetNull( )
   {
      gxTv_SdtGxPendingEvent_Mode = "" ;
   }

   public boolean getgxTv_SdtGxPendingEvent_Mode_IsNull( )
   {
      return false ;
   }

   public short getgxTv_SdtGxPendingEvent_Initialized( )
   {
      return gxTv_SdtGxPendingEvent_Initialized ;
   }

   public void setgxTv_SdtGxPendingEvent_Initialized( short value )
   {
      SetDirty("Initialized");
      gxTv_SdtGxPendingEvent_Initialized = value ;
   }

   public void setgxTv_SdtGxPendingEvent_Initialized_SetNull( )
   {
      gxTv_SdtGxPendingEvent_Initialized = (short)(0) ;
   }

   public boolean getgxTv_SdtGxPendingEvent_Initialized_IsNull( )
   {
      return false ;
   }

   public java.util.UUID getgxTv_SdtGxPendingEvent_Pendingeventid_Z( )
   {
      return gxTv_SdtGxPendingEvent_Pendingeventid_Z ;
   }

   public void setgxTv_SdtGxPendingEvent_Pendingeventid_Z( java.util.UUID value )
   {
      SetDirty("Pendingeventid_Z");
      gxTv_SdtGxPendingEvent_Pendingeventid_Z = value ;
   }

   public void setgxTv_SdtGxPendingEvent_Pendingeventid_Z_SetNull( )
   {
      gxTv_SdtGxPendingEvent_Pendingeventid_Z = java.util.UUID.fromString("00000000-0000-0000-0000-000000000000") ;
   }

   public boolean getgxTv_SdtGxPendingEvent_Pendingeventid_Z_IsNull( )
   {
      return false ;
   }

   public java.util.Date getgxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z( )
   {
      return gxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z ;
   }

   public void setgxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z( java.util.Date value )
   {
      SetDirty("Pendingeventtimestamp_Z");
      gxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z = value ;
   }

   public void setgxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z_SetNull( )
   {
      gxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z = CommonUtil.resetTime( CommonUtil.nullDate() );
   }

   public boolean getgxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z_IsNull( )
   {
      return false ;
   }

   public String getgxTv_SdtGxPendingEvent_Pendingeventbc_Z( )
   {
      return gxTv_SdtGxPendingEvent_Pendingeventbc_Z ;
   }

   public void setgxTv_SdtGxPendingEvent_Pendingeventbc_Z( String value )
   {
      SetDirty("Pendingeventbc_Z");
      gxTv_SdtGxPendingEvent_Pendingeventbc_Z = value ;
   }

   public void setgxTv_SdtGxPendingEvent_Pendingeventbc_Z_SetNull( )
   {
      gxTv_SdtGxPendingEvent_Pendingeventbc_Z = "" ;
   }

   public boolean getgxTv_SdtGxPendingEvent_Pendingeventbc_Z_IsNull( )
   {
      return false ;
   }

   public short getgxTv_SdtGxPendingEvent_Pendingeventaction_Z( )
   {
      return gxTv_SdtGxPendingEvent_Pendingeventaction_Z ;
   }

   public void setgxTv_SdtGxPendingEvent_Pendingeventaction_Z( short value )
   {
      SetDirty("Pendingeventaction_Z");
      gxTv_SdtGxPendingEvent_Pendingeventaction_Z = value ;
   }

   public void setgxTv_SdtGxPendingEvent_Pendingeventaction_Z_SetNull( )
   {
      gxTv_SdtGxPendingEvent_Pendingeventaction_Z = (short)(0) ;
   }

   public boolean getgxTv_SdtGxPendingEvent_Pendingeventaction_Z_IsNull( )
   {
      return false ;
   }

   public short getgxTv_SdtGxPendingEvent_Pendingeventstatus_Z( )
   {
      return gxTv_SdtGxPendingEvent_Pendingeventstatus_Z ;
   }

   public void setgxTv_SdtGxPendingEvent_Pendingeventstatus_Z( short value )
   {
      SetDirty("Pendingeventstatus_Z");
      gxTv_SdtGxPendingEvent_Pendingeventstatus_Z = value ;
   }

   public void setgxTv_SdtGxPendingEvent_Pendingeventstatus_Z_SetNull( )
   {
      gxTv_SdtGxPendingEvent_Pendingeventstatus_Z = (short)(0) ;
   }

   public boolean getgxTv_SdtGxPendingEvent_Pendingeventstatus_Z_IsNull( )
   {
      return false ;
   }

   public void initialize( int remoteHandle )
   {
      initialize( ) ;
      gxpendingevent_bc obj ;
      obj = new gxpendingevent_bc( remoteHandle, (ModelContext) context) ;
      obj.initialize();
      obj.SetSDT(this, (byte)(1));
      setTransaction( obj) ;
      obj.SetMode("INS");
   }

   public void initialize( )
   {
      gxTv_SdtGxPendingEvent_Pendingeventid = java.util.UUID.fromString("00000000-0000-0000-0000-000000000000") ;
      gxTv_SdtGxPendingEvent_Pendingeventtimestamp = CommonUtil.resetTime( CommonUtil.nullDate() );
      gxTv_SdtGxPendingEvent_Pendingeventbc = "" ;
      gxTv_SdtGxPendingEvent_Pendingeventdata = "" ;
      gxTv_SdtGxPendingEvent_Pendingeventerrors = "" ;
      gxTv_SdtGxPendingEvent_Pendingeventextras = "" ;
      gxTv_SdtGxPendingEvent_Pendingeventfiles = "" ;
      gxTv_SdtGxPendingEvent_Mode = "" ;
      gxTv_SdtGxPendingEvent_Pendingeventid_Z = java.util.UUID.fromString("00000000-0000-0000-0000-000000000000") ;
      gxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z = CommonUtil.resetTime( CommonUtil.nullDate() );
      gxTv_SdtGxPendingEvent_Pendingeventbc_Z = "" ;
      sTagName = "" ;
      sDateCnv = "" ;
      sNumToPad = "" ;
      datetime_STZ = CommonUtil.resetTime( CommonUtil.nullDate() );
   }

   public SdtGxPendingEvent Clone( )
   {
      SdtGxPendingEvent sdt ;
      gxpendingevent_bc obj ;
      sdt = (SdtGxPendingEvent)(clone()) ;
      obj = (gxpendingevent_bc)(sdt.getTransaction()) ;
      obj.SetSDT(sdt, (byte)(0));
      return sdt ;
   }

   protected short gxTv_SdtGxPendingEvent_Pendingeventaction ;
   protected short gxTv_SdtGxPendingEvent_Pendingeventstatus ;
   protected short gxTv_SdtGxPendingEvent_Initialized ;
   protected short gxTv_SdtGxPendingEvent_Pendingeventaction_Z ;
   protected short gxTv_SdtGxPendingEvent_Pendingeventstatus_Z ;
   protected short readOk ;
   protected short nOutParmCount ;
   protected String gxTv_SdtGxPendingEvent_Mode ;
   protected String sTagName ;
   protected String sDateCnv ;
   protected String sNumToPad ;
   protected java.util.Date gxTv_SdtGxPendingEvent_Pendingeventtimestamp ;
   protected java.util.Date gxTv_SdtGxPendingEvent_Pendingeventtimestamp_Z ;
   protected java.util.Date datetime_STZ ;
   protected String gxTv_SdtGxPendingEvent_Pendingeventdata ;
   protected String gxTv_SdtGxPendingEvent_Pendingeventerrors ;
   protected String gxTv_SdtGxPendingEvent_Pendingeventextras ;
   protected String gxTv_SdtGxPendingEvent_Pendingeventfiles ;
   protected String gxTv_SdtGxPendingEvent_Pendingeventbc ;
   protected String gxTv_SdtGxPendingEvent_Pendingeventbc_Z ;
   protected java.util.UUID gxTv_SdtGxPendingEvent_Pendingeventid ;
   protected java.util.UUID gxTv_SdtGxPendingEvent_Pendingeventid_Z ;
}

