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

import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DownloadNESwPayloadTest extends LcmBasePayloadTest {
    private static String expectedDownloadNESwPayload = "{" + "\"ipaddress-v4-oam\":\"192.168.1.10\","
            + "\"playbook-name\":\"test_playbook\"," + "\"swToBeDownloaded\":["
            + SwToBeDownloadedElementTest.getExpectedSwToBeDownloadedField() + "]" + "}";

    public DownloadNESwPayload buildDownloadNESwPayload() {
        DownloadNESwPayload downloadNESwPayload = new DownloadNESwPayload();

        downloadNESwPayload.setIpaddressV4Oam(ipaddressV4Oam);
        downloadNESwPayload.setPlaybookName(playbookName);

        SwToBeDownloadedElementTest swToBeDownloadedFieldTest = new SwToBeDownloadedElementTest();
        SwToBeDownloadedElement swToBeDownloadedElement = swToBeDownloadedFieldTest.buildSwToBeDownloadedField();
        downloadNESwPayload.setSwToBeDownloaded(Collections.singletonList(swToBeDownloadedElement));

        return downloadNESwPayload;
    }

    public static String getExpectedDownloadNESwPayload() {
        return expectedDownloadNESwPayload;
    }

    @Test
    public final void testDownloadNESwPayload() {
        DownloadNESwPayload downloadNESwPayload = buildDownloadNESwPayload();

        try {
            String downloadNESwPayloadString = convertToSting(downloadNESwPayload);
            assertEquals(expectedDownloadNESwPayload, downloadNESwPayloadString);
        } catch (Exception e) {
            fail("Convert DownloadNESwPayload to String error: " + e.toString());
        }
    }
}
