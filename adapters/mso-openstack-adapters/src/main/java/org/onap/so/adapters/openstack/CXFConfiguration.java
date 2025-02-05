/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.adapters.openstack;

import java.util.Arrays;
import java.util.HashSet;
import jakarta.servlet.Servlet;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.openapi.OpenApiFeature;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.onap.so.adapters.cloudregion.CloudRegionRestV1;
import org.onap.so.client.policy.JettisonStyleMapperProvider;
import org.onap.so.logging.jaxrs.filter.SOAuditLogContainerFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;


@Configuration
public class CXFConfiguration {
    @Autowired
    private CloudRegionRestV1 cloudRegionRestV1;
    @Autowired
    private JettisonStyleMapperProvider jettisonStyleObjectMapper;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private SOAuditLogContainerFilter soAuditLogContainerFilter;


    @Bean(name = Bus.DEFAULT_BUS_ID)
    public SpringBus springBus() {
        return new SpringBus();
    }

    @Bean
    public ServletRegistrationBean soapDispatcherServlet() {
        ServletRegistrationBean servletRegistrationBean =
                new ServletRegistrationBean((Servlet) new CXFServlet(), "/services/*");
        servletRegistrationBean.setName("services");
        return servletRegistrationBean;
    }

    // Uses normal Jackson marshalling semantics
    @Bean
    public Server rsServerApi() {
        JAXRSServerFactoryBean endpoint = new JAXRSServerFactoryBean();
        endpoint.setBus(springBus());
        endpoint.setServiceBeans(Arrays.<Object>asList(cloudRegionRestV1));
        endpoint.setAddress("/api");
        endpoint.setFeatures(Arrays.asList(new LoggingFeature()));
        endpoint.setProviders(Arrays.asList(new JacksonJsonProvider(mapper), soAuditLogContainerFilter));
        return endpoint.create();
    }

    @Bean
    public OpenApiFeature createSwaggerFeature() {
        OpenApiFeature openApiFeature = new OpenApiFeature();
        openApiFeature.setPrettyPrint(true);
        openApiFeature.setTitle("SO Orchestration Application");
        openApiFeature.setContactName("The ONAP SO team");
        openApiFeature.setDescription("This project is the SO Orchestration Engine");
        openApiFeature.setVersion("1.0.0");
        openApiFeature.setResourcePackages(
                new HashSet<String>(Arrays.asList("org.onap.so.adapters.network,org.onap.so.adapters.vnf")));
        openApiFeature.setScan(true);
        return openApiFeature;
    }
}
