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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.onap.so.client.sdnc.lcm.beans.LcmDmaapRequest;
import org.onap.so.client.sdnc.lcm.beans.LcmDmaapResponse;
import org.onap.so.client.dmaap.rest.RestPublisher;
import org.onap.so.client.dmaap.rest.RestConsumer;

public class SDNCLcmDmaapClient {

    private static Logger logger = LoggerFactory.getLogger(SDNCLcmDmaapClient.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private RestPublisher dmaapPublisher;
    private RestConsumer dmaapConsumer;

    public SDNCLcmDmaapClient(SDNCLcmProperties sdncLcmProperties) {
        Properties properties = new Properties();

        properties.put("host", sdncLcmProperties.getDmaapHost());
        if (sdncLcmProperties.getDmaapAuth() != null && sdncLcmProperties.getKey() != null) {
            properties.put("auth", sdncLcmProperties.getDmaapAuth());
            properties.put("key", sdncLcmProperties.getKey());
        }
        properties.put("partition", sdncLcmProperties.getDmaapPartition());
        properties.put("timeout", sdncLcmProperties.getDmaapTimeout());
        properties.put("environment", sdncLcmProperties.getDmaapEnvironment());

        properties.put("topic", sdncLcmProperties.getDmaaPLcmWriteTopic());
        dmaapPublisher = new RestPublisher(properties);

        properties.put("topic", sdncLcmProperties.getDmaaPLcmReadTopic());
        dmaapConsumer = new RestConsumer(properties);
    }

    public void sendRequest(LcmDmaapRequest lcmDmaapRequest) throws Exception {
        String lcmRestRequestString = mapper.writeValueAsString(lcmDmaapRequest);

        dmaapPublisher.send(lcmRestRequestString);
    }

    public Iterable<String> fetch() {
        return dmaapConsumer.fetch();
    }

    public List<LcmDmaapResponse> getResponse() {
        List<LcmDmaapResponse> responseList = new ArrayList<>();

        Iterable<String> itrString = dmaapConsumer.fetch();
        for (String message : itrString) {
            LcmDmaapResponse lcmDmaapResponse;
            try {
                lcmDmaapResponse = mapper.readValue(message, LcmDmaapResponse.class);
            } catch (Exception e) {
                logger.warn("Invalid SDNC LCM DMaaP response: " + message);
                continue;
            }

            responseList.add(lcmDmaapResponse);
        }

        return responseList;
    }
}
