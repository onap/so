/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG.
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

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.constants.Status;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.db.request.data.repository.InfraActiveRequestsRepository;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.springframework.web.client.HttpClientErrorException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PauseOrchestrationRequestTest {

    @Mock
    private RequestHandlerUtils requestHandlerUtils;

    @Mock
    private InfraActiveRequestsRepository infraActiveRequestsRepository;

    @Mock
    private MsoRequest msoRequest;

    @Mock
    private ServiceInstances serviceInstances;

    @InjectMocks
    @Spy
    private PauseOrchestrationRequest pauseOrchestrationRequest;

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
    private final Timestamp startTimeStamp = new Timestamp(System.currentTimeMillis());
    private final Action action = Action.createInstance;
    private final Boolean aLaCarte = false;
    private final String version = "7";
    private final String requestUri = "http:localhost:6746/onap/so/infra/orchestrationRequests/v7/pause";
    private final RecipeLookupResult lookupResult = new RecipeLookupResult("/mso/async/services/WorkflowActionBB", 80);
    private RequestClientParameter requestClientParameter = null;
    private ObjectMapper mapper = new ObjectMapper();
    private InfraActiveRequests infraActiveRequest1 = new InfraActiveRequests();
    private InfraActiveRequests infraActiveRequest2 = new InfraActiveRequests();
    private InfraActiveRequests currentActiveRequest = new InfraActiveRequests();
    private List<InfraActiveRequests> infraActiveRequestsList = new ArrayList<>();
    private List<InfraActiveRequests> infraActiveRequestsListEmpty = new ArrayList<>();
    private ServiceInstancesRequest sir = new ServiceInstancesRequest();
    private String requestBody = null;
    private ContainerRequestContext requestContext = null;
    private HashMap<String, String> instanceIdMap = new HashMap<>();
    ModelInfo modelInfo;

    @Before
    public void setup() throws ValidateException, IOException {
        // Setup general requestHandler mocks
        when(requestHandlerUtils.getRequestUri(any(), anyString())).thenReturn(requestUri);

        // Setup InfraActiveRequests
        setInfraActiveRequest1();
        setInfraActiveRequest2();
        setCurrentActiveRequest();
        infraActiveRequestsList.add(infraActiveRequest1);
        infraActiveRequestsList.add(infraActiveRequest2);
        requestBody = infraActiveRequest1.getRequestBody();
        sir = mapper.readValue(requestBody, ServiceInstancesRequest.class);
        setRequestClientParameter();
        instanceIdMap.put("serviceInstanceId", SERVICE_INSTANCE_ID);
        modelInfo = sir.getRequestDetails().getModelInfo();
    }

    public String getRequestBody(String request) throws IOException {
        request = "src/test/resources/ResumeOrchestrationRequest" + request;
        return new String(Files.readAllBytes(Paths.get(request)));
    }

    private void setInfraActiveRequest1() throws IOException {
        infraActiveRequest1.setTenantId("tenant-id");
        infraActiveRequest1.setRequestBody(getRequestBody("/RequestBody.json"));
        infraActiveRequest1.setCloudRegion("cloudRegion");
        infraActiveRequest1.setRequestScope(SERVICE);
        infraActiveRequest1.setServiceInstanceId(SERVICE_INSTANCE_ID);
        infraActiveRequest1.setServiceInstanceName(SERVICE_INSTANCE_NAME);
        infraActiveRequest1.setRequestStatus(Status.IN_PROGRESS.toString());
        infraActiveRequest1.setRequestAction(Action.createInstance.toString());
        infraActiveRequest1.setServiceType("serviceType");
        infraActiveRequest1.setVnfId(VNF_ID);
        infraActiveRequest1.setVfModuleId(VFMODULE_ID);
        infraActiveRequest1.setNetworkId(NETWORK_ID);
        infraActiveRequest1.setVolumeGroupId(VOLUME_GROUP_ID);
        infraActiveRequest1.setVnfName(VNF_NAME);
        infraActiveRequest1.setVfModuleName(VFMODULE_NAME);
        infraActiveRequest1.setNetworkName(NETWORK_NAME);
        infraActiveRequest1.setVolumeGroupName(VOLUME_GROUP_NAME);
        infraActiveRequest1.setRequestId(REQUEST_ID);
    }

    private void setInfraActiveRequest2() throws IOException {
        infraActiveRequest2.setTenantId("tenant-id");
        infraActiveRequest2.setRequestBody(getRequestBody("/RequestBody.json"));
        infraActiveRequest2.setCloudRegion("cloudRegion");
        infraActiveRequest2.setRequestScope(SERVICE);
        infraActiveRequest2.setServiceInstanceId(SERVICE_INSTANCE_ID);
        infraActiveRequest2.setServiceInstanceName(SERVICE_INSTANCE_NAME);
        infraActiveRequest2.setRequestStatus(Status.IN_PROGRESS.toString());
        infraActiveRequest2.setRequestAction(Action.createInstance.toString());
        infraActiveRequest2.setServiceType("serviceType");
        infraActiveRequest2.setVnfId(VNF_ID);
        infraActiveRequest2.setVfModuleId(VFMODULE_ID);
        infraActiveRequest2.setNetworkId(NETWORK_ID);
        infraActiveRequest2.setVolumeGroupId(VOLUME_GROUP_ID);
        infraActiveRequest2.setVnfName(VNF_NAME);
        infraActiveRequest2.setVfModuleName(VFMODULE_NAME);
        infraActiveRequest2.setNetworkName(NETWORK_NAME);
        infraActiveRequest2.setVolumeGroupName(VOLUME_GROUP_NAME);
        infraActiveRequest2.setRequestId(REQUEST_ID);
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
        currentActiveRequest.setProgress(new Long(5));
    }

    private void setRequestClientParameter() throws IOException {
        requestClientParameter =
                new RequestClientParameter.Builder().setRequestId(CURRENT_REQUEST_ID).setRecipeTimeout(80)
                        .setRequestAction(Action.createInstance.toString()).setPnfCorrelationId("pnfCorrelationId")
                        .setVnfType(null).setNetworkType(null).setRequestDetails(requestBody).setApiVersion(version)
                        .setALaCarte(aLaCarte).setRequestUri(requestUri).setInstanceGroupId(null).build();
    }

    @Test
    public void pauseOrchestrationRequestTest() throws Exception {
        Response response = null;
        when(infraActiveRequestsRepository.getListOfRequestsInProgress()).thenReturn(infraActiveRequestsList);
        doReturn(currentActiveRequest).when(requestHandlerUtils).createNewRecordCopyForPauseAbort(anyList(),
                nullable(String.class), any(Timestamp.class), nullable(String.class), nullable(String.class),
                nullable(String.class));
        doReturn(response).when(pauseOrchestrationRequest).pauseRequest(anyList(), any(InfraActiveRequests.class),
                nullable(String.class), anyString());

        pauseOrchestrationRequest.pauseOrchestrationRequest("7", requestContext);

        verify(pauseOrchestrationRequest).pauseRequest(anyList(), any(InfraActiveRequests.class),
                nullable(String.class), anyString());
    }

    @Test
    public void pauseOrchestationRequestDbNullResultTest() throws Exception {
        when(infraActiveRequestsRepository.getListOfRequestsInProgress()).thenReturn(infraActiveRequestsListEmpty);

        thrown.expect(ValidateException.class);
        thrown.expectMessage("None of the SO(Service Instance) is InProgress status for Pause:");
        pauseOrchestrationRequest.pauseOrchestrationRequest("7", requestContext);
    }

    @Test
    public void pauseOrchestationRequestDbErrorTest() throws Exception {
        when(infraActiveRequestsRepository.getListOfRequestsInProgress()).thenThrow(HttpClientErrorException.class);

        thrown.expect(ValidateException.class);
        thrown.expectMessage("Exception while searching requests which are in progress ");
        pauseOrchestrationRequest.pauseOrchestrationRequest("7", requestContext);
    }

    @Test
    public void pauseRequestTest() throws ApiException, IOException {
        Response response = null;
        when(requestHandlerUtils.convertJsonToServiceInstanceRequest(anyString(), any(Actions.class), anyString(),
                anyString())).thenReturn(sir);
        when(serviceInstances.getPnfCorrelationId(any(ServiceInstancesRequest.class))).thenReturn("pnfCorrelationId");
        doReturn(lookupResult).when(requestHandlerUtils).getServiceInstanceOrchestrationURI(sir, action, aLaCarte,
                currentActiveRequest);
        doReturn(requestClientParameter).when(pauseOrchestrationRequest).setRequestClientParameter(lookupResult,
                version, currentActiveRequest, "pnfCorrelationId", sir, aLaCarte);

        doNothing().when(requestHandlerUtils).updateStatus(infraActiveRequest1, Status.PAUSED,
                "Service Instantiation is Paused from this : ");
        when(requestHandlerUtils.postBPELRequest(any(InfraActiveRequests.class), any(RequestClientParameter.class),
                anyString(), anyString())).thenReturn(response);

        pauseOrchestrationRequest.pauseRequest(infraActiveRequestsList, currentActiveRequest, requestUri, version);

        verify(requestHandlerUtils).postBPELRequest(currentActiveRequest, requestClientParameter,
                lookupResult.getOrchestrationURI(), infraActiveRequest1.getRequestScope());
    }

    @Test
    public void setRequestClientParameterTest() throws ApiException, IOException {
        // doReturn(ModelType.service).when(requestHandlerUtils).getModelType(action, modelInfo);
        when(requestHandlerUtils.mapJSONtoMSOStyle(anyString(), any(ServiceInstancesRequest.class), anyBoolean(),
                any(Action.class))).thenReturn(requestBody);
        RequestClientParameter result = pauseOrchestrationRequest.setRequestClientParameter(lookupResult, version,
                currentActiveRequest, "pnfCorrelationId", sir, aLaCarte);
        assertThat(requestClientParameter, sameBeanAs(result));
    }
}
