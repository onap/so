/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Ericsson. All rights reserved.
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
package org.onap.so.asdc.etsi.pkg.processor;

import static org.onap.so.asdc.etsi.pkg.processor.HttpRestServiceProviderConfiguration.SDC_HTTP_REST_SERVICE_PROVIDER_BEAN;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import java.security.GeneralSecurityException;
import java.util.Optional;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@Service
public class SdcResourceProvider {
    private final static Logger LOGGER = LoggerFactory.getLogger(SdcResourceProvider.class);

    private static final String SERVICE_NAME = "SO-SDC-CONTROLLER";

    private final HttpRestServiceProvider httpRestServiceProvider;

    private final SdcBasicHttpConfigurationProvider sdcBasicHttpConfigurationProvider;

    @Autowired
    public SdcResourceProvider(
            @Qualifier(SDC_HTTP_REST_SERVICE_PROVIDER_BEAN) final HttpRestServiceProvider httpRestServiceProvider,
            final SdcBasicHttpConfigurationProvider sdcBasicHttpConfigurationProvider) {
        this.httpRestServiceProvider = httpRestServiceProvider;
        this.sdcBasicHttpConfigurationProvider = sdcBasicHttpConfigurationProvider;
    }

    public Optional<byte[]> getVnfResource(final String resourceId) {
        LOGGER.debug("Will get resource from SDC using resource id: {}", resourceId);
        try {
            final HttpHeaders headers = getHttpHeaders();
            headers.add(ACCEPT, APPLICATION_OCTET_STREAM_VALUE);
            final String url = getSdcResourceEndPoint(resourceId);
            LOGGER.debug("will invoke url: {} to get resource ", url);
            final ResponseEntity<byte[]> responseEntity =
                    httpRestServiceProvider.getHttpResponse(url, headers, byte[].class);

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                if (responseEntity.hasBody()) {
                    return Optional.of(responseEntity.getBody());
                }
                LOGGER.error("Received response without body");
            }
            LOGGER.error("Unexpected Status code received : {}", responseEntity.getStatusCode());
            return Optional.empty();
        } catch (final Exception exception) {
            LOGGER.error("Unable to get {} resource from SDC", resourceId, exception);
            return Optional.empty();
        }
    }

    private String getSdcResourceEndPoint(final String resourceId) {
        return sdcBasicHttpConfigurationProvider.getEndPoint() + "/sdc/v1/catalog/resources/" + resourceId
                + "/toscaModel";
    }


    private HttpHeaders getHttpHeaders() throws GeneralSecurityException {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, sdcBasicHttpConfigurationProvider.getBasicAuthorization());
        headers.add("X-ECOMP-InstanceID", SERVICE_NAME);
        headers.add("X-FromAppId", SERVICE_NAME);
        return headers;
    }
}
