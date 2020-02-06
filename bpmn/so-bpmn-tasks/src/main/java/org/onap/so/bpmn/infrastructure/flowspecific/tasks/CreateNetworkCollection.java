/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Modifications Copyright (c) 2020 Nokia
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

import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Collection;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAINetworkResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateNetworkCollection {

    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private ExtractPojosForBB extractPojosForBB;
    @Autowired
    private AAINetworkResources aaiNetworkResources;

    private static final String UNDERSCORE = "_";
    private static final String NETWORK_COLLECTION_NAME = "networkCollectionName";

    /**
     * BPMN access method to build Network Collection Name
     * 
     * @param execution
     * @throws Exception
     */
    public void buildNetworkCollectionName(BuildingBlockExecution execution) throws Exception {
        try {
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            InstanceGroup instanceGroup = serviceInstance.getCollection().getInstanceGroup();
            if (instanceGroup.getModelInfoInstanceGroup() != null) {
                // Build collection name assembling SI name and IG function
                if (serviceInstance.getServiceInstanceName() != null
                        && instanceGroup.getModelInfoInstanceGroup().getFunction() != null) {
                    String networkCollectionName = serviceInstance.getServiceInstanceName().concat(UNDERSCORE)
                            .concat(instanceGroup.getModelInfoInstanceGroup().getFunction());
                    // set networkCollectionName object on execution to be re-used within current BB
                    execution.setVariable(NETWORK_COLLECTION_NAME, networkCollectionName);
                } else {
                    throw new IllegalArgumentException(
                            "Cannot generate collection name because either one or both fields are null: "
                                    + " Service Instance Name: " + serviceInstance.getServiceInstanceName()
                                    + ", Instance Group Function: "
                                    + instanceGroup.getModelInfoInstanceGroup().getFunction());
                }
            } else {
                throw new IllegalArgumentException("Instance group model info is null");
            }
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to connect Network Collection
     * 
     * @param execution
     * @throws Exception
     */
    public void connectCollectionToInstanceGroup(BuildingBlockExecution execution) throws Exception {
        execution.setVariable("connectCollectionToInstanceGroupRollback", false);
        try {
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            Collection networkCollection = serviceInstance.getCollection();
            aaiNetworkResources.connectNetworkCollectionInstanceGroupToNetworkCollection(
                    networkCollection.getInstanceGroup(), networkCollection);
            execution.setVariable("connectCollectionToInstanceGroupRollback", true);
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to connect Instance Group to Cloud Region
     * 
     * @param execution
     * @throws Exception
     */
    public void connectInstanceGroupToCloudRegion(BuildingBlockExecution execution) throws Exception {
        execution.setVariable("connectInstanceGroupToCloudRegionRollback", false);
        try {
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            Collection networkCollection = serviceInstance.getCollection();
            aaiNetworkResources.connectInstanceGroupToCloudRegion(networkCollection.getInstanceGroup(),
                    execution.getGeneralBuildingBlock().getCloudRegion());
            execution.setVariable("connectInstanceGroupToCloudRegionRollback", true);
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to connect Network Collection
     * 
     * @param execution
     * @throws Exception
     */
    public void connectCollectionToServiceInstance(BuildingBlockExecution execution) throws Exception {
        execution.setVariable("connectCollectionToServiceInstanceRollback", false);
        try {
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            Collection networkCollection = serviceInstance.getCollection();
            aaiNetworkResources.connectNetworkCollectionToServiceInstance(networkCollection, serviceInstance);
            execution.setVariable("connectCollectionToServiceInstanceRollback", true);
        } catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }
}
