package com.genexus.opentelemetry;

import io.opentelemetry.context.Context;

/**
 * Wrapper class for OpenTelemetry Context.
 * Provides access to trace context for propagation between processes.
 */
public class GXTraceContext {
	private final Context context;
	
	/**
	 * Creates a GXTraceContext from an existing OpenTelemetry Context
	 * 
	 * @param context the OpenTelemetry Context to wrap
	 */
	public GXTraceContext(Context context) {
		this.context = context;
	}
	
	/**
	 * Returns the underlying OpenTelemetry Context
	 * 
	 * @return the underlying Context instance
	 */
	public Context getTraceContext() {
		return this.context;
	}
}