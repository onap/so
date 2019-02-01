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

package org.onap.so.bpmn.infrastructure.aai.tasks;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Collection;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.LineOfBusiness;
import org.onap.so.bpmn.servicedecomposition.bbobjects.OwningEntity;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Platform;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Project;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.BBObjectNotFoundException;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAIConfigurationResources;
import org.onap.so.client.orchestration.AAINetworkResources;
import org.onap.so.client.orchestration.AAIServiceInstanceResources;
import org.onap.so.client.orchestration.AAIVfModuleResources;
import org.onap.so.client.orchestration.AAIVnfResources;
import org.onap.so.client.orchestration.AAIVolumeGroupResources;
import org.onap.so.client.orchestration.AAIVpnBindingResources;
import org.onap.so.logger.MessageEnum;
import org.onap.so.logger.MsoLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AAICreateTasks {

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, AAICreateTasks.class);
	private static final String networkTypeProvider = "PROVIDER";
	private static String NETWORK_COLLECTION_NAME = "networkCollectionName";
	@Autowired
	private AAIServiceInstanceResources aaiSIResources;
	@Autowired
	private AAIVnfResources aaiVnfResources;
	@Autowired
	private ExceptionBuilder exceptionUtil;
	@Autowired
	private ExtractPojosForBB extractPojosForBB;
	@Autowired
	private AAIVolumeGroupResources aaiVolumeGroupResources;
	@Autowired
	private AAIVfModuleResources aaiVfModuleResources;
	@Autowired
	private AAINetworkResources aaiNetworkResources;
	@Autowired
	private AAIVpnBindingResources aaiVpnBindingResources;
	@Autowired
	private AAIConfigurationResources aaiConfigurationResources;
	@Autowired
	private Environment env;

	public void createServiceInstance(BuildingBlockExecution execution) {
		try {
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			Customer customer = execution.getGeneralBuildingBlock().getCustomer();
			aaiSIResources.createServiceInstance(serviceInstance, customer);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}

    public void createServiceSubscription(BuildingBlockExecution execution){
        try{
            ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution,
                    ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
            Customer customer = execution.getGeneralBuildingBlock().getCustomer();
            if (null == customer) {
                String errorMessage = "Exception in creating ServiceSubscription. Customer not present for ServiceInstanceID: "
                        + serviceInstance.getServiceInstanceId();
                msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, errorMessage, "BPMN", MsoLogger.getServiceName(),
                        MsoLogger.ErrorCode.UnknownError, errorMessage);
                exceptionUtil.buildAndThrowWorkflowException(execution, 7000, errorMessage);
            }
            aaiSIResources.createServiceSubscription(customer);
        } catch (BpmnError ex) {
            throw ex;
        }
        catch (Exception ex) {
            exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
        }
    }

	public void createProject(BuildingBlockExecution execution) {
		try {
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			Project project = serviceInstance.getProject();
			if(project != null) {
				if (project.getProjectName() == null || "".equals(project.getProjectName())) {
					msoLogger.info("ProjectName is null in input. Skipping create project...");
				} else {
					aaiSIResources.createProjectandConnectServiceInstance(project, serviceInstance);
				}
			}
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}

	public void createOwningEntity(BuildingBlockExecution execution) {
		try {
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			OwningEntity owningEntity = serviceInstance.getOwningEntity();
			String owningEntityId = owningEntity.getOwningEntityId();
			String owningEntityName = owningEntity.getOwningEntityName();
			if (owningEntityId == null || "".equals(owningEntityId)) {
				String msg = "Exception in AAICreateOwningEntity. OwningEntityId is null.";
				execution.setVariable("ErrorCreateOEAAI", msg);
				exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
			} else {
				if (aaiSIResources.existsOwningEntity(owningEntity)) {
					aaiSIResources.connectOwningEntityandServiceInstance(owningEntity, serviceInstance);
				} else {
					if (owningEntityName == null || "".equals(owningEntityName)) {
						String msg = "Exception in AAICreateOwningEntity. Can't create an owningEntity with no owningEntityName.";
						msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(),
								MsoLogger.ErrorCode.UnknownError, msg);
						exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
					} else {
						if(aaiSIResources.existsOwningEntityName(owningEntityName)){
							String msg = "Exception in AAICreateOwningEntity. Can't create OwningEntity as name already exists in AAI associated with a different owning-entity-id (name must be unique)";
							msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(),
									MsoLogger.ErrorCode.UnknownError, msg);
							exceptionUtil.buildAndThrowWorkflowException(execution, 7000, msg);
						}else{
							aaiSIResources.createOwningEntityandConnectServiceInstance(owningEntity, serviceInstance);
						}
					}
				}
			}
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}

	public void createVnf(BuildingBlockExecution execution) {
		try {
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			execution.setVariable("callHoming", Boolean.TRUE.equals(vnf.isCallHoming()));
			aaiVnfResources.createVnfandConnectServiceInstance(vnf, serviceInstance);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void createPlatform(BuildingBlockExecution execution) {
		try {
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
			Platform platform = vnf.getPlatform();
			if(platform != null) {
				if (platform.getPlatformName() == null || "".equals(platform.getPlatformName())) {
					msoLogger.debug("PlatformName is null in input. Skipping create platform...");
				} else {
					aaiVnfResources.createPlatformandConnectVnf(platform,vnf);
				}
			}
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}

	}
	
	public void createLineOfBusiness(BuildingBlockExecution execution) {
		try {
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
			LineOfBusiness lineOfBusiness = vnf.getLineOfBusiness();
			if(lineOfBusiness != null) {
				if (lineOfBusiness.getLineOfBusinessName() == null || "".equals(lineOfBusiness.getLineOfBusinessName())) {
					msoLogger.info("lineOfBusiness is null in input. Skipping create lineOfBusiness...");
				} else {
					aaiVnfResources.createLineOfBusinessandConnectVnf(lineOfBusiness,vnf);
				}
			}
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}

	public void createVolumeGroup(BuildingBlockExecution execution) {
		try {
			GeneralBuildingBlock gBBInput = execution.getGeneralBuildingBlock();
			
			GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
			VolumeGroup volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID, execution.getLookupMap().get(ResourceKey.VOLUME_GROUP_ID));
			CloudRegion cloudRegion = gBBInput.getCloudRegion();
			aaiVolumeGroupResources.createVolumeGroup(volumeGroup, cloudRegion);
			aaiVolumeGroupResources.connectVolumeGroupToVnf(genericVnf, volumeGroup, cloudRegion);
			aaiVolumeGroupResources.connectVolumeGroupToTenant(volumeGroup,cloudRegion);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void createVfModule(BuildingBlockExecution execution) {
		try {
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
			VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID, execution.getLookupMap().get(ResourceKey.VF_MODULE_ID));
			int moduleIndex = 0;
			if (vfModule.getModelInfoVfModule() != null && !Boolean.TRUE.equals(vfModule.getModelInfoVfModule().getIsBaseBoolean())) {
				moduleIndex = this.getLowestUnusedVfModuleIndexFromAAIVnfResponse(vnf, vfModule);
			}
			vfModule.setModuleIndex(moduleIndex);
			aaiVfModuleResources.createVfModule(vfModule, vnf);
		} catch (Exception ex) {			
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}	
	}
	
	/**
	 * BPMN access method to establish relationships in AAI
	 * @param execution
	 * @throws Exception
	 */
	public void connectVfModuleToVolumeGroup(BuildingBlockExecution execution) {
		try {
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
			VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID, execution.getLookupMap().get(ResourceKey.VF_MODULE_ID));
			VolumeGroup volumeGroup = null;
			try{
				volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID, execution.getLookupMap().get(ResourceKey.VOLUME_GROUP_ID));
			} catch (BBObjectNotFoundException e){
				msoLogger.info("VolumeGroup not found. Skipping Connect between VfModule and VolumeGroup");
			}
			if (volumeGroup != null) {
				aaiVfModuleResources.connectVfModuleToVolumeGroup(vnf, vfModule, volumeGroup, execution.getGeneralBuildingBlock().getCloudRegion());
			}
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}	
	}
	
	/**
	 * BPMN access method to execute Create L3Network operation (PUT )in AAI
	 * @param execution
	 * @throws Exception
	 */
	public void createNetwork(BuildingBlockExecution execution) {
		try {
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			L3Network l3network =  extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID, execution.getLookupMap().get(ResourceKey.NETWORK_ID));
			//set default to false. ToBe updated by SDNC
			l3network.setIsBoundToVpn(false);
			//define is bound to vpn flag as true if NEUTRON_NETWORK_TYPE is PROVIDER
			if (l3network.getModelInfoNetwork().getNeutronNetworkType().equalsIgnoreCase(networkTypeProvider))
				l3network.setIsBoundToVpn(true);
			//put network shell in AAI
			aaiNetworkResources.createNetworkConnectToServiceInstance(l3network, serviceInstance);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}	
	}
	public void createCustomer(BuildingBlockExecution execution) throws Exception {
		try {
			Customer customer = execution.getGeneralBuildingBlock().getCustomer();
			
			aaiVpnBindingResources.createCustomer(customer);
		} catch(Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	/**
	 * BPMN access method to execute NetworkCollection operation (PUT) in AAI
	 * @param execution
	 * @throws Exception
	 */
	public void createNetworkCollection(BuildingBlockExecution execution) {
		try {
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			Collection networkCollection =  serviceInstance.getCollection();
			//pass name generated for NetworkCollection/NetworkCollectionInstanceGroup in previous step of the BB flow
			//put shell in AAI
			networkCollection.setName(execution.getVariable(NETWORK_COLLECTION_NAME));
			aaiNetworkResources.createNetworkCollection(networkCollection);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}	
	}
	
	/**
	 * BPMN access method to execute NetworkCollectionInstanceGroup operation (PUT) in AAI
	 * @param execution
	 * @throws Exception
	 */
	public void createNetworkCollectionInstanceGroup(BuildingBlockExecution execution) {
		try {
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			InstanceGroup instanceGroup =  serviceInstance.getCollection().getInstanceGroup();
			//set name generated for NetworkCollection/NetworkCollectionInstanceGroup in previous step of the BB flow
			instanceGroup.setInstanceGroupName(execution.getVariable(NETWORK_COLLECTION_NAME));
			//put shell in AAI
			aaiNetworkResources.createNetworkInstanceGroup(instanceGroup);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}	
	}
	
	
	/**
	 * BPMN access method to establish relationships in AAI
	 * @param execution
	 * @throws Exception
	 */
	public void connectNetworkToTenant(BuildingBlockExecution execution) {
		try {
			L3Network l3network =  extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID, execution.getLookupMap().get(ResourceKey.NETWORK_ID));
			aaiNetworkResources.connectNetworkToTenant(l3network, execution.getGeneralBuildingBlock().getCloudRegion());
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}	
	}
	
	/**
	 * BPMN access method to establish relationships in AAI
	 * @param execution
	 * @throws Exception
	 */
	public void connectNetworkToCloudRegion(BuildingBlockExecution execution) {
		try {
			L3Network l3network =  extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID, execution.getLookupMap().get(ResourceKey.NETWORK_ID));
			aaiNetworkResources.connectNetworkToCloudRegion(l3network, execution.getGeneralBuildingBlock().getCloudRegion());
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}	
	}
	
	/**
	 * BPMN access method to establish relationships in AAI
	 * @param execution
	 * @throws Exception
	 */
	public void connectVnfToCloudRegion(BuildingBlockExecution execution) {
		try {
			boolean cloudRegionsToSkip = false;
			String[] cloudRegions = env.getProperty("mso.bpmn.cloudRegionIdsToSkipAddingVnfEdgesTo", String[].class);
			if (cloudRegions != null){
				cloudRegionsToSkip = Arrays.stream(cloudRegions).anyMatch(execution.getGeneralBuildingBlock().getCloudRegion().getLcpCloudRegionId()::equals);
			}
			if(!cloudRegionsToSkip) {
				GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
				aaiVnfResources.connectVnfToCloudRegion(vnf, execution.getGeneralBuildingBlock().getCloudRegion());
			}
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}	
	}
	
	/**
	 * BPMN access method to establish relationships in AAI
	 * @param execution
	 * @throws Exception
	 */
	public void connectVnfToTenant(BuildingBlockExecution execution) {
		try {
			GenericVnf vnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
			aaiVnfResources.connectVnfToTenant(vnf, execution.getGeneralBuildingBlock().getCloudRegion());
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}	
	}
	
	/**
	 * BPMN access method to establish relationships in AAI
	 * @param execution
	 * @throws Exception
	 */
	public void connectNetworkToNetworkCollectionServiceInstance(BuildingBlockExecution execution) {
		try {
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			L3Network l3network =  extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID, execution.getLookupMap().get(ResourceKey.NETWORK_ID));
			aaiNetworkResources.connectNetworkToNetworkCollectionServiceInstance(l3network, serviceInstance);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}	
	}
	
	/**
	 * BPMN access method to establish relationships in AAI
	 * @param execution
	 * @throws Exception
	 */
	public void connectNetworkToNetworkCollectionInstanceGroup(BuildingBlockExecution execution) {
		try {
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID));
			L3Network l3network =  extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID, execution.getLookupMap().get(ResourceKey.NETWORK_ID));
			//connect network only if Instance Group / Collection objects exist
			if (serviceInstance.getCollection() != null && serviceInstance.getCollection().getInstanceGroup() != null)
				aaiNetworkResources.connectNetworkToNetworkCollectionInstanceGroup(l3network, serviceInstance.getCollection().getInstanceGroup());
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}	
	}
	
	public void createConfiguration(BuildingBlockExecution execution){
		try{
			Configuration configuration = extractPojosForBB.extractByKey(execution, ResourceKey.CONFIGURATION_ID, execution.getLookupMap().get(ResourceKey.CONFIGURATION_ID));
			aaiConfigurationResources.createConfiguration(configuration);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	/**
	 * Groups existing vf modules by the model uuid of our new vf module and returns the lowest unused index
	 * 
	 * if we have a module type A, and there are 3 instances of those, 
	 * and then module type B has 2 instances, if we are adding a new module type A, 
	 * the vf-module-index should be 3 assuming contiguous indices (not 5, or 2)
	 * 
	 */
	protected int getLowestUnusedVfModuleIndexFromAAIVnfResponse(GenericVnf genericVnf, VfModule newVfModule) {
		
		String newVfModuleModelInvariantUUID = null;
		if (newVfModule.getModelInfoVfModule() != null) {
			newVfModuleModelInvariantUUID = newVfModule.getModelInfoVfModule().getModelInvariantUUID();
		}
		
		
		if (genericVnf != null && genericVnf.getVfModules() != null && !genericVnf.getVfModules().isEmpty()) {
			Set<Integer> moduleIndices = new TreeSet<>();
			for (VfModule vfModule : genericVnf.getVfModules()) {
				if (vfModule.getModelInfoVfModule() != null) {
					if (vfModule.getModelInfoVfModule().getModelInvariantUUID().equals(newVfModuleModelInvariantUUID)) {
						moduleIndices.add(vfModule.getModuleIndex());
					}
				}
			}
			Object[] array = moduleIndices.toArray();
			for (int i=0; i < array.length; i++) {
				if ((int) array[i] != i) {
					return i;
				}
			}
			return array.length;
		} else {
			return 0;
		}
	}
}
