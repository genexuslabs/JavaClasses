package com.genexus.opentelemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GXSpanContextTest {
    
    private SdkTracerProvider tracerProvider;
    private io.opentelemetry.api.trace.Tracer tracer;
    
    @BeforeEach
    void setUp() {
        // Create a tracer provider for testing
        tracerProvider = SdkTracerProvider.builder()
            .setSampler(Sampler.alwaysOn())
            .build();
        
        // Get a tracer from the provider
        tracer = tracerProvider.get("GXSpanContextTest");
    }

    @Test
    void testConstructorWithSpanContext() {
        // Create a valid SpanContext
        String traceId = TraceId.fromLongs(1234, 5678);
        String spanId = SpanId.fromLong(4321);
        TraceFlags traceFlags = TraceFlags.getSampled();
        TraceState traceState = TraceState.getDefault();
        
        SpanContext spanContext = SpanContext.create(traceId, spanId, traceFlags, traceState);
        
        // Create GXSpanContext with the SpanContext
        GXSpanContext gxSpanContext = new GXSpanContext(spanContext);
        
        // Verify GXSpanContext has the correct values
        assertEquals(traceId, gxSpanContext.traceId());
        assertEquals(spanId, gxSpanContext.spanId());
        assertEquals(spanContext, gxSpanContext.getSpanContext());
    }
    
    @Test
    void testDefaultConstructor() {
        // Create a span with known values
        String expectedTraceId = TraceId.fromLongs(8765, 4321);
        String expectedSpanId = SpanId.fromLong(9876);
        TraceFlags traceFlags = TraceFlags.getSampled();
        TraceState traceState = TraceState.getDefault();
        
        // Create a span context with our expected values
        SpanContext expectedSpanContext = SpanContext.create(
            expectedTraceId, 
            expectedSpanId, 
            traceFlags, 
            traceState
        );
        
        // Create a span with this context
        Span span = Span.wrap(expectedSpanContext);
        
        // Make this the current span in the context
        try (Scope scope = Context.current().with(span).makeCurrent()) {
            // Create GXSpanContext with the default constructor - should use the current span
            GXSpanContext gxSpanContext = new GXSpanContext();
            
            // Verify GXSpanContext has the same values as our test span
            assertNotNull(gxSpanContext);
            assertNotNull(gxSpanContext.getSpanContext());
            assertEquals(expectedTraceId, gxSpanContext.traceId());
            assertEquals(expectedSpanId, gxSpanContext.spanId());
        }
    }
}