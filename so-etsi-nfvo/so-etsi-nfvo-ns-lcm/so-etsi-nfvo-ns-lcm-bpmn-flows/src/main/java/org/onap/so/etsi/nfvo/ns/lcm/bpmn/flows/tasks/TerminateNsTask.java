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
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoNfInst;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoNsInst;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.State;
import org.onap.so.etsi.nfvo.ns.lcm.database.service.DatabaseServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.NS_INSTANCE_ID_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum.FINISHED;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum.IN_PROGRESS;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum.STARTED;

/**
 * @author Andrew Lamb (andrew.a.lamb@est.tech)
 *
 */
@Component
public class TerminateNsTask extends AbstractNetworkServiceTask {

    private static final String IS_NS_TERMINATION_SUCCESSFUL_PARAM_NAME = "isNsTerminationSuccessful";
    private static final Logger logger = LoggerFactory.getLogger(TerminateNsTask.class);

    @Autowired
    public TerminateNsTask(final DatabaseServiceProvider databaseServiceProvider) {
        super(databaseServiceProvider);
    }

    public void setJobStatusToStarted(final DelegateExecution execution) {
        setJobStatus(execution, STARTED, "Terminate NS workflow process started");
    }

    public void setJobStatusToFinished(final DelegateExecution execution) {
        setJobStatus(execution, FINISHED, "Terminate NS workflow process finished");
    }

    public void setJobStatusToError(final DelegateExecution execution) {
        updateNsInstanceStatus(execution, State.FAILED);
        setJobStatusToError(execution, "Terminate NS workflow process failed");
    }

    public void updateNsInstanceStatusToTerminating(final DelegateExecution execution) {
        logger.info("Executing updateNsInstanceStatusToTerminating");
        setJobStatus(execution, IN_PROGRESS, "Updating NfvoNsInst Status to " + State.TERMINATING);
        updateNsInstanceStatus(execution, State.TERMINATING);
        logger.info("Finished executing updateNsInstanceStatusToTerminating  ...");
    }

    public void updateNsInstanceStatusToNotInstantiated(final DelegateExecution execution) {
        logger.info("Executing updateNsInstanceStatusToNotInstantiated");
        setJobStatus(execution, IN_PROGRESS, "Updating NfvoNsInst Status to " + State.NOT_INSTANTIATED);
        updateNsInstanceStatus(execution, State.NOT_INSTANTIATED);
        logger.info("Finished executing updateNsInstanceStatusToNotInstantiated  ...");
    }

    public void getVnfIdsInNs(final DelegateExecution execution) {
        logger.info("Executing getVnfIdsInNs ...");
        setJobStatus(execution, IN_PROGRESS, "Getting Each VnfId In Ns");
        final List<String> nfvoNfInstIds = getNfvoNfInstIds(execution);
        execution.setVariable(CamundaVariableNameConstants.NFVO_NF_INST_IDS_PARAM_NAME, nfvoNfInstIds);
        logger.info("Finished executing getVnfIdsInNs ...");
    }

    public void checkIfVnfTerminationWasSuccessful(final DelegateExecution execution) {
        logger.info("Executing checkIfVnfTerminationWasSuccessful");

        final String nsInstId = (String) execution.getVariable(NS_INSTANCE_ID_PARAM_NAME);
        final List<NfvoNfInst> nfInstances = databaseServiceProvider.getNfvoNfInstByNsInstId(nsInstId);

        if ((nfInstances != null) && !(nfInstances.isEmpty())) {
            final String message = "Found NF Instances";
            nfInstances.stream().forEach(instance -> {
                logger.error("VNF : {} {} termination failed", instance.getNfInstId(), instance.getName());
                execution.setVariable(IS_NS_TERMINATION_SUCCESSFUL_PARAM_NAME, false);
            });
            abortOperation(execution, message);
        }

        execution.setVariable(IS_NS_TERMINATION_SUCCESSFUL_PARAM_NAME, true);
        logger.info("Finished executing checkIfVnfTerminationWasSuccessful");
    }

    public void logTimeOut(final DelegateExecution execution) {
        logger.error("Vnf termination timedOut ...");
        final String nsInstId = (String) execution.getVariable(NS_INSTANCE_ID_PARAM_NAME);
        final List<NfvoNfInst> nfInstances = databaseServiceProvider.getNfvoNfInstByNsInstId(nsInstId);
        if (nfInstances != null) {
            nfInstances.stream().forEach(instance -> {
                logger.info("Current status {} of vnf: {}", instance.getStatus(), instance.getName());
            });
        }
    }

    private void updateNsInstanceStatus(final DelegateExecution execution, final State nsStatus) {
        final NfvoNsInst nfvoNsInst = getNfvoNsInst(execution);
        logger.info("Updating NfvoNsInst Status to {} and saving to DB", nsStatus);
        nfvoNsInst.setStatus(nsStatus);
        databaseServiceProvider.saveNfvoNsInst(nfvoNsInst);
    }

    private List<String> getNfvoNfInstIds(final DelegateExecution execution) {
        final String nsInstId = (String) execution.getVariable(NS_INSTANCE_ID_PARAM_NAME);
        logger.info("Getting NfvoNfInstList using nsInstId: {}", nsInstId);
        final List<NfvoNfInst> nfvoNfInstList = databaseServiceProvider.getNfvoNfInstByNsInstId(nsInstId);

        if (nfvoNfInstList.size() == 0) {
            final String message = "Unable to find NF Instances in database using id: " + nsInstId;
            abortOperation(execution, message);
        }

        final List<String> vnfIdsList = new ArrayList<>();

        nfvoNfInstList.stream().forEach(nfvoNfInst -> {
            vnfIdsList.add(nfvoNfInst.getNfInstId());
        });

        return vnfIdsList;
    }

}
