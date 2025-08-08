package com.genexus.opentelemetry;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

class OtelTracerTest {

    // These tests can't properly test the static initialization of OtelTracer
    // because GlobalOpenTelemetry is hard to mock. We'll focus on testing the public methods.

    @Test
    void testCreateSpan() {
        // This is a basic test of the public API
        // Real functionality is difficult to test without integration tests
        OtelTracer tracer = new OtelTracer();
        OtelSpan span = tracer.createSpan("test-span");
        
        // We can't make strong assertions because of the static initialization
        // Just verify no exceptions are thrown
        assertNotNull(span);
    }

    @Test
    void testCreateSpanWithType() {
        OtelTracer tracer = new OtelTracer();
        Byte spanType = 1; // SERVER
        OtelSpan span = tracer.createSpan("test-span-with-type", spanType);
        
        assertNotNull(span);
    }

    @Test
    void testCreateSpanWithContext() {
        OtelTracer tracer = new OtelTracer();
        Byte spanType = 2; // CLIENT
        
        // Create a mock context
        Context context = Context.root();
        GXTraceContext gxContext = new GXTraceContext(context);
        
        OtelSpan span = tracer.createSpan("test-span-with-context", gxContext, spanType);
        
        assertNotNull(span);
    }

    @Test
    void testCreateSpanWithLinkedSpans() {
        OtelTracer tracer = new OtelTracer();
        Byte spanType = 3; // PRODUCER
        
        // Create a mock context
        Context context = Context.root();
        GXTraceContext gxContext = new GXTraceContext(context);
        
        // Create a mock span context
        String traceId = TraceId.fromLongs(1234, 5678);
        String spanId = SpanId.fromLong(4321);
        SpanContext spanContext = SpanContext.create(
            traceId,
            spanId,
            TraceFlags.getSampled(),
            TraceState.getDefault()
        );
        
        GXSpanContext gxSpanContext = new GXSpanContext(spanContext);
        List<GXSpanContext> linkedSpans = new ArrayList<>();
        linkedSpans.add(gxSpanContext);
        
        OtelSpan span = tracer.createSpan(
            "test-span-with-links",
            gxContext,
            spanType,
            linkedSpans.iterator()
        );
        
        assertNotNull(span);
    }

    @Test
    void testGetCurrentSpan() {
        OtelSpan span = OtelTracer.getCurrentSpan();
        assertNotNull(span);
    }
    
    @Test
    void testCreateSpanWithNullContext() {
        OtelTracer tracer = new OtelTracer();
        Byte spanType = 2; // CLIENT
        
        // Pass null context - should not throw exception
        OtelSpan span = tracer.createSpan("test-span-with-null-context", null, spanType);
        
        assertNotNull(span);
    }
    
    @Test
    void testCreateSpanWithNullIterator() {
        OtelTracer tracer = new OtelTracer();
        Byte spanType = 3; // PRODUCER
        
        Context context = Context.root();
        GXTraceContext gxContext = new GXTraceContext(context);
        
        // Pass null iterator - should fall back to simpler method
        OtelSpan span = tracer.createSpan(
            "test-span-with-null-iterator",
            gxContext,
            spanType,
            null
        );
        
        assertNotNull(span);
    }
}