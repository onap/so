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

package org.onap.so.adapters.catalogdb;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.onap.so.adapters.catalogdb.rest.CatalogDbAdapterRest;
import org.onap.so.adapters.catalogdb.rest.ServiceRestImpl;
import org.onap.so.adapters.catalogdb.rest.VnfRestImpl;
import org.onap.so.logging.jaxrs.filter.JaxRsFilterLogging;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
@ApplicationPath("/ecomp/mso/catalog")
public class JerseyConfiguration extends ResourceConfig {

    @PostConstruct
    public void setUp() {
        register(CatalogDbAdapterRest.class);
        register(OpenApiResource.class);
        register(AcceptHeaderOpenApiResource.class);
        register(JaxRsFilterLogging.class);
        register(ServiceRestImpl.class);
        register(VnfRestImpl.class);
        OpenAPI oas = new OpenAPI();
        Info info = new Info();
        info.title("Swagger apihandlerinfra bootstrap code");
        info.setVersion("1.0.2");

        SwaggerConfiguration oasConfig = new SwaggerConfiguration().openAPI(oas).prettyPrint(true)
                .resourcePackages(Stream.of("org.onap.so.apihandlerinfra").collect(Collectors.toSet()));

        try {
            new JaxrsOpenApiContextBuilder().application(this).openApiConfiguration(oasConfig).buildContext(true);
        } catch (OpenApiConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}

