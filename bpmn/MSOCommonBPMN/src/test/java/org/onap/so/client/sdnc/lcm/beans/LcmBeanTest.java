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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.onap.so.client.sdnc.common.SDNCConstants;

public class LcmBeanTest {
    private static Logger logger = LoggerFactory.getLogger(LcmBeanTest.class);

    protected String requestId = "9f77f437-1515-44bd-a420-0aaf8a3c31a0";
    protected String subRequestId = "c197a4b5-18d9-48a2-ad2d-a3b56858501c";
    protected String timestamp = "2020-02-25T10:20:28.116Z";

    protected String pnfName = "testpnf";
    protected String action = "TestAction";
    protected String operation = "test-operation";

    protected String inputPayload = "{\"testPayload\": \"input test\"}";
    protected String outputPayload = "{\"testPayload\": \"output test\"}";

    public String getOperation() {
        return operation;
    }

    public LcmFlags buildSDNCFlags() {
        LcmFlags lcmFlags = new LcmFlags();

        lcmFlags.setMode(SDNCConstants.LCM_FLAGS_MODE_NORMAL);
        lcmFlags.setForce(SDNCConstants.LCM_FLAGS_FORCE_FALSE);
        lcmFlags.setTtl(SDNCConstants.LCM_FLAGS_TTL);

        return lcmFlags;
    }

    public LcmCommonHeader buildLcmCommonHeader() {
        LcmCommonHeader lcmCommonHeader = new LcmCommonHeader();

        lcmCommonHeader.setApiVer(SDNCConstants.LCM_API_VER);
        lcmCommonHeader.setFlags(buildSDNCFlags());
        lcmCommonHeader.setOriginatorId(SDNCConstants.SYSTEM_NAME);
        lcmCommonHeader.setRequestId(requestId);
        lcmCommonHeader.setSubRequestId(subRequestId);
        lcmCommonHeader.setTimestamp(timestamp);

        return lcmCommonHeader;
    }

    public String convertToSting(Object msgObject) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        String msgString = mapper.writeValueAsString(msgObject);
        logger.debug("{}\n{}", msgObject.getClass().getSimpleName(), msgString);

        return msgString;
    }
}
