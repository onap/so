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
package org.onap.so.bpmn.infrastructure.adapter.cnfm.tasks;

import org.onap.logging.filter.base.ONAPComponents;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;


/**
 * This class performs CNF Delete
 *
 * @author raviteja.karumuri@est.tech
 */
@Component
public class CnfDeleteTask {

    private static final String AS_INSTANCE_ID = "asInstanceid";
    private static final Logger LOGGER = LoggerFactory.getLogger(CnfInstantiateTask.class);
    private final ExceptionBuilder exceptionUtil;
    private final CnfmHttpServiceProvider cnfmHttpServiceProvider;

    @Autowired
    public CnfDeleteTask(final CnfmHttpServiceProvider cnfmHttpServiceProvider, final ExceptionBuilder exceptionUtil) {
        this.cnfmHttpServiceProvider = cnfmHttpServiceProvider;
        this.exceptionUtil = exceptionUtil;
    }

    public void invokeCnfmToDeleteAsInstnace(final BuildingBlockExecution execution) {
        try {
            LOGGER.debug("Executing DelteAsInstance task  ...");
            final ExecuteBuildingBlock executeBuildingBlock =
                    (ExecuteBuildingBlock) execution.getVariable("buildingBlock");

            final GeneralBuildingBlock generalBuildingBlock = execution.getGeneralBuildingBlock();

            final RequestDetails requestDetails = executeBuildingBlock.getRequestDetails();
            LOGGER.debug("RequestDetails of DeleteAsInstance: {}", requestDetails);

            if (isNull(requestDetails) && isNull(requestDetails.getModelInfo())
                    && isNull(requestDetails.getRequestInfo()) && isNull(requestDetails.getCloudConfiguration())
                    && isNull(generalBuildingBlock)) {
                LOGGER.error("Missing Mandatory attribute from RequestDetails: {} or GeneralBuildingBlock: {}",
                        requestDetails, generalBuildingBlock);
                exceptionUtil.buildAndThrowWorkflowException(execution, 2000,
                        "Missing Mandatory attribute from RequestDetails or GeneralBuildingBlock", ONAPComponents.SO);
            }

            LOGGER.debug("Finished executing DeleteAsInstance task ...");

        } catch (final Exception exception) {
            LOGGER.error("Unable to invoke DeleteAsInstance", exception);
            exceptionUtil.buildAndThrowWorkflowException(execution, 2001, exception);
        }
    }
}
