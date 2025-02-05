/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceEntity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.apihandlerinfra.exceptions.ContactCamundaException;
import org.onap.so.apihandlerinfra.exceptions.RequestDbFailureException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class CamundaRequestHandlerUnitTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplate restTemplateRetry;

    @Mock
    private Environment env;

    @InjectMocks
    @Spy
    private CamundaRequestHandler camundaRequestHandler;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final String REQUEST_ID = "eca3a1b1-43ab-457e-ab1c-367263d148b4";
    private ResponseEntity<List<HistoricActivityInstanceEntity>> activityInstanceResponse = null;
    private ResponseEntity<List<HistoricProcessInstanceEntity>> processInstanceResponse = null;
    private List<HistoricActivityInstanceEntity> activityInstanceList = null;
    private List<HistoricProcessInstanceEntity> processInstanceList = null;



    @Before
    public void setup() throws IOException {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        activityInstanceList = mapper.readValue(
                new String(Files.readAllBytes(
                        Paths.get("src/test/resources/OrchestrationRequest/ActivityInstanceHistoryResponse.json"))),
                new TypeReference<List<HistoricActivityInstanceEntity>>() {});
        processInstanceList = mapper.readValue(
                new String(Files.readAllBytes(
                        Paths.get("src/test/resources/OrchestrationRequest/ProcessInstanceHistoryResponse.json"))),
                new TypeReference<List<HistoricProcessInstanceEntity>>() {});
        processInstanceResponse =
                new ResponseEntity<List<HistoricProcessInstanceEntity>>(processInstanceList, HttpStatus.ACCEPTED);
        activityInstanceResponse =
                new ResponseEntity<List<HistoricActivityInstanceEntity>>(activityInstanceList, HttpStatus.ACCEPTED);

        doReturn("/sobpmnengine/history/process-instance").when(env).getProperty("mso.camunda.rest.history.uri");
        doReturn("/sobpmnengine/history/activity-instance").when(env).getProperty("mso.camunda.rest.activity.uri");
        doReturn("auth").when(env).getRequiredProperty("mso.camundaAuth");
        doReturn("key").when(env).getRequiredProperty("mso.msoKey");
        doReturn("http://localhost:8089").when(env).getProperty("mso.camundaURL");

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        restTemplate.setRequestFactory(factory);
        doReturn(restTemplate).when(camundaRequestHandler).getRestTemplate(false);

        HttpComponentsClientHttpRequestFactory factoryRetry = new HttpComponentsClientHttpRequestFactory();
        factoryRetry.setConnectTimeout(15000);
        restTemplate.setRequestFactory(factoryRetry);
        doReturn(restTemplateRetry).when(camundaRequestHandler).getRestTemplate(true);
    }

    public HttpHeaders setHeaders() {
        HttpHeaders headers = new HttpHeaders();
        List<org.springframework.http.MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.setAccept(acceptableMediaTypes);
        headers.add(HttpHeaders.AUTHORIZATION, "auth");

        return headers;
    }

    @Test
    public void getActivityNameTest() throws IOException {
        String expectedActivityName = "Last task executed: BB to Execute";
        String actualActivityName = camundaRequestHandler.getActivityName(activityInstanceList);

        assertEquals(expectedActivityName, actualActivityName);
    }

    @Test
    public void getActivityNameNullActivityNameTest() throws IOException {
        String expectedActivityName = "Task name is null.";
        HistoricActivityInstanceEntity activityInstance = new HistoricActivityInstanceEntity();
        List<HistoricActivityInstanceEntity> activityInstanceList = new ArrayList<>();
        activityInstanceList.add(activityInstance);

        String actualActivityName = camundaRequestHandler.getActivityName(activityInstanceList);

        assertEquals(expectedActivityName, actualActivityName);
    }

    @Test
    public void getActivityNameNullListTest() throws IOException {
        String expectedActivityName = "No results returned on activityInstance history lookup.";
        List<HistoricActivityInstanceEntity> activityInstanceList = null;
        String actualActivityName = camundaRequestHandler.getActivityName(activityInstanceList);

        assertEquals(expectedActivityName, actualActivityName);
    }

    @Test
    public void getActivityNameEmptyListTest() throws IOException {
        String expectedActivityName = "No results returned on activityInstance history lookup.";
        List<HistoricActivityInstanceEntity> activityInstanceList = new ArrayList<>();
        String actualActivityName = camundaRequestHandler.getActivityName(activityInstanceList);

        assertEquals(expectedActivityName, actualActivityName);
    }

    @Test
    public void getCamundActivityHistoryNullTest() throws IOException, ContactCamundaException {
        HistoricProcessInstanceEntity processInstance = new HistoricProcessInstanceEntity();
        processInstance.setId("c4c6b647-a26e-11e9-b144-0242ac14000b");
        List<HistoricProcessInstanceEntity> processInstanceList = new ArrayList<>();
        processInstanceList.add(processInstance);
        ResponseEntity<List<HistoricProcessInstanceEntity>> response =
                new ResponseEntity<>(processInstanceList, HttpStatus.OK);
        doReturn(null).when(camundaRequestHandler).getCamundaActivityHistory("c4c6b647-a26e-11e9-b144-0242ac14000b");

        String actualTaskName = camundaRequestHandler.getTaskInformation(response, REQUEST_ID);

        assertNull(actualTaskName);
    }

    @Test
    public void getCamundActivityHistoryErrorTest() throws IOException, ContactCamundaException {
        HistoricProcessInstanceEntity processInstance = new HistoricProcessInstanceEntity();
        processInstance.setId("c4c6b647-a26e-11e9-b144-0242ac14000b");
        List<HistoricProcessInstanceEntity> processInstanceList = new ArrayList<>();
        processInstanceList.add(processInstance);
        ResponseEntity<List<HistoricProcessInstanceEntity>> response =
                new ResponseEntity<>(processInstanceList, HttpStatus.OK);
        doThrow(RestClientException.class).when(camundaRequestHandler)
                .getCamundaActivityHistory("c4c6b647-a26e-11e9-b144-0242ac14000b");

        String actualTaskName = camundaRequestHandler.getTaskInformation(response, REQUEST_ID);

        assertNull(actualTaskName);
    }

    @Test
    public void getTaskName() throws IOException, ContactCamundaException {
        doReturn(processInstanceResponse).when(camundaRequestHandler).getCamundaProcessInstanceHistory(REQUEST_ID,
                false, false, true);
        doReturn(activityInstanceResponse).when(camundaRequestHandler)
                .getCamundaActivityHistory("c2fd4066-a26e-11e9-b144-0242ac14000b");
        doReturn("Last task executed: BB to Execute").when(camundaRequestHandler).getActivityName(activityInstanceList);
        String expectedTaskName = "Last task executed: BB to Execute";

        String actualTaskName = camundaRequestHandler.getTaskName(REQUEST_ID);

        assertEquals(expectedTaskName, actualTaskName);
    }

    @Test
    public void getTaskNameNullProcessInstanceListTest() throws IOException, ContactCamundaException {
        ResponseEntity<List<HistoricProcessInstanceEntity>> response = new ResponseEntity<>(null, HttpStatus.OK);

        String actual = camundaRequestHandler.getTaskInformation(response, REQUEST_ID);

        assertNull(actual);
    }

    @Test
    public void getTaskNameNullProcessInstanceIdTest() throws IOException, ContactCamundaException {
        HistoricProcessInstanceEntity processInstance = new HistoricProcessInstanceEntity();
        List<HistoricProcessInstanceEntity> processInstanceList = new ArrayList<>();
        processInstanceList.add(processInstance);
        ResponseEntity<List<HistoricProcessInstanceEntity>> response =
                new ResponseEntity<>(processInstanceList, HttpStatus.OK);

        String actual = camundaRequestHandler.getTaskInformation(response, REQUEST_ID);

        assertNull(actual);
    }

    @Test
    public void getTaskNameEmptyProcessInstanceListTest() throws IOException, ContactCamundaException {
        List<HistoricProcessInstanceEntity> processInstanceList = new ArrayList<>();
        ResponseEntity<List<HistoricProcessInstanceEntity>> response =
                new ResponseEntity<>(processInstanceList, HttpStatus.OK);

        String actual = camundaRequestHandler.getTaskInformation(response, REQUEST_ID);

        assertNull(actual);
    }

    @Test
    public void getTaskNameProcessInstanceLookupFailureTest() throws IOException, ContactCamundaException {
        doThrow(HttpClientErrorException.class).when(camundaRequestHandler).getCamundaProcessInstanceHistory(REQUEST_ID,
                false, false, true);

        String result = camundaRequestHandler.getTaskName(REQUEST_ID);
        assertNull(result);
    }

    @Test
    public void getCamundaActivityHistoryTest() throws IOException, ContactCamundaException {
        HttpHeaders headers = setHeaders();
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        String targetUrl =
                "http://localhost:8089/sobpmnengine/history/activity-instance?processInstanceId=c4c6b647-a26e-11e9-b144-0242ac14000b&maxResults=1";
        doReturn(activityInstanceResponse).when(restTemplate).exchange(targetUrl, HttpMethod.GET, requestEntity,
                new ParameterizedTypeReference<List<HistoricActivityInstanceEntity>>() {});
        doReturn(headers).when(camundaRequestHandler).setCamundaHeaders("auth", "key");
        ResponseEntity<List<HistoricActivityInstanceEntity>> actualResponse =
                camundaRequestHandler.getCamundaActivityHistory("c4c6b647-a26e-11e9-b144-0242ac14000b");
        assertEquals(activityInstanceResponse, actualResponse);
    }

    @Test
    public void getCamundaActivityHistoryErrorTest() {
        HttpHeaders headers = setHeaders();
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        String targetUrl =
                "http://localhost:8089/sobpmnengine/history/activity-instance?processInstanceId=c4c6b647-a26e-11e9-b144-0242ac14000b&maxResults=1";
        doThrow(new ResourceAccessException("IOException")).when(restTemplate).exchange(targetUrl, HttpMethod.GET,
                requestEntity, new ParameterizedTypeReference<List<HistoricActivityInstanceEntity>>() {});
        doReturn(headers).when(camundaRequestHandler).setCamundaHeaders("auth", "key");

        thrown.expect(ResourceAccessException.class);
        camundaRequestHandler.getCamundaActivityHistory("c4c6b647-a26e-11e9-b144-0242ac14000b");

        verify(restTemplate, times(1)).exchange(targetUrl, HttpMethod.GET, requestEntity,
                new ParameterizedTypeReference<List<HistoricActivityInstanceEntity>>() {});
    }

    @Test
    public void getCamundaProccesInstanceHistoryTest() throws IOException, ContactCamundaException {
        HttpHeaders headers = setHeaders();
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        String targetUrl = "http://localhost:8089/sobpmnengine/history/process-instance?processInstanceBusinessKey="
                + REQUEST_ID + "&active=true&maxResults=1";
        doReturn(processInstanceResponse).when(restTemplate).exchange(targetUrl, HttpMethod.GET, requestEntity,
                new ParameterizedTypeReference<List<HistoricProcessInstanceEntity>>() {});
        doReturn(headers).when(camundaRequestHandler).setCamundaHeaders("auth", "key");

        ResponseEntity<List<HistoricProcessInstanceEntity>> actualResponse =
                camundaRequestHandler.getCamundaProcessInstanceHistory(REQUEST_ID, false, true, false);
        assertEquals(processInstanceResponse, actualResponse);
    }

    @Test
    public void getCamundaProccesInstanceHistoryRetryTest() {
        HttpHeaders headers = setHeaders();
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        String targetUrl = "http://localhost:8089/sobpmnengine/history/process-instance?processInstanceBusinessKey="
                + REQUEST_ID + "&active=true&maxResults=1";
        doThrow(new ResourceAccessException("I/O error")).when(restTemplateRetry).exchange(targetUrl, HttpMethod.GET,
                requestEntity, new ParameterizedTypeReference<List<HistoricProcessInstanceEntity>>() {});
        doReturn(headers).when(camundaRequestHandler).setCamundaHeaders("auth", "key");

        thrown.expect(ResourceAccessException.class);
        camundaRequestHandler.getCamundaProcessInstanceHistory(REQUEST_ID, true, true, false);

        verify(restTemplateRetry, times(2)).exchange(targetUrl, HttpMethod.GET, requestEntity,
                new ParameterizedTypeReference<List<HistoricProcessInstanceEntity>>() {});
    }

    @Test
    public void getCamundaProccesInstanceHistoryNoRetryTest() {
        HttpHeaders headers = setHeaders();
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        String targetUrl = "http://localhost:8089/sobpmnengine/history/process-instance?processInstanceBusinessKey="
                + REQUEST_ID + "&sortBy=startTime&sortOrder=desc&maxResults=1";
        doThrow(HttpClientErrorException.class).when(restTemplate).exchange(targetUrl, HttpMethod.GET, requestEntity,
                new ParameterizedTypeReference<List<HistoricProcessInstanceEntity>>() {});
        doReturn(headers).when(camundaRequestHandler).setCamundaHeaders("auth", "key");

        thrown.expect(HttpStatusCodeException.class);
        camundaRequestHandler.getCamundaProcessInstanceHistory(REQUEST_ID, false, false, true);

        verify(restTemplate, times(1)).exchange(targetUrl, HttpMethod.GET, requestEntity,
                new ParameterizedTypeReference<List<HistoricProcessInstanceEntity>>() {});
    }

    @Test
    public void getCamundaProccesInstanceHistoryFailThenSuccessTest() throws IOException, ContactCamundaException {
        HttpHeaders headers = setHeaders();
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        String targetUrl = "http://localhost:8089/sobpmnengine/history/process-instance?processInstanceBusinessKey="
                + REQUEST_ID + "&sortBy=startTime&sortOrder=desc&maxResults=1";
        when(restTemplateRetry.exchange(targetUrl, HttpMethod.GET, requestEntity,
                new ParameterizedTypeReference<List<HistoricProcessInstanceEntity>>() {}))
                        .thenThrow(new ResourceAccessException("I/O Exception")).thenReturn(processInstanceResponse);
        doReturn(headers).when(camundaRequestHandler).setCamundaHeaders("auth", "key");

        ResponseEntity<List<HistoricProcessInstanceEntity>> actualResponse =
                camundaRequestHandler.getCamundaProcessInstanceHistory(REQUEST_ID, true, false, true);

        assertEquals(processInstanceResponse, actualResponse);
        verify(restTemplateRetry, times(2)).exchange(targetUrl, HttpMethod.GET, requestEntity,
                new ParameterizedTypeReference<List<HistoricProcessInstanceEntity>>() {});
    }

    @Test
    public void setCamundaHeadersTest() throws ContactCamundaException, RequestDbFailureException {
        String encryptedAuth = "015E7ACF706C6BBF85F2079378BDD2896E226E09D13DC2784BA309E27D59AB9FAD3A5E039DF0BB8408"; // user:password
        String key = "07a7159d3bf51a0e53be7a8f89699be7";

        HttpHeaders headers = camundaRequestHandler.setCamundaHeaders(encryptedAuth, key);
        List<org.springframework.http.MediaType> acceptedType = headers.getAccept();

        String expectedAcceptedType = "application/json";
        assertEquals(expectedAcceptedType, acceptedType.get(0).toString());
        String basicAuth = headers.getFirst(HttpHeaders.AUTHORIZATION);
        String expectedBasicAuth = "Basic dXNlcjpwYXNzd29yZA==";

        assertEquals(expectedBasicAuth, basicAuth);
    }
}
