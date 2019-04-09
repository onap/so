/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.so.adapters.vnfmadapter.jobmanagement;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.VnfmServiceProvider;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200;
import org.onap.so.adapters.vnfmadapter.rest.exceptions.JobNotFoundException;
import org.onap.vnfmadapter.v1.model.OperationEnum;
import org.onap.vnfmadapter.v1.model.OperationStateEnum;
import org.onap.vnfmadapter.v1.model.OperationStatusRetrievalStatusEnum;
import org.onap.vnfmadapter.v1.model.QueryJobResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Manages jobs enabling the status of jobs to be queried. A job is associated with an operation on a VNFM.
 */
@Component
public class JobManager {
    private static final String SEPARATOR = "_";
    private static Logger logger = getLogger(JobManager.class);
    private final Map<String, VnfmOperation> mapOfJobIdToVnfmOperation = Maps.newConcurrentMap();
    private final VnfmServiceProvider vnfmServiceProvider;

    @Autowired
    JobManager(final VnfmServiceProvider vnfmServiceProvider) {
        this.vnfmServiceProvider = vnfmServiceProvider;
    }

    /**
     * Create a job associated with an operation on a VNFM.
     *
     * @param vnfmId the VNFM the operation relates to
     * @param operationId the ID of the associated VNFM operation
     * @param waitForNotificationForSuccess if set to <code>true</code> the {@link QueryJobResponse#getOperationState()}
     *        shall not return {@link org.onap.vnfmadapter.v1.model.OperationStateEnum#COMPLETED} unless a required
     *        notification has been processed
     * @return the ID of the job. Can be used to query the job using {@link #getVnfmOperation(String)}
     */
    public String createJob(final String vnfmId, final String operationId,
            final boolean waitForNotificationForSuccess) {
        final String jobId = vnfmId + SEPARATOR + UUID.randomUUID().toString();
        final VnfmOperation vnfmOperation = new VnfmOperation(vnfmId, operationId, waitForNotificationForSuccess);
        mapOfJobIdToVnfmOperation.put(jobId, vnfmOperation);
        return jobId;
    }

    /**
     * Get the operation, associated with the given job ID, from the VNFM.
     *
     * @param jobId the job ID
     * @return the associated operation from the VNFM, or <code>null</code> of no operation is associated with the given
     *         job ID
     */
    public QueryJobResponse getVnfmOperation(final String jobId) {
        final VnfmOperation vnfmOperation = mapOfJobIdToVnfmOperation.get(jobId);
        final QueryJobResponse response = new QueryJobResponse();

        if (vnfmOperation == null) {
            throw new JobNotFoundException("No job found with ID: " + jobId);
        }

        final Optional<InlineResponse200> operationOptional =
                vnfmServiceProvider.getOperation(vnfmOperation.getVnfmId(), vnfmOperation.getOperationId());
        if (!operationOptional.isPresent()) {
            return response.operationStatusRetrievalStatus(OperationStatusRetrievalStatusEnum.OPERATION_NOT_FOUND);
        }
        final InlineResponse200 operation = operationOptional.get();

        logger.debug("Job Id: " + jobId + ", operationId: " + operation.getId() + ", operation details: " + operation);

        response.setOperationStatusRetrievalStatus(OperationStatusRetrievalStatusEnum.STATUS_FOUND);
        response.setId(operation.getId());
        response.setOperation(OperationEnum.fromValue(operation.getOperation().getValue()));
        response.setOperationState(getOperationState(vnfmOperation, operation));
        response.setStartTime(operation.getStartTime());
        response.setStateEnteredTime(operation.getStateEnteredTime());
        response.setVnfInstanceId(operation.getVnfInstanceId());

        return response;
    }

    private OperationStateEnum getOperationState(final VnfmOperation vnfmOperation,
            final InlineResponse200 operationResponse) {
        final OperationStateEnum operationState =
                OperationStateEnum.fromValue(operationResponse.getOperationState().getValue());
        switch (vnfmOperation.getNotificationStatus()) {
            case NOTIFICATION_PROCESSING_NOT_REQUIRED:
            default:
                return operationState;
            case NOTIFICATION_PROCESSING_PENDING:
                return org.onap.vnfmadapter.v1.model.OperationStateEnum.PROCESSING;
            case NOTIFICATION_PROCEESING_SUCCESSFUL:
                return operationState;
            case NOTIFICATION_PROCESSING_FAILED:
                return org.onap.vnfmadapter.v1.model.OperationStateEnum.FAILED;
        }
    }

    public void notificationProcessedForOperation(final String operationId,
            final boolean notificationProcessingWasSuccessful) {
        final java.util.Optional<VnfmOperation> relatedOperation = mapOfJobIdToVnfmOperation.values().stream()
                .filter(operation -> operation.getOperationId().equals(operationId)).findFirst();
        if (relatedOperation.isPresent()) {
            relatedOperation.get().setNotificationProcessed(notificationProcessingWasSuccessful);
        }
        logger.debug("No operation found for operation ID " + operationId);
    }

}
