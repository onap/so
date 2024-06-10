/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Bell Canada
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
import java.util.List;
import java.util.Map;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.client.exception.PayloadGenerationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.Optional;
import static org.onap.so.client.cds.PayloadConstants.PROPERTIES;
import static org.onap.so.client.cds.PayloadConstants.SEPARATOR;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ServiceCDSRequestProvider implements CDSRequestProvider {

    private String resolutionKey;
    private BuildingBlockExecution execution;
    private String bluePrintName;
    private String bluePrintVersion;

    @Autowired
    private ConfigureInstanceParamsForService configureInstanceParamsForService;

    @Override
    public String getBlueprintName() {
        return bluePrintName;
    }

    @Override
    public String getBlueprintVersion() {
        return bluePrintVersion;
    }

    @Override
    public <T> void setExecutionObject(T executionObject) {
        execution = (BuildingBlockExecution) executionObject;
    }

    @Override
    public Optional<String> buildRequestPayload(String action) throws PayloadGenerationException {
        JsonObject cdsPropertyObject = new JsonObject();
        JsonObject serviceObject = new JsonObject();
        try {
            ServiceInstance serviceInstance = execution.getGeneralBuildingBlock().getServiceInstance();
            bluePrintName = serviceInstance.getModelInfoServiceInstance().getBlueprintName();
            bluePrintVersion = serviceInstance.getModelInfoServiceInstance().getBlueprintVersion();
            resolutionKey = serviceInstance.getServiceInstanceName();

            serviceObject.addProperty("service-instance-id", serviceInstance.getServiceInstanceId());
            serviceObject.addProperty("service-model-uuid",
                    serviceInstance.getModelInfoServiceInstance().getModelUuid());

            final GeneralBuildingBlock buildingBlock = execution.getGeneralBuildingBlock();
            List<Map<String, Object>> userParamsFromRequest =
                    buildingBlock.getRequestContext().getRequestParameters().getUserParams();
            if (userParamsFromRequest != null && userParamsFromRequest.size() != 0) {
                configureInstanceParamsForService.populateInstanceParams(serviceObject, userParamsFromRequest);
            }
        } catch (Exception e) {
            throw new PayloadGenerationException("Failed to buildPropertyObjectForService", e);
        }

        cdsPropertyObject.addProperty("resolution-key", resolutionKey);
        cdsPropertyObject.add(action + SEPARATOR + PROPERTIES, serviceObject);

        return Optional.of(buildRequestJsonObject(cdsPropertyObject, action));
    }

}
