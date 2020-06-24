package org.onap.so.bpmn.infrastructure.decisionpoint.impl;



import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 * Fetches health check workflow based on the controller_scope. Invoke the corresponding health check workflow after
 * validation. Returns the health check result.
 */


public class PrepareServiceUpgradeDE implements JavaDelegate {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    static final String PNF_HC_WORKFLOW = "GenericPnfHealthCheck";

    @Autowired
    protected ExceptionBuilder exceptionBuilder;

    @Autowired
    protected RuntimeService runtimeService;



    @Override
    public void execute(DelegateExecution execution) throws Exception {

        String controllerScope = (String) execution.getVariable("CONTROLLER_SCOPE");
        String wflToInvoke = fetchHealthCheckWflwUsingScope(controllerScope);

        if (wflToInvoke.equals(PNF_HC_WORKFLOW)) {
            boolean isValid = validateParamsForPnf(execution);
            if (isValid == true) {
                ProcessInstanceWithVariables instance =
                        runtimeService.createProcessInstanceByKey(wflToInvoke).executeWithVariablesInReturn();
                execution.setVariable("HEALTHCHECK_RESULT", instance.getVariables().get("RESULT"));

            } else {
                // TODO throw exception

            }

        }

        // TODO validate and invoke vnf health check workflow here



    }

    /**
     * This method returns the health check workflow names based on the controller scope. At present the workflow names
     * are hardcoded.
     * 
     * @param scope controller scope
     * @return workflow name
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
     * This method validates the parameters to be passed for health check
     * 
     * @param execution Execution obj
     * @return boolean result of validation
     */
    private boolean validateParamsForPnf(DelegateExecution execution) {
        boolean isValid = true;
        List<String> pnfHealthCheckParams =
                Arrays.asList("pnfName", "pnf-id", "mode", "pnf-ipv4-address", "service-instance-id");
        List<String> invalidVariables = new ArrayList<>();
        for (String param : pnfHealthCheckParams) {
            if (!execution.hasVariable(param) && execution.getVariable(param).toString().isEmpty()) {
                invalidVariables.add(param);
                isValid = false;
            }
        }
        if (invalidVariables.size() > 0) {
            logger.debug("Validation error for the pnf health check attributes: " + (invalidVariables));
        }
        return isValid;
    }
}
