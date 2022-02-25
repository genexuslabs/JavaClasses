package com.genexus.internet.websocket;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import json.org.json.JSONObject;

import com.genexus.Application;
import com.genexus.GXutil;
import com.genexus.ModelContext;
import com.genexus.db.DynamicExecute;
import com.genexus.util.GXService;
import com.genexus.util.GXServices;
import com.genexus.xml.GXXMLSerializable;
import com.genexus.websocket.ISession;

public class GXWebSocketCommon {

	private static String[] handlerCache = new String[HandlerType.values().length];
	
	private static GXWebSocketSessionCollection wsClients = new GXWebSocketSessionCollection();

	private ConcurrentHashMap<Integer, GXWebSocketSession> sessions = new ConcurrentHashMap<Integer, GXWebSocketSession>();

	public enum HandlerType {
	    ReceivedMessage, OnOpen, OnClose, OnError
	}
	
	public void closedSession(GXWebSocketSession session)
	{				
		wsClients.remove(session);
	}

	protected void OnOpenCommon (ISession session) {
		GXWebSocketSession client = getGXWebSocketSession(session);
		wsClients.put(client);
	
		Object[] parms = new Object[1];
		parms[0] = client.getId();		
		ExecuteHandler(HandlerType.OnOpen, parms);
	}

	protected void OnMessageCommon (String txt, ISession session) {
		Object[] parms = new Object[2];
		parms[0] = getGXWebSocketSession(session).getId();
		
		try {
			
			GXXMLSerializable nInfo = (GXXMLSerializable) Class.forName("com.genexuscore.genexus.server.SdtNotificationInfo").getConstructor().newInstance();			
			JSONObject jInfo = new JSONObject();
			jInfo.put("Message", txt);
			nInfo.FromJSONObject(jInfo);
			parms[1] = nInfo;
			ExecuteHandler(HandlerType.ReceivedMessage, parms);
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();		
		} catch (Exception e) {			
			e.printStackTrace();
		}			
	} 
	
	private void ExecuteHandler(HandlerType type, Object[] parameters){		
		String handler = getHandlerClassName(type);				
		if (handler != null){						
			try {				
				if (!DynamicExecute.dynamicExecute(ModelContext.getModelContext(GXutil.class), -1, Application.class, handler, parameters)){
					System.out.println("GXWebSocket - Handler could not be executed: " + handler);				
				}	
			}
			catch (Exception e){
				System.out.println("GXWebSocket - Handler failed executing action: " + handler);		
				e.printStackTrace();				
			}
		}	
	}
	
	private String getPtyTypeName(HandlerType type){
		String typeName = "";
		switch(type)
		{
			case ReceivedMessage:
				typeName = "WEBNOTIFICATIONS_RECEIVED_HANDLER";
				break;
			case OnClose:
				typeName = "WEBNOTIFICATIONS_ONCLOSE_HANDLER";
				break;
			case OnError:
				typeName = "WEBNOTIFICATIONS_ONERROR_HANDLER";
				break;
			case OnOpen:
				typeName = "WEBNOTIFICATIONS_ONOPEN_HANDLER";
				break;
		}
		return typeName;
	}
		
	private String getHandlerClassName(HandlerType hType){		
		int idx = hType.ordinal();
		String handlerClassName = handlerCache[idx];		
		if (handlerClassName == null){	
			String type = getPtyTypeName(hType);			
			GXService service = GXServices.getInstance().get(GXServices.WEBNOTIFICATIONS_SERVICE);											
			if (service != null && service.getProperties() != null){			
				String className = service.getProperties().get(type);
				if (className != null && className.length() > 0){
					handlerClassName = GXutil.getClassName(className.toLowerCase());
					handlerCache[idx] = handlerClassName;
				}
			}			
		}		
		return handlerClassName;
	}

	protected void myOnCloseCommon (ISession session) {
		GXWebSocketSession client = getGXWebSocketSession(session);
		closedSession(client);
		Object[] parms = new Object[1];
		parms[0] = client.getId();	
		ExecuteHandler(HandlerType.OnClose, parms);
		sessions.remove(client.getSession().getHashCode());
	}

	protected void onErrorCommon(Throwable exception, ISession session) {
		Object[] parms = new Object[2];
		parms[0] = getGXWebSocketSession(session).getId();
		parms[1] = exception.getMessage();
		ExecuteHandler(HandlerType.OnError, parms);
    }

	protected SendResponseType sendCommon(String clientId, String message) {
		SendResponseType result = SendResponseType.SessionNotFound;
		List<GXWebSocketSession> list = wsClients.getById(clientId);
		if (list != null){
			for (GXWebSocketSession session : list){
				result = sendMessage( session,message);
			}
		}		
		return result;
	}

	private SendResponseType sendMessage( GXWebSocketSession session, String message) {
		SendResponseType result = SendResponseType.SessionInvalid;
		if (session != null ){
			if (session.getSession().isOpen()){
				try {
					session.getSession().sendEndPointText(message);
					result = SendResponseType.OK;					
				} catch (IOException e) {	
					result = SendResponseType.SendFailed;
					System.out.println("GXWebSocket - sendMessage failed. " + e.getMessage());
				}
			}
			else
			{				
				System.out.println("GXWebSocket - sendMessage failed Session Was Closed");
			}
		}
		else
		{
			result =  SendResponseType.SessionNotFound;
			System.out.println("GXWebSocketServlet - sendMessage failed SessionNotFound");
		}
		return result;
	}

	
	protected void broadcastCommon(String message) {
		for ( GXWebSocketSession session : wsClients.getAll() ) {		    		   
		    sendMessage(session,message);
		}	
	}

	public boolean start() {
		return true;
	}

	private GXWebSocketSession getGXWebSocketSession(ISession session) {
		if (sessions.containsKey(session.getHashCode()))
			return sessions.get(session.getHashCode());
		GXWebSocketSession socketSession = new GXWebSocketSession(session);
		sessions.put(socketSession.getSession().getHashCode(), socketSession);
		return socketSession;
	}
}


