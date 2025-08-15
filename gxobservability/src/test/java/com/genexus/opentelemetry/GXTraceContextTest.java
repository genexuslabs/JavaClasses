package com.genexus.opentelemetry;

import io.opentelemetry.context.Context;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GXTraceContextTest {

    @Test
    void testConstructorWithContext() {
        // Create a root context
        Context context = Context.root();
        
        // Create GXTraceContext with the context
        GXTraceContext gxTraceContext = new GXTraceContext(context);
        
        // Verify GXTraceContext has the correct context
        assertEquals(context, gxTraceContext.getTraceContext());
    }
}