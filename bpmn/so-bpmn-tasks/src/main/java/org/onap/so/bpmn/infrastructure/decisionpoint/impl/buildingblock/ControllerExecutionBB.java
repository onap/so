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

import java.util.List;
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class is used for {@ref BuildingBlockExecution}.
 *
 * Each delegation method decides which controller implementation to use based on the parameters.
 */
@Component
public class ControllerExecutionBB {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private List<ControllerRunnable<BuildingBlockExecution>> controllers;

    @Autowired
    private ExceptionBuilder exceptionBuilder;

    private static String CONTROLLER_ACTOR_PARAM = "actor";
    private static String CONTROLLER_ACTION_PARAM = "action";

    public void execute(final BuildingBlockExecution execution) {

        String controllerActor = getControllerActor(execution);
        String controllerAction = getControlerAction(execution);
        ControllerContext<BuildingBlockExecution> controllerContext = new ControllerContext();
        controllerContext.setExecution(execution);
        controllerContext.setControllerAction(controllerAction);
        controllerContext.setControllerActor(controllerActor);
        ControllerRunnable controller = getController(controllerContext);
        if (controller == null) {
            exceptionBuilder.buildAndThrowWorkflowException(execution, 9000,
                    "Unable to find the controller implementation", ONAPComponents.SO);
        } else {
            if (controller.ready(controllerContext)) {
                controller.prepare(controllerContext);
                controller.run(controllerContext);
            } else {
                exceptionBuilder.buildAndThrowWorkflowException(execution, 9001,
                        "Controller is NOT Ready for the action", ONAPComponents.SO);
            }
        }
    }

    private String getControlerAction(BuildingBlockExecution execution) {
        Object object = execution.getVariable(CONTROLLER_ACTION_PARAM);
        String controllerAction = String.valueOf(object);
        return controllerAction;
    }

    /**
     * this method is used to get the controller actor, there could be few places to get the actor,
     *
     * 1. From execution context 2. From Datbase 3. From actor selection table
     *
     * TODO: fetch from database if the CONTROLLER_ACTOR variable is not available from context.
     *
     * @param execution
     * @return
     */
    private String getControllerActor(BuildingBlockExecution execution) {
        Object object = execution.getVariable(CONTROLLER_ACTOR_PARAM);
        String controllerActor = String.valueOf(object);
        return controllerActor;
    }

    private ControllerRunnable getController(ControllerContext<BuildingBlockExecution> context) {
        return controllers.stream().filter(controllerRunnable -> controllerRunnable.understand(context)).findFirst()
                .orElse(null);
    }
}
