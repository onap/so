/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.bpmn.core.json.JsonUtils;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.*;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.NF_SOFTWARE_VERSION;

/**
 * This implementation of {@link JavaDelegate} is used to check the SO catalogdb for PNF config flag.
 */
@Component
public class NfSoftwareUpgradeCheckerDelegate implements JavaDelegate {

    private Logger logger = LoggerFactory.getLogger(NfSoftwareUpgradeCheckerDelegate.class);

    private static final String SERVICE_INSTANCE_NAME = "serviceInstanceName";
    private static final String BPMN_REQUEST = "bpmnRequest";
    private static final String REQUEST_MODEL_INFO = "requestDetails.modelInfo";
    private static final String REQUEST_INSTANCE_NAME = "requestDetails.requestInfo.instanceName";

    // ERROR CODE for variable not found in the delegation Context
    private static final int ERROR_CODE = 601;

    @Autowired
    protected ExceptionBuilder exceptionUtil;

    @Autowired
    protected CatalogDbClient catalogDbClient;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        logger.debug("Running execute block for activity id:{}, name:{}", delegateExecution.getCurrentActivityId(),
                delegateExecution.getCurrentActivityName());

        // extract requestId
        String requestId = (String) delegateExecution.getVariable("mso-request-id");
        delegateExecution.setVariable(MSO_REQUEST_ID, requestId);

        String bpmnRequest = (String) delegateExecution.getVariable(BPMN_REQUEST);

        String serviceModelInfo = JsonUtils.getJsonValue(bpmnRequest, REQUEST_MODEL_INFO);
        delegateExecution.setVariable(SERVICE_MODEL_INFO, serviceModelInfo);

        String serviceInstanceName = JsonUtils.getJsonValue(bpmnRequest, REQUEST_INSTANCE_NAME);
        delegateExecution.setVariable(SERVICE_INSTANCE_NAME, serviceInstanceName);

        logger.trace("Completed preProcessRequest PnfSoftwareUpgradeServiceRequest Request ");

        //Generate PNF UUID
        UUID uuid = UUID.randomUUID();
        logger.debug("Generated UUID for pnf: {}, version: {}, variant: {}", uuid, uuid.version(), uuid.variant());
        delegateExecution.setVariable(PNF_UUID, uuid.toString());

        if (delegateExecution.hasVariable(SERVICE_MODEL_INFO)) {
            String serviceModelUuid = JsonUtils.getJsonValue(serviceModelInfo, MODEL_UUID);
            delegateExecution.setVariable(MODEL_UUID, serviceModelUuid);
            List<PnfResourceCustomization> pnfCustomizations =
                    catalogDbClient.getPnfResourceCustomizationByModelUuid(serviceModelUuid);
            if (pnfCustomizations != null && !pnfCustomizations.isEmpty()) {
                PnfResourceCustomization pnfResourceCustomization = pnfCustomizations.get(0);
                delegateExecution.setVariable(PRC_BLUEPRINT_NAME, pnfResourceCustomization.getBlueprintName());
                delegateExecution.setVariable(PRC_BLUEPRINT_VERSION, pnfResourceCustomization.getBlueprintVersion());
                delegateExecution.setVariable(PRC_CUSTOMIZATION_UUID,
                        pnfResourceCustomization.getModelCustomizationUUID());
                delegateExecution.setVariable(PRC_INSTANCE_NAME, pnfResourceCustomization.getModelInstanceName());
                delegateExecution.setVariable(NF_SOFTWARE_VERSION, pnfResourceCustomization.getSoftwareVersion());
            } else {
                logger.warn("Unable to find the PNF resource customizations of model service UUID: {}",
                        serviceModelUuid);
                exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ERROR_CODE,
                        "Unable to find the PNF resource customizations of model service UUID:  " + serviceModelUuid);
            }
        } else {
            logger.warn("Unable to find the parameter: {} in the execution context", SERVICE_MODEL_INFO);
            exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ERROR_CODE,
                    "Unable to find parameter " + SERVICE_MODEL_INFO);
        }
    }
}
