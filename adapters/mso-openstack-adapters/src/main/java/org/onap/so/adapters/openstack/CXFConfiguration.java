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
import javax.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.swagger.Swagger2Feature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.onap.so.adapters.cloudregion.CloudRegionRestV1;
import org.onap.so.adapters.network.MsoNetworkAdapterImpl;
import org.onap.so.adapters.tenant.MsoTenantAdapterImpl;
import org.onap.so.adapters.tenant.TenantAdapterRest;
import org.onap.so.adapters.vnf.MsoVnfAdapterAsyncImpl;
import org.onap.so.adapters.vnf.MsoVnfAdapterImpl;
import org.onap.so.client.policy.JettisonStyleMapperProvider;
import org.onap.so.logging.cxf.interceptor.SOAPLoggingInInterceptor;
import org.onap.so.logging.cxf.interceptor.SOAPLoggingOutInterceptor;
import org.onap.so.logging.jaxrs.filter.SOAuditLogContainerFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;


@Configuration
public class CXFConfiguration {
    @Autowired
    private TenantAdapterRest tenantAdapterRest;
    @Autowired
    private MsoNetworkAdapterImpl networkAdapterImpl;
    @Autowired
    private MsoTenantAdapterImpl tenantAdapterImpl;
    @Autowired
    private MsoVnfAdapterImpl vnfAdapterImpl;
    @Autowired
    private MsoVnfAdapterAsyncImpl vnfAdapterAsyncImpl;
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
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new CXFServlet(), "/services/*");
        servletRegistrationBean.setName("services");
        return servletRegistrationBean;
    }

    /*
     * network adapter endpoint
     */
    @Bean
    public Endpoint networkAdapterEndpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), networkAdapterImpl);
        endpoint.publish("/NetworkAdapter");
        endpoint.setWsdlLocation("NetworkAdapter.wsdl");
        endpoint.getInInterceptors().add(new SOAPLoggingInInterceptor());
        endpoint.getOutInterceptors().add(new SOAPLoggingOutInterceptor());
        endpoint.getOutFaultInterceptors().add(new SOAPLoggingOutInterceptor());
        return endpoint;
    }

    /*
     * tenant adapter endpoint
     */
    @Bean
    public Endpoint tenantAdapterEndpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), tenantAdapterImpl);
        endpoint.publish("/TenantAdapter");
        endpoint.setWsdlLocation("TenantAdapter.wsdl");
        endpoint.getInInterceptors().add(new SOAPLoggingInInterceptor());
        endpoint.getOutInterceptors().add(new SOAPLoggingOutInterceptor());
        endpoint.getOutFaultInterceptors().add(new SOAPLoggingOutInterceptor());
        return endpoint;
    }

    /*
     * vnfAdapterEndpoint VnfAsyncAdapterEndpoint VnfCloudAdapterEndpoint
     */
    @Bean
    public Endpoint vnfAdapterEndpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), vnfAdapterImpl);
        endpoint.publish("/VnfAdapter");
        endpoint.setWsdlLocation("VnfAdapter.wsdl");
        endpoint.getInInterceptors().add(new SOAPLoggingInInterceptor());
        endpoint.getOutInterceptors().add(new SOAPLoggingOutInterceptor());
        endpoint.getOutFaultInterceptors().add(new SOAPLoggingOutInterceptor());
        return endpoint;
    }

    @Bean
    public Endpoint vnfAsyncAdapterEndpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), vnfAdapterAsyncImpl);
        endpoint.publish("/VnfAsyncAdapter");
        endpoint.setWsdlLocation("VnfAsyncAdapter.wsdl");
        endpoint.getInInterceptors().add(new SOAPLoggingInInterceptor());
        endpoint.getOutInterceptors().add(new SOAPLoggingOutInterceptor());
        endpoint.getOutFaultInterceptors().add(new SOAPLoggingOutInterceptor());
        return endpoint;
    }

    // Uses Jettson Style marshalling semantics
    @Bean
    public Server rsServer() {
        JAXRSServerFactoryBean endpoint = new JAXRSServerFactoryBean();
        endpoint.setBus(springBus());
        endpoint.setServiceBeans(Arrays.<Object>asList(tenantAdapterRest));
        endpoint.setAddress("/rest");
        endpoint.setFeatures(Arrays.asList(createSwaggerFeature(), new LoggingFeature()));
        endpoint.setProviders(Arrays.asList(new JacksonJsonProvider(jettisonStyleObjectMapper.getMapper()),
                soAuditLogContainerFilter));
        return endpoint.create();
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
    public Swagger2Feature createSwaggerFeature() {
        Swagger2Feature swagger2Feature = new Swagger2Feature();
        swagger2Feature.setPrettyPrint(true);
        swagger2Feature.setTitle("SO Orchestration Application");
        swagger2Feature.setContact("The ONAP SO team");
        swagger2Feature.setDescription("This project is the SO Orchestration Engine");
        swagger2Feature.setVersion("1.0.0");
        swagger2Feature.setResourcePackage(
                "org.onap.so.adapters.network,org.onap.so.adapters.tenant,org.onap.so.adapters.vnf");
        swagger2Feature.setScan(true);
        return swagger2Feature;
    }
}
