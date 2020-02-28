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

import java.net.URI;
import org.onap.so.client.RestPropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SDNCLcmClientBuilder {
    private static Logger logger = LoggerFactory.getLogger(SDNCLcmClientBuilder.class);
    private final SDNCLcmProperties sdncLcmProperties;

    public SDNCLcmClientBuilder() {
        sdncLcmProperties = RestPropertiesLoader.getInstance().getNewImpl(SDNCLcmProperties.class);
    }

    public SDNCLcmClientBuilder(SDNCLcmProperties pros) {
        sdncLcmProperties = pros;
    }

    public SDNCLcmRestClient newSDNCLcmRestClient(String operation) throws SDNCLcmClientBuilderException {
        URI pathUri;
        try {
            String path = sdncLcmProperties.getPath() + operation;
            pathUri = new URI(path);
            logger.debug("SDNC host: " + sdncLcmProperties.getHost());
            logger.debug("SDNC API path: " + pathUri.getPath());
        } catch (Exception e) {
            String msg = "Error API path syntax: ";
            logger.error(msg, e);
            throw new SDNCLcmClientBuilderException(msg + e.toString());
        }

        try {
            SDNCLcmRestClient sdncLcmRestClient = new SDNCLcmRestClient(sdncLcmProperties, pathUri);
            logger.debug("Create SDNCLcmRestClient success");
            return sdncLcmRestClient;
        } catch (Exception e) {
            String msg = "Create SDNCLcmRestClient failure: ";
            logger.error(msg, e);
            throw new SDNCLcmClientBuilderException(msg + e.toString());
        }
    }
}
