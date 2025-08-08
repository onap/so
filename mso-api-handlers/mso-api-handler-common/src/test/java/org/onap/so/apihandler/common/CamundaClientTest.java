/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.apihandler.common;


import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.BPMNFailureException;
import org.onap.so.apihandlerinfra.exceptions.ClientConnectionException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class CamundaClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Environment env;

    @Spy
    private ResponseHandler responseHandler;

    @Spy
    @InjectMocks
    private CamundaClient client;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        when(env.getRequiredProperty("mso.camundaAuth"))
                .thenReturn("015E7ACF706C6BBF85F2079378BDD2896E226E09D13DC2784BA309E27D59AB9FAD3A5E039DF0BB8408");
        when(env.getRequiredProperty("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7");
        when(env.getRequiredProperty("mso.camundaURL")).thenReturn("http://localhost:8080");
    }

    public String inputStream(String JsonInput) throws IOException {
        JsonInput = "src/test/resources/CamundaClientTest" + JsonInput;
        String input = new String(Files.readAllBytes(Paths.get(JsonInput)));
        return input;
    }

    @Test
    public void createBPMNFailureExceptionNoResponseBodyTest() {
        HttpServerErrorException e = new HttpServerErrorException(HttpStatus.NOT_FOUND);
        BPMNFailureException ex = client.createBPMNFailureException(e);
        assertEquals(HttpStatus.NOT_IMPLEMENTED.value(), ex.getHttpResponseCode());
        assertEquals("Request Failed due to BPEL error with HTTP Status = 404 NOT_FOUND", ex.getMessage());
    }

    @Test
    public void createBPMNFailureExceptionWithCamundaResponseTest() throws IOException {
        HttpClientErrorException e = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, null,
                inputStream("/CamundaFailure.json").getBytes(), null);
        BPMNFailureException ex = client.createBPMNFailureException(e);
        assertEquals(HttpStatus.BAD_GATEWAY.value(), ex.getHttpResponseCode());
        assertEquals(
                "Request Failed due to BPEL error with HTTP Status = 500 INTERNAL_SERVER_ERROR <aetgt:WorkflowException xmlns:aetgt=\"http://org.onap/so/workflow/schema/v1\"><aetgt:ErrorMessage>Exception in create execution list 500 </aetgt:ErrorMessage><aetgt:ErrorCode>7000</aetgt:ErrorCode></aetgt:WorkflowException>",
                ex.getMessage());
    }

    @Test
    public void createBPMNFailureExceptionTest() {
        String response = "Request failed";
        HttpClientErrorException e =
                new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, null, response.getBytes(), null);
        BPMNFailureException ex = client.createBPMNFailureException(e);
        assertEquals(HttpStatus.BAD_GATEWAY.value(), ex.getHttpResponseCode());
        assertEquals("Request Failed due to BPEL error with HTTP Status = 500 INTERNAL_SERVER_ERROR Request failed",
                ex.getMessage());
    }

    @Test
    public void wrapVIDRequestTest() throws IOException {
        String requestId = "f7ce78bb-423b-11e7-93f8-0050569a796";
        boolean isBaseVfModule = true;
        int recipeTimeout = 10000;
        String requestAction = "createInstance";
        String serviceInstanceId = "12345679";
        String pnfCorrelationId = "12345679";
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
        String instanceGroupId = "ff305d54-75b4-431b-adb2-eb6b9e5ff000";
        String operationType = "activation";

        String testResult = client.wrapVIDRequest(requestId, isBaseVfModule, recipeTimeout, requestAction,
                serviceInstanceId, pnfCorrelationId, vnfId, vfModuleId, volumeGroupId, networkId, configurationId,
                serviceType, vnfType, vfModuleType, networkType, requestDetails, apiVersion, aLaCarte, requestUri, "",
                instanceGroupId, false, operationType);
        String expected = inputStream("/WrappedVIDRequest.json");

        JSONAssert.assertEquals(expected, testResult, false);
    }

    @Test
    public void getClientConnectionExceptionTest() throws ApiException {
        doThrow(ResourceAccessException.class).when(restTemplate).exchange(eq("http://localhost:8080/path"),
                eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
        thrown.expect(ClientConnectionException.class);
        thrown.expectMessage("Client from http://localhost:8080/path failed to connect or respond");
        client.get("/path");
    }

    @Test
    public void postClientConnectionExceptionTest() throws ApiException, IOException {
        String jsonReq = inputStream("/WrappedVIDRequest.json");
        doThrow(ResourceAccessException.class).when(restTemplate).postForEntity(eq("http://localhost:8080/path"),
                any(HttpEntity.class), eq(String.class));
        thrown.expect(ClientConnectionException.class);
        thrown.expectMessage("Client from http://localhost:8080/path failed to connect or respond");
        client.post(jsonReq, "/path");
    }

    @Test
    public void getHttpStatusCodeExceptionTest() throws ApiException {
        HttpServerErrorException e = new HttpServerErrorException(HttpStatus.NOT_FOUND);
        doThrow(e).when(restTemplate).exchange(eq("http://localhost:8080/path"), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(String.class));
        thrown.expect(BPMNFailureException.class);
        thrown.expectMessage("Request Failed due to BPEL error with HTTP Status = 404 NOT_FOUND");
        client.get("/path");
    }

    @Test
    public void postHttpStatusCodeExceptionTest() throws ApiException, IOException {
        HttpServerErrorException e = new HttpServerErrorException(HttpStatus.NOT_FOUND);
        String jsonReq = inputStream("/WrappedVIDRequest.json");
        doThrow(e).when(restTemplate).postForEntity(eq("http://localhost:8080/path"), any(HttpEntity.class),
                eq(String.class));
        thrown.expect(BPMNFailureException.class);
        thrown.expectMessage("Request Failed due to BPEL error with HTTP Status = 404 NOT_FOUND");
        client.post(jsonReq, "/path");
    }

}
