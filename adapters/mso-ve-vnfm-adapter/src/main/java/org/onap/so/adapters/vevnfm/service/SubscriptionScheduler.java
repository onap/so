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
import org.onap.so.adapters.vevnfm.exception.VeVnfmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class SubscriptionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionScheduler.class);

    @Autowired
    private SubscriberService subscriberService;

    private String subscribedId;

    private EsrSystemInfo info;

    public void setInfo(final EsrSystemInfo info) {
        this.info = info;
    }

    @Scheduled(fixedRate = 5000, initialDelay = 2000)
    void subscribeTask() throws VeVnfmException {
        if (info != null) {
            if (subscribedId == null) {
                logger.info("Starting subscribe task");
                subscribedId = subscriberService.subscribe(info);
            }
        }
    }

    @Scheduled(fixedRate = 20000)
    void checkSubscribeTask() throws VeVnfmException {
        if (info != null) {
            if (subscribedId != null) {
                logger.info("Checking subscription: {}", subscribedId);
                if (!subscriberService.checkSubscription(info, subscribedId)) {
                    logger.info("Subscription {} not available", subscribedId);
                    subscribedId = null;
                }
            }
        }
    }
}
