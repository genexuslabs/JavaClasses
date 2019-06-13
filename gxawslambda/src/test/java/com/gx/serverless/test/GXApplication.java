/*
               File: GXApplication
        Description: No description for object
             Author: GeneXus Java Generator version 15_0_11-123400
       Generated on: July 19, 2018 16:10:34.62
       Program type: Callable routine
          Main DBMS: SQL Server
*/
package com.gx.serverless.test;
import com.gx.serverless.*;
import java.util.*;
import javax.ws.rs.core.Application;
import com.genexus.util.GXServices;
import com.genexus.webpanels.WebUtils;

public final  class GXApplication extends Application
{
   public Set<Class<?>> getClasses( )
   {
      Set<Class<?>> rrcs = new HashSet<Class<?>>();
      WebUtils.getGXApplicationClasses( getClass(), rrcs);
      WebUtils.AddExternalServices( getClass(), rrcs);
      return rrcs ;
   }

}

