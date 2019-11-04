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

package org.onap.so.adapters.vnfmadapter.extclients.vnfm;

import com.google.common.base.Optional;
import org.onap.aai.domain.yang.EsrVnfm;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.CreateVnfRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse200;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse2001;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InlineResponse201.InstantiationStateEnum;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.InstantiateVnfRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.LccnSubscriptionRequest;
import org.onap.so.adapters.vnfmadapter.extclients.vnfm.model.TerminateVnfRequest;
import org.onap.so.adapters.vnfmadapter.jobmanagement.JobManager;
import org.onap.so.adapters.vnfmadapter.rest.exceptions.VnfmRequestFailureException;
import org.onap.so.rest.exceptions.RestProcessingException;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class VnfmServiceProviderImpl implements VnfmServiceProvider {
    private static final Logger logger = LoggerFactory.getLogger(VnfmServiceProviderImpl.class);

    private final VnfmServiceProviderConfiguration vnfmServiceProviderConfiguration;
    private final VnfmUrlProvider urlProvider;

    @Autowired
    public VnfmServiceProviderImpl(final VnfmUrlProvider urlProvider,
            VnfmServiceProviderConfiguration vnfmServiceProviderConfiguration) {
        this.vnfmServiceProviderConfiguration = vnfmServiceProviderConfiguration;
        this.urlProvider = urlProvider;
    }

    @Override
    public Optional<InlineResponse201> getVnf(final EsrVnfm vnfm, final String vnfSelfLink) {
        return getHttpServiceProvider(vnfm).get(vnfSelfLink, InlineResponse201.class);
    }

    @Override
    public String instantiateVnf(final EsrVnfm vnfm, final String vnfSelfLink,
            final InstantiateVnfRequest instantiateVnfRequest) {
        logger.debug("Sending instantiate request " + instantiateVnfRequest + " to : " + vnfSelfLink);

        ResponseEntity<Void> response = null;
        try {
            response = getHttpServiceProvider(vnfm).postHttpRequest(instantiateVnfRequest, vnfSelfLink + "/instantiate",
                    Void.class);
        } catch (final Exception exception) {
            final String errorMessage =
                    "Instantiate request to " + vnfSelfLink + " resulted in exception" + instantiateVnfRequest;
            logger.error(errorMessage, exception);
            throw new VnfmRequestFailureException(errorMessage, exception);
        }
        if (response.getStatusCode() != HttpStatus.ACCEPTED) {
            final String errorMessage = "Instantiate request to " + vnfSelfLink + " returned status code: "
                    + response.getStatusCode() + ", request: " + instantiateVnfRequest;
            logger.error(errorMessage);
            throw new VnfmRequestFailureException(errorMessage);
        }
        final String locationHeader = response.getHeaders().get("Location").iterator().next();
        return locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
    }

    @Override
    public InlineResponse2001 subscribeForNotifications(final EsrVnfm vnfm,
            final LccnSubscriptionRequest subscriptionRequest) {
        logger.info("Subscribing for notifications {}", subscriptionRequest);
        final String url = urlProvider.getSubscriptionsUrl(vnfm.getVnfmId());
        ResponseEntity<InlineResponse2001> response = null;
        try {
            response = getHttpServiceProvider(vnfm).postHttpRequest(subscriptionRequest, url, InlineResponse2001.class);
            logger.info("Subscribing for notifications response {}", response);
        } catch (final Exception exception) {
            final String errorMessage =
                    "Subscription to VNFM " + vnfm.getVnfmId() + " resulted in exception" + subscriptionRequest;
            logger.error(errorMessage, exception);
            throw new VnfmRequestFailureException(errorMessage, exception);
        }
        if (response.getStatusCode() != HttpStatus.CREATED) {
            final String errorMessage = "Subscription to VNFM " + vnfm.getVnfmId() + " returned status code: "
                    + response.getStatusCode() + ", request: " + subscriptionRequest;
            logger.error(errorMessage);
            throw new VnfmRequestFailureException(errorMessage);
        }
        return response.getBody();
    }

    @Override
    public String terminateVnf(final EsrVnfm vnfm, final String vnfSelfLink,
            final TerminateVnfRequest terminateVnfRequest) {
        logger.debug("Sending terminate request " + terminateVnfRequest + " to : " + vnfSelfLink);

        ResponseEntity<Void> response = null;
        try {
            response = getHttpServiceProvider(vnfm).postHttpRequest(terminateVnfRequest, vnfSelfLink + "/terminate",
                    Void.class);
        } catch (final RestProcessingException restProcessingException) {
            if (restProcessingException.getStatusCode() == HttpStatus.CONFLICT.value()) {
                InlineResponse201 vnf = getVnf(vnfm, vnfSelfLink).get();
                if (vnf.getInstantiationState().equals(InstantiationStateEnum.NOT_INSTANTIATED)) {
                    return JobManager.ALREADY_COMPLETED_OPERATION_ID;
                } else {
                    final String errorMessage =
                            "Terminate request to " + vnfSelfLink + " resulted in exception" + terminateVnfRequest;
                    logger.error(errorMessage, restProcessingException);
                    throw new VnfmRequestFailureException(errorMessage, restProcessingException);
                }
            }
        } catch (final Exception exception) {
            final String errorMessage =
                    "Terminate request to " + vnfSelfLink + " resulted in exception" + terminateVnfRequest;
            logger.error(errorMessage, exception);
            throw new VnfmRequestFailureException(errorMessage, exception);
        }
        checkIfResponseIsAcceptable(response, vnfSelfLink, terminateVnfRequest);
        final String locationHeader = response.getHeaders().get("Location").iterator().next();
        return locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
    }

    @Override
    public void deleteVnf(final EsrVnfm vnfm, final String vnfSelfLink) {
        logger.debug("Sending delete request to : " + vnfSelfLink);
        final ResponseEntity<Void> response = getHttpServiceProvider(vnfm).deleteHttpRequest(vnfSelfLink, Void.class);
        if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
            throw new VnfmRequestFailureException(
                    "Delete request to " + vnfSelfLink + " return status code: " + response.getStatusCode());
        }
    }

    @Override
    public Optional<InlineResponse200> getOperation(final EsrVnfm vnfm, final String operationId) {
        final String url = urlProvider.getOperationUrl(vnfm.getVnfmId(), operationId);
        return getHttpServiceProvider(vnfm).get(url, InlineResponse200.class);
    }

    @Override
    public Optional<InlineResponse201> createVnf(final EsrVnfm vnfm, final CreateVnfRequest createVnfRequest) {
        final String url = urlProvider.getCreationUrl(vnfm.getVnfmId());
        logger.debug("Sending create request {} to : {}", createVnfRequest, url);
        try {
            return getHttpServiceProvider(vnfm).post(createVnfRequest, url, InlineResponse201.class);
        } catch (final Exception exception) {
            final String errorMessage =
                    "Create request to vnfm:" + vnfm.getVnfmId() + " resulted in exception" + createVnfRequest;
            logger.error(errorMessage, exception);
            throw new VnfmRequestFailureException(errorMessage, exception);
        }
    }

    private void checkIfResponseIsAcceptable(ResponseEntity<Void> response, String vnfSelfLink,
            TerminateVnfRequest terminateVnfRequest) {
        if (response == null) {
            final String errorMessage =
                    "Terminate request to " + vnfSelfLink + ", response is null, " + "request: " + terminateVnfRequest;
            logger.error(errorMessage);
            throw new VnfmRequestFailureException(errorMessage);
        }
        if (response.getStatusCode() != HttpStatus.ACCEPTED) {
            final String errorMessage = "Terminate request to " + vnfSelfLink + ", returned status code: "
                    + response.getStatusCode() + ", request: " + terminateVnfRequest;
            logger.error(errorMessage);
            throw new VnfmRequestFailureException(errorMessage);
        }
    }

    private HttpRestServiceProvider getHttpServiceProvider(final EsrVnfm vnfm) {
        return vnfmServiceProviderConfiguration.getHttpRestServiceProvider(vnfm);
    }

}
