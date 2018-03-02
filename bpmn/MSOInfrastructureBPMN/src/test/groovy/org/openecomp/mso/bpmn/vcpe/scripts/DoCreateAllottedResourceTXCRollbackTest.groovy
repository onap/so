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
import org.camunda.bpm.engine.delegate.DelegateExecution
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
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import static org.junit.Assert.*;
import static org.mockito.Mockito.*
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDeleteAllottedResource
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetAllottedResource
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchAllottedResource

import org.openecomp.mso.bpmn.core.RollbackData

import com.github.tomakehurst.wiremock.junit.WireMockRule

class DoCreateAllottedResourceTXCRollbackTest extends GroovyTestBase {
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(PORT)

	String Prefix = "DCARTXCRB_"
	String RbType = "DCARTXC_"

	@BeforeClass
	public static void setUpBeforeClass() {
		// nothing for now
	}
	  
    @Before
	public void init()
	{
		MockitoAnnotations.initMocks(this)
	}
	
	public DoCreateAllottedResourceTXCRollbackTest() {
		super("DoCreateAllottedResourceTXCRollback")
	}
	
	
	// ***** preProcessRequest *****
			
	@Test
//	@Ignore  
	public void preProcessRequest() {
		ExecutionEntity mex = setupMock()
		initPreProcess(mex)
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		DoCreateAllottedResourceTXCRollback.preProcessRequest(mex)

		verify(mex).getVariable(DBGFLAG)
		verify(mex).setVariable("prefix", Prefix)
		verify(mex).setVariable("serviceInstanceId", "sii")
		verify(mex).setVariable("parentServiceInstanceId", "psii")
		verify(mex).setVariable("allottedResourceId", "myid")
		verify(mex).setVariable("rollbackAAI", true)
		verify(mex).setVariable("aaiARPath", "mypath")
		verify(mex).setVariable("rollbackSDNC", true)
		verify(mex).setVariable("deactivateSdnc", "myactivate")
		verify(mex).setVariable("deleteSdnc", "mycreate")
		verify(mex).setVariable("unassignSdnc", "true")
		verify(mex).setVariable("sdncDeactivateRequest", "activatereq")
		verify(mex).setVariable("sdncDeleteRequest", "createreq")
		verify(mex).setVariable("sdncUnassignRequest", "assignreq")
		
		verify(mex, never()).setVariable("skipRollback", true)
	}
			
	@Test
//	@Ignore  
	public void preProcessRequest_RollbackDisabled() {
		ExecutionEntity mex = setupMock()
		initPreProcess(mex)
		
		when(mex.getVariable("disableRollback")).thenReturn("true")
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		DoCreateAllottedResourceTXCRollback.preProcessRequest(mex)

		verify(mex).getVariable(DBGFLAG)
		verify(mex).setVariable("prefix", Prefix)
		verify(mex).setVariable("serviceInstanceId", "sii")
		verify(mex).setVariable("parentServiceInstanceId", "psii")
		verify(mex).setVariable("allottedResourceId", "myid")
		verify(mex).setVariable("rollbackAAI", true)
		verify(mex).setVariable("aaiARPath", "mypath")
		verify(mex).setVariable("rollbackSDNC", true)
		verify(mex).setVariable("deactivateSdnc", "myactivate")
		verify(mex).setVariable("deleteSdnc", "mycreate")
		verify(mex).setVariable("unassignSdnc", "true")
		verify(mex).setVariable("sdncDeactivateRequest", "activatereq")
		verify(mex).setVariable("sdncDeleteRequest", "createreq")
		verify(mex).setVariable("sdncUnassignRequest", "assignreq")
		
		verify(mex).setVariable("skipRollback", true)
	}
			
	@Test
//	@Ignore  
	public void preProcessRequest_NoAAI() {
		ExecutionEntity mex = setupMock()
		def data = initPreProcess(mex)
		
		when(mex.getVariable("rollbackAAI")).thenReturn(false)
		data.put(RbType, "rollbackAAI", "false")
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		DoCreateAllottedResourceTXCRollback.preProcessRequest(mex)

		verify(mex).getVariable(DBGFLAG)
		verify(mex).setVariable("prefix", Prefix)
		verify(mex).setVariable("serviceInstanceId", "sii")
		verify(mex).setVariable("parentServiceInstanceId", "psii")
		verify(mex).setVariable("allottedResourceId", "myid")
		verify(mex, never()).setVariable("rollbackAAI", true)
		verify(mex, never()).setVariable("aaiARPath", "mypath")
		verify(mex).setVariable("rollbackSDNC", true)
		verify(mex).setVariable("deactivateSdnc", "myactivate")
		verify(mex).setVariable("deleteSdnc", "mycreate")
		verify(mex).setVariable("unassignSdnc", "true")
		verify(mex).setVariable("sdncDeactivateRequest", "activatereq")
		verify(mex).setVariable("sdncDeleteRequest", "createreq")
		verify(mex).setVariable("sdncUnassignRequest", "assignreq")
		
		verify(mex, never()).setVariable("skipRollback", true)
	}
			
	@Test
//	@Ignore  
	public void preProcessRequest_NoAssign() {
		ExecutionEntity mex = setupMock()
		def data = initPreProcess(mex)
		
		when(mex.getVariable("rollbackSDNC")).thenReturn(false)
		data.put(RbType, "rollbackSDNCassign", "false")
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		DoCreateAllottedResourceTXCRollback.preProcessRequest(mex)

		verify(mex).getVariable(DBGFLAG)
		verify(mex).setVariable("prefix", Prefix)
		verify(mex).setVariable("serviceInstanceId", "sii")
		verify(mex).setVariable("parentServiceInstanceId", "psii")
		verify(mex).setVariable("allottedResourceId", "myid")
		verify(mex).setVariable("rollbackAAI", true)
		verify(mex).setVariable("aaiARPath", "mypath")
		verify(mex, never()).setVariable("rollbackSDNC", true)
		verify(mex, never()).setVariable("deactivateSdnc", "myactivate")
		verify(mex, never()).setVariable("deleteSdnc", "mycreate")
		verify(mex, never()).setVariable("unassignSdnc", "true")
		verify(mex, never()).setVariable("sdncDeactivateRequest", "activatereq")
		verify(mex, never()).setVariable("sdncDeleteRequest", "createreq")
		verify(mex, never()).setVariable("sdncUnassignRequest", "assignreq")
		
		verify(mex, never()).setVariable("skipRollback", true)
	}
			
	@Test
//	@Ignore  
	public void preProcessRequest_NoAAI_NoAssign() {
		ExecutionEntity mex = setupMock()
		initPreProcess(mex)
		
		when(mex.getVariable("rollbackAAI")).thenReturn(false)
		when(mex.getVariable("rollbackSDNC")).thenReturn(false)
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		DoCreateAllottedResourceTXCRollback.preProcessRequest(mex)

		verify(mex).setVariable("skipRollback", true)
	}
			
	@Test
//	@Ignore  
	public void preProcessRequest_NoRbStructure() {
		ExecutionEntity mex = setupMock()
		initPreProcess(mex)
		
		when(mex.getVariable("rollbackData")).thenReturn(new RollbackData())
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		DoCreateAllottedResourceTXCRollback.preProcessRequest(mex)

		verify(mex).setVariable("skipRollback", true)
	}
			
	@Test
//	@Ignore  
	public void preProcessRequest_NullRb() {
		ExecutionEntity mex = setupMock()
		initPreProcess(mex)
		
		when(mex.getVariable("rollbackData")).thenReturn(null)
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		DoCreateAllottedResourceTXCRollback.preProcessRequest(mex)

		verify(mex).setVariable("skipRollback", true)
	}
			
	@Test
//	@Ignore  
	public void preProcessRequest_BpmnError() {
		ExecutionEntity mex = setupMock()
		initPreProcess(mex)
		
		when(mex.getVariable("rollbackData")).thenThrow(new BpmnError("expected exception"))
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		
		assertTrue(doBpmnError( { _ -> DoCreateAllottedResourceTXCRollback.preProcessRequest(mex) }))
	}
			
	@Test
//	@Ignore  
	public void preProcessRequest_Ex() {
		ExecutionEntity mex = setupMock()
		initPreProcess(mex)
		
		when(mex.getVariable("rollbackData")).thenThrow(new RuntimeException("expected exception"))
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		
		assertTrue(doBpmnError( { _ -> DoCreateAllottedResourceTXCRollback.preProcessRequest(mex) }))
	}
			
	@Test
//	@Ignore  
	public void updateAaiAROrchStatus() {
		ExecutionEntity mex = setupMock()
		initUpdateAaiAROrchStatus(mex)
		
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceTXCRollback/arGetById.xml")
		MockPatchAllottedResource(CUST, SVC, INST, ARID)
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		DoCreateAllottedResourceTXCRollback.updateAaiAROrchStatus(mex, "success")
	}
			
	@Test
//	@Ignore  
	public void updateAaiAROrchStatus_EmptyResponse() {
		ExecutionEntity mex = setupMock()
		initUpdateAaiAROrchStatus(mex)
		
		wireMockRule
			.stubFor(get(urlMatching("/aai/v[0-9]+/.*"))
					.willReturn(aResponse()
						.withStatus(200)))
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		
		assertTrue(doBpmnError( { _ -> DoCreateAllottedResourceTXCRollback.updateAaiAROrchStatus(mex, "success") }))
	}
			
	@Test
//	@Ignore  
	public void updateAaiAROrchStatus_NoArPath() {
		ExecutionEntity mex = setupMock()
		initUpdateAaiAROrchStatus(mex)
		
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceTXCRollback/arGetById.xml")
		MockPatchAllottedResource(CUST, SVC, INST, ARID)
			
		when(mex.getVariable("aaiARPath")).thenReturn(null)
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		
		assertTrue(doBpmnError( { _ -> DoCreateAllottedResourceTXCRollback.updateAaiAROrchStatus(mex, "success") }))
	}
	
	
	// ***** validateSDNCResp *****
	
	@Test
//	@Ignore
	public void validateSDNCResp() {
		ExecutionEntity mex = setupMock()
		initValidateSDNCResp(mex)
		def resp = initValidateSDNCResp_Resp()
		
		when(mex.getVariable(Prefix+"sdncResponseSuccess")).thenReturn(true)
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		
		DoCreateAllottedResourceTXCRollback.validateSDNCResp(mex, resp, "create")
		
		verify(mex).getVariable("WorkflowException")
		verify(mex).getVariable("SDNCA_SuccessIndicator")
	}
	
	@Test
//	@Ignore
	public void validateSDNCResp_Unsuccessful() {
		ExecutionEntity mex = setupMock()
		initValidateSDNCResp(mex)
		def resp = initValidateSDNCResp_Resp()
		
		when(mex.getVariable(Prefix+"sdncResponseSuccess")).thenReturn(false)
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		
		assertTrue(doBpmnError( { _ -> DoCreateAllottedResourceTXCRollback.validateSDNCResp(mex, resp, "create") }))
	}
	
	@Test
//	@Ignore
	public void validateSDNCResp_BpmnError404() {
		ExecutionEntity mex = setupMock()
		initValidateSDNCResp(mex)
		def resp = initValidateSDNCResp_Resp()
		when(mex.getVariable(Prefix+"sdncResponseSuccess")).thenReturn(true)
		
		when(mex.getVariable("WorkflowException")).thenThrow(new BpmnError("404", "expected exception"))
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		
		DoCreateAllottedResourceTXCRollback.validateSDNCResp(mex, resp, "create")
	}
	
	@Test
//	@Ignore
	public void validateSDNCResp_BpmnError() {
		ExecutionEntity mex = setupMock()
		initValidateSDNCResp(mex)
		def resp = initValidateSDNCResp_Resp()
		when(mex.getVariable(Prefix+"sdncResponseSuccess")).thenReturn(true)
		
		when(mex.getVariable("WorkflowException")).thenThrow(new BpmnError("expected exception"))
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		
		assertTrue(doBpmnError( { _ -> DoCreateAllottedResourceTXCRollback.validateSDNCResp(mex, resp, "create") }))
	}
	
	@Test
//	@Ignore
	public void validateSDNCResp_Ex() {
		ExecutionEntity mex = setupMock()
		initValidateSDNCResp(mex)
		def resp = initValidateSDNCResp_Resp()
		when(mex.getVariable(Prefix+"sdncResponseSuccess")).thenReturn(true)
		
		when(mex.getVariable("WorkflowException")).thenThrow(new RuntimeException("expected exception"))
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		
		assertTrue(doBpmnError( { _ -> DoCreateAllottedResourceTXCRollback.validateSDNCResp(mex, resp, "create") }))
	}
			
	@Test
//	@Ignore  
	public void deleteAaiAR() {
		ExecutionEntity mex = setupMock()
		initDeleteAaiAR(mex)
		
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceTXCRollback/arGetById.xml")
		MockDeleteAllottedResource(CUST, SVC, INST, ARID, VERS)
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		DoCreateAllottedResourceTXCRollback.deleteAaiAR(mex)
	}
			
	@Test
//	@Ignore  
	public void deleteAaiAR_NoArPath() {
		ExecutionEntity mex = setupMock()
		initDeleteAaiAR(mex)
		
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceTXCRollback/arGetById.xml")
		MockDeleteAllottedResource(CUST, SVC, INST, ARID, VERS)
			
		when(mex.getVariable("aaiARPath")).thenReturn("")
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		
		assertTrue(doBpmnError( { _ -> DoCreateAllottedResourceTXCRollback.deleteAaiAR(mex) }))
	}
			
	@Test
//	@Ignore  
	public void deleteAaiAR_BpmnError() {
		ExecutionEntity mex = setupMock()
		initDeleteAaiAR(mex)
		
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceTXCRollback/arGetById.xml")
		MockDeleteAllottedResource(CUST, SVC, INST, ARID, VERS)
			
		when(mex.getVariable("aaiARPath")).thenThrow(new BpmnError("expected exception"))
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		
		assertTrue(doBpmnError( { _ -> DoCreateAllottedResourceTXCRollback.deleteAaiAR(mex) }))
	}
			
	@Test
//	@Ignore  
	public void deleteAaiAR_Ex() {
		ExecutionEntity mex = setupMock()
		initDeleteAaiAR(mex)
		
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoCreateAllottedResourceTXCRollback/arGetById.xml")
		MockDeleteAllottedResource(CUST, SVC, INST, ARID, VERS)
			
		when(mex.getVariable("aaiARPath")).thenThrow(new RuntimeException("expected exception"))
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		
		assertTrue(doBpmnError( { _ -> DoCreateAllottedResourceTXCRollback.deleteAaiAR(mex) }))
	}
			
	@Test
//	@Ignore  
	public void postProcessRequest() {
		ExecutionEntity mex = setupMock()
		initPostProcessRequest(mex)
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		DoCreateAllottedResourceTXCRollback.postProcessRequest(mex)

		verify(mex).getVariable(DBGFLAG)
		verify(mex).setVariable("rollbackData", null)
		verify(mex).setVariable("rolledBack", true)
	}
			
	@Test
//	@Ignore  
	public void postProcessRequest_RolledBack() {
		ExecutionEntity mex = setupMock()
		initPostProcessRequest(mex)
		
		when(mex.getVariable("skipRollback")).thenReturn(true)
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		DoCreateAllottedResourceTXCRollback.postProcessRequest(mex)

		verify(mex).getVariable(DBGFLAG)
		verify(mex).setVariable("rollbackData", null)
		verify(mex, never()).setVariable("rolledBack", true)
	}
			
	@Test
//	@Ignore  
	public void postProcessRequest_BpmnError() {
		ExecutionEntity mex = setupMock()
		initPostProcessRequest(mex)
		
		when(mex.getVariable("skipRollback")).thenThrow(new BpmnError("expected exception"))
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		DoCreateAllottedResourceTXCRollback.postProcessRequest(mex)

		verify(mex).getVariable(DBGFLAG)
		verify(mex).setVariable("rollbackData", null)
		verify(mex, never()).setVariable("rolledBack", true)
	}
			
	@Test
//	@Ignore  
	public void postProcessRequest_Ex() {
		ExecutionEntity mex = setupMock()
		initPostProcessRequest(mex)
		
		when(mex.getVariable("skipRollback")).thenThrow(new RuntimeException("expected exception"))
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		DoCreateAllottedResourceTXCRollback.postProcessRequest(mex)

		verify(mex).getVariable(DBGFLAG)
		verify(mex).setVariable("rollbackData", null)
		verify(mex, never()).setVariable("rolledBack", true)
	}
			
	@Test
//	@Ignore  
	public void processRollbackException() {
		ExecutionEntity mex = setupMock()
		initProcessRollbackException(mex)
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		DoCreateAllottedResourceTXCRollback.processRollbackException(mex)

		verify(mex).getVariable(DBGFLAG)
		verify(mex).setVariable("rollbackData", null)
		verify(mex).setVariable("rolledBack", false)
		verify(mex).setVariable("rollbackError", "Caught exception in AllottedResource Create Rollback")
		verify(mex).setVariable("WorkflowException", null)
	}
			
	@Test
//	@Ignore  
	public void processRollbackException_BpmnError() {
		ExecutionEntity mex = setupMock()
		initProcessRollbackException(mex)
		
		doThrow(new BpmnError("expected exception")).when(mex).setVariable("rollbackData", null)
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		DoCreateAllottedResourceTXCRollback.processRollbackException(mex)
	}
			
	@Test
//	@Ignore  
	public void processRollbackException_Ex() {
		ExecutionEntity mex = setupMock()
		initProcessRollbackException(mex)
		
		doThrow(new RuntimeException("expected exception")).when(mex).setVariable("rollbackData", null)
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		DoCreateAllottedResourceTXCRollback.processRollbackException(mex)
	}
			
	@Test
//	@Ignore  
	public void processRollbackJavaException() {
		ExecutionEntity mex = setupMock()
		initProcessRollbackException(mex)
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		DoCreateAllottedResourceTXCRollback.processRollbackJavaException(mex)

		verify(mex).getVariable(DBGFLAG)
		verify(mex).setVariable("rollbackData", null)
		verify(mex).setVariable("rolledBack", false)
		verify(mex).setVariable("rollbackError", "Caught Java exception in AllottedResource Create Rollback")
		verify(mex, never()).setVariable("WorkflowException", null)
	}
			
	@Test
//	@Ignore  
	public void processRollbackJavaException_BpmnError() {
		ExecutionEntity mex = setupMock()
		initProcessRollbackException(mex)
		
		doThrow(new BpmnError("expected exception")).when(mex).setVariable("rollbackData", null)
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		DoCreateAllottedResourceTXCRollback.processRollbackJavaException(mex)
	}
			
	@Test
//	@Ignore  
	public void processRollbackJavaException_Ex() {
		ExecutionEntity mex = setupMock()
		initProcessRollbackException(mex)
		
		doThrow(new RuntimeException("expected exception")).when(mex).setVariable("rollbackData", null)
		
		DoCreateAllottedResourceTXCRollback DoCreateAllottedResourceTXCRollback = new DoCreateAllottedResourceTXCRollback()
		DoCreateAllottedResourceTXCRollback.processRollbackJavaException(mex)
	}
	
	private RollbackData initPreProcess(ExecutionEntity mex) {
		def data = new RollbackData()
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("rollbackData")).thenReturn(data)
		when(mex.getVariable("rollbackAAI")).thenReturn(true)
		when(mex.getVariable("rollbackSDNC")).thenReturn(true)
		when(mex.getVariable("disableRollback")).thenReturn("false")
		
		data.put("SERVICEINSTANCE", "allottedResourceId", "myid")
		
		data.put(RbType, "serviceInstanceId", "sii")
		data.put(RbType, "parentServiceInstanceId", "psii")
		
		data.put(RbType, "rollbackAAI", "true")
		data.put(RbType, "aaiARPath", "mypath")
		
		data.put(RbType, "rollbackSDNCassign", "true")
		data.put(RbType, "rollbackSDNCactivate", "myactivate")
		data.put(RbType, "rollbackSDNCcreate", "mycreate")
		data.put(RbType, "sdncActivateRollbackReq", "activatereq")
		data.put(RbType, "sdncCreateRollbackReq", "createreq")
		data.put(RbType, "sdncAssignRollbackReq", "assignreq")
		
		return data
	}
	
	private initUpdateAaiAROrchStatus(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("aaiARPath")).thenReturn(aaiUriPfx + "/aai/v9/business/customers/customer/"+CUST+"/service-subscriptions/service-subscription/"+SVC+"/service-instances/service-instance/"+INST+"/allotted-resources/allotted-resource/"+ARID)
		when(mex.getVariable("URN_aai_endpoint")).thenReturn(aaiUriPfx)
	}
	
	private initValidateSDNCResp(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("prefix")).thenReturn(Prefix)
		when(mex.getVariable("SDNCA_SuccessIndicator")).thenReturn(true)
	}
	
	private String initValidateSDNCResp_Resp() {
		return "<response-data>&lt;response-code&gt;200&lt;/response-code&gt;</response-data>"
	}
	
	private initDeleteAaiAR(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("aaiARResourceVersion")).thenReturn(VERS)
		when(mex.getVariable("aaiARPath")).thenReturn(aaiUriPfx + "/aai/v9/business/customers/customer/"+CUST+"/service-subscriptions/service-subscription/"+SVC+"/service-instances/service-instance/"+INST+"/allotted-resources/allotted-resource/"+ARID)
		when(mex.getVariable("URN_aai_endpoint")).thenReturn(aaiUriPfx)
	}
	
	private initPostProcessRequest(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("skipRollback")).thenReturn(false)
	}
	
	private initProcessRollbackException(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
	}
	
	private initProcessRollbackJavaException(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
	}
		
}
