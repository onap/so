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

package org.onap.so.bpmn.common.listener.flowmanipulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.listener.ListenerRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FlowManipulatorListenerRunner extends ListenerRunner {

    private static Logger logger = LoggerFactory.getLogger(FlowManipulatorListenerRunner.class);

    protected List<FlowManipulator> flowManipulators;

    @PostConstruct
    protected void init() {

        flowManipulators = new ArrayList<>(
                Optional.ofNullable(context.getBeansOfType(FlowManipulator.class)).orElse(new HashMap<>()).values());

    }

    public void modifyFlows(List<ExecuteBuildingBlock> flowsToExecute, BuildingBlockExecution execution) {

        ExecuteBuildingBlock currentBB = flowsToExecute.get(execution.getCurrentSequence());
        logger.debug(" modifyFlows entry currentBB ::{},flow to execute size ::{}", currentBB, flowsToExecute.size());

        List<FlowManipulator> filtered = filterListeners(flowManipulators,
                (item -> item.shouldRunFor(currentBB.getBuildingBlock().getBpmnFlowName(),
                        execution.getCurrentSequence() == 0, execution)));

        logger.info("Running flow manipulators:\n{}",
                filtered.stream().map(item -> item.getClass().getName()).collect(Collectors.joining("\n")));
        filtered.forEach(item -> item.run(flowsToExecute, currentBB, execution));
        logger.debug(" modifyFlows ended ");
    }


}
