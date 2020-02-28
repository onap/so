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

package org.onap.so.client.sdnc.lcm;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.onap.so.client.sdnc.lcm.beans.LcmBeanTest;
import org.onap.so.client.sdnc.lcm.beans.LcmInput;
import org.onap.so.client.sdnc.lcm.beans.LcmInputTest;

public class SDNCLcmMessageBuilderTest extends LcmBeanTest {

    public LcmInput buildLcmInputForPnf() {
        LcmInput lcmInput =
                SDNCLcmMessageBuilder.buildLcmInputForPnf(requestId, subRequestId, pnfName, action, inputPayload);

        lcmInput.getCommonHeader().setTimestamp(timestamp);

        return lcmInput;
    }

    @Test
    public final void testBuildLcmRestRequestForPnf() {
        LcmInput lcmInput = buildLcmInputForPnf();

        String expectedLcmInput = LcmInputTest.getExpectedLcmInput();
        try {
            String lcmInputString = convertToSting(lcmInput);
            assertEquals(expectedLcmInput, lcmInputString);
        } catch (Exception e) {
            fail("Convert LcmInput to String error: " + e.toString());
        }
    }
}
