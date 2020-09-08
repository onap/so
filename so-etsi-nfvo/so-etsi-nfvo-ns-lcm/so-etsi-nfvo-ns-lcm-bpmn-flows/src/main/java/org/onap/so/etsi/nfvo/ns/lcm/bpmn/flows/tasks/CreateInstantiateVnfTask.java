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

import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.NS_INSTANCE_ID_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum.ERROR;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum.FINISHED;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum.IN_PROGRESS;
import java.time.LocalDateTime;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.CreateVnfRequest;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.CreateVnfResponse;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.Tenant;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.aai.AaiServiceProvider;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.vnfm.Sol003AdapterServiceProvider;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoNfInst;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoNsInst;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.State;
import org.onap.so.etsi.nfvo.ns.lcm.database.service.DatabaseServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 */
@Component
public class CreateInstantiateVnfTask extends AbstractNetworkServiceTask {
    private static final Logger logger = LoggerFactory.getLogger(CreateInstantiateVnfTask.class);
    private static final String NF_INST_ID_PARAM_NAME = "NF_INST_ID";
    public static final String CREATE_VNF_RESPONSE_PARAM_NAME = "createVnfResponse";
    private final AaiServiceProvider aaiServiceProvider;
    private final Sol003AdapterServiceProvider sol003AdapterServiceProvider;

    @Autowired
    public CreateInstantiateVnfTask(final DatabaseServiceProvider databaseServiceProvider,
            final AaiServiceProvider aaiServiceProvider,
            final Sol003AdapterServiceProvider sol003AdapterServiceProvider) {
        super(databaseServiceProvider);
        this.aaiServiceProvider = aaiServiceProvider;
        this.sol003AdapterServiceProvider = sol003AdapterServiceProvider;
    }

    public void checkIfNfInstanceExistsInDb(final DelegateExecution execution) {
        logger.info("Executing checkIfNfInstanceInDb");
        final CreateInstantiateRequest request = (CreateInstantiateRequest) execution.getVariable("request");
        logger.info("request: {}", request);

        setJobStatus(execution, JobStatusEnum.IN_PROGRESS, "Checking if NF Instance record exists in database for "
                + request.getNsInstId() + " and " + request.getVnfName());
        if (!databaseServiceProvider.isNsInstExists(request.getNsInstId(), request.getVnfName())) {
            abortOperation(execution,
                    request.getNsInstId() + " " + request.getVnfName() + " VNF instance already exists");
        }
        logger.info("Finished executing checkIfNfInstanceInDb  ...");

    }

    public void createNfInstanceInDb(final DelegateExecution execution) {
        logger.info("Executing createNfInstanceInDb");
        final CreateInstantiateRequest request = (CreateInstantiateRequest) execution.getVariable("request");
        logger.info("request: {}", request);

        setJobStatus(execution, IN_PROGRESS, "Creating NF Instance record in database for " + request.getVnfName());

        final Optional<NfvoNsInst> optional = databaseServiceProvider.getNfvoNsInst(request.getNsInstId());

        if (optional.isEmpty()) {
            abortOperation(execution, "NfvoNsInst: " + request.getNsInstId() + "not founda");
        }

        final NfvoNsInst nfvoNsInst = optional.get();
        final LocalDateTime now = LocalDateTime.now();
        final NfvoNfInst nfvoNfInst = new NfvoNfInst().status(State.NOT_INSTANTIATED).createTime(now)
                .lastUpdateTime(now).name(request.getVnfName()).vnfdId(request.getVnfdId())
                .packageId(request.getVnfPkgId()).nfvoNsInst(nfvoNsInst);
        databaseServiceProvider.saveNfvoNfInst(nfvoNfInst);
        execution.setVariable(NF_INST_ID_PARAM_NAME, nfvoNfInst.getNfInstId());
        logger.info("Finished executing createNfInstanceInDb  ...");

    }

    public void createGenericVnfInAai(final DelegateExecution execution) {
        logger.info("Executing createGenericVnfInAai");
        try {
            final CreateInstantiateRequest request = (CreateInstantiateRequest) execution.getVariable("request");

            setJobStatus(execution, IN_PROGRESS, "Creating GenericVnf record in AAI for " + request.getVnfName());

            final String nfInstId = (String) execution.getVariable(NF_INST_ID_PARAM_NAME);
            final String nsInstId = (String) execution.getVariable(NS_INSTANCE_ID_PARAM_NAME);
            final NfvoNsInst nfvoNsInst = getNfvoNsInst(execution, nsInstId);

            final GenericVnf genericVnf = new GenericVnf();
            genericVnf.setVnfId(nfInstId);
            genericVnf.setVnfName(request.getVnfName());
            genericVnf.setVnfType(nfvoNsInst.getName() + "/" + request.getVnfName());
            genericVnf.setServiceId(request.getNsInstId());
            genericVnf.setNfType(request.getNfType());
            genericVnf.setOperationalStatus("Created");
            genericVnf.setIsClosedLoopDisabled(false);

            aaiServiceProvider.createGenericVnfAndConnectServiceInstance(nsInstId, nfInstId, genericVnf);

            final Tenant tenant = request.getTenant();
            aaiServiceProvider.connectGenericVnfToTenant(nfInstId, tenant.getCloudOwner(), tenant.getRegionName(),
                    tenant.getTenantId());

        } catch (final Exception exception) {
            final String message = "Unable to Create GenericVnf in AAI";
            logger.error(message, exception);
            abortOperation(execution, message);
        }

        logger.info("Finished executing createGenericVnfInAai  ...");
    }

    public void invokeCreateInstantiationRequest(final DelegateExecution execution) {
        logger.info("Executing invokeCreateInstantiationRequest");

        try {
            final CreateInstantiateRequest request = (CreateInstantiateRequest) execution.getVariable("request");
            logger.info("request: {}", request);

            setJobStatus(execution, IN_PROGRESS,
                    "Invoking SOL003 adapter for creating and instantiating VNF: " + request.getVnfName());

            final String nsInstId = (String) execution.getVariable(NS_INSTANCE_ID_PARAM_NAME);
            final NfvoNsInst nfvoNsInst = getNfvoNsInst(execution, nsInstId);
            final CreateVnfRequest vnfRequest = new CreateVnfRequest();
            vnfRequest.setTenant(request.getTenant());
            vnfRequest.setName(nfvoNsInst.getName() + "." + request.getVnfName());
            vnfRequest.setPkgId(request.getVnfPkgId());
            vnfRequest.setAdditionalParams(request.getAdditionalParams());
            final String vnfId = (String) execution.getVariable(NF_INST_ID_PARAM_NAME);
            final Optional<CreateVnfResponse> optional =
                    sol003AdapterServiceProvider.invokeCreateInstantiationRequest(vnfId, vnfRequest);

            if (!optional.isPresent()) {
                final String errorMessage = "Unexpected error while processing create and instantiation request";
                logger.error(errorMessage);
                abortOperation(execution, errorMessage);
            }

            final CreateVnfResponse vnfResponse = optional.get();

            logger.info("Vnf instantiation response: {}", vnfResponse);
            execution.setVariable(CREATE_VNF_RESPONSE_PARAM_NAME, vnfResponse);
            setJobStatus(execution, IN_PROGRESS, "Successfully invoked SOL003 adapater creating and instantiating VNF: "
                    + request.getVnfName() + " CreaetVnfResponse Job Id: " + vnfResponse.getJobId());
            logger.debug("Finished executing invokeCreateInstantiationRequest ...");
        } catch (final Exception exception) {
            final String message = "Unable to invoke create and instantiation request";
            logger.error(message, exception);
            abortOperation(execution, message);
        }

    }

    public void updateNfInstanceStatusToInstantiated(final DelegateExecution execution) {
        logger.info("Executing updateNfInstanceStatusToInstantiated");

        updateNfInstanceStatus(execution, State.INSTANTIATED);
        final CreateInstantiateRequest request = (CreateInstantiateRequest) execution.getVariable("request");
        setJobStatus(execution, FINISHED, "Successfully created and Instantiated VNF: " + request.getVnfName()
                + " will set status to " + State.INSTANTIATED);

        logger.info("Finished executing updateNfInstanceStatusToInstantiated  ...");

    }

    public void updateNfInstanceStatusToFailed(final DelegateExecution execution) {
        logger.info("Executing updateNfInstanceStatusToActive");

        updateNfInstanceStatus(execution, State.FAILED);
        final CreateInstantiateRequest request = (CreateInstantiateRequest) execution.getVariable("request");
        setJobStatus(execution, ERROR, "Failed to create and instantiate VNF: " + request.getVnfName()
                + " will set status to " + State.FAILED);

        logger.info("Finished executing updateNfInstanceStatusToInstantiated  ...");

    }

    private void updateNfInstanceStatus(final DelegateExecution execution, final State vnfStatus) {
        final String nfInstId = (String) execution.getVariable(NF_INST_ID_PARAM_NAME);

        final Optional<NfvoNfInst> optional = databaseServiceProvider.getNfvoNfInst(nfInstId);
        if (!optional.isPresent()) {
            final String message = "Unable to find NfvoNfInst record in database using nfInstId: " + nfInstId;
            logger.error(message);

            abortOperation(execution, message);

        }

        final NfvoNfInst nfvoNfInst = optional.get();
        nfvoNfInst.setStatus(vnfStatus);
        databaseServiceProvider.saveNfvoNfInst(nfvoNfInst);
    }

    private NfvoNsInst getNfvoNsInst(final DelegateExecution execution, final String nsInstId) {
        logger.info("Getting NfvoNsInst to update with nsInstId: {}", nsInstId);
        final Optional<NfvoNsInst> optionalNfvoNsInst = databaseServiceProvider.getNfvoNsInst(nsInstId);

        if (!optionalNfvoNsInst.isPresent()) {
            final String message = "Unable to find NS Instance in datababse using id: " + nsInstId;
            abortOperation(execution, message);
        }

        return optionalNfvoNsInst.get();
    }

}
