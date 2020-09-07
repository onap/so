/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
package org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.tasks;

import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.OPERATION_STATUS_PARAM_NAME;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.OperationStateEnum;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.OperationStatusRetrievalStatusEnum;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.QueryJobResponse;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.vnfm.Sol003AdapterServiceProvider;
import org.onap.so.etsi.nfvo.ns.lcm.database.service.DatabaseServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.common.collect.ImmutableSet;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Component
public class MonitorSol003AdapterJobTask extends AbstractNetworkServiceTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorSol003AdapterJobTask.class);
    public static final ImmutableSet<OperationStateEnum> OPERATION_FINISHED_STATES =
            ImmutableSet.of(OperationStateEnum.COMPLETED, OperationStateEnum.FAILED, OperationStateEnum.ROLLED_BACK);
    public static final ImmutableSet<OperationStatusRetrievalStatusEnum> OPERATION_RETRIEVAL_STATES = ImmutableSet
            .of(OperationStatusRetrievalStatusEnum.STATUS_FOUND, OperationStatusRetrievalStatusEnum.WAITING_FOR_STATUS);
    protected final Sol003AdapterServiceProvider sol003AdapterServiceProvider;

    @Autowired
    public MonitorSol003AdapterJobTask(final Sol003AdapterServiceProvider sol003AdapterServiceProvider,
            final DatabaseServiceProvider databaseServiceProvider) {
        super(databaseServiceProvider);
        this.sol003AdapterServiceProvider = sol003AdapterServiceProvider;
    }

    public boolean hasOperationFinished(final DelegateExecution execution) {
        LOGGER.debug("Executing hasOperationFinished  ...");

        final OperationStateEnum operationStatus =
                (OperationStateEnum) execution.getVariable(OPERATION_STATUS_PARAM_NAME);
        if (operationStatus != null) {
            return OPERATION_FINISHED_STATES.contains(operationStatus);
        }
        LOGGER.debug("OperationStatus is not present yet... ");
        LOGGER.debug("Finished executing hasOperationFinished ...");
        return false;
    }

    protected OperationStateEnum getOperationStatus(final DelegateExecution execution, final String jobId) {

        final Optional<QueryJobResponse> instantiateOperationJobStatus =
                sol003AdapterServiceProvider.getInstantiateOperationJobStatus(jobId);

        if (instantiateOperationJobStatus.isPresent()) {
            final QueryJobResponse queryJobResponse = instantiateOperationJobStatus.get();

            if (!OPERATION_RETRIEVAL_STATES.contains(queryJobResponse.getOperationStatusRetrievalStatus())) {
                final String message = "Received invalid operation retrieval state: "
                        + queryJobResponse.getOperationStatusRetrievalStatus();
                LOGGER.error(message);
                abortOperation(execution, message);
            }
            if (queryJobResponse.getOperationState() != null) {
                final OperationStateEnum operationStatus = queryJobResponse.getOperationState();
                LOGGER.debug("Operation {} with {} and operation retrieval status : {}", queryJobResponse.getId(),
                        operationStatus, queryJobResponse.getOperationStatusRetrievalStatus());
                return queryJobResponse.getOperationState();
            }

            LOGGER.debug("Operation {} without operationStatus and operation retrieval status :{}",
                    queryJobResponse.getId(), queryJobResponse.getOperationStatusRetrievalStatus());
        }
        return null;
    }
}
