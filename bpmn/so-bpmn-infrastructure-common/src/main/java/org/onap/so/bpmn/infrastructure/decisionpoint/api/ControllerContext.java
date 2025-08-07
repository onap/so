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
package org.onap.so.bpmn.infrastructure.decisionpoint.api;

/**
 * This class is used to represent the Context used by {@ref ControllerRunnable}.
 */
public class ControllerContext<T> {

    /**
     * Action to be executed against controller.
     *
     * e.g, healthcheck, scaleout, config-assign
     *
     * Action is case insensitive.
     */
    private String controllerAction;

    /**
     * Controller actor.
     *
     * e.g., CDS, SDNC, APPC.
     *
     * actor name is case insensitive.
     */
    private String controllerActor;

    /**
     * scope: PNF, VNF, VF.
     */
    private String controllerScope;

    /**
     * Execution context, buildingblockExecution or DelegateExecution.
     */
    private T execution;

    public ControllerContext() {}

    public void setExecution(T execution) {
        this.execution = execution;
    }

    public T getExecution() {
        return execution;
    }

    public String getControllerAction() {
        return controllerAction;
    }

    public void setControllerAction(String controllerAction) {
        this.controllerAction = controllerAction;
    }

    public String getControllerActor() {
        return controllerActor;
    }

    public void setControllerActor(String controllerActor) {
        this.controllerActor = controllerActor;
    }

    public String getControllerScope() {
        return controllerScope;
    }

    public void setControllerScope(String controllerScope) {
        this.controllerScope = controllerScope;
    }

    public String toString() {
        return "Controller context for actor: " + controllerActor + ", action: " + controllerAction + ",scope: "
                + controllerScope;
    }
}
