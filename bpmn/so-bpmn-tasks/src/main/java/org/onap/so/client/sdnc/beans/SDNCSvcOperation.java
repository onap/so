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

package org.onap.so.client.sdnc.beans;

public enum SDNCSvcOperation {

    VF_MODULE_TOPOLOGY_OPERATION("vf-module-topology-operation"),
    NETWORK_TOPOLOGY_OPERATION("network-topology-operation"),
    VNF_TOPOLOGY_OPERATION("vnf-topology-operation"),
    CONTRAIL_ROUTE_TOPOLOGY_OPERATION("contrail-route-topology-operation"),
    SECURITY_ZONE_TOPOLOGY_OPERATION("security-zone-topology-operation"),
    PORT_MIRROR_TOPOLOGY_OPERATION("port-mirror-topology-operation"),
    SERVICE_TOPOLOGY_OPERATION("service-topology-operation"),
    GENERIC_CONFIGURATION_TOPOLOGY_OPERATION("generic-configuration-topology-operation");

    private final String name;

    private SDNCSvcOperation(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
