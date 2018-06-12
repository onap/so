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

package org.openecomp.mso.bpmn.servicedecomposition.tasks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.openecomp.mso.bpmn.common.BuildingBlockExecution;
import org.openecomp.mso.bpmn.common.DelegateExecutionImpl;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Collection;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Customer;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.GenericVnf;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.InstanceGroup;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.L3Network;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.LineOfBusiness;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.NetworkPolicy;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.OwningEntity;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Platform;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.Project;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.RouteTableReference;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.ServiceSubscription;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VfModule;
import org.openecomp.mso.bpmn.servicedecomposition.bbobjects.VolumeGroup;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.openecomp.mso.bpmn.servicedecomposition.entities.GeneralBuildingBlock;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ResourceKey;
import org.openecomp.mso.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.OrchestrationContext;
import org.openecomp.mso.bpmn.servicedecomposition.sdncbbobjects.RequestContext;
import org.openecomp.mso.client.aai.AAICommonObjectMapperProvider;
import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.entities.AAIResultWrapper;
import org.openecomp.mso.client.aai.entities.Relationships;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.aai.entities.uri.AAIUriFactory;
import org.openecomp.mso.client.exception.ExceptionBuilder;
import org.openecomp.mso.db.catalog.beans.CollectionResource;
import org.openecomp.mso.db.catalog.beans.CollectionResourceCustomization;
import org.openecomp.mso.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.openecomp.mso.db.catalog.beans.NetworkResourceCustomization;
import org.openecomp.mso.db.catalog.beans.OrchestrationStatus;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;
import org.openecomp.mso.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.serviceinstancebeans.ModelInfo;
import org.openecomp.mso.serviceinstancebeans.ModelType;
import org.openecomp.mso.serviceinstancebeans.Networks;
import org.openecomp.mso.serviceinstancebeans.RelatedInstance;
import org.openecomp.mso.serviceinstancebeans.RelatedInstanceList;
import org.openecomp.mso.serviceinstancebeans.RequestDetails;
import org.openecomp.mso.serviceinstancebeans.RequestParameters;
import org.openecomp.mso.serviceinstancebeans.Resources;
import org.openecomp.mso.serviceinstancebeans.VfModules;
import org.openecomp.mso.serviceinstancebeans.Vnfs;
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
	private static final String CLOUD_OWNER = "att-aic";

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
			boolean aLaCarte = executeBB.isaLaCarte();
			boolean homing = executeBB.isHoming();
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
			BuildingBlockExecution gBuildingBlockExecution = new DelegateExecutionImpl(execution);
			exceptionUtil.buildAndThrowWorkflowException(gBuildingBlockExecution, 7000, e);
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
		RequestDetails requestDetails = bbInputSetupUtils.getRequestDetails(requestId);
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

	protected void populateLookupKeyMapWithIds(WorkflowResourceIds workflowResourceIds,
			Map<ResourceKey, String> lookupKeyMap) {
		lookupKeyMap.put(ResourceKey.SERVICE_INSTANCE_ID, workflowResourceIds.getServiceInstanceId());
		lookupKeyMap.put(ResourceKey.NETWORK_ID, workflowResourceIds.getNetworkId());
		lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, workflowResourceIds.getVnfId());
		lookupKeyMap.put(ResourceKey.VF_MODULE_ID, workflowResourceIds.getVfModuleId());
		lookupKeyMap.put(ResourceKey.VOLUME_GROUP_ID, workflowResourceIds.getVolumeGroupId());
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

	protected void populateObjectsOnAssignAndCreateFlows(RequestDetails requestDetails, Service service, String bbName,
			ServiceInstance serviceInstance, Map<ResourceKey, String> lookupKeyMap, String resourceId, String vnfType)
			throws Exception {
		ModelInfo modelInfo = requestDetails.getModelInfo();
		String instanceName = requestDetails.getRequestInfo().getInstanceName();
		ModelType modelType = modelInfo.getModelType();
		org.openecomp.mso.serviceinstancebeans.Platform platform = requestDetails.getPlatform();
		org.openecomp.mso.serviceinstancebeans.LineOfBusiness lineOfBusiness = requestDetails.getLineOfBusiness();

		if (modelType.equals(ModelType.network)) {
			this.populateL3Network(instanceName, modelInfo, service, bbName, serviceInstance, lookupKeyMap, resourceId);
		} else if (modelType.equals(ModelType.vnf)) {
			this.populateGenericVnf(modelInfo, instanceName, platform, lineOfBusiness, service, bbName, serviceInstance,
					lookupKeyMap, requestDetails, resourceId, vnfType);
		} else if (modelType.equals(ModelType.volumeGroup)) {
			this.populateVolumeGroup(requestDetails, service, bbName, serviceInstance, lookupKeyMap, resourceId);
		} else if (modelType.equals(ModelType.vfModule)) {
			this.populateVfModule(requestDetails, service, bbName, serviceInstance, lookupKeyMap, resourceId);
		} else {
			return;
		}
	}

	protected void populateVfModule(RequestDetails requestDetails, Service service, String bbName,
			ServiceInstance serviceInstance, Map<ResourceKey, String> lookupKeyMap, String resourceId)
			throws Exception {
		boolean foundByName = false;
		boolean foundById = false;
		String vnfModelCustomizationUUID = null;
		if (requestDetails.getRelatedInstanceList() != null) {
			for (RelatedInstanceList relatedInstanceList : requestDetails.getRelatedInstanceList()) {
				RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();
				if (relatedInstance.getModelInfo().getModelType().equals(ModelType.vnf)) {
					vnfModelCustomizationUUID = relatedInstance.getModelInfo().getModelCustomizationUuid();
				}
			}
		}
		GenericVnf vnf = null;
		for (GenericVnf tempVnf : serviceInstance.getVnfs()) {
			if (tempVnf.getVnfId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID))) {
				vnf = tempVnf;
				vnfModelCustomizationUUID = this.bbInputSetupUtils.getAAIGenericVnf(vnf.getVnfId())
						.getModelCustomizationId();
				ModelInfo modelInfo = new ModelInfo();
				modelInfo.setModelCustomizationUuid(vnfModelCustomizationUUID);
				this.mapCatalogVnf(tempVnf, modelInfo, service);
				break;
			}
		}
		if (vnf != null) {
			for (VfModule vfModule : vnf.getVfModules()) {
				if (lookupKeyMap.get(ResourceKey.VF_MODULE_ID) != null
						&& vfModule.getVfModuleId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.VF_MODULE_ID))) {
					foundById = true;
					this.mapCatalogVfModule(vfModule, requestDetails.getModelInfo(), service,
							vnfModelCustomizationUUID);
				} else if (requestDetails.getRequestInfo().getInstanceName() != null && vfModule.getVfModuleName()
						.equalsIgnoreCase(requestDetails.getRequestInfo().getInstanceName())) {
					foundByName = true;
					lookupKeyMap.put(ResourceKey.VF_MODULE_ID, vfModule.getVfModuleId());
					this.mapCatalogVfModule(vfModule, requestDetails.getModelInfo(), service,
							vnfModelCustomizationUUID);
				}
			}
			if (!foundByName && !foundById && bbName.equalsIgnoreCase(AssignFlows.VF_MODULE.toString())) {
				VfModule vfModule = this.createVfModule(lookupKeyMap, service, requestDetails,
						vnfModelCustomizationUUID, resourceId);
				this.mapCatalogVfModule(vfModule, requestDetails.getModelInfo(), service, vnfModelCustomizationUUID);
				vnf.getVfModules().add(vfModule);
			}
		} else {
			msoLogger.debug("Related VNF instance Id not found:  " + lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID));
			throw new Exception("Could not find relevant information for related VNF");
		}
	}

	protected void mapCatalogVfModule(VfModule vfModule, ModelInfo modelInfo, Service service,
			String vnfModelCustomizationUUID) {
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
					.filter(x -> modelInfo.getModelCustomizationUuid().equalsIgnoreCase(x.getModelCustomizationUUID()))// find
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

	protected VfModule createVfModule(Map<ResourceKey, String> lookupKeyMap, Service service,
			RequestDetails requestDetails, String vnfModelCustomizationUUID, String vfModuleId) {
		lookupKeyMap.put(ResourceKey.VF_MODULE_ID, vfModuleId);
		VfModule vfModule = new VfModule();
		vfModule.setVfModuleId(vfModuleId);
		vfModule.setVfModuleName(requestDetails.getRequestInfo().getInstanceName());
		vfModule.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		this.mapCatalogVfModule(vfModule, requestDetails.getModelInfo(), service, vnfModelCustomizationUUID);
		return vfModule;
	}

	protected void populateVolumeGroup(RequestDetails requestDetails, Service service, String bbName,
			ServiceInstance serviceInstance, Map<ResourceKey, String> lookupKeyMap, String resourceId)
			throws Exception {
		boolean foundByName = false;
		boolean foundById = false;
		String vnfModelCustomizationUUID = null;
		if (requestDetails.getRelatedInstanceList() != null) {
			for (RelatedInstanceList relatedInstanceList : requestDetails.getRelatedInstanceList()) {
				RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();
				if (relatedInstance.getModelInfo().getModelType().equals(ModelType.vnf)) {
					vnfModelCustomizationUUID = relatedInstance.getModelInfo().getModelCustomizationUuid();
				}
			}
		}
		GenericVnf vnf = null;
		for (GenericVnf tempVnf : serviceInstance.getVnfs()) {
			if (tempVnf.getVnfId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID))) {
				vnf = tempVnf;
				vnfModelCustomizationUUID = this.bbInputSetupUtils.getAAIGenericVnf(vnf.getVnfId())
						.getModelCustomizationId();
				ModelInfo modelInfo = new ModelInfo();
				modelInfo.setModelCustomizationUuid(vnfModelCustomizationUUID);
				this.mapCatalogVnf(tempVnf, modelInfo, service);
				break;
			}
		}
		if (vnf != null && vnfModelCustomizationUUID != null) {
			for (VolumeGroup volumeGroup : vnf.getVolumeGroups()) {
				if (lookupKeyMap.get(ResourceKey.VOLUME_GROUP_ID) != null && volumeGroup.getVolumeGroupId()
						.equalsIgnoreCase(lookupKeyMap.get(ResourceKey.VOLUME_GROUP_ID))) {
					foundById = true;
					this.mapCatalogVolumeGroup(volumeGroup, requestDetails, service, vnfModelCustomizationUUID);
				} else if (requestDetails.getRequestInfo().getInstanceName() != null && volumeGroup.getVolumeGroupName()
						.equalsIgnoreCase(requestDetails.getRequestInfo().getInstanceName())) {
					foundByName = true;
					lookupKeyMap.put(ResourceKey.VOLUME_GROUP_ID, volumeGroup.getVolumeGroupId());
					this.mapCatalogVolumeGroup(volumeGroup, requestDetails, service, vnfModelCustomizationUUID);
				}
			}
			if (!foundByName && !foundById && bbName.equalsIgnoreCase(AssignFlows.VOLUME_GROUP.toString())) {
				vnf.getVolumeGroups().add(this.createVolumeGroup(lookupKeyMap, service, requestDetails,
						vnfModelCustomizationUUID, resourceId));
			}
		} else {
			msoLogger.debug("Related VNF instance Id not found:  " + lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID));
			throw new Exception("Could not find relevant information for related VNF");
		}
	}

	protected VolumeGroup createVolumeGroup(Map<ResourceKey, String> lookupKeyMap, Service service,
			RequestDetails requestDetails, String vnfModelCustomizationUUID, String volumeGroupId) {
		lookupKeyMap.put(ResourceKey.VOLUME_GROUP_ID, volumeGroupId);
		VolumeGroup volumeGroup = new VolumeGroup();
		volumeGroup.setVolumeGroupId(volumeGroupId);
		volumeGroup.setVolumeGroupName(requestDetails.getRequestInfo().getInstanceName());
		volumeGroup.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		this.mapCatalogVolumeGroup(volumeGroup, requestDetails, service, vnfModelCustomizationUUID);
		return volumeGroup;
	}

	protected void mapCatalogVolumeGroup(VolumeGroup volumeGroup, RequestDetails requestDetails, Service service,
			String vnfModelCustomizationUUID) {
		VfModuleCustomization vfResourceCustomization = getVfResourceCustomization(requestDetails, service,
				vnfModelCustomizationUUID);
		if (vfResourceCustomization != null) {
			volumeGroup.setModelInfoVfModule(this.mapperLayer.mapCatalogVfModuleToVfModule(vfResourceCustomization));
		}
	}

	protected VfModuleCustomization getVfResourceCustomization(RequestDetails requestDetails, Service service,
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
						.equalsIgnoreCase(requestDetails.getModelInfo().getModelCustomizationUuid())) {
					return vfResourceCust;
				}
			}

		}
		return null;
	}

	protected void populateGenericVnf(ModelInfo modelInfo, String instanceName,
			org.openecomp.mso.serviceinstancebeans.Platform platform,
			org.openecomp.mso.serviceinstancebeans.LineOfBusiness lineOfBusiness, Service service, String bbName,
			ServiceInstance serviceInstance, Map<ResourceKey, String> lookupKeyMap, RequestDetails requestDetails,
			String resourceId, String vnfType) {
		boolean foundByName = false;
		boolean foundById = false;
		ModelInfo instanceGroupModelInfo = null;
		String instanceGroupId = null;
		if (requestDetails.getRelatedInstanceList() != null) {
			for (RelatedInstanceList relatedInstanceList : requestDetails.getRelatedInstanceList()) {
				RelatedInstance relatedInstance = relatedInstanceList.getRelatedInstance();
				if (relatedInstance.getModelInfo().getModelType().equals(ModelType.networkCollection)) {
					instanceGroupModelInfo = relatedInstance.getModelInfo();
					instanceGroupId = relatedInstance.getInstanceId();
				}
			}
		}
		for (GenericVnf genericVnf : serviceInstance.getVnfs()) {
			if (lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID) != null
					&& genericVnf.getVnfId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.GENERIC_VNF_ID))) {
				foundById = true;
				org.onap.aai.domain.yang.GenericVnf vnf = bbInputSetupUtils.getAAIGenericVnf(genericVnf.getVnfId());
				if(vnf!=null){
					modelInfo.setModelCustomizationUuid(vnf.getModelCustomizationId());
				}
				this.mapCatalogVnf(genericVnf, modelInfo, service);
			} else if (instanceName != null && genericVnf.getVnfName().equalsIgnoreCase(instanceName)) {
				foundByName = true;
				lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, genericVnf.getVnfId());
				org.onap.aai.domain.yang.GenericVnf vnf = bbInputSetupUtils.getAAIGenericVnf(genericVnf.getVnfId());
				if(vnf!=null){
					modelInfo.setModelCustomizationUuid(vnf.getModelCustomizationId());
				}
				this.mapCatalogVnf(genericVnf, modelInfo, service);
			}
		}
		if (!foundByName && !foundById && bbName.equalsIgnoreCase(AssignFlows.VNF.toString())) {
			if(vnfType == null || vnfType.isEmpty()) {
				vnfType = service.getModelName() + "/" + modelInfo.getModelCustomizationName();
			}
			GenericVnf genericVnf = this.createGenericVnf(lookupKeyMap, instanceName, platform, lineOfBusiness,
					resourceId, vnfType);
			serviceInstance.getVnfs().add(genericVnf);
			this.mapCatalogVnf(genericVnf, modelInfo, service);
			this.mapVnfcCollectionInstanceGroup(genericVnf, modelInfo, service);
			if (instanceGroupId != null && instanceGroupModelInfo != null)
				this.mapNetworkCollectionInstanceGroup(genericVnf, instanceGroupId);
		}
	}

	protected void mapVnfcCollectionInstanceGroup(GenericVnf genericVnf, ModelInfo modelInfo, Service service) {
		VnfResourceCustomization vnfResourceCustomization = getVnfResourceCustomizationFromService(modelInfo, service);
		List<VnfcInstanceGroupCustomization> vnfcInstanceGroups = vnfResourceCustomization
				.getVnfcInstanceGroupCustomizations();
		for (VnfcInstanceGroupCustomization vnfcInstanceGroupCust : vnfcInstanceGroups) {
			InstanceGroup instanceGroup = this.createInstanceGroup();
			instanceGroup.setModelInfoInstanceGroup(
					this.mapperLayer.mapCatalogInstanceGroupToInstanceGroup(vnfcInstanceGroupCust.getInstanceGroup()));
			instanceGroup.getModelInfoInstanceGroup().setFunction(vnfcInstanceGroupCust.getFunction());
			instanceGroup.setDescription(vnfcInstanceGroupCust.getDescription());
			genericVnf.getInstanceGroups().add(instanceGroup);
		}
	}

	protected void mapNetworkCollectionInstanceGroup(GenericVnf genericVnf, String instanceGroupId) {
		org.onap.aai.domain.yang.InstanceGroup aaiInstanceGroup = this.bbInputSetupUtils
				.getAAIInstanceGroup(instanceGroupId);
		InstanceGroup instanceGroup = this.mapperLayer.mapAAIInstanceGroupIntoInstanceGroup(aaiInstanceGroup);
		instanceGroup.setModelInfoInstanceGroup(this.mapperLayer.mapCatalogInstanceGroupToInstanceGroup(
				this.bbInputSetupUtils.getCatalogInstanceGroup(aaiInstanceGroup.getModelVersionId())));
		genericVnf.getInstanceGroups().add(instanceGroup);
	}

	protected GenericVnf createGenericVnf(Map<ResourceKey, String> lookupKeyMap, String instanceName,
			org.openecomp.mso.serviceinstancebeans.Platform platform,
			org.openecomp.mso.serviceinstancebeans.LineOfBusiness lineOfBusiness, String vnfId, String vnfType) {
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
			ServiceInstance serviceInstance, Map<ResourceKey, String> lookupKeyMap, String resourceId) {
		boolean foundByName = false;
		boolean foundById = false;
		for (L3Network network : serviceInstance.getNetworks()) {
			if (lookupKeyMap.get(ResourceKey.NETWORK_ID) != null
					&& network.getNetworkId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.NETWORK_ID))) {
				foundById = true;
				this.mapCatalogNetwork(network, modelInfo, service);
			} else if (instanceName != null && network.getNetworkName().equalsIgnoreCase(instanceName)) {
				foundByName = true;
				lookupKeyMap.put(ResourceKey.NETWORK_ID, network.getNetworkId());
				this.mapCatalogNetwork(network, modelInfo, service);
			}
		}
		if (!foundByName && !foundById && bbName.equalsIgnoreCase(AssignFlows.NETWORK.toString())) {
			L3Network l3Network = this.createNetwork(lookupKeyMap, service, instanceName, resourceId);
			serviceInstance.getNetworks().add(l3Network);
			this.mapCatalogNetwork(l3Network, modelInfo, service);
		}
	}

	protected L3Network createNetwork(Map<ResourceKey, String> lookupKeyMap, Service service, String instanceName,
			String networkId) {
		lookupKeyMap.put(ResourceKey.NETWORK_ID, networkId);
		L3Network network = new L3Network();
		network.setNetworkId(networkId);
		network.setNetworkName(instanceName);
		network.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
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
					project, owningEntity, lookupKeyMap, resourceId, executeBB.isaLaCarte(),
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
		org.onap.aai.domain.yang.CloudRegion aaiCloudRegion = bbInputSetupUtils.getCloudRegion(requestDetails,
				CLOUD_OWNER);
		CloudRegion cloudRegion = mapperLayer.mapCloudRegion(requestDetails, aaiCloudRegion, CLOUD_OWNER);
		outputBB.setOrchContext(orchContext);
		outputBB.setRequestContext(requestContext);
		outputBB.setCloudRegion(cloudRegion);
		if(customer == null){
			Map<String, String> uriKeys = bbInputSetupUtils.getURIKeysFromServiceInstance(serviceInstance.getServiceInstanceId());
			String globalCustomerId = uriKeys.get("global-customer-id");
			String subscriptionServiceType = uriKeys.get("service-type");
			customer = mapCustomer(globalCustomerId, subscriptionServiceType);
		}
		customer.getServiceSubscription().getServiceInstances().add(serviceInstance);
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
		serviceInstance.setModelInfoServiceInstance(mapperLayer.mapCatalogServiceIntoServiceInstance(service));
		return serviceInstance;
	}

	protected GeneralBuildingBlock getGBBMacro(ExecuteBuildingBlock executeBB, RequestDetails requestDetails,
			Map<ResourceKey, String> lookupKeyMap, String requestAction, String resourceId, String vnfType)
			throws Exception {
		GeneralBuildingBlock gBB = this.getGBBALaCarteService(executeBB, requestDetails, lookupKeyMap, requestAction,
				resourceId);
		RequestParameters requestParams = requestDetails.getRequestParameters();
		Service service = bbInputSetupUtils
				.getCatalogServiceByModelUUID(requestDetails.getModelInfo().getModelVersionId());
		if (service == null) {
			service = bbInputSetupUtils.getCatalogServiceByModelVersionAndModelInvariantUUID(
					requestDetails.getModelInfo().getModelVersion(),
					requestDetails.getModelInfo().getModelInvariantId());
			if (service == null) {
				throw new Exception("Could not find service for model version Id: "
						+ requestDetails.getModelInfo().getModelVersionId() + " and for model invariant Id: "
						+ requestDetails.getModelInfo().getModelInvariantId());
			}
		}
		if (requestParams.getUserParams() == null) {
			throw new Exception();
		}

		ServiceInstance serviceInstance = gBB.getCustomer().getServiceSubscription().getServiceInstances().get(0);
		String bbName = executeBB.getBuildingBlock().getBpmnFlowName();
		int sequenceNumber = executeBB.getBuildingBlock().getSequenceNumber();
		String input = mapper.writeValueAsString(requestParams.getUserParams().get(0).get("service"));
		org.openecomp.mso.serviceinstancebeans.Service serviceMacro = mapper.readValue(input,
				org.openecomp.mso.serviceinstancebeans.Service.class);

		Resources resources = (Resources) serviceMacro.getResources();
		Vnfs vnfs = null;
		VfModules vfModules = null;
		Networks networks = null;

		if (bbName.contains("Vnf")) {
			vnfs = resources.getVnfs().get(sequenceNumber);
			if(bbName.equalsIgnoreCase(AssignFlows.VNF.toString())) {
				resourceId = this.generateRandomUUID();
			}
			this.populateGenericVnf(vnfs.getModelInfo(), vnfs.getInstanceName(), vnfs.getPlatform(),
					vnfs.getLineOfBusiness(), service, bbName, serviceInstance, lookupKeyMap, requestDetails,
					resourceId, vnfType);
		} else if (bbName.contains("VfModule")) {
			vfModules = getVfModulesFromRequest(sequenceNumber, resources);
			vnfs = getVnfRelatedToVfModuleFromRequest(resources, vfModules);
			if (vnfs == null) {
				throw new Exception("Could not find Vnf to orchestrate VfModule");
			}
			if(bbName.equalsIgnoreCase(AssignFlows.VF_MODULE.toString())) {
				resourceId = this.generateRandomUUID();
			}
			this.populateMacroVfModule(vfModules, vfModules.getInstanceName(), vfModules.getVolumeGroupInstanceName(),
					service, bbName, serviceInstance, lookupKeyMap, vnfs, resourceId);
		} else if (bbName.contains("Network")) {
			networks = resources.getNetworks().get(executeBB.getBuildingBlock().getSequenceNumber());
			if(bbName.equalsIgnoreCase(AssignFlows.NETWORK.toString())) {
				resourceId = this.generateRandomUUID();
			}
			this.populateL3Network(networks.getInstanceName(), networks.getModelInfo(), service, bbName,
					serviceInstance, lookupKeyMap, resourceId);
		}

		this.populateNetworkCollectionAndInstanceGroupAssign(service, bbName, serviceInstance, resourceId);
		return gBB;
	}

	protected String generateRandomUUID() {
		return UUID.randomUUID().toString();
	}

	protected void populateMacroVfModule(VfModules vfModules, String instanceName, String volumeGroupInstanceName,
			Service service, String bbName, ServiceInstance serviceInstance, Map<ResourceKey, String> lookupKeyMap,
			Vnfs vnfs, String resourceId) {
		boolean foundByName = false;
		boolean foundById = false;
		String vnfModelCustomizationUUID = vnfs.getModelInfo().getModelCustomizationId();
		for (GenericVnf vnf : serviceInstance.getVnfs()) {
			for (VfModule vfModule : vnf.getVfModules()) {
				if (instanceName != null && vfModule.getVfModuleName().equalsIgnoreCase(instanceName)) {
					foundByName = !foundByName;
					lookupKeyMap.put(ResourceKey.VF_MODULE_ID, vfModule.getVfModuleId());
					this.mapCatalogVfModule(vfModule, vfModules.getModelInfo(), service, vnfModelCustomizationUUID);
				}
				if (lookupKeyMap.get(ResourceKey.VF_MODULE_ID) != null
						&& vfModule.getVfModuleId().equalsIgnoreCase(lookupKeyMap.get(ResourceKey.VF_MODULE_ID))) {
					foundById = !foundById;
					this.mapCatalogVfModule(vfModule, vfModules.getModelInfo(), service, vnfModelCustomizationUUID);
				}
			}
		}

		if (!foundByName && !foundById && bbName.equalsIgnoreCase(AssignFlows.VF_MODULE.toString())) {
			for (GenericVnf vnf : serviceInstance.getVnfs()) {
				if (vnf.getVnfName().equalsIgnoreCase(vnfs.getInstanceName())) {
					lookupKeyMap.put(ResourceKey.GENERIC_VNF_ID, vnf.getVnfId());
					VfModule vfModule = this.createMacroVfModule(lookupKeyMap, instanceName,
							vfModules.getInstanceParams(), resourceId);
					vnf.getVfModules().add(vfModule);
					this.mapCatalogVfModule(vfModule, vfModules.getModelInfo(), service, vnfModelCustomizationUUID);
					break;
				}
			}
		}
	}

	protected VfModule createMacroVfModule(Map<ResourceKey, String> lookupKeyMap, String instanceName,
			List<Map<String, String>> instanceParams, String vfModuleId) {
		lookupKeyMap.put(ResourceKey.VF_MODULE_ID, vfModuleId);
		VfModule vfModule = new VfModule();
		vfModule.setVfModuleId(vfModuleId);
		vfModule.setVfModuleName(instanceName);
		for (Map<String, String> params : instanceParams) {
			vfModule.getCloudParams().putAll(params);
		}
		vfModule.setOrchestrationStatus(OrchestrationStatus.PRECREATED);
		return vfModule;
	}

	protected VfModules getVfModulesFromRequest(int sequenceNumber, Resources resources) {
		VfModules vfModules;
		List<VfModules> vfModule = new LinkedList<>();
		for (Vnfs vnfTemp : resources.getVnfs()) {
			for (VfModules vfModuleTemp : vnfTemp.getVfModules()) {
				vfModule.add(vfModuleTemp);
			}
		}
		vfModules = vfModule.get(sequenceNumber);
		return vfModules;
	}

	protected Vnfs getVnfRelatedToVfModuleFromRequest(Resources resources, VfModules vfModules) {
		for (Vnfs vnfTemp : resources.getVnfs()) {
			if (vnfTemp.getVfModules().contains(vfModules)) {
				return vnfTemp;
			}
		}
		return null;
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
				if(!serviceInstanceAAI.getModelVersionId().equalsIgnoreCase(service.getModelUUID())) {
					throw new Exception("AAI Service Instance already exists with different model version id.");
				} else {
				return this.getExistingServiceInstance(serviceInstanceAAI);
				}
			} else {
		return createServiceInstance(requestDetails, project, owningEntity, lookupKeyMap, serviceInstanceId);
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

	protected ServiceInstance getExistingServiceInstance(org.onap.aai.domain.yang.ServiceInstance serviceInstanceAAI)
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
			ServiceInstance serviceInstance, String resourceId) throws Exception {
		if (serviceInstance.getCollection() == null
				&& bbName.equalsIgnoreCase(AssignFlows.NETWORK_COLLECTION.toString())) {
			String resourceUuid = this.generateRandomUUID();
			Collection collection = this.createCollection(resourceUuid);
			serviceInstance.setCollection(collection);
		}
		if (serviceInstance.getCollection() != null && serviceInstance.getCollection().getInstanceGroup() == null
				&& bbName.equalsIgnoreCase(AssignFlows.NETWORK_COLLECTION.toString())) {
			InstanceGroup instanceGroup = this.createInstanceGroup();
			serviceInstance.getCollection().setInstanceGroup(instanceGroup);
		}
		if (serviceInstance.getCollection() != null) {
			this.mapCatalogCollection(service, serviceInstance.getCollection());
			if (serviceInstance.getCollection().getInstanceGroup() != null) {
				this.mapCatalogNetworkCollectionInstanceGroup(service,
						serviceInstance.getCollection().getInstanceGroup());
			}
		}
	}

	protected void mapCatalogNetworkCollectionInstanceGroup(Service service, InstanceGroup instanceGroup) {
		CollectionResourceCustomization collectionCust = service.getCollectionResourceCustomization();
		boolean isVlanTagging = false;
		if (collectionCust != null) {
			CollectionResource collectionResource = collectionCust.getCollectionResource();
			if (collectionResource != null
					&& collectionCust.getCollectionResource().getToscaNodeType().equals("NetworkCollection")) {
				isVlanTagging = !isVlanTagging;
				org.openecomp.mso.db.catalog.beans.InstanceGroup catalogInstanceGroup = collectionResource
						.getInstanceGroup();
				if (catalogInstanceGroup != null) {
					instanceGroup.setModelInfoInstanceGroup(
							mapperLayer.mapCatalogInstanceGroupToInstanceGroup(catalogInstanceGroup));
				}
			}
		}
		if (isVlanTagging) {
			List<CollectionResourceInstanceGroupCustomization> instanceGroupCustList = this.bbInputSetupUtils
					.getCollectionResourceInstanceGroupCustomization(collectionCust.getModelCustomizationUUID());
			if (instanceGroupCustList.size() > 1) {
				msoLogger.debug("Too many instance group cust for Network collection: "
						+ collectionCust.getModelCustomizationUUID());
			} else if (instanceGroupCustList.isEmpty()) {
				msoLogger.debug(
						"No instance group cust for Network collection: " + collectionCust.getModelCustomizationUUID());
			} else {
				CollectionResourceInstanceGroupCustomization instanceGroupCust = instanceGroupCustList.get(0);
				instanceGroup.setDescription(instanceGroupCust.getDescription());
				instanceGroup.getModelInfoInstanceGroup().setFunction(instanceGroupCust.getFunction());
			}
		}
	}

	protected void mapCatalogCollection(Service service, Collection collection) {
		CollectionResourceCustomization collectionCust = service.getCollectionResourceCustomization();
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
		this.mapCollection(relationships.getByType(AAIObjectType.COLLECTION),
				relationships.getByType(AAIObjectType.INSTANCE_GROUP), serviceInstance);
	}


	protected void mapGenericVnfs(List<AAIResourceUri> list, List<GenericVnf> genericVnfs) {
		for (AAIResourceUri aaiResourceUri : list) {
			genericVnfs.add(this.mapGenericVnf(aaiResourceUri));
		}
	}

	protected GenericVnf mapGenericVnf(AAIResourceUri aaiResourceUri) {
		AAIResultWrapper aaiGenericVnfWrapper = this.bbInputSetupUtils.getAAIGenericVnf(aaiResourceUri);
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
			this.mapVolumeGroups(relationships.getByType(AAIObjectType.VOLUME_GROUP), genericVnf.getVolumeGroups());
			this.mapInstanceGroups(relationships.getByType(AAIObjectType.INSTANCE_GROUP),
					genericVnf.getInstanceGroups());
		}

		return genericVnf;
	}

	protected void mapInstanceGroups(List<AAIResultWrapper> instanceGroups, List<InstanceGroup> instanceGroupsList) {
		for (AAIResultWrapper volumeGroupWrapper : instanceGroups) {
			instanceGroupsList.add(this.mapInstanceGroup(volumeGroupWrapper));
		}
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
		instanceGroup.setModelInfoInstanceGroup(this.mapperLayer.mapCatalogInstanceGroupToInstanceGroup(
				this.bbInputSetupUtils.getCatalogInstanceGroup(aaiInstanceGroup.getModelVersionId())));
		return instanceGroup;
	}

	protected void mapVolumeGroups(List<AAIResultWrapper> volumeGroups, List<VolumeGroup> volumeGroupsList) {
		for (AAIResultWrapper volumeGroupWrapper : volumeGroups) {
			volumeGroupsList.add(this.mapVolumeGroup(volumeGroupWrapper));
		}
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

	protected void mapCollection(List<AAIResultWrapper> collections, List<AAIResultWrapper> instanceGroups,
			ServiceInstance serviceInstance) {
		if (!collections.isEmpty()) {
			AAIResultWrapper collectionWrapper = collections.get(0);
			Optional<org.onap.aai.domain.yang.Collection> aaiCollectionOp = collectionWrapper
					.asBean(org.onap.aai.domain.yang.Collection.class);
			org.onap.aai.domain.yang.Collection aaiCollection = null;
			if (!aaiCollectionOp.isPresent()) {
				return;
			}
			aaiCollection = aaiCollectionOp.get();

			AAIResultWrapper instanceGroupWrapper = instanceGroups.get(0);
			Optional<org.onap.aai.domain.yang.InstanceGroup> aaiInstanceGroupOp = instanceGroupWrapper
					.asBean(org.onap.aai.domain.yang.InstanceGroup.class);
			org.onap.aai.domain.yang.InstanceGroup aaiInstanceGroup = null;
			if (!aaiInstanceGroupOp.isPresent()) {
				return;
			}
			aaiInstanceGroup = aaiInstanceGroupOp.get();

			Collection collection = this.mapperLayer.mapAAICollectionIntoCollection(aaiCollection);
			InstanceGroup instanceGroup = this.mapperLayer.mapAAIInstanceGroupIntoInstanceGroup(aaiInstanceGroup);
			Optional<Relationships> relationshipsOp = instanceGroupWrapper.getRelationships();
			Relationships relationships = null;
			if (relationshipsOp.isPresent()) {
				relationships = relationshipsOp.get();
			} else {
				return;
			}
			this.mapL3Networks(relationships.getRelatedAAIUris(AAIObjectType.L3_NETWORK),
					instanceGroup.getL3Networks());

			collection.setInstanceGroup(instanceGroup);
			serviceInstance.setCollection(collection);
		}
	}

	protected void mapL3Networks(List<AAIResourceUri> list, List<L3Network> l3Networks) {
		for (AAIResourceUri aaiResourceUri : list) {
			l3Networks.add(this.mapL3Network(aaiResourceUri));
		}
	}

	protected L3Network mapL3Network(AAIResourceUri aaiResourceUri) {
		AAIResultWrapper aaiNetworkWrapper = this.bbInputSetupUtils.getAAIL3Network(aaiResourceUri);
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
			this.mapRouteTableReferences(relationships.getByType(AAIObjectType.ROUTE_TABLE_REFERENCE),
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
