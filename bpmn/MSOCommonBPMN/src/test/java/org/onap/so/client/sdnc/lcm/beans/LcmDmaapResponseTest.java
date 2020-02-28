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

public class LcmDmaapResponseTest extends LcmBeanTest {
    private static String expectedLcmDmaapResponse = "{" + "\"version\":\"1.0\"," + "\"type\":\"response\","
            + "\"cambria.partition\":\"MSO\","
            + "\"correlation-id\":\"9f77f437-1515-44bd-a420-0aaf8a3c31a0-c197a4b5-18d9-48a2-ad2d-a3b56858501c\","
            + "\"rpc-name\":\"test-operation\"," + "\"body\":" + LcmRestResponseTest.getExpectedLcmRestResponse() + "}";

    public LcmDmaapResponse buildLcmDmaapResponse() {
        LcmDmaapResponse lcmDmaapResponse = new LcmDmaapResponse();

        LcmRestResponseTest lcmRestResponseTest = new LcmRestResponseTest();
        LcmRestResponse lcmRestResponse = lcmRestResponseTest.buildLcmRestResponse();
        LcmCommonHeader lcmCommonHeader = lcmRestResponse.getOutput().getCommonHeader();
        String correlationId = lcmCommonHeader.getRequestId() + "-" + lcmCommonHeader.getSubRequestId();

        lcmDmaapResponse.setVersion(SDNCConstants.LCM_DMAAP_MSG_VER);
        lcmDmaapResponse.setType(SDNCConstants.LCM_DMAAP_MSG_TYPE_RESPONSE);
        lcmDmaapResponse.setCambriaPartition(SDNCConstants.SYSTEM_NAME);
        lcmDmaapResponse.setCorrelationId(correlationId);
        lcmDmaapResponse.setRpcName(operation);
        lcmDmaapResponse.setBody(lcmRestResponse);

        return lcmDmaapResponse;
    }

    public static String getExpectedLcmDmaapResponse() {
        return expectedLcmDmaapResponse;
    }

    @Test
    public final void testLcmDmaapResponse() {
        LcmDmaapResponse lcmDmaapResponse = buildLcmDmaapResponse();

        try {
            String lcmDmaapResponseString = convertToSting(lcmDmaapResponse);
            assertEquals(expectedLcmDmaapResponse, lcmDmaapResponseString);
        } catch (Exception e) {
            fail("Convert LcmDmaapResponse to String error: " + e.toString());
        }
    }
}
