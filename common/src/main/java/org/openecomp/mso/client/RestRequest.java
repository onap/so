package org.openecomp.mso.client;

import java.util.Optional;
import java.util.concurrent.Callable;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class RestRequest implements Callable<Response> {

	private final RestClient client;
	private final String method;
	private final Object entity;
	
	public RestRequest(RestClient client, String method, Object entity) {
		this.client = client;
		this.method = method;
		this.entity = entity;
	}
	@Override
	public Response call() throws Exception {
		final Response response;
		if ("GET".equals(method)) {
			response = this.client.getBuilder().accept(this.client.getAccept()).get();
		} else if ("POST".equals(method)) {
			response = this.client.getBuilder().accept(this.client.getAccept()).post(Entity.entity(entity, this.client.getContentType()));
		} else if ("PATCH".equals(method)) {
			response = this.client.getBuilder().header("X-HTTP-Method-Override", "PATCH").accept(this.client.getAccept())
					.post(Entity.entity(entity, this.client.getMergeContentType()));
		} else if ("DELETE".equals(method)) {
			if (entity == null) {
				response = this.client.getBuilder().accept(this.client.getAccept()).delete();

			} else {
				response = this.client.getBuilder().header("X-HTTP-Method-Override", "DELETE").accept(this.client.getAccept())
						.post(Entity.entity(entity, this.client.getContentType()));
			}
		} else if ("PUT".equals(method)) {
			response = this.client.getBuilder().accept(this.client.getAccept()).put(Entity.entity(entity, this.client.getContentType()));
		} else {
			response = Response.serverError().entity(method + " not valid").build();
		}
		
		Optional<ResponseExceptionMapper> mapper = this.client.addResponseExceptionMapper();
		if (mapper.isPresent()) {
			try {
				mapper.get().map(response);
			} catch (NotFoundException e) {
				if (this.client.props.mapNotFoundToEmpty()) {
					return response;
				} else {
					throw e;
				}
			}
		} else {
			if (response.getStatus() == Status.NOT_FOUND.getStatusCode() && this.client.props.mapNotFoundToEmpty()) {
				return response;
			}
		}

		return response;
	}

}
