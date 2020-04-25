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
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
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
public class VfModuleCDSRequestProvider implements CDSRequestProvider {
    private String blueprintName;
    private String blueprintVersion;
    private BuildingBlockExecution execution;

    @Autowired
    private ExtractPojosForBB extractPojosForBB;

    @Autowired
    private ConfigureInstanceParamsForVfModule configureInstanceParamsForVfModule;

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
        JsonObject vfModuleObject = new JsonObject();
        String vfModuleName;

        try {
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);

            final String modelCustomizationUuidForVnf = genericVnf.getModelInfoGenericVnf().getModelCustomizationUuid();

            blueprintName = genericVnf.getModelInfoGenericVnf().getBlueprintName();
            blueprintVersion = genericVnf.getModelInfoGenericVnf().getBlueprintVersion();

            VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            vfModuleName = vfModule.getVfModuleName();

            final String modelCustomizationUuidForVfModule =
                    vfModule.getModelInfoVfModule().getModelCustomizationUUID();

            vfModuleObject.addProperty("service-instance-id", serviceInstance.getServiceInstanceId());
            vfModuleObject.addProperty("service-model-uuid",
                    serviceInstance.getModelInfoServiceInstance().getModelUuid());
            vfModuleObject.addProperty("vnf-id", genericVnf.getVnfId());
            vfModuleObject.addProperty("vnf-name", genericVnf.getVnfName());
            vfModuleObject.addProperty("vf-module-id", vfModule.getVfModuleId());
            vfModuleObject.addProperty("vf-module-name", vfModule.getVfModuleName());
            vfModuleObject.addProperty("vf-module-customization-uuid",
                    vfModule.getModelInfoVfModule().getModelCustomizationUUID());

            final GeneralBuildingBlock buildingBlock = execution.getGeneralBuildingBlock();
            List<Map<String, Object>> userParamsFromRequest =
                    buildingBlock.getRequestContext().getRequestParameters().getUserParams();

            configureInstanceParamsForVfModule.populateInstanceParams(vfModuleObject, userParamsFromRequest,
                    modelCustomizationUuidForVnf, modelCustomizationUuidForVfModule);
        } catch (Exception e) {
            throw new PayloadGenerationException("Failed to buildPropertyObject for VF-Module", e);
        }

        // Not sure for resolutionKey should be same as vfModule name.
        cdsPropertyObject.addProperty("resolution-key", vfModuleName);
        cdsPropertyObject.addProperty("template-prefix", vfModuleName + action);
        cdsPropertyObject.add(action + SEPARATOR + PROPERTIES, vfModuleObject);

        return Optional.of(buildRequestJsonObject(cdsPropertyObject, action));
    }

}
