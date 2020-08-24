/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nokia
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.service.level.impl;

import static org.assertj.core.api.Assertions.assertThat;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Test;

public class ServiceLevelUpgradeTest {

    @Test
    public void ServiceLevelUpgradeWithPnf() throws Exception {
        // given
        ServiceLevelUpgrade testedObject = new ServiceLevelUpgrade();
        DelegateExecution execution = prepareExecution();
        execution.setVariable(ServiceLevelConstants.RESOURCE_TYPE, "pnf");
        // when
        testedObject.execute(execution);
        // then
        assertThat(execution.getVariable(ServiceLevelConstants.SOFTWARE_WORKFLOW_TO_INVOKE))
                .isEqualTo("PNFSoftwareUpgrade");
        assertThat(execution.getVariable(ServiceLevelConstants.CONTROLLER_STATUS)).isEqualTo("");
    }

    @Test
    public void ServiceLevelUpgradeWithVnf() throws Exception {
        // given
        ServiceLevelUpgrade testedObject = new ServiceLevelUpgrade();
        DelegateExecution execution = prepareExecution();
        execution.setVariable(ServiceLevelConstants.RESOURCE_TYPE, "vnf");
        // when
        testedObject.execute(execution);
        // then
        assertThat(execution.getVariable(ServiceLevelConstants.SOFTWARE_WORKFLOW_TO_INVOKE)).isNull();
        assertThat(execution.getVariable(ServiceLevelConstants.CONTROLLER_STATUS)).isNull();
    }

    private DelegateExecution prepareExecution() {
        DelegateExecution execution = new DelegateExecutionFake();
        execution.setVariable(ServiceLevelConstants.SERVICE_INSTANCE_ID, "serviceTest");
        execution.setVariable(ServiceLevelConstants.BPMN_REQUEST, "bpmnRequestTest");
        execution.setVariable(ServiceLevelConstants.PNF_NAME, "pnfNameTest");
        return execution;
    }

}
