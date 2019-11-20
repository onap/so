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
package org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda;

import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class is used for camunda {@ref DelegateExecution}.
 *
 * Each delegation method decides which controller implementation to use based on the parameters.
 */
@Component
public class ControllerExecutionDE implements JavaDelegate {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private List<ControllerRunnable<DelegateExecution>> controllers;

    @Autowired
    private ExceptionBuilder exceptionBuilder;

    private static String CONTROLLER_ACTOR_PARAM = "actor";
    private static String CONTROLLER_ACTION_PARAM = "action";
    private static String CONTROLLER_SCOPE_PARAM = "scope";

    public void execute(final DelegateExecution execution) {

        String controllerActor = getControllerActor(execution);
        String controllerAction = getControllerAction(execution);
        String controllerScope = getControllerScope(execution);
        ControllerContext<DelegateExecution> controllerContext = new ControllerContext<>();
        controllerContext.setExecution(execution);
        controllerContext.setControllerAction(controllerAction);
        controllerContext.setControllerActor(controllerActor);
        controllerContext.setControllerScope(controllerScope);
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

    private String getControllerScope(DelegateExecution execution) {
        if (execution.hasVariable(CONTROLLER_SCOPE_PARAM)) {
            String controllerScope = String.valueOf(execution.getVariable(CONTROLLER_SCOPE_PARAM));
            return controllerScope;
        } else {
            return "";
        }
    }

    private String getControllerAction(DelegateExecution execution) {

        if (execution.hasVariable(CONTROLLER_ACTION_PARAM)) {
            String controllerAction = String.valueOf(execution.getVariable(CONTROLLER_ACTION_PARAM));
            return controllerAction;
        } else {
            return "";
        }
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
    private String getControllerActor(DelegateExecution execution) {

        if (execution.hasVariable(CONTROLLER_ACTOR_PARAM)) {
            String controllerActor = String.valueOf(execution.getVariable(CONTROLLER_ACTOR_PARAM));
            return controllerActor;
        }
        return "";
    }

    private ControllerRunnable getController(ControllerContext<DelegateExecution> context) {
        return controllers.stream().filter(controllerRunnable -> controllerRunnable.understand(context)).findFirst()
                .orElse(null);
    }
}
