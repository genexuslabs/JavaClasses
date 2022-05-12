package com.genexus.genexusserverlessapi;
import com.genexus.*;

public final  class StructSdtEventMessages implements Cloneable, java.io.Serializable
{
   public StructSdtEventMessages( )
   {
      this( -1, new ModelContext( StructSdtEventMessages.class ));
   }

   public StructSdtEventMessages( int remoteHandle ,
                                  ModelContext context )
   {
      gxTv_SdtEventMessages_Eventmessage_N = (byte)(1) ;
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

   public java.util.Vector<com.genexus.genexusserverlessapi.StructSdtEventMessage> getEventmessage( )
   {
      return gxTv_SdtEventMessages_Eventmessage ;
   }

   public void setEventmessage( java.util.Vector<com.genexus.genexusserverlessapi.StructSdtEventMessage> value )
   {
      gxTv_SdtEventMessages_Eventmessage_N = (byte)(0) ;
      gxTv_SdtEventMessages_N = (byte)(0) ;
      gxTv_SdtEventMessages_Eventmessage = value ;
   }

   protected byte gxTv_SdtEventMessages_Eventmessage_N ;
   protected byte gxTv_SdtEventMessages_N ;
   protected java.util.Vector<com.genexus.genexusserverlessapi.StructSdtEventMessage> gxTv_SdtEventMessages_Eventmessage=null ;
}

