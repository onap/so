/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Nokia
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

package org.onap.so.bpmn.infrastructure.workflow.tasks.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Test;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;

public class HomingListenerTest {

    private static final String CALLED_HOMING = "calledHoming";

    @Test
    public void shouldRunForAssignVnfBB() {
        assertThat(new HomingListener().shouldRunFor("AssignVnfBB", false, null)).isTrue();
    }

    @Test
    public void shouldNotRunForDifferentThanAssignVnfBB() {
        assertThat(new HomingListener().shouldRunFor("someDifferentBB", false, null)).isFalse();
    }

    @Test
    public void runWithHoming() {
        // given
        DelegateExecution execution = new DelegateExecutionFake();
        execution.setVariable("homing", true);
        execution.setVariable(CALLED_HOMING, false);
        BuildingBlockExecution buildingBlockExecution = new DelegateExecutionImpl(execution);
        ExecuteBuildingBlock executeBuildingBlock = new ExecuteBuildingBlock();
        // when
        new HomingListener().run(null, executeBuildingBlock, buildingBlockExecution);
        // then
        assertThat(executeBuildingBlock.isHoming()).isTrue();
        assertThat((boolean) buildingBlockExecution.getVariable(CALLED_HOMING)).isTrue();
    }
}
