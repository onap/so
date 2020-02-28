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
import org.onap.so.client.sdnc.common.SDNCConstants;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LcmDmaapRequestTest extends LcmBeanTest {
    private static String expectedLcmDmaapRequest = "{" + "\"version\":\"1.0\"," + "\"type\":\"request\","
            + "\"cambria.partition\":\"MSO\","
            + "\"correlation-id\":\"9f77f437-1515-44bd-a420-0aaf8a3c31a0-c197a4b5-18d9-48a2-ad2d-a3b56858501c\","
            + "\"rpc-name\":\"test-operation\"," + "\"body\":" + LcmRestRequestTest.getExpectedLcmRestRequest() + "}";

    public LcmDmaapRequest buildLcmDmaapRequest() {
        LcmDmaapRequest lcmDmaapRequest = new LcmDmaapRequest();

        LcmRestRequestTest lcmRestRequestTest = new LcmRestRequestTest();
        LcmRestRequest lcmRestRequest = lcmRestRequestTest.buildLcmRestRequest();
        LcmCommonHeader lcmCommonHeader = lcmRestRequest.getInput().getCommonHeader();
        String correlationId = lcmCommonHeader.getRequestId() + "-" + lcmCommonHeader.getSubRequestId();

        lcmDmaapRequest.setVersion(SDNCConstants.LCM_DMAAP_MSG_VER);
        lcmDmaapRequest.setType(SDNCConstants.LCM_DMAAP_MSG_TYPE_REQUEST);
        lcmDmaapRequest.setCambriaPartition(SDNCConstants.SYSTEM_NAME);
        lcmDmaapRequest.setCorrelationId(correlationId);
        lcmDmaapRequest.setRpcName(operation);
        lcmDmaapRequest.setBody(lcmRestRequest);

        return lcmDmaapRequest;
    }

    public static String getExpectedLcmDmaapRequest() {
        return expectedLcmDmaapRequest;
    }

    @Test
    public final void testLcmDmaapRequest() {
        LcmDmaapRequest lcmDmaapRequest = buildLcmDmaapRequest();

        try {
            String lcmDmaapRequestString = convertToSting(lcmDmaapRequest);
            assertEquals(expectedLcmDmaapRequest, lcmDmaapRequestString);
        } catch (Exception e) {
            fail("Convert LcmDmaapRequest to String error: " + e.toString());
        }
    }
}
