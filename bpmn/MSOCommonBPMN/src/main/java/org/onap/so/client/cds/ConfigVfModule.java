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
import org.onap.so.client.exception.BBObjectNotFoundException;
import java.util.Optional;

public class ConfigVfModule implements Actor {

    private static final String SCOPE = "vf-module";
    private String action;
    private JsonObject vfModuleObject;

    @Override
    public Optional<String> buildRequestPayload(ExtractPojosForBB extractPojosForBB, BuildingBlockExecution execution) {

        JsonObject vfModuleObject = buildPropertyObjectForVfModule(extractPojosForBB, execution);

        // TODO Need to figure out for vf module how I can build resolution key.
        String requestBuilder = "{\"" + "config-" + action + "-" + SCOPE + "-request\":{" + "\"resolution-key\":" + "\""
                + "resolutionKey" + "\"," + "\"template-prefix\":" + "\"" + getTemplatePrefix() + "\","
                + PropertyPayloadBuilder.buildConfigProperties(action, SCOPE, vfModuleObject) + '}';

        return Optional.of(requestBuilder);
    }

    @Override
    public void setAction(String action) {
        this.action = action;
    }

    private JsonObject buildPropertyObjectForVfModule(ExtractPojosForBB extractPojosForBB,
            BuildingBlockExecution execution) {

        try {
            vfModuleObject = new JsonObject();

            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);

            vfModuleObject.addProperty("service-instance-id", serviceInstance.getServiceInstanceId());
            vfModuleObject.addProperty("service-model-uuid",
                    serviceInstance.getModelInfoServiceInstance().getModelUuid());
            vfModuleObject.addProperty("vnf-id", genericVnf.getVnfId());
            vfModuleObject.addProperty("vnf-name", genericVnf.getVnfName());
            vfModuleObject.addProperty("vf-module-id", vfModule.getVfModuleId());
            vfModuleObject.addProperty("vf-module-name", vfModule.getVfModuleName());
            vfModuleObject.addProperty("vf-module-customization-uuid",
                    vfModule.getModelInfoVfModule().getModelCustomizationUUID());

            // TODO Add instance parameters based on action.

        } catch (BBObjectNotFoundException e) {
            // TODO Add error handling.
            e.printStackTrace();
        }

        return vfModuleObject;
    }

    // TODO Add logic to create template-prefix.
    private String getTemplatePrefix() {
        return "template-prefix";
    }
}
