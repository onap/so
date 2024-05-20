/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.adapters.appc.orchestrator.client;

import static org.hamcrest.CoreMatchers.any;
import static org.mockito.Mockito.doReturn;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.so.adapters.appc.orchestrator.client.ApplicationControllerCallback;
import org.onap.so.adapters.appc.orchestrator.client.ApplicationControllerSupport;
import org.onap.appc.client.lcm.model.Status;
import org.onap.appc.client.lcm.exceptions.AppcClientException;
import org.onap.appc.client.lcm.model.ResumeTrafficOutput;

public class ApplicationControllerCallbackTest {

    @InjectMocks
    ApplicationControllerCallback appcTaskCallback;

    @Mock
    ApplicationControllerSupport applicationControllerSupport;

    @Mock
    ExternalTask mockExternalTask;

    @Mock
    ExternalTaskService mockExternalTaskService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        appcTaskCallback = new ApplicationControllerCallback(mockExternalTask, mockExternalTaskService,
                applicationControllerSupport);
    }

    @Test
    public void onResponse_appcCallback_success_Test() throws Exception {
        Status status = new Status();
        status.setCode(400);
        ResumeTrafficOutput response = new ResumeTrafficOutput();
        response.setStatus(status);
        doReturn(status).when(applicationControllerSupport).getStatusFromGenericResponse(response);
        doReturn(true).when(applicationControllerSupport).getFinalityOf(status);
        doReturn(StatusCategory.NORMAL).when(applicationControllerSupport).getCategoryOf(status);
        appcTaskCallback.onResponse(response);
        Mockito.verify(mockExternalTaskService).complete(mockExternalTask);
    }

    @Test
    public void onResponse_appcCallback_intermediateResponse_Test() throws Exception {
        Status status = new Status();
        status.setCode(100);
        ResumeTrafficOutput response = new ResumeTrafficOutput();
        response.setStatus(status);
        doReturn(status).when(applicationControllerSupport).getStatusFromGenericResponse(response);
        appcTaskCallback.onResponse(response);
        // Mockito.verifyZeroInteractions(mockExternalTaskService);
        Mockito.verifyNoInteractions(mockExternalTaskService);
    }

    @Test
    public void onResponse_appcCallback_failure_Test() throws Exception {
        String testFailure = "test failure";
        Status status = new Status();
        status.setCode(200);
        status.setMessage(testFailure);
        ResumeTrafficOutput response = new ResumeTrafficOutput();
        response.setStatus(status);
        doReturn(status).when(applicationControllerSupport).getStatusFromGenericResponse(response);
        doReturn(true).when(applicationControllerSupport).getFinalityOf(status);
        doReturn(StatusCategory.ERROR).when(applicationControllerSupport).getCategoryOf(status);
        appcTaskCallback.onResponse(response);
        Mockito.verify(mockExternalTaskService).handleBpmnError(mockExternalTask, "MSOWorkflowException", testFailure);
    }

    @Test
    public void onException_appcCallback_failure_Test() throws Exception {
        String testFailure = "test failure";
        AppcClientException appcException = new AppcClientException(testFailure);
        Status status = new Status();
        status.setCode(200);
        String exceptionMessage = "Exception on APPC request: " + testFailure;
        status.setMessage(exceptionMessage);
        doReturn(status).when(applicationControllerSupport).buildStatusFromAppcException(appcException);
        doReturn(StatusCategory.ERROR).when(applicationControllerSupport).getCategoryOf(status);
        appcTaskCallback.onException(appcException);
        Mockito.verify(mockExternalTaskService).handleBpmnError(mockExternalTask, "MSOWorkflowException",
                exceptionMessage);
    }
}
