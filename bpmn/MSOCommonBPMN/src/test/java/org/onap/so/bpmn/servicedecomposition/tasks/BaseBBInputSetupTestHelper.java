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

import org.onap.so.bpmn.servicedecomposition.entities.ConfigurationResourceKeys;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import java.util.HashMap;
import java.util.Map;

public class BaseBBInputSetupTestHelper {

    public static Map<ResourceKey, String> prepareLookupKeyMap() {
        Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
        lookupKeyMap.put(ResourceKey.NETWORK_ID, "networkId");
        lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, "vnfId");
        lookupKeyMap.put(ResourceKey.VF_MODULE_ID, "vfModuleId");
        lookupKeyMap.put(ResourceKey.VOLUME_GROUP_ID, "volumeGroupId");
        lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, "serviceInstanceId");
        lookupKeyMap.put(ResourceKey.CONFIGURATION_ID, "configurationId");
        return lookupKeyMap;
    }

    public static ConfigurationResourceKeys prepareConfigurationResourceKeys() {
        ConfigurationResourceKeys configResourceKeys = new ConfigurationResourceKeys();
        configResourceKeys.setCvnfcCustomizationUUID("cvnfcCustomizationUUID");
        configResourceKeys.setVfModuleCustomizationUUID("vfModuleCustomizationUUID");
        configResourceKeys.setVnfResourceCustomizationUUID("vnfResourceCustomizationUUID");
        return configResourceKeys;
    }
}
