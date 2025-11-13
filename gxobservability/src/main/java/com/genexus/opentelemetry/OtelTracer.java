package com.genexus.opentelemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import java.util.Iterator;
import java.util.regex.*;
public class OtelTracer {
	private static final String OTEL_SERVICE_NAME = "OTEL_SERVICE_NAME";
	private static final String OTEL_SERVICE_VERSION = "OTEL_SERVICE_VERSION";
	private static final String OTEL_RESOURCE_ATTRIBUTES = "OTEL_RESOURCE_ATTRIBUTES";
	private static final String JAVA_INSTRUMENTATION_SCOPE_NAME = "JAVA_INSTRUMENTATION_SCOPE_NAME";
	private static final String JAVA_INSTRUMENTATION_SCOPE_VERSION = "JAVA_INSTRUMENTATION_SCOPE_VERSION";
	private static StringPair instrumentationScope=getInstrumentationScope();
	private static Tracer tracer=getTracer(instrumentationScope);
	static class StringPair {
		final String name;
		final String version;

		StringPair(String name, String version) {
			this.name = name;
			this.version = version;
		}
	}
	public enum SpanType
	{
		INTERNAL,
		SERVER,
		CLIENT,
		PRODUCER,
		CONSUMER
	}
	public OtelSpan createSpan(String displayName)
	{
		Span otelspan= createAndStartSpan(displayName);
		return new OtelSpan(otelspan);
	}
	public OtelSpan createSpan(String displayName, Byte spanTypeByte)
	{
		io.opentelemetry.api.trace.SpanKind spanKind = toSpanKind(spanTypeByte);
		Span otelspan = createAndStartSpan(displayName, spanKind);
		return new OtelSpan(otelspan);
	}
	public OtelSpan createSpan(String displayName, GXTraceContext gxTraceContext, Byte spanTypeByte)
	{
		io.opentelemetry.api.trace.SpanKind spanKind = toSpanKind(spanTypeByte);
		Span otelspan = createAndStartSpan(displayName,spanKind,gxTraceContext.getTraceContext());
		return new OtelSpan(otelspan);
	}
	public OtelSpan createSpan(String displayName, GXTraceContext gxTraceContext, Byte spanTypeByte, Iterator<GXSpanContext> gxSpanContextIterator)
	{
		io.opentelemetry.api.trace.SpanKind spanKind = toSpanKind(spanTypeByte);
		Span otelspan = createAndStartSpan(displayName,spanKind,gxTraceContext.getTraceContext(),gxSpanContextIterator);
		return new OtelSpan(otelspan);
	}
	public static OtelSpan getCurrentSpan()
	{
		return new OtelSpan(Span.current());
	}
	//region Private methods
	private static StringPair getInstrumentationScope()
	{
		String name="GeneXus.Tracing";
		String version="";

		String javaInstrumentationScopeName = System.getenv(JAVA_INSTRUMENTATION_SCOPE_NAME);
		String javaInstrumentationScopeVersion = System.getenv(JAVA_INSTRUMENTATION_SCOPE_VERSION);

		if (javaInstrumentationScopeName!=null && !javaInstrumentationScopeName.trim().isEmpty())
		{
			name = javaInstrumentationScopeName;
			if (javaInstrumentationScopeVersion!=null && !javaInstrumentationScopeVersion.trim().isEmpty()) {
				version = javaInstrumentationScopeVersion;
			}
		}
		else
		{
			String serviceName = System.getenv(OTEL_SERVICE_NAME);

			if (serviceName==null || serviceName.trim().isEmpty()) {
				String pattern = "(?:\\b\\w+\\b=\\w+)(?:,(?:\\b\\w+\\b=\\w+))*";
				Pattern regex = Pattern.compile(pattern);
				Matcher matcher = regex.matcher(OTEL_RESOURCE_ATTRIBUTES);

				while (matcher.find()) {
					String[] keyValue = matcher.group().split("=");
					if (keyValue[0].equals("service.name")) {
						serviceName = keyValue[1];
						break;
					}
				}
			}

			String serviceVersion = System.getenv(OTEL_SERVICE_VERSION);
			if (serviceVersion==null || serviceVersion.trim().isEmpty()) {
				String pattern = "(?:\\b\\w+\\b=\\w+)(?:,(?:\\b\\w+\\b=\\w+))*";
				Pattern regex = Pattern.compile(pattern);
				Matcher matcher = regex.matcher(OTEL_RESOURCE_ATTRIBUTES);

				while (matcher.find()) {

					String[] keyValue = matcher.group().split("=");

					if (keyValue[0].equals("service.version")) {
						serviceVersion = keyValue[1];
						break;
					}
				}
			}

			if (serviceName!=null && !serviceName.trim().isEmpty())
				name = serviceName;
			if (serviceVersion!=null && !serviceVersion.trim().isEmpty())
				version = serviceVersion;

		}
		return new StringPair(name,version);
	}

	private static Tracer getTracer(StringPair instrumentationScope)
	{
		OpenTelemetry openTelemetry = GlobalOpenTelemetry.get();
		if (openTelemetry != null)
			return openTelemetry.getTracer(instrumentationScope.name,instrumentationScope.version);
		return null;
	}

	private static Span createAndStartSpan(String displayName)
	{
		if (tracer != null) {
			if (!displayName.isEmpty())
				return tracer.spanBuilder(displayName).startSpan();
		}
		return null;
	}
	private static Span createAndStartSpan(String displayName, io.opentelemetry.api.trace.SpanKind spanKind)
	{
		if  (tracer != null) {
			if (!displayName.isEmpty() && spanKind != null)
				return tracer.spanBuilder(displayName).setSpanKind(spanKind).startSpan();
			else if (spanKind == null) {
				return tracer.spanBuilder(displayName).startSpan();
			}
		}
		return null;
	}
	private static Span createAndStartSpan(String displayName, io.opentelemetry.api.trace.SpanKind spanKind, Context context)
	{
		if  (tracer != null) {
			if (!displayName.isEmpty() && spanKind != null && context != null)
				return tracer.spanBuilder(displayName).setSpanKind(spanKind).setParent(context).startSpan();
			else {
				if (!displayName.isEmpty() && spanKind != null && context == null)
					return tracer.spanBuilder(displayName).setSpanKind(spanKind).setNoParent().startSpan();
			}
		}
		return null;
	}
	private static Span createAndStartSpan(String displayName, io.opentelemetry.api.trace.SpanKind spanKind, Context context, Iterator<GXSpanContext> gxSpanContexts)
	{
		if  (tracer != null) {
			SpanBuilder spanBuilder;
			if (!displayName.isEmpty() && spanKind != null && context != null) {
				spanBuilder = tracer.spanBuilder(displayName).setSpanKind(spanKind).setParent(context);
			} else {
				if (!displayName.isEmpty() && spanKind != null && context == null)
					spanBuilder = tracer.spanBuilder(displayName).setSpanKind(spanKind).setNoParent();
				else
					return null;
			}
			while (gxSpanContexts.hasNext()) {
				if (spanBuilder != null)
					spanBuilder.addLink(gxSpanContexts.next().getSpanContext());
			}
			return spanBuilder.startSpan();
		}
		return null;
	}

	private static io.opentelemetry.api.trace.SpanKind toSpanKind (Byte spanTypeByte){
		switch (spanTypeByte) {
			case 0:
				return io.opentelemetry.api.trace.SpanKind.INTERNAL;
			case 1:
				return io.opentelemetry.api.trace.SpanKind.SERVER;
			case 2:
				return io.opentelemetry.api.trace.SpanKind.CLIENT;
			case 3:
				return io.opentelemetry.api.trace.SpanKind.PRODUCER;
			case 4:
				return io.opentelemetry.api.trace.SpanKind.CONSUMER;
		}
		return null;
	}
	//endregion

}