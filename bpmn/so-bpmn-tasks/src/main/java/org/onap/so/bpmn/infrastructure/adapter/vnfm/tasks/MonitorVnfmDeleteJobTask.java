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
package org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks;

import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.DELETE_VNF_RESPONSE_PARAM_NAME;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.OPERATION_STATUS_PARAM_NAME;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.DeleteVnfResponse;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.OperationStateEnum;
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
public class MonitorVnfmDeleteJobTask extends MonitorVnfmJobTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorVnfmDeleteJobTask.class);

    @Autowired
    public MonitorVnfmDeleteJobTask(
            @Qualifier("VnfmAdapterServiceProvider") final VnfmAdapterServiceProvider vnfmAdapterServiceProvider,
            final ExceptionBuilder exceptionUtil) {
        super(vnfmAdapterServiceProvider, exceptionUtil);
    }

    /**
     * Get the current operation status of Delete job
     * 
     * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
     */
    public void getCurrentOperationStatus(final BuildingBlockExecution execution) {
        try {
            LOGGER.debug("Executing getCurrentOperationStatus  ...");
            final DeleteVnfResponse deleteVnfResponse = execution.getVariable(Constants.DELETE_VNF_RESPONSE_PARAM_NAME);
            execution.setVariable(OPERATION_STATUS_PARAM_NAME,
                    getOperationStatus(execution, deleteVnfResponse.getJobId()));
            LOGGER.debug("Finished executing getCurrentOperationStatus ...");
        } catch (final Exception exception) {
            final String message = "Unable to invoke get current Operation status";
            LOGGER.error(message);
            exceptionUtil.buildAndThrowWorkflowException(execution, 1216, message);

        }
    }

    /**
     * Log and throw exception on timeout for job status
     * 
     * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
     */
    public void timeOutLogFailue(final BuildingBlockExecution execution) {
        final String message = "Delete operation time out";
        LOGGER.error(message);
        exceptionUtil.buildAndThrowWorkflowException(execution, 1213, message);
    }

    /**
     * Check the final status of delete throw exception if not completed successfully
     * 
     * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
     */
    public void checkIfOperationWasSuccessful(final BuildingBlockExecution execution) {
        LOGGER.debug("Executing checkIfOperationWasSuccessful  ...");
        final Optional<OperationStateEnum> operationStatusOption = execution.getVariable(OPERATION_STATUS_PARAM_NAME);
        final DeleteVnfResponse deleteVnfResponse = execution.getVariable(DELETE_VNF_RESPONSE_PARAM_NAME);
        if (operationStatusOption == null || !operationStatusOption.isPresent()) {
            final String message =
                    "Unable to delete jobId: " + (deleteVnfResponse != null ? deleteVnfResponse.getJobId() : "null")
                            + "Unable to retrieve OperationStatus";
            LOGGER.error(message);
            exceptionUtil.buildAndThrowWorkflowException(execution, 1214, message);
        } else if (operationStatusOption != null && operationStatusOption.isPresent()) {
            final OperationStateEnum operationStatus = operationStatusOption.get();
            if (operationStatus != OperationStateEnum.COMPLETED) {
                final String message =
                        "Unable to Delete jobId: " + (deleteVnfResponse != null ? deleteVnfResponse.getJobId() : "null")
                                + " OperationStatus: " + operationStatus;
                LOGGER.error(message);
                exceptionUtil.buildAndThrowWorkflowException(execution, 1215, message);
            }
            LOGGER.debug("Successfully completed Deletion of job {}", deleteVnfResponse);
        }
    }
}
