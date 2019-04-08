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

package org.onap.so.asdc.client.test.emulators;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class JsonStatusDataTest {

    @Test
    public void instantiateNotifFromJsonFileTest() {
        JsonStatusData jsonStatusData = new JsonStatusData();
        JsonStatusData returnedVal = null;
        try {
            returnedVal = jsonStatusData.instantiateNotifFromJsonFile(jsonStatusData.getArtifactURL());
        } catch (Exception ex) {

        }
        assertEquals(returnedVal, null);
    }

    @Test
    public void setGetAttributes() {
        JsonStatusData jsonStatusData = new JsonStatusData();
        jsonStatusData.setAttribute("test", "test");
        jsonStatusData.getStatus();
        jsonStatusData.getTimestamp();
        jsonStatusData.getComponentName();
        jsonStatusData.getConsumerID();
        jsonStatusData.getDistributionID();
        String errReason = jsonStatusData.getErrorReason();
        assertEquals(errReason, "MSO FAILURE");

    }



}
