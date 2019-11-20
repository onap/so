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

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.buildingblock.controller.sdnc;

import java.util.Arrays;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;
import org.onap.so.bpmn.infrastructure.decisionpoint.impl.buildingblock.controller.LcmControllerBB;
import org.springframework.stereotype.Component;

/**
 * This implementation of {@link ControllerRunnable} is created to demonstrate how to support SDNC based controller.
 *
 * For demo purpose, the following actions are supported, UpgradePreCheck UpgradeSoftware UpgradePostCheck, all of these
 * actions are appc based actions and they need to be refactored to proper SDNC LCM actions.
 */
@Component
public class SdncControllerBB extends LcmControllerBB {

    private static final String[] supportedActions = new String[] {};

    private static final int SDNC_ERROR_CODE = 1002;

    @Override
    public Boolean understand(ControllerContext<BuildingBlockExecution> context) {
        return context.getControllerActor().equalsIgnoreCase("sdnc");
    }

    private boolean isActionSupported(ControllerContext<BuildingBlockExecution> context) {
        return Arrays.stream(supportedActions).anyMatch(action -> action.equals(context.getControllerAction()));
    }

    @Override
    public Boolean ready(ControllerContext<BuildingBlockExecution> context) {
        return true;
    }

    /**
     * This method is left empty intentionally. If you are planning to use the SDNC Controller, please implement here.
     *
     * You can use the Appc Client proxy, like, {@ref ApplicationControllerAction}
     * {@ref ApplicationControllerOrchestrator} {@ref ApplicationControllerClient}
     *
     * Or create your own SDNC Client proxy.
     */
    @Override
    protected int callLcmClient(ControllerContext<BuildingBlockExecution> context) {
        return 0;
    }

    @Override
    protected int getErrorCode() {
        return SDNC_ERROR_CODE;
    }
}
