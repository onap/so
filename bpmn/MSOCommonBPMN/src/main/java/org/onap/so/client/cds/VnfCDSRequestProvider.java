/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Bell Canada
 * ================================================================================
 * Modifications Copyright (c) 2020 Tech Mahindra
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
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.PayloadGenerationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.onap.so.client.cds.PayloadConstants.PROPERTIES;
import static org.onap.so.client.cds.PayloadConstants.SEPARATOR;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VnfCDSRequestProvider implements CDSRequestProvider {
    private String blueprintName;
    private String blueprintVersion;
    private BuildingBlockExecution execution;

    @Autowired
    private ExtractPojosForBB extractPojosForBB;

    @Autowired
    private ConfigureInstanceParamsForVnf configureInstanceParamsForVnf;

    @Override
    public String getBlueprintName() {
        return blueprintName;
    }

    @Override
    public String getBlueprintVersion() {
        return blueprintVersion;
    }

    @Override
    public <T> void setExecutionObject(T executionObject) {
        execution = (BuildingBlockExecution) executionObject;
    }

    @Override
    public Optional<String> buildRequestPayload(String action) throws PayloadGenerationException {
        JsonObject cdsPropertyObject = new JsonObject();
        JsonObject vnfObject = new JsonObject();
        String resolutionKey;
        try {
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);

            final String modelCustomizationUuid = genericVnf.getModelInfoGenericVnf().getModelCustomizationUuid();

            resolutionKey = genericVnf.getVnfName();
            blueprintName = genericVnf.getModelInfoGenericVnf().getBlueprintName();
            blueprintVersion = genericVnf.getModelInfoGenericVnf().getBlueprintVersion();

            vnfObject.addProperty("service-instance-id", serviceInstance.getServiceInstanceId());
            vnfObject.addProperty("service-model-uuid", serviceInstance.getModelInfoServiceInstance().getModelUuid());
            vnfObject.addProperty("vnf-id", genericVnf.getVnfId());
            vnfObject.addProperty("vnf-name", genericVnf.getVnfName());
            vnfObject.addProperty("vnf-customization-uuid", modelCustomizationUuid);

            final GeneralBuildingBlock buildingBlock = execution.getGeneralBuildingBlock();
            List<Map<String, Object>> userParamsFromRequest =
                    buildingBlock.getRequestContext().getRequestParameters().getUserParams();
            String vnfInstanceName = execution.getLookupMap().getOrDefault(ResourceKey.VNF_INSTANCE_NAME, "");
            if (userParamsFromRequest != null && userParamsFromRequest.size() != 0) {
                configureInstanceParamsForVnf.populateInstanceParams(vnfObject, userParamsFromRequest,
                        modelCustomizationUuid, vnfInstanceName);
            }
        } catch (Exception e) {
            throw new PayloadGenerationException("Failed to buildPropertyObjectForVnf", e);
        }

        cdsPropertyObject.addProperty("resolution-key", resolutionKey);
        cdsPropertyObject.add(action + SEPARATOR + PROPERTIES, vnfObject);

        return Optional.of(buildRequestJsonObject(cdsPropertyObject, action));
    }
}
