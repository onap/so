/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda.controller.sdnc;

import java.util.Arrays;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.bpmn.core.json.JsonUtils;
import org.onap.so.client.sdnc.lcm.beans.payload.*;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.REQUEST_PAYLOAD;
import org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda.controller.common.SoPropertyConstants;

public class SDNCLcmPayloadBuilder {

    private static Logger logger = LoggerFactory.getLogger(SDNCLcmPayloadBuilder.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static ActivateNESwPayload buildActivateNESwPayload(DelegateExecution execution) {
        String requestPayload = String.valueOf(execution.getVariable(REQUEST_PAYLOAD));

        ActivateNESwPayload activateNESwPayload = new ActivateNESwPayload();

        String ipaddressV4Oam = JsonUtils.getJsonValue(requestPayload, "ipaddressV4Oam");
        activateNESwPayload.setIpaddressV4Oam(ipaddressV4Oam);

        String targetSwVersion = String.valueOf(execution.getVariable(SoPropertyConstants.TARGET_SOFTWARE_VERSION));
        if (targetSwVersion == null) {
            targetSwVersion = JsonUtils.getJsonValue(requestPayload, "targetSwVersion");
        }
        activateNESwPayload.setSwVersionToBeActivated(targetSwVersion);

        String playbookName = JsonUtils.getJsonValue(requestPayload, "activateNESwPlaybook");
        if (playbookName != null) {
            activateNESwPayload.setPlaybookName(playbookName);
        }

        return activateNESwPayload;
    }

    public static DownloadNESwPayload buildDownloadNESwPayload(DelegateExecution execution) {
        String requestPayload = String.valueOf(execution.getVariable(REQUEST_PAYLOAD));

        DownloadNESwPayload downloadNESwPayload = new DownloadNESwPayload();

        String ipaddressV4Oam = JsonUtils.getJsonValue(requestPayload, "ipaddressV4Oam");
        downloadNESwPayload.setIpaddressV4Oam(ipaddressV4Oam);

        String swToBeDownloadedString = JsonUtils.getJsonValue(requestPayload, "swToBeDownloaded");
        SwToBeDownloadedElement[] swToBeDownloadedArray;

        try {
            swToBeDownloadedArray = mapper.readValue(swToBeDownloadedString, SwToBeDownloadedElement[].class);
            downloadNESwPayload.setSwToBeDownloaded(Arrays.asList(swToBeDownloadedArray));
        } catch (Exception e) {
            logger.error("Parse SwToBeDownloaded error: ", e);
        }

        String playbookName = JsonUtils.getJsonValue(requestPayload, "downloadNESwPlaybook");
        if (playbookName != null) {
            downloadNESwPayload.setPlaybookName(playbookName);
        }

        return downloadNESwPayload;
    }

    public static UpgradePostCheckPayload buildUpgradePostCheckPayload(DelegateExecution execution) {
        String requestPayload = String.valueOf(execution.getVariable(REQUEST_PAYLOAD));

        UpgradePostCheckPayload upgradePostCheckPayload = new UpgradePostCheckPayload();

        String ipaddressV4Oam = JsonUtils.getJsonValue(requestPayload, "ipaddressV4Oam");
        upgradePostCheckPayload.setIpaddressV4Oam(ipaddressV4Oam);

        String oldSwVersion = JsonUtils.getJsonValue(requestPayload, "oldSwVersion");
        upgradePostCheckPayload.setOldSwVersion(oldSwVersion);

        String targetSwVersion = String.valueOf(execution.getVariable(SoPropertyConstants.TARGET_SOFTWARE_VERSION));
        if (targetSwVersion == null) {
            targetSwVersion = JsonUtils.getJsonValue(requestPayload, "targetSwVersion");
        }
        upgradePostCheckPayload.setTargetSwVersion(targetSwVersion);

        String ruleName = JsonUtils.getJsonValue(requestPayload, "postCheckRuleName");
        upgradePostCheckPayload.setRuleName(ruleName);

        String additionalData = JsonUtils.getJsonValue(requestPayload, "postCheckAdditionalData");
        upgradePostCheckPayload.setAdditionalData(additionalData);

        String playbookName = JsonUtils.getJsonValue(requestPayload, "postCheckPlaybook");
        if (playbookName != null) {
            upgradePostCheckPayload.setPlaybookName(playbookName);
        }

        return upgradePostCheckPayload;
    }

    public static UpgradePreCheckPayload buildUpgradePreCheckPayload(DelegateExecution execution) {
        String requestPayload = String.valueOf(execution.getVariable(REQUEST_PAYLOAD));

        UpgradePreCheckPayload upgradePreCheckPayload = new UpgradePreCheckPayload();

        String ipaddressV4Oam = JsonUtils.getJsonValue(requestPayload, "ipaddressV4Oam");
        upgradePreCheckPayload.setIpaddressV4Oam(ipaddressV4Oam);

        String oldSwVersion = JsonUtils.getJsonValue(requestPayload, "oldSwVersion");
        upgradePreCheckPayload.setOldSwVersion(oldSwVersion);

        String targetSwVersion = String.valueOf(execution.getVariable(SoPropertyConstants.TARGET_SOFTWARE_VERSION));
        if (targetSwVersion == null) {
            targetSwVersion = JsonUtils.getJsonValue(requestPayload, "targetSwVersion");
        }
        upgradePreCheckPayload.setTargetSwVersion(targetSwVersion);

        String ruleName = JsonUtils.getJsonValue(requestPayload, "preCheckRuleName");
        upgradePreCheckPayload.setRuleName(ruleName);

        String additionalData = JsonUtils.getJsonValue(requestPayload, "preCheckAdditionalData");
        upgradePreCheckPayload.setAdditionalData(additionalData);

        String playbookName = JsonUtils.getJsonValue(requestPayload, "preCheckPlaybook");
        if (playbookName != null) {
            upgradePreCheckPayload.setPlaybookName(playbookName);
        }

        return upgradePreCheckPayload;
    }

    public static String convertToSting(Object msgObject) throws JsonProcessingException {
        return mapper.writeValueAsString(msgObject);
    }
}
