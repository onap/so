/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.asdc.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.asdc.client.test.emulators.NotificationDataImpl;

public class NotificationLoggingTest {
    private NotificationDataImpl notificationData;

    @Before
    public void before() {
        notificationData = new NotificationDataImpl();
    }

    @Test
    public void logNotificationTest() {
        notificationData.setDistributionID("distributionID");
        notificationData.setServiceVersion("123456");
        notificationData.setServiceUUID("serviceUUID");
        notificationData.setWorkloadContext("workloadContext");

        String response = NotificationLogging.logNotification(notificationData);

        assertTrue(response.contains("ASDC Notification"));
        assertTrue(response.contains("ResourcesType not recognized"));
        assertTrue(response.contains("ServiceNameNULL"));
        assertTrue(response.contains("ServiceUUIDserviceUUID"));
        assertTrue(response.contains("ResourcesImplNULL"));
        assertTrue(response.contains("ServiceArtifactsType not recognized"));
        assertTrue(response.contains("ServiceDescriptionNULL"));
        assertTrue(response.contains("DistributionIDdistributionID"));
        assertTrue(response.contains("ServiceInvariantUUIDNULL"));
        assertTrue(response.contains("WorkloadContextworkloadContext"));
    }

    @Test
    public void logNotificationNullTest() {
        notificationData = null;

        String response = NotificationLogging.logNotification(notificationData);

        assertEquals("NULL", response);
    }
}
