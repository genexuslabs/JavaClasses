package com.genexus.ws.rs.core;

public abstract class Response extends jakarta.ws.rs.core.Response{

	public static Response.ResponseBuilder notModifiedWrapped() {
		return new Response.ResponseBuilder(jakarta.ws.rs.core.Response.notModified());
	}

	public static Response.ResponseBuilder okWrapped(Object entity) {
		return new Response.ResponseBuilder(jakarta.ws.rs.core.Response.ok(entity));
	}

	public static Response.ResponseBuilder okWrapped() {
		return new Response.ResponseBuilder(jakarta.ws.rs.core.Response.ok());
	}

	public static Response.ResponseBuilder notFound() {
		return new Response.ResponseBuilder(jakarta.ws.rs.core.Response.status(Status.NOT_FOUND));
	}

	public static Response.ResponseBuilder conflict() {
		return new Response.ResponseBuilder(jakarta.ws.rs.core.Response.status(Status.CONFLICT));
	}

	public static Response.ResponseBuilder forbidden() {
		return new Response.ResponseBuilder(jakarta.ws.rs.core.Response.status(Status.FORBIDDEN));
	}

	public static Response.ResponseBuilder unauthorized() {
		return new Response.ResponseBuilder(jakarta.ws.rs.core.Response.status(Status.UNAUTHORIZED));
	}

	public static Response.ResponseBuilder noContentWrapped() {
		return new Response.ResponseBuilder(jakarta.ws.rs.core.Response.noContent());
	}

	public static Response.ResponseBuilder createdWrapped(java.net.URI uri) {
		return new Response.ResponseBuilder(jakarta.ws.rs.core.Response.created(uri));
	}

	public static Response.ResponseBuilder statusWrapped(int status) {
		return new Response.ResponseBuilder(jakarta.ws.rs.core.Response.status(status));
	}
	public static class ResponseBuilder implements IResponseBuilder{
		jakarta.ws.rs.core.Response.ResponseBuilder rb;

		ResponseBuilder(jakarta.ws.rs.core.Response.ResponseBuilder rb) {
			this.rb = rb;
		}

		public jakarta.ws.rs.core.Response build() {
			return rb.build();
		}
		public void type(String type) {
			rb.type(type);
		}
		public void entity(Object entity) { rb.entity(entity); }

		public IResponseBuilder status(short i) { return new ResponseBuilder(rb.status(i)); }
		public IResponseBuilder entityWrapped(Object entity) { return new ResponseBuilder(rb.entity(entity)); }

		public void header(String header, Object object) {
			rb.header(header, object);
		}
	}
}