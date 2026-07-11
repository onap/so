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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.sdc.utils.DistributionStatusEnum;
import org.onap.so.asdc.tenantIsolation.DistributionStatus;
import org.onap.so.asdc.tenantIsolation.WatchdogDistribution;

@RunWith(MockitoJUnitRunner.class)
public class WatchdogStatusWaiterTest {

    @Mock
    private WatchdogDistribution wd;
    @Mock
    private ASDCConfiguration asdcConfig;

    @InjectMocks
    private WatchdogStatusWaiter waiter;

    private final AtomicLong fakeNow = new AtomicLong(0);

    @Before
    public void setUp() {
        when(asdcConfig.getWatchDogTimeout()).thenReturn(1); // 1s -> 1000ms
        waiter.setSleeper(millis -> fakeNow.addAndGet(millis)); // sleeping advances the fake clock
        waiter.setTimeSupplier(fakeNow::get);
    }

    @Test
    public void waitForComponents_success_reportsCompleteOk() throws Exception {
        when(wd.getOverallDistributionStatus("dist-1")).thenReturn(DistributionStatus.SUCCESS.name());

        WatchdogStatusResult result = waiter.waitForComponents("dist-1");

        assertTrue(result.isDeploySuccess());
        assertEquals(DistributionStatusEnum.DISTRIBUTION_COMPLETE_OK.name(), result.getOverallStatus());
        assertNull(result.getWatchdogError());
    }

    @Test
    public void waitForComponents_failure_reportsCompleteError() throws Exception {
        when(wd.getOverallDistributionStatus("dist-1")).thenReturn(DistributionStatus.FAILURE.name());

        WatchdogStatusResult result = waiter.waitForComponents("dist-1");

        assertFalse(result.isDeploySuccess());
        assertEquals(DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.name(), result.getOverallStatus());
        assertNull(result.getWatchdogError());
    }

    @Test
    public void waitForComponents_staysIncompleteUntilTimeout_reportsTimeoutError() throws Exception {
        when(wd.getOverallDistributionStatus("dist-1")).thenReturn(DistributionStatus.INCOMPLETE.name());

        WatchdogStatusResult result = waiter.waitForComponents("dist-1");

        assertFalse(result.isDeploySuccess());
        assertEquals(DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.name(), result.getOverallStatus());
        assertEquals("Timeout occurred while waiting for all components to report status", result.getWatchdogError());
    }

    @Test
    public void waitForComponents_nullStatusUntilTimeout_reportsError() throws Exception {
        when(wd.getOverallDistributionStatus("dist-1")).thenReturn(null);

        WatchdogStatusResult result = waiter.waitForComponents("dist-1");

        assertFalse(result.isDeploySuccess());
        assertEquals(DistributionStatusEnum.DISTRIBUTION_COMPLETE_ERROR.name(), result.getOverallStatus());
    }

    @Test
    public void waitForComponents_exceptionThenSuccess_recoversAndReportsOk() throws Exception {
        when(wd.getOverallDistributionStatus("dist-1")).thenThrow(new RuntimeException("transient"))
                .thenReturn(DistributionStatus.SUCCESS.name());

        WatchdogStatusResult result = waiter.waitForComponents("dist-1");

        assertTrue(result.isDeploySuccess());
        assertEquals(DistributionStatusEnum.DISTRIBUTION_COMPLETE_OK.name(), result.getOverallStatus());
    }
}
