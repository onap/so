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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.aai.domain.yang.Pnf;
import org.onap.so.bpmn.core.json.JsonUtils;
import org.onap.so.bpmn.infrastructure.pnf.management.PnfManagement;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.PnfResourceCustomization;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.SERVICE_MODEL_INFO;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.MSO_REQUEST_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PRC_INSTANCE_NAME;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PRC_CONTROLLER_ACTOR;
import static org.onap.so.client.cds.PayloadConstants.*;

/**
 * This implementation of {@link JavaDelegate} is used to populate the execution object for
 * software upgrade
 */
@Component
public class NfSoftwareUpgradeCheckerDelegate implements JavaDelegate {

    private final Logger logger = LoggerFactory.getLogger(NfSoftwareUpgradeCheckerDelegate.class);
    private static final String SERVICE_INSTANCE_NAME = "serviceInstanceName";
    private static final String BPMN_REQUEST = "bpmnRequest";
    private static final String RESOURCE_CUSTOMIZATION_UUID_PARAM = "resource_customization_uuid";

    // ERROR CODE for variable not found in the delegation Context
    private static final int ERROR_CODE = 601;

    @Autowired
    private PnfManagement pnfManagement;

    @Autowired
    protected ExceptionBuilder exceptionUtil;

    @Autowired
    protected CatalogDbClient catalogDbClient;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        logger.debug("Running execute block for activity id:{}, name:{}", delegateExecution.getCurrentActivityId(),
                delegateExecution.getCurrentActivityName());

        RequestDetails bpmnRequest =
                mapper.readValue(JsonUtils.getJsonValue(String.valueOf(delegateExecution.getVariable(BPMN_REQUEST)),
                        "requestDetails"), RequestDetails.class);

        if (bpmnRequest.getModelInfo() == null) {
            logger.warn("Unable to find the parameter: {} in the execution context", SERVICE_MODEL_INFO);
            exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ERROR_CODE,
                    "Unable to find parameter " + SERVICE_MODEL_INFO);
        }

        String requestId = String.valueOf(delegateExecution.getVariable("mso-request-id"));
        String serviceInstanceName = bpmnRequest.getRequestInfo().getInstanceName();
        String pnfName = bpmnRequest.getRequestParameters().getPnfCorrelationId();
        String serviceModelUuid = bpmnRequest.getModelInfo().getModelUuid();
        String targetSoftwareVersion = bpmnRequest.getRequestParameters().getUserParamValue(PRC_TARGET_SOFTWARE_VERSION);

        List<PnfResourceCustomization> pnfCustomizations =
                catalogDbClient.getPnfResourceCustomizationByModelUuid(serviceModelUuid);

        if (pnfCustomizations == null || pnfCustomizations.isEmpty()) {
            logger.warn("Unable to find the PNF resource customizations of model service UUID: {}",
                    serviceModelUuid);
            exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ERROR_CODE,
                    "Unable to find the PNF resource customizations of model service UUID:  " + serviceModelUuid);
        }

        delegateExecution.setVariable(MSO_REQUEST_ID, requestId);
        delegateExecution.setVariable(SERVICE_MODEL_INFO, bpmnRequest.getModelInfo());
        delegateExecution.setVariable(SERVICE_INSTANCE_NAME, serviceInstanceName);
        delegateExecution.setVariable(PNF_CORRELATION_ID, pnfName);
        delegateExecution.setVariable(MODEL_UUID, serviceModelUuid);
        delegateExecution.setVariable(PRC_TARGET_SOFTWARE_VERSION, targetSoftwareVersion);

        logger.trace("Completed preProcessRequest PnfSoftwareUpgradeServiceRequest Request ");

        try {
            Optional<Pnf> pnfOptional = pnfManagement.getEntryFor(pnfName);
            if (pnfOptional.isPresent()) {
                Pnf pnf = pnfOptional.get();
                delegateExecution.setVariable(PNF_UUID, pnf.getPnfId());
            } else {
                exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ERROR_CODE,
                        "AAI entry for PNF: " + pnfName + " does not exist");
            }
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
            exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ERROR_CODE,
                    "Unable to fetch from AAI" + e.getMessage());
        }

        PnfResourceCustomization pnfResourceCustomization = pnfCustomizations.get(0);
        delegateExecution.setVariable(PRC_BLUEPRINT_NAME, pnfResourceCustomization.getBlueprintName());
        delegateExecution.setVariable(PRC_BLUEPRINT_VERSION, pnfResourceCustomization.getBlueprintVersion());
        delegateExecution.setVariable(PRC_CUSTOMIZATION_UUID, pnfResourceCustomization.getModelCustomizationUUID());
        delegateExecution.setVariable(RESOURCE_CUSTOMIZATION_UUID_PARAM, pnfResourceCustomization.getModelCustomizationUUID());
        delegateExecution.setVariable(PRC_INSTANCE_NAME, pnfResourceCustomization.getModelInstanceName());
        delegateExecution.setVariable(PRC_CONTROLLER_ACTOR, pnfResourceCustomization.getControllerActor());
    }
}
