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

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda.controller.appc;

import java.util.HashMap;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.appc.client.lcm.model.Action;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerRunnable;
import org.onap.so.client.appc.ApplicationControllerAction;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Sample AppcControllerDE implementation of {@ref ControllerRunnable}.
 *
 * It"s similiar as {@ref AppcClient} groovy code.
 */
@Component
public class AppcControllerDE implements ControllerRunnable<DelegateExecution> {

    private static final Logger logger = LoggerFactory.getLogger(AppcControllerDE.class);

    @Override
    public Boolean understand(ControllerContext<DelegateExecution> context) {
        return context.getControllerActor().equalsIgnoreCase("appc")
                || context.getControllerActor().equalsIgnoreCase("sdnc");
    }

    @Override
    public Boolean ready(ControllerContext<DelegateExecution> context) {
        return true;
    }

    @Override
    public void prepare(ControllerContext<DelegateExecution> context) {

    }

    @Override
    public void run(final ControllerContext<DelegateExecution> context) {
        logger.trace("Start runCommand ");
        DelegateExecution execution = context.getExecution();
        String controllerAction = context.getControllerAction();
        execution.setVariable("rollbackVnfStop", false);
        execution.setVariable("rollbackVnfLock", false);
        execution.setVariable("rollbackQuiesceTraffic", false);
        String appcCode = "1002";
        String responsePayload = "";
        String appcMessage = "";
        try {
            String vnfId = String.valueOf(execution.getVariable("vnfId"));
            String msoRequestId = String.valueOf(execution.getVariable("msoRequestId"));
            String vnfName = String.valueOf(execution.getVariable("vnfName"));
            String aicIdentity = String.valueOf(execution.getVariable("aicIdentity"));
            String vnfHostIpAddress = String.valueOf(execution.getVariable("vnfHostIpAddress"));
            String vmIdList = String.valueOf(execution.getVariable("vmIdList"));
            String vserverIdList = String.valueOf(execution.getVariable("vserverIdList"));
            String identityUrl = String.valueOf(execution.getVariable("identityUrl"));
            String controllerType = String.valueOf(execution.getVariable("controllerType"));
            String vfModuleId = String.valueOf(execution.getVariable("vfModuleId"));
            HashMap<String, String> payloadInfo = new HashMap<String, String>();
            payloadInfo.put("vnfName", vnfName);
            payloadInfo.put("aicIdentity", aicIdentity);
            payloadInfo.put("vnfHostIpAddress", vnfHostIpAddress);
            payloadInfo.put("vmIdList", vmIdList);
            payloadInfo.put("vserverIdList", vserverIdList);
            payloadInfo.put("identityUrl", identityUrl);
            payloadInfo.put("vfModuleId", vfModuleId);
            Optional<String> payload = Optional.empty();
            logger.debug("Running APP-C action: " + controllerAction);
            logger.debug("VNFID: " + vnfId);
            execution.setVariable("msoRequestId", msoRequestId);
            execution.setVariable("failedActivity", "APP-C");
            execution.setVariable("workStep", controllerAction.toString());
            if (execution.getVariable("payload") != null) {
                String pay = String.valueOf(execution.getVariable("payload"));
                payload = Optional.of(pay);
            }
            if (controllerAction.equalsIgnoreCase("HealthCheck")) {
                Integer healthCheckIndex = (Integer) execution.getVariable("healthCheckIndex");
                execution.setVariable("workStep", controllerAction.toString() + healthCheckIndex);
                execution.setVariable("healthCheckIndex", healthCheckIndex + 1);
            }
            ApplicationControllerAction client = new ApplicationControllerAction();
            logger.debug("Created Application Controller Action Object");
            // PayloadInfo contains extra information that adds on to payload before making request to appc
            client.runAppCCommand(Action.fromValue(controllerAction.toString()), msoRequestId, vnfId, payload,
                    payloadInfo, controllerType);
            logger.debug("ran through the main method for Application Contoller");
            appcCode = client.getErrorCode();
            appcMessage = client.getErrorMessage();
        } catch (BpmnError e) {
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    "Caught exception in run ", "BPMN", ErrorCode.UnknownError.getValue(), "Exception is:\n" + e);
            appcMessage = e.getMessage();
        }
        execution.setVariable("errorCode", appcCode);
        if (appcCode == "0" && !controllerAction.equalsIgnoreCase("")) {
            if (controllerAction.equalsIgnoreCase("lock")) {
                execution.setVariable("rollbackVnfLock", true);
            }
            if (controllerAction.equalsIgnoreCase("unlock")) {
                execution.setVariable("rollbackVnfLock", false);
            }
            if (controllerAction.equalsIgnoreCase("start")) {
                execution.setVariable("rollbackVnfStop", true);
            }
            if (controllerAction.equalsIgnoreCase("stop")) {
                execution.setVariable("rollbackVnfStop", false);
            }
            if (controllerAction.equalsIgnoreCase("QuiesceTraffic")) {
                execution.setVariable("rollbackQuiesceTraffic", true);
            }
            if (controllerAction.equalsIgnoreCase("ResumeTraffic")) {
                execution.setVariable("rollbackQuiesceTraffic", false);
            }
        }
        execution.setVariable("errorText", appcMessage);
        execution.setVariable("responsePayload", responsePayload);
        logger.debug("Error Message: " + appcMessage);
        logger.debug("ERROR CODE: " + execution.getVariable("errorCode"));
        logger.trace("End of runCommand ");
    }
}
