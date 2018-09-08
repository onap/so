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


import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.tasks.ExtractPojosForBB;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAIConfigurationResources;
import org.onap.so.client.orchestration.AAINetworkResources;
import org.onap.so.client.orchestration.AAIServiceInstanceResources;
import org.onap.so.client.orchestration.AAIVfModuleResources;
import org.onap.so.client.orchestration.AAIVnfResources;
import org.onap.so.client.orchestration.AAIVolumeGroupResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AAIDeleteTasks {
	
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
	
	public void deleteVfModule(BuildingBlockExecution execution) throws Exception {		
		GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
		VfModule vfModule = extractPojosForBB.extractByKey(execution, ResourceKey.VF_MODULE_ID, execution.getLookupMap().get(ResourceKey.VF_MODULE_ID));
		
		execution.setVariable("aaiVfModuleRollback", false);
		try {
			aaiVfModuleResources.deleteVfModule(vfModule, genericVnf);
			execution.setVariable("aaiVfModuleRollback", true);
		} catch (Exception ex) {			
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}	
	}

	public void deleteVnf(BuildingBlockExecution execution) throws Exception {		
		GenericVnf genericVnf = extractPojosForBB.extractByKey(execution, ResourceKey.GENERIC_VNF_ID, execution.getLookupMap().get(ResourceKey.GENERIC_VNF_ID));
		
		execution.setVariable("aaiVnfRollback", false);
		try {
			aaiVnfResources.deleteVnf(genericVnf);
			execution.setVariable("aaiVnfRollback", true);
		} catch (Exception ex) {			
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}	
	}
	
	public void deleteServiceInstance(BuildingBlockExecution execution) throws Exception {
		try {
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID)); 
			aaiSIResources.deleteServiceInstance(serviceInstance);
		}
		catch(Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
		
	}
	
	public void deleteNetwork(BuildingBlockExecution execution) throws Exception {
		try {
			L3Network l3network =  extractPojosForBB.extractByKey(execution, ResourceKey.NETWORK_ID, execution.getLookupMap().get(ResourceKey.NETWORK_ID)); 
			aaiNetworkResources.deleteNetwork(l3network);
			execution.setVariable("isRollbackNeeded", true);
		}
		catch(Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void deleteCollection(BuildingBlockExecution execution) throws Exception {
		try {
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID)); 
			aaiNetworkResources.deleteCollection(serviceInstance.getCollection());
		}
		catch(Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void deleteInstanceGroup(BuildingBlockExecution execution) throws Exception {
		try {
			ServiceInstance serviceInstance = extractPojosForBB.extractByKey(execution, ResourceKey.SERVICE_INSTANCE_ID, execution.getLookupMap().get(ResourceKey.SERVICE_INSTANCE_ID)); 
			aaiNetworkResources.deleteNetworkInstanceGroup(serviceInstance.getCollection().getInstanceGroup());
		}
		catch(Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	
	public void deleteVolumeGroup(BuildingBlockExecution execution) {
		try {
			VolumeGroup volumeGroup = extractPojosForBB.extractByKey(execution, ResourceKey.VOLUME_GROUP_ID, execution.getLookupMap().get(ResourceKey.VOLUME_GROUP_ID));
			CloudRegion cloudRegion = execution.getGeneralBuildingBlock().getCloudRegion();
			aaiVolumeGroupResources.deleteVolumeGroup(volumeGroup, cloudRegion);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
	public void deleteConfiguration(BuildingBlockExecution execution) {
		try {
			Configuration configuration = extractPojosForBB.extractByKey(execution, ResourceKey.CONFIGURATION_ID, execution.getLookupMap().get(ResourceKey.CONFIGURATION_ID));
			aaiConfigurationResources.deleteConfiguration(configuration);
		} catch (Exception ex) {
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, ex);
		}
	}
}
