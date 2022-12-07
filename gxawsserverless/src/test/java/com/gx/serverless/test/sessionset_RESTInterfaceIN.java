package com.gx.serverless.test;
import com.genexus.*;
import com.fasterxml.jackson.annotation.*;

@javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.NONE)
@javax.xml.bind.annotation.XmlType(name = "sessionset_RESTInterfaceIN", namespace ="http://tempuri.org/")
@JsonPropertyOrder(alphabetic=true)
@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.NONE, getterVisibility=JsonAutoDetect.Visibility.NONE, isGetterVisibility=JsonAutoDetect.Visibility.NONE)
public final  class sessionset_RESTInterfaceIN
{
   String AV8SessionName;
   @JsonProperty("SessionName")
   public String getSessionName( )
   {
      if ( GXutil.strcmp(AV8SessionName, null) == 0 )
      {
         return "" ;
      }
      else
      {
         return AV8SessionName ;
      }
   }

   @JsonProperty("SessionName")
   public void setSessionName(  String Value )
   {
      if ( Value == null )
      {
         AV8SessionName = "" ;
      }
      else
      {
         AV8SessionName= Value;
      }
   }


   String AV9SessionValue;
   @JsonProperty("SessionValue")
   public String getSessionValue( )
   {
      if ( GXutil.strcmp(AV9SessionValue, null) == 0 )
      {
         return "" ;
      }
      else
      {
         return AV9SessionValue ;
      }
   }

   @JsonProperty("SessionValue")
   public void setSessionValue(  String Value )
   {
      if ( Value == null )
      {
         AV9SessionValue = "" ;
      }
      else
      {
         AV9SessionValue= Value;
      }
   }


}

