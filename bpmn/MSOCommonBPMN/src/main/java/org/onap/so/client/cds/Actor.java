package org.onap.so.client.cds;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import java.util.Optional;

public interface Actor {

    /**
     * Build entire payload for CDS.
     * 
     * @param extractPojosForBB - ExtractPojosBB object
     * @param buildingBlockExecution - BuildingBlockExecution object
     * @return "payload":{ "config-<action>-<scope>":{ // information about resolution key, property configuration and
     *         template prefix based on the scope and action}
     */
    Optional<String> buildRequestPayload(ExtractPojosForBB extractPojosForBB,
            BuildingBlockExecution buildingBlockExecution);

    /**
     * Set the SO action.
     * 
     * @param action (assign/deploy/unassign/undeploy etc..)
     */
    void setAction(String action);
}
