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
import org.onap.so.serviceinstancebeans.ModelInfo;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AbortOrchestrationRequestTest {
    @Mock
    private RequestHandlerUtils requestHandlerUtils;

    @Mock
    private InfraActiveRequestsService infraActiveRequestsService;

    @Mock
    private MsoRequest msoRequest;

    @Mock
    private ServiceInstances serviceInstances;

    @Mock
    private PauseOrchestrationRequest pauseOrchestrationRequest;

    @InjectMocks
    @Spy
    private AbortOrchestrationRequest abortOrchestrationRequest;

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
    private final String requestUri = "http://localhost:6746/onap/so/infra/orchestrationRequests/v7/abort";
    private final RecipeLookupResult lookupResult = new RecipeLookupResult("/mso/async/services/WorkflowActionBB", 80);
    private RequestClientParameter requestClientParameter = null;
    private ObjectMapper mapper = new ObjectMapper();
    private InfraActiveRequests infraActiveRequests = new InfraActiveRequests();
    private InfraActiveRequests currentActiveRequest = new InfraActiveRequests();
    private List<InfraActiveRequests> infraActiveRequestsList = new ArrayList<>();
    private List<InfraActiveRequests> infraActiveRequestsListEmpty = new ArrayList<>();
    private ServiceInstancesRequest serviceInstancesRequest = new ServiceInstancesRequest();
    private String requestBody = null;
    private ContainerRequestContext requestContext = null;
    private HashMap<String, String> instanceIdMap = new HashMap<>();
    ModelInfo modelInfo;

    @Before
    public void setup() throws ValidateException, IOException {
        // Setup general requestHandler mocks
        when(requestHandlerUtils.getRequestUri(any(), anyString())).thenReturn(requestUri);

        // Setup InfraActiveRequests
        setInfraActiveRequest();
        setCurrentActiveRequest();
        setRequestClientParameter();
        infraActiveRequestsList.add(infraActiveRequests);

        requestBody = infraActiveRequests.getRequestBody();
        serviceInstancesRequest = mapper.readValue(requestBody, ServiceInstancesRequest.class);
        instanceIdMap.put("serviceInstanceId", SERVICE_INSTANCE_ID);
        modelInfo = serviceInstancesRequest.getRequestDetails().getModelInfo();
    }

    public String getRequestBody(String request) throws IOException {
        request = "src/test/resources/ResumeOrchestrationRequest" + request;
        return new String(Files.readAllBytes(Paths.get(request)));
    }

    private void setInfraActiveRequest() throws IOException {
        infraActiveRequests.setTenantId("tenant-id");
        infraActiveRequests.setRequestBody(getRequestBody("/RequestBody.json"));
        infraActiveRequests.setCloudRegion("cloudRegion");
        infraActiveRequests.setRequestScope(SERVICE);
        infraActiveRequests.setServiceInstanceId(SERVICE_INSTANCE_ID);
        infraActiveRequests.setServiceInstanceName(SERVICE_INSTANCE_NAME);
        infraActiveRequests.setRequestStatus(Status.IN_PROGRESS.toString());
        infraActiveRequests.setRequestAction(Action.createInstance.toString());
        infraActiveRequests.setServiceType("serviceType");
        infraActiveRequests.setVnfId(VNF_ID);
        infraActiveRequests.setVfModuleId(VFMODULE_ID);
        infraActiveRequests.setNetworkId(NETWORK_ID);
        infraActiveRequests.setVolumeGroupId(VOLUME_GROUP_ID);
        infraActiveRequests.setVnfName(VNF_NAME);
        infraActiveRequests.setVfModuleName(VFMODULE_NAME);
        infraActiveRequests.setNetworkName(NETWORK_NAME);
        infraActiveRequests.setVolumeGroupName(VOLUME_GROUP_NAME);
        infraActiveRequests.setRequestId(REQUEST_ID);
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
        requestClientParameter = new RequestClientParameter.Builder().setRequestId(REQUEST_ID).setRecipeTimeout(80)
                .setRequestAction(Action.createInstance.toString()).setServiceInstanceId(SERVICE_INSTANCE_ID)
                .setPnfCorrelationId("pnfCorrelationId").setVnfId(VNF_ID).setVfModuleId(VFMODULE_ID)
                .setVolumeGroupId(VOLUME_GROUP_ID).setNetworkId(NETWORK_ID).setServiceType("serviceType")
                .setVnfType(null).setNetworkType(null).setRequestDetails(requestBody).setApiVersion(version)
                .setALaCarte(aLaCarte).setRequestUri(requestUri).setInstanceGroupId(null).build();
    }

    @Test
    public void abortOrchestationRequestTest() throws Exception {
        Response response = null;
        when(infraActiveRequestsService.getListOfRequestsInProgress()).thenReturn(infraActiveRequestsList);
        doReturn(currentActiveRequest).when(requestHandlerUtils).createNewRecordCopyForPauseAbort(anyList(),
                nullable(String.class), any(Timestamp.class), nullable(String.class), nullable(String.class),
                nullable(String.class));
        doReturn(response).when(abortOrchestrationRequest).abortRequest(anyList(), any(InfraActiveRequests.class),
                nullable(String.class), anyString());

        abortOrchestrationRequest.abortOrchestrationRequest("7", requestContext);

        verify(abortOrchestrationRequest).abortRequest(anyList(), any(InfraActiveRequests.class),
                nullable(String.class), anyString());
    }

    @Test
    public void abortOrchestationRequestDbNullResultTest() throws Exception {
        when(infraActiveRequestsService.getListOfRequestsInProgress()).thenReturn(infraActiveRequestsListEmpty);

        thrown.expect(ValidateException.class);
        thrown.expectMessage("None of the SO(Service Instance) is InProgress status for Abort:");
        abortOrchestrationRequest.abortOrchestrationRequest("7", requestContext);
    }

    @Test
    public void abortOrchestationRequestDbErrorTest() throws Exception {
        when(infraActiveRequestsService.getListOfRequestsInProgress()).thenThrow(HttpClientErrorException.class);

        thrown.expect(ValidateException.class);
        thrown.expectMessage("Exception while searching requests which are in progress ");
        abortOrchestrationRequest.abortOrchestrationRequest("7", requestContext);
    }

    @Test
    public void abortRequestTest() throws ApiException, IOException {
        Response response = null;
        when(requestHandlerUtils.convertJsonToServiceInstanceRequest(anyString(), any(Actions.class), anyString(),
                anyString())).thenReturn(serviceInstancesRequest);
        when(serviceInstances.getPnfCorrelationId(any(ServiceInstancesRequest.class))).thenReturn("pnfCorrelationId");
        doReturn(lookupResult).when(requestHandlerUtils).getServiceInstanceOrchestrationURI(serviceInstancesRequest,
                action, aLaCarte, currentActiveRequest);
        doNothing().when(requestHandlerUtils).updateStatus(infraActiveRequests, Status.ABORTED,
                "Service instantiation is aborted");

        doReturn(requestClientParameter).when(pauseOrchestrationRequest).setRequestClientParameter(lookupResult,
                version, currentActiveRequest, "pnfCorrelationId", serviceInstancesRequest, aLaCarte);
        when(requestHandlerUtils.postBPELRequest(any(InfraActiveRequests.class), any(RequestClientParameter.class),
                anyString(), anyString())).thenReturn(response);

        abortOrchestrationRequest.abortRequest(infraActiveRequestsList, currentActiveRequest, requestUri, version);

        verify(requestHandlerUtils).postBPELRequest(currentActiveRequest, requestClientParameter,
                lookupResult.getOrchestrationURI(), infraActiveRequests.getRequestScope());
    }
}
