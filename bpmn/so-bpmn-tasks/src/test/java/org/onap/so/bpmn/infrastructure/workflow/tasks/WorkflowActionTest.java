/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.Relationship;
import org.onap.aai.domain.yang.RelationshipList;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.VfModules;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Collection;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Configuration;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.onap.so.client.aai.entities.Relationships;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResource;
import org.onap.so.db.catalog.beans.CollectionResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.ConfigurationResource;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.HeatEnvironment;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.InstanceGroup;
import org.onap.so.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.VnfVfmoduleCvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.macro.NorthBoundRequest;
import org.onap.so.db.catalog.beans.macro.OrchestrationFlow;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.SubscriberInfo;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WorkflowActionTest extends BaseTaskTest {
	
	
	@Mock
	protected Environment environment;
	@InjectMocks
	protected WorkflowAction workflowAction;
	private DelegateExecution execution;

	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Before
	public void before() throws Exception {
		execution = new DelegateExecutionFake();
		org.onap.aai.domain.yang.ServiceInstance servInstance = new org.onap.aai.domain.yang.ServiceInstance();
		servInstance.setServiceInstanceId("TEST");
		when(bbSetupUtils.getAAIServiceInstanceByName(anyString(), anyObject())).thenReturn(servInstance);
		workflowAction.setBbInputSetupUtils(bbSetupUtils);
		workflowAction.setBbInputSetup(bbInputSetup);
	}
	/**
	 * ALACARTE TESTS
	 */
	@Test
	public void selectExecutionListALaCarteNetworkCreateTest() throws Exception{
		String gAction = "createInstance";
		String resource = "Network";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroAssign.json")));
		execution.setVariable("bpmnRequest", bpmnRequest);		
		execution.setVariable("aLaCarte", true);
		execution.setVariable("apiVersion", "7");
		execution.setVariable("requestUri", "v6/networks/123");
		
		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		List<OrchestrationFlow> orchFlows = createFlowList("AssignNetwork1802BB","CreateNetworkBB","ActivateNetworkBB");
		northBoundRequest.setOrchestrationFlowList(orchFlows);

		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction,resource,true, "my-custom-cloud-owner")).thenReturn(northBoundRequest);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEqualsBulkFlowName(ebbs,"AssignNetwork1802BB","CreateNetworkBB","ActivateNetworkBB");
	}
	
	@Test
	public void selectExecutionListALaCarteNetworkDeleteTest() throws Exception{
		String gAction = "deleteInstance";
		String resource = "Network";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroAssign.json")));
		execution.setVariable("bpmnRequest", bpmnRequest);		
		execution.setVariable("aLaCarte", true);
		execution.setVariable("apiVersion", "7");
		execution.setVariable("requestUri", "v6/networks/123");
		
		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		List<OrchestrationFlow> orchFlows = createFlowList("DeactivateNetworkBB","DeleteNetworkBB","UnassignNetwork1802BB");
		northBoundRequest.setOrchestrationFlowList(orchFlows);
		
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction,resource,true,"my-custom-cloud-owner")).thenReturn(northBoundRequest);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEqualsBulkFlowName(ebbs,"DeactivateNetworkBB","DeleteNetworkBB","UnassignNetwork1802BB");
	}
	
	@Test
	public void selectExecutionListALaCarteServiceCreateTest() throws Exception{
		String gAction = "createInstance";
		String resource = "Service";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroAssign.json")));
		execution.setVariable("bpmnRequest", bpmnRequest);		
		execution.setVariable("aLaCarte", true);
		execution.setVariable("apiVersion", "7");
		execution.setVariable("requestUri", "v6/serviceInstances/123");
		
		
		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		List<OrchestrationFlow> orchFlows = createFlowList("AssignServiceInstanceBB","ActivateServiceInstanceBB");
		northBoundRequest.setOrchestrationFlowList(orchFlows);	
		
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction,resource,true,"my-custom-cloud-owner")).thenReturn(northBoundRequest);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEqualsBulkFlowName(ebbs,"AssignServiceInstanceBB","ActivateServiceInstanceBB");
	}
	
	/**
	 * SERVICE MACRO TESTS
	 */
	@Test
	public void selectExecutionListServiceMacroAssignTest() throws Exception{
		String gAction = "assignInstance";
		String resource = "Service";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroAssign.json")));
		execution.setVariable("bpmnRequest", bpmnRequest);
		execution.setVariable("aLaCarte", false);
		execution.setVariable("apiVersion", "7");
		execution.setVariable("requestUri", "v6/serviceInstances/123");
		
		
		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		List<OrchestrationFlow> orchFlows = createFlowList("AssignServiceInstanceBB","AssignNetworkBB","AssignVnfBB","AssignVolumeGroupBB","AssignVfModuleBB");
		northBoundRequest.setOrchestrationFlowList(orchFlows);
		
		VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
		vfModuleCustomization.setModelCustomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
		HeatEnvironment volumeHeatEnv = new HeatEnvironment();
		vfModuleCustomization.setVolumeHeatEnv(volumeHeatEnv);
		org.onap.so.db.catalog.beans.VfModule vfModule = new org.onap.so.db.catalog.beans.VfModule();
		HeatTemplate volumeHeatTemplate = new HeatTemplate();
		vfModule.setVolumeHeatTemplate(volumeHeatTemplate);
		vfModuleCustomization.setVfModule(vfModule);
		
		VfModuleCustomization vfModuleCustomization2 = new VfModuleCustomization();
		vfModuleCustomization2.setModelCustomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");
		HeatEnvironment heatEnvironment = new HeatEnvironment();
		vfModuleCustomization2.setHeatEnvironment(heatEnvironment);
		org.onap.so.db.catalog.beans.VfModule vfModule2 = new org.onap.so.db.catalog.beans.VfModule();
		HeatTemplate moduleHeatTemplate = new HeatTemplate();
		vfModule2.setModuleHeatTemplate(moduleHeatTemplate);
		vfModuleCustomization2.setVfModule(vfModule2);
		
		VfModuleCustomization vfModuleCustomization3 = vfModuleCustomization2;
		vfModuleCustomization3.setModelCustomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");
		
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction,resource,false,"my-custom-cloud-owner")).thenReturn(northBoundRequest);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f")).thenReturn(vfModuleCustomization);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8")).thenReturn(vfModuleCustomization2);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("da4d4327-fb7d-4311-ac7a-be7ba60cf969")).thenReturn(vfModuleCustomization3);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEqualsBulkFlowName(ebbs,"AssignServiceInstanceBB","AssignVnfBB","AssignVolumeGroupBB","AssignVfModuleBB","AssignVfModuleBB","AssignVfModuleBB");
	}
	
	@Test
	public void selectExecutionListServiceMacroAssignNoCloudTest() throws Exception{
		String gAction = "assignInstance";
		String resource = "Service";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroAssignNoCloud.json")));
		execution.setVariable("bpmnRequest", bpmnRequest);
		execution.setVariable("aLaCarte", false);
		execution.setVariable("apiVersion", "7");
		execution.setVariable("requestUri", "v6/serviceInstances/123");
		
		
		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		List<OrchestrationFlow> orchFlows = createFlowList("AssignServiceInstanceBB","AssignNetworkBB","AssignVnfBB","AssignVolumeGroupBB","AssignVfModuleBB");
		northBoundRequest.setOrchestrationFlowList(orchFlows);
		
		VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
		vfModuleCustomization.setModelCustomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
		HeatEnvironment volumeHeatEnv = new HeatEnvironment();
		vfModuleCustomization.setVolumeHeatEnv(volumeHeatEnv);
		org.onap.so.db.catalog.beans.VfModule vfModule = new org.onap.so.db.catalog.beans.VfModule();
		HeatTemplate volumeHeatTemplate = new HeatTemplate();
		vfModule.setVolumeHeatTemplate(volumeHeatTemplate);
		vfModuleCustomization.setVfModule(vfModule);
		
		VfModuleCustomization vfModuleCustomization2 = new VfModuleCustomization();
		vfModuleCustomization2.setModelCustomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");
		HeatEnvironment heatEnvironment = new HeatEnvironment();
		vfModuleCustomization2.setHeatEnvironment(heatEnvironment);
		org.onap.so.db.catalog.beans.VfModule vfModule2 = new org.onap.so.db.catalog.beans.VfModule();
		HeatTemplate moduleHeatTemplate = new HeatTemplate();
		vfModule2.setModuleHeatTemplate(moduleHeatTemplate);
		vfModuleCustomization2.setVfModule(vfModule2);
		
		VfModuleCustomization vfModuleCustomization3 = vfModuleCustomization2;
		vfModuleCustomization3.setModelCustomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");
		
		when(environment.getProperty("org.onap.so.cloud-owner")).thenReturn("att-aic");
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction,resource,false,"att-aic")).thenReturn(northBoundRequest);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f")).thenReturn(vfModuleCustomization);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8")).thenReturn(vfModuleCustomization2);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("da4d4327-fb7d-4311-ac7a-be7ba60cf969")).thenReturn(vfModuleCustomization3);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEqualsBulkFlowName(ebbs,"AssignServiceInstanceBB","AssignVnfBB","AssignVolumeGroupBB","AssignVfModuleBB","AssignVfModuleBB","AssignVfModuleBB");
	}
	
	@Test
	public void selectExecutionListServiceMacroActivateTest() throws Exception{
		String gAction = "activateInstance";
		String resource = "Service";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroActivateDeleteUnassign.json")));
		execution.setVariable("bpmnRequest", bpmnRequest);		
		execution.setVariable("aLaCarte", false);
		execution.setVariable("apiVersion", "7");
		execution.setVariable("requestUri", "v6/serviceInstances/si0");
		

		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		List<OrchestrationFlow> orchFlows = createFlowList("CreateNetworkBB","ActivateNetworkBB","CreateVolumeGroupBB","ActivateVolumeGroupBB","CreateVfModuleBB","ActivateVfModuleBB"
				,"ActivateVnfBB","ActivateServiceInstanceBB");
		northBoundRequest.setOrchestrationFlowList(orchFlows);	
		
		ServiceInstance serviceInstanceAAI = new ServiceInstance();
		serviceInstanceAAI.setServiceInstanceId("si0");
		org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO = new org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance();
		org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf = new org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf();
		vnf.setVnfId("vnf0");
		
		org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule = new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
		vfModule.setVfModuleId("vfModule0");
		vnf.getVfModules().add(vfModule);
		org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule2 = new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
		vfModule2.setVfModuleId("vfModule1");
		vnf.getVfModules().add(vfModule2);
		
		org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup volumeGroup = new org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup();
		volumeGroup.setVolumeGroupId("volumeGroup0");
		vnf.getVolumeGroups().add(volumeGroup);
		
		serviceInstanceMSO.getVnfs().add(vnf);
		
		doReturn(serviceInstanceAAI).when(bbSetupUtils).getAAIServiceInstanceById("si0");
		doReturn(serviceInstanceMSO).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction,resource,false,"my-custom-cloud-owner")).thenReturn(northBoundRequest);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEqualsBulkFlowName(ebbs,"CreateVolumeGroupBB","ActivateVolumeGroupBB","CreateVfModuleBB","CreateVfModuleBB","ActivateVfModuleBB","ActivateVfModuleBB","ActivateVnfBB","ActivateServiceInstanceBB");
		assertEquals("volumeGroup0", ebbs.get(0).getWorkflowResourceIds().getVolumeGroupId());
		assertEquals("volumeGroup0", ebbs.get(1).getWorkflowResourceIds().getVolumeGroupId());
		assertEquals("vfModule0", ebbs.get(2).getWorkflowResourceIds().getVfModuleId());
		assertEquals("vfModule1", ebbs.get(3).getWorkflowResourceIds().getVfModuleId());
		assertEquals("vfModule0", ebbs.get(4).getWorkflowResourceIds().getVfModuleId());
		assertEquals("vfModule1", ebbs.get(5).getWorkflowResourceIds().getVfModuleId());
		assertEquals("vnf0", ebbs.get(6).getWorkflowResourceIds().getVnfId());
		assertEquals("si0", ebbs.get(7).getWorkflowResourceIds().getServiceInstanceId());

	}
	
	@Test
	public void selectExecutionListServiceMacroDeactivateTest() throws Exception{
		String gAction = "deactivateInstance";
		String resource = "Service";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroActivateDeleteUnassign.json")));
		execution.setVariable("bpmnRequest", bpmnRequest);		
		execution.setVariable("aLaCarte", false);
		execution.setVariable("apiVersion", "7");
		execution.setVariable("requestUri", "v6/serviceInstances/123");
		
		
		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		List<OrchestrationFlow> orchFlows = createFlowList("DeactivateServiceInstanceBB");
		northBoundRequest.setOrchestrationFlowList(orchFlows);	
		
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction,resource,false,"my-custom-cloud-owner")).thenReturn(northBoundRequest);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"DeactivateServiceInstanceBB");
	}

	@Test
	public void selectExecutionListServiceMacroEmptyServiceTest() throws Exception{
		String gAction = "createInstance";
		String resource = "Service";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroActivateDeleteUnassign.json")));
		execution.setVariable("bpmnRequest", bpmnRequest);			
		execution.setVariable("aLaCarte", false);
		execution.setVariable("apiVersion", "7");
		execution.setVariable("requestUri", "v6/serviceInstances/123");
		
		
		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		northBoundRequest.setIsToplevelflow(true);
		List<OrchestrationFlow> orchFlows = createFlowList("AssignServiceInstanceBB","CreateNetworkCollectionBB","AssignNetworkBB","AssignVnfBB","AssignVolumeGroupBB","AssignVfModuleBB"
				,"CreateNetworkBB","ActivateNetworkBB","CreateVolumeGroupBB","ActivateVolumeGroupBB","CreateVfModuleBB","ActivateVfModuleBB","AssignFabricConfigurationBB","ActivateFabricConfigurationBB"
				,"ActivateVnfBB","ActivateNetworkCollectionBB","ActivateServiceInstanceBB");
		northBoundRequest.setOrchestrationFlowList(orchFlows);			
		
		Service service = new Service();
		doReturn(service).when(catalogDbClient).getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a");
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction,resource,false,"my-custom-cloud-owner")).thenReturn(northBoundRequest);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEqualsBulkFlowName(ebbs,"AssignServiceInstanceBB","ActivateServiceInstanceBB");
	}
	
	@Test
	public void selectExecutionListServiceMacroCreateJustNetworkTest() throws Exception{
		String gAction = "createInstance";
		String resource = "Service";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroActivateDeleteUnassign.json")));
		execution.setVariable("bpmnRequest", bpmnRequest);			
		execution.setVariable("aLaCarte", false);
		execution.setVariable("apiVersion", "7");
		execution.setVariable("requestUri", "v6/serviceInstances/123");
		
		
		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		northBoundRequest.setIsToplevelflow(true);
		List<OrchestrationFlow> orchFlows = createFlowList("AssignServiceInstanceBB","CreateNetworkCollectionBB","AssignNetworkBB","AssignVnfBB","AssignVolumeGroupBB","AssignVfModuleBB"
				,"CreateNetworkBB","ActivateNetworkBB","CreateVolumeGroupBB","ActivateVolumeGroupBB","CreateVfModuleBB","ActivateVfModuleBB","AssignFabricConfigurationBB","ActivateFabricConfigurationBB"
				,"ActivateVnfBB","ActivateNetworkCollectionBB","ActivateServiceInstanceBB");
		northBoundRequest.setOrchestrationFlowList(orchFlows);			
		
		Service service = new Service();
		NetworkResourceCustomization networkCustomization = new NetworkResourceCustomization();
		networkCustomization.setModelCustomizationUUID("1234");
		service.getNetworkCustomizations().add(networkCustomization);
		doReturn(service).when(catalogDbClient).getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a");
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction,resource,false,"my-custom-cloud-owner")).thenReturn(northBoundRequest);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEqualsBulkFlowName(ebbs,"AssignServiceInstanceBB","AssignNetworkBB","CreateNetworkBB","ActivateNetworkBB","ActivateServiceInstanceBB");
	}
	
	@Test
	public void selectExecutionListServiceMacroCreateWithNetworkCollectionTest() throws Exception{
		String gAction = "createInstance";
		String resource = "Service";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroActivateDeleteUnassign.json")));
		execution.setVariable("bpmnRequest", bpmnRequest);			
		execution.setVariable("aLaCarte", false);
		execution.setVariable("apiVersion", "7");
		execution.setVariable("requestUri", "v6/serviceInstances/123");
		
		
		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		northBoundRequest.setIsToplevelflow(true);
		List<OrchestrationFlow> orchFlows = createFlowList("AssignServiceInstanceBB","CreateNetworkCollectionBB","AssignNetworkBB","AssignVnfBB","AssignVolumeGroupBB","AssignVfModuleBB"
				,"CreateNetworkBB","ActivateNetworkBB","CreateVolumeGroupBB","ActivateVolumeGroupBB","CreateVfModuleBB","ActivateVfModuleBB","AssignFabricConfigurationBB","ActivateFabricConfigurationBB"
				,"ActivateVnfBB","ActivateNetworkCollectionBB","ActivateServiceInstanceBB");
		northBoundRequest.setOrchestrationFlowList(orchFlows);			
		
		Service service = new Service();
		List<NetworkResourceCustomization> networkCustomizations = new ArrayList<>();
		NetworkResourceCustomization networkCust = new NetworkResourceCustomization();
		networkCust.setModelCustomizationUUID("123");
		networkCustomizations.add(networkCust);
		service.setNetworkCustomizations(networkCustomizations);
		NetworkCollectionResourceCustomization collectionResourceCustomization = new NetworkCollectionResourceCustomization();
		collectionResourceCustomization.setModelCustomizationUUID("123");
		
		CollectionResource collectionResource = new CollectionResource();
		collectionResource.setToscaNodeType("NetworkCollection");
		InstanceGroup instanceGroup = new InstanceGroup();
		instanceGroup.setToscaNodeType("NetworkCollectionResource");
		instanceGroup.setCollectionNetworkResourceCustomizations(new ArrayList<>());
		CollectionNetworkResourceCustomization collectionNetworkResourceCust = new CollectionNetworkResourceCustomization();
		collectionNetworkResourceCust.setModelCustomizationUUID("123");
		collectionNetworkResourceCust.setNetworkResourceCustomization(collectionResourceCustomization);
		instanceGroup.getCollectionNetworkResourceCustomizations().add(collectionNetworkResourceCust );
		List<CollectionResourceInstanceGroupCustomization> collectionInstanceGroupCustomizations = new ArrayList<>();
		CollectionResourceInstanceGroupCustomization collectionInstanceGroupCustomization = new CollectionResourceInstanceGroupCustomization();
		collectionInstanceGroupCustomization.setModelCustomizationUUID("123");
		collectionInstanceGroupCustomization.setSubInterfaceNetworkQuantity(3);
		collectionInstanceGroupCustomizations.add(collectionInstanceGroupCustomization);
		collectionInstanceGroupCustomization.setInstanceGroup(instanceGroup);
		collectionInstanceGroupCustomization.setCollectionResourceCust(collectionResourceCustomization);
		instanceGroup.setCollectionInstanceGroupCustomizations(collectionInstanceGroupCustomizations);
		collectionResource.setInstanceGroup(instanceGroup);
		collectionResourceCustomization.setCollectionResource(collectionResource);;
		service.getCollectionResourceCustomizations().add(collectionResourceCustomization);
		doReturn(service).when(catalogDbClient).getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a");
		doReturn(collectionResourceCustomization).when(catalogDbClient).getNetworkCollectionResourceCustomizationByID("123");
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction,resource,false,"my-custom-cloud-owner")).thenReturn(northBoundRequest);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEqualsBulkFlowName(ebbs,"AssignServiceInstanceBB","CreateNetworkCollectionBB","AssignNetworkBB","CreateNetworkBB","ActivateNetworkBB","AssignNetworkBB","CreateNetworkBB","ActivateNetworkBB"
				,"AssignNetworkBB","CreateNetworkBB","ActivateNetworkBB","ActivateNetworkCollectionBB","ActivateServiceInstanceBB");
		assertEquals("Network id not empty", !ebbs.get(2).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
		assertEquals("Network id not empty", !ebbs.get(3).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
		assertEquals("Network id not empty", !ebbs.get(4).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
		assertEquals("Network id same for AssignNetworkBB CreateNetworkBB ActivateNetworkBB",
				ebbs.get(2).getWorkflowResourceIds().getNetworkId() == ebbs.get(3).getWorkflowResourceIds().getNetworkId() 
				&& ebbs.get(3).getWorkflowResourceIds().getNetworkId() == ebbs.get(4).getWorkflowResourceIds().getNetworkId(), true);
		assertEquals("Network id not empty", !ebbs.get(5).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
		assertEquals("Network id not empty", !ebbs.get(6).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
		assertEquals("Network id not empty", !ebbs.get(7).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
		assertEquals("Network id same for AssignNetworkBB CreateNetworkBB ActivateNetworkBB",
				ebbs.get(5).getWorkflowResourceIds().getNetworkId() == ebbs.get(6).getWorkflowResourceIds().getNetworkId() 
				&& ebbs.get(6).getWorkflowResourceIds().getNetworkId() == ebbs.get(7).getWorkflowResourceIds().getNetworkId(), true);
		assertEquals("Network id not empty", !ebbs.get(8).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
		assertEquals("Network id not empty", !ebbs.get(9).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
		assertEquals("Network id not empty", !ebbs.get(10).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
		assertEquals("Network id same for AssignNetworkBB CreateNetworkBB ActivateNetworkBB",
				ebbs.get(8).getWorkflowResourceIds().getNetworkId() == ebbs.get(9).getWorkflowResourceIds().getNetworkId() 
				&& ebbs.get(9).getWorkflowResourceIds().getNetworkId() == ebbs.get(10).getWorkflowResourceIds().getNetworkId(), true);
	}

	@Test
	public void selectExecutionListServiceMacroCreateWithUserParams() throws Exception{
		String gAction = "createInstance";
		String resource = "Service";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroAssign.json")));
		execution.setVariable("bpmnRequest", bpmnRequest);			
		execution.setVariable("aLaCarte", false);
		execution.setVariable("apiVersion", "7");
		execution.setVariable("requestUri", "v6/serviceInstances/123");
		
		
		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		List<OrchestrationFlow> orchFlows = createFlowList("AssignServiceInstanceBB","CreateNetworkCollectionBB","AssignNetworkBB","AssignVnfBB","AssignVolumeGroupBB","AssignVfModuleBB"
				,"CreateNetworkBB","ActivateNetworkBB","CreateVolumeGroupBB","ActivateVolumeGroupBB","CreateVfModuleBB","ActivateVfModuleBB","AssignFabricConfigurationBB","ActivateFabricConfigurationBB"
				,"ActivateVnfBB","ActivateNetworkCollectionBB","ActivateServiceInstanceBB");
		northBoundRequest.setOrchestrationFlowList(orchFlows);			
		
		Service service = new Service();
		service.setModelUUID("3c40d244-808e-42ca-b09a-256d83d19d0a");
		
		VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
		vfModuleCustomization.setModelCustomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
		HeatEnvironment volumeHeatEnv = new HeatEnvironment();
		vfModuleCustomization.setVolumeHeatEnv(volumeHeatEnv);
		org.onap.so.db.catalog.beans.VfModule vfModule = new org.onap.so.db.catalog.beans.VfModule();
		HeatTemplate volumeHeatTemplate = new HeatTemplate();
		vfModule.setVolumeHeatTemplate(volumeHeatTemplate);
		vfModuleCustomization.setVfModule(vfModule);
		
		VfModuleCustomization vfModuleCustomization2 = new VfModuleCustomization();
		vfModuleCustomization2.setModelCustomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");
		HeatEnvironment heatEnvironment = new HeatEnvironment();
		vfModuleCustomization2.setHeatEnvironment(heatEnvironment);
		org.onap.so.db.catalog.beans.VfModule vfModule2 = new org.onap.so.db.catalog.beans.VfModule();
		HeatTemplate moduleHeatTemplate = new HeatTemplate();
		vfModule2.setModuleHeatTemplate(moduleHeatTemplate);
		vfModuleCustomization2.setVfModule(vfModule2);
		
		VfModuleCustomization vfModuleCustomization3 = vfModuleCustomization2;
		vfModuleCustomization3.setModelCustomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");
		
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction,resource,false,"my-custom-cloud-owner")).thenReturn(northBoundRequest);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f")).thenReturn(vfModuleCustomization);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8")).thenReturn(vfModuleCustomization2);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("da4d4327-fb7d-4311-ac7a-be7ba60cf969")).thenReturn(vfModuleCustomization3);
		when(catalogDbClient.getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a")).thenReturn(service);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEqualsBulkFlowName(ebbs,"AssignServiceInstanceBB","AssignVnfBB","AssignVolumeGroupBB","AssignVfModuleBB","AssignVfModuleBB","AssignVfModuleBB","CreateVolumeGroupBB"
				,"ActivateVolumeGroupBB","CreateVfModuleBB","CreateVfModuleBB","CreateVfModuleBB","ActivateVfModuleBB","ActivateVfModuleBB","ActivateVfModuleBB","ActivateVnfBB","ActivateServiceInstanceBB");
		assertEquals(3,ebbs.get(0).getWorkflowResourceIds().getServiceInstanceId().length());
		int randomUUIDLength = UUID.randomUUID().toString().length();
		assertEquals(randomUUIDLength,ebbs.get(1).getWorkflowResourceIds().getVnfId().length());
		assertEquals(randomUUIDLength,ebbs.get(2).getWorkflowResourceIds().getVolumeGroupId().length());
		assertEquals(randomUUIDLength,ebbs.get(3).getWorkflowResourceIds().getVfModuleId().length());
		assertEquals(randomUUIDLength,ebbs.get(4).getWorkflowResourceIds().getVfModuleId().length());
		assertEquals(randomUUIDLength,ebbs.get(5).getWorkflowResourceIds().getVfModuleId().length());
		assertEquals(randomUUIDLength,ebbs.get(6).getWorkflowResourceIds().getVolumeGroupId().length());
		assertEquals(randomUUIDLength,ebbs.get(7).getWorkflowResourceIds().getVolumeGroupId().length());
		assertEquals(randomUUIDLength,ebbs.get(8).getWorkflowResourceIds().getVfModuleId().length());
		assertEquals(randomUUIDLength,ebbs.get(9).getWorkflowResourceIds().getVfModuleId().length());
		assertEquals(randomUUIDLength,ebbs.get(10).getWorkflowResourceIds().getVfModuleId().length());
		assertEquals(randomUUIDLength,ebbs.get(11).getWorkflowResourceIds().getVfModuleId().length());
		assertEquals(randomUUIDLength,ebbs.get(12).getWorkflowResourceIds().getVfModuleId().length());
		assertEquals(randomUUIDLength,ebbs.get(13).getWorkflowResourceIds().getVfModuleId().length());
		assertEquals(randomUUIDLength,ebbs.get(14).getWorkflowResourceIds().getVnfId().length());
		assertEquals(3,ebbs.get(0).getWorkflowResourceIds().getServiceInstanceId().length());
		assertEquals(true, execution.getVariable("homing"));
	}
	
	@Test
	public void selectExecutionListServiceMacroDeleteTest() throws Exception{
		String gAction = "deleteInstance";
		String resource = "Service";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroActivateDeleteUnassign.json")));
		execution.setVariable("bpmnRequest", bpmnRequest);		
		execution.setVariable("aLaCarte", false);
		execution.setVariable("apiVersion", "7");
		execution.setVariable("requestUri", "v6/serviceInstances/123");
		
		
		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		List<OrchestrationFlow> orchFlows = createFlowList("DeactivateVfModuleBB","DeleteVfModuleBB","DeactivateVolumeGroupBB","DeleteVolumeGroupBB","DeactivateVnfBB","DeactivateNetworkBB"
				,"DeleteNetworkBB","DeleteNetworkCollectionBB","DeactivateServiceInstanceBB","UnassignVfModuleBB","UnassignVolumeGroupBB","UnassignVnfBB","UnassignNetworkBB","UnassignServiceInstanceBB");
		northBoundRequest.setOrchestrationFlowList(orchFlows);	
		
		ServiceInstance serviceInstanceAAI = new ServiceInstance();
		serviceInstanceAAI.setServiceInstanceId("aaisi123");
		org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO = new org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance();
		org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf = new org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf();
		vnf.setVnfId("vnfId123");
		
		org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule = new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
		vfModule.setVfModuleId("vfModule1");
		vnf.getVfModules().add(vfModule);
		org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule2 = new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
		vfModule2.setVfModuleId("vfModule2");
		vnf.getVfModules().add(vfModule2);
		
		org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup volumeGroup = new org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup();
		volumeGroup.setVolumeGroupId("vg123");
		vnf.getVolumeGroups().add(volumeGroup);
		
		serviceInstanceMSO.getVnfs().add(vnf);
		
		doReturn(serviceInstanceAAI).when(bbSetupUtils).getAAIServiceInstanceById("123");
		doReturn(serviceInstanceMSO).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction,resource,false,"my-custom-cloud-owner")).thenReturn(northBoundRequest);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEqualsBulkFlowName(ebbs,"DeactivateVfModuleBB","DeactivateVfModuleBB","DeleteVfModuleBB","DeleteVfModuleBB","DeactivateVolumeGroupBB","DeleteVolumeGroupBB","DeactivateVnfBB"
				,"DeactivateServiceInstanceBB","UnassignVfModuleBB","UnassignVfModuleBB","UnassignVolumeGroupBB","UnassignVnfBB","UnassignServiceInstanceBB");
	}
	
	@Test
	public void selectExecutionListServiceMacroUnassignTest() throws Exception{
		String gAction = "unassignInstance";
		String resource = "Service";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroActivateDeleteUnassign.json")));
		execution.setVariable("bpmnRequest", bpmnRequest);			
		execution.setVariable("aLaCarte", false);
		execution.setVariable("apiVersion", "7");
		execution.setVariable("requestUri", "v6/serviceInstances/123");
		
		
		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		List<OrchestrationFlow> orchFlows = createFlowList("UnassignVfModuleBB","UnassignVolumeGroupBB","UnassignVnfBB","UnassignNetworkBB","UnassignServiceInstanceBB");
		northBoundRequest.setOrchestrationFlowList(orchFlows);		
		
		ServiceInstance serviceInstanceAAI = new ServiceInstance();
		serviceInstanceAAI.setServiceInstanceId("aaisi123");
		org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO = new org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance();
		org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf = new org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf();
		vnf.setVnfId("vnfId123");
		
		org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule = new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
		vfModule.setVfModuleId("vfModule1");
		vnf.getVfModules().add(vfModule);
		org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule2 = new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
		vfModule2.setVfModuleId("vfModule2");
		vnf.getVfModules().add(vfModule2);
		
		org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup volumeGroup = new org.onap.so.bpmn.servicedecomposition.bbobjects.VolumeGroup();
		volumeGroup.setVolumeGroupId("vg123");
		vnf.getVolumeGroups().add(volumeGroup);
		
		serviceInstanceMSO.getVnfs().add(vnf);
		
		doReturn(serviceInstanceAAI).when(bbSetupUtils).getAAIServiceInstanceById("123");
		doReturn(serviceInstanceMSO).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction,resource,false,"my-custom-cloud-owner")).thenReturn(northBoundRequest);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEqualsBulkFlowName(ebbs,"UnassignVfModuleBB","UnassignVfModuleBB","UnassignVolumeGroupBB","UnassignVnfBB","UnassignServiceInstanceBB");
	}
	
	@Test
	public void selectExecutionListServiceMacroDeleteNetworkCollectionTest() throws Exception{
		String gAction = "deleteInstance";
		String resource = "Service";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroActivateDeleteUnassign.json")));
		execution.setVariable("bpmnRequest", bpmnRequest);			
		execution.setVariable("aLaCarte", false);
		execution.setVariable("apiVersion", "7");
		execution.setVariable("requestUri", "v6/serviceInstances/123");
		
		
		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		List<OrchestrationFlow> orchFlows = createFlowList("DeactivateVfModuleBB","DeleteVfModuleBB","DeactivateVolumeGroupBB","DeleteVolumeGroupBB","DeactivateVnfBB","DeactivateNetworkBB"
				,"DeleteNetworkBB","DeleteNetworkCollectionBB","DeactivateServiceInstanceBB","UnassignVfModuleBB","UnassignVolumeGroupBB","UnassignVnfBB","UnassignNetworkBB","UnassignServiceInstanceBB");
		northBoundRequest.setOrchestrationFlowList(orchFlows);	
		
		ServiceInstance serviceInstanceAAI = new ServiceInstance();
		serviceInstanceAAI.setServiceInstanceId("aaisi123");
		org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO = new org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance();
		
		org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network network = new org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network();
		network.setNetworkId("123");
		serviceInstanceMSO.getNetworks().add(network);
		org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network network2 = new org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network();
		network2.setNetworkId("321");
		serviceInstanceMSO.getNetworks().add(network2);
		
		Collection collection = new Collection();
		serviceInstanceMSO.setCollection(collection);
		
		doReturn(serviceInstanceAAI).when(bbSetupUtils).getAAIServiceInstanceById("123");
		doReturn(serviceInstanceMSO).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction,resource,false,"my-custom-cloud-owner")).thenReturn(northBoundRequest);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEqualsBulkFlowName(ebbs,"DeactivateNetworkBB","DeleteNetworkBB","UnassignNetworkBB","DeactivateNetworkBB","DeleteNetworkBB","UnassignNetworkBB","DeleteNetworkCollectionBB"
				,"DeactivateServiceInstanceBB","UnassignServiceInstanceBB");
	}
	
    @Test
    public void selectExecutionListVnfMacroRecreateTest() throws Exception{
        String gAction = "recreateInstance";
        String resource = "Vnf";
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/VnfMacroReplace.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);          
        execution.setVariable("aLaCarte", false);
        execution.setVariable("apiVersion", "7");
        execution.setVariable("requestUri", "v7/serviceInstances/123/vnfs/1234/recreate");
        execution.setVariable("serviceInstanceId", "123");
        execution.setVariable("vnfId", "1234");
        
        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("AAICheckVnfInMaintBB","AAISetVnfInMaintBB","DeactivateFabricConfigurationBB", "UnassignFabricConfigurationBB", "DeactivateVfModuleBB","DeleteVfModuleBB","DeactivateVnfBB","CreateVfModuleBB"
                ,"ActivateVfModuleBB","AssignFabricConfigurationBB","ActivateFabricConfigurationBB","ActivateVnfBB","SDNOVnfHealthCheckBB","AAIUnsetVnfInMaintBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);  
        
        ServiceInstance serviceInstanceAAI = new ServiceInstance();
        serviceInstanceAAI.setServiceInstanceId("123");
        org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO = new org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance();
        org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf = new org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf();
        vnf.setVnfId("1234");
        
        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule = new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
        vfModule.setVfModuleId("vfModule1");
        vnf.getVfModules().add(vfModule);
        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule2 = new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
        vfModule2.setVfModuleId("vfModule2");
        vnf.getVfModules().add(vfModule2);
        
        serviceInstanceMSO.getVnfs().add(vnf);
        
        doReturn(serviceInstanceAAI).when(bbSetupUtils).getAAIServiceInstanceById("123");
        doReturn(serviceInstanceMSO).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction,resource,false,"cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs,"AAICheckVnfInMaintBB","AAISetVnfInMaintBB","DeactivateVfModuleBB","DeactivateVfModuleBB","DeleteVfModuleBB","DeleteVfModuleBB","DeactivateVnfBB"
                ,"CreateVfModuleBB","CreateVfModuleBB","ActivateVfModuleBB","ActivateVfModuleBB","ActivateVnfBB","SDNOVnfHealthCheckBB","AAIUnsetVnfInMaintBB");
    }
    
    @Test
    public void selectExecutionListVnfMacroReplaceTest() throws Exception{
        String gAction = "replaceInstance";
        String resource = "Vnf";
        execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
        execution.setVariable("requestAction", gAction);
        String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/VnfMacroReplace.json")));
        execution.setVariable("bpmnRequest", bpmnRequest);          
        execution.setVariable("aLaCarte", false);
        execution.setVariable("apiVersion", "7");
        execution.setVariable("requestUri", "v7/serviceInstances/123/vnfs/1234/replace");
        execution.setVariable("serviceInstanceId", "123");
        execution.setVariable("vnfId", "1234");
        
        NorthBoundRequest northBoundRequest = new NorthBoundRequest();
        List<OrchestrationFlow> orchFlows = createFlowList("AAICheckVnfInMaintBB","AAISetVnfInMaintBB","DeactivateFabricConfigurationBB","UnassignFabricConfigurationBB","DeactivateVfModuleBB","DeleteVfModuleBB"
                ,"DeactivateVnfBB","ChangeModelVfModuleBB","CreateVfModuleBB","ActivateVfModuleBB","AssignFabricConfigurationBB","ActivateFabricConfigurationBB","ChangeModelVnfBB","ActivateVnfBB","ChangeModelServiceInstanceBB","SDNOVnfHealthCheckBB","AAIUnsetVnfInMaintBB");
        northBoundRequest.setOrchestrationFlowList(orchFlows);  

        ServiceInstance serviceInstanceAAI = new ServiceInstance();
        serviceInstanceAAI.setServiceInstanceId("123");
        org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance serviceInstanceMSO = new org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance();
        org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf vnf = new org.onap.so.bpmn.servicedecomposition.bbobjects.GenericVnf();
        vnf.setVnfId("1234");
        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule = new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
        vfModule.setVfModuleId("vfModule1");
        vnf.getVfModules().add(vfModule);
        org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule vfModule2 = new org.onap.so.bpmn.servicedecomposition.bbobjects.VfModule();
        vfModule2.setVfModuleId("vfModule2");
        vnf.getVfModules().add(vfModule2);
        serviceInstanceMSO.getVnfs().add(vnf);
        VfModule vfModuleAAI = new VfModule();
        vfModuleAAI.setVfModuleId("vfModule2");
        RelationshipList relationshipList = new RelationshipList();
        Relationship relationship = new Relationship();
        relationshipList.getRelationship().add(relationship);
        vfModuleAAI.setRelationshipList(relationshipList);
        Relationships relationships = new Relationships("abc");
        Configuration config = new Configuration();
        config.setConfigurationId("configId");
        Optional<Configuration> configOp = Optional.of(config);
        Optional<Relationships> relationshipsOp = Optional.of(relationships);
        
        doReturn(relationshipsOp).when(workflowActionUtils).extractRelationshipsVnfc(isA(Relationships.class));
        doReturn(configOp).when(workflowActionUtils).extractRelationshipsConfiguration(isA(Relationships.class));
        doReturn(vfModuleAAI).when(bbSetupUtils).getAAIVfModule("1234", "vfModule2");
        doReturn(serviceInstanceAAI).when(bbSetupUtils).getAAIServiceInstanceById("123");
        doReturn(serviceInstanceMSO).when(bbInputSetup).getExistingServiceInstance(serviceInstanceAAI);
        when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction,resource,false,"cloud-owner")).thenReturn(northBoundRequest);
        workflowAction.selectExecutionList(execution);
        List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
        assertEqualsBulkFlowName(ebbs,"AAICheckVnfInMaintBB","AAISetVnfInMaintBB", "DeactivateFabricConfigurationBB", "UnassignFabricConfigurationBB", "DeactivateVfModuleBB","DeactivateVfModuleBB","DeleteVfModuleBB","DeleteVfModuleBB","DeactivateVnfBB"
                ,"ChangeModelVfModuleBB" ,"ChangeModelVfModuleBB" , "CreateVfModuleBB","CreateVfModuleBB", "ActivateVfModuleBB","ActivateVfModuleBB", "AssignFabricConfigurationBB", "ActivateFabricConfigurationBB", "ChangeModelVnfBB", "ActivateVnfBB","ChangeModelServiceInstanceBB","SDNOVnfHealthCheckBB","AAIUnsetVnfInMaintBB");
    }
	
	@Ignore
	@Test
	public void selectExecutionListNetworkCollectionMacroCreate() throws Exception{
		String gAction = "createInstance";
		String resource = "NetworkCollection";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/CreateNetworkCollection.json")));
		execution.setVariable("bpmnRequest", bpmnRequest);		
		execution.setVariable("aLaCarte", false);
		execution.setVariable("apiVersion", "7");
		execution.setVariable("requestUri", "v6/serviceInstances/123/networkCollections/123");
		
		
		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		List<OrchestrationFlow> orchFlows = createFlowList("CreateNetworkCollectionBB","AssignNetworkBB","CreateNetworkBB","ActivateNetworkBB","ActivateNetworkCollectionBB");
		northBoundRequest.setOrchestrationFlowList(orchFlows);
		
		Service service = new Service();
		CollectionResourceCustomization collectionResourceCustomization = new CollectionResourceCustomization();
		CollectionResource collectionResource = new CollectionResource();
		collectionResource.setToscaNodeType("Data NetworkCollection Data");
		collectionResourceCustomization.setCollectionResource(collectionResource);
		service.getCollectionResourceCustomizations().add(collectionResourceCustomization);
		
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction,resource,false,"my-custom-cloud-owner")).thenReturn(northBoundRequest);
		when(catalogDbClient.getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a")).thenReturn(service);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEqualsBulkFlowName(ebbs,"CreateNetworkCollectionBB","AssignNetworkBB","CreateNetworkBB","ActivateNetworkBB","AssignNetworkBB","CreateNetworkBB","ActivateNetworkBB","ActivateNetworkCollectionBB");
	}
	
	@Ignore
	@Test
	public void selectExecutionListNetworkCollectionMacroDelete() throws Exception{
		String gAction = "deleteInstance";
		String resource = "NetworkCollection";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/CreateNetworkCollection.json")));
		execution.setVariable("bpmnRequest", bpmnRequest);			
		execution.setVariable("aLaCarte", false);
		execution.setVariable("apiVersion", "7");
		execution.setVariable("requestUri", "v6/serviceInstances/123/networkCollections/123");
		
		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		List<OrchestrationFlow> orchFlows = createFlowList("DeactivateNetworkBB","DeleteNetworkBB","UnassignNetworkBB","DeactivateNetworkCollectionBB","DeleteNetworkCollectionBB");
		northBoundRequest.setOrchestrationFlowList(orchFlows);		
		
		Service service = new Service();
		CollectionResourceCustomization collectionResourceCustomization = new CollectionResourceCustomization();
		CollectionResource collectionResource = new CollectionResource();
		collectionResource.setToscaNodeType("Data NetworkCollection Data");
		collectionResourceCustomization.setCollectionResource(collectionResource);
		service.getCollectionResourceCustomizations().add(collectionResourceCustomization);
		
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction,resource,false,"my-custom-cloud-owner")).thenReturn(northBoundRequest);
		when(catalogDbClient.getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a")).thenReturn(service);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEqualsBulkFlowName(ebbs,"DeactivateNetworkBB","DeleteNetworkBB","UnassignNetworkBB","DeactivateNetworkBB","DeleteNetworkBB","UnassignNetworkBB","DeactivateNetworkCollectionBB"
				,"DeleteNetworkCollectionBB");
	}
	
	@Test
	public void selectExecutionListALaCarteVfModuleNoFabricCreateTest() throws Exception{
		String gAction = "createInstance";
		String resource = "VfModule";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/VfModuleCreateWithFabric.json")));
		execution.setVariable("bpmnRequest", bpmnRequest);		
		execution.setVariable("aLaCarte", true);
		execution.setVariable("apiVersion", "7");
		execution.setVariable("requestUri", "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules");
		
		
		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		List<OrchestrationFlow> orchFlows = createFlowList("AssignVfModuleBB","CreateVfModuleBB","ActivateVfModuleBB","AssignFabricConfigurationBB","ActivateFabricConfigurationBB");
		northBoundRequest.setOrchestrationFlowList(orchFlows);	
		
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction,resource,true,"my-custom-cloud-owner")).thenReturn(northBoundRequest);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEqualsBulkFlowName(ebbs,"AssignVfModuleBB","CreateVfModuleBB","ActivateVfModuleBB");
	}
	
	@Test
	public void selectExecutionListALaCarteVfModuleFabricCreateTest() throws Exception{
		String gAction = "createInstance";
		String resource = "VfModule";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/VfModuleCreateWithFabric.json")));
		execution.setVariable("bpmnRequest", bpmnRequest);		
		execution.setVariable("aLaCarte", true);
		execution.setVariable("apiVersion", "7");
		execution.setVariable("requestUri", "v7/serviceInstances/f647e3ef-6d2e-4cd3-bff4-8df4634208de/vnfs/b80b16a5-f80d-4ffa-91c8-bd47c7438a3d/vfModules");
		
		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		List<OrchestrationFlow> orchFlows = createFlowList("AssignVfModuleBB","CreateVfModuleBB","ActivateVfModuleBB","AssignFabricConfigurationBB","ActivateFabricConfigurationBB");
		northBoundRequest.setOrchestrationFlowList(orchFlows);
		
		List<CvnfcCustomization> cvnfcCustomizations = new ArrayList<CvnfcCustomization>();
		CvnfcCustomization cvnfcCustomization = new CvnfcCustomization();
		VnfVfmoduleCvnfcConfigurationCustomization vnfVfmoduleCvnfcConfigurationCustomization = new VnfVfmoduleCvnfcConfigurationCustomization();
		ConfigurationResource configurationResource = new ConfigurationResource();
		configurationResource.setToscaNodeType("FabricConfiguration");
		vnfVfmoduleCvnfcConfigurationCustomization.setConfigurationResource(configurationResource);
		Set<VnfVfmoduleCvnfcConfigurationCustomization> custSet = new HashSet<VnfVfmoduleCvnfcConfigurationCustomization>();
		custSet.add(vnfVfmoduleCvnfcConfigurationCustomization);
		cvnfcCustomization.setVnfVfmoduleCvnfcConfigurationCustomization(custSet);
		cvnfcCustomizations.add(cvnfcCustomization);
		
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner(gAction,resource,true,"my-custom-cloud-owner")).thenReturn(northBoundRequest);
		when(catalogDbClient.getCvnfcCustomizationByVnfCustomizationUUIDAndVfModuleCustomizationUUID("fc25201d-36d6-43a3-8d39-fdae88e526ae", "9a6d01fd-19a7-490a-9800-460830a12e0b")).thenReturn(cvnfcCustomizations);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEqualsBulkFlowName(ebbs,"AssignVfModuleBB","CreateVfModuleBB","ActivateVfModuleBB","AssignFabricConfigurationBB","ActivateFabricConfigurationBB");
	}
	
	/**
	 * WorkflowActionBB Tests
	 */
	
	@Test
	public void pluralTest() {
		List<String> items = Arrays.asList(
				"serviceInstances, Service",
				"vnfs, Vnf",
				"vfModules, VfModule",
				"networks, Network",
				"invalidNames, invalidNames");
		items.forEach(item -> {
			String[] split = item.split(",");
			String type = split[0].trim();
			String expected = split[1].trim();
			assertThat(workflowAction.convertTypeFromPlural(type), equalTo(expected));
		});
	}
	
	@Test
	public void sortExecutionPathByObjectForVlanTaggingCreateTest() throws Exception{
		List<ExecuteBuildingBlock> executeFlows = new ArrayList<>();
		ExecuteBuildingBlock ebb = new ExecuteBuildingBlock();
		BuildingBlock bb = new BuildingBlock();
		bb.setBpmnFlowName("AssignNetworkBB");
		bb.setKey("0");
		ebb.setBuildingBlock(bb);
		executeFlows.add(ebb);
		ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock();
		BuildingBlock bb2 = new BuildingBlock();
		bb2.setBpmnFlowName("AssignNetworkBB");
		bb2.setKey("1");
		ebb2.setBuildingBlock(bb2);
		executeFlows.add(ebb2);
		ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock();
		BuildingBlock bb3 = new BuildingBlock();
		bb3.setBpmnFlowName("CreateNetworkBB");
		bb3.setKey("0");
		ebb3.setBuildingBlock(bb3);
		executeFlows.add(ebb3);
		ExecuteBuildingBlock ebb4 = new ExecuteBuildingBlock();
		BuildingBlock bb4 = new BuildingBlock();
		bb4.setBpmnFlowName("CreateNetworkBB");
		bb4.setKey("1");
		ebb4.setBuildingBlock(bb4);
		executeFlows.add(ebb4);
		ExecuteBuildingBlock ebb5 = new ExecuteBuildingBlock();
		BuildingBlock bb5 = new BuildingBlock();
		bb5.setBpmnFlowName("ActivateNetworkBB");
		bb5.setKey("0");
		ebb5.setBuildingBlock(bb5);
		executeFlows.add(ebb5);
		ExecuteBuildingBlock ebb6 = new ExecuteBuildingBlock();
		BuildingBlock bb6 = new BuildingBlock();
		bb6.setBpmnFlowName("ActivateNetworkBB");
		bb6.setKey("1");
		ebb6.setBuildingBlock(bb6);
		executeFlows.add(ebb6);

		List<ExecuteBuildingBlock> sorted = workflowAction.sortExecutionPathByObjectForVlanTagging(executeFlows,"createInstance");
		assertEqualsBulkFlowName(sorted,"AssignNetworkBB","CreateNetworkBB","ActivateNetworkBB","AssignNetworkBB","CreateNetworkBB","ActivateNetworkBB");
	}
	
	@Test
	public void sortExecutionPathByObjectForVlanTaggingDeleteTest() throws Exception{
		List<ExecuteBuildingBlock> executeFlows = new ArrayList<>();
		ExecuteBuildingBlock ebb = new ExecuteBuildingBlock();
		BuildingBlock bb = new BuildingBlock();
		bb.setBpmnFlowName("DeactivateNetworkBB");
		bb.setKey("0");
		ebb.setBuildingBlock(bb);
		executeFlows.add(ebb);
		ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock();
		BuildingBlock bb2 = new BuildingBlock();
		bb2.setBpmnFlowName("DeactivateNetworkBB");
		bb2.setKey("1");
		ebb2.setBuildingBlock(bb2);
		executeFlows.add(ebb2);
		ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock();
		BuildingBlock bb3 = new BuildingBlock();
		bb3.setBpmnFlowName("DeleteNetworkBB");
		bb3.setKey("0");
		ebb3.setBuildingBlock(bb3);
		executeFlows.add(ebb3);
		ExecuteBuildingBlock ebb4 = new ExecuteBuildingBlock();
		BuildingBlock bb4 = new BuildingBlock();
		bb4.setBpmnFlowName("DeleteNetworkBB");
		bb4.setKey("1");
		ebb4.setBuildingBlock(bb4);
		executeFlows.add(ebb4);
		ExecuteBuildingBlock ebb5 = new ExecuteBuildingBlock();
		BuildingBlock bb5 = new BuildingBlock();
		bb5.setBpmnFlowName("UnassignNetworkBB");
		bb5.setKey("0");
		ebb5.setBuildingBlock(bb5);
		executeFlows.add(ebb5);
		ExecuteBuildingBlock ebb6 = new ExecuteBuildingBlock();
		BuildingBlock bb6 = new BuildingBlock();
		bb6.setBpmnFlowName("UnassignNetworkBB");
		bb6.setKey("1");
		ebb6.setBuildingBlock(bb6);
		executeFlows.add(ebb6);
		
		List<ExecuteBuildingBlock> sorted = workflowAction.sortExecutionPathByObjectForVlanTagging(executeFlows,"deleteInstance");
		assertEqualsBulkFlowName(sorted,"DeactivateNetworkBB","DeleteNetworkBB","UnassignNetworkBB","DeactivateNetworkBB","DeleteNetworkBB","UnassignNetworkBB");
	}
	@Test
	public void queryNorthBoundRequestCatalogDbNestedTest() throws MalformedURLException {
		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		List<OrchestrationFlow> orchFlows = createFlowList("AAICheckVnfInMaintBB","AAISetVnfInMaintBB","VNF-Macro-Replace","SDNOVnfHealthCheckBB","AAIUnsetVnfInMaintBB");
		northBoundRequest.setOrchestrationFlowList(orchFlows);
		List<OrchestrationFlow> macroFlows = createFlowList("DeactivateVfModuleBB","DeleteVfModuleBB","DeactivateVnfBB","CreateVfModuleBB","ActivateVfModuleBB","ActivateVnfBB");
		
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwner("replaceInstance","Vnf",false,"my-custom-cloud-owner")).thenReturn(northBoundRequest);
		when(catalogDbClient.getOrchestrationFlowByAction("VNF-Macro-Replace")).thenReturn(macroFlows);
		List<OrchestrationFlow> flows = workflowAction.queryNorthBoundRequestCatalogDb(execution, "replaceInstance", WorkflowType.VNF, false,"my-custom-cloud-owner");
		assertEquals(flows.get(0).getFlowName(),"AAICheckVnfInMaintBB");
		assertEquals(flows.get(1).getFlowName(),"AAISetVnfInMaintBB");
		assertEquals(flows.get(2).getFlowName(),"DeactivateVfModuleBB");
		assertEquals(flows.get(3).getFlowName(),"DeleteVfModuleBB");
		assertEquals(flows.get(4).getFlowName(),"DeactivateVnfBB");	
		assertEquals(flows.get(5).getFlowName(),"CreateVfModuleBB");	
		assertEquals(flows.get(6).getFlowName(),"ActivateVfModuleBB");	
		assertEquals(flows.get(7).getFlowName(),"ActivateVnfBB");	
		assertEquals(flows.get(8).getFlowName(),"SDNOVnfHealthCheckBB");
		assertEquals(flows.get(9).getFlowName(),"AAIUnsetVnfInMaintBB");
	}	
	
	@Test
	public void queryNorthBoundRequestCatalogDbTransportTest() throws MalformedURLException {
		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		List<OrchestrationFlow> orchFlows = createFlowList("AssignServiceInstanceBB");
		northBoundRequest.setOrchestrationFlowList(orchFlows);
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScopeAndCloudOwnerAndServiceType("createInstance","Service",true,"my-custom-cloud-owner","TRANSPORT")).thenReturn(northBoundRequest);

		List<OrchestrationFlow> flows = workflowAction.queryNorthBoundRequestCatalogDb(execution, "createInstance", WorkflowType.SERVICE, true,"my-custom-cloud-owner","TRANSPORT");
		assertEquals(flows.get(0).getFlowName(),"AssignServiceInstanceBB");
	}

	@Test
	public void extractResourceIdAndTypeFromUriTest(){
		String uri = "/v6/serviceInstances/123";
		String uri2 = "/v6/serviceInstances/123/vnfs/1234";
		String uri3 = "/v6/serviceInstances";
		String uri4 = "/v6/serviceInstances/assign";
		String uri5 = "'/v6/serviceInstances/123/vnfs";
		String uri6 = "/v6/serviceInstances/123/vnfs/1234/someAction";
		String uri7 = "/v6/serviceInstances/123/vnfs/1234/vfModules/5678/replace";
		String uri8 = "/v6/serviceInstances/123/vnfs/1234/vfModules/scaleOut";
		Resource expected1 = new Resource(WorkflowType.SERVICE, "123", true);
		Resource expected2 = new Resource(WorkflowType.VNF, "1234", false);
		Resource expected3 = new Resource(WorkflowType.VNF, "1234", false);
		Resource expected4 = new Resource(WorkflowType.VFMODULE, "5678", false);
		Resource result = workflowAction.extractResourceIdAndTypeFromUri(uri);
		assertEquals(expected1.getResourceId(),result.getResourceId());
		assertEquals(expected1.getResourceType(),result.getResourceType());
		result = workflowAction.extractResourceIdAndTypeFromUri(uri2);
		assertEquals(expected2.getResourceId(),result.getResourceId());
		assertEquals(expected2.getResourceType(),result.getResourceType());
		result = workflowAction.extractResourceIdAndTypeFromUri(uri3);
		assertEquals("Service", result.getResourceType().toString());
		assertEquals(UUID.randomUUID().toString().length(),result.getResourceId().length());
		result = workflowAction.extractResourceIdAndTypeFromUri(uri4);
		assertEquals("Service", result.getResourceType().toString());
		assertEquals(UUID.randomUUID().toString().length(),result.getResourceId().length());
		result = workflowAction.extractResourceIdAndTypeFromUri(uri5);
		assertEquals("Vnf", result.getResourceType().toString());
		assertEquals(UUID.randomUUID().toString().length(),result.getResourceId().length());
		result = workflowAction.extractResourceIdAndTypeFromUri(uri6);
		assertEquals(expected3.getResourceId(),result.getResourceId());
		assertEquals(expected3.getResourceType(),result.getResourceType());
		result = workflowAction.extractResourceIdAndTypeFromUri(uri7);
		assertEquals(expected4.getResourceId(),result.getResourceId());
		assertEquals(expected4.getResourceType(),result.getResourceType());
		result = workflowAction.extractResourceIdAndTypeFromUri(uri8);
        assertEquals(UUID.randomUUID().toString().length(),result.getResourceId().length());    
        assertEquals("VfModule", result.getResourceType().toString());
		
	}
	
	@Test
	public void extractResourceIdAndTypeFromUriInvalidTypeTest() {
		this.expectedException.expect(IllegalArgumentException.class);
		this.expectedException.expectMessage(containsString("Uri could not be parsed. No type found."));
		workflowAction.extractResourceIdAndTypeFromUri("/v6/serviceInstances/123/vnfs/1234/vfmodules/5678/replace");
	}
	
	@Test
	public void extractResourceIdAndTypeFromUriInvalidUriTest() {
		this.expectedException.expect(IllegalArgumentException.class);
		this.expectedException.expectMessage(containsString("Uri could not be parsed:"));
		workflowAction.extractResourceIdAndTypeFromUri("something that doesn't match anything");
	}
	
	@Test
	public void populateResourceIdsFromApiHandlerTest(){
		execution.setVariable("serviceInstanceId", "123");
		execution.setVariable("vnfId", "888");
		WorkflowResourceIds x = workflowAction.populateResourceIdsFromApiHandler(execution);
		assertEquals("123",x.getServiceInstanceId());
		assertEquals("888",x.getVnfId());
		assertNull(x.getVolumeGroupId());
	}
	
	@Test
	public void validateResourceIdInAAITest() throws Exception{
		//SI
		RequestDetails reqDetails = new RequestDetails();
		SubscriberInfo subInfo = new SubscriberInfo();
		subInfo.setGlobalSubscriberId("id123");
		reqDetails.setSubscriberInfo(subInfo);
		RequestParameters reqParams = new RequestParameters();
		reqParams.setSubscriptionServiceType("subServiceType123");
		reqDetails.setRequestParameters(reqParams );
		WorkflowResourceIds workflowResourceIds = new WorkflowResourceIds();
		ServiceInstance si = new ServiceInstance();
		si.setServiceInstanceId("siId123");
		Optional<ServiceInstance> siOp = Optional.of(si);
		when(bbSetupUtils.getAAIServiceInstanceByName("id123", "subServiceType123", "siName123")).thenReturn(siOp);
		when(bbSetupUtils.getAAIServiceInstanceByName("id123", "subServiceType123", "111111")).thenReturn(Optional.empty());
		String id = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.SERVICE, "siName123", reqDetails, workflowResourceIds);
		assertEquals("siId123",id);
		String id2 = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.SERVICE, "111111", reqDetails, workflowResourceIds);
		assertEquals("generatedId123",id2);
		
		//Network
		L3Network network = new L3Network();
		network.setNetworkId("id123");
		network.setNetworkName("name123");
		workflowResourceIds.setServiceInstanceId("siId123");
		Optional<L3Network> opNetwork = Optional.of(network);
		when(bbSetupUtils.getRelatedNetworkByNameFromServiceInstance("siId123","name123")).thenReturn(opNetwork);
		when(bbSetupUtils.getRelatedNetworkByNameFromServiceInstance("siId123","111111")).thenReturn(Optional.empty());
		id = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.NETWORK, "name123", reqDetails, workflowResourceIds);
		assertEquals("id123",id);
		id2 = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.NETWORK, "111111", reqDetails, workflowResourceIds);
		assertEquals("generatedId123",id2);
		
		//Vnf
		GenericVnf vnf = new GenericVnf();
		vnf.setVnfId("id123");
		vnf.setVnfName("vnfName123");
		Optional<GenericVnf> opVnf = Optional.of(vnf);
		when(bbSetupUtils.getRelatedVnfByNameFromServiceInstance("siId123","name123")).thenReturn(opVnf);
		when(bbSetupUtils.getRelatedVnfByNameFromServiceInstance("siId123","111111")).thenReturn(Optional.empty());
		id = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.VNF, "name123", reqDetails, workflowResourceIds);
		assertEquals("id123",id);
		id2 = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.VNF, "111111", reqDetails, workflowResourceIds);
		assertEquals("generatedId123",id2);
		
		//VfModule
		VfModules vfModules = new VfModules();
		VfModule vfModule = new VfModule();
		vfModule.setVfModuleId("id123");
		vfModule.setVfModuleName("name123");
		vfModules.getVfModule().add(vfModule);
		vnf.setVfModules(vfModules);
		workflowResourceIds.setVnfId("id123");
		when(bbSetupUtils.getAAIGenericVnf("id123")).thenReturn(vnf);
		id = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.VFMODULE, "name123", reqDetails, workflowResourceIds);
		assertEquals("id123",id);

		GenericVnf vnf2 = new GenericVnf();
		VfModules vfModules2 = new VfModules();
		VfModule vfModule2 = new VfModule();
		vfModule2.setVfModuleId("id123");
		vfModule2.setVfModuleName("name123");
		vfModules2.getVfModule().add(vfModule2);
		vnf2.setVfModules(vfModules2);
		workflowResourceIds.setVnfId("id111");
		when(bbSetupUtils.getAAIGenericVnf("id111")).thenReturn(vnf2);
		id2 = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.VFMODULE, "111111", reqDetails, workflowResourceIds);
		assertEquals("generatedId123",id2);
		
		//VolumeGroup
		VolumeGroup volumeGroup = new VolumeGroup();
		volumeGroup.setVolumeGroupId("id123");
		volumeGroup.setVolumeGroupName("name123");
		workflowResourceIds.setVnfId("id123");
		Optional<VolumeGroup> opVolumeGroup = Optional.of(volumeGroup);
		when(bbSetupUtils.getRelatedVolumeGroupByNameFromVnf("id123","name123")).thenReturn(opVolumeGroup);
		id = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.VOLUMEGROUP, "name123", reqDetails, workflowResourceIds);
		assertEquals("id123",id);
		
		workflowResourceIds.setVnfId("id444");
		when(bbSetupUtils.getAAIGenericVnf("id444")).thenReturn(vnf);
		when(bbSetupUtils.getRelatedVolumeGroupByNameFromVfModule("id123", "id123","111111")).thenReturn(opVolumeGroup);
		when(bbSetupUtils.getRelatedVolumeGroupByNameFromVnf("id444","111111")).thenReturn(Optional.empty());
		id2 = workflowAction.validateResourceIdInAAI("generatedId123", WorkflowType.VOLUMEGROUP, "111111", reqDetails, workflowResourceIds);
		assertEquals("id123",id2);
	}
	
	@Test
	public void handleRuntimeExceptionTest(){
		execution.setVariable("BPMN_javaExpMsg", "test runtime error message");
		execution.setVariable("testProcessKey", "testProcessKeyValue");
		try{
			workflowAction.handleRuntimeException(execution);
		} catch (BpmnError wfe) {
			assertEquals("MSOWorkflowException",wfe.getErrorCode());
		}
	}
	
	@Ignore
	@Test
	public void traverseCatalogDbServiceMultipleNetworkTest() throws IOException{
		execution.setVariable("testProcessKey", "testProcessKeyValue");
		Service service = new Service();
		List<NetworkResourceCustomization> networkCustomizations = new ArrayList<>();
		NetworkResourceCustomization networkCust = new NetworkResourceCustomization();
		networkCust.setModelCustomizationUUID("123");
		networkCustomizations.add(networkCust);
		service.setNetworkCustomizations(networkCustomizations);
		NetworkCollectionResourceCustomization collectionResourceCustomization = new NetworkCollectionResourceCustomization();
		collectionResourceCustomization.setModelCustomizationUUID("123");
		CollectionResource collectionResource = new CollectionResource();
		collectionResource.setToscaNodeType("NetworkCollection");
		InstanceGroup instanceGroup = new InstanceGroup();
		List<CollectionResourceInstanceGroupCustomization> collectionInstanceGroupCustomizations = new ArrayList<>();
		CollectionResourceInstanceGroupCustomization collectionInstanceGroupCustomization = new CollectionResourceInstanceGroupCustomization();
		collectionInstanceGroupCustomization.setSubInterfaceNetworkQuantity(3);
		collectionInstanceGroupCustomizations.add(collectionInstanceGroupCustomization);
		instanceGroup.setCollectionInstanceGroupCustomizations(collectionInstanceGroupCustomizations);
		collectionResource.setInstanceGroup(instanceGroup);
		collectionResourceCustomization.setCollectionResource(collectionResource);;
		service.setModelUUID("abc");
		service.getCollectionResourceCustomizations().add(collectionResourceCustomization);
		service.getCollectionResourceCustomizations().add(collectionResourceCustomization);
		doReturn(service).when(catalogDbClient).getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a");
		doReturn(collectionResourceCustomization).when(catalogDbClient).getNetworkCollectionResourceCustomizationByID("123");
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/ServiceMacroActivateDeleteUnassign.json")));
		ObjectMapper mapper = new ObjectMapper();
		ServiceInstancesRequest sIRequest = mapper.readValue(bpmnRequest, ServiceInstancesRequest.class);
		List<Resource> resourceCounter = new ArrayList<>();
		thrown.expect(BpmnError.class);
		workflowAction.traverseCatalogDbService(execution, sIRequest, resourceCounter);
	}
	
	@Test
	public void sortVfModulesByBaseFirstTest(){
		List<Resource> resources = new ArrayList<>();
		Resource resource1 = new Resource(WorkflowType.VFMODULE,"111",false);
		resource1.setBaseVfModule(false);
		resources.add(resource1);
		Resource resource2 = new Resource(WorkflowType.VFMODULE,"222",false);
		resource2.setBaseVfModule(false);
		resources.add(resource2);
		Resource resource3 = new Resource(WorkflowType.VFMODULE,"333",false);
		resource3.setBaseVfModule(true);
		resources.add(resource3);
		List<Resource> result = workflowAction.sortVfModulesByBaseFirst(resources);
		assertEquals("333",result.get(0).getResourceId());
		assertEquals("222",result.get(1).getResourceId());
		assertEquals("111",result.get(2).getResourceId());
	}
	
	@Test
	public void sortVfModulesByBaseLastTest(){
		List<Resource> resources = new ArrayList<>();
		Resource resource1 = new Resource(WorkflowType.VFMODULE,"111",false);
		resource1.setBaseVfModule(true);
		resources.add(resource1);
		Resource resource2 = new Resource(WorkflowType.VFMODULE,"222",false);
		resource2.setBaseVfModule(false);
		resources.add(resource2);
		Resource resource3 = new Resource(WorkflowType.VFMODULE,"333",false);
		resource3.setBaseVfModule(false);
		resources.add(resource3);
		List<Resource> result = workflowAction.sortVfModulesByBaseLast(resources);
		assertEquals("333",result.get(0).getResourceId());
		assertEquals("222",result.get(1).getResourceId());
		assertEquals("111",result.get(2).getResourceId());
	}
	
	private List<OrchestrationFlow> createFlowList (String... flowNames){
		List<OrchestrationFlow> result = new ArrayList<>();
		for(String flowName : flowNames){
			OrchestrationFlow orchFlow = new OrchestrationFlow();
			orchFlow.setFlowName(flowName);
			result.add(orchFlow);
		}
		return result;
	}
	
	private void assertEqualsBulkFlowName (List<ExecuteBuildingBlock> ebbs, String... flowNames){
		for(int i = 0; i<ebbs.size(); i++){
			assertEquals(ebbs.get(i).getBuildingBlock().getBpmnFlowName(),flowNames[i]);
		}
	}
}
