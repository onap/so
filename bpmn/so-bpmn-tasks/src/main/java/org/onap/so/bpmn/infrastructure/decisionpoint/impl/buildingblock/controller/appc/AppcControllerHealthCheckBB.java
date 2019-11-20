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

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.buildingblock.controller.appc;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;
import org.onap.so.bpmn.infrastructure.flowspecific.tasks.GenericVnfHealthCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This component is used to execute the Appc Controller healthcheck tasks.
 */
@Component
public class AppcControllerHealthCheckBB implements ControllerRunnable<BuildingBlockExecution> {

    @Autowired
    private GenericVnfHealthCheck healthCheck;

    @Override
    public Boolean understand(ControllerContext<BuildingBlockExecution> context) {
        return (context.getControllerActor().equalsIgnoreCase("appc")
                || context.getControllerActor().equalsIgnoreCase("sdnc"))
                && context.getControllerAction().equals("HealthCheck")
                && context.getControllerScope().equalsIgnoreCase("vnf");
    }

    @Override
    public Boolean ready(ControllerContext<BuildingBlockExecution> context) {
        return true;
    }

    @Override
    public void prepare(ControllerContext<BuildingBlockExecution> context) {
        healthCheck.setParamsForGenericVnfHealthCheck(context.getExecution());
    }

    @Override
    public void run(ControllerContext<BuildingBlockExecution> context) {
        healthCheck.callAppcClient(context.getExecution());
    }
}
