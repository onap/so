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

package org.onap.so.bpmn.infrastructure.common.name.generation;

import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.springframework.stereotype.Component;

@Component
public class AAIObjectInstanceNameGenerator {

    public String generateInstanceGroupName(InstanceGroup instanceGroup, GenericVnf vnf) {
        if (vnf.getVnfName() != null && instanceGroup.getModelInfoInstanceGroup().getFunction() != null) {
            return vnf.getVnfName() + "_" + instanceGroup.getModelInfoInstanceGroup().getFunction();
        } else {
            throw new IllegalArgumentException(
                    "Cannot generate instance group name because either one or both fields are null: "
                            + " Vnf Instance Name: " + vnf.getVnfName() + ", Instance Group Function: "
                            + instanceGroup.getModelInfoInstanceGroup().getFunction());
        }
    }

}
