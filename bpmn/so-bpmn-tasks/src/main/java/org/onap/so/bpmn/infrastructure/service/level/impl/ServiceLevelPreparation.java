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
import org.onap.so.bpmn.infrastructure.service.level.AbstractServiceLevelPreparable;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;


/**
 * Fetches health check workflow based on the controller_scope. Invoke the corresponding health check workflow after
 * validation.
 */
@Component("ServiceLevelPreparation")
public class ServiceLevelPreparation extends AbstractServiceLevelPreparable implements JavaDelegate {

    // Health check parameters to be validated for pnf resource
    private static final List<String> PNF_HC_PARAMS =
            Arrays.asList("SERVICE_MODEL_INFO", "SERVICE_INSTANCE_NAME", "MODEL_UUID");

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        if (execution.hasVariable(RESOURCE_TYPE) && execution.getVariable(RESOURCE_TYPE) != null) {
            final String controllerScope = (String) execution.getVariable(RESOURCE_TYPE);
            LOG.debug("Scope retrieved from delegate execution: " + controllerScope);
            final String wflName = fetchWorkflowUsingScope(execution, controllerScope);
            LOG.debug("Health check workflow fetched for the scope: {} is: {}", controllerScope, wflName);

            if ("pnf".equalsIgnoreCase(controllerScope)) {
                // validateParamsWithScope(execution, controllerScope, PNF_HC_PARAMS);
                LOG.info("Parameters validated successfully for {}", wflName);
                execution.setVariable(WORKFLOW_TO_INVOKE, wflName);
                execution.setVariable(CONTROLLER_STATUS, "");
            }
        } else {
            exceptionBuilder.buildAndThrowWorkflowException(execution, ERROR_CODE,
                    "Controller scope not found to invoke resource level health check");
        }
    }

    @Override
    public String fetchWorkflowUsingScope(DelegateExecution execution, final String scope) {
        String wflName = null;
        switch (scope.toLowerCase()) {
            case "pnf":
                wflName = GENERIC_PNF_HEALTH_CHECK_WORKFLOW;
                break;
            case "vnf":
                wflName = GENERIC_VNF_HEALTH_CHECK_WORKFLOW;
                break;
            default:
                exceptionBuilder.buildAndThrowWorkflowException(execution, ERROR_CODE,
                        "No valid health check work flow retrieved for the scope: " + scope);
        }
        return wflName;
    }

}
