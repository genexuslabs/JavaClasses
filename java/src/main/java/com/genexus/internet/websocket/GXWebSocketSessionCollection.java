package com.genexus.internet.websocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GXWebSocketSessionCollection {
	private ConcurrentHashMap<String, List<GXWebSocketSession>> clients = new ConcurrentHashMap<String, List<GXWebSocketSession>>();	
	
	public void put(GXWebSocketSession ws){
		String key = ws.getId();
		if (!clients.containsKey(key)){
			clients.put(key, Collections.synchronizedList(new ArrayList<GXWebSocketSession>()) );		
		}
		clients.get(key).remove(ws);
		clients.get(key).add(ws);
		//System.out.println("New WebSocket " + key + " - " + clients.get(key).size()  );
	}
	
	public boolean remove(GXWebSocketSession ws){
		String key = ws.getId();
		Boolean removed = false;
		if (clients.containsKey(key)){
			removed = clients.get(key).remove(ws);
			if (removed && clients.get(key).size() == 0)
				clients.remove(key);
		}
		//System.out.println("Removed websocket" + key + " - " + clients.get(key).size()  );
		return removed;
	}
	
	public List<GXWebSocketSession> getById(String key){
		//System.out.println("getById websocket" + key + " - " + clients.get(key).size()  );
		return clients.get(key);
	}
	
	public List<GXWebSocketSession> getAll(){
		List<GXWebSocketSession> allWS = new ArrayList<GXWebSocketSession>();
		for (List<GXWebSocketSession> list : clients.values()){
			for (GXWebSocketSession s : list){
				allWS.add(s);
			}
		}
		return allWS;
	}
	
}
