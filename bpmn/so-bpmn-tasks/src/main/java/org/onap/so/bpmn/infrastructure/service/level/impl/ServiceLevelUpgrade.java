package org.onap.so.bpmn.infrastructure.service.level.impl;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.bpmn.infrastructure.service.level.AbstractServiceLevelPreparable;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
public class ServiceLevelUpgrade extends AbstractServiceLevelPreparable implements JavaDelegate {

    private static final List<String> PNF_SWU_PARAMS = Arrays.asList(ServiceLevelConstants.SERVICE_INSTANCE_ID,
            ServiceLevelConstants.RESOURCE_TYPE, ServiceLevelConstants.BPMN_REQUEST, ServiceLevelConstants.PNF_NAME);

    @Override
    protected String fetchWorkflowUsingScope(DelegateExecution execution, String scope) {
        String wflName = null;
        switch (scope.toLowerCase()) {
            case ServiceLevelConstants.PNF:
                wflName = ServiceLevelConstants.PNF_SOFTWARE_UPGRADE_WORKFLOW;
                break;
            case ServiceLevelConstants.VNF:
                wflName = ServiceLevelConstants.GENERIC_VNF_HEALTH_CHECK_WORKFLOW;
                break;
            default:
                exceptionBuilder.buildAndThrowWorkflowException(execution, ServiceLevelConstants.ERROR_CODE,
                        "No valid health check work flow retrieved for the scope: " + scope);
        }
        return wflName;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        if (execution.hasVariable(ServiceLevelConstants.RESOURCE_TYPE)
                && execution.getVariable(ServiceLevelConstants.RESOURCE_TYPE) != null) {
            final String controllerScope = (String) execution.getVariable(ServiceLevelConstants.RESOURCE_TYPE);
            LOG.debug("Scope retrieved from delegate execution: " + controllerScope);
            final String wflName = fetchWorkflowUsingScope(execution, controllerScope);
            LOG.debug("Health check workflow fetched for the scope: {} is: {}", controllerScope, wflName);

            if ("pnf".equalsIgnoreCase(controllerScope)) {
                validateParamsWithScope(execution, controllerScope, PNF_SWU_PARAMS);
                LOG.info("Parameters validated successfully for {}", wflName);
                execution.setVariable(ServiceLevelConstants.SOFTWARE_WORKFLOW_TO_INVOKE, wflName);
                execution.setVariable(ServiceLevelConstants.CONTROLLER_STATUS, "");
            }

        } else {
            exceptionBuilder.buildAndThrowWorkflowException(execution, ServiceLevelConstants.ERROR_CODE,
                    "Controller scope not found to invoke resource level health check");
        }
    }
}
