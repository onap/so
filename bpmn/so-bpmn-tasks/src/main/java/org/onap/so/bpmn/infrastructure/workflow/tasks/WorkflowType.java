/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

public enum WorkflowType {

    SERVICE("Service"),
    VNF("Vnf"),
    PNF("Pnf"),
    VFMODULE("VfModule"),
    VOLUMEGROUP("VolumeGroup"),
    NETWORK("Network"),
    VIRTUAL_LINK("VirtualLink"),
    NETWORKCOLLECTION("NetworkCollection"),
    CONFIGURATION("Configuration"),
    INSTANCE_GROUP("InstanceGroup"),
    NETWORK_SLICE_SUBNET("NetworkSliceSubnet"),
    CNF("Cnf");

    private final String type;

    private WorkflowType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }

    public static WorkflowType fromString(String text) {
        for (WorkflowType x : WorkflowType.values()) {
            if (x.type.equalsIgnoreCase(text)) {
                return x;
            }
        }
        return null;
    }
}
