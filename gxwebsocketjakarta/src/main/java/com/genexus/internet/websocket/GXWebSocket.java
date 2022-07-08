package com.genexus.internet.websocket;

import com.genexus.Application;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.server.ServerEndpoint;

import com.genexus.websocket.Session;

@ServerEndpoint(value = "/gxwebsocket")
public class GXWebSocket extends GXWebSocketCommon implements IGXWebSocketService {

	public GXWebSocket(){
		Application.registerSocketService(this);
	}

	@OnOpen
	public void OnOpen (jakarta.websocket.Session session) {
		OnOpenCommon(new Session(session));
	}
		
	@OnMessage
	public void OnMessage (String txt, jakarta.websocket.Session session) {
		OnMessageCommon(txt, new Session(session));
	}

	@OnClose
	public void myOnClose (jakarta.websocket.Session session, CloseReason reason) {
		myOnCloseCommon(new Session(session));
	}
		
	@OnError
    public void onError(Throwable exception, jakarta.websocket.Session session) {
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


