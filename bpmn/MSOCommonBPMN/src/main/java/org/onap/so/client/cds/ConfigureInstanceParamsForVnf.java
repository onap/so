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
import com.google.gson.JsonObject;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.Vnfs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConfigureInstanceParamsForVnf implements Action {

    private static final Logger logger = LoggerFactory.getLogger(ConfigureInstanceParamsForVnf.class);

    @Override
    public void populateJsonBasedOnAction(JsonObject jsonObject, GeneralBuildingBlock buildingBlock, String modelCustomizationUuid) {
        try {
            List<Map<String, Object>> userParamsFromRequest = buildingBlock.getRequestContext().getRequestParameters().getUserParams();
            Service service = getServiceFromRequestUserParams(userParamsFromRequest);
            List<Map<String, String>> instanceParamsList =
                    getInstanceParamForVnf(service, modelCustomizationUuid);

            instanceParamsList.stream()
                    .flatMap(instanceParamsMap -> instanceParamsMap.entrySet().stream())
                    .forEachOrdered(entry -> jsonObject.addProperty(entry.getKey(), entry.getValue()));

        } catch (Exception e) {
            // TODO Add exception builder instead
            logger.error("An exception occurred when creating ConfigAssignPropertiesForVnf for CDS request", e);
            e.printStackTrace();
        }

    }
    private Service getServiceFromRequestUserParams(List<Map<String, Object>> userParams) throws Exception {
        Map<String, Object> serviceMap = userParams.stream().filter(key -> key.containsKey("service")).findFirst()
                .orElseThrow(() -> new Exception("Can not find service in userParams section in generalBuildingBlock"));
        return getServiceObjectFromServiceMap(serviceMap);
    }

    private Service getServiceObjectFromServiceMap(Map<String, Object> serviceMap) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        String serviceFromJson = objectMapper.writeValueAsString(serviceMap.get("service"));
        try {
            return objectMapper.readValue(serviceFromJson, Service.class);
        } catch (Exception e) {
            logger.error(String.format(
                    "An exception occurred while converting json object to Service object. The json is: %s",
                    serviceFromJson), e);
            throw e;
        }
    }

    private List<Map<String, String>> getInstanceParamForVnf(Service service, String genericVnfModelCustomizationUuid)
            throws Exception {
        Optional<Vnfs> foundedVnf = service.getResources().getVnfs().stream()
                .filter(vnfs -> vnfs.getModelInfo().getModelCustomizationId().equals(genericVnfModelCustomizationUuid))
                .findFirst();
        if (foundedVnf.isPresent()) {
            return foundedVnf.get().getInstanceParams();
        } else {
            throw new Exception(String.format("Can not find vnf for genericVnfModelCustomizationUuid: %s",
                    genericVnfModelCustomizationUuid));
        }
    }
}
