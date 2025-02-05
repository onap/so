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

import static org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda.controller.common.SoPropertyConstants.CONTROLLER_STATUS;
import java.util.HashMap;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.onap.appc.client.lcm.model.Action;
import org.onap.so.logging.filter.base.ErrorCode;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.appc.ApplicationControllerAction;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.ControllerSelectionReference;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.logger.LoggingAnchor;
import org.onap.so.logger.MessageEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenericVnfHealthCheck {

    private static final Logger logger = LoggerFactory.getLogger(GenericVnfHealthCheck.class);
    public static final String VNF_NAME = "vnfName";
    public static final String OAM_IP_ADDRESS = "oamIpAddress";
    public static final String VNF_HOST_IP_ADDRESS = "vnfHostIpAddress";
    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private ExtractPojosForBB extractPojosForBB;
    @Autowired
    private CatalogDbClient catalogDbClient;
    @Autowired
    private ApplicationControllerAction appCClient;

    public void setParamsForGenericVnfHealthCheck(BuildingBlockExecution execution) {

        GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();

        try {
            ControllerSelectionReference controllerSelectionReference;
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            String vnfId = vnf.getVnfId();
            String vnfName = vnf.getVnfName();
            String vnfType = vnf.getVnfType();
            String oamIpAddress = vnf.getIpv4OamAddress();
            String actionCategory = Action.HealthCheck.toString();
            controllerSelectionReference =
                    catalogDbClient.getControllerSelectionReferenceByVnfTypeAndActionCategory(vnfType, actionCategory);
            String controllerName = controllerSelectionReference.getControllerName();

            execution.setVariable("vnfId", vnfId);
            execution.setVariable(VNF_NAME, vnfName);
            execution.setVariable(OAM_IP_ADDRESS, oamIpAddress);
            execution.setVariable(VNF_HOST_IP_ADDRESS, oamIpAddress);
            execution.setVariable("msoRequestId", gBBInput.getRequestContext().getMsoRequestId());
            execution.setVariable("action", actionCategory);
            execution.setVariable("controllerType", controllerName);

        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    public void callAppcClient(BuildingBlockExecution execution) {
        logger.trace("Start runAppcCommand ");
        String appcCode = "1002";
        String appcMessage;
        try {
            Action action = Action.valueOf(execution.getVariable("action"));
            String msoRequestId = execution.getVariable("msoRequestId");
            String vnfId = execution.getVariable("vnfId");
            Optional<String> payload = Optional.empty();
            if (execution.getVariable("payload") != null) {
                String pay = execution.getVariable("payload");
                payload = Optional.of(pay);
            }
            String controllerType = execution.getVariable("controllerType");
            HashMap<String, String> payloadInfo = new HashMap<>();
            payloadInfo.put(VNF_NAME, execution.getVariable(VNF_NAME));
            payloadInfo.put("vfModuleId", execution.getVariable("vfModuleId"));
            payloadInfo.put(OAM_IP_ADDRESS, execution.getVariable(OAM_IP_ADDRESS));
            payloadInfo.put(VNF_HOST_IP_ADDRESS, execution.getVariable(VNF_HOST_IP_ADDRESS));

            logger.debug("Running APP-C action: {}", action);
            logger.debug("VNFID: {}", vnfId);
            // PayloadInfo contains extra information that adds on to payload before making request to appc
            appCClient.runAppCCommand(action, msoRequestId, vnfId, payload, payloadInfo, controllerType);
            appcCode = appCClient.getErrorCode();
            appcMessage = appCClient.getErrorMessage();
        } catch (BpmnError ex) {
            logger.error(LoggingAnchor.FOUR, MessageEnum.BPMN_GENERAL_EXCEPTION_ARG.toString(),
                    "Caught exception in GenericVnfHealthCheck", "BPMN", ErrorCode.UnknownError.getValue(), ex);
            appcMessage = ex.getMessage();
            exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(appcCode), appcMessage);
        } catch (Exception e) {
            if (e instanceof java.util.concurrent.TimeoutException) {
                appcMessage = "Request to APPC timed out. ";
                logger.error(LoggingAnchor.FIVE, MessageEnum.RA_CONNECTION_EXCEPTION.toString(),
                        "Caught timedOut exception in runAppcCommand in GenericVnfHealthCheck", "BPMN",
                        ErrorCode.UnknownError.getValue(), "APPC Error", e);
                throw e;
            } else {
                logger.error(LoggingAnchor.FIVE, MessageEnum.BPMN_GENERAL_EXCEPTION.toString(),
                        "Caught exception in runAppcCommand in GenericVnfHealthCheck", "BPMN",
                        ErrorCode.UnknownError.getValue(), "APPC Error", e);
                appcMessage = e.getMessage();
                exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(appcCode), appcMessage);
            }
        }

        logger.error("Error Message: " + appcMessage);
        logger.error("ERROR CODE: " + appcCode);
        if (appcCode != null && !("0").equals(appcCode)) {
            exceptionUtil.buildAndThrowWorkflowException(execution, Integer.parseInt(appcCode), appcMessage);
        }

        execution.setVariable(CONTROLLER_STATUS, "Success");
        logger.debug("Successfully end of runAppCommand ");
    }
}
