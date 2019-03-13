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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetup;
import org.onap.so.bpmn.servicedecomposition.tasks.BBInputSetupUtils;
import org.onap.so.client.exception.ExceptionBuilder;
import org.onap.so.client.orchestration.AAIConfigurationResources;
import org.onap.so.db.catalog.beans.ConfigurationResource;
import org.onap.so.db.catalog.beans.CvnfcCustomization;
import org.onap.so.db.catalog.beans.VnfVfmoduleCvnfcConfigurationCustomization;
import org.onap.so.db.catalog.beans.macro.OrchestrationFlow;
import org.onap.so.db.catalog.client.CatalogDbClient;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.RelatedInstance;
import org.onap.so.serviceinstancebeans.RequestDetails;
import org.onap.so.serviceinstancebeans.ServiceInstancesRequest;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowActionUnitTest {

	private final static String JSON_FILE_LOCATION = "src/test/resources/__files/Macro/";
	
	@Mock
	private CatalogDbClient catalogDbClient;
	@Mock
	private BBInputSetup bbInputSetup;
	@Mock
	private BBInputSetupUtils bbInputSetupUtils;
	@Mock
	private ExceptionBuilder exceptionBuilder;
	@Mock
	private AAIConfigurationResources aaiConfigurationResources;
	
	@InjectMocks
	@Spy
	private WorkflowAction workflowAction;
	
	@Test
	public void filterOrchFlowsHasFabricTest() {
		
		List<OrchestrationFlow> flows = createFlowList(
				"DeactivateFabricConfigurationBB",
				"flow x",
				"flow y",
				"ActivateFabricConfigurationBB",
				"flow z");
		doReturn(Arrays.asList("yes", "yes")).when(workflowAction).traverseCatalogDbForConfiguration(ArgumentMatchers.any(String.class), ArgumentMatchers.isNull());
		
		ServiceInstancesRequest sIRequest = new ServiceInstancesRequest();
		RequestDetails requestDetails = new RequestDetails();
		ModelInfo modelInfo = new ModelInfo();
		requestDetails.setModelInfo(modelInfo);
		RelatedInstance relatedInstance = new RelatedInstance();
		sIRequest.setRequestDetails(requestDetails);
		
		List<OrchestrationFlow> result = workflowAction.filterOrchFlows(sIRequest, flows, WorkflowType.VFMODULE, mock(DelegateExecution.class));
		
		assertThat(result, is(flows));
	}
	
	@Test
	public void filterOrchFlowNoFabricTest() {
		List<OrchestrationFlow> flows = createFlowList(
				"DeactivateFabricConfigurationBB",
				"flow x",
				"flow y",
				"ActivateFabricConfigurationBB",
				"flow z");
		
		ServiceInstancesRequest sIRequest = new ServiceInstancesRequest();
		RequestDetails requestDetails = new RequestDetails();
		ModelInfo modelInfo = new ModelInfo();
		modelInfo.setModelCustomizationUuid("");
		requestDetails.setModelInfo(modelInfo);
		sIRequest.setRequestDetails(requestDetails);
		
		List<OrchestrationFlow> result = workflowAction.filterOrchFlows(sIRequest, flows, WorkflowType.VFMODULE, mock(DelegateExecution.class));
		List<OrchestrationFlow> expected = createFlowList(
				"flow x",
				"flow y",
				"flow z");
		
		assertThat(result, is(expected));
	}
	
	@Test
	public void traverseCatalogDbForConfigurationTest() {
		
		CvnfcCustomization cvnfcCustomization = new CvnfcCustomization();
		VnfVfmoduleCvnfcConfigurationCustomization vfModuleCustomization = new VnfVfmoduleCvnfcConfigurationCustomization();
		ConfigurationResource configuration = new ConfigurationResource();
		configuration.setToscaNodeType("FabricConfiguration");
		configuration.setModelUUID("my-uuid");
		vfModuleCustomization.setConfigurationResource(configuration);
		cvnfcCustomization.setVnfVfmoduleCvnfcConfigurationCustomization(Collections.singleton(vfModuleCustomization));
		List<CvnfcCustomization> cvnfcCustomizations = Arrays.asList(cvnfcCustomization);
		when(catalogDbClient.getCvnfcCustomizationByVnfCustomizationUUIDAndVfModuleCustomizationUUID(any(String.class), any(String.class)))
			.thenReturn(cvnfcCustomizations);
		
		List<VnfVfmoduleCvnfcConfigurationCustomization> results = workflowAction.traverseCatalogDbForConfiguration("myVnfCustomizationId", "myVfModuleCustomizationId");
		
		assertThat(results, is(Arrays.asList(vfModuleCustomization)));
		
	}
	
	@Test
	public void verifyFilterOrchInvocation() throws Exception {
		DelegateExecution execution = mock(DelegateExecution.class);
		
		when(execution.getVariable(eq("aLaCarte"))).thenReturn(true);
		when(execution.getVariable(eq("bpmnRequest"))).thenReturn(getJson("ServiceMacroAssign.json"));
		when(execution.getVariable(eq("requestUri"))).thenReturn("/v6/serviceInstances/123/vnfs/1234");
		
		OrchestrationFlow flow = new OrchestrationFlow();
		flow.setFlowName("flow x");
		
		List<OrchestrationFlow> flows = Arrays.asList(flow);
		doReturn(Arrays.asList(flow)).when(workflowAction).queryNorthBoundRequestCatalogDb(any(), any(), any(), anyBoolean(), any(), any());
		workflowAction.selectExecutionList(execution);
		
		verify(workflowAction, times(1)).filterOrchFlows(any(), eq(flows), any(), any());
		
		flow = new OrchestrationFlow();
		flow.setFlowName("flow y");
		flows = Arrays.asList(flow);
		when(execution.getVariable(eq("aLaCarte"))).thenReturn(false);
		workflowAction.selectExecutionList(execution);
		
		verify(workflowAction, never()).filterOrchFlows(any(), eq(flows), any(), any());

	}
	
	private String getJson(String filename) throws IOException {
		 return new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + filename)));
	}
	
	private List<OrchestrationFlow> createFlowList(String... myList) {
		
		List<OrchestrationFlow> result = new ArrayList<>();
		for (String name : myList) {
			OrchestrationFlow flow = new OrchestrationFlow();
			flow.setFlowName(name);
			result.add(flow);
		}
		
		return result;
		
	}
}
