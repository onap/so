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

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.aai.AaiServiceProvider;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoNsInst;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.State;
import org.onap.so.etsi.nfvo.ns.lcm.database.service.DatabaseServiceProvider;
import org.onap.so.etsi.nfvo.ns.lcm.model.InlineResponse400;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Optional;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.NS_INSTANCE_ID_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.NS_WORKFLOW_PROCESSING_EXCEPTION_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum.FINISHED;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum.IN_PROGRESS;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum.STARTED;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.State.NOT_INSTANTIATED;

/**
 * @author Andrew Lamb (andrew.a.lamb@est.tech)
 *
 */
@Component
public class DeleteNsTask extends AbstractNetworkServiceTask {

    private static final String NS_INSTANCE_EXISTS_PARAM_NAME = "nsInstanceExists";
    private static final String NS_INSTANCE_IS_IN_NOT_INSTANTIATED_STATE_PARAM_NAME = "isInNotInstantiatedState";
    private static final Logger logger = LoggerFactory.getLogger(DeleteNsTask.class);
    private final AaiServiceProvider aaiServiceProvider;

    @Autowired
    public DeleteNsTask(final DatabaseServiceProvider databaseServiceProvider,
            final AaiServiceProvider aaiServiceProvider) {
        super(databaseServiceProvider);
        this.aaiServiceProvider = aaiServiceProvider;
    }

    public void setJobStatusToStarted(final DelegateExecution execution) {
        setJobStatus(execution, STARTED, "Delete NS workflow process started");
    }

    public void setJobStatusToFinished(final DelegateExecution execution) {
        setJobStatus(execution, FINISHED, "Delete NS workflow process finished");
    }

    public void setJobStatusInProgress(final DelegateExecution execution, final String message) {
        setJobStatus(execution, IN_PROGRESS, message);
    }

    public void setJobStatusToError(final DelegateExecution execution) {
        setJobStatusToError(execution, "Delete NS workflow process failed");
    }

    public void checkIfNsInstanceExistsInDb(final DelegateExecution execution) {
        logger.info("Executing checkIfNsInstanceExistsInDb  ...");
        setJobStatusInProgress(execution, "Checking that NS Instance Exists in DB");

        final String nsInstId = (String) execution.getVariable(NS_INSTANCE_ID_PARAM_NAME);
        final Optional<NfvoNsInst> optionalNfvoNsInst = databaseServiceProvider.getNfvoNsInst(nsInstId);
        final boolean nsInstanceExists = optionalNfvoNsInst.isPresent();
        logger.info("NS Instance entry with id: {} {} exist in database", nsInstId,
                nsInstanceExists ? "does" : "doesn't");
        execution.setVariable(NS_INSTANCE_EXISTS_PARAM_NAME, nsInstanceExists);

        if (!nsInstanceExists) {
            final String message =
                    "NS Instance with id: " + nsInstId + " does not exist in database, so will not be deleted.";
            logger.info(message);
            execution.setVariable(NS_WORKFLOW_PROCESSING_EXCEPTION_PARAM_NAME, new InlineResponse400().detail(message));
        }

        logger.info("Finished executing checkIfNsInstanceExistsInDb ...");
    }

    public void checkthatNsInstanceInDbIsInNotInstantiatedState(final DelegateExecution execution) {
        logger.info("Executing checkthatNsInstanceInDbIsInNotInstantiatedState ...");
        setJobStatusInProgress(execution, "Checking that NS Instance is in NOT_INSTANTIATED state in Db");

        final String nsInstId = (String) execution.getVariable(NS_INSTANCE_ID_PARAM_NAME);
        final NfvoNsInst nfvoNsInst = getNfvoNsInst(execution, nsInstId);
        final State nfvoNsInstState = nfvoNsInst.getStatus();
        final boolean nsInstanceIsNotInstantiated = NOT_INSTANTIATED.equals(nfvoNsInstState);
        logger.info("Ns Instance entry with nsInstId: {} is in state: {}", nsInstId, nfvoNsInstState);
        execution.setVariable(NS_INSTANCE_IS_IN_NOT_INSTANTIATED_STATE_PARAM_NAME, nsInstanceIsNotInstantiated);

        if (!nsInstanceIsNotInstantiated) {
            final String message =
                    "Cannot Delete NS Instance with id: " + nsInstId + " in the state: " + nfvoNsInstState;
            logger.info(message);
            execution.setVariable(NS_WORKFLOW_PROCESSING_EXCEPTION_PARAM_NAME, new InlineResponse400().detail(message));
        }

        logger.info("Finished executing checkthatNsInstanceInDbIsInNotInstantiatedState ...");
    }

    public void deleteNsInstanceFromAai(final DelegateExecution execution) {
        logger.info("Executing deleteNsInstanceFromAAI ...");
        setJobStatusInProgress(execution, "Deleting NS Instance from AAI");

        final String nsInstId = (String) execution.getVariable(NS_INSTANCE_ID_PARAM_NAME);
        final NfvoNsInst nfvoNsInst = getNfvoNsInst(execution, nsInstId);
        final String globalCustomerId = nfvoNsInst.getGlobalCustomerId();
        final String serviceType = nfvoNsInst.getServiceType();

        aaiServiceProvider.deleteServiceInstance(globalCustomerId, serviceType, nsInstId);

        logger.info("Finished executing deleteNsInstanceFromAAI ...");
    }

    public void deleteNsInstanceFromDb(final DelegateExecution execution) {
        logger.info("Executing deleteNsInstanceFromDb ...");
        setJobStatusInProgress(execution, "Deleting NS Instance from Db");

        final String nsInstId = (String) execution.getVariable(NS_INSTANCE_ID_PARAM_NAME);
        databaseServiceProvider.deleteNfvoNsInst(nsInstId);

        logger.info("Finished executing deleteNsInstanceFromDb ...");
    }
}
