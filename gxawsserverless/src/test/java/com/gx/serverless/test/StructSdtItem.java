/*
               File: StructSdtItem
        Description: Item
             Author: GeneXus Java Generator version 15_0_11-123400
       Generated on: July 19, 2018 16:10:34.50
       Program type: Callable routine
          Main DBMS: SQL Server
*/
package com.gx.serverless.test;
import com.genexus.*;

public final  class StructSdtItem implements Cloneable, java.io.Serializable
{
   public StructSdtItem( )
   {
      gxTv_SdtItem_Itemname = "" ;
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

   public short getItemid( )
   {
      return gxTv_SdtItem_Itemid ;
   }

   public void setItemid( short value )
   {
      gxTv_SdtItem_Itemid = value ;
   }

   public String getItemname( )
   {
      return gxTv_SdtItem_Itemname ;
   }

   public void setItemname( String value )
   {
      gxTv_SdtItem_Itemname = value ;
   }

   protected short gxTv_SdtItem_Itemid ;
   protected String gxTv_SdtItem_Itemname ;
}

