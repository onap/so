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
    private static String expectedSwToBeDownloadedElement = "{" + "\"swLocation\":\"http://192.168.1.20/test.zip\","
            + "\"swFileSize\":123456," + "\"swFileCompression\":\"ZIP\"," + "\"swFileFormat\":\"binary\"" + "}";

    private static String expectedDownloadNESwPayload =
            "{" + "\"ipaddress-v4-oam\":\"192.168.1.10\"," + "\"playbook-name\":\"test_playbook\","
                    + "\"swToBeDownloaded\":[" + expectedSwToBeDownloadedElement + "]" + "}";

    public SwToBeDownloadedElement buildSwToBeDownloadedElement() {
        SwToBeDownloadedElement swToBeDownloadedElement = new SwToBeDownloadedElement();

        swToBeDownloadedElement.setSwLocation("http://192.168.1.20/test.zip");
        swToBeDownloadedElement.setSwFileSize(123456L);
        swToBeDownloadedElement.setSwFileCompression("ZIP");
        swToBeDownloadedElement.setSwFileFormat("binary");

        return swToBeDownloadedElement;
    }

    public DownloadNESwPayload buildDownloadNESwPayload() {
        DownloadNESwPayload downloadNESwPayload = new DownloadNESwPayload();

        downloadNESwPayload.setIpaddressV4Oam(ipaddressV4Oam);
        downloadNESwPayload.setPlaybookName(playbookName);

        SwToBeDownloadedElement swToBeDownloadedElement = buildSwToBeDownloadedElement();
        downloadNESwPayload.setSwToBeDownloaded(Collections.singletonList(swToBeDownloadedElement));

        return downloadNESwPayload;
    }

    @Test
    public final void testSwToBeDownloadedElement() {
        SwToBeDownloadedElement swToBeDownloadedElement = buildSwToBeDownloadedElement();

        try {
            String swToBeDownloadedElementString = convertToSting(swToBeDownloadedElement);
            assertEquals(expectedSwToBeDownloadedElement, swToBeDownloadedElementString);
        } catch (Exception e) {
            fail("Convert SwToBeDownloadedElement to String error: " + e.toString());
        }
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
