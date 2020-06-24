/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 *
 * ================================================================================
 * Copyright (c) 2020 Ericsson. All rights reserved
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

package org.onap.so.bpmn.infrastructure.service.level.impl;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.bpmn.infrastructure.service.level.AbstractServiceUpgradeDE;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;


/**
 * Fetches health check workflow based on the controller_scope. Invoke the corresponding health check workflow after
 * validation.
 */
@Component("PrepareServiceUpgradeDE")
public class PrepareServiceUpgradeDE extends AbstractServiceUpgradeDE implements JavaDelegate {

    private static final int ERROR_CODE = 601;
    private static final String WORKFLOW_TO_INVOKE = "healthCheckWorkflow";

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String controllerScope = (String) execution.getVariable("CONTROLLER_SCOPE");
        String wflName = fetchWorkflowUsingScope(controllerScope);
        log.debug("Health check workflow fetched for the scope: {}", wflName);
        if (wflName.equals(PNF_HC_WORKFLOW)) {
            boolean isValid = validateParamsWithScope(execution, controllerScope);
            if (isValid) {
                log.info("Parameters validated successfully for {}", wflName);
                execution.setVariable(WORKFLOW_TO_INVOKE, PNF_HC_WORKFLOW);
            } else {
                exceptionBuilder.buildAndThrowWorkflowException(execution, ERROR_CODE,
                        "Validation failed for pnf health check");
            }
        } else if (wflName.equals(VNF_HC_WORKFLOW)) {

            // TODO Validate and invoke VNF HealthCheck workflow here

        } else {
            // For controller scope other than pnf and vnf
            exceptionBuilder.buildAndThrowWorkflowException(execution, ERROR_CODE,
                    "No valid health check workflow found for the scope: " + controllerScope);
        }
    }

    @Override
    protected String fetchWorkflowUsingScope(final String scope) {
        switch (scope.toLowerCase()) {
            case "pnf":
                return PNF_HC_WORKFLOW;
            case "vnf":
                return VNF_HC_WORKFLOW;
            default:
                // TODO Assign a default wfl
                return "Dummy Workflow";
        }
    }

    @Override
    public boolean validateParamsWithScope(DelegateExecution execution, String scope) {
        boolean isValid = true;
        List<String> invalidVariables = new ArrayList<>();
        if (scope.toLowerCase().equals("pnf")) {
            for (String param : PNF_HC_PARAMS) {
                if (!execution.hasVariable(param) || execution.getVariable(param) == null
                        || execution.getVariable(param).toString().isEmpty()) {
                    invalidVariables.add(param);
                }
            }
            if (invalidVariables.size() > 0) {
                isValid = false;
                log.error("Validation error for the pnf health check attributes: {}", invalidVariables);
            }
        }
        // TODO Validate VNF health check params for scope:vnf if needed

        return isValid;
    }

}
