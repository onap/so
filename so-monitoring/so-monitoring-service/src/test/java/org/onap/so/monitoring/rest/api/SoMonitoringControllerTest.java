/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
 * 
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.monitoring.rest.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.onap.so.client.RestTemplateConfig.CONFIGURABLE_REST_TEMPLATE;
import static org.onap.so.configuration.rest.BasicHttpHeadersProvider.AUTHORIZATION_HEADER;
import static org.onap.so.monitoring.rest.api.Constants.ACTIVITY_INSTANCE_RESPONSE_JSON_FILE;
import static org.onap.so.monitoring.rest.api.Constants.EMPTY_ARRAY_RESPONSE;
import static org.onap.so.monitoring.rest.api.Constants.EMPTY_STRING;
import static org.onap.so.monitoring.rest.api.Constants.END_TIME_IN_MS;
import static org.onap.so.monitoring.rest.api.Constants.ID;
import static org.onap.so.monitoring.rest.api.Constants.PROCCESS_INSTANCE_RESPONSE_JSON_FILE;
import static org.onap.so.monitoring.rest.api.Constants.PROCESS_DEF_RESPONSE_JSON_FILE;
import static org.onap.so.monitoring.rest.api.Constants.PROCESS_INSTACE_ID;
import static org.onap.so.monitoring.rest.api.Constants.PROCESS_INSTANCE_VARIABLES_RESPONSE_JSON_FILE;
import static org.onap.so.monitoring.rest.api.Constants.PROCRESS_DEF_ID;
import static org.onap.so.monitoring.rest.api.Constants.SEARCH_RESULT_RESPONSE_JSON_FILE;
import static org.onap.so.monitoring.rest.api.Constants.SINGLE_PROCCESS_INSTANCE_RESPONSE_JSON_FILE;
import static org.onap.so.monitoring.rest.api.Constants.START_TIME_IN_MS;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.so.monitoring.configuration.camunda.CamundaRestUrlProvider;
import org.onap.so.monitoring.configuration.database.DatabaseUrlProvider;
import org.onap.so.monitoring.model.ActivityInstanceDetail;
import org.onap.so.monitoring.model.ProcessDefinitionDetail;
import org.onap.so.monitoring.model.ProcessInstanceDetail;
import org.onap.so.monitoring.model.ProcessInstanceIdDetail;
import org.onap.so.monitoring.model.ProcessInstanceVariableDetail;
import org.onap.so.monitoring.model.SoInfraRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;


/**
 * @author waqas.ikram@ericsson.com, andrei.barcovschi@ericsson.com
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class SoMonitoringControllerTest {
    private static final String CAMUNDA_BASIC_AUTH =
            "Basic YWRtaW46S3A4Yko0U1hzek0wV1hsaGFrM2VIbGNzZTJnQXc4NHZhb0dHbUp2VXkyVQ==";

    private static final String DATABASE_BASIC_AUTH = "Basic YnBlbDpwYXNzd29yZDEk";

    @Autowired
    @Qualifier(CONFIGURABLE_REST_TEMPLATE)
    private RestTemplate restTemplate;

    @Autowired
    private CamundaRestUrlProvider urlProvider;

    @Autowired
    private DatabaseUrlProvider databaseUrlProvider;

    private MockRestServiceServer mockRestServiceServer;


    @Autowired
    private SoMonitoringController objUnderTest;

    @Before
    public void setUp() throws Exception {
        mockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    public void test_GetProcessInstance_SuccessResponseWithDataFromCamunda() throws Exception {
        final String jsonString = getJsonResponse(PROCCESS_INSTANCE_RESPONSE_JSON_FILE);
        this.mockRestServiceServer.expect(requestTo(urlProvider.getHistoryProcessInstanceUrl(ID)))
                .andExpect(header(AUTHORIZATION_HEADER, CAMUNDA_BASIC_AUTH))
                .andRespond(withSuccess(jsonString, MediaType.APPLICATION_JSON));

        final Response response = objUnderTest.getProcessInstanceId(ID);

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        final ProcessInstanceIdDetail actualProcessInstance = (ProcessInstanceIdDetail) response.getEntity();
        assertEquals("dba707b6-8c02-11e8-a6ba-022a5dba5402", actualProcessInstance.getProcessInstanceId());
    }

    @Test
    public void test_GetProcessInstance_SuccessResponseWithEmptyDataFromCamunda() throws Exception {
        final String jsonString = EMPTY_ARRAY_RESPONSE;
        this.mockRestServiceServer.expect(requestTo(urlProvider.getHistoryProcessInstanceUrl(ID)))
                .andRespond(withSuccess(jsonString, MediaType.APPLICATION_JSON));

        final Response response = objUnderTest.getProcessInstanceId(ID);
        assertEquals(Status.NO_CONTENT.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
    }

    @Test
    public void test_GetProcessInstance_FailureResponseWithEmptyDataFromCamunda() throws Exception {
        this.mockRestServiceServer.expect(requestTo(urlProvider.getHistoryProcessInstanceUrl(ID)))
                .andRespond(withBadRequest());

        final Response response = objUnderTest.getProcessInstanceId(ID);
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void test_GetProcessInstance_UnauthorizedRequestFromCamunda() throws Exception {
        this.mockRestServiceServer.expect(requestTo(urlProvider.getHistoryProcessInstanceUrl(ID)))
                .andRespond(withUnauthorizedRequest());

        final Response response = objUnderTest.getProcessInstanceId(ID);
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    public void test_GetSinlgeProcessInstance_SuccessResponseWithDataFromCamunda() throws Exception {
        final String jsonString = getJsonResponse(SINGLE_PROCCESS_INSTANCE_RESPONSE_JSON_FILE);
        this.mockRestServiceServer.expect(requestTo(urlProvider.getSingleProcessInstanceUrl(PROCESS_INSTACE_ID)))
                .andRespond(withSuccess(jsonString, MediaType.APPLICATION_JSON));

        final Response response = objUnderTest.getSingleProcessInstance(PROCESS_INSTACE_ID);

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        final ProcessInstanceDetail actualProcessInstance = (ProcessInstanceDetail) response.getEntity();
        assertEquals(PROCESS_INSTACE_ID, actualProcessInstance.getProcessInstanceId());
        assertEquals("EricssonNetworkSliceV1:3:28f9e0fc-9b00-11e8-a57a-022ac90273ed",
                actualProcessInstance.getProcessDefinitionId());
        assertEquals("EricssonNetworkSliceV1", actualProcessInstance.getProcessDefinitionName());
        assertNull(actualProcessInstance.getSuperProcessInstanceId());
    }

    @Test
    public void test_GetSingleProcessInstance_WithBadRequestResponseFromCamunda() throws Exception {
        this.mockRestServiceServer.expect(requestTo(urlProvider.getSingleProcessInstanceUrl(PROCESS_INSTACE_ID)))
                .andRespond(withBadRequest());

        final Response response = objUnderTest.getSingleProcessInstance(PROCESS_INSTACE_ID);
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    public void test_GetSingleProcessInstance_WithUnauthorizedRequestResponseFromCamunda() throws Exception {
        this.mockRestServiceServer.expect(requestTo(urlProvider.getSingleProcessInstanceUrl(PROCESS_INSTACE_ID)))
                .andRespond(withUnauthorizedRequest());

        final Response response = objUnderTest.getSingleProcessInstance(PROCESS_INSTACE_ID);
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    public void test_GetSingleProcessInstance_NullAndEmptyProcessInstanceIdFromCamunda() throws Exception {

        Response response = objUnderTest.getSingleProcessInstance(null);
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        response = objUnderTest.getSingleProcessInstance("");
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }


    @Test
    public void test_GetProcessInstance_EmptyRequestID() throws Exception {

        Response response = objUnderTest.getProcessInstanceId(EMPTY_STRING);
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        response = objUnderTest.getProcessInstanceId(null);
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }


    @Test
    public void test_GetProcessDefinitionXml_SuccessResponseWithDataFromCamunda() throws Exception {
        final String jsonString = getJsonResponse(PROCESS_DEF_RESPONSE_JSON_FILE);
        this.mockRestServiceServer.expect(requestTo(urlProvider.getProcessDefinitionUrl(PROCRESS_DEF_ID)))
                .andRespond(withSuccess(jsonString, MediaType.APPLICATION_JSON));

        final Response response = objUnderTest.getProcessDefinitionXml(PROCRESS_DEF_ID);
        assertEquals(Status.OK.getStatusCode(), response.getStatus());

        final ProcessDefinitionDetail actual = (ProcessDefinitionDetail) response.getEntity();
        assertEquals(PROCRESS_DEF_ID, actual.getProcessDefinitionId());
    }

    @Test
    public void test_GetProcessDefinitionXml_BadRequestResponseFromCamunda() throws Exception {
        this.mockRestServiceServer.expect(requestTo(urlProvider.getProcessDefinitionUrl(PROCRESS_DEF_ID)))
                .andRespond(withBadRequest());

        final Response response = objUnderTest.getProcessDefinitionXml(PROCRESS_DEF_ID);
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    public void test_GetProcessDefinitionXml_UnauthorizedRequestFromCamunda() throws Exception {
        this.mockRestServiceServer.expect(requestTo(urlProvider.getProcessDefinitionUrl(PROCRESS_DEF_ID)))
                .andRespond(withUnauthorizedRequest());

        final Response response = objUnderTest.getProcessDefinitionXml(PROCRESS_DEF_ID);
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    public void test_GetProcessDefinitionXml_NullValues() throws Exception {
        Response response = objUnderTest.getProcessDefinitionXml(EMPTY_STRING);
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        response = objUnderTest.getProcessDefinitionXml(null);
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    public void test_GetActivityInstanceDetail_SuccessResponseWithDataFromCamunda() throws Exception {
        final String jsonString = getJsonResponse(ACTIVITY_INSTANCE_RESPONSE_JSON_FILE);
        this.mockRestServiceServer.expect(requestTo(urlProvider.getActivityInstanceUrl(PROCESS_INSTACE_ID)))
                .andRespond(withSuccess(jsonString, MediaType.APPLICATION_JSON));

        final Response response = objUnderTest.getActivityInstanceDetail(PROCESS_INSTACE_ID);
        @SuppressWarnings("unchecked")
        final List<ActivityInstanceDetail> actual = (List<ActivityInstanceDetail>) response.getEntity();
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertEquals(12, actual.size());
        final ActivityInstanceDetail activityInstanceDetail = actual.get(0);
        assertEquals("createVCPE_startEvent", activityInstanceDetail.getActivityId());
        assertEquals("Start Flow", activityInstanceDetail.getActivityName());
        assertEquals("startEvent", activityInstanceDetail.getActivityType());
        assertEquals(PROCESS_INSTACE_ID, activityInstanceDetail.getProcessInstanceId());
        assertNull(activityInstanceDetail.getCalledProcessInstanceId());
        assertEquals("26", activityInstanceDetail.getDurationInMillis());
        assertEquals("2018-08-03T16:00:31.815+0000", activityInstanceDetail.getStartTime());
        assertEquals("2018-08-03T16:00:31.841+0000", activityInstanceDetail.getEndTime());

        final ActivityInstanceDetail callActivityInstanceDetail = actual.get(4);
        assertEquals("DecomposeService", callActivityInstanceDetail.getActivityId());
        assertEquals("Call Decompose Service", callActivityInstanceDetail.getActivityName());
        assertEquals("callActivity", callActivityInstanceDetail.getActivityType());
        assertEquals("59d99609-9736-11e8-8caf-022ac9304eeb", callActivityInstanceDetail.getCalledProcessInstanceId());
    }

    @Test
    public void test_GetActivityInstanceDetail_SuccessResponseWithEmptyDataFromCamunda() throws Exception {
        this.mockRestServiceServer.expect(requestTo(urlProvider.getActivityInstanceUrl(PROCESS_INSTACE_ID)))
                .andRespond(withSuccess(EMPTY_ARRAY_RESPONSE, MediaType.APPLICATION_JSON));

        final Response response = objUnderTest.getActivityInstanceDetail(PROCESS_INSTACE_ID);
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    public void test_GetActivityInstanceDetail_UnauthorizedRequestFromCamunda() throws Exception {
        this.mockRestServiceServer.expect(requestTo(urlProvider.getActivityInstanceUrl(PROCESS_INSTACE_ID)))
                .andRespond(withUnauthorizedRequest());

        final Response response = objUnderTest.getActivityInstanceDetail(PROCESS_INSTACE_ID);
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    public void test_GetActivityInstanceDetail_BadRequestFromCamunda() throws Exception {
        this.mockRestServiceServer.expect(requestTo(urlProvider.getActivityInstanceUrl(PROCESS_INSTACE_ID)))
                .andRespond(withBadRequest());

        final Response response = objUnderTest.getActivityInstanceDetail(PROCESS_INSTACE_ID);
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    public void test_GetActivityInstanceDetail_NullValues() throws Exception {
        Response response = objUnderTest.getActivityInstanceDetail(EMPTY_STRING);
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        response = objUnderTest.getActivityInstanceDetail(null);
        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    public void test_GetProcessInstanceVariables_SuccessResponseWithDataFromCamunda() throws Exception {
        final String jsonString = getJsonResponse(PROCESS_INSTANCE_VARIABLES_RESPONSE_JSON_FILE);
        this.mockRestServiceServer.expect(requestTo(urlProvider.getProcessInstanceVariablesUrl(PROCESS_INSTACE_ID)))
                .andRespond(withSuccess(jsonString, MediaType.APPLICATION_JSON));

        final Response response = objUnderTest.getProcessInstanceVariables(PROCESS_INSTACE_ID);

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        final List<ProcessInstanceVariableDetail> actual = (List<ProcessInstanceVariableDetail>) response.getEntity();
        assertEquals(230, actual.size());

        final ProcessInstanceVariableDetail variableDetail = actual.get(0);
        assertEquals("serviceType", variableDetail.getName());
        assertEquals("String", variableDetail.getType());
        assertEquals("PNFSERVICE", variableDetail.getValue());
    }

    @Test
    public void test_GetProcessInstanceVariables_SuccessResponseWithEmptyDataFromCamunda() throws Exception {
        this.mockRestServiceServer.expect(requestTo(urlProvider.getProcessInstanceVariablesUrl(PROCESS_INSTACE_ID)))
                .andRespond(withSuccess(EMPTY_ARRAY_RESPONSE, MediaType.APPLICATION_JSON));

        final Response response = objUnderTest.getProcessInstanceVariables(PROCESS_INSTACE_ID);

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    public void test_GetProcessInstanceVariables_BadRequestFromCamunda() throws Exception {
        this.mockRestServiceServer.expect(requestTo(urlProvider.getProcessInstanceVariablesUrl(PROCESS_INSTACE_ID)))
                .andRespond(withBadRequest());

        final Response response = objUnderTest.getProcessInstanceVariables(PROCESS_INSTACE_ID);

        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    public void test_GetProcessInstanceVariables_UnauthorizedRequestFromCamunda() throws Exception {
        this.mockRestServiceServer.expect(requestTo(urlProvider.getProcessInstanceVariablesUrl(PROCESS_INSTACE_ID)))
                .andRespond(withUnauthorizedRequest());

        final Response response = objUnderTest.getProcessInstanceVariables(PROCESS_INSTACE_ID);

        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    public void test_GetProcessInstanceVariables_NullAndEmptyValues() throws Exception {

        Response response = objUnderTest.getProcessInstanceVariables(EMPTY_STRING);

        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

        response = objUnderTest.getProcessInstanceVariables(null);

        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertNotNull(response.getEntity());

    }

    @Test
    public void test_GetInfraActiveRequests_SuccessResponseWithSoInfraRequestList() throws Exception {
        final String jsonString = getJsonResponse(SEARCH_RESULT_RESPONSE_JSON_FILE);
        this.mockRestServiceServer
                .expect(requestTo(databaseUrlProvider.getSearchUrl(START_TIME_IN_MS, END_TIME_IN_MS, null)))
                .andExpect(header(AUTHORIZATION_HEADER, DATABASE_BASIC_AUTH))
                .andRespond(withSuccess(jsonString, MediaType.APPLICATION_JSON));

        final Response response =
                objUnderTest.getInfraActiveRequests(Collections.emptyMap(), START_TIME_IN_MS, END_TIME_IN_MS, null);

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        final List<SoInfraRequest> actual = (List<SoInfraRequest>) response.getEntity();
        assertEquals(3, actual.size());

        final Map<String, SoInfraRequest> actualRequests = new HashMap<>();
        for (final SoInfraRequest soInfraRequest : actual) {
            actualRequests.put(soInfraRequest.getRequestId(), soInfraRequest);
        }
        final SoInfraRequest infraRequest = actualRequests.get("9383dc81-7a6c-4673-8082-650d50a82a1a");
        assertNull(infraRequest.getEndTime());
        assertEquals("IN_PROGRESS", infraRequest.getRequestStatus());
    }

    private String getJsonResponse(final Path path) throws IOException {
        return new String(Files.readAllBytes(path));
    }

}
