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

package org.onap.so.bpmn.servicedecomposition.tasks;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.logging.filter.base.ONAPComponents;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.db.request.client.RequestsDbClient;
import org.onap.so.utils.Components;

@RunWith(MockitoJUnitRunner.class)
public class ExecuteBuildingBlockRainyDayUnitTest {

    @Mock
    private RequestsDbClient requestsDbClient;

    @InjectMocks
    @Spy
    private ExecuteBuildingBlockRainyDay executeBuildingBlockRainyDay;

    private DelegateExecution execution;
    private DelegateExecution executionNullisRollback;
    private String msoRequestId = "ef7c004b-829f-4773-a7d8-4de29feef5b1";
    private InfraActiveRequests request = new InfraActiveRequests();
    private WorkflowException exception;
    private WorkflowException noExtSystemErrorSourceException;

    @Before
    public void setup() {
        exception = new WorkflowException("Test exception", 7000, "", "", ONAPComponents.SDNC);
        noExtSystemErrorSourceException =
                new WorkflowException("Test exception without extsystemErrorSource", 7000, "", "");

        execution = new DelegateExecutionFake();
        execution.setVariable("mso-request-id", "ef7c004b-829f-4773-a7d8-4de29feef5b1");

        executionNullisRollback = new DelegateExecutionFake();
        executionNullisRollback.setVariable("mso-request-id", "ef7c004b-829f-4773-a7d8-4de29feef5b1");
    }

    @Test
    public void updateExtSystemErrorSourceTest() {
        doReturn(request).when(requestsDbClient).getInfraActiveRequestbyRequestId(msoRequestId);
        doNothing().when(requestsDbClient).updateInfraActiveRequests(request);
        execution.setVariable("isRollback", false);
        execution.setVariable("WorkflowException", exception);
        executeBuildingBlockRainyDay.updateExtSystemErrorSource(execution);
        request.setExtSystemErrorSource(ONAPComponents.SDNC.toString());

        verify(requestsDbClient, Mockito.times(1)).updateInfraActiveRequests(request);
    }

    @Test
    public void updateExtSystemErrorSourceisRollbackTest() {
        doReturn(request).when(requestsDbClient).getInfraActiveRequestbyRequestId(msoRequestId);
        doNothing().when(requestsDbClient).updateInfraActiveRequests(request);
        execution.setVariable("isRollback", true);
        execution.setVariable("WorkflowException", exception);
        executeBuildingBlockRainyDay.updateExtSystemErrorSource(execution);
        request.setExtSystemErrorSource(ONAPComponents.SDNC.toString());

        verify(requestsDbClient, Mockito.times(1)).updateInfraActiveRequests(request);
    }

    @Test
    public void updateExtSystemErrorSourceisRollbackONAPComponentsNullTest() {
        doReturn(request).when(requestsDbClient).getInfraActiveRequestbyRequestId(msoRequestId);
        doNothing().when(requestsDbClient).updateInfraActiveRequests(request);
        execution.setVariable("isRollback", true);
        execution.setVariable("WorkflowException", noExtSystemErrorSourceException);
        executeBuildingBlockRainyDay.updateExtSystemErrorSource(execution);
        request.setExtSystemErrorSource(Components.UNKNOWN.toString());

        verify(requestsDbClient, Mockito.times(1)).updateInfraActiveRequests(request);
    }

    @Test
    public void updateExtSystemErrorSourceONAPComponentsNullTest() {
        doReturn(request).when(requestsDbClient).getInfraActiveRequestbyRequestId(msoRequestId);
        doNothing().when(requestsDbClient).updateInfraActiveRequests(request);
        execution.setVariable("isRollback", false);
        execution.setVariable("WorkflowException", noExtSystemErrorSourceException);
        executeBuildingBlockRainyDay.updateExtSystemErrorSource(execution);
        request.setExtSystemErrorSource(Components.UNKNOWN.toString());

        verify(requestsDbClient, Mockito.times(1)).updateInfraActiveRequests(request);
    }

    @Test
    public void updateExtSystemErrorSourceONAPComponentsisRollbackNullTest() {
        doReturn(request).when(requestsDbClient).getInfraActiveRequestbyRequestId(msoRequestId);
        doNothing().when(requestsDbClient).updateInfraActiveRequests(request);
        executionNullisRollback.setVariable("WorkflowException", exception);
        executeBuildingBlockRainyDay.updateExtSystemErrorSource(executionNullisRollback);
        request.setExtSystemErrorSource(ONAPComponents.SDNC.toString());

        verify(requestsDbClient, Mockito.times(1)).updateInfraActiveRequests(request);
    }
}
