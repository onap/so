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

import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.INSTANTIATE_NS_REQUEST_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.NETWORK_SERVICE_DESCRIPTOR_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.NS_INSTANCE_ID_PARAM_NAME;
import static org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.CamundaVariableNameConstants.VNF_CREATE_INSTANTIATE_REQUESTS;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum.FINISHED;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum.IN_PROGRESS;
import static org.onap.so.etsi.nfvo.ns.lcm.database.beans.JobStatusEnum.STARTED;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.Tenant;
import org.onap.so.adapters.etsisol003adapter.pkgm.extclients.etsicatalog.model.NsdInfo;
import org.onap.so.adapters.etsisol003adapter.pkgm.extclients.etsicatalog.model.VnfPkgInfo;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.exceptions.EtsiCatalogManagerRequestFailureException;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.extclients.etsicatalog.EtsiCatalogPackageManagementServiceProvider;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.nsd.NetworkServiceDescriptor;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.nsd.VirtualNetworkFunction;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoNfInst;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoNsInst;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.State;
import org.onap.so.etsi.nfvo.ns.lcm.database.service.DatabaseServiceProvider;
import org.onap.so.etsi.nfvo.ns.lcm.model.InstantiateNsRequest;
import org.onap.so.etsi.nfvo.ns.lcm.model.NsInstancesnsInstanceIdinstantiateAdditionalParamsForVnf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 * @author Andrew Lamb (andrew.a.lamb@est.tech)
 *
 */
@Component
public class InstantiateNsTask extends AbstractNetworkServiceTask {

    private static final String IS_NS_INSTANTIATION_SUCCESSFUL_PARAM_NAME = "isNsInstantiationSuccessful";
    private static final String VNFD_ID_TO_VNF_PKG_ID_MAPPING_PARAM_NAME = "vnfdIdToVnfPkgIdMapping";
    private static final Logger logger = LoggerFactory.getLogger(InstantiateNsTask.class);
    private final EtsiCatalogPackageManagementServiceProvider etsiCatalogPackageManagementServiceProvider;

    @Autowired
    public InstantiateNsTask(final DatabaseServiceProvider databaseServiceProvider,
            final EtsiCatalogPackageManagementServiceProvider etsiCatalogPackageManagementServiceProvider) {
        super(databaseServiceProvider);
        this.etsiCatalogPackageManagementServiceProvider = etsiCatalogPackageManagementServiceProvider;

    }

    public void setJobStatusToStarted(final DelegateExecution execution) {
        setJobStatus(execution, STARTED, "Instantiate NS workflow process started");
    }

    public void setJobStatusToFinished(final DelegateExecution execution) {
        setJobStatus(execution, FINISHED, "Instantiate NS workflow process finished");
    }

    public void updateNsInstanceStatusToInstantiating(final DelegateExecution execution) {
        logger.info("Executing updateNsInstanceStatusToInstantiating");
        setJobStatus(execution, IN_PROGRESS, "Updating NfvoNsInst Status to " + State.INSTANTIATING);
        updateNsInstanceStatus(execution, State.INSTANTIATING);

        logger.info("Finished executing updateNsInstanceStatusToInstantiating  ...");
    }


    public void checkIfVnfInstantiationWasSuccessful(final DelegateExecution execution) {
        logger.info("Executing checkIfVnfInstantiationWasSuccessful");

        @SuppressWarnings("unchecked")
        final List<CreateInstantiateRequest> requests =
                (List<CreateInstantiateRequest>) execution.getVariable(VNF_CREATE_INSTANTIATE_REQUESTS);

        final String nsInstId = (String) execution.getVariable(NS_INSTANCE_ID_PARAM_NAME);
        final List<NfvoNfInst> nfInstances = databaseServiceProvider.getNfvoNfInstByNsInstId(nsInstId);

        if (nfInstances == null || nfInstances.isEmpty()) {
            final String message = "Found empty nf instances";
            abortOperation(execution, message);
        }

        if (requests.size() != nfInstances.size()) {
            final String message = "Missing nf Instance. Request triggered: " + requests.size()
                    + " Nf Instace reqeust found in database " + nfInstances.size();
            abortOperation(execution, message);
        }

        execution.setVariable(IS_NS_INSTANTIATION_SUCCESSFUL_PARAM_NAME, true);

        nfInstances.stream().forEach(instance -> {
            if (!State.INSTANTIATED.equals(instance.getStatus())) {
                logger.error("VNF : {} {} instantiation failed", instance.getNfInstId(), instance.getName());
                execution.setVariable(IS_NS_INSTANTIATION_SUCCESSFUL_PARAM_NAME, false);
            }
        });

        logger.info("Finished executing checkIfVnfInstantiationWasSuccessful  ...");
    }

    public void updateNsInstanceStatusToInstantiated(final DelegateExecution execution) {
        logger.info("Executing updateNsInstanceStatusToInstantiated");
        final String nsInstId = (String) execution.getVariable(NS_INSTANCE_ID_PARAM_NAME);
        setJobStatus(execution, FINISHED, "Successfully " + State.INSTANTIATED + " NS: " + nsInstId);

        updateNsInstanceStatus(execution, State.INSTANTIATED);
        logger.info("Finished executing updateNsInstanceStatusToInstantiated  ...");
    }

    public void getAndParseNsdFromEtsiCatalog(final DelegateExecution execution) {
        logger.info("Executing getAndParseNsdFromEtsiCatalog");
        setJobStatus(execution, IN_PROGRESS, "Retrieving and parsing NSD from ETSI Catalog Manager");
        final NfvoNsInst nfvoNsInst = getNfvoNsInst(execution);
        final String nsPackageId = nfvoNsInst.getNsPackageId();

        try {
            final Optional<NetworkServiceDescriptor> optional =
                    etsiCatalogPackageManagementServiceProvider.getNetworkServiceDescriptor(nsPackageId);

            if (optional.isEmpty()) {
                final String message = "Unable to parse NSD " + nsPackageId;
                logger.error(message);
                abortOperation(execution, message);
            }

            final NetworkServiceDescriptor networkServiceDescriptor = optional.get();
            logger.info("Found {}", networkServiceDescriptor);
            execution.setVariable(NETWORK_SERVICE_DESCRIPTOR_PARAM_NAME, networkServiceDescriptor);

        } catch (final EtsiCatalogManagerRequestFailureException failureException) {
            final String message =
                    "Unexpected exception occured while getting nsd content using nsPackageId: " + nsPackageId;
            logger.error(message, failureException);

            abortOperation(execution, message);
        }

        logger.info("Finished executing getAndParseNsdFromEtsiCatalog  ...");

    }

    public void prepareCreateInstantiateRequests(final DelegateExecution execution) {
        logger.info("Executing prepareCreateInstantiateRequests ...");
        setJobStatus(execution, IN_PROGRESS, "Preparing VNF CreateInstantiate requests");
        final NetworkServiceDescriptor networkServiceDescriptor =
                (NetworkServiceDescriptor) execution.getVariable(NETWORK_SERVICE_DESCRIPTOR_PARAM_NAME);
        final String nsInstId = (String) execution.getVariable(NS_INSTANCE_ID_PARAM_NAME);
        final NfvoNsInst nfvoNsInst = getNfvoNsInst(execution, nsInstId);

        final InstantiateNsRequest instantiateNsRequest =
                (InstantiateNsRequest) execution.getVariable(INSTANTIATE_NS_REQUEST_PARAM_NAME);

        final List<NsInstancesnsInstanceIdinstantiateAdditionalParamsForVnf> additionalParamsForVnfList =
                instantiateNsRequest.getAdditionalParamsForVnf();

        @SuppressWarnings("unchecked")
        final Map<String, String> vnfdIdToVnfPkgIdMapping =
                (Map<String, String>) execution.getVariable(VNFD_ID_TO_VNF_PKG_ID_MAPPING_PARAM_NAME);

        final List<CreateInstantiateRequest> requests = new ArrayList<>();

        for (final VirtualNetworkFunction vnf : networkServiceDescriptor.getVnfs()) {
            final List<String> vnfmInfoList = vnf.getVnfmInfoList();

            final String vnfdId = vnf.getVnfdId();
            final String vnfPkgId = vnfdIdToVnfPkgIdMapping.get(vnfdId);
            if (vnfdId == null || vnfmInfoList.isEmpty() || vnfPkgId == null) {
                final String message = "Unable to find VnfdId/vnfmInfo/VnfPkgId for vnf: " + vnf;
                abortOperation(execution, message);
            }

            final Optional<NsInstancesnsInstanceIdinstantiateAdditionalParamsForVnf> additionalParamsForVnfOptional =
                    getAdditionalParamsForVnf(vnfdId, additionalParamsForVnfList);

            if (additionalParamsForVnfOptional.isEmpty()) {
                final String message = "Unable to find AdditionalParamsForVnf for " + vnfdId
                        + " in instantiateNsRequest : " + instantiateNsRequest;
                abortOperation(execution, message);
            }

            final NsInstancesnsInstanceIdinstantiateAdditionalParamsForVnf additionalParamsForVnf =
                    additionalParamsForVnfOptional.get();

            @SuppressWarnings("unchecked")
            final Map<String, String> additionalParams =
                    (Map<String, String>) additionalParamsForVnf.getAdditionalParams();

            final Optional<Tenant> optional = getTenant(additionalParams);
            if (optional.isEmpty()) {
                final String message = "Unable to find Tenant information for " + vnfdId + " in instantiateNsRequest : "
                        + instantiateNsRequest;
                abortOperation(execution, message);
            }

            final String vnfInstanceName = additionalParamsForVnf.getVnfInstanceName();
            final String vnfName = StringUtils.isNotBlank(vnfInstanceName) ? vnfInstanceName
                    : nfvoNsInst.getName() + "." + vnf.getVnfName();

            final CreateInstantiateRequest createInstantiateRequest =
                    new CreateInstantiateRequest().nsInstId(nsInstId).vnfdId(vnfdId).vnfName(vnfName).vnfPkgId(vnfPkgId)
                            .nfType(vnfmInfoList.get(0)).tenant(optional.get()).additionalParams(additionalParams);

            logger.info("Adding request to list: {}", createInstantiateRequest);
            requests.add(createInstantiateRequest);

        }
        execution.setVariable(VNF_CREATE_INSTANTIATE_REQUESTS, requests);

        logger.info("Finished executing prepareCreateInstantiateRequests  ...");
    }

    public void getVnfPkgIdForEachVnfdId(final DelegateExecution execution) {
        logger.info("Executing getVnfPkgIdForEachVnfdId ...");

        setJobStatus(execution, IN_PROGRESS, "Getting VnfPkgId for each VnfdId");

        final String nsInstId = (String) execution.getVariable(NS_INSTANCE_ID_PARAM_NAME);
        final NfvoNsInst nfvoNsInst = getNfvoNsInst(execution, nsInstId);
        final String nsPackageId = nfvoNsInst.getNsPackageId();

        final Map<String, String> vnfdIdToVnfPkgIdMapping = new HashMap<>();
        try {
            final Optional<NsdInfo> nsdInfoOptional =
                    etsiCatalogPackageManagementServiceProvider.getNSPackageModel(nsPackageId);

            if (nsdInfoOptional.isEmpty()) {
                final String message = "Unable to find NS package using NS package id: " + nsPackageId;
                logger.error(message);
                abortOperation(execution, message);
            }

            final NsdInfo nsdInfo = nsdInfoOptional.get();
            for (final String vnfPkgId : nsdInfo.getVnfPkgIds()) {
                final Optional<VnfPkgInfo> optional =
                        etsiCatalogPackageManagementServiceProvider.getVnfPkgInfo(vnfPkgId);

                if (optional.isEmpty()) {
                    final String message = "Unable to find VNF package using NS vnfPkgId: " + vnfPkgId;
                    logger.error(message);
                    abortOperation(execution, message);
                }
                final VnfPkgInfo vnfPkgInfo = optional.get();
                vnfdIdToVnfPkgIdMapping.put(vnfPkgInfo.getVnfdId(), vnfPkgId);

            }
            logger.info("vnfdIdToVnfPkgIdMapping: {}", vnfdIdToVnfPkgIdMapping);
            execution.setVariable(VNFD_ID_TO_VNF_PKG_ID_MAPPING_PARAM_NAME, vnfdIdToVnfPkgIdMapping);

        } catch (final EtsiCatalogManagerRequestFailureException failureException) {
            final String message =
                    "Unexpected exception occured while getting VNF package using nsPackageId: " + nsPackageId;
            logger.error(message, failureException);

            abortOperation(execution, message);
        }

        logger.info("Finished executing getVnfPkgIdForEachVnfdId ...");

    }

    public void logTimeOut(final DelegateExecution execution) {
        logger.error("Vnf instantiation timedOut ...");
        final String nsInstId = (String) execution.getVariable(NS_INSTANCE_ID_PARAM_NAME);
        final List<NfvoNfInst> nfInstances = databaseServiceProvider.getNfvoNfInstByNsInstId(nsInstId);
        if (nfInstances != null) {
            nfInstances.stream().forEach(instance -> {
                logger.info("Current status {} of vnf: {}", instance.getStatus(), instance.getName());
            });
        }
    }

    public void setJobStatusToError(final DelegateExecution execution) {
        updateNsInstanceStatus(execution, State.FAILED);
        setJobStatusToError(execution, "Instantiate NS workflow process failed");
    }

    private Optional<Tenant> getTenant(final Map<String, String> additionalParams) {
        final String vimId = (String) additionalParams.get("vim_id");
        if (vimId != null) {
            final String[] splitString = vimId.split("_");
            if (splitString.length == 3) {
                logger.info("Found Tenant in additionalParams: {}", additionalParams);
                return Optional.of(
                        new Tenant().cloudOwner(splitString[0]).regionName(splitString[1]).tenantId(splitString[2]));
            }

        }
        logger.error("Unable to find Tenant in additionalParams: {}", additionalParams);
        return Optional.empty();
    }

    private Optional<NsInstancesnsInstanceIdinstantiateAdditionalParamsForVnf> getAdditionalParamsForVnf(
            final String vnfdId,
            final List<NsInstancesnsInstanceIdinstantiateAdditionalParamsForVnf> additionalParamsForVnfList) {
        final Optional<NsInstancesnsInstanceIdinstantiateAdditionalParamsForVnf> optional =
                additionalParamsForVnfList.stream().filter(entry -> vnfdId.equals(entry.getVnfProfileId())).findFirst();
        if (optional.isPresent()) {
            logger.info("Found AdditionalParamsForVnf in instantiateNsRequest using vnfdId: {}", vnfdId);

        }
        return optional;
    }

}
