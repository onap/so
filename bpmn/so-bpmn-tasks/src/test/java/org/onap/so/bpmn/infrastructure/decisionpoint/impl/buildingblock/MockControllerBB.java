/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.buildingblock;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;
import org.springframework.stereotype.Component;

@Component
public class MockControllerBB implements ControllerRunnable<BuildingBlockExecution> {

    public static final String TEST_ACTOR = "test-controller";
    public static final String TEST_ACTION = "configuration";

    @Override
    public Boolean understand(ControllerContext<BuildingBlockExecution> context) {
        return context.getControllerAction().equalsIgnoreCase(TEST_ACTION)
                && context.getControllerActor().equalsIgnoreCase(TEST_ACTOR);
    }

    @Override
    public Boolean ready(ControllerContext<BuildingBlockExecution> context) {
        context.getExecution().setVariable("stage", "ready");
        return true;
    }

    @Override
    public void prepare(ControllerContext<BuildingBlockExecution> context) {
        context.getExecution().setVariable("stage", "prepare");
    }

    @Override
    public void run(ControllerContext<BuildingBlockExecution> context) {
        context.getExecution().setVariable("stage", "run");
    }
}
