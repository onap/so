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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.appc.client.lcm.model.Action;
import org.onap.appc.client.lcm.model.Status;
import org.onap.logging.filter.base.ONAPComponents;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;
import org.onap.so.client.appc.ApplicationControllerAction;
import org.onap.so.client.appc.ApplicationControllerOrchestrator;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This implementation of {@link ControllerRunnable} is used for Appc/SDNC controller execution.
 *
 * For demo purpose, the following actions are supported, UpgradePreCheck UpgradeSoftware UpgradePostCheck
 */
@Component
public class AppcControllerBB implements ControllerRunnable<BuildingBlockExecution> {

    private static final String[] supportedActions =
            new String[] {"UpgradePreCheck", "UpgradeSoftware", "UpgradePostCheck"};

    private static final int APPC_ERROR_CODE = 1002;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApplicationControllerAction appCClient;

    @Autowired
    private ExceptionBuilder exceptionUtil;

    @Autowired
    List<ControllerRunnable<BuildingBlockExecution>> prepareList;

    @Autowired
    private ApplicationControllerAction client;

    @Override
    public Boolean understand(ControllerContext<BuildingBlockExecution> context) {
        return (context.getControllerActor().equalsIgnoreCase("appc")
                || context.getControllerActor().equalsIgnoreCase("sdnc")) && isActionSupported(context);
    }

    private boolean isActionSupported(ControllerContext<BuildingBlockExecution> context) {
        return Arrays.stream(supportedActions).anyMatch(action -> action.equals(context.getControllerAction()));
    }

    @Override
    public Boolean ready(ControllerContext<BuildingBlockExecution> context) {
        return true;
    }

    @Override
    public void prepare(ControllerContext<BuildingBlockExecution> context) {
        prepareList.stream().filter(prepare -> prepare.understand(context))
                .forEach(prepare -> prepare.prepare(context));
    }

    @Override
    public void run(ControllerContext<BuildingBlockExecution> context) {
        callAppcClient(context);
    }

    public void callAppcClient(ControllerContext<BuildingBlockExecution> context) {

        BuildingBlockExecution execution = context.getExecution();
        try {
            Action action = Action.valueOf(context.getControllerAction());
            String msoRequestId = execution.getVariable("msoRequestId");
            String vnfId = execution.getVariable("vnfId");
            Optional<String> payload = Optional.empty();
            if (execution.getVariable("payload") != null) {
                String pay = execution.getVariable("payload");
                payload = Optional.of(pay);
            }

            // PayloadInfo contains extra information that adds on to payload before making request to appc
            client.runAppCCommand(action, msoRequestId, vnfId, payload, new HashMap<String, String>(),
                    context.getControllerActor());
        } catch (Exception e) {
            exceptionUtil.buildAndThrowWorkflowException(execution, APPC_ERROR_CODE, e.getMessage(), ONAPComponents.SO);
        }


    }
}
