/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.appc;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.onap.appc.client.lcm.model.Action;
import org.onap.appc.client.lcm.model.Status;
import org.onap.so.bpmn.appc.payload.PayloadClient;
import org.onap.so.bpmn.core.json.JsonUtils;
import org.onap.so.client.appc.ApplicationControllerSupport.StatusCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ApplicationControllerAction {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationControllerAction.class);
    private static final String PAYLOAD_NOT_PRESENT_ERROR_MSG = "Payload is not present for ";
    private static final ObjectMapper mapper = new ObjectMapper();
    protected ApplicationControllerOrchestrator client = new ApplicationControllerOrchestrator();
    private String errorCode = "1002";
    private String errorMessage = "Unable to reach App C Servers";

    public void runAppCCommand(Action action, String msoRequestId, String vnfId, Optional<String> payload,
            Map<String, String> payloadInfo, String controllerType) {
        Status appCStatus = null;
        try {
            String vnfName = payloadInfo.getOrDefault("vnfName", "");
            String aicIdentity = payloadInfo.getOrDefault("vnfName", "");
            String vnfHostIpAddress = payloadInfo.getOrDefault("vnfHostIpAddress", "");
            String vmIdList = payloadInfo.getOrDefault("vmIdList", "");
            String vserverIdList = payloadInfo.getOrDefault("vserverIdList", "");
            String identityUrl = payloadInfo.getOrDefault("identityUrl", "");
            switch (action) {
                case ResumeTraffic:
                    appCStatus = resumeTrafficAction(msoRequestId, vnfId, vnfName, controllerType);
                    break;
                case Start:
                case Stop:
                    appCStatus = startStopAction(action, msoRequestId, vnfId, aicIdentity, controllerType);
                    break;
                case Unlock:
                case Lock:
                    appCStatus = client.vnfCommand(action, msoRequestId, vnfId, Optional.empty(), Optional.empty(),
                            controllerType);
                    break;
                case QuiesceTraffic:
                    appCStatus = quiesceTrafficAction(msoRequestId, vnfId, payload, vnfName, controllerType);
                    break;
                case DistributeTraffic:
                    appCStatus = distributeTrafficAction(msoRequestId, vnfId, payload, vnfName, controllerType);
                    break;
                case DistributeTrafficCheck:
                    appCStatus = distributeTrafficCheckAction(msoRequestId, vnfId, payload, vnfName, controllerType);
                    break;
                case HealthCheck:
                    appCStatus = healthCheckAction(msoRequestId, vnfId, vnfName, vnfHostIpAddress, controllerType);
                    break;
                case Snapshot:
                    if (vmIdList.isEmpty()) {
                        logger.warn("vmIdList is Empty in AppCClient");
                        break;
                    }
                    String vmIds = JsonUtils.getJsonValue(vmIdList, "vmIds");
                    if (vmIds == null) {
                        logger.warn("vmIds null in AppCClient");
                        break;
                    }
                    if (vserverIdList.isEmpty()) {
                        logger.warn("vserverIdList is empty in AppCClient");
                        break;
                    }
                    String vserverIds = JsonUtils.getJsonValue(vserverIdList, "vserverIds");
                    if (vserverIds == null) {
                        logger.warn("vserverIds  null in AppCClient");
                        break;
                    }
                    appCStatus = getSnapshotActionAppcStatus(msoRequestId, vnfId, controllerType, identityUrl, vmIds,
                            vserverIds);
                    break;
                case ConfigModify:
                case ConfigScaleOut:
                    appCStatus = payloadAction(action, msoRequestId, vnfId, payload, controllerType);
                    break;
                case UpgradePreCheck:
                case UpgradePostCheck:
                case UpgradeSoftware:
                case UpgradeBackup:
                    appCStatus = upgradeAction(action, msoRequestId, vnfId, payload, vnfName, controllerType);
                    break;
                default:
                    errorMessage = "Unable to idenify Action request for AppCClient";
                    break;
            }
            if (appCStatus != null) {
                errorCode = Integer.toString(appCStatus.getCode());
                errorMessage = appCStatus.getMessage();

                if (ApplicationControllerSupport.getCategoryOf(appCStatus).equals(StatusCategory.NORMAL)) {
                    errorCode = "0";
                }
            }
        } catch (JsonProcessingException e) {
            logger.error("Incorrect Payload format for action request: {}", action.toString(), e);
            errorMessage = e.getMessage();
        } catch (ApplicationControllerOrchestratorException e) {
            logger.error("Error building Appc request: {}", e.getMessage(), e);
            errorCode = "1002";
            errorMessage = e.getMessage();
        } catch (Exception e) {
            logger.error("Error building Appc request: {}", e.getMessage(), e);
            errorMessage = e.getMessage();
        }
    }

    private Status getSnapshotActionAppcStatus(String msoRequestId, String vnfId, String controllerType,
            String identityUrl, String vmIds, String vserverIds)
            throws IOException, ApplicationControllerOrchestratorException {
        Status appcStatus = null;
        String vmId = "";
        String vserverId = "";
        List<String> vmIdJsonList = mapper.readValue(vmIds, new TypeReference<List<String>>() {});
        List<String> vserverIdJsonList = mapper.readValue(vserverIds, new TypeReference<List<String>>() {});
        int i = 0;
        while (i < vmIdJsonList.size()) {
            vmId = vmIdJsonList.get(i);
            vserverId = vserverIdJsonList.get(i);
            Optional<String> vserverIdString = Optional.of(vserverId);
            appcStatus = snapshotAction(msoRequestId, vnfId, vmId, vserverIdString, identityUrl, controllerType);
            i++;
        }
        return appcStatus;
    }

    private Status payloadAction(Action action, String msoRequestId, String vnfId, Optional<String> payload,
            String controllerType) throws ApplicationControllerOrchestratorException {
        if (!(payload.isPresent())) {
            throw new IllegalArgumentException(PAYLOAD_NOT_PRESENT_ERROR_MSG + action.toString());
        }
        return client.vnfCommand(action, msoRequestId, vnfId, Optional.empty(), payload, controllerType);
    }

    private Status quiesceTrafficAction(String msoRequestId, String vnfId, Optional<String> payload, String vnfName,
            String controllerType) throws JsonProcessingException, ApplicationControllerOrchestratorException {
        if (!(payload.isPresent())) {
            throw new IllegalArgumentException(PAYLOAD_NOT_PRESENT_ERROR_MSG + Action.QuiesceTraffic.toString());
        }
        payload = PayloadClient.quiesceTrafficFormat(payload, vnfName);
        return client.vnfCommand(Action.QuiesceTraffic, msoRequestId, vnfId, Optional.empty(), payload, controllerType);
    }

    private Status upgradeAction(Action action, String msoRequestId, String vnfId, Optional<String> payload,
            String vnfName, String controllerType)
            throws JsonProcessingException, ApplicationControllerOrchestratorException {
        if (!(payload.isPresent())) {
            throw new IllegalArgumentException(PAYLOAD_NOT_PRESENT_ERROR_MSG + action.toString());
        }
        payload = PayloadClient.upgradeFormat(payload, vnfName);
        return client.vnfCommand(action, msoRequestId, vnfId, Optional.empty(), payload, controllerType);
    }

    private Status distributeTrafficAction(String msoRequestId, String vnfId, Optional<String> payload, String vnfName,
            String controllerType) throws JsonProcessingException, ApplicationControllerOrchestratorException {
        if (!(payload.isPresent())) {
            throw new IllegalArgumentException(PAYLOAD_NOT_PRESENT_ERROR_MSG + Action.DistributeTraffic.toString());
        }
        payload = PayloadClient.distributeTrafficFormat(payload, vnfName);
        return client.vnfCommand(Action.DistributeTraffic, msoRequestId, vnfId, Optional.empty(), payload,
                controllerType);
    }

    private Status distributeTrafficCheckAction(String msoRequestId, String vnfId, Optional<String> payload,
            String vnfName, String controllerType)
            throws JsonProcessingException, ApplicationControllerOrchestratorException {
        if (!(payload.isPresent())) {
            throw new IllegalArgumentException(
                    PAYLOAD_NOT_PRESENT_ERROR_MSG + Action.DistributeTrafficCheck.toString());
        }
        payload = PayloadClient.distributeTrafficCheckFormat(payload, vnfName);
        return client.vnfCommand(Action.DistributeTrafficCheck, msoRequestId, vnfId, Optional.empty(), payload,
                controllerType);
    }

    private Status resumeTrafficAction(String msoRequestId, String vnfId, String vnfName, String controllerType)
            throws JsonProcessingException, ApplicationControllerOrchestratorException {
        Optional<String> payload = PayloadClient.resumeTrafficFormat(vnfName);
        return client.vnfCommand(Action.ResumeTraffic, msoRequestId, vnfId, Optional.empty(), payload, controllerType);
    }

    private Status startStopAction(Action action, String msoRequestId, String vnfId, String aicIdentity,
            String controllerType) throws JsonProcessingException, ApplicationControllerOrchestratorException {
        Optional<String> payload = PayloadClient.startStopFormat(aicIdentity);
        return client.vnfCommand(action, msoRequestId, vnfId, Optional.empty(), payload, controllerType);
    }

    private Status healthCheckAction(String msoRequestId, String vnfId, String vnfName, String vnfHostIpAddress,
            String controllerType) throws JsonProcessingException, ApplicationControllerOrchestratorException {
        Optional<String> payload = PayloadClient.healthCheckFormat(vnfName, vnfHostIpAddress);
        return client.vnfCommand(Action.HealthCheck, msoRequestId, vnfId, Optional.empty(), payload, controllerType);
    }

    private Status snapshotAction(String msoRequestId, String vnfId, String vmId, Optional<String> vserverId,
            String identityUrl, String controllerType)
            throws JsonProcessingException, ApplicationControllerOrchestratorException {
        Optional<String> payload = PayloadClient.snapshotFormat(vmId, identityUrl);
        return client.vnfCommand(Action.Snapshot, msoRequestId, vnfId, vserverId, payload, controllerType);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
