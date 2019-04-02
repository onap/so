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

import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.CREATE_VNF_RESPONSE_PARAM_NAME;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.OPERATION_FINISHED_STATES;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.OPERATION_RETRIEVAL_STATES;
import static org.onap.so.bpmn.infrastructure.adapter.vnfm.tasks.Constants.OPERATION_STATUS_PARAM_NAME;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.vnfmadapter.v1.model.CreateVnfResponse;
import org.onap.vnfmadapter.v1.model.OperationStateEnum;
import org.onap.vnfmadapter.v1.model.QueryJobResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.common.base.Optional;


/**
 * @author waqas.ikram@est.tech
 *
 */
@Component
public class MonitorVnfmCreateJobTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorVnfmCreateJobTask.class);
  private final ExceptionBuilder exceptionUtil;
  private final VnfmAdapterServiceProvider vnfmAdapterServiceProvider;

  @Autowired
  public MonitorVnfmCreateJobTask(final VnfmAdapterServiceProvider vnfmAdapterServiceProvider,
      final ExceptionBuilder exceptionUtil) {
    this.vnfmAdapterServiceProvider = vnfmAdapterServiceProvider;
    this.exceptionUtil = exceptionUtil;
  }

  /**
   * Get the current operation status of instantiation job
   * 
   * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
   */
  public void getCurrentOperationStatus(final BuildingBlockExecution execution) {
    LOGGER.debug("Executing getCurrentOperationStatus  ...");
    final CreateVnfResponse vnfInstantiateResponse = execution.getVariable(CREATE_VNF_RESPONSE_PARAM_NAME);
    execution.setVariable(OPERATION_STATUS_PARAM_NAME, getOperationStatus(execution, vnfInstantiateResponse));
    LOGGER.debug("Finished executing getCurrentOperationStatus ...");
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
   * Log and throw exception on timeout for job status
   * 
   * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
   */
  public void timeOutLogFailue(final BuildingBlockExecution execution) {
    final String message = "Instantiation operation time out";
    LOGGER.error(message);
    exceptionUtil.buildAndThrowWorkflowException(execution, 1205, message);
  }

  /**
   * Check the final status of instantiation throw exception if not completed successfully
   * 
   * @param execution {@link org.onap.so.bpmn.common.DelegateExecutionImpl}
   */
  public void checkIfOperationWasSuccessful(final BuildingBlockExecution execution) {
    LOGGER.debug("Executing checkIfOperationWasSuccessful  ...");
    final Optional<OperationStateEnum> operationStatusOption = execution.getVariable(OPERATION_STATUS_PARAM_NAME);
    final CreateVnfResponse vnfInstantiateResponse = execution.getVariable(CREATE_VNF_RESPONSE_PARAM_NAME);
    if (operationStatusOption == null || !operationStatusOption.isPresent()) {
      final String message = "Unable to instantiate jobId: "
          + (vnfInstantiateResponse != null ? vnfInstantiateResponse.getJobId() : "null")
          + "Unable to retrieve OperationStatus";
      LOGGER.error(message);
      exceptionUtil.buildAndThrowWorkflowException(execution, 1206, message);
    }
    if (operationStatusOption.isPresent()) {
      final OperationStateEnum operationStatus = operationStatusOption.get();
      if (operationStatus != OperationStateEnum.COMPLETED) {
        final String message = "Unable to instantiate jobId: "
            + (vnfInstantiateResponse != null ? vnfInstantiateResponse.getJobId() : "null") + " OperationStatus: "
            + operationStatus;
        LOGGER.error(message);
        exceptionUtil.buildAndThrowWorkflowException(execution, 1207, message);
      }
      LOGGER.debug("Successfully completed instatiation of job {}", vnfInstantiateResponse);
    }
  }

  private Optional<OperationStateEnum> getOperationStatus(final BuildingBlockExecution execution,
      final CreateVnfResponse vnfInstantiateResponse) {

    final Optional<QueryJobResponse> instantiateOperationJobStatus =
        vnfmAdapterServiceProvider.getInstantiateOperationJobStatus(vnfInstantiateResponse.getJobId());

    if (instantiateOperationJobStatus.isPresent()) {
      final QueryJobResponse queryJobResponse = instantiateOperationJobStatus.get();

      if (!OPERATION_RETRIEVAL_STATES.contains(queryJobResponse.getOperationStatusRetrievalStatus())) {
        final String message =
            "Recevied invalid operation reterivel state: " + queryJobResponse.getOperationStatusRetrievalStatus();
        LOGGER.error(message);
        exceptionUtil.buildAndThrowWorkflowException(execution, 1203, message);
      }

      if (queryJobResponse.getOperationState() != null) {
        final OperationStateEnum operationStatus = queryJobResponse.getOperationState();
        LOGGER.debug("Operation {} with {} and operation retrieval status : {}", queryJobResponse.getId(),
            operationStatus, queryJobResponse.getOperationStatusRetrievalStatus());
        return Optional.of(queryJobResponse.getOperationState());
      }

      LOGGER.debug("Operation {} without operationStatus and operation retrieval status :{}", queryJobResponse.getId(),
          queryJobResponse.getOperationStatusRetrievalStatus());

    }
    return Optional.absent();
  }
}
