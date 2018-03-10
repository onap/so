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
import static com.github.tomakehurst.wiremock.client.WireMock.delete
import static com.github.tomakehurst.wiremock.client.WireMock.get
import static com.github.tomakehurst.wiremock.client.WireMock.patch
import static com.github.tomakehurst.wiremock.client.WireMock.put
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching
import static org.junit.Assert.*
import static org.mockito.Mockito.*
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockDeleteAllottedResource
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetAllottedResource
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockPatchAllottedResource
import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockQueryAllottedResourceById

import java.util.Map

import org.openecomp.mso.bpmn.core.RollbackData
import org.openecomp.mso.bpmn.vcpe.scripts.MapSetter

import com.github.tomakehurst.wiremock.junit.WireMockRule

class DoDeleteAllottedResourceTXCTest extends GroovyTestBase {
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(PORT)

	String Prefix = "DDARTXC_"

	@BeforeClass
	public static void setUpBeforeClass() {
		// nothing for now
	}
	  
    @Before
	public void init()
	{
		MockitoAnnotations.initMocks(this)
	}
	
	public DoDeleteAllottedResourceTXCTest() {
		super("DoDeleteAllottedResourceTXC")
	}
	
	
	// ***** preProcessRequest *****
			
	@Test
//	@Ignore  
	public void preProcessRequest() {
		ExecutionEntity mex = setupMock()
		initPreProcess(mex)
		
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		DoDeleteAllottedResourceTXC.preProcessRequest(mex)

		verify(mex).getVariable(DBGFLAG)
		verify(mex).setVariable("prefix", Prefix)
		verify(mex).setVariable("sdncCallbackUrl", "sdncurn")
				
		assertTrue(checkMissingPreProcessRequest("URN_mso_workflow_sdncadapter_callback"))
		assertTrue(checkMissingPreProcessRequest("serviceInstanceId"))
		assertTrue(checkMissingPreProcessRequest("allottedResourceId"))
	}
			
	@Test
//	@Ignore  
	public void preProcessRequest_BpmnError() {
		ExecutionEntity mex = setupMock()
		initPreProcess(mex)
		
		when(mex.getVariable("serviceInstanceId")).thenThrow(new BpmnError("expected exception"))
		
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		assertTrue(doBpmnError( { _ -> DoDeleteAllottedResourceTXC.preProcessRequest(mex) }))
	}
			
	@Test
//	@Ignore  
	public void preProcessRequest_Ex() {
		ExecutionEntity mex = setupMock()
		initPreProcess(mex)
		
		when(mex.getVariable("serviceInstanceId")).thenThrow(new RuntimeException("expected exception"))
		
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		assertTrue(doBpmnError( { _ -> DoDeleteAllottedResourceTXC.preProcessRequest(mex) }))
	}
	
	
	// ***** getAaiAR *****
	
	@Test
//	@Ignore
	public void getAaiAR() {
		MockQueryAllottedResourceById(ARID, "GenericFlows/getARUrlById.xml")
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoDeleteAllottedResourceTXC/arGetById.xml")
		
		ExecutionEntity mex = setupMock()
		initGetAaiAR(mex)
		
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		DoDeleteAllottedResourceTXC.getAaiAR(mex)
		
		verify(mex).setVariable("parentServiceInstanceId", INST)
	}
	
	@Test
//	@Ignore
	public void getAaiAR_EmptyResponse() {
		
		// note: empty result-link
		wireMockRule
			.stubFor(get(urlMatching("/aai/.*/search/.*"))
					.willReturn(aResponse()
						.withStatus(200)
						.withHeader("Content-Type", "text/xml")
						.withBody("<result-data></result-data>")))
			
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoDeleteAllottedResourceTXC/arGetById.xml")
		
		ExecutionEntity mex = setupMock()
		initGetAaiAR(mex)
		
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		assertTrue(doBpmnError( { _ -> DoDeleteAllottedResourceTXC.getAaiAR(mex) }))
	}
	
	
	// ***** updateAaiAROrchStatus *****
	
	@Test
//	@Ignore
	public void updateAaiAROrchStatus() {
		ExecutionEntity mex = setupMock()
		initUpdateAaiAROrchStatus(mex)
		
		MockPatchAllottedResource(CUST, SVC, INST, ARID)
					
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		DoDeleteAllottedResourceTXC.updateAaiAROrchStatus(mex, "success")
	}
	
	
	// ***** buildSDNCRequest *****
	
	@Test
//	@Ignore
	public void buildSDNCRequest() {
		ExecutionEntity mex = setupMock()
		initBuildSDNCRequest(mex)
		
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		String result = DoDeleteAllottedResourceTXC.buildSDNCRequest(mex, "myact", "myreq")
		
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
//	@Ignore
	public void buildSDNCRequest_Ex() {
		ExecutionEntity mex = setupMock()
		initBuildSDNCRequest(mex)
		
		when(mex.getVariable("allottedResourceId")).thenThrow(new RuntimeException("expected exception"))
		
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceTXC.buildSDNCRequest(mex, "myact", "myreq") }))
	}
	
	
	// ***** preProcessSDNCUnassign *****
	
	@Test
//	@Ignore
	public void preProcessSDNCUnassign() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPreProcessSDNC(mex)
					
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		DoDeleteAllottedResourceTXC.preProcessSDNCUnassign(mex)
		
		def req = map.get("sdncUnassignRequest")
		
		assertTrue(req.indexOf("<sdncadapter:SvcAction>unassign</") >= 0)
		assertTrue(req.indexOf("<request-action>DeleteTunnelXConnInstance</") >= 0)
		assertTrue(req.indexOf("<sdncadapter:RequestId>") >= 0)
	}
	
	@Test
//	@Ignore
	public void preProcessSDNCUnassign_BpmnError() {
		ExecutionEntity mex = setupMock()
		initPreProcessSDNC(mex)
		
		when(mex.getVariable("allottedResourceId")).thenThrow(new BpmnError("expected exception"))
					
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceTXC.preProcessSDNCUnassign(mex) }))
	}
	
	@Test
//	@Ignore
	public void preProcessSDNCUnassign_Ex() {
		ExecutionEntity mex = setupMock()
		initPreProcessSDNC(mex)
		
		when(mex.getVariable("allottedResourceId")).thenThrow(new RuntimeException("expected exception"))
					
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceTXC.preProcessSDNCUnassign(mex) }))
	}
	
	
	// ***** preProcessSDNCDelete *****
	
	@Test
//	@Ignore
	public void preProcessSDNCDelete() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPreProcessSDNC(mex)
					
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		DoDeleteAllottedResourceTXC.preProcessSDNCDelete(mex)
		
		def req = map.get("sdncDeleteRequest")
		
		assertTrue(req.indexOf("<sdncadapter:SvcAction>delete</") >= 0)
		assertTrue(req.indexOf("<request-action>DeleteTunnelXConnInstance</") >= 0)
		assertTrue(req.indexOf("<sdncadapter:RequestId>") >= 0)
	}
	
	@Test
//	@Ignore
	public void preProcessSDNCDelete_BpmnError() {
		ExecutionEntity mex = setupMock()
		initPreProcessSDNC(mex)
		
		when(mex.getVariable("allottedResourceId")).thenThrow(new BpmnError("expected exception"))
					
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceTXC.preProcessSDNCDelete(mex) }))
	}
	
	@Test
//	@Ignore
	public void preProcessSDNCDelete_Ex() {
		ExecutionEntity mex = setupMock()
		initPreProcessSDNC(mex)
		
		when(mex.getVariable("allottedResourceId")).thenThrow(new RuntimeException("expected exception"))
					
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceTXC.preProcessSDNCDelete(mex) }))
	}
	
	
	// ***** preProcessSDNCDeactivate *****
	
	@Test
//	@Ignore
	public void preProcessSDNCDeactivate() {
		ExecutionEntity mex = setupMock()
		def map = setupMap(mex)
		initPreProcessSDNC(mex)
					
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		DoDeleteAllottedResourceTXC.preProcessSDNCDeactivate(mex)
		
		def req = map.get("sdncDeactivateRequest")
		
		assertTrue(req.indexOf("<sdncadapter:SvcAction>deactivate</") >= 0)
		assertTrue(req.indexOf("<request-action>DeleteTunnelXConnInstance</") >= 0)
		assertTrue(req.indexOf("<sdncadapter:RequestId>") >= 0)
	}
	
	@Test
//	@Ignore
	public void preProcessSDNCDeactivate_BpmnError() {
		ExecutionEntity mex = setupMock()
		initPreProcessSDNC(mex)
		
		when(mex.getVariable("allottedResourceId")).thenThrow(new BpmnError("expected exception"))
					
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceTXC.preProcessSDNCDeactivate(mex) }))
	}
	
	@Test
//	@Ignore
	public void preProcessSDNCDeactivate_Ex() {
		ExecutionEntity mex = setupMock()
		initPreProcessSDNC(mex)
		
		when(mex.getVariable("allottedResourceId")).thenThrow(new RuntimeException("expected exception"))
					
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceTXC.preProcessSDNCDeactivate(mex) }))
	}
	
	
	// ***** validateSDNCResp *****
	
	@Test
//	@Ignore
	public void validateSDNCResp() {
		ExecutionEntity mex = setupMock()
		def data = initValidateSDNCResp(mex)
		def resp = initValidateSDNCResp_Resp(200)
		
		when(mex.getVariable(Prefix+"sdncResponseSuccess")).thenReturn(true)
		
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		DoDeleteAllottedResourceTXC.validateSDNCResp(mex, resp, "create")
		
		verify(mex).getVariable("WorkflowException")
		verify(mex).getVariable("SDNCA_SuccessIndicator")		
		verify(mex).getVariable(Prefix+"sdncResponseSuccess")
		
		verify(mex, never()).getVariable(Prefix + "sdncRequestDataResponseCode")
		verify(mex, never()).setVariable("wasDeleted", false)
	}
	
	@Test
//	@Ignore
	public void validateSDNCResp_Fail404_Deactivate_FailNotFound() {
		ExecutionEntity mex = setupMock()
		def data = initValidateSDNCResp(mex)
		
		def resp = initValidateSDNCResp_Resp(404)
		when(mex.getVariable(Prefix+"sdncRequestDataResponseCode")).thenReturn("404")
		when(mex.getVariable("failNotFound")).thenReturn("true")
		
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		assertTrue(doBpmnError( { _ -> DoDeleteAllottedResourceTXC.validateSDNCResp(mex, resp, "deactivate")}))
	}
	
	@Test
//	@Ignore
	public void validateSDNCResp_Fail404_Deactivate() {
		ExecutionEntity mex = setupMock()
		def data = initValidateSDNCResp(mex)
		
		def resp = initValidateSDNCResp_Resp(404)
		when(mex.getVariable(Prefix+"sdncRequestDataResponseCode")).thenReturn("404")
		
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		DoDeleteAllottedResourceTXC.validateSDNCResp(mex, resp, "deactivate")
		
		verify(mex).setVariable("ARNotFoundInSDNC", true)
		verify(mex).setVariable("wasDeleted", false)
	}
	
	@Test
//	@Ignore
	public void validateSDNCResp_Fail404() {
		ExecutionEntity mex = setupMock()
		def data = initValidateSDNCResp(mex)
		
		def resp = initValidateSDNCResp_Resp(404)
		when(mex.getVariable(Prefix+"sdncRequestDataResponseCode")).thenReturn("404")
		
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		assertTrue(doBpmnError( { _ -> DoDeleteAllottedResourceTXC.validateSDNCResp(mex, resp, "create")}))
	}
	
	@Test
//	@Ignore
	public void validateSDNCResp_Deactivate() {
		ExecutionEntity mex = setupMock()
		def data = initValidateSDNCResp(mex)
		def resp = initValidateSDNCResp_Resp(200)
		
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		assertTrue(doBpmnError( { _ -> DoDeleteAllottedResourceTXC.validateSDNCResp(mex, resp, "deactivate")}))
	}
	
	@Test
//	@Ignore
	public void validateSDNCResp_BpmnError() {
		ExecutionEntity mex = setupMock()
		initValidateSDNCResp(mex)
		def resp = initValidateSDNCResp_Resp(200)
		
		when(mex.getVariable("WorkflowException")).thenThrow(new BpmnError("expected exception"))
		
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceTXC.validateSDNCResp(mex, resp, "create") }))
	}
	
	@Test
//	@Ignore
	public void validateSDNCResp_Ex() {
		ExecutionEntity mex = setupMock()
		initValidateSDNCResp(mex)
		def resp = initValidateSDNCResp_Resp(200)
		
		when(mex.getVariable("WorkflowException")).thenThrow(new RuntimeException("expected exception"))
		
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		assertTrue(doBpmnError({ _ -> DoDeleteAllottedResourceTXC.validateSDNCResp(mex, resp, "create") }))
	}
			
	@Test
//	@Ignore  
	public void deleteAaiAR() {
		ExecutionEntity mex = setupMock()
		initDeleteAaiAR(mex)
		
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoDeleteAllottedResourceTXC/arGetById.xml")
		MockDeleteAllottedResource(CUST, SVC, INST, ARID, VERS)
		
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		DoDeleteAllottedResourceTXC.deleteAaiAR(mex)
	}
			
	@Test
//	@Ignore  
	public void deleteAaiAR_NoArPath() {
		ExecutionEntity mex = setupMock()
		initDeleteAaiAR(mex)
		
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoDeleteAllottedResourceTXC/arGetById.xml")
		MockDeleteAllottedResource(CUST, SVC, INST, ARID, VERS)
			
		when(mex.getVariable("aaiARPath")).thenReturn("")
		
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		assertTrue(doBpmnError( { _ -> DoDeleteAllottedResourceTXC.deleteAaiAR(mex) }))
	}
			
	@Test
//	@Ignore  
	public void deleteAaiAR_BpmnError() {
		ExecutionEntity mex = setupMock()
		initDeleteAaiAR(mex)
		
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoDeleteAllottedResourceTXC/arGetById.xml")
		MockDeleteAllottedResource(CUST, SVC, INST, ARID, VERS)
			
		when(mex.getVariable("aaiARPath")).thenThrow(new BpmnError("expected exception"))
		
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		assertTrue(doBpmnError( { _ -> DoDeleteAllottedResourceTXC.deleteAaiAR(mex) }))
	}
			
	@Test
//	@Ignore  
	public void deleteAaiAR_Ex() {
		ExecutionEntity mex = setupMock()
		initDeleteAaiAR(mex)
		
		MockGetAllottedResource(CUST, SVC, INST, ARID, "VCPE/DoDeleteAllottedResourceTXC/arGetById.xml")
		MockDeleteAllottedResource(CUST, SVC, INST, ARID, VERS)
			
		when(mex.getVariable("aaiARPath")).thenThrow(new RuntimeException("expected exception"))
		
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		assertTrue(doBpmnError( { _ -> DoDeleteAllottedResourceTXC.deleteAaiAR(mex) }))
	}
	
	private boolean checkMissingPreProcessRequest(String fieldnm) {
		ExecutionEntity mex = setupMock()
		initPreProcess(mex)
								
		DoDeleteAllottedResourceTXC DoDeleteAllottedResourceTXC = new DoDeleteAllottedResourceTXC()
		
		when(mex.getVariable(fieldnm)).thenReturn("")
		
		return doBpmnError( { _ -> DoDeleteAllottedResourceTXC.preProcessRequest(mex) })
	}
	
	private void initPreProcess(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("URN_mso_workflow_sdncadapter_callback")).thenReturn("sdncurn")
		when(mex.getVariable("serviceInstanceId")).thenReturn("sii")
		when(mex.getVariable("allottedResourceId")).thenReturn("ari")
	}
	
	private void initGetAaiAR(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("allottedResourceType")).thenReturn("TXC")
		when(mex.getVariable("allottedResourceRole")).thenReturn("TXC")
		when(mex.getVariable("allottedResourceId")).thenReturn(ARID)
		when(mex.getVariable("CSI_service")).thenReturn(FileUtil.readResourceFile("__files/VCPE/DoDeleteAllottedResourceTXC/getAR.xml"))
		when(mex.getVariable("URN_mso_workflow_global_default_aai_namespace")).thenReturn(urnProps.get("mso.workflow.global.default.aai.namespace"))
		when(mex.getVariable("URN_mso_workflow_global_default_aai_version")).thenReturn(urnProps.get("mso.workflow.global.default.aai.version"))
		when(mex.getVariable("URN_mso_workflow_default_aai_v8_nodes_query_uri")).thenReturn(urnProps.get("mso.workflow.default.aai.v8.nodes-query.uri"))
		when(mex.getVariable("URN_aai_endpoint")).thenReturn(aaiUriPfx)
		when(mex.getVariable("aaiARPath")).thenReturn(aaiUriPfx + "/aai/v9/business/customers/customer/"+CUST+"/service-subscriptions/service-subscription/"+SVC+"/service-instances/service-instance/"+INST+"/allotted-resources/allotted-resource/"+ARID)
		when(mex.getVariable("aaiAROrchStatus")).thenReturn("Active")
	}
	
	private initUpdateAaiAROrchStatus(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("aaiARPath")).thenReturn(aaiUriPfx + "/aai/v9/business/customers/customer/"+CUST+"/service-subscriptions/service-subscription/"+SVC+"/service-instances/service-instance/"+INST+"/allotted-resources/allotted-resource/"+ARID)
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
	
	private initPreProcessSDNC(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
	}
	
	private initValidateSDNCResp(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("prefix")).thenReturn(Prefix)
		when(mex.getVariable("SDNCA_SuccessIndicator")).thenReturn(true)
	}
	
	private String initValidateSDNCResp_Resp(int code) {
		return "<response-data>&lt;response-code&gt;${code}&lt;/response-code&gt;</response-data>"
	}
	
	private initDeleteAaiAR(ExecutionEntity mex) {
		when(mex.getVariable(DBGFLAG)).thenReturn("true")
		when(mex.getVariable("aaiARPath")).thenReturn(aaiUriPfx + "/aai/v9/business/customers/customer/"+CUST+"/service-subscriptions/service-subscription/"+SVC+"/service-instances/service-instance/"+INST+"/allotted-resources/allotted-resource/"+ARID)
		when(mex.getVariable("aaiARResourceVersion")).thenReturn("myvers")
		when(mex.getVariable("URN_aai_endpoint")).thenReturn(aaiUriPfx)
	}
		
}
