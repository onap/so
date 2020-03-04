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

public class UpgradePreCheckPayloadTest extends LcmBasePayloadTest {
    private static String expectedUpgradePreCheckPayload = "{" + "\"ipaddress-v4-oam\":\"192.168.1.10\","
            + "\"playbook-name\":\"test_playbook\"," + "\"oldSwVersion\":\"v1\"," + "\"targetSwVersion\":\"v2\","
            + "\"ruleName\":\"r101\"," + "\"additionalData\":\"{}\"" + "}";

    public UpgradePreCheckPayload buildUpgradePreCheckPayload() {
        UpgradePreCheckPayload upgradePreCheckPayload = new UpgradePreCheckPayload();

        upgradePreCheckPayload.setIpaddressV4Oam(ipaddressV4Oam);
        upgradePreCheckPayload.setPlaybookName(playbookName);
        upgradePreCheckPayload.setOldSwVersion("v1");
        upgradePreCheckPayload.setTargetSwVersion("v2");
        upgradePreCheckPayload.setRuleName("r101");
        upgradePreCheckPayload.setAdditionalData("{}");

        return upgradePreCheckPayload;
    }

    @Test
    public final void testUpgradePreCheckPayload() {
        UpgradePreCheckPayload upgradePreCheckPayload = buildUpgradePreCheckPayload();

        try {
            String upgradePreCheckPayloadString = convertToSting(upgradePreCheckPayload);
            assertEquals(expectedUpgradePreCheckPayload, upgradePreCheckPayloadString);
        } catch (Exception e) {
            fail("Convert UpgradePreCheckPayload to String error: " + e.toString());
        }
    }
}
