package org.onap.so.bpmn.infrastructure.service.level.impl;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.bpmn.infrastructure.service.level.AbstractServiceLevelPreparable;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
public class ServiceLevelUpgrade extends AbstractServiceLevelPreparable implements JavaDelegate {
    private static final String BPMN_REQUEST = "bpmnRequest";
    private static final String RESOURCE_TYPE = "resourceType";
    private static final String SERVICE_INSTANCE_ID = "serviceInstanceId";
    private static final String PNF_NAME = "pnfName";

    private static final List<String> PNF_SWU_PARAMS =
            Arrays.asList(SERVICE_INSTANCE_ID, RESOURCE_TYPE, BPMN_REQUEST, PNF_NAME);

    @Override
    protected String fetchWorkflowUsingScope(DelegateExecution execution, String scope) {
        String wflName = null;
        switch (scope.toLowerCase()) {
            case "pnf":
                wflName = PNF_SOFTWARE_UPGRADE_WORKFLOW;
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

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        if (execution.hasVariable(RESOURCE_TYPE) && execution.getVariable(RESOURCE_TYPE) != null) {
            final String controllerScope = (String) execution.getVariable(RESOURCE_TYPE);
            LOG.debug("Scope retrieved from delegate execution: " + controllerScope);
            final String wflName = fetchWorkflowUsingScope(execution, controllerScope);
            LOG.debug("Health check workflow fetched for the scope: {} is: {}", controllerScope, wflName);

            if ("pnf".equalsIgnoreCase(controllerScope)) {
                validateParamsWithScope(execution, controllerScope, PNF_SWU_PARAMS);
                LOG.info("Parameters validated successfully for {}", wflName);
                execution.setVariable(SOFTWARE_WORKFLOW_TO_INVOKE, wflName);
                execution.setVariable(CONTROLLER_STATUS, "");
            }

        } else {
            exceptionBuilder.buildAndThrowWorkflowException(execution, ERROR_CODE,
                    "Controller scope not found to invoke resource level health check");
        }
    }
}
