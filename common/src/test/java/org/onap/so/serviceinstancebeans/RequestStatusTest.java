/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.serviceinstancebeans;

import org.skyscreamer.jsonassert.JSONAssert;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RequestStatusTest {

    @Test
    public void requestStatusDefaultValues() throws Exception {
        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setRequestState("COMPLETE");
        requestStatus.setStatusMessage("STATUS: COMPLETED");
        requestStatus.setPercentProgress(100);
        requestStatus.setTimeStamp("Fri, 08 Mar 2019 04:41:42 GMT");
        String expectedResponse =
                "{\"requestState\":\"COMPLETE\",\"statusMessage\":\"STATUS: COMPLETED\",\"percentProgress\":100,\"timestamp\":\"Fri, 08 Mar 2019 04:41:42 GMT\"}";

        ObjectMapper mapper = new ObjectMapper();
        String realResponse = mapper.writeValueAsString(requestStatus);

        JSONAssert.assertEquals(expectedResponse, realResponse, false);
    }
}
