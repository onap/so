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

import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.CREATE_NS_REQUEST_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.CREATE_NS_RESPONSE_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.CREATE_NS_WORKFLOW_PROCESSING_EXCEPTION_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.GLOBAL_CUSTOMER_ID_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.NS_INSTANCE_ID_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.NS_PACKAGE_MODEL_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.SERVICE_TYPE_PARAM_NAME;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.so.adapters.etsisol003adapter.pkgm.extclients.etsicatalog.model.NsdInfo;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.exceptions.EtsiCatalogManagerRequestFailureException;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.aai.AaiServiceProvider;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.etsicatalog.EtsiCatalogPackageManagementServiceProvider;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoNsInst;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.State;
import org.onap.so.etsi.nfvo.ns.lcm.database.service.DatabaseServiceProvider;
import org.onap.so.etsi.nfvo.ns.lcm.model.CreateNsRequest;
import org.onap.so.etsi.nfvo.ns.lcm.model.InlineResponse400;
import org.onap.so.etsi.nfvo.ns.lcm.model.NsInstancesNsInstance;
import org.onap.so.etsi.nfvo.ns.lcm.model.NsInstancesNsInstance.NsStateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Component
public class CreateNsTask extends AbstractNetworkServiceTask {
    private static final String NETWORK_SERVICE_NAME = "NetworkService";
    private static final String NETWORK_SERVICE_ROLE = "NetworkService";
    private static final String DOES_NS_PACKAGE_EXISTS_PARAM_NAME = "doesNsPackageExists";
    private static final String DOES_NS_INSTANCE_EXISTS_PARAM_NAME = "doesNsInstanceExists";
    private static final Logger logger = LoggerFactory.getLogger(CreateNsTask.class);
    private final EtsiCatalogPackageManagementServiceProvider etsiCatalogPackageManagementServiceProvider;
    private final AaiServiceProvider aaiServiceProvider;

    @Autowired
    public CreateNsTask(final DatabaseServiceProvider databaseServiceProvider,
            final AaiServiceProvider aaiServiceProvider,
            final EtsiCatalogPackageManagementServiceProvider etsiCatalogPackageManagementServiceProvider) {
        super(databaseServiceProvider);
        this.aaiServiceProvider = aaiServiceProvider;
        this.etsiCatalogPackageManagementServiceProvider = etsiCatalogPackageManagementServiceProvider;
    }

    public void setJobStatusToStarted(final DelegateExecution execution) {
        setJobStatus(execution, JobStatusEnum.STARTED, "Create NS workflow process started");
    }

    public void setJobStatusToFinished(final DelegateExecution execution) {
        setJobStatus(execution, JobStatusEnum.FINISHED, "Create NS workflow process finished");
    }

    public void setJobStatusToError(final DelegateExecution execution) {
        setJobStatusToError(execution, "Create NS workflow process failed");
    }

    public void getNsPackage(final DelegateExecution execution) {
        logger.info("Retrieving NS package from ETSI Catalog Manager ...");
        setJobStatus(execution, JobStatusEnum.IN_PROGRESS, "Retrieving NS package from ETSI Catalog Manager");

        final CreateNsRequest createNsRequest = (CreateNsRequest) execution.getVariable(CREATE_NS_REQUEST_PARAM_NAME);

        try {
            final Optional<NsdInfo> optional =
                    etsiCatalogPackageManagementServiceProvider.getNSPackageModel(createNsRequest.getNsdId());

            if (optional.isPresent()) {
                final NsdInfo packageModel = optional.get();
                logger.info("NS Package exists {}", packageModel);
                execution.setVariable(NS_PACKAGE_MODEL_PARAM_NAME, packageModel);
                execution.setVariable(DOES_NS_PACKAGE_EXISTS_PARAM_NAME, true);
            } else {
                final String message = "Unable to find NS package using NsdId: " + createNsRequest.getNsdId();
                logger.error(message);
                execution.setVariable(DOES_NS_PACKAGE_EXISTS_PARAM_NAME, false);
                execution.setVariable(CREATE_NS_WORKFLOW_PROCESSING_EXCEPTION_PARAM_NAME,
                        new InlineResponse400().detail(message));
            }

        } catch (final EtsiCatalogManagerRequestFailureException failureException) {
            final String message =
                    "Unexpected exception occured while getting ns package using nsdId: " + createNsRequest.getNsdId();
            logger.error(message, failureException);

            execution.setVariable(DOES_NS_PACKAGE_EXISTS_PARAM_NAME, false);

            execution.setVariable(CREATE_NS_WORKFLOW_PROCESSING_EXCEPTION_PARAM_NAME,
                    new InlineResponse400().title(message).detail(message));
        }

    }

    public void doesNsInstanceExistsInDb(final DelegateExecution execution) {
        logger.info("Executing doesNsInstanceExistsInDb  ...");

        setJobStatus(execution, JobStatusEnum.IN_PROGRESS, "Checking if NS package exists in database");

        final CreateNsRequest createNsRequest =
                (CreateNsRequest) execution.getVariables().get(CREATE_NS_REQUEST_PARAM_NAME);

        final boolean exists = databaseServiceProvider.isNsInstExists(createNsRequest.getNsName());
        logger.info("Ns Instance entry {} exists in database", exists ? "does" : "doesn't");
        execution.setVariable(DOES_NS_INSTANCE_EXISTS_PARAM_NAME, exists);

        if (exists) {
            final Optional<NfvoNsInst> optional =
                    databaseServiceProvider.getNfvoNsInstByName(createNsRequest.getNsName());
            final NfvoNsInst nfvoNsInst = optional.get();
            execution.setVariable(CREATE_NS_WORKFLOW_PROCESSING_EXCEPTION_PARAM_NAME, new InlineResponse400()
                    .detail("Ns Instance already exists in database : " + nfvoNsInst.toString()));
        }

        logger.info("Finished executing doesNsInstanceExistsInDb  ...");

    }

    public void createNsInstanceInDb(final DelegateExecution execution) {
        logger.info("Executing createNsInstanceInDb  ...");

        setJobStatus(execution, JobStatusEnum.IN_PROGRESS, "Checking if NS package exists");

        final CreateNsRequest createNsRequest = (CreateNsRequest) execution.getVariable(CREATE_NS_REQUEST_PARAM_NAME);
        final NsdInfo packageMode = (NsdInfo) execution.getVariable(NS_PACKAGE_MODEL_PARAM_NAME);

        final String globalCustomerId = (String) execution.getVariable(GLOBAL_CUSTOMER_ID_PARAM_NAME);
        final String serviceType = (String) execution.getVariable(SERVICE_TYPE_PARAM_NAME);

        final String nsInstId = UUID.randomUUID().toString();
        execution.setVariable(NS_INSTANCE_ID_PARAM_NAME, nsInstId);

        databaseServiceProvider.saveNfvoNsInst(new NfvoNsInst().nsInstId(nsInstId).name(createNsRequest.getNsName())
                .nsPackageId(packageMode.getId()).nsdId(packageMode.getNsdId())
                .nsdInvariantId(packageMode.getNsdInvariantId()).description(createNsRequest.getNsDescription())
                .status(State.NOT_INSTANTIATED).statusUpdatedTime(LocalDateTime.now())
                .globalCustomerId(globalCustomerId).serviceType(serviceType));
        logger.info("Finished executing createNsInstanceInDb  ...");

    }


    public void createNsInstanceInAai(final DelegateExecution execution) {
        logger.info("Executing createNsInstanceInAai  ...");
        try {
            setJobStatus(execution, JobStatusEnum.IN_PROGRESS, "Creating NS Instance in AAI");

            final CreateNsRequest createNsRequest =
                    (CreateNsRequest) execution.getVariable(CREATE_NS_REQUEST_PARAM_NAME);
            final String nsInstId = (String) execution.getVariable(NS_INSTANCE_ID_PARAM_NAME);

            final String globalCustomerId = (String) execution.getVariable(GLOBAL_CUSTOMER_ID_PARAM_NAME);
            final String serviceType = (String) execution.getVariable(SERVICE_TYPE_PARAM_NAME);

            final ServiceInstance aaiServiceInstance = new ServiceInstance();
            aaiServiceInstance.setServiceInstanceId(nsInstId);
            aaiServiceInstance.setServiceInstanceName(createNsRequest.getNsName());
            aaiServiceInstance.setServiceType(NETWORK_SERVICE_NAME);
            aaiServiceInstance.setServiceRole(NETWORK_SERVICE_ROLE);

            aaiServiceProvider.createServiceInstance(globalCustomerId, serviceType, aaiServiceInstance);
        } catch (final Exception exception) {
            final String message = "Unable to Create Service Instance in AAI";
            logger.error(message, exception);
            abortOperation(execution, message, new InlineResponse400().detail(message));
        }
        logger.info("Finished executing createNsInstanceInAai  ...");

    }

    public void setCreateNsResponse(final DelegateExecution execution) {
        logger.info("Executing setCreateNsResponse  ...");
        final String nsInstId = (String) execution.getVariable(NS_INSTANCE_ID_PARAM_NAME);
        final Optional<NfvoNsInst> optional = databaseServiceProvider.getNfvoNsInst(nsInstId);

        if (optional.isPresent()) {
            final NfvoNsInst nfvoNsInst = optional.get();
            final NsInstancesNsInstance response = new NsInstancesNsInstance().id(nfvoNsInst.getNsInstId())
                    .nsInstanceName(nfvoNsInst.getName()).nsdId(nfvoNsInst.getNsdId())
                    .nsdInfoId(nfvoNsInst.getNsPackageId()).nsInstanceDescription(nfvoNsInst.getDescription())
                    .nsState(NsStateEnum.fromValue(nfvoNsInst.getStatus().toString()));
            logger.info("Saving CreateNsResponse: {} in Execution ...", response);
            execution.setVariable(CREATE_NS_RESPONSE_PARAM_NAME, response);
        } else {
            final String message = "Unable to find NS Instance in datababse using id: " + nsInstId;
            logger.error(message);
            abortOperation(execution, message);
        }

        logger.info("Finished executing setCreateNsResponse  ...");

    }

}
