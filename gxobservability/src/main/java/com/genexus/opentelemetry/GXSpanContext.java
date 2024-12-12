package com.genexus.opentelemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
public class GXSpanContext
{
	private io.opentelemetry.api.trace.SpanContext _spanContext;

	public io.opentelemetry.api.trace.SpanContext getSpanContext()
	{
		return _spanContext;
	}
	public GXSpanContext(io.opentelemetry.api.trace.SpanContext spanContext)
	{
		this._spanContext = spanContext;
	}
	public GXSpanContext()
	{
		_spanContext = Span.current().getSpanContext();
	}
	public String traceId()
	{
		return _spanContext.getTraceId();
	}
	public String spanId()
	{
		return _spanContext.getSpanId();
	}
}