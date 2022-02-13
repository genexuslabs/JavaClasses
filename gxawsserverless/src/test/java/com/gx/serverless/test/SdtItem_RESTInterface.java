/*
               File: SdtItem_RESTInterface
        Description: Test
             Author: GeneXus Java Generator version 15_0_11-123400
       Generated on: July 19, 2018 16:59:50.50
       Program type: Callable routine
          Main DBMS: SQL Server
*/
package com.gx.serverless.test;
import com.gx.serverless.*;
import com.genexus.*;
import javax.xml.bind.annotation.*;
import com.fasterxml.jackson.annotation.*;

@XmlRootElement(name =  "Item" , namespace = "ServerlessBasicTest" )
@XmlType(propOrder={ "itemid", "itemname" })
@JsonPropertyOrder(alphabetic=true)
@JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.NONE, getterVisibility=JsonAutoDetect.Visibility.NONE, isGetterVisibility=JsonAutoDetect.Visibility.NONE)
public final  class SdtItem_RESTInterface extends GxGenericCollectionItem<SdtItem>
{
   public SdtItem_RESTInterface( )
   {
      super(new SdtItem());
   }

   public SdtItem_RESTInterface( SdtItem psdt )
   {
      super(psdt);
   }

   @XmlElement(name="ItemId")
   @JsonProperty("ItemId")
   public short getgxTv_SdtItem_Itemid( )
   {
      return ((SdtItem)getSdt()).getgxTv_SdtItem_Itemid() ;
   }

   @JsonProperty("ItemId")
   public void setgxTv_SdtItem_Itemid(  short Value )
   {
      ((SdtItem)getSdt()).setgxTv_SdtItem_Itemid(Value);
   }


   @XmlElement(name="ItemName")
   @JsonProperty("ItemName")
   public String getgxTv_SdtItem_Itemname( )
   {
      return GXutil.rtrim(((SdtItem)getSdt()).getgxTv_SdtItem_Itemname()) ;
   }

   @JsonProperty("ItemName")
   public void setgxTv_SdtItem_Itemname(  String Value )
   {
      ((SdtItem)getSdt()).setgxTv_SdtItem_Itemname(Value);
   }


   int remoteHandle = -1;
}

