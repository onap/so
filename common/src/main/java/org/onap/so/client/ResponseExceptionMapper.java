/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Optional;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

public abstract class ResponseExceptionMapper {

	public void map(Response response) {
		
		response.bufferEntity();
		if (response.getStatus() >= 300) {
			String message = "empty message";
			if (response.hasEntity()) {
				StringWriter writer = new StringWriter();
				try {
					IOUtils.copy((InputStream)response.getEntity(), writer, "UTF-8");
				} catch (IOException e) {
					writer.append("failed to read entity stream");
				}
				Optional<String> result = this.extractMessage(writer.toString());
				if (result.isPresent()) {
					message = result.get();
				}
			}
			Response.Status status = Response.Status.fromStatusCode(response.getStatus());
			WebApplicationException webAppException;
			switch (status) {
			case BAD_REQUEST:
				webAppException = new BadRequestException(message);
				break;
			case UNAUTHORIZED:
				webAppException = new NotAuthorizedException(message);
				break;
			case FORBIDDEN:
				webAppException = new ForbiddenException(message);
				break;
			case NOT_FOUND:
				webAppException = new NotFoundException(message);
				break;
			case METHOD_NOT_ALLOWED:
				webAppException = new NotAllowedException(message);
				break;
			case NOT_ACCEPTABLE:
				webAppException = new NotAcceptableException(message);
				break;
			case PRECONDITION_FAILED:
				webAppException = new PreconditionFailedException(message);
				break;
			case UNSUPPORTED_MEDIA_TYPE:
				webAppException = new NotSupportedException(message);
				break;
			case INTERNAL_SERVER_ERROR:
				webAppException = new InternalServerErrorException(message);
				break;
			case SERVICE_UNAVAILABLE:
				webAppException = new WebApplicationException(message);
				break;
			default:
				webAppException = new WebApplicationException(message);
			}
			throw webAppException;
		}
	}
	
	public abstract Optional<String> extractMessage(String entity);
}
