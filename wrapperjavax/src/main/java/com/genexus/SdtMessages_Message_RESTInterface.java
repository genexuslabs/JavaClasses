package com.genexus;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@XmlType(name =  "Messages.Message" , namespace = "Genexus" , propOrder={ "id", "type", "description" })
@JsonPropertyOrder(alphabetic=true)
@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.NONE, getterVisibility=JsonAutoDetect.Visibility.NONE, isGetterVisibility=JsonAutoDetect.Visibility.NONE)
public final  class SdtMessages_Message_RESTInterface extends GxGenericCollectionItem<com.genexus.SdtMessages_Message>
{
   public SdtMessages_Message_RESTInterface( )
   {
      super(new com.genexus.SdtMessages_Message ());
   }

   public SdtMessages_Message_RESTInterface( com.genexus.SdtMessages_Message psdt )
   {
      super(psdt);
   }

   @XmlElement(name="Id")
   @JsonProperty("Id")
   public String getgxTv_SdtMessages_Message_Id( )
   {
      return CommonUtil.rtrim(((com.genexus.SdtMessages_Message)getSdt()).getgxTv_SdtMessages_Message_Id()) ;
   }

   @JsonProperty("Id")
   public void setgxTv_SdtMessages_Message_Id(  String Value )
   {
      ((com.genexus.SdtMessages_Message)getSdt()).setgxTv_SdtMessages_Message_Id(Value);
   }


   @XmlElement(name="Type")
   @JsonProperty("Type")
   public byte getgxTv_SdtMessages_Message_Type( )
   {
      return ((com.genexus.SdtMessages_Message)getSdt()).getgxTv_SdtMessages_Message_Type() ;
   }

   @JsonProperty("Type")
   public void setgxTv_SdtMessages_Message_Type(  byte Value )
   {
      ((com.genexus.SdtMessages_Message)getSdt()).setgxTv_SdtMessages_Message_Type(Value);
   }


   @XmlElement(name="Description")
   @JsonProperty("Description")
   public String getgxTv_SdtMessages_Message_Description( )
   {
      return CommonUtil.rtrim(((com.genexus.SdtMessages_Message)getSdt()).getgxTv_SdtMessages_Message_Description()) ;
   }

   @JsonProperty("Description")
   public void setgxTv_SdtMessages_Message_Description(  String Value )
   {
      ((com.genexus.SdtMessages_Message)getSdt()).setgxTv_SdtMessages_Message_Description(Value);
   }


   int remoteHandle = -1;
}

