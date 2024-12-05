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

package org.onap.so.bpmn.infrastructure;

import java.util.Arrays;
import java.util.HashSet;
import jakarta.servlet.Servlet;
import jakarta.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.openapi.OpenApiFeature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.onap.so.bpmn.common.adapter.sdnc.SDNCCallbackAdapterPortType;
import org.onap.so.bpmn.common.adapter.vnf.VnfAdapterNotify;
import org.onap.so.bpmn.common.workflow.service.WorkflowAsyncResource;
import org.onap.so.bpmn.common.workflow.service.WorkflowMessageResource;
import org.onap.so.bpmn.common.workflow.service.WorkflowResource;
import org.onap.so.logging.cxf.interceptor.SOAPLoggingInInterceptor;
import org.onap.so.logging.cxf.interceptor.SOAPLoggingOutInterceptor;
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
    private Bus bus;

    @Autowired
    private WorkflowMessageResource wmr;

    @Autowired
    private WorkflowResource workflowResource;

    @Autowired
    private WorkflowAsyncResource workflowAsyncResource;

    @Autowired
    private SOAuditLogContainerFilter soAuditLogContainerFilter;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private SDNCCallbackAdapterPortType sdncAdapterCallbackServiceImpl;

    @Autowired
    private VnfAdapterNotify vnfAdapterNotifyServiceImpl;

    @Bean
    public ServletRegistrationBean cxfServlet() {
        return new ServletRegistrationBean((Servlet) new CXFServlet(), "/mso/*");
    }

    @Bean
    public Endpoint vnfAdapterCallback() {
        EndpointImpl endpoint = new EndpointImpl(bus, vnfAdapterNotifyServiceImpl);
        endpoint.publish("/VNFAdaptercallback");
        endpoint.getInInterceptors().add(new SOAPLoggingInInterceptor());
        endpoint.getOutInterceptors().add(new SOAPLoggingOutInterceptor());
        endpoint.getOutFaultInterceptors().add(new SOAPLoggingOutInterceptor());
        // return Endpoint.create(vnfAdapterNotifyServiceImpl);
        return endpoint;
    }

    @Bean
    public Endpoint sndcAdapterCallback() {
        EndpointImpl endpoint = new EndpointImpl(bus, sdncAdapterCallbackServiceImpl);
        endpoint.publish("/SDNCAdapterCallbackService");
        endpoint.getInInterceptors().add(new SOAPLoggingInInterceptor());
        endpoint.getOutInterceptors().add(new SOAPLoggingOutInterceptor());
        endpoint.getOutFaultInterceptors().add(new SOAPLoggingOutInterceptor());
        // return Endpoint.create(endpoint);
        return endpoint;
    }

    @Bean
    public Server rsServer() {
        JAXRSServerFactoryBean endpoint = new JAXRSServerFactoryBean();
        endpoint.setBus(bus);
        endpoint.setServiceBeans(Arrays.<Object>asList(wmr, workflowResource, workflowAsyncResource));
        endpoint.setAddress("/");
        endpoint.setFeatures(Arrays.asList(createSwaggerFeature(), new LoggingFeature()));
        endpoint.setProviders(Arrays.asList(new JacksonJsonProvider(mapper), soAuditLogContainerFilter));

        return endpoint.create();
    }

    @Bean
    public OpenApiFeature createSwaggerFeature() {
        OpenApiFeature swagger2Feature = new OpenApiFeature();
        swagger2Feature.setPrettyPrint(true);
        swagger2Feature.setTitle("SO Orchestration Application");
        swagger2Feature.setContactName("The ONAP SO team");
        swagger2Feature.setDescription("This project is the SO Orchestration Engine");
        swagger2Feature.setVersion("1.0.0");
        swagger2Feature
                .setResourcePackages(new HashSet<String>(Arrays.asList("org.onap.so.bpmn.common.workflow.service")));
        swagger2Feature.setScan(true);
        return swagger2Feature;
    }
}
