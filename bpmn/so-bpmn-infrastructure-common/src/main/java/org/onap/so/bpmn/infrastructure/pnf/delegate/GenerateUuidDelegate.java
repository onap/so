package org.onap.so.bpmn.infrastructure.pnf.delegate;

import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;

import java.util.UUID;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class GenerateUuidDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution){
        String uuid = UUID.randomUUID().toString();
        delegateExecution.setVariable(PNF_UUID, uuid);
    }
}
