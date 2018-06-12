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

package org.openecomp.mso.bpmn.infrastructure.workflow.tasks;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Triple;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.javatuples.Pair;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.VolumeGroup;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowCallbackResponse;
import org.openecomp.mso.bpmn.common.workflow.service.WorkflowContextHolder;
import org.openecomp.mso.bpmn.core.WorkflowException;
import org.openecomp.mso.bpmn.servicedecomposition.entities.BuildingBlock;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.openecomp.mso.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.openecomp.mso.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.openecomp.mso.client.aai.AAICommonObjectMapperProvider;
import org.openecomp.mso.client.aai.AAIObjectType;
import org.openecomp.mso.client.aai.entities.AAIResultWrapper;
import org.openecomp.mso.client.aai.entities.Relationships;
import org.openecomp.mso.client.aai.entities.uri.AAIResourceUri;
import org.openecomp.mso.client.db.catalog.CatalogDbClient;
import org.openecomp.mso.client.db.request.RequestsDbClient;
import org.openecomp.mso.client.exception.ExceptionBuilder;
import org.openecomp.mso.client.orchestration.AAIServiceInstanceResources;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.db.catalog.beans.macro.NorthBoundRequest;
import org.openecomp.mso.db.catalog.beans.macro.OrchestrationFlow;
import org.openecomp.mso.db.request.beans.InfraActiveRequests;
import org.openecomp.mso.logger.MessageEnum;
import org.openecomp.mso.logger.MsoLogger;
import org.openecomp.mso.serviceinstancebeans.Networks;
import org.openecomp.mso.serviceinstancebeans.RequestDetails;
import org.openecomp.mso.serviceinstancebeans.RequestReferences;
import org.openecomp.mso.serviceinstancebeans.Service;
import org.openecomp.mso.serviceinstancebeans.ServiceInstancesRequest;
import org.openecomp.mso.serviceinstancebeans.ServiceInstancesResponse;
import org.openecomp.mso.serviceinstancebeans.VfModules;
import org.openecomp.mso.serviceinstancebeans.Vnfs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@Component
public class WorkflowAction {

	private static final String G_ORCHESTRATION_FLOW = "gOrchestrationFlow";
	private static final String G_ACTION = "requestAction";
	private static final String G_CURRENT_SEQUENCE = "gCurrentSequence";
	private static final String G_REQUEST_ID = "mso-request-id";
	private static final String G_BPMN_REQUEST = "bpmnRequest";
	private static final String G_ALACARTE = "aLaCarte";
	private static final String G_APIVERSION = "apiVersion";
	private static final String G_URI = "requestUri";
	private static final String SERVICE = "Service";
	private static final String VNF = "Vnf";
	private static final String VNF_TYPE = "vnfType";
	private static final String NETWORK = "Network";
	private static final String NETWORKCOLLECTION = "NetworkCollection";
	private static final String VOLUMEGROUP = "VolumeGroup";
	private static final String VFMODULE = "VfModule";
	private static final String G_ISTOPLEVELFLOW = "isTopLevelFlow";
	
	private static final String supportedTypes = "vnfs|vfModules|networks|networkCollections|volumeGroups|serviceInstances";


	@Autowired
	protected BBInputSetupUtils bbInputSetupUtils;
	@Autowired
	private ExceptionBuilder exceptionBuilder;
	@Autowired
	private CatalogDbClient catalogDbClient;
	
	@Autowired
	private RequestsDbClient requestDbclient;
	
	public void setBbInputSetupUtils(BBInputSetupUtils bbInputSetupUtils) {
		this.bbInputSetupUtils = bbInputSetupUtils;
	}

	private static final MsoLogger msoLogger = MsoLogger.getMsoLogger(MsoLogger.Catalog.BPEL, WorkflowAction.class);

	public void selectExecutionList(DelegateExecution execution) throws Exception {
		final String requestAction = (String) execution.getVariable(G_ACTION);
		final String requestId = (String) execution.getVariable(G_REQUEST_ID);
		final String bpmnRequest = (String) execution.getVariable(G_BPMN_REQUEST);
		final boolean aLaCarte = (boolean) execution.getVariable(G_ALACARTE);
		final String apiVersion = (String) execution.getVariable(G_APIVERSION);
		final String uri = (String) execution.getVariable(G_URI);
		final String vnfType = (String) execution.getVariable(VNF_TYPE);
		boolean vlanTagging = false;
		List<OrchestrationFlow> orchFlows = (List<OrchestrationFlow>) execution.getVariable(G_ORCHESTRATION_FLOW);
		List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
		WorkflowResourceIds workflowResourceIds = readResourceIdsFromExecution(execution);
		LinkedHashMap<String,Integer> resourceCounter = new LinkedHashMap<>();
		resourceCounter.put(SERVICE, 0);
		resourceCounter.put(VNF, 0);
		resourceCounter.put(NETWORK, 0);
		resourceCounter.put(NETWORKCOLLECTION, 0);
		resourceCounter.put(VFMODULE, 0);
		resourceCounter.put(VOLUMEGROUP, 0);
		execution.setVariable("sentSyncResponse", false);
		execution.setVariable("homing", false);
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			execution.setVariable(G_ISTOPLEVELFLOW, true);
			ServiceInstancesRequest sIRequest = mapper.readValue(bpmnRequest, ServiceInstancesRequest.class);
			Resource resource = extractResource(uri);
			String resourceName = resource.getResourceType();
			String resourceId = "";
			if(resource.isGenerated()){
				resourceId = determineResourceId(resource.getResourceId(), resourceName,sIRequest.getRequestDetails().getRequestInfo().getInstanceName(),sIRequest.getRequestDetails(), workflowResourceIds);
			}else{
				resourceId = resource.getResourceId();
			}
			execution.setVariable("resourceId", resourceId);
			execution.setVariable("resourceName", resourceName);
			
			if (aLaCarte) {
				if (orchFlows == null || orchFlows.isEmpty()) {
					orchFlows = queryReferenceData(execution, requestAction,resourceName,aLaCarte);
				}
				for (OrchestrationFlow orchFlow : orchFlows) {
					ExecuteBuildingBlock ebb = buildExecuteBuildingBlock(orchFlow, requestId, 0, apiVersion, resourceId, requestAction, aLaCarte, vnfType, workflowResourceIds);
					flowsToExecute.add(ebb);
				}
			}else{
				List<Map<String, Object>> userParams = sIRequest.getRequestDetails().getRequestParameters()
						.getUserParams();
				for (Map<String, Object> params : userParams) {
					if (params.containsKey("service")) {
						ObjectMapper obj = new ObjectMapper();
						String input = obj.writeValueAsString(params.get("service"));
						Service validate = obj.readValue(input, Service.class);
						resourceCounter.put(SERVICE, resourceCounter.get(SERVICE).intValue()+1);
						if (validate.getResources().getVnfs() != null) {
							for (Vnfs vnf : validate.getResources().getVnfs()) {
								resourceCounter.put(VNF, resourceCounter.get(VNF).intValue()+1);
								if (vnf.getVfModules() != null) {
									for (VfModules vfModule : vnf.getVfModules()) {
										resourceCounter.put(VFMODULE, resourceCounter.get(VFMODULE).intValue()+1);					
										VfModuleCustomization vfModuleCustomization = catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID(vfModule.getModelInfo().getModelCustomizationUuid());
										if(vfModuleCustomization !=null && vfModuleCustomization.getVolumeHeatEnv() != null){
							 				resourceCounter.put(VOLUMEGROUP, resourceCounter.get(VOLUMEGROUP).intValue()+1);
							 			} 
									}
								}
							}
						}
						if (validate.getResources().getNetworks() != null) {
							for (Networks network : validate.getResources().getNetworks()) {
								resourceCounter.put(NETWORK, resourceCounter.get(NETWORK).intValue()+1);
							}
							//Custom logic for Vlan Tagging Service-Macro-Create, Service-Macro-Delete, NetworkCollection-Macro-Create, NetworkCollection-Macro-Delete
							if(resourceName.equals(SERVICE)&&aLaCarte == false &&(requestAction.equals("createInstance")||requestAction.equals("deleteInstance"))||resourceName.equals(NETWORKCOLLECTION)){
								org.openecomp.mso.db.catalog.beans.Service service = catalogDbClient.getServiceByID(sIRequest.getRequestDetails().getModelInfo().getModelVersionId());
								if(service!=null){
									try{
										String toscaNodeType = service.getCollectionResourceCustomization().getCollectionResource().getToscaNodeType();
										if(toscaNodeType.contains(NETWORKCOLLECTION)){
											vlanTagging = true;
											resourceCounter.put(NETWORKCOLLECTION, resourceCounter.get(NETWORKCOLLECTION).intValue()+1);
											break;
										}
									}catch (Exception ex){
										msoLogger.debug("Did not find a NetworkCollection associated with this service");
									}
								}								
							}
						}
					}
				}
			
				msoLogger.info("Found Service Instances: " + resourceCounter.get(SERVICE).intValue() + "      VNFs: " + resourceCounter.get(VNF).intValue()
						+ "      Networks: " + resourceCounter.get(NETWORK).intValue() + "      VfModules: " + resourceCounter.get(VFMODULE).intValue()
						+ "      VolumeGroups: " + resourceCounter.get(VOLUMEGROUP).intValue() + "     networkCollections: " + resourceCounter.get(NETWORKCOLLECTION).intValue());
				if (orchFlows == null || orchFlows.isEmpty()) {
					orchFlows = queryReferenceData(execution, requestAction,resourceName,aLaCarte);
				}
				flowsToExecute = buildFlowExecutionList(orchFlows, resourceCounter, requestId, apiVersion, resourceId, resourceName, requestAction, aLaCarte, vnfType, workflowResourceIds);
				if(vlanTagging == true){
					msoLogger.info("Sorting for Vlan Tagging");
					flowsToExecute = sortByCompleteResourceObject(flowsToExecute,requestAction);
				}
				if(resourceName.equals("Service")&&(requestAction.equals("createInstance")||requestAction.equals("assignInstance"))&&resourceCounter.get(VNF)>0){
					execution.setVariable("homing", true);
				}
			}

			if (flowsToExecute.size() == 0) {
				throw new IllegalStateException("Macro did not come up with a valid execution path.");
			}
			
			msoLogger.info("List of BuildingBlocks to execute:");
			for(ExecuteBuildingBlock ebb : flowsToExecute){
				msoLogger.info(ebb.getBuildingBlock().getBpmnFlowName());
			}
			
			execution.setVariable(G_CURRENT_SEQUENCE, 0);
			execution.setVariable("retryCount", 0);
			execution.setVariable("flowsToExecute", flowsToExecute);

		} catch (Exception ex) {
			String msg = "Exception in create execution list " + ex.getMessage();
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(),
					MsoLogger.ErrorCode.UnknownError, msg, ex);
			execution.setVariable("gErrorInCreateArray", msg);
			execution.setVariable("WorkflowActionErrorMessage", msg);
			exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, msg);
		}
	}

	protected WorkflowResourceIds readResourceIdsFromExecution(DelegateExecution execution) {
		WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
		workflowResourceIds.setServiceInstanceId((String) execution.getVariable("serviceInstanceId"));
		workflowResourceIds.setNetworkId((String) execution.getVariable("networkId"));
		workflowResourceIds.setVfModuleId((String) execution.getVariable("vfModuleId"));
		workflowResourceIds.setVnfId((String) execution.getVariable("vnfId"));
		workflowResourceIds.setVolumeGroupId((String) execution.getVariable("volumeGroupId"));
		return workflowResourceIds;
	}

	protected Resource extractResource(String uri) {
		Pattern patt = Pattern.compile("[vV]\\d+.*?(?:(?:/(?<type>" + supportedTypes + ")(?:/(?<id>[^/]+))?)(?:/(?<action>[^/]+))?)?$");
		Matcher m = patt.matcher(uri);
		Boolean generated = false;
	
		if(m.find()) {
			msoLogger.debug("found match on " + uri + ": " + m);
			String type = m.group("type");
			String id = m.group("id");
			String action = m.group("action");
			if (type == null) {
				throw new IllegalArgumentException("Uri could not be parsed. No type found. " + uri);
			}
			if (action == null) {
				if (type.equals("serviceInstances") && (id == null || id.equals("assign"))) {
					id = UUID.randomUUID().toString();
					generated = true;
				}
			} else {
				if (action.matches(supportedTypes)) {
					type=action;
					id = UUID.randomUUID().toString();
					generated = true;
					type = action;
				}
			}
			return new Resource(convertTypeFromPlural(type), id, generated);
		} else {
			throw new IllegalArgumentException("Uri could not be parsed: " + uri);
		}
	}
	
	protected String determineResourceId(String generatedResourceId, String type, String instanceName, RequestDetails reqDetails, WorkflowResourceIds workflowResourceIds) throws Exception {
		try{
			if("SERVICE".equalsIgnoreCase(type)){ 
				String globalCustomerId = reqDetails.getSubscriberInfo().getGlobalSubscriberId();
				String serviceType = reqDetails.getRequestParameters().getSubscriptionServiceType();
				if (instanceName != null) {
					Optional<ServiceInstance> serviceInstanceAAI = bbInputSetupUtils
							.getAAIServiceInstanceByName(globalCustomerId, serviceType, instanceName);
					if (serviceInstanceAAI.isPresent()) {
						return serviceInstanceAAI.get().getServiceInstanceId();
					}
				}
			}else if("NETWORK".equalsIgnoreCase(type)){ 
				Optional<L3Network> network = bbInputSetupUtils.getRelatedNetworkByNameFromServiceInstance(workflowResourceIds.getServiceInstanceId(), instanceName);
				if(network.isPresent()){
					return network.get().getNetworkId();
				}
			}else if("VNF".equalsIgnoreCase(type)){	
				Optional<GenericVnf> vnf = bbInputSetupUtils.getRelatedVnfByNameFromServiceInstance(workflowResourceIds.getServiceInstanceId(), instanceName);
				if(vnf.isPresent()){
					return vnf.get().getVnfId();
				}
			}else if("VFMODULE".equalsIgnoreCase(type)){
				GenericVnf vnf = bbInputSetupUtils.getAAIGenericVnf(workflowResourceIds.getVnfId());
				if(vnf!=null && vnf.getVfModules()!=null){
					for(org.onap.aai.domain.yang.VfModule vfModule : vnf.getVfModules().getVfModule()){		
						if(vfModule.getVfModuleName().equalsIgnoreCase(instanceName)){	
							return vfModule.getVfModuleId();
						}	
					}
				}
			}else if("VOLUMEGROUP".equalsIgnoreCase(type)){
				Optional<VolumeGroup> volumeGroup = bbInputSetupUtils.getRelatedVolumeGroupByNameFromVnf(workflowResourceIds.getVnfId(), instanceName);
				if(volumeGroup.isPresent()){
					return volumeGroup.get().getVolumeGroupId();
				}
				GenericVnf vnf = bbInputSetupUtils.getAAIGenericVnf(workflowResourceIds.getVnfId());
				if(vnf!=null && vnf.getVfModules()!=null){
					for(org.onap.aai.domain.yang.VfModule vfModule : vnf.getVfModules().getVfModule()){		
						Optional<VolumeGroup> volumeGroupFromVfModule = bbInputSetupUtils.getRelatedVolumeGroupByNameFromVfModule(vfModule.getVfModuleId(), instanceName);
						if(volumeGroupFromVfModule.isPresent()){
							return volumeGroupFromVfModule.get().getVolumeGroupId();
						}
					}
				}
			}
			return generatedResourceId;
		} catch (Exception ex){
			throw new IllegalStateException("WorkflowAction was unable to verify if the instance name already exist in AAI.");
		}
	}
	
	protected String convertTypeFromPlural(String type) {
		if (!type.matches(supportedTypes)) {
			return type;
		} else {
			if (type.equals("serviceInstances")) {
				return SERVICE;
			} else {
				return type.substring(0, 1).toUpperCase() + type.substring(1, type.length()-1);
			}
		}
	}
	protected List<ExecuteBuildingBlock> sortByCompleteResourceObject(List<ExecuteBuildingBlock> orchFlows,String requestAction){
		List<ExecuteBuildingBlock> sortedOrchFlows = new ArrayList();
		int count = 0;
		if(requestAction.equals("createInstance")){
			for(ExecuteBuildingBlock ebb : orchFlows){
				if(ebb.getBuildingBlock().getBpmnFlowName().equals("AssignNetworkBB")){
					sortedOrchFlows.add(ebb);
					for(ExecuteBuildingBlock ebb2 : orchFlows){
						if(ebb2.getBuildingBlock().getBpmnFlowName().equals("CreateNetworkBB")&&ebb2.getBuildingBlock().getSequenceNumber()==count){
							sortedOrchFlows.add(ebb2);
						} else if(ebb2.getBuildingBlock().getBpmnFlowName().equals("ActivateNetworkBB")&&ebb2.getBuildingBlock().getSequenceNumber()==count){
							sortedOrchFlows.add(ebb2);
							break;
						}
					}
					count++;
				}else if(ebb.getBuildingBlock().getBpmnFlowName().equals("CreateNetworkBB")||ebb.getBuildingBlock().getBpmnFlowName().equals("ActivateNetworkBB")){
					continue;
				}else if(!ebb.getBuildingBlock().getBpmnFlowName().equals("")){
					sortedOrchFlows.add(ebb);
				}
			}
		}else if(requestAction.equals("deleteInstance")){
			for(ExecuteBuildingBlock ebb : orchFlows){
				if(ebb.getBuildingBlock().getBpmnFlowName().equals("DeactivateNetworkBB")){
					sortedOrchFlows.add(ebb);
					for(ExecuteBuildingBlock ebb2 : orchFlows){
						if(ebb2.getBuildingBlock().getBpmnFlowName().equals("DeleteNetworkBB")&&ebb2.getBuildingBlock().getSequenceNumber()==count){
							sortedOrchFlows.add(ebb2);
						} else if(ebb2.getBuildingBlock().getBpmnFlowName().equals("UnassignNetworkBB")&&ebb2.getBuildingBlock().getSequenceNumber()==count){
							sortedOrchFlows.add(ebb2);
							break;
						}
					}
					count++;
				}else if(ebb.getBuildingBlock().getBpmnFlowName().equals("DeleteNetworkBB")||ebb.getBuildingBlock().getBpmnFlowName().equals("UnassignNetworkBB")){
					continue;
				}else if(!ebb.getBuildingBlock().getBpmnFlowName().equals("")){
					sortedOrchFlows.add(ebb);
				}
			}
		}
		return sortedOrchFlows;
	}
	
	protected List<ExecuteBuildingBlock> buildFlowExecutionList(List<OrchestrationFlow> orchFlows,
			LinkedHashMap<String,Integer> resourceCounter, String requestId, String apiVersion, String resourceId, String resource, String requestAction, boolean aLaCarte, String vnfType, WorkflowResourceIds workflowResourceIds) {
		List<ExecuteBuildingBlock> flowsToExecute = new ArrayList<>();
		for (OrchestrationFlow orchFlow : orchFlows) {
			if (orchFlow.getFlowName().contains(SERVICE)) {
				for (int i = 0; i < resourceCounter.get(SERVICE).intValue(); i++) {
					flowsToExecute.add(buildExecuteBuildingBlock(orchFlow, requestId, i, apiVersion, resourceId, requestAction, aLaCarte, vnfType, workflowResourceIds));
				}
			} else if (orchFlow.getFlowName().contains(VNF)) {
				for (int i = 0; i < resourceCounter.get(VNF).intValue(); i++) {
					flowsToExecute.add(buildExecuteBuildingBlock(orchFlow, requestId, i, apiVersion, resourceId, requestAction, aLaCarte, vnfType, workflowResourceIds));
				}
			} else if (orchFlow.getFlowName().contains(NETWORK)
					&& !orchFlow.getFlowName().contains(NETWORKCOLLECTION)) {
				for (int i = 0; i < resourceCounter.get(NETWORK).intValue(); i++) {
					flowsToExecute.add(buildExecuteBuildingBlock(orchFlow, requestId, i, apiVersion, resourceId, requestAction, aLaCarte, vnfType, workflowResourceIds));
				}
			} else if (orchFlow.getFlowName().contains(VFMODULE)) {
				for (int i = 0; i < resourceCounter.get(VFMODULE).intValue(); i++) {
					flowsToExecute.add(buildExecuteBuildingBlock(orchFlow, requestId, i, apiVersion, resourceId, requestAction, aLaCarte, vnfType, workflowResourceIds));
				}
			} else if (orchFlow.getFlowName().contains(VOLUMEGROUP)) {
				for (int i = 0; i < resourceCounter.get(VOLUMEGROUP).intValue(); i++) {
					flowsToExecute.add(buildExecuteBuildingBlock(orchFlow, requestId, i, apiVersion, resourceId, requestAction, aLaCarte, vnfType, workflowResourceIds));
				}
			}else if (orchFlow.getFlowName().contains(NETWORKCOLLECTION)) {
				for (int i = 0; i < resourceCounter.get(NETWORKCOLLECTION).intValue(); i++) {
					flowsToExecute.add(buildExecuteBuildingBlock(orchFlow, requestId, i, apiVersion, resourceId, requestAction, aLaCarte, vnfType, workflowResourceIds));
					break;
				}
			}else{
				flowsToExecute.add(buildExecuteBuildingBlock(orchFlow,requestId,0,apiVersion, resourceId, requestAction, aLaCarte, vnfType, workflowResourceIds));
			}
		}
		return flowsToExecute;
	}

	protected ExecuteBuildingBlock buildExecuteBuildingBlock(OrchestrationFlow orchFlow, String requestId,
			int count, String apiVersion, String resourceId, String requestAction, boolean aLaCarte, String vnfType, WorkflowResourceIds workflowResourceIds) {
		ExecuteBuildingBlock executeBuildingBlock = new ExecuteBuildingBlock();
		BuildingBlock buildingBlock = new BuildingBlock();
		buildingBlock.setBpmnFlowName(orchFlow.getFlowName());
		buildingBlock.setMsoId(UUID.randomUUID().toString());
		buildingBlock.setSequenceNumber(count);
		executeBuildingBlock.setApiVersion(apiVersion);
		executeBuildingBlock.setaLaCarte(aLaCarte);
		executeBuildingBlock.setRequestAction(requestAction);
		executeBuildingBlock.setResourceId(resourceId);
		executeBuildingBlock.setVnfType(vnfType);
		executeBuildingBlock.setWorkflowResourceIds(workflowResourceIds);
		executeBuildingBlock.setRequestId(requestId);
		executeBuildingBlock.setBuildingBlock(buildingBlock);
		return executeBuildingBlock;
	}

	protected List<OrchestrationFlow> queryReferenceData(DelegateExecution execution, String requestAction, String resourceName,boolean aLaCarte) throws MalformedURLException {
		NorthBoundRequest northBoundRequest = catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(requestAction,resourceName,aLaCarte);
		execution.setVariable(G_ISTOPLEVELFLOW, northBoundRequest.getIsToplevelflow());
		List<OrchestrationFlow> flows = northBoundRequest.getOrchestrationFlowList();
		List<OrchestrationFlow> listToExecute = new ArrayList();
		if (flows == null)
			flows = new ArrayList<>();
		for(OrchestrationFlow flow : flows){
			if(!flow.getFlowName().contains("BB")){
				List<OrchestrationFlow> macroQueryFlows = catalogDbClient.getOrchestrationFlowByAction(flow.getFlowName());
				for(OrchestrationFlow macroFlow : macroQueryFlows){
					listToExecute.add(macroFlow);
				}
			}else{
				listToExecute.add(flow);
			}
		}
		return listToExecute;
	}

	public void selectBB(DelegateExecution execution) {
		List<ExecuteBuildingBlock> flowsToExecute = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		execution.setVariable("MacroRollback", false);
		int currentSequence = (int) execution.getVariable(G_CURRENT_SEQUENCE);
		ExecuteBuildingBlock ebb = flowsToExecute.get(currentSequence);
		if((boolean) execution.getVariable("homing")){
			if(ebb.getBuildingBlock().getBpmnFlowName().equals("AssignVnfBB")&&ebb.getBuildingBlock().getSequenceNumber()==0){
				ebb.setHoming(true);
			}
		}else{
			ebb.setHoming(false);
		}
		execution.setVariable("buildingBlock", ebb);
		currentSequence++;
		if (currentSequence >= flowsToExecute.size()) {
			execution.setVariable("completed", true);
		} else {
			execution.setVariable("completed", false);
			execution.setVariable(G_CURRENT_SEQUENCE, currentSequence);
		}
	}

	public void sendSyncAck(DelegateExecution execution) {
		final String requestId = (String) execution.getVariable(G_REQUEST_ID);
		final String resourceId= (String)execution.getVariable("resourceId");
		ServiceInstancesResponse serviceInstancesResponse = new ServiceInstancesResponse();
		RequestReferences requestRef = new RequestReferences();
		requestRef.setInstanceId(resourceId);
		requestRef.setRequestId(requestId);
		serviceInstancesResponse.setRequestReferences(requestRef);
		ObjectMapper mapper = new ObjectMapper();
		String jsonRequest = "";
		try {
			jsonRequest = mapper.writeValueAsString(serviceInstancesResponse);
		} catch (JsonProcessingException e) {
			String msg = "Could not marshall ServiceInstancesRequest to Json string to respond to API Handler ";
			msoLogger.error(MessageEnum.BPMN_GENERAL_EXCEPTION_ARG, msg, "BPMN", MsoLogger.getServiceName(),
					MsoLogger.ErrorCode.UnknownError, msg);
			execution.setVariable("WorkflowActionErrorMessage", msg);
			exceptionBuilder.buildAndThrowWorkflowException(execution, 7000, msg);
		}
		WorkflowCallbackResponse callbackResponse = new WorkflowCallbackResponse();
		callbackResponse.setStatusCode(200);
		callbackResponse.setMessage("Success");
		callbackResponse.setResponse(jsonRequest);
		String processKey = execution.getProcessEngineServices().getRepositoryService()
				.getProcessDefinition(execution.getProcessDefinitionId()).getKey();
		WorkflowContextHolder.getInstance().processCallback(processKey, execution.getProcessInstanceId(), requestId,
				callbackResponse);
		msoLogger.info("Successfully sent sync ack.");
	}
	
	public void sendErrorSyncAck(DelegateExecution execution){
		final String requestId = (String) execution.getVariable(G_REQUEST_ID);
		try {
			ExceptionBuilder exceptionBuilder = new ExceptionBuilder();
			String errorMsg = (String) execution.getVariable("WorkflowActionErrorMessage");
			if(errorMsg==null){
				errorMsg = "WorkflowAction failed unexpectedly.";
			}
			String processKey = exceptionBuilder.getProcessKey(execution);
			String buildworkflowException =
					"<aetgt:WorkflowException xmlns:aetgt=\"http://org.openecomp/mso/workflow/schema/v1\"><aetgt:ErrorMessage>"+ errorMsg + "</aetgt:ErrorMessage><aetgt:ErrorCode>7000</aetgt:ErrorCode></aetgt:WorkflowException>";
			WorkflowCallbackResponse callbackResponse = new WorkflowCallbackResponse();
			callbackResponse.setStatusCode(500);
			callbackResponse.setMessage("Fail");
			callbackResponse.setResponse(buildworkflowException);
			WorkflowContextHolder.getInstance().processCallback(processKey, execution.getProcessInstanceId(), requestId, callbackResponse);
			execution.setVariable("sentSyncResponse", true);
		} catch (Exception ex) {
			msoLogger.debug(" Sending Sync Error Activity Failed. " + "\n" + ex.getMessage());
		}
	}

	public void setupCompleteMsoProcess(DelegateExecution execution) {
		final String requestId = (String) execution.getVariable(G_REQUEST_ID);
		final String action = (String) execution.getVariable(G_ACTION);
		final String resourceId = (String)execution.getVariable("resourceId");
		final boolean aLaCarte = (boolean)execution.getVariable(G_ALACARTE);
		final String resourceName = (String)execution.getVariable("resourceName");
		final String source = (String) execution.getVariable("source");
		String macroAction = "";
		if(aLaCarte){
			macroAction = "ALaCarte-" + resourceName + "-" + action;
		}else{
			macroAction = "Macro-" + resourceName + "-" + action;
		}
		String msoCompletionRequest =  "<aetgt:MsoCompletionRequest xmlns:aetgt=\"http://org.openecomp/mso/workflow/schema/v1\" xmlns:ns=\"http://org.openecomp/mso/request/types/v1\"><request-info xmlns=\"http://org.openecomp/mso/infra/vnf-request/v1\"><request-id>"+requestId+"</request-id><action>"+action+"</action><source>"+source+"</source></request-info><status-message>" + macroAction + " request was executed correctly.</status-message><serviceInstanceId>"+resourceId+"</serviceInstanceId><mso-bpel-name>WorkflowActionBB</mso-bpel-name></aetgt:MsoCompletionRequest>";
		execution.setVariable("CompleteMsoProcessRequest", msoCompletionRequest);
		execution.setVariable("mso-request-id", requestId);
		execution.setVariable("mso-service-instance-id", resourceId);
	}
	
	public void setupFalloutHandler(DelegateExecution execution){
		final String requestId = (String) execution.getVariable(G_REQUEST_ID);
		final String action = (String) execution.getVariable(G_ACTION);
		final String resourceId = (String) execution.getVariable("resourceId");
		String exceptionMsg = "";
		if (execution.getVariable("WorkflowActionErrorMessage") != null) {
			exceptionMsg = (String) execution.getVariable("WorkflowActionErrorMessage");
		} else {
			exceptionMsg = "Error in WorkflowAction";
		}
		execution.setVariable("mso-service-instance-id", resourceId);
		execution.setVariable("mso-request-id", requestId);
		String falloutRequest = "<aetgt:FalloutHandlerRequest xmlns:aetgt=\"http://org.openecomp/mso/workflow/schema/v1\"xmlns:ns=\"http://org.openecomp/mso/request/types/v1\"xmlns:wfsch=\"http://org.openecomp/mso/workflow/schema/v1\"><request-info xmlns=\"http://org.openecomp/mso/infra/vnf-request/v1\"><request-id>"+ requestId +"</request-id><action>"+ action +"</action><source>VID</source></request-info><aetgt:WorkflowException xmlns:aetgt=\"http://org.openecomp/mso/workflow/schema/v1\"><aetgt:ErrorMessage>"+ exceptionMsg + "</aetgt:ErrorMessage><aetgt:ErrorCode>7000</aetgt:ErrorCode></aetgt:WorkflowException></aetgt:FalloutHandlerRequest>";

		execution.setVariable("falloutRequest", falloutRequest);
	}
	
	public void checkRetryStatus(DelegateExecution execution){
		if(execution.getVariable("handlingCode")=="Retry"){
			int currSequence = (int)execution.getVariable("gCurrentSequence");
			currSequence--;
			execution.setVariable("gCurrentSequence",currSequence);
			int currRetryCount = (int)execution.getVariable("retryCount");
			currRetryCount++;
			execution.setVariable("retryCount", currRetryCount);
		}
	}
	
	/**
	 * Rollback will only handle Create/Activate/Assign Macro flows.
	 * Execute layer will rollback the flow its currently working on.
	 */
	public void rollbackExecutionPath(DelegateExecution execution){
		List<ExecuteBuildingBlock> flowsToExecute = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		List<ExecuteBuildingBlock> rollbackFlows = new ArrayList();
		int currentSequence = (int) execution.getVariable(G_CURRENT_SEQUENCE) - 1;
		for(int i = flowsToExecute.size()-1; i>=0;i--){
			if(i>=currentSequence){
				flowsToExecute.remove(i);
			}else{
				ExecuteBuildingBlock ebb = flowsToExecute.get(i);
				BuildingBlock bb = flowsToExecute.get(i).getBuildingBlock();
				String flowName = flowsToExecute.get(i).getBuildingBlock().getBpmnFlowName();
				if(flowName.contains("Assign")){
					flowName = "Unassign" + flowName.substring(7,flowName.length());
				}else if (flowName.contains("Create")){
					flowName = "Delete" + flowName.substring(6,flowName.length());
				}else if (flowName.contains("Activate")){
					flowName = "Deactivate" + flowName.substring(8,flowName.length());
				}
				flowsToExecute.get(i).getBuildingBlock().setBpmnFlowName(flowName);
				rollbackFlows.add(flowsToExecute.get(i));
			}
		}
		if(rollbackFlows.isEmpty())
			execution.setVariable("isRollbackNeeded", false);
		else
			execution.setVariable("isRollbackNeeded", true);
		
		execution.setVariable("flowsToExecute", rollbackFlows);
		execution.setVariable("handlingCode", "PreformingRollback");
		
	}
	
	public void abortCallErrorHandling(DelegateExecution execution) {
		String msg = "Flow has failed. Rainy day handler has decided to abort the process.";
		Exception exception = new Exception(msg);
		msoLogger.error(exception);
		throw new BpmnError(msg);
	}
	
	public void updateRequestStatusToFailed (DelegateExecution execution){
		try {
			String requestId = (String) execution.getVariable(G_REQUEST_ID);
			InfraActiveRequests request = requestDbclient.getInfraActiveRequestbyRequestId(requestId);
			try{
				WorkflowException exception = (WorkflowException) execution.getVariable("WorkflowException");
				request.setStatusMessage(exception.getErrorMessage());
			} catch (Exception ex){
				request.setStatusMessage("Unexpected Error in BPMN");
			}
			request.setRequestStatus("FAILED");
			request.setLastModifiedBy("CamundaBPMN");
			requestDbclient.updateInfraActiveRequests(request);
		} catch (Exception e) {
			msoLogger.error(e);
			throw new BpmnError("Error Updating Request Database");
		}
	}
}
