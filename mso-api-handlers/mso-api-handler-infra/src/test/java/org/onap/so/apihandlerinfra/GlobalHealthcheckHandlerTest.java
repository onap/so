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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.springframework.test.util.ReflectionTestUtils;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


public class GlobalHealthcheckHandlerTest {
    @Mock
    RestTemplate restTemplate;

    @Mock
    ContainerRequestContext requestContext;
    
    @InjectMocks
    @Spy
	GlobalHealthcheckHandler globalhealth;
	
    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule(); 
    
    @Test
    public void testQuerySubsystemHealthNullResult(){
    	ReflectionTestUtils.setField(globalhealth, "actuatorContextPath", "/manage");
    	ReflectionTestUtils.setField(globalhealth, "endpointBpmn", "http://localhost:8080");
    	
		Mockito.when(restTemplate.exchange(ArgumentMatchers.any(URI.class), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>> any(), 
				ArgumentMatchers.<Class<Object>> any())).thenReturn(null);
		
		String result = globalhealth.querySubsystemHealth(MsoSubsystems.BPMN);
		System.out.println(result);
		assertEquals(HealthcheckStatus.DOWN.toString(),result);		
    }
    
    @Test
    public void testQuerySubsystemHealthNotNullResult(){
    	ReflectionTestUtils.setField(globalhealth, "actuatorContextPath", "/manage");
    	ReflectionTestUtils.setField(globalhealth, "endpointAsdc", "http://localhost:8080");
    	
		SubsystemHealthcheckResponse subSystemResponse = new SubsystemHealthcheckResponse();
		subSystemResponse.setStatus("UP");
		ResponseEntity<Object> r = new ResponseEntity<>(subSystemResponse,HttpStatus.OK);
		
		Mockito.when(restTemplate.exchange(ArgumentMatchers.any(URI.class), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>> any(), 
				ArgumentMatchers.<Class<Object>> any())).thenReturn(r);
		
		String result = globalhealth.querySubsystemHealth(MsoSubsystems.ASDC);
		System.out.println(result);
		assertEquals(HealthcheckStatus.UP.toString(),result);	    	
    }
    
    private Response globalHealthcheck (String status){
    	ReflectionTestUtils.setField(globalhealth, "actuatorContextPath", "/manage");
    	ReflectionTestUtils.setField(globalhealth, "endpointAsdc", "http://localhost:8080");
    	ReflectionTestUtils.setField(globalhealth, "endpointSdnc", "http://localhost:8081");
    	ReflectionTestUtils.setField(globalhealth, "endpointBpmn", "http://localhost:8082");
    	ReflectionTestUtils.setField(globalhealth, "endpointCatalogdb", "http://localhost:8083");
    	ReflectionTestUtils.setField(globalhealth, "endpointOpenstack", "http://localhost:8084");
    	ReflectionTestUtils.setField(globalhealth, "endpointRequestdb", "http://localhost:8085");
    	ReflectionTestUtils.setField(globalhealth, "endpointRequestdbAttsvc", "http://localhost:8086");

		SubsystemHealthcheckResponse subSystemResponse = new SubsystemHealthcheckResponse();

		subSystemResponse.setStatus(status);
		ResponseEntity<Object> r = new ResponseEntity<>(subSystemResponse,HttpStatus.OK);		
		Mockito.when(restTemplate.exchange(ArgumentMatchers.any(URI.class), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>> any(), 
				ArgumentMatchers.<Class<Object>> any())).thenReturn(r);
		
		Mockito.when(requestContext.getProperty(anyString())).thenReturn("1234567890");
		Response response = globalhealth.globalHealthcheck(true, requestContext);
		
		return response;
    }
    
	@Test
	public void globalHealthcheckAllUPTest() throws JSONException {
		Response response = globalHealthcheck("UP");
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatus());
		HealthcheckResponse root;
		root = (HealthcheckResponse) response.getEntity();		
		String apistatus = root.getApih();
		assertTrue(apistatus.equalsIgnoreCase(HealthcheckStatus.UP.toString()));
		
		String bpmnstatus = root.getBpmn();
		assertTrue(bpmnstatus.equalsIgnoreCase(HealthcheckStatus.UP.toString()));
		
		String sdncstatus = root.getSdncAdapter();
		assertTrue(sdncstatus.equalsIgnoreCase(HealthcheckStatus.UP.toString()));
		
		String asdcstatus = root.getAsdcController();
		assertTrue(asdcstatus.equalsIgnoreCase(HealthcheckStatus.UP.toString()));
		
		String catastatus = root.getCatalogdbAdapter();
		assertTrue(catastatus.equalsIgnoreCase(HealthcheckStatus.UP.toString()));			
		
		String reqdbstatus = root.getRequestdbAdapter();
		assertTrue(reqdbstatus.equalsIgnoreCase(HealthcheckStatus.UP.toString()));			
		
		String openstatus = root.getOpenstackAdapter();
		assertTrue(openstatus.equalsIgnoreCase(HealthcheckStatus.UP.toString()));			

		String reqdbattstatus = root.getRequestdbAdapterAttsvc();
		assertTrue(reqdbattstatus.equalsIgnoreCase(HealthcheckStatus.UP.toString()));			
	}
	
	@Test
	public void globalHealthcheckAllDOWNTest() throws JSONException {
		Response response = globalHealthcheck("DOWN");
		assertEquals(Response.Status.OK.getStatusCode(),response.getStatus());
		HealthcheckResponse root;
		root = (HealthcheckResponse) response.getEntity();		
		String apistatus = root.getApih();
		assertTrue(apistatus.equalsIgnoreCase(HealthcheckStatus.UP.toString()));
		
		String bpmnstatus = root.getBpmn();
		assertTrue(bpmnstatus.equalsIgnoreCase(HealthcheckStatus.DOWN.toString()));
		
		String sdncstatus = root.getSdncAdapter();
		assertTrue(sdncstatus.equalsIgnoreCase(HealthcheckStatus.DOWN.toString()));
		
		String asdcstatus = root.getAsdcController();
		assertTrue(asdcstatus.equalsIgnoreCase(HealthcheckStatus.DOWN.toString()));
		
		String catastatus = root.getCatalogdbAdapter();
		assertTrue(catastatus.equalsIgnoreCase(HealthcheckStatus.DOWN.toString()));			
		
		String reqdbstatus = root.getRequestdbAdapter();
		assertTrue(reqdbstatus.equalsIgnoreCase(HealthcheckStatus.DOWN.toString()));			
		
		String openstatus = root.getOpenstackAdapter();
		assertTrue(openstatus.equalsIgnoreCase(HealthcheckStatus.DOWN.toString()));			

		String reqdbattstatus = root.getRequestdbAdapterAttsvc();
		assertTrue(reqdbattstatus.equalsIgnoreCase(HealthcheckStatus.DOWN.toString()));			
	}

	@Test
	public void buildHttpEntityForRequestTest(){
		HttpEntity<String> he = globalhealth.buildHttpEntityForRequest();
		assertEquals (MediaType.APPLICATION_JSON,he.getHeaders().getAccept().get(0));
		assertEquals (MediaType.APPLICATION_JSON,he.getHeaders().getContentType());
	}
	
	@Test
	public void getEndpointUrlForSubsystemEnumTest(){
    	ReflectionTestUtils.setField(globalhealth, "actuatorContextPath", "/manage");
    	ReflectionTestUtils.setField(globalhealth, "endpointAsdc", "http://localhost:8080");
    	ReflectionTestUtils.setField(globalhealth, "endpointSdnc", "http://localhost:8081");
    	ReflectionTestUtils.setField(globalhealth, "endpointBpmn", "http://localhost:8082");
    	ReflectionTestUtils.setField(globalhealth, "endpointCatalogdb", "http://localhost:8083");
    	ReflectionTestUtils.setField(globalhealth, "endpointOpenstack", "http://localhost:8084");
    	ReflectionTestUtils.setField(globalhealth, "endpointRequestdb", "http://localhost:8085");
    	ReflectionTestUtils.setField(globalhealth, "endpointRequestdbAttsvc", "http://localhost:8086");
    	
		String result = globalhealth.getEndpointUrlForSubsystemEnum(MsoSubsystems.ASDC);
		assertEquals("http://localhost:8080", result);
		result = globalhealth.getEndpointUrlForSubsystemEnum(MsoSubsystems.SDNC);
		assertEquals("http://localhost:8081", result);
		result = globalhealth.getEndpointUrlForSubsystemEnum(MsoSubsystems.BPMN);
		assertEquals("http://localhost:8082", result);
		result = globalhealth.getEndpointUrlForSubsystemEnum(MsoSubsystems.CATALOGDB);
		assertEquals("http://localhost:8083", result);
		result = globalhealth.getEndpointUrlForSubsystemEnum(MsoSubsystems.OPENSTACK);
		assertEquals("http://localhost:8084", result);
		result = globalhealth.getEndpointUrlForSubsystemEnum(MsoSubsystems.REQUESTDB);
		assertEquals("http://localhost:8085", result);
		result = globalhealth.getEndpointUrlForSubsystemEnum(MsoSubsystems.REQUESTDBATT);
		assertEquals("http://localhost:8086", result);
	}
	
	@Test
	public void processResponseFromSubsystemTest(){
		SubsystemHealthcheckResponse subSystemResponse = new SubsystemHealthcheckResponse();
		subSystemResponse.setStatus("UP");
		ResponseEntity<SubsystemHealthcheckResponse> r = new ResponseEntity<>(subSystemResponse,HttpStatus.OK);
		String result = globalhealth.processResponseFromSubsystem(r,MsoSubsystems.BPMN);
		assertEquals("UP",result);
	}
	
}