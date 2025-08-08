package com.genexus.opentelemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;

/**
 * Wrapper class for OpenTelemetry SpanContext.
 * Provides access to trace and span identifiers.
 */
public class GXSpanContext {
	private final SpanContext spanContext;

	/**
	 * Returns the underlying OpenTelemetry SpanContext
	 * 
	 * @return the underlying SpanContext instance
	 */
	public SpanContext getSpanContext() {
		return spanContext;
	}
	
	/**
	 * Creates a GXSpanContext from an existing SpanContext
	 * 
	 * @param spanContext the OpenTelemetry SpanContext to wrap
	 */
	public GXSpanContext(SpanContext spanContext) {
		this.spanContext = spanContext;
	}
	
	/**
	 * Creates a GXSpanContext from the current active span
	 */
	public GXSpanContext() {
		spanContext = Span.current().getSpanContext();
	}
	
	/**
	 * Gets the trace ID of the current span context
	 * 
	 * @return the trace ID as a hexadecimal string
	 */
	public String traceId() {
		return spanContext.getTraceId();
	}
	
	/**
	 * Gets the span ID of the current span context
	 * 
	 * @return the span ID as a hexadecimal string
	 */
	public String spanId() {
		return spanContext.getSpanId();
	}
}