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

package org.onap.so.adapters.etsisol003adapter.lcm.lifecycle;

import java.util.Map;
import org.onap.aai.domain.yang.EsrVnfm;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.Relationship;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.SdcPackageProvider;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.aai.AaiHelper;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.aai.AaiServiceProvider;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.aai.OamIpAddressSource;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.aai.OamIpAddressSource.OamIpAddressType;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.VnfmHelper;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.VnfmServiceProvider;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.InlineResponse201;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.InstantiateVnfRequest;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.LccnSubscriptionRequest;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.TerminateVnfRequest;
import org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.TerminateVnfRequest.TerminationTypeEnum;
import org.onap.so.adapters.etsisol003adapter.lcm.jobmanagement.JobManager;
import org.onap.so.adapters.etsisol003adapter.lcm.rest.exceptions.VnfNotFoundException;
import org.onap.so.adapters.etsisol003adapter.lcm.rest.exceptions.VnfmNotFoundException;
import org.onap.so.adapters.etsisol003adapter.lcm.rest.exceptions.VnfmRequestFailureException;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.CreateVnfRequest;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.CreateVnfResponse;
import org.onap.so.adapters.etsisol003adapter.lcm.v1.model.DeleteVnfResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.common.base.Optional;

/**
 * Manages lifecycle operations towards the VNFMs.
 */
@Component
public class LifecycleManager {
    private static final Logger logger = LoggerFactory.getLogger(LifecycleManager.class);
    private final AaiServiceProvider aaiServiceProvider;
    private final VnfmServiceProvider vnfmServiceProvider;
    private final AaiHelper aaiHelper;
    private final VnfmHelper vnfmHelper;
    private final JobManager jobManager;
    private final SdcPackageProvider packageProvider;

    @Autowired
    LifecycleManager(final AaiServiceProvider aaiServiceProvider, final AaiHelper aaiHelper,
            final VnfmHelper vnfmHelper, final VnfmServiceProvider vnfmServiceProvider, final JobManager jobManager,
            final SdcPackageProvider packageProvider) {
        this.aaiServiceProvider = aaiServiceProvider;
        this.vnfmServiceProvider = vnfmServiceProvider;
        this.aaiHelper = aaiHelper;
        this.vnfmHelper = vnfmHelper;
        this.jobManager = jobManager;
        this.packageProvider = packageProvider;
    }

    /**
     * Create a VNF on a VNFM.
     *
     * @param vnfIdInAai the ID of the VNF in AAI
     * @param request the create request
     * @return the response to the request
     */
    public CreateVnfResponse createVnf(final String vnfIdInAai, final CreateVnfRequest request) {
        final GenericVnf genericVnf = getGenericVnfFromAai(vnfIdInAai);
        EsrVnfm vnfm = aaiHelper.getAssignedVnfm(genericVnf);
        checkIfVnfAlreadyExistsInVnfm(vnfm, genericVnf);

        if (vnfm == null) {
            vnfm = aaiHelper.selectVnfm(genericVnf);
            aaiServiceProvider.invokePutGenericVnfToVnfmRelationship(genericVnf, vnfm.getVnfmId());
        }
        final InlineResponse201 vnfmResponse = sendCreateRequestToVnfm(request, genericVnf, vnfIdInAai, vnfm);

        logger.info("Create response: {}", vnfmResponse);

        genericVnf.setSelflink(getSelfLink(vnfmResponse, vnfm));

        final GenericVnf genericVnfPatch = new GenericVnf();
        genericVnfPatch.setVnfId(genericVnf.getVnfId());
        genericVnfPatch.setSelflink(genericVnf.getSelflink());
        aaiServiceProvider.invokePatchGenericVnf(genericVnfPatch);

        final String vnfIdInVnfm = vnfmResponse.getId();

        final OamIpAddressSource oamIpAddressSource = extractOamIpAddressSource(request);
        aaiHelper.setOamIpAddressSource(vnfIdInVnfm, oamIpAddressSource);

        createNotificationSubscription(vnfm, vnfIdInVnfm);
        final String operationId = sendInstantiateRequestToVnfm(vnfm, genericVnf, request);

        final String jobId = jobManager.createJob(vnfm.getVnfmId(), operationId, false);
        final CreateVnfResponse response = new CreateVnfResponse();
        response.setJobId(jobId);
        return response;
    }

    private String getSelfLink(final InlineResponse201 vnfmResponse, final EsrVnfm vnfm) {
        if (vnfmResponse.getLinks() != null && vnfmResponse.getLinks().getSelf() != null
                && vnfmResponse.getLinks().getSelf().getHref() != null) {
            return vnfmResponse.getLinks().getSelf().getHref();
        }
        return vnfm.getEsrSystemInfoList().getEsrSystemInfo().iterator().next().getServiceUrl() + "/vnf_instances/"
                + vnfmResponse.getId();
    }

    private OamIpAddressSource extractOamIpAddressSource(final CreateVnfRequest request) {
        final Map<String, String> additionalParams = request.getAdditionalParams();
        try {
            final String sourceType = additionalParams.remove("oamIpAddressSourceType");
            final String sourceValue = additionalParams.remove("oamIpAddressSourceValue");
            final OamIpAddressType oamIpAddressType = OamIpAddressType.valueOf(sourceType.toUpperCase());
            return new OamIpAddressSource(oamIpAddressType, sourceValue);
        } catch (final NullPointerException | IllegalArgumentException exception) {
            logger.debug("Additional Params not set for OAM IP address source", exception);
            return null;
        }
    }

    private void checkIfVnfAlreadyExistsInVnfm(final EsrVnfm vnfm, final GenericVnf genericVnf) {
        if (genericVnf.getSelflink() != null && !genericVnf.getSelflink().isEmpty() && vnfm != null) {
            Optional<InlineResponse201> response = Optional.absent();
            try {
                response = vnfmServiceProvider.getVnf(vnfm, genericVnf.getSelflink());
            } catch (final Exception exception) {
                logger.debug("Ignoring invalid self link in generic vnf", exception);
            }
            if (response.isPresent()) {
                throw new IllegalArgumentException("VNF " + genericVnf.getVnfId()
                        + " is already defined on the VNFM, self link: " + genericVnf.getSelflink());
            }
        }
    }

    private InlineResponse201 sendCreateRequestToVnfm(final CreateVnfRequest aaiRequest, final GenericVnf genericVnf,
            final String vnfIdInAai, final EsrVnfm vnfm) {
        logger.debug("Sending a create request to SVNFM " + aaiRequest);
        final org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.CreateVnfRequest vnfmRequest =
                new org.onap.so.adapters.etsisol003adapter.lcm.extclients.vnfm.model.CreateVnfRequest();

        final String vnfdId = packageProvider.getVnfdId(genericVnf.getModelVersionId());
        vnfmRequest.setVnfdId(vnfdId);
        vnfmRequest.setVnfInstanceName(aaiRequest.getName().replaceAll(" ", "_"));
        vnfmRequest.setVnfInstanceDescription(vnfIdInAai);

        final Optional<InlineResponse201> optionalResponse = vnfmServiceProvider.createVnf(vnfm, vnfmRequest);

        try {
            return optionalResponse.get();
        } catch (final Exception exception) {
            final String errorMessage = "Unable to return response from VNFM";
            logger.error(errorMessage, exception);
            throw new VnfmRequestFailureException(errorMessage, exception);
        }
    }

    private void createNotificationSubscription(final EsrVnfm vnfm, final String vnfId) {
        try {
            final LccnSubscriptionRequest subscriptionRequest = vnfmHelper.createNotificationSubscriptionRequest(vnfId);
            vnfmServiceProvider.subscribeForNotifications(vnfm, subscriptionRequest);
        } catch (final Exception exception) {
            logger.warn("Subscription for notifications to VNFM: " + vnfm.getVnfmId() + " for VNF " + vnfId
                    + " failed. AAI will not be updated unless the VNFM is configured by other means to send notifications relating to this VNF",
                    exception);
        }
    }

    private String sendInstantiateRequestToVnfm(final EsrVnfm vnfm, final GenericVnf genericVnf,
            final CreateVnfRequest createVnfRequest) {

        final InstantiateVnfRequest instantiateVnfRequest =
                vnfmHelper.createInstantiateRequest(createVnfRequest.getTenant(), createVnfRequest,
                        packageProvider.getFlavourId(genericVnf.getModelVersionId()));
        final String jobId = vnfmServiceProvider.instantiateVnf(vnfm, genericVnf.getSelflink(), instantiateVnfRequest);

        logger.info("Instantiate VNF request successfully sent to " + genericVnf.getSelflink());
        return jobId;
    }

    /**
     * Delete a VNF on a VNFM.
     *
     * @param vnfIdInAai the ID of the VNF in AAI
     * @return the response to the request
     */
    public DeleteVnfResponse deleteVnf(final String vnfIdInAai) {
        final GenericVnf genericVnf = getGenericVnfFromAai(vnfIdInAai);
        final EsrVnfm vnfm = getAssignedVnfm(genericVnf);

        final String operationId = sendTerminateRequestToVnfm(vnfm, genericVnf);

        if (operationId.equals(JobManager.ALREADY_COMPLETED_OPERATION_ID)) {
            sendDeleteRequestToVnfm(genericVnf);
        }
        final String jobId = jobManager.createJob(vnfm.getVnfmId(), operationId, true);

        return new DeleteVnfResponse().jobId(jobId);
    }

    private String sendTerminateRequestToVnfm(final EsrVnfm vnfm, final GenericVnf genericVnf) {
        final TerminateVnfRequest terminateVnfRequest = new TerminateVnfRequest();
        terminateVnfRequest.setTerminationType(TerminationTypeEnum.FORCEFUL);
        return vnfmServiceProvider.terminateVnf(vnfm, genericVnf.getSelflink(), terminateVnfRequest);
    }

    private GenericVnf getGenericVnfFromAai(final String vnfIdInAai) {
        final GenericVnf genericVnf = aaiServiceProvider.invokeGetGenericVnf(vnfIdInAai);
        if (genericVnf == null) {
            throw new VnfNotFoundException("VNF not found in AAI: " + vnfIdInAai);
        }
        logger.debug("Retrieved generic VNF from AAI: " + genericVnf);
        return genericVnf;
    }

    private EsrVnfm getAssignedVnfm(final GenericVnf genericVnf) {
        final EsrVnfm vnfm = aaiHelper.getAssignedVnfm(genericVnf);
        if (vnfm == null) {
            throw new VnfmNotFoundException("No VNFM found in AAI for VNF " + genericVnf.getVnfId());
        }
        return vnfm;
    }

    private void sendDeleteRequestToVnfm(final GenericVnf genericVnf) {

        vnfmServiceProvider.deleteVnf(aaiHelper.getAssignedVnfm(genericVnf), genericVnf.getSelflink());

        final GenericVnf genericVnfPatch = new GenericVnf();
        genericVnfPatch.setVnfId(genericVnf.getVnfId());
        genericVnfPatch.setOrchestrationStatus("Assigned");
        genericVnfPatch.setSelflink("");
        aaiServiceProvider.invokePatchGenericVnf(genericVnfPatch);

        for (final Relationship relationship : genericVnf.getRelationshipList().getRelationship()) {
            if (relationship.getRelatedTo().equals("vserver")) {
                aaiServiceProvider.invokeDeleteVserver(
                        aaiHelper.getRelationshipData(relationship, "cloud-region.cloud-owner"),
                        aaiHelper.getRelationshipData(relationship, "cloud-region.cloud-region-id"),
                        aaiHelper.getRelationshipData(relationship, "tenant.tenant-id"),
                        aaiHelper.getRelationshipData(relationship, "vserver.vserver-id"));
            }
        }


    }
}
