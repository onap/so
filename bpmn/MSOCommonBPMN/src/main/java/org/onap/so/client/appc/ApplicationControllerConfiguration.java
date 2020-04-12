/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.appc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationControllerConfiguration {
    @Value("${appc.client.topic.read.name}")
    private String readTopic;

    @Value("${appc.client.topic.read.timeout}")
    private String readTimeout;

    @Value("${appc.client.response.timeout}")
    private String responseTimeout;

    @Value("${appc.client.topic.write}")
    private String write;

    @Value("${appc.client.poolMembers}")
    private String poolMembers;

    @Value("${appc.client.key}")
    private String clientKey;

    @Value("${appc.client.secret}")
    private String clientSecret;

    @Value("${appc.client.service}")
    private String service;

    public String getClientKey() {
        return clientKey;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getPoolMembers() {
        return poolMembers;
    }

    public String getReadTimeout() {
        return readTimeout;
    }

    public String getResponseTimeout() {
        return responseTimeout;
    }

    public String getReadTopic() {
        return readTopic;
    }

    public String getService() {
        return service;
    }

    public String getWrite() {
        return write;
    }
}

