package com.genexus.opentelemetry;

import java.util.concurrent.atomic.AtomicReference;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.api.baggage.Baggage;

/**
 * Wrapper class for OpenTelemetry Span.
 * Provides methods to manage spans, attributes, and context propagation.
 */
public class OtelSpan {
	private final Span span;
	
	/**
	 * Enum representing the different status codes a span can have
	 */
	public enum SpanStatusCode {
		UNSET,
		OK,
		ERROR
	}
	
	/**
	 * Creates an OtelSpan with an existing OpenTelemetry Span
	 * 
	 * @param span the OpenTelemetry Span to wrap
	 */
	public OtelSpan(Span span) {
		this.span = span;
	}
	
	/**
	 * Creates an empty OtelSpan with no underlying span
	 */
	public OtelSpan() {
		this.span = null;
	}
	
	//region Properties
	
	/**
	 * Gets the trace ID of the current span
	 * 
	 * @return the trace ID as a string, or empty string if no span exists
	 */
	public String getTraceId() {
		if (span != null && span.getSpanContext() != null) {
			return span.getSpanContext().getTraceId();
		}
		return "";
	}
	
	/**
	 * Gets the span ID of the current span
	 * 
	 * @return the span ID as a string, or empty string if no span exists
	 */
	public String getSpanId() {
		if (span != null && span.getSpanContext() != null) {
			return span.getSpanContext().getSpanId();
		}
		return "";
	}
	
	/**
	 * Checks if the current span is recording events
	 * 
	 * @return true if recording, false otherwise
	 */
	public Boolean isRecording() {
		return span != null && span.isRecording();
	}
	
	/**
	 * Gets the span context of the current span
	 * 
	 * @return a GXSpanContext wrapping the current span context, or null if no span exists
	 */
	public GXSpanContext getSpanContext() {
		if (span != null && span.getSpanContext() != null) {
			return new GXSpanContext(span.getSpanContext());
		}
		return null;
	}
	//endregion
	
	//region Methods
	
	/**
	 * Ends the current span
	 */
	public void endSpan() {
		if (span != null) {
			span.end();
		}
	}
	
	/**
	 * Adds a baggage item to the current context
	 * 
	 * @param key the baggage item key
	 * @param value the baggage item value
	 * @return a new GXTraceContext containing the added baggage
	 */
	public GXTraceContext addBaggage(String key, String value) {
		Context context = addBaggageReturnContext(key, value);
		return new GXTraceContext(context);
	}
	
	/**
	 * Gets a baggage item from the specified trace context
	 * 
	 * @param key the baggage item key
	 * @param gxTraceContext the trace context to get the baggage from
	 * @return the baggage item value, or empty string if not found
	 */
	public String getBaggageItem(String key, GXTraceContext gxTraceContext) {
		return getBaggageItemInContext(gxTraceContext.getTraceContext(), key);
	}
	
	/**
	 * Gets the current trace context
	 * 
	 * @return a new GXTraceContext containing the current context
	 */
	public GXTraceContext getGXTraceContext() {
		Context context = getContext();
		if (context != null) {
			return new GXTraceContext(context);
		}
		return new GXTraceContext(Context.current());
	}
	
	/**
	 * Records an exception with the given message
	 * 
	 * @param message the exception message
	 */
	public void recordException(String message) {
		if (span != null && message != null) {
			recordException(span, new Throwable(message));
		}
	}
	
	/**
	 * Sets a string attribute on the current span
	 * 
	 * @param key the attribute key
	 * @param value the attribute value
	 */
	public void setStringAttribute(String key, String value) {
		if (span != null && key != null && value != null) {
			span.setAttribute(key, value);
		}
	}
	
	/**
	 * Sets a boolean attribute on the current span
	 * 
	 * @param key the attribute key
	 * @param value the attribute value
	 */
	public void setBooleanAttribute(String key, boolean value) {
		if (span != null && key != null) {
			span.setAttribute(key, value);
		}
	}
	
	/**
	 * Sets a double attribute on the current span
	 * 
	 * @param key the attribute key
	 * @param value the attribute value
	 */
	public void setDoubleAttribute(String key, double value) {
		if (span != null && key != null) {
			span.setAttribute(key, value);
		}
	}
	
	/**
	 * Sets a long attribute on the current span
	 * 
	 * @param key the attribute key
	 * @param value the attribute value
	 */
	public void setLongAttribute(String key, long value) {
		if (span != null && key != null) {
			span.setAttribute(key, value);
		}
	}
	
	/**
	 * Sets the status of the current span
	 * 
	 * @param spanStatusCodeByte the status code as a byte
	 */
	public void setStatus(Byte spanStatusCodeByte) {
		if (span != null && spanStatusCodeByte != null) {
			StatusCode statusCode = toStatusCode(spanStatusCodeByte);
			if (statusCode != null) {
				span.setStatus(statusCode);
			}
		}
	}
	
	/**
	 * Sets the status of the current span with a description
	 * 
	 * @param spanStatusCodeByte the status code as a byte
	 * @param message the status description
	 */
	public void setStatus(Byte spanStatusCodeByte, String message) {
		if (span != null && spanStatusCodeByte != null && message != null) {
			StatusCode statusCode = toStatusCode(spanStatusCodeByte);
			if (statusCode != null) {
				span.setStatus(statusCode, message);
			}
		}
	}
	//endregion
	
	//region Private methods
	
	/**
	 * Gets a baggage item from the specified context
	 * 
	 * @param context the context to get the baggage from
	 * @param key the baggage item key
	 * @return the baggage item value, or empty string if not found
	 */
	private String getBaggageItemInContext(Context context, String key) {
		if (context == null || key == null) {
			return "";
		}
		
		AtomicReference<String> value = new AtomicReference<>("");
		Baggage.fromContext(context).asMap().forEach((k, v) -> {
			if (k.equals(key)) {
				value.set(v.getValue());
			}
		});
		return value.get();
	}
	
	/**
	 * Adds a baggage item to the current context
	 * 
	 * @param key the baggage item key
	 * @param value the baggage item value
	 * @return the new context with the added baggage
	 */
	private Context addBaggageReturnContext(String key, String value) {
		if (key == null || value == null) {
			return Context.current();
		}
		
		Baggage baggage = Baggage.current().toBuilder().put(key, value).build();
		Context context = getContext();
		return baggage.storeInContext(context != null ? context : Context.current());
	}
	
	/**
	 * Gets the current context with the span
	 * 
	 * @return the current context with the span, or null if no span exists
	 */
	private Context getContext() {
		if (span != null) {
			return Context.current().with(span);
		}
		return Context.current();
	}
	
	/**
	 * Records an exception on the specified span
	 * 
	 * @param span the span to record the exception on
	 * @param exc the exception to record
	 */
	private static void recordException(Span span, Throwable exc) {
		if (span != null && exc != null) {
			span.recordException(exc);
		}
	}
	
	/**
	 * Converts a byte value to a StatusCode
	 * 
	 * @param spanStatusCode the status code as a byte
	 * @return the corresponding StatusCode, or null if invalid
	 */
	private static StatusCode toStatusCode(Byte spanStatusCode) {
		if (spanStatusCode == null) {
			return null;
		}
		
		switch (spanStatusCode) {
			case 0:
				return StatusCode.UNSET;
			case 1:
				return StatusCode.OK;
			case 2:
				return StatusCode.ERROR;
			default:
				return null;
		}
	}
	
	/**
	 * Converts a StatusCode to a SpanStatusCode
	 * 
	 * @param statusCode the StatusCode to convert
	 * @return the corresponding SpanStatusCode, or null if invalid
	 */
	private static SpanStatusCode fromStatusCode(StatusCode statusCode) {
		if (statusCode == null) {
			return null;
		}
		
		switch (statusCode) {
			case UNSET:
				return SpanStatusCode.UNSET;
			case OK:
				return SpanStatusCode.OK;
			case ERROR:
				return SpanStatusCode.ERROR;
			default:
				return null;
		}
	}
	//endregion
}