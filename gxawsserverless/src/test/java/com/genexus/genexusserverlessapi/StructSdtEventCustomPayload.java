package com.genexus.genexusserverlessapi;
import com.genexus.*;

@javax.xml.bind.annotation.XmlType(name = "EventCustomPayload", namespace ="ServerlessAPI")
public final  class StructSdtEventCustomPayload implements Cloneable, java.io.Serializable
{
   public StructSdtEventCustomPayload( )
   {
      this( -1, new ModelContext( StructSdtEventCustomPayload.class ));
   }

   public StructSdtEventCustomPayload( int remoteHandle ,
                                       ModelContext context )
   {
   }

   public  StructSdtEventCustomPayload( java.util.Vector<StructSdtEventCustomPayload_CustomPayloadItem> value )
   {
      item = value;
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

   @javax.xml.bind.annotation.XmlElement(name="CustomPayloadItem",namespace="ServerlessAPI")
   public java.util.Vector<StructSdtEventCustomPayload_CustomPayloadItem> getItem( )
   {
      return item;
   }

   public void setItem( java.util.Vector<StructSdtEventCustomPayload_CustomPayloadItem> value )
   {
      item = value;
   }

   protected  java.util.Vector<StructSdtEventCustomPayload_CustomPayloadItem> item = new java.util.Vector<>();
}

