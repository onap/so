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
import java.util.Map;
import org.onap.so.adapters.nwrest.CreateNetworkResponse;
import org.onap.so.adapters.nwrest.UpdateNetworkResponse;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Collection;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Pnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Subnet;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAICollectionResources;
import org.onap.so.client.orchestration.AAIConfigurationResources;
import org.onap.so.client.orchestration.AAINetworkResources;
import org.onap.so.client.orchestration.AAIPnfResources;
import org.onap.so.client.orchestration.AAIServiceInstanceResources;
import org.onap.so.client.orchestration.AAIVfModuleResources;
import org.onap.so.client.orchestration.AAIVnfResources;
import org.onap.so.client.orchestration.AAIVolumeGroupResources;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AAIUpdateTasks {
    private static final Logger logger = LoggerFactory.getLogger(AAIUpdateTasks.class);
    private static final String ALACARTE = "aLaCarte";
    private static final String MULTI_STAGE_DESIGN_OFF = "false";
    private static final String MULTI_STAGE_DESIGN_ON = "true";
    @Autowired
    private AAIServiceInstanceResources aaiServiceInstanceResources;
    @Autowired
    private AAIVnfResources aaiVnfResources;
    @Autowired
    private AAIVfModuleResources aaiVfModuleResources;
    @Autowired
    private ExceptionBuilder exceptionUtil;
    @Autowired
    private ExtractPojosForBB extractPojosForBB;
    @Autowired
    private AAIVolumeGroupResources aaiVolumeGroupResources;
    @Autowired
    private AAINetworkResources aaiNetworkResources;
    @Autowired
    private AAICollectionResources aaiCollectionResources;
    @Autowired
    private AAIConfigurationResources aaiConfigurationResources;
    @Autowired
    private AAIPnfResources aaiPnfResources;

    /**
     * BPMN access method to update the status of Service to Assigned in AAI
     *
     * @param execution
     */
    public void updateOrchestrationStatusAssignedService(BuildingBlockExecution execution) {
        try {
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            aaiServiceInstanceResources.updateOrchestrationStatusServiceInstance(serviceInstance,
                    OrchestrationStatus.ASSIGNED);
            execution.setVariable("aaiServiceInstanceRollback", true);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateOrchestrationStatusAssignedService", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update status of Service to Active in AAI
     *
     * @param execution
     */
    public void updateOrchestrationStatusActiveService(BuildingBlockExecution execution) {
        try {
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            aaiServiceInstanceResources.updateOrchestrationStatusServiceInstance(serviceInstance,
                    OrchestrationStatus.ACTIVE);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateOrchestrationStatusActiveService", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }


    /**
     * BPMN access method to update status of Pnf to Active in AAI
     *
     * @param execution
     */
    public void updateOrchestrationStatusActivePnf(BuildingBlockExecution execution) {
        try {
            Pnf pnf = extractPojosForBB.extractByKey(execution, ResourceKey.PNF);
            aaiPnfResources.updateOrchestrationStatusPnf(pnf, OrchestrationStatus.ACTIVE);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateOrchestrationStatusActivePnf", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update status of Vnf to Assigned in AAI
     *
     * @param execution
     */
    public void updateOrchestrationStatusAssignedVnf(BuildingBlockExecution execution) {
        try {
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            aaiVnfResources.updateOrchestrationStatusVnf(vnf, OrchestrationStatus.ASSIGNED);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateOrchestrationStatusAssignedVnf", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update status of Vnf to Active in AAI
     *
     * @param execution
     */
    public void updateOrchestrationStatusActiveVnf(BuildingBlockExecution execution) {
        try {
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            aaiVnfResources.updateOrchestrationStatusVnf(vnf, OrchestrationStatus.ACTIVE);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateOrchestrationStatusActiveVnf", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update status of VolumeGroup to Assigned in AAI
     *
     * @param execution
     */
    public void updateOrchestrationStatusAssignedVolumeGroup(BuildingBlockExecution execution) {
        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();

            VolumeGroup volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID);
            CloudRegion cloudRegion = gBBInput.getCloudRegion();
            volumeGroup.setHeatStackId("");
            aaiVolumeGroupResources.updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion,
                    OrchestrationStatus.ASSIGNED);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateOrchestrationStatusAssignedVolumeGroup", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update status of VolumeGroup to Active in AAI
     *
     * @param execution
     */
    public void updateOrchestrationStatusActiveVolumeGroup(BuildingBlockExecution execution) {
        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();

            VolumeGroup volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID);
            CloudRegion cloudRegion = gBBInput.getCloudRegion();

            aaiVolumeGroupResources.updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion,
                    OrchestrationStatus.ACTIVE);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateOrchestrationStatusActiveVolumeGroup", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update status of VolumeGroup to Created in AAI
     *
     * @param execution
     */
    public void updateOrchestrationStatusCreatedVolumeGroup(BuildingBlockExecution execution) {
        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();

            VolumeGroup volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID);
            CloudRegion cloudRegion = gBBInput.getCloudRegion();

            aaiVolumeGroupResources.updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion,
                    OrchestrationStatus.CREATED);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateOrchestrationStatusCreatedVolumeGroup", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update HeatStackId and VolumeGroup in AAI
     *
     * @param execution
     */
    public void updateHeatStackIdVolumeGroup(BuildingBlockExecution execution) {
        try {
            GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
            String heatStackId = execution.getVariable("heatStackId");
            if (heatStackId == null) {
                heatStackId = "";
            }
            VolumeGroup volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID);
            CloudRegion cloudRegion = gBBInput.getCloudRegion();
            volumeGroup.setHeatStackId(heatStackId);

            aaiVolumeGroupResources.updateHeatStackIdVolumeGroup(volumeGroup, cloudRegion);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateHeatStackIdVolumeGroup", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update status of VfModule to Assigned in AAI
     *
     * @param execution
     */
    public void updateOrchestrationStatusAssignedVfModule(BuildingBlockExecution execution) {
        try {
            VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            vfModule.setHeatStackId("");
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            aaiVfModuleResources.updateOrchestrationStatusVfModule(vfModule, vnf, OrchestrationStatus.ASSIGNED);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateOrchestrationStatusAssignedVfModule", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update status of VfModule to PendingActivation in AAI
     *
     * @param execution
     */
    public void updateOrchestrationStatusPendingActivationVfModule(BuildingBlockExecution execution) {
        try {
            VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            aaiVfModuleResources.updateOrchestrationStatusVfModule(vfModule, vnf,
                    OrchestrationStatus.PENDING_ACTIVATION);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateOrchestrationStatusPendingActivationVfModule", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update status of VfModule to AssignedOrPendingActivation in AAI
     *
     * @param execution
     */
    public void updateOrchestrationStatusAssignedOrPendingActivationVfModule(BuildingBlockExecution execution) {
        try {
            VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            vfModule.setHeatStackId("");
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            String multiStageDesign = MULTI_STAGE_DESIGN_OFF;
            if (vnf.getModelInfoGenericVnf() != null) {
                multiStageDesign = vnf.getModelInfoGenericVnf().getMultiStageDesign();
            }
            boolean aLaCarte = (boolean) execution.getVariable(ALACARTE);
            if (aLaCarte && multiStageDesign != null && multiStageDesign.equalsIgnoreCase(MULTI_STAGE_DESIGN_ON)) {
                aaiVfModuleResources.updateOrchestrationStatusVfModule(vfModule, vnf,
                        OrchestrationStatus.PENDING_ACTIVATION);
            } else {
                aaiVfModuleResources.updateOrchestrationStatusVfModule(vfModule, vnf, OrchestrationStatus.ASSIGNED);
            }
        } catch (Exception ex) {
            logger.error(
                    "Exception occurred in AAIUpdateTasks updateOrchestrationStatusAssignedOrPendingActivationVfModule",
                    ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update status of VfModule to Created in AAI
     *
     * @param execution
     *
     */
    public void updateOrchestrationStatusCreatedVfModule(BuildingBlockExecution execution) {
        try {
            VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            aaiVfModuleResources.updateOrchestrationStatusVfModule(vfModule, vnf, OrchestrationStatus.CREATED);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateOrchestrationStatusCreatedVfModule", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update aaiDeactivateVfModuleRollback to true for deactivating the VfModule
     *
     * @param execution
     * @throws buildAndThrowWorkflowException
     */
    public void updateOrchestrationStatusDeactivateVfModule(BuildingBlockExecution execution) {
        execution.setVariable("aaiDeactivateVfModuleRollback", false);
        try {
            VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            aaiVfModuleResources.updateOrchestrationStatusVfModule(vfModule, vnf, OrchestrationStatus.CREATED);
            execution.setVariable("aaiDeactivateVfModuleRollback", true);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateOrchestrationStatusDeactivateVfModule", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update status of L3Network to Assigned in AAI
     *
     * @param execution
     * @throws BBObjectNotFoundException
     */
    public void updateOrchestrationStatusAssignedNetwork(BuildingBlockExecution execution) {
        updateNetwork(execution, OrchestrationStatus.ASSIGNED);
    }

    /**
     * BPMN access method to update status of L3Network to Active in AAI
     *
     * @param execution
     * @throws BBObjectNotFoundException
     */
    public void updateOrchestrationStatusActiveNetwork(BuildingBlockExecution execution) {
        updateNetwork(execution, OrchestrationStatus.ACTIVE);
    }

    /**
     * BPMN access method to update status of L3Network to Created in AAI
     *
     * @param execution
     * @throws BBObjectNotFoundException
     */
    public void updateOrchestrationStatusCreatedNetwork(BuildingBlockExecution execution) {
        updateNetwork(execution, OrchestrationStatus.CREATED);
    }

    protected void updateNetwork(BuildingBlockExecution execution, OrchestrationStatus status) {
        try {
            L3Network l3Network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);
            updateNetworkAAI(l3Network, status);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateNetwork", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    protected void updateNetworkAAI(L3Network l3Network, OrchestrationStatus status) {
        L3Network copiedl3Network = l3Network.shallowCopyId();

        copiedl3Network.setOrchestrationStatus(status);
        l3Network.setOrchestrationStatus(status);
        aaiNetworkResources.updateNetwork(copiedl3Network);

        List<Subnet> subnets = l3Network.getSubnets();
        if (subnets != null) {
            for (Subnet subnet : subnets) {
                Subnet copiedSubnet = subnet.shallowCopyId();
                copiedSubnet.setOrchestrationStatus(status);
                aaiNetworkResources.updateSubnet(copiedl3Network, copiedSubnet);
            }
        }
    }

    /**
     * BPMN access method to update status of L3Network Collection to Active in AAI
     *
     * @param execution
     * @throws BBObjectNotFoundException
     */
    public void updateOrchestrationStatusActiveNetworkCollection(BuildingBlockExecution execution) {
        execution.setVariable("aaiNetworkCollectionActivateRollback", false);
        try {
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            Collection networkCollection = serviceInstance.getCollection();
            Collection copiedNetworkCollection = networkCollection.shallowCopyId();

            networkCollection.setOrchestrationStatus(OrchestrationStatus.ACTIVE);
            copiedNetworkCollection.setOrchestrationStatus(OrchestrationStatus.ACTIVE);
            aaiCollectionResources.updateCollection(copiedNetworkCollection);
            execution.setVariable("aaiNetworkCollectionActivateRollback", true);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateOrchestrationStatusActiveNetworkCollection", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update status of VfModule to Active in AAI
     *
     * @param execution
     */
    public void updateOrchestrationStatusActivateVfModule(BuildingBlockExecution execution) {
        execution.setVariable("aaiActivateVfModuleRollback", false);
        try {
            VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            aaiVfModuleResources.updateOrchestrationStatusVfModule(vfModule, vnf, OrchestrationStatus.ACTIVE);
            execution.setVariable("aaiActivateVfModuleRollback", true);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateOrchestrationStatusActivateVfModule", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update HeatStackId of VfModule in AAI
     *
     * @param execution
     */
    public void updateHeatStackIdVfModule(BuildingBlockExecution execution) {
        try {
            String heatStackId = execution.getVariable("heatStackId");
            if (heatStackId == null) {
                heatStackId = "";
            }
            VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            vfModule.setHeatStackId(heatStackId);
            aaiVfModuleResources.updateHeatStackIdVfModule(vfModule, vnf);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateHeatStackIdVfModule", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update L3Network after it was created in cloud
     *
     * @param execution
     * @throws Exception
     */
    public void updateNetworkCreated(BuildingBlockExecution execution) throws Exception {
        execution.setVariable("aaiNetworkActivateRollback", false);
        L3Network l3network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);
        L3Network copiedl3network = l3network.shallowCopyId();
        CreateNetworkResponse response = execution.getVariable("createNetworkResponse");
        try {
            if (response.getNetworkFqdn() != null) {
                l3network.setContrailNetworkFqdn(response.getNetworkFqdn());
            }
            l3network.setOrchestrationStatus(OrchestrationStatus.CREATED);
            l3network.setHeatStackId(response.getNetworkStackId());
            l3network.setNeutronNetworkId(response.getNeutronNetworkId());

            copiedl3network.setContrailNetworkFqdn(response.getNetworkFqdn());
            copiedl3network.setOrchestrationStatus(OrchestrationStatus.CREATED);
            copiedl3network.setHeatStackId(response.getNetworkStackId());
            copiedl3network.setNeutronNetworkId(response.getNeutronNetworkId());

            aaiNetworkResources.updateNetwork(copiedl3network);

            Map<String, String> subnetMap = response.getSubnetMap();
            List<Subnet> subnets = l3network.getSubnets();
            if (subnets != null && subnetMap != null) {
                for (Subnet subnet : subnets) {
                    Subnet copiedSubnet = subnet.shallowCopyId();
                    copiedSubnet.setNeutronSubnetId(subnetMap.get(copiedSubnet.getSubnetId()));
                    copiedSubnet.setOrchestrationStatus(OrchestrationStatus.CREATED);
                    aaiNetworkResources.updateSubnet(copiedl3network, copiedSubnet);
                }
            }

            execution.setVariable("aaiNetworkActivateRollback", true);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateNetworkCreated", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update L3Network after it was updated in cloud
     *
     * @param execution
     * @throws Exception
     */
    public void updateNetworkUpdated(BuildingBlockExecution execution) throws Exception {
        L3Network l3network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);
        L3Network copiedl3network = l3network.shallowCopyId();
        UpdateNetworkResponse response = execution.getVariable("updateNetworkResponse");
        try {
            copiedl3network.setNeutronNetworkId(response.getNeutronNetworkId());
            aaiNetworkResources.updateNetwork(copiedl3network);

            Map<String, String> subnetMap = response.getSubnetMap();
            List<Subnet> subnets = l3network.getSubnets();
            if (subnets != null && subnetMap != null) {
                for (Subnet subnet : subnets) {
                    Subnet copiedSubnet = subnet.shallowCopyId();
                    copiedSubnet.setNeutronSubnetId(subnetMap.get(copiedSubnet.getSubnetId()));
                    copiedSubnet.setOrchestrationStatus(OrchestrationStatus.CREATED);
                    aaiNetworkResources.updateSubnet(copiedl3network, copiedSubnet);
                }
            }
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateNetworkUpdated", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update L3Network Object
     *
     * @param execution
     */
    public void updateObjectNetwork(BuildingBlockExecution execution) {
        try {
            L3Network l3network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);
            aaiNetworkResources.updateNetwork(l3network);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateObjectNetwork", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update ServiceInstance
     *
     * @param execution
     */
    public void updateServiceInstance(BuildingBlockExecution execution) {
        try {
            ServiceInstance serviceInstance =
                    extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
            aaiServiceInstanceResources.updateServiceInstance(serviceInstance);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateServiceInstance", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update Vnf Object
     *
     * @param execution
     */
    public void updateObjectVnf(BuildingBlockExecution execution) {
        try {
            GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            aaiVnfResources.updateObjectVnf(genericVnf);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateObjectVnf", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update status of VfModuleRollback as true
     *
     * @param execution
     */
    public void updateOrchestrationStatusDeleteVfModule(BuildingBlockExecution execution) {
        execution.setVariable("aaiDeleteVfModuleRollback", false);
        try {
            VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            vfModule.setHeatStackId("");
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);

            VfModule copiedVfModule = vfModule.shallowCopyId();
            copiedVfModule.setHeatStackId("");
            aaiVfModuleResources.updateOrchestrationStatusVfModule(vfModule, vnf, OrchestrationStatus.ASSIGNED);
            execution.setVariable("aaiDeleteVfModuleRollback", true);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateOrchestrationStatusDeleteVfModule", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update Model of VfModule
     *
     * @param execution
     */
    public void updateModelVfModule(BuildingBlockExecution execution) {
        try {
            VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            aaiVfModuleResources.changeAssignVfModule(vfModule, vnf);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateModelVfModule", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update status of FabricConfiguration to Assigned in AAI
     *
     * @param execution
     */
    public void updateOrchestrationStatusAssignFabricConfiguration(BuildingBlockExecution execution) {
        try {
            Configuration configuration = extractPojosForBB.extractByKey(execution, ResourceKey.CONFIGURATION_ID);
            aaiConfigurationResources.updateOrchestrationStatusConfiguration(configuration,
                    OrchestrationStatus.ASSIGNED);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateOrchestrationStatusAssignFabricConfiguration", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update status of FabricConfiguration to Active in AAI
     *
     * @param execution
     */
    public void updateOrchestrationStatusActivateFabricConfiguration(BuildingBlockExecution execution) {
        try {
            Configuration configuration = extractPojosForBB.extractByKey(execution, ResourceKey.CONFIGURATION_ID);
            aaiConfigurationResources.updateOrchestrationStatusConfiguration(configuration, OrchestrationStatus.ACTIVE);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateOrchestrationStatusActivateFabricConfiguration",
                    ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update status of FabricConfiguration to deactive in AAI
     *
     * @param execution
     */
    public void updateOrchestrationStatusDeactivateFabricConfiguration(BuildingBlockExecution execution) {
        try {
            Configuration configuration = extractPojosForBB.extractByKey(execution, ResourceKey.CONFIGURATION_ID);
            aaiConfigurationResources.updateOrchestrationStatusConfiguration(configuration,
                    OrchestrationStatus.ASSIGNED);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateOrchestrationStatusDeactivateFabricConfiguration",
                    ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update Ipv4OamAddress of Vnf
     *
     * @param execution
     */
    public void updateIpv4OamAddressVnf(BuildingBlockExecution execution) {
        try {
            String ipv4OamAddress = execution.getVariable("oamManagementV4Address");
            if (ipv4OamAddress != null) {
                GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
                GenericVnf copiedGenericVnf = genericVnf.shallowCopyId();

                genericVnf.setIpv4OamAddress(ipv4OamAddress);
                copiedGenericVnf.setIpv4OamAddress(ipv4OamAddress);

                aaiVnfResources.updateObjectVnf(copiedGenericVnf);
            }
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateIpv4OamAddressVnf", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update ManagementV6Address of Vnf
     *
     * @param execution
     */
    public void updateManagementV6AddressVnf(BuildingBlockExecution execution) {
        try {
            String managementV6Address = execution.getVariable("oamManagementV6Address");
            if (managementV6Address != null) {
                GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
                GenericVnf copiedGenericVnf = genericVnf.shallowCopyId();

                genericVnf.setManagementV6Address(managementV6Address);
                copiedGenericVnf.setManagementV6Address(managementV6Address);

                aaiVnfResources.updateObjectVnf(copiedGenericVnf);
            }
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateManagementV6AddressVnf", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update ContrailServiceInstanceFqdn of VfModule
     *
     * @param execution
     */
    public void updateContrailServiceInstanceFqdnVfModule(BuildingBlockExecution execution) {
        try {
            String contrailServiceInstanceFqdn = execution.getVariable("contrailServiceInstanceFqdn");
            if (contrailServiceInstanceFqdn != null) {
                VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
                GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
                vfModule.setContrailServiceInstanceFqdn(contrailServiceInstanceFqdn);
                aaiVfModuleResources.updateContrailServiceInstanceFqdnVfModule(vfModule, vnf);
            }
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateContrailServiceInstanceFqdnVfModule", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update status of Vnf to ConfigAssigned in AAI
     *
     * @param execution
     */
    public void updateOrchestrationStatusConfigAssignedVnf(BuildingBlockExecution execution) {
        try {
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            aaiVnfResources.updateOrchestrationStatusVnf(vnf, OrchestrationStatus.CONFIGASSIGNED);
        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateOrchestrationStatusConfigAssignedVnf", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

    /**
     * BPMN access method to update status of Vnf to Configure in AAI
     *
     * @param execution
     */
    public void updateOrchestrationStatusConfigDeployConfigureVnf(BuildingBlockExecution execution) {
        try {
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            aaiVnfResources.updateOrchestrationStatusVnf(vnf, OrchestrationStatus.CONFIGURE);

        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateOrchestrationStatusConfigDeployConfigureVnf", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }

    }

    /**
     * BPMN access method to update status of Vnf to configured in AAI
     *
     * @param execution
     */
    public void updateOrchestrationStatusConfigDeployConfiguredVnf(BuildingBlockExecution execution) {
        try {
            GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
            aaiVnfResources.updateOrchestrationStatusVnf(vnf, OrchestrationStatus.CONFIGURED);

        } catch (Exception ex) {
            logger.error("Exception occurred in AAIUpdateTasks updateOrchestrationStatusConfigDeployConfiguredVnf", ex);
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }

    }
}
