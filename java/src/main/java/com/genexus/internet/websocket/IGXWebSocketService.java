package com.genexus.internet.websocket;

public interface IGXWebSocketService {
       SendResponseType send(String clientId, String message);
       void broadcast(String message);	   	 
}

