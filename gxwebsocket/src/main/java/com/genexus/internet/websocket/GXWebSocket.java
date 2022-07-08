package com.genexus.internet.websocket;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;

import com.genexus.Application;
import com.genexus.websocket.Session;

@ServerEndpoint(value = "/gxwebsocket")
public class GXWebSocket extends GXWebSocketCommon implements IGXWebSocketService {

	public GXWebSocket() {
		Application.registerSocketService(this);
	}

	@OnOpen
	public void OnOpen (javax.websocket.Session session) {
		OnOpenCommon(new Session(session));
	}
		
	@OnMessage
	public void OnMessage (String txt, javax.websocket.Session session) {
		OnMessageCommon(txt, new Session(session));
	}

	@OnClose
	public void myOnClose (javax.websocket.Session session, CloseReason reason) {
		myOnCloseCommon(new Session(session));
	}
		
	@OnError
    public void onError(Throwable exception, javax.websocket.Session session) {
		onErrorCommon(exception, new Session(session));
    }
		
	public SendResponseType send(String clientId, String message) {
		return sendCommon(clientId, message);
	}
	
	public void broadcast(String message) {
		broadcastCommon(message);
	}

	public boolean start() {
		return true;
	}	
}


