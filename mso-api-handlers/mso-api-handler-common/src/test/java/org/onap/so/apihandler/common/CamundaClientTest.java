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

package org.onap.so.apihandler.common;


import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.so.apihandler.common.CamundaClient;
import org.onap.so.apihandler.common.CommonConstants;
import org.onap.so.apihandler.common.RequestClient;
import org.onap.so.apihandler.common.RequestClientFactory;
import org.springframework.mock.env.MockEnvironment;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;


/**
 * This class implements test methods of Camunda Beans.
 *
 *
 */
public class CamundaClientTest{



    @Mock
    private HttpClient mockHttpClient;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void tesCamundaPost() throws JsonGenerationException,
    JsonMappingException, IOException {


        String responseBody ="{\"links\":[{\"method\":\"GET\",\"href\":\"http://localhost:9080/engine-rest/process-instance/2047c658-37ae-11e5-9505-7a1020524153\",\"rel\":\"self\"}],\"id\":\"2047c658-37ae-11e5-9505-7a1020524153\",\"definitionId\":\"dummy:10:73298961-37ad-11e5-9505-7a1020524153\",\"businessKey\":null,\"caseInstanceId\":null,\"ended\":true,\"suspended\":false}";

        HttpResponse mockResponse = createResponse(200, responseBody);
        mockHttpClient = Mockito.mock(HttpClient.class);
        Mockito.when(mockHttpClient.execute(Mockito.any(HttpPost.class)))
        .thenReturn(mockResponse);

        String reqXML = "<xml>test</xml>";
        String orchestrationURI = "/engine-rest/process-definition/key/dummy/start";
        MockEnvironment environment = new MockEnvironment();
        
        environment.setProperty("mso.camundaUR", "yourValue1");
 
        
        RequestClientFactory reqClientFactory = new RequestClientFactory();
        reqClientFactory.setEnv(environment);
        RequestClient requestClient = reqClientFactory.getRequestClient(orchestrationURI);
        
        requestClient.setClient(mockHttpClient);
        HttpResponse response = requestClient.post(reqXML, "reqId", "timeout", "version", null, null);


        int statusCode = response.getStatusLine().getStatusCode();
        assertEquals(requestClient.getType(), CommonConstants.CAMUNDA);
        assertEquals(statusCode, HttpStatus.SC_OK);

  
        requestClient = reqClientFactory.getRequestClient(orchestrationURI);
        requestClient.setClient(mockHttpClient);
        response = requestClient.post(null, "reqId", null, null, null, null);
        assertEquals(requestClient.getType(), CommonConstants.CAMUNDA);
        assertEquals(statusCode, HttpStatus.SC_OK);
    }

    private HttpResponse createResponse(int respStatus,
                                        String respBody) {
        HttpResponse response = new BasicHttpResponse(
                                                      new BasicStatusLine(
                                                                          new ProtocolVersion("HTTP", 1, 1), respStatus, ""));
        response.setStatusCode(respStatus);
        try {
            response.setEntity(new StringEntity(respBody));
            response.setHeader("Content-Type", "application/json");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
    
    public String inputStream(String JsonInput)throws IOException{
		JsonInput = "src/test/resources/CamundaClientTest" + JsonInput;
		String input = new String(Files.readAllBytes(Paths.get(JsonInput)));
		return input;
	}
    
    @Test
    public void wrapVIDRequestTest() throws IOException{
    	CamundaClient testClient = new CamundaClient();
    	testClient.setUrl("/mso/async/services/CreateGenericALaCarteServiceInstance");
    	
    	String requestId = "f7ce78bb-423b-11e7-93f8-0050569a796";
    	boolean isBaseVfModule = true;
    	int recipeTimeout = 10000;
    	String requestAction = "createInstance";
    	String serviceInstanceId = "12345679";
    	String correlationId = "12345679";
    	String vnfId = "234567891";
    	String vfModuleId = "345678912";
    	String volumeGroupId = "456789123";
    	String networkId = "567891234";
    	String configurationId = "678912345";
    	String serviceType = "testService";
    	String vnfType = "testVnf";
    	String vfModuleType = "vfModuleType";
    	String networkType = "networkType";
    	String requestDetails = "{requestDetails: }";
    	String apiVersion = "6";
    	boolean aLaCarte = true;
    	String requestUri = "v7/serviceInstances/assign";
    	
    	String testResult = testClient.wrapVIDRequest(requestId, isBaseVfModule, recipeTimeout, requestAction, serviceInstanceId, correlationId,
    						vnfId, vfModuleId, volumeGroupId, networkId, configurationId, serviceType, 
    						vnfType, vfModuleType, networkType, requestDetails, apiVersion, aLaCarte, requestUri, "");
    	String expected = inputStream("/WrappedVIDRequest.json");
    	
    	assertEquals(expected, testResult);
    }




}
