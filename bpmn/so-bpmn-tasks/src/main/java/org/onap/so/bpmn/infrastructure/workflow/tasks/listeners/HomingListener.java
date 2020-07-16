/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

import java.util.List;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.listener.flowmanipulator.FlowManipulator;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.springframework.stereotype.Component;

@Component
public class HomingListener implements FlowManipulator {


    @Override
    public boolean shouldRunFor(String currentBBName, boolean isFirst, BuildingBlockExecution execution) {
        return "AssignVnfBB".equals(currentBBName);
    }

    @Override
    public void run(List<ExecuteBuildingBlock> flowsToExecute, ExecuteBuildingBlock currentBB,
            BuildingBlockExecution execution) {

        boolean homing = execution.getVariable("homing");
        boolean calledHoming = execution.getVariable("calledHoming");
        if (homing && !calledHoming) {
            currentBB.setHoming(true);
            execution.setVariable("calledHoming", true);
        }
    }

}
