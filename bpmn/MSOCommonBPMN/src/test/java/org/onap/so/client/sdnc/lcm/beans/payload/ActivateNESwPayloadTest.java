/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.onap.so.client.sdnc.lcm.beans.payload;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ActivateNESwPayloadTest extends LcmBasePayloadTest {
    private static String expectedActivateNESwPayload = "{" + "\"ipaddress-v4-oam\":\"192.168.1.10\","
            + "\"playbook-name\":\"test_playbook\"," + "\"swVersionToBeActivated\":\"v2\"" + "}";

    public ActivateNESwPayload buildActivateNESwPayload() {
        ActivateNESwPayload activateNESwPayload = new ActivateNESwPayload();

        activateNESwPayload.setIpaddressV4Oam(ipaddressV4Oam);
        activateNESwPayload.setPlaybookName(playbookName);
        activateNESwPayload.setSwVersionToBeActivated("v2");

        return activateNESwPayload;
    }

    public static String getExpectedActivateNESwPayload() {
        return expectedActivateNESwPayload;
    }

    @Test
    public final void testActivateNESwPayload() {
        ActivateNESwPayload activateNESwPayload = buildActivateNESwPayload();

        try {
            String activateNESwPayloadString = convertToSting(activateNESwPayload);
            assertEquals(expectedActivateNESwPayload, activateNESwPayloadString);
        } catch (Exception e) {
            fail("Convert ActivateNESwPayload to String error: " + e.toString());
        }
    }
}
