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

public class LcmRestResponseTest extends LcmBeanTest {
    private static String expectedLcmRestResponse = "{" + "\"output\":" + LcmOutputTest.getExpectedLcmOutput() + "}";

    public LcmRestResponse buildLcmRestResponse() {
        LcmRestResponse lcmRestResponse = new LcmRestResponse();

        LcmOutputTest lcmOutputTest = new LcmOutputTest();
        lcmRestResponse.setOutput(lcmOutputTest.buildLcmOutput());

        return lcmRestResponse;
    }

    public static String getExpectedLcmRestResponse() {
        return expectedLcmRestResponse;
    }

    @Test
    public final void testLcmRestResponse() {
        LcmRestResponse lcmRestResponse = buildLcmRestResponse();

        try {
            String lcmRestResponseString = convertToSting(lcmRestResponse);
            assertEquals(expectedLcmRestResponse, lcmRestResponseString);
        } catch (Exception e) {
            fail("Convert LcmRestResponse to String error: " + e.toString());
        }
    }
}
