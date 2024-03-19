package com.genexus.opentelemetry;

import java.util.concurrent.atomic.AtomicReference;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.extension.annotations.SpanAttribute;
import io.opentelemetry.extension.annotations.WithSpan;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
public class OtelSpan {
	private Span span;
	public enum SpanStatusCode
	{
		UNSET,
		OK,
		ERROR
	}
	public OtelSpan(Span span)
	{
		this.span=span;
	}
	public OtelSpan()
	{}
	//region EO Properties
	public String getTraceId()
	{
		if (span != null)
			return span.getSpanContext().getTraceId();
		return "";
	}
	public String getSpanId()
	{
		if (span != null)
			return span.getSpanContext().getSpanId();
		return "";
	}
	public Boolean isRecording()
	{
		if (span != null)
			return span.isRecording();
		return false;
	}
	public GXSpanContext getSpanContext() {
		return new GXSpanContext(getSpanContext(span));
	}
	//endregion
	//region EO Methods
	public void endSpan()
	{
		if (span!=null)
			span.end();
	}
	public GXTraceContext addBaggage(String key, String value)
	{
		return new GXTraceContext(addBaggageReturnContext(key, value));
	}
	public String getBaggaeItem(String key,GXTraceContext gxTraceContext)
	{
		return getBaggaeItemInContext(gxTraceContext.getTraceContext(),key);
	}
	public GXTraceContext getGXTraceContext()
	{
		return new GXTraceContext(getContext());
	}
	public void recordException(String message)
	{
		recordException(span,new Throwable(message));
	}
	public void setStringAttribute(String key, String value)
	{
		if (span != null)
			span.setAttribute(key,value);
	}
	public void setBooleanAttribute(String key, boolean value)
	{
		if (span != null)
			span.setAttribute(key,value);
	}
	public void setDoubleAttribute(String key, double value)
	{
		if (span != null)
			span.setAttribute(key,value);
	}
	public void setLongAttribute(String key, long value)
	{
		if (span != null)
			span.setAttribute(key,value);
	}
	public void setStatus(Byte spanStatusCodeByte)
	{
		StatusCode statusCode = toStatusCode(spanStatusCodeByte);
		if (span != null)
			span.setStatus(statusCode);
	}
	public void setStatus(Byte spanStatusCodeByte, String message)
	{
		StatusCode statusCode = toStatusCode(spanStatusCodeByte);
		if (span != null)
			span.setStatus(statusCode, message);
	}
	//endregion

	//region Private methods

	private String getBaggaeItemInContext(Context context, String key)
	{
		AtomicReference<String> value = new AtomicReference<>("");
		Baggage.fromContext(context).asMap().forEach((k, v) -> {
			if (k.equals(key)) {
				value.set(v.getValue());
			}
		});
		if (value != null)
			return value.get();
		return "";
	}
	private Context addBaggageReturnContext(String key, String value)
	{
		Baggage baggage = Baggage.current().toBuilder().put(key,value).build();
		return baggage.storeInContext(getContext());
	}
	private Context getContext()
	{
		if (span != null)
			return Context.current().with(span);
		return null;
	}
	private Context getContextCurrentSpan()
	{
		return Context.current();
	}

	private static void recordException(Span span, Throwable exc) {
		if (span != null && exc != null) {
			span.recordException(exc);
		}
	}
	private io.opentelemetry.api.trace.SpanContext getSpanContext(Span span)
	{
		if (span != null)
			return span.getSpanContext();
		return null;
	}
	private boolean isRecording(Span span)
	{
		if (span != null)
			return span.isRecording();
		return false;
	}
	private Span current()
	{
		return Span.current();

	}
	private static StatusCode toStatusCode (Byte spanStatusCode){
		switch (spanStatusCode) {
			case 0:
				return StatusCode.UNSET;
			case 1:
				return StatusCode.OK;
			case 2:
				return StatusCode.ERROR;
		}
		return null;
	}
	private static SpanStatusCode fromStatusCode (StatusCode statusCode){
		switch (statusCode) {
			case UNSET:
				return SpanStatusCode.UNSET;
			case OK:
				return SpanStatusCode.OK;
			case ERROR:
				return SpanStatusCode.ERROR;
		}
		return null;
	}
	//endregion

}