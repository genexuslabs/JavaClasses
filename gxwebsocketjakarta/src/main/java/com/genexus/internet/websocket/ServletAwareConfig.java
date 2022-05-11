package com.genexus.internet.websocket;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.*;

public class ServletAwareConfig extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
    	
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        config.getUserProperties().put("httpSession", httpSession);
    }

}