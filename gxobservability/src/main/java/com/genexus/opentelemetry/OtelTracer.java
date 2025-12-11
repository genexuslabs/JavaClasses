package com.genexus.opentelemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main class for creating and managing OpenTelemetry spans.
 * Provides methods to create spans with different configurations.
 */
public class OtelTracer {
	private static final String OTEL_SERVICE_NAME = "OTEL_SERVICE_NAME";
	private static final String OTEL_SERVICE_VERSION = "OTEL_SERVICE_VERSION";
	private static final String OTEL_RESOURCE_ATTRIBUTES = "OTEL_RESOURCE_ATTRIBUTES";
	private static final String JAVA_INSTRUMENTATION_SCOPE_NAME = "JAVA_INSTRUMENTATION_SCOPE_NAME";
	private static final String JAVA_INSTRUMENTATION_SCOPE_VERSION = "JAVA_INSTRUMENTATION_SCOPE_VERSION";
	
	private static final StringPair instrumentationScope = getInstrumentationScope();
	private static final Tracer tracer = getTracer(instrumentationScope);
	
	/**
	 * Helper class to store name and version information
	 */
	static class StringPair {
		final String name;
		final String version;

		StringPair(String name, String version) {
			this.name = name != null ? name : "";
			this.version = version != null ? version : "";
		}
	}
	
	/**
	 * Enum representing the different types of spans
	 */
	public enum SpanType {
		INTERNAL,
		SERVER,
		CLIENT,
		PRODUCER,
		CONSUMER
	}
	
	/**
	 * Creates a new span with the specified name
	 * 
	 * @param displayName the name of the span
	 * @return a new OtelSpan instance
	 */
	public OtelSpan createSpan(String displayName) {
		Span otelspan = createAndStartSpan(displayName);
		return new OtelSpan(otelspan);
	}
	
	/**
	 * Creates a new span with the specified name and type
	 * 
	 * @param displayName the name of the span
	 * @param spanTypeByte the type of the span as a byte
	 * @return a new OtelSpan instance
	 */
	public OtelSpan createSpan(String displayName, Byte spanTypeByte) {
		SpanKind spanKind = toSpanKind(spanTypeByte);
		Span otelspan = createAndStartSpan(displayName, spanKind);
		return new OtelSpan(otelspan);
	}
	
	/**
	 * Creates a new span with the specified name, type, and parent context
	 * 
	 * @param displayName the name of the span
	 * @param gxTraceContext the parent context
	 * @param spanTypeByte the type of the span as a byte
	 * @return a new OtelSpan instance
	 */
	public OtelSpan createSpan(String displayName, GXTraceContext gxTraceContext, Byte spanTypeByte) {
		if (gxTraceContext == null) {
			return createSpan(displayName, spanTypeByte);
		}
		
		SpanKind spanKind = toSpanKind(spanTypeByte);
		Span otelspan = createAndStartSpan(displayName, spanKind, gxTraceContext.getTraceContext());
		return new OtelSpan(otelspan);
	}
	
	/**
	 * Creates a new span with the specified name, type, parent context, and linked spans
	 * 
	 * @param displayName the name of the span
	 * @param gxTraceContext the parent context
	 * @param spanTypeByte the type of the span as a byte
	 * @param gxSpanContextIterator an iterator of span contexts to link
	 * @return a new OtelSpan instance
	 */
	public OtelSpan createSpan(String displayName, GXTraceContext gxTraceContext, Byte spanTypeByte, Iterator<GXSpanContext> gxSpanContextIterator) {
		if (gxTraceContext == null || gxSpanContextIterator == null) {
			return createSpan(displayName, gxTraceContext, spanTypeByte);
		}
		
		SpanKind spanKind = toSpanKind(spanTypeByte);
		Span otelspan = createAndStartSpan(
			displayName, 
			spanKind, 
			gxTraceContext.getTraceContext(), 
			gxSpanContextIterator
		);
		return new OtelSpan(otelspan);
	}
	
	/**
	 * Gets the current active span
	 * 
	 * @return an OtelSpan wrapping the current span
	 */
	public static OtelSpan getCurrentSpan() {
		return new OtelSpan(Span.current());
	}
	
	//region Private methods
	
	/**
	 * Gets the instrumentation scope information from environment variables
	 * 
	 * @return a StringPair containing the scope name and version
	 */
	private static StringPair getInstrumentationScope() {
		String name = "GeneXus.Tracing";
		String version = "";

		String javaInstrumentationScopeName = System.getenv(JAVA_INSTRUMENTATION_SCOPE_NAME);
		String javaInstrumentationScopeVersion = System.getenv(JAVA_INSTRUMENTATION_SCOPE_VERSION);

		if (javaInstrumentationScopeName != null && !javaInstrumentationScopeName.trim().isEmpty()) {
			name = javaInstrumentationScopeName;
			if (javaInstrumentationScopeVersion != null && !javaInstrumentationScopeVersion.trim().isEmpty()) {
				version = javaInstrumentationScopeVersion;
			}
		} else {
			String serviceName = System.getenv(OTEL_SERVICE_NAME);
			String resourceAttributes = System.getenv(OTEL_RESOURCE_ATTRIBUTES);

			if ((serviceName == null || serviceName.trim().isEmpty()) && resourceAttributes != null) {
				serviceName = extractAttributeValue(resourceAttributes, "service.name");
			}

			String serviceVersion = System.getenv(OTEL_SERVICE_VERSION);
			if ((serviceVersion == null || serviceVersion.trim().isEmpty()) && resourceAttributes != null) {
				serviceVersion = extractAttributeValue(resourceAttributes, "service.version");
			}

			if (serviceName != null && !serviceName.trim().isEmpty()) {
				name = serviceName;
			}
			if (serviceVersion != null && !serviceVersion.trim().isEmpty()) {
				version = serviceVersion;
			}
		}
		return new StringPair(name, version);
	}
	
	/**
	 * Extracts an attribute value from a resource attributes string
	 * 
	 * @param resourceAttributes the resource attributes string
	 * @param attributeName the name of the attribute to extract
	 * @return the attribute value, or null if not found
	 */
	private static String extractAttributeValue(String resourceAttributes, String attributeName) {
		if (resourceAttributes == null || attributeName == null) {
			return null;
		}
		
		String pattern = "(\\w[\\w.\\-]*?)=([^,]+)";
		Pattern regex = Pattern.compile(pattern);
		Matcher matcher = regex.matcher(resourceAttributes);

		while (matcher.find()) {
			String[] keyValue = matcher.group().split("=");
			if (keyValue.length == 2 && keyValue[0].equals(attributeName)) {
				return keyValue[1];
			}
		}
		return null;
	}

	/**
	 * Gets a Tracer instance from the GlobalOpenTelemetry
	 * 
	 * @param instrumentationScope the instrumentation scope information
	 * @return a Tracer instance, or null if not available
	 */
	private static Tracer getTracer(StringPair instrumentationScope) {
		if (instrumentationScope == null) {
			return null;
		}
		
		try {
			OpenTelemetry openTelemetry = GlobalOpenTelemetry.get();
			if (openTelemetry != null) {
				return openTelemetry.getTracer(
					instrumentationScope.name,
					instrumentationScope.version
				);
			}
		} catch (IllegalStateException e) {
			// OpenTelemetry not properly initialized
		}
		return null;
	}

	/**
	 * Creates and starts a span with the specified name
	 * 
	 * @param displayName the name of the span
	 * @return the created span, or null if creation failed
	 */
	private static Span createAndStartSpan(String displayName) {
		if (tracer != null && displayName != null && !displayName.isEmpty()) {
			return tracer.spanBuilder(displayName).startSpan();
		}
		return null;
	}
	
	/**
	 * Creates and starts a span with the specified name and kind
	 * 
	 * @param displayName the name of the span
	 * @param spanKind the kind of the span
	 * @return the created span, or null if creation failed
	 */
	private static Span createAndStartSpan(String displayName, SpanKind spanKind) {
		if (tracer == null || displayName == null || displayName.isEmpty()) {
			return null;
		}
		
		SpanBuilder builder = tracer.spanBuilder(displayName);
		if (spanKind != null) {
			builder.setSpanKind(spanKind);
		}
		return builder.startSpan();
	}
	
	/**
	 * Creates and starts a span with the specified name, kind, and parent context
	 * 
	 * @param displayName the name of the span
	 * @param spanKind the kind of the span
	 * @param context the parent context
	 * @return the created span, or null if creation failed
	 */
	private static Span createAndStartSpan(String displayName, SpanKind spanKind, Context context) {
		if (tracer == null || displayName == null || displayName.isEmpty()) {
			return null;
		}
		
		SpanBuilder builder = tracer.spanBuilder(displayName);
		if (spanKind != null) {
			builder.setSpanKind(spanKind);
		}
		
		if (context != null) {
			builder.setParent(context);
		} else {
			builder.setNoParent();
		}
		
		return builder.startSpan();
	}
	
	/**
	 * Creates and starts a span with the specified name, kind, parent context, and linked spans
	 * 
	 * @param displayName the name of the span
	 * @param spanKind the kind of the span
	 * @param context the parent context
	 * @param gxSpanContexts an iterator of span contexts to link
	 * @return the created span, or null if creation failed
	 */
	private static Span createAndStartSpan(
		String displayName, 
		SpanKind spanKind, 
		Context context, 
		Iterator<GXSpanContext> gxSpanContexts
	) {
		if (tracer == null || displayName == null || displayName.isEmpty() || gxSpanContexts == null) {
			return null;
		}
		
		SpanBuilder spanBuilder = tracer.spanBuilder(displayName);
		
		if (spanKind != null) {
			spanBuilder.setSpanKind(spanKind);
		}
		
		if (context != null) {
			spanBuilder.setParent(context);
		} else {
			spanBuilder.setNoParent();
		}
		
		while (gxSpanContexts.hasNext()) {
			GXSpanContext spanContext = gxSpanContexts.next();
			if (spanContext != null && spanContext.getSpanContext() != null) {
				spanBuilder.addLink(spanContext.getSpanContext());
			}
		}
		
		return spanBuilder.startSpan();
	}

	/**
	 * Converts a byte value to a SpanKind
	 * 
	 * @param spanTypeByte the span type as a byte
	 * @return the corresponding SpanKind, or null if invalid
	 */
	private static SpanKind toSpanKind(Byte spanTypeByte) {
		if (spanTypeByte == null) {
			return null;
		}
		
		switch (spanTypeByte) {
			case 0:
				return SpanKind.INTERNAL;
			case 1:
				return SpanKind.SERVER;
			case 2:
				return SpanKind.CLIENT;
			case 3:
				return SpanKind.PRODUCER;
			case 4:
				return SpanKind.CONSUMER;
			default:
				return null;
		}
	}
	//endregion
}