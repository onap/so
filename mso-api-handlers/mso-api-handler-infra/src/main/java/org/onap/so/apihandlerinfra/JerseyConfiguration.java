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

import javax.annotation.PostConstruct;
import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.onap.so.apihandler.filters.RequestUriFilter;
import org.onap.so.apihandlerinfra.exceptions.ApiExceptionMapper;
import org.onap.so.apihandlerinfra.tenantisolation.CloudOrchestration;
import org.onap.so.apihandlerinfra.tenantisolation.CloudResourcesOrchestration;
import org.onap.so.apihandlerinfra.tenantisolation.ModelDistributionRequest;
import org.onap.so.logging.jaxrs.filter.JaxRsFilterLogging;
import org.onap.so.web.exceptions.RuntimeExceptionMapper;
import org.springframework.context.annotation.Configuration;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

@Configuration
@ApplicationPath("/")
public class JerseyConfiguration extends ResourceConfig {



	@PostConstruct
	public void setUp() {
		register(GlobalHealthcheckHandler.class);		
		register(NodeHealthcheckHandler.class);
		register(ServiceInstances.class);
		register(TasksHandler.class);
		register(CloudOrchestration.class);
		register(CloudResourcesOrchestration.class);
		register(OrchestrationRequests.class);		
		register(JaxRsFilterLogging.class);
		register(ManualTasks.class);
		register(TasksHandler.class);
		register(ApiListingResource.class);
		register(SwaggerSerializers.class);
		register(ApiExceptionMapper.class);
		register(RuntimeExceptionMapper.class);
		register(RequestUriFilter.class);
		register(E2EServiceInstances.class);
		// this registration seems to be needed to get predictable
		// execution behavior for the above JSON Exception Mappers
		register(com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider.class);
		register(ModelDistributionRequest.class);
		property(ServletProperties.FILTER_FORWARD_ON_404, true);
		BeanConfig beanConfig = new BeanConfig();
		beanConfig.setVersion("1.0.2");
		beanConfig.setSchemes(new String[] { "https" });
		beanConfig.setResourcePackage("org.onap.so.apihandlerinfra");
		beanConfig.setPrettyPrint(true);
		beanConfig.setScan(true);
	}

}
