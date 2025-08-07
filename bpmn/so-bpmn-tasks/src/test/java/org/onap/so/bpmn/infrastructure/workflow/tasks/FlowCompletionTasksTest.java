/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.core.WorkflowException;
import org.onap.so.db.request.beans.InfraActiveRequests;

public class FlowCompletionTasksTest extends BaseTaskTest {

    @InjectMocks
    protected FlowCompletionTasks flowCompletionTasks = new FlowCompletionTasks();

    @Before
    public void before() {
        setRequestContext();
    }

    @Test
    public void updateRequestDbStatusComplete_Test() throws Exception {
        InfraActiveRequests mockedRequest = new InfraActiveRequests();
        when(requestsDbClient.getInfraActiveRequestbyRequestId(any(String.class))).thenReturn(mockedRequest);
        doNothing().when(requestsDbClient).updateInfraActiveRequests(any(InfraActiveRequests.class));
        flowCompletionTasks.updateRequestDbStatus(execution);
        verify(requestsDbClient, times(1)).updateInfraActiveRequests(any(InfraActiveRequests.class));
        assertEquals(mockedRequest.getRequestStatus(), "COMPLETE");
    }

    @Test
    public void updateRequestDbStatusFailed_Test() throws Exception {
        WorkflowException workflowException = new WorkflowException("testProcessKey", 7000, "Error");
        execution.setVariable("WorkflowException", workflowException);
        InfraActiveRequests mockedRequest = new InfraActiveRequests();
        when(requestsDbClient.getInfraActiveRequestbyRequestId(any(String.class))).thenReturn(mockedRequest);
        doNothing().when(requestsDbClient).updateInfraActiveRequests(any(InfraActiveRequests.class));
        flowCompletionTasks.updateRequestDbStatus(execution);
        verify(requestsDbClient, times(1)).updateInfraActiveRequests(any(InfraActiveRequests.class));
        assertEquals(mockedRequest.getRequestStatus(), "FAILED");
    }
}
