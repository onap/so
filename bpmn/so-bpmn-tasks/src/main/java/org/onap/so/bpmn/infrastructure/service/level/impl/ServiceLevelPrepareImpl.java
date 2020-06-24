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

package org.onap.so.bpmn.infrastructure.service.level.impl;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.bpmn.infrastructure.service.level.ServiceLevelPrepare;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Fetches health check workflow based on the controller_scope. Invoke the corresponding health check workflow after
 * validation.
 */
@Component("ServiceLevelPrepareImpl")
public class ServiceLevelPrepareImpl implements JavaDelegate, ServiceLevelPrepare {

    private static final int ERROR_CODE = 601;

    // Health check parameters to be validated for pnf resource
    public static final List<String> PNF_HC_PARAMS = Arrays.asList("SERVICE_MODEL_INFO", "SERVICE_INSTANCE_NAME",
            "PNF_CORRELATION_ID", "MODEL_UUID", "PNF_UUID", "PRC_BLUEPRINT_NAME", "PRC_BLUEPRINT_VERSION",
            "PRC_CUSTOMIZATION_UUID", "RESOURCE_CUSTOMIZATION_UUID_PARAM", "PRC_INSTANCE_NAME", "PRC_CONTROLLER_ACTOR",
            "REQUEST_PAYLOAD");

    private static final Logger LOG = LoggerFactory.getLogger(ServiceLevelPrepareImpl.class);

    @Autowired
    private ExceptionBuilder exceptionBuilder;


    @Override
    public void execute(DelegateExecution execution) throws Exception {
        if (execution.hasVariable(EXECUTION_SCOPE) && execution.getVariable(EXECUTION_SCOPE) != null) {
            final String controllerScope = (String) execution.getVariable(EXECUTION_SCOPE);
            LOG.debug("Scope retrieved from delegate execution: " + controllerScope);
            final String wflName = fetchWorkflowUsingScope(controllerScope);
            if (wflName != null && !wflName.isEmpty()) {
                LOG.debug("Health check workflow fetched for the scope: {}", wflName);
                validateParamsWithScope(execution, controllerScope);
                LOG.info("Parameters validated successfully for {}", wflName);
                execution.setVariable(WORKFLOW_TO_INVOKE, wflName);
            } else {
                exceptionBuilder.buildAndThrowWorkflowException(execution, ERROR_CODE,
                        "No valid health check workflow retrieved for the scope: " + controllerScope);
            }
        } else {
            exceptionBuilder.buildAndThrowWorkflowException(execution, ERROR_CODE,
                    "Controller scope not found to invoke resource level health check");
        }
    }

    @Override
    public String fetchWorkflowUsingScope(final String scope) {
        switch (scope.toLowerCase()) {
            case "pnf":
                return PNF_HC_WORKFLOW;
            case "vnf":
                return VNF_HC_WORKFLOW;
            default:
                return null;
        }
    }

    @Override
    public void validateParamsWithScope(DelegateExecution execution, final String scope) throws Exception {
        List<String> invalidVariables = new ArrayList<>();
        if ("pnf".equalsIgnoreCase(scope)) {
            for (String param : PNF_HC_PARAMS) {
                if (!execution.hasVariable(param) || execution.getVariable(param) == null
                        || String.valueOf(execution.getVariable(param)).isEmpty()) {
                    invalidVariables.add(param);
                }
            }
            if (invalidVariables.size() > 0) {
                LOG.error("Validation error for the pnf health check attributes: {}", invalidVariables);
                exceptionBuilder.buildAndThrowWorkflowException(execution, ERROR_CODE,
                        "Validation of health check workflow parameters failed for the scope: " + scope);
            }
        }

        // TODO Validate VNF health check params for scope:vnf if needed


    }

}
