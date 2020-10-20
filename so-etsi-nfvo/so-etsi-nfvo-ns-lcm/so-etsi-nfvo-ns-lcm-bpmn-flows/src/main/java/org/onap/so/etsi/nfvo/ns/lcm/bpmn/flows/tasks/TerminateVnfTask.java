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

import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.DELETE_VNF_RESPONSE_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.NF_INST_ID_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.TERMINATE_VNF_VNFID_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum.ERROR;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum.FINISHED;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum.IN_PROGRESS;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.DeleteVnfResponse;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.aai.AaiServiceProvider;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.vnfm.Sol003AdapterServiceProvider;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.State;
import org.onap.so.etsi.nfvo.ns.lcm.database.service.DatabaseServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Andrew Lamb (andrew.a.lamb@est.tech)
 */
@Component
public class TerminateVnfTask extends AbstractNetworkServiceTask {
    private static final Logger logger = LoggerFactory.getLogger(TerminateVnfTask.class);
    private final AaiServiceProvider aaiServiceProvider;
    private final Sol003AdapterServiceProvider sol003AdapterServiceProvider;

    @Autowired
    public TerminateVnfTask(final DatabaseServiceProvider databaseServiceProvider,
            final AaiServiceProvider aaiServiceProvider,
            final Sol003AdapterServiceProvider sol003AdapterServiceProvider) {
        super(databaseServiceProvider);
        this.aaiServiceProvider = aaiServiceProvider;
        this.sol003AdapterServiceProvider = sol003AdapterServiceProvider;
    }

    public void checkIfNfInstanceExistsInDb(final DelegateExecution execution) {
        logger.info("Executing checkIfNfInstanceInDb");
        final String vnfId = (String) execution.getVariable(TERMINATE_VNF_VNFID_PARAM_NAME);
        logger.info("vnfId: {}", vnfId);
        execution.setVariable(NF_INST_ID_PARAM_NAME, vnfId);

        addJobStatus(execution, JobStatusEnum.IN_PROGRESS,
                "Checking if VNF Instance with id: " + vnfId + " exists in database.");
        if (!databaseServiceProvider.isNfInstExists(vnfId)) {
            abortOperation(execution,
                    "VNF instance with id: " + vnfId + " does not exist in database, so will not be terminated.");
        }
        logger.info("Finished executing checkIfNfInstanceInDb  ...");

    }

    public void invokeTerminateRequest(final DelegateExecution execution) {
        logger.info("Executing invokeTerminateRequest");
        final String vnfId = (String) execution.getVariable(TERMINATE_VNF_VNFID_PARAM_NAME);

        try {
            addJobStatus(execution, IN_PROGRESS, "Invoking SOL003 adapter for terminating VNF with vnfId: " + vnfId);

            final Optional<DeleteVnfResponse> optional = sol003AdapterServiceProvider.invokeTerminationRequest(vnfId);

            if (optional.isEmpty()) {
                final String errorMessage = "Unexpected error while processing terminate request for vnfId: " + vnfId;
                logger.error(errorMessage);
                abortOperation(execution, errorMessage);
            }

            final DeleteVnfResponse vnfResponse = optional.get();

            logger.info("Vnf delete response: {}", vnfResponse);
            execution.setVariable(DELETE_VNF_RESPONSE_PARAM_NAME, vnfResponse);
            addJobStatus(execution, IN_PROGRESS, "Successfully invoked SOL003 adapter terminate VNF with vnfId: "
                    + vnfId + " DeleteVnfResponse Job Id: " + vnfResponse.getJobId());
            logger.debug("Finished executing invokeTerminateRequest ...");
        } catch (final Exception exception) {
            final String message = "Unable to invoke terminate request for vnfId: " + vnfId;
            logger.error(message, exception);
            abortOperation(execution, message);
        }

    }

    public void deleteGenericVnfFromAai(final DelegateExecution execution) {
        logger.info("Executing deleteGenericVnfFromAai");
        final String vnfId = (String) execution.getVariable(TERMINATE_VNF_VNFID_PARAM_NAME);

        try {
            addJobStatus(execution, IN_PROGRESS, "Deleting GenericVnf record from AAI for vnfId: " + vnfId);
            aaiServiceProvider.deleteGenericVnf(vnfId);

        } catch (final Exception exception) {
            final String message = "Unable to Delete GenericVnf from AAI for vnfId: " + vnfId;
            logger.error(message, exception);
            abortOperation(execution, message);
        }

        logger.info("Finished executing deleteGenericVnfFromAai ...");
    }

    public void deleteNfInstanceFromDb(final DelegateExecution execution) {
        logger.info("Executing deleteNfInstanceFromDb");
        final String vnfId = (String) execution.getVariable(TERMINATE_VNF_VNFID_PARAM_NAME);

        addJobStatus(execution, IN_PROGRESS, "Deleting NF Instance record from Database for vnfId: " + vnfId);
        databaseServiceProvider.deleteNfvoNfInst(vnfId);

        addJobStatus(execution, FINISHED, "Successfully finished terminating VNF with vnfId: " + vnfId);
        logger.info("Finished executing deleteNfInstanceFromDb ...");
    }

    public void updateNfInstanceStatusToTerminating(final DelegateExecution execution) {
        logger.info("Executing updateNfInstanceStatusToTerminating");

        updateNfInstanceStatus(execution, State.TERMINATING);
        final String vnfId = (String) execution.getVariable(TERMINATE_VNF_VNFID_PARAM_NAME);
        addJobStatus(execution, IN_PROGRESS,
                "Terminating VNF with vnfId: " + vnfId + " will set status to " + State.TERMINATING);

        logger.info("Finished executing updateNfInstanceStatusToTerminating  ...");

    }

    public void updateNfInstanceStatusToNotInstantiated(final DelegateExecution execution) {
        logger.info("Executing updateNfInstanceStatusToNotInstantiated");

        updateNfInstanceStatus(execution, State.NOT_INSTANTIATED);
        final String vnfId = (String) execution.getVariable(TERMINATE_VNF_VNFID_PARAM_NAME);
        addJobStatus(execution, IN_PROGRESS,
                "Successfully terminated VNF with vnfId: " + vnfId + " will set status to " + State.NOT_INSTANTIATED);

        logger.info("Finished executing updateNfInstanceStatusToInstantiated  ...");

    }

    public void updateNfInstanceStatusToFailed(final DelegateExecution execution) {
        logger.info("Executing updateNfInstanceStatusToFailed");

        updateNfInstanceStatus(execution, State.FAILED);
        final String vnfId = (String) execution.getVariable(TERMINATE_VNF_VNFID_PARAM_NAME);
        addJobStatus(execution, ERROR,
                "Failed to terminate VNF with vnfId: " + vnfId + " will set status to " + State.FAILED);

        logger.info("Finished executing updateNfInstanceStatusToFailed  ...");

    }

    private void updateNfInstanceStatus(final DelegateExecution execution, final State vnfStatus) {
        final String vnfId = (String) execution.getVariable(TERMINATE_VNF_VNFID_PARAM_NAME);

        final boolean isSuccessful = databaseServiceProvider.updateNfInstState(vnfId, vnfStatus);
        if (!isSuccessful) {
            final String message = "Unable to update NfvoNfInst " + vnfId + " status to" + vnfStatus + " in database";
            logger.error(message);
            abortOperation(execution, message);
        }
    }

}
