/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Nokia Intellectual Property. All rights reserved.
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
package org.onap.so.bpmn.servicedecomposition.tasks;

import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ConfigurationResourceKeys;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.serviceinstancebeans.RequestDetails;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseBBInputSetupTest {

    protected ExecuteBuildingBlock changeInEbbALaCarte(ExecuteBuildingBlock executeBB, Boolean aLaCarte) {
        return executeBB.copyAndChangeExecuteBuildingBlock(new ExecuteBuildingBlock.Builder().withaLaCarte(aLaCarte));
    }

    protected ExecuteBuildingBlock changeInBbFlowName(ExecuteBuildingBlock executeBB, String flowName) {
        BuildingBlock changedBuildingBlock = executeBB.getBuildingBlock().copyAndChangeBuildingBlock(flowName);
        ExecuteBuildingBlock.Builder executeBuildingBlockBuilder =
                new ExecuteBuildingBlock.Builder().withBuildingBlock(changedBuildingBlock);
        return executeBB.copyAndChangeExecuteBuildingBlock(executeBuildingBlockBuilder);
    }

    protected ExecuteBuildingBlock changeInBbKey(ExecuteBuildingBlock executeBB, String key) {
        BuildingBlock.Builder builder = new BuildingBlock.Builder().withKey(key);
        BuildingBlock changedBuildingBlock = executeBB.getBuildingBlock().copyAndChangeBuildingBlock(builder);
        ExecuteBuildingBlock.Builder executeBuildingBlockBuilder =
                new ExecuteBuildingBlock.Builder().withBuildingBlock(changedBuildingBlock);
        return executeBB.copyAndChangeExecuteBuildingBlock(executeBuildingBlockBuilder);
    }

    protected ExecuteBuildingBlock changeInBbIsVirtualLink(ExecuteBuildingBlock executeBB, Boolean isVirtualLink) {
        BuildingBlock.Builder builder = new BuildingBlock.Builder().withIsVirtualLink(isVirtualLink);
        BuildingBlock changedBuildingBlock = executeBB.getBuildingBlock().copyAndChangeBuildingBlock(builder);
        ExecuteBuildingBlock.Builder executeBuildingBlockBuilder =
                new ExecuteBuildingBlock.Builder().withBuildingBlock(changedBuildingBlock);
        return executeBB.copyAndChangeExecuteBuildingBlock(executeBuildingBlockBuilder);
    }

    protected ExecuteBuildingBlock changeInEbbConfResourceKeys(ExecuteBuildingBlock executeBB,
            ConfigurationResourceKeys keys) {
        ExecuteBuildingBlock.Builder executeBuildingBlockBuilder =
                new ExecuteBuildingBlock.Builder().withConfigurationResourceKeys(keys);
        return executeBB.copyAndChangeExecuteBuildingBlock(executeBuildingBlockBuilder);
    }

    protected ExecuteBuildingBlock changeInEbbRequestDetails(ExecuteBuildingBlock executeBB, RequestDetails details) {
        ExecuteBuildingBlock.Builder executeBuildingBlockBuilder =
                new ExecuteBuildingBlock.Builder().withRequestDetails(details);
        return executeBB.copyAndChangeExecuteBuildingBlock(executeBuildingBlockBuilder);
    }

    protected Map<ResourceKey, String> prepareLookupKeyMap() {
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        lookupKeyMap.put(ResourceKey.NETWORK_ID, "networkId");
        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, "vnfId");
        lookupKeyMap.put(ResourceKey.VF_MODULE_ID, "vfModuleId");
        lookupKeyMap.put(ResourceKey.VOLUME_GROUP_ID, "volumeGroupId");
        lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, "serviceInstanceId");
        lookupKeyMap.put(ResourceKey.CONFIGURATION_ID, "configurationId");
        return lookupKeyMap;
    }

    protected ConfigurationResourceKeys prepareConfigurationResourceKeys() {
        ConfigurationResourceKeys configResourceKeys = new ConfigurationResourceKeys();
        configResourceKeys.setCvnfcCustomizationUUID("cvnfcCustomizationUUID");
        configResourceKeys.setVfModuleCustomizationUUID("vfModuleCustomizationUUID");
        configResourceKeys.setVnfResourceCustomizationUUID("vnfResourceCustomizationUUID");
        return configResourceKeys;
    }
}
