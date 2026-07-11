/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.onap.so.asdc.client;

import java.util.function.LongSupplier;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.onap.so.asdc.tenantIsolation.DistributionStatus;
import org.onap.so.asdc.tenantIsolation.WatchdogDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Blocks until all distribution components report a terminal status or the configured watchdog timeout elapses.
 * Extracted from ASDCController; the clock and sleeper are injectable so the timeout/success/error branches can be
 * unit-tested deterministically without real waiting.
 */
@Component
public class WatchdogStatusWaiter {

    private static final Logger logger = LoggerFactory.getLogger(WatchdogStatusWaiter.class);

    @FunctionalInterface
    interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    @Autowired
    private WatchdogDistribution wd;

    @Autowired
    private ASDCConfiguration asdcConfig;

    private Sleeper sleeper = Thread::sleep;
    private LongSupplier timeSupplier = System::currentTimeMillis;

    void setSleeper(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    void setTimeSupplier(LongSupplier timeSupplier) {
        this.timeSupplier = timeSupplier;
    }

    public WatchdogStatusResult waitForComponents(String distributionId) {
        long initialStartTime = timeSupplier.getAsLong();
        boolean componentsComplete = false;
        String distributionStatus = null;
        String watchdogError = null;
        String overallStatus = null;
        boolean isDeploySuccess = false;
        int watchDogTimeout = asdcConfig.getWatchDogTimeout() * 1000;

        while (!componentsComplete && (timeSupplier.getAsLong() - initialStartTime) < watchDogTimeout) {
            try {
                distributionStatus = wd.getOverallDistributionStatus(distributionId);
                sleeper.sleep(watchDogTimeout / 10);
            } catch (Exception e) {
                logger.debug("Exception in Watchdog Loop {}", e.getMessage());
                sleepQuietly(watchDogTimeout / 10);
            }

            if (distributionStatus != null
                    && !distributionStatus.equalsIgnoreCase(DistributionStatus.INCOMPLETE.name())) {
                if (distributionStatus.equalsIgnoreCase(DistributionStatus.SUCCESS.name())) {
                    isDeploySuccess = true;
                    overallStatus = DistributionStatusEnum.DISTRIBUTION_COMPLETE_OK.name();
                } else {
                    overallStatus = DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.name();
                }
                componentsComplete = true;
            }
        }

        if (!componentsComplete) {
            logger.debug("Timeout of {} seconds was reached before all components reported status", watchDogTimeout);
            watchdogError = "Timeout occurred while waiting for all components to report status";
            overallStatus = DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.name();
        }

        if (distributionStatus == null) {
            overallStatus = DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.name();
            logger.debug("DistributionStatus is null for DistributionId: {}", distributionId);
        }

        return new WatchdogStatusResult(overallStatus, watchdogError, isDeploySuccess);
    }

    private void sleepQuietly(long millis) {
        try {
            sleeper.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
