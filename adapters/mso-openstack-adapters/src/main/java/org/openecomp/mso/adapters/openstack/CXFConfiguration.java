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

package org.openecomp.mso.adapters.openstack;

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
import org.openecomp.mso.adapters.network.MsoNetworkAdapterAsyncImpl;
import org.openecomp.mso.adapters.network.MsoNetworkAdapterImpl;
import org.openecomp.mso.adapters.network.NetworkAdapterRest;
import org.openecomp.mso.adapters.tenant.MsoTenantAdapterImpl;
import org.openecomp.mso.adapters.tenant.TenantAdapterRest;
import org.openecomp.mso.adapters.vnf.MsoVnfAdapterAsyncImpl;
import org.openecomp.mso.adapters.vnf.MsoVnfAdapterImpl;
import org.openecomp.mso.adapters.vnf.MsoVnfCloudifyAdapterImpl;
import org.openecomp.mso.adapters.vnf.VnfAdapterRest;
import org.openecomp.mso.adapters.vnf.VnfAdapterRestV2;
import org.openecomp.mso.adapters.vnf.VolumeAdapterRest;
import org.openecomp.mso.adapters.vnf.VolumeAdapterRestV2;
import org.openecomp.mso.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;


@Configuration
public class CXFConfiguration {
	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA, CXFConfiguration.class);
	
	@Autowired
	private NetworkAdapterRest networkAdapterRest;
	@Autowired
	private TenantAdapterRest tenantAdapterRest;
	@Autowired
	private VnfAdapterRest vnfAdapterRest;
	@Autowired
	private VnfAdapterRestV2 vnfAdapterRestV2;
	@Autowired
	private VolumeAdapterRest volumeAdapterRest;
	@Autowired
	private VolumeAdapterRestV2 volumeAdapterRestV2;
	@Autowired
	private MsoNetworkAdapterImpl networkAdapterImpl;
	@Autowired
	private MsoNetworkAdapterAsyncImpl networkAdapterAsyncImpl;
	@Autowired
	private MsoTenantAdapterImpl tenantAdapterImpl;
	@Autowired
	private MsoVnfAdapterImpl vnfAdapterImpl;
	@Autowired
	private MsoVnfAdapterAsyncImpl vnfAdapterAsyncImpl;
	@Autowired
	private MsoVnfCloudifyAdapterImpl vnfCloudifyAdapterImpl;
	@Autowired
	private ObjectMapper mapper; 	
	
	
    @Bean(name=Bus.DEFAULT_BUS_ID)
    public SpringBus springBus() {      
        return new SpringBus();
    }	
	
	@Bean
	public ServletRegistrationBean SoapDispatcherServlet() {
		ServletRegistrationBean servletRegistrationBean = 
					new ServletRegistrationBean(new CXFServlet(), "/services/*");
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
        return endpoint;
    }	
	
    @Bean
    public Endpoint networkAdapterAsyncEndpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), networkAdapterAsyncImpl);
        endpoint.publish("/NetworkAdapterAsync");
        endpoint.setWsdlLocation("NetworkAdapterAsync.wsdl");
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
        return endpoint;
    }
    
    /*
     * vnfAdapterEndpoint
     * VnfAsyncAdapterEndpoint
     * VnfCloudAdapterEndpoint
     */
    @Bean
    public Endpoint vnfAdapterEndpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), vnfAdapterImpl);
        endpoint.publish("/VnfAdapter");
        endpoint.setWsdlLocation("VnfAdapter.wsdl");
        return endpoint;
    }
    
    @Bean
    public Endpoint VnfAsyncAdapterEndpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), vnfAdapterAsyncImpl);
        endpoint.publish("/VnfAsyncAdapter");
        endpoint.setWsdlLocation("VnfAsyncAdapter.wsdl");
        return endpoint;
    }
    
    @Bean
    public Endpoint VnfCloudAdapterEndpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), vnfCloudifyAdapterImpl);
        endpoint.publish("/VnfCloudifyAdapterImpl");
        endpoint.setWsdlLocation("VnfCloudifyAdapterImpl.wsdl");
        return endpoint;
    }
    
    @Bean
    public Server rsServer() {
        JAXRSServerFactoryBean endpoint = new JAXRSServerFactoryBean();
        endpoint.setBus(springBus());
        endpoint.setServiceBeans(Arrays.<Object>asList(networkAdapterRest, 
        							tenantAdapterRest,
        							vnfAdapterRest,
        							vnfAdapterRestV2,
        							volumeAdapterRest,
        							volumeAdapterRestV2));
        endpoint.setAddress("/rest");
        endpoint.setFeatures(Arrays.asList(createSwaggerFeature(), new LoggingFeature()));
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        endpoint.setProvider(new JacksonJsonProvider(mapper));
        return endpoint.create();
    }	
	
	
    @Bean
    public Swagger2Feature createSwaggerFeature() {
    	Swagger2Feature swagger2Feature= new Swagger2Feature();
        swagger2Feature.setPrettyPrint(true);
        swagger2Feature.setTitle("SO Orchestration Application");
        swagger2Feature.setContact("The ONAP SO team");
        swagger2Feature.setDescription("This project is the SO Orchestration Engine");
        swagger2Feature.setVersion("1.0.0");
        swagger2Feature.setResourcePackage("org.openecomp.mso.adapters.network,org.openecomp.mso.adapters.tenant,org.openecomp.mso.adapters.vnf");
        swagger2Feature.setScan(true);
        return swagger2Feature;
    }	
}
