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

import java.util.Optional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ResponseExceptionMapper {
    private static final Logger logger = LoggerFactory.getLogger(ResponseExceptionMapper.class);

    public void map(Response response) {
        if (response.getStatus() >= 300) {
            String body = "";
            String message = "empty message";
            try {
                response.bufferEntity();
                if (response.hasEntity()) {
                    body = response.readEntity(String.class);
                }
            } catch (IllegalStateException e) {
                body = "failed to read entity stream";
                logger.error(body, e);
            } catch (ProcessingException e) {
                body = "could not buffer stream";
                logger.error(body, e);
            }
            Optional<String> result = this.extractMessage(body);
            if (result.isPresent()) {
                message = result.get();
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
