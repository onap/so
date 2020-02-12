/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
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
package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AssignNetwork {

    private static final Logger logger = LoggerFactory.getLogger(AssignNetwork.class);

    @Autowired
    private ExtractPojosForBB extractPojosForBB;

    /**
     * Check if network was found by name
     * 
     * @param execution
     * @return
     */
    public boolean networkFoundByName(BuildingBlockExecution execution) {
        try {
            L3Network l3network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);

            if (!OrchestrationStatus.PRECREATED.equals(l3network.getOrchestrationStatus())) {
                logger.debug("network found in NOT PRECREATED status");
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
        return false;
    }
}
