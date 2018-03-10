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
import org.camunda.bpm.engine.runtime.Execution
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
import static com.github.tomakehurst.wiremock.client.WireMock.put
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import static org.junit.Assert.*
import static org.mockito.Mockito.*
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetAllottedResource
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchAllottedResource
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutAllottedResource
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPutAllottedResource_500

import java.util.Map

import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.vcpe.scripts.MapSetter

import com.github.tomakehurst.wiremock.junit.WireMockRule

class DoCreateAllottedResourceTXCTest extends GroovyTestBase {
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(PORT)

	String Prefix = "DCARTXC_"

	@BeforeClass
	public static void setUpBeforeClass() {
		// nothing for now
	}
	  
    @Before
	public void init()
	{
		MockitoAnnotations.initMocks(this)
	}
	
	public DoCreateAllottedResourceTXCTest() {
		super("DoCreateAllottedResourceTXC")
	}
	
	
	// ***** preProcessRequest *****
			
	@Test
	// @Ignore  
	public void preProcessRequest() {
		ExecutionEntity mex = setupMock()
		initPreProcess(mex)
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		DoCreateAllottedResourceTXC.preProcessRequest(mex)

		verify(mex).getVariable(DBGFLAG)
		verify(mex).setVariable("prefix", Prefix)
				
		assertTrue(checkMissingPreProcessRequest("URN_mso_workflow_sdncadapter_callback"))
		assertTrue(checkMissingPreProcessRequest("URN_mso_workflow_sdnc_replication_delay"))
		assertTrue(checkMissingPreProcessRequest("serviceInstanceId"))
		assertTrue(checkMissingPreProcessRequest("parentServiceInstanceId"))
		assertTrue(checkMissingPreProcessRequest("allottedResourceModelInfo"))
		assertTrue(checkMissingPreProcessRequest("brgWanMacAddress"))
		assertTrue(checkMissingPreProcessRequest("allottedResourceRole"))
		assertTrue(checkMissingPreProcessRequest("allottedResourceType"))
	}
	
	
	// ***** getAaiAR *****
	
	@Test
	// @Ignore
	public void getAaiAR() {
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceTXC/getArTxc.xml")
		
		ExecutionEntity mex = setupMock()
		initGetAaiAR(mex)
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		DoCreateAllottedResourceTXC.getAaiAR(mex)
		
		verify(mex).setVariable("foundActiveAR", true)
	}
	
	@Test
	// @Ignore
	public void getAaiAR_Duplicate() {
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceTXC/getArTxc.xml")
		
		ExecutionEntity mex = setupMock()
		initGetAaiAR(mex)
		
		// fail if duplicate
		when(mex.getVariable("failExists")).thenReturn("true")
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		assertTrue(doBpmnError( { _ -> DoCreateAllottedResourceTXC.getAaiAR(mex) }))
	}
	
	@Test
	// @Ignore
	public void getAaiAR_NotActive() {
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceTXC/getArTxc.xml")
		
		ExecutionEntity mex = setupMock()
		initGetAaiAR(mex)
		
		// not active
		when(mex.getVariable("aaiAROrchStatus")).thenReturn("not-active")
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		assertTrue(doBpmnError( { _ -> DoCreateAllottedResourceTXC.getAaiAR(mex) }))
	}
	
	@Test
	// @Ignore
	public void getAaiAR_NoStatus() {
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceTXC/getArTxc.xml")
		
		ExecutionEntity mex = setupMock()
		initGetAaiAR(mex)
		
		when(mex.getVariable("aaiAROrchStatus")).thenReturn(null)
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		DoCreateAllottedResourceTXC.getAaiAR(mex)
		
		verify(mex, never()).setVariable("foundActiveAR", true)
	}
	
	
	// ***** createAaiAR *****
	
	@Test
	// @Ignore
	public void createAaiAR() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initCreateAaiAr(mex)
		
		MockPutAllottedResource(CUST, SVC, INST, ARID)
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		DoCreateAllottedResourceTXC.createAaiAR(mex)
		
		def data = map.get("rollbackData")
		assertNotNull(data)
		assertTrue(data instanceof RollbackData)
		
		assertEquals("45", data.get(Prefix, "disableRollback"))
		assertEquals("true", data.get(Prefix, "rollbackAAI"))
		assertEquals(ARID, data.get(Prefix, "allottedResourceId"))
		assertEquals("sii", data.get(Prefix, "serviceInstanceId"))
		assertEquals("psii", data.get(Prefix, "parentServiceInstanceId"))
		assertEquals(mex.getVariable("PSI_resourceLink")+"/allotted-resources/allotted-resource/"+ARID, data.get(Prefix, "aaiARPath"))
	}
	
	@Test
	// @Ignore
	public void createAaiAR_NoArid_NoModelUuids() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initCreateAaiAr(mex)
			
		// no allottedResourceId - will be generated
		
		when(mex.getVariable("allottedResourceId")).thenReturn(null)
		
		wireMockRule
			.stubFor(put(urlMatching("/aai/.*/allotted-resource/.*"))
					.willReturn(aResponse()
						.withStatus(200)))
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		DoCreateAllottedResourceTXC.createAaiAR(mex)
		
		def arid = map.get("allottedResourceId")
		assertNotNull(arid)
		assertFalse(arid.isEmpty())
		
		def data = map.get("rollbackData")
		assertNotNull(data)
		assertTrue(data instanceof RollbackData)
		
		assertEquals(arid, data.get(Prefix, "allottedResourceId"))
	}
	
	@Test
	// @Ignore
	public void createAaiAR_MissingPsiLink() {
		ExecutionEntity mex = setupMock()
		initCreateAaiAr(mex)
		
		when(mex.getVariable("PSI_resourceLink")).thenReturn(null)
		
		MockPutAllottedResource(CUST, SVC, INST, ARID)
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.createAaiAR(mex) }))
	}
	
	@Test
	// @Ignore
	public void createAaiAR_HttpFailed() {
		ExecutionEntity mex = setupMock()
		initCreateAaiAr(mex)
		
		MockPutAllottedResource_500(CUST, SVC, INST, ARID)
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.createAaiAR(mex) }))
	}
	
	@Test
	// @Ignore
	public void createAaiAR_BpmnError() {
		ExecutionEntity mex = setupMock()
		initCreateAaiAr(mex)
		
		when(mex.getVariable("URN_aai_endpoint")).thenThrow(new BpmnError("expected exception"))
		
		MockPutAllottedResource(CUST, SVC, INST, ARID)
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.createAaiAR(mex) }))
	}
	
	@Test
	// @Ignore
	public void createAaiAR_Ex() {
		ExecutionEntity mex = setupMock()
		initCreateAaiAr(mex)
		
		when(mex.getVariable("URN_aai_endpoint")).thenThrow(new RuntimeException("expected exception"))
		
		MockPutAllottedResource(CUST, SVC, INST, ARID)
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.createAaiAR(mex) }))
	}
	
	
	// ***** buildSDNCRequest *****
	
	@Test
	// @Ignore
	public void buildSDNCRequest() {
		ExecutionEntity mex = setupMock()
		initBuildSDNCRequest(mex)
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		String result = DoCreateAllottedResourceTXC.buildSDNCRequest(mex, "myact", "myreq")
		
		assertTrue(result.indexOf("<sdncadapter:RequestId>myreq</") >= 0)
		assertTrue(result.indexOf("<sdncadapter:SvcAction>myact</") >= 0)
		assertTrue(result.indexOf("<allotted-resource-id>ari</") >= 0)
		assertTrue(result.indexOf("<sdncadapter:SvcInstanceId>sii</") >= 0)
		assertTrue(result.indexOf("<service-instance-id>sii</") >= 0)
		assertTrue(result.indexOf("<parent-service-instance-id>psii</") >= 0)
		assertTrue(result.indexOf("<subscription-service-type>sst</") >= 0)
		assertTrue(result.indexOf("<global-customer-id>gci</") >= 0)
		assertTrue(result.indexOf("<sdncadapter:CallbackUrl>scu</") >= 0)
		assertTrue(result.indexOf("<request-id>mri</") >= 0)
		assertTrue(result.indexOf("<model-invariant-uuid/>") >= 0)
		assertTrue(result.indexOf("<model-uuid/>") >= 0)
		assertTrue(result.indexOf("<model-customization-uuid/>") >= 0)
		assertTrue(result.indexOf("<model-version/>") >= 0)
		assertTrue(result.indexOf("<model-name/>") >= 0)
	}
	
	@Test
	// @Ignore
	public void buildSDNCRequest_Ex() {
		ExecutionEntity mex = setupMock()
		initBuildSDNCRequest(mex)
		
		when(mex.getVariable("allottedResourceId")).thenThrow(new RuntimeException("expected exception"))
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.buildSDNCRequest(mex, "myact", "myreq") }))
	}
	
	
	// ***** preProcessSDNCAssign *****
	
	@Test
	// @Ignore
	public void preProcessSDNCAssign() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		def data = initPreProcessSDNC(mex)
					
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		DoCreateAllottedResourceTXC.preProcessSDNCAssign(mex)
		
		def req = map.get("sdncAssignRequest")
		assertNotNull(req)
		
		assertEquals(data, map.get("rollbackData"))
		
		def rbreq = data.get(Prefix, "sdncAssignRollbackReq")
		
		assertTrue(req.indexOf("<sdncadapter:SvcAction>assign</") >= 0)
		assertTrue(req.indexOf("<request-action>CreateTunnelXConnInstance</") >= 0)
		assertTrue(req.indexOf("<sdncadapter:RequestId>") >= 0)
		
		assertTrue(rbreq.indexOf("<sdncadapter:SvcAction>unassign</") >= 0)
		assertTrue(rbreq.indexOf("<request-action>DeleteTunnelXConnInstance</") >= 0)
		assertTrue(rbreq.indexOf("<sdncadapter:RequestId>") >= 0)
	}
	
	@Test
	// @Ignore
	public void preProcessSDNCAssign_BpmnError() {
		ExecutionEntity mex = setupMock()
		initPreProcessSDNC(mex)
		
		when(mex.getVariable("rollbackData")).thenThrow(new BpmnError("expected exception"))
					
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.preProcessSDNCAssign(mex) }))
	}
	
	@Test
	// @Ignore
	public void preProcessSDNCAssign_Ex() {
		ExecutionEntity mex = setupMock()
		initPreProcessSDNC(mex)
		
		when(mex.getVariable("rollbackData")).thenThrow(new RuntimeException("expected exception"))
					
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.preProcessSDNCAssign(mex) }))
	}
	
	
	// ***** preProcessSDNCCreate *****
	
	@Test
	// @Ignore
	public void preProcessSDNCCreate() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		def data = initPreProcessSDNC(mex)
					
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		DoCreateAllottedResourceTXC.preProcessSDNCCreate(mex)
		
		def req = map.get("sdncCreateRequest")
		assertNotNull(req)
		
		assertEquals(data, map.get("rollbackData"))
		
		def rbreq = data.get(Prefix, "sdncCreateRollbackReq")
		
		assertTrue(req.indexOf("<sdncadapter:SvcAction>create</") >= 0)
		assertTrue(req.indexOf("<request-action>CreateTunnelXConnInstance</") >= 0)
		assertTrue(req.indexOf("<sdncadapter:RequestId>") >= 0)
		
		assertTrue(rbreq.indexOf("<sdncadapter:SvcAction>delete</") >= 0)
		assertTrue(rbreq.indexOf("<request-action>DeleteTunnelXConnInstance</") >= 0)
		assertTrue(rbreq.indexOf("<sdncadapter:RequestId>") >= 0)
		
	}
	
	@Test
	// @Ignore
	public void preProcessSDNCCreate_BpmnError() {
		ExecutionEntity mex = setupMock()
		initPreProcessSDNC(mex)
		
		when(mex.getVariable("rollbackData")).thenThrow(new BpmnError("expected exception"))
					
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.preProcessSDNCCreate(mex) }))
	}
	
	@Test
	// @Ignore
	public void preProcessSDNCCreate_Ex() {
		ExecutionEntity mex = setupMock()
		initPreProcessSDNC(mex)
		
		when(mex.getVariable("rollbackData")).thenThrow(new RuntimeException("expected exception"))
					
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.preProcessSDNCCreate(mex) }))
	}
	
	
	// ***** preProcessSDNCActivate *****
	
	@Test
	// @Ignore
	public void preProcessSDNCActivate() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		def data = initPreProcessSDNC(mex)
					
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		DoCreateAllottedResourceTXC.preProcessSDNCActivate(mex)
		
		def req = map.get("sdncActivateRequest")
		assertNotNull(req)
		
		assertEquals(data, map.get("rollbackData"))
		
		def rbreq = data.get(Prefix, "sdncActivateRollbackReq")
		
		assertTrue(req.indexOf("<sdncadapter:SvcAction>activate</") >= 0)
		assertTrue(req.indexOf("<request-action>CreateTunnelXConnInstance</") >= 0)
		assertTrue(req.indexOf("<sdncadapter:RequestId>") >= 0)
		
		assertTrue(rbreq.indexOf("<sdncadapter:SvcAction>deactivate</") >= 0)
		assertTrue(rbreq.indexOf("<request-action>DeleteTunnelXConnInstance</") >= 0)
		assertTrue(rbreq.indexOf("<sdncadapter:RequestId>") >= 0)
		
	}
	
	@Test
	// @Ignore
	public void preProcessSDNCActivate_BpmnError() {
		ExecutionEntity mex = setupMock()
		initPreProcessSDNC(mex)
		
		when(mex.getVariable("rollbackData")).thenThrow(new BpmnError("expected exception"))
					
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.preProcessSDNCActivate(mex) }))
	}
	
	@Test
	// @Ignore
	public void preProcessSDNCActivate_Ex() {
		ExecutionEntity mex = setupMock()
		initPreProcessSDNC(mex)
		
		when(mex.getVariable("rollbackData")).thenThrow(new RuntimeException("expected exception"))
					
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.preProcessSDNCActivate(mex) }))
	}
	
	
	// ***** validateSDNCResp *****
	
	@Test
	// @Ignore
	public void validateSDNCResp() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		def data = initValidateSDNCResp(mex)
		def resp = initValidateSDNCResp_Resp()
		
		when(mex.getVariable(Prefix+"sdncResponseSuccess")).thenReturn(true)
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		DoCreateAllottedResourceTXC.validateSDNCResp(mex, resp, "create")
		
		verify(mex).getVariable("WorkflowException")
		verify(mex).getVariable("SDNCA_SuccessIndicator")
		verify(mex).getVariable("rollbackData")
		
		assertEquals(data, map.get("rollbackData"))
		
		assertEquals("true", data.get(Prefix, "rollback" +  "SDNCcreate"))
		
	}
	
	@Test
	// @Ignore
	public void validateSDNCResp_Get() {
		ExecutionEntity mex = setupMock()
		def data = initValidateSDNCResp(mex)
		def resp = initValidateSDNCResp_Resp()
		
		when(mex.getVariable(Prefix+"sdncResponseSuccess")).thenReturn(true)
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		DoCreateAllottedResourceTXC.validateSDNCResp(mex, resp, "get")
		
		verify(mex).getVariable("WorkflowException")
		verify(mex).getVariable("SDNCA_SuccessIndicator")
		
		verify(mex, never()).getVariable("rollbackData")
	}
	
	@Test
	// @Ignore
	public void validateSDNCResp_Unsuccessful() {
		ExecutionEntity mex = setupMock()
		initValidateSDNCResp(mex)
		def resp = initValidateSDNCResp_Resp()
		
		// unsuccessful
		when(mex.getVariable(Prefix+"sdncResponseSuccess")).thenReturn(false)
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.validateSDNCResp(mex, resp, "create") }))
	}
	
	@Test
	// @Ignore
	public void validateSDNCResp_BpmnError() {
		ExecutionEntity mex = setupMock()
		initValidateSDNCResp(mex)
		def resp = initValidateSDNCResp_Resp()
		
		when(mex.getVariable("WorkflowException")).thenThrow(new BpmnError("expected exception"))
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.validateSDNCResp(mex, resp, "create") }))
	}
	
	@Test
	// @Ignore
	public void validateSDNCResp_Ex() {
		ExecutionEntity mex = setupMock()
		initValidateSDNCResp(mex)
		def resp = initValidateSDNCResp_Resp()
		
		when(mex.getVariable("WorkflowException")).thenThrow(new RuntimeException("expected exception"))
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.validateSDNCResp(mex, resp, "create") }))
	}
	
	
	// ***** preProcessSDNCGet *****
	
	@Test
	// @Ignore
	public void preProcessSDNCGet_FoundAR() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPreProcessSDNCGet(mex)
					
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		DoCreateAllottedResourceTXC.preProcessSDNCGet(mex)
		
		String req = map.get("sdncGetRequest")
		
		assertTrue(req.indexOf("<sdncadapter:RequestId>") >= 0)
		assertTrue(req.indexOf("<sdncadapter:SvcInstanceId>sii</") >= 0)
		assertTrue(req.indexOf("<sdncadapter:SvcOperation>arlink</") >= 0)
		assertTrue(req.indexOf("<sdncadapter:CallbackUrl>myurl</") >= 0)
		
	}
	
	@Test
	// @Ignore
	public void preProcessSDNCGet_NotFoundAR() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPreProcessSDNCGet(mex)
		
		when(mex.getVariable("foundActiveAR")).thenReturn(false)
					
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		DoCreateAllottedResourceTXC.preProcessSDNCGet(mex)
		
		String req = map.get("sdncGetRequest")
		
		assertTrue(req.indexOf("<sdncadapter:RequestId>") >= 0)
		assertTrue(req.indexOf("<sdncadapter:SvcInstanceId>sii</") >= 0)
		assertTrue(req.indexOf("<sdncadapter:SvcOperation>assignlink</") >= 0)
		assertTrue(req.indexOf("<sdncadapter:CallbackUrl>myurl</") >= 0)
		
	}
	
	@Test
	// @Ignore
	public void preProcessSDNCGet_Ex() {
		ExecutionEntity mex = setupMock()
		initPreProcessSDNCGet(mex)
		
		when(mex.getVariable("foundActiveAR")).thenThrow(new RuntimeException("expected exception"))
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.preProcessSDNCGet(mex) }))
	}
	
	
	// ***** updateAaiAROrchStatus *****
	
	@Test
	// @Ignore
	public void updateAaiAROrchStatus() {
		MockPatchAllottedResource(CUST, SVC, INST, ARID)
		
		ExecutionEntity mex = setupMock()
		initUpdateAaiAROrchStatus(mex)
					
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		DoCreateAllottedResourceTXC.updateAaiAROrchStatus(mex, "success")
	}
	
	
	// ***** generateOutputs *****
	
	@Test
	// @Ignore
	public void generateOutputs() {
		ExecutionEntity mex = setupMock()
		def txctop = FileUtil.readResourceFile("__files/VCPE/DoCreateAllottedResourceTXC/SDNCTopologyQueryCallback.xml")
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("enhancedCallbackRequestData")).thenReturn(txctop)
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		DoCreateAllottedResourceTXC.generateOutputs(mex)
		
		verify(mex).setVariable("allotedResourceName", "namefromrequest")
		verify(mex).setVariable("vni", "my-vni")
		verify(mex).setVariable("vgmuxBearerIP", "my-bearer-ip")
		verify(mex).setVariable("vgmuxLanIP", "my-lan-ip")
		
	}
	
	@Test
	// @Ignore
	public void generateOutputs_BadXml() {
		ExecutionEntity mex = setupMock()
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("enhancedCallbackRequestData")).thenReturn("invalid xml")
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		DoCreateAllottedResourceTXC.generateOutputs(mex)
		
		verify(mex, never()).setVariable(anyString(), anyString())
		
	}
	
	@Test
	// @Ignore
	public void generateOutputs_BpmnError() {
		ExecutionEntity mex = setupMock()
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("enhancedCallbackRequestData")).thenThrow(new BpmnError("expected exception"))
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		DoCreateAllottedResourceTXC.generateOutputs(mex)
		verify(mex, never()).setVariable(anyString(), anyString())
		
	}
	
	@Test
	// @Ignore
	public void generateOutputs_Ex() {
		ExecutionEntity mex = setupMock()
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("enhancedCallbackRequestData")).thenThrow(new RuntimeException("expected exception"))
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		DoCreateAllottedResourceTXC.generateOutputs(mex)
		verify(mex, never()).setVariable(anyString(), anyString())
		
	}
	
	
	// ***** preProcessRollback *****
	
	@Test
	// @Ignore
	public void preProcessRollback() {
		ExecutionEntity mex = setupMock()
		WorkflowException wfe = mock(WorkflowException.class)
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("WorkflowException")).thenReturn(wfe)
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		DoCreateAllottedResourceTXC.preProcessRollback(mex)
		
		verify(mex).setVariable("prevWorkflowException", wfe)
		
	}
	
	@Test
	// @Ignore
	public void preProcessRollback_NotWFE() {
		ExecutionEntity mex = setupMock()
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("WorkflowException")).thenReturn("I'm not a WFE")
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		DoCreateAllottedResourceTXC.preProcessRollback(mex)
		
//			verify(mex, never()).setVariable("prevWorkflowException", any())
		
	}
	
	@Test
	// @Ignore
	public void preProcessRollback_BpmnError() {
		ExecutionEntity mex = setupMock()
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("WorkflowException")).thenThrow(new BpmnError("expected exception"))
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		DoCreateAllottedResourceTXC.preProcessRollback(mex)
		
	}
	
	@Test
	// @Ignore
	public void preProcessRollback_Ex() {
		ExecutionEntity mex = setupMock()
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("WorkflowException")).thenThrow(new RuntimeException("expected exception"))
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		DoCreateAllottedResourceTXC.preProcessRollback(mex)
		
	}
	
	
	// ***** postProcessRollback *****
	
	@Test
	// @Ignore
	public void postProcessRollback() {
		ExecutionEntity mex = setupMock()
		WorkflowException wfe = mock(WorkflowException.class)
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("prevWorkflowException")).thenReturn(wfe)
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		DoCreateAllottedResourceTXC.postProcessRollback(mex)
		
		verify(mex).setVariable("WorkflowException", wfe)
		verify(mex).setVariable("rollbackData", null)
		
	}
	
	@Test
	// @Ignore
	public void postProcessRollback_NotWFE() {
		ExecutionEntity mex = setupMock()
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("prevWorkflowException")).thenReturn("I'm not a WFE")
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		DoCreateAllottedResourceTXC.postProcessRollback(mex)
		
//			verify(mex, never()).setVariable("WorkflowException", any())
		verify(mex).setVariable("rollbackData", null)
		
	}
	
	@Test
	// @Ignore
	public void postProcessRollback_BpmnError() {
		ExecutionEntity mex = setupMock()
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("prevWorkflowException")).thenThrow(new BpmnError("expected exception"))
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoCreateAllottedResourceTXC.postProcessRollback(mex) }))
		verify(mex, never()).setVariable("rollbackData", null)
		
	}
	
	@Test
	// @Ignore
	public void postProcessRollback_Ex() {
		ExecutionEntity mex = setupMock()
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("prevWorkflowException")).thenThrow(new RuntimeException("expected exception"))
		
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		DoCreateAllottedResourceTXC.postProcessRollback(mex)
		verify(mex, never()).setVariable("rollbackData", null)
		
	}
	
	private boolean checkMissingPreProcessRequest(String fieldnm) {
		ExecutionEntity mex = setupMock()
		initPreProcess(mex)
								
		DoCreateAllottedResourceTXC DoCreateAllottedResourceTXC = new DoCreateAllottedResourceTXC()
		
		when(mex.getVariable(fieldnm)).thenReturn("")
		
		return doBpmnError( { _ -> DoCreateAllottedResourceTXC.preProcessRequest(mex) })
	}
	
	private void initPreProcess(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("URN_mso_workflow_sdncadapter_callback")).thenReturn("sdncurn")
		when(mex.getVariable("URN_mso_workflow_sdnc_replication_delay")).thenReturn("sdncdelay")
		when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
		when(mex.getVariable("parentServiceInstanceId")).thenReturn("psii")
		when(mex.getVariable("allottedResourceModelInfo")).thenReturn("armi")
		when(mex.getVariable("brgWanMacAddress")).thenReturn("bwma")
		when(mex.getVariable("allottedResourceRole")).thenReturn("arr")
		when(mex.getVariable("allottedResourceType")).thenReturn("art")
	}
	
	private void initGetAaiAR(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("allottedResourceType")).thenReturn("TXCt")
		when(mex.getVariable("allottedResourceRole")).thenReturn("TXCr")
		when(mex.getVariable("CSI_service")).thenReturn(FileUtil.readResourceFile("__files/VCPE/DoCreateAllottedResourceTXC/getAR.xml"))
		when(mex.getVariable("URN_aai_endpoint")).thenReturn(aaiUriPfx)
		when(mex.getVariable("aaiAROrchStatus")).thenReturn("Active")
	}
	
	private initCreateAaiAr(ExecutionEntity mex) {				
		when(mex.getVariable("disableRollback")).thenReturn(45)
		when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
		when(mex.getVariable("parentServiceInstanceId")).thenReturn("psii")
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("allottedResourceId")).thenReturn(ARID)
		when(mex.getVariable("URN_aai_endpoint")).thenReturn(aaiUriPfx)
		when(mex.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn(urnProps.get("mso.workflow.global.default.aai.namespace"))
		when(mex.getVariable("PSI_resourceLink")).thenReturn(aaiUriPfx + "/aai/v9/business/customers/customer/"+CUST+"/service-subscriptions/service-subscription/"+SVC+"/service-instances/service-instance/"+INST)
		when(mex.getVariable("allottedResourceType")).thenReturn("TXCt")
		when(mex.getVariable("allottedResourceRole")).thenReturn("TXCr")
		when(mex.getVariable("CSI_resourceLink")).thenReturn(aaiUriPfx+"/aai/v9/mycsi")
		when(mex.getVariable("allottedResourceModelInfo")).thenReturn("""
				{
					"modelInvariantUuid":"modelinvuuid",
					"modelUuid":"modeluuid",
					"modelCustomizationUuid":"modelcustuuid"
				}
			""")
	}
	
	private initBuildSDNCRequest(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("allottedResourceId")).thenReturn("ari")
		when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
		when(mex.getVariable("parentServiceInstanceId")).thenReturn("psii")
		when(mex.getVariable("subscriptionServiceType")).thenReturn("sst")
		when(mex.getVariable("globalCustomerId")).thenReturn("gci")
		when(mex.getVariable("sdncCallbackUrl")).thenReturn("scu")
		when(mex.getVariable("msoRequestId")).thenReturn("mri")
	}
	
	private RollbackData initPreProcessSDNC(ExecutionEntity mex) {
		def data = new RollbackData()
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("rollbackData")).thenReturn(data)
		
		return data
	}
	
	private initPreProcessSDNCGet(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("sdncCallbackUrl")).thenReturn("myurl")
		when(mex.getVariable("foundActiveAR")).thenReturn(true)
		when(mex.getVariable("aaiARGetResponse")).thenReturn("<selflink>arlink</selflink>")
		when(mex.getVariable("sdncAssignResponse")).thenReturn("<response-data>&lt;object-path&gt;assignlink&lt;/object-path&gt;</response-data>")
		when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
		when(mex.getVariable("sdncCallbackUrl")).thenReturn("myurl")
	}
	
	private RollbackData initValidateSDNCResp(ExecutionEntity mex) {
		def data = new RollbackData()
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("prefix")).thenReturn(Prefix)
		when(mex.getVariable("SDNCA_SuccessIndicator")).thenReturn(true)
		when(mex.getVariable("rollbackData")).thenReturn(data)
		
		return data
	}
	
	private String initValidateSDNCResp_Resp() {
		return "<response-data>&lt;response-code&gt;200&lt;/response-code&gt;</response-data>"
	}
	
	private initUpdateAaiAROrchStatus(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("aaiARPath")).thenReturn(aaiUriPfx + "/aai/v9/business/customers/customer/"+CUST+"/service-subscriptions/service-subscription/"+SVC+"/service-instances/service-instance/"+INST+"/allotted-resources/allotted-resource/"+ARID)
	}
		
}
