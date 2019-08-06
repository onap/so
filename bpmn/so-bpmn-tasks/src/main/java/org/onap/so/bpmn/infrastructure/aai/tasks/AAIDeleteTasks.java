/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

package org.onap.so.bpmn.infrastructure.aai.tasks;


import java.util.List;
import java.util.Optional;
import org.onap.aai.domain.yang.NetworkPolicies;
import org.onap.aai.domain.yang.NetworkPolicy;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.aai.AAIObjectPlurals;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.aai.entities.uri.AAIUriFactory;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAIConfigurationResources;
import org.onap.so.client.orchestration.AAIInstanceGroupResources;
import org.onap.so.client.orchestration.AAINetworkResources;
import org.onap.so.client.orchestration.AAIServiceInstanceResources;
import org.onap.so.client.orchestration.AAIVfModuleResources;
import org.onap.so.client.orchestration.AAIVnfResources;
import org.onap.so.client.orchestration.AAIVolumeGroupResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AAIDeleteTasks {
    private static final Logger logger = LoggerFactory.getLogger(AAIDeleteTasks.class);

    private static String contrailNetworkPolicyFqdnList = "contrailNetworkPolicyFqdnList";
    private static String networkPolicyFqdnParam = "network-policy-fqdn";

    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private ExtractPojosForBB extractPojosForBB;
    @Autowired
    private AAIServiceInstanceResources aaiSIResources;
    @Autowired
    private AAIVnfResources aaiVnfResources;
    @Autowired
    private AAIVfModuleResources aaiVfModuleResources;
    @Autowired
    private AAINetworkResources aaiNetworkResources;
    @Autowired
    private AAIVolumeGroupResources aaiVolumeGroupResources;
    @Autowired
    private AAIConfigurationResources aaiConfigurationResources;
    @Autowired
    private AAIInstanceGroupResources aaiInstanceGroupResources;

    /**
     * BPMN access method to delete the VfModule from A&AI.
     *
     * @param execution
     */
    public void deleteVfModule(BuildingBlockExecution execution) throws Exception {
        logger.debug("STARTED AAIDeleteTasks deleteVfModule process");
        GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
        VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);

        execution.setVariable("aaiVfModuleRollback", false);
        try {
            aaiVfModuleResources.deleteVfModule(vfModule, genericVnf);
            execution.setVariable("aaiVfModuleRollback", true);
        } catch (Exception ex) {
            logger.debug("Exception occurred in  AAIDeleteTasks deleteVfModule process");
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
        logger.debug("ENDED AAIDeleteTasks deleteVfModule process");
    }

    /**
     * BPMN access method to delete the Vnf from A&AI.
     *
     * @param execution
     */
    public void deleteVnf(BuildingBlockExecution execution) throws Exception {
        logger.debug("STARTED AAIDeleteTasks deleteVnf process");
        GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);

        execution.setVariable("aaiVnfRollback", false);
        try {
            aaiVnfResources.deleteVnf(genericVnf);
            execution.setVariable("aaiVnfRollback", true);
        } catch (Exception ex) {
            logger.debug("Exception occurred in  AAIDeleteTasks deleteVnf process");
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
        logger.debug("ENDED AAIDeleteTasks deleteVnf process");
    }

    /**
     * BPMN access method to delete the ServiceInstance from A&AI.
     *
     * @param execution
     */
    public void deleteServiceInstance(BuildingBlockExecution execution) throws Exception {
        logger.debug("STARTED AAIDeleteTasks deleteServiceInstance process");
        try {
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            aaiSIResources.deleteServiceInstance(serviceInstance);
        } catch (Exception ex) {
            logger.debug("Exception occurred in  AAIDeleteTasks deleteServiceInstance");
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
        logger.debug("ENDED AAIDeleteTasks deleteServiceInstance");

    }

    /**
     * BPMN access method to delete the L3 Network from A&AI.
     *
     * @param execution
     */
    public void deleteNetwork(BuildingBlockExecution execution) throws Exception {
        logger.debug("STARTED AAIDeleteTasks deleteNetwork process");
        try {
            L3Network l3network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);
            aaiNetworkResources.deleteNetwork(l3network);
            execution.setVariable("isRollbackNeeded", true);
        } catch (Exception ex) {
            logger.debug("Exception occurred in  AAIDeleteTasks deleteNetwork");
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
        logger.debug("ENDED AAIDeleteTasks deleteNetwork");
    }

    /**
     * BPMN access method to delete the collection from A&AI.
     *
     * @param execution
     */
    public void deleteCollection(BuildingBlockExecution execution) throws Exception {
        logger.debug("STARTED AAIDeleteTasks deleteCollection process");
        try {
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            aaiNetworkResources.deleteCollection(serviceInstance.getCollection());
        } catch (Exception ex) {
            logger.debug("Exception occurred in  AAIDeleteTasks deleteCollection");
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
        logger.debug("ENDED AAIDeleteTasks deleteCollection");
    }

    /**
     * BPMN access method to delete the InstanceGroup from A&AI.
     *
     * @param execution
     */
    public void deleteInstanceGroup(BuildingBlockExecution execution) throws Exception {
        logger.debug("STARTED AAIDeleteTasks deleteInstanceGroup process");
        try {
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            aaiNetworkResources.deleteNetworkInstanceGroup(serviceInstance.getCollection().getInstanceGroup());
        } catch (Exception ex) {
            logger.debug("Exception occurred in  AAIDeleteTasks deleteInstanceGroup");
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
        logger.debug("ENDED AAIDeleteTasks deleteInstanceGroup");
    }

    /**
     * BPMN access method to delete the VolumeGroup from A&AI.
     *
     * @param execution
     */
    public void deleteVolumeGroup(BuildingBlockExecution execution) {
        logger.debug("STARTED AAIDeleteTasks deleteVolumeGroup process");
        try {
            VolumeGroup volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID);
            CloudRegion cloudRegion = execution.getGeneralBuildingBlock().getCloudRegion();
            aaiVolumeGroupResources.deleteVolumeGroup(volumeGroup, cloudRegion);
        } catch (Exception ex) {
            logger.debug("Exception occurred in  AAIDeleteTasks deleteVolumeGroup");
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
        logger.debug("ENDED AAIDeleteTasks deleteVolumeGroup");
    }

    /**
     * BPMN access method to delete the Configuration from A&AI.
     *
     * @param execution
     */
    public void deleteConfiguration(BuildingBlockExecution execution) {
        logger.debug("STARTED AAIDeleteTasks deleteConfiguration process");
        try {
            Configuration configuration = extractPojosForBB.extractByKey(execution, ResourceKey.CONFIGURATION_ID);
            aaiConfigurationResources.deleteConfiguration(configuration);
        } catch (Exception ex) {
            logger.debug("Exception occurred in AAIDeleteTasks deleteConfiguration");
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
        logger.debug("ENDED AAIDeleteTasks deleteConfiguration");
    }

    /**
     * BPMN access method to delete the InstanceGroup of Vnf from A&AI.
     *
     * @param execution
     */
    public void deleteInstanceGroupVnf(BuildingBlockExecution execution) {
        logger.debug("STARTED AAIDeleteTasks deleteInstanceGroupVnf process");
        try {
            InstanceGroup instanceGroup = extractPojosForBB.extractByKey(execution, ResourceKey.INSTANCE_GROUP_ID);
            aaiInstanceGroupResources.deleteInstanceGroup(instanceGroup);
        } catch (Exception ex) {
            logger.debug("Exception occurred in  AAIDeleteTasks deleteInstanceGroupVnf");
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
        logger.debug("ENDED AAIDeleteTasks deleteInstanceGroupVnf");
    }

    /**
     * BPMN access method to delete the Network Policies from A&AI.
     *
     * @param execution
     */
    public void deleteNetworkPolicies(BuildingBlockExecution execution) {
        logger.debug("STARTED AAIDeleteTasks deleteNetworkPolicies process");
        try {
            String fqdns = execution.getVariable(contrailNetworkPolicyFqdnList);
            if (fqdns != null && !fqdns.isEmpty()) {
                String[] fqdnList = fqdns.split(",");
                int fqdnCount = fqdnList.length;
                if (fqdnCount > 0) {
                    for (int i = 0; i < fqdnCount; i++) {
                        String fqdn = fqdnList[i];
                        AAIResourceUri uri = AAIUriFactory.createResourceUri(AAIObjectPlurals.NETWORK_POLICY);
                        uri.queryParam(networkPolicyFqdnParam, fqdn);
                        Optional<NetworkPolicies> oNetPolicies = aaiNetworkResources.getNetworkPolicies(uri);
                        if (oNetPolicies.isPresent()) {
                            NetworkPolicies networkPolicies = oNetPolicies.get();
                            List<NetworkPolicy> networkPolicyList = networkPolicies.getNetworkPolicy();
                            if (networkPolicyList != null && !networkPolicyList.isEmpty()) {
                                NetworkPolicy networkPolicy = networkPolicyList.get(0);
                                String networkPolicyId = networkPolicy.getNetworkPolicyId();
                                logger.debug("Deleting network-policy with network-policy-id {}", networkPolicyId);
                                aaiNetworkResources.deleteNetworkPolicy(networkPolicyId);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.debug("Exception occurred in  AAIDeleteTasks deleteNetworkPolicies");
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
        logger.debug("ENDED AAIDeleteTasks deleteNetworkPolicies");
    }
}
