package com.genexus.gxdynamiccall.test;
import com.genexus.*;

public final  class GXcfg
{
   public static int strcmp( String Left ,
                             String Right )
   {
      return GXutil.rtrim(Left).compareTo(GXutil.rtrim(Right));
   }

}