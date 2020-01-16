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

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        BuildingBlockExecution gBuildingBlockExecution =
                (BuildingBlockExecution) delegateExecution.getVariable("gBuildingBlockExecution");
        execute(gBuildingBlockExecution);
    }

    protected abstract void execute(BuildingBlockExecution gBuildingBlockExecution) throws Exception;

    @Autowired
    public void setPnfManagement(PnfManagement pnfManagement) {
        this.pnfManagement = pnfManagement;
    }

    protected Pnf extractPnf(BuildingBlockExecution execution) throws BBObjectNotFoundException {
        return extractPojosForBB.extractByKey(execution, ResourceKey.PNF);
    }
}
