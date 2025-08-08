/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.client.adapter.vnf.mapper;

import java.util.UUID;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

@Component("VnfAdapterObjectMapperUtils")
public class VnfAdapterObjectMapperUtils {

    public String getRandomUuid() {
        return UUID.randomUUID().toString();
    }

    public String createCallbackUrl(String messageType, String correlator) {
        String endpoint = getProperty("mso.workflow.message.endpoint");
        if (endpoint != null) {
            while (endpoint.endsWith("/")) {
                endpoint = endpoint.substring(0, endpoint.length() - 1);
            }
        }

        return endpoint + "/" + UriUtils.encodePathSegment(messageType, "UTF-8") + "/"
                + UriUtils.encodePathSegment(correlator, "UTF-8");
    }

    protected String getProperty(String key) {

        return UrnPropertiesReader.getVariable(key);
    }

}
