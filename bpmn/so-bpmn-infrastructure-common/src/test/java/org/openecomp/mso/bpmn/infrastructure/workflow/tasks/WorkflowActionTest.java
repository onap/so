package org.openecomp.mso.bpmn.infrastructure.workflow.tasks;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.L3Network;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.VfModule;
import org.onap.aai.domain.yang.VfModules;
import org.onap.aai.domain.yang.VolumeGroup;
import org.openecomp.mso.bpmn.BaseTaskTest;
import org.openecomp.mso.bpmn.servicedecomposition.entities.BuildingBlock;
import org.openecomp.mso.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import org.openecomp.mso.bpmn.servicedecomposition.entities.WorkflowResourceIds;
import org.openecomp.mso.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.openecomp.mso.db.catalog.beans.CollectionResource;
import org.openecomp.mso.db.catalog.beans.CollectionResourceCustomization;
import org.openecomp.mso.db.catalog.beans.HeatEnvironment;
import org.openecomp.mso.db.catalog.beans.Service;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;
import org.openecomp.mso.db.catalog.beans.macro.NorthBoundRequest;
import org.openecomp.mso.db.catalog.beans.macro.OrchestrationFlow;
import org.openecomp.mso.serviceinstancebeans.ModelInfo;
import org.openecomp.mso.serviceinstancebeans.RequestDetails;
import org.openecomp.mso.serviceinstancebeans.RequestParameters;
import org.openecomp.mso.serviceinstancebeans.SubscriberInfo;
import org.springframework.beans.factory.annotation.Autowired;

public class WorkflowActionTest extends BaseTaskTest {
	@Autowired
	protected WorkflowAction workflowAction;
	
	private DelegateExecution execution;
	
	@Mock
	BBInputSetupUtils bbSetupUtils;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Before
	public void before() throws Exception {
		execution = new ExecutionImpl();
		org.onap.aai.domain.yang.ServiceInstance servInstance = new org.onap.aai.domain.yang.ServiceInstance();
		servInstance.setServiceInstanceId("TEST");
		when(bbSetupUtils.getAAIServiceInstanceByName(anyString(), anyObject())).thenReturn(servInstance);
		workflowAction.setBbInputSetupUtils(bbSetupUtils);
	}
	
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
		
		ModelInfo modelInfo = new ModelInfo();
		modelInfo.setModelCustomizationId("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
		VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
		vfModuleCustomization.setModelCustomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
		HeatEnvironment volumeHeatEnv = new HeatEnvironment();
		vfModuleCustomization.setVolumeHeatEnv(volumeHeatEnv);
		
		VfModuleCustomization vfModuleCustomization2 = new VfModuleCustomization();
		ModelInfo modelInfo2 = new ModelInfo();
		modelInfo2.setModelCustomizationId("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");
		VfModuleCustomization vfModuleCustomization3 = new VfModuleCustomization();
		ModelInfo modelInfo3 = new ModelInfo();
		modelInfo3.setModelCustomizationId("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");
		VfModuleCustomization vfModuleCustomization4 = new VfModuleCustomization();
		ModelInfo modelInfo4 = new ModelInfo();
		modelInfo4.setModelCustomizationId("123");
		
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,false)).thenReturn(northBoundRequest);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f")).thenReturn(vfModuleCustomization);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8")).thenReturn(vfModuleCustomization2);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8")).thenReturn(vfModuleCustomization3);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("da4d4327-fb7d-4311-ac7a-be7ba60cf969")).thenReturn(vfModuleCustomization4);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"AssignServiceInstanceBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"AssignVnfBB");
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"AssignVolumeGroupBB");
		assertEquals(ebbs.get(3).getBuildingBlock().getBpmnFlowName(),"AssignVfModuleBB");
		assertEquals(ebbs.get(4).getBuildingBlock().getBpmnFlowName(),"AssignVfModuleBB");
		assertEquals(ebbs.get(5).getBuildingBlock().getBpmnFlowName(),"AssignVfModuleBB");
		assertEquals(ebbs.get(6).getBuildingBlock().getBpmnFlowName(),"AssignVfModuleBB");
	}
	
	@Test
	public void selectExecutionListServiceMacroActivateTest() throws Exception{
		String gAction = "activateInstance";
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
		
		VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
		ModelInfo modelInfo = new ModelInfo();
		modelInfo.setModelCustomizationId("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
		VfModuleCustomization vfModuleCustomization2 = new VfModuleCustomization();
		ModelInfo modelInfo2 = new ModelInfo();
		modelInfo2.setModelCustomizationId("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");
		VfModuleCustomization vfModuleCustomization3 = new VfModuleCustomization();
		ModelInfo modelInfo3 = new ModelInfo();
		modelInfo3.setModelCustomizationId("da4d4327-fb7d-4311-ac7a-be7ba60cf969");
		
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,false)).thenReturn(northBoundRequest);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f")).thenReturn(vfModuleCustomization);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8")).thenReturn(vfModuleCustomization2);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("da4d4327-fb7d-4311-ac7a-be7ba60cf969")).thenReturn(vfModuleCustomization3);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"CreateVfModuleBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"CreateVfModuleBB");		
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"CreateVfModuleBB");
		assertEquals(ebbs.get(3).getBuildingBlock().getBpmnFlowName(),"CreateVfModuleBB");
		assertEquals(ebbs.get(4).getBuildingBlock().getBpmnFlowName(),"ActivateVfModuleBB");
		assertEquals(ebbs.get(5).getBuildingBlock().getBpmnFlowName(),"ActivateVfModuleBB");		
		assertEquals(ebbs.get(6).getBuildingBlock().getBpmnFlowName(),"ActivateVfModuleBB");
		assertEquals(ebbs.get(7).getBuildingBlock().getBpmnFlowName(),"ActivateVfModuleBB");
		assertEquals(ebbs.get(8).getBuildingBlock().getBpmnFlowName(),"ActivateVnfBB");
		assertEquals(ebbs.get(9).getBuildingBlock().getBpmnFlowName(),"ActivateServiceInstanceBB");
	}
	

	@Test
	public void selectExecutionListServiceMacroCreateNoNetworkNoNetworkCollectionTest() throws Exception{
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
		
		VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
		ModelInfo modelInfo = new ModelInfo();
		modelInfo.setModelCustomizationId("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
		VfModuleCustomization vfModuleCustomization2 = new VfModuleCustomization();
		ModelInfo modelInfo2 = new ModelInfo();
		modelInfo2.setModelCustomizationId("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");
		VfModuleCustomization vfModuleCustomization3 = new VfModuleCustomization();
		ModelInfo modelInfo3 = new ModelInfo();
		modelInfo3.setModelCustomizationId("da4d4327-fb7d-4311-ac7a-be7ba60cf969");
		
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,false)).thenReturn(northBoundRequest);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f")).thenReturn(vfModuleCustomization);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8")).thenReturn(vfModuleCustomization2);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("da4d4327-fb7d-4311-ac7a-be7ba60cf969")).thenReturn(vfModuleCustomization3);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"AssignServiceInstanceBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"AssignVnfBB");
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"AssignVfModuleBB");
		assertEquals(ebbs.get(3).getBuildingBlock().getBpmnFlowName(),"AssignVfModuleBB");
		assertEquals(ebbs.get(4).getBuildingBlock().getBpmnFlowName(),"AssignVfModuleBB");
		assertEquals(ebbs.get(5).getBuildingBlock().getBpmnFlowName(),"AssignVfModuleBB");
		assertEquals(ebbs.get(6).getBuildingBlock().getBpmnFlowName(),"CreateVfModuleBB");
		assertEquals(ebbs.get(7).getBuildingBlock().getBpmnFlowName(),"CreateVfModuleBB");
		assertEquals(ebbs.get(8).getBuildingBlock().getBpmnFlowName(),"CreateVfModuleBB");		
		assertEquals(ebbs.get(9).getBuildingBlock().getBpmnFlowName(),"CreateVfModuleBB");
		assertEquals(ebbs.get(10).getBuildingBlock().getBpmnFlowName(),"ActivateVfModuleBB");
		assertEquals(ebbs.get(11).getBuildingBlock().getBpmnFlowName(),"ActivateVfModuleBB");
		assertEquals(ebbs.get(12).getBuildingBlock().getBpmnFlowName(),"ActivateVfModuleBB");		
		assertEquals(ebbs.get(13).getBuildingBlock().getBpmnFlowName(),"ActivateVfModuleBB");
		assertEquals(ebbs.get(14).getBuildingBlock().getBpmnFlowName(),"ActivateVnfBB");
		assertEquals(ebbs.get(15).getBuildingBlock().getBpmnFlowName(),"ActivateServiceInstanceBB");
	}
	
	@Test
	public void selectExecutionListServiceMacroCreateNoNetworkCollectionTest() throws Exception{
		String gAction = "createInstance";
		String resource = "Service";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/CreateNetworkCollection.json")));
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
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,false)).thenReturn(northBoundRequest);
		when(catalogDbClient.getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a")).thenReturn(service);

		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"AssignServiceInstanceBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"AssignNetworkBB");
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"AssignNetworkBB");
		assertEquals(ebbs.get(3).getBuildingBlock().getBpmnFlowName(),"CreateNetworkBB");
		assertEquals(ebbs.get(4).getBuildingBlock().getBpmnFlowName(),"CreateNetworkBB");
		assertEquals(ebbs.get(5).getBuildingBlock().getBpmnFlowName(),"ActivateNetworkBB");
		assertEquals(ebbs.get(6).getBuildingBlock().getBpmnFlowName(),"ActivateNetworkBB");
		assertEquals(ebbs.get(7).getBuildingBlock().getBpmnFlowName(),"ActivateServiceInstanceBB");
	}
	
	@Test
	public void selectExecutionListServiceMacroCreateWithNetworkCollectionTest() throws Exception{
		String gAction = "createInstance";
		String resource = "Service";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/CreateNetworkCollection.json")));
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
		CollectionResourceCustomization collectionResourceCustomization = new CollectionResourceCustomization();
		CollectionResource collectionResource = new CollectionResource();
		collectionResource.setToscaNodeType("Data NetworkCollection Data");
		collectionResourceCustomization.setCollectionResource(collectionResource);
		service.setCollectionResourceCustomization(collectionResourceCustomization);
		
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,false)).thenReturn(northBoundRequest);
		when(catalogDbClient.getServiceByID("3c40d244-808e-42ca-b09a-256d83d19d0a")).thenReturn(service);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"AssignServiceInstanceBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"CreateNetworkCollectionBB");
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"AssignNetworkBB");
		assertEquals(ebbs.get(3).getBuildingBlock().getBpmnFlowName(),"CreateNetworkBB");
		assertEquals(ebbs.get(4).getBuildingBlock().getBpmnFlowName(),"ActivateNetworkBB");
		assertEquals(ebbs.get(5).getBuildingBlock().getBpmnFlowName(),"AssignNetworkBB");
		assertEquals(ebbs.get(6).getBuildingBlock().getBpmnFlowName(),"CreateNetworkBB");
		assertEquals(ebbs.get(7).getBuildingBlock().getBpmnFlowName(),"ActivateNetworkBB");
		assertEquals(ebbs.get(8).getBuildingBlock().getBpmnFlowName(),"ActivateNetworkCollectionBB");		
		assertEquals(ebbs.get(9).getBuildingBlock().getBpmnFlowName(),"ActivateServiceInstanceBB");
	}

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
		service.setCollectionResourceCustomization(collectionResourceCustomization);
		
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
		service.setCollectionResourceCustomization(collectionResourceCustomization);
		
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
	
	@Test
	public void selectExecutionListServiceMacroDeleteNetworkCollectionTest() throws Exception{
		String gAction = "deleteInstance";
		String resource = "Service";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		String bpmnRequest = new String(Files.readAllBytes(Paths.get("src/test/resources/__files/Macro/CreateNetworkCollection.json")));
		execution.setVariable("bpmnRequest", bpmnRequest);			
		execution.setVariable("aLaCarte", false);
		execution.setVariable("apiVersion", "7");
		execution.setVariable("requestUri", "v6/serviceInstances/123");
		
		NorthBoundRequest northBoundRequest = new NorthBoundRequest();
		List<OrchestrationFlow> orchFlows = new LinkedList<>();
		northBoundRequest.setOrchestrationFlowList(orchFlows);				
		OrchestrationFlow orch1 = new OrchestrationFlow();
		orch1.setFlowName("DeactivateVfModuleBB");
		orchFlows.add(orch1);
		OrchestrationFlow orch2 = new OrchestrationFlow();
		orch2.setFlowName("DeleteVfModuleBB");
		orchFlows.add(orch2);
		OrchestrationFlow orch3 = new OrchestrationFlow();
		orch3.setFlowName("DeactivateVolumeGroupBB");
		orchFlows.add(orch3);
		OrchestrationFlow orch4 = new OrchestrationFlow();
		orch4.setFlowName("DeleteVolumeGroupBB");
		orchFlows.add(orch4);
		OrchestrationFlow orch5 = new OrchestrationFlow();
		orch5.setFlowName("DeactivateVnfBB");
		orchFlows.add(orch5);
		OrchestrationFlow orch6 = new OrchestrationFlow();
		orch6.setFlowName("DeactivateNetworkBB");
		orchFlows.add(orch6);
		OrchestrationFlow orch7 = new OrchestrationFlow();
		orch7.setFlowName("DeleteNetworkBB");
		orchFlows.add(orch7);
		OrchestrationFlow orch8 = new OrchestrationFlow();
		orch8.setFlowName("DeactivateNetworkCollectionBB");
		orchFlows.add(orch8);
		OrchestrationFlow orch9 = new OrchestrationFlow();
		orch9.setFlowName("DeleteNetworkCollectionBB");
		orchFlows.add(orch9);
		OrchestrationFlow orch10 = new OrchestrationFlow();
		orch10.setFlowName("DeactivateServiceInstanceBB");
		orchFlows.add(orch10);
		OrchestrationFlow orch11 = new OrchestrationFlow();
		orch11.setFlowName("UnassignVfModuleBB");
		orchFlows.add(orch11);
		OrchestrationFlow orch12 = new OrchestrationFlow();
		orch12.setFlowName("UnassignVolumeGroupBB");
		orchFlows.add(orch12);
		OrchestrationFlow orch13 = new OrchestrationFlow();
		orch13.setFlowName("UnassignVnfBB");
		orchFlows.add(orch13);
		OrchestrationFlow orch14 = new OrchestrationFlow();
		orch14.setFlowName("UnassignNetworkBB");
		orchFlows.add(orch14);
		OrchestrationFlow orch15 = new OrchestrationFlow();
		orch15.setFlowName("UnassignServiceInstanceBB");
		orchFlows.add(orch15);
		
		Service service = new Service();
		CollectionResourceCustomization collectionResourceCustomization = new CollectionResourceCustomization();
		CollectionResource collectionResource = new CollectionResource();
		collectionResource.setToscaNodeType("Data NetworkCollection Data");
		collectionResourceCustomization.setCollectionResource(collectionResource);
		service.setCollectionResourceCustomization(collectionResourceCustomization);
		
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
		assertEquals(ebbs.get(8).getBuildingBlock().getBpmnFlowName(),"DeactivateServiceInstanceBB");
		assertEquals(ebbs.get(9).getBuildingBlock().getBpmnFlowName(),"UnassignServiceInstanceBB");
	}
	
	@Test
	public void selectExecutionListServiceMacroDeactivateTest() throws Exception{
		String gAction = "deactivateInstance";
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
		orch1.setFlowName("DeactivateVfModuleBB");
		orchFlows.add(orch1);
		OrchestrationFlow orch2 = new OrchestrationFlow();
		orch2.setFlowName("DeleteVfModuleBB");
		orchFlows.add(orch2);
		OrchestrationFlow orch3 = new OrchestrationFlow();
		orch3.setFlowName("DeactivateVolumeGroupBB");
		orchFlows.add(orch3);
		OrchestrationFlow orch4 = new OrchestrationFlow();
		orch4.setFlowName("DeleteVolumeGroupBB");
		orchFlows.add(orch4);
		OrchestrationFlow orch5 = new OrchestrationFlow();
		orch5.setFlowName("DeactivateVnfBB");
		orchFlows.add(orch5);
		OrchestrationFlow orch6 = new OrchestrationFlow();
		orch6.setFlowName("DeactivateServiceInstanceBB");
		orchFlows.add(orch6);		
		
		VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
		ModelInfo modelInfo = new ModelInfo();
		modelInfo.setModelCustomizationId("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
		VfModuleCustomization vfModuleCustomization2 = new VfModuleCustomization();
		ModelInfo modelInfo2 = new ModelInfo();
		modelInfo2.setModelCustomizationId("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");
		VfModuleCustomization vfModuleCustomization3 = new VfModuleCustomization();
		ModelInfo modelInfo3 = new ModelInfo();
		modelInfo3.setModelCustomizationId("da4d4327-fb7d-4311-ac7a-be7ba60cf969");
		
		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,false)).thenReturn(northBoundRequest);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f")).thenReturn(vfModuleCustomization);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8")).thenReturn(vfModuleCustomization2);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("da4d4327-fb7d-4311-ac7a-be7ba60cf969")).thenReturn(vfModuleCustomization3);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"DeactivateVfModuleBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"DeactivateVfModuleBB");		
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"DeactivateVfModuleBB");
		assertEquals(ebbs.get(3).getBuildingBlock().getBpmnFlowName(),"DeactivateVfModuleBB");
		assertEquals(ebbs.get(4).getBuildingBlock().getBpmnFlowName(),"DeleteVfModuleBB");
		assertEquals(ebbs.get(5).getBuildingBlock().getBpmnFlowName(),"DeleteVfModuleBB");		
		assertEquals(ebbs.get(6).getBuildingBlock().getBpmnFlowName(),"DeleteVfModuleBB");
		assertEquals(ebbs.get(7).getBuildingBlock().getBpmnFlowName(),"DeleteVfModuleBB");
		assertEquals(ebbs.get(8).getBuildingBlock().getBpmnFlowName(),"DeactivateVnfBB");
		assertEquals(ebbs.get(9).getBuildingBlock().getBpmnFlowName(),"DeactivateServiceInstanceBB");
	}
	
	@Test
	public void selectBBTest() throws Exception{
		String gAction = "Delete-Network-Collection";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		execution.setVariable("gCurrentSequence", 0);
		execution.setVariable("homing", false);
		List<ExecuteBuildingBlock> flowsToExecute = new ArrayList();
		ExecuteBuildingBlock ebb = new ExecuteBuildingBlock();
		flowsToExecute.add(ebb);
		execution.setVariable("flowsToExecute", flowsToExecute);
		workflowAction.selectBB(execution);
		boolean success = (boolean) execution.getVariable("completed");
		assertEquals(true,success);
	}
	
	@Test
	public void select2BBTest() throws Exception{
		String gAction = "Delete-Network-Collection";
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", gAction);
		execution.setVariable("gCurrentSequence", 0);
		execution.setVariable("homing", false);
		List<ExecuteBuildingBlock> flowsToExecute = new ArrayList();
		ExecuteBuildingBlock ebb = new ExecuteBuildingBlock();
		ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock();
		flowsToExecute.add(ebb);
		flowsToExecute.add(ebb2);
		execution.setVariable("flowsToExecute", flowsToExecute);
		workflowAction.selectBB(execution);
		boolean success = (boolean) execution.getVariable("completed");
		assertEquals(false,success);
	}
	
	@Test
	public void msoCompleteProcessTest() throws Exception{
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("requestAction", "createInstance");
		execution.setVariable("resourceId", "123");
		execution.setVariable("source","MSO");
		execution.setVariable("resourceName", "Service");
		execution.setVariable("aLaCarte", true);
		workflowAction.setupCompleteMsoProcess(execution);
		String response = (String) execution.getVariable("CompleteMsoProcessRequest");
		assertEquals(response,"<aetgt:MsoCompletionRequest xmlns:aetgt=\"http://org.openecomp/mso/workflow/schema/v1\" xmlns:ns=\"http://org.openecomp/mso/request/types/v1\"><request-info xmlns=\"http://org.openecomp/mso/infra/vnf-request/v1\"><request-id>00f704ca-c5e5-4f95-a72c-6889db7b0688</request-id><action>createInstance</action><source>MSO</source></request-info><status-message>ALaCarte-Service-createInstance request was executed correctly.</status-message><serviceInstanceId>123</serviceInstanceId><mso-bpel-name>WorkflowActionBB</mso-bpel-name></aetgt:MsoCompletionRequest>");
	}
	
	@Test
	public void sortByCompleteResourceObjectTest() throws Exception{
		List<ExecuteBuildingBlock> executeFlows = new ArrayList();
		ExecuteBuildingBlock ebb = new ExecuteBuildingBlock();
		BuildingBlock bb = new BuildingBlock();
		bb.setBpmnFlowName("AssignNetworkBB");
		bb.setSequenceNumber(0);
		ebb.setBuildingBlock(bb);
		executeFlows.add(ebb);
		ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock();
		BuildingBlock bb2 = new BuildingBlock();
		bb2.setBpmnFlowName("AssignNetworkBB");
		bb2.setSequenceNumber(1);
		ebb2.setBuildingBlock(bb2);
		executeFlows.add(ebb2);
		ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock();
		BuildingBlock bb3 = new BuildingBlock();
		bb3.setBpmnFlowName("CreateNetworkBB");
		bb3.setSequenceNumber(0);
		ebb3.setBuildingBlock(bb3);
		executeFlows.add(ebb3);
		ExecuteBuildingBlock ebb4 = new ExecuteBuildingBlock();
		BuildingBlock bb4 = new BuildingBlock();
		bb4.setBpmnFlowName("CreateNetworkBB");
		bb4.setSequenceNumber(1);
		ebb4.setBuildingBlock(bb4);
		executeFlows.add(ebb4);
		ExecuteBuildingBlock ebb5 = new ExecuteBuildingBlock();
		BuildingBlock bb5 = new BuildingBlock();
		bb5.setBpmnFlowName("ActivateNetworkBB");
		bb5.setSequenceNumber(0);
		ebb5.setBuildingBlock(bb5);
		executeFlows.add(ebb5);
		ExecuteBuildingBlock ebb6 = new ExecuteBuildingBlock();
		BuildingBlock bb6 = new BuildingBlock();
		bb6.setBpmnFlowName("ActivateNetworkBB");
		bb6.setSequenceNumber(1);
		ebb6.setBuildingBlock(bb6);
		executeFlows.add(ebb6);
		List<ExecuteBuildingBlock> sorted = workflowAction.sortByCompleteResourceObject(executeFlows,"createInstance");
		assertEquals(sorted.get(0).getBuildingBlock().getBpmnFlowName(),"AssignNetworkBB");
		assertEquals(sorted.get(1).getBuildingBlock().getBpmnFlowName(),"CreateNetworkBB");
		assertEquals(sorted.get(2).getBuildingBlock().getBpmnFlowName(),"ActivateNetworkBB");
		assertEquals(sorted.get(3).getBuildingBlock().getBpmnFlowName(),"AssignNetworkBB");
		assertEquals(sorted.get(4).getBuildingBlock().getBpmnFlowName(),"CreateNetworkBB");
		assertEquals(sorted.get(5).getBuildingBlock().getBpmnFlowName(),"ActivateNetworkBB");
	}
	
	@Test
	public void sortByCompleteResourceObjectDeleteTest() throws Exception{
		List<ExecuteBuildingBlock> executeFlows = new ArrayList();
		ExecuteBuildingBlock ebb = new ExecuteBuildingBlock();
		BuildingBlock bb = new BuildingBlock();
		bb.setBpmnFlowName("DeactivateNetworkBB");
		bb.setSequenceNumber(0);
		ebb.setBuildingBlock(bb);
		executeFlows.add(ebb);
		ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock();
		BuildingBlock bb2 = new BuildingBlock();
		bb2.setBpmnFlowName("DeactivateNetworkBB");
		bb2.setSequenceNumber(1);
		ebb2.setBuildingBlock(bb2);
		executeFlows.add(ebb2);
		ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock();
		BuildingBlock bb3 = new BuildingBlock();
		bb3.setBpmnFlowName("DeleteNetworkBB");
		bb3.setSequenceNumber(0);
		ebb3.setBuildingBlock(bb3);
		executeFlows.add(ebb3);
		ExecuteBuildingBlock ebb4 = new ExecuteBuildingBlock();
		BuildingBlock bb4 = new BuildingBlock();
		bb4.setBpmnFlowName("DeleteNetworkBB");
		bb4.setSequenceNumber(1);
		ebb4.setBuildingBlock(bb4);
		executeFlows.add(ebb4);
		ExecuteBuildingBlock ebb5 = new ExecuteBuildingBlock();
		BuildingBlock bb5 = new BuildingBlock();
		bb5.setBpmnFlowName("UnassignNetworkBB");
		bb5.setSequenceNumber(0);
		ebb5.setBuildingBlock(bb5);
		executeFlows.add(ebb5);
		ExecuteBuildingBlock ebb6 = new ExecuteBuildingBlock();
		BuildingBlock bb6 = new BuildingBlock();
		bb6.setBpmnFlowName("UnassignNetworkBB");
		bb6.setSequenceNumber(1);
		ebb6.setBuildingBlock(bb6);
		executeFlows.add(ebb6);
		List<ExecuteBuildingBlock> sorted = workflowAction.sortByCompleteResourceObject(executeFlows,"deleteInstance");
		assertEquals(sorted.get(0).getBuildingBlock().getBpmnFlowName(),"DeactivateNetworkBB");
		assertEquals(sorted.get(1).getBuildingBlock().getBpmnFlowName(),"DeleteNetworkBB");
		assertEquals(sorted.get(2).getBuildingBlock().getBpmnFlowName(),"UnassignNetworkBB");
		assertEquals(sorted.get(3).getBuildingBlock().getBpmnFlowName(),"DeactivateNetworkBB");
		assertEquals(sorted.get(4).getBuildingBlock().getBpmnFlowName(),"DeleteNetworkBB");
		assertEquals(sorted.get(5).getBuildingBlock().getBpmnFlowName(),"UnassignNetworkBB");
	}
	@Test
	public void selectExecutionListServiceMacroUnassignTest() throws Exception{
		String gAction = "unassignInstance";
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
		
		ModelInfo modelInfo = new ModelInfo();
		modelInfo.setModelCustomizationId("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
		VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
		vfModuleCustomization.setModelCustomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
		HeatEnvironment volumeHeatEnv = new HeatEnvironment();
		vfModuleCustomization.setVolumeHeatEnv(volumeHeatEnv);	
		
		VfModuleCustomization vfModuleCustomization2 = new VfModuleCustomization();
		ModelInfo modelInfo2 = new ModelInfo();
		modelInfo2.setModelCustomizationId("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");
		
		VfModuleCustomization vfModuleCustomization3 = new VfModuleCustomization();
		ModelInfo modelInfo3 = new ModelInfo();
		modelInfo3.setModelCustomizationId("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");
		
		VfModuleCustomization vfModuleCustomization4 = new VfModuleCustomization();
		ModelInfo modelInfo4 = new ModelInfo();
		modelInfo4.setModelCustomizationId("123");

		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,false)).thenReturn(northBoundRequest);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f")).thenReturn(vfModuleCustomization);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8")).thenReturn(vfModuleCustomization2);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("da4d4327-fb7d-4311-ac7a-be7ba60cf969")).thenReturn(vfModuleCustomization3);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("da4d4327-fb7d-4311-ac7a-be7ba60cf969")).thenReturn(vfModuleCustomization4);
		workflowAction.selectExecutionList(execution); 
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"UnassignVfModuleBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"UnassignVfModuleBB");
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"UnassignVfModuleBB");
		assertEquals(ebbs.get(3).getBuildingBlock().getBpmnFlowName(),"UnassignVfModuleBB");	
		assertEquals(ebbs.get(4).getBuildingBlock().getBpmnFlowName(),"UnassignVolumeGroupBB");
		assertEquals(ebbs.get(5).getBuildingBlock().getBpmnFlowName(),"UnassignVnfBB");
		assertEquals(ebbs.get(6).getBuildingBlock().getBpmnFlowName(),"UnassignServiceInstanceBB");
	}
	
	@Test
	public void setupFalloutHandlerTest(){
		execution.setVariable("mso-request-id", "00f704ca-c5e5-4f95-a72c-6889db7b0688");
		execution.setVariable("serviceInstanceId", "123");
		execution.setVariable("WorkflowActionErrorMessage", "Error in WorkFlowAction");
		execution.setVariable("requestAction", "createInstance");
		workflowAction.setupFalloutHandler(execution);
		assertEquals(execution.getVariable("falloutRequest"),"<aetgt:FalloutHandlerRequest xmlns:aetgt=\"http://org.openecomp/mso/workflow/schema/v1\"xmlns:ns=\"http://org.openecomp/mso/request/types/v1\"xmlns:wfsch=\"http://org.openecomp/mso/workflow/schema/v1\"><request-info xmlns=\"http://org.openecomp/mso/infra/vnf-request/v1\"><request-id>00f704ca-c5e5-4f95-a72c-6889db7b0688</request-id><action>createInstance</action><source>VID</source></request-info><aetgt:WorkflowException xmlns:aetgt=\"http://org.openecomp/mso/workflow/schema/v1\"><aetgt:ErrorMessage>Error in WorkFlowAction</aetgt:ErrorMessage><aetgt:ErrorCode>7000</aetgt:ErrorCode></aetgt:WorkflowException></aetgt:FalloutHandlerRequest>");
	}
	
	@Test
	public void selectExecutionListServiceMacroDeleteTest() throws Exception{
		String gAction = "deleteInstance";
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
		orch1.setFlowName("DeactivateVfModuleBB");
		orchFlows.add(orch1);
		OrchestrationFlow orch2 = new OrchestrationFlow();
		orch2.setFlowName("DeleteVfModuleBB");
		orchFlows.add(orch2);
		OrchestrationFlow orch3 = new OrchestrationFlow();
		orch3.setFlowName("DeactivateVolumeGroupfBB");
		orchFlows.add(orch3);
		OrchestrationFlow orch4 = new OrchestrationFlow();
		orch4.setFlowName("DeleteVolumeGroupfBB");
		orchFlows.add(orch4);
		OrchestrationFlow orch5 = new OrchestrationFlow();
		orch5.setFlowName("DeactivateVnfBB");
		orchFlows.add(orch5);
		OrchestrationFlow orch6 = new OrchestrationFlow();
		orch6.setFlowName("DeactivateNetworkBB");
		orchFlows.add(orch6);
		OrchestrationFlow orch7 = new OrchestrationFlow();
		orch7.setFlowName("DeleteNetworkBB");
		orchFlows.add(orch7);
		OrchestrationFlow orch8 = new OrchestrationFlow();
		orch8.setFlowName("DeactivateServiceInstanceBB");
		orchFlows.add(orch8);
		OrchestrationFlow orch9 = new OrchestrationFlow();
		orch9.setFlowName("UnassignVfModuleBB");
		orchFlows.add(orch9);
		OrchestrationFlow orch10 = new OrchestrationFlow();
		orch10.setFlowName("UnassignVolumeGroupBB");
		orchFlows.add(orch10);
		OrchestrationFlow orch11 = new OrchestrationFlow();
		orch11.setFlowName("UnassignVnfBB");
		orchFlows.add(orch11);		
		OrchestrationFlow orch12 = new OrchestrationFlow();
		orch12.setFlowName("UnassignNetworkBB");
		orchFlows.add(orch12);
		OrchestrationFlow orch13 = new OrchestrationFlow();
		orch13.setFlowName("UnassignServiceInstanceBB");
		orchFlows.add(orch13);
		
		ModelInfo modelInfo = new ModelInfo();
		modelInfo.setModelCustomizationId("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
		VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
		vfModuleCustomization.setModelCustomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f");
		HeatEnvironment volumeHeatEnv = new HeatEnvironment();
		vfModuleCustomization.setVolumeHeatEnv(volumeHeatEnv);	
		
		VfModuleCustomization vfModuleCustomization2 = new VfModuleCustomization();
		ModelInfo modelInfo2 = new ModelInfo();
		modelInfo2.setModelCustomizationId("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");
		
		VfModuleCustomization vfModuleCustomization3 = new VfModuleCustomization();
		ModelInfo modelInfo3 = new ModelInfo();
		modelInfo3.setModelCustomizationId("72d9d1cd-f46d-447a-abdb-451d6fb05fa8");
		
		VfModuleCustomization vfModuleCustomization4 = new VfModuleCustomization();
		ModelInfo modelInfo4 = new ModelInfo();
		modelInfo4.setModelCustomizationId("123");

		when(catalogDbClient.getNorthBoundRequestByActionAndIsALaCarteAndRequestScope(gAction,resource,false)).thenReturn(northBoundRequest);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("a25e8e8c-58b8-4eec-810c-97dcc1f5cb7f")).thenReturn(vfModuleCustomization);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("72d9d1cd-f46d-447a-abdb-451d6fb05fa8")).thenReturn(vfModuleCustomization2);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("da4d4327-fb7d-4311-ac7a-be7ba60cf969")).thenReturn(vfModuleCustomization3);
		when(catalogDbClient.getVfModuleCustomizationByModelCuztomizationUUID("da4d4327-fb7d-4311-ac7a-be7ba60cf969")).thenReturn(vfModuleCustomization4);
		workflowAction.selectExecutionList(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"DeactivateVfModuleBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"DeactivateVfModuleBB");
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"DeactivateVfModuleBB");
		assertEquals(ebbs.get(3).getBuildingBlock().getBpmnFlowName(),"DeactivateVfModuleBB");	
		assertEquals(ebbs.get(4).getBuildingBlock().getBpmnFlowName(),"DeleteVfModuleBB");
		assertEquals(ebbs.get(5).getBuildingBlock().getBpmnFlowName(),"DeleteVfModuleBB");
		assertEquals(ebbs.get(6).getBuildingBlock().getBpmnFlowName(),"DeleteVfModuleBB");
		assertEquals(ebbs.get(7).getBuildingBlock().getBpmnFlowName(),"DeleteVfModuleBB");
		assertEquals(ebbs.get(8).getBuildingBlock().getBpmnFlowName(),"DeactivateVolumeGroupfBB");
		assertEquals(ebbs.get(9).getBuildingBlock().getBpmnFlowName(),"DeleteVolumeGroupfBB");
		assertEquals(ebbs.get(10).getBuildingBlock().getBpmnFlowName(),"DeactivateVnfBB");
		assertEquals(ebbs.get(11).getBuildingBlock().getBpmnFlowName(),"DeactivateServiceInstanceBB");
		assertEquals(ebbs.get(12).getBuildingBlock().getBpmnFlowName(),"UnassignVfModuleBB");	
		assertEquals(ebbs.get(13).getBuildingBlock().getBpmnFlowName(),"UnassignVfModuleBB");
		assertEquals(ebbs.get(14).getBuildingBlock().getBpmnFlowName(),"UnassignVfModuleBB");
		assertEquals(ebbs.get(15).getBuildingBlock().getBpmnFlowName(),"UnassignVfModuleBB");
		assertEquals(ebbs.get(16).getBuildingBlock().getBpmnFlowName(),"UnassignVolumeGroupBB");
		assertEquals(ebbs.get(17).getBuildingBlock().getBpmnFlowName(),"UnassignVnfBB"); 
		assertEquals(ebbs.get(18).getBuildingBlock().getBpmnFlowName(),"UnassignServiceInstanceBB");
	}	

	@Test
	public void rollbackExecutionPathTest(){
		List<ExecuteBuildingBlock> flowsToExecute = new ArrayList();
		ExecuteBuildingBlock ebb1 = new ExecuteBuildingBlock();
		BuildingBlock bb1 = new BuildingBlock();
		bb1.setBpmnFlowName("CreateNetworkBB");
		flowsToExecute.add(ebb1);
		ebb1.setBuildingBlock(bb1);
		ExecuteBuildingBlock ebb2 = new ExecuteBuildingBlock();
		BuildingBlock bb2 = new BuildingBlock();
		bb2.setBpmnFlowName("ActivateNetworkBB");
		flowsToExecute.add(ebb2);
		ebb2.setBuildingBlock(bb2);
		ExecuteBuildingBlock ebb3 = new ExecuteBuildingBlock();
		BuildingBlock bb3 = new BuildingBlock();
		bb3.setBpmnFlowName("CreateVolumeGroupBB");
		flowsToExecute.add(ebb3);
		ebb3.setBuildingBlock(bb3);
		ExecuteBuildingBlock ebb4 = new ExecuteBuildingBlock();
		BuildingBlock bb4 = new BuildingBlock();
		bb4.setBpmnFlowName("ActivateVolumeGroupBB");
		flowsToExecute.add(ebb4);
		ebb4.setBuildingBlock(bb4);
		ExecuteBuildingBlock ebb5 = new ExecuteBuildingBlock();
		BuildingBlock bb5 = new BuildingBlock();
		bb5.setBpmnFlowName("CreateVfModuleBB");
		flowsToExecute.add(ebb5);
		ebb5.setBuildingBlock(bb5);
		ExecuteBuildingBlock ebb6 = new ExecuteBuildingBlock();
		BuildingBlock bb6 = new BuildingBlock();
		bb6.setBpmnFlowName("ActivateVfModuleBB");
		flowsToExecute.add(ebb6);
		ebb6.setBuildingBlock(bb6);
		ExecuteBuildingBlock ebb7 = new ExecuteBuildingBlock();
		BuildingBlock bb7 = new BuildingBlock();
		bb7.setBpmnFlowName("ActivateVnfBB");
		ebb7.setBuildingBlock(bb7);
		flowsToExecute.add(ebb7);
		ExecuteBuildingBlock ebb8 = new ExecuteBuildingBlock();
		BuildingBlock bb8 = new BuildingBlock();
		bb8.setBpmnFlowName("ActivateServiceInstance");
		ebb8.setBuildingBlock(bb8);
		flowsToExecute.add(ebb8);
		
		execution.setVariable("flowsToExecute", flowsToExecute);
		execution.setVariable("gCurrentSequence", 6);
		
		workflowAction.rollbackExecutionPath(execution);
		List<ExecuteBuildingBlock> ebbs = (List<ExecuteBuildingBlock>) execution.getVariable("flowsToExecute");
		assertEquals(ebbs.get(0).getBuildingBlock().getBpmnFlowName(),"DeleteVfModuleBB");
		assertEquals(ebbs.get(1).getBuildingBlock().getBpmnFlowName(),"DeactivateVolumeGroupBB");
		assertEquals(ebbs.get(2).getBuildingBlock().getBpmnFlowName(),"DeleteVolumeGroupBB");
		assertEquals(ebbs.get(3).getBuildingBlock().getBpmnFlowName(),"DeactivateNetworkBB");
		assertEquals(ebbs.get(4).getBuildingBlock().getBpmnFlowName(),"DeleteNetworkBB");		
	}
	
	@Test
	public void extractResourceTest(){
		String uri = "/v6/serviceInstances/123";
		String uri2 = "/v6/serviceInstances/123/vnfs/1234";
		String uri3 = "/v6/serviceInstances";
		String uri4 = "/v6/serviceInstances/assign";
		String uri5 = "'/v6/serviceInstances/123/vnfs";
		String uri6 = "/v6/serviceInstances/123/vnfs/1234/someAction";
		String uri7 = "/v6/serviceInstances/123/vnfs/1234/vfModules/5678/replace";
		
		Resource expected1 = new Resource("Service", "123", true);
		Resource expected2 = new Resource("Vnf", "1234", false);
		Resource expected3 = new Resource("Vnf", "1234", false);
		Resource expected4 = new Resource("VfModule", "5678", false);
		Resource result = workflowAction.extractResource(uri);
		assertEquals(expected1.getResourceId(),result.getResourceId());
		assertEquals(expected1.getResourceType(),result.getResourceType());
		result = workflowAction.extractResource(uri2);
		assertEquals(expected2.getResourceId(),result.getResourceId());
		assertEquals(expected2.getResourceType(),result.getResourceType());
		result = workflowAction.extractResource(uri3);
		assertEquals("Service", result.getResourceType());
		assertEquals(UUID.randomUUID().toString().length(),result.getResourceId().length());
		result = workflowAction.extractResource(uri4);
		assertEquals("Service", result.getResourceType());
		assertEquals(UUID.randomUUID().toString().length(),result.getResourceId().length());
		result = workflowAction.extractResource(uri5);
		assertEquals("Vnf", result.getResourceType());
		assertEquals(UUID.randomUUID().toString().length(),result.getResourceId().length());
		result = workflowAction.extractResource(uri6);
		assertEquals(expected3.getResourceId(),result.getResourceId());
		assertEquals(expected3.getResourceType(),result.getResourceType());
		result = workflowAction.extractResource(uri7);
		assertEquals(expected4.getResourceId(),result.getResourceId());
		assertEquals(expected4.getResourceType(),result.getResourceType());
	}
	
	@Test
	public void extractResourceInvalidUri() {
		this.expectedException.expect(IllegalArgumentException.class);
		this.expectedException.expectMessage(containsString("Uri could not be parsed:"));
		workflowAction.extractResource("something that doesn't match anything");
	}
	
	@Test
	public void extractResourceInvalidType() {
		this.expectedException.expect(IllegalArgumentException.class);
		this.expectedException.expectMessage(containsString("Uri could not be parsed. No type found."));
		workflowAction.extractResource("/v6/serviceInstances/123/vnfs/1234/vfmodules/5678/replace");
	}
	
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
	public void checkRetryStatusTest(){
		execution.setVariable("handlingCode","Retry");
		execution.setVariable("retryCount", 1);
		execution.setVariable("gCurrentSequence",1);
		workflowAction.checkRetryStatus(execution);
		assertEquals(0,execution.getVariable("gCurrentSequence"));
	}
	
	@Test
	public void queryReferenceDataNestedMacroFlowsTest() throws MalformedURLException {
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
		List<OrchestrationFlow> flows = workflowAction.queryReferenceData(execution, "replaceInstance", "Vnf", false);
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
	public void readResourceIdsFromExecutionTest(){
		execution.setVariable("serviceInstanceId", "123");
		execution.setVariable("vnfId", "888");
		WorkflowResourceIds x = workflowAction.readResourceIdsFromExecution(execution);
		assertEquals("123",x.getServiceInstanceId());
		assertEquals("888",x.getVnfId());
		assertNull(x.getVolumeGroupId());
	}
	
	@Test
	public void determineResourceIdTest() throws Exception{
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
		String id = workflowAction.determineResourceId("generatedId123", "Service", "siName123", reqDetails, workflowResourceIds);
		assertEquals("siId123",id);
		String id2 = workflowAction.determineResourceId("generatedId123", "Service", "111111", reqDetails, workflowResourceIds);
		assertEquals("generatedId123",id2);
		
		//Network
		L3Network network = new L3Network();
		network.setNetworkId("id123");
		network.setNetworkName("name123");
		workflowResourceIds.setServiceInstanceId("siId123");
		Optional<L3Network> opNetwork = Optional.of(network);
		when(bbSetupUtils.getRelatedNetworkByNameFromServiceInstance("siId123","name123")).thenReturn(opNetwork);
		when(bbSetupUtils.getRelatedNetworkByNameFromServiceInstance("siId123","111111")).thenReturn(Optional.empty());
		id = workflowAction.determineResourceId("generatedId123", "Network", "name123", reqDetails, workflowResourceIds);
		assertEquals("id123",id);
		id2 = workflowAction.determineResourceId("generatedId123", "Network", "111111", reqDetails, workflowResourceIds);
		assertEquals("generatedId123",id2);
		
		//Vnf
		GenericVnf vnf = new GenericVnf();
		vnf.setVnfId("id123");
		vnf.setVnfName("vnfName123");
		Optional<GenericVnf> opVnf = Optional.of(vnf);
		when(bbSetupUtils.getRelatedVnfByNameFromServiceInstance("siId123","name123")).thenReturn(opVnf);
		when(bbSetupUtils.getRelatedVnfByNameFromServiceInstance("siId123","111111")).thenReturn(Optional.empty());
		id = workflowAction.determineResourceId("generatedId123", "Vnf", "name123", reqDetails, workflowResourceIds);
		assertEquals("id123",id);
		id2 = workflowAction.determineResourceId("generatedId123", "Vnf", "111111", reqDetails, workflowResourceIds);
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
		id = workflowAction.determineResourceId("generatedId123", "VfModule", "name123", reqDetails, workflowResourceIds);
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
		id2 = workflowAction.determineResourceId("generatedId123", "VfModule", "111111", reqDetails, workflowResourceIds);
		assertEquals("generatedId123",id2);
		
		//VolumeGroup
		VolumeGroup volumeGroup = new VolumeGroup();
		volumeGroup.setVolumeGroupId("id123");
		volumeGroup.setVolumeGroupName("name123");
		workflowResourceIds.setVnfId("id123");
		Optional<VolumeGroup> opVolumeGroup = Optional.of(volumeGroup);
		when(bbSetupUtils.getRelatedVolumeGroupByNameFromVnf("id123","name123")).thenReturn(opVolumeGroup);
		id = workflowAction.determineResourceId("generatedId123", "VolumeGroup", "name123", reqDetails, workflowResourceIds);
		assertEquals("id123",id);
		
		workflowResourceIds.setVnfId("id444");
		when(bbSetupUtils.getAAIGenericVnf("id444")).thenReturn(vnf);
		when(bbSetupUtils.getRelatedVolumeGroupByNameFromVfModule("id123","111111")).thenReturn(opVolumeGroup);
		when(bbSetupUtils.getRelatedVolumeGroupByNameFromVnf("id444","111111")).thenReturn(Optional.empty());
		id2 = workflowAction.determineResourceId("generatedId123", "VolumeGroup", "111111", reqDetails, workflowResourceIds);
		assertEquals("id123",id2);
	}
}
