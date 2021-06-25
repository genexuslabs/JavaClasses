package com.genexus.ws.rs.core;

import java.net.URI;

public class UriInfo implements IUriInfo{
	private jakarta.ws.rs.core.UriInfo ui;

	public UriInfo(jakarta.ws.rs.core.UriInfo ui) {
		this.ui = ui;
	}

	public URI getAbsolutePath() {
		return ui.getAbsolutePath();
	}

	public URI getRequestUri() {
		return ui.getRequestUri();
	}
}
