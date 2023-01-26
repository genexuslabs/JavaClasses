/*
               File: genexus.common.StructSdtGridState_InputValuesItem
        Description: GridState
             Author: GeneXus Java Generator version 17_0_0-144773
       Generated on: September 16, 2020 14:15:57.80
       Program type: Callable routine
          Main DBMS: Oracle
*/
package com.genexuscore.genexus.common ;
import com.genexus.*;

public final  class StructSdtGridState_InputValuesItem implements Cloneable, java.io.Serializable
{
   public StructSdtGridState_InputValuesItem( )
   {
      gxTv_SdtGridState_InputValuesItem_Name = "" ;
      gxTv_SdtGridState_InputValuesItem_Value = "" ;
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

   public String getName( )
   {
      return gxTv_SdtGridState_InputValuesItem_Name ;
   }

   public void setName( String value )
   {
      gxTv_SdtGridState_InputValuesItem_Name = value ;
   }

   public String getValue( )
   {
      return gxTv_SdtGridState_InputValuesItem_Value ;
   }

   public void setValue( String value )
   {
      gxTv_SdtGridState_InputValuesItem_Value = value ;
   }

   protected String gxTv_SdtGridState_InputValuesItem_Name ;
   protected String gxTv_SdtGridState_InputValuesItem_Value ;
}

