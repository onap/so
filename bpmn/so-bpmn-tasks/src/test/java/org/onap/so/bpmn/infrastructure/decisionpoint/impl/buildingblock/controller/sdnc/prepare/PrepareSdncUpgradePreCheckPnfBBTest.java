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

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.buildingblock.controller.sdnc.prepare;

import static org.assertj.core.api.Assertions.assertThat;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;

public class PrepareSdncUpgradePreCheckPnfBBTest {

    private PrepareSdncUpgradePreCheckPnfBB testedObject;

    @Before
    public void setup() {
        testedObject = new PrepareSdncUpgradePreCheckPnfBB();
    }

    @Test
    public void understandTrue() {
        ControllerContext<BuildingBlockExecution> controllerContext =
                createControllerContext("sdnc", "UpgradePreCheck", "pnf");
        boolean result = testedObject.understand(controllerContext);
        assertThat(result).isTrue();
    }

    @Test
    public void understandFalse() {
        ControllerContext<BuildingBlockExecution> controllerContext =
                createControllerContext("actor1", "action1", "scope1");
        boolean result = testedObject.understand(controllerContext);
        assertThat(result).isFalse();
    }

    @Test
    public void prepare_jsonWithoutActionPayload() {
        String payloadWithoutActionArray = "{\"json name\": \"test1\"}";
        ControllerContext<BuildingBlockExecution> controllerContext =
                createControllerContext(payloadWithoutActionArray);
        testedObject.prepare(controllerContext);

        assertThat((String) controllerContext.getExecution().getVariable("payload"))
                .isEqualTo(payloadWithoutActionArray);
    }

    private ControllerContext<BuildingBlockExecution> createControllerContext(String actor, String action,
            String scope) {
        ControllerContext<BuildingBlockExecution> controllerContext = new ControllerContext<>();
        controllerContext.setControllerActor(actor);
        controllerContext.setControllerAction(action);
        controllerContext.setControllerScope(scope);
        return controllerContext;
    }

    private ControllerContext<BuildingBlockExecution> createControllerContext(String payload) {
        ControllerContext<BuildingBlockExecution> controllerContext = new ControllerContext<>();
        controllerContext.setExecution(prepareBuildingBlockExecution(payload));
        return controllerContext;
    }

    private BuildingBlockExecution prepareBuildingBlockExecution(String payload) {
        DelegateExecution execution = new DelegateExecutionFake();
        execution.setVariable("payload", payload);
        return new DelegateExecutionImpl(execution);
    }
}
