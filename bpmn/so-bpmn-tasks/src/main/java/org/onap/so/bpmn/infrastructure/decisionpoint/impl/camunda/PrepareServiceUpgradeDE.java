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

package org.onap.so.bpmn.infrastructure.decisionpoint.impl.camunda;


import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.onap.so.bpmn.core.WorkflowException;
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
 * validation. Returns the health check result.
 */

@Component("PrepareServiceUpgradeDE")
public class PrepareServiceUpgradeDE implements JavaDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareServiceUpgradeDE.class);
    private static final String PNF_HC_WORKFLOW = "GenericPnfHealthCheck";
    private static final String PNF_HC_RESULT = "HEALTH_CHECK_RESULT";
    private static final List<String> PNF_HC_PARAMS =
            Arrays.asList("PNF_NAME", "PNF_ID", "PNF_IPV4_ADDR", "SERVICE_INST_ID");

    private static final String WORKFLOW_EXCEPTION = "WorkflowException";
    private WorkflowException workflowException;

    @Autowired
    private ExceptionBuilder exceptionBuilder;

    @Autowired
    private RuntimeService runtimeService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String controllerScope = (String) execution.getVariable("CONTROLLER_SCOPE");
        String wflName = fetchHealthCheckWflwUsingScope(controllerScope);
        LOG.debug("Health check workflow to be invoked: {}", wflName);
        try {
            if (wflName.equals(PNF_HC_WORKFLOW)) {
                boolean isValid = validateParamsForPnf(execution);
                if (isValid == true) {
                    LOG.info("Parameters validated successfully for {}", wflName);
                    invokePnfHealthCheckWorkflow(execution, wflName);
                } else {
                    throw new Exception("Validation of parameters failed for " + wflName);
                }
            }

            // TODO validate and invoke vnf health check workflow here

        } catch (Exception e) {
            exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, e.getMessage());
        }

    }

    /**
     * This method invokes the corresponding worklfow for pnf health check through runtimeService of camunda bpmn and
     * takes action based on the results.
     */
    protected void invokePnfHealthCheckWorkflow(DelegateExecution execution, String wflName) throws Exception {
        ProcessInstanceWithVariables healthCheckInstance = runtimeService.createProcessInstanceByKey(wflName)
                .setVariables(execution.getVariables()).executeWithVariablesInReturn();
        workflowException = (WorkflowException) healthCheckInstance.getVariables().get(WORKFLOW_EXCEPTION);
        if (workflowException != null) {
            throw new Exception(workflowException.getErrorMessage());
        } else {
            String result = healthCheckInstance.getVariables().get(PNF_HC_RESULT).toString();
            LOG.info("Pnf health check workflow completed with result: {}", result);
            execution.setVariable(PNF_HC_RESULT, result);
        }
    }

    /**
     * This method returns the health check workflow names based on the controller scope. At present the workflow names
     * are hardcoded.
     */
    private String fetchHealthCheckWflwUsingScope(String scope) {
        String wflName = null;
        switch (scope) {
            case "pnf":
                wflName = PNF_HC_WORKFLOW;
                break;
            case "vnf":
                // TODO define and return vnf wfl
                break;
            default:
                // TODO Assign a default wfl
        }
        return wflName;
    }

    /**
     * This method validates the execution parameters to be passed for health check workflow.
     */
    protected boolean validateParamsForPnf(DelegateExecution execution) {
        boolean isValid = true;
        List<String> invalidVariables = new ArrayList<>();
        for (String param : PNF_HC_PARAMS) {
            if (!execution.hasVariable(param) || execution.getVariable(param).toString().isEmpty()) {
                invalidVariables.add(param);
            }
        }
        if (invalidVariables.size() > 0) {
            isValid = false;
            LOG.error("Validation error for the pnf health check attributes: {}", invalidVariables);
        }
        return isValid;
    }



}
