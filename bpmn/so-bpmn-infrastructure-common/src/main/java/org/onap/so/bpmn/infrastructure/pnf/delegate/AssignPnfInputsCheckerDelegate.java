package org.onap.so.bpmn.infrastructure.pnf.delegate;

import com.google.common.base.Strings;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.bpmn.common.scripts.ExceptionUtil;
import org.springframework.stereotype.Component;

import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;

@Component
public class AssignPnfInputsCheckerDelegate implements JavaDelegate {

    public static final String UUID_REGEX =
            "(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[1-5]{1}[0-9a-f]{3}-[89ab]{1}[0-9a-f]{3}-[0-9a-f]{12}$";

    @Override
    public void execute(DelegateExecution execution) {
        validatePnfCorrelationId(execution);
        validatePnfUuid(execution);
    }

    private void validatePnfCorrelationId(DelegateExecution execution) {
        String pnfCorrelationId = (String) execution.getVariable(PNF_CORRELATION_ID);
        if (Strings.isNullOrEmpty(pnfCorrelationId)) {
            new ExceptionUtil().buildAndThrowWorkflowException(execution, 9999,
                    "pnfCorrelationId variable not defined");
        }
    }

    private void validatePnfUuid(DelegateExecution execution) {
        String pnfUuid = (String) execution.getVariable(PNF_UUID);
        if (Strings.isNullOrEmpty(pnfUuid)) {
            new ExceptionUtil().buildAndThrowWorkflowException(execution, 9999, "pnfUuid variable not defined");
        }
        if (!pnfUuid.matches(UUID_REGEX)) {
            new ExceptionUtil().buildAndThrowWorkflowException(execution, 9999, "pnfUuid is not a valid UUID");
        }
    }
}
