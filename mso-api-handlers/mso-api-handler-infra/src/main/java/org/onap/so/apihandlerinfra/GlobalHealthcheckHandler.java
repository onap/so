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


import java.net.URI;
import java.util.Collections;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import java.util.UUID;
import org.apache.http.HttpStatus;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


@Component
@Path("/globalhealthcheck")
@Api(value="/globalhealthcheck",description="APIH Infra Global Health Check")
public class GlobalHealthcheckHandler {
    private static MsoLogger msoLogger = MsoLogger.getMsoLogger (MsoLogger.Catalog.APIH, GlobalHealthcheckHandler.class);
    private static final String CONTEXTPATH_PROPERTY = "management.context-path";    
    private static final String PROPERTY_DOMAIN	 = "mso.health.endpoints";    
    private static final String CATALOGDB_PROPERTY = PROPERTY_DOMAIN+".catalogdb";
	private static final String REQUESTDB_PROPERTY = PROPERTY_DOMAIN+".requestdb";
	private static final String SDNC_PROPERTY = PROPERTY_DOMAIN+".sdnc";
	private static final String OPENSTACK_PROPERTY = PROPERTY_DOMAIN+".openstack";
	private static final String BPMN_PROPERTY = PROPERTY_DOMAIN+".bpmn";
	private static final String ASDC_PROPERTY = PROPERTY_DOMAIN+".asdc";
	private static final String REQUESTDBATTSVC_PROPERTY = PROPERTY_DOMAIN+".requestdbattsvc";
	private static final String DEFAULT_PROPERTY_VALUE = "";
	
    // e.g. /manage
    private String actuatorContextPath;
	private String endpointCatalogdb;
	private String endpointRequestdb;
	private String endpointSdnc;
	private String endpointOpenstack;
	private String endpointBpmn;
	private String endpointAsdc;
	private String endpointRequestdbAttsvc;
	
	@Autowired
	private Environment env;

	@Autowired
	private RestTemplate restTemplate;
	private final String health = "/health";

	
	@PostConstruct
	protected void init() {
		actuatorContextPath  = env.getProperty(CONTEXTPATH_PROPERTY, String.class, DEFAULT_PROPERTY_VALUE);
		endpointCatalogdb  = env.getProperty(CATALOGDB_PROPERTY, String.class, DEFAULT_PROPERTY_VALUE);
		endpointRequestdb  = env.getProperty(REQUESTDB_PROPERTY, String.class, DEFAULT_PROPERTY_VALUE);
		endpointSdnc  = env.getProperty(SDNC_PROPERTY, String.class, DEFAULT_PROPERTY_VALUE);		
		endpointOpenstack  = env.getProperty(OPENSTACK_PROPERTY, String.class, DEFAULT_PROPERTY_VALUE);		
		endpointBpmn  = env.getProperty(BPMN_PROPERTY, String.class, DEFAULT_PROPERTY_VALUE);		
		endpointAsdc  = env.getProperty(ASDC_PROPERTY, String.class, DEFAULT_PROPERTY_VALUE);		
		endpointRequestdbAttsvc  = env.getProperty(REQUESTDBATTSVC_PROPERTY, String.class, DEFAULT_PROPERTY_VALUE);		
	}
	
    @GET
    @Produces("application/json")
	@ApiOperation(value="Performing global health check",response=Response.class)
    @Transactional
    public Response globalHealthcheck (@DefaultValue("true") @QueryParam("enableBpmn") boolean enableBpmn, 
    									@Context ContainerRequestContext requestContext) {
    	Response HEALTH_CHECK_RESPONSE = null;
        // Build internal response object
        HealthcheckResponse rsp = new HealthcheckResponse();
    	
    	try{
            long startTime = System.currentTimeMillis ();
            MsoLogger.setServiceName ("GlobalHealthcheck");
            // Generated RequestId
            String requestId = requestContext.getProperty("requestId").toString();
            MsoLogger.setLogContext(requestId, null);
            msoLogger.info(MessageEnum.APIH_GENERATED_REQUEST_ID, requestId, "", "");
            
            // set APIH status, this is the main entry point
            rsp.setApih(HealthcheckStatus.UP.toString());
            // set BPMN
            rsp.setBpmn(querySubsystemHealth(MsoSubsystems.BPMN));
            // set SDNCAdapter
            rsp.setSdncAdapter(querySubsystemHealth(MsoSubsystems.SDNC));        
            // set ASDCController
            rsp.setAsdcController(querySubsystemHealth(MsoSubsystems.ASDC));        
            // set CatalogDbAdapter
            rsp.setCatalogdbAdapter(querySubsystemHealth(MsoSubsystems.CATALOGDB));    
            // set RequestDbAdapter
            rsp.setRequestdbAdapter(querySubsystemHealth(MsoSubsystems.REQUESTDB));        
            // set OpenStackAdapter
            rsp.setOpenstackAdapter(querySubsystemHealth(MsoSubsystems.OPENSTACK));        
            // set RequestDbAdapterAttSvc
            rsp.setRequestdbAdapterAttsvc(querySubsystemHealth(MsoSubsystems.REQUESTDBATT));
            // set Message
            rsp.setMessage(String.format("HttpStatus: %s", HttpStatus.SC_OK));
            msoLogger.info(rsp.toString(), "", "");

            HEALTH_CHECK_RESPONSE = Response.status (HttpStatus.SC_OK)
                    .entity (rsp)
                    .build ();
            
    	}catch (Exception ex){
    		msoLogger.error(ex);
    		rsp.setMessage(ex.getMessage());
            HEALTH_CHECK_RESPONSE = Response.status (HttpStatus.SC_INTERNAL_SERVER_ERROR)
                    .entity (rsp)
                    .build ();
    	}
        
        return HEALTH_CHECK_RESPONSE;
    }
    
    protected HttpEntity<String> buildHttpEntityForRequest(){
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON)); 
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);       
        return entity;
    }
    
    protected String querySubsystemHealth(MsoSubsystems subsystem){
    	try{
            // get port number for the subsystem
    		String ept = getEndpointUrlForSubsystemEnum(subsystem);
    		
        	// build final endpoint url
			UriBuilder builder = UriBuilder.fromPath(ept).path(actuatorContextPath).path(health);
			URI uri = builder.build();
        	msoLogger.info("Calculated URL: "+uri.toString(), "", "");        	
            
            ResponseEntity<SubsystemHealthcheckResponse> result = 
            		restTemplate.exchange(uri, HttpMethod.GET, buildHttpEntityForRequest(), SubsystemHealthcheckResponse.class);
            
    		return processResponseFromSubsystem(result,subsystem);
    		
    	}catch(Exception ex){
			msoLogger.error("Exception occured in GlobalHealthcheckHandler.querySubsystemHealth() "+ ex);
    		return HealthcheckStatus.DOWN.toString();
    	}
    }
	protected String processResponseFromSubsystem(ResponseEntity<SubsystemHealthcheckResponse> result, MsoSubsystems subsystem){
        if(result == null || result.getStatusCodeValue() != HttpStatus.SC_OK){
        	msoLogger.error(String.format("Globalhealthcheck: checking subsystem: %s failed ! result object is: %s", 
        			subsystem,
        			result == null? "NULL": result));
        	return HealthcheckStatus.DOWN.toString();
        }
        
        SubsystemHealthcheckResponse body = result.getBody();

		String status = body.getStatus();
		if("UP".equalsIgnoreCase(status)){
			return HealthcheckStatus.UP.toString();
		}else{
			msoLogger.error(subsystem + ", query health endpoint did not return UP status!");
			return HealthcheckStatus.DOWN.toString();
		}
	}
    
    
    protected String getEndpointUrlForSubsystemEnum(MsoSubsystems subsystem){
    	switch (subsystem){
    	case SDNC:
    		return this.endpointSdnc;
    	case ASDC:
    		return this.endpointAsdc;
    	case BPMN:
    		return this.endpointBpmn;
    	case CATALOGDB:
    		return this.endpointCatalogdb;
    	case OPENSTACK:
    		return this.endpointOpenstack;
    	case REQUESTDB:
    		return this.endpointRequestdb;
    	case REQUESTDBATT:
    		return this.endpointRequestdbAttsvc;
    	default:
    		return "";
    	}     	
    }
}
