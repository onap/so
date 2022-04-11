/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2021 Bell Canada
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

import static org.onap.so.client.cds.ConfigureInstanceParamsUtil.applyJsonParamsToObject;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.onap.so.client.exception.PayloadGenerationException;
import org.onap.so.serviceinstancebeans.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigureInstanceParamsForService {

    @Autowired
    private ExtractServiceFromUserParameters extractServiceFromUserParameters;

    /**
     * Read instance parameters for Service and put into JsonObject.
     *
     * @param jsonObject - JsonObject which will hold the payload to send to CDS.
     * @param userParamsFromRequest - User parameters.
     * @throws PayloadGenerationException if it doesn't able to populate instance parameters from SO payload.
     */
    public void populateInstanceParams(JsonObject jsonObject, List<Map<String, Object>> userParamsFromRequest)
            throws PayloadGenerationException {
        try {
            Optional<Service> service =
                    extractServiceFromUserParameters.getServiceFromRequestUserParams(userParamsFromRequest);

            service.map(Service::getInstanceParams).ifPresent(p -> applyJsonParamsToObject(p, jsonObject));
        } catch (Exception e) {
            throw new PayloadGenerationException("Failed to resolve instance parameters", e);
        }
    }
}
