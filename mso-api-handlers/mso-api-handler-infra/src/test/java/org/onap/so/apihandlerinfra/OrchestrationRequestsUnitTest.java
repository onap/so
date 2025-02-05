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
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import jakarta.ws.rs.core.Response;
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
import org.onap.so.apihandlerinfra.exceptions.ContactCamundaException;
import org.onap.so.apihandlerinfra.exceptions.ValidateException;
import org.onap.so.constants.OrchestrationRequestFormat;
import org.onap.so.constants.Status;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.serviceinstancebeans.InstanceReferences;
import org.onap.so.serviceinstancebeans.Request;
import org.onap.so.serviceinstancebeans.RequestStatus;
import org.springframework.core.env.Environment;

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
    @Mock
    private Environment env;
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
    private static final String WORKFLOW_NAME = "testWorkflowName";
    private static final String OPERATION_NAME = "testOperationName";
    private static final String REQUEST_ACTION = "createInstance";
    private InfraActiveRequests iar;
    boolean includeCloudRequest = false;
    private static final String ROLLBACK_EXT_SYSTEM_ERROR_SOURCE = "SDNC";
    private Timestamp startTime = new Timestamp(System.currentTimeMillis());


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
        iar.setResourceStatusMessage("The vf module already exist");
        iar.setStartTime(startTime);
    }

    @Test
    public void mapInfraActiveRequestToRequestWithOriginalRequestIdTest() throws ApiException {
        doReturn("Last task executed: Call SDNC").when(camundaRequestHandler).getTaskName(REQUEST_ID);
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestState(iar.getRequestStatus());
        requestStatus.setStatusMessage(
                String.format("FLOW STATUS: %s RETRY STATUS: %s ROLLBACK STATUS: %s RESOURCE STATUS: %s",
                        FLOW_STATUS + TASK_INFORMATION, RETRY_STATUS_MESSAGE, ROLLBACK_STATUS_MESSAGE,
                        "The vf module already exist"));

        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setOriginalRequestId(ORIGINAL_REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);
        expected.setStartTime(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(startTime) + " GMT");

        iar.setOriginalRequestId(ORIGINAL_REQUEST_ID);

        Request result = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest,
                OrchestrationRequestFormat.DETAIL.toString(), "v7");
        assertThat(result, sameBeanAs(expected));
    }

    @Test
    public void mapInfraActiveRequestToRequestOriginalRequestIdNullTest() throws ApiException {
        doReturn("Last task executed: Call SDNC").when(camundaRequestHandler).getTaskName(REQUEST_ID);
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestState(iar.getRequestStatus());
        requestStatus.setStatusMessage(
                String.format("FLOW STATUS: %s RETRY STATUS: %s ROLLBACK STATUS: %s RESOURCE STATUS: %s",
                        FLOW_STATUS + TASK_INFORMATION, RETRY_STATUS_MESSAGE, ROLLBACK_STATUS_MESSAGE,
                        "The vf module already exist"));

        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);
        expected.setStartTime(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(startTime) + " GMT");

        iar.setWorkflowName(WORKFLOW_NAME);
        iar.setOperationName(OPERATION_NAME);

        Request result = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest,
                OrchestrationRequestFormat.DETAIL.toString(), "v7");
        assertThat(result, sameBeanAs(expected));
    }

    @Test
    public void mapInfraActiveRequestToRequestWithWorkflowNameAndOperationNameTest() throws ApiException {
        doReturn("Last task executed: Call SDNC").when(camundaRequestHandler).getTaskName(REQUEST_ID);
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestState(iar.getRequestStatus());
        requestStatus.setStatusMessage(
                String.format("FLOW STATUS: %s RETRY STATUS: %s ROLLBACK STATUS: %s RESOURCE STATUS: %s",
                        FLOW_STATUS + TASK_INFORMATION, RETRY_STATUS_MESSAGE, ROLLBACK_STATUS_MESSAGE,
                        "The vf module already exist"));

        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setOriginalRequestId(ORIGINAL_REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);
        expected.setStartTime(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(startTime) + " GMT");
        expected.setWorkflowName(WORKFLOW_NAME);
        expected.setOperationName(OPERATION_NAME);

        iar.setOriginalRequestId(ORIGINAL_REQUEST_ID);
        iar.setWorkflowName(WORKFLOW_NAME);
        iar.setOperationName(OPERATION_NAME);

        Request result = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest,
                OrchestrationRequestFormat.DETAIL.toString(), "v8");
        assertThat(result, sameBeanAs(expected));
    }

    @Test
    public void mapInfraActiveRequestToRequestWithNoWorkflowNameAndNoOperationNameTest() throws ApiException {
        doReturn("Last task executed: Call SDNC").when(camundaRequestHandler).getTaskName(REQUEST_ID);
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestState(iar.getRequestStatus());
        requestStatus.setStatusMessage(
                String.format("FLOW STATUS: %s RETRY STATUS: %s ROLLBACK STATUS: %s RESOURCE STATUS: %s",
                        FLOW_STATUS + TASK_INFORMATION, RETRY_STATUS_MESSAGE, ROLLBACK_STATUS_MESSAGE,
                        "The vf module already exist"));

        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setOriginalRequestId(ORIGINAL_REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);
        expected.setStartTime(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(startTime) + " GMT");


        expected.setWorkflowName(REQUEST_ACTION);
        expected.setRequestType(REQUEST_ACTION);

        iar.setOriginalRequestId(ORIGINAL_REQUEST_ID);
        iar.setRequestAction(REQUEST_ACTION);
        iar.setWorkflowName(null);
        iar.setOperationName(null);

        Request result = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest,
                OrchestrationRequestFormat.DETAIL.toString(), "v8");
        assertThat(result, sameBeanAs(expected));
    }

    @Test
    public void mapRequestStatusAndExtSysErrSrcToRequestFalseTest() throws ApiException {
        doReturn("Last task executed: Call SDNC").when(camundaRequestHandler).getTaskName(REQUEST_ID);
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestState(iar.getRequestStatus());
        requestStatus.setStatusMessage(
                String.format("FLOW STATUS: %s RETRY STATUS: %s ROLLBACK STATUS: %s RESOURCE STATUS: %s",
                        FLOW_STATUS + TASK_INFORMATION, RETRY_STATUS_MESSAGE, ROLLBACK_STATUS_MESSAGE,
                        "The vf module already exist"));

        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);
        expected.setStartTime(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(startTime) + " GMT");

        includeCloudRequest = false;

        Request actual = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest,
                OrchestrationRequestFormat.DETAIL.toString(), "v7");
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
        expected.setStartTime(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(startTime) + " GMT");

        includeCloudRequest = false;

        Request actual = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest,
                OrchestrationRequestFormat.STATUSDETAIL.toString(), "v7");
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void mapRequestStatusAndExtSysErrSrcToRequestStatusDetailV8Test() throws ApiException {
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
        requestStatus.setResourceStatusMessage("The vf module already exist");

        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);
        expected.setStartTime(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(startTime) + " GMT");

        includeCloudRequest = false;

        Request actual = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest,
                OrchestrationRequestFormat.STATUSDETAIL.toString(), "v8");
        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void mapRequestStatusNoTaskInfoTest() throws ApiException {
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestState(iar.getRequestStatus());
        requestStatus.setStatusMessage(
                "FLOW STATUS: FlowStatus RETRY STATUS: RetryStatusMessage ROLLBACK STATUS: RollbackStatusMessage RESOURCE STATUS: The vf module already exist");

        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);
        expected.setStartTime(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(startTime) + " GMT");

        includeCloudRequest = false;

        Request actual = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest,
                OrchestrationRequestFormat.SIMPLENOTASKINFO.toString(), "v7");
        assertThat(expected, sameBeanAs(actual));
    }

    @Test
    public void mapRequestStatusNullFormatTest() throws ApiException {
        doReturn("TaskName").when(camundaRequestHandler).getTaskName(REQUEST_ID);
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestState(iar.getRequestStatus());
        requestStatus.setStatusMessage(
                "FLOW STATUS: FlowStatus TASK INFORMATION: TaskName RETRY STATUS: RetryStatusMessage ROLLBACK STATUS: RollbackStatusMessage RESOURCE STATUS: The vf module already exist");

        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);
        expected.setStartTime(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(startTime) + " GMT");

        includeCloudRequest = false;

        Request actual = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest, null, "v7");
        assertThat(expected, sameBeanAs(actual));
    }

    @Test
    public void mapRequestStatusAndExtSysErrSrcToRequestDetailTest() throws ApiException {
        doReturn("Last task executed: Call SDNC").when(camundaRequestHandler).getTaskName(REQUEST_ID);
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestState(iar.getRequestStatus());
        requestStatus.setStatusMessage(
                String.format("FLOW STATUS: %s RETRY STATUS: %s ROLLBACK STATUS: %s RESOURCE STATUS: %s",
                        FLOW_STATUS + TASK_INFORMATION, RETRY_STATUS_MESSAGE, ROLLBACK_STATUS_MESSAGE,
                        "The vf module already exist"));

        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);
        expected.setStartTime(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(startTime) + " GMT");

        includeCloudRequest = false;

        Request actual = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest,
                OrchestrationRequestFormat.DETAIL.toString(), "v7");

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
        expected.setStartTime(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(startTime) + " GMT");

        includeCloudRequest = false;
        iar.setFlowStatus(null);
        iar.setResourceStatusMessage(null);

        Request actual = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest,
                OrchestrationRequestFormat.DETAIL.toString(), "v7");

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
                OrchestrationRequestFormat.DETAIL.toString(), "v7");

        assertTrue(actual.getRequestStatus().getStatusMessage()
                .contains("Error Source: " + ROLLBACK_EXT_SYSTEM_ERROR_SOURCE));
    }

    @Test
    public void mapRequestStatusAndExtSysErrSrcToRequestFlowStatusSuccessfulCompletionTest() throws ApiException {
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestState(iar.getRequestStatus());
        requestStatus.setStatusMessage(
                String.format("FLOW STATUS: %s RETRY STATUS: %s ROLLBACK STATUS: %s RESOURCE STATUS: %s",
                        "Successfully completed all Building Blocks", RETRY_STATUS_MESSAGE, ROLLBACK_STATUS_MESSAGE,
                        "The vf module already exist"));

        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);
        expected.setStartTime(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(startTime) + " GMT");

        includeCloudRequest = false;
        iar.setFlowStatus("Successfully completed all Building Blocks");

        Request actual = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest,
                OrchestrationRequestFormat.DETAIL.toString(), "v7");

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void mapRequestStatusAndExtSysErrSrcToRequestFlowStatusSuccessfulRollbackTest() throws ApiException {
        InstanceReferences instanceReferences = new InstanceReferences();
        instanceReferences.setServiceInstanceId(SERVICE_INSTANCE_ID);
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestState(iar.getRequestStatus());
        requestStatus.setStatusMessage(
                String.format("FLOW STATUS: %s RETRY STATUS: %s ROLLBACK STATUS: %s RESOURCE STATUS: %s",
                        "All Rollback flows have completed successfully", RETRY_STATUS_MESSAGE, ROLLBACK_STATUS_MESSAGE,
                        "The vf module already exist"));

        Request expected = new Request();
        expected.setRequestId(REQUEST_ID);
        expected.setInstanceReferences(instanceReferences);
        expected.setRequestStatus(requestStatus);
        expected.setRequestScope(SERVICE);
        expected.setStartTime(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss").format(startTime) + " GMT");

        includeCloudRequest = false;
        iar.setFlowStatus("All Rollback flows have completed successfully");

        Request actual = orchestrationRequests.mapInfraActiveRequestToRequest(iar, includeCloudRequest,
                OrchestrationRequestFormat.DETAIL.toString(), "v7");

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

    @Test
    public void infraActiveRequestNullValidationExceptionTest() throws ApiException {
        iar.setRequestId(REQUEST_ID);
        thrown.expect(ValidateException.class);
        thrown.expectMessage(containsString("Null response from RequestDB when searching by RequestId " + REQUEST_ID));
        orchestrationRequests.infraActiveRequestLookup(iar.getRequestId());
    }

    @Test
    public void isRequestProcessingDataRequiredTest() {
        boolean required = orchestrationRequests.isRequestProcessingDataRequired("simpleNoTaskInfo");
        assertFalse(required);
    }

    @Test
    public void taskNameLookup() throws ContactCamundaException {
        InfraActiveRequests req = new InfraActiveRequests();
        req.setRequestId("70debc2a-d6bc-4795-87ba-38a94d9b0b99");
        Instant startInstant = Instant.now().minus(1, ChronoUnit.DAYS);
        req.setStartTime(Timestamp.from(startInstant));
        when(env.getProperty("mso.camundaCleanupInterval")).thenReturn(null);
        when(camundaRequestHandler.getTaskName("70debc2a-d6bc-4795-87ba-38a94d9b0b99")).thenReturn("taskName");

        RequestStatus requestStatus = new RequestStatus();
        req.setFlowStatus("Building blocks 1 of 3 completed.");

        orchestrationRequests.mapRequestStatusAndExtSysErrSrcToRequest(req, requestStatus, null, "v7");
        assertEquals("FLOW STATUS: Building blocks 1 of 3 completed. TASK INFORMATION: taskName",
                requestStatus.getStatusMessage());
    }

    @Test
    public void noCamundaLookupAfterInterval() throws ContactCamundaException {
        InfraActiveRequests req = new InfraActiveRequests();
        req.setRequestId("70debc2a-d6bc-4795-87ba-38a94d9b0b99");
        Instant startInstant = Instant.now().minus(36, ChronoUnit.DAYS);
        req.setStartTime(Timestamp.from(startInstant));
        when(env.getProperty("mso.camundaCleanupInterval")).thenReturn("35");

        RequestStatus requestStatus = new RequestStatus();
        req.setFlowStatus("Building blocks 1 of 3 completed.");

        orchestrationRequests.mapRequestStatusAndExtSysErrSrcToRequest(req, requestStatus, null, "v7");
        assertEquals("FLOW STATUS: Building blocks 1 of 3 completed.", requestStatus.getStatusMessage());
    }
}
