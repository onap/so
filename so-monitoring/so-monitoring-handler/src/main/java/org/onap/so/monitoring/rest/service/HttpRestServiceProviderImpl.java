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
package org.onap.so.monitoring.rest.service;

import org.onap.so.monitoring.exception.InvalidRestRequestException;
import org.onap.so.monitoring.exception.RestProcessingException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author waqas.ikram@ericsson.com
 */
public class HttpRestServiceProviderImpl implements HttpRestServiceProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRestServiceProviderImpl.class);
    private final RestTemplate restTemplate;

    public HttpRestServiceProviderImpl(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public <T> Optional<T> getHttpResponse(final String url, final Class<T> clazz) {
        LOGGER.trace("Will invoke HTTP GET using URL: " + url);
        try {
            final ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, null, clazz);
            if (!response.getStatusCode().equals(HttpStatus.OK)) {
                final String message = "Unable to invoke HTTP GET using URL: " + url + 
                    ", Response Code: " + response.getStatusCode();
                LOGGER.error(message);
                return Optional.absent();
            }

            if (response.hasBody()) {
                return Optional.of(response.getBody());
            }
        } catch (final HttpClientErrorException httpClientErrorException) {
            final String message = "Unable to invoke HTTP GET using url: " + url + ", Response: " +
                    httpClientErrorException.getRawStatusCode();
            LOGGER.error(message, httpClientErrorException);
            final int rawStatusCode = httpClientErrorException.getRawStatusCode();
            if (rawStatusCode == HttpStatus.BAD_REQUEST.value() || rawStatusCode == HttpStatus.NOT_FOUND.value()) {
                throw new InvalidRestRequestException("No result found for given url: " + url);
            }
            throw new RestProcessingException("Unable to invoke HTTP GET using URL: " + url);

        } catch (final RestClientException restClientException) {
            LOGGER.error("Unable to invoke HTTP GET using url: " + url, restClientException);
            throw new RestProcessingException("Unable to invoke HTTP GET using URL: " + 
                                              url, restClientException);
        }

        return Optional.absent();
    }

    @Override
    public <T> Optional<T> postHttpRequest(final Object object, final String url, final Class<T> clazz) {
        try {
            final HttpEntity<?> request = new HttpEntity<>(object);
            final ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, request, clazz);
            if (!response.getStatusCode().equals(HttpStatus.OK)) {
                final String message = "Unable to invoke HTTP GET using URL: " + url + 
                    ", Response Code: " + response.getStatusCode();
                LOGGER.error(message);
                return Optional.absent();
            }

            if (response.hasBody()) {
                return Optional.of(response.getBody());
            }

        } catch (final HttpClientErrorException httpClientErrorException) {
            final String message = "Unable to invoke HTTP POST using url: " + url + 
                ", Response: " + httpClientErrorException.getRawStatusCode();
            LOGGER.error(message, httpClientErrorException);
            final int rawStatusCode = httpClientErrorException.getRawStatusCode();
            if (rawStatusCode == HttpStatus.BAD_REQUEST.value() || rawStatusCode == HttpStatus.NOT_FOUND.value()) {
                throw new InvalidRestRequestException("No result found for given url: " + url);
            }
            throw new RestProcessingException("Unable to invoke HTTP POST using URL: " + url);

        } catch (final RestClientException restClientException) {
            LOGGER.error("Unable to invoke HTTP POST using url: " + url, restClientException);
            throw new RestProcessingException("Unable to invoke HTTP POST using URL: " 
                                              + url, restClientException);
        }

        return Optional.absent();
    }


}
