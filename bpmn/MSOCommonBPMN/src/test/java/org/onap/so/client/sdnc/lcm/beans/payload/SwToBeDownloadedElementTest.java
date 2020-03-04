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

public class SwToBeDownloadedElementTest extends LcmBasePayloadTest {
    private static String expectedSwToBeDownloadedField = "{" + "\"swLocation\":\"http://192.168.1.20:1080/test.zip\","
            + "\"swFileSize\":123456," + "\"swFileCompression\":\"GZIP\"," + "\"swFileFormat\":\"binary\"" + "}";

    public SwToBeDownloadedElement buildSwToBeDownloadedField() {
        SwToBeDownloadedElement swToBeDownloadedElement = new SwToBeDownloadedElement();

        swToBeDownloadedElement.setSwLocation("http://192.168.1.20:1080/test.zip");
        swToBeDownloadedElement.setSwFileSize(123456);
        swToBeDownloadedElement.setSwFileCompression("GZIP");
        swToBeDownloadedElement.setSwFileFormat("binary");

        return swToBeDownloadedElement;
    }

    public static String getExpectedSwToBeDownloadedField() {
        return expectedSwToBeDownloadedField;
    }

    @Test
    public final void testSwToBeDownloadedField() {
        SwToBeDownloadedElement swToBeDownloadedElement = buildSwToBeDownloadedField();

        try {
            String swToBeDownloadedFieldString = convertToSting(swToBeDownloadedElement);
            assertEquals(expectedSwToBeDownloadedField, swToBeDownloadedFieldString);
        } catch (Exception e) {
            fail("Convert SwToBeDownloadedField to String error: " + e.toString());
        }
    }
}
