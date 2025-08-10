/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.scripts

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.onap.so.bpmn.core.WorkflowException
import org.onap.so.bpmn.core.domain.AllottedResource
import org.onap.so.bpmn.core.domain.HomingSolution
import org.onap.so.bpmn.core.domain.InventoryType
import org.onap.so.bpmn.core.domain.License
import org.onap.so.bpmn.core.domain.ModelInfo
import org.onap.so.bpmn.core.domain.ModuleResource
import org.onap.so.bpmn.core.domain.NetworkResource
import org.onap.so.bpmn.core.domain.ResourceInstance
import org.onap.so.bpmn.core.domain.ResourceType
import org.onap.so.bpmn.core.domain.ServiceDecomposition
import org.onap.so.bpmn.core.domain.ServiceInstance
import org.onap.so.bpmn.core.domain.VnfResource

import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
class DoCreateVnfAndModulesTest {

	@Captor
	static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

	@Before
	void init() throws IOException {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testPreProcessRequest() {
		ExecutionEntity mockExecution = setupMock()
		setupBasicProcessInputs(mockExecution)

		ServiceDecomposition serviceDecomposition = createServiceDecomposition()
		when(mockExecution.getVariable("serviceDecomposition")).thenReturn(serviceDecomposition)

		Map<String,String> vfModuleNames = new HashMap<String,String>()
		vfModuleNames.put("3ec98c7a-ac20-49a1-9e0d-09fea7e8db45", "VGWA:e2:25:25:25:%")
		vfModuleNames.put("cc250e7e-746b-4d84-8064-df20c74213a6", "VGWB:f9:32:32:32:%")
		when(mockExecution.getVariable("vfModuleNames")).thenReturn(vfModuleNames)

		DoCreateVnfAndModules obj = new DoCreateVnfAndModules()
		obj.preProcessRequest(mockExecution)

		Mockito.verify(mockExecution, times(11)).setVariable(captor.capture(), captor.capture())

		List list = captor.getAllValues()
		for (int i = 0; i < list.size(); i+=2) {
			System.out.println("captor[" + i/2 + "]: " + list.get(i)
				+ (i+1 < list.size() ? ("=" + list.get(i+1)) : ""))
		}

		String someKey = list.get(18)
		Assert.assertEquals("numOfCreatedAddOnModules", someKey)
		Integer someValue = list.get(19)
		Assert.assertEquals(0, someValue)

		String lastKey = list.get(20)
		Assert.assertEquals("rollbackData", lastKey)
	}

	@Test
	void testQueryCatalogDB() {
		ExecutionEntity mockExecution = setupMock()
		setupBasicProcessInputs(mockExecution)

		ServiceDecomposition serviceDecomposition = createServiceDecomposition()
		when(mockExecution.getVariable("serviceDecomposition")).thenReturn(serviceDecomposition)

		DoCreateVnfAndModules obj = new DoCreateVnfAndModules()
		obj.queryCatalogDB(mockExecution)

		Mockito.verify(mockExecution, times(11)).setVariable(captor.capture(), captor.capture())

		List list = captor.getAllValues()
		for (int i = 0; i < list.size(); i+=2) {
			System.out.println("captor[" + i/2 + "]: " + list.get(i)
				+ (i+1 < list.size() ? ("=" + list.get(i+1)) : ""))
		}

		String vfModuleNameKey = list.get(12)
		Assert.assertEquals("baseVfModuleName", vfModuleNameKey)
		String vfModuleNameValue = list.get(13)
		Assert.assertEquals(null, vfModuleNameValue)

		String lastKey = list.get(20)
		Assert.assertEquals("baseVfModuleId", lastKey)
	}

	@Test
	void testQueryCatalogDBWithVfModuleNames() {
		ExecutionEntity mockExecution = setupMock()
		setupBasicProcessInputs(mockExecution)

		ServiceDecomposition serviceDecomposition = createServiceDecomposition()
		when(mockExecution.getVariable("serviceDecomposition")).thenReturn(serviceDecomposition)

		Map<String,String> vfModuleNames = new HashMap<String,String>()
		vfModuleNames.put("3ec98c7a-ac20-49a1-9e0d-09fea7e8db45", "VGWA:e2:25:25:25:%")
		vfModuleNames.put("cc250e7e-746b-4d84-8064-df20c74213a6", "VGWB:f9:32:32:32:%")
		when(mockExecution.getVariable("vfModuleNames")).thenReturn(vfModuleNames)

		DoCreateVnfAndModules obj = new DoCreateVnfAndModules()
		obj.queryCatalogDB(mockExecution)

		Mockito.verify(mockExecution, times(11)).setVariable(captor.capture(), captor.capture())

		List list = captor.getAllValues()
		for (int i = 0; i < list.size(); i+=2) {
			System.out.println("captor[" + i/2 + "]: " + list.get(i)
				+ (i+1 < list.size() ? ("=" + list.get(i+1)) : ""))
		}

		String vfModuleNameKey = list.get(12)
		Assert.assertEquals("baseVfModuleName", vfModuleNameKey)
		String vfModuleNameValue = list.get(13)
		Assert.assertEquals("VGWA:e2:25:25:25:%", vfModuleNameValue)

		String lastKey = list.get(20)
		Assert.assertEquals("baseVfModuleId", lastKey)
	}

	@Test
	void testPreProcessAddonModule() {
		ExecutionEntity mockExecution = setupMock()
		setupBasicProcessInputs(mockExecution)

		ServiceDecomposition serviceDecomposition = createServiceDecomposition()
		when(mockExecution.getVariable("serviceDecomposition")).thenReturn(serviceDecomposition)

		VnfResource vnf = serviceDecomposition.getVnfResources().get(0);
		List<ModuleResource> vfModules = vnf.getAllVfModuleObjects()

		for (int i = vfModules.size()-1; i >= 0; i--) {
			if (vfModules.get(i).getIsBase()) {
				vfModules.remove(i);
			}
		}

		when(mockExecution.getVariable("addOnModules")).thenReturn(vfModules)
		when(mockExecution.getVariable("addOnModulesDeployed")).thenReturn(0)

		DoCreateVnfAndModules obj = new DoCreateVnfAndModules()
		obj.preProcessAddOnModule(mockExecution)

		Mockito.verify(mockExecution, times(9)).setVariable(captor.capture(), captor.capture())

		List list = captor.getAllValues()
		for (int i = 0; i < list.size(); i+=2) {
			System.out.println("captor[" + i/2 + "]: " + list.get(i)
				+ (i+1 < list.size() ? ("=" + list.get(i+1)) : ""))
		}

		String vfModuleNameKey = list.get(14)
		Assert.assertEquals("addOnVfModuleName", vfModuleNameKey)
		String vfModuleNameValue = list.get(15)
		Assert.assertEquals(null, vfModuleNameValue)

		String lastKey = list.get(16)
		Assert.assertEquals("initialCount", lastKey)
	}

	@Test
	void testPreProcessAddonModuleWithVfModuleNames() {
		ExecutionEntity mockExecution = setupMock()
		setupBasicProcessInputs(mockExecution)

		ServiceDecomposition serviceDecomposition = createServiceDecomposition()
		when(mockExecution.getVariable("serviceDecomposition")).thenReturn(serviceDecomposition)

		Map<String,String> vfModuleNames = new HashMap<String,String>()
		vfModuleNames.put("3ec98c7a-ac20-49a1-9e0d-09fea7e8db45", "VGWA:e2:25:25:25:%")
		vfModuleNames.put("cc250e7e-746b-4d84-8064-df20c74213a6", "VGWB:f9:32:32:32:%")
		when(mockExecution.getVariable("vfModuleNames")).thenReturn(vfModuleNames)

		VnfResource vnf = serviceDecomposition.getVnfResources().get(0);
		List<ModuleResource> vfModules = vnf.getAllVfModuleObjects()

		for (int i = vfModules.size()-1; i >= 0; i--) {
			if (vfModules.get(i).getIsBase()) {
				vfModules.remove(i);
			}
		}

		when(mockExecution.getVariable("addOnModules")).thenReturn(vfModules)
		when(mockExecution.getVariable("addOnModulesDeployed")).thenReturn(0)

		DoCreateVnfAndModules obj = new DoCreateVnfAndModules()
		obj.preProcessAddOnModule(mockExecution)

		Mockito.verify(mockExecution, times(9)).setVariable(captor.capture(), captor.capture())

		List list = captor.getAllValues()
		for (int i = 0; i < list.size(); i+=2) {
			System.out.println("captor[" + i/2 + "]: " + list.get(i)
				+ (i+1 < list.size() ? ("=" + list.get(i+1)) : ""))
		}

		String vfModuleNameKey = list.get(14)
		Assert.assertEquals("addOnVfModuleName", vfModuleNameKey)
		String vfModuleNameValue = list.get(15)
		Assert.assertEquals("VGWB:f9:32:32:32:%", vfModuleNameValue)

		String lastKey = list.get(16)
		Assert.assertEquals("initialCount", lastKey)
	}

	private static setupBasicProcessInputs(ExecutionEntity mockExecution) {
		when(mockExecution.getVariable("prefix")).thenReturn("DCVAM_")
		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
		when(mockExecution.getVariable("msoRequestId")).thenReturn("28a7f01e-a6aa-44fd-b25e-e06e14873cd7")
		when(mockExecution.getVariable("serviceInstanceId")).thenReturn("7d34a7df-d6c3-4f1c-8710-576412134a5a")
		when(mockExecution.getVariable("productFamilyId")).thenReturn("a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb")
		when(mockExecution.getVariable("lcpCloudRegionId")).thenReturn("RegionOne")
		when(mockExecution.getVariable("tenantId")).thenReturn("b8ad3842ab3642f7bf3fbe4e4d3b9f86")
		when(mockExecution.getVariable("disableRollback")).thenReturn("true")
		when(mockExecution.getVariable("delayMS")).thenReturn("0")

	}

	private static ExecutionEntity setupMock() {
		ProcessDefinition mockProcessDefinition = mock(ProcessDefinition.class)
		when(mockProcessDefinition.getKey()).thenReturn("DoCreateVnfAndModules")
		RepositoryService mockRepositoryService = mock(RepositoryService.class)
		when(mockRepositoryService.getProcessDefinition()).thenReturn(mockProcessDefinition)
		when(mockRepositoryService.getProcessDefinition().getKey()).thenReturn("DoCreateVnfAndModules")
		when(mockRepositoryService.getProcessDefinition().getId()).thenReturn("100")
		ProcessEngineServices mockProcessEngineServices = mock(ProcessEngineServices.class)
		when(mockProcessEngineServices.getRepositoryService()).thenReturn(mockRepositoryService)

		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		// Initialize prerequisite variables
		when(mockExecution.getId()).thenReturn("100")
		when(mockExecution.getProcessDefinitionId()).thenReturn("DoCreateVnfAndModules")
		when(mockExecution.getProcessInstanceId()).thenReturn("DoCreateVnfAndModules")
		when(mockExecution.getProcessEngineServices()).thenReturn(mockProcessEngineServices)
		when(mockExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(mockExecution.getProcessDefinitionId())).thenReturn(mockProcessDefinition)

		return mockExecution
	}

	public static ServiceDecomposition createServiceDecomposition() {
		ServiceDecomposition serviceDecomposition = new ServiceDecomposition()

		ServiceInstance serviceInstance = new ServiceInstance()
		serviceInstance.setInstanceId("7d34a7df-d6c3-4f1c-8710-576412134a5a")
		serviceDecomposition.setServiceInstance(serviceInstance)
		serviceDecomposition.setServiceType("")
		serviceDecomposition.setServiceRole("")

		ModelInfo serviceModelInfo = new ModelInfo()
		serviceDecomposition.setModelInfo(serviceModelInfo)
		serviceModelInfo.setModelName("vcpesvc_rescust_1111")
		serviceModelInfo.setModelUuid("1dffd5f9-bb29-4a47-8073-9b9b07f4943a")
		serviceModelInfo.setModelVersion("1.0")
		serviceModelInfo.setModelCustomizationUuid("")
		serviceModelInfo.setModelCustomizationName("")
		serviceModelInfo.setModelInstanceName("")
		serviceModelInfo.setModelType("")

		List<VnfResource> vnfResources = new ArrayList<VnfResource>()
		serviceDecomposition.setVnfResources(vnfResources)

		VnfResource vnfResource = new VnfResource()
		vnfResources.add(vnfResource)
		vnfResource.setResourceId("9504b6b3-d346-4387-952c-8f9b7570b055")
		vnfResource.setResourceType(ResourceType.VNF)
		ModelInfo vnfModelInfo = new ModelInfo()
		vnfResource.setModelInfo(vnfModelInfo)
		vnfModelInfo.setModelName("vcpevsp_vgw_1111")
		vnfModelInfo.setModelUuid("289e96fd-a679-4286-a8a2-d76f930d650b")
		vnfModelInfo.setModelInvariantUuid("0327af89-f836-4086-aadb-17d5c9bd8a83");
		vnfModelInfo.setModelVersion("1.0");
		vnfModelInfo.setModelCustomizationUuid("cf151beb-9510-44a1-a165-c783e673baa1");
		vnfModelInfo.setModelCustomizationName("");
		vnfModelInfo.setModelInstanceName("vcpevsp_vgw_1111 0")
		vnfModelInfo.setModelType("")
		vnfResource.setResourceInstance(new ResourceInstance())
		vnfResource.setHomingSolution(new HomingSolution())
		vnfResource.setToscaNodeType("org.openecomp.resource.vf.VcpevspVgw1111")
		vnfResource.setMultiStageDesign("false")
		vnfResource.setMultiStageDesign("false")

		List<ModuleResource> moduleResources = new ArrayList<ModuleResource>()
		vnfResource.setModules(moduleResources)

		ModuleResource moduleResource = new ModuleResource()
		moduleResources.add(moduleResource)
		moduleResource.setResourceType(ResourceType.MODULE)
		ModelInfo moduleModelInfo = new ModelInfo()
		moduleResource.setModelInfo(moduleModelInfo);
		moduleModelInfo.setModelName("VcpevspVgw1111..base_vcpe_vgw..module-0")
		moduleModelInfo.setModelUuid("cf35b6b8-1f31-4efc-87a7-d53f840b8fdf")
		moduleModelInfo.setModelInvariantUuid("3ec98c7a-ac20-49a1-9e0d-09fea7e8db45")
		moduleModelInfo.setModelVersion("1")
		moduleModelInfo.setModelCustomizationUuid("281085b3-4598-4c94-811d-58cc685763e7")
		moduleModelInfo.setModelCustomizationName("")
		moduleModelInfo.setModelInstanceName("")
		moduleModelInfo.setModelType("")
		moduleResource.setResourceInstance(new ResourceInstance())
		moduleResource.setHomingSolution(new HomingSolution())
		moduleResource.setHasVolumeGroup(false)
		moduleResource.setIsBase(true)
		moduleResource.setVfModuleLabel("base_vcpe_vgw")
		moduleResource.setInitialCount(1)

		// For testing an add-on module; not in the actual vCPE model
		moduleResource = new ModuleResource()
		moduleResources.add(moduleResource)
		moduleResource.setResourceType(ResourceType.MODULE)
		moduleModelInfo = new ModelInfo()
		moduleResource.setModelInfo(moduleModelInfo);
		moduleModelInfo.setModelName("VcpevspVgw1111..addon_vcpe_vgw..module-1")
		moduleModelInfo.setModelUuid("8c8b41b2-8466-41b4-ae8d-5924830c40e8")
		moduleModelInfo.setModelInvariantUuid("cc250e7e-746b-4d84-8064-df20c74213a6")
		moduleModelInfo.setModelVersion("1")
		moduleModelInfo.setModelCustomizationUuid("99424afc-1fb4-4598-a99b-3e0690b4cb03")
		moduleModelInfo.setModelCustomizationName("")
		moduleModelInfo.setModelInstanceName("")
		moduleModelInfo.setModelType("")
		moduleResource.setResourceInstance(new ResourceInstance())
		moduleResource.setHomingSolution(new HomingSolution())
		moduleResource.setHasVolumeGroup(false)
		moduleResource.setIsBase(false)
		moduleResource.setVfModuleLabel("addon_vcpe_vgw")
		moduleResource.setInitialCount(1)

		serviceDecomposition.setNetworkResources(new ArrayList<NetworkResource>())

		List<AllottedResource> allottedResources = new ArrayList<AllottedResource>()
		serviceDecomposition.setAllottedResources(allottedResources)

		AllottedResource ar = new AllottedResource()
		allottedResources.add(ar)
		ar.setResourceId("ed4a3a9a-1411-4924-a9ee-61a41871a040")
		ar.setResourceType(ResourceType.ALLOTTED_RESOURCE)
		ModelInfo arModelInfo = new ModelInfo()
		ar.setModelInfo(arModelInfo)
		arModelInfo.setModelName("vcpear_tunnelxconn_1111")
		arModelInfo.setModelUuid("d99e5442-c5e4-4197-ad8d-54f7ad43dd83")
		arModelInfo.setModelInvariantUuid("61c17371-e824-4587-a9bb-21782aa28391")
		arModelInfo.setModelVersion("1.0")
		arModelInfo.setModelCustomizationUuid("506bb474-b8c2-41fd-aeec-4371c3ef58a4")
		arModelInfo.setModelCustomizationName("")
		arModelInfo.setModelInstanceName("vcpear_tunnelxconn_1111 0")
		arModelInfo.setModelType("")
		ar.setResourceInstance(new ResourceInstance())
		HomingSolution homingSolution = new HomingSolution()
		ar.setHomingSolution(homingSolution)
		homingSolution.setInventoryType(InventoryType.service)
		homingSolution.setServiceInstanceId("d600c1c0-ff45-40e4-bf29-45a95fa64556")
		homingSolution.setCloudOwner("CloudOwner")
		homingSolution.setCloudRegionId("RegionOne")
		VnfResource vnf = new VnfResource()
		homingSolution.setVnf(vnf)
		vnf.setResourceId("cea5e96e-9c67-437c-bf94-2329d277be09")
		vnf.setResourceType(ResourceType.VNF)
		vnf.setResourceInstance(new ResourceInstance())
		vnf.setHomingSolution(new HomingSolution())
		vnf.setVnfHostname("vnfHostName")
		homingSolution.setLicense(new License())
		homingSolution.setRehome(false)
		ar.setToscaNodeType("org.openecomp.resource.vf.VcpearTunnelxconn1111")
		ar.setAllottedResourceType("TunnelXConnect")
		ar.setAllottedResourceRole("TunnelXConn")
		ar.setProvidingServiceModelName("org.openecomp.service.VcpesvcVgmux1111")
		ar.setProvidingServiceModelInvariantUuid("d5751cb3-b9e9-470b-9c29-76a5e3ea12d0")
		ar.setProvidingServiceModelUuid("61b6e96a-f0c6-4f34-a91c-dab3574dd025")
		ar.setNfType("TunnelXConn")
		ar.setNfRole("TunnelXConn")

		ar = new AllottedResource()
		allottedResources.add(ar)
		ar.setResourceId("3b1b3686-ccfe-4e7c-9d6b-76419db398f9")
		ar.setResourceType(ResourceType.ALLOTTED_RESOURCE)
		arModelInfo = new ModelInfo()
		ar.setModelInfo(arModelInfo)
		arModelInfo.setModelName("vcpear_brg_1111")
		arModelInfo.setModelUuid("6b0a5aa5-98d8-455c-8cd1-618a3f1ac859")
		arModelInfo.setModelInvariantUuid("531f9aa5-dea4-4958-89ad-ef03f77cbf07")
		arModelInfo.setModelVersion("1.0")
		arModelInfo.setModelCustomizationUuid("d23ac3fe-ea54-4060-a7c1-ec9178c79620")
		arModelInfo.setModelCustomizationName("")
		arModelInfo.setModelInstanceName("vcpear_brg_1111 0")
		arModelInfo.setModelType("")
		ar.setResourceInstance(new ResourceInstance())
		homingSolution = new HomingSolution()
		ar.setHomingSolution(homingSolution)
		homingSolution.setInventoryType(InventoryType.service)
		homingSolution.setServiceInstanceId("bc28ebca-0cc3-4bf8-9ce9-d1524e4bec79")
		homingSolution.setCloudOwner("CloudOwner")
		homingSolution.setCloudRegionId("RegionOne")
		vnf = new VnfResource()
		homingSolution.setVnf(vnf)
		vnf.setResourceId("65183e95-e6f1-46cb-9315-2da27a24c2b9")
		vnf.setResourceType(ResourceType.VNF)
		vnf.setResourceInstance(new ResourceInstance())
		vnf.setHomingSolution(new HomingSolution())
		vnf.setVnfHostname("vnfHostName")
		homingSolution.setLicense(new License())
		homingSolution.setRehome(false)
		ar.setToscaNodeType("org.openecomp.resource.vf.VcpearBrg1111")
		ar.setAllottedResourceType("BRG")
		ar.setAllottedResourceRole("BRG")
		ar.setProvidingServiceModelName("org.openecomp.service.VcpesvcVbrg1111")
		ar.setProvidingServiceModelInvariantUuid("6eff53bf-0045-41b0-bd48-b4e1284e5b7a")
		ar.setProvidingServiceModelUuid("0e500bca-15ac-42eb-a2f1-4bfd3b2828ff")
		ar.setNfType("BRG")
		ar.setNfRole("BRG")

		return serviceDecomposition
	}
}
