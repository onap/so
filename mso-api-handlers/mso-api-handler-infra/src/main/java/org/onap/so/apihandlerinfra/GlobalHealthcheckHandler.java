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


import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.http.HttpStatus;
import org.onap.so.apihandlerinfra.HealthCheckConfig.Endpoint;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;


@Component
@Path("/globalhealthcheck")
@OpenAPIDefinition(info = @Info(title = "/globalhealthcheck", description = "APIH Infra Global Health Check"))
public class GlobalHealthcheckHandler {
    private static Logger logger = LoggerFactory.getLogger(GlobalHealthcheckHandler.class);
    protected static final String CONTEXTPATH_PROPERTY = "management.endpoints.web.base-path";
    protected static final String PROPERTY_DOMAIN = "mso.health";
    protected static final String CATALOGDB_PROPERTY = PROPERTY_DOMAIN + ".endpoints.catalogdb";
    protected static final String REQUESTDB_PROPERTY = PROPERTY_DOMAIN + ".endpoints.requestdb";
    protected static final String SDNC_PROPERTY = PROPERTY_DOMAIN + ".endpoints.sdnc";
    protected static final String OPENSTACK_PROPERTY = PROPERTY_DOMAIN + ".endpoints.openstack";
    protected static final String BPMN_PROPERTY = PROPERTY_DOMAIN + ".endpoints.bpmn";
    protected static final String ASDC_PROPERTY = PROPERTY_DOMAIN + ".endpoints.asdc";
    protected static final String REQUESTDBATTSVC_PROPERTY = PROPERTY_DOMAIN + ".endpoints.requestdbattsvc";
    protected static final String MSO_AUTH_PROPERTY = PROPERTY_DOMAIN + ".auth";
    protected static final String DEFAULT_PROPERTY_VALUE = "";

    // e.g. /manage
    private String actuatorContextPath;

    @Autowired
    private Environment env;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HealthCheckConfig config;

    private static final String HEALTH = "/health";

    private String msoAuth;

    @PostConstruct
    protected void init() {
        actuatorContextPath = env.getProperty(CONTEXTPATH_PROPERTY, String.class, DEFAULT_PROPERTY_VALUE);
        msoAuth = env.getProperty(MSO_AUTH_PROPERTY, String.class, DEFAULT_PROPERTY_VALUE);
    }

    @GET
    @Produces("application/json")
    @Operation(description = "Performing global health check", responses = @ApiResponse(
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))))
    @Transactional
    public Response globalHealthcheck(@DefaultValue("true") @QueryParam("enableBpmn") boolean enableBpmn,
            @Context ContainerRequestContext requestContext) {
        Response HEALTH_CHECK_RESPONSE = null;
        // Build internal response object
        HealthCheckResponse rsp = new HealthCheckResponse();

        try {
            // Generated RequestId
            String requestId = requestContext.getProperty("requestId").toString();
            logger.info(LoggingAnchor.TWO, MessageEnum.APIH_GENERATED_REQUEST_ID.toString(), requestId);

            List<Endpoint> endpoints = config.getEndpoints().stream().filter(item -> {
                if (!enableBpmn && SoSubsystems.BPMN.equals(item.getSubsystem())) {
                    return false;
                } else {
                    return true;
                }
            }).collect(Collectors.toList());

            for (Endpoint endpoint : endpoints) {
                rsp.getSubsystems().add(querySubsystemHealth(endpoint));
            }

            // set Message
            rsp.setMessage(String.format("HttpStatus: %s", HttpStatus.SC_OK));
            logger.info(rsp.toString());

            HEALTH_CHECK_RESPONSE = Response.status(HttpStatus.SC_OK).entity(rsp).build();

        } catch (Exception ex) {
            logger.error("Exception occurred", ex);
            rsp.setMessage(ex.getMessage());
            HEALTH_CHECK_RESPONSE = Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(rsp).build();
        }

        return HEALTH_CHECK_RESPONSE;
    }

    protected HttpEntity<String> buildHttpEntityForRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.set(HttpHeaders.AUTHORIZATION, msoAuth);
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        return entity;
    }

    protected HealthCheckSubsystem querySubsystemHealth(Endpoint subsystem) {
        HealthCheckStatus status = HealthCheckStatus.DOWN;
        URI uri = subsystem.getUri();
        try {
            // get port number for the subsystem
            // build final endpoint url
            uri = UriBuilder.fromUri(subsystem.getUri()).path(actuatorContextPath).path(HEALTH).build();
            logger.info("Calculated URL: {}", uri);

            ResponseEntity<SubsystemHealthcheckResponse> result = restTemplate.exchange(uri, HttpMethod.GET,
                    buildHttpEntityForRequest(), SubsystemHealthcheckResponse.class);

            status = processResponseFromSubsystem(result, subsystem);


        } catch (Exception ex) {
            logger.error("Exception occured in GlobalHealthcheckHandler.querySubsystemHealth() ", ex);
        }

        return new HealthCheckSubsystem(subsystem.getSubsystem(), uri, status);
    }

    protected HealthCheckStatus processResponseFromSubsystem(ResponseEntity<SubsystemHealthcheckResponse> result,
            Endpoint endpoint) {
        if (result == null || result.getStatusCodeValue() != HttpStatus.SC_OK) {
            logger.error(String.format("Globalhealthcheck: checking subsystem: %s failed ! result object is: %s",
                    endpoint.getSubsystem(), result == null ? "NULL" : result));
            return HealthCheckStatus.DOWN;
        }

        SubsystemHealthcheckResponse body = result.getBody();

        String status = body.getStatus();
        if ("UP".equalsIgnoreCase(status)) {
            return HealthCheckStatus.UP;
        } else {
            logger.error("{}, query health endpoint did not return UP status!", endpoint.getSubsystem());
            return HealthCheckStatus.DOWN;
        }
    }

}
