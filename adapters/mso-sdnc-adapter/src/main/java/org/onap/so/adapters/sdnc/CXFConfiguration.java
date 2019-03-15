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

import javax.xml.ws.Endpoint;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.swagger.Swagger2Feature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.onap.so.adapters.sdnc.sdncrest.SNIROResponse;
import org.onap.so.logging.cxf.interceptor.SOAPLoggingInInterceptor;
import org.onap.so.logging.cxf.interceptor.SOAPLoggingOutInterceptor;
import org.onap.so.logging.jaxrs.filter.JaxRsFilterLogging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;


@Configuration("CXFConfiguration")
public class CXFConfiguration {
	
	JAXRSServerFactoryBean endpoint;
	
    @Autowired
    private Bus bus;
    
	@Autowired
	private JaxRsFilterLogging jaxRsFilterLogging;
	
	@Autowired
	private SDNCAdapterPortType sdncAdapterPortImpl;
	
	@Autowired 
	private SNIROResponse sniroResponse;
    
	@Autowired
	private ObjectMapper mapper;
	
    @Bean
    public Server rsServer() {
        endpoint = new JAXRSServerFactoryBean();
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        endpoint.setBus(bus);
        endpoint.setServiceBeans(Arrays.<Object>asList(sniroResponse));
        endpoint.setAddress("/rest");
        endpoint.setFeatures(Arrays.asList(createSwaggerFeature(), new LoggingFeature()));
        endpoint.setProviders(Arrays.asList(new JacksonJsonProvider(mapper),jaxRsFilterLogging));
        return endpoint.create();
    }

    @Bean
    public ServletRegistrationBean cxfServlet() {
        return new ServletRegistrationBean(new CXFServlet(), "/adapters/*");
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
    public Swagger2Feature createSwaggerFeature() {
    	Swagger2Feature swagger2Feature= new Swagger2Feature();
    	swagger2Feature.setBasePath("/services/rest");
        swagger2Feature.setPrettyPrint(true);
        swagger2Feature.setTitle("SO Orchestration Application");
        swagger2Feature.setContact("The ONAP SO team");
        swagger2Feature.setDescription("This project is the SO Orchestration Engine");
        swagger2Feature.setVersion("1.0.0");
        swagger2Feature.setResourcePackage("org.onap.so.adapters.sdnc");
        swagger2Feature.setScan(true);
        return swagger2Feature;
    }
}
