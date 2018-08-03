/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
package org.onap.so.montoring.rest.service;

import org.onap.so.montoring.exception.InvalidRestRequestException;
import org.onap.so.montoring.exception.RestProcessingException;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Optional;

/**
 * @author waqas.ikram@ericsson.com
 */
@Service
public class HttpRestServiceProviderImpl implements HttpRestServiceProvider {

    private static final XLogger LOGGER = XLoggerFactory.getXLogger(HttpRestServiceProviderImpl.class);

    private final RestTemplate restTemplate;

    @Autowired
    public HttpRestServiceProviderImpl(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public <T> Optional<T> getHttpResponse(final String url, final Class<T> clazz) {
        LOGGER.trace("Will invoke HTTP GET using URL: {}", url);
        try {
            final ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, null, clazz);
            if (!response.getStatusCode().equals(HttpStatus.OK)) {
                LOGGER.error("Unable to invoke HTTP GET using URL: {}, Response Code: {}", url,
                        response.getStatusCode());
                return Optional.absent();
            }

            if (response.hasBody()) {
                return Optional.of(response.getBody());
            }
        } catch (final HttpClientErrorException httpClientErrorException) {
            LOGGER.error("Unable to invoke HTTP GET using url: {}, Response: {}", url,
                    httpClientErrorException.getRawStatusCode(), httpClientErrorException);
            final int rawStatusCode = httpClientErrorException.getRawStatusCode();
            if (rawStatusCode == HttpStatus.BAD_REQUEST.value() || rawStatusCode == HttpStatus.NOT_FOUND.value()) {
                throw new InvalidRestRequestException("No result found for given url: " + url);
            }
            throw new RestProcessingException("Unable to invoke HTTP GET using URL: " + url);

        } catch (final RestClientException restClientException) {
            LOGGER.error("Unable to invoke HTTP GET using url: {}", url, restClientException);
            throw new RestProcessingException("Unable to invoke HTTP GET using URL: " + url, restClientException);
        }

        return Optional.absent();
    }


}
