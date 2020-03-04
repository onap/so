/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix
 *  Modifications Copyright (C) 2020 Huawei
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

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda.controller.sdnc;

import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.so.client.sdnc.lcm.beans.payload.ActivateNESwPayload;
import org.springframework.stereotype.Component;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.core.json.JsonUtils;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda.controller.common.SoPropertyConstants;
import org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda.controller.LcmControllerDE;
import org.onap.so.client.sdnc.common.SDNCConstants;
import org.onap.so.client.sdnc.lcm.*;
import org.onap.so.client.sdnc.lcm.beans.*;
import org.onap.so.client.sdnc.lcm.beans.payload.*;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.REQUEST_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.REQUEST_PAYLOAD;

@Component
public class SdncControllerDE extends LcmControllerDE {

    private static final int SDNC_DELEGATE_EXECUTION_ERROR_CODE = 1103;

    @Override
    public Boolean understand(ControllerContext<DelegateExecution> context) {
        return context.getControllerActor().equalsIgnoreCase("sdnc");
    }

    @Override
    public Boolean ready(ControllerContext<DelegateExecution> context) {
        return true;
    }

    @Override
    protected int callLcmClient(ControllerContext<DelegateExecution> context) {
        DelegateExecution execution = context.getExecution();

        logger.debug("Running activity for id: {}, name: {}", execution.getCurrentActivityId(),
                execution.getCurrentActivityName());

        boolean result;
        try {
            LcmInput lcmInput = buildLcmInput(execution);
            if (lcmInput != null) {
                result = sendLcmRequest(execution, lcmInput);
            } else {
                logger.error("Build LCM Input error");
                result = false;
            }
        } catch (Exception e) {
            logger.error("Call SDNC LCM Client failure: ", e);
            result = false;
        }

        if (result) {
            execution.setVariable(SoPropertyConstants.CONTROLLER_STATUS, "Success");
        } else {
            execution.setVariable(SoPropertyConstants.CONTROLLER_STATUS, "Failure");
        }

        return 0;
    }

    @Override
    protected int getErrorCode() {
        return SDNC_DELEGATE_EXECUTION_ERROR_CODE;
    }

    private LcmOutput sendSyncRequest(String operation, LcmInput lcmInput) {
        SDNCLcmClientBuilder sdncLcmClientBuilder = new SDNCLcmClientBuilder();
        SDNCLcmRestClient sdncLcmRestClient;
        try {
            sdncLcmRestClient = sdncLcmClientBuilder.newSDNCLcmRestClient(operation);
        } catch (SDNCLcmClientBuilderException e) {
            logger.error("Create SDNCLcmRestClient error: ", e);
            return null;
        }

        return sdncLcmRestClient.sendRequest(lcmInput);
    }

    private LcmOutput sendAsyncRequest(String operation, LcmInput lcmInput) {
        SDNCLcmClientBuilder sdncLcmClientBuilder = new SDNCLcmClientBuilder();
        SDNCLcmDmaapClient sdncLcmDmaapClient;
        try {
            sdncLcmDmaapClient = sdncLcmClientBuilder.newSDNCLcmDmaapClient();
        } catch (SDNCLcmClientBuilderException e) {
            logger.error("Create SDNCLcmDmaapClient error: ", e);
            return null;
        }

        LcmDmaapRequest lcmDmaapRequest = SDNCLcmMessageBuilder.buildLcmDmaapRequest(operation, lcmInput);
        try {
            sdncLcmDmaapClient.sendRequest(lcmDmaapRequest);
        } catch (Exception e) {
            logger.error("SDNCLcmDmaapClient sends request error: ", e);
            return null;
        }

        List<LcmDmaapResponse> LcmDmaapResponseList;

        while (true) {
            LcmDmaapResponseList = sdncLcmDmaapClient.getResponse();
            if (LcmDmaapResponseList.size() > 0) {
                break;
            }
        }

        return LcmDmaapResponseList.get(0).getBody().getOutput();
    }

    public static String toLowerHyphen(String lcmAction) {
        String regex = "([a-z0-9A-Z])(?=[A-Z])";
        String replacement = "$1-";
        return lcmAction.replaceAll(regex, replacement).toLowerCase();
    }

    private LcmInput buildLcmInput(DelegateExecution execution) throws JsonProcessingException {
        String requestId = String.valueOf(execution.getVariable(REQUEST_ID));
        String requestAction = String.valueOf(execution.getVariable(SoPropertyConstants.SO_ACTION));
        String pnfName = String.valueOf(execution.getVariable(PNF_CORRELATION_ID));
        logger.debug(String.format("requestId: %s, action: %s, pnfName: %s", requestId, requestAction, pnfName));

        String requestPayload = String.valueOf(execution.getVariable(REQUEST_PAYLOAD));
        logger.debug("SO request payload: " + requestPayload);

        String lcmAction;
        String lcmPayload;

        switch (requestAction) {
            case SoPropertyConstants.ACTION_PRE_CHECK:
                lcmAction = SDNCLcmActionConstants.UPGRADE_PRE_CHECK;

                UpgradePreCheckPayload upgradePreCheckPayload;
                upgradePreCheckPayload = SDNCLcmPayloadBuilder.buildUpgradePreCheckPayload(execution);
                lcmPayload = SDNCLcmPayloadBuilder.convertToSting(upgradePreCheckPayload);
                break;
            case SoPropertyConstants.ACTION_DOWNLOAD_N_E_SW:
                lcmAction = SDNCLcmActionConstants.DOWNLOAD_N_E_SW;

                DownloadNESwPayload downloadNESwPayload;
                downloadNESwPayload = SDNCLcmPayloadBuilder.buildDownloadNESwPayload(execution);
                lcmPayload = SDNCLcmPayloadBuilder.convertToSting(downloadNESwPayload);
                break;
            case SoPropertyConstants.ACTION_ACTIVATE_N_E_SW:
                lcmAction = SDNCLcmActionConstants.ACTIVATE_N_E_SW;

                ActivateNESwPayload activateNESwPayload;
                activateNESwPayload = SDNCLcmPayloadBuilder.buildActivateNESwPayload(execution);
                lcmPayload = SDNCLcmPayloadBuilder.convertToSting(activateNESwPayload);
                break;
            case SoPropertyConstants.ACTION_POST_CHECK:
                lcmAction = SDNCLcmActionConstants.UPGRADE_POST_CHECK;

                UpgradePostCheckPayload upgradePostCheckPayload;
                upgradePostCheckPayload = SDNCLcmPayloadBuilder.buildUpgradePostCheckPayload(execution);
                lcmPayload = SDNCLcmPayloadBuilder.convertToSting(upgradePostCheckPayload);
                break;
            default:
                logger.error("Unsupported SO Action: " + requestAction);
                return null;
        }

        logger.debug("SDNC LCM payload for {}: {}", lcmAction, lcmPayload);

        String subRequestId = UUID.randomUUID().toString();
        LcmInput lcmInput =
                SDNCLcmMessageBuilder.buildLcmInputForPnf(requestId, subRequestId, pnfName, lcmAction, lcmPayload);

        ObjectMapper mapper = new ObjectMapper();
        String lcmInputMsg = mapper.writeValueAsString(lcmInput);
        logger.debug("SDNC input message:\n" + lcmInputMsg);

        return lcmInput;
    }

    private boolean parseLcmOutput(LcmOutput lcmOutput, String lcmAction) {
        if (lcmOutput == null) {
            logger.error("Call SDNC LCM API failure");
            return false;
        }

        LcmStatus lcmStatus = lcmOutput.getStatus();

        if (lcmStatus.getCode() == SDNCConstants.LCM_OUTPUT_SUCCESS_CODE) {
            logger.debug("Call SDNC LCM API success: " + lcmStatus.getMessage());
        } else {
            logger.error("Call SDNC LCM API failure: " + lcmStatus.getMessage());
        }

        String outputPayload = lcmOutput.getPayload();
        logger.debug("SDNC LCM action: {}, result: {}", lcmAction, outputPayload);
        if (outputPayload != null) {
            String result = JsonUtils.getJsonValue(outputPayload, "result");
            if ("Success".equals(result)) {
                logger.debug("Run SDNC LCM action {} success", lcmAction);
                return true;
            } else {
                String reason = JsonUtils.getJsonValue(outputPayload, "reason");
                logger.error("Run SDNC LCM action {} failure, reason: {}", lcmAction, reason);
            }
        }

        return false;
    }

    private boolean sendLcmRequest(DelegateExecution execution, LcmInput lcmInput) {
        String actionMode = String.valueOf(execution.getVariable("mode"));
        String lcmOperation = toLowerHyphen(lcmInput.getAction());

        LcmOutput lcmOutput;
        if ("async".equals(actionMode)) {
            lcmOutput = sendAsyncRequest(lcmOperation, lcmInput);
        } else {
            lcmOutput = sendSyncRequest(lcmOperation, lcmInput);
        }

        return parseLcmOutput(lcmOutput, lcmInput.getAction());
    }
}
