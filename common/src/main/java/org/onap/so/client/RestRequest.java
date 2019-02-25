/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.client;

import java.util.Optional;
import java.util.concurrent.Callable;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestRequest implements Callable<Response> {

	private static final Logger logger = LoggerFactory.getLogger(RestRequest.class);

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
				if (this.client.props.mapNotFoundToEmpty() && "GET".equals(method)) {
					logger.debug("RestClient recieved not found on URL: {}", this.client.getWebTarget().getUri());
					return response;
				} else {
					throw e;
				}
			}
		}

		return response;
	}

}
