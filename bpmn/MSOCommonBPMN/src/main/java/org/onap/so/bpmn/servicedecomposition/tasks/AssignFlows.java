/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

public enum AssignFlows {

    SERVICE_INSTANCE("AssignServiceInstanceBB"),
    VNF("AssignVnfBB"),
    VF_MODULE("AssignVfModuleBB"),
    NETWORK_A_LA_CARTE("AssignNetwork1802BB"),
    NETWORK_MACRO("AssignNetworkBB"),
    VOLUME_GROUP("AssignVolumeGroupBB"),
    NETWORK_COLLECTION("CreateNetworkCollectionBB"),
    FABRIC_CONFIGURATION("AddFabricConfigurationBB"),
    VRF_CONFIGURATION("AssignVrfConfigurationBBV2");

    private final String flowName;

    private AssignFlows(String flowName) {
        this.flowName = flowName;
    }

    @Override
    public String toString() {
        return this.flowName;
    }
}
