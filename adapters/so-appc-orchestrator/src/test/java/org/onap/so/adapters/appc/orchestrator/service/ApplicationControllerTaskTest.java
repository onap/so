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

package org.onap.so.adapters.appc.orchestrator.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
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
import org.onap.so.appc.orchestrator.service.beans.ApplicationControllerTaskRequest;
import org.onap.so.logging.tasks.AuditMDCSetup;
import org.onap.appc.client.lcm.model.Status;

public class ApplicationControllerTaskTest extends ApplicationControllerTask {

    @InjectMocks
    ApplicationControllerTask appcTaskService = new ApplicationControllerTask();

    @Mock
    ApplicationControllerTaskImpl applicationControllerTaskImpl;

    @Mock
    ExternalTask mockExternalTask;

    @Mock
    ExternalTaskService mockExternalTaskService;

    @Mock
    private AuditMDCSetup mdcSetup;

    private ApplicationControllerTaskRequest request = new ApplicationControllerTaskRequest();
    private String msoRequestId = "testRequestId";

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        doNothing().when(mdcSetup).setupMDC(mockExternalTask);
        doReturn(request).when(mockExternalTask).getVariable("appcOrchestratorRequest");
        doReturn(msoRequestId).when(mockExternalTask).getVariable("mso-request-id");
    }

    @Test
    public void executeExternalTask_appc_success_Test() throws Exception {
        Status status = new Status();
        doReturn(status).when(applicationControllerTaskImpl).execute(any(String.class),
                any(ApplicationControllerTaskRequest.class), any(ApplicationControllerCallback.class));
        appcTaskService.executeExternalTask(mockExternalTask, mockExternalTaskService);
        Mockito.verify(applicationControllerTaskImpl).execute(any(String.class),
                any(ApplicationControllerTaskRequest.class), any(ApplicationControllerCallback.class));
    }
}
