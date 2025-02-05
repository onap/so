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

package org.onap.so.asdc;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.onap.so.asdc.client.test.rest.ASDCRestInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.jaxrs2.SwaggerSerializers;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
@ApplicationPath("/test")
public class JerseyConfiguration extends ResourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(JerseyConfiguration.class);

    @PostConstruct
    public void setUp() {
        register(ASDCRestInterface.class);
        register(OpenApiResource.class);
        register(SwaggerSerializers.class);

        final OpenAPI openApi = new OpenAPI();
        Info info = new Info();
        info.setVersion("1.0.2");
        info.setTitle("Swagger asdc-controller code");
        openApi.setInfo(info);

        SwaggerConfiguration swaggerConfig = new SwaggerConfiguration().openAPI(openApi).prettyPrint(true)
                .resourcePackages(Stream.of("org.onap.so.asdc").collect(Collectors.toSet()));

        try {
            JaxrsOpenApiContextBuilder jaxrsConfig = new JaxrsOpenApiContextBuilder();
            jaxrsConfig.application(this).openApiConfiguration(swaggerConfig).buildContext(true);
        } catch (OpenApiConfigurationException e) {
            logger.error("Error during jersey configuration", e);
            throw new RuntimeException(e.getMessage(), e);
        }

    }
}
