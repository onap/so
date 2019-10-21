/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Bell Canada
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
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;

public interface UpdateOrchestrationStatusForCds {

    /**
     * Update orchestration status in AAI for VNF/VF-MODULE.
     * 
     * @param extractPojosForBB - ExtractPojosForBB object
     * @param execution - BuildingBlockExecution object
     * @throws BBObjectNotFoundException
     */
    void updateAAI(ExtractPojosForBB extractPojosForBB, BuildingBlockExecution execution)
            throws BBObjectNotFoundException;
}
