/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix
 *  Modifications Copyright (C) 2020 Huawei Technologies Co., Ltd.
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
import org.springframework.stereotype.Component;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.infrastructure.decisionpoint.api.ControllerContext;
import org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda.controller.common.SoPropertyConstants;
import org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda.controller.LcmControllerDE;
import org.onap.so.client.exception.BadResponseException;
import org.onap.so.client.exception.PayloadGenerationException;
import org.onap.so.client.sdnc.common.SDNCConstants;
import org.onap.so.client.sdnc.lcm.*;
import org.onap.so.client.sdnc.lcm.beans.*;
import org.onap.so.client.sdnc.lcm.beans.payload.ActivateNESwPayload;
import org.onap.so.client.sdnc.lcm.beans.payload.DownloadNESwPayload;
import org.onap.so.client.sdnc.lcm.beans.payload.UpgradePostCheckPayload;
import org.onap.so.client.sdnc.lcm.beans.payload.UpgradePreCheckPayload;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.REQUEST_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.REQUEST_PAYLOAD;

@Component
public class SdncControllerDE extends LcmControllerDE {

    private static final int SDNC_DELEGATE_EXECUTION_ERROR_CODE = 1103;
    private static final ObjectMapper mapper = new ObjectMapper();

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

        try {
            LcmInput lcmInput = buildLcmInput(execution);
            sendLcmRequest(execution, lcmInput);

            execution.setVariable(SoPropertyConstants.CONTROLLER_STATUS, "Success");
        } catch (Exception e) {
            execution.setVariable(SoPropertyConstants.CONTROLLER_STATUS, "Failure");

            exceptionUtil.buildAndThrowWorkflowException(execution, SDNC_DELEGATE_EXECUTION_ERROR_CODE, e);
        }

        logger.debug("Finish activity for id: {}, name: {}", execution.getCurrentActivityId(),
                execution.getCurrentActivityName());

        return 0;
    }

    @Override
    protected int getErrorCode() {
        return SDNC_DELEGATE_EXECUTION_ERROR_CODE;
    }

    private LcmOutput sendSyncRequest(String operation, LcmInput lcmInput) throws BadResponseException {
        SDNCLcmClientBuilder sdncLcmClientBuilder = new SDNCLcmClientBuilder();
        SDNCLcmRestClient sdncLcmRestClient;
        try {
            sdncLcmRestClient = sdncLcmClientBuilder.newSDNCLcmRestClient(operation);
        } catch (SDNCLcmClientBuilderException e) {
            logger.error("Create SDNCLcmRestClient error: ", e);
            throw new BadResponseException("Can not send request to SDNC.");
        }

        LcmOutput lcmOutput;
        try {
            lcmOutput = sdncLcmRestClient.sendRequest(lcmInput);
        } catch (Exception e) {
            logger.error("SDNCLcmRestClient sends request failure: ", e);
            throw new BadResponseException("Send request to SDNC failure.");
        }
        return lcmOutput;
    }

    private LcmOutput selectLcmOutputFromDmaapResponses(List<LcmDmaapResponse> lcmDmaapResponses, LcmInput lcmInput) {
        String requestId = lcmInput.getCommonHeader().getRequestId();
        String subRequestId = lcmInput.getCommonHeader().getSubRequestId();

        for (LcmDmaapResponse lcmDmaapResponse : lcmDmaapResponses) {
            LcmOutput lcmOutput = lcmDmaapResponse.getBody().getOutput();
            if (requestId.equals(lcmOutput.getCommonHeader().getRequestId())
                    && subRequestId.equals(lcmOutput.getCommonHeader().getSubRequestId())) {
                return lcmOutput;
            }
        }

        return null;
    }

    private LcmOutput sendAsyncRequest(String operation, LcmInput lcmInput) throws BadResponseException {
        SDNCLcmClientBuilder sdncLcmClientBuilder = new SDNCLcmClientBuilder();
        SDNCLcmDmaapClient sdncLcmDmaapClient;
        try {
            sdncLcmDmaapClient = sdncLcmClientBuilder.newSDNCLcmDmaapClient();
        } catch (SDNCLcmClientBuilderException e) {
            logger.error("Create SDNCLcmDmaapClient error: ", e);
            throw new BadResponseException("Can not send request to SDNC.");
        }

        LcmDmaapRequest lcmDmaapRequest = SDNCLcmMessageBuilder.buildLcmDmaapRequest(operation, lcmInput);
        try {
            sdncLcmDmaapClient.sendRequest(lcmDmaapRequest);
        } catch (Exception e) {
            logger.error("SDNCLcmDmaapClient sends request failure: ", e);
            throw new BadResponseException("Send request to SDNC failure.");
        }

        long timeout = sdncLcmClientBuilder.getSDNCLcmProperties().getActionTimeout();
        long startTime = System.currentTimeMillis();
        while (true) {
            List<LcmDmaapResponse> lcmDmaapResponses = sdncLcmDmaapClient.getResponse();
            if (lcmDmaapResponses.size() > 0) {
                LcmOutput lcmOutput = selectLcmOutputFromDmaapResponses(lcmDmaapResponses, lcmInput);
                if (lcmOutput != null) {
                    return lcmOutput;
                }
            }

            long stopTime = System.currentTimeMillis();
            if ((stopTime - startTime) > timeout) {
                String msg = "Timeout for SDNC LCM action " + lcmInput.getAction();
                logger.error(msg);
                throw new BadResponseException(msg);
            }
        }
    }

    public static String toLowerHyphen(String lcmAction) {
        String regex = "([a-z0-9A-Z])(?=[A-Z])";
        String replacement = "$1-";
        return lcmAction.replaceAll(regex, replacement).toLowerCase();
    }

    private String convertToSting(Object msgObject) throws PayloadGenerationException {
        try {
            return SDNCLcmPayloadBuilder.convertToSting(msgObject);
        } catch (JsonProcessingException e) {
            throw new PayloadGenerationException(e.getMessage());
        }
    }

    private LcmInput buildLcmInput(DelegateExecution execution) throws PayloadGenerationException {
        String requestId = String.valueOf(execution.getVariable(REQUEST_ID));
        String requestAction = String.valueOf(execution.getVariable(SoPropertyConstants.SO_ACTION));
        String pnfName = String.valueOf(execution.getVariable(PNF_CORRELATION_ID));
        logger.debug(String.format("requestId: %s, action: %s, pnfName: %s", requestId, requestAction, pnfName));

        String requestPayload = String.valueOf(execution.getVariable(REQUEST_PAYLOAD));
        logger.debug("SO request payload: {}", requestPayload);

        String lcmAction;
        String lcmPayload;

        switch (requestAction) {
            case SoPropertyConstants.ACTION_PRE_CHECK:
                lcmAction = SDNCLcmActionConstants.UPGRADE_PRE_CHECK;

                UpgradePreCheckPayload upgradePreCheckPayload;
                upgradePreCheckPayload = SDNCLcmPayloadBuilder.buildUpgradePreCheckPayload(execution);
                lcmPayload = convertToSting(upgradePreCheckPayload);
                break;
            case SoPropertyConstants.ACTION_DOWNLOAD_N_E_SW:
                lcmAction = SDNCLcmActionConstants.DOWNLOAD_N_E_SW;

                DownloadNESwPayload downloadNESwPayload;
                downloadNESwPayload = SDNCLcmPayloadBuilder.buildDownloadNESwPayload(execution);
                lcmPayload = convertToSting(downloadNESwPayload);
                break;
            case SoPropertyConstants.ACTION_ACTIVATE_N_E_SW:
                lcmAction = SDNCLcmActionConstants.ACTIVATE_N_E_SW;

                ActivateNESwPayload activateNESwPayload;
                activateNESwPayload = SDNCLcmPayloadBuilder.buildActivateNESwPayload(execution);
                lcmPayload = convertToSting(activateNESwPayload);
                break;
            case SoPropertyConstants.ACTION_POST_CHECK:
                lcmAction = SDNCLcmActionConstants.UPGRADE_POST_CHECK;

                UpgradePostCheckPayload upgradePostCheckPayload;
                upgradePostCheckPayload = SDNCLcmPayloadBuilder.buildUpgradePostCheckPayload(execution);
                lcmPayload = convertToSting(upgradePostCheckPayload);
                break;
            default:
                String msg = "Unsupported SO Action: " + requestAction;
                logger.error(msg);
                throw new PayloadGenerationException(msg);
        }

        String subRequestId = UUID.randomUUID().toString();
        LcmInput lcmInput =
                SDNCLcmMessageBuilder.buildLcmInputForPnf(requestId, subRequestId, pnfName, lcmAction, lcmPayload);

        try {
            String lcmInputMsg = mapper.writeValueAsString(lcmInput);
            logger.debug("SDNC input message for {}: {}", lcmAction, lcmInputMsg);
        } catch (JsonProcessingException e) {
            throw new PayloadGenerationException(e.getMessage());
        }

        return lcmInput;
    }

    private void parseLcmOutput(LcmOutput lcmOutput, String lcmAction) throws BadResponseException {
        LcmStatus lcmStatus = lcmOutput.getStatus();
        int lcmStatusCode = lcmStatus.getCode();
        String outputPayload = lcmOutput.getPayload();

        if (lcmStatusCode == SDNCConstants.LCM_OUTPUT_SUCCESS_CODE) {
            logger.debug("Call SDNC LCM API for {} success, code: {}, message: {}, payload: {}", lcmAction,
                    lcmStatusCode, lcmStatus.getMessage(), outputPayload);
        } else {
            String msg = String.format("Call SDNC LCM API for %s failure, code: %d, message: %s, payload: %s",
                    lcmAction, lcmStatusCode, lcmStatus.getMessage(), outputPayload);
            logger.error(msg);
            throw new BadResponseException(msg);
        }
    }

    private void sendLcmRequest(DelegateExecution execution, LcmInput lcmInput) throws BadResponseException {
        String actionMode = String.valueOf(execution.getVariable(SoPropertyConstants.SO_ACTION_MODE));
        String lcmOperation = toLowerHyphen(lcmInput.getAction());

        LcmOutput lcmOutput;
        if ("async".equals(actionMode)) {
            lcmOutput = sendAsyncRequest(lcmOperation, lcmInput);
        } else {
            lcmOutput = sendSyncRequest(lcmOperation, lcmInput);
        }

        parseLcmOutput(lcmOutput, lcmInput.getAction());
    }
}
