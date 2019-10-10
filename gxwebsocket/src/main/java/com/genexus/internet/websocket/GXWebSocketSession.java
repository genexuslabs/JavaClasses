package com.genexus.internet.websocket;

import javax.websocket.Session;


public class GXWebSocketSession{
	
	private String id;
	private Session session;
	
	public String getId(){
		return id;
	}
	public GXWebSocketSession(Session session){			
		id = session.getQueryString();
		this.session = session;
	}
	public Session getSession()
	{
		return session;
	}
	public boolean equals(Object obj){
		return ((GXWebSocketSession)obj).session.equals(this.session);		
	}
}
