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

import java.time.Instant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.so.client.sdnc.common.SDNCConstants;
import org.onap.so.client.sdnc.lcm.beans.*;

public class SDNCLcmMessageBuilder {

    public static LcmFlags getSDNCFlags() {
        LcmFlags lcmFlags = new LcmFlags();

        lcmFlags.setMode(SDNCConstants.LCM_FLAGS_MODE_NORMAL);
        lcmFlags.setForce(SDNCConstants.LCM_FLAGS_FORCE_FALSE);
        lcmFlags.setTtl(SDNCConstants.LCM_FLAGS_TTL);

        return lcmFlags;
    }

    public static LcmCommonHeader buildLcmCommonHeader(String requestId, String subRequestId) {
        LcmCommonHeader lcmCommonHeader = new LcmCommonHeader();

        lcmCommonHeader.setApiVer(SDNCConstants.LCM_API_VER);
        lcmCommonHeader.setOriginatorId(SDNCConstants.SYSTEM_NAME);
        lcmCommonHeader.setRequestId(requestId);
        lcmCommonHeader.setSubRequestId(subRequestId);
        lcmCommonHeader.setFlags(getSDNCFlags());
        lcmCommonHeader.setTimestamp(Instant.now().toString());

        return lcmCommonHeader;
    }

    public static LcmInput buildLcmInputForPnf(String requestId, String subRequestId, String pnfName, String action,
            String payload) {
        LcmInput lcmInput = new LcmInput();

        LcmCommonHeader lcmCommonHeader = buildLcmCommonHeader(requestId, subRequestId);

        LcmActionIdentifiers sdncActionIdentifiers = new LcmActionIdentifiers();
        sdncActionIdentifiers.setPnfName(pnfName);

        lcmInput.setCommonHeader(lcmCommonHeader);
        lcmInput.setAction(action);
        lcmInput.setActionIdentifiers(sdncActionIdentifiers);
        lcmInput.setPayload(payload);

        return lcmInput;
    }

    public static LcmRestRequest buildLcmRestRequestForPnf(String requestId, String subRequestId, String pnfName,
            String action, String payload) {
        LcmRestRequest lcmRestRequest = new LcmRestRequest();

        LcmInput lcmInput = buildLcmInputForPnf(requestId, subRequestId, pnfName, action, payload);
        lcmRestRequest.setInput(lcmInput);

        return lcmRestRequest;
    }

    public static String convertToSting(Object msgObject) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(msgObject);
    }
}
