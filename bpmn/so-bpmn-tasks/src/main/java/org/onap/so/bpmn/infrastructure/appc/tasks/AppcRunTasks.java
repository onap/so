/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.infrastructure.appc.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.onap.so.logger.LoggingAnchor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.aai.domain.yang.Vserver;
import org.onap.appc.client.lcm.model.Action;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestParameters;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.Relationships;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.appc.ApplicationControllerAction;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAIVnfResources;
import org.onap.so.db.catalog.beans.ControllerSelectionReference;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.logger.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppcRunTasks {
    private static final Logger logger = LoggerFactory.getLogger(AppcRunTasks.class);
    public static final String ROLLBACK_VNF_STOP = "rollbackVnfStop";
    public static final String ROLLBACK_VNF_LOCK = "rollbackVnfLock";
    public static final String ROLLBACK_QUIESCE_TRAFFIC = "rollbackQuiesceTraffic";
    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private ExtractPojosForBB extractPojosForBB;
    @Autowired
    private CatalogDbClient catalogDbClient;
    @Autowired
    private ApplicationControllerAction appCClient;
    @Autowired
    private AAIVnfResources aaiVnfResources;

    public void preProcessActivity(BuildingBlockExecution execution) {
        execution.setVariable("actionSnapshot", Action.Snapshot);
        execution.setVariable("actionLock", Action.Lock);
        execution.setVariable("actionUnlock", Action.Unlock);
        execution.setVariable("actionUpgradePreCheck", Action.UpgradePreCheck);
        execution.setVariable("actionUpgradePostCheck", Action.UpgradePostCheck);
        execution.setVariable("actionQuiesceTraffic", Action.QuiesceTraffic);
        execution.setVariable("actionUpgradeBackup", Action.UpgradeBackup);
        execution.setVariable("actionUpgradeSoftware", Action.UpgradeSoftware);
        execution.setVariable("actionResumeTraffic", Action.ResumeTraffic);
        execution.setVariable("actionStop", Action.Stop);
        execution.setVariable("actionStart", Action.Start);
        execution.setVariable("actionHealthCheck", Action.HealthCheck);
        execution.setVariable("actionDistributeTraffic", Action.DistributeTraffic);
        execution.setVariable("actionDistributeTrafficCheck", Action.DistributeTrafficCheck);
        execution.setVariable(ROLLBACK_VNF_STOP, false);
        execution.setVariable(ROLLBACK_VNF_LOCK, false);
        execution.setVariable(ROLLBACK_QUIESCE_TRAFFIC, false);

        GenericVnf vnf = null;
        try {
            vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
        } catch (BBObjectNotFoundException e) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "No valid VNF exists");
        }

        try {
            getVserversForAppc(execution, vnf);
        } catch (Exception e) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "Unable to retrieve vservers");
        }

    }

    public void runAppcCommand(BuildingBlockExecution execution, Action action) {
        logger.trace("Start runAppcCommand ");
        String appcCode = "1002";
        String appcMessage = "";
        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
            GenericVnf vnf = null;
            try {
                vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            } catch (BBObjectNotFoundException e) {
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, "No valid VNF exists");
            }
            String vnfId = null;
            String vnfName = null;
            String vnfType = null;
            String vnfHostIpAddress = null;

            if (vnf != null) {
                vnfId = vnf.getVnfId();
                vnfName = vnf.getVnfName();
                vnfType = vnf.getVnfType();
                vnfHostIpAddress = vnf.getIpv4OamAddress();
            }
            String msoRequestId = gBBInput.getRequestContext().getMsoRequestId();

            String aicIdentity = execution.getVariable("aicIdentity");
            String vmIdList = execution.getVariable("vmIdList");
            String vserverIdList = execution.getVariable("vserverIdList");
            String identityUrl = execution.getVariable("identityUrl");

            ControllerSelectionReference controllerSelectionReference = catalogDbClient
                    .getControllerSelectionReferenceByVnfTypeAndActionCategory(vnfType, action.toString());
            String controllerType = controllerSelectionReference.getControllerName();

            String vfModuleId = null;
            VfModule vfModule = null;
            try {
                vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            } catch (BBObjectNotFoundException e) {
            }
            if (vfModule != null) {
                vfModuleId = vfModule.getVfModuleId();
            }

            HashMap<String, String> payloadInfo = buildPayloadInfo(vnfName, aicIdentity, vnfHostIpAddress, vmIdList,
                    vserverIdList, identityUrl, vfModuleId);
            Optional<String> payload = null;
            RequestParameters requestParameters = gBBInput.getRequestContext().getRequestParameters();
            if (requestParameters != null) {
                String pay = requestParameters.getPayload();
                if (pay != null) {
                    payload = Optional.of(pay);
                }
            }
            logger.debug("Running APP-C action: {}", action.toString());
            logger.debug("VNFID: {}", vnfId);
            appCClient.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);
            appcCode = appCClient.getErrorCode();
            appcMessage = appCClient.getErrorMessage();
            mapRollbackVariables(execution, action, appcCode);
        } catch (Exception e) {
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION.toString(),
                    "Caught exception in runAppcCommand", "BPMN", ErrorCode.UnknownError.getValue(), "APPC Error", e);
            appcMessage = e.getMessage();
        }

        logger.error("Error Message: {}", appcMessage);
        logger.error("ERROR CODE: {}", appcCode);
        logger.trace("End of runAppCommand ");
        if (appcCode != null && !appcCode.equals("0")) {
            exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(appcCode), appcMessage);
        }
    }

    protected void mapRollbackVariables(BuildingBlockExecution execution, Action action, String appcCode) {
        if (appcCode != null && appcCode.equals("0") && action != null) {
            if (action.equals(Action.Lock)) {
                execution.setVariable(ROLLBACK_VNF_LOCK, true);
            } else if (action.equals(Action.Unlock)) {
                execution.setVariable(ROLLBACK_VNF_LOCK, false);
            } else if (action.equals(Action.Start)) {
                execution.setVariable(ROLLBACK_VNF_STOP, false);
            } else if (action.equals(Action.Stop)) {
                execution.setVariable(ROLLBACK_VNF_STOP, true);
            } else if (action.equals(Action.QuiesceTraffic)) {
                execution.setVariable(ROLLBACK_QUIESCE_TRAFFIC, true);
            } else if (action.equals(Action.ResumeTraffic)) {
                execution.setVariable(ROLLBACK_QUIESCE_TRAFFIC, false);
            }
        }
    }

    private HashMap<String, String> buildPayloadInfo(String vnfName, String aicIdentity, String vnfHostIpAddress,
            String vmIdList, String vserverIdList, String identityUrl, String vfModuleId) {
        HashMap<String, String> payloadInfo = new HashMap<String, String>();
        payloadInfo.put("vnfName", vnfName);
        payloadInfo.put("aicIdentity", aicIdentity);
        payloadInfo.put("vnfHostIpAddress", vnfHostIpAddress);
        payloadInfo.put("vmIdList", vmIdList);
        payloadInfo.put("vserverIdList", vserverIdList);
        payloadInfo.put("identityUrl", identityUrl);
        payloadInfo.put("vfModuleId", vfModuleId);
        return payloadInfo;
    }

    protected void getVserversForAppc(BuildingBlockExecution execution, GenericVnf vnf) throws Exception {
        AAIResultWrapper aaiRW = aaiVnfResources.queryVnfWrapperById(vnf);

        if (aaiRW != null && aaiRW.getRelationships() != null) {
            Relationships relationships = aaiRW.getRelationships().get();
            if (relationships != null) {
                List<AAIResourceUri> vserverUris = relationships.getRelatedAAIUris(AAIObjectType.VSERVER);
                JSONArray vserverIds = new JSONArray();
                JSONArray vserverSelfLinks = new JSONArray();
                for (AAIResourceUri j : vserverUris) {
                    if (j != null) {
                        if (j.getURIKeys() != null) {
                            String vserverId = j.getURIKeys().get("vserver-id");
                            vserverIds.put(vserverId);
                        }
                        Optional<Vserver> oVserver = aaiVnfResources.getVserver(j);
                        if (oVserver.isPresent()) {
                            Vserver vserver = oVserver.get();
                            if (vserver != null) {
                                String vserverSelfLink = vserver.getVserverSelflink();
                                vserverSelfLinks.put(vserverSelfLink);
                            }
                        }
                    }
                }

                JSONObject vmidsArray = new JSONObject();
                JSONObject vserveridsArray = new JSONObject();
                vmidsArray.put("vmIds", vserverSelfLinks.toString());
                vserveridsArray.put("vserverIds", vserverIds.toString());
                logger.debug("vmidsArray is: {}", vmidsArray.toString());
                logger.debug("vserveridsArray is: {}", vserveridsArray.toString());

                execution.setVariable("vmIdList", vmidsArray.toString());
                execution.setVariable("vserverIdList", vserveridsArray.toString());
            }
        }
    }
}
