/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2020 Nokia.
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

package org.onap.so.bpmn.common.listener.flowmanipulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;
import org.onap.so.bpmn.common.BBConstants;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.listener.ListenerRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FlowManipulatorListenerRunner extends ListenerRunner {

    private static Logger logger = LoggerFactory.getLogger(FlowManipulatorListenerRunner.class);

    protected List<PreFlowManipulator> flowManipulators;

    protected List<PostFlowManipulator> postflowManipulators;

    @PostConstruct
    protected void init() {

        flowManipulators = new ArrayList<>(
                Optional.ofNullable(context.getBeansOfType(PreFlowManipulator.class)).orElse(new HashMap<>()).values());

        postflowManipulators = new ArrayList<>(Optional.ofNullable(context.getBeansOfType(PostFlowManipulator.class))
                .orElse(new HashMap<>()).values());

    }

    public void modifyFlows(List<ExecuteBuildingBlock> flowsToExecute, BuildingBlockExecution execution) {
        int sequenceBeforeFlowManipulator;
        do {
            sequenceBeforeFlowManipulator = execution.getVariable(BBConstants.G_CURRENT_SEQUENCE);
            if (sequenceBeforeFlowManipulator >= flowsToExecute.size()) {
                break;
            }
            ExecuteBuildingBlock currentBB = flowsToExecute.get(execution.getCurrentSequence());
            List<PreFlowManipulator> filtered = filterListeners(flowManipulators,
                    (item -> item.shouldRunFor(currentBB.getBuildingBlock().getBpmnFlowName(),
                            execution.getCurrentSequence() == 0, execution)));

            logger.info("Running pre flow manipulators:\n{}",
                    filtered.stream().map(item -> item.getClass().getName()).collect(Collectors.joining("\n")));
            filtered.forEach(item -> item.run(flowsToExecute, currentBB, execution));
        } while (isBuildingBlockSkipped(sequenceBeforeFlowManipulator, execution));
    }

    public void postModifyFlows(List<ExecuteBuildingBlock> flowsToExecute, BuildingBlockExecution execution) {
        int sequenceBeforeFlowManipulator;
        do {
            sequenceBeforeFlowManipulator = execution.getVariable(BBConstants.G_CURRENT_SEQUENCE);
            ExecuteBuildingBlock currentBB = flowsToExecute.get(execution.getCurrentSequence() - 1);
            List<PostFlowManipulator> filtered = filterListeners(postflowManipulators,
                    (item -> item.shouldRunFor(currentBB.getBuildingBlock().getBpmnFlowName(),
                            execution.getCurrentSequence() == 0, execution)));

            logger.info("Running post flow manipulators:\n{}",
                    filtered.stream().map(item -> item.getClass().getName()).collect(Collectors.joining("\n")));
            filtered.forEach(item -> item.run(flowsToExecute, currentBB, execution));
        } while (isBuildingBlockSkipped(sequenceBeforeFlowManipulator, execution));
    }

    private boolean isBuildingBlockSkipped(int sequenceBeforeFlowManipulator, BuildingBlockExecution execution) {
        return sequenceBeforeFlowManipulator != (int) execution.getVariable(BBConstants.G_CURRENT_SEQUENCE);
    }
}
