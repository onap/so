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

import java.util.LinkedList;
import java.util.List;
import org.onap.aai.domain.yang.EsrSystemInfo;
import org.onap.so.adapters.vevnfm.aai.EsrId;
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

    private final SubscriberService subscriberService;
    private List<EsrId> esrIds;

    @Autowired
    public SubscriptionScheduler(final SubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }

    public void setInfos(final List<EsrSystemInfo> infos) {
        esrIds = new LinkedList<>();

        for (final EsrSystemInfo info : infos) {
            final EsrId esrId = new EsrId();
            esrId.setInfo(info);
            esrIds.add(esrId);
        }
    }

    List<EsrId> getEsrIds() {
        return esrIds;
    }

    @Scheduled(fixedRate = 5000, initialDelay = 2000)
    void subscribeTask() throws VeVnfmException {
        if (isEsrIdsValid()) {
            for (final EsrId esrId : esrIds) {
                singleSubscribe(esrId);
            }
        }
    }

    @Scheduled(fixedRate = 20000)
    void checkSubscribeTask() throws VeVnfmException {
        if (isEsrIdsValid()) {
            for (final EsrId esrId : esrIds) {
                singleCheckSubscription(esrId);
            }
        }
    }

    private boolean isEsrIdsValid() {
        return esrIds != null && !esrIds.isEmpty();
    }

    private void singleSubscribe(final EsrId esrId) {
        if (esrId.getId() == null) {
            logger.info("Single subscribe task");
            esrId.setId(subscriberService.subscribe(esrId.getInfo()));
        }
    }

    private void singleCheckSubscription(final EsrId esrId) {
        if (esrId.getId() != null) {
            logger.info("Checking subscription: {}", esrId.getId());
            if (!subscriberService.checkSubscription(esrId.getInfo(), esrId.getId())) {
                logger.info("Subscription {} not available", esrId.getId());
                esrId.setId(null);
            }
        }
    }
}
