package org.openecomp.mso.bpmn.infrastructure.scripts;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil;
import org.openecomp.mso.client.dmaap.DmaapConsumer;
import org.openecomp.mso.client.sdno.dmaap.PnfReadyEventConsumer;

public class PnfReadyEventHandler {

    private ExceptionUtil exceptionUtil;

    private static final String TOPIC_NAME = "VES event";

    public PnfReadyEventHandler() {
        exceptionUtil = new ExceptionUtil();
    }

    public void getPnfReadyEventFromDmaap (DelegateExecution execution) throws Exception {
        Object correlationIdVar = execution.getVariable("correlationId");
        if (!(correlationIdVar instanceof String)) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 500, "correlationId variable is not String type");
        }
        String correlationId = (String) correlationIdVar;
        DmaapConsumer dmaapConsumer = new PnfReadyEventConsumer(correlationId);
        dmaapConsumer.consume();
        // TODO inform camunda process that event has been received
    }
}
