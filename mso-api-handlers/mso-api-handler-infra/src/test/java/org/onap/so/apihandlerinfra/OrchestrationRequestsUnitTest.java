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

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.apihandler.common.ResponseBuilder;
import org.onap.so.apihandlerinfra.exceptions.ApiException;
import org.onap.so.constants.OrchestrationRequestFormat;
import org.onap.so.constants.Status;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.serviceinstancebeans.InstanceReferences;
import org.onap.so.serviceinstancebeans.Request;
import org.onap.so.serviceinstancebeans.RequestStatus;

@RunWith(MockitoJUnitRunner.class)
public class OrchestrationRequestsUnitTest {
    @Mock
    private RequestsDbClient requestDbClient;
    @Mock
    private MsoRequest msoRequest;
    @Mock
    private ResponseBuilder builder;
    @Mock
    private Response response;
    @Mock
    private CamundaRequestHandler camundaRequestHandler;
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @InjectMocks
    private OrchestrationRequests orchestrationRequests;

    private static final String REQUEST_ID = "7cb9aa56-dd31-41e5-828e-d93027d4ebba";
    private static final String SERVICE_INSTANCE_ID = "7cb9aa56-dd31-41e5-828e-d93027d4ebbb";
    private static final String ORIGINAL_REQUEST_ID = "8f2d38a6-7c20-465a-bd7e-075645f1394b";
    private static final String SERVICE = "service";
    private static final String EXT_SYSTEM_ERROR_SOURCE = "external system error source";
    private static final String FLOW_STATUS = "FlowStatus";
    private static final String RETRY_STATUS_MESSAGE = "RetryStatusMessage";
    private static final String ROLLBACK_STATUS_MESSAGE = "RollbackStatusMessage";
    private static final String TASK_INFORMATION = " TASK INFORMATION: Last task executed: Call SDNC";
    private InfraActiveRequests iar;
    boolean includeCloudRequest = false;
    private static final String ROLLBACK_EXT_SYSTEM_ERROR_SOURCE = "SDNC";


    @Before
    public void setup() {
        iar = new InfraActiveRequests();
        iar.setRequestScope(SERVICE);
        iar.setRequestId(REQUEST_ID);
        iar.setServiceInstanceId(SERVICE_INSTANCE_ID);
        iar.setExtSystemErrorSource(EXT_SYSTEM_ERROR_SOURCE);
        iar.setRollbackExtSystemErrorSource(ROLLBACK_EXT_SYSTEM_ERROR_SOURCE);
        iar.setFlowStatus(FLOW_STATUS);
        iar.setRollbackStatusMessage(ROLLBACK_STATUS_MESSAGE);
        iar.setRetryStatusMessage(RETRY_STATUS_MESSAGE);
    }

    @Test
    public void mapInfraActiveRequestToRequestWithOriginalRequestIdTest() throws ApiException {
        doReturn("Last task executed: Call SDNC").when(camundaRequestHandler).getTaskName(REQUEST_ID);
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestState(iar.getRequestStatus());
        requestStatus.setStatusMessage(String.format("FLOW STATUS: %s RETRY STATUS: %s ROLLBACK STATUS: %s",
                FLOW_STATUS + TASK_INFORMATION, RETRY_STATUS_MESSAGE, ROLLBACK_STATUS_MESSAGE));

        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setOriginalRequestId(ORIGINAL_REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);

        iar.setOriginalRequestId(ORIGINAL_REQUEST_ID);

        Request result = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest,
                OrchestrationRequestFormat.DETAIL.toString());
        assertThat(result, sameBeanAs(expected));
    }

    @Test
    public void mapInfraActiveRequestToRequestOriginalRequestIdNullTest() throws ApiException {
        doReturn("Last task executed: Call SDNC").when(camundaRequestHandler).getTaskName(REQUEST_ID);
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestState(iar.getRequestStatus());
        requestStatus.setStatusMessage(String.format("FLOW STATUS: %s RETRY STATUS: %s ROLLBACK STATUS: %s",
                FLOW_STATUS + TASK_INFORMATION, RETRY_STATUS_MESSAGE, ROLLBACK_STATUS_MESSAGE));
        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);

        Request result = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest,
                OrchestrationRequestFormat.DETAIL.toString());
        assertThat(result, sameBeanAs(expected));
    }

    @Test
    public void mapRequestStatusAndExtSysErrSrcToRequestFalseTest() throws ApiException {
        doReturn("Last task executed: Call SDNC").when(camundaRequestHandler).getTaskName(REQUEST_ID);
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestState(iar.getRequestStatus());
        requestStatus.setStatusMessage(String.format("FLOW STATUS: %s RETRY STATUS: %s ROLLBACK STATUS: %s",
                FLOW_STATUS + TASK_INFORMATION, RETRY_STATUS_MESSAGE, ROLLBACK_STATUS_MESSAGE));

        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);

        includeCloudRequest = false;

        Request actual = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest,
                OrchestrationRequestFormat.DETAIL.toString());
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void mapRequestStatusAndExtSysErrSrcToRequestStatusDetailTest() throws ApiException {
        doReturn(null).when(camundaRequestHandler).getTaskName(REQUEST_ID);
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setExtSystemErrorSource(EXT_SYSTEM_ERROR_SOURCE);
        requestStatus.setRollbackExtSystemErrorSource(ROLLBACK_EXT_SYSTEM_ERROR_SOURCE);
        requestStatus.setRequestState(iar.getRequestStatus());
        requestStatus.setFlowStatus(FLOW_STATUS);
        requestStatus.setRollbackStatusMessage(ROLLBACK_STATUS_MESSAGE);
        requestStatus.setRetryStatusMessage(RETRY_STATUS_MESSAGE);

        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);

        includeCloudRequest = false;

        Request actual = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest,
                OrchestrationRequestFormat.STATUSDETAIL.toString());
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void mapRequestStatusAndExtSysErrSrcToRequestDetailTest() throws ApiException {
        doReturn("Last task executed: Call SDNC").when(camundaRequestHandler).getTaskName(REQUEST_ID);
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestState(iar.getRequestStatus());
        requestStatus.setStatusMessage(String.format("FLOW STATUS: %s RETRY STATUS: %s ROLLBACK STATUS: %s",
                FLOW_STATUS + TASK_INFORMATION, RETRY_STATUS_MESSAGE, ROLLBACK_STATUS_MESSAGE));

        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);

        includeCloudRequest = false;

        Request actual = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest,
                OrchestrationRequestFormat.DETAIL.toString());

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void mapRequestStatusAndExtSysErrSrcToRequestNoFlowStatusTest() throws ApiException {
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestState(iar.getRequestStatus());
        requestStatus.setStatusMessage(
                String.format("RETRY STATUS: %s ROLLBACK STATUS: %s", RETRY_STATUS_MESSAGE, ROLLBACK_STATUS_MESSAGE));

        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);

        includeCloudRequest = false;
        iar.setFlowStatus(null);

        Request actual = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest,
                OrchestrationRequestFormat.DETAIL.toString());

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void mapRequestStatusAndExtSysErrSrcToRequestErrorMessageTest() throws ApiException {
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        iar.setExtSystemErrorSource(ROLLBACK_EXT_SYSTEM_ERROR_SOURCE);
        iar.setFlowStatus(null);
        iar.setStatusMessage("Error retrieving cloud region from AAI");

        Request actual = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest,
                OrchestrationRequestFormat.DETAIL.toString());

        assertTrue(actual.getRequestStatus()
                .getStatusMessage()
                .contains("Error Source: " + ROLLBACK_EXT_SYSTEM_ERROR_SOURCE));
    }

    @Test
    public void mapRequestStatusAndExtSysErrSrcToRequestFlowStatusSuccessfulCompletionTest() throws ApiException {
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestState(iar.getRequestStatus());
        requestStatus.setStatusMessage(String.format("FLOW STATUS: %s RETRY STATUS: %s ROLLBACK STATUS: %s",
                "Successfully completed all Building Blocks", RETRY_STATUS_MESSAGE, ROLLBACK_STATUS_MESSAGE));

        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);

        includeCloudRequest = false;
        iar.setFlowStatus("Successfully completed all Building Blocks");

        Request actual = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest,
                OrchestrationRequestFormat.DETAIL.toString());

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void mapRequestStatusAndExtSysErrSrcToRequestFlowStatusSuccessfulRollbackTest() throws ApiException {
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestState(iar.getRequestStatus());
        requestStatus.setStatusMessage(String.format("FLOW STATUS: %s RETRY STATUS: %s ROLLBACK STATUS: %s",
                "All Rollback flows have completed successfully", RETRY_STATUS_MESSAGE, ROLLBACK_STATUS_MESSAGE));

        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);

        includeCloudRequest = false;
        iar.setFlowStatus("All Rollback flows have completed successfully");

        Request actual = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest,
                OrchestrationRequestFormat.DETAIL.toString());

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void requestStatusExtSystemErrorSourceTest() {
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setExtSystemErrorSource(EXT_SYSTEM_ERROR_SOURCE);
        assertThat(requestStatus.getExtSystemErrorSource(), is(equalTo(EXT_SYSTEM_ERROR_SOURCE)));
    }

    @Test
    public void mapRequestStatusToRequestForFormatDetailTest() throws ApiException {
        iar.setRequestStatus(Status.ABORTED.toString());
        String result =
                orchestrationRequests.mapRequestStatusToRequest(iar, OrchestrationRequestFormat.DETAIL.toString());

        assertEquals(Status.ABORTED.toString(), result);
    }

    @Test
    public void mapRequestStatusToRequestForFormatStatusDetailTest() throws ApiException {
        iar.setRequestStatus(Status.ABORTED.toString());
        String result = orchestrationRequests.mapRequestStatusToRequest(iar, "statusDetail");

        assertEquals(Status.ABORTED.toString(), result);
    }


    @Test
    public void mapRequestStatusToRequestForFormatEmptyStringTest() throws ApiException {
        iar.setRequestStatus(Status.ABORTED.toString());
        String result = orchestrationRequests.mapRequestStatusToRequest(iar, StringUtils.EMPTY);

        assertEquals(Status.FAILED.toString(), result);
    }
}
