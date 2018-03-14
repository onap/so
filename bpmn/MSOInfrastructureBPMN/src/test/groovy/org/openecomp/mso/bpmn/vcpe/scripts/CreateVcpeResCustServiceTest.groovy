/*
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
package org.openecomp.mso.bpmn.vcpe.scripts


import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.Ignore
import org.mockito.MockitoAnnotations
import org.camunda.bpm.engine.delegate.BpmnError
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.mock.FileUtil

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.get
import static com.github.tomakehurst.wiremock.client.WireMock.patch
import static com.github.tomakehurst.wiremock.client.WireMock.put
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import static org.junit.Assert.*;
import static org.mockito.Mockito.*
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetAllottedResource
import org.openecomp.mso.bpmn.core.domain.ServiceDecomposition
import org.openecomp.mso.bpmn.core.domain.VnfResource
import org.openecomp.mso.bpmn.core.domain.AllottedResource
import org.openecomp.mso.bpmn.core.domain.ModelInfo
import org.openecomp.mso.bpmn.core.domain.HomingSolution
import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.vcpe.scripts.MapGetter
import org.openecomp.mso.bpmn.vcpe.scripts.MapSetter

import com.github.tomakehurst.wiremock.junit.WireMockRule

class CreateVcpeResCustServiceTest extends GroovyTestBase {
	
	private static String request
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(PORT)

	String Prefix = "CVRCS_"
	String RbType = "DCRENI_"

	@BeforeClass
	public static void setUpBeforeClass() {
		request = FileUtil.readResourceFile("__files/VCPE/CreateVcpeResCustService/request.json")
	}
	  
    @Before
	public void init()
	{
		MockitoAnnotations.initMocks(this)
	}
	
	public CreateVcpeResCustServiceTest() {
		super("CreateVcpeResCustService")
	}
	
	
	// ***** preProcessRequest *****
			
	@Test
	@Ignore // 1802 merge
	public void preProcessRequest() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPreProcess(mex)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.preProcessRequest(mex)

		verify(mex).getVariable(DBGFLAG)
		verify(mex).setVariable("prefix", Prefix)
		verify(mex).setVariable("aaiDistDelay", "aaidelay")
		verify(mex).setVariable("createVcpeServiceRequest", request)
		verify(mex).setVariable("msoRequestId", "mri")
		assertEquals("sii", map.get("serviceInstanceId"))
		verify(mex).setVariable("requestAction", "ra")
		verify(mex).setVariable("source", "VID")
		verify(mex).setVariable("globalSubscriberId", CUST)
		verify(mex).setVariable("globalCustomerId", CUST)
		verify(mex).setVariable("subscriptionServiceType", SVC)
		verify(mex).setVariable("disableRollback", "false")
		verify(mex).setVariable("productFamilyId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb")		
		assertTrue(map.containsKey("subscriberInfo"))
		
		verify(mex).setVariable("brgWanMacAddress", "brgmac")
		verify(mex).setVariable("customerLocation", ["customerLatitude":"32.897480", "customerLongitude":"-97.040443", "customerName":"some_company"])
		verify(mex).setVariable("homingSolution", "sniro")
		assertTrue(map.containsKey("serviceInputParams"))
		assertTrue(map.containsKey(Prefix+"requestInfo"))
		
		def reqinfo = map.get(Prefix+"requestInfo")
		assertTrue(reqinfo.indexOf("<request-id>mri</") >= 0)
		assertTrue(reqinfo.indexOf("<source>VID</") >= 0)
	}
			
	@Test
	// @Ignore  
	public void preProcessRequest_MissingAaiDistDelay() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPreProcess(mex)
		
		when(mex.getVariable("URN_mso_workflow_aai_distribution_delay")).thenReturn(null)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> CreateVcpeResCustService.preProcessRequest(mex) }))
	}
			
	@Test
	@Ignore // 1802 merge
	public void preProcessRequest_EmptyParts() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPreProcess(mex)
		
		def req = request
					.replace('"source"', '"sourceXXX"')
					.replace('"BRG_WAN_MAC_Address"', '"BRG_WAN_MAC_AddressXXX"')
					.replace('"Customer_Location"', '"Customer_LocationXXX"')
		
		when(mex.getVariable("bpmnRequest")).thenReturn(req)
		when(mex.getVariable("serviceInstanceId")).thenReturn(null)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.preProcessRequest(mex)

		verify(mex).getVariable(DBGFLAG)
		verify(mex).setVariable("prefix", Prefix)
		verify(mex).setVariable("createVcpeServiceRequest", req)
		verify(mex).setVariable("msoRequestId", "mri")
		assertNotNull(map.get("serviceInstanceId"))
		assertFalse(map.get("serviceInstanceId").isEmpty())
		verify(mex).setVariable("requestAction", "ra")
		verify(mex).setVariable("source", "VID")
		verify(mex).setVariable("globalSubscriberId", CUST)
		verify(mex).setVariable("globalCustomerId", CUST)
		verify(mex).setVariable("subscriptionServiceType", SVC)
		verify(mex).setVariable("disableRollback", "false")
		verify(mex).setVariable("productFamilyId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb")
		assertTrue(map.containsKey("subscriberInfo"))
		
		assertEquals("", map.get("brgWanMacAddress"))
		assertEquals("", map.get("customerLocation"))
		assertEquals("oof", map.get("homingSolution"))
		assertTrue(map.containsKey("serviceInputParams"))
		assertTrue(map.containsKey(Prefix+"requestInfo"))
		
		def reqinfo = map.get(Prefix+"requestInfo")
		println reqinfo
		assertTrue(reqinfo.indexOf("<request-id>mri</") >= 0)
		assertTrue(reqinfo.indexOf("<source>VID</") >= 0)
	}
			
	@Test
	// @Ignore  
	public void preProcessRequest_MissingSubscriberId() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPreProcess(mex)
		
		def req = request
					.replace('"globalSubscriberId"', '"globalSubscriberIdXXX"')
		
		when(mex.getVariable("bpmnRequest")).thenReturn(req)
		when(mex.getVariable("serviceInstanceId")).thenReturn(null)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> CreateVcpeResCustService.preProcessRequest(mex) }))
	}
			
	@Test
	// @Ignore  
	public void preProcessRequest_BpmnError() {
		ExecutionEntity mex = setupMock()
		initPreProcess(mex)
		
		when(mex.getVariable("bpmnRequest")).thenThrow(new BpmnError("expected exception"))
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> CreateVcpeResCustService.preProcessRequest(mex) }))
	}
			
	@Test
	// @Ignore  
	public void preProcessRequest_Ex() {
		ExecutionEntity mex = setupMock()
		initPreProcess(mex)
		
		when(mex.getVariable("bpmnRequest")).thenThrow(new RuntimeException("expected exception"))
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> CreateVcpeResCustService.preProcessRequest(mex) }))
	}
	
	// ***** sendSyncResponse *****
			
	@Test
	// @Ignore  
	public void sendSyncResponse() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initSendSyncResponse(mex)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.sendSyncResponse(mex)

		verify(mex, times(2)).getVariable(DBGFLAG)
		
		verify(mex).setVariable(processName+"WorkflowResponseSent", "true")
		
		assertEquals("202", map.get(processName+"ResponseCode"))
		assertEquals("Success", map.get(processName+"Status"))
		
		def resp = map.get(processName+"Response")
		
		assertTrue(resp.indexOf('"instanceId":"sii"') >= 0)
		assertTrue(resp.indexOf('"requestId":"mri"') >= 0)
	}
			
	@Test
	// @Ignore  
	public void sendSyncResponse_Ex() {
		ExecutionEntity mex = setupMock()
		initSendSyncResponse(mex)
		
		when(mex.getVariable("serviceInstanceId")).thenThrow(new RuntimeException("expected exception"))
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> CreateVcpeResCustService.sendSyncResponse(mex) }))
	}
	
	
	// ***** prepareDecomposeService *****
			
	@Test
	// @Ignore  
	public void prepareDecomposeService() {
		ExecutionEntity mex = setupMock()
		initPrepareDecomposeService(mex)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.prepareDecomposeService(mex)

		verify(mex).getVariable(DBGFLAG)
		verify(mex).setVariable("serviceModelInfo", "mi")
	}
			
	@Test
	// @Ignore  
	public void prepareDecomposeService_Ex() {
		ExecutionEntity mex = setupMock()
		initPrepareDecomposeService(mex)
		
		when(mex.getVariable("createVcpeServiceRequest")).thenThrow(new RuntimeException("expected exception"))
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> CreateVcpeResCustService.prepareDecomposeService(mex) }))
	}
	
	
	// ***** prepareCreateServiceInstance *****
			
	@Test
	// @Ignore  
	public void prepareCreateServiceInstance() {
		ExecutionEntity mex = setupMock()
		initPrepareCreateServiceInstance(mex)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.prepareCreateServiceInstance(mex)

		verify(mex).getVariable(DBGFLAG)
		verify(mex).setVariable("serviceInstanceName", "VCPE1")
		verify(mex).setVariable("serviceDecompositionString", "mydecomp")
	}
			
	@Test
	// @Ignore  
	public void prepareCreateServiceInstance_Ex() {
		ExecutionEntity mex = setupMock()
		initPrepareCreateServiceInstance(mex)
		
		when(mex.getVariable("createVcpeServiceRequest")).thenThrow(new RuntimeException("expected exception"))
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> CreateVcpeResCustService.prepareCreateServiceInstance(mex) }))
	}
	
	
	// ***** postProcessServiceInstanceCreate *****
			
	@Test
	// @Ignore  
	public void postProcessServiceInstanceCreate() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPostProcessServiceInstanceCreate(mex)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.postProcessServiceInstanceCreate(mex)

		verify(mex).getVariable(DBGFLAG)
		
		def reqinfo = map.get(Prefix+"setUpdateDbInstancePayload")
		
		assertTrue(reqinfo.indexOf("<requestId>mri</") >= 0)
		assertTrue(reqinfo.indexOf("<serviceInstanceId>sii</") >= 0)
		assertTrue(reqinfo.indexOf("<serviceInstanceName>sin</") >= 0)
	}
			
	@Test
	// @Ignore  
	public void postProcessServiceInstanceCreate_BpmnError() {
		ExecutionEntity mex = setupMock()
		initPostProcessServiceInstanceCreate(mex)
		
		doThrow(new BpmnError("expected exception")).when(mex).setVariable(endsWith("setUpdateDbInstancePayload"), any())
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> CreateVcpeResCustService.postProcessServiceInstanceCreate(mex) }))
	}
			
	@Test
	// @Ignore  
	public void postProcessServiceInstanceCreate_Ex() {
		ExecutionEntity mex = setupMock()
		initPostProcessServiceInstanceCreate(mex)
		
		doThrow(new RuntimeException("expected exception")).when(mex).setVariable(endsWith("setUpdateDbInstancePayload"), any())
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> CreateVcpeResCustService.postProcessServiceInstanceCreate(mex) }))
	}
	
	
	// ***** processDecomposition *****
			
	@Test
	// @Ignore  
	public void processDecomposition() {
		ExecutionEntity mex = setupMock()
		def svcdecomp = initProcessDecomposition(mex)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.processDecomposition(mex)

		verify(mex).getVariable(DBGFLAG)
		
		verify(mex).setVariable("vnfList", svcdecomp.getServiceVnfs())
		verify(mex).setVariable("vnfListString", '[myvnf]')
		verify(mex).setVariable(Prefix+"VNFsCount", 1)
		
		verify(mex).setVariable("vnfModelInfo", "mymodel")
		verify(mex).setVariable("vnfModelInfoString", "mymodel")
	}
			
	@Test
	// @Ignore  
	public void processDecomposition_EmptyNet_EmptyVnf() {
		ExecutionEntity mex = setupMock()
		def svcdecomp = initProcessDecomposition(mex)
		
		when(svcdecomp.getServiceVnfs()).thenReturn(new LinkedList<VnfResource>())
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.processDecomposition(mex)

		verify(mex).getVariable(DBGFLAG)
		
		verify(mex).setVariable("vnfList", svcdecomp.getServiceVnfs())
		verify(mex).setVariable("vnfListString", '[]')
		verify(mex).setVariable(Prefix+"VNFsCount", 0)
		
		verify(mex).setVariable("vnfModelInfo", "")
		verify(mex).setVariable("vnfModelInfoString", "")
	}
			
	@Test
	// @Ignore  
	public void processDecomposition_Ex() {
		ExecutionEntity mex = setupMock()
		def svcdecomp = initProcessDecomposition(mex)
		
		when(svcdecomp.getServiceVnfs()).thenThrow(new RuntimeException("expected exception"))
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> CreateVcpeResCustService.processDecomposition(mex) }))
	}
	
	
	// ***** filterVnfs *****
			
	@Test
	// @Ignore  
	public void filterVnfs() {
		ExecutionEntity mex = setupMock()
		def svcdecomp = initFilterVnfs(mex)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.processDecomposition(mex)
		
		verify(mex).setVariable("vnfListString", '[myvnf3, myvnf5]')
	}
			
	@Test
	// @Ignore  
	public void filterVnfs_Null() {
		ExecutionEntity mex = setupMock()
		def svcdecomp = initFilterVnfs(mex)
		
		when(svcdecomp.getServiceVnfs()).thenReturn(null)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.processDecomposition(mex)
		
		// nothing more to check, as long as it didn't throw an exception
	}
	
	
	// ***** prepareCreateAllottedResourceTXC *****
			
	@Test
	// @Ignore  
	public void prepareCreateAllottedResourceTXC() {
		ExecutionEntity mex = setupMock()
		initPrepareCreateAllottedResourceTXC(mex)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.prepareCreateAllottedResourceTXC(mex)

		verify(mex).getVariable(DBGFLAG)
		
		verify(mex).setVariable("createTXCAR", true)
		verify(mex).setVariable("allottedResourceModelInfoTXC", "modelB")
		verify(mex).setVariable("allottedResourceRoleTXC", "TXCr")
		verify(mex).setVariable("allottedResourceTypeTXC", "TunnelXConn")
		verify(mex).setVariable("parentServiceInstanceIdTXC", "homeB")
	}
			
	@Test
	// @Ignore  
	public void prepareCreateAllottedResourceTXC_NullArList() {
		ExecutionEntity mex = setupMock()
		def svcdecomp = initPrepareCreateAllottedResourceTXC(mex)
		
		when(svcdecomp.getServiceAllottedResources()).thenReturn(null)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.prepareCreateAllottedResourceTXC(mex)

		verify(mex).getVariable(DBGFLAG)
		
		verify(mex, never()).setVariable("createTXCAR", true)
		verify(mex, never()).setVariable("allottedResourceModelInfoTXC", "modelB")
		verify(mex, never()).setVariable("allottedResourceRoleTXC", "TXCr")
		verify(mex, never()).setVariable("allottedResourceTypeTXC", "TunnelXConn")
		verify(mex, never()).setVariable("parentServiceInstanceIdTXC", "homeB")
	}
			
	@Test
	// @Ignore  
	public void prepareCreateAllottedResourceTXC_Ex() {
		ExecutionEntity mex = setupMock()
		initPrepareCreateAllottedResourceTXC(mex)
		
		when(mex.getVariable("createVcpeServiceRequest")).thenThrow(new RuntimeException("expected exception"))
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> CreateVcpeResCustService.prepareCreateAllottedResourceTXC(mex) }))
	}
	
	
	// ***** prepareCreateAllottedResourceBRG *****
			
	@Test
	// @Ignore  
	public void prepareCreateAllottedResourceBRG() {
		ExecutionEntity mex = setupMock()
		initPrepareCreateAllottedResourceBRG(mex)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.prepareCreateAllottedResourceBRG(mex)

		verify(mex).getVariable(DBGFLAG)
		
		verify(mex).setVariable("createBRGAR", true)
		verify(mex).setVariable("allottedResourceModelInfoBRG", "modelB")
		verify(mex).setVariable("allottedResourceRoleBRG", "BRGr")
		verify(mex).setVariable("allottedResourceTypeBRG", "BRG")
		verify(mex).setVariable("parentServiceInstanceIdBRG", "homeB")
	}
			
	@Test
	// @Ignore  
	public void prepareCreateAllottedResourceBRG_NullArList() {
		ExecutionEntity mex = setupMock()
		def svcdecomp = initPrepareCreateAllottedResourceBRG(mex)
		
		when(svcdecomp.getServiceAllottedResources()).thenReturn(null)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.prepareCreateAllottedResourceBRG(mex)

		verify(mex).getVariable(DBGFLAG)
		
		verify(mex, never()).setVariable("createBRGAR", true)
		verify(mex, never()).setVariable("allottedResourceModelInfoBRG", "modelB")
		verify(mex, never()).setVariable("allottedResourceRoleBRG", "BRGr")
		verify(mex, never()).setVariable("allottedResourceTypeBRG", "BRG")
		verify(mex, never()).setVariable("parentServiceInstanceIdBRG", "homeB")
	}
			
	@Test
	// @Ignore  
	public void prepareCreateAllottedResourceBRG_Ex() {
		ExecutionEntity mex = setupMock()
		initPrepareCreateAllottedResourceBRG(mex)
		
		when(mex.getVariable("createVcpeServiceRequest")).thenThrow(new RuntimeException("expected exception"))
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> CreateVcpeResCustService.prepareCreateAllottedResourceBRG(mex) }))
	}
	
	
	// ***** prepareVnfAndModulesCreate *****
			
	@Test
	// @Ignore  
	public void prepareVnfAndModulesCreate() {
		ExecutionEntity mex = setupMock()
		initPrepareVnfAndModulesCreate(mex)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.prepareVnfAndModulesCreate(mex)

		verify(mex).getVariable(DBGFLAG)
		
		verify(mex).setVariable("productFamilyId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb")
		verify(mex).setVariable("lcpCloudRegionId", "mdt1")
		verify(mex).setVariable("tenantId", "8b1df54faa3b49078e3416e21370a3ba")
	}
			
	@Test
	// @Ignore  
	public void prepareVnfAndModulesCreate_EmptyList() {
		ExecutionEntity mex = setupMock()
		initPrepareVnfAndModulesCreate(mex)
		
		when(mex.getVariable("vnfList")).thenReturn(new LinkedList<VnfResource>())
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.prepareVnfAndModulesCreate(mex)

		verify(mex).getVariable(DBGFLAG)
		
		verify(mex).setVariable("productFamilyId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb")
		verify(mex).setVariable("lcpCloudRegionId", "mdt1")
		verify(mex).setVariable("tenantId", "8b1df54faa3b49078e3416e21370a3ba")
	}
			
	@Test
	// @Ignore  
	public void prepareVnfAndModulesCreate_NullList() {
		ExecutionEntity mex = setupMock()
		initPrepareVnfAndModulesCreate(mex)
		
		when(mex.getVariable("vnfList")).thenReturn(null)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.prepareVnfAndModulesCreate(mex)

		verify(mex).getVariable(DBGFLAG)
		
		verify(mex).setVariable("productFamilyId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb")
		verify(mex).setVariable("lcpCloudRegionId", "mdt1")
		verify(mex).setVariable("tenantId", "8b1df54faa3b49078e3416e21370a3ba")
	}
			
	@Test
	// @Ignore  
	public void prepareVnfAndModulesCreate_Ex() {
		ExecutionEntity mex = setupMock()
		initPrepareVnfAndModulesCreate(mex)
		
		when(mex.getVariable("vnfList")).thenThrow(new RuntimeException("expected exception"))
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> CreateVcpeResCustService.prepareVnfAndModulesCreate(mex) }))
	}
	
	
	// ***** validateVnfCreate *****
			
	@Test
	// @Ignore  
	public void validateVnfCreate() {
		ExecutionEntity mex = setupMock()
		initValidateVnfCreate(mex)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.validateVnfCreate(mex)

		verify(mex).getVariable(DBGFLAG)
		
		verify(mex).setVariable(Prefix+"VnfsCreatedCount", 3)
	}
			
	@Test
	// @Ignore  
	public void validateVnfCreate_Ex() {
		ExecutionEntity mex = setupMock()
		initValidateVnfCreate(mex)
		
		when(mex.getVariable(Prefix+"VnfsCreatedCount")).thenThrow(new RuntimeException("expected exception"))
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> CreateVcpeResCustService.validateVnfCreate(mex) }))
	}
	
	
	// ***** postProcessResponse *****
			
	@Test
	// @Ignore  
	public void postProcessResponse() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPostProcessResponse(mex)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.postProcessResponse(mex)

		verify(mex).getVariable(DBGFLAG)
		
		verify(mex).setVariable(Prefix+"Success", true)
		
		def reqinfo = map.get(Prefix+"CompleteMsoProcessRequest")
		
		assertTrue(reqinfo.indexOf("request-id>mri</") >= 0)
		assertTrue(reqinfo.indexOf("source>mysrc</") >= 0)
		assertTrue(reqinfo.indexOf("serviceInstanceId>sii</") >= 0)
	}
			
	@Test
	// @Ignore  
	public void postProcessResponse_BpmnError() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPostProcessResponse(mex)
		
		when(mex.getVariable("source")).thenThrow(new BpmnError("expected exception"))
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> CreateVcpeResCustService.postProcessResponse(mex) }))
	}
			
	@Test
	// @Ignore  
	public void postProcessResponse_Ex() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPostProcessResponse(mex)
		
		when(mex.getVariable("source")).thenThrow(new RuntimeException("expected exception"))
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> CreateVcpeResCustService.postProcessResponse(mex) }))
	}
	
	
	// ***** preProcessRollback *****
			
	@Test
	// @Ignore  
	public void preProcessRollback() {
		ExecutionEntity mex = setupMock()
		def wfe = initPreProcessRollback(mex)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.preProcessRollback(mex)

		verify(mex).getVariable(DBGFLAG)
		
		verify(mex).setVariable("prevWorkflowException", wfe)
	}
			
	@Test
	// @Ignore  
	public void preProcessRollback_NullWfe() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		def wfe = initPreProcessRollback(mex)
		
		when(mex.getVariable("WorkflowException")).thenReturn(null)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.preProcessRollback(mex)

		verify(mex).getVariable(DBGFLAG)
		
		assertFalse(map.containsKey("prevWorkflowException"))
	}
			
	@Test
	// @Ignore  
	public void preProcessRollback_BpmnError() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		def wfe = initPreProcessRollback(mex)
		
		when(mex.getVariable("WorkflowException")).thenThrow(new BpmnError("expected exception"))
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.preProcessRollback(mex)

		verify(mex).getVariable(DBGFLAG)
		
		assertFalse(map.containsKey("prevWorkflowException"))
	}
			
	@Test
	// @Ignore  
	public void preProcessRollback_Ex() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		def wfe = initPreProcessRollback(mex)
		
		when(mex.getVariable("WorkflowException")).thenThrow(new RuntimeException("expected exception"))
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.preProcessRollback(mex)

		verify(mex).getVariable(DBGFLAG)
		
		assertFalse(map.containsKey("prevWorkflowException"))
	}
	
	
	// ***** postProcessRollback *****
			
	@Test
	// @Ignore  
	public void postProcessRollback() {
		ExecutionEntity mex = setupMock()
		def wfe = initPostProcessRollback(mex)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.postProcessRollback(mex)

		verify(mex).getVariable(DBGFLAG)
		
		verify(mex).setVariable("WorkflowException", wfe)
	}
			
	@Test
	// @Ignore  
	public void postProcessRollback_NullWfe() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		def wfe = initPostProcessRollback(mex)
		
		when(mex.getVariable("prevWorkflowException")).thenReturn(null)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.postProcessRollback(mex)

		verify(mex).getVariable(DBGFLAG)
		
		assertFalse(map.containsKey("WorkflowException"))
	}
			
	@Test
	// @Ignore  
	public void postProcessRollback_BpmnError() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		def wfe = initPostProcessRollback(mex)
		
		when(mex.getVariable("prevWorkflowException")).thenThrow(new BpmnError("expected exception"))
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		assertTrue(doBpmnError( { _ -> CreateVcpeResCustService.postProcessRollback(mex) }))
	}
			
	@Test
	// @Ignore  
	public void postProcessRollback_Ex() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		def wfe = initPostProcessRollback(mex)
		
		when(mex.getVariable("prevWorkflowException")).thenThrow(new RuntimeException("expected exception"))
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.postProcessRollback(mex)

		verify(mex).getVariable(DBGFLAG)
		
		assertFalse(map.containsKey("WorkflowException"))
	}
	
	
	// ***** prepareFalloutRequest *****
			
	@Test
	// @Ignore  
	public void prepareFalloutRequest() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPrepareFalloutRequest(mex)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.prepareFalloutRequest(mex)

		verify(mex, times(2)).getVariable(DBGFLAG)
		
		def fo = map.get(Prefix+"falloutRequest")
		
		assertTrue(fo.indexOf("<hello>world</") >= 0)
		assertTrue(fo.indexOf("ErrorMessage>mymsg</") >= 0)
		assertTrue(fo.indexOf("ErrorCode>999</") >= 0)
	}
			
	@Test
	// @Ignore  
	public void prepareFalloutRequest_Ex() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPrepareFalloutRequest(mex)
		
		when(mex.getVariable("WorkflowException")).thenThrow(new RuntimeException("expected exception"))
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> CreateVcpeResCustService.prepareFalloutRequest(mex) }))
	}
	
	// ***** sendSyncError *****
			
	@Test
	// @Ignore  
	public void sendSyncError() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initSendSyncError(mex)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.sendSyncError(mex)

		verify(mex, times(2)).getVariable(DBGFLAG)
		
		verify(mex).setVariable(processName+"WorkflowResponseSent", "true")
		
		assertEquals("500", map.get(processName+"ResponseCode"))
		assertEquals("Fail", map.get(processName+"Status"))
		
		def resp = map.get(processName+"Response")
		
		assertTrue(resp.indexOf("ErrorMessage>mymsg</") >= 0)
		
		verify(mex).setVariable("WorkflowResponse", resp)
	}
			
	@Test
	// @Ignore  
	public void sendSyncError_NotWfe() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initSendSyncError(mex)
		
		when(mex.getVariable("WorkflowException")).thenReturn("not a WFE")
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.sendSyncError(mex)

		verify(mex, times(2)).getVariable(DBGFLAG)
		
		verify(mex).setVariable(processName+"WorkflowResponseSent", "true")
		
		assertEquals("500", map.get(processName+"ResponseCode"))
		assertEquals("Fail", map.get(processName+"Status"))
		
		def resp = map.get(processName+"Response")
		
		assertTrue(resp.indexOf("ErrorMessage>Sending Sync Error.</") >= 0)
		
		verify(mex).setVariable("WorkflowResponse", resp)
	}
			
	@Test
	// @Ignore  
	public void sendSyncError_NullWfe() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initSendSyncError(mex)
		
		when(mex.getVariable("WorkflowException")).thenReturn(null)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		CreateVcpeResCustService.sendSyncError(mex)

		verify(mex, times(2)).getVariable(DBGFLAG)
		
		verify(mex).setVariable(processName+"WorkflowResponseSent", "true")
		
		assertEquals("500", map.get(processName+"ResponseCode"))
		assertEquals("Fail", map.get(processName+"Status"))
		
		def resp = map.get(processName+"Response")
		
		assertTrue(resp.indexOf("ErrorMessage>Sending Sync Error.</") >= 0)
		
		verify(mex).setVariable("WorkflowResponse", resp)
	}
			
	@Test
	// @Ignore  
	public void sendSyncError_Ex() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initSendSyncError(mex)
		
		when(mex.getVariable("WorkflowException")).thenThrow(new RuntimeException("expected exception"))
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		
		CreateVcpeResCustService.sendSyncError(mex)
		
		assertFalse(map.containsKey(processName+"ResponseCode"))
	}
	
	
	// ***** processJavaException *****
			
	@Test
	// @Ignore  
	public void processJavaException() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initProcessJavaException(mex)
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> CreateVcpeResCustService.processJavaException(mex) }))

		verify(mex, times(2)).getVariable(DBGFLAG)
		
		verify(mex).setVariable("prefix", Prefix)
		
		def wfe = map.get("WorkflowException")
		
		assertEquals("Caught a Java Lang Exception", wfe.getErrorMessage())
	}
			
	@Test
	// @Ignore  
	public void processJavaException_BpmnError() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initProcessJavaException(mex)
		
		when(mex.getVariables()).thenThrow(new BpmnError("expected exception"))
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> CreateVcpeResCustService.processJavaException(mex) }))
		
		assertFalse(map.containsKey("WorkflowException"))
	}
			
	@Test
	// @Ignore  
	public void processJavaException_Ex() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initProcessJavaException(mex)
		
		when(mex.getVariables()).thenThrow(new RuntimeException("expected exception"))
		
		CreateVcpeResCustService CreateVcpeResCustService = new CreateVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> CreateVcpeResCustService.processJavaException(mex) }))
		
		def wfe = map.get("WorkflowException")
		
		assertEquals("Exception in processJavaException method", wfe.getErrorMessage())
	}
	
	
	private void initPreProcess(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("bpmnRequest")).thenReturn(request)
		when(mex.getVariable("URN_mso_workflow_aai_distribution_delay")).thenReturn("aaidelay")
		when(mex.getVariable("mso-request-id")).thenReturn("mri")
		when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
		when(mex.getVariable("requestAction")).thenReturn("ra")
	}
	
	private initSendSyncResponse(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("mso-request-id")).thenReturn("mri")
		when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
	}
	
	private void initPrepareDecomposeService(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("createVcpeServiceRequest")).thenReturn('{"requestDetails":{"modelInfo":"mi"}}')
	}
	
	private void initPrepareCreateServiceInstance(ExecutionEntity mex) {
		ServiceDecomposition svcdecomp = mock(ServiceDecomposition.class)
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("createVcpeServiceRequest")).thenReturn(request)
		when(mex.getVariable("serviceDecomposition")).thenReturn(svcdecomp)
		
		when(svcdecomp.toJsonStringNoRootName()).thenReturn("mydecomp")
	}
	
	private void initPostProcessServiceInstanceCreate(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("mso-request-id")).thenReturn("mri")
		when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
		when(mex.getVariable("serviceInstanceName")).thenReturn("sin")
	}
	
	private ServiceDecomposition initProcessDecomposition(ExecutionEntity mex) {
		List<VnfResource> vnflst = new LinkedList<>()
		vnflst.add(makeVnf("", ""))
		vnflst.add(makeVnf("2", "BRG"))
		vnflst.add(makeVnf("3", "BRG"))
			
		ServiceDecomposition svcdecomp = mock(ServiceDecomposition.class)
		when(svcdecomp.getServiceVnfs()).thenReturn(vnflst)
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("serviceDecomposition")).thenReturn(svcdecomp)
		when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
		when(mex.getVariable("serviceInstanceName")).thenReturn("sin")
		
		return svcdecomp
	}
	
	private ServiceDecomposition initFilterVnfs(ExecutionEntity mex) {
		List<VnfResource> vnflst = new LinkedList<>()
		vnflst.add(makeVnf("", "BRG"))
		vnflst.add(makeVnf("2", "TunnelXConn"))
		vnflst.add(makeVnf("3", ""))
		vnflst.add(makeVnf("4", "BRG"))
		vnflst.add(makeVnf("5", "other"))
			
		ServiceDecomposition svcdecomp = mock(ServiceDecomposition.class)
		when(svcdecomp.getServiceVnfs()).thenReturn(vnflst)
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("serviceDecomposition")).thenReturn(svcdecomp)
		when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
		when(mex.getVariable("serviceInstanceName")).thenReturn("sin")
		
		return svcdecomp
	}
	
	private initAwaitAaiDistribution(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
	}
	
	private ServiceDecomposition initPrepareCreateAllottedResourceTXC(ExecutionEntity mex) {
		ServiceDecomposition svcdecomp = mock(ServiceDecomposition.class)
		List<AllottedResource> arlst = new LinkedList<>()
		
		arlst.add(makeArBRG("A"))
		arlst.add(makeArTXC("B"))
		arlst.add(makeArBRG("C"))
		
		when(svcdecomp.getServiceAllottedResources()).thenReturn(arlst)
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("createVcpeServiceRequest")).thenReturn(request)
		when(mex.getVariable("serviceDecomposition")).thenReturn(svcdecomp)
		when(mex.getVariable("allottedResourceId")).thenReturn(ARID)
		
		return svcdecomp
	}
	
	private ServiceDecomposition initPrepareCreateAllottedResourceBRG(ExecutionEntity mex) {
		ServiceDecomposition svcdecomp = mock(ServiceDecomposition.class)
		List<AllottedResource> arlst = new LinkedList<>()
		
		arlst.add(makeArTXC("A"))
		arlst.add(makeArBRG("B"))
		arlst.add(makeArTXC("C"))
		
		when(svcdecomp.getServiceAllottedResources()).thenReturn(arlst)
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("createVcpeServiceRequest")).thenReturn(request)
		when(mex.getVariable("serviceDecomposition")).thenReturn(svcdecomp)
		when(mex.getVariable("allottedResourceId")).thenReturn(ARID)
		
		return svcdecomp
	}
	
	private AllottedResource makeArTXC(String id) {
		AllottedResource ar = mock(AllottedResource.class)
		ModelInfo mod = mock(ModelInfo.class)
		HomingSolution home = mock(HomingSolution.class)
		
		when(ar.toJsonStringNoRootName()).thenReturn("json"+id)
		when(ar.getAllottedResourceType()).thenReturn("TunnelXConn")
		when(ar.getModelInfo()).thenReturn(mod)
		when(ar.getAllottedResourceRole()).thenReturn("TXCr")
		when(ar.getHomingSolution()).thenReturn(home)
		
		when(mod.toJsonStringNoRootName()).thenReturn("model"+id)
		
		when(home.getServiceInstanceId()).thenReturn("home"+id)
		
		return ar
	}
	
	private AllottedResource makeArBRG(String id) {
		AllottedResource ar = mock(AllottedResource.class)
		ModelInfo mod = mock(ModelInfo.class)
		HomingSolution home = mock(HomingSolution.class)
		
		when(ar.toJsonStringNoRootName()).thenReturn("json"+id)
		when(ar.getAllottedResourceType()).thenReturn("BRG")
		when(ar.getModelInfo()).thenReturn(mod)
		when(ar.getAllottedResourceRole()).thenReturn("BRGr")
		when(ar.getHomingSolution()).thenReturn(home)
		
		when(mod.toJsonStringNoRootName()).thenReturn("model"+id)
		
		when(home.getServiceInstanceId()).thenReturn("home"+id)
		
		return ar
	}
	
	private initPrepareVnfAndModulesCreate(ExecutionEntity mex) {
		
		List<VnfResource> vnflst = new LinkedList<>()
		
		vnflst.add(makeVnf("A", "BRG"))
		vnflst.add(makeVnf("B", ""))
		vnflst.add(makeVnf("C", ""))
		vnflst.add(makeVnf("D", "TunnelXConn"))
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("createVcpeServiceRequest")).thenReturn(request)
		when(mex.getVariable("vnfList")).thenReturn(vnflst)
		when(mex.getVariable(Prefix+"VnfsCreatedCount")).thenReturn(2)
		when(mex.getVariable("vnfModelInfo")).thenReturn("nomodel")
		when(mex.getVariable("sdncVersion")).thenReturn("myvers")
	}
	
	private VnfResource makeVnf(String id, String role) {		
		ModelInfo mod = mock(ModelInfo.class)		
		VnfResource vnf = mock(VnfResource.class)
		
		when(mod.toString()).thenReturn('{"modelInfo":"mymodel'+id+'"}')
		
		when(vnf.toString()).thenReturn("myvnf"+id)
		when(vnf.getModelInfo()).thenReturn(mod)
		when(vnf.getNfRole()).thenReturn(role)

		return vnf
	}
	
	private initValidateVnfCreate(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable(Prefix+"VnfsCreatedCount")).thenReturn(2)
	}
	
	private initPostProcessResponse(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("source")).thenReturn("mysrc")
		when(mex.getVariable("mso-request-id")).thenReturn("mri")
		when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
	}
	
	private WorkflowException initPreProcessRollback(ExecutionEntity mex) {
		WorkflowException wfe = mock(WorkflowException.class)
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("WorkflowException")).thenReturn(wfe)
		
		return wfe
	}
	
	private WorkflowException initPostProcessRollback(ExecutionEntity mex) {
		WorkflowException wfe = mock(WorkflowException.class)
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("prevWorkflowException")).thenReturn(wfe)
		
		return wfe
	}
	
	private initPrepareFalloutRequest(ExecutionEntity mex) {
		WorkflowException wfe = mock(WorkflowException.class)
		
		when(wfe.getErrorMessage()).thenReturn("mymsg")
		when(wfe.getErrorCode()).thenReturn(999)
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("WorkflowException")).thenReturn(wfe)
		when(mex.getVariable(Prefix+"requestInfo")).thenReturn("<hello>world</hello>")
		
		return wfe
	}
	
	private initSendSyncError(ExecutionEntity mex) {
		WorkflowException wfe = mock(WorkflowException.class)
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("mso-request-id")).thenReturn("mri")
		when(mex.getVariable("WorkflowException")).thenReturn(wfe)
		
		when(wfe.getErrorMessage()).thenReturn("mymsg")
	}
	
	private initProcessJavaException(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
	}
		
}
