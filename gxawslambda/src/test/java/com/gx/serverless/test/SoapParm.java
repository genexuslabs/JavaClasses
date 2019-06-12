/*
               File: SoapParm
        Description: No description for object
             Author: GeneXus Java Generator version 15_0_11-123400
       Generated on: July 19, 2018 16:10:34.62
       Program type: Callable routine
          Main DBMS: SQL Server
*/
package com.gx.serverless.test;
import com.gx.serverless.*;
import com.genexus.*;

public final  class SoapParm
{
   public static String read_section( ModelContext context ,
                                      com.genexus.xml.XMLReader oReader ,
                                      com.genexus.internet.Location oLocation )
   {
      String sSection ;
      sSection = "" ;
      if ( oReader.getNodeType() == 1 )
      {
         sSection = oReader.getName() ;
         oReader.read();
         while ( ! ( ( GXutil.strcmp(oReader.getName(), sSection) == 0 ) && ( oReader.getNodeType() == 2 ) ) )
         {
            if ( GXutil.strcmp(GXutil.lower( oReader.getName()), "host") == 0 )
            {
               oLocation.setHost( oReader.getValue() );
            }
            else if ( GXutil.strcmp(GXutil.lower( oReader.getName()), "port") == 0 )
            {
               oLocation.setPort( (int)(GXutil.lval( oReader.getValue())) );
            }
            else if ( GXutil.strcmp(GXutil.lower( oReader.getName()), "baseurl") == 0 )
            {
               oLocation.setBaseURL( oReader.getValue() );
            }
            else if ( GXutil.strcmp(GXutil.lower( oReader.getName()), "resourcename") == 0 )
            {
               oLocation.setResourceName( oReader.getValue() );
            }
            else if ( GXutil.strcmp(GXutil.lower( oReader.getName()), "secure") == 0 )
            {
               oLocation.setSecure( (byte)(GXutil.lval( oReader.getValue())) );
            }
            else if ( GXutil.strcmp(GXutil.lower( oReader.getName()), "proxyserverhost") == 0 )
            {
               oLocation.setProxyServerHost( oReader.getValue() );
            }
            else if ( GXutil.strcmp(GXutil.lower( oReader.getName()), "proxyserverport") == 0 )
            {
               oLocation.setProxyServerPort( (int)(GXutil.lval( oReader.getValue())) );
            }
            else if ( GXutil.strcmp(GXutil.lower( oReader.getName()), "timeout") == 0 )
            {
               oLocation.setTimeout( (short)(GXutil.lval( oReader.getValue())) );
            }
            else if ( GXutil.strcmp(GXutil.lower( oReader.getName()), "cancelonerror") == 0 )
            {
               oLocation.setCancelOnError( (short)(GXutil.lval( oReader.getValue())) );
            }
            else if ( GXutil.strcmp(oReader.getName(), "Authentication") == 0 )
            {
               oLocation.setAuthentication( (byte)(1) );
               oLocation.setAuthenticationMethod( (byte)(GXutil.lval( GXutil.ltrim( GXutil.str( 0, 9, 0)))) );
               oReader.read();
               while ( ! ( ( GXutil.strcmp(oReader.getName(), "Authentication") == 0 ) && ( oReader.getNodeType() == 2 ) ) )
               {
                  if ( GXutil.strcmp(GXutil.lower( oReader.getName()), "authenticationmethod") == 0 )
                  {
                     oLocation.setAuthenticationMethod( (byte)(GXutil.lval( oReader.getValue())) );
                  }
                  else if ( GXutil.strcmp(GXutil.lower( oReader.getName()), "authenticationuser") == 0 )
                  {
                     oLocation.setAuthenticationUser( oReader.getValue() );
                  }
                  else if ( GXutil.strcmp(GXutil.lower( oReader.getName()), "authenticationrealm") == 0 )
                  {
                     oLocation.setAuthenticationRealm( oReader.getValue() );
                  }
                  else if ( GXutil.strcmp(GXutil.lower( oReader.getName()), "authenticationpassword") == 0 )
                  {
                     oLocation.setAuthenticationPassword( oReader.getValue() );
                  }
                  oReader.read();
               }
            }
            else if ( GXutil.strcmp(oReader.getName(), "Proxyauthentication") == 0 )
            {
               oLocation.setProxyAuthenticationMethod( (byte)(GXutil.lval( GXutil.ltrim( GXutil.str( 0, 9, 0)))) );
               oReader.read();
               while ( ! ( ( GXutil.strcmp(oReader.getName(), "Proxyauthentication") == 0 ) && ( oReader.getNodeType() == 2 ) ) )
               {
                  if ( GXutil.strcmp(GXutil.lower( oReader.getName()), "proxyauthenticationmethod") == 0 )
                  {
                     oLocation.setProxyAuthenticationMethod( (byte)(GXutil.lval( oReader.getValue())) );
                  }
                  else if ( GXutil.strcmp(GXutil.lower( oReader.getName()), "proxyauthenticationuser") == 0 )
                  {
                     oLocation.setProxyAuthenticationUser( oReader.getValue() );
                  }
                  else if ( GXutil.strcmp(GXutil.lower( oReader.getName()), "proxyauthenticationrealm") == 0 )
                  {
                     oLocation.setProxyAuthenticationRealm( oReader.getValue() );
                  }
                  else if ( GXutil.strcmp(GXutil.lower( oReader.getName()), "proxyauthenticationpassword") == 0 )
                  {
                     oLocation.setProxyAuthenticationPassword( oReader.getValue() );
                  }
                  oReader.read();
               }
            }
            oReader.read();
         }
      }
      return sSection ;
   }

   public static void initlocations( ModelContext context ,
                                     com.genexus.xml.XMLReader oReader )
   {
      String sSection ;
      String sName ;
      byte nFirstRead ;
      com.genexus.internet.Location oLocation ;
      context.globals.nLocRead = (byte)(1) ;
      context.globals.colLocations = new com.genexus.internet.LocationCollection();
      if ( oReader.getErrCode() == 0 )
      {
         if ( oReader.readType((short)(1), "GXLocations") > 0 )
         {
            oReader.read();
            while ( ! ( ( GXutil.strcmp(oReader.getName(), "GXLocations") == 0 ) && ( oReader.getNodeType() == 2 ) ) )
            {
               if ( ( GXutil.strcmp(oReader.getName(), "GXLocation") == 0 ) && ( oReader.getNodeType() == 1 ) )
               {
                  sName = oReader.getAttributeByName("name") ;
                  oLocation = context.globals.colLocations.item(sName) ;
                  context.globals.nSOAPErr = (short)(0) ;
                  if ( oLocation == null )
                  {
                     context.globals.nSOAPErr = (short)(1) ;
                  }
                  if ( context.globals.nSOAPErr != 0 )
                  {
                     /* Error while reading XML. Code:  4 . Message:  Input error . */
                     oLocation = new com.genexus.internet.Location();
                     oLocation.setName( sName );
                     oLocation.setHost( "localhost" );
                     oLocation.setPort( 80 );
                     oLocation.setBaseURL( "/" );
                     oLocation.setSecure( (byte)(0) );
                     oLocation.setProxyServerHost( "" );
                     oLocation.setProxyServerPort( 0 );
                     oLocation.setTimeout( (short)(0) );
                     oLocation.setCancelOnError( (short)(0) );
                     oLocation.setAuthentication( (byte)(0) );
                     oLocation.setAuthenticationMethod( (byte)(0) );
                     oLocation.setAuthenticationRealm( "" );
                     oLocation.setAuthenticationUser( "" );
                     oLocation.setAuthenticationPassword( "" );
                     oLocation.setGroupLocation( "" );
                     context.globals.colLocations.add(oLocation, sName);
                  }
                  oLocation.setGroupLocation( "" );
                  nFirstRead = (byte)(1) ;
                  oReader.read();
                  while ( ! ( ( GXutil.strcmp(oReader.getName(), "GXLocation") == 0 ) && ( oReader.getNodeType() == 2 ) ) )
                  {
                     sSection = read_section( context, oReader, oLocation) ;
                     if ( ( ( GXutil.strcmp(sSection, "Common") == 0 ) && ( nFirstRead == 1 ) ) || ( GXutil.strcmp(sSection, "HTTP") == 0 ) )
                     {
                        nFirstRead = (byte)(0) ;
                     }
                     oReader.read();
                  }
               }
               oReader.read();
            }
         }
      }
   }

   public static void assigngroupproperties( ModelContext context ,
                                             com.genexus.internet.Location oLocation )
   {
      String sLocation ;
      com.genexus.internet.Location oGroupLocation ;
      int nOldSOAPErr ;
      if ( oLocation != null )
      {
         sLocation = oLocation.getGroupLocation() ;
         if ( GXutil.strcmp(sLocation, "") != 0 )
         {
            nOldSOAPErr = context.globals.nSOAPErr ;
            oGroupLocation = context.globals.colLocations.item(sLocation) ;
            context.globals.nSOAPErr = (short)(0) ;
            if ( oGroupLocation == null )
            {
               context.globals.nSOAPErr = (short)(1) ;
            }
            if ( context.globals.nSOAPErr == 0 )
            {
               oLocation.setHost( oGroupLocation.getHost() );
               oLocation.setPort( oGroupLocation.getPort() );
               oLocation.setBaseURL( oGroupLocation.getBaseURL() );
               oLocation.setSecure( oGroupLocation.getSecure() );
               oLocation.setProxyServerHost( oGroupLocation.getProxyServerHost() );
               oLocation.setProxyServerPort( oGroupLocation.getProxyServerPort() );
               oLocation.setTimeout( oGroupLocation.getTimeout() );
               oLocation.setCancelOnError( oGroupLocation.getCancelOnError() );
               oLocation.setAuthentication( oGroupLocation.getAuthentication() );
               oLocation.setAuthenticationMethod( oGroupLocation.getAuthenticationMethod() );
               oLocation.setAuthenticationRealm( oGroupLocation.getAuthenticationRealm() );
               oLocation.setAuthenticationUser( oGroupLocation.getAuthenticationUser() );
               oLocation.setAuthenticationPassword( oGroupLocation.getAuthenticationPassword() );
               oLocation.setProxyAuthentication( oGroupLocation.getProxyAuthentication() );
               oLocation.setProxyAuthenticationMethod( oGroupLocation.getProxyAuthenticationMethod() );
               oLocation.setProxyAuthenticationRealm( oGroupLocation.getProxyAuthenticationRealm() );
               oLocation.setProxyAuthenticationUser( oGroupLocation.getProxyAuthenticationUser() );
               oLocation.setProxyAuthenticationPassword( oGroupLocation.getProxyAuthenticationPassword() );
            }
            context.globals.nSOAPErr = (short)(nOldSOAPErr) ;
         }
      }
   }

   public static com.genexus.internet.Location getlocation( ModelContext context ,
                                                            String sLocation )
   {
      com.genexus.xml.XMLReader oReader ;
      com.genexus.internet.Location oLocation ;
      if ( context.globals.nLocRead == 0 )
      {
         oReader = new com.genexus.xml.XMLReader();
         oReader.openResource("location.xml");
         initlocations( context, oReader) ;
         if ( oReader.getErrCode() == 0 )
         {
            oReader.close();
         }
      }
      context.globals.nSOAPErr = (short)(0) ;
      oLocation = context.globals.colLocations.item(sLocation) ;
      context.globals.nSOAPErr = (short)(0) ;
      if ( oLocation == null )
      {
         context.globals.nSOAPErr = (short)(1) ;
      }
      assigngroupproperties( context, oLocation) ;
      if ( context.globals.nSOAPErr != 0 )
      {
         context.globals.nSOAPErr = (short)(-20007) ;
         context.globals.sSOAPErrMsg = "Invalid location name." ;
         oLocation = new com.genexus.internet.Location();
         oLocation.setName( sLocation );
         oLocation.setHost( "" );
         oLocation.setPort( -1 );
         oLocation.setBaseURL( "" );
         oLocation.setSecure( (byte)(-1) );
         oLocation.setProxyServerHost( "" );
         oLocation.setProxyServerPort( -1 );
         oLocation.setTimeout( (short)(-1) );
         oLocation.setCancelOnError( (short)(0) );
         oLocation.setAuthentication( (byte)(0) );
         oLocation.setAuthenticationMethod( (byte)(0) );
         oLocation.setAuthenticationRealm( "" );
         oLocation.setAuthenticationUser( "" );
         oLocation.setAuthenticationPassword( "" );
         oLocation.setGroupLocation( "" );
         context.globals.colLocations.add(oLocation, sLocation);
      }
      else
      {
         context.globals.nSOAPErr = (short)(0) ;
         context.globals.sSOAPErrMsg = "" ;
      }
      return oLocation ;
   }

   public static void assignlocationproperties( ModelContext context ,
                                                String sLocation ,
                                                com.genexus.internet.HttpClient oClient )
   {
      com.genexus.internet.Location oLocation ;
      com.genexus.internet.Location oGroupLocation ;
      String sGroupLocation ;
      short nGroupErr ;
      oLocation = SoapParm.getlocation(context, sLocation) ;
      if ( context.globals.nSOAPErr != 0 )
      {
         sGroupLocation = "LOC:" + oClient.getHost() + oClient.getBaseURL() ;
         oGroupLocation = context.globals.colLocations.item(sGroupLocation) ;
         nGroupErr = (short)(0) ;
         if ( oGroupLocation == null )
         {
            nGroupErr = (short)(1) ;
         }
         if ( nGroupErr == 0 )
         {
            context.globals.nSOAPErr = (short)(0) ;
            oLocation.setGroupLocation( sGroupLocation );
            assigngroupproperties( context, oLocation) ;
         }
      }
      if ( context.globals.nSOAPErr == 0 )
      {
         if ( GXutil.strcmp(oLocation.getHost(), "") != 0 )
         {
            oClient.setHost( oLocation.getHost() );
         }
         if ( oLocation.getPort() != -1 )
         {
            oClient.setPort( oLocation.getPort() );
         }
         if ( GXutil.strcmp(oLocation.getProxyServerHost(), "") != 0 )
         {
            oClient.setProxyServerHost( oLocation.getProxyServerHost() );
         }
         if ( oLocation.getProxyServerPort() != -1 )
         {
            oClient.setProxyServerPort( (short)(oLocation.getProxyServerPort()) );
         }
         if ( GXutil.strcmp(oLocation.getBaseURL(), "") != 0 )
         {
            oClient.setBaseURL( oLocation.getBaseURL() );
         }
         if ( oLocation.getSecure() != -1 )
         {
            oClient.setSecure( (byte)((GXutil.boolval( GXutil.str( oLocation.getSecure(), 1, 0)) ? 1 : 0)) );
         }
         if ( oLocation.getTimeout() != -1 )
         {
            oClient.setTimeout( oLocation.getTimeout() );
         }
         if ( GXutil.strcmp(oLocation.getProxyAuthenticationUser(), "") != 0 )
         {
            oClient.addProxyAuthentication(oLocation.getProxyAuthenticationMethod(), oLocation.getProxyAuthenticationRealm(), oLocation.getProxyAuthenticationUser(), oLocation.getProxyAuthenticationPassword());
         }
         if ( GXutil.strcmp(oLocation.getCertificate(), "") != 0 )
         {
            oClient.addCertificate(oLocation.getCertificate());
         }
         if ( oLocation.getAuthentication() == 1 )
         {
            oClient.addAuthentication(oLocation.getAuthenticationMethod(), oLocation.getAuthenticationRealm(), oLocation.getAuthenticationUser(), oLocation.getAuthenticationPassword());
         }
      }
   }

   public static String getresourcename( ModelContext context ,
                                         String sLocation )
   {
      com.genexus.internet.Location oLocation ;
      oLocation = SoapParm.getlocation(context, sLocation) ;
      if ( context.globals.nSOAPErr == 0 )
      {
         sLocation = oLocation.getResourceName() ;
      }
      else
      {
         sLocation = "" ;
      }
      return sLocation ;
   }

}

