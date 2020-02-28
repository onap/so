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

public class LcmRestRequestTest extends LcmBeanTest {
    private static String expectedLcmRestRequest = "{" + "\"input\":" + LcmInputTest.getExpectedLcmInput() + "}";

    public LcmRestRequest buildLcmRestRequest() {
        LcmRestRequest lcmRestRequest = new LcmRestRequest();

        LcmInputTest lcmInputTest = new LcmInputTest();
        lcmRestRequest.setInput(lcmInputTest.buildLcmInput());

        return lcmRestRequest;
    }

    public static String getExpectedLcmRestRequest() {
        return expectedLcmRestRequest;
    }

    @Test
    public final void testLcmRestRequest() {
        LcmRestRequest lcmRestRequest = buildLcmRestRequest();

        try {
            String lcmRestRequestString = convertToSting(lcmRestRequest);
            assertEquals(expectedLcmRestRequest, lcmRestRequestString);
        } catch (Exception e) {
            fail("Convert LcmRestRequest to String error: " + e.toString());
        }
    }
}
