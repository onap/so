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
import org.onap.so.client.sdnc.common.SDNCConstants;

public class LcmOutputTest extends LcmBeanTest {
    private static String expectedLcmOutput = "{" + "\"common-header\":{" + "\"api-ver\":\"2.00\","
            + "\"originator-id\":\"MSO\"," + "\"request-id\":\"9f77f437-1515-44bd-a420-0aaf8a3c31a0\","
            + "\"sub-request-id\":\"c197a4b5-18d9-48a2-ad2d-a3b56858501c\"" + "},"
            + "\"status\":{\"code\":400,\"message\":\"Test output message\"},"
            + "\"payload\":\"{\\\"testPayload\\\": \\\"output test\\\"}\"}";

    @Override
    public LcmCommonHeader buildLcmCommonHeader() {
        LcmCommonHeader lcmCommonHeader = new LcmCommonHeader();

        lcmCommonHeader.setApiVer(SDNCConstants.LCM_API_VER);
        lcmCommonHeader.setOriginatorId(SDNCConstants.SYSTEM_NAME);
        lcmCommonHeader.setRequestId(requestId);
        lcmCommonHeader.setSubRequestId(subRequestId);

        return lcmCommonHeader;
    }

    public LcmStatus buildLcmStatus() {
        LcmStatus lcmStatus = new LcmStatus();

        lcmStatus.setCode(400);
        lcmStatus.setMessage("Test output message");

        return lcmStatus;
    }

    public LcmOutput buildLcmOutput() {
        LcmOutput lcmOutput = new LcmOutput();

        LcmCommonHeader lcmCommonHeader = buildLcmCommonHeader();
        LcmStatus lcmStatus = buildLcmStatus();

        lcmOutput.setCommonHeader(lcmCommonHeader);
        lcmOutput.setStatus(lcmStatus);
        lcmOutput.setPayload(outputPayload);

        return lcmOutput;
    }

    public static String getExpectedLcmOutput() {
        return expectedLcmOutput;
    }

    @Test
    public final void testLcmOutput() {
        LcmOutput lcmOutput = buildLcmOutput();

        try {
            String lcmOutputString = convertToSting(lcmOutput);
            assertEquals(expectedLcmOutput, lcmOutputString);
        } catch (Exception e) {
            fail("Convert LcmOutput to String error: " + e.toString());
        }
    }
}
