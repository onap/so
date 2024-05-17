/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.apihandlerinfra;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.transaction.Transactional;
import java.net.UnknownHostException;

@Path("/nodehealthcheck")
@OpenAPIDefinition(info = @Info(title = "/nodehealthcheck", description = "API Handler Infra Node Health Check"))
@Component
public class NodeHealthcheckHandler {

    private static Logger logger = LoggerFactory.getLogger(NodeHealthcheckHandler.class);

    private static final String CHECK_HTML =
            "<!DOCTYPE html><html><head><meta charset=\"ISO-8859-1\"><title>Health Check</title></head><body>Application ready</body></html>";

    public static final Response HEALTH_CHECK_RESPONSE = Response.status(HttpStatus.SC_OK).entity(CHECK_HTML).build();

    @GET
    @Produces("text/html")
    @Operation(description = "Performing node health check", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response nodeHealthcheck(@Context ContainerRequestContext requestContext) throws UnknownHostException {
        // Generated RequestId
        String requestId = requestContext.getProperty("requestId").toString();
        logger.info(LoggingAnchor.TWO, MessageEnum.APIH_GENERATED_REQUEST_ID.toString(), requestId);
        return HEALTH_CHECK_RESPONSE;
    }
}
