package com.genexus.internet.websocket;

public interface IGXWebSocketAsync {
		boolean start();
       SendResponseType send(String clientId, String message);
       void broadcast(String message);	   	 
}

