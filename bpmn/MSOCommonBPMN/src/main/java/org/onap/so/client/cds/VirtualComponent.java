package org.onap.so.client.cds;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import java.util.Optional;

public interface VirtualComponent {

    /**
     * Build entire payload for CDS.
     * 
     * @param extractPojosForBB - ExtractPojosBB object
     * @param buildingBlockExecution - BuildingBlockExecution object
     * @param action - action could be assign/deploy/undeploy etc.
     * @return "payload":{ "config-<action>-<scope>":{ // information about resolution key, property configuration and
     *         template prefix based on the scope and action}
     */
    Optional<String> buildRequestPayload(ExtractPojosForBB extractPojosForBB,
            BuildingBlockExecution buildingBlockExecution, String action);


    /**
     * Get the blueprint name for CDS payload
     *
     * @return blueprint name
     */
    String getBlueprintName();

    /**
     * Get the blueprint version for CDS payload
     * 
     * @return blueprint version
     */
    String getBlueprintVersion();
}
