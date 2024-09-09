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

import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.OPERATION_FINISHED_STATES;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.OPERATION_RETRIEVAL_STATES;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.OPERATION_STATUS_PARAM_NAME;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.OperationStateEnum;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.QueryJobResponse;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.google.common.base.Optional;


/**
 * 
 * @author Lathishbabu Ganesan (lathishbabu.ganesan@est.tech)
 *
 */
@Component
public class MonitorVnfmJobTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorVnfmJobTask.class);
    protected final ExceptionBuilder exceptionUtil;
    protected final VnfmAdapterServiceProvider vnfmAdapterServiceProvider;

    @Autowired
    public MonitorVnfmJobTask(
            @Qualifier("VnfmAdapterServiceProvider") final VnfmAdapterServiceProvider vnfmAdapterServiceProvider,
            final ExceptionBuilder exceptionUtil) {
        this.vnfmAdapterServiceProvider = vnfmAdapterServiceProvider;
        this.exceptionUtil = exceptionUtil;
    }

    /**
     * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
     * @return boolean to indicate whether job has competed or not
     */
    public boolean hasOperationFinished(final BuildingBlockExecution execution) {
        LOGGER.debug("Executing hasOperationFinished  ...");

        final Optional<OperationStateEnum> operationStatusOption = execution.getVariable(OPERATION_STATUS_PARAM_NAME);
        if (operationStatusOption != null && operationStatusOption.isPresent()) {
            return OPERATION_FINISHED_STATES.contains(operationStatusOption.get());
        }
        LOGGER.debug("OperationStatus is not present yet... ");
        LOGGER.debug("Finished executing hasOperationFinished ...");
        return false;
    }

    /**
     * This method calls the Vnfm adapter and gets the Operation status of the job
     * 
     * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
     * @param jobId unique job id
     * @return Operation State
     */
    protected Optional<OperationStateEnum> getOperationStatus(final BuildingBlockExecution execution,
            final String jobId) {

        final Optional<QueryJobResponse> instantiateOperationJobStatus =
                vnfmAdapterServiceProvider.getInstantiateOperationJobStatus(jobId);

        if (instantiateOperationJobStatus.isPresent()) {
            final QueryJobResponse queryJobResponse = instantiateOperationJobStatus.get();

            if (!OPERATION_RETRIEVAL_STATES.contains(queryJobResponse.getOperationStatusRetrievalStatus())) {
                final String message = "Recevied invalid operation reterivel state: "
                        + queryJobResponse.getOperationStatusRetrievalStatus();
                LOGGER.error(message);
                exceptionUtil.buildAndThrowWorkflowException(execution, 1203, message);
            }
            if (queryJobResponse.getOperationState() != null) {
                final OperationStateEnum operationStatus = queryJobResponse.getOperationState();
                LOGGER.debug("Operation {} with {} and operation retrieval status : {}", queryJobResponse.getId(),
                        operationStatus, queryJobResponse.getOperationStatusRetrievalStatus());
                return Optional.of(queryJobResponse.getOperationState());
            }

            LOGGER.debug("Operation {} without operationStatus and operation retrieval status :{}",
                    queryJobResponse.getId(), queryJobResponse.getOperationStatusRetrievalStatus());
        }
        return Optional.absent();
    }
}
