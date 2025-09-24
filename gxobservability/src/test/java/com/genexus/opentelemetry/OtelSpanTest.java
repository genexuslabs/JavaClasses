package com.genexus.opentelemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OtelSpanTest {

    private Span mockSpan;
    private SpanContext mockSpanContext;
    private OtelSpan otelSpan;
    private final String traceId = TraceId.fromLongs(1234, 5678);
    private final String spanId = SpanId.fromLong(4321);

    @BeforeEach
    void setUp() {
        // Create mock span context
        mockSpanContext = SpanContext.create(
            traceId,
            spanId,
            TraceFlags.getSampled(),
            TraceState.getDefault()
        );
        
        // Create mock span
        mockSpan = Mockito.mock(Span.class);
        when(mockSpan.getSpanContext()).thenReturn(mockSpanContext);
        when(mockSpan.isRecording()).thenReturn(true);
        
        // Create OtelSpan with mock span
        otelSpan = new OtelSpan(mockSpan);
    }

    @Test
    void testGetTraceId() {
        assertEquals(traceId, otelSpan.getTraceId());
        
        // Test with null span
        OtelSpan nullSpan = new OtelSpan(null);
        assertEquals("", nullSpan.getTraceId());
    }

    @Test
    void testGetSpanId() {
        assertEquals(spanId, otelSpan.getSpanId());
        
        // Test with null span
        OtelSpan nullSpan = new OtelSpan(null);
        assertEquals("", nullSpan.getSpanId());
    }

    @Test
    void testIsRecording() {
        assertTrue(otelSpan.isRecording());
        
        // Test with null span
        OtelSpan nullSpan = new OtelSpan(null);
        assertFalse(nullSpan.isRecording());
    }

    @Test
    void testSetStringAttribute() {
        String key = "string.key";
        String value = "string value";
        
        otelSpan.setStringAttribute(key, value);
        verify(mockSpan, times(1)).setAttribute(key, value);
        
        // Test with null key or value (should not call setAttribute)
        reset(mockSpan);
        otelSpan.setStringAttribute(null, value);
        otelSpan.setStringAttribute(key, null);
        verify(mockSpan, never()).setAttribute(anyString(), anyString());
    }

    @Test
    void testSetBooleanAttribute() {
        String key = "boolean.key";
        boolean value = true;
        
        otelSpan.setBooleanAttribute(key, value);
        verify(mockSpan, times(1)).setAttribute(key, value);
        
        // Test with null key (should not call setAttribute)
        reset(mockSpan);
        otelSpan.setBooleanAttribute(null, value);
        verify(mockSpan, never()).setAttribute(anyString(), anyBoolean());
    }

    @Test
    void testSetStatus() {
        Byte errorStatus = 2; // ERROR
        
        otelSpan.setStatus(errorStatus);
        verify(mockSpan, times(1)).setStatus(io.opentelemetry.api.trace.StatusCode.ERROR);
        
        // Test with null status code (should not call setStatus)
        reset(mockSpan);
        otelSpan.setStatus(null);
        verify(mockSpan, never()).setStatus(any(io.opentelemetry.api.trace.StatusCode.class));
    }

    @Test
    void testSetStatusWithMessage() {
        Byte okStatus = 1; // OK
        String message = "Status message";
        
        otelSpan.setStatus(okStatus, message);
        verify(mockSpan, times(1)).setStatus(io.opentelemetry.api.trace.StatusCode.OK, message);
        
        // Test with null inputs (should not call setStatus)
        reset(mockSpan);
        otelSpan.setStatus(null, message);
        otelSpan.setStatus(okStatus, null);
        verify(mockSpan, never()).setStatus(any(io.opentelemetry.api.trace.StatusCode.class), anyString());
    }

    @Test
    void testEndSpan() {
        otelSpan.endSpan();
        verify(mockSpan, times(1)).end();
        
        // Test with null span (should not throw exception)
        OtelSpan nullSpan = new OtelSpan(null);
        nullSpan.endSpan(); // Should not throw exception
    }
}