/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Ericsson. All rights reserved.
 * ================================================================================
 * Copyright (C) 2019 Samsung
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
package org.onap.so.bpmn.infrastructure.adapter.cnfm.tasks;

import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.CREATE_CNF_STATUS_RESPONSE_PARAM_NAME;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.OPERATION_STATUS_PARAM_NAME;
import java.net.URI;
import java.util.Optional;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.CreateVnfResponse;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.QueryJobResponse;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.client.exception.ExceptionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.common.collect.ImmutableSet;
import org.onap.so.cnfm.lcm.model.AsLcmOpOcc;
import org.onap.so.cnfm.lcm.model.AsLcmOpOcc.OperationStateEnum;


/**
 * @author sagar.shetty@est.tech
 */
@Component
public class MonitorCnfmCreateJobTask {

    public static final ImmutableSet<OperationStateEnum> OPERATION_FINISHED_STATES =
            ImmutableSet.of(OperationStateEnum.COMPLETED, OperationStateEnum.FAILED, OperationStateEnum.ROLLED_BACK);
    private static final String CNFM_REQUEST_STATUS_CHECK_URL = "CnfmStatusCheckUrl";
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorCnfmCreateJobTask.class);
    protected final ExceptionBuilder exceptionUtil;
    private final CnfmHttpServiceProvider cnfmHttpServiceProvider;

    @Autowired
    public MonitorCnfmCreateJobTask(final CnfmHttpServiceProvider cnfmHttpServiceProvider,
                                    final ExceptionBuilder exceptionUtil) {
        this.cnfmHttpServiceProvider = cnfmHttpServiceProvider;
        this.exceptionUtil = exceptionUtil;
    }

    /**
     * Get the current operation status of instantiation job
     *
     * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
     */
    public void getCurrentOperationStatus(final BuildingBlockExecution execution) {
        try {
            LOGGER.debug("Executing getCurrentOperationStatus  ...");
            final URI operation_status_url = execution.getVariable(CNFM_REQUEST_STATUS_CHECK_URL);
            LOGGER.debug("Executing getCurrentOperationStatus for CNF... :{}", operation_status_url.toString());
            final Optional<AsLcmOpOcc> instantiateOperationJobStatus =
                    cnfmHttpServiceProvider.getInstantiateOperationJobStatus(operation_status_url.toString());
            if (instantiateOperationJobStatus.isPresent()) {
                final AsLcmOpOcc asLcmOpOccResponse = instantiateOperationJobStatus.get();
                if (asLcmOpOccResponse.getOperationState() != null) {
                    final OperationStateEnum operationStatus = asLcmOpOccResponse.getOperationState();
                    LOGGER.debug("Operation {} with {} and operation retrieval status : {}", asLcmOpOccResponse.getId(),
                            operationStatus, asLcmOpOccResponse.getOperationState());
                    execution.setVariable(OPERATION_STATUS_PARAM_NAME, asLcmOpOccResponse.getOperationState());
                }

                LOGGER.debug("Operation {} without operationStatus and operation retrieval status :{}",
                        asLcmOpOccResponse.getId(), asLcmOpOccResponse.getOperationState());
            }
            execution.setVariable(CREATE_CNF_STATUS_RESPONSE_PARAM_NAME, instantiateOperationJobStatus.get());
            LOGGER.debug("Finished executing getCurrentOperationStatus for CNF...");
        } catch (final Exception exception) {
            final String message = "Unable to invoke get current Operation status";
            LOGGER.error(message);
            exceptionUtil.buildAndThrowWorkflowException(execution, 1209, message);

        }
    }

    /**
     * Log and throw exception on timeout for job status
     *
     * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
     */
    public void timeOutLogFailue(final BuildingBlockExecution execution) {
        final String message = "CNF Instantiation operation time out";
        LOGGER.error(message);
        exceptionUtil.buildAndThrowWorkflowException(execution, 1205, message);
    }

    /**
     * Check the final status of instantiation throw exception if not completed successfully
     *
     * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
     */
    public void checkIfOperationWasSuccessful(final BuildingBlockExecution execution) {
        LOGGER.debug("Executing CNF checkIfOperationWasSuccessful  ...");
        final OperationStateEnum operationStatusOption = execution.getVariable(OPERATION_STATUS_PARAM_NAME);
        final AsLcmOpOcc cnfInstantiateStautusResponse = execution.getVariable(CREATE_CNF_STATUS_RESPONSE_PARAM_NAME);
        if (operationStatusOption == null) {
            final String message = "Unable to instantiate CNF jobId: "
                    + (cnfInstantiateStautusResponse != null ? cnfInstantiateStautusResponse.getId() : "null")
                    + "Unable to retrieve OperationStatus";
            LOGGER.error(message);
            exceptionUtil.buildAndThrowWorkflowException(execution, 1206, message);
        } else {
            final OperationStateEnum operationStatus = operationStatusOption;
            if (operationStatus != OperationStateEnum.COMPLETED) {
                final String message = "Unable to instantiate jobId: "
                        + (cnfInstantiateStautusResponse != null ? cnfInstantiateStautusResponse.getId() : "null")
                        + " OperationStatus: " + operationStatus;
                LOGGER.error(message);
                exceptionUtil.buildAndThrowWorkflowException(execution, 1207, message);
            }
            LOGGER.debug("Successfully completed CNF instatiation of job status {}", cnfInstantiateStautusResponse);
        }
    }

    /**
     * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
     * @return boolean to indicate whether job has competed or not
     */
    public boolean hasOperationFinished(final BuildingBlockExecution execution) {
        LOGGER.debug("Executing hasOperationFinished  ...");

        final OperationStateEnum operationStatusOption = execution.getVariable(OPERATION_STATUS_PARAM_NAME);
        if (operationStatusOption != null) {
            return OPERATION_FINISHED_STATES.contains(operationStatusOption);
        }
        LOGGER.debug("OperationStatus is not present yet... ");
        LOGGER.debug("Finished executing hasOperationFinished ...");
        return false;
    }
}
