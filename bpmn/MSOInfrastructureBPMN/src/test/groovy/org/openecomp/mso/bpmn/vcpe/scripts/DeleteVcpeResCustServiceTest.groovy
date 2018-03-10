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
import static com.github.tomakehurst.wiremock.client.WireMock.get
import static com.github.tomakehurst.wiremock.client.WireMock.patch
import static com.github.tomakehurst.wiremock.client.WireMock.put
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import static org.junit.Assert.*
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

class DeleteVcpeResCustServiceTest extends GroovyTestBase {
	
	private static String request
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(PORT)

	String Prefix = "DVRCS_"
	String RbType = "DCRENI_"

	@BeforeClass
	public static void setUpBeforeClass() {
		request = FileUtil.readResourceFile("__files/VCPE/DeleteVcpeResCustService/request.json")
	}
	  
    @Before
	public void init()
	{
		MockitoAnnotations.initMocks(this)
	}
	
	public DeleteVcpeResCustServiceTest() {
		super("DeleteVcpeResCustService")
	}
	
	
	// ***** preProcessRequest *****
			
	@Test
//	@Ignore  
	public void preProcessRequest() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPreProcess(mex)
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		DeleteVcpeResCustService.preProcessRequest(mex)

		verify(mex).getVariable(DBGFLAG)
		
		assertEquals(Prefix, map.get("prefix"))
		assertEquals(request, map.get("DeleteVcpeResCustServiceRequest"))
		assertEquals("mri", map.get("msoRequestId"))
		assertEquals("ra", map.get("requestAction"))
		assertEquals("VID", map.get("source"))
		assertEquals(CUST, map.get("globalSubscriberId"))
		assertEquals(CUST, map.get("globalCustomerId"))
		assertEquals("false", map.get("disableRollback"))
		assertEquals("a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb", map.get("productFamilyId"))
		assertEquals(SVC, map.get("subscriptionServiceType"))
		
		assertEquals("mdt1", map.get("lcpCloudRegionId"))
		assertEquals("8b1df54faa3b49078e3416e21370a3ba", map.get("tenantId"))
		assertEquals("1707", map.get("sdncVersion"))
		assertEquals("service-instance", map.get("GENGS_type"))
		assertEquals("""{"tenantId":"8b1df54faa3b49078e3416e21370a3ba","lcpCloudRegionId":"mdt1"}""", map.get("cloudConfiguration"))
		assertTrue(map.containsKey(Prefix+"requestInfo"))
		
		def reqinfo = map.get(Prefix+"requestInfo")
		assertTrue(reqinfo.indexOf("<request-id>mri</") >= 0)
		assertTrue(reqinfo.indexOf("<source>VID</") >= 0)
	}
			
	@Test
//	@Ignore  
	public void preProcessRequest_EmptyParts() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPreProcess(mex)
		
		def req = request
					.replace('"source"', '"sourceXXX"')
		
		when(mex.getVariable("bpmnRequest")).thenReturn(req)
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		DeleteVcpeResCustService.preProcessRequest(mex)

		verify(mex).getVariable(DBGFLAG)
		verify(mex).setVariable("prefix", Prefix)
		verify(mex).setVariable("DeleteVcpeResCustServiceRequest", req)
		verify(mex).setVariable("msoRequestId", "mri")
		verify(mex).setVariable("requestAction", "ra")
		verify(mex).setVariable("source", "VID")
		verify(mex).setVariable("globalSubscriberId", CUST)
		verify(mex).setVariable("globalCustomerId", CUST)
		verify(mex).setVariable("disableRollback", "false")
		verify(mex).setVariable("productFamilyId", "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb")
		verify(mex).setVariable("subscriptionServiceType", SVC)	
		
		verify(mex).setVariable("lcpCloudRegionId", "mdt1")
		verify(mex).setVariable("tenantId", "8b1df54faa3b49078e3416e21370a3ba")
		assertEquals("""{"tenantId":"8b1df54faa3b49078e3416e21370a3ba","lcpCloudRegionId":"mdt1"}""", map.get("cloudConfiguration"))
		verify(mex).setVariable("sdncVersion", "1707")
		verify(mex).setVariable("GENGS_type", "service-instance")
		assertTrue(map.containsKey(Prefix+"requestInfo"))
		
		def reqinfo = map.get(Prefix+"requestInfo")
		println reqinfo
		assertTrue(reqinfo.indexOf("<request-id>mri</") >= 0)
		assertTrue(reqinfo.indexOf("<source>VID</") >= 0)
	}
			
	@Test
//	@Ignore  
	public void preProcessRequest_MissingServiceInstanceId() {
		ExecutionEntity mex = setupMock()
		initPreProcess(mex)
		
		when(mex.getVariable("serviceInstanceId")).thenReturn(null)
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> DeleteVcpeResCustService.preProcessRequest(mex) }))
	}
			
	@Test
//	@Ignore  
	public void preProcessRequest_BpmnError() {
		ExecutionEntity mex = setupMock()
		initPreProcess(mex)
		
		when(mex.getVariable("bpmnRequest")).thenThrow(new BpmnError("expected exception"))
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> DeleteVcpeResCustService.preProcessRequest(mex) }))
	}
			
	@Test
//	@Ignore  
	public void preProcessRequest_Ex() {
		ExecutionEntity mex = setupMock()
		initPreProcess(mex)
		
		when(mex.getVariable("bpmnRequest")).thenThrow(new RuntimeException("expected exception"))
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> DeleteVcpeResCustService.preProcessRequest(mex) }))
	}
	
	private void initPreProcess(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("bpmnRequest")).thenReturn(request)
		when(mex.getVariable("mso-request-id")).thenReturn("mri")
		when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
		when(mex.getVariable("requestAction")).thenReturn("ra")
	}
	
	// ***** sendSyncResponse *****
			
	@Test
//	@Ignore  
	public void sendSyncResponse() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initSendSyncResponse(mex)
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		DeleteVcpeResCustService.sendSyncResponse(mex)

		verify(mex, times(2)).getVariable(DBGFLAG)
		
		verify(mex).setVariable(processName+"WorkflowResponseSent", "true")
		
		assertEquals("202", map.get(processName+"ResponseCode"))
		assertEquals("Success", map.get(processName+"Status"))
		
		def resp = map.get(processName+"Response")
		
		assertTrue(resp.indexOf('"instanceId":"sii"') >= 0)
		assertTrue(resp.indexOf('"requestId":"mri"') >= 0)
	}
			
	@Test
//	@Ignore  
	public void sendSyncResponse_Ex() {
		ExecutionEntity mex = setupMock()
		initSendSyncResponse(mex)
		
		when(mex.getVariable("serviceInstanceId")).thenThrow(new RuntimeException("expected exception"))
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> DeleteVcpeResCustService.sendSyncResponse(mex) }))
	}
	
	private initSendSyncResponse(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("mso-request-id")).thenReturn("mri")
		when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
	}
	
	// ***** prepareServiceDelete *****
			
	@Test
//	@Ignore  
	public void prepareServiceDelete() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPrepareServiceDelete(mex)
		
		myMockGetAr("/aai/v11/anytxc", 200, "arGetTXCById.xml")
		myMockGetAr("/aai/v11/anybrg", 200, "arGetBRGById.xml")
		myMockGetAr("/aai/v11/other", 200, "arGetOtherById.xml")
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		DeleteVcpeResCustService.prepareServiceDelete(mex)
		
		verify(mex).setVariable(Prefix+"TunnelXConn", true)
		assertEquals("ar-txcA", map.get("TXC_allottedResourceId"))
		
		verify(mex).setVariable(Prefix+"BRG", true)
		assertEquals("ar-brgB", map.get("BRG_allottedResourceId"))
		
		verify(mex).setVariable(Prefix+"vnfsCount", 2)
		assertNotNull(map.get(Prefix+"relatedVnfIdList"))
		assertEquals("[vnfX, vnfY]", map.get(Prefix+"relatedVnfIdList").toString())
		
		verify(mex, never()).setVariable(processName+"WorkflowResponseSent", "true")
	}
			
	@Test
//	@Ignore  
	public void prepareServiceDelete_NotFound() {
		ExecutionEntity mex = setupMock()
		initPrepareServiceDelete(mex)
		
		when(mex.getVariable("GENGS_FoundIndicator")).thenReturn(false)
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		
		assertTrue(doBpmnError({ _ -> DeleteVcpeResCustService.prepareServiceDelete(mex) }))
		
		verify(mex, never()).setVariable(processName+"WorkflowResponseSent", "true")
	}
			
	@Test
//	@Ignore  
	public void prepareServiceDelete_Empty() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPrepareServiceDelete(mex)
		
		when(mex.getVariable("GENGS_service")).thenReturn("<empty></empty>")
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		DeleteVcpeResCustService.prepareServiceDelete(mex)

		verify(mex).getVariable(DBGFLAG)
		
		verify(mex).setVariable(Prefix+"TunnelXConn", false)
		assertNull(map.get("TXC_allottedResourceId"))
		
		verify(mex).setVariable(Prefix+"BRG", false)
		assertNull(map.get("BRG_allottedResourceId"))
		
		assertEquals(0, map.get(Prefix+"vnfsCount"))
		assertFalse(map.containsKey(Prefix+"relatedVnfIdList"))
		
		verify(mex, never()).setVariable(processName+"WorkflowResponseSent", "true")
	}
			
	@Test
//	@Ignore  
	public void prepareServiceDelete_BpmnError() {
		ExecutionEntity mex = setupMock()
		initPrepareServiceDelete(mex)
		
		when(mex.getVariable("GENGS_FoundIndicator")).thenThrow(new BpmnError("expected exception"))
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		
		assertTrue(doBpmnError({ _ -> DeleteVcpeResCustService.prepareServiceDelete(mex) }))
		
		verify(mex, never()).setVariable(processName+"WorkflowResponseSent", "true")
	}
			
	@Test
//	@Ignore  
	public void prepareServiceDelete_Ex() {
		ExecutionEntity mex = setupMock()
		initPrepareServiceDelete(mex)
		
		when(mex.getVariable("GENGS_FoundIndicator")).thenThrow(new RuntimeException("expected exception"))
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		
		assertTrue(doBpmnError({ _ -> DeleteVcpeResCustService.prepareServiceDelete(mex) }))
		
		verify(mex).setVariable(processName+"WorkflowResponseSent", "true")
	}
	
	private initPrepareServiceDelete(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
		when(mex.getVariable("GENGS_FoundIndicator")).thenReturn(true)
		when(mex.getVariable("mso-request-id")).thenReturn("mri")
		when(mex.getVariable("DeleteVcpeResCustServiceRequest")).thenReturn(request)
		when(mex.getVariable("URN_aai_endpoint")).thenReturn(aaiUriPfx)
		when(mex.getVariable("GENGS_service")).thenReturn(FileUtil.readResourceFile("__files/VCPE/DeleteVcpeResCustService/serviceToDelete.xml"))
	}
	
	// ***** getAaiAr *****
	
	@Test
//	@Ignore
	public void getAaiAr() {
		myMockGetAr("/myurl/ar1", 200, "arGetBRGById.xml")
		
		ExecutionEntity mex = setupMock()
		initGetAaiAr(mex)
				
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		def (type, id) = DeleteVcpeResCustService.getAaiAr(mex, "/myurl/ar1")
		
		assertEquals("BRG", type)
		assertEquals("ar-brgB", id)
	}
	
	@Test
//	@Ignore
	public void getAaiAr_401() {
		myMockGetAr("/myurl/ar1", 401, "arGetBRGById.xml")
		
		ExecutionEntity mex = setupMock()
		initGetAaiAr(mex)
				
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		def (type, id) = DeleteVcpeResCustService.getAaiAr(mex, "/myurl/ar1")
		
		assertEquals(null, type)
		assertEquals(null, id)
	}
	
	@Test
//	@Ignore
	public void getAaiAr_EmptyResponse() {
		myMockGetAr("/myurl/ar1", 200, "empty.txt")
		
		ExecutionEntity mex = setupMock()
		initGetAaiAr(mex)
				
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		def (type, id) = DeleteVcpeResCustService.getAaiAr(mex, "/myurl/ar1")
		
		assertEquals(null, type)
		assertEquals(null, id)
	}
	
	private void initGetAaiAr(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("URN_aai_endpoint")).thenReturn(aaiUriPfx)
	}
	
	// ***** prepareVnfAndModulesDelete *****
			
	@Test
//	@Ignore  
	public void prepareVnfAndModulesDelete() {
		ExecutionEntity mex = setupMock()
		initPrepareVnfAndModulesDelete(mex)
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		DeleteVcpeResCustService.prepareVnfAndModulesDelete(mex)

		verify(mex).getVariable(DBGFLAG)
		
		verify(mex).setVariable("vnfId", "vnfB")
	}
			
	@Test
//	@Ignore  
	public void prepareVnfAndModulesDelete_Empty() {
		ExecutionEntity mex = setupMock()
		initPrepareVnfAndModulesDelete(mex)
		
		when(mex.getVariable(Prefix+"relatedVnfIdList")).thenReturn(new LinkedList())
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		DeleteVcpeResCustService.prepareVnfAndModulesDelete(mex)

		verify(mex).getVariable(DBGFLAG)
		
		verify(mex).setVariable("vnfId", "")
	}
			
	@Test
//	@Ignore  
	public void prepareVnfAndModulesDelete_Ex() {
		ExecutionEntity mex = setupMock()
		initPrepareVnfAndModulesDelete(mex)
		
		when(mex.getVariable(Prefix+"relatedVnfIdList")).thenThrow(new RuntimeException("expected exception"))
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		
		assertTrue(doBpmnError({ _ -> DeleteVcpeResCustService.prepareVnfAndModulesDelete(mex) }))
	}
	
	private initPrepareVnfAndModulesDelete(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable(Prefix+"relatedVnfIdList")).thenReturn(Arrays.asList("vnfA", "vnfB", "vnfC"))
		when(mex.getVariable(Prefix+"vnfsDeletedCount")).thenReturn(1)
	}
	
	// ***** validateVnfDelete *****
			
	@Test
//	@Ignore  
	public void validateVnfDelete() {
		ExecutionEntity mex = setupMock()
		initValidateVnfDelete(mex)
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		DeleteVcpeResCustService.validateVnfDelete(mex)

		verify(mex).getVariable(DBGFLAG)
		
		verify(mex).setVariable(Prefix+"vnfsDeletedCount", 3)
	}
			
	@Test
//	@Ignore  
	public void validateVnfDelete_Ex() {
		ExecutionEntity mex = setupMock()
		initValidateVnfDelete(mex)
		
		when(mex.getVariable(Prefix+"vnfsDeletedCount")).thenThrow(new RuntimeException("expected exception"))
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		
		assertTrue(doBpmnError({ _ -> DeleteVcpeResCustService.validateVnfDelete(mex) }))
	}
	
	private initValidateVnfDelete(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable(Prefix+"vnfsDeletedCount")).thenReturn(2)
	}
	
	// ***** postProcessResponse *****
			
	@Test
//	@Ignore  
	public void postProcessResponse() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPostProcessResponse(mex)
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		DeleteVcpeResCustService.postProcessResponse(mex)

		verify(mex).getVariable(DBGFLAG)
		
		assertEquals(true, map.get(Prefix+"Success"))
		
		def req = map.get(Prefix+"CompleteMsoProcessRequest")
		
		assertTrue(req.indexOf("<request-id>mri</") >= 0)
		assertTrue(req.indexOf("<source>mysrc</") >= 0)
	}
			
	@Test
//	@Ignore  
	public void postProcessResponse_BpmnError() {
		ExecutionEntity mex = setupMock()
		initPostProcessResponse(mex)
		
		when(mex.getVariable("source")).thenThrow(new BpmnError("expected exception"))
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> DeleteVcpeResCustService.postProcessResponse(mex) }))
	}
			
	@Test
//	@Ignore  
	public void postProcessResponse_Ex() {
		ExecutionEntity mex = setupMock()
		initPostProcessResponse(mex)
		
		when(mex.getVariable("source")).thenThrow(new RuntimeException("expected exception"))
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> DeleteVcpeResCustService.postProcessResponse(mex) }))
	}
	
	private initPostProcessResponse(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("source")).thenReturn("mysrc")
		when(mex.getVariable("msoRequestId")).thenReturn("mri")
	}
	
	
	// ***** prepareFalloutRequest *****
			
	@Test
//	@Ignore  
	public void prepareFalloutRequest() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPrepareFalloutRequest(mex)
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		DeleteVcpeResCustService.prepareFalloutRequest(mex)

		verify(mex, times(2)).getVariable(DBGFLAG)
		
		def fo = map.get(Prefix+"falloutRequest")
		
		assertTrue(fo.indexOf("<hello>world</") >= 0)
		assertTrue(fo.indexOf("ErrorMessage>mymsg</") >= 0)
		assertTrue(fo.indexOf("ErrorCode>999</") >= 0)
	}
			
	@Test
//	@Ignore  
	public void prepareFalloutRequest_Ex() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPrepareFalloutRequest(mex)
		
		when(mex.getVariable("WorkflowException")).thenThrow(new RuntimeException("expected exception"))
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> DeleteVcpeResCustService.prepareFalloutRequest(mex) }))
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
	
	// ***** sendSyncError *****
			
	@Test
//	@Ignore  
	public void sendSyncError() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initSendSyncError(mex)
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		DeleteVcpeResCustService.sendSyncError(mex)

		verify(mex, times(2)).getVariable(DBGFLAG)
		
		verify(mex).setVariable(processName+"WorkflowResponseSent", "true")
		
		assertEquals("500", map.get(processName+"ResponseCode"))
		assertEquals("Fail", map.get(processName+"Status"))
		
		def resp = map.get(processName+"Response")
		
		assertTrue(resp.indexOf("ErrorMessage>mymsg</") >= 0)
		
		verify(mex).setVariable("WorkflowResponse", resp)
	}
			
	@Test
//	@Ignore  
	public void sendSyncError_NotWfe() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initSendSyncError(mex)
		
		when(mex.getVariable("WorkflowException")).thenReturn("not a WFE")
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		DeleteVcpeResCustService.sendSyncError(mex)

		verify(mex, times(2)).getVariable(DBGFLAG)
		
		verify(mex).setVariable(processName+"WorkflowResponseSent", "true")
		
		assertEquals("500", map.get(processName+"ResponseCode"))
		assertEquals("Fail", map.get(processName+"Status"))
		
		def resp = map.get(processName+"Response")
		
		assertTrue(resp.indexOf("ErrorMessage>Sending Sync Error.</") >= 0)
		
		verify(mex).setVariable("WorkflowResponse", resp)
	}
			
	@Test
//	@Ignore  
	public void sendSyncError_NullWfe() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initSendSyncError(mex)
		
		when(mex.getVariable("WorkflowException")).thenReturn(null)
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		DeleteVcpeResCustService.sendSyncError(mex)

		verify(mex, times(2)).getVariable(DBGFLAG)
		
		verify(mex).setVariable(processName+"WorkflowResponseSent", "true")
		
		assertEquals("500", map.get(processName+"ResponseCode"))
		assertEquals("Fail", map.get(processName+"Status"))
		
		def resp = map.get(processName+"Response")
		
		assertTrue(resp.indexOf("ErrorMessage>Sending Sync Error.</") >= 0)
		
		verify(mex).setVariable("WorkflowResponse", resp)
	}
			
	@Test
//	@Ignore  
	public void sendSyncError_Ex() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initSendSyncError(mex)
		
		when(mex.getVariable("WorkflowException")).thenThrow(new RuntimeException("expected exception"))
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		
		DeleteVcpeResCustService.sendSyncError(mex)
		
		assertFalse(map.containsKey(processName+"ResponseCode"))
	}
	
	private initSendSyncError(ExecutionEntity mex) {
		WorkflowException wfe = mock(WorkflowException.class)
		
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("mso-request-id")).thenReturn("mri")
		when(mex.getVariable("WorkflowException")).thenReturn(wfe)
		
		when(wfe.getErrorMessage()).thenReturn("mymsg")
	}
	
	
	// ***** processJavaException *****
			
	@Test
//	@Ignore  
	public void processJavaException() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initProcessJavaException(mex)
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> DeleteVcpeResCustService.processJavaException(mex) }))

		verify(mex, times(2)).getVariable(DBGFLAG)
		
		verify(mex).setVariable("prefix", Prefix)
		
		def wfe = map.get("WorkflowException")
		
		assertEquals("Caught a Java Lang Exception", wfe.getErrorMessage())
	}
			
	@Test
//	@Ignore  
	public void processJavaException_BpmnError() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initProcessJavaException(mex)
		
		when(mex.getVariables()).thenThrow(new BpmnError("expected exception"))
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> DeleteVcpeResCustService.processJavaException(mex) }))
		
		assertFalse(map.containsKey("WorkflowException"))
	}
			
	@Test
//	@Ignore  
	public void processJavaException_Ex() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initProcessJavaException(mex)
		
		when(mex.getVariables()).thenThrow(new RuntimeException("expected exception"))
		
		DeleteVcpeResCustService DeleteVcpeResCustService = new DeleteVcpeResCustService()
		
		assertTrue(doBpmnError( { _ -> DeleteVcpeResCustService.processJavaException(mex) }))
		
		def wfe = map.get("WorkflowException")
		
		assertEquals("Exception in processJavaException method", wfe.getErrorMessage())
	}
	
	private initProcessJavaException(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
	}
	
	private void myMockGetAr(String url, int status, String fileResp) {
		stubFor(get(urlMatching(url))
				.willReturn(aResponse()
						.withStatus(status)
						.withHeader("Content-Type", "text/xml")
						.withBodyFile("VCPE/DeleteVcpeResCustService/" + fileResp)))
	}
}
