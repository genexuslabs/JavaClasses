package com.genexus.opentelemetry;

import io.opentelemetry.api.trace.Span;

/**
 * Helper class for OpenTelemetry operations.
 * Provides utility methods for working with OpenTelemetry.
 */
public class OpenTelemetryHelper {

	/**
	 * Records an exception on the specified span
	 * 
	 * @param span the span to record the exception on
	 * @param exc the exception to record
	 */
	public static void recordException(Span span, Throwable exc) {
		if (span != null && exc != null) {
			span.recordException(exc);
		}
	}

	/**
	 * Records an exception on the current active span
	 * 
	 * @param exc the exception to record
	 * @throws NullPointerException if exc is null
	 */
	public static void recordException(Throwable exc) {
		recordException(Span.current(), exc);
	}
}