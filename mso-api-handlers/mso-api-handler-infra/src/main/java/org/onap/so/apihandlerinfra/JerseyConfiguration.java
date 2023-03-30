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

package org.onap.so.apihandlerinfra;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.onap.logging.filter.base.Constants;
import org.onap.so.apihandler.filters.RequestIdFilter;
import org.onap.so.apihandlerinfra.exceptions.ApiExceptionMapper;
import org.onap.so.apihandlerinfra.infra.rest.Network;
import org.onap.so.apihandlerinfra.infra.rest.ServiceInstance;
import org.onap.so.apihandlerinfra.infra.rest.VfModules;
import org.onap.so.apihandlerinfra.infra.rest.Vnf;
import org.onap.so.apihandlerinfra.infra.rest.Volumes;
import org.onap.so.apihandlerinfra.infra.rest.exception.mapper.AAIEntityNotFoundMapper;
import org.onap.so.apihandlerinfra.infra.rest.exception.mapper.CloudConfigurationNotFoundMapper;
import org.onap.so.apihandlerinfra.infra.rest.exception.mapper.NoRecipeExceptionMapper;
import org.onap.so.apihandlerinfra.infra.rest.exception.mapper.RequestConflictMapper;
import org.onap.so.apihandlerinfra.infra.rest.exception.mapper.WorkflowEngineConnectionMapper;
import org.onap.so.apihandlerinfra.tenantisolation.CloudOrchestration;
import org.onap.so.apihandlerinfra.tenantisolation.CloudResourcesOrchestration;
import org.onap.so.apihandlerinfra.tenantisolation.ModelDistributionRequest;
import org.onap.so.logging.jaxrs.filter.SOAuditLogContainerFilter;
import org.onap.so.utils.Components;
import org.onap.so.web.exceptions.RuntimeExceptionMapper;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
@ApplicationPath("/")
public class JerseyConfiguration extends ResourceConfig {

    @PostConstruct
    public void setUp() {
        System.setProperty(Constants.Property.PARTNER_NAME, Components.APIH.toString());
        register(GlobalHealthcheckHandler.class);
        register(NodeHealthcheckHandler.class);
        register(ServiceInstances.class);
        register(Onap3gppServiceInstances.class);
        register(ServiceIntentApiHandler.class);
        register(TasksHandler.class);
        register(CloudOrchestration.class);
        register(CloudResourcesOrchestration.class);
        register(OrchestrationRequests.class);
        register(VfModules.class);
        register(Vnf.class);
        register(Network.class);
        register(Volumes.class);
        register(ServiceInstance.class);
        register(SOAuditLogContainerFilter.class);
        register(ManualTasks.class);
        register(TasksHandler.class);
        register(OpenApiResource.class);
        register(AcceptHeaderOpenApiResource.class);
        register(ApiExceptionMapper.class);
        register(RuntimeExceptionMapper.class);
        register(RequestIdFilter.class);
        register(E2EServiceInstances.class);
        register(WorkflowSpecificationsHandler.class);
        register(InstanceManagement.class);
        register(ResumeOrchestrationRequest.class);
        register(AAIEntityNotFoundMapper.class);
        register(CloudConfigurationNotFoundMapper.class);
        register(NoRecipeExceptionMapper.class);
        register(RequestConflictMapper.class);
        register(WorkflowEngineConnectionMapper.class);
        register(OrchestrationTasks.class);
        register(ManagedObject3gppServiceInstances.class);
        // this registration seems to be needed to get predictable
        // execution behavior for the above JSON Exception Mappers
        register(com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider.class);

        register(ModelDistributionRequest.class);
        property(ServletProperties.FILTER_FORWARD_ON_404, true);

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
