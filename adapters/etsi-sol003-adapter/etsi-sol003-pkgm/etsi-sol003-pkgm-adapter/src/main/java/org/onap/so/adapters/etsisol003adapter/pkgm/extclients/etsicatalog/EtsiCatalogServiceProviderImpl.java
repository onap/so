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

package org.onap.so.adapters.etsisol003adapter.pkgm.extclients.etsicatalog;

import static org.onap.so.adapters.etsisol003adapter.pkgm.extclients.etsicatalog.EtsiCatalogServiceProviderConfiguration.ETSI_CATALOG_SERVICE_PROVIDER_BEAN;
import java.util.Optional;
import org.onap.so.adapters.etsisol003adapter.pkgm.extclients.etsicatalog.model.NsdmSubscription;
import org.onap.so.adapters.etsisol003adapter.pkgm.extclients.etsicatalog.model.PkgmSubscription;
import org.onap.so.adapters.etsisol003adapter.pkgm.extclients.etsicatalog.model.VnfPkgInfo;
import org.onap.so.adapters.etsisol003adapter.pkgm.model.InlineResponse2001;
import org.onap.so.adapters.etsisol003adapter.pkgm.rest.exceptions.EtsiCatalogManagerBadRequestException;
import org.onap.so.adapters.etsisol003adapter.pkgm.rest.exceptions.EtsiCatalogManagerRequestFailureException;
import org.onap.so.adapters.etsisol003adapter.pkgm.rest.exceptions.SubscriptionNotFoundException;
import org.onap.so.adapters.etsisol003adapter.pkgm.rest.exceptions.VnfPkgBadRequestException;
import org.onap.so.adapters.etsisol003adapter.pkgm.rest.exceptions.VnfPkgConflictException;
import org.onap.so.adapters.etsisol003adapter.pkgm.rest.exceptions.VnfPkgNotFoundException;
import org.onap.so.rest.exceptions.HttpResouceNotFoundException;
import org.onap.so.rest.exceptions.InvalidRestRequestException;
import org.onap.so.rest.exceptions.RestProcessingException;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Provides the implementations of the REST Requests to the ETSI Catalog Manager.
 * 
 * @author gareth.roper@est.tech
 */
@Service
public class EtsiCatalogServiceProviderImpl implements EtsiCatalogServiceProvider {
    private static final Logger logger = LoggerFactory.getLogger(EtsiCatalogServiceProviderImpl.class);

    private final HttpRestServiceProvider httpServiceProvider;
    private final EtsiCatalogUrlProvider etsiCatalogUrlProvider;
    private final ConversionService conversionService;

    @Autowired
    public EtsiCatalogServiceProviderImpl(final EtsiCatalogUrlProvider etsiCatalogUrlProvider,
            @Qualifier(ETSI_CATALOG_SERVICE_PROVIDER_BEAN) final HttpRestServiceProvider httpServiceProvider,
            final ConversionService conversionService) {
        this.etsiCatalogUrlProvider = etsiCatalogUrlProvider;
        this.httpServiceProvider = httpServiceProvider;
        this.conversionService = conversionService;
    }

    @Override
    public Optional<byte[]> getVnfPackageContent(final String vnfPkgId)
            throws EtsiCatalogManagerRequestFailureException {
        final String vnfRequestUrl = etsiCatalogUrlProvider.getVnfPackageContentUrl(vnfPkgId);
        final String vnfRequestName = "getVnfPackageContent";
        return requestVnfElement(vnfPkgId, vnfRequestUrl, vnfRequestName);
    }

    @Override
    public Optional<byte[]> getVnfPackageArtifact(final String vnfPkgId, final String artifactPath) {
        try {
            final ResponseEntity<byte[]> response = httpServiceProvider.getHttpResponse(
                    etsiCatalogUrlProvider.getVnfPackageArtifactUrl(vnfPkgId, artifactPath), byte[].class);
            logger.info("getVnfPackageArtifact Request to ETSI Catalog Manager Status Code: {}",
                    response.getStatusCodeValue());
            if (response.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(response.getBody());
            }
        } catch (final HttpResouceNotFoundException httpResouceNotFoundException) {
            logger.error("Caught HttpResouceNotFoundException", httpResouceNotFoundException);
            throw new VnfPkgNotFoundException("No Vnf Package Artifact found with vnfPkgId: \"" + vnfPkgId
                    + "\" and artifactPath: \"" + artifactPath + "\".");
        } catch (final RestProcessingException restProcessingException) {
            logger.error("Caught RestProcessingException with Status Code: {}", restProcessingException.getStatusCode(),
                    restProcessingException);
            if (restProcessingException.getStatusCode() == HttpStatus.CONFLICT.value()) {
                throw new VnfPkgConflictException("A conflict occurred with the state of the resource,\n"
                        + "due to the attribute: onboardingState not being set to ONBOARDED.");
            }
        }
        throw new EtsiCatalogManagerRequestFailureException("Internal Server Error Occurred.");
    }

    @Override
    public Optional<InlineResponse2001[]> getVnfPackages() {
        try {
            final ResponseEntity<VnfPkgInfo[]> response =
                    httpServiceProvider.getHttpResponse(etsiCatalogUrlProvider.getVnfPackagesUrl(), VnfPkgInfo[].class);
            logger.info("getVnfPackages Request to ETSI Catalog Manager Status Code: {}",
                    response.getStatusCodeValue());
            if (response.getStatusCode().is2xxSuccessful()) {
                if (response.hasBody()) {
                    final VnfPkgInfo[] vnfPackages = response.getBody();
                    assert (vnfPackages != null);
                    final InlineResponse2001[] responses = new InlineResponse2001[vnfPackages.length];
                    for (int index = 0; index < vnfPackages.length; index++) {
                        if (conversionService.canConvert(vnfPackages[index].getClass(), InlineResponse2001.class)) {
                            final InlineResponse2001 inlineResponse2001 =
                                    conversionService.convert(vnfPackages[index], InlineResponse2001.class);
                            if (inlineResponse2001 != null) {
                                responses[index] = inlineResponse2001;
                            }
                        } else {
                            logger.error("Unable to find Converter for response class: {}",
                                    vnfPackages[index].getClass());
                        }
                    }
                    return Optional.of(responses);
                }
                logger.error("Received response without body ...");
            }
            logger.error("Unexpected status code received {}", response.getStatusCode());
            return Optional.empty();
        } catch (final InvalidRestRequestException invalidRestRequestException) {
            logger.error("Caught InvalidRestRequestException", invalidRestRequestException);
            throw new VnfPkgBadRequestException("Error: Bad Request Received");
        } catch (final HttpResouceNotFoundException httpResouceNotFoundException) {
            logger.error("Caught HttpResouceNotFoundException", httpResouceNotFoundException);
            throw new VnfPkgNotFoundException("No Vnf Packages found");
        } catch (final RestProcessingException restProcessingException) {
            logger.error("Caught RestProcessingException with Status Code: {}", restProcessingException.getStatusCode(),
                    restProcessingException);
            throw new EtsiCatalogManagerRequestFailureException("Internal Server Error Occurred.");
        }
    }

    @Override
    public Optional<InlineResponse2001> getVnfPackage(final String vnfPkgId) {
        try {
            final ResponseEntity<VnfPkgInfo> response = httpServiceProvider
                    .getHttpResponse(etsiCatalogUrlProvider.getVnfPackageUrl(vnfPkgId), VnfPkgInfo.class);
            logger.info("getVnfPackage Request for vnfPkgId {} to ETSI Catalog Manager Status Code: {}", vnfPkgId,
                    response.getStatusCodeValue());
            if (response.getStatusCode().is2xxSuccessful()) {
                if (response.hasBody()) {
                    final VnfPkgInfo vnfPkgInfo = response.getBody();
                    if (conversionService.canConvert(vnfPkgInfo.getClass(), InlineResponse2001.class)) {
                        return Optional.ofNullable(conversionService.convert(vnfPkgInfo, InlineResponse2001.class));
                    }
                    logger.error("Unable to find Converter for response class: {}", vnfPkgInfo.getClass());
                }
                logger.error("Received response without body ....");
            }
            return Optional.empty();
        } catch (final InvalidRestRequestException invalidRestRequestException) {
            logger.error("Caught InvalidRestRequestException", invalidRestRequestException);
            throw new VnfPkgBadRequestException("Error: Bad Request Received");
        } catch (final HttpResouceNotFoundException httpResouceNotFoundException) {
            logger.error("Caught HttpResouceNotFoundException", httpResouceNotFoundException);
            throw new VnfPkgNotFoundException("No Vnf Package found with vnfPkgId: " + vnfPkgId);
        } catch (final RestProcessingException restProcessingException) {
            logger.error("Caught RestProcessingException with Status Code: {}", restProcessingException.getStatusCode(),
                    restProcessingException);
            throw new EtsiCatalogManagerRequestFailureException("Internal Server Error Occurred.");
        }
    }

    @Override
    public Optional<byte[]> getVnfPackageVnfd(final String vnfPkgId) {
        final String vnfRequestUrl = etsiCatalogUrlProvider.getVnfPackageVnfdUrl(vnfPkgId);
        final String vnfRequestName = "getVnfPackageVnfd";
        return requestVnfElement(vnfPkgId, vnfRequestUrl, vnfRequestName);
    }

    @Override
    public Optional<PkgmSubscription> postSubscription(
            final org.onap.so.adapters.etsisol003adapter.pkgm.extclients.etsicatalog.model.PkgmSubscriptionRequest etsiCatalogManagerSubscriptionRequest) {
        try {
            final ResponseEntity<PkgmSubscription> responseEntity =
                    httpServiceProvider.postHttpRequest(etsiCatalogManagerSubscriptionRequest,
                            etsiCatalogUrlProvider.getSubscriptionUrl(), PkgmSubscription.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                if (responseEntity.hasBody()) {
                    return Optional.of(responseEntity.getBody());
                }
                logger.error("Received response without body on postSubscription");
            }
            logger.error("Unexpected Status Code Received on postSubscription: {}", responseEntity.getStatusCode());
            return Optional.empty();
        } catch (final InvalidRestRequestException invalidRestRequestException) {
            logger.error("Caught InvalidRestRequestException", invalidRestRequestException);
            throw new EtsiCatalogManagerBadRequestException(
                    "Bad Request Received on postSubscription call to ETSI Catalog Manager.");
        } catch (final RestProcessingException restProcessingException) {
            logger.error("Caught RestProcessingException with Status Code: {}", restProcessingException.getStatusCode(),
                    restProcessingException);
            throw new EtsiCatalogManagerRequestFailureException(
                    "Internal Server Error Occurred. On postSubscription with StatusCode: "
                            + restProcessingException.getStatusCode());
        }
    }

    @Override
    public boolean deleteSubscription(final String subscriptionId) {
        try {
            final ResponseEntity<Void> responseEntity = httpServiceProvider
                    .deleteHttpRequest(etsiCatalogUrlProvider.getSubscriptionUrl() + "/" + subscriptionId, Void.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                logger.info("Subscription with ID: {} has been successfully deleted from the ETSI Catalog Manager",
                        subscriptionId);
                return true;
            }
            logger.error("Unexpected Status Code Received on deleteSubscription: {}", responseEntity.getStatusCode());
            return false;
        } catch (final HttpResouceNotFoundException resouceNotFoundException) {
            final String message = "Unable to find subscription in ETSI Catalog Manager using id: " + subscriptionId;
            logger.error(message);
            throw new SubscriptionNotFoundException(message);
        } catch (final InvalidRestRequestException invalidRestRequestException) {
            logger.error("Caught InvalidRestRequestException on deleteSubscription call to ETSI Catalog Manager.",
                    invalidRestRequestException);
            throw new EtsiCatalogManagerBadRequestException(
                    "Bad Request Received on deleteSubscription call to ETSI Catalog Manager.");
        }
    }

    @Override
    public Optional<NsdmSubscription> getSubscription(final String subscriptionId) {
        try {
            final ResponseEntity<NsdmSubscription> responseEntity = httpServiceProvider.getHttpResponse(
                    etsiCatalogUrlProvider.getSubscriptionUrl() + "/" + subscriptionId, NsdmSubscription.class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                logger.debug("Found subscription with ID: {} in ETSI Catalog Manager", subscriptionId);
                return Optional.ofNullable(responseEntity.getBody());
            }
            logger.error("Unexpected Status Code Received on getting subscription from ETSI Catalog Manager: {}",
                    responseEntity.getStatusCode());
        } catch (final HttpResouceNotFoundException resouceNotFoundException) {
            logger.error("Unable to find subscription in ETSI Catalog Manager using id: {}", subscriptionId);
            return Optional.empty();
        } catch (final RestProcessingException | InvalidRestRequestException exception) {
            logger.error("Unable to query ETSI Catalog Manager for subscription using id: {}", subscriptionId,
                    exception);
        }
        throw new EtsiCatalogManagerRequestFailureException("Internal Server Error Occurred.");
    }

    private Optional<byte[]> requestVnfElement(final String vnfPkgId, final String vnfRequestUrl,
            final String vnfRequestName) {
        try {
            final ResponseEntity<byte[]> response = httpServiceProvider.getHttpResponse(vnfRequestUrl, byte[].class);
            logger.info("{} Request to ETSI Catalog Manager Status Code: {}", vnfRequestName,
                    response.getStatusCodeValue());
            if (response.getStatusCode() == HttpStatus.OK) {
                return Optional.ofNullable(response.getBody());
            }
        } catch (final HttpResouceNotFoundException httpResouceNotFoundException) {
            logger.error("Caught HttpResouceNotFoundException", httpResouceNotFoundException);
            throw new VnfPkgNotFoundException("No Vnf Package found with vnfPkgId: " + vnfPkgId);
        } catch (final RestProcessingException restProcessingException) {
            logger.error("Caught RestProcessingException with Status Code: {}", restProcessingException.getStatusCode(),
                    restProcessingException);
            if (restProcessingException.getStatusCode() == HttpStatus.CONFLICT.value()) {
                throw new VnfPkgConflictException("A conflict occurred with the state of the resource,\n"
                        + "due to the attribute: onboardingState not being set to ONBOARDED.");
            }
        }
        throw new EtsiCatalogManagerRequestFailureException("Internal Server Error Occurred.");
    }
}
