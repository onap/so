/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Bell Canada
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.bpmn.infrastructure.aai.tasks.cds;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.orchestration.AAIVfModuleResources;
import org.onap.so.client.orchestration.AAIVnfResources;
import org.onap.so.db.catalog.beans.OrchestrationStatus;

public class UpdateOrchestrationStatus {

    private OrchestrationStatus orchestrationStatus;

    /**
     * Update orchestration status in AAI for vnf/vf-module etc..
     *
     * @param pojosForBB - ExtractPojosForBB object
     * @param execution - BuildingBlockExecution Object
     * @param action - action(configAssign/configDeploy/configUndeploy etc..)
     * @param t - AaiVnfResources or AaiVfModuleResources or any other object specific to updateOrchestrationStatus
     * @throws BBObjectNotFoundException if BBObject is missing.
     */
    public <T> void updateAAI(ExtractPojosForBB pojosForBB, BuildingBlockExecution execution, String action, T t)
            throws BBObjectNotFoundException {

        if (t instanceof AAIVnfResources) {
            AAIVnfResources vnfResources = (AAIVnfResources) t;
            GenericVnf vnf = pojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            OrchestrationStatus status = getOrchestrationStauts(action);

            vnfResources.updateOrchestrationStatusVnf(vnf, status);
        } else if (t instanceof AAIVfModuleResources) {
            AAIVfModuleResources vfModuleResources = (AAIVfModuleResources) t;
            VfModule vfModule = pojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            GenericVnf vnf = pojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            OrchestrationStatus status = getOrchestrationStauts(action);

            vfModuleResources.updateOrchestrationStatusVfModule(vfModule, vnf, status);
        }
    }

    private OrchestrationStatus getOrchestrationStauts(String action) {
        /**
         * At this state, OrcherstationStatus enum associated with configAssign and configDeploy. I am not sure which is
         * the correct approach. 1. Are we going to map each specific action to OrchestrationStauts ? 2. We will have
         * only one generic status for all actions ?
         */

        switch (action) {
            case "configAssign":
                return OrchestrationStatus.ASSIGNED;
            case "configDeploy":
                return OrchestrationStatus.CONFIGURED;
            default:
                throw new IllegalArgumentException("Invalid action to set Orchestration status: " + action);
        }
    }
}
