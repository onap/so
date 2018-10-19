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

package org.onap.so.bpmn.servicedecomposition.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.javatuples.Pair;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.common.DelegateExecutionImpl;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Collection;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Customer;
import org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.onap.so.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.bbobjects.LineOfBusiness;
import org.onap.so.bpmn.servicedecomposition.bbobjects.NetworkPolicy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.OwningEntity;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Platform;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Project;
import org.onap.so.bpmn.servicedecomposition.bbobjects.RouteTableReference;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.entities.ConfigurationResourceKeys;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.onap.so.bpmn.servicedecomposition.generalobjects.OrchestrationContext;
import org.onap.so.bpmn.servicedecomposition.generalobjects.RequestContext;
import org.onap.so.client.aai.AAICommonObjectMapperProvider;
import org.onap.so.client.aai.AAIObjectType;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.Relationships;
import org.onap.so.client.aai.entities.uri.AAIResourceUri;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResource;
import org.onap.so.db.catalog.beans.CollectionResourceCustomization;
import org.onap.so.db.catalog.beans.ConfigurationResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.OrchestrationStatus;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfResourceCustomization;
import org.onap.so.db.catalog.beans.VnfVfmoduleCvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.onap.so.db.request.beans.InfraActiveRequests;
import org.onap.so.logger.MsoLogger;
import org.onap.so.serviceinstancebeans.CloudConfiguration;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.Networks;
import org.onap.so.serviceinstancebeans.RelatedInstance;
import org.onap.so.serviceinstancebeans.RelatedInstanceList;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.Resources;
import org.onap.so.serviceinstancebeans.VfModules;
import org.onap.so.serviceinstancebeans.Vnfs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Component("BBInputSetup")
public class BBInputSetup implements JavaDelegate {

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, BBInputSetup.class);
	private static final String FLOW_VAR_NAME = "flowToBeCalled";
	private static final String LOOKUP_KEY_MAP_VAR_NAME = "lookupKeyMap";
	private static final String GBB_INPUT_VAR_NAME = "gBBInput";
	private static final String EXECUTE_BB_VAR_NAME = "buildingBlock";
	private static final String VOLUME_GROUP = "VolumeGroup";
	private static final String VF_MODULE = "VfModule";
	private static final String NETWORK = "Network";
	private static final String VNF = "Vnf";
	private static final String NETWORK_COLLECTION = "NetworkCollection";

	@Autowired
	private BBInputSetupUtils bbInputSetupUtils;

	@Autowired
	private BBInputSetupMapperLayer mapperLayer;

	@Autowired
	private ExceptionBuilder exceptionUtil;

	private ObjectMapper mapper = new ObjectMapper();

	public BBInputSetupUtils getBbInputSetupUtils() {
		return bbInputSetupUtils;
	}

	public void setBbInputSetupUtils(BBInputSetupUtils bbInputSetupUtils) {
		this.bbInputSetupUtils = bbInputSetupUtils;
	}

	public BBInputSetupMapperLayer getMapperLayer() {
		return mapperLayer;
	}

	public void setMapperLayer(BBInputSetupMapperLayer mapperLayer) {
		this.mapperLayer = mapperLayer;
	}

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		try {
			GeneralBuildingBlock outputBB = null;
			ExecuteBuildingBlock executeBB = this.getExecuteBBFromExecution(execution);
			String resourceId = executeBB.getResourceId();
			String requestAction = executeBB.getRequestAction();
			String vnfType = executeBB.getVnfType();
			boolean aLaCarte = Boolean.TRUE.equals(executeBB.isaLaCarte());
			boolean homing = Boolean.TRUE.equals(executeBB.isHoming());
			Map<ResourceKey, String> lookupKeyMap = new HashMap<>();
			outputBB = this.getGBB(executeBB, lookupKeyMap, requestAction, aLaCarte, resourceId, vnfType);
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			msoLogger.debug("GeneralBB: " + mapper.writeValueAsString(outputBB));

			setHomingFlag(outputBB, homing, lookupKeyMap);

			execution.setVariable(FLOW_VAR_NAME, executeBB.getBuildingBlock().getBpmnFlowName());
			execution.setVariable(GBB_INPUT_VAR_NAME, outputBB);
			execution.setVariable(LOOKUP_KEY_MAP_VAR_NAME, lookupKeyMap);

			BuildingBlockExecution gBuildingBlockExecution = new DelegateExecutionImpl(execution);
			execution.setVariable("gBuildingBlockExecution", gBuildingBlockExecution);
			execution.setVariable("RetryCount", 1);
			execution.setVariable("handlingCode", "Success");
		} catch (Exception e) {
			msoLogger.error(e);
			exceptionUtil.buildAndThrowWorkflowException(execution, 7000, e.getMessage());
		}
	}

	protected void setHomingFlag(GeneralBuildingBlock outputBB, boolean homing, Map<ResourceKey, String> lookupKeyMap) {

		if (lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID) != null && homing && outputBB != null) {
			for (GenericVnf vnf : outputBB.getCustomer().getServiceSubscription().getServiceInstances().get(0).getVnfs()) {
				if (vnf.getVnfId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID))) {
					vnf.setCallHoming(homing);
				}
			}
		}
	}

	protected ExecuteBuildingBlock getExecuteBBFromExecution(DelegateExecution execution) {
		return (ExecuteBuildingBlock) execution.getVariable(EXECUTE_BB_VAR_NAME);
	}

	protected GeneralBuildingBlock getGBB(ExecuteBuildingBlock executeBB, Map<ResourceKey, String> lookupKeyMap,
			String requestAction, boolean aLaCarte, String resourceId, String vnfType) throws Exception {
		String requestId = executeBB.getRequestId();
		this.populateLookupKeyMapWithIds(executeBB.getWorkflowResourceIds(), lookupKeyMap);
		RequestDetails requestDetails = executeBB.getRequestDetails();
		if(requestDetails == null) {
			requestDetails = bbInputSetupUtils.getRequestDetails(requestId);
		}
		if (requestDetails.getModelInfo() == null) {
			return this.getGBBCM(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId);
		}
		else {
			ModelType modelType = requestDetails.getModelInfo().getModelType();
			if (aLaCarte && modelType.equals(ModelType.service)) {
				return this.getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId);
			} else if (aLaCarte && !modelType.equals(ModelType.service)) {
				return this.getGBBALaCarteNonService(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId,
						vnfType);
			} else {
				return this.getGBBMacro(executeBB, requestDetails, lookupKeyMap, requestAction, resourceId, vnfType);
			}
		}
	}

	protected void populateLookupKeyMapWithIds(WorkflowResourceIds workflowResourceIds,
			Map<ResourceKey, String> lookupKeyMap) {
		lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, workflowResourceIds.getServiceInstanceId());
		lookupKeyMap.put(ResourceKey.NETWORK_ID, workflowResourceIds.getNetworkId());
		lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, workflowResourceIds.getVnfId());
		lookupKeyMap.put(ResourceKey.VF_MODULE_ID, workflowResourceIds.getVfModuleId());
		lookupKeyMap.put(ResourceKey.VOLUME_GROUP_ID, workflowResourceIds.getVolumeGroupId());
		lookupKeyMap.put(ResourceKey.CONFIGURATION_ID, workflowResourceIds.getConfigurationId());
	}

	protected GeneralBuildingBlock getGBBALaCarteNonService(ExecuteBuildingBlock executeBB,
			RequestDetails requestDetails, Map<ResourceKey, String> lookupKeyMap, String requestAction,
			String resourceId, String vnfType) throws Exception {
		String bbName = executeBB.getBuildingBlock().getBpmnFlowName();
		String serviceInstanceId = lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID);
		org.onap.aai.domain.yang.ServiceInstance aaiServiceInstance = null;
		if (serviceInstanceId != null) {
			aaiServiceInstance = bbInputSetupUtils.getAAIServiceInstanceById(serviceInstanceId);
		}
		Service service = null;
		if (aaiServiceInstance != null) {
			service = bbInputSetupUtils.getCatalogServiceByModelUUID(aaiServiceInstance.getModelVersionId());
		}
		if (aaiServiceInstance != null && service != null) {
			ServiceInstance serviceInstance = this.getExistingServiceInstance(aaiServiceInstance);
			serviceInstance.setModelInfoServiceInstance(this.mapperLayer.mapCatalogServiceIntoServiceInstance(service));
			this.populateObjectsOnAssignAndCreateFlows(requestDetails, service, bbName, serviceInstance, lookupKeyMap,
					resourceId, vnfType);
			return this.populateGBBWithSIAndAdditionalInfo(requestDetails, serviceInstance, executeBB, requestAction, null);
		} else {
			msoLogger.debug("Related Service Instance from AAI: " + aaiServiceInstance);
			msoLogger.debug("Related Service Instance Model Info from AAI: " + service);
			throw new Exception("Could not find relevant information for related Service Instance");
		}
	}
	
	protected GeneralBuildingBlock getGBBCM(ExecuteBuildingBlock executeBB,
			RequestDetails requestDetails, Map<ResourceKey, String> lookupKeyMap, String requestAction,
			String resourceId) throws Exception {		
		ServiceInstance serviceInstance = new ServiceInstance();
		String serviceInstanceId = lookupKeyMap.get(ResourceKey.SERVICE_INSTANCE_ID);
		serviceInstance.setServiceInstanceId(serviceInstanceId);
		
		List<GenericVnf> genericVnfs = serviceInstance.getVnfs();
		
		String vnfId = lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID);
		org.onap.aai.domain.yang.GenericVnf aaiGenericVnf = bbInputSetupUtils.getAAIGenericVnf(vnfId);
		
		GenericVnf genericVnf = this.mapperLayer.mapAAIGenericVnfIntoGenericVnf(aaiGenericVnf);
		genericVnfs.add(genericVnf);		
		
		return this.populateGBBWithSIAndAdditionalInfo(requestDetails, serviceInstance, executeBB, requestAction, new Customer());
		
	}

	protected void populateObjectsOnAssignAndCreateFlows(RequestDetails requestDetails, Service service, String bbName,
			ServiceInstance serviceInstance, Map<ResourceKey, String> lookupKeyMap, String resourceId, String vnfType)
			throws Exception {
		ModelInfo modelInfo = requestDetails.getModelInfo();
		String instanceName = requestDetails.getRequestInfo().getInstanceName();
		ModelType modelType = modelInfo.getModelType();
		RelatedInstanceList[] relatedInstanceList = requestDetails.getRelatedInstanceList();

		org.onap.so.serviceinstancebeans.Platform platform = requestDetails.getPlatform();
		org.onap.so.serviceinstancebeans.LineOfBusiness lineOfBusiness = requestDetails.getLineOfBusiness();

		if (modelType.equals(ModelType.network)) {
			lookupKeyMap.put(ResourceKey.NETWORK_ID, resourceId);
			this.populateL3Network(instanceName, modelInfo, service, bbName, serviceInstance, lookupKeyMap, resourceId, null);
		} else if (modelType.equals(ModelType.vnf)) {
			lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, resourceId);
			this.populateGenericVnf(modelInfo, instanceName, platform, lineOfBusiness, service, bbName, serviceInstance,
					lookupKeyMap, relatedInstanceList, resourceId, vnfType, null);
		} else if (modelType.equals(ModelType.volumeGroup)) {
			lookupKeyMap.put(ResourceKey.VOLUME_GROUP_ID, resourceId);
			this.populateVolumeGroup(modelInfo, service, bbName, serviceInstance, lookupKeyMap, resourceId,
					relatedInstanceList, instanceName, vnfType, null);
		} else if (modelType.equals(ModelType.vfModule)) {
			lookupKeyMap.put(ResourceKey.VF_MODULE_ID, resourceId);
			this.populateVfModule(modelInfo, service, bbName, serviceInstance, lookupKeyMap, resourceId,
					relatedInstanceList, instanceName, null, requestDetails.getCloudConfiguration());
		} else {
			return;
		}
	}

	protected void populateConfiguration(ModelInfo modelInfo, Service service, String bbName,
			ServiceInstance serviceInstance, Map<ResourceKey, String> lookupKeyMap, String resourceId, String instanceName, ConfigurationResourceKeys configurationResourceKeys) {
		Configuration configuration = null;
		for (Configuration configurationTemp : serviceInstance.getConfigurations()) {
			if (lookupKeyMap.get(ResourceKey.CONFIGURATION_ID) != null
					&& configurationTemp.getConfigurationId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.CONFIGURATION_ID))) {
				configuration = configurationTemp;
				org.onap.aai.domain.yang.Configuration aaiConfiguration = bbInputSetupUtils.getAAIConfiguration(configuration.getConfigurationId());
				if(aaiConfiguration!=null){
					modelInfo.setModelCustomizationUuid(aaiConfiguration.getModelCustomizationId());
				}
			}
		}
		if (configuration == null && bbName.equalsIgnoreCase(AssignFlows.FABRIC_CONFIGURATION.toString())) {
			configuration = this.createConfiguration(lookupKeyMap, instanceName, resourceId);
			serviceInstance.getConfigurations().add(configuration);
		}
		if(configuration != null) {
			this.mapCatalogConfiguration(configuration, modelInfo, service, configurationResourceKeys);
		}
	}

	protected Configuration createConfiguration(Map<ResourceKey, String> lookupKeyMap,
			String instanceName, String resourceId) {
		lookupKeyMap.put(ResourceKey.CONFIGURATION_ID, resourceId);
		Configuration configuration = new Configuration();
		configuration.setConfigurationId(resourceId);
		configuration.setConfigurationName(instanceName);
		configuration.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		return configuration;
	}

	protected void mapCatalogConfiguration(Configuration configuration, ModelInfo modelInfo, Service service, ConfigurationResourceKeys configurationResourceKeys) {
		ConfigurationResourceCustomization configurationResourceCustomization = findConfigurationResourceCustomization(modelInfo, service);
		VnfVfmoduleCvnfcConfigurationCustomization vnfVfmoduleCvnfcConfigurationCustomization = 
				findVnfVfmoduleCvnfcConfigurationCustomization(configurationResourceKeys.getVfModuleCustomizationUUID(),
						configurationResourceKeys.getVnfResourceCustomizationUUID(), configurationResourceKeys.getCvnfcCustomizationUUID(), configurationResourceCustomization);
		if (configurationResourceCustomization != null && vnfVfmoduleCvnfcConfigurationCustomization != null) {
			configuration.setModelInfoConfiguration(this.mapperLayer.mapCatalogConfigurationToConfiguration(configurationResourceCustomization
					, vnfVfmoduleCvnfcConfigurationCustomization));
		}
	}

	protected VnfVfmoduleCvnfcConfigurationCustomization findVnfVfmoduleCvnfcConfigurationCustomization(String vfModuleCustomizationUUID, 
			String vnfResourceCustomizationUUID, String cvnfcCustomizationUUID, ConfigurationResourceCustomization configurationResourceCustomization) {
		for(VnfVfmoduleCvnfcConfigurationCustomization vnfVfmoduleCvnfcConfigurationCustomization : 
			configurationResourceCustomization.getConfigurationResource().getVnfVfmoduleCvnfcConfigurationCustomization()) {
			if(vnfVfmoduleCvnfcConfigurationCustomization.getVfModuleCustomization().getModelCustomizationUUID().equalsIgnoreCase(vfModuleCustomizationUUID)
					&& vnfVfmoduleCvnfcConfigurationCustomization.getVnfResourceCustomization().getModelCustomizationUUID().equalsIgnoreCase(vnfResourceCustomizationUUID)
					&& vnfVfmoduleCvnfcConfigurationCustomization.getCvnfcCustomization().getModelCustomizationUUID().equalsIgnoreCase(cvnfcCustomizationUUID)) {
				return vnfVfmoduleCvnfcConfigurationCustomization;
			}
		}
		return null;
	}

	protected ConfigurationResourceCustomization findConfigurationResourceCustomization(ModelInfo modelInfo, Service service) {
		for (ConfigurationResourceCustomization resourceCust : service.getConfigurationCustomizations()) {
			if (resourceCust.getModelCustomizationUUID().equalsIgnoreCase(modelInfo.getModelCustomizationUuid())) {
				return resourceCust;
			}
		}
		return null;
	}

	protected void populateVfModule(ModelInfo modelInfo, Service service, String bbName,
			ServiceInstance serviceInstance, Map<ResourceKey, String> lookupKeyMap, String resourceId,
			RelatedInstanceList[] relatedInstanceList, String instanceName, List<Map<String, String>> instanceParams, CloudConfiguration cloudConfiguration) throws Exception {
		String vnfModelCustomizationUUID = null;
		if (relatedInstanceList != null) {
			for (RelatedInstanceList relatedInstList : relatedInstanceList) {
				RelatedInstance relatedInstance = relatedInstList.getRelatedInstance();
				if (relatedInstance.getModelInfo().getModelType().equals(ModelType.vnf)) {
					vnfModelCustomizationUUID = relatedInstance.getModelInfo().getModelCustomizationId();
				}
				if (relatedInstance.getModelInfo().getModelType().equals(ModelType.volumeGroup)) {
					lookupKeyMap.put(ResourceKey.VOLUME_GROUP_ID, relatedInstance.getInstanceId());
				}
			}
		}
		GenericVnf vnf = null;
		for (GenericVnf tempVnf : serviceInstance.getVnfs()) {
			if (tempVnf.getVnfId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID))) {
				vnf = tempVnf;
				vnfModelCustomizationUUID = this.bbInputSetupUtils.getAAIGenericVnf(vnf.getVnfId())
						.getModelCustomizationId();
				ModelInfo vnfModelInfo = new ModelInfo();
				vnfModelInfo.setModelCustomizationUuid(vnfModelCustomizationUUID);
				this.mapCatalogVnf(tempVnf, vnfModelInfo, service);
				if (lookupKeyMap.get(ResourceKey.VOLUME_GROUP_ID) == null) {
					for(VolumeGroup volumeGroup : tempVnf.getVolumeGroups()) {
						String volumeGroupCustId = 
								this.bbInputSetupUtils.getAAIVolumeGroup(cloudConfiguration.getCloudOwner(), 
										cloudConfiguration.getLcpCloudRegionId(), volumeGroup.getVolumeGroupId()).getModelCustomizationId();
						if(modelInfo.getModelCustomizationId().equalsIgnoreCase(volumeGroupCustId)) {
							lookupKeyMap.put(ResourceKey.VOLUME_GROUP_ID, volumeGroup.getVolumeGroupId());
							break;
						}
					}
				}
				break;
			}
		}
		if (vnf != null) {
			VfModule vfModule = null;
			for (VfModule vfModuleTemp : vnf.getVfModules()) {
				if (lookupKeyMap.get(ResourceKey.VF_MODULE_ID) != null
						&& vfModuleTemp.getVfModuleId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.VF_MODULE_ID))) {
					vfModule = vfModuleTemp;
					String vfModuleCustId = bbInputSetupUtils.getAAIVfModule(vnf.getVnfId(), vfModule.getVfModuleId()).getModelCustomizationId();
					modelInfo.setModelCustomizationId(vfModuleCustId);
					break;
				}
			}
			if (vfModule == null && bbName.equalsIgnoreCase(AssignFlows.VF_MODULE.toString())) {
				vfModule = createVfModule(lookupKeyMap,
						resourceId, instanceName, instanceParams);
				vnf.getVfModules().add(vfModule);
			}
			if(vfModule != null) {
				mapCatalogVfModule(vfModule, modelInfo, service, vnfModelCustomizationUUID);
			}
		} else {
			msoLogger.debug("Related VNF instance Id not found:  " + lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID));
			throw new Exception("Could not find relevant information for related VNF");
		}
	}

	protected void mapCatalogVfModule(VfModule vfModule, ModelInfo modelInfo, Service service,
			String vnfModelCustomizationUUID) {
		if(modelInfo.getModelCustomizationUuid() != null) {
			modelInfo.setModelCustomizationId(modelInfo.getModelCustomizationUuid());
		}
		VnfResourceCustomization vnfResourceCustomization = null;
		for (VnfResourceCustomization resourceCust : service.getVnfCustomizations()) {
			if (resourceCust.getModelCustomizationUUID().equalsIgnoreCase(vnfModelCustomizationUUID)) {
				vnfResourceCustomization = resourceCust;
				break;
			}
		}
		if (vnfResourceCustomization != null) {
			VfModuleCustomization vfResourceCustomization = vnfResourceCustomization.getVfModuleCustomizations()
					.stream() // Convert to steam
					.filter(x -> modelInfo.getModelCustomizationId().equalsIgnoreCase(x.getModelCustomizationUUID()))// find
																														// what
																														// we
																														// want
					.findAny() // If 'findAny' then return found
					.orElse(null);
			if (vfResourceCustomization != null) {
				vfModule.setModelInfoVfModule(this.mapperLayer.mapCatalogVfModuleToVfModule(vfResourceCustomization));
			}
		}
	}

	protected VfModule createVfModule(Map<ResourceKey, String> lookupKeyMap, String vfModuleId, String instanceName, List<Map<String, String>> instanceParams) {
		lookupKeyMap.put(ResourceKey.VF_MODULE_ID, vfModuleId);
		VfModule vfModule = new VfModule();
		vfModule.setVfModuleId(vfModuleId);
		vfModule.setVfModuleName(instanceName);
		vfModule.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		if(instanceParams != null) {
			for(Map<String, String> params : instanceParams) {
				vfModule.getCloudParams().putAll(params);
			}
		}
		return vfModule;
	}

	protected void populateVolumeGroup(ModelInfo modelInfo, Service service, String bbName,
			ServiceInstance serviceInstance, Map<ResourceKey, String> lookupKeyMap, String resourceId,
			RelatedInstanceList[] relatedInstanceList, String instanceName, String vnfType, List<Map<String, String>> instanceParams) throws Exception {
		VolumeGroup volumeGroup = null;
		GenericVnf vnf = null;
		String vnfModelCustomizationUUID = null;
		String generatedVnfType = vnfType;
		if (generatedVnfType == null || generatedVnfType.isEmpty()) {
			generatedVnfType = service.getModelName() + "/" + modelInfo.getModelCustomizationName();
		}
		if (relatedInstanceList != null) {
			for (RelatedInstanceList relatedInstList : relatedInstanceList) {
				RelatedInstance relatedInstance = relatedInstList.getRelatedInstance();
				if (relatedInstance.getModelInfo().getModelType().equals(ModelType.vnf)) {
					vnfModelCustomizationUUID = relatedInstance.getModelInfo().getModelCustomizationUuid();
					break;
				}
			}
		}
		for (GenericVnf tempVnf : serviceInstance.getVnfs()) {
			if (tempVnf.getVnfId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID))) {
				vnf = tempVnf;
				vnfModelCustomizationUUID = bbInputSetupUtils.getAAIGenericVnf(vnf.getVnfId())
						.getModelCustomizationId();
				ModelInfo vnfModelInfo = new ModelInfo();
				vnfModelInfo.setModelCustomizationUuid(vnfModelCustomizationUUID);
				mapCatalogVnf(tempVnf, vnfModelInfo, service);
				break;
			}
		}
		if (vnf != null && vnfModelCustomizationUUID != null) {
			for (VolumeGroup volumeGroupTemp : vnf.getVolumeGroups()) {
				if (lookupKeyMap.get(ResourceKey.VOLUME_GROUP_ID) != null && volumeGroupTemp.getVolumeGroupId()
						.equalsIgnoreCase(lookupKeyMap.get(ResourceKey.VOLUME_GROUP_ID))) {
					volumeGroup = volumeGroupTemp;
					break;
				}
			}
			if (volumeGroup == null && bbName.equalsIgnoreCase(AssignFlows.VOLUME_GROUP.toString())) {
				volumeGroup = createVolumeGroup(lookupKeyMap, resourceId, instanceName, generatedVnfType, instanceParams);
				vnf.getVolumeGroups().add(volumeGroup);
			}
			if(volumeGroup != null) {
				mapCatalogVolumeGroup(volumeGroup, modelInfo, service, vnfModelCustomizationUUID);
			}
		} else {
			msoLogger.debug("Related VNF instance Id not found:  " + lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID));
			throw new Exception("Could not find relevant information for related VNF");
		}
	}

	protected VolumeGroup createVolumeGroup(Map<ResourceKey, String> lookupKeyMap, String volumeGroupId, String instanceName, String vnfType, List<Map<String, String>> instanceParams) {
		lookupKeyMap.put(ResourceKey.VOLUME_GROUP_ID, volumeGroupId);
		VolumeGroup volumeGroup = new VolumeGroup();
		volumeGroup.setVolumeGroupId(volumeGroupId);
		volumeGroup.setVolumeGroupName(instanceName);
		volumeGroup.setVnfType(vnfType);
		volumeGroup.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		if(instanceParams != null) {
			for(Map<String, String> params : instanceParams) {
				volumeGroup.getCloudParams().putAll(params);
			}
		}
		return volumeGroup;
	}

	protected void mapCatalogVolumeGroup(VolumeGroup volumeGroup, ModelInfo modelInfo, Service service,
			String vnfModelCustomizationUUID) {
		VfModuleCustomization vfResourceCustomization = getVfResourceCustomization(modelInfo, service,
				vnfModelCustomizationUUID);
		if (vfResourceCustomization != null) {
			volumeGroup.setModelInfoVfModule(this.mapperLayer.mapCatalogVfModuleToVfModule(vfResourceCustomization));
		}
	}

	protected VfModuleCustomization getVfResourceCustomization(ModelInfo modelInfo, Service service,
			String vnfModelCustomizationUUID) {
		VnfResourceCustomization vnfResourceCustomization = null;
		for (VnfResourceCustomization resourceCust : service.getVnfCustomizations()) {
			if (resourceCust.getModelCustomizationUUID().equalsIgnoreCase(vnfModelCustomizationUUID)) {
				vnfResourceCustomization = resourceCust;
				break;
			}
		}
		if (vnfResourceCustomization != null) {
			for (VfModuleCustomization vfResourceCust : vnfResourceCustomization.getVfModuleCustomizations()) {
				if (vfResourceCust.getModelCustomizationUUID()
						.equalsIgnoreCase(modelInfo.getModelCustomizationUuid())) {
					return vfResourceCust;
				}
			}

		}
		return null;
	}

	protected void populateGenericVnf(ModelInfo modelInfo, String instanceName,
			org.onap.so.serviceinstancebeans.Platform platform,
			org.onap.so.serviceinstancebeans.LineOfBusiness lineOfBusiness, Service service, String bbName,
			ServiceInstance serviceInstance, Map<ResourceKey, String> lookupKeyMap,
			RelatedInstanceList[] relatedInstanceList, String resourceId, String vnfType, List<Map<String, String>> instanceParams) {
		GenericVnf vnf = null;
		ModelInfo instanceGroupModelInfo = null;
		String instanceGroupId = null;
		String generatedVnfType = vnfType;
		if (generatedVnfType == null || generatedVnfType.isEmpty()) {
			generatedVnfType = service.getModelName() + "/" + modelInfo.getModelCustomizationName();
		}
		if (relatedInstanceList != null) {
			for (RelatedInstanceList relatedInstList : relatedInstanceList) {
				RelatedInstance relatedInstance = relatedInstList.getRelatedInstance();
				if (relatedInstance.getModelInfo().getModelType().equals(ModelType.networkInstanceGroup)) {
					instanceGroupModelInfo = relatedInstance.getModelInfo();
					instanceGroupId = relatedInstance.getInstanceId();
				}
			}
		}
		for (GenericVnf vnfTemp : serviceInstance.getVnfs()) {
			if (lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID) != null
					&& vnfTemp.getVnfId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID))) {
				String vnfModelCustId = bbInputSetupUtils.getAAIGenericVnf(vnfTemp.getVnfId()).getModelCustomizationId();
				modelInfo.setModelCustomizationUuid(vnfModelCustId);
				vnf = vnfTemp;
				break;
			}
		}
		if (vnf == null && bbName.equalsIgnoreCase(AssignFlows.VNF.toString())) {
			vnf = createGenericVnf(lookupKeyMap, instanceName, platform, lineOfBusiness,
					resourceId, generatedVnfType, instanceParams);
			serviceInstance.getVnfs().add(vnf);
		}
		if(vnf != null) {
			mapCatalogVnf(vnf, modelInfo, service);
			mapVnfcCollectionInstanceGroup(vnf, modelInfo, service);
			if (instanceGroupId != null && instanceGroupModelInfo != null) {
				mapNetworkCollectionInstanceGroup(vnf, instanceGroupId);
			}
		}
	}

	protected void mapVnfcCollectionInstanceGroup(GenericVnf genericVnf, ModelInfo modelInfo, Service service) {
		VnfResourceCustomization vnfResourceCustomization = getVnfResourceCustomizationFromService(modelInfo, service);
		if(vnfResourceCustomization != null) {
		List<VnfcInstanceGroupCustomization> vnfcInstanceGroups = vnfResourceCustomization
				.getVnfcInstanceGroupCustomizations();
		for (VnfcInstanceGroupCustomization vnfcInstanceGroupCust : vnfcInstanceGroups) {
			InstanceGroup instanceGroup = this.createInstanceGroup();
				instanceGroup.setModelInfoInstanceGroup(this.mapperLayer
						.mapCatalogInstanceGroupToInstanceGroup(null, vnfcInstanceGroupCust.getInstanceGroup()));
			instanceGroup.getModelInfoInstanceGroup().setFunction(vnfcInstanceGroupCust.getFunction());
			instanceGroup.setDescription(vnfcInstanceGroupCust.getDescription());
			genericVnf.getInstanceGroups().add(instanceGroup);
		}
	}
	}

	protected void mapNetworkCollectionInstanceGroup(GenericVnf genericVnf, String instanceGroupId) {
		org.onap.aai.domain.yang.InstanceGroup aaiInstanceGroup = bbInputSetupUtils
				.getAAIInstanceGroup(instanceGroupId);
		InstanceGroup instanceGroup = this.mapperLayer.mapAAIInstanceGroupIntoInstanceGroup(aaiInstanceGroup);
		instanceGroup.setModelInfoInstanceGroup(this.mapperLayer.mapCatalogInstanceGroupToInstanceGroup(
				null, this.bbInputSetupUtils.getCatalogInstanceGroup(aaiInstanceGroup.getModelVersionId())));
		genericVnf.getInstanceGroups().add(instanceGroup);
	}

	protected GenericVnf createGenericVnf(Map<ResourceKey, String> lookupKeyMap, String instanceName,
			org.onap.so.serviceinstancebeans.Platform platform,
			org.onap.so.serviceinstancebeans.LineOfBusiness lineOfBusiness, String vnfId, String vnfType, List<Map<String, String>> instanceParams) {
		lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, vnfId);
		GenericVnf genericVnf = new GenericVnf();
		genericVnf.setVnfId(vnfId);
		genericVnf.setVnfName(instanceName);
		genericVnf.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		genericVnf.setVnfType(vnfType);
		if (platform != null) {
			genericVnf.setPlatform(this.mapperLayer.mapRequestPlatform(platform));
		}
		if (lineOfBusiness != null) {
			genericVnf.setLineOfBusiness(this.mapperLayer.mapRequestLineOfBusiness(lineOfBusiness));
		}
		if(instanceParams != null) {
			for(Map<String, String> params : instanceParams) {
				genericVnf.getCloudParams().putAll(params);
			}
		}
		return genericVnf;
	}

	protected void mapCatalogVnf(GenericVnf genericVnf, ModelInfo modelInfo, Service service) {
		VnfResourceCustomization vnfResourceCustomization = getVnfResourceCustomizationFromService(modelInfo, service);
		if (vnfResourceCustomization != null) {
			genericVnf.setModelInfoGenericVnf(this.mapperLayer.mapCatalogVnfToVnf(vnfResourceCustomization));
		}
	}

	protected VnfResourceCustomization getVnfResourceCustomizationFromService(ModelInfo modelInfo, Service service) {
		VnfResourceCustomization vnfResourceCustomization = null;
		for (VnfResourceCustomization resourceCust : service.getVnfCustomizations()) {
			if (resourceCust.getModelCustomizationUUID().equalsIgnoreCase(modelInfo.getModelCustomizationUuid())) {
				vnfResourceCustomization = resourceCust;
				break;
			}
		}
		return vnfResourceCustomization;
	}

	protected void populateL3Network(String instanceName, ModelInfo modelInfo, Service service, String bbName,
			ServiceInstance serviceInstance, Map<ResourceKey, String> lookupKeyMap, String resourceId, List<Map<String, String>> instanceParams) {
		L3Network network = null;
		for (L3Network networkTemp : serviceInstance.getNetworks()) {
			if (lookupKeyMap.get(ResourceKey.NETWORK_ID) != null
					&& networkTemp.getNetworkId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.NETWORK_ID))) {
				network = networkTemp;
				break;
			}
		}
		if (network == null
				&& (bbName.equalsIgnoreCase(AssignFlows.NETWORK_A_LA_CARTE.toString()) 
						|| bbName.equalsIgnoreCase(AssignFlows.NETWORK_MACRO.toString()))) {
			network = createNetwork(lookupKeyMap, instanceName, resourceId, instanceParams);
			serviceInstance.getNetworks().add(network);
		}
		if(network != null) {
			mapCatalogNetwork(network, modelInfo, service);
		}
	}

	protected L3Network createNetwork(Map<ResourceKey, String> lookupKeyMap, String instanceName,
			String networkId, List<Map<String, String>> instanceParams) {
		lookupKeyMap.put(ResourceKey.NETWORK_ID, networkId);
		L3Network network = new L3Network();
		network.setNetworkId(networkId);
		network.setNetworkName(instanceName);
		network.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		if(instanceParams != null) {
			for(Map<String, String> params : instanceParams) {
				network.getCloudParams().putAll(params);
			}
		}
		return network;
	}

	protected void mapCatalogNetwork(L3Network network, ModelInfo modelInfo, Service service) {
		NetworkResourceCustomization networkResourceCustomization = null;
		for (NetworkResourceCustomization resourceCust : service.getNetworkCustomizations()) {
			if (resourceCust.getModelCustomizationUUID().equalsIgnoreCase(modelInfo.getModelCustomizationUuid())) {
				networkResourceCustomization = resourceCust;
				break;
			}
		}
		if (networkResourceCustomization != null) {
			network.setModelInfoNetwork(this.mapperLayer.mapCatalogNetworkToNetwork(networkResourceCustomization));
		}
	}

	protected GeneralBuildingBlock getGBBALaCarteService(ExecuteBuildingBlock executeBB, RequestDetails requestDetails,
			Map<ResourceKey, String> lookupKeyMap, String requestAction, String resourceId) throws Exception {
		Customer customer = getCustomerAndServiceSubscription(requestDetails, resourceId);
		if (customer != null) {
			Project project = null;
			OwningEntity owningEntity = null;

			if (requestDetails.getProject() != null)
				project = mapperLayer.mapRequestProject(requestDetails.getProject());
			if (requestDetails.getOwningEntity() != null)
				owningEntity = mapperLayer.mapRequestOwningEntity(requestDetails.getOwningEntity());


			Service service = bbInputSetupUtils
					.getCatalogServiceByModelUUID(requestDetails.getModelInfo().getModelVersionId());
			if (service == null) {
				service = bbInputSetupUtils.getCatalogServiceByModelVersionAndModelInvariantUUID(
						requestDetails.getModelInfo().getModelVersion(),
						requestDetails.getModelInfo().getModelInvariantId());
			if(service == null) {
					throw new Exception("Could not find service for model version Id: "
							+ requestDetails.getModelInfo().getModelVersionId() + " and for model invariant Id: "
							+ requestDetails.getModelInfo().getModelInvariantId());
				}
			}
			ServiceInstance serviceInstance = this.getALaCarteServiceInstance(service, requestDetails, customer,
					project, owningEntity, lookupKeyMap, resourceId, Boolean.TRUE.equals(executeBB.isaLaCarte()),
					executeBB.getBuildingBlock().getBpmnFlowName());
			return this.populateGBBWithSIAndAdditionalInfo(requestDetails, serviceInstance, executeBB, requestAction, customer);
		} else {
			throw new Exception("Could not find customer");
		}
	}

	protected Customer getCustomerAndServiceSubscription(RequestDetails requestDetails, String resourceId) {
		Customer customer;
		if (requestDetails.getSubscriberInfo() != null) {
			customer = this.getCustomerFromRequest(requestDetails);
		} else {
			customer = this.getCustomerFromURI(resourceId);
		}
		if (customer != null) {
			ServiceSubscription serviceSubscription = null;
			serviceSubscription = getServiceSubscription(requestDetails, customer);
			if (serviceSubscription == null) {
				serviceSubscription = getServiceSubscriptionFromURI(resourceId, customer);
			}
			customer.setServiceSubscription(serviceSubscription);
			return customer;
		} else {
			return null;
		}
	}

	protected ServiceSubscription getServiceSubscriptionFromURI(String resourceId, Customer customer) {
		Map<String, String> uriKeys = bbInputSetupUtils.getURIKeysFromServiceInstance(resourceId);
		String subscriptionServiceType = uriKeys.get("service-type");
		org.onap.aai.domain.yang.ServiceSubscription serviceSubscriptionAAI = bbInputSetupUtils
				.getAAIServiceSubscription(customer.getGlobalCustomerId(), subscriptionServiceType);
		if (serviceSubscriptionAAI != null) {
			return mapperLayer.mapAAIServiceSubscription(serviceSubscriptionAAI);
		} else {
			return null;
		}
	}

	protected Customer getCustomerFromURI(String resourceId) {
		Map<String, String> uriKeys = bbInputSetupUtils.getURIKeysFromServiceInstance(resourceId);
		String globalCustomerId = uriKeys.get("global-customer-id");
		org.onap.aai.domain.yang.Customer customerAAI = this.bbInputSetupUtils.getAAICustomer(globalCustomerId);
		if (customerAAI != null) {
			return mapperLayer.mapAAICustomer(customerAAI);
		} else {
			return null;
		}
	}

	protected GeneralBuildingBlock populateGBBWithSIAndAdditionalInfo(RequestDetails requestDetails,
			ServiceInstance serviceInstance, ExecuteBuildingBlock executeBB, String requestAction, Customer customer) {
		GeneralBuildingBlock outputBB = new GeneralBuildingBlock();
		OrchestrationContext orchContext = mapperLayer.mapOrchestrationContext(requestDetails);
		RequestContext requestContext = mapperLayer.mapRequestContext(requestDetails);
		requestContext.setAction(requestAction);
		requestContext.setMsoRequestId(executeBB.getRequestId());
		org.onap.aai.domain.yang.CloudRegion aaiCloudRegion = bbInputSetupUtils
				.getCloudRegion(requestDetails.getCloudConfiguration());
		CloudRegion cloudRegion = mapperLayer.mapCloudRegion(requestDetails.getCloudConfiguration(), aaiCloudRegion);
		outputBB.setOrchContext(orchContext);
		outputBB.setRequestContext(requestContext);
		outputBB.setCloudRegion(cloudRegion);
		if(customer == null){
			Map<String, String> uriKeys = bbInputSetupUtils.getURIKeysFromServiceInstance(serviceInstance.getServiceInstanceId());
			String globalCustomerId = uriKeys.get("global-customer-id");
			String subscriptionServiceType = uriKeys.get("service-type");
			customer = mapCustomer(globalCustomerId, subscriptionServiceType);
		}
		outputBB.setServiceInstance(serviceInstance);
		if (customer.getServiceSubscription() != null) {
			customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
		}
		outputBB.setCustomer(customer);
		return outputBB;
	}

	protected ServiceSubscription getServiceSubscription(RequestDetails requestDetails, Customer customer) {
		org.onap.aai.domain.yang.ServiceSubscription aaiServiceSubscription = bbInputSetupUtils
				.getAAIServiceSubscription(customer.getGlobalCustomerId(),
						requestDetails.getRequestParameters().getSubscriptionServiceType());
		if (aaiServiceSubscription != null) {
		return mapperLayer.mapAAIServiceSubscription(aaiServiceSubscription);
		} else {
			return null;
		}
	}

	protected Customer getCustomerFromRequest(RequestDetails requestDetails) {
		org.onap.aai.domain.yang.Customer aaiCustomer = bbInputSetupUtils
				.getAAICustomer(requestDetails.getSubscriberInfo().getGlobalSubscriberId());
		if (aaiCustomer != null) {
		return mapperLayer.mapAAICustomer(aaiCustomer);
		} else {
			return null;
		}
	}

	protected ServiceInstance getALaCarteServiceInstance(Service service, RequestDetails requestDetails,
			Customer customer, Project project, OwningEntity owningEntity, Map<ResourceKey, String> lookupKeyMap,
			String serviceInstanceId, boolean aLaCarte, String bbName) throws Exception {
		ServiceInstance serviceInstance = this.getServiceInstanceHelper(requestDetails, customer, project, owningEntity,
				lookupKeyMap, serviceInstanceId, aLaCarte, service, bbName);
		org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI = this.bbInputSetupUtils
				.getAAIServiceInstanceById(serviceInstanceId);
		if (serviceInstanceAAI != null
				&& !serviceInstanceAAI.getModelVersionId().equalsIgnoreCase(service.getModelUUID())) {
			Service tempService = this.bbInputSetupUtils
					.getCatalogServiceByModelUUID(serviceInstanceAAI.getModelVersionId());
			if (tempService != null) {
				serviceInstance
						.setModelInfoServiceInstance(mapperLayer.mapCatalogServiceIntoServiceInstance(tempService));
				return serviceInstance;
			} else {
				throw new Exception(
						"Could not find model of existing SI. Service Instance in AAI already exists with different model version id: "
								+ serviceInstanceAAI.getModelVersionId());
			}
		}
		serviceInstance.setModelInfoServiceInstance(mapperLayer.mapCatalogServiceIntoServiceInstance(service));
		return serviceInstance;
	}

	protected GeneralBuildingBlock getGBBMacro(ExecuteBuildingBlock executeBB, RequestDetails requestDetails,
			Map<ResourceKey, String> lookupKeyMap, String requestAction, String resourceId, String vnfType)
			throws Exception {
		String bbName = executeBB.getBuildingBlock().getBpmnFlowName();
		String key = executeBB.getBuildingBlock().getKey();
		GeneralBuildingBlock gBB = this.getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap, requestAction,
				resourceId);
		RequestParameters requestParams = requestDetails.getRequestParameters();
		Service service = null;
		if (gBB != null && gBB.getServiceInstance() != null
				&& gBB.getServiceInstance().getModelInfoServiceInstance() != null
				&& gBB.getServiceInstance().getModelInfoServiceInstance().getModelUuid() != null) {
			service = bbInputSetupUtils.getCatalogServiceByModelUUID(
					gBB.getServiceInstance().getModelInfoServiceInstance().getModelUuid());
		} else {
			throw new Exception("Could not get service instance for macro request");
		}
		if (requestParams != null && requestParams.getUserParams() != null) {
			for(Map<String, Object> userParams : requestParams.getUserParams()) {
				if(userParams.containsKey("service")) {
					String input = mapper.writeValueAsString(userParams.get("service"));
					return getGBBMacroUserParams(executeBB, requestDetails, lookupKeyMap, vnfType, bbName, key, gBB,
							requestParams, service, input);
				}
			}
		}
		if (requestAction.equalsIgnoreCase("deactivateInstance")) {
			return gBB;
		} else if (requestAction.equalsIgnoreCase("createInstance")) {
		return getGBBMacroNoUserParamsCreate(executeBB, lookupKeyMap, bbName, key, gBB, service);
		} else if (requestAction.equalsIgnoreCase("deleteInstance")
				|| requestAction.equalsIgnoreCase("unassignInstance")
				|| requestAction.equalsIgnoreCase("activateInstance")
				|| requestAction.equalsIgnoreCase("activateFabricConfiguration")) {
			return getGBBMacroExistingService(executeBB, lookupKeyMap, bbName, gBB, service, requestAction,
					requestDetails.getCloudConfiguration());
		} else {
		throw new IllegalArgumentException(
				"No user params on requestAction: assignInstance. Please specify user params.");
		}
	}

	protected GeneralBuildingBlock getGBBMacroNoUserParamsCreate(ExecuteBuildingBlock executeBB,
			Map<ResourceKey, String> lookupKeyMap, String bbName, String key, GeneralBuildingBlock gBB, Service service)
			throws Exception {
		ServiceInstance serviceInstance = gBB.getServiceInstance();
		if (bbName.contains(NETWORK) && !bbName.contains(NETWORK_COLLECTION)) {
			String networkId = lookupKeyMap.get(ResourceKey.NETWORK_ID);
			ModelInfo networkModelInfo = new ModelInfo();
			if((!Boolean.TRUE.equals(executeBB.getBuildingBlock().getIsVirtualLink()))) {
				NetworkResourceCustomization networkCust = getNetworkCustomizationByKey(key, service);
				if (networkCust != null) {
					networkModelInfo.setModelCustomizationUuid(networkCust.getModelCustomizationUUID());
					this.populateL3Network(null, networkModelInfo, service, bbName, serviceInstance, lookupKeyMap,
							networkId, null);
				} else {
					msoLogger.debug("Could not find a network customization with key: " + key);
				}
			} else {
				msoLogger.debug("Orchestrating on Collection Network Resource Customization");
				CollectionNetworkResourceCustomization collectionNetworkResourceCust = bbInputSetupUtils.getCatalogCollectionNetworkResourceCustByID(key);
				L3Network l3Network = getVirtualLinkL3Network(lookupKeyMap, bbName, key, networkId, collectionNetworkResourceCust, serviceInstance);
				NetworkResourceCustomization networkResourceCustomization = 
						mapperLayer.mapCollectionNetworkResourceCustToNetworkResourceCust(collectionNetworkResourceCust);
				if(l3Network != null) {
					l3Network.setModelInfoNetwork(mapperLayer.mapCatalogNetworkToNetwork(networkResourceCustomization));
				}
			}
		} else if(bbName.contains("Configuration")) {
			String configurationId = lookupKeyMap.get(ResourceKey.CONFIGURATION_ID);
			ModelInfo configurationModelInfo = new ModelInfo();
			configurationModelInfo.setModelCustomizationUuid(key);
			this.populateConfiguration(configurationModelInfo, service, bbName, serviceInstance, lookupKeyMap, configurationId, null, executeBB.getConfigurationResourceKeys());
		}
		if (executeBB.getWorkflowResourceIds() != null) {
			this.populateNetworkCollectionAndInstanceGroupAssign(service, bbName, serviceInstance,
					executeBB.getWorkflowResourceIds().getNetworkCollectionId(), key);
		}
		return gBB;
	}

	protected L3Network getVirtualLinkL3Network(Map<ResourceKey, String> lookupKeyMap, String bbName, String key,
			String networkId, CollectionNetworkResourceCustomization collectionNetworkResourceCust, ServiceInstance serviceInstance) {
		if(collectionNetworkResourceCust != null) {
			if((bbName.equalsIgnoreCase(AssignFlows.NETWORK_A_LA_CARTE.toString())
				|| bbName.equalsIgnoreCase(AssignFlows.NETWORK_MACRO.toString()))) {
				L3Network network = createNetwork(lookupKeyMap, null, networkId, null);				
				serviceInstance.getNetworks().add(network);
				return network;
			} else {
				for (L3Network network : serviceInstance.getNetworks()) {
					if (network.getNetworkId().equalsIgnoreCase(networkId)) {
						return network;
					}
				}
			}
		}
		return null;
	}

	protected NetworkResourceCustomization getNetworkCustomizationByKey(String key, Service service) {
		for (NetworkResourceCustomization networkCust : service.getNetworkCustomizations()) {
			if (networkCust.getModelCustomizationUUID().equalsIgnoreCase(key)) {
				return networkCust;
			}
		}
		return null;
	}

	protected GeneralBuildingBlock getGBBMacroExistingService(ExecuteBuildingBlock executeBB,
			Map<ResourceKey, String> lookupKeyMap, String bbName, GeneralBuildingBlock gBB, Service service,
			String requestAction, CloudConfiguration cloudConfiguration) throws Exception {
		ServiceInstance serviceInstance = gBB.getServiceInstance();
		if (cloudConfiguration != null && requestAction.equalsIgnoreCase("deleteInstance")) {
			org.onap.aai.domain.yang.CloudRegion aaiCloudRegion = bbInputSetupUtils.getCloudRegion(cloudConfiguration);
			CloudRegion cloudRegion = mapperLayer.mapCloudRegion(cloudConfiguration, aaiCloudRegion);
			gBB.setCloudRegion(cloudRegion);
		}
		if (bbName.contains(VNF)) {
			for (GenericVnf genericVnf : serviceInstance.getVnfs()) {
				if (lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID) != null
						&& genericVnf.getVnfId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID))) {
					org.onap.aai.domain.yang.GenericVnf vnf = bbInputSetupUtils.getAAIGenericVnf(genericVnf.getVnfId());
					ModelInfo modelInfo = new ModelInfo();
					if (vnf != null) {
						modelInfo.setModelCustomizationUuid(vnf.getModelCustomizationId());
					}
					this.mapCatalogVnf(genericVnf, modelInfo, service);
				}
			}
		} else if (bbName.contains(VF_MODULE)) {
			for (GenericVnf vnf : serviceInstance.getVnfs()) {
				for (VfModule vfModule : vnf.getVfModules()) {
					if (lookupKeyMap.get(ResourceKey.VF_MODULE_ID) != null
							&& vfModule.getVfModuleId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.VF_MODULE_ID))) {
						String vnfModelCustomizationUUID = this.bbInputSetupUtils.getAAIGenericVnf(vnf.getVnfId())
								.getModelCustomizationId();
						ModelInfo vnfModelInfo = new ModelInfo();
						vnfModelInfo.setModelCustomizationUuid(vnfModelCustomizationUUID);
						this.mapCatalogVnf(vnf, vnfModelInfo, service);
						lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, vnf.getVnfId());
						String vfModuleCustomizationUUID = this.bbInputSetupUtils
								.getAAIVfModule(vnf.getVnfId(), vfModule.getVfModuleId()).getModelCustomizationId();
						ModelInfo vfModuleModelInfo = new ModelInfo();
						vfModuleModelInfo.setModelCustomizationId(vfModuleCustomizationUUID);
						this.mapCatalogVfModule(vfModule, vfModuleModelInfo, service, vnfModelCustomizationUUID);
						break;
					}
				}
			}
		} else if (bbName.contains(VOLUME_GROUP)) {
			for (GenericVnf vnf : serviceInstance.getVnfs()) {
				for (VolumeGroup volumeGroup : vnf.getVolumeGroups()) {
					if (lookupKeyMap.get(ResourceKey.VOLUME_GROUP_ID) != null && volumeGroup.getVolumeGroupId()
							.equalsIgnoreCase(lookupKeyMap.get(ResourceKey.VOLUME_GROUP_ID))) {
						String vnfModelCustomizationUUID = this.bbInputSetupUtils.getAAIGenericVnf(vnf.getVnfId())
								.getModelCustomizationId();
						ModelInfo vnfModelInfo = new ModelInfo();
						vnfModelInfo.setModelCustomizationUuid(vnfModelCustomizationUUID);
						this.mapCatalogVnf(vnf, vnfModelInfo, service);
						lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, vnf.getVnfId());
						if (cloudConfiguration != null) {
							String volumeGroupCustomizationUUID = this.bbInputSetupUtils.getAAIVolumeGroup(cloudConfiguration.getCloudOwner(),
									cloudConfiguration.getLcpCloudRegionId(), volumeGroup.getVolumeGroupId())
									.getModelCustomizationId();
							ModelInfo volumeGroupModelInfo = new ModelInfo();
							volumeGroupModelInfo.setModelCustomizationId(volumeGroupCustomizationUUID);
							this.mapCatalogVolumeGroup(volumeGroup, volumeGroupModelInfo, service,
									vnfModelCustomizationUUID);
						}
						break;
					}
				}
			}
		} else if (bbName.contains(NETWORK)) {
			for (L3Network network : serviceInstance.getNetworks()) {
				if (lookupKeyMap.get(ResourceKey.NETWORK_ID) != null
						&& network.getNetworkId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.NETWORK_ID))) {
					String networkCustomizationUUID = this.bbInputSetupUtils.getAAIL3Network(network.getNetworkId())
							.getModelCustomizationId();
					ModelInfo modelInfo = new ModelInfo();
					modelInfo.setModelCustomizationUuid(networkCustomizationUUID);
					this.mapCatalogNetwork(network, modelInfo, service);
					break;
				}
			}
		} else if (bbName.contains("Fabric")) {
			for(Configuration configuration : serviceInstance.getConfigurations()) {
				if(lookupKeyMap.get(ResourceKey.CONFIGURATION_ID) != null
						&& configuration.getConfigurationId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.CONFIGURATION_ID))) {
					String configurationCustUUID = this.bbInputSetupUtils.getAAIConfiguration(configuration.getConfigurationId())
							.getModelCustomizationId();
					ModelInfo modelInfo = new ModelInfo();
					modelInfo.setModelCustomizationUuid(configurationCustUUID);
					this.mapCatalogConfiguration(configuration, modelInfo, service, executeBB.getConfigurationResourceKeys());
					break;
				}
			}
		}
		if (executeBB.getWorkflowResourceIds() != null) {
			this.populateNetworkCollectionAndInstanceGroupAssign(service, bbName, serviceInstance,
					executeBB.getWorkflowResourceIds().getNetworkCollectionId(), executeBB.getBuildingBlock().getKey());
		}
		return gBB;
	}

	protected GeneralBuildingBlock getGBBMacroUserParams(ExecuteBuildingBlock executeBB, RequestDetails requestDetails,
			Map<ResourceKey, String> lookupKeyMap, String vnfType, String bbName, String key, GeneralBuildingBlock gBB,
			RequestParameters requestParams, Service service, String input) throws Exception {
		ServiceInstance serviceInstance = gBB.getServiceInstance();
		org.onap.so.serviceinstancebeans.Service serviceMacro = mapper.readValue(input,
				org.onap.so.serviceinstancebeans.Service.class);

		Resources resources = serviceMacro.getResources();
		Vnfs vnfs = null;
		VfModules vfModules = null;
		Networks networks = null;
		CloudConfiguration cloudConfiguration = requestDetails.getCloudConfiguration();
		CloudRegion cloudRegion = getCloudRegionFromMacroRequest(cloudConfiguration, resources);
		gBB.setCloudRegion(cloudRegion);
		if (bbName.contains(VNF)) {
			vnfs = findVnfsByKey(key, resources, vnfs);
			String vnfId = lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID);
			// This stores the vnf id in request db to be retrieved later when
			// working on a vf module or volume group
			InfraActiveRequests request = this.bbInputSetupUtils.getInfraActiveRequest(executeBB.getRequestId());
			if (request != null) {
				this.bbInputSetupUtils.updateInfraActiveRequestVnfId(request, vnfId);
			}
			this.populateGenericVnf(vnfs.getModelInfo(), vnfs.getInstanceName(), vnfs.getPlatform(),
					vnfs.getLineOfBusiness(), service, bbName, serviceInstance, lookupKeyMap, null, vnfId, vnfType, vnfs.getInstanceParams());
		} else if (bbName.contains(VF_MODULE) || bbName.contains(VOLUME_GROUP)) {
			Pair<Vnfs, VfModules> vnfsAndVfModules = getVfModulesAndItsVnfsByKey(key, resources);
			vfModules = vnfsAndVfModules.getValue1();
			vnfs = vnfsAndVfModules.getValue0();
			lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, getVnfId(executeBB, lookupKeyMap));
			if (vnfs == null) {
				throw new Exception("Could not find Vnf to orchestrate VfModule");
			}
			ModelInfo modelInfo = vfModules.getModelInfo();
			if (bbName.contains(VOLUME_GROUP)) {
				String volumeGroupId = lookupKeyMap.get(ResourceKey.VOLUME_GROUP_ID);
				this.populateVolumeGroup(modelInfo, service, bbName, serviceInstance, lookupKeyMap, volumeGroupId, null,
						vfModules.getVolumeGroupInstanceName(), vnfType, vfModules.getInstanceParams());
			} else {
				String vfModuleId = lookupKeyMap.get(ResourceKey.VF_MODULE_ID);
				CloudConfiguration cloudConfig = new CloudConfiguration();
				cloudConfig.setLcpCloudRegionId(cloudRegion.getLcpCloudRegionId());
				cloudConfig.setCloudOwner(cloudRegion.getCloudOwner());
				this.populateVfModule(modelInfo, service, bbName, serviceInstance, lookupKeyMap, vfModuleId, null,
						vfModules.getInstanceName(), vfModules.getInstanceParams(), cloudConfig);
			}
		} else if (bbName.contains(NETWORK)) {
			networks = findNetworksByKey(key, resources);
			String networkId = lookupKeyMap.get(ResourceKey.NETWORK_ID);
			this.populateL3Network(networks.getInstanceName(), networks.getModelInfo(), service, bbName,
					serviceInstance, lookupKeyMap, networkId, networks.getInstanceParams());
		} else if (bbName.contains("Configuration")) {
			String configurationId = lookupKeyMap.get(ResourceKey.CONFIGURATION_ID);
			ModelInfo configurationModelInfo = new ModelInfo();
			configurationModelInfo.setModelCustomizationUuid(key);
			ConfigurationResourceCustomization configurationCust = findConfigurationResourceCustomization(configurationModelInfo, service);
			if(configurationCust != null) {
				this.populateConfiguration(configurationModelInfo, service, bbName, serviceInstance, lookupKeyMap, configurationId, null, executeBB.getConfigurationResourceKeys());
			} else {
				msoLogger.debug("Could not find a configuration customization with key: " + key);
			}
		}
		return gBB;
	}
	
	protected Networks findNetworksByKey(String key, Resources resources) {
		for (Networks networks : resources.getNetworks()) {
			if (networks.getModelInfo().getModelCustomizationId().equalsIgnoreCase(key)) {
				return networks;
			}
		}
		return null;
	}

	protected Pair<Vnfs, VfModules> getVfModulesAndItsVnfsByKey(String key, Resources resources) {
		for (Vnfs vnfs : resources.getVnfs()) {
			for (VfModules vfModules : vnfs.getVfModules()) {
				if (vfModules.getModelInfo().getModelCustomizationId().equalsIgnoreCase(key)) {
					return new Pair<Vnfs, VfModules>(vnfs, vfModules);
				}
			}
		}
		return null;
	}

	protected Vnfs findVnfsByKey(String key, Resources resources, Vnfs vnfs) {
		for (Vnfs tempVnfs : resources.getVnfs()) {
			if (tempVnfs.getModelInfo().getModelCustomizationId().equalsIgnoreCase(key)) {
				vnfs = tempVnfs;
				break;
			}
		}
		return vnfs;
	}

	protected CloudRegion getCloudRegionFromMacroRequest(CloudConfiguration cloudConfiguration, Resources resources) {
		if(cloudConfiguration == null) {
			for(Vnfs vnfs : resources.getVnfs()) {
				if(cloudConfiguration == null) {
					cloudConfiguration = vnfs.getCloudConfiguration();
				} else {
					break;
				}
				for(VfModules vfModules : vnfs.getVfModules()) {
					if(cloudConfiguration == null) {
						cloudConfiguration = vfModules.getCloudConfiguration();
					} else {
						break;
					}
				}
			}
			for(Networks networks : resources.getNetworks()) {
				if(cloudConfiguration == null) {
				cloudConfiguration = networks.getCloudConfiguration();
				} else {
					break;
			}
		}
		}
		if(cloudConfiguration != null) {
			org.onap.aai.domain.yang.CloudRegion aaiCloudRegion = bbInputSetupUtils.getCloudRegion(cloudConfiguration);
			return mapperLayer.mapCloudRegion(cloudConfiguration, aaiCloudRegion);
		} else {
			msoLogger.debug("Could not find any cloud configuration for this request.");
			return null;
		}
	}

	protected String getVnfId(ExecuteBuildingBlock executeBB, Map<ResourceKey, String> lookupKeyMap) {
		String vnfId = lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID);
		if (vnfId == null) {
			InfraActiveRequests request = this.bbInputSetupUtils.getInfraActiveRequest(executeBB.getRequestId());
			vnfId = request.getVnfId();
		}

		return vnfId;
	}

	protected String generateRandomUUID() {
		return UUID.randomUUID().toString();
	}

	protected ServiceInstance getServiceInstanceHelper(RequestDetails requestDetails, Customer customer,
			Project project, OwningEntity owningEntity, Map<ResourceKey, String> lookupKeyMap, String serviceInstanceId,
			boolean aLaCarte, Service service, String bbName) throws Exception {
		if (requestDetails.getRequestInfo().getInstanceName() == null && aLaCarte
				&& bbName.equalsIgnoreCase(AssignFlows.SERVICE_INSTANCE.toString())) {
			throw new Exception("Request invalid missing: RequestInfo:InstanceName");
		} else {
			org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI = null;
			if (aLaCarte && bbName.equalsIgnoreCase(AssignFlows.SERVICE_INSTANCE.toString())) {
				serviceInstanceAAI = bbInputSetupUtils
					.getAAIServiceInstanceByName(requestDetails.getRequestInfo().getInstanceName(), customer);
			}
			if (serviceInstanceId != null && serviceInstanceAAI == null) {
				serviceInstanceAAI = bbInputSetupUtils.getAAIServiceInstanceById(serviceInstanceId);
			}
			if (serviceInstanceAAI != null) {
				lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, serviceInstanceId);
				return this.getExistingServiceInstance(serviceInstanceAAI);
			} else {
				return createServiceInstance(requestDetails, project, owningEntity, lookupKeyMap,
						serviceInstanceId);
			}
		}
	}

	protected ServiceInstance createServiceInstance(RequestDetails requestDetails, Project project,
													OwningEntity owningEntity, Map<ResourceKey, String> lookupKeyMap, String serviceInstanceId) {
		ServiceInstance serviceInstance = new ServiceInstance();
		lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, serviceInstanceId);
		serviceInstance.setServiceInstanceId(serviceInstanceId);
		if(requestDetails.getRequestInfo() != null) {
		serviceInstance.setServiceInstanceName(requestDetails.getRequestInfo().getInstanceName());
		}
		serviceInstance.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		serviceInstance.setProject(project);
		serviceInstance.setOwningEntity(owningEntity);
		return serviceInstance;
	}

	public ServiceInstance getExistingServiceInstance(org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI)
			throws Exception {
		ServiceInstance serviceInstance = mapperLayer.mapAAIServiceInstanceIntoServiceInstance(serviceInstanceAAI);
		if (serviceInstanceAAI.getRelationshipList() != null
				&& serviceInstanceAAI.getRelationshipList().getRelationship() != null
				&& !serviceInstanceAAI.getRelationshipList().getRelationship().isEmpty()) {
			addRelationshipsToSI(serviceInstanceAAI, serviceInstance);
		}
		return serviceInstance;
	}

	protected void populateNetworkCollectionAndInstanceGroupAssign(Service service, String bbName,
			ServiceInstance serviceInstance, String resourceId, String key) throws Exception {
		if (serviceInstance.getCollection() == null
				&& bbName.equalsIgnoreCase(AssignFlows.NETWORK_COLLECTION.toString())) {
			Collection collection = this.createCollection(resourceId);
			serviceInstance.setCollection(collection);
			this.mapCatalogCollection(service, serviceInstance.getCollection(), key);
			if(isVlanTagging(service, key)) {
			InstanceGroup instanceGroup = this.createInstanceGroup();
			serviceInstance.getCollection().setInstanceGroup(instanceGroup);
				this.mapCatalogNetworkCollectionInstanceGroup(service,
						serviceInstance.getCollection().getInstanceGroup(), key);
			}
		}
	}

	protected CollectionResourceCustomization findCatalogNetworkCollection(Service service, String key) {
		for(CollectionResourceCustomization collectionCust : service.getCollectionResourceCustomizations()){
			if(collectionCust.getModelCustomizationUUID().equalsIgnoreCase(key)) {
				return collectionCust;
			}
		}
			return null;
	}
			
	protected boolean isVlanTagging(Service service, String key) {
		CollectionResourceCustomization collectionCust = findCatalogNetworkCollection(service, key);
		if (collectionCust != null) {
			CollectionResource collectionResource = collectionCust.getCollectionResource();
			if (collectionResource != null
					&& collectionResource.getInstanceGroup() != null
					&& collectionResource.getInstanceGroup().getToscaNodeType() != null
					&& collectionResource.getInstanceGroup().getToscaNodeType().contains("NetworkCollection")) {
				return true;
			}
		}
		return false;
	}

	protected void mapCatalogNetworkCollectionInstanceGroup(Service service, InstanceGroup instanceGroup, String key) {
		CollectionResourceCustomization collectionCust = this.findCatalogNetworkCollection(service, key);
		org.onap.so.db.catalog.beans.InstanceGroup catalogInstanceGroup = collectionCust.getCollectionResource().getInstanceGroup();
		instanceGroup.setModelInfoInstanceGroup(
				mapperLayer.mapCatalogInstanceGroupToInstanceGroup(collectionCust, catalogInstanceGroup));
		}

	protected void mapCatalogCollection(Service service, Collection collection, String key) {
		CollectionResourceCustomization collectionCust = findCatalogNetworkCollection(service, key);
		if (collectionCust != null) {
			CollectionResource collectionResource = collectionCust.getCollectionResource();
			if (collectionResource != null) {
				collection.setModelInfoCollection(
						mapperLayer.mapCatalogCollectionToCollection(collectionCust, collectionResource));
			}
		}
	}

	protected Collection createCollection(String collectionId) {
		Collection collection = new Collection();
		collection.setId(collectionId);
		collection.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		return collection;
	}

	protected InstanceGroup createInstanceGroup() {
		InstanceGroup instanceGroup = new InstanceGroup();
		String instanceGroupId = this.generateRandomUUID();
		instanceGroup.setId(instanceGroupId);
		return instanceGroup;
	}

	protected void addRelationshipsToSI(org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI,
			ServiceInstance serviceInstance) throws Exception {
		AAIResultWrapper serviceInstanceWrapper = new AAIResultWrapper(
				new AAICommonObjectMapperProvider().getMapper().writeValueAsString(serviceInstanceAAI));
		Optional<Relationships> relationshipsOp = serviceInstanceWrapper.getRelationships();
		Relationships relationships = null;
		if (relationshipsOp.isPresent()) {
			relationships = relationshipsOp.get();
		} else {
			return;
		}

		this.mapProject(relationships.getByType(AAIObjectType.PROJECT), serviceInstance);
		this.mapOwningEntity(relationships.getByType(AAIObjectType.OWNING_ENTITY), serviceInstance);
		this.mapL3Networks(relationships.getRelatedAAIUris(AAIObjectType.L3_NETWORK), serviceInstance.getNetworks());
		this.mapGenericVnfs(relationships.getRelatedAAIUris(AAIObjectType.GENERIC_VNF), serviceInstance.getVnfs());
		this.mapCollection(relationships.getByType(AAIObjectType.COLLECTION), serviceInstance);
		this.mapConfigurations(relationships.getRelatedAAIUris(AAIObjectType.CONFIGURATION), serviceInstance.getConfigurations());
	}

	protected void mapConfigurations(List<AAIResourceUri> relatedAAIUris, List<Configuration> configurations) {
		for (AAIResourceUri aaiResourceUri : relatedAAIUris) {
			configurations.add(mapConfiguration(aaiResourceUri));
		}
	}

	protected Configuration mapConfiguration(AAIResourceUri aaiResourceUri) {
		AAIResultWrapper aaiConfigurationWrapper = this.bbInputSetupUtils.getAAIResourceDepthOne(aaiResourceUri);
		Optional<org.onap.aai.domain.yang.Configuration> aaiConfigurationOp = aaiConfigurationWrapper
				.asBean(org.onap.aai.domain.yang.Configuration.class);
		if (!aaiConfigurationOp.isPresent()) {
			return null;
		}

		return this.mapperLayer.mapAAIConfiguration(aaiConfigurationOp.get());
	}

	protected void mapGenericVnfs(List<AAIResourceUri> list, List<GenericVnf> genericVnfs) {
		for (AAIResourceUri aaiResourceUri : list) {
			genericVnfs.add(this.mapGenericVnf(aaiResourceUri));
		}
	}

	protected GenericVnf mapGenericVnf(AAIResourceUri aaiResourceUri) {
		AAIResultWrapper aaiGenericVnfWrapper = this.bbInputSetupUtils.getAAIResourceDepthOne(aaiResourceUri);
		Optional<org.onap.aai.domain.yang.GenericVnf> aaiGenericVnfOp = aaiGenericVnfWrapper
				.asBean(org.onap.aai.domain.yang.GenericVnf.class);
		if (!aaiGenericVnfOp.isPresent()) {
			return null;
		}

		GenericVnf genericVnf = this.mapperLayer.mapAAIGenericVnfIntoGenericVnf(aaiGenericVnfOp.get());

		Optional<Relationships> relationshipsOp = aaiGenericVnfWrapper.getRelationships();
		if (relationshipsOp.isPresent()) {
			Relationships relationships = relationshipsOp.get();
			this.mapPlatform(relationships.getByType(AAIObjectType.PLATFORM), genericVnf);
			this.mapLineOfBusiness(relationships.getByType(AAIObjectType.LINE_OF_BUSINESS), genericVnf);
			genericVnf.getVolumeGroups().addAll(mapVolumeGroups(relationships.getByType(AAIObjectType.VOLUME_GROUP)));
			genericVnf.getInstanceGroups().addAll(mapInstanceGroups(relationships.getByType(AAIObjectType.INSTANCE_GROUP)));
		}

		return genericVnf;
	}

	protected List<InstanceGroup> mapInstanceGroups(List<AAIResultWrapper> instanceGroups) {
		List<InstanceGroup> instanceGroupsList = new ArrayList<>();
		for (AAIResultWrapper volumeGroupWrapper : instanceGroups) {
			instanceGroupsList.add(this.mapInstanceGroup(volumeGroupWrapper));
		}
		return instanceGroupsList;
	}

	protected InstanceGroup mapInstanceGroup(AAIResultWrapper instanceGroupWrapper) {
		Optional<org.onap.aai.domain.yang.InstanceGroup> aaiInstanceGroupOp = instanceGroupWrapper
				.asBean(org.onap.aai.domain.yang.InstanceGroup.class);
		org.onap.aai.domain.yang.InstanceGroup aaiInstanceGroup = null;

		if (!aaiInstanceGroupOp.isPresent()) {
			return null;
		}

		aaiInstanceGroup = aaiInstanceGroupOp.get();
		InstanceGroup instanceGroup = this.mapperLayer.mapAAIInstanceGroupIntoInstanceGroup(aaiInstanceGroup);
		instanceGroup.setModelInfoInstanceGroup(this.mapperLayer.mapCatalogInstanceGroupToInstanceGroup(null,
				this.bbInputSetupUtils.getCatalogInstanceGroup(aaiInstanceGroup.getModelVersionId())));
		return instanceGroup;
	}

	protected List<VolumeGroup> mapVolumeGroups(List<AAIResultWrapper> volumeGroups) {
		List<VolumeGroup> volumeGroupsList = new ArrayList<>();
		for (AAIResultWrapper volumeGroupWrapper : volumeGroups) {
			volumeGroupsList.add(this.mapVolumeGroup(volumeGroupWrapper));
		}
		return volumeGroupsList;
	}

	protected VolumeGroup mapVolumeGroup(AAIResultWrapper volumeGroupWrapper) {
		Optional<org.onap.aai.domain.yang.VolumeGroup> aaiVolumeGroupOp = volumeGroupWrapper
				.asBean(org.onap.aai.domain.yang.VolumeGroup.class);
		org.onap.aai.domain.yang.VolumeGroup aaiVolumeGroup = null;

		if (!aaiVolumeGroupOp.isPresent()) {
			return null;
		}

		aaiVolumeGroup = aaiVolumeGroupOp.get();
		return this.mapperLayer.mapAAIVolumeGroup(aaiVolumeGroup);
	}

	protected void mapLineOfBusiness(List<AAIResultWrapper> lineOfBusinesses, GenericVnf genericVnf) {
		if (!lineOfBusinesses.isEmpty()) {
			AAIResultWrapper lineOfBusinessWrapper = lineOfBusinesses.get(0);
			Optional<org.onap.aai.domain.yang.LineOfBusiness> aaiLineOfBusinessOp = lineOfBusinessWrapper
					.asBean(org.onap.aai.domain.yang.LineOfBusiness.class);
			org.onap.aai.domain.yang.LineOfBusiness aaiLineOfBusiness = null;
			if (!aaiLineOfBusinessOp.isPresent()) {
				return;
			}
			aaiLineOfBusiness = aaiLineOfBusinessOp.get();

			LineOfBusiness lineOfBusiness = this.mapperLayer.mapAAILineOfBusiness(aaiLineOfBusiness);
			genericVnf.setLineOfBusiness(lineOfBusiness);
		}
	}

	protected void mapPlatform(List<AAIResultWrapper> platforms, GenericVnf genericVnf) {
		if (!platforms.isEmpty()) {
			AAIResultWrapper platformWrapper = platforms.get(0);
			Optional<org.onap.aai.domain.yang.Platform> aaiPlatformOp = platformWrapper
					.asBean(org.onap.aai.domain.yang.Platform.class);
			org.onap.aai.domain.yang.Platform aaiPlatform = null;
			if (!aaiPlatformOp.isPresent()) {
				return;
			}
			aaiPlatform = aaiPlatformOp.get();

			Platform platform = this.mapperLayer.mapAAIPlatform(aaiPlatform);
			genericVnf.setPlatform(platform);
		}
	}

	protected void mapCollection(List<AAIResultWrapper> collections, ServiceInstance serviceInstance) {
		if (!collections.isEmpty()) {
			AAIResultWrapper collectionWrapper = collections.get(0);
			Optional<org.onap.aai.domain.yang.Collection> aaiCollectionOp = collectionWrapper
					.asBean(org.onap.aai.domain.yang.Collection.class);
			org.onap.aai.domain.yang.Collection aaiCollection = null;
			if (!aaiCollectionOp.isPresent()) {
				return;
			}
			aaiCollection = aaiCollectionOp.get();

			Collection collection = this.mapperLayer.mapAAICollectionIntoCollection(aaiCollection);
			NetworkCollectionResourceCustomization collectionResourceCust = 
					bbInputSetupUtils.getCatalogNetworkCollectionResourceCustByID(aaiCollection.getCollectionCustomizationId());
			collection.setModelInfoCollection(mapperLayer.mapCatalogCollectionToCollection(collectionResourceCust, collectionResourceCust.getCollectionResource()));
			Optional<Relationships> relationshipsOp = collectionWrapper.getRelationships();
			Relationships relationships = null;
			if (relationshipsOp.isPresent()) {
				relationships = relationshipsOp.get();
			} else {
				serviceInstance.setCollection(collection);
				return;
			}
			List<InstanceGroup> instanceGroupsList = mapInstanceGroups(relationships.getByType(AAIObjectType.INSTANCE_GROUP));
			if(!instanceGroupsList.isEmpty()) {
				collection.setInstanceGroup(instanceGroupsList.get(0));
			}
			serviceInstance.setCollection(collection);
		}
	}

	protected void mapL3Networks(List<AAIResourceUri> list, List<L3Network> l3Networks) {
		for (AAIResourceUri aaiResourceUri : list) {
			l3Networks.add(this.mapL3Network(aaiResourceUri));
		}
	}

	protected L3Network mapL3Network(AAIResourceUri aaiResourceUri) {
		AAIResultWrapper aaiNetworkWrapper = this.bbInputSetupUtils.getAAIResourceDepthTwo(aaiResourceUri);
		Optional<org.onap.aai.domain.yang.L3Network> aaiL3NetworkOp = aaiNetworkWrapper
				.asBean(org.onap.aai.domain.yang.L3Network.class);
		org.onap.aai.domain.yang.L3Network aaiL3Network = null;

		if (!aaiL3NetworkOp.isPresent()) {
			return null;
		}

		aaiL3Network = aaiL3NetworkOp.get();
		L3Network network = this.mapperLayer.mapAAIL3Network(aaiL3Network);

		Optional<Relationships> relationshipsOp = aaiNetworkWrapper.getRelationships();
		if (relationshipsOp.isPresent()) {
			Relationships relationships = relationshipsOp.get();
			this.mapNetworkPolicies(relationships.getByType(AAIObjectType.NETWORK_POLICY),
					network.getNetworkPolicies());
			mapRouteTableReferences(relationships.getByType(AAIObjectType.ROUTE_TABLE_REFERENCE),
					network.getContrailNetworkRouteTableReferences());
		}

		return network;
	}

	protected void mapNetworkPolicies(List<AAIResultWrapper> aaiNetworkPolicies, List<NetworkPolicy> networkPolicies) {
		for (AAIResultWrapper networkPolicyWrapper : aaiNetworkPolicies) {
			networkPolicies.add(this.mapNetworkPolicy(networkPolicyWrapper));
		}
	}

	protected NetworkPolicy mapNetworkPolicy(AAIResultWrapper networkPolicyWrapper) {
		Optional<org.onap.aai.domain.yang.NetworkPolicy> aaiNetworkPolicyOp = networkPolicyWrapper
				.asBean(org.onap.aai.domain.yang.NetworkPolicy.class);
		org.onap.aai.domain.yang.NetworkPolicy aaiNetworkPolicy = null;

		if (!aaiNetworkPolicyOp.isPresent()) {
			return null;
		}

		aaiNetworkPolicy = aaiNetworkPolicyOp.get();
		return this.mapperLayer.mapAAINetworkPolicy(aaiNetworkPolicy);
	}

	protected void mapRouteTableReferences(List<AAIResultWrapper> routeTableReferences,
			List<RouteTableReference> contrailNetworkRouteTableReferences) {
		for (AAIResultWrapper routeTableReferenceWrapper : routeTableReferences) {
			contrailNetworkRouteTableReferences.add(this.mapRouteTableReference(routeTableReferenceWrapper));
		}
	}

	protected RouteTableReference mapRouteTableReference(AAIResultWrapper routeTableReferenceWrapper) {
		Optional<org.onap.aai.domain.yang.RouteTableReference> aaiRouteTableReferenceOp = routeTableReferenceWrapper
				.asBean(org.onap.aai.domain.yang.RouteTableReference.class);
		org.onap.aai.domain.yang.RouteTableReference aaiRouteTableReference = null;

		if (!aaiRouteTableReferenceOp.isPresent()) {
			return null;
		}

		aaiRouteTableReference = aaiRouteTableReferenceOp.get();
		return this.mapperLayer.mapAAIRouteTableReferenceIntoRouteTableReference(aaiRouteTableReference);
	}

	protected void mapOwningEntity(List<AAIResultWrapper> owningEntities, ServiceInstance serviceInstance) {
		if (!owningEntities.isEmpty()) {
			AAIResultWrapper owningEntityWrapper = owningEntities.get(0);
			Optional<org.onap.aai.domain.yang.OwningEntity> aaiOwningEntityOp = owningEntityWrapper
					.asBean(org.onap.aai.domain.yang.OwningEntity.class);
			org.onap.aai.domain.yang.OwningEntity aaiOwningEntity = null;
			if (!aaiOwningEntityOp.isPresent()) {
				return;
			}
			aaiOwningEntity = aaiOwningEntityOp.get();

			OwningEntity owningEntity = this.mapperLayer.mapAAIOwningEntity(aaiOwningEntity);
			serviceInstance.setOwningEntity(owningEntity);
		}
	}

	protected void mapProject(List<AAIResultWrapper> projects, ServiceInstance serviceInstance) {
		if (!projects.isEmpty()) {
			AAIResultWrapper projectWrapper = projects.get(0);
			Optional<org.onap.aai.domain.yang.Project> aaiProjectOp = projectWrapper
					.asBean(org.onap.aai.domain.yang.Project.class);
			org.onap.aai.domain.yang.Project aaiProject = null;
			if (!aaiProjectOp.isPresent()) {
				return;
			}
			aaiProject = aaiProjectOp.get();

			Project project = this.mapperLayer.mapAAIProject(aaiProject);
			serviceInstance.setProject(project);
		}
	}
	protected Customer mapCustomer(String globalCustomerId, String subscriptionServiceType) {
		org.onap.aai.domain.yang.Customer aaiCustomer = this.bbInputSetupUtils.getAAICustomer(globalCustomerId);
		org.onap.aai.domain.yang.ServiceSubscription aaiServiceSubscription = this.bbInputSetupUtils
				.getAAIServiceSubscription(globalCustomerId, subscriptionServiceType);
		Customer customer = this.mapperLayer.mapAAICustomer(aaiCustomer);
		ServiceSubscription serviceSubscription = this.mapperLayer.mapAAIServiceSubscription(aaiServiceSubscription);
		if(serviceSubscription != null){
			customer.setServiceSubscription(serviceSubscription);
		}
		return customer;
	}


}
