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

package org.onap.so.client.sdnc.lcm.beans.payload;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LcmBasePayloadTest {
    private static Logger logger = LoggerFactory.getLogger(LcmBasePayloadTest.class);

    protected String ipaddressV4Oam = "192.168.1.10";
    protected String playbookName = "test_playbook";

    public String convertToSting(Object msgObject) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        String msgString = mapper.writeValueAsString(msgObject);
        logger.debug(msgObject.getClass().getSimpleName() + "\n" + msgString);

        return msgString;
    }
}
