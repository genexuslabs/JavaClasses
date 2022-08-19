package com.genexus.internet.websocket;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;

import com.genexus.websocket.Session;

@ServerEndpoint(value = "/gxwebsocket")
public class GXWebSocket {

	private GXWebSocketService wsService;

	public GXWebSocket() {
		wsService = GXWebSocketService.getService();
	}

	@OnOpen
	public void onOpen(javax.websocket.Session session) {
		wsService.onOpen(new Session(session));
	}
		
	@OnMessage
	public void onMessage(String txt, javax.websocket.Session session) {
		wsService.onMessage(txt, new Session(session));
	}

	@OnClose
	public void onClose(javax.websocket.Session session, CloseReason reason) {
		wsService.onClose(new Session(session));
	}
		
	@OnError
    public void onError(Throwable exception, javax.websocket.Session session) {
		wsService.onError(exception, new Session(session));
    }

}


