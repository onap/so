/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Nokia
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

import com.google.gson.JsonObject;
import org.onap.so.client.exception.PayloadGenerationException;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.Pnfs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ConfigureInstanceParamsForPnf {

    private ExtractServiceFromUserParameters extractServiceFromUserParameters;

    @Autowired
    public ConfigureInstanceParamsForPnf(ExtractServiceFromUserParameters extractServiceFromUserParameters) {
        this.extractServiceFromUserParameters = extractServiceFromUserParameters;
    }

    /**
     * Read instance parameters for PNF and put into JsonObject.
     *
     * @param jsonObject - JsonObject which will hold the payload to send to CDS.
     * @param userParamsFromRequest - User parameters.
     * @param modelCustomizationUuid - Unique ID for Pnf.
     * @throws PayloadGenerationException if it doesn't able to populate instance parameters from SO payload.
     */
    public void populateInstanceParams(JsonObject jsonObject, List<Map<String, Object>> userParamsFromRequest,
            String modelCustomizationUuid) throws PayloadGenerationException {
        try {
            Service service = extractServiceFromUserParameters.getServiceFromRequestUserParams(userParamsFromRequest);
            List<Map<String, String>> instanceParamsList = getInstanceParamForPnf(service, modelCustomizationUuid);

            instanceParamsList.stream().flatMap(instanceParamsMap -> instanceParamsMap.entrySet().stream())
                    .forEachOrdered(entry -> jsonObject.addProperty(entry.getKey(), entry.getValue()));
        } catch (Exception exception) {
            throw new PayloadGenerationException("Couldn't able to resolve instance parameters", exception);
        }
    }

    private List<Map<String, String>> getInstanceParamForPnf(Service service, String genericPnfModelCustomizationUuid)
            throws PayloadGenerationException {
        Optional<Pnfs> foundedPnfs = service.getResources().getPnfs().stream()
                .filter(pnfs -> pnfs.getModelInfo().getModelCustomizationId().equals(genericPnfModelCustomizationUuid))
                .findFirst();
        if (foundedPnfs.isPresent()) {
            return foundedPnfs.get().getInstanceParams();
        } else {
            throw new PayloadGenerationException(String.format(
                    "Can not find pnf for genericPnfModelCustomizationUuid: %s", genericPnfModelCustomizationUuid));
        }
    }
}
