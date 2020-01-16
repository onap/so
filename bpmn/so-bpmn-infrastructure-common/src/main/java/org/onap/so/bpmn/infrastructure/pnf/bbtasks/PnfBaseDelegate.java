package org.onap.so.bpmn.infrastructure.pnf.bbtasks;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.pnf.management.PnfManagement;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class PnfBaseDelegate implements JavaDelegate {
    protected PnfManagement pnfManagement;
    @Autowired
    protected ExceptionBuilder exceptionUtil;
    @Autowired
    protected ExtractPojosForBB extractPojosForBB;

    @Autowired
    public void setPnfManagement(PnfManagement pnfManagement) {
        this.pnfManagement = pnfManagement;
    }

    protected Pnf extractPnf(DelegateExecution execution) throws BBObjectNotFoundException {
        return extractPojosForBB.extractByKey((BuildingBlockExecution) execution.getVariable("gBuildingBlockExecution"),
                ResourceKey.PNF);
    }
}
