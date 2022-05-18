package com.genexus.genexusserverlessapi;
import com.genexus.*;

public final  class StructSdtEventMessageResponse implements Cloneable, java.io.Serializable
{
   public StructSdtEventMessageResponse( )
   {
      this( -1, new ModelContext( StructSdtEventMessageResponse.class ));
   }

   public StructSdtEventMessageResponse( int remoteHandle ,
                                         ModelContext context )
   {
      gxTv_SdtEventMessageResponse_Errormessage = "" ;
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

   public boolean getHandled( )
   {
      return gxTv_SdtEventMessageResponse_Handled ;
   }

   public void setHandled( boolean value )
   {
      gxTv_SdtEventMessageResponse_N = (byte)(0) ;
      gxTv_SdtEventMessageResponse_Handled = value ;
   }

   public String getErrormessage( )
   {
      return gxTv_SdtEventMessageResponse_Errormessage ;
   }

   public void setErrormessage( String value )
   {
      gxTv_SdtEventMessageResponse_N = (byte)(0) ;
      gxTv_SdtEventMessageResponse_Errormessage = value ;
   }

   protected byte gxTv_SdtEventMessageResponse_N ;
   protected boolean gxTv_SdtEventMessageResponse_Handled ;
   protected String gxTv_SdtEventMessageResponse_Errormessage ;
}

