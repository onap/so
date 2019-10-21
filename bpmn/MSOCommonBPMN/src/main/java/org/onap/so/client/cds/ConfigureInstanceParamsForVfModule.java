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

import com.google.gson.JsonObject;
import org.onap.so.client.cds.ExtractServiceFromUserParameters;
import org.onap.so.client.exception.PayloadGenerationException;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.VfModules;
import org.onap.so.serviceinstancebeans.Vnfs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class ConfigureInstanceParamsForVfModule {

    @Autowired
    private ExtractServiceFromUserParameters extractServiceFromUserParameters;

    /**
     * Read instance parameters for VF-Module and put into JsonObject.
     *
     * @param jsonObject- JsonObject which will hold the payload to send to CDS.
     * @param userParamsFromRequest - User parameters for a vf-module
     * @param vnfCustomizationUuid - Unique ID for vnf.
     * @param vfModuleCustomizationUuid - Unique ID for vf-module.
     * @throws PayloadGenerationException- If it doesn't able to populate instance parameters from SO payload.
     */
    public void populateInstanceParams(JsonObject jsonObject, List<Map<String, Object>> userParamsFromRequest,
            String vnfCustomizationUuid, String vfModuleCustomizationUuid) throws PayloadGenerationException {
        try {
            Service service = extractServiceFromUserParameters.getServiceFromRequestUserParams(userParamsFromRequest);

            List<Map<String, String>> instanceParamsList =
                    getInstanceParams(service, vnfCustomizationUuid, vfModuleCustomizationUuid);

            instanceParamsList.stream().flatMap(instanceParamsMap -> instanceParamsMap.entrySet().stream())
                    .forEachOrdered(entry -> jsonObject.addProperty(entry.getKey(), entry.getValue()));
        } catch (Exception e) {
            throw new PayloadGenerationException("Couldn't able to resolve instance parameters", e);
        }
    }

    private List<Map<String, String>> getInstanceParams(Service service, String vnfCustomizationUuid,
            String vfModuleCustomizationUuid) throws PayloadGenerationException {

        Vnfs foundedVnf = service.getResources().getVnfs().stream()
                .filter(vnfs -> vnfs.getModelInfo().getModelCustomizationId().equals(vnfCustomizationUuid)).findFirst()
                .orElseThrow(() -> new PayloadGenerationException(String
                        .format("Can not find vnf for genericVnfModelCustomizationUuid: %s", vnfCustomizationUuid)));

        VfModules vfModule = foundedVnf.getVfModules().stream().filter(
                vfModules -> vfModules.getModelInfo().getModelCustomizationId().equals(vfModuleCustomizationUuid))
                .findFirst().orElseThrow(() -> new PayloadGenerationException(String
                        .format("Can not find vnf for vfModuleCustomizationUuid: %s", vfModuleCustomizationUuid)));

        return vfModule.getInstanceParams();
    }
}
