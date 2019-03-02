package com.genexus.db;

import com.genexus.common.classes.AbstractGXConnection;


public class ConnectionInformation
{
	AbstractGXConnection roConnection;
	public AbstractGXConnection rwConnection;
	String 		 user;
	String 		 password;

	ConnectionInformation()
	{
	}

	ConnectionInformation(AbstractGXConnection rwConnection, AbstractGXConnection roConnection, String user, String password)
	{
		this.rwConnection = rwConnection;
		this.roConnection = roConnection;
		this.user 	  = user;
		this.password 	  = password;
	}
}
