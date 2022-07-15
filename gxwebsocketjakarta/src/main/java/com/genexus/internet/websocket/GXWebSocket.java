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
public class GXWebSocket implements IGXWebSocketService {

	private GXWebSocketService wsService;

	public GXWebSocket() {
		wsService = GXWebSocketService.getService();
	}

	@OnOpen
	public void OnOpen(jakarta.websocket.Session session) {
		wsService.onOpen(new Session(session));
	}

	@OnMessage
	public void OnMessage(String txt, jakarta.websocket.Session session) {
		wsService.onMessage(txt, new Session(session));
	}

	@OnClose
	public void myOnClose(jakarta.websocket.Session session, CloseReason reason) {
		wsService.onClose(new Session(session));
	}

	@OnError
	public void onError(Throwable exception, jakarta.websocket.Session session) {
		wsService.onError(exception, new Session(session));
	}

	public SendResponseType send(String clientId, String message) {
		return wsService.send(clientId, message);
	}

	public void broadcast(String message) {
		wsService.broadcast(message);
	}
}


