package org.onap.so.bpmn.infrastructure.pnf.bbtasks;

import joptsimple.internal.Strings;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.io.IOException;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.AAI_CONTAINS_INFO_ABOUT_PNF;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_CORRELATION_ID;

@Component
public class CheckAaiForPnfCorrelationId extends PnfBaseDelegate {
    private static final Logger logger = LoggerFactory.getLogger(CheckAaiForPnfCorrelationId.class);

    @Override
    public void execute(DelegateExecution execution) {
        try {
            String pnfCorrelationId = extractPnf(execution).getPnfName();
            checkIfPnfCorrelationIdPresent(execution, pnfCorrelationId);
            checkIfPnfExistsInAai(execution, pnfCorrelationId);
        } catch (BBObjectNotFoundException e) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, e);
        }

    }

    private void checkIfPnfCorrelationIdPresent(DelegateExecution execution, String pnfCorrelationId) {
        if (Strings.isNullOrEmpty(pnfCorrelationId)) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, PNF_CORRELATION_ID + " is not set");
        }
    }

    private void checkIfPnfExistsInAai(DelegateExecution execution, String pnfCorrelationId) {
        try {
            boolean isEntry = pnfManagement.getEntryFor(pnfCorrelationId).isPresent();
            logger.debug("AAI entry is found for pnf correlation id {}: {}", PNF_CORRELATION_ID, isEntry);
            execution.setVariableLocal(AAI_CONTAINS_INFO_ABOUT_PNF, isEntry);
        } catch (IOException e) {
            logger.error("Exception in check AAI for pnf_correlation_id execution", e);
            exceptionUtil.buildAndThrowWorkflowException(execution, 9999, e);
        }
    }
}
