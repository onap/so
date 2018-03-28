/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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

package org.openecomp.mso.asdc.client.test.emulators;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class JsonNotificationDataTest {


    @Test
    public void setAttributeTest()
    {
        JsonNotificationData jsonNotificationData = new JsonNotificationData();
        jsonNotificationData.setWorkloadContext("test");
        jsonNotificationData.setAttribute("distributionID","test");
        jsonNotificationData.setAttribute("workloadContext","test");
        String getWorkloadContextVal = jsonNotificationData.getWorkloadContext();
        assertEquals("test",getWorkloadContextVal);
    }

    @Test
    public void getAttributeTest()
    {
        JsonNotificationData jsonNotificationData = new JsonNotificationData();
        jsonNotificationData.setAttribute("workloadContext","test");;
        jsonNotificationData.getArtifactMetadataByUUID("test");
        jsonNotificationData.getDistributionID();
        jsonNotificationData.getResources();
        jsonNotificationData.getServiceArtifacts();
        jsonNotificationData.getServiceDescription();
        jsonNotificationData.getServiceInvariantUUID();
        jsonNotificationData.getServiceName();
        jsonNotificationData.getServiceUUID();
        jsonNotificationData.getServiceVersion();
        String getWorkloadContextVal = jsonNotificationData.getWorkloadContext();
        assertEquals("test",getWorkloadContextVal);
    }
}
