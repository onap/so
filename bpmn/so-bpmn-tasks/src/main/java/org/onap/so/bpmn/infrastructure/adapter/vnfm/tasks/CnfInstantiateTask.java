/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Ericsson. All rights reserved.
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
 * 
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks;

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * This class performs CNF Instantiation
 * 
 * @author sagar.shetty@est.tech
 */
@Component
public class CnfInstantiateTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(CnfInstantiateTask.class);
    private final ExtractPojosForBB extractPojosForBB;

    @Autowired
    public CnfInstantiateTask(final ExtractPojosForBB extractPojosForBB) {
        this.extractPojosForBB = extractPojosForBB;
    }

    public void handleCnfInstatiate(final BuildingBlockExecution execution) {
        try {
            LOGGER.debug("Executing handleCnfInstatiate  ...");
            ExecuteBuildingBlock executeBuildingBlock = (ExecuteBuildingBlock) execution.getVariable("buildingBlock");
            RequestDetails requestDetails = executeBuildingBlock.getRequestDetails();
            LOGGER.debug("RequestDetails: {}", requestDetails);
            LOGGER.debug("Finished executing handleCnfInstatiate ...");
        } catch (final Exception exception) {
            LOGGER.error("Unable to instantiate CNF", exception);
        }
    }
}
