package com.genexus.ws.rs.core;

import java.net.URI;

public interface IUriInfo {
	URI getAbsolutePath();
	URI getRequestUri();
}
