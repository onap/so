/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 TechMahindra
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

package org.openecomp.mso.adapters.sdncrest;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

public class RequestInformationTest {

    private RequestInformation requestInformation;

    @Before
    public void setUp() {
        requestInformation = new RequestInformation();
    }

    @Test
    public void testGetRequestId() {
        requestInformation.setRequestId("requestId");
        Assert.assertNotNull(requestInformation.getRequestId());
        Assert.assertEquals(requestInformation.getRequestId(), "requestId");
    }

    @Test
    public void testGetSource() {
        requestInformation.setSource("source");
        Assert.assertNotNull(requestInformation.getSource());
        Assert.assertEquals(requestInformation.getSource(), "source");
    }

    @Test
    public void testGetNotificationUrl() {
        requestInformation.setNotificationUrl("notificationUrl");
        Assert.assertNotNull(requestInformation.getNotificationUrl());
        Assert.assertEquals(requestInformation.getNotificationUrl(), "notificationUrl");
    }
}
