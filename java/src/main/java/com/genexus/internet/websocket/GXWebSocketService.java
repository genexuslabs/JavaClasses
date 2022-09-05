package com.genexus.internet.websocket;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.genexus.db.Namespace;
import com.genexus.db.UserInformation;
import com.genexus.diagnostics.core.ILogger;
import com.genexus.diagnostics.core.LogManager;
import json.org.json.JSONException;
import json.org.json.JSONObject;

import com.genexus.Application;
import com.genexus.GXutil;
import com.genexus.ModelContext;
import com.genexus.db.DynamicExecute;
import com.genexus.util.GXService;
import com.genexus.util.GXServices;
import com.genexus.xml.GXXMLSerializable;
import com.genexus.websocket.ISession;

public class GXWebSocketService {
	public static final ILogger logger = LogManager.getLogger(GXWebSocketService.class);
	private static GXWebSocketService instance;

	private String[] handlerCache = new String[HandlerType.values().length];
	private GXWebSocketSessionCollection wsClients = new GXWebSocketSessionCollection();
	private ConcurrentHashMap<Integer, GXWebSocketSession> sessions = new ConcurrentHashMap<Integer, GXWebSocketSession>();

	public enum HandlerType {
		ReceivedMessage, OnOpen, OnClose, OnError
	}

	public static GXWebSocketService getService() {
		if (instance == null) {
			synchronized (GXWebSocketService.class) {
				if (instance == null) {
					instance = new GXWebSocketService();
				}
			}
		}
		return instance;
	}

	public void closedSession(GXWebSocketSession session) {
		wsClients.remove(session);
	}

	protected void onOpen(ISession session) {
		logger.debug(String.format("WebSocket - Connection opened '%s'", session.getId()));

		GXWebSocketSession client = getGXWebSocketSession(session);
		wsClients.put(client);

		Object[] parms = new Object[1];
		parms[0] = client.getId();
		executeHandler(HandlerType.OnOpen, parms);
	}

	protected void onMessage(String txt, ISession session) {
		logger.debug(String.format("WebSocket - New Message received '%s'", session.getId()));

		Object[] parameters = new Object[2];
		parameters[0] = getGXWebSocketSession(session).getId();

		try {

			GXXMLSerializable nInfo = (GXXMLSerializable) Class.forName("com.genexuscore.genexus.server.SdtNotificationInfo").getConstructor().newInstance();
			JSONObject jInfo = new JSONObject();
			jInfo.put("Message", txt);
			nInfo.FromJSONObject(jInfo);
			parameters[1] = nInfo;
			executeHandler(HandlerType.ReceivedMessage, parameters);

		} catch (ClassNotFoundException e) {
			logger.error("WebSocket - SdtNotificationInfo class not found", e);
		} catch (JSONException | InstantiationException | IllegalAccessException| InvocationTargetException | NoSuchMethodException e) {
			logger.error("WebSocket - General error ", e);
		}
	}

	private void executeHandler(HandlerType type, Object[] parameters) {
		String handler = getHandlerClassName(type);
		if (handler != null) {
			ModelContext modelContext = ModelContext.getModelContext(Application.gxCfg);
			UserInformation ui = Application.getConnectionManager().createUserInformation(Namespace.getNamespace(modelContext.getNAME_SPACE()));
			int remoteHandle = ui.getHandle();
			try {
				if (!DynamicExecute.dynamicExecute(modelContext, remoteHandle, Application.class, handler, parameters)) {
					logger.error(String.format("WebSocket - Handler '%s' failed to execute", handler));
				}
			} catch (Exception e) {
				logger.error(String.format("WebSocket - Handler '%s' failed to execute", handler));
			}
			finally {
				Application.cleanupConnection(remoteHandle);
			}
		}
	}

	private String getPtyTypeName(HandlerType type) {
		String typeName = "";
		switch (type) {
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

	private String getHandlerClassName(HandlerType hType) {
		int idx = hType.ordinal();
		String handlerClassName = handlerCache[idx];
		if (handlerClassName == null) {
			String type = getPtyTypeName(hType);
			GXService service = GXServices.getInstance().get(GXServices.WEBNOTIFICATIONS_SERVICE);
			if (service != null && service.getProperties() != null) {
				String className = service.getProperties().get(type);
				if (className != null && className.length() > 0) {
					handlerClassName = GXutil.getClassName(className.toLowerCase());
					handlerCache[idx] = handlerClassName;
				}
			}
		}
		return handlerClassName;
	}

	protected void onClose(ISession session) {
		logger.debug(String.format("WebSocket - Connection closed '%s'", session.getId()));

		GXWebSocketSession client = getGXWebSocketSession(session);
		closedSession(client);
		Object[] parms = new Object[1];
		parms[0] = client.getId();
		executeHandler(HandlerType.OnClose, parms);
		sessions.remove(client.getSession().getHashCode());
	}

	protected void onError(Throwable exception, ISession session) {
		logger.debug(String.format("WebSocket - Connection error '%s'", session.getId()));

		Object[] parms = new Object[2];
		parms[0] = getGXWebSocketSession(session).getId();
		parms[1] = exception.getMessage();
		executeHandler(HandlerType.OnError, parms);
	}

	public SendResponseType send(String clientId, String message) {
		logger.debug(String.format("WebSocket - Try send message to '%s'", clientId));

		SendResponseType result = SendResponseType.SessionNotFound;
		List<GXWebSocketSession> list = wsClients.getById(clientId);
		if (list != null) {
			for (GXWebSocketSession session : list) {
				result = sendMessage(session, message);
			}
		}
		return result;
	}

	private SendResponseType sendMessage(GXWebSocketSession session, String message) {
		SendResponseType result = SendResponseType.SessionInvalid;
		if (session != null) {
			if (session.getSession().isOpen()) {
				try {
					session.getSession().sendEndPointText(message);
					result = SendResponseType.OK;
				} catch (IOException e) {
					result = SendResponseType.SendFailed;
					logger.warn("WebSocket - sendMessage failed", e);
				}
			} else {
				logger.warn("WebSocket - sendMessage failed because session was invalid");
			}
		} else {
			result = SendResponseType.SessionNotFound;
		}
		return result;
	}

	public void broadcast(String message) {
		for (GXWebSocketSession session : wsClients.getAll()) {
			sendMessage(session, message);
		}
	}

	private GXWebSocketSession getGXWebSocketSession(ISession session) {
		if (sessions.containsKey(session.getHashCode())) {
			return sessions.get(session.getHashCode());
		}
		GXWebSocketSession socketSession = new GXWebSocketSession(session);
		sessions.put(socketSession.getSession().getHashCode(), socketSession);
		return socketSession;
	}
}


