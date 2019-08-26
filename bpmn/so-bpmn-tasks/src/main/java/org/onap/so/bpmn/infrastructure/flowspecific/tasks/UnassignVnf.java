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

package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import java.util.List;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.infrastructure.common.name.generation.AAIObjectInstanceNameGenerator;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.modelinfo.ModelInfoInstanceGroup;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAIInstanceGroupResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component()
public class UnassignVnf {
    private static final Logger logger = LoggerFactory.getLogger(UnassignVnf.class);
    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private ExtractPojosForBB extractPojosForBB;
    @Autowired
    private AAIInstanceGroupResources aaiInstanceGroupResources;
    @Autowired
    private AAIObjectInstanceNameGenerator aaiObjectInstanceNameGenerator;

    /**
     * BPMN access method to deleting instanceGroup in AAI.
     *
     * It will extract the vnf from BBobject ,It will get the instance group from the vnf and add it into a list.
     *
     * Then iterate that list and check the ModelInfoInstanceGroup type.
     *
     * Then it will delete that.
     *
     * @param execution
     */
    public void deleteInstanceGroups(BuildingBlockExecution execution) {
        try {
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            List<InstanceGroup> instanceGroups = vnf.getInstanceGroups();
            for (InstanceGroup instanceGroup : instanceGroups) {
                if (ModelInfoInstanceGroup.TYPE_VNFC
                        .equalsIgnoreCase(instanceGroup.getModelInfoInstanceGroup().getType())) {
                    aaiInstanceGroupResources.deleteInstanceGroup(instanceGroup);
                }
            }
        } catch (Exception ex) {
            logger.error("Exception occurred in UnassignVnf deleteInstanceGroups", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }
}
