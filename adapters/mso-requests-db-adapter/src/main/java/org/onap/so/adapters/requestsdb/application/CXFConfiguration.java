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

package org.onap.so.adapters.requestsdb.application;

import java.util.Arrays;
import java.util.HashSet;
import jakarta.servlet.Servlet;
import org.apache.cxf.Bus;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.openapi.OpenApiFeature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.onap.so.adapters.requestsdb.MsoRequestsDbAdapter;
import org.onap.so.logging.cxf.interceptor.SOAPLoggingInInterceptor;
import org.onap.so.logging.cxf.interceptor.SOAPLoggingOutInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CXFConfiguration {

    @Autowired
    private Bus bus;

    @Autowired
    private MsoRequestsDbAdapter requestDbAdapterImpl;

    @Bean
    public ServletRegistrationBean cxfServlet() {

        return new ServletRegistrationBean((Servlet) new CXFServlet(), "/services/*");
    }

    @Bean
    public EndpointImpl requestEndpointk() {
        EndpointImpl endpoint = new EndpointImpl(bus, requestDbAdapterImpl);
        endpoint.publish("/RequestsDbAdapter");
        LoggingFeature logFeature = new LoggingFeature();
        logFeature.setPrettyLogging(true);
        logFeature.initialize(bus);
        endpoint.getFeatures().add(logFeature);
        endpoint.getInInterceptors().add(new SOAPLoggingInInterceptor());
        endpoint.getOutInterceptors().add(new SOAPLoggingOutInterceptor());
        endpoint.getOutFaultInterceptors().add(new SOAPLoggingOutInterceptor());
        return endpoint;
    }

    @Bean
    public OpenApiFeature createSwaggerFeature() {
        OpenApiFeature openApiFeature = new OpenApiFeature();
        openApiFeature.setPrettyPrint(true);
        openApiFeature.setTitle("SO Request Adapter");
        openApiFeature.setContactName("The ONAP SO team");
        openApiFeature.setDescription("This project is the SO Orchestration Engine");
        openApiFeature.setVersion("1.0.0");
        openApiFeature.setResourcePackages(new HashSet<String>(Arrays.asList("org.onap.so.adapters.requestdb")));
        openApiFeature.setScan(true);
        return openApiFeature;
    }

}
