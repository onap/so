/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Ericsson. All rights reserved.
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

import static org.onap.so.bpmn.servicedecomposition.entities.ResourceKey.GENERIC_VNF_ID;
import java.util.Optional;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.exception.GenericVnfNotFoundException;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAIVnfResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * @author Lathishbabu Ganesan (lathishbabu.ganesan@est.tech)
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
public abstract class MonitorVnfmNodeTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorVnfmNodeTask.class);

    private final ExtractPojosForBB extractPojosForBB;
    private final ExceptionBuilder exceptionUtil;
    private final AAIVnfResources aaiVnfResources;

    @Autowired
    public MonitorVnfmNodeTask(final ExtractPojosForBB extractPojosForBB, final ExceptionBuilder exceptionUtil,
            final AAIVnfResources aaiVnfResources) {
        this.exceptionUtil = exceptionUtil;
        this.extractPojosForBB = extractPojosForBB;
        this.aaiVnfResources = aaiVnfResources;
    }

    /**
     * Check the final status of vnf in A&AI
     * 
     * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
     */
    public void getNodeStatus(final BuildingBlockExecution execution) {
        try {
            LOGGER.debug("Executing getNodeStatus  ...");

            final org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf =
                    extractPojosForBB.extractByKey(execution, GENERIC_VNF_ID);

            final String vnfId = vnf.getVnfId();
            LOGGER.debug("Query A&AI for generic VNF using vnfID: {}", vnfId);
            final Optional<GenericVnf> aaiGenericVnfOptional = aaiVnfResources.getGenericVnf(vnfId);

            if (!aaiGenericVnfOptional.isPresent()) {
                throw new GenericVnfNotFoundException("Unable to find generic vnf in A&AI using vnfID: " + vnfId);
            }
            final GenericVnf genericVnf = aaiGenericVnfOptional.get();
            final String orchestrationStatus = genericVnf.getOrchestrationStatus();
            LOGGER.debug("Found generic vnf with orchestration status : {}", orchestrationStatus);

            execution.setVariable(getNodeStatusVariableName(), isOrchestrationStatusValid(orchestrationStatus));

        } catch (final Exception exception) {
            LOGGER.error("Unable to get vnf from AAI", exception);
            exceptionUtil.buildAndThrowWorkflowException(execution, 1220, exception);
        }
    }

    /**
     * Get variable to store in execution context
     * 
     * @return the variable name
     */
    public abstract String getNodeStatusVariableName();

    /**
     * @param orchestrationStatus the orchestration status from A&AI
     * @return true if valid
     */
    public abstract boolean isOrchestrationStatusValid(final String orchestrationStatus);


    /**
     * Log and throw exception on timeout for job status
     * 
     * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
     */
    public void timeOutLogFailue(final BuildingBlockExecution execution) {
        final String message = "Node operation time out";
        LOGGER.error(message);
        exceptionUtil.buildAndThrowWorkflowException(execution, 1221, message);
    }
}
