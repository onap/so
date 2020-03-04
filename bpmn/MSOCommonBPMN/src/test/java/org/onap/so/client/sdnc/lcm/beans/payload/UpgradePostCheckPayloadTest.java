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

public class UpgradePostCheckPayloadTest extends LcmBasePayloadTest {
    private static String expectedUpgradePostCheckPayload = "{" + "\"ipaddress-v4-oam\":\"192.168.1.10\","
            + "\"playbook-name\":\"test_playbook\"," + "\"oldSwVersion\":\"v1\"," + "\"targetSwVersion\":\"v2\","
            + "\"ruleName\":\"r102\"," + "\"additionalData\":\"{}\"" + "}";

    public UpgradePostCheckPayload buildUpgradePostCheckPayload() {
        UpgradePostCheckPayload upgradePostCheckPayload = new UpgradePostCheckPayload();

        upgradePostCheckPayload.setIpaddressV4Oam(ipaddressV4Oam);
        upgradePostCheckPayload.setPlaybookName(playbookName);
        upgradePostCheckPayload.setOldSwVersion("v1");
        upgradePostCheckPayload.setTargetSwVersion("v2");
        upgradePostCheckPayload.setRuleName("r102");
        upgradePostCheckPayload.setAdditionalData("{}");

        return upgradePostCheckPayload;
    }

    @Test
    public final void testUpgradePostCheckPayload() {
        UpgradePostCheckPayload upgradePostCheckPayload = buildUpgradePostCheckPayload();

        try {
            String upgradePostCheckPayloadString = convertToSting(upgradePostCheckPayload);
            assertEquals(expectedUpgradePostCheckPayload, upgradePostCheckPayloadString);
        } catch (Exception e) {
            fail("Convert UpgradePostCheckPayload to String error: " + e.toString());
        }
    }
}
