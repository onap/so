package org.onap.so.bpmn.infrastructure.aai.tasks.cds;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.orchestration.AAIVnfResources;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateOrchestrationStatusForVnf implements UpdateOrchestrationStatusForCds {

    @Autowired
    private AAIVnfResources aaiVnfResources;

    private OrchestrationStatus status;

    public UpdateOrchestrationStatusForVnf(OrchestrationStatus status) {
        this.status = status;
    }

    @Override
    public void updateAAI(ExtractPojosForBB pojosForBB, BuildingBlockExecution execution)
            throws BBObjectNotFoundException {
        GenericVnf vnf = pojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
        aaiVnfResources.updateOrchestrationStatusVnf(vnf, status);
    }
}
