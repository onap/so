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
package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.onap.so.logger.LoggingAnchor;
import org.onap.appc.client.lcm.model.Action;
import org.onap.so.bpmn.appc.payload.beans.ConfigScaleOutPayload;
import org.onap.so.bpmn.appc.payload.beans.RequestParametersConfigScaleOut;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.appc.ApplicationControllerAction;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.ControllerSelectionReference;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.logging.filter.base.ErrorCode;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

@Component
public class ConfigurationScaleOut {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationScaleOut.class);
    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private ExtractPojosForBB extractPojosForBB;
    @Autowired
    private CatalogDbClient catalogDbClient;
    @Autowired
    private ApplicationControllerAction appCClient;
    private static final String ACTION = "action";
    private static final String MSO_REQUEST_ID = "msoRequestId";
    private static final String VNF_ID = "vnfId";
    private static final String VNF_NAME = "vnfName";
    private static final String VFMODULE_ID = "vfModuleId";
    private static final String CONTROLLER_TYPE = "controllerType";
    private static final String PAYLOAD = "payload";
    private static final ObjectMapper mapper = new ObjectMapper();

    public void setParamsForConfigurationScaleOut(BuildingBlockExecution execution) {

        GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();

        try {
            List<Map<String, String>> jsonPathForCfgParams = gBBInput.getRequestContext().getConfigurationParameters();
            String key = null;
            String paramValue = null;
            String configScaleOutPayloadString = null;
            ControllerSelectionReference controllerSelectionReference;
            ConfigScaleOutPayload configPayload = new ConfigScaleOutPayload();
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            String vnfId = vnf.getVnfId();
            String vnfName = vnf.getVnfName();
            String vnfType = vnf.getVnfType();
            String actionCategory = Action.ConfigScaleOut.toString();
            controllerSelectionReference =
                    catalogDbClient.getControllerSelectionReferenceByVnfTypeAndActionCategory(vnfType, actionCategory);
            String controllerName = controllerSelectionReference.getControllerName();

            VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            String sdncVfModuleQueryResponse = execution.getVariable("SDNCQueryResponse_" + vfModule.getVfModuleId());

            Map<String, String> paramsMap = new HashMap<>();
            RequestParametersConfigScaleOut requestParameters = new RequestParametersConfigScaleOut();
            String configScaleOutParam = null;
            if (jsonPathForCfgParams != null) {
                for (Map<String, String> param : jsonPathForCfgParams) {
                    for (Map.Entry<String, String> entry : param.entrySet()) {
                        key = entry.getKey();
                        paramValue = entry.getValue();
                        try {
                            configScaleOutParam = JsonPath.parse(sdncVfModuleQueryResponse).read(paramValue);
                        } catch (ClassCastException e) {
                            configScaleOutParam = null;
                            logger.warn("Incorrect JSON path. Path points to object rather than value causing: ", e);
                        }
                        paramsMap.put(key, configScaleOutParam);
                    }
                }
            }
            requestParameters.setVfModuleId(vfModule.getVfModuleId());
            requestParameters.setVnfHostIpAddress(vnf.getIpv4OamAddress());
            configPayload.setConfigurationParameters(paramsMap);
            configPayload.setRequestParameters(requestParameters);
            configScaleOutPayloadString = mapper.writeValueAsString(configPayload);

            execution.setVariable(ACTION, actionCategory);
            execution.setVariable(MSO_REQUEST_ID, gBBInput.getRequestContext().getMsoRequestId());
            execution.setVariable(VNF_ID, vnfId);
            execution.setVariable(VNF_NAME, vnfName);
            execution.setVariable(VFMODULE_ID, vfModule.getVfModuleId());
            execution.setVariable(CONTROLLER_TYPE, controllerName);
            execution.setVariable(PAYLOAD, configScaleOutPayloadString);
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    public void callAppcClient(BuildingBlockExecution execution) {
        logger.trace("Start runAppcCommand ");
        String appcCode = "1002";
        String appcMessage = "";
        try {
            Action commandAction = Action.valueOf(execution.getVariable(ACTION));
            String msoRequestId = execution.getVariable(MSO_REQUEST_ID);
            String vnfId = execution.getVariable(VNF_ID);
            Optional<String> payloadString = null;
            if (execution.getVariable(PAYLOAD) != null) {
                String pay = execution.getVariable(PAYLOAD);
                payloadString = Optional.of(pay);
            }
            String controllerType = execution.getVariable(CONTROLLER_TYPE);
            HashMap<String, String> payloadInfo = new HashMap<>();
            payloadInfo.put(VNF_NAME, execution.getVariable(VNF_NAME));
            payloadInfo.put(VFMODULE_ID, execution.getVariable(VFMODULE_ID));
            logger.debug("Running APP-C action: {}", commandAction.toString());
            logger.debug("VNFID: {}", vnfId);
            // PayloadInfo contains extra information that adds on to payload before making request to appc
            appCClient.runAppCCommand(commandAction, msoRequestId, vnfId, payloadString, payloadInfo, controllerType);
            appcCode = appCClient.getErrorCode();
            appcMessage = appCClient.getErrorMessage();

        } catch (Exception e) {
            logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION.toString(),
                    "Caught exception in runAppcCommand in ConfigurationScaleOut", "BPMN",
                    ErrorCode.UnknownError.getValue(), "APPC Error", e);
            appcMessage = e.getMessage();
        }
        logger.error("Error Message: " + appcMessage);
        logger.error("ERROR CODE: " + appcCode);
        logger.trace("End of runAppCommand ");
        if (appcCode != null && !("0").equals(appcCode)) {
            exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(appcCode), appcMessage);
        }
    }
}
