package com.genexus.util;

import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;
import com.genexus.internet.*;

public class LDAPClient {
	private static final boolean DEBUG = com.genexus.DebugFlag.DEBUG;
  String ldapHost;
  int port;
  String authentication;
  String user;
  String password;
  byte secure;
  DirContext ctx;

  public LDAPClient() {
    port = 389;
    authentication = "simple";
    user = "";
    password = "";
  }

  public void setHost(String host)
  {
    ldapHost = host;
  }

  public void setPort(int port)
  {
    this.port = port;
  }

  public void setAuthenticationMethod(String authentication)
  {
    this.authentication = authentication;
  }

  public void setUser(String user)
  {
    this.user = user;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }
  
  public void setSecure(byte secure)
  {
    this.secure = secure;
  }  

  public byte connect()
  {
    String host;
    Hashtable env = new Hashtable(5);

    if (ldapHost.equals(""))
    {
      return 0;
    }
    else
    {
      env.put(Context.INITIAL_CONTEXT_FACTORY,
              "com.sun.jndi.ldap.LdapCtxFactory");
      host = "ldap://" + ldapHost + ":" + port;
      env.put(Context.PROVIDER_URL, host);

      env.put(Context.SECURITY_AUTHENTICATION, authentication);
      env.put(Context.SECURITY_PRINCIPAL, user);
      env.put(Context.SECURITY_CREDENTIALS, password);
	  if (secure == 1)
	  {
		  env.put(Context.SECURITY_PROTOCOL, "ssl");
	  }

      try {
        // Create initial context
        ctx = new InitialDirContext(env);
      }
      catch (NamingException e) {
      	if (DEBUG)
      	{
        	System.err.println(e);
        }
        return 0;
      }

      return 1;
    }
  }

  public void disconnect()
  {
    try
    {
      ctx.close();
    }
    catch (NamingException e)
    {
    	if (DEBUG)
    	{
      	System.err.println(e);
      }
    }
  }

  public Vector getAttribute(String attName, String context, GXProperties ldapAttributes)
  {
    Vector strResult = new Vector();
    Attributes matchAttrs = new BasicAttributes(true);
    String searchFilter = "";
    if (ldapAttributes.count() > 0)
    {
	   	searchFilter = "(&";
	    for(int i=0; i<ldapAttributes.count(); i++)
	    {
	    	searchFilter = searchFilter + "(" + ldapAttributes.item(i).name + "=" + ldapAttributes.item(i).value + ")";
	    }
	    searchFilter = searchFilter + ")";
	  }
    try
    {
    	SearchControls searchControls = new SearchControls();
    	searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);    	
    	NamingEnumeration resultSearch = ctx.search(context, searchFilter, searchControls);      
    	while (resultSearch.hasMoreElements())
      {
        SearchResult sr = (SearchResult) resultSearch.next();
        Attributes resultAtts = sr.getAttributes();
        Attribute resultAtt = resultAtts.get(attName);
        if (resultAtt==null)
        {
        	if (DEBUG)
        	{
	          System.err.println("Attribute " + attName + " not found");
	          NamingEnumeration validAtts = resultAtts.getIDs();
	          System.err.println("Valid attributes are:");
	          while (validAtts.hasMoreElements())
	          {
	            String validAtt = (String) validAtts.next();
	            System.err.print(" " + validAtt);
	          }
	        }
        }
        else
        {
          for (int j = 0; j < resultAtt.size(); j++) {
            strResult.addElement( (String) resultAtt.get(j));
          }
        }
      }
      return strResult;
    }
    catch(NamingException e)
    {
    	if (DEBUG)
    	{
      	System.err.println(e);
      }
      return strResult;
    }
  }
}
