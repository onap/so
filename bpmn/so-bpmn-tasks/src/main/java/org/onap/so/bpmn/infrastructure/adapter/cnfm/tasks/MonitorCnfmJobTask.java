/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2023 Nordix Foundation.
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

import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.CNF_STATUS_RESPONSE_PARAM_NAME;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.OPERATION_STATUS_PARAM_NAME;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.cnfm.lcm.model.AsLcmOpOcc;
import org.onap.so.cnfm.lcm.model.AsLcmOpOcc.OperationStateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author sagar.shetty@est.tech
 * @author Raviteja Karumuri (raviteja.karumuri@est.tech)
 */
@Component
public class MonitorCnfmJobTask {

    private static final String CNFM_REQUEST_STATUS_CHECK_URL = "CnfmStatusCheckUrl";
    private static final String MONITOR_JOB_NAME = "MonitorJobName";
    public static final Set<OperationStateEnum> OPERATION_FINISHED_STATES =
            Set.of(OperationStateEnum.COMPLETED, OperationStateEnum.FAILED, OperationStateEnum.ROLLED_BACK);
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorCnfmJobTask.class);
    protected final ExceptionBuilder exceptionUtil;
    private final CnfmHttpServiceProvider cnfmHttpServiceProvider;

    @Autowired
    public MonitorCnfmJobTask(
            @Qualifier("CnfmHttpServiceProviderImpl") final CnfmHttpServiceProvider cnfmHttpServiceProvider,
            final ExceptionBuilder exceptionUtil) {
        this.cnfmHttpServiceProvider = cnfmHttpServiceProvider;
        this.exceptionUtil = exceptionUtil;
    }

    /**
     * Get the current operation status of cnfm job
     * 
     * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
     */
    public void getCurrentOperationStatus(final BuildingBlockExecution execution) {
        try {
            LOGGER.debug("Executing getCurrentOperationStatus  ...");
            final Optional<URI> operationStatusURL = Optional.of(execution.getVariable(CNFM_REQUEST_STATUS_CHECK_URL));
            LOGGER.debug("Executing getCurrentOperationStatus for CNF... :{}", operationStatusURL);
            final Optional<AsLcmOpOcc> operationalJobStatus =
                    cnfmHttpServiceProvider.getOperationJobStatus(String.valueOf(operationStatusURL
                            .orElseThrow(() -> new BpmnError("Operational Status check url Not found"))));

            final AsLcmOpOcc asLcmOpOccResponse = operationalJobStatus
                    .orElseThrow(() -> new BpmnError("Unable to get operational Job status from the CNFM"));

            if (asLcmOpOccResponse.getOperationState() != null) {
                final OperationStateEnum operationStatus = asLcmOpOccResponse.getOperationState();
                LOGGER.debug("Operation {} with {} and operation retrieval status : {}", asLcmOpOccResponse.getId(),
                        operationStatus, operationStatus);
                execution.setVariable(OPERATION_STATUS_PARAM_NAME, operationStatus);
            } else {
                LOGGER.debug("Operation {} without operationStatus", asLcmOpOccResponse.getId());
                exceptionUtil.buildAndThrowWorkflowException(execution, 1206,
                        new Exception("Operation Status is empty"));
            }
            execution.setVariable(CNF_STATUS_RESPONSE_PARAM_NAME, asLcmOpOccResponse);
            LOGGER.debug("Finished executing getCurrentOperationStatus for CNF...");
        } catch (final Exception exception) {
            LOGGER.error("Unable to invoke get current Operation status");
            exceptionUtil.buildAndThrowWorkflowException(execution, 1209, exception);
        }
    }

    /**
     * Log and throw exception on timeout for job status
     *
     * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
     */
    public void timeOutLogFailure(final BuildingBlockExecution execution) {
        String message = "CNF" + execution.getVariable(MONITOR_JOB_NAME) + "operation time out";
        LOGGER.error(message);
        exceptionUtil.buildAndThrowWorkflowException(execution, 1205, new Exception(message));
    }

    /**
     * Check the final status of cnfm job throw exception if not completed successfully
     * 
     * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
     */
    public void checkIfOperationWasSuccessful(final BuildingBlockExecution execution) {
        LOGGER.debug("Executing CNF checkIfOperationWasSuccessful  ...");
        final OperationStateEnum operationStatusOption = execution.getVariable(OPERATION_STATUS_PARAM_NAME);
        final AsLcmOpOcc cnfStautusResponse = execution.getVariable(CNF_STATUS_RESPONSE_PARAM_NAME);
        if ((operationStatusOption == OperationStateEnum.FAILED)
                || (operationStatusOption == OperationStateEnum.FAILED_TEMP)) {
            final String message = "Unable to" + execution.getVariable(MONITOR_JOB_NAME) + "CNF jobId: "
                    + (cnfStautusResponse != null ? cnfStautusResponse.getId() : "null");
            LOGGER.error(message);
            exceptionUtil.buildAndThrowWorkflowException(execution, 1206, new Exception());
        } else if ((operationStatusOption == OperationStateEnum.COMPLETED)) {
            String monitorJobName = execution.getVariable(MONITOR_JOB_NAME);
            LOGGER.debug("Successfully completed CNF {} job", monitorJobName);
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
