package org.onap.so.bpmn.infrastructure.service.level.impl;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class ServiceLevelUpgrade implements JavaDelegate {

    private static final String RESOURCE_TYPE = "resourceType";

    @Override
    public void execute(final DelegateExecution execution) {

        final String resourceType = String.valueOf(execution.getVariable(RESOURCE_TYPE));

        if ("pnf".equalsIgnoreCase(resourceType)) {
            execution.setVariable("softwareUpgradeWorkflow", "PNFSoftwareUpgrade");
            execution.setVariable("healthCheckWorkflow", "PNFSoftwareUpgrade");
            execution.setVariable("ControllerStatus", "");
        }else{
            //TODO : write handling for vnf and more...
        }
    }

    private Map extractServiceInfoFromAai() {
        // TODO 1. extract pnf name from AAI
        // TODO 2. extract software version
        // TODO 3. extract ActorType etc...
        return null;
    }
}
