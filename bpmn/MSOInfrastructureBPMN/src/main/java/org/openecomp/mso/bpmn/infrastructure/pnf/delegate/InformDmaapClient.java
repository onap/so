package org.openecomp.mso.bpmn.infrastructure.pnf.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.openecomp.mso.bpmn.infrastructure.pnf.implementation.DmaapClient;
import org.springframework.beans.factory.annotation.Autowired;

public class InformDmaapClient implements JavaDelegate {

    private DmaapClient dmaapClient;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String correlationId = (String) execution.getVariable(ExecutionVariableNames.CORRELATION_ID);
        dmaapClient.registerForUpdate(correlationId, () -> execution.getProcessEngineServices().getRuntimeService()
                .createMessageCorrelation("WorkflowMessage")
                .processInstanceBusinessKey(execution.getProcessBusinessKey())
                .correlateWithResult());
    }

    @Autowired
    public void setDmaapClient(DmaapClient dmaapClient) {
        this.dmaapClient = dmaapClient;
    }
}
