/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Bell Canada
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
package org.onap.so.client.cds;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.so.client.exception.PayloadGenerationException;
import org.onap.so.serviceinstancebeans.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class ExtractServiceFromUserParameters {

    private static final String SERVICE_KEY = "service";

    @Autowired
    private ObjectMapper objectMapper;

    public Service getServiceFromRequestUserParams(List<Map<String, Object>> userParams) throws Exception {
        Map<String, Object> serviceMap = userParams.stream().filter(key -> key.containsKey(SERVICE_KEY)).findFirst()
                .orElseThrow(() -> new Exception("Can not find service in userParams section in generalBuildingBlock"));
        return getServiceObjectFromServiceMap(serviceMap);
    }

    private Service getServiceObjectFromServiceMap(Map<String, Object> serviceMap) throws PayloadGenerationException {
        try {
            String serviceFromJson = objectMapper.writeValueAsString(serviceMap.get(SERVICE_KEY));
            return objectMapper.readValue(serviceFromJson, Service.class);
        } catch (Exception e) {
            throw new PayloadGenerationException("An exception occurred while converting json object to Service object",
                    e);
        }
    }
}
