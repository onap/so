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
	
	public void updateOrchestrationStatusAssignedService(BuildingBlockExecution execution) {
		try {
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
			aaiServiceInstanceResources.updateOrchestrationStatusServiceInstance(serviceInstance, OrchestrationStatus.ASSIGNED);
			execution.setVariable("aaiServiceInstanceRollback", true);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void updateOrchestrationStatusActiveService(BuildingBlockExecution execution) {
		try {
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
			aaiServiceInstanceResources.updateOrchestrationStatusServiceInstance(serviceInstance, OrchestrationStatus.ACTIVE);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}

	public void updateOrchestrationStatusAssignedVnf(BuildingBlockExecution execution) {
		try {
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
			aaiVnfResources.updateOrchestrationStatusVnf(vnf,OrchestrationStatus.ASSIGNED);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void updateOrchestrationStatusActiveVnf(BuildingBlockExecution execution) {
		try {
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
			aaiVnfResources.updateOrchestrationStatusVnf(vnf,OrchestrationStatus.ACTIVE);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void updateOrchestrationStatusAssignedVolumeGroup(BuildingBlockExecution execution) {
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			
			VolumeGroup volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID);
			CloudRegion cloudRegion = gBBInput.getCloudRegion();
			volumeGroup.setHeatStackId("");
			aaiVolumeGroupResources.updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion, OrchestrationStatus.ASSIGNED);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void updateOrchestrationStatusActiveVolumeGroup(BuildingBlockExecution execution) {
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			
			VolumeGroup volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID);
			CloudRegion cloudRegion = gBBInput.getCloudRegion();
			
			aaiVolumeGroupResources.updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion, OrchestrationStatus.ACTIVE);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void updateOrchestrationStatusCreatedVolumeGroup(BuildingBlockExecution execution) {
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			
			VolumeGroup volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID);
			CloudRegion cloudRegion = gBBInput.getCloudRegion();
			
			aaiVolumeGroupResources.updateOrchestrationStatusVolumeGroup(volumeGroup, cloudRegion, OrchestrationStatus.CREATED);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
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
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void updateOrchestrationStatusAssignedVfModule(BuildingBlockExecution execution) {
		try {
			VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
			vfModule.setHeatStackId("");
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
			aaiVfModuleResources.updateOrchestrationStatusVfModule(vfModule,vnf,OrchestrationStatus.ASSIGNED);
		} catch (Exception ex) {			
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void updateOrchestrationStatusPendingActivationVfModule(BuildingBlockExecution execution) {
		try {
			VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
			aaiVfModuleResources.updateOrchestrationStatusVfModule(vfModule,vnf,OrchestrationStatus.PENDING_ACTIVATION);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
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
				aaiVfModuleResources.updateOrchestrationStatusVfModule(vfModule,vnf,OrchestrationStatus.PENDING_ACTIVATION);
			}
			else {
				aaiVfModuleResources.updateOrchestrationStatusVfModule(vfModule,vnf,OrchestrationStatus.ASSIGNED);
			}
		} catch (Exception ex) {			
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void updateOrchestrationStatusCreatedVfModule(BuildingBlockExecution execution) {		
		try {
			VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
			aaiVfModuleResources.updateOrchestrationStatusVfModule(vfModule,vnf,OrchestrationStatus.CREATED);			
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}	
	
	public void updateOrchestrationStatusDeactivateVfModule(BuildingBlockExecution execution) {	
		execution.setVariable("aaiDeactivateVfModuleRollback", false);
		try {
			VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
			aaiVfModuleResources.updateOrchestrationStatusVfModule(vfModule,vnf,OrchestrationStatus.CREATED);
			execution.setVariable("aaiDeactivateVfModuleRollback", true);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	/**
	 * BPMN access method to update status of L3Network to Assigned in AAI
	 * @param execution
	 * @throws BBObjectNotFoundException 
	 */
	public void updateOrchestrationStatusAssignedNetwork(BuildingBlockExecution execution) {
		updateNetwork(execution, OrchestrationStatus.ASSIGNED);
	}
	
	/**
	 * BPMN access method to update status of L3Network to Active in AAI
	 * @param execution
	 * @throws BBObjectNotFoundException 
	 */
	public void updateOrchestrationStatusActiveNetwork(BuildingBlockExecution execution) {
		updateNetwork(execution, OrchestrationStatus.ACTIVE);
	}
	
	/**
	 * BPMN access method to update status of L3Network to Created in AAI
	 * @param execution
	 * @throws BBObjectNotFoundException 
	 */
	public void updateOrchestrationStatusCreatedNetwork(BuildingBlockExecution execution) {
		updateNetwork(execution, OrchestrationStatus.CREATED);
	}
	
	protected void updateNetwork(BuildingBlockExecution execution, OrchestrationStatus status) {
		try {
			L3Network l3Network =  extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);
			updateNetworkAAI(l3Network, status);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	protected void updateNetworkAAI(L3Network l3Network, OrchestrationStatus status) {
		L3Network copiedl3Network = l3Network.shallowCopyId();

		copiedl3Network.setOrchestrationStatus(status);
		l3Network.setOrchestrationStatus(status);
		aaiNetworkResources.updateNetwork(copiedl3Network);
		
		List<Subnet> subnets = l3Network.getSubnets();
		if (subnets != null){
			for (Subnet subnet : subnets){
				Subnet copiedSubnet = subnet.shallowCopyId();
				copiedSubnet.setOrchestrationStatus(status);
				aaiNetworkResources.updateSubnet(copiedl3Network, copiedSubnet);
			}
		}
	}
	
	/**
	 * BPMN access method to update status of L3Network Collection to Active in AAI
	 * @param execution
	 * @throws BBObjectNotFoundException 
	 */
	public void updateOrchestrationStatusActiveNetworkCollection(BuildingBlockExecution execution) {
		execution.setVariable("aaiNetworkCollectionActivateRollback", false);
		try {
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
			Collection networkCollection = serviceInstance.getCollection();
			Collection copiedNetworkCollection = networkCollection.shallowCopyId();

			networkCollection.setOrchestrationStatus(OrchestrationStatus.ACTIVE);
			copiedNetworkCollection.setOrchestrationStatus(OrchestrationStatus.ACTIVE);
			aaiCollectionResources.updateCollection(copiedNetworkCollection);
			execution.setVariable("aaiNetworkCollectionActivateRollback", true);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void updateOrchestrationStatusActivateVfModule(BuildingBlockExecution execution) {
		execution.setVariable("aaiActivateVfModuleRollback", false);
		try {
			VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
			aaiVfModuleResources.updateOrchestrationStatusVfModule(vfModule, vnf, OrchestrationStatus.ACTIVE);
			execution.setVariable("aaiActivateVfModuleRollback", true);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
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
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	/**
	 * BPMN access method to update L3Network after it was created in cloud
	 * @param execution
	 * @throws Exception
	 */
	public void updateNetworkCreated(BuildingBlockExecution execution) throws Exception {
		execution.setVariable("aaiNetworkActivateRollback", false);
		L3Network l3network =  extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);
		L3Network copiedl3network = l3network.shallowCopyId();
		CreateNetworkResponse response = execution.getVariable("createNetworkResponse");
		try {
			if(response.getNetworkFqdn()!=null){
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
			if (subnets != null && subnetMap != null){
				for (Subnet subnet: subnets){
					Subnet copiedSubnet = subnet.shallowCopyId();
					copiedSubnet.setNeutronSubnetId(subnetMap.get(copiedSubnet.getSubnetId()));
					copiedSubnet.setOrchestrationStatus(OrchestrationStatus.CREATED);
					aaiNetworkResources.updateSubnet(copiedl3network, copiedSubnet);
				}
			}
			
			execution.setVariable("aaiNetworkActivateRollback", true);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	/**
	 * BPMN access method to update L3Network after it was updated in cloud
	 * @param execution
	 * @throws Exception
	 */
	public void updateNetworkUpdated(BuildingBlockExecution execution) throws Exception {
		L3Network l3network =  extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);
		L3Network copiedl3network = l3network.shallowCopyId();
		UpdateNetworkResponse response = execution.getVariable("updateNetworkResponse");
		try {
			copiedl3network.setNeutronNetworkId(response.getNeutronNetworkId());
			aaiNetworkResources.updateNetwork(copiedl3network);
			
			Map<String, String> subnetMap = response.getSubnetMap();
			List<Subnet> subnets = l3network.getSubnets();
			if (subnets != null && subnetMap != null){
				for (Subnet subnet: subnets){
					Subnet copiedSubnet = subnet.shallowCopyId();
					copiedSubnet.setNeutronSubnetId(subnetMap.get(copiedSubnet.getSubnetId()));
					copiedSubnet.setOrchestrationStatus(OrchestrationStatus.CREATED);
					aaiNetworkResources.updateSubnet(copiedl3network, copiedSubnet);
				}
			}
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void updateObjectNetwork(BuildingBlockExecution execution) {
		try {
			L3Network l3network = extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID);
			aaiNetworkResources.updateNetwork(l3network);
		} catch(Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	/**
	 * BPMN access method to update ServiceInstance
	 * @param execution
	 */
	public void updateServiceInstance(BuildingBlockExecution execution) {
		try {
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID);
			aaiServiceInstanceResources.updateServiceInstance(serviceInstance);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void updateObjectVnf(BuildingBlockExecution execution) {
		try {
			GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
			aaiVnfResources.updateObjectVnf(genericVnf);
		} catch(Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void updateOrchestrationStatusDeleteVfModule(BuildingBlockExecution execution) {	
		execution.setVariable("aaiDeleteVfModuleRollback", false);
		try {
			VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
			vfModule.setHeatStackId("");
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);

			VfModule copiedVfModule = vfModule.shallowCopyId();
			copiedVfModule.setHeatStackId("");
			aaiVfModuleResources.updateOrchestrationStatusVfModule(vfModule,vnf,OrchestrationStatus.ASSIGNED);
			execution.setVariable("aaiDeleteVfModuleRollback", true);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}

	public void updateModelVfModule(BuildingBlockExecution execution) {
		try {
			VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID);
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
			aaiVfModuleResources.changeAssignVfModule(vfModule, vnf);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	public void updateOrchestrationStatusActivateFabricConfiguration(BuildingBlockExecution execution) {
		try {
			Configuration configuration = extractPojosForBB.extractByKey(execution, ResourceKey.CONFIGURATION_ID);
			aaiConfigurationResources.updateOrchestrationStatusConfiguration(configuration, OrchestrationStatus.ACTIVE);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void updateOrchestrationStatusDeactivateFabricConfiguration(BuildingBlockExecution execution) {
		try {
			Configuration configuration = extractPojosForBB.extractByKey(execution, ResourceKey.CONFIGURATION_ID);
			aaiConfigurationResources.updateOrchestrationStatusConfiguration(configuration, OrchestrationStatus.ASSIGNED);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
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
		} catch(Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
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
		} catch(Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
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
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void updateOrchestrationStatusConfigAssignedVnf(BuildingBlockExecution execution) {
		try {
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID);
			aaiVnfResources.updateOrchestrationStatusVnf(vnf, OrchestrationStatus.CONFIGASSIGNED);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
}
