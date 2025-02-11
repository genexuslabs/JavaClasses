package com.genexus.genexusserverlessapi ;
import com.genexus.*;

public final  class StructSdtEventMessage implements Cloneable, java.io.Serializable
{
   public StructSdtEventMessage( )
   {
      this( -1, new ModelContext( StructSdtEventMessage.class ));
   }

   public StructSdtEventMessage( int remoteHandle ,
                                 ModelContext context )
   {
      java.util.Calendar cal = java.util.Calendar.getInstance();
      cal.set(1, 0, 1, 0, 0, 0);
      cal.set(java.util.Calendar.MILLISECOND, 0);
      gxTv_SdtEventMessage_Eventmessageid = "" ;
      gxTv_SdtEventMessage_Eventmessagedate = cal.getTime() ;
      gxTv_SdtEventMessage_Eventmessagesourcetype = "" ;
      gxTv_SdtEventMessage_Eventmessagedata = "" ;
      gxTv_SdtEventMessage_Eventmessageversion = "" ;
      gxTv_SdtEventMessage_Eventmessageid_N = (byte)(1) ;
      gxTv_SdtEventMessage_Eventmessagedate_N = (byte)(1) ;
      gxTv_SdtEventMessage_Eventmessagesourcetype_N = (byte)(1) ;
      gxTv_SdtEventMessage_Eventmessagedata_N = (byte)(1) ;
      gxTv_SdtEventMessage_Eventmessageproperties_N = (byte)(1) ;
      gxTv_SdtEventMessage_Eventmessageversion_N = (byte)(1) ;
   }

   public Object clone()
   {
      Object cloned = null;
      try
      {
         cloned = super.clone();
      }catch (CloneNotSupportedException e){ ; }
      return cloned;
   }

   public String getEventmessageid( )
   {
      return gxTv_SdtEventMessage_Eventmessageid ;
   }

   public void setEventmessageid( String value )
   {
      gxTv_SdtEventMessage_Eventmessageid_N = (byte)(0) ;
      sdtIsNull = (byte)(0) ;
      gxTv_SdtEventMessage_Eventmessageid = value ;
   }

   public java.util.Date getEventmessagedate( )
   {
      return gxTv_SdtEventMessage_Eventmessagedate ;
   }

   public void setEventmessagedate( java.util.Date value )
   {
      gxTv_SdtEventMessage_Eventmessagedate_N = (byte)(0) ;
      sdtIsNull = (byte)(0) ;
      gxTv_SdtEventMessage_Eventmessagedate = value ;
   }

   public String getEventmessagesourcetype( )
   {
      return gxTv_SdtEventMessage_Eventmessagesourcetype ;
   }

   public void setEventmessagesourcetype( String value )
   {
      gxTv_SdtEventMessage_Eventmessagesourcetype_N = (byte)(0) ;
      sdtIsNull = (byte)(0) ;
      gxTv_SdtEventMessage_Eventmessagesourcetype = value ;
   }

   public String getEventmessagedata( )
   {
      return gxTv_SdtEventMessage_Eventmessagedata ;
   }

   public void setEventmessagedata( String value )
   {
      gxTv_SdtEventMessage_Eventmessagedata_N = (byte)(0) ;
      sdtIsNull = (byte)(0) ;
      gxTv_SdtEventMessage_Eventmessagedata = value ;
   }

   public java.util.Vector<com.genexus.genexusserverlessapi.StructSdtEventMessageProperty> getEventmessageproperties( )
   {
      return gxTv_SdtEventMessage_Eventmessageproperties ;
   }

   public void setEventmessageproperties( java.util.Vector<com.genexus.genexusserverlessapi.StructSdtEventMessageProperty> value )
   {
      gxTv_SdtEventMessage_Eventmessageproperties_N = (byte)(0) ;
      sdtIsNull = (byte)(0) ;
      gxTv_SdtEventMessage_Eventmessageproperties = value ;
   }

   public String getEventmessageversion( )
   {
      return gxTv_SdtEventMessage_Eventmessageversion ;
   }

   public void setEventmessageversion( String value )
   {
      gxTv_SdtEventMessage_Eventmessageversion_N = (byte)(0) ;
      sdtIsNull = (byte)(0) ;
      gxTv_SdtEventMessage_Eventmessageversion = value ;
   }

   protected byte gxTv_SdtEventMessage_Eventmessageid_N ;
   protected byte gxTv_SdtEventMessage_Eventmessagedate_N ;
   protected byte gxTv_SdtEventMessage_Eventmessagesourcetype_N ;
   protected byte gxTv_SdtEventMessage_Eventmessagedata_N ;
   protected byte gxTv_SdtEventMessage_Eventmessageproperties_N ;
   protected byte gxTv_SdtEventMessage_Eventmessageversion_N ;
   protected byte sdtIsNull ;
   protected String gxTv_SdtEventMessage_Eventmessagedata ;
   protected String gxTv_SdtEventMessage_Eventmessageid ;
   protected String gxTv_SdtEventMessage_Eventmessagesourcetype ;
   protected String gxTv_SdtEventMessage_Eventmessageversion ;
   protected java.util.Date gxTv_SdtEventMessage_Eventmessagedate ;
   protected java.util.Vector<com.genexus.genexusserverlessapi.StructSdtEventMessageProperty> gxTv_SdtEventMessage_Eventmessageproperties=null ;
}

