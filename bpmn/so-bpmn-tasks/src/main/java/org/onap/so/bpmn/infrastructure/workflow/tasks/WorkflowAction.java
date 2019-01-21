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

package org.onap.so.bpmn.infrastructure.workflow.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.javatuples.Pair;
import org.slf4j.LoggerFactory;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ConfigurationResourceKeys;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetup;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.bpmn.infrastructure.workflow.tasks.Resource;
import org.onap.so.client.aai.AAICommonObjectMapperProvider;
import org.onap.so.client.aai.entities.AAIResultWrapper;
import org.onap.so.client.aai.entities.Relationships;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAIConfigurationResources;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfVfmoduleCvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.macro.NorthBoundRequest;
import org.onap.so.db.catalog.beans.macro.OrchestrationFlow;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.logger.MsoLogger;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.ModelType;
import org.onap.so.serviceinstancebeans.Networks;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.VfModules;
import org.onap.so.serviceinstancebeans.Vnfs;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WorkflowAction {

	private static final String WORKFLOW_ACTION_ERROR_MESSAGE = "WorkflowActionErrorMessage";
	private static final String SERVICE_INSTANCES = "serviceInstances";
	private static final String VF_MODULES = "vfModules";
	private static final String WORKFLOW_ACTION_WAS_UNABLE_TO_VERIFY_IF_THE_INSTANCE_NAME_ALREADY_EXIST_IN_AAI = "WorkflowAction was unable to verify if the instance name already exist in AAI.";
	private static final String G_ORCHESTRATION_FLOW = "gOrchestrationFlow";
	private static final String G_ACTION = "requestAction";
	private static final String G_CURRENT_SEQUENCE = "gCurrentSequence";
	private static final String G_REQUEST_ID = "mso-request-id";
	private static final String G_BPMN_REQUEST = "bpmnRequest";
	private static final String G_ALACARTE = "aLaCarte";
	private static final String G_APIVERSION = "apiVersion";
	private static final String G_URI = "requestUri";
	private static final String G_ISTOPLEVELFLOW = "isTopLevelFlow";
	private static final String VNF_TYPE = "vnfType";
	private static final String SERVICE = "Service";
	private static final String VNF = "Vnf";
	private static final String VFMODULE = "VfModule";
	private static final String VOLUMEGROUP = "VolumeGroup";
	private static final String NETWORK = "Network";
	private static final String NETWORKCOLLECTION = "NetworkCollection";
	private static final String CONFIGURATION = "Configuration";
	private static final String ASSIGNINSTANCE = "assignInstance";
	private static final String CREATEINSTANCE = "createInstance";
	private static final String USERPARAMSERVICE = "service";
	private static final String supportedTypes = "vnfs|vfModules|networks|networkCollections|volumeGroups|serviceInstances";
	private static final String HOMINGSOLUTION = "Homing_Solution";
	private static final String FABRIC_CONFIGURATION = "FabricConfiguration";
	private static final String G_SERVICE_TYPE = "serviceType";
	private static final String SERVICE_TYPE_TRANSPORT = "TRANSPORT";
	private static final Logger logger = LoggerFactory.getLogger(WorkflowAction.class);
	
	@Autowired
	protected BBInputSetup bbInputSetup;
	@Autowired
	protected BBInputSetupUtils bbInputSetupUtils;
	@Autowired
	private ExceptionBuilder exceptionBuilder;
	@Autowired
	private CatalogDbClient catalogDbClient;
	@Autowired
	private AAIConfigurationResources aaiConfigurationResources;
	@Autowired
	private WorkflowActionExtractResourcesAAI workflowActionUtils;

	@Autowired
        private Environment environment;
	private String defaultCloudOwner = "org.onap.so.cloud-owner";

	public void setBbInputSetupUtils(BBInputSetupUtils bbInputSetupUtils) {
		this.bbInputSetupUtils = bbInputSetupUtils;
	}

	public void setBbInputSetup(BBInputSetup bbInputSetup) {
		this.bbInputSetup = bbInputSetup;
	}

	public void selectExecutionList(DelegateExecution execution) throws Exception {
		final String requestAction = (String) execution.getVariable(G_ACTION);
		final String requestId = (String) execution.getVariable(G_REQUEST_ID);
		final String bpmnRequest = (String) execution.getVariable(G_BPMN_REQUEST);
		final boolean aLaCarte = (boolean) execution.getVariable(G_ALACARTE);
		final String apiVersion = (String) execution.getVariable(G_APIVERSION);
		final String uri = (String) execution.getVariable(G_URI);
		final String vnfType = (String) execution.getVariable(VNF_TYPE);
		String serviceInstanceId = (String) execution.getVariable("serviceInstanceId");
		final String serviceType = Optional.ofNullable((String) execution.getVariable(G_SERVICE_TYPE)).orElse("");

		List<OrchestrationFlow> orchFlows = (List<OrchestrationFlow>) execution.getVariable(G_ORCHESTRATION_FLOW);
		List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
		WorkflowResourceIds workflowResourceIds = populateResourceIdsFromApiHandler(execution);
		List<Pair<WorkflowType, String>> aaiResourceIds = new ArrayList<>();
		List<Resource> resourceCounter = new ArrayList<>();
		execution.setVariable("sentSyncResponse", false);
		execution.setVariable("homing", false);
		execution.setVariable("calledHoming", false);

		try {
			ObjectMapper mapper = new ObjectMapper();
			execution.setVariable(G_ISTOPLEVELFLOW, true);
			ServiceInstancesRequest sIRequest = mapper.readValue(bpmnRequest, ServiceInstancesRequest.class);
			RequestDetails requestDetails = sIRequest.getRequestDetails();
			String cloudOwner = "";
			try{
				cloudOwner = requestDetails.getCloudConfiguration().getCloudOwner();
			} catch (Exception ex) {
				cloudOwner = environment.getProperty(defaultCloudOwner);
			}
			boolean suppressRollback = false;
			try{
				suppressRollback = requestDetails.getRequestInfo().getSuppressRollback();
			} catch (Exception ex) {
				suppressRollback = false;
			}
			execution.setVariable("suppressRollback", suppressRollback);
			Resource resource = extractResourceIdAndTypeFromUri(uri);
			WorkflowType resourceType = resource.getResourceType();
			execution.setVariable("resourceName", resourceType.toString());
			String resourceId = "";
			if (resource.isGenerated()) {
				resourceId = validateResourceIdInAAI(resource.getResourceId(), resourceType,
						sIRequest.getRequestDetails().getRequestInfo().getInstanceName(), sIRequest.getRequestDetails(),
						workflowResourceIds);
			} else {
				resourceId = resource.getResourceId();
			}
			if((serviceInstanceId == null || serviceInstanceId.equals("")) && resourceType == WorkflowType.SERVICE){
				serviceInstanceId = resourceId;
			}
			execution.setVariable("resourceId", resourceId);
			execution.setVariable("resourceType", resourceType);

			if (aLaCarte) {
				if (orchFlows == null || orchFlows.isEmpty()) {
						orchFlows = queryNorthBoundRequestCatalogDb(execution, requestAction, resourceType, aLaCarte, cloudOwner, serviceType);
				}
				orchFlows = filterOrchFlows(orchFlows, resourceType, execution);
				String key = "";
				ModelInfo modelInfo = sIRequest.getRequestDetails().getModelInfo();
				if(modelInfo.getModelType().equals(ModelType.service)) {
					key = modelInfo.getModelVersionId();
				} else {
					key = modelInfo.getModelCustomizationId();
				}
				Resource resourceKey = new Resource(resourceType, key, aLaCarte);
				for (OrchestrationFlow orchFlow : orchFlows) {
					ExecuteBuildingBlock ebb = buildExecuteBuildingBlock(orchFlow, requestId, resourceKey, apiVersion, resourceId,
							requestAction, aLaCarte, vnfType, workflowResourceIds, requestDetails, false, null, false);
					flowsToExecute.add(ebb);
				}
			} else {
				boolean foundRelated = false;
				boolean containsService = false;
				if (resourceType == WorkflowType.SERVICE && requestAction.equalsIgnoreCase(ASSIGNINSTANCE)) {
					// SERVICE-MACRO-ASSIGN will always get user params with a
					// service.
					if (sIRequest.getRequestDetails().getRequestParameters().getUserParams() != null) {
						List<Map<String, Object>> userParams = sIRequest.getRequestDetails().getRequestParameters()
								.getUserParams();
						for (Map<String, Object> params : userParams) {
							if (params.containsKey(USERPARAMSERVICE)) {
								containsService = true;
							}
						}
						if (containsService) {
							traverseUserParamsService(execution, resourceCounter, sIRequest, requestAction);
						}
					} else {
						buildAndThrowException(execution,
								"Service-Macro-Assign request details must contain user params with a service");
					}
				} else if (resourceType == WorkflowType.SERVICE
						&& requestAction.equalsIgnoreCase(CREATEINSTANCE)) {
					// SERVICE-MACRO-CREATE will get user params with a service,
					// a service with a network, a service with a
					// networkcollection, OR an empty service.
					// If user params is just a service or null and macro
					// queries the SI and finds a VNF, macro fails.

					if (sIRequest.getRequestDetails().getRequestParameters().getUserParams() != null) {
						List<Map<String, Object>> userParams = sIRequest.getRequestDetails().getRequestParameters()
								.getUserParams();
						for (Map<String, Object> params : userParams) {
							if (params.containsKey(USERPARAMSERVICE)) {
								containsService = true;
							}
						}
					}
					if (containsService) {
						foundRelated = traverseUserParamsService(execution, resourceCounter, sIRequest, requestAction);
					}
					if (!foundRelated) {
						traverseCatalogDbService(execution, sIRequest, resourceCounter);
					}
				} else if (resourceType == WorkflowType.SERVICE
						&& (requestAction.equalsIgnoreCase("activateInstance")
								|| requestAction.equalsIgnoreCase("unassignInstance")
								|| requestAction.equalsIgnoreCase("deleteInstance")
								|| requestAction.equalsIgnoreCase("activate" + FABRIC_CONFIGURATION))) {
					// SERVICE-MACRO-ACTIVATE, SERVICE-MACRO-UNASSIGN, and
					// SERVICE-MACRO-DELETE
					// Will never get user params with service, macro will have
					// to query the SI in AAI to find related instances.
					traverseAAIService(execution, resourceCounter, resourceId, aaiResourceIds);
				} else if (resourceType == WorkflowType.SERVICE
						&& requestAction.equalsIgnoreCase("deactivateInstance")) {
					resourceCounter.add(new Resource(WorkflowType.SERVICE,"",false));
				} else if (resourceType == WorkflowType.VNF && (requestAction.equalsIgnoreCase("replaceInstance") || (requestAction.equalsIgnoreCase("recreateInstance")))) {
					traverseAAIVnf(execution, resourceCounter, workflowResourceIds.getServiceInstanceId(), workflowResourceIds.getVnfId(), aaiResourceIds);
				} else {
					buildAndThrowException(execution, "Current Macro Request is not supported");
				}
				String foundObjects = "";
				for(WorkflowType type : WorkflowType.values()){
					foundObjects = foundObjects + type + " - " + resourceCounter.stream().filter(x -> type.equals(x.getResourceType())).collect(Collectors.toList()).size() + "    ";
				}
				logger.info("Found {}", foundObjects);

				if (orchFlows == null || orchFlows.isEmpty()) {
					orchFlows = queryNorthBoundRequestCatalogDb(execution, requestAction, resourceType, aLaCarte, cloudOwner, serviceType);
				}
				flowsToExecute = buildExecuteBuildingBlockList(orchFlows, resourceCounter, requestId, apiVersion, resourceId,
						resourceType, requestAction, aLaCarte, vnfType, workflowResourceIds, requestDetails);
				if (!resourceCounter.stream().filter(x -> WorkflowType.NETWORKCOLLECTION == x.getResourceType()).collect(Collectors.toList()).isEmpty()) {
					logger.info("Sorting for Vlan Tagging");
					flowsToExecute = sortExecutionPathByObjectForVlanTagging(flowsToExecute, requestAction);
				}
				// By default, enable homing at VNF level for CREATEINSTANCE and ASSIGNINSTANCE
				if (resourceType == WorkflowType.SERVICE
					&& (requestAction.equals(CREATEINSTANCE) || requestAction.equals(ASSIGNINSTANCE))
					&& !resourceCounter.stream().filter(x -> WorkflowType.VNF.equals(x.getResourceType())).collect(Collectors.toList()).isEmpty()) {
					execution.setVariable("homing", true);
					execution.setVariable("calledHoming", false);
				}
				if (resourceType == WorkflowType.SERVICE && (requestAction.equalsIgnoreCase(ASSIGNINSTANCE) || requestAction.equalsIgnoreCase(CREATEINSTANCE))){
					generateResourceIds(flowsToExecute, resourceCounter, serviceInstanceId);
				}else{
					updateResourceIdsFromAAITraversal(flowsToExecute, resourceCounter, aaiResourceIds, serviceInstanceId);
				}
			}

			// If the user set "Homing_Solution" to "none", disable homing, else if "Homing_Solution" is specified, enable it.
			if (sIRequest.getRequestDetails().getRequestParameters().getUserParams() != null) {
				List<Map<String, Object>> userParams = sIRequest.getRequestDetails().getRequestParameters().getUserParams();
				for (Map<String, Object> params : userParams) {
					if (params.containsKey(HOMINGSOLUTION)) {
						if (params.get(HOMINGSOLUTION).equals("none")) {
							execution.setVariable("homing", false);
						} else {
							execution.setVariable("homing", true);
						}
					}
				}
			}

			if (flowsToExecute.isEmpty()) {
				throw new IllegalStateException("Macro did not come up with a valid execution path.");
			}

			logger.info("List of BuildingBlocks to execute:");
			for (ExecuteBuildingBlock ebb : flowsToExecute) {
				logger.info(ebb.getBuildingBlock().getBpmnFlowName());
			}

			execution.setVariable(G_CURRENT_SEQUENCE, 0);
			execution.setVariable("retryCount", 0);
			execution.setVariable("isRollback", false);
			execution.setVariable("flowsToExecute", flowsToExecute);
			execution.setVariable("isRollbackComplete", false);

		} catch (Exception ex) {
			buildAndThrowException(execution, "Exception in create execution list " + ex.getMessage(), ex);
		}
	}

	protected List<Resource> sortVfModulesByBaseFirst(List<Resource> vfModuleResources) {
		int count = 0;
		for(Resource resource : vfModuleResources){
			if(resource.isBaseVfModule()){
				Collections.swap(vfModuleResources, 0, count);
				break;
		}
			count++;
		}
		return vfModuleResources;
	}
	
	protected List<Resource> sortVfModulesByBaseLast(List<Resource> vfModuleResources) {
		int count = 0;
		for(Resource resource : vfModuleResources){
			if(resource.isBaseVfModule()){
				Collections.swap(vfModuleResources, vfModuleResources.size()-1, count);
				break;
		}
			count++;
		}
		return vfModuleResources;
	}

	private void updateResourceIdsFromAAITraversal(List<ExecuteBuildingBlock> flowsToExecute,
			List<Resource> resourceCounter, List<Pair<WorkflowType, String>> aaiResourceIds, String serviceInstanceId) {
		for(Pair<WorkflowType,String> pair : aaiResourceIds){
			logger.debug(pair.getValue0() + ", " + pair.getValue1());
		}
		
		Arrays.stream(WorkflowType.values()).filter(type -> !type.equals(WorkflowType.SERVICE)).forEach(type -> {
			List<Resource> resources = resourceCounter.stream().filter(x -> type.equals(x.getResourceType())).collect(Collectors.toList());
			for(int i = 0; i < resources.size(); i++){
				updateWorkflowResourceIds(flowsToExecute, type, resources.get(i).getResourceId(), retrieveAAIResourceId(aaiResourceIds,type), null, serviceInstanceId);
		}
		});
	}

	private String retrieveAAIResourceId(List<Pair<WorkflowType, String>> aaiResourceIds, WorkflowType resource){
		String id = null;
		for(int i = 0; i<aaiResourceIds.size();i++){
			if(aaiResourceIds.get(i).getValue0() == resource){
				id = aaiResourceIds.get(i).getValue1();
				aaiResourceIds.remove(i);
				break;
			}
		}
		return id;
	}
	private void generateResourceIds(List<ExecuteBuildingBlock> flowsToExecute, List<Resource> resourceCounter, String serviceInstanceId) {
		Arrays.stream(WorkflowType.values()).filter(type -> !type.equals(WorkflowType.SERVICE)).forEach(type -> {
			List<Resource> resources = resourceCounter.stream().filter(x -> type.equals(x.getResourceType())).collect(Collectors.toList());
			for(int i = 0; i < resources.size(); i++){
				Resource resource = resourceCounter.stream().filter(x -> type.equals(x.getResourceType()))
						.collect(Collectors.toList()).get(i);
				updateWorkflowResourceIds(flowsToExecute, type, resource.getResourceId(), null, resource.getVirtualLinkKey(),serviceInstanceId);
			}
		});
	}	
	
	protected void updateWorkflowResourceIds(List<ExecuteBuildingBlock> flowsToExecute, WorkflowType resource, String key, String id, String virtualLinkKey, String serviceInstanceId){
		String resourceId = id;
		if(resourceId==null){
			resourceId = UUID.randomUUID().toString();
		}
		for(ExecuteBuildingBlock ebb : flowsToExecute){
			if(key != null && key.equalsIgnoreCase(ebb.getBuildingBlock().getKey()) && ebb.getBuildingBlock().getBpmnFlowName().contains(resource.toString())){
				WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
				workflowResourceIds.setServiceInstanceId(serviceInstanceId);
				if(resource == WorkflowType.VNF){
					workflowResourceIds.setVnfId(resourceId);
				}else if(resource == WorkflowType.VFMODULE){
					workflowResourceIds.setVfModuleId(resourceId);
				}else if(resource == WorkflowType.VOLUMEGROUP){
					workflowResourceIds.setVolumeGroupId(resourceId);
				}else if(resource == WorkflowType.NETWORK){
					workflowResourceIds.setNetworkId(resourceId);
				}else if(resource == WorkflowType.NETWORKCOLLECTION){
					workflowResourceIds.setNetworkCollectionId(resourceId);
				}else if(resource == WorkflowType.CONFIGURATION){
					workflowResourceIds.setConfigurationId(resourceId);
				}
				ebb.setWorkflowResourceIds(workflowResourceIds);
			}
			if(virtualLinkKey != null && ebb.getBuildingBlock().getIsVirtualLink() 
					&& virtualLinkKey.equalsIgnoreCase(ebb.getBuildingBlock().getVirtualLinkKey())) {
				WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
				workflowResourceIds.setServiceInstanceId(serviceInstanceId);
				workflowResourceIds.setNetworkId(resourceId);
				ebb.setWorkflowResourceIds(workflowResourceIds);
			}
		}
	}

	protected CollectionResourceCustomization findCatalogNetworkCollection(DelegateExecution execution, org.onap.so.db.catalog.beans.Service service) {
		CollectionResourceCustomization networkCollection = null;
		int count = 0;
		for(CollectionResourceCustomization collectionCust : service.getCollectionResourceCustomizations()){
			if(catalogDbClient.getNetworkCollectionResourceCustomizationByID(collectionCust.getModelCustomizationUUID()) 
					instanceof NetworkCollectionResourceCustomization) {
				networkCollection = collectionCust;
				count++;
			}
		}
		if(count == 0){
			return null;
		}else if(count > 1) {
			buildAndThrowException(execution, "Found multiple Network Collections in the Service model, only one per Service is supported.");
		}
		return networkCollection;
	}
	
	protected void traverseCatalogDbService(DelegateExecution execution, ServiceInstancesRequest sIRequest,
			List<Resource> resourceCounter) {
		String modelUUID = sIRequest.getRequestDetails().getModelInfo().getModelVersionId();
		org.onap.so.db.catalog.beans.Service service = catalogDbClient.getServiceByID(modelUUID);
		if (service == null) {
			buildAndThrowException(execution, "Could not find the service model in catalog db.");
		} else {
			resourceCounter.add(new Resource(WorkflowType.SERVICE,service.getModelUUID(),false));
			if (service.getVnfCustomizations() == null || service.getVnfCustomizations().isEmpty()) {
				List<CollectionResourceCustomization> customizations = service.getCollectionResourceCustomizations();
				if(customizations.isEmpty()) {
					logger.debug("No Collections found. CollectionResourceCustomization list is empty.");
				}else{
					CollectionResourceCustomization collectionResourceCustomization = findCatalogNetworkCollection(execution, service);
					if(collectionResourceCustomization!=null){
						resourceCounter.add(new Resource(WorkflowType.NETWORKCOLLECTION,collectionResourceCustomization.getModelCustomizationUUID(),false));
						logger.debug("Found a network collection");
						if(collectionResourceCustomization.getCollectionResource()!=null){
							if(collectionResourceCustomization.getCollectionResource().getInstanceGroup() != null){
								String toscaNodeType = collectionResourceCustomization.getCollectionResource().getInstanceGroup().getToscaNodeType();
								if (toscaNodeType != null && toscaNodeType.contains("NetworkCollection")) {
									int minNetworks = 0;
									org.onap.so.db.catalog.beans.InstanceGroup instanceGroup = collectionResourceCustomization.getCollectionResource().getInstanceGroup();
									CollectionResourceInstanceGroupCustomization collectionInstCust = null;
									if(!instanceGroup.getCollectionInstanceGroupCustomizations().isEmpty()) {
										for(CollectionResourceInstanceGroupCustomization collectionInstanceGroupTemp : instanceGroup.getCollectionInstanceGroupCustomizations()) {
											if(collectionInstanceGroupTemp.getModelCustomizationUUID().equalsIgnoreCase(collectionResourceCustomization.getModelCustomizationUUID())) {
												collectionInstCust = collectionInstanceGroupTemp;
												break;
											}
										}
										if(collectionInstCust != null && collectionInstCust.getSubInterfaceNetworkQuantity() != null) {
											minNetworks = collectionInstCust.getSubInterfaceNetworkQuantity();
										}
									}
									logger.debug("minNetworks: {}" , minNetworks);
									CollectionNetworkResourceCustomization collectionNetworkResourceCust = null;
									for(CollectionNetworkResourceCustomization collectionNetworkTemp : instanceGroup.getCollectionNetworkResourceCustomizations()) {
										if(collectionNetworkTemp.getNetworkResourceCustomization().getModelCustomizationUUID().equalsIgnoreCase(collectionResourceCustomization.getModelCustomizationUUID())) {
											collectionNetworkResourceCust = collectionNetworkTemp;
											break;
										}
									}
									for (int i = 0; i < minNetworks; i++) {
										if(collectionNetworkResourceCust != null && collectionInstCust != null) {
											Resource resource = new Resource(WorkflowType.VIRTUAL_LINK,collectionNetworkResourceCust.getModelCustomizationUUID(),false);
											resource.setVirtualLinkKey(Integer.toString(i));
											resourceCounter.add(resource);
										}
									}
								} else {
									logger.debug("Instance Group tosca node type does not contain NetworkCollection:  {}" , toscaNodeType);
								}
							}else{
								logger.debug("No Instance Group found for network collection.");
							}
						}else{
							logger.debug("No Network Collection found. collectionResource is null");
						}
					} else {
						logger.debug("No Network Collection Customization found");
					}
				}
				if (resourceCounter.stream().filter(x -> WorkflowType.NETWORKCOLLECTION == x.getResourceType()).collect(Collectors.toList()).isEmpty()) {
					if (service.getNetworkCustomizations() == null) {
						logger.debug("No networks were found on this service model");
					} else {
						for (int i = 0; i < service.getNetworkCustomizations().size(); i++) {
							resourceCounter.add(new Resource(WorkflowType.NETWORK,service.getNetworkCustomizations().get(i).getModelCustomizationUUID(),false));
						}
					}
				}
			} else {
				buildAndThrowException(execution,
						"Cannot orchestrate Service-Macro-Create without user params with a vnf. Please update ASDC model for new macro orchestration support or add service_recipe records to route to old macro flows");
			}
		}
	}

	protected void traverseAAIService(DelegateExecution execution, List<Resource> resourceCounter, String resourceId, List<Pair<WorkflowType, String>> aaiResourceIds) {
		try {
			ServiceInstance serviceInstanceAAI = bbInputSetupUtils.getAAIServiceInstanceById(resourceId);
			org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO = bbInputSetup
					.getExistingServiceInstance(serviceInstanceAAI);
			resourceCounter.add(new Resource(WorkflowType.SERVICE,serviceInstanceMSO.getServiceInstanceId(),false));
			if (serviceInstanceMSO.getVnfs() != null) {
				for (org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf : serviceInstanceMSO
						.getVnfs()) {
					aaiResourceIds.add(new Pair<WorkflowType, String>(WorkflowType.VNF, vnf.getVnfId()));
					resourceCounter.add(new Resource(WorkflowType.VNF,vnf.getVnfId(),false));
					if (vnf.getVfModules() != null) {
						for (VfModule vfModule : vnf.getVfModules()) {
							aaiResourceIds.add(new Pair<WorkflowType, String>(WorkflowType.VFMODULE, vfModule.getVfModuleId()));
							resourceCounter.add(new Resource(WorkflowType.VFMODULE,vfModule.getVfModuleId(),false));
						}
					}
					if (vnf.getVolumeGroups() != null) {
						for (org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup volumeGroup : vnf
								.getVolumeGroups()) {
							aaiResourceIds.add(new Pair<WorkflowType, String>(WorkflowType.VOLUMEGROUP, volumeGroup.getVolumeGroupId()));
							resourceCounter.add(new Resource(WorkflowType.VOLUMEGROUP,volumeGroup.getVolumeGroupId(),false));
						}
					}
				}
			}
			if (serviceInstanceMSO.getNetworks() != null) {
				for (org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network network : serviceInstanceMSO
						.getNetworks()) {
					aaiResourceIds.add(new Pair<WorkflowType, String>(WorkflowType.NETWORK, network.getNetworkId()));
					resourceCounter.add(new Resource(WorkflowType.NETWORK,network.getNetworkId(),false));
				}
			}
			if (serviceInstanceMSO.getCollection() != null) {
				logger.debug("found networkcollection");
				aaiResourceIds.add(new Pair<WorkflowType, String>(WorkflowType.NETWORKCOLLECTION, serviceInstanceMSO.getCollection().getId()));
				resourceCounter.add(new Resource(WorkflowType.NETWORKCOLLECTION,serviceInstanceMSO.getCollection().getId(),false));
			}
			if (serviceInstanceMSO.getConfigurations() !=null) {
				for(Configuration config : serviceInstanceMSO.getConfigurations()){
					Optional<org.onap.aai.domain.yang.Configuration> aaiConfig = aaiConfigurationResources.getConfiguration(config.getConfigurationId());
					if(aaiConfig.isPresent() && aaiConfig.get().getRelationshipList()!=null){
						for(Relationship relationship : aaiConfig.get().getRelationshipList().getRelationship()){
							if(relationship.getRelatedTo().contains("vnfc")){
								aaiResourceIds.add(new Pair<WorkflowType, String>(WorkflowType.CONFIGURATION, config.getConfigurationId()));
								resourceCounter.add(new Resource(WorkflowType.CONFIGURATION,config.getConfigurationId(),false));
								break;
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			buildAndThrowException(execution,
					"Could not find existing Service Instance or related Instances to execute the request on.");
		}
	}

	private void traverseAAIVnf(DelegateExecution execution, List<Resource> resourceCounter, String serviceId, String vnfId,
			List<Pair<WorkflowType, String>> aaiResourceIds) {
		try{
			ServiceInstance serviceInstanceAAI = bbInputSetupUtils.getAAIServiceInstanceById(serviceId);
			org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO = bbInputSetup
					.getExistingServiceInstance(serviceInstanceAAI);
			resourceCounter.add(new Resource(WorkflowType.SERVICE,serviceInstanceMSO.getServiceInstanceId(),false));
			if (serviceInstanceMSO.getVnfs() != null) {
				for (org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf : serviceInstanceMSO
						.getVnfs()) {
					if(vnf.getVnfId().equals(vnfId)){
						aaiResourceIds.add(new Pair<WorkflowType, String>(WorkflowType.VNF, vnf.getVnfId()));
						resourceCounter.add(new Resource(WorkflowType.VNF,vnf.getVnfId(),false));
						if (vnf.getVfModules() != null) {
							for (VfModule vfModule : vnf.getVfModules()) {
								aaiResourceIds.add(new Pair<WorkflowType, String>(WorkflowType.VFMODULE, vfModule.getVfModuleId()));
								resourceCounter.add(new Resource(WorkflowType.VFMODULE,vfModule.getVfModuleId(),false));	
								findConfigurationsInsideVfModule(execution, vnf.getVnfId(), vfModule.getVfModuleId(), resourceCounter, aaiResourceIds);
							}
						}
						if (vnf.getVolumeGroups() != null) {
							for (org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup volumeGroup : vnf
									.getVolumeGroups()) {
								aaiResourceIds.add(new Pair<WorkflowType, String>(WorkflowType.VOLUMEGROUP, volumeGroup.getVolumeGroupId()));
								resourceCounter.add(new Resource(WorkflowType.VOLUMEGROUP,volumeGroup.getVolumeGroupId(),false));
							}
						}
						break;
					}
				}
			}
		} catch (Exception ex) {
			buildAndThrowException(execution,
					"Could not find existing Vnf or related Instances to execute the request on.");
		}
	}

	private void findConfigurationsInsideVfModule(DelegateExecution execution, String vnfId, String vfModuleId, List<Resource> resourceCounter, 
			List<Pair<WorkflowType, String>> aaiResourceIds) {
		try{
			org.onap.aai.domain.yang.VfModule aaiVfModule = bbInputSetupUtils.getAAIVfModule(vnfId, vfModuleId);
			AAIResultWrapper vfModuleWrapper = new AAIResultWrapper(
					new AAICommonObjectMapperProvider().getMapper().writeValueAsString(aaiVfModule));
			Optional<Relationships> relationshipsOp;
			relationshipsOp = vfModuleWrapper.getRelationships();
			if(relationshipsOp.isPresent()) {
				relationshipsOp = workflowActionUtils.extractRelationshipsVnfc(relationshipsOp.get());
				if(relationshipsOp.isPresent()){
					Optional<Configuration> config = workflowActionUtils.extractRelationshipsConfiguration(relationshipsOp.get());
					if(config.isPresent()){
						aaiResourceIds.add(new Pair<WorkflowType, String>(WorkflowType.CONFIGURATION, config.get().getConfigurationId()));
						resourceCounter.add(new Resource(WorkflowType.CONFIGURATION, config.get().getConfigurationId(), false));
					}
				}
			}
		}catch (Exception ex){
			buildAndThrowException(execution,
					"Failed to find Configuration object from the vfModule.");
		}
	}
	
	protected boolean traverseUserParamsService(DelegateExecution execution, List<Resource> resourceCounter,
			ServiceInstancesRequest sIRequest, String requestAction)
			throws IOException {
		boolean foundRelated = false;
		boolean foundVfModuleOrVG = false;
		String vnfCustomizationUUID = "";
		String vfModuleCustomizationUUID = "";
		if (sIRequest.getRequestDetails().getRequestParameters().getUserParams() != null) {
			List<Map<String, Object>> userParams = sIRequest.getRequestDetails().getRequestParameters().getUserParams();
			for (Map<String, Object> params : userParams) {
				if (params.containsKey(USERPARAMSERVICE)) {
					ObjectMapper obj = new ObjectMapper();
					String input = obj.writeValueAsString(params.get(USERPARAMSERVICE));
					Service validate = obj.readValue(input, Service.class);
					resourceCounter.add(new Resource(WorkflowType.SERVICE,validate.getModelInfo().getModelVersionId(),false));
					if (validate.getResources().getVnfs() != null) {
						for (Vnfs vnf : validate.getResources().getVnfs()) {
							resourceCounter.add(new Resource(WorkflowType.VNF,vnf.getModelInfo().getModelCustomizationId(),false));
							foundRelated = true;
							if(vnf.getModelInfo()!=null && vnf.getModelInfo().getModelCustomizationUuid()!=null){
								vnfCustomizationUUID = vnf.getModelInfo().getModelCustomizationUuid();
							}
							if (vnf.getVfModules() != null) {
								for (VfModules vfModule : vnf.getVfModules()) {
									VfModuleCustomization vfModuleCustomization = catalogDbClient
											.getVfModuleCustomizationByModelCuztomizationUUID(
													vfModule.getModelInfo().getModelCustomizationUuid());
									if (vfModuleCustomization != null) {

										if(vfModuleCustomization.getVfModule() != null && vfModuleCustomization.getVfModule().getVolumeHeatTemplate() != null && vfModuleCustomization.getVolumeHeatEnv() != null) {
											resourceCounter.add(new Resource(WorkflowType.VOLUMEGROUP,vfModuleCustomization.getModelCustomizationUUID(),false));
											foundRelated = true;
											foundVfModuleOrVG = true;
										}

										if(vfModuleCustomization.getVfModule() != null && vfModuleCustomization.getVfModule().getModuleHeatTemplate() != null && vfModuleCustomization.getHeatEnvironment() != null){
											foundRelated = true;
											foundVfModuleOrVG = true;
											Resource resource = new Resource(WorkflowType.VFMODULE,vfModuleCustomization.getModelCustomizationUUID(),false);
											if(vfModuleCustomization.getVfModule().getIsBase()!=null && vfModuleCustomization.getVfModule().getIsBase()){
												resource.setBaseVfModule(true);
											}else{
												resource.setBaseVfModule(false);
											}
											resourceCounter.add(resource);
											if(vfModule.getModelInfo()!=null && vfModule.getModelInfo().getModelCustomizationUuid()!=null){
												vfModuleCustomizationUUID = vfModule.getModelInfo().getModelCustomizationUuid();
											}
											if(!vnfCustomizationUUID.equals("")&&!vfModuleCustomizationUUID.equals("")){
												List<String> configs = traverseCatalogDbForConfiguration(vnfCustomizationUUID,vfModuleCustomizationUUID);
												for(String config : configs){
													Resource configResource = new Resource(WorkflowType.CONFIGURATION,config,false);
													resource.setVnfCustomizationId(vnf.getModelInfo().getModelCustomizationId());
													resource.setVfModuleCustomizationId(vfModule.getModelInfo().getModelCustomizationId());
													resourceCounter.add(configResource);
												}
											}
										}
										if(!foundVfModuleOrVG){
											buildAndThrowException(execution, "Could not determine if vfModule was a vfModule or volume group. Heat template and Heat env are null");
										}
									}
								}
							}
						}
					}
					if (validate.getResources().getNetworks() != null) {
						for (Networks network : validate.getResources().getNetworks()) {
							resourceCounter.add(new Resource(WorkflowType.NETWORK,network.getModelInfo().getModelCustomizationId(),false));
							foundRelated = true;
						}
						if (requestAction.equals(CREATEINSTANCE)) {
							String networkColCustId = queryCatalogDBforNetworkCollection(execution, sIRequest);
							if (networkColCustId != null) {
								resourceCounter.add(new Resource(WorkflowType.NETWORKCOLLECTION,networkColCustId,false));
								foundRelated = true;
							}
						}
					}
					break;
				}
			}
		}
		return foundRelated;
	}
	

	protected List<String> traverseCatalogDbForConfiguration(String vnfCustomizationUUID, String vfModuleCustomizationUUID) {
		List<String> configurations = new ArrayList<>();
		try{
			List<CvnfcCustomization> cvnfcCustomizations = catalogDbClient.getCvnfcCustomizationByVnfCustomizationUUIDAndVfModuleCustomizationUUID(vnfCustomizationUUID, vfModuleCustomizationUUID);
			for(CvnfcCustomization cvnfc : cvnfcCustomizations){
				for(VnfVfmoduleCvnfcConfigurationCustomization customization : cvnfc.getVnfVfmoduleCvnfcConfigurationCustomization()){
					if(customization.getConfigurationResource().getToscaNodeType().contains(FABRIC_CONFIGURATION)){
						configurations.add(customization.getConfigurationResource().getModelUUID());
					}
				}
			}
			logger.debug("found {} configurations" , configurations.size() );
			return configurations;
		} catch (Exception ex){
			logger.error("Error in finding configurations", ex);
			return configurations;
		}
	}

	protected String queryCatalogDBforNetworkCollection(DelegateExecution execution, ServiceInstancesRequest sIRequest) {
		org.onap.so.db.catalog.beans.Service service = catalogDbClient
				.getServiceByID(sIRequest.getRequestDetails().getModelInfo().getModelVersionId());
		if (service != null) {
			CollectionResourceCustomization networkCollection = this.findCatalogNetworkCollection(execution, service);
			if(networkCollection != null) {
				return networkCollection.getModelCustomizationUUID();
			}
		}
		return null;
	}

	protected WorkflowResourceIds populateResourceIdsFromApiHandler(DelegateExecution execution) {
		WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
		workflowResourceIds.setServiceInstanceId((String) execution.getVariable("serviceInstanceId"));
		workflowResourceIds.setNetworkId((String) execution.getVariable("networkId"));
		workflowResourceIds.setVfModuleId((String) execution.getVariable("vfModuleId"));
		workflowResourceIds.setVnfId((String) execution.getVariable("vnfId"));
		workflowResourceIds.setVolumeGroupId((String) execution.getVariable("volumeGroupId"));
		return workflowResourceIds;
	}

	protected Resource extractResourceIdAndTypeFromUri(String uri) {
	    Pattern patt = Pattern.compile(
	            "[vV]\\d+.*?(?:(?:/(?<type>" + supportedTypes + ")(?:/(?<id>[^/]+))?)(?:/(?<action>[^/]+))?)?$");
	    Matcher m = patt.matcher(uri);
	    Boolean generated = false;

	    if (m.find()) {
	        logger.debug("found match on {} : {} " , uri ,  m);
	        String type = m.group("type");
	        String id = m.group("id");
	        String action = m.group("action");
	        if (type == null) {
	            throw new IllegalArgumentException("Uri could not be parsed. No type found. " + uri);
	        }
	        if (action == null) {
	            if (type.equals(SERVICE_INSTANCES) && (id == null || id.equals("assign"))) {
	                id = UUID.randomUUID().toString();
	                generated = true;
	            }else if (type.equals(VF_MODULES) && id.equals("scaleOut")) {
	                id = UUID.randomUUID().toString();
	                generated = true;
	            }
	        } else {
	            if (action.matches(supportedTypes)) {
	                id = UUID.randomUUID().toString();
	                generated = true;
	                type = action;
	            }
	        }
	        return new Resource(WorkflowType.fromString(convertTypeFromPlural(type)), id, generated);
	    } else {
	        throw new IllegalArgumentException("Uri could not be parsed: " + uri);
	    }
	}

	protected String validateResourceIdInAAI(String generatedResourceId, WorkflowType type, String instanceName,
			RequestDetails reqDetails, WorkflowResourceIds workflowResourceIds) throws Exception {
		try {
			if ("SERVICE".equalsIgnoreCase(type.toString())) {
				String globalCustomerId = reqDetails.getSubscriberInfo().getGlobalSubscriberId();
				String serviceType = reqDetails.getRequestParameters().getSubscriptionServiceType();
				if (instanceName != null) {
					Optional<ServiceInstance> serviceInstanceAAI = bbInputSetupUtils
							.getAAIServiceInstanceByName(globalCustomerId, serviceType, instanceName);
					if (serviceInstanceAAI.isPresent()) {
						return serviceInstanceAAI.get().getServiceInstanceId();
					}
				}
			} else if ("NETWORK".equalsIgnoreCase(type.toString())) {
				Optional<L3Network> network = bbInputSetupUtils.getRelatedNetworkByNameFromServiceInstance(
						workflowResourceIds.getServiceInstanceId(), instanceName);
				if (network.isPresent()) {
					return network.get().getNetworkId();
				}
			} else if ("VNF".equalsIgnoreCase(type.toString())) {
				Optional<GenericVnf> vnf = bbInputSetupUtils.getRelatedVnfByNameFromServiceInstance(
						workflowResourceIds.getServiceInstanceId(), instanceName);
				if (vnf.isPresent()) {
					return vnf.get().getVnfId();
				}
			} else if ("VFMODULE".equalsIgnoreCase(type.toString())) {
				GenericVnf vnf = bbInputSetupUtils.getAAIGenericVnf(workflowResourceIds.getVnfId());
				if (vnf != null && vnf.getVfModules() != null) {
					for (org.onap.aai.domain.yang.VfModule vfModule : vnf.getVfModules().getVfModule()) {
						if (vfModule.getVfModuleName().equalsIgnoreCase(instanceName)) {
							return vfModule.getVfModuleId();
						}
					}
				}
			} else if ("VOLUMEGROUP".equalsIgnoreCase(type.toString())) {
				Optional<VolumeGroup> volumeGroup = bbInputSetupUtils
						.getRelatedVolumeGroupByNameFromVnf(workflowResourceIds.getVnfId(), instanceName);
				if (volumeGroup.isPresent()) {
					return volumeGroup.get().getVolumeGroupId();
				}
				GenericVnf vnf = bbInputSetupUtils.getAAIGenericVnf(workflowResourceIds.getVnfId());
				if (vnf != null && vnf.getVfModules() != null) {
					for (org.onap.aai.domain.yang.VfModule vfModule : vnf.getVfModules().getVfModule()) {
						Optional<VolumeGroup> volumeGroupFromVfModule = bbInputSetupUtils
								.getRelatedVolumeGroupByNameFromVfModule(vnf.getVnfId(), vfModule.getVfModuleId(), instanceName);
						if (volumeGroupFromVfModule.isPresent()) {
							return volumeGroupFromVfModule.get().getVolumeGroupId();
						}
					}
				}
			}
			return generatedResourceId;
		} catch (Exception ex) {
			logger.error(WORKFLOW_ACTION_WAS_UNABLE_TO_VERIFY_IF_THE_INSTANCE_NAME_ALREADY_EXIST_IN_AAI, ex);
			throw new IllegalStateException(
					WORKFLOW_ACTION_WAS_UNABLE_TO_VERIFY_IF_THE_INSTANCE_NAME_ALREADY_EXIST_IN_AAI);
		}
	}

	protected String convertTypeFromPlural(String type) {
		if (!type.matches(supportedTypes)) {
			return type;
		} else {
			if (type.equals(SERVICE_INSTANCES)) {
				return SERVICE;
			} else {
				return type.substring(0, 1).toUpperCase() + type.substring(1, type.length() - 1);
			}
		}
	}

	protected List<ExecuteBuildingBlock> sortExecutionPathByObjectForVlanTagging(List<ExecuteBuildingBlock> orchFlows,
			String requestAction) {
		List<ExecuteBuildingBlock> sortedOrchFlows = new ArrayList<>();
		if (requestAction.equals(CREATEINSTANCE)) {
			for (ExecuteBuildingBlock ebb : orchFlows) {
				if (ebb.getBuildingBlock().getBpmnFlowName().equals("AssignNetworkBB")) {
					String key = ebb.getBuildingBlock().getKey();
					boolean isVirtualLink = Boolean.TRUE.equals(ebb.getBuildingBlock().getIsVirtualLink());
					String virtualLinkKey = ebb.getBuildingBlock().getVirtualLinkKey();
					sortedOrchFlows.add(ebb);
					for (ExecuteBuildingBlock ebb2 : orchFlows) {
						if (!isVirtualLink && ebb2.getBuildingBlock().getBpmnFlowName().equals("CreateNetworkBB")
								&& ebb2.getBuildingBlock().getKey().equalsIgnoreCase(key)) {
							sortedOrchFlows.add(ebb2);
							break;
						}
						if(isVirtualLink && ebb2.getBuildingBlock().getBpmnFlowName().equals("CreateNetworkBB")
								&& ebb2.getBuildingBlock().getVirtualLinkKey().equalsIgnoreCase(virtualLinkKey)) {
							sortedOrchFlows.add(ebb2);
							break;
						}
					}
					for (ExecuteBuildingBlock ebb2 : orchFlows) {
						if (!isVirtualLink && ebb2.getBuildingBlock().getBpmnFlowName().equals("ActivateNetworkBB")
								&& ebb2.getBuildingBlock().getKey().equalsIgnoreCase(key)) {
							sortedOrchFlows.add(ebb2);
							break;
						}
						if(isVirtualLink && ebb2.getBuildingBlock().getBpmnFlowName().equals("ActivateNetworkBB")
								&& ebb2.getBuildingBlock().getVirtualLinkKey().equalsIgnoreCase(virtualLinkKey)) {
							sortedOrchFlows.add(ebb2);
							break;
						}
					}
				} else if (ebb.getBuildingBlock().getBpmnFlowName().equals("CreateNetworkBB")
						|| ebb.getBuildingBlock().getBpmnFlowName().equals("ActivateNetworkBB")) {
					continue;
				} else if (!ebb.getBuildingBlock().getBpmnFlowName().equals("")) {
					sortedOrchFlows.add(ebb);
				}
			}
		} else if (requestAction.equals("deleteInstance")) {
			for (ExecuteBuildingBlock ebb : orchFlows) {
				if (ebb.getBuildingBlock().getBpmnFlowName().equals("DeactivateNetworkBB")) {
					sortedOrchFlows.add(ebb);
					String key = ebb.getBuildingBlock().getKey();
					for (ExecuteBuildingBlock ebb2 : orchFlows) {
						if (ebb2.getBuildingBlock().getBpmnFlowName().equals("DeleteNetworkBB")
								&& ebb2.getBuildingBlock().getKey().equalsIgnoreCase(key)) {
							sortedOrchFlows.add(ebb2);
							break;
						}
					}
					for (ExecuteBuildingBlock ebb2 : orchFlows) {
						if (ebb2.getBuildingBlock().getBpmnFlowName().equals("UnassignNetworkBB")
								&& ebb2.getBuildingBlock().getKey().equalsIgnoreCase(key)) {
							sortedOrchFlows.add(ebb2);
							break;
						}
					}
				} else if (ebb.getBuildingBlock().getBpmnFlowName().equals("DeleteNetworkBB")
						|| ebb.getBuildingBlock().getBpmnFlowName().equals("UnassignNetworkBB")) {
					continue;
				} else if (!ebb.getBuildingBlock().getBpmnFlowName().equals("")) {
					sortedOrchFlows.add(ebb);
				}
			}
		}
		return sortedOrchFlows;
	}

	protected List<ExecuteBuildingBlock> buildExecuteBuildingBlockList(List<OrchestrationFlow> orchFlows,
			List<Resource> resourceCounter, String requestId, String apiVersion, String resourceId, WorkflowType resourceType,
			String requestAction, boolean aLaCarte, String vnfType,
			WorkflowResourceIds workflowResourceIds, RequestDetails requestDetails) {
		List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
		for (OrchestrationFlow orchFlow : orchFlows) {
			if (orchFlow.getFlowName().contains(SERVICE)) {
				for (int i = 0; i < resourceCounter.stream().filter(x -> WorkflowType.SERVICE == x.getResourceType()).collect(Collectors.toList()).size(); i++) {
					workflowResourceIds.setServiceInstanceId(resourceId);
					flowsToExecute.add(buildExecuteBuildingBlock(orchFlow, requestId, resourceCounter.stream().filter(x -> WorkflowType.SERVICE == x.getResourceType())
							.collect(Collectors.toList()).get(i), apiVersion, resourceId,
							requestAction, aLaCarte, vnfType, workflowResourceIds, requestDetails, false, null, false));
				}
			} else if (orchFlow.getFlowName().contains(VNF)) {
				for (int i = 0; i < resourceCounter.stream().filter(x -> WorkflowType.VNF == x.getResourceType()).collect(Collectors.toList()).size(); i++) {
					flowsToExecute.add(buildExecuteBuildingBlock(orchFlow, requestId, resourceCounter.stream().filter(x -> WorkflowType.VNF == x.getResourceType())
							.collect(Collectors.toList()).get(i), apiVersion, resourceId,
							requestAction, aLaCarte, vnfType, workflowResourceIds, requestDetails, false, null, false));
				}
			} else if (orchFlow.getFlowName().contains(NETWORK)
					&& !orchFlow.getFlowName().contains(NETWORKCOLLECTION)) {
				for (int i = 0; i < resourceCounter.stream().filter(x -> WorkflowType.NETWORK == x.getResourceType()).collect(Collectors.toList()).size(); i++) {
					flowsToExecute.add(buildExecuteBuildingBlock(orchFlow, requestId, resourceCounter.stream().filter(x -> WorkflowType.NETWORK == x.getResourceType())
							.collect(Collectors.toList()).get(i), apiVersion, resourceId,
							requestAction, aLaCarte, vnfType, workflowResourceIds, requestDetails, false, null, false));
				}
				for (int i = 0; i < resourceCounter.stream().filter(x -> WorkflowType.VIRTUAL_LINK == x.getResourceType()).collect(Collectors.toList()).size(); i++) {
					Resource resource = resourceCounter.stream().filter(x -> WorkflowType.VIRTUAL_LINK == x.getResourceType())
							.collect(Collectors.toList()).get(i);
					flowsToExecute.add(buildExecuteBuildingBlock(orchFlow, requestId, resource, apiVersion, resourceId,
							requestAction, aLaCarte, vnfType, workflowResourceIds, requestDetails, true, resource.getVirtualLinkKey(), false));
				}
			} else if (orchFlow.getFlowName().contains(VFMODULE)) {
				List<Resource> vfModuleResourcesSorted = null;
				if(requestAction.equals("createInstance")||requestAction.equals("assignInstance")||requestAction.equals("activateInstance")){
					vfModuleResourcesSorted = sortVfModulesByBaseFirst(resourceCounter.stream().filter(x -> WorkflowType.VFMODULE == x.getResourceType())
						.collect(Collectors.toList()));
				}else{
					vfModuleResourcesSorted = sortVfModulesByBaseLast(resourceCounter.stream().filter(x -> WorkflowType.VFMODULE == x.getResourceType())
							.collect(Collectors.toList()));
				}
				for (int i = 0; i < vfModuleResourcesSorted.size(); i++) {
					flowsToExecute.add(buildExecuteBuildingBlock(orchFlow, requestId, vfModuleResourcesSorted.get(i), apiVersion, resourceId,
							requestAction, aLaCarte, vnfType, workflowResourceIds, requestDetails, false, null, false));
				}
			} else if (orchFlow.getFlowName().contains(VOLUMEGROUP)) {
				for (int i = 0; i < resourceCounter.stream().filter(x -> WorkflowType.VOLUMEGROUP == x.getResourceType()).collect(Collectors.toList()).size(); i++) {
					flowsToExecute.add(buildExecuteBuildingBlock(orchFlow, requestId, resourceCounter.stream().filter(x -> WorkflowType.VOLUMEGROUP == x.getResourceType())
							.collect(Collectors.toList()).get(i), apiVersion, resourceId,
							requestAction, aLaCarte, vnfType, workflowResourceIds, requestDetails, false, null, false));
				}
			} else if (orchFlow.getFlowName().contains(NETWORKCOLLECTION)) {
				for (int i = 0; i < resourceCounter.stream().filter(x -> WorkflowType.NETWORKCOLLECTION == x.getResourceType()).collect(Collectors.toList()).size(); i++) {
					flowsToExecute.add(buildExecuteBuildingBlock(orchFlow, requestId, resourceCounter.stream().filter(x -> WorkflowType.NETWORKCOLLECTION == x.getResourceType())
							.collect(Collectors.toList()).get(i), apiVersion, resourceId,
							requestAction, aLaCarte, vnfType, workflowResourceIds, requestDetails, false, null, false));
				}
			} else if (orchFlow.getFlowName().contains(CONFIGURATION)) {
				for (int i = 0; i < resourceCounter.stream().filter(x -> WorkflowType.CONFIGURATION == x.getResourceType()).collect(Collectors.toList()).size(); i++) {
					flowsToExecute.add(buildExecuteBuildingBlock(orchFlow, requestId, resourceCounter.stream().filter(x -> WorkflowType.CONFIGURATION == x.getResourceType())
							.collect(Collectors.toList()).get(i), apiVersion, resourceId,
							requestAction, aLaCarte, vnfType, workflowResourceIds, requestDetails, false, null, true));
				}
			}else {
				flowsToExecute.add(buildExecuteBuildingBlock(orchFlow, requestId, null, apiVersion, resourceId,
						requestAction, aLaCarte, vnfType, workflowResourceIds, requestDetails, false, null, false));
			}
		}
		return flowsToExecute;
	}

	protected ExecuteBuildingBlock buildExecuteBuildingBlock(OrchestrationFlow orchFlow, String requestId, Resource resource,
			String apiVersion, String resourceId, String requestAction, boolean aLaCarte, String vnfType,
			WorkflowResourceIds workflowResourceIds, RequestDetails requestDetails, boolean isVirtualLink, String virtualLinkKey, boolean isConfiguration) {
		ExecuteBuildingBlock executeBuildingBlock = new ExecuteBuildingBlock();
		BuildingBlock buildingBlock = new BuildingBlock();
		buildingBlock.setBpmnFlowName(orchFlow.getFlowName());
		buildingBlock.setMsoId(UUID.randomUUID().toString());
		if(resource == null){
			buildingBlock.setKey("");
		}else{
			buildingBlock.setKey(resource.getResourceId());
		}
		buildingBlock.setIsVirtualLink(isVirtualLink);
		buildingBlock.setVirtualLinkKey(virtualLinkKey);
		executeBuildingBlock.setApiVersion(apiVersion);
		executeBuildingBlock.setaLaCarte(aLaCarte);
		executeBuildingBlock.setRequestAction(requestAction);
		executeBuildingBlock.setResourceId(resourceId);
		executeBuildingBlock.setVnfType(vnfType);
		executeBuildingBlock.setWorkflowResourceIds(workflowResourceIds);
		executeBuildingBlock.setRequestId(requestId);
		executeBuildingBlock.setBuildingBlock(buildingBlock);
		executeBuildingBlock.setRequestDetails(requestDetails);
		if(isConfiguration){
			ConfigurationResourceKeys configurationResourceKeys = new ConfigurationResourceKeys();
			configurationResourceKeys.setCvnfcCustomizationUUID(resource.getCvnfModuleCustomizationId());
			configurationResourceKeys.setVfModuleCustomizationUUID(resource.getVfModuleCustomizationId());
			configurationResourceKeys.setVnfResourceCustomizationUUID(resource.getVnfCustomizationId());
			executeBuildingBlock.setConfigurationResourceKeys(configurationResourceKeys);
		}
		return executeBuildingBlock;
	}

	protected List<OrchestrationFlow> queryNorthBoundRequestCatalogDb(DelegateExecution execution, String requestAction,
			WorkflowType resourceName, boolean aLaCarte, String cloudOwner) {
		return this.queryNorthBoundRequestCatalogDb(execution, requestAction, resourceName, aLaCarte, cloudOwner, "");
	}
	
	protected List<OrchestrationFlow> queryNorthBoundRequestCatalogDb(DelegateExecution execution, String requestAction,
			WorkflowType resourceName, boolean aLaCarte, String cloudOwner, String serviceType) {
		List<OrchestrationFlow> listToExecute = new ArrayList<>();
		NorthBoundRequest northBoundRequest = null;
		if (serviceType.equalsIgnoreCase(SERVICE_TYPE_TRANSPORT)) {
			northBoundRequest = catalogDbClient
					.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwnerAndServiceType(requestAction,
							resourceName.toString(), aLaCarte, cloudOwner, serviceType);
		} else {
			northBoundRequest = catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(
					requestAction, resourceName.toString(), aLaCarte, cloudOwner);
		}
		if(northBoundRequest == null){
			if(aLaCarte){
				buildAndThrowException(execution,"The request: ALaCarte " + resourceName + " " + requestAction + " is not supported by GR_API.");
			}else{
				buildAndThrowException(execution,"The request: Macro " + resourceName + " " + requestAction + " is not supported by GR_API.");
			}
		} else {
		if(northBoundRequest.getIsToplevelflow()!=null){
			execution.setVariable(G_ISTOPLEVELFLOW, northBoundRequest.getIsToplevelflow());
		}
		List<OrchestrationFlow> flows = northBoundRequest.getOrchestrationFlowList();
		if (flows == null)
			flows = new ArrayList<>();
		for (OrchestrationFlow flow : flows) {
			if (!flow.getFlowName().contains("BB")) {
				List<OrchestrationFlow> macroQueryFlows = catalogDbClient
						.getOrchestrationFlowByAction(flow.getFlowName());
				for (OrchestrationFlow macroFlow : macroQueryFlows) {
					listToExecute.add(macroFlow);
				}
			} else {
				listToExecute.add(flow);
			}
		}
		}
		return listToExecute;
	}
	
	protected List<OrchestrationFlow> filterOrchFlows(List<OrchestrationFlow> orchFlows, WorkflowType resourceType, DelegateExecution execution) {
		List<OrchestrationFlow> result = new ArrayList<>(orchFlows);
		if (resourceType.equals(WorkflowType.VFMODULE)) {
			List<String> fabricCustomizations = traverseCatalogDbForConfiguration((String)execution.getVariable("vnfId"), (String)execution.getVariable("vfModuleId"));
			if (fabricCustomizations.isEmpty()) {
				result = orchFlows.stream().filter(item -> !item.getFlowName().contains(FABRIC_CONFIGURATION)).collect(Collectors.toList());
			}
		}
		return result;
	}

	protected void buildAndThrowException(DelegateExecution execution, String msg, Exception ex) {
		logger.error(msg, ex);
		execution.setVariable(WORKFLOW_ACTION_ERROR_MESSAGE, msg);
		exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, msg);
	}

	protected void buildAndThrowException(DelegateExecution execution, String msg) {
		logger.error(msg);
		execution.setVariable(WORKFLOW_ACTION_ERROR_MESSAGE, msg);
		exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, msg);
	}
	
	public void handleRuntimeException (DelegateExecution execution){
		StringBuilder wfeExpMsg = new StringBuilder("Runtime error ");
		String runtimeErrorMessage = null;
		try{
			String javaExpMsg = (String) execution.getVariable("BPMN_javaExpMsg");
			if (javaExpMsg != null && !javaExpMsg.isEmpty()) {
				wfeExpMsg = wfeExpMsg.append(": ").append(javaExpMsg);
			}
			runtimeErrorMessage = wfeExpMsg.toString();
			logger.error(runtimeErrorMessage);
			execution.setVariable(WORKFLOW_ACTION_ERROR_MESSAGE, runtimeErrorMessage);
		} catch (Exception e){
			logger.error("Runtime error", e);
			//if runtime message was mulformed
			runtimeErrorMessage = "Runtime error";
		}
		exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, runtimeErrorMessage);
	}
}
