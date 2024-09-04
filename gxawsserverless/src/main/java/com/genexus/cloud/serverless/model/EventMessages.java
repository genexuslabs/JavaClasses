package com.genexus.cloud.serverless.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class EventMessages {
	@JsonProperty("EventMessage")
	List<EventMessage> eventMessages = new ArrayList<>();

	public void add(EventMessage msg) {
		eventMessages.add(msg);
	}
}
