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

package org.openecomp.mso.client.aai;

import java.io.IOException;

import org.openecomp.mso.client.aai.entities.AAIError;

@Provider
@Priority(value = 1)
public class AAIClientResponseExceptionMapper implements ClientResponseFilter {

	@Override
	public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
		if (responseContext.getStatus() != Response.Status.OK.getStatusCode() && responseContext.hasEntity()) {
			AAIError error = new ObjectMapper().readValue(responseContext.getEntityStream(), AAIError.class);
			String message = error.getRequestError().getServiceException().getText();

			Response.Status status = Response.Status.fromStatusCode(responseContext.getStatus());
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
}
