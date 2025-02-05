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
package org.onap.so.adapters.sdnc;

import java.util.Arrays;
import java.util.HashSet;
<<<<<<< HEAD
import javax.xml.ws.Endpoint;
||||||| parent of 30f7ad6a1 (Runtime issues of jdk17)
import javax.xml.ws.Endpoint;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import jakarta.servlet.Servlet;
=======
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import jakarta.xml.ws.Endpoint;
>>>>>>> 30f7ad6a1 (Runtime issues of jdk17)
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.openapi.OpenApiFeature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.onap.so.adapters.sdnc.sdncrest.SNIROResponse;
import org.onap.so.logging.cxf.interceptor.SOAPLoggingInInterceptor;
import org.onap.so.logging.cxf.interceptor.SOAPLoggingOutInterceptor;
import org.onap.so.logging.jaxrs.filter.SOAuditLogContainerFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
<<<<<<< HEAD
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
||||||| parent of 30f7ad6a1 (Runtime issues of jdk17)
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

=======
import org.springframework.context.annotation.Lazy;
>>>>>>> 30f7ad6a1 (Runtime issues of jdk17)


@Configuration("CXFConfiguration")
public class CXFConfiguration {

    JAXRSServerFactoryBean endpoint;

    @Autowired
    @Lazy
    private Bus bus;

    @Autowired
    @Lazy
    private SOAuditLogContainerFilter soAuditLogContainerFilter;

    @Autowired
    private SDNCAdapterPortType sdncAdapterPortImpl;

    @Autowired
    private SNIROResponse sniroResponse;

    @Autowired
    private ObjectMapper mapper;

    public SOAuditLogContainerFilter soAuditLogContainerFilter() {
        return new SOAuditLogContainerFilter();
    }

    @Bean
    public Bus cxfBus() {
        return new SpringBus();
    }

    @Bean
    public Server rsServer() {
        endpoint = new JAXRSServerFactoryBean();
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        endpoint.setBus(bus);
        endpoint.setServiceBeans(Arrays.<Object>asList(sniroResponse));
        endpoint.setAddress("/rest");
        endpoint.setFeatures(Arrays.asList(createSwaggerFeature(), new LoggingFeature()));
        endpoint.setProviders(Arrays.asList(new JacksonJsonProvider(mapper), soAuditLogContainerFilter));
        return endpoint.create();
    }

    @Bean
<<<<<<< HEAD
    public ServletRegistrationBean cxfServlet() {
        return new ServletRegistrationBean(new CXFServlet(), "/adapters/*");
||||||| parent of 30f7ad6a1 (Runtime issues of jdk17)
    public ServletRegistrationBean cxfServlet() {
        return new ServletRegistrationBean((Servlet) new CXFServlet(), "/adapters/*");
=======
    public ServletRegistrationBean<CXFServlet> cxfServlet() {
        ServletRegistrationBean<CXFServlet> registrationBean =
                new ServletRegistrationBean<>(new CXFServlet(), "/adapters/*");
        return registrationBean;
>>>>>>> 30f7ad6a1 (Runtime issues of jdk17)
    }

    @Bean
    public Endpoint sndcAdapter() {
        EndpointImpl wsdlEndpoint = new EndpointImpl(bus, sdncAdapterPortImpl);
        wsdlEndpoint.getInInterceptors().add(new SOAPLoggingInInterceptor());
        wsdlEndpoint.getOutInterceptors().add(new SOAPLoggingOutInterceptor());
        wsdlEndpoint.getOutFaultInterceptors().add(new SOAPLoggingOutInterceptor());
        wsdlEndpoint.publish("/SDNCAdapter");
        return wsdlEndpoint;
    }


    @Bean
    public OpenApiFeature createSwaggerFeature() {
        OpenApiFeature openApiFeature = new OpenApiFeature();
        openApiFeature.setPrettyPrint(true);
        openApiFeature.setTitle("SO Orchestration Application");
        openApiFeature.setContactName("The ONAP SO team");
        openApiFeature.setDescription("This project is the SO Orchestration Engine");
        openApiFeature.setVersion("1.0.0");
        openApiFeature.setResourcePackages(new HashSet<String>(Arrays.asList("org.onap.so.adapters.sdnc")));
        openApiFeature.setScan(true);
        return openApiFeature;
    }
}
