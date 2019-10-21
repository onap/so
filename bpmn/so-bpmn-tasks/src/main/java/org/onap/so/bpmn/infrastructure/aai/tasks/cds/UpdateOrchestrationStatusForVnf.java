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
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.orchestration.AAIVnfResources;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

public class UpdateOrchestrationStatusForVnf implements UpdateOrchestrationStatusForCds {

    private AAIVnfResources aaiVnfResources;

    private OrchestrationStatus status;

    public UpdateOrchestrationStatusForVnf(OrchestrationStatus status) {
        aaiVnfResources = new AAIVnfResources();
        this.status = status;
    }

    @Override
    public void updateAAI(ExtractPojosForBB pojosForBB, BuildingBlockExecution execution)
            throws BBObjectNotFoundException {
        GenericVnf vnf = pojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
        aaiVnfResources.updateOrchestrationStatusVnf(vnf, status);
    }
}
