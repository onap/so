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


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.onap.so.logger.HttpHeadersConstants.ONAP_REQUEST_ID;
import static org.onap.so.logger.HttpHeadersConstants.REQUESTOR_ID;
import static org.onap.so.logger.MdcConstants.CLIENT_ID;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.onap.so.apihandlerinfra.exceptions.ContactCamundaException;
import org.onap.so.apihandlerinfra.exceptions.RequestDbFailureException;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.logger.HttpHeadersConstants;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestInfo;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RequestHandlerUtilsTest extends BaseTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private ObjectMapper errorMapper = new ObjectMapper();

    @Autowired
    private RequestHandlerUtils requestHandlerUtils;

    @Value("${wiremock.server.port}")
    private String wiremockPort;

    private URL initialUrl;
    private int initialPort;
    private HttpHeaders headers;

    @Before
    public void beforeClass() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        errorMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        errorMapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        // set headers
        headers = new HttpHeaders();
        headers.set(ONAPLogConstants.Headers.PARTNER_NAME, "test_name");
        headers.set(HttpHeadersConstants.TRANSACTION_ID, "32807a28-1a14-4b88-b7b3-2950918aa76d");
        headers.set(ONAP_REQUEST_ID, "32807a28-1a14-4b88-b7b3-2950918aa76d");
        headers.set(ONAPLogConstants.MDCs.REQUEST_ID, "32807a28-1a14-4b88-b7b3-2950918aa76d");
        headers.set(CLIENT_ID, "VID");
        headers.set(REQUESTOR_ID, "xxxxxx");
        try { // generate one-time port number to avoid RANDOM port number later.
            initialUrl = new URL(createURLWithPort(Constants.ORCHESTRATION_REQUESTS_PATH));
            initialPort = initialUrl.getPort();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        wireMockServer.stubFor(post(urlMatching(".*/infraActiveRequests.*")).willReturn(aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).withStatus(HttpStatus.SC_OK)));
    }

    public String inputStream(String JsonInput) throws IOException {
        JsonInput = "src/test/resources/ServiceInstanceTest" + JsonInput;
        return new String(Files.readAllBytes(Paths.get(JsonInput)));
    }

    public ResponseEntity<String> sendRequest(String requestJson, String uriPath, HttpMethod reqMethod,
            HttpHeaders headers) {

        if (!headers.containsKey(HttpHeaders.ACCEPT)) {
            headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        }
        if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(createURLWithPort(uriPath, initialPort));

        HttpEntity<String> request = new HttpEntity<>(requestJson, headers);

        return restTemplate.exchange(builder.toUriString(), reqMethod, request, String.class);
    }

    public ResponseEntity<String> sendRequest(String requestJson, String uriPath, HttpMethod reqMethod) {
        return sendRequest(requestJson, uriPath, reqMethod, new HttpHeaders());
    }

    @Test
    public void test_mapJSONtoMSOStyle() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        String testRequest = inputStream("/ServiceInstanceDefault.json");
        String resultString = requestHandlerUtils.mapJSONtoMSOStyle(testRequest, null, false, null);
        ServiceInstancesRequest sir = mapper.readValue(resultString, ServiceInstancesRequest.class);
        ModelInfo modelInfo = sir.getRequestDetails().getModelInfo();
        assertEquals("f7ce78bb-423b-11e7-93f8-0050569a796", modelInfo.getModelCustomizationUuid());
        assertEquals("modelInstanceName", modelInfo.getModelInstanceName());
        assertEquals("f7ce78bb-423b-11e7-93f8-0050569a7965", modelInfo.getModelInvariantUuid());
        assertEquals("10", modelInfo.getModelUuid());

    }


    @Test
    public void test_mapJSONtoMSOStyleUsePreload() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        String testRequest = inputStream("/ServiceInstanceDefault.json");
        ServiceInstancesRequest sir = new ServiceInstancesRequest();
        RequestDetails rd = new RequestDetails();
        RequestParameters rp = new RequestParameters();
        rp.setUsePreload(true);
        rd.setRequestParameters(rp);
        sir.setRequestDetails(rd);
        String resultString = requestHandlerUtils.mapJSONtoMSOStyle(testRequest, sir, false, null);
        ServiceInstancesRequest sir1 = mapper.readValue(resultString, ServiceInstancesRequest.class);
        assertTrue(sir1.getRequestDetails().getRequestParameters().getUsePreload());
    }

    @Test
    public void setServiceTypeTestALaCarte() throws JsonProcessingException {
        String requestScope = ModelType.service.toString();
        Boolean aLaCarteFlag = true;
        ServiceInstancesRequest sir = new ServiceInstancesRequest();
        RequestDetails requestDetails = new RequestDetails();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setSource("VID");
        requestDetails.setRequestInfo(requestInfo);
        sir.setRequestDetails(requestDetails);
        Service defaultService = new Service();
        defaultService.setServiceType("testServiceTypeALaCarte");

        wireMockServer.stubFor(get(urlMatching(".*/service/search/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        String serviceType = requestHandlerUtils.getServiceType(requestScope, sir, aLaCarteFlag);
        assertEquals(serviceType, "testServiceTypeALaCarte");
    }

    @Test
    public void setServiceTypeTest() throws JsonProcessingException {
        String requestScope = ModelType.service.toString();
        Boolean aLaCarteFlag = false;
        ServiceInstancesRequest sir = new ServiceInstancesRequest();
        RequestDetails requestDetails = new RequestDetails();
        RequestInfo requestInfo = new RequestInfo();
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelVersionId("0dd91181-49da-446b-b839-cd959a96f04a");
        requestInfo.setSource("VID");
        requestDetails.setModelInfo(modelInfo);
        requestDetails.setRequestInfo(requestInfo);
        sir.setRequestDetails(requestDetails);
        Service defaultService = new Service();
        defaultService.setServiceType("testServiceType");

        wireMockServer.stubFor(get(urlMatching(".*/service/0dd91181-49da-446b-b839-cd959a96f04a"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        String serviceType = requestHandlerUtils.getServiceType(requestScope, sir, aLaCarteFlag);
        assertEquals(serviceType, "testServiceType");
    }

    @Test
    public void setServiceTypeTestDefault() throws JsonProcessingException {
        String requestScope = ModelType.service.toString();
        Boolean aLaCarteFlag = false;
        ServiceInstancesRequest sir = new ServiceInstancesRequest();
        RequestDetails requestDetails = new RequestDetails();
        RequestInfo requestInfo = new RequestInfo();
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelVersionId("0dd91181-49da-446b-b839-cd959a96f04a");
        requestInfo.setSource("VID");
        requestDetails.setModelInfo(modelInfo);
        requestDetails.setRequestInfo(requestInfo);
        sir.setRequestDetails(requestDetails);
        Service defaultService = new Service();
        defaultService.setServiceType("testServiceType");

        wireMockServer.stubFor(get(urlMatching(".*/service/0dd91181-49da-446b-b839-cd959a96f04a"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withStatus(HttpStatus.SC_NOT_FOUND)));
        wireMockServer.stubFor(get(urlMatching(".*/service/search/.*"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .withBody(mapper.writeValueAsString(defaultService)).withStatus(HttpStatus.SC_OK)));

        String serviceType = requestHandlerUtils.getServiceType(requestScope, sir, aLaCarteFlag);
        assertEquals(serviceType, "testServiceType");
    }

    @Test
    public void setServiceTypeTestNetwork() throws JsonProcessingException {
        String requestScope = ModelType.network.toString();
        Boolean aLaCarteFlag = null;
        ServiceInstancesRequest sir = new ServiceInstancesRequest();
        RequestDetails requestDetails = new RequestDetails();
        RequestInfo requestInfo = new RequestInfo();
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelName("networkModelName");
        requestInfo.setSource("VID");
        requestDetails.setModelInfo(modelInfo);
        requestDetails.setRequestInfo(requestInfo);
        sir.setRequestDetails(requestDetails);

        String serviceType = requestHandlerUtils.getServiceType(requestScope, sir, aLaCarteFlag);
        assertEquals(serviceType, "networkModelName");
    }

    @Test
    public void setServiceInstanceIdInstanceGroupTest() throws JsonParseException, JsonMappingException, IOException {
        String requestScope = "instanceGroup";
        ServiceInstancesRequest sir =
                mapper.readValue(inputStream("/CreateInstanceGroup.json"), ServiceInstancesRequest.class);
        assertEquals("ddcbbf3d-f2c1-4ca0-8852-76a807285efc",
                requestHandlerUtils.setServiceInstanceId(requestScope, sir));
    }

    @Test
    public void setServiceInstanceIdTest() {
        String requestScope = "vnf";
        ServiceInstancesRequest sir = new ServiceInstancesRequest();
        sir.setServiceInstanceId("f0a35706-efc4-4e27-80ea-a995d7a2a40f");
        assertEquals("f0a35706-efc4-4e27-80ea-a995d7a2a40f",
                requestHandlerUtils.setServiceInstanceId(requestScope, sir));
    }

    @Test
    public void setServiceInstanceIdReturnNullTest() {
        String requestScope = "vnf";
        ServiceInstancesRequest sir = new ServiceInstancesRequest();
        assertNull(requestHandlerUtils.setServiceInstanceId(requestScope, sir));
    }

    @Test
    public void camundaHistoryCheckTest() throws ContactCamundaException, RequestDbFailureException {
        wireMockServer.stubFor(get(
                ("/sobpmnengine/history/process-instance?variables=mso-request-id_eq_f0a35706-efc4-4e27-80ea-a995d7a2a40f"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBodyFile("Camunda/HistoryCheckResponse.json")
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        InfraActiveRequests duplicateRecord = new InfraActiveRequests();
        duplicateRecord.setRequestId("f0a35706-efc4-4e27-80ea-a995d7a2a40f");
        boolean inProgress = false;
        inProgress = requestHandlerUtils.camundaHistoryCheck(duplicateRecord, null);
        assertTrue(inProgress);
    }

    @Test
    public void camundaHistoryCheckNoneFoundTest() throws ContactCamundaException, RequestDbFailureException {
        wireMockServer.stubFor(get(
                ("/sobpmnengine/history/process-instance?variables=mso-request-id_eq_f0a35706-efc4-4e27-80ea-a995d7a2a40f"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBody("[]").withStatus(org.apache.http.HttpStatus.SC_OK)));

        InfraActiveRequests duplicateRecord = new InfraActiveRequests();
        duplicateRecord.setRequestId("f0a35706-efc4-4e27-80ea-a995d7a2a40f");
        boolean inProgress = false;
        inProgress = requestHandlerUtils.camundaHistoryCheck(duplicateRecord, null);
        assertFalse(inProgress);
    }

    @Test
    public void camundaHistoryCheckNotInProgressTest() throws ContactCamundaException, RequestDbFailureException {
        wireMockServer.stubFor(get(
                ("/sobpmnengine/history/process-instance?variables=mso-request-id_eq_f0a35706-efc4-4e27-80ea-a995d7a2a40f"))
                        .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withBodyFile("Camunda/HistoryCheckResponseCompleted.json")
                                .withStatus(org.apache.http.HttpStatus.SC_OK)));

        InfraActiveRequests duplicateRecord = new InfraActiveRequests();
        duplicateRecord.setRequestId("f0a35706-efc4-4e27-80ea-a995d7a2a40f");
        boolean inProgress = false;
        inProgress = requestHandlerUtils.camundaHistoryCheck(duplicateRecord, null);
        assertFalse(inProgress);
    }

    @Test
    public void setCamundaHeadersTest() throws ContactCamundaException, RequestDbFailureException {
        String encryptedAuth = "015E7ACF706C6BBF85F2079378BDD2896E226E09D13DC2784BA309E27D59AB9FAD3A5E039DF0BB8408"; // user:password
        String key = "07a7159d3bf51a0e53be7a8f89699be7";
        HttpHeaders headers = requestHandlerUtils.setCamundaHeaders(encryptedAuth, key);
        List<org.springframework.http.MediaType> acceptedType = headers.getAccept();
        String expectedAcceptedType = "application/json";
        assertEquals(expectedAcceptedType, acceptedType.get(0).toString());
        String basicAuth = headers.getFirst(HttpHeaders.AUTHORIZATION);
        String expectedBasicAuth = "Basic dXNlcjpwYXNzd29yZA==";
        assertEquals(expectedBasicAuth, basicAuth);
    }

}
