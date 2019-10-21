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
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.PayloadGenerationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.onap.so.client.cds.PayloadConstants.PROPERTIES;
import static org.onap.so.client.cds.PayloadConstants.SEPARATOR;

public class VnfCDSRequestProvider implements CDSRequestProvider {
    private String resolutionKey;
    private JsonObject vnfObject;
    private String blueprintName;
    private String blueprintVersion;
    private BuildingBlockExecution execution;
    private ExtractPojosForBB extractPojosForBB;

    VnfCDSRequestProvider(ExtractPojosForBB extractPojosForBB) {
        this.extractPojosForBB = extractPojosForBB;
    }

    @Override
    public Optional<String> buildRequestPayload(String action) throws PayloadGenerationException {
        JsonObject cdsPropertyObject = new JsonObject();
        vnfObject = buildPropertyObjectForVnf();

        cdsPropertyObject.addProperty("resolution-key", this.resolutionKey);
        cdsPropertyObject.add(action + SEPARATOR + PROPERTIES, vnfObject);

        return Optional.of(buildRequestJsonObject(cdsPropertyObject, action));
    }

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

    private JsonObject buildPropertyObjectForVnf() throws PayloadGenerationException {
        try {
            vnfObject = new JsonObject();
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);

            final String modelCustomizationUuid = genericVnf.getModelInfoGenericVnf().getModelCustomizationUuid();

            resolutionKey = genericVnf.getVnfName();

            blueprintName = genericVnf.getBlueprintName();
            blueprintVersion = genericVnf.getBlueprintVersion();

            vnfObject.addProperty("service-instance-id", serviceInstance.getServiceInstanceId());
            vnfObject.addProperty("service-model-uuid", serviceInstance.getModelInfoServiceInstance().getModelUuid());
            vnfObject.addProperty("vnf-id", genericVnf.getVnfId());
            vnfObject.addProperty("vnf-name", genericVnf.getVnfName());
            vnfObject.addProperty("vnf-customization-uuid", modelCustomizationUuid);

            final GeneralBuildingBlock buildingBlock = execution.getGeneralBuildingBlock();
            ConfigureInstanceParamsForVnf instanceParamsForVnf = new ConfigureInstanceParamsForVnf();
            List<Map<String, Object>> userParamsFromRequest =
                    buildingBlock.getRequestContext().getRequestParameters().getUserParams();

            instanceParamsForVnf.populateInstanceParams(vnfObject, userParamsFromRequest, modelCustomizationUuid);
        } catch (Exception e) {
            throw new PayloadGenerationException("Failed to buildPropertyObjectForVnf", e);
        }

        return vnfObject;
    }
}
