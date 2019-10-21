/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 TechMahindra.
 * Copyright (C) 2019 Nokia.
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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ConfigurePayloadForCds {

    private BuildingBlockExecution execution;
    private String scope;
    private String action;
    private ExtractPojosForBB extractPojosForBB;
    private static final Map<String, Supplier<Action>> WORKFLOWACTION;
    private GeneralBuildingBlock generalBuildingBlock;
    private Supplier<Action> actionSupplier;

    // At the moment we are reading instance parameters details from SO payload when we are doing config-assign
    // but if we need to send some more details(configuration details) to CDS based on the type of action
    // then in this case implement a class and add it in this map.
    static {
        final Map<String, Supplier<Action>> actions = new HashMap<>();
        actions.put("vnf-assign", ConfigureInstanceParamsForVnf::new);
//        actions.put("vf-module-assign", ConfigureInstanceParamsForVfModule::new);
        WORKFLOWACTION = Collections.unmodifiableMap(actions);
    }

    public ConfigurePayloadForCds(BuildingBlockExecution execution, String scope, String action, ExtractPojosForBB pojosForBB) {
        this.execution = execution;
        this.scope = scope;
        this.action = action;
        this.extractPojosForBB = pojosForBB;
        this.generalBuildingBlock = execution.getGeneralBuildingBlock();
    }

    public String buildPayloadForCds() {
        JsonArray jsonElements = generateConfigPropertiesPayload();
        return buildRequestPayload(jsonElements);
    }

    private JsonArray generateConfigPropertiesPayload() {
        try {
            final String workflowAction = scope + "-" + action;
            actionSupplier = WORKFLOWACTION.get(workflowAction);
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            switch (scope) {
                // TODO Need to figure it out how I will set blueprint name and blueprint version for Each VNF or VF-MODULE
                case "vnf":
                    JsonArray jsonElementsForVnf = new JsonArray();
                    List<GenericVnf> genericVnfs = serviceInstance.getVnfs();
                    genericVnfs.stream()
                            .map(genericVnf -> buildJsonObjectForVnf(serviceInstance, genericVnf))
                            .forEachOrdered(jsonElementsForVnf::add);
                    return jsonElementsForVnf;
                case "vf-module":
                    JsonArray jsonElementsForVfModule = new JsonArray();
                    serviceInstance.getVnfs().forEach(genericVnf ->
                            genericVnf.getVfModules()
                                    .stream()
                                    .map(vfModule -> buildJsonObjectForVfModule(serviceInstance, vfModule, genericVnf))
                                    .forEachOrdered(jsonElementsForVfModule::add));
                    return jsonElementsForVfModule;
            }
        }
        catch(Exception ex) {
            // TODO error handling
        }
        return new JsonArray();
    }

    private JsonObject buildJsonObjectForVnf(ServiceInstance serviceInstance, GenericVnf genericVnf) {
        JsonObject vnfObject = new JsonObject();
        vnfObject.addProperty("service-instance-id", serviceInstance.getServiceInstanceId());
        vnfObject.addProperty("service-model-uuid", serviceInstance.getModelInfoServiceInstance().getModelUuid());
        vnfObject.addProperty("vnf-id", genericVnf.getVnfId());
        vnfObject.addProperty("vnf-name", genericVnf.getVnfName());
        vnfObject.addProperty("vnf-customization-uuid",genericVnf.getModelInfoGenericVnf().getModelCustomizationUuid());
        actionSupplier.get().populateJsonBasedOnAction(vnfObject, generalBuildingBlock, genericVnf.getModelInfoGenericVnf().getModelCustomizationUuid());
        return vnfObject;
    }

    private JsonObject buildJsonObjectForVfModule(ServiceInstance serviceInstance, VfModule vfModule, GenericVnf genericVnf) {
        JsonObject vfModuleObject = new JsonObject();
        vfModuleObject.addProperty("service-instance-id", serviceInstance.getServiceInstanceId());
        vfModuleObject.addProperty("service-model-uuid", serviceInstance.getModelInfoServiceInstance().getModelUuid());
        vfModuleObject.addProperty("vnf-id", genericVnf.getVnfId());
        vfModuleObject.addProperty("vnf-name", genericVnf.getVnfName());
        vfModuleObject.addProperty("vf-module-id", vfModule.getVfModuleId());
        vfModuleObject.addProperty("vf-module-name", vfModule.getVfModuleName());
        vfModuleObject.addProperty("vf-module-customization-uuid", vfModule.getModelInfoVfModule().getModelCustomizationUUID());
        actionSupplier.get().populateJsonBasedOnAction(vfModuleObject, generalBuildingBlock, vfModule.getModelInfoVfModule().getModelCustomizationUUID());
        return vfModuleObject;
    }

    /**
     * {
     *
     *         "config-(assign/deploy)-request": {
     *             "resolution-key": "demo-30/05",
     *             "config-(assign/deploy)-properties": {
     *                // Property Payload
     *             }
     *         }
     *  }
     */
    private String buildRequestPayload(JsonArray jsonElements) {
        final StringBuilder requestBuilder = new StringBuilder();
        requestBuilder.append("{\"").append("config-").append(action).append("-").append(scope).append("-request\":{");
        requestBuilder.append("\"resolution-key\":").append("\"").append("resolutionKey").append("\",");
        requestBuilder.append(ConfigProperties.buildPropertyPayloadFromJsonElements(action, scope, jsonElements));
        requestBuilder.append('}');
        return requestBuilder.toString();
    }
}
