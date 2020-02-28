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

package org.onap.so.client.sdnc.lcm.beans;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LcmInputTest extends LcmBeanTest {
    private static String expectedLcmInput = "{" + "\"common-header\":{" + "\"api-ver\":\"2.00\","
            + "\"flags\":{\"mode\":\"NORMAL\",\"force\":\"FALSE\",\"ttl\":65000}," + "\"originator-id\":\"MSO\","
            + "\"request-id\":\"9f77f437-1515-44bd-a420-0aaf8a3c31a0\","
            + "\"sub-request-id\":\"c197a4b5-18d9-48a2-ad2d-a3b56858501c\","
            + "\"timestamp\":\"2020-02-25T10:20:28.116Z\"" + "}," + "\"action\":\"TestAction\","
            + "\"action-identifiers\":{\"pnf-name\":\"testpnf\"},"
            + "\"payload\":\"{\\\"testPayload\\\": \\\"input test\\\"}\"}";

    public LcmInput buildLcmInput() {
        LcmInput lcmInput = new LcmInput();

        LcmCommonHeader lcmCommonHeader = buildLcmCommonHeader();

        LcmActionIdentifiers lcmActionIdentifiers = new LcmActionIdentifiers();
        lcmActionIdentifiers.setPnfName(pnfName);

        lcmInput.setCommonHeader(lcmCommonHeader);
        lcmInput.setAction(action);
        lcmInput.setActionIdentifiers(lcmActionIdentifiers);
        lcmInput.setPayload(inputPayload);

        return lcmInput;
    }

    public static String getExpectedLcmInput() {
        return expectedLcmInput;
    }

    @Test
    public final void testLcmInput() {
        LcmInput lcmInput = buildLcmInput();

        try {
            String lcmInputString = convertToSting(lcmInput);
            assertEquals(expectedLcmInput, lcmInputString);
        } catch (Exception e) {
            fail("Convert LcmInput to String error: " + e.toString());
        }
    }
}
