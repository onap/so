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

package org.onap.so.bpmn.infrastructure.bpmn.subprocess;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.onap.so.bpmn.BaseBPMNTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;


public class UpdateNetworkBBTest extends BaseBPMNTest {
    @Test
    public void updateNetworkBBTest() {
        mockSubprocess("SDNCHandler", "My Mock Process Name", "GenericStub");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("UpdateNetworkBB", variables);
        assertThat(processInstance).isNotNull();
        assertThat(processInstance).isStarted().hasPassedInOrder("UpdateNetworkBB_Start", "SDNCChangeAssignNetwork",
                "CallActivity_sdncHandlerCallChangeAssign", "QueryVpnBindingAAI", "QueryNetworkPolicyAAI",
                "QueryNetworkTableRefAAI", "Create_Network_ServiceTask", "CallActivity_NetworkAdapterRestV1",
                "ServiceTask_ProcessResponse", "Update_Network_AAI_ServiceTask", "UpdateNetworkBB_End");
        assertThat(processInstance).isEnded();
    }

    @Test
    public void updateNetworkBBExceptionTest() {
        mockSubprocess("SDNCHandler", "My Mock Process Name", "GenericStub");
        doThrow(new BpmnError("7000", "TESTING ERRORS")).when(aaiQueryTasks)
                .queryNetworkVpnBinding(any(BuildingBlockExecution.class));

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("UpdateNetworkBB", variables);
        assertThat(processInstance).isStarted()
                .hasPassedInOrder("UpdateNetworkBB_Start", "SDNCChangeAssignNetwork",
                        "CallActivity_sdncHandlerCallChangeAssign", "QueryVpnBindingAAI")
                .hasNotPassed("QueryNetworkPolicyAAI", "QueryNetworkTableRefAAI", "UpdateNetworkAdapter",
                        "UpdateNetworkAAI", "UpdateNetworkBB_End");
        assertThat(processInstance).isEnded();
    }
}
