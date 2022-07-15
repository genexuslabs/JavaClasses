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
public class GXWebSocket implements IGXWebSocketService {

	private GXWebSocketService wsService;

	public GXWebSocket() {
		wsService = GXWebSocketService.getService();
	}

	@OnOpen
	public void OnOpen(javax.websocket.Session session) {
		wsService.onOpen(new Session(session));
	}
		
	@OnMessage
	public void OnMessage(String txt, javax.websocket.Session session) {
		wsService.onMessage(txt, new Session(session));
	}

	@OnClose
	public void myOnClose(javax.websocket.Session session, CloseReason reason) {
		wsService.onClose(new Session(session));
	}
		
	@OnError
    public void onError(Throwable exception, javax.websocket.Session session) {
		wsService.onError(exception, new Session(session));
    }
		
	public SendResponseType send(String clientId, String message) {
		return wsService.send(clientId, message);
	}
	
	public void broadcast(String message) {
		wsService.broadcast(message);
	}
}


