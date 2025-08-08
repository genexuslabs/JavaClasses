package com.genexus.genexusserverlessapi ;
import com.genexus.*;

public final  class StructSdtEventMessageProperty implements Cloneable, java.io.Serializable
{
   public StructSdtEventMessageProperty( )
   {
      this( -1, new ModelContext( StructSdtEventMessageProperty.class ));
   }

   public StructSdtEventMessageProperty( int remoteHandle ,
                                         ModelContext context )
   {
      gxTv_SdtEventMessageProperty_Propertyid = "" ;
      gxTv_SdtEventMessageProperty_Propertyvalue = "" ;
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

   public String getPropertyid( )
   {
      return gxTv_SdtEventMessageProperty_Propertyid ;
   }

   public void setPropertyid( String value )
   {
      sdtIsNull = (byte)(0) ;
      gxTv_SdtEventMessageProperty_Propertyid = value ;
   }

   public String getPropertyvalue( )
   {
      return gxTv_SdtEventMessageProperty_Propertyvalue ;
   }

   public void setPropertyvalue( String value )
   {
      sdtIsNull = (byte)(0) ;
      gxTv_SdtEventMessageProperty_Propertyvalue = value ;
   }

   protected byte sdtIsNull ;
   protected String gxTv_SdtEventMessageProperty_Propertyid ;
   protected String gxTv_SdtEventMessageProperty_Propertyvalue ;
}

