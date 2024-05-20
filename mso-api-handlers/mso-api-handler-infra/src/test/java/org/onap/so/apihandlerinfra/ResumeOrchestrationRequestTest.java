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
import static org.junit.Assert.assertEquals;
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
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
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
import org.onap.so.constants.Status;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
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
    private static final String VNF_ID = "00032ab7-na18-42e5-965d-8ea592502017";
    private static final String VFMODULE_ID = "00032ab7-na18-42e5-965d-8ea592502016";
    private static final String NETWORK_ID = "00032ab7-na18-42e5-965d-8ea592502015";
    private static final String VOLUME_GROUP_ID = "00032ab7-na18-42e5-965d-8ea592502014";
    private static final String SERVICE_INSTANCE_NAME = "serviceInstanceName";
    private static final String VNF_NAME = "vnfName";
    private static final String VFMODULE_NAME = "vfModuleName";
    private static final String NETWORK_NAME = "networkName";
    private static final String VOLUME_GROUP_NAME = "volumeGroupName";
    private static final String SERVICE = "service";
    private static final String VNF = "vnf";
    private static final String VFMODULE = "vfModule";
    private static final String NETWORK = "network";
    private static final String VOLUME_GROUP = "volumeGroup";
    private final Timestamp startTimeStamp = new Timestamp(System.currentTimeMillis());
    private final Action action = Action.createInstance;
    private final Boolean aLaCarte = false;
    private final String version = "7";
    private final String requestUri =
            "http:localhost:6746/onap/so/infra/orchestrationRequests/v7/00032ab7-na18-42e5-965d-8ea592502019/resume";
    private final RecipeLookupResult lookupResult = new RecipeLookupResult("/mso/async/services/WorkflowActionBB", 80);
    private RequestClientParameter requestClientParameter = null;
    private RequestClientParameter requestClientParameterVfModule = null;
    private ObjectMapper mapper = new ObjectMapper();
    private InfraActiveRequests infraActiveRequest = new InfraActiveRequests();
    private InfraActiveRequests currentActiveRequest = new InfraActiveRequests();
    private InfraActiveRequests infraActiveRequestVfModule = new InfraActiveRequests();
    private ServiceInstancesRequest sir = new ServiceInstancesRequest();
    private ServiceInstancesRequest sirNullALaCarte = new ServiceInstancesRequest();
    private String requestBody = null;
    private String requestBodyNullALaCarte = null;
    private ContainerRequestContext requestContext = null;
    private HashMap<String, String> instanceIdMap = new HashMap<>();
    ModelInfo modelInfo;


    @Before
    public void setup() throws ValidateException, IOException {
        // Setup general requestHandler mocks
        when(requestHandler.getRequestUri(any(), anyString())).thenReturn(requestUri);

        // Setup InfraActiveRequests
        setInfraActiveRequest();
        setCurrentActiveRequest();
        setInfraActiveRequestVfModule();

        requestBody = infraActiveRequest.getRequestBody();
        sir = mapper.readValue(requestBody, ServiceInstancesRequest.class);
        requestBodyNullALaCarte = getRequestBody("/ALaCarteNull.json");
        sirNullALaCarte = mapper.readValue(requestBodyNullALaCarte, ServiceInstancesRequest.class);
        setRequestClientParameter();
        setRequestClientParameterVfModule();
        instanceIdMap.put("serviceInstanceId", SERVICE_INSTANCE_ID);
        modelInfo = sir.getRequestDetails().getModelInfo();
    }

    public String getRequestBody(String request) throws IOException {
        request = "src/test/resources/ResumeOrchestrationRequest" + request;
        return new String(Files.readAllBytes(Paths.get(request)));
    }

    private void setInfraActiveRequest() throws IOException {
        infraActiveRequest.setTenantId("tenant-id");
        infraActiveRequest.setRequestBody(getRequestBody("/RequestBody.json"));
        infraActiveRequest.setCloudRegion("cloudRegion");
        infraActiveRequest.setRequestScope(SERVICE);
        infraActiveRequest.setServiceInstanceId(SERVICE_INSTANCE_ID);
        infraActiveRequest.setServiceInstanceName(SERVICE_INSTANCE_NAME);
        infraActiveRequest.setRequestStatus(Status.IN_PROGRESS.toString());
        infraActiveRequest.setRequestAction(Action.createInstance.toString());
        infraActiveRequest.setServiceType("serviceType");
        infraActiveRequest.setVnfId(VNF_ID);
        infraActiveRequest.setVfModuleId(VFMODULE_ID);
        infraActiveRequest.setNetworkId(NETWORK_ID);
        infraActiveRequest.setVolumeGroupId(VOLUME_GROUP_ID);
        infraActiveRequest.setVnfName(VNF_NAME);
        infraActiveRequest.setVfModuleName(VFMODULE_NAME);
        infraActiveRequest.setNetworkName(NETWORK_NAME);
        infraActiveRequest.setVolumeGroupName(VOLUME_GROUP_NAME);
        infraActiveRequest.setRequestId(REQUEST_ID);
    }

    private void setCurrentActiveRequest() throws IOException {
        currentActiveRequest.setRequestId(CURRENT_REQUEST_ID);
        currentActiveRequest.setSource("VID");
        currentActiveRequest.setStartTime(startTimeStamp);
        currentActiveRequest.setTenantId("tenant-id");
        currentActiveRequest.setRequestBody(getRequestBody("/RequestBody.json"));
        currentActiveRequest.setCloudRegion("cloudRegion");
        currentActiveRequest.setRequestScope(SERVICE);
        currentActiveRequest.setServiceInstanceId(SERVICE_INSTANCE_ID);
        currentActiveRequest.setServiceInstanceName(SERVICE_INSTANCE_NAME);
        currentActiveRequest.setRequestStatus(Status.IN_PROGRESS.toString());
        currentActiveRequest.setLastModifiedBy(Constants.MODIFIED_BY_APIHANDLER);
        currentActiveRequest.setRequestAction(Action.createInstance.toString());
        currentActiveRequest.setRequestUrl(requestUri);
        currentActiveRequest.setRequestorId("xxxxxx");
        // currentActiveRequest.setProgress(new Long(5));
        Long value = Long.valueOf(5);
        currentActiveRequest.setProgress(value);
    }

    private void setInfraActiveRequestVfModule() throws IOException {
        infraActiveRequestVfModule.setRequestBody(getRequestBody("/RequestBody.json"));
        infraActiveRequestVfModule.setRequestScope(VFMODULE);
        infraActiveRequestVfModule.setRequestStatus(Status.IN_PROGRESS.toString());
        infraActiveRequestVfModule.setRequestAction(Action.createInstance.toString());
        infraActiveRequestVfModule.setVnfId(VNF_ID);
        infraActiveRequestVfModule.setVfModuleId(VFMODULE_ID);
        infraActiveRequestVfModule.setVnfName(VNF_NAME);
        infraActiveRequestVfModule.setVfModuleName(VFMODULE_NAME);
    }

    private void setRequestClientParameter() {
        requestClientParameter =
                new RequestClientParameter.Builder().setRequestId(CURRENT_REQUEST_ID).setRecipeTimeout(80)
                        .setRequestAction(Action.createInstance.toString()).setServiceInstanceId(SERVICE_INSTANCE_ID)
                        .setPnfCorrelationId("pnfCorrelationId").setVnfId(VNF_ID).setVfModuleId(VFMODULE_ID)
                        .setVolumeGroupId(VOLUME_GROUP_ID).setNetworkId(NETWORK_ID).setServiceType("serviceType")
                        .setVnfType(null).setNetworkType(null).setRequestDetails(requestBody).setApiVersion(version)
                        .setALaCarte(aLaCarte).setRequestUri(requestUri).setInstanceGroupId(null).build();
    }

    private void setRequestClientParameterVfModule() {
        requestClientParameterVfModule =
                new RequestClientParameter.Builder().setRequestId(CURRENT_REQUEST_ID).setRecipeTimeout(80)
                        .setRequestAction(Action.createInstance.toString()).setPnfCorrelationId("pnfCorrelationId")
                        .setVnfId(VNF_ID).setVfModuleId(VFMODULE_ID).setRequestDetails(requestBody)
                        .setApiVersion(version).setALaCarte(aLaCarte).setRequestUri(requestUri).build();
    }

    @Test
    public void resumeOrchestationRequestTest() throws Exception {
        Response response = null;
        when(requestDbClient.getInfraActiveRequestbyRequestId(REQUEST_ID)).thenReturn(infraActiveRequest);
        doReturn(currentActiveRequest).when(requestHandler).createNewRecordCopyFromInfraActiveRequest(
                any(InfraActiveRequests.class), nullable(String.class), any(Timestamp.class), nullable(String.class),
                nullable(String.class), nullable(String.class), anyString());
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
        doReturn(instanceIdMap).when(resumeReq).setInstanceIdMap(infraActiveRequest, ModelType.service.toString());
        doReturn(SERVICE_INSTANCE_NAME).when(resumeReq).getInstanceName(infraActiveRequest,
                ModelType.service.toString(), currentActiveRequest);
        when(requestHandler.convertJsonToServiceInstanceRequest(anyString(), any(Actions.class), anyString(),
                anyString())).thenReturn(sir);
        when(serviceInstances.getPnfCorrelationId(any(ServiceInstancesRequest.class))).thenReturn("pnfCorrelationId");
        doReturn(lookupResult).when(requestHandler).getServiceInstanceOrchestrationURI(sir, action, aLaCarte,
                currentActiveRequest);
        doReturn(requestClientParameter).when(resumeReq).setRequestClientParameter(lookupResult, version,
                infraActiveRequest, currentActiveRequest, "pnfCorrelationId", aLaCarte, sir);
        doNothing().when(resumeReq).requestDbSave(currentActiveRequest);
        when(requestHandler.postBPELRequest(any(InfraActiveRequests.class), any(RequestClientParameter.class),
                anyString(), anyString())).thenReturn(response);
        doNothing().when(resumeReq).checkForInProgressRequest(currentActiveRequest, instanceIdMap, SERVICE,
                SERVICE_INSTANCE_NAME, action);

        resumeReq.resumeRequest(infraActiveRequest, currentActiveRequest, version,
                "/onap/so/infra/orchestrationRequests/v7/requests/00032ab7-na18-42e5-965d-8ea592502018/resume");
        verify(requestHandler).postBPELRequest(currentActiveRequest, requestClientParameter,
                lookupResult.getOrchestrationURI(), infraActiveRequest.getRequestScope());
    }

    @Test
    public void setRequestClientParameterTest() throws ApiException, IOException {
        doReturn(ModelType.service).when(requestHandler).getModelType(action, modelInfo);
        when(requestHandler.mapJSONtoMSOStyle(anyString(), any(ServiceInstancesRequest.class), anyBoolean(),
                any(Action.class))).thenReturn(requestBody);
        RequestClientParameter result = resumeReq.setRequestClientParameter(lookupResult, version, infraActiveRequest,
                currentActiveRequest, "pnfCorrelationId", aLaCarte, sir);
        assertThat(requestClientParameter, sameBeanAs(result));
    }

    @Test
    public void setRequestClientParameterVfModuleTest() throws ApiException, IOException {
        when(requestHandler.mapJSONtoMSOStyle(anyString(), any(ServiceInstancesRequest.class), anyBoolean(),
                any(Action.class))).thenReturn(requestBody);
        doReturn(ModelType.vfModule).when(requestHandler).getModelType(action, modelInfo);
        RequestClientParameter result = resumeReq.setRequestClientParameter(lookupResult, version,
                infraActiveRequestVfModule, currentActiveRequest, "pnfCorrelationId", aLaCarte, sir);
        assertThat(requestClientParameterVfModule, sameBeanAs(result));
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
        doReturn(instanceIdMap).when(resumeReq).setInstanceIdMap(infraActiveRequest, ModelType.service.toString());
        doReturn(SERVICE_INSTANCE_NAME).when(resumeReq).getInstanceName(infraActiveRequest,
                ModelType.service.toString(), currentActiveRequest);
        when(requestHandler.convertJsonToServiceInstanceRequest(anyString(), any(Actions.class), anyString(),
                anyString())).thenReturn(sirNullALaCarte);
        when(serviceInstances.getPnfCorrelationId(any(ServiceInstancesRequest.class))).thenReturn("pnfCorrelationId");
        doReturn(false).when(msoRequest).getAlacarteFlag(sirNullALaCarte);
        doReturn(lookupResult).when(requestHandler).getServiceInstanceOrchestrationURI(sirNullALaCarte, action, false,
                currentActiveRequest);
        doReturn(requestClientParameter).when(resumeReq).setRequestClientParameter(lookupResult, version,
                infraActiveRequest, currentActiveRequest, "pnfCorrelationId", aLaCarte, sirNullALaCarte);
        doReturn(false).when(resumeReq).setALaCarteFlagIfNull(SERVICE, action);
        doNothing().when(resumeReq).requestDbSave(currentActiveRequest);
        when(requestHandler.postBPELRequest(any(InfraActiveRequests.class), any(RequestClientParameter.class),
                anyString(), anyString())).thenReturn(response);
        doNothing().when(resumeReq).checkForInProgressRequest(currentActiveRequest, instanceIdMap, SERVICE,
                SERVICE_INSTANCE_NAME, action);

        resumeReq.resumeRequest(infraActiveRequest, currentActiveRequest, version,
                "/onap/so/infra/orchestrationRequests/v7/requests/00032ab7-na18-42e5-965d-8ea592502018/resume");
        verify(requestHandler).postBPELRequest(currentActiveRequest, requestClientParameter,
                lookupResult.getOrchestrationURI(), infraActiveRequest.getRequestScope());
    }

    @Test
    public void setRequestClientParameterErrorTest() throws ApiException, IOException {
        doReturn(ModelType.service).when(requestHandler).getModelType(action, modelInfo);
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
                SERVICE, currentActiveRequest);
        doReturn(true).when(requestHandler).camundaHistoryCheck(infraActiveRequest, currentActiveRequest);
        doThrow(DuplicateRequestException.class).when(requestHandler).buildErrorOnDuplicateRecord(currentActiveRequest,
                action, instanceIdMap, SERVICE_INSTANCE_NAME, SERVICE, infraActiveRequest);

        thrown.expect(DuplicateRequestException.class);
        resumeReq.checkForInProgressRequest(currentActiveRequest, instanceIdMap, SERVICE, SERVICE_INSTANCE_NAME,
                action);
    }

    @Test
    public void checkForInProgressRequestNoInProgressRequests() throws ApiException {
        doReturn(null).when(requestHandler).duplicateCheck(action, instanceIdMap, SERVICE_INSTANCE_NAME, SERVICE,
                currentActiveRequest);

        resumeReq.checkForInProgressRequest(currentActiveRequest, instanceIdMap, SERVICE, SERVICE_INSTANCE_NAME,
                action);
        verify(requestHandler).duplicateCheck(action, instanceIdMap, SERVICE_INSTANCE_NAME, SERVICE,
                currentActiveRequest);
    }

    @Test
    public void checkForInProgressRequestCamundaHistoryCheckReturnsNoInProgress() throws ApiException {
        doReturn(infraActiveRequest).when(requestHandler).duplicateCheck(action, instanceIdMap, SERVICE_INSTANCE_NAME,
                SERVICE, currentActiveRequest);
        doReturn(false).when(requestHandler).camundaHistoryCheck(infraActiveRequest, currentActiveRequest);

        resumeReq.checkForInProgressRequest(currentActiveRequest, instanceIdMap, SERVICE, SERVICE_INSTANCE_NAME,
                action);
        verify(requestHandler).duplicateCheck(action, instanceIdMap, SERVICE_INSTANCE_NAME, SERVICE,
                currentActiveRequest);
        verify(requestHandler).camundaHistoryCheck(infraActiveRequest, currentActiveRequest);
    }

    @Test
    public void setInstanceIdMapServiceTest() {
        HashMap<String, String> expected = new HashMap<>();
        expected.put("serviceInstanceId", SERVICE_INSTANCE_ID);
        HashMap<String, String> result = resumeReq.setInstanceIdMap(infraActiveRequest, SERVICE);
        assertEquals(result, expected);
    }

    @Test
    public void setInstanceIdMapRequestScopeNotValidTest() {
        HashMap<String, String> expected = new HashMap<>();
        HashMap<String, String> result = resumeReq.setInstanceIdMap(infraActiveRequest, "test");
        assertEquals(result, expected);
    }

    @Test
    public void setInstanceIdMapVnfTest() {
        HashMap<String, String> expected = new HashMap<>();
        expected.put("vnfInstanceId", VNF_ID);
        HashMap<String, String> result = resumeReq.setInstanceIdMap(infraActiveRequest, VNF);
        assertEquals(result, expected);
    }

    @Test
    public void setInstanceIdMapVfModuleTest() {
        HashMap<String, String> expected = new HashMap<>();
        expected.put("vfModuleInstanceId", VFMODULE_ID);
        HashMap<String, String> result = resumeReq.setInstanceIdMap(infraActiveRequest, VFMODULE);
        assertEquals(result, expected);
    }

    @Test
    public void setInstanceIdMapNetworkTest() {
        HashMap<String, String> expected = new HashMap<>();
        expected.put("networkInstanceId", NETWORK_ID);
        HashMap<String, String> result = resumeReq.setInstanceIdMap(infraActiveRequest, NETWORK);
        assertEquals(result, expected);
    }

    @Test
    public void setInstanceIdMapVolumeGroupTest() {
        HashMap<String, String> expected = new HashMap<>();
        expected.put("volumeGroupInstanceId", VOLUME_GROUP_ID);
        HashMap<String, String> result = resumeReq.setInstanceIdMap(infraActiveRequest, VOLUME_GROUP);
        assertEquals(result, expected);
    }

    @Test
    public void setInstanceNameServiceTest() throws ValidateException, RequestDbFailureException {
        String result = resumeReq.getInstanceName(infraActiveRequest, SERVICE, currentActiveRequest);
        assertEquals(result, SERVICE_INSTANCE_NAME);
    }

    @Test
    public void setInstanceNameRequestScopeNotValidTest() throws ValidateException, RequestDbFailureException {
        thrown.expect(ValidateException.class);
        thrown.expectMessage(
                "requestScope: \"test\" from request: 00032ab7-na18-42e5-965d-8ea592502019 does not match a ModelType enum.");

        resumeReq.getInstanceName(infraActiveRequest, "test", currentActiveRequest);
    }

    @Test
    public void setInstanceNameVnfTest() throws ValidateException, RequestDbFailureException {
        String result = resumeReq.getInstanceName(infraActiveRequest, VNF, currentActiveRequest);
        assertEquals(result, VNF_NAME);
    }

    @Test
    public void setInstanceNameVfModuleTest() throws ValidateException, RequestDbFailureException {
        String result = resumeReq.getInstanceName(infraActiveRequest, VFMODULE, currentActiveRequest);
        assertEquals(result, VFMODULE_NAME);
    }

    @Test
    public void setInstanceNameNetworkTest() throws ValidateException, RequestDbFailureException {
        String result = resumeReq.getInstanceName(infraActiveRequest, NETWORK, currentActiveRequest);
        assertEquals(result, NETWORK_NAME);
    }

    @Test
    public void setInstanceNameVolumeGroupTest() throws ValidateException, RequestDbFailureException {
        String result = resumeReq.getInstanceName(infraActiveRequest, VOLUME_GROUP, currentActiveRequest);
        assertEquals(result, VOLUME_GROUP_NAME);
    }

    @Test
    public void setALaCarteFlagIfNullTest() {
        Boolean aLaCarteFlag = resumeReq.setALaCarteFlagIfNull(SERVICE, action);
        assertEquals(aLaCarteFlag, false);
    }

    @Test
    public void setALaCarteFlagIfNullVnfTest() {
        Boolean aLaCarteFlag = resumeReq.setALaCarteFlagIfNull(VNF, action);
        assertEquals(aLaCarteFlag, true);
    }

    @Test
    public void setALaCarteFlagIfNullRecreateTest() {
        Boolean aLaCarteFlag = resumeReq.setALaCarteFlagIfNull(VNF, Action.recreateInstance);
        assertEquals(aLaCarteFlag, false);
    }
}
