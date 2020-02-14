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

import org.onap.aai.domain.yang.EsrSystemInfo;
import org.onap.so.adapters.vevnfm.aai.AaiConnection;
import org.onap.so.adapters.vevnfm.exception.VeVnfmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@EnableRetry
public class StartupService {

    private static final Logger logger = LoggerFactory.getLogger(StartupService.class);

    @Value("${vnfm.default-endpoint}")
    private String vnfmDefaultEndpoint;

    @Autowired
    private AaiConnection aaiConnection;

    @Autowired
    private SubscriberService subscriberService;

    @Retryable(value = {VeVnfmException.class}, maxAttempts = 5, backoff = @Backoff(delay = 5000, multiplier = 10))
    public EsrSystemInfo receiveVnfm() throws VeVnfmException {
        return aaiConnection.receiveVnfm();
    }

    @Recover
    public EsrSystemInfo recoverReceiveVnfm(final Throwable e) {
        logger.warn("Connection to AAI failed");
        final EsrSystemInfo info = new EsrSystemInfo();
        info.setServiceUrl(vnfmDefaultEndpoint);
        logger.warn("This EsrSystemInfo is used by default: {}", info);

        return info;
    }

    @Retryable(value = {VeVnfmException.class}, maxAttempts = 5, backoff = @Backoff(delay = 5000, multiplier = 10))
    public void subscribe(final EsrSystemInfo info) throws VeVnfmException {
        final boolean done = subscriberService.subscribe(info);

        if (!done) {
            throw new VeVnfmException("Could not subscribe to VNFM");
        }
    }

    @Recover
    public void recoverSubscribe(final Throwable e, final EsrSystemInfo info) {
        logger.warn("Subscription to VNFM at this endpoint {} failed", info.getServiceUrl());
    }
}
