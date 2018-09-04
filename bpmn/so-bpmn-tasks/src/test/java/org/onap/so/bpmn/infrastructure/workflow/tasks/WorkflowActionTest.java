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
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.mockito.delegate.DelegateExecutionFake;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.VfModules;
import org.onap.aai.domain.yang.VolumeGroup;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.Collection;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.onap.so.db.catalog.beans.CollectionNetworkResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResource;
import org.onap.so.db.catalog.beans.CollectionResourceCustomization;
import org.onap.so.db.catalog.beans.CollectionResourceInstanceGroupCustomization;
import org.onap.so.db.catalog.beans.HeatEnvironment;
import org.onap.so.db.catalog.beans.HeatTemplate;
import org.onap.so.db.catalog.beans.InstanceGroup;
import org.onap.so.db.catalog.beans.NetworkCollectionResourceCustomization;
import org.onap.so.db.catalog.beans.NetworkResourceCustomization;
import org.onap.so.db.catalog.beans.Service;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.db.catalog.beans.macro.NorthBoundRequest;
import org.onap.so.db.catalog.beans.macro.OrchestrationFlow;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.RequestParameters;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;
import org.onap.so.serviceinstancebeans.SubscriberInfo;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

public class WorkflowActionTest extends BaseTaskTest {
	@Autowired
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
		List<OrchestrationFlow> orchFlows = new LinkedList<>();
		northBoundRequest.setOrchestrationFlowList(orchFlows);
		OrchestrationFlow orch1 = new OrchestrationFlow();
		orch1.setFlowName("AssignNetwork1802BB");
		orchFlows.add(orch1);
		OrchestrationFlow orch2 = new OrchestrationFlow();
		orch2.setFlowName("CreateNetworkBB");
		orchFlows.add(orch2);
		OrchestrationFlow orch3 = new OrchestrationFlow();
		orch3.setFlowName("ActivateNetworkBB");
		orchFlows.add(orch3);

		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,true)).thenReturn(northBoundRequest);
	
		
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"AssignNetwork1802BB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"CreateNetworkBB");
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"ActivateNetworkBB");
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
		List<OrchestrationFlow> orchFlows = new LinkedList<>();
		northBoundRequest.setOrchestrationFlowList(orchFlows);
		OrchestrationFlow orch1 = new OrchestrationFlow();
		orch1.setFlowName("DeactivateNetworkBB");
		orchFlows.add(orch1);
		OrchestrationFlow orch2 = new OrchestrationFlow();
		orch2.setFlowName("DeleteNetworkBB");
		orchFlows.add(orch2);
		OrchestrationFlow orch3 = new OrchestrationFlow();
		orch3.setFlowName("UnassignNetwork1802BB");
		orchFlows.add(orch3);
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,true)).thenReturn(northBoundRequest);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"DeactivateNetworkBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"DeleteNetworkBB");
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"UnassignNetwork1802BB");
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
		List<OrchestrationFlow> orchFlows = new LinkedList<>();
		northBoundRequest.setOrchestrationFlowList(orchFlows);		
		OrchestrationFlow orch1 = new OrchestrationFlow();
		orch1.setFlowName("AssignServiceInstanceBB");
		orchFlows.add(orch1);
		OrchestrationFlow orch2 = new OrchestrationFlow();
		orch2.setFlowName("ActivateServiceInstanceBB");
		orchFlows.add(orch2);
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,true)).thenReturn(northBoundRequest);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"AssignServiceInstanceBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"ActivateServiceInstanceBB");
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
		List<OrchestrationFlow> orchFlows = new LinkedList<>();
		northBoundRequest.setOrchestrationFlowList(orchFlows);
		OrchestrationFlow orch1 = new OrchestrationFlow();
		orch1.setFlowName("AssignServiceInstanceBB");
		orchFlows.add(orch1);
		OrchestrationFlow orch2 = new OrchestrationFlow();
		orch2.setFlowName("AssignNetworkBB");
		orchFlows.add(orch2);
		OrchestrationFlow orch3 = new OrchestrationFlow();
		orch3.setFlowName("AssignVnfBB");
		orchFlows.add(orch3);
		OrchestrationFlow orch4 = new OrchestrationFlow();
		orch4.setFlowName("AssignVolumeGroupBB");
		orchFlows.add(orch4);
		OrchestrationFlow orch5 = new OrchestrationFlow();
		orch5.setFlowName("AssignVfModuleBB");
		orchFlows.add(orch5);
		
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
		
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,false)).thenReturn(northBoundRequest);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f")).thenReturn(vfModuleCustomization);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8")).thenReturn(vfModuleCustomization2);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("da4d4327-fb7d-4311-ac7a-be7ba60cf969")).thenReturn(vfModuleCustomization3);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"AssignServiceInstanceBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"AssignVnfBB");
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"AssignVolumeGroupBB");
		assertEquals(ebbs.get(3).getBuildingBlock().getBpmnFlowName(),"AssignVfModuleBB");
		assertEquals(ebbs.get(4).getBuildingBlock().getBpmnFlowName(),"AssignVfModuleBB");
		assertEquals(ebbs.get(5).getBuildingBlock().getBpmnFlowName(),"AssignVfModuleBB");
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
		List<OrchestrationFlow> orchFlows = new LinkedList<>();
		northBoundRequest.setOrchestrationFlowList(orchFlows);	
		OrchestrationFlow orch1 = new OrchestrationFlow();
		orch1.setFlowName("CreateNetworkBB");
		orchFlows.add(orch1);
		OrchestrationFlow orch2 = new OrchestrationFlow();
		orch2.setFlowName("ActivateNetworkBB");
		orchFlows.add(orch2);
		OrchestrationFlow orch3 = new OrchestrationFlow();
		orch3.setFlowName("CreateVolumeGroupBB");
		orchFlows.add(orch3);
		OrchestrationFlow orch4 = new OrchestrationFlow();
		orch4.setFlowName("ActivateVolumeGroupBB");
		orchFlows.add(orch4);
		OrchestrationFlow orch5 = new OrchestrationFlow();
		orch5.setFlowName("CreateVfModuleBB");
		orchFlows.add(orch5);
		OrchestrationFlow orch6 = new OrchestrationFlow();
		orch6.setFlowName("ActivateVfModuleBB");
		orchFlows.add(orch6);
		OrchestrationFlow orch7 = new OrchestrationFlow();
		orch7.setFlowName("ActivateVnfBB");
		orchFlows.add(orch7);
		OrchestrationFlow orch8 = new OrchestrationFlow();
		orch8.setFlowName("ActivateServiceInstanceBB");
		orchFlows.add(orch8);
		
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
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,false)).thenReturn(northBoundRequest);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals("CreateVolumeGroupBB", ebbs.get(0).getBuildingBlock().getBpmnFlowName());
		assertEquals("volumeGroup0", ebbs.get(0).getWorkflowResourceIds().getVolumeGroupId());
		assertEquals("ActivateVolumeGroupBB", ebbs.get(1).getBuildingBlock().getBpmnFlowName());		
		assertEquals("volumeGroup0", ebbs.get(1).getWorkflowResourceIds().getVolumeGroupId());
		assertEquals("CreateVfModuleBB", ebbs.get(2).getBuildingBlock().getBpmnFlowName());
		assertEquals("vfModule0", ebbs.get(2).getWorkflowResourceIds().getVfModuleId());
		assertEquals("CreateVfModuleBB", ebbs.get(3).getBuildingBlock().getBpmnFlowName());
		assertEquals("vfModule1", ebbs.get(3).getWorkflowResourceIds().getVfModuleId());
		assertEquals("ActivateVfModuleBB", ebbs.get(4).getBuildingBlock().getBpmnFlowName());
		assertEquals("vfModule0", ebbs.get(4).getWorkflowResourceIds().getVfModuleId());
		assertEquals("ActivateVfModuleBB", ebbs.get(5).getBuildingBlock().getBpmnFlowName());	
		assertEquals("vfModule1", ebbs.get(5).getWorkflowResourceIds().getVfModuleId());
		assertEquals("ActivateVnfBB", ebbs.get(6).getBuildingBlock().getBpmnFlowName());
		assertEquals("vnf0", ebbs.get(6).getWorkflowResourceIds().getVnfId());
		assertEquals("ActivateServiceInstanceBB", ebbs.get(7).getBuildingBlock().getBpmnFlowName());		
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
		List<OrchestrationFlow> orchFlows = new LinkedList<>();
		northBoundRequest.setOrchestrationFlowList(orchFlows);
		OrchestrationFlow orch = new OrchestrationFlow();
		orch.setFlowName("DeactivateServiceInstanceBB");
		orchFlows.add(orch);		
		
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,false)).thenReturn(northBoundRequest);
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
		List<OrchestrationFlow> orchFlows = new LinkedList<>();
		northBoundRequest.setOrchestrationFlowList(orchFlows);			
		OrchestrationFlow orch1 = new OrchestrationFlow();
		orch1.setFlowName("AssignServiceInstanceBB");
		orchFlows.add(orch1);
		OrchestrationFlow orch2 = new OrchestrationFlow();
		orch2.setFlowName("CreateNetworkCollectionBB");
		orchFlows.add(orch2);
		OrchestrationFlow orch3 = new OrchestrationFlow();
		orch3.setFlowName("AssignNetworkBB");
		orchFlows.add(orch3);
		OrchestrationFlow orch4 = new OrchestrationFlow();
		orch4.setFlowName("AssignVnfBB");
		orchFlows.add(orch4);
		OrchestrationFlow orch5 = new OrchestrationFlow();
		orch5.setFlowName("AssignVolumeGroupBB");
		orchFlows.add(orch5);
		OrchestrationFlow orch6 = new OrchestrationFlow();
		orch6.setFlowName("AssignVfModuleBB");
		orchFlows.add(orch6);
		OrchestrationFlow orch7 = new OrchestrationFlow();
		orch7.setFlowName("CreateNetworkBB");
		orchFlows.add(orch7);
		OrchestrationFlow orch8 = new OrchestrationFlow();
		orch8.setFlowName("ActivateNetworkBB");
		orchFlows.add(orch8);
		OrchestrationFlow orch9 = new OrchestrationFlow();
		orch9.setFlowName("CreateVolumeGroupBB");
		orchFlows.add(orch9);
		OrchestrationFlow orch10 = new OrchestrationFlow();
		orch10.setFlowName("ActivateVolumeGroupBB");
		orchFlows.add(orch10);
		OrchestrationFlow orch11 = new OrchestrationFlow();
		orch11.setFlowName("CreateVfModuleBB");
		orchFlows.add(orch11);
		OrchestrationFlow orch12 = new OrchestrationFlow();
		orch12.setFlowName("ActivateVfModuleBB");
		orchFlows.add(orch12);
		OrchestrationFlow orch13 = new OrchestrationFlow();
		orch13.setFlowName("AssignFabricConfigurationBB");
		orchFlows.add(orch13);
		OrchestrationFlow orch14 = new OrchestrationFlow();
		orch14.setFlowName("ActivateFabricConfigurationBB");
		orchFlows.add(orch14);
		OrchestrationFlow orch15 = new OrchestrationFlow();
		orch15.setFlowName("ActivateVnfBB");
		orchFlows.add(orch15);
		OrchestrationFlow orch16 = new OrchestrationFlow();
		orch16.setFlowName("ActivateNetworkCollectionBB");
		orchFlows.add(orch16);
		OrchestrationFlow orch17 = new OrchestrationFlow();
		orch17.setFlowName("ActivateServiceInstanceBB");
		orchFlows.add(orch17);
		
		Service service = new Service();
		doReturn(service).when(catalogDbClient).getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a");
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,false)).thenReturn(northBoundRequest);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"AssignServiceInstanceBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"ActivateServiceInstanceBB");
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
		List<OrchestrationFlow> orchFlows = new LinkedList<>();
		northBoundRequest.setOrchestrationFlowList(orchFlows);		
		OrchestrationFlow orch1 = new OrchestrationFlow();
		orch1.setFlowName("AssignServiceInstanceBB");
		orchFlows.add(orch1);
		OrchestrationFlow orch2 = new OrchestrationFlow();
		orch2.setFlowName("CreateNetworkCollectionBB");
		orchFlows.add(orch2);
		OrchestrationFlow orch3 = new OrchestrationFlow();
		orch3.setFlowName("AssignNetworkBB");
		orchFlows.add(orch3);
		OrchestrationFlow orch4 = new OrchestrationFlow();
		orch4.setFlowName("AssignVnfBB");
		orchFlows.add(orch4);
		OrchestrationFlow orch5 = new OrchestrationFlow();
		orch5.setFlowName("AssignVolumeGroupBB");
		orchFlows.add(orch5);
		OrchestrationFlow orch6 = new OrchestrationFlow();
		orch6.setFlowName("AssignVfModuleBB");
		orchFlows.add(orch6);
		OrchestrationFlow orch7 = new OrchestrationFlow();
		orch7.setFlowName("CreateNetworkBB");
		orchFlows.add(orch7);
		OrchestrationFlow orch8 = new OrchestrationFlow();
		orch8.setFlowName("ActivateNetworkBB");
		orchFlows.add(orch8);
		OrchestrationFlow orch9 = new OrchestrationFlow();
		orch9.setFlowName("CreateVolumeGroupBB");
		orchFlows.add(orch9);
		OrchestrationFlow orch10 = new OrchestrationFlow();
		orch10.setFlowName("ActivateVolumeGroupBB");
		orchFlows.add(orch10);
		OrchestrationFlow orch11 = new OrchestrationFlow();
		orch11.setFlowName("CreateVfModuleBB");
		orchFlows.add(orch11);
		OrchestrationFlow orch12 = new OrchestrationFlow();
		orch12.setFlowName("ActivateVfModuleBB");
		orchFlows.add(orch12);
		OrchestrationFlow orch13 = new OrchestrationFlow();
		orch13.setFlowName("ActivateVnfBB");
		orchFlows.add(orch13);
		OrchestrationFlow orch14 = new OrchestrationFlow();
		orch14.setFlowName("ActivateNetworkCollectionBB");
		orchFlows.add(orch14);
		OrchestrationFlow orch15 = new OrchestrationFlow();
		orch15.setFlowName("ActivateServiceInstanceBB");
		orchFlows.add(orch15);
		
		Service service = new Service();
		NetworkResourceCustomization networkCustomization = new NetworkResourceCustomization();
		networkCustomization.setModelCustomizationUUID("1234");
		service.getNetworkCustomizations().add(networkCustomization);
		doReturn(service).when(catalogDbClient).getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a");
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,false)).thenReturn(northBoundRequest);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"AssignServiceInstanceBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"AssignNetworkBB");
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"CreateNetworkBB");
		assertEquals(ebbs.get(3).getBuildingBlock().getBpmnFlowName(),"ActivateNetworkBB");
		assertEquals(ebbs.get(4).getBuildingBlock().getBpmnFlowName(),"ActivateServiceInstanceBB");
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
		List<OrchestrationFlow> orchFlows = new LinkedList<>();
		northBoundRequest.setOrchestrationFlowList(orchFlows);		
		OrchestrationFlow orch1 = new OrchestrationFlow();
		orch1.setFlowName("AssignServiceInstanceBB");
		orchFlows.add(orch1);
		OrchestrationFlow orch2 = new OrchestrationFlow();
		orch2.setFlowName("CreateNetworkCollectionBB");
		orchFlows.add(orch2);
		OrchestrationFlow orch3 = new OrchestrationFlow();
		orch3.setFlowName("AssignNetworkBB");
		orchFlows.add(orch3);
		OrchestrationFlow orch4 = new OrchestrationFlow();
		orch4.setFlowName("AssignVnfBB");
		orchFlows.add(orch4);
		OrchestrationFlow orch5 = new OrchestrationFlow();
		orch5.setFlowName("AssignVolumeGroupBB");
		orchFlows.add(orch5);
		OrchestrationFlow orch6 = new OrchestrationFlow();
		orch6.setFlowName("AssignVfModuleBB");
		orchFlows.add(orch6);
		OrchestrationFlow orch7 = new OrchestrationFlow();
		orch7.setFlowName("CreateNetworkBB");
		orchFlows.add(orch7);
		OrchestrationFlow orch8 = new OrchestrationFlow();
		orch8.setFlowName("ActivateNetworkBB");
		orchFlows.add(orch8);
		OrchestrationFlow orch9 = new OrchestrationFlow();
		orch9.setFlowName("CreateVolumeGroupBB");
		orchFlows.add(orch9);
		OrchestrationFlow orch10 = new OrchestrationFlow();
		orch10.setFlowName("ActivateVolumeGroupBB");
		orchFlows.add(orch10);
		OrchestrationFlow orch11 = new OrchestrationFlow();
		orch11.setFlowName("CreateVfModuleBB");
		orchFlows.add(orch11);
		OrchestrationFlow orch12 = new OrchestrationFlow();
		orch12.setFlowName("ActivateVfModuleBB");
		orchFlows.add(orch12);
		OrchestrationFlow orch13 = new OrchestrationFlow();
		orch13.setFlowName("ActivateVnfBB");
		orchFlows.add(orch13);
		OrchestrationFlow orch14 = new OrchestrationFlow();
		orch14.setFlowName("ActivateNetworkCollectionBB");
		orchFlows.add(orch14);
		OrchestrationFlow orch15 = new OrchestrationFlow();
		orch15.setFlowName("ActivateServiceInstanceBB");
		orchFlows.add(orch15);
		
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
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,false)).thenReturn(northBoundRequest);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"AssignServiceInstanceBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"CreateNetworkCollectionBB");
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"AssignNetworkBB");
		assertEquals("Network id not empty", !ebbs.get(2).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
		assertEquals(ebbs.get(3).getBuildingBlock().getBpmnFlowName(),"CreateNetworkBB");
		assertEquals("Network id not empty", !ebbs.get(3).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
		assertEquals(ebbs.get(4).getBuildingBlock().getBpmnFlowName(),"ActivateNetworkBB");
		assertEquals("Network id not empty", !ebbs.get(4).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
		assertEquals("Network id same for AssignNetworkBB CreateNetworkBB ActivateNetworkBB",
				ebbs.get(2).getWorkflowResourceIds().getNetworkId() == ebbs.get(3).getWorkflowResourceIds().getNetworkId() 
				&& ebbs.get(3).getWorkflowResourceIds().getNetworkId() == ebbs.get(4).getWorkflowResourceIds().getNetworkId(), true);
		assertEquals(ebbs.get(5).getBuildingBlock().getBpmnFlowName(),"AssignNetworkBB");
		assertEquals("Network id not empty", !ebbs.get(5).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
		assertEquals(ebbs.get(6).getBuildingBlock().getBpmnFlowName(),"CreateNetworkBB");
		assertEquals("Network id not empty", !ebbs.get(6).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
		assertEquals(ebbs.get(7).getBuildingBlock().getBpmnFlowName(),"ActivateNetworkBB");
		assertEquals("Network id not empty", !ebbs.get(7).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
		assertEquals("Network id same for AssignNetworkBB CreateNetworkBB ActivateNetworkBB",
				ebbs.get(5).getWorkflowResourceIds().getNetworkId() == ebbs.get(6).getWorkflowResourceIds().getNetworkId() 
				&& ebbs.get(6).getWorkflowResourceIds().getNetworkId() == ebbs.get(7).getWorkflowResourceIds().getNetworkId(), true);
		assertEquals(ebbs.get(8).getBuildingBlock().getBpmnFlowName(),"AssignNetworkBB");
		assertEquals("Network id not empty", !ebbs.get(8).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
		assertEquals(ebbs.get(9).getBuildingBlock().getBpmnFlowName(),"CreateNetworkBB");
		assertEquals("Network id not empty", !ebbs.get(9).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
		assertEquals(ebbs.get(10).getBuildingBlock().getBpmnFlowName(),"ActivateNetworkBB");
		assertEquals("Network id not empty", !ebbs.get(10).getWorkflowResourceIds().getNetworkId().isEmpty(), true);
		assertEquals("Network id same for AssignNetworkBB CreateNetworkBB ActivateNetworkBB",
				ebbs.get(8).getWorkflowResourceIds().getNetworkId() == ebbs.get(9).getWorkflowResourceIds().getNetworkId() 
				&& ebbs.get(9).getWorkflowResourceIds().getNetworkId() == ebbs.get(10).getWorkflowResourceIds().getNetworkId(), true);
		assertEquals(ebbs.get(11).getBuildingBlock().getBpmnFlowName(),"ActivateNetworkCollectionBB");
		assertEquals(ebbs.get(12).getBuildingBlock().getBpmnFlowName(),"ActivateServiceInstanceBB");
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
		List<OrchestrationFlow> orchFlows = new LinkedList<>();
		northBoundRequest.setOrchestrationFlowList(orchFlows);		
		OrchestrationFlow orch1 = new OrchestrationFlow();
		orch1.setFlowName("AssignServiceInstanceBB");
		orchFlows.add(orch1);
		OrchestrationFlow orch2 = new OrchestrationFlow();
		orch2.setFlowName("CreateNetworkCollectionBB");
		orchFlows.add(orch2);
		OrchestrationFlow orch3 = new OrchestrationFlow();
		orch3.setFlowName("AssignNetworkBB");
		orchFlows.add(orch3);
		OrchestrationFlow orch4 = new OrchestrationFlow();
		orch4.setFlowName("AssignVnfBB");
		orchFlows.add(orch4);
		OrchestrationFlow orch5 = new OrchestrationFlow();
		orch5.setFlowName("AssignVolumeGroupBB");
		orchFlows.add(orch5);
		OrchestrationFlow orch6 = new OrchestrationFlow();
		orch6.setFlowName("AssignVfModuleBB");
		orchFlows.add(orch6);
		OrchestrationFlow orch7 = new OrchestrationFlow();
		orch7.setFlowName("CreateNetworkBB");
		orchFlows.add(orch7);
		OrchestrationFlow orch8 = new OrchestrationFlow();
		orch8.setFlowName("ActivateNetworkBB");
		orchFlows.add(orch8);
		OrchestrationFlow orch9 = new OrchestrationFlow();
		orch9.setFlowName("CreateVolumeGroupBB");
		orchFlows.add(orch9);
		OrchestrationFlow orch10 = new OrchestrationFlow();
		orch10.setFlowName("ActivateVolumeGroupBB");
		orchFlows.add(orch10);
		OrchestrationFlow orch11 = new OrchestrationFlow();
		orch11.setFlowName("CreateVfModuleBB");
		orchFlows.add(orch11);
		OrchestrationFlow orch12 = new OrchestrationFlow();
		orch12.setFlowName("ActivateVfModuleBB");
		orchFlows.add(orch12);
		OrchestrationFlow orch13 = new OrchestrationFlow();
		orch13.setFlowName("ActivateVnfBB");
		orchFlows.add(orch13);
		OrchestrationFlow orch14 = new OrchestrationFlow();
		orch14.setFlowName("ActivateNetworkCollectionBB");
		orchFlows.add(orch14);
		OrchestrationFlow orch15 = new OrchestrationFlow();
		orch15.setFlowName("ActivateServiceInstanceBB");
		orchFlows.add(orch15);
		
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
		
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,false)).thenReturn(northBoundRequest);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f")).thenReturn(vfModuleCustomization);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8")).thenReturn(vfModuleCustomization2);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("da4d4327-fb7d-4311-ac7a-be7ba60cf969")).thenReturn(vfModuleCustomization3);
		when(catalogDbClient.getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a")).thenReturn(service);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"AssignServiceInstanceBB");
		assertEquals(3,ebbs.get(0).getWorkflowResourceIds().getServiceInstanceId().length());
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"AssignVnfBB");
		assertEquals(UUID.randomUUID().toString().length(),ebbs.get(1).getWorkflowResourceIds().getVnfId().length());
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"AssignVolumeGroupBB");
		assertEquals(UUID.randomUUID().toString().length(),ebbs.get(2).getWorkflowResourceIds().getVolumeGroupId().length());
		assertEquals(ebbs.get(3).getBuildingBlock().getBpmnFlowName(),"AssignVfModuleBB");
		assertEquals(UUID.randomUUID().toString().length(),ebbs.get(3).getWorkflowResourceIds().getVfModuleId().length());
		assertEquals(ebbs.get(4).getBuildingBlock().getBpmnFlowName(),"AssignVfModuleBB");
		assertEquals(UUID.randomUUID().toString().length(),ebbs.get(4).getWorkflowResourceIds().getVfModuleId().length());
		assertEquals(ebbs.get(5).getBuildingBlock().getBpmnFlowName(),"AssignVfModuleBB");
		assertEquals(UUID.randomUUID().toString().length(),ebbs.get(5).getWorkflowResourceIds().getVfModuleId().length());
		assertEquals(ebbs.get(6).getBuildingBlock().getBpmnFlowName(),"CreateVolumeGroupBB");
		assertEquals(UUID.randomUUID().toString().length(),ebbs.get(6).getWorkflowResourceIds().getVolumeGroupId().length());
		assertEquals(ebbs.get(7).getBuildingBlock().getBpmnFlowName(),"ActivateVolumeGroupBB");
		assertEquals(UUID.randomUUID().toString().length(),ebbs.get(7).getWorkflowResourceIds().getVolumeGroupId().length());
		assertEquals(ebbs.get(8).getBuildingBlock().getBpmnFlowName(),"CreateVfModuleBB");		
		assertEquals(UUID.randomUUID().toString().length(),ebbs.get(8).getWorkflowResourceIds().getVfModuleId().length());
		assertEquals(ebbs.get(9).getBuildingBlock().getBpmnFlowName(),"CreateVfModuleBB");
		assertEquals(UUID.randomUUID().toString().length(),ebbs.get(9).getWorkflowResourceIds().getVfModuleId().length());
		assertEquals(ebbs.get(10).getBuildingBlock().getBpmnFlowName(),"CreateVfModuleBB");
		assertEquals(UUID.randomUUID().toString().length(),ebbs.get(10).getWorkflowResourceIds().getVfModuleId().length());
		assertEquals(ebbs.get(11).getBuildingBlock().getBpmnFlowName(),"ActivateVfModuleBB");
		assertEquals(UUID.randomUUID().toString().length(),ebbs.get(11).getWorkflowResourceIds().getVfModuleId().length());
		assertEquals(ebbs.get(12).getBuildingBlock().getBpmnFlowName(),"ActivateVfModuleBB");
		assertEquals(UUID.randomUUID().toString().length(),ebbs.get(12).getWorkflowResourceIds().getVfModuleId().length());
		assertEquals(ebbs.get(13).getBuildingBlock().getBpmnFlowName(),"ActivateVfModuleBB");
		assertEquals(UUID.randomUUID().toString().length(),ebbs.get(13).getWorkflowResourceIds().getVfModuleId().length());
		assertEquals(ebbs.get(14).getBuildingBlock().getBpmnFlowName(),"ActivateVnfBB");
		assertEquals(UUID.randomUUID().toString().length(),ebbs.get(14).getWorkflowResourceIds().getVnfId().length());
		assertEquals(ebbs.get(15).getBuildingBlock().getBpmnFlowName(),"ActivateServiceInstanceBB");
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
		List<OrchestrationFlow> orchFlows = new LinkedList<>();
		northBoundRequest.setOrchestrationFlowList(orchFlows);	
		OrchestrationFlow orch3 = new OrchestrationFlow();
		orch3.setFlowName("DeactivateVfModuleBB");
		orchFlows.add(orch3);
		OrchestrationFlow orch4 = new OrchestrationFlow();
		orch4.setFlowName("DeleteVfModuleBB");
		orchFlows.add(orch4);
		OrchestrationFlow orch5 = new OrchestrationFlow();
		orch5.setFlowName("DeactivateVolumeGroupBB");
		orchFlows.add(orch5);
		OrchestrationFlow orch6 = new OrchestrationFlow();
		orch6.setFlowName("DeleteVolumeGroupBB");
		orchFlows.add(orch6);
		OrchestrationFlow orch7 = new OrchestrationFlow();
		orch7.setFlowName("DeactivateVnfBB");
		orchFlows.add(orch7);
		OrchestrationFlow orch8 = new OrchestrationFlow();
		orch8.setFlowName("DeactivateNetworkBB");
		orchFlows.add(orch8);	
		OrchestrationFlow orch9 = new OrchestrationFlow();
		orch9.setFlowName("DeleteNetworkBB");
		orchFlows.add(orch9);	
		OrchestrationFlow orch10 = new OrchestrationFlow();
		orch10.setFlowName("DeleteNetworkCollectionBB");
		orchFlows.add(orch10);	
		OrchestrationFlow orch11 = new OrchestrationFlow();
		orch11.setFlowName("DeactivateServiceInstanceBB");
		orchFlows.add(orch11);	
		OrchestrationFlow orch12 = new OrchestrationFlow();
		orch12.setFlowName("UnassignVfModuleBB");
		orchFlows.add(orch12);	
		OrchestrationFlow orch13 = new OrchestrationFlow();
		orch13.setFlowName("UnassignVolumeGroupBB");
		orchFlows.add(orch13);	
		OrchestrationFlow orch14 = new OrchestrationFlow();
		orch14.setFlowName("UnassignVnfBB");
		orchFlows.add(orch14);	
		OrchestrationFlow orch15 = new OrchestrationFlow();
		orch15.setFlowName("UnassignNetworkBB");
		orchFlows.add(orch15);	
		OrchestrationFlow orch16 = new OrchestrationFlow();
		orch16.setFlowName("UnassignServiceInstanceBB");
		orchFlows.add(orch16);	
		
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
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,false)).thenReturn(northBoundRequest);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"DeactivateVfModuleBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"DeactivateVfModuleBB");		
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"DeleteVfModuleBB");
		assertEquals(ebbs.get(3).getBuildingBlock().getBpmnFlowName(),"DeleteVfModuleBB");
		assertEquals(ebbs.get(4).getBuildingBlock().getBpmnFlowName(),"DeactivateVolumeGroupBB");
		assertEquals(ebbs.get(5).getBuildingBlock().getBpmnFlowName(),"DeleteVolumeGroupBB");		
		assertEquals(ebbs.get(6).getBuildingBlock().getBpmnFlowName(),"DeactivateVnfBB");
		assertEquals(ebbs.get(7).getBuildingBlock().getBpmnFlowName(),"DeactivateServiceInstanceBB");
		assertEquals(ebbs.get(8).getBuildingBlock().getBpmnFlowName(),"UnassignVfModuleBB");
		assertEquals(ebbs.get(9).getBuildingBlock().getBpmnFlowName(),"UnassignVfModuleBB");
		assertEquals(ebbs.get(10).getBuildingBlock().getBpmnFlowName(),"UnassignVolumeGroupBB");
		assertEquals(ebbs.get(11).getBuildingBlock().getBpmnFlowName(),"UnassignVnfBB");
		assertEquals(ebbs.get(12).getBuildingBlock().getBpmnFlowName(),"UnassignServiceInstanceBB");
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
		List<OrchestrationFlow> orchFlows = new LinkedList<>();
		northBoundRequest.setOrchestrationFlowList(orchFlows);		
		OrchestrationFlow orch1 = new OrchestrationFlow();
		orch1.setFlowName("UnassignVfModuleBB");
		orchFlows.add(orch1);
		OrchestrationFlow orch2 = new OrchestrationFlow();
		orch2.setFlowName("UnassignVolumeGroupBB");
		orchFlows.add(orch2);
		OrchestrationFlow orch3 = new OrchestrationFlow();
		orch3.setFlowName("UnassignVnfBB");
		orchFlows.add(orch3);
		OrchestrationFlow orch4 = new OrchestrationFlow();
		orch4.setFlowName("UnassignNetworkBB");
		orchFlows.add(orch4);
		OrchestrationFlow orch5 = new OrchestrationFlow();
		orch5.setFlowName("UnassignServiceInstanceBB");
		orchFlows.add(orch5);
		
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
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,false)).thenReturn(northBoundRequest);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"UnassignVfModuleBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"UnassignVfModuleBB");		
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"UnassignVolumeGroupBB");
		assertEquals(ebbs.get(3).getBuildingBlock().getBpmnFlowName(),"UnassignVnfBB");
		assertEquals(ebbs.get(4).getBuildingBlock().getBpmnFlowName(),"UnassignServiceInstanceBB");
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
		List<OrchestrationFlow> orchFlows = new LinkedList<>();
		northBoundRequest.setOrchestrationFlowList(orchFlows);	
		OrchestrationFlow orch3 = new OrchestrationFlow();
		orch3.setFlowName("DeactivateVfModuleBB");
		orchFlows.add(orch3);
		OrchestrationFlow orch4 = new OrchestrationFlow();
		orch4.setFlowName("DeleteVfModuleBB");
		orchFlows.add(orch4);
		OrchestrationFlow orch5 = new OrchestrationFlow();
		orch5.setFlowName("DeactivateVolumeGroupBB");
		orchFlows.add(orch5);
		OrchestrationFlow orch6 = new OrchestrationFlow();
		orch6.setFlowName("DeleteVolumeGroupBB");
		orchFlows.add(orch6);
		OrchestrationFlow orch7 = new OrchestrationFlow();
		orch7.setFlowName("DeactivateVnfBB");
		orchFlows.add(orch7);
		OrchestrationFlow orch8 = new OrchestrationFlow();
		orch8.setFlowName("DeactivateNetworkBB");
		orchFlows.add(orch8);	
		OrchestrationFlow orch9 = new OrchestrationFlow();
		orch9.setFlowName("DeleteNetworkBB");
		orchFlows.add(orch9);	
		OrchestrationFlow orch10 = new OrchestrationFlow();
		orch10.setFlowName("DeleteNetworkCollectionBB");
		orchFlows.add(orch10);	
		OrchestrationFlow orch11 = new OrchestrationFlow();
		orch11.setFlowName("DeactivateServiceInstanceBB");
		orchFlows.add(orch11);	
		OrchestrationFlow orch12 = new OrchestrationFlow();
		orch12.setFlowName("UnassignVfModuleBB");
		orchFlows.add(orch12);	
		OrchestrationFlow orch13 = new OrchestrationFlow();
		orch13.setFlowName("UnassignVolumeGroupBB");
		orchFlows.add(orch13);	
		OrchestrationFlow orch14 = new OrchestrationFlow();
		orch14.setFlowName("UnassignVnfBB");
		orchFlows.add(orch14);	
		OrchestrationFlow orch15 = new OrchestrationFlow();
		orch15.setFlowName("UnassignNetworkBB");
		orchFlows.add(orch15);	
		OrchestrationFlow orch16 = new OrchestrationFlow();
		orch16.setFlowName("UnassignServiceInstanceBB");
		orchFlows.add(orch16);	
		
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
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,false)).thenReturn(northBoundRequest);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"DeactivateNetworkBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"DeleteNetworkBB");
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"UnassignNetworkBB");
		assertEquals(ebbs.get(3).getBuildingBlock().getBpmnFlowName(),"DeactivateNetworkBB");
		assertEquals(ebbs.get(4).getBuildingBlock().getBpmnFlowName(),"DeleteNetworkBB");
		assertEquals(ebbs.get(5).getBuildingBlock().getBpmnFlowName(),"UnassignNetworkBB");
		assertEquals(ebbs.get(6).getBuildingBlock().getBpmnFlowName(),"DeleteNetworkCollectionBB");
		assertEquals(ebbs.get(7).getBuildingBlock().getBpmnFlowName(),"DeactivateServiceInstanceBB");
		assertEquals(ebbs.get(8).getBuildingBlock().getBpmnFlowName(),"UnassignServiceInstanceBB");
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
		List<OrchestrationFlow> orchFlows = new LinkedList<>();
		northBoundRequest.setOrchestrationFlowList(orchFlows);
		OrchestrationFlow orch1 = new OrchestrationFlow();
		orch1.setFlowName("CreateNetworkCollectionBB");
		orchFlows.add(orch1);
		OrchestrationFlow orch2 = new OrchestrationFlow();
		orch2.setFlowName("AssignNetworkBB");
		orchFlows.add(orch2);
		OrchestrationFlow orch3 = new OrchestrationFlow();
		orch3.setFlowName("CreateNetworkBB");
		orchFlows.add(orch3);
		OrchestrationFlow orch4 = new OrchestrationFlow();
		orch4.setFlowName("ActivateNetworkBB");
		orchFlows.add(orch4);
		OrchestrationFlow orch5 = new OrchestrationFlow();
		orch5.setFlowName("ActivateNetworkCollectionBB");
		orchFlows.add(orch5);
		
		Service service = new Service();
		CollectionResourceCustomization collectionResourceCustomization = new CollectionResourceCustomization();
		CollectionResource collectionResource = new CollectionResource();
		collectionResource.setToscaNodeType("Data NetworkCollection Data");
		collectionResourceCustomization.setCollectionResource(collectionResource);
		service.getCollectionResourceCustomizations().add(collectionResourceCustomization);
		
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,false)).thenReturn(northBoundRequest);
		when(catalogDbClient.getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a")).thenReturn(service);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"CreateNetworkCollectionBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"AssignNetworkBB");
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"CreateNetworkBB");
		assertEquals(ebbs.get(3).getBuildingBlock().getBpmnFlowName(),"ActivateNetworkBB");
		assertEquals(ebbs.get(4).getBuildingBlock().getBpmnFlowName(),"AssignNetworkBB");
		assertEquals(ebbs.get(5).getBuildingBlock().getBpmnFlowName(),"CreateNetworkBB");
		assertEquals(ebbs.get(6).getBuildingBlock().getBpmnFlowName(),"ActivateNetworkBB");
		assertEquals(ebbs.get(7).getBuildingBlock().getBpmnFlowName(),"ActivateNetworkCollectionBB");
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
		List<OrchestrationFlow> orchFlows = new LinkedList<>();
		northBoundRequest.setOrchestrationFlowList(orchFlows);		
		OrchestrationFlow orch1 = new OrchestrationFlow();
		orch1.setFlowName("DeactivateNetworkBB");
		orchFlows.add(orch1);
		OrchestrationFlow orch2 = new OrchestrationFlow();
		orch2.setFlowName("DeleteNetworkBB");
		orchFlows.add(orch2);
		OrchestrationFlow orch3 = new OrchestrationFlow();
		orch3.setFlowName("UnassignNetworkBB");
		orchFlows.add(orch3);
		OrchestrationFlow orch4 = new OrchestrationFlow();
		orch4.setFlowName("DeactivateNetworkCollectionBB");
		orchFlows.add(orch4);
		OrchestrationFlow orch5 = new OrchestrationFlow();
		orch5.setFlowName("DeleteNetworkCollectionBB");
		orchFlows.add(orch5);
		
		Service service = new Service();
		CollectionResourceCustomization collectionResourceCustomization = new CollectionResourceCustomization();
		CollectionResource collectionResource = new CollectionResource();
		collectionResource.setToscaNodeType("Data NetworkCollection Data");
		collectionResourceCustomization.setCollectionResource(collectionResource);
		service.getCollectionResourceCustomizations().add(collectionResourceCustomization);
		
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,false)).thenReturn(northBoundRequest);
		when(catalogDbClient.getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a")).thenReturn(service);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"DeactivateNetworkBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"DeleteNetworkBB");
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"UnassignNetworkBB");
		assertEquals(ebbs.get(3).getBuildingBlock().getBpmnFlowName(),"DeactivateNetworkBB");
		assertEquals(ebbs.get(4).getBuildingBlock().getBpmnFlowName(),"DeleteNetworkBB");
		assertEquals(ebbs.get(5).getBuildingBlock().getBpmnFlowName(),"UnassignNetworkBB");
		assertEquals(ebbs.get(6).getBuildingBlock().getBpmnFlowName(),"DeactivateNetworkCollectionBB");
		assertEquals(ebbs.get(7).getBuildingBlock().getBpmnFlowName(),"DeleteNetworkCollectionBB");
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
		assertEquals(sorted.get(0).getBuildingBlock().getBpmnFlowName(),"AssignNetworkBB");
		assertEquals(sorted.get(1).getBuildingBlock().getBpmnFlowName(),"CreateNetworkBB");
		assertEquals(sorted.get(2).getBuildingBlock().getBpmnFlowName(),"ActivateNetworkBB");
		assertEquals(sorted.get(3).getBuildingBlock().getBpmnFlowName(),"AssignNetworkBB");
		assertEquals(sorted.get(4).getBuildingBlock().getBpmnFlowName(),"CreateNetworkBB");
		assertEquals(sorted.get(5).getBuildingBlock().getBpmnFlowName(),"ActivateNetworkBB");
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
		assertEquals(sorted.get(0).getBuildingBlock().getBpmnFlowName(),"DeactivateNetworkBB");
		assertEquals(sorted.get(1).getBuildingBlock().getBpmnFlowName(),"DeleteNetworkBB");
		assertEquals(sorted.get(2).getBuildingBlock().getBpmnFlowName(),"UnassignNetworkBB");
		assertEquals(sorted.get(3).getBuildingBlock().getBpmnFlowName(),"DeactivateNetworkBB");
		assertEquals(sorted.get(4).getBuildingBlock().getBpmnFlowName(),"DeleteNetworkBB");
		assertEquals(sorted.get(5).getBuildingBlock().getBpmnFlowName(),"UnassignNetworkBB");
	}
	@Test
	public void queryNorthBoundRequestCatalogDbNestedTest() throws MalformedURLException {
		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		List<OrchestrationFlow> orchFlows = new LinkedList<>();
		OrchestrationFlow orch1 = new OrchestrationFlow();
		orch1.setFlowName("AAICheckVnfInMaintBB");
		orchFlows.add(orch1);
		OrchestrationFlow orch2 = new OrchestrationFlow();
		orch2.setFlowName("AAISetVnfInMaintBB");
		orchFlows.add(orch2);
		OrchestrationFlow orch3 = new OrchestrationFlow();
		orch3.setFlowName("VNF-Macro-Replace");
		orchFlows.add(orch3);
		OrchestrationFlow orch4 = new OrchestrationFlow();
		orch4.setFlowName("SDNOVnfHealthCheckBB");
		orchFlows.add(orch4);
		OrchestrationFlow orch5 = new OrchestrationFlow();
		orch5.setFlowName("AAIUnsetVnfInMaintBB");
		orchFlows.add(orch5);
		northBoundRequest.setOrchestrationFlowList(orchFlows);
		
		List<OrchestrationFlow> macroFlows = new LinkedList<>();
		OrchestrationFlow o1 = new OrchestrationFlow();
		o1.setFlowName("DeactivateVfModuleBB");
		macroFlows.add(o1);
		OrchestrationFlow o2 = new OrchestrationFlow();
		o2.setFlowName("DeleteVfModuleBB");
		macroFlows.add(o2);
		OrchestrationFlow o3 = new OrchestrationFlow();
		o3.setFlowName("DeactivateVnfBB");
		macroFlows.add(o3);
		OrchestrationFlow o4 = new OrchestrationFlow();
		o4.setFlowName("CreateVfModuleBB");
		macroFlows.add(o4);
		OrchestrationFlow o5 = new OrchestrationFlow();
		o5.setFlowName("ActivateVfModuleBB");
		macroFlows.add(o5);
		OrchestrationFlow o6 = new OrchestrationFlow();
		o6.setFlowName("ActivateVnfBB");
		macroFlows.add(o6);
		
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope("replaceInstance","Vnf",false)).thenReturn(northBoundRequest);
		when(catalogDbClient.getOrchestrationFlowByAction("VNF-Macro-Replace")).thenReturn(macroFlows);
		List<OrchestrationFlow> flows = workflowAction.queryNorthBoundRequestCatalogDb(execution, "replaceInstance", WorkflowType.VNF, false);
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
	public void extractResourceIdAndTypeFromUriTest(){
		String uri = "/v6/serviceInstances/123";
		String uri2 = "/v6/serviceInstances/123/vnfs/1234";
		String uri3 = "/v6/serviceInstances";
		String uri4 = "/v6/serviceInstances/assign";
		String uri5 = "'/v6/serviceInstances/123/vnfs";
		String uri6 = "/v6/serviceInstances/123/vnfs/1234/someAction";
		String uri7 = "/v6/serviceInstances/123/vnfs/1234/vfModules/5678/replace";
		
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
}
