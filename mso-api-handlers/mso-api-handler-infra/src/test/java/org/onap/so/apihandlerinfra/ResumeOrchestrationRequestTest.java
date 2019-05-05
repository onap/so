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

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.HashMap;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.apihandler.common.RequestClientParameter;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.apihandlerinfra.exceptions.DuplicateRequestException;
import org.onap.so.apihandlerinfra.exceptions.RequestDbFailureException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.springframework.web.client.HttpClientErrorException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(MockitoJUnitRunner.class)
public class ResumeOrchestrationRequestTest {
    @Mock
    private RequestHandlerUtils requestHandler;

    @Mock
    private MsoRequest msoRequest;

    @Mock
    private ServiceInstances serviceInstances;

    @Mock
    private RequestsDbClient requestDbClient;

    @InjectMocks
    @Spy
    private ResumeOrchestrationRequest resumeReq;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final String CURRENT_REQUEST_ID = "eca3a1b1-43ab-457e-ab1c-367263d148b4";
    private static final String REQUEST_ID = "00032ab7-na18-42e5-965d-8ea592502019";
    private static final String SERVICE_INSTANCE_ID = "00032ab7-na18-42e5-965d-8ea592502018";
    private static final String SERVICE_INSTANCE_NAME = "serviceInstanceName";
    private static final String REQUEST_SCOPE = "service";
    private final Timestamp startTimeStamp = new Timestamp(System.currentTimeMillis());
    private final Action action = Action.createInstance;
    private final Boolean aLaCarte = false;
    private final String version = "7";
    private final String requestUri =
            "http:localhost:6746/onap/so/infra/orchestrationRequests/v7/00032ab7-na18-42e5-965d-8ea592502019/resume";
    private final RecipeLookupResult lookupResult = new RecipeLookupResult("/mso/async/services/WorkflowActionBB", 80);
    private RequestClientParameter requestClientParameter = null;
    private ObjectMapper mapper = new ObjectMapper();
    private InfraActiveRequests infraActiveRequest = new InfraActiveRequests();
    private InfraActiveRequests currentActiveRequest = new InfraActiveRequests();
    private ServiceInstancesRequest sir = new ServiceInstancesRequest();
    private ServiceInstancesRequest sirNullALaCarte = new ServiceInstancesRequest();
    private String requestBody = null;
    private String requestBodyNullALaCarte = null;
    private ContainerRequestContext requestContext = null;
    private HashMap<String, String> instanceIdMap = new HashMap<>();


    @Before
    public void setup() throws ValidateException, IOException {
        // Setup general requestHandler mocks
        when(requestHandler.getRequestUri(any(), anyString())).thenReturn(requestUri);

        // Setup InfraActiveRequests
        setInfraActiveRequest();
        setCurrentActiveRequest();

        requestBody = infraActiveRequest.getRequestBody();
        sir = mapper.readValue(requestBody, ServiceInstancesRequest.class);
        requestBodyNullALaCarte = getRequestBody("/ALaCarteNull.json");
        sirNullALaCarte = sir = mapper.readValue(requestBodyNullALaCarte, ServiceInstancesRequest.class);
        setRequestClientParameter();
        instanceIdMap.put("serviceInstanceId", SERVICE_INSTANCE_ID);
    }

    public String getRequestBody(String request) throws IOException {
        request = "src/test/resources/ResumeOrchestrationRequest" + request;
        return new String(Files.readAllBytes(Paths.get(request)));
    }

    private void setInfraActiveRequest() throws IOException {
        infraActiveRequest.setTenantId("tenant-id");
        infraActiveRequest.setRequestBody(getRequestBody("/RequestBody.json"));
        infraActiveRequest.setAicCloudRegion("cloudRegion");
        infraActiveRequest.setRequestScope(REQUEST_SCOPE);
        infraActiveRequest.setServiceInstanceId(SERVICE_INSTANCE_ID);
        infraActiveRequest.setServiceInstanceName(SERVICE_INSTANCE_NAME);
        infraActiveRequest.setRequestStatus(Status.IN_PROGRESS.toString());
        infraActiveRequest.setRequestAction(Action.createInstance.toString());
        infraActiveRequest.setServiceType("serviceType");
    }

    private void setCurrentActiveRequest() throws IOException {
        currentActiveRequest.setRequestId(CURRENT_REQUEST_ID);
        currentActiveRequest.setSource("VID");
        currentActiveRequest.setStartTime(startTimeStamp);
        currentActiveRequest.setTenantId("tenant-id");
        currentActiveRequest.setRequestBody(getRequestBody("/RequestBody.json"));
        currentActiveRequest.setAicCloudRegion("cloudRegion");
        currentActiveRequest.setRequestScope(REQUEST_SCOPE);
        currentActiveRequest.setServiceInstanceId(SERVICE_INSTANCE_ID);
        currentActiveRequest.setServiceInstanceName(SERVICE_INSTANCE_NAME);
        currentActiveRequest.setRequestStatus(Status.IN_PROGRESS.toString());
        currentActiveRequest.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);
        currentActiveRequest.setRequestAction(Action.createInstance.toString());
        currentActiveRequest.setRequestUrl(requestUri);
        currentActiveRequest.setRequestorId("xxxxxx");
        currentActiveRequest.setProgress(new Long(5));
    }

    private void setRequestClientParameter() {
        requestClientParameter = new RequestClientParameter.Builder().setRequestId(CURRENT_REQUEST_ID)
                .setRecipeTimeout(80).setRequestAction(Action.createInstance.toString())
                .setServiceInstanceId(SERVICE_INSTANCE_ID).setPnfCorrelationId("pnfCorrelationId").setVnfId(null)
                .setVfModuleId(null).setVolumeGroupId(null).setNetworkId(null).setServiceType("serviceType")
                .setVnfType(null).setNetworkType(null).setRequestDetails(requestBody).setApiVersion(version)
                .setALaCarte(aLaCarte).setRequestUri(requestUri).setInstanceGroupId(null).build();
    }

    @Test
    public void resumeOrchestationRequestTest() throws Exception {
        Response response = null;
        when(requestDbClient.getInfraActiveRequestbyRequestId(REQUEST_ID)).thenReturn(infraActiveRequest);
        doReturn(currentActiveRequest).when(requestHandler).createNewRecordCopyFromInfraActiveRequest(
                any(InfraActiveRequests.class), nullable(String.class), any(Timestamp.class), nullable(String.class),
                nullable(String.class), nullable(String.class));
        doReturn(response).when(resumeReq).resumeRequest(any(InfraActiveRequests.class), any(InfraActiveRequests.class),
                anyString(), nullable(String.class));

        resumeReq.resumeOrchestrationRequest(REQUEST_ID, "v7", requestContext);

        verify(resumeReq).resumeRequest(infraActiveRequest, currentActiveRequest, version, null);
    }

    @Test
    public void resumeOrchestationRequestDbNullResultTest() throws Exception {
        when(requestDbClient.getInfraActiveRequestbyRequestId("00032ab7-na18-42e5-965d-8ea592502018")).thenReturn(null);

        thrown.expect(ValidateException.class);
        thrown.expectMessage(
                "Null response from requestDB when searching by requestId: 00032ab7-na18-42e5-965d-8ea592502018");
        resumeReq.resumeOrchestrationRequest("00032ab7-na18-42e5-965d-8ea592502018", "v7", requestContext);
    }

    @Test
    public void resumeOrchestationRequestDbErrorTest() throws Exception {
        when(requestDbClient.getInfraActiveRequestbyRequestId("00032ab7-na18-42e5-965d-8ea592502018"))
                .thenThrow(HttpClientErrorException.class);

        thrown.expect(ValidateException.class);
        thrown.expectMessage("Exception while performing requestDb lookup by requestId");
        resumeReq.resumeOrchestrationRequest("00032ab7-na18-42e5-965d-8ea592502018", "v7", requestContext);
    }

    @Test
    public void resumeRequestTest() throws ApiException, IOException {
        Response response = null;
        when(requestHandler.convertJsonToServiceInstanceRequest(anyString(), any(Actions.class), anyString(),
                anyString())).thenReturn(sir);
        when(serviceInstances.getPnfCorrelationId(any(ServiceInstancesRequest.class))).thenReturn("pnfCorrelationId");
        doReturn(lookupResult).when(resumeReq).serviceRecipeLookup(currentActiveRequest, sir, action, aLaCarte);
        doReturn(requestClientParameter).when(resumeReq).setRequestClientParameter(lookupResult, version,
                infraActiveRequest, currentActiveRequest, "pnfCorrelationId", aLaCarte, sir);
        doNothing().when(resumeReq).requestDbSave(currentActiveRequest);
        when(requestHandler.postBPELRequest(any(InfraActiveRequests.class), any(RequestClientParameter.class),
                anyString(), anyString())).thenReturn(response);
        doNothing().when(resumeReq).checkForInProgressRequest(currentActiveRequest, SERVICE_INSTANCE_ID, REQUEST_SCOPE,
                SERVICE_INSTANCE_NAME, action);

        resumeReq.resumeRequest(infraActiveRequest, currentActiveRequest, version,
                "/onap/so/infra/orchestrationRequests/v7/requests/00032ab7-na18-42e5-965d-8ea592502018/resume");
        verify(requestHandler).postBPELRequest(currentActiveRequest, requestClientParameter,
                lookupResult.getOrchestrationURI(), infraActiveRequest.getRequestScope());
    }

    @Test
    public void serviceRecipeLookupTest() throws ApiException, IOException {
        when(serviceInstances.getServiceURI(any(ServiceInstancesRequest.class), any(Action.class), anyBoolean()))
                .thenReturn(lookupResult);
        RecipeLookupResult result = resumeReq.serviceRecipeLookup(currentActiveRequest, sir, action, aLaCarte);
        assertThat(result, sameBeanAs(lookupResult));
    }

    @Test
    public void setRequestClientParameterTest() throws ApiException, IOException {
        when(requestHandler.mapJSONtoMSOStyle(anyString(), any(ServiceInstancesRequest.class), anyBoolean(),
                any(Action.class))).thenReturn(requestBody);
        RequestClientParameter result = resumeReq.setRequestClientParameter(lookupResult, version, infraActiveRequest,
                currentActiveRequest, "pnfCorrelationId", aLaCarte, sir);
        assertThat(requestClientParameter, sameBeanAs(result));
    }

    @Test
    public void requestDbSaveTest() throws RequestDbFailureException {
        doNothing().when(requestDbClient).save(currentActiveRequest);
        resumeReq.requestDbSave(currentActiveRequest);
        verify(requestDbClient).save(currentActiveRequest);
    }

    @Test
    public void resumeRequestTestALaCarteNull() throws ApiException, IOException {
        Response response = null;

        when(requestHandler.convertJsonToServiceInstanceRequest(anyString(), any(Actions.class), anyString(),
                anyString())).thenReturn(sirNullALaCarte);
        when(serviceInstances.getPnfCorrelationId(any(ServiceInstancesRequest.class))).thenReturn("pnfCorrelationId");
        doReturn(lookupResult).when(resumeReq).serviceRecipeLookup(currentActiveRequest, sir, action, aLaCarte);
        doReturn(requestClientParameter).when(resumeReq).setRequestClientParameter(lookupResult, version,
                infraActiveRequest, currentActiveRequest, "pnfCorrelationId", aLaCarte, sir);
        doNothing().when(resumeReq).requestDbSave(currentActiveRequest);
        when(requestHandler.postBPELRequest(any(InfraActiveRequests.class), any(RequestClientParameter.class),
                anyString(), anyString())).thenReturn(response);
        doNothing().when(resumeReq).checkForInProgressRequest(currentActiveRequest, SERVICE_INSTANCE_ID, REQUEST_SCOPE,
                SERVICE_INSTANCE_NAME, action);

        resumeReq.resumeRequest(infraActiveRequest, currentActiveRequest, version,
                "/onap/so/infra/orchestrationRequests/v7/requests/00032ab7-na18-42e5-965d-8ea592502018/resume");
        verify(requestHandler).postBPELRequest(currentActiveRequest, requestClientParameter,
                lookupResult.getOrchestrationURI(), infraActiveRequest.getRequestScope());
    }

    @Test
    public void serviceRecipeLookupErrorTest() throws IOException, ApiException {
        when(serviceInstances.getServiceURI(sir, action, aLaCarte))
                .thenThrow(new IOException("Error occurred on recipe lookup"));
        doNothing().when(requestHandler).updateStatus(any(InfraActiveRequests.class), any(Status.class), anyString());

        thrown.expect(ValidateException.class);
        thrown.expectMessage("Error occurred on recipe lookup");
        resumeReq.serviceRecipeLookup(currentActiveRequest, sir, action, aLaCarte);
    }

    @Test
    public void setRequestClientParameterErrorTest() throws ApiException, IOException {
        when(requestHandler.mapJSONtoMSOStyle(anyString(), any(ServiceInstancesRequest.class), anyBoolean(),
                any(Action.class))).thenThrow(new IOException("IOException"));

        thrown.expect(ValidateException.class);
        thrown.expectMessage("IOException while generating requestClientParameter to send to BPMN: IOException");
        resumeReq.setRequestClientParameter(lookupResult, version, infraActiveRequest, currentActiveRequest,
                "pnfCorrelationId", aLaCarte, sir);
    }

    @Test
    public void checkForInProgressRequest() throws ApiException {
        doReturn(infraActiveRequest).when(requestHandler).duplicateCheck(action, instanceIdMap, SERVICE_INSTANCE_NAME,
                REQUEST_SCOPE, currentActiveRequest);
        doReturn(true).when(requestHandler).camundaHistoryCheck(infraActiveRequest, currentActiveRequest);
        doThrow(DuplicateRequestException.class).when(requestHandler).buildErrorOnDuplicateRecord(currentActiveRequest,
                action, instanceIdMap, SERVICE_INSTANCE_NAME, REQUEST_SCOPE, infraActiveRequest);

        thrown.expect(DuplicateRequestException.class);
        resumeReq.checkForInProgressRequest(currentActiveRequest, SERVICE_INSTANCE_ID, REQUEST_SCOPE,
                SERVICE_INSTANCE_NAME, action);
    }

    @Test
    public void checkForInProgressRequestNoInProgressRequests() throws ApiException {
        doReturn(null).when(requestHandler).duplicateCheck(action, instanceIdMap, SERVICE_INSTANCE_NAME, REQUEST_SCOPE,
                currentActiveRequest);

        resumeReq.checkForInProgressRequest(currentActiveRequest, SERVICE_INSTANCE_ID, REQUEST_SCOPE,
                SERVICE_INSTANCE_NAME, action);
        verify(requestHandler).duplicateCheck(action, instanceIdMap, SERVICE_INSTANCE_NAME, REQUEST_SCOPE,
                currentActiveRequest);
    }

    @Test
    public void checkForInProgressRequestCamundaHistoryCheckReturnsNoInProgress() throws ApiException {
        doReturn(infraActiveRequest).when(requestHandler).duplicateCheck(action, instanceIdMap, SERVICE_INSTANCE_NAME,
                REQUEST_SCOPE, currentActiveRequest);
        doReturn(false).when(requestHandler).camundaHistoryCheck(infraActiveRequest, currentActiveRequest);

        resumeReq.checkForInProgressRequest(currentActiveRequest, SERVICE_INSTANCE_ID, REQUEST_SCOPE,
                SERVICE_INSTANCE_NAME, action);
        verify(requestHandler).duplicateCheck(action, instanceIdMap, SERVICE_INSTANCE_NAME, REQUEST_SCOPE,
                currentActiveRequest);
        verify(requestHandler).camundaHistoryCheck(infraActiveRequest, currentActiveRequest);
    }


}
