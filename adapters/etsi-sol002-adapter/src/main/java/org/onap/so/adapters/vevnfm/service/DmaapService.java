/*-
 * ============LICENSE_START=======================================================
 * SO
 * ================================================================================
 * Copyright (C) 2020 Samsung. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.so.adapters.vevnfm.service;

import org.onap.so.adapters.etsisol003adapter.lcm.lcn.model.VnfLcmOperationOccurrenceNotification;
import org.onap.so.adapters.vevnfm.configuration.ConfigProperties;
import org.onap.so.adapters.vevnfm.event.DmaapEvent;
import org.onap.so.rest.service.HttpRestServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class DmaapService {

    private static final Logger logger = LoggerFactory.getLogger(DmaapService.class);

    private final String endpoint;
    private final String topic;
    private final String closedLoopControlName;
    private final String version;
    private final HttpRestServiceProvider restProvider;

    @Autowired
    public DmaapService(final ConfigProperties configProperties, final HttpRestServiceProvider restProvider) {
        this.endpoint = configProperties.getDmaapEndpoint();
        this.topic = configProperties.getDmaapTopic();
        this.closedLoopControlName = configProperties.getDmaapClosedLoopControlName();
        this.version = configProperties.getDmaapVersion();
        this.restProvider = restProvider;
    }

    public DmaapEvent createDmaapEvent(final VnfLcmOperationOccurrenceNotification notification,
            final String vserverName, final String genericId) {
        return new DmaapEvent(closedLoopControlName, version, notification, vserverName, genericId);
    }

    public void send(final DmaapEvent event) {
        try {
            final ResponseEntity<String> response = restProvider.postHttpRequest(event, getUrl(), String.class);
            final HttpStatus statusCode = response.getStatusCode();
            final String body = response.getBody();

            logger.info("The DMaaP replied with the code {} and the body {}", statusCode, body);
        } catch (Exception e) {
            logger.warn("An issue connecting to DMaaP", e);
        }
    }

    private String getUrl() {
        return endpoint + topic;
    }
}
