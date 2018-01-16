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

package org.openecomp.mso.client;

import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Priority;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.ext.Provider;
import org.apache.commons.io.IOUtils;

@Provider
@Priority(value = 1)
public class ResponseExceptionMapper implements ClientResponseFilter {

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        if (isError(responseContext)) {
            String message;
            if (responseContext.hasEntity()) {
                message = extractMessage(responseContext.getEntityStream());
            } else {
                message = "empty message";
            }
            Response.Status status = Response.Status.fromStatusCode(responseContext.getStatus());
            switch (status) {
                case BAD_REQUEST:
                    throw new BadRequestException(message);
                case UNAUTHORIZED:
                    throw new NotAuthorizedException(message);
                case FORBIDDEN:
                    throw new ForbiddenException(message);
                case NOT_FOUND:
                    throw new NotFoundException(message);
                case METHOD_NOT_ALLOWED:
                    throw new NotAllowedException(message);
                case NOT_ACCEPTABLE:
                    throw new NotAcceptableException(message);
                case PRECONDITION_FAILED:
                    throw new PreconditionFailedException(message);
                case UNSUPPORTED_MEDIA_TYPE:
                    throw new NotSupportedException(message);
                case INTERNAL_SERVER_ERROR:
                    throw new InternalServerErrorException(message);
                case SERVICE_UNAVAILABLE:
                    throw new WebApplicationException(message);
                default:
                    throw new WebApplicationException(message);
            }
        }
    }

    private boolean isError(ClientResponseContext responseContext) {
        Family family = responseContext.getStatusInfo().getFamily();
        return family == Family.CLIENT_ERROR || family == Family.SERVER_ERROR || family == Family.OTHER;
    }

    private String extractMessage(InputStream stream) throws IOException {
        final String input = IOUtils.toString(stream, "UTF-8");
        stream.close();
        return input;
    }
}
