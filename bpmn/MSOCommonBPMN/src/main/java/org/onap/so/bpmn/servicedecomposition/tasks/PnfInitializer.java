package org.onap.so.bpmn.servicedecomposition.tasks;

import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.so.serviceinstancebeans.Pnfs;

final class PnfInitializer {
    private Pnf pnf;

    PnfInitializer(Pnf pnf) {
        this.pnf = pnf;
    }

    Pnf populatePnf(Pnfs pnfs, String pnfId) {
        pnf.setPnfId(pnfId);
        pnf.setPnfName(pnfs.getInstanceName());
        pnf.setModelCustomizationId(pnfs.getModelInfo().getModelCustomizationId());
        pnf.setModelInvariantId(pnfs.getModelInfo().getModelInvariantId());
        pnf.setModelVersionId(pnfs.getModelInfo().getModelVersionId());
        pnf.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
        return pnf;
    }
}
