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
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static org.onap.so.client.cds.PayloadConstants.VFMODULE_SCOPE;
import static org.onap.so.client.cds.PayloadConstants.CONFIG;
import java.util.Optional;

@Component
public class ConfigVfModule implements VirtualComponent {

    private JsonObject vfModuleObject;
    private String blueprintName;
    private String blueprintVersion;
    private String action;
    private BuildingBlockExecution execution;

    @Autowired
    private ExtractPojosForBB extractPojosForBB;

    @Override
    public Optional<String> buildRequestPayload(String action) throws Exception {
        this.action = action;

        vfModuleObject = buildPropertyObjectForVfModule(extractPojosForBB);

        // TODO Need to figure out for vf module how I can build resolution key.
        String requestBuilder = "{\"" + CONFIG + action + "-" + VFMODULE_SCOPE + "-request\":{" + "\"resolution-key\":"
                + "\"" + "resolutionKey" + "\"," + "\"template-prefix\":" + "\"" + getTemplatePrefix() + "\","
                + PropertyPayloadBuilder.buildConfigProperties(action, VFMODULE_SCOPE, vfModuleObject) + '}';

        return Optional.of(requestBuilder);
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

    private JsonObject buildPropertyObjectForVfModule(ExtractPojosForBB extractPojosForBB) throws Exception {
        vfModuleObject = new JsonObject();

        ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
        GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);

        blueprintName = genericVnf.getBlueprintName();
        blueprintVersion = genericVnf.getBlueprintVersion();

        VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);

        vfModuleObject.addProperty("service-instance-id", serviceInstance.getServiceInstanceId());
        vfModuleObject.addProperty("service-model-uuid", serviceInstance.getModelInfoServiceInstance().getModelUuid());
        vfModuleObject.addProperty("vnf-id", genericVnf.getVnfId());
        vfModuleObject.addProperty("vnf-name", genericVnf.getVnfName());
        vfModuleObject.addProperty("vf-module-id", vfModule.getVfModuleId());
        vfModuleObject.addProperty("vf-module-name", vfModule.getVfModuleName());
        vfModuleObject.addProperty("vf-module-customization-uuid",
                vfModule.getModelInfoVfModule().getModelCustomizationUUID());

        // TODO Add instance parameters based on action for VF-Module.

        return vfModuleObject;
    }

    // TODO Add logic to create template-prefix.
    private String getTemplatePrefix() {
        return "template-prefix";
    }
}
