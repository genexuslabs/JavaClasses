package com.genexus.genexusserverlessapi ;
import com.genexus.*;

public final  class StructSdtEventMessagesList implements Cloneable, java.io.Serializable
{
   public StructSdtEventMessagesList( )
   {
      this( -1, new ModelContext( StructSdtEventMessagesList.class ));
   }

   public StructSdtEventMessagesList( int remoteHandle ,
                                      ModelContext context )
   {
      gxTv_SdtEventMessagesList_Items_N = (byte)(1) ;
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

   public java.util.Vector getItems( )
   {
      return gxTv_SdtEventMessagesList_Items ;
   }

   public void setItems( java.util.Vector value )
   {
      gxTv_SdtEventMessagesList_Items_N = (byte)(0) ;
      sdtIsNull = (byte)(0) ;
      gxTv_SdtEventMessagesList_Items = value ;
   }

   protected byte gxTv_SdtEventMessagesList_Items_N ;
   protected byte sdtIsNull ;
   protected java.util.Vector gxTv_SdtEventMessagesList_Items=null ;
}

