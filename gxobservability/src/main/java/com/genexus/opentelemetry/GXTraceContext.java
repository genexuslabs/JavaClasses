package com.genexus.opentelemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;

public class GXTraceContext
{
	private Context context;
	public GXTraceContext(io.opentelemetry.context.Context context)
	{
		this.context = context;
	}
	public Context getTraceContext()
	{
		return this.context;
	}
}