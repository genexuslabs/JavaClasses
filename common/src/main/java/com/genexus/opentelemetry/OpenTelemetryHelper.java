package com.genexus.opentelemetry;

import io.opentelemetry.api.trace.Span;

public class OpenTelemetryHelper {

	public static void recordException(Span span, Throwable exc) {
		if (span != null && exc != null) {
			span.recordException(exc);
		}
	}

	public static void recordException(Throwable exc) {
		recordException(Span.current(), exc);
	}
}
