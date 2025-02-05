/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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

package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

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
import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.beans.factory.annotation.Primary;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.*;

/**
 * This implementation of {@link JavaDelegate} is used to populate the execution object for pnf actions
 */
@Component
public class GenericPnfDispatcher implements JavaDelegate {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String SERVICE_INSTANCE_NAME = "serviceInstanceName";
    private static final String BPMN_REQUEST = "bpmnRequest";
    private static final String RESOURCE_CUSTOMIZATION_UUID_PARAM = "resource_customization_uuid";
    private static final String PNF_NAME = "pnfName";

    // ERROR CODE for variable not found in the delegation Context
    private static final int ERROR_CODE = 601;

    // @Primary
    @Autowired
    @Qualifier("PnfManagement")
    private PnfManagement pnfManagement;

    @Autowired
    private ExceptionBuilder exceptionUtil;

    @Autowired
    private CatalogDbClient catalogDbClient;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        logger.debug("Running execute block for activity id: {}, name: {}", delegateExecution.getCurrentActivityId(),
                delegateExecution.getCurrentActivityName());

        RequestDetails bpmnRequestDetails = requestVerification(delegateExecution);

        final String serviceInstanceName = bpmnRequestDetails.getRequestInfo().getInstanceName();
        final String pnfName;
        if (delegateExecution.getVariable(PNF_NAME) == null
                || String.valueOf(delegateExecution.getVariable(PNF_NAME)).trim().isEmpty()) {
            pnfName = bpmnRequestDetails.getRequestParameters().getUserParamValue(PNF_NAME);
        } else {
            pnfName = String.valueOf(delegateExecution.getVariable(PNF_NAME));
        }
        final String serviceModelUuid = bpmnRequestDetails.getModelInfo().getModelUuid();
        final List<Map<String, Object>> userParams = bpmnRequestDetails.getRequestParameters().getUserParams();
        final Pnf pnf = getPnfByPnfName(delegateExecution, pnfName);
        final List<PnfResourceCustomization> pnfCustomizations =
                getPnfResourceCustomizations(delegateExecution, serviceModelUuid);
        final PnfResourceCustomization pnfResourceCustomization = pnfCustomizations.get(0);
        final String payload = bpmnRequestDetails.getRequestParameters().getPayload();

        populateExecution(delegateExecution, bpmnRequestDetails, pnfResourceCustomization, pnf, serviceInstanceName,
                pnfName, serviceModelUuid, userParams, payload);

        logger.trace("Completed dispatcher request for PNF.");
    }

    private RequestDetails requestVerification(DelegateExecution delegateExecution) throws IOException {
        RequestDetails bpmnRequestDetails = mapper.readValue(
                JsonUtils.getJsonValue(String.valueOf(delegateExecution.getVariable(BPMN_REQUEST)), "requestDetails"),
                RequestDetails.class);

        throwIfNull(delegateExecution, bpmnRequestDetails.getModelInfo(), SERVICE_MODEL_INFO);
        throwIfNull(delegateExecution, bpmnRequestDetails.getRequestInfo(), "RequestInfo");
        throwIfNull(delegateExecution, bpmnRequestDetails.getRequestParameters(), "RequestParameters");
        throwIfNull(delegateExecution, bpmnRequestDetails.getRequestParameters().getUserParams(), "UserParams");

        return bpmnRequestDetails;
    }

    private void populateExecution(DelegateExecution delegateExecution, RequestDetails bpmnRequestDetails,
            PnfResourceCustomization pnfResourceCustomization, Pnf pnf, String serviceInstanceName, String pnfName,
            String serviceModelUuid, List<Map<String, Object>> userParams, String payload) {

        delegateExecution.setVariable(SERVICE_MODEL_INFO, bpmnRequestDetails.getModelInfo());
        delegateExecution.setVariable(SERVICE_INSTANCE_NAME, serviceInstanceName);
        delegateExecution.setVariable(PNF_CORRELATION_ID, pnfName);
        delegateExecution.setVariable(MODEL_UUID, serviceModelUuid);
        delegateExecution.setVariable(PNF_UUID, pnf.getPnfId());
        delegateExecution.setVariable(PRC_BLUEPRINT_NAME, pnfResourceCustomization.getBlueprintName());
        delegateExecution.setVariable(PRC_BLUEPRINT_VERSION, pnfResourceCustomization.getBlueprintVersion());
        delegateExecution.setVariable(PRC_CUSTOMIZATION_UUID, pnfResourceCustomization.getModelCustomizationUUID());
        delegateExecution.setVariable(RESOURCE_CUSTOMIZATION_UUID_PARAM,
                pnfResourceCustomization.getModelCustomizationUUID());
        delegateExecution.setVariable(PRC_INSTANCE_NAME, pnfResourceCustomization.getModelInstanceName());
        delegateExecution.setVariable(PRC_CONTROLLER_ACTOR, pnfResourceCustomization.getControllerActor());

        for (Map<String, Object> param : userParams) {
            if (param.containsKey("name") && param.containsKey("value")) {
                delegateExecution.setVariable(param.get("name").toString(), param.get("value").toString());
            }
        }

        delegateExecution.setVariable(REQUEST_PAYLOAD, payload);
    }

    private Pnf getPnfByPnfName(DelegateExecution delegateExecution, String pnfName) {
        Optional<Pnf> pnfOptional = Optional.empty();
        try {
            pnfOptional = pnfManagement.getEntryFor(pnfName);
        } catch (IOException e) {
            throwExceptionWithWarn(delegateExecution, "Unable to fetch from AAI" + e.getMessage());
        }
        if (!pnfOptional.isPresent()) {
            throwExceptionWithWarn(delegateExecution, "AAI entry for PNF: " + pnfName + " does not exist");
        }
        return pnfOptional.get();
    }

    private List<PnfResourceCustomization> getPnfResourceCustomizations(DelegateExecution delegateExecution,
            String serviceModelUuid) {
        List<PnfResourceCustomization> pnfCustomizations =
                catalogDbClient.getPnfResourceCustomizationByModelUuid(serviceModelUuid);

        if (pnfCustomizations == null || pnfCustomizations.isEmpty()) {
            throwExceptionWithWarn(delegateExecution,
                    "Unable to find the PNF resource customizations of model service UUID:  " + serviceModelUuid);
        }
        return pnfCustomizations;
    }

    private void throwIfNull(DelegateExecution delegateExecution, Object obj, String param) {
        if (obj == null) {
            throwExceptionWithWarn(delegateExecution,
                    "Unable to find the parameter: " + param + " in the execution context");
        }
    }

    private void throwExceptionWithWarn(DelegateExecution delegateExecution, String exceptionMsg) {
        logger.warn(exceptionMsg);
        exceptionUtil.buildAndThrowWorkflowException(delegateExecution, ERROR_CODE, exceptionMsg);
    }
}
