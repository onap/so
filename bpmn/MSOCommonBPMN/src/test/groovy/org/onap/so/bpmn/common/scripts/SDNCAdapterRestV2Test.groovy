/*- 
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

package org.onap.so.bpmn.common.scripts

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.onap.so.bpmn.mock.FileUtil
import org.onap.so.bpmn.core.json.JsonUtils

import static org.mockito.Mockito.*

@RunWith(MockitoJUnitRunner.class)
public class SDNCAdapterRestV2Test {
	
	@Before
	public void init()
	{
		MockitoAnnotations.initMocks(this)
	}

	@Test
	public void testPreProcessRequest() {
		// bpTimeout is empty and "mso.adapters.sdnc.timeout" is defined

		String sdncAdapterWorkflowRequest = FileUtil.readResourceFile("__files/SDN-ETHERNET-INTERNET/SDNCAdapterRestV1/sdnc_request.json");
		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("prefix")).thenReturn("SDNCREST_")
		when(mockExecution.getVariable("mso-request-id")).thenReturn("testMsoRequestId")
		when(mockExecution.getVariable("SDNCREST_Request")).thenReturn(sdncAdapterWorkflowRequest)
		when(mockExecution.getVariable("mso.adapters.po.auth")).thenReturn("5E12ACACBD552A415E081E29F2C4772F9835792A51C766CCFDD7433DB5220B59969CB2798C")
		when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

		when(mockExecution.getVariable("mso.adapters.sdnc.rest.endpoint")).thenReturn("http://localhost:18080/adapters/rest/v1/sdnc/")

		when(mockExecution.getVariable("mso.adapters.sdnc.timeout")).thenReturn("PT5M")

		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
		when(mockExecution.getVariable("testProcessKey")).thenReturn("testProcessKey")


		SDNCAdapterRestV2 sdncAdapterRestV2 = new SDNCAdapterRestV2()
		sdncAdapterRestV2.preProcessRequest(mockExecution)
		


		verify(mockExecution).setVariable("prefix","SDNCREST_")

		verify(mockExecution).setVariable("SDNCREST_SuccessIndicator",false)
		verify(mockExecution).setVariable("SDNCREST_requestType","SDNCServiceRequest")
		verify(mockExecution).setVariable("SDNCAResponse_CORRELATOR","0d883b7f-dd34-4e1b-9ed5-341d33052360-1511808197479")
		verify(mockExecution).setVariable("SDNCREST_sdncAdapterMethod","POST")
		verify(mockExecution).setVariable("SDNCREST_timeout","PT5M")
	}
	
	@Test
	public void testPreProcessRequestGoodTimeout() {
		// bpTimeout is valid and "mso.adapters.sdnc.timeout" is undefined
		
		String sdncAdapterWorkflowRequest = FileUtil.readResourceFile("__files/SDN-ETHERNET-INTERNET/SDNCAdapterRestV1/sdnc_request.json");
		sdncAdapterWorkflowRequest = JsonUtils.addJsonValue(sdncAdapterWorkflowRequest, "SDNCServiceRequest.bpTimeout", "PT20S")
		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("prefix")).thenReturn("SDNCREST_")
		when(mockExecution.getVariable("mso-request-id")).thenReturn("testMsoRequestId")
		when(mockExecution.getVariable("SDNCREST_Request")).thenReturn(sdncAdapterWorkflowRequest)
		when(mockExecution.getVariable("mso.adapters.po.auth")).thenReturn("5E12ACACBD552A415E081E29F2C4772F9835792A51C766CCFDD7433DB5220B59969CB2798C")
		when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

		when(mockExecution.getVariable("mso.adapters.sdnc.rest.endpoint")).thenReturn("http://localhost:18080/adapters/rest/v1/sdnc/")

		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
		when(mockExecution.getVariable("testProcessKey")).thenReturn("testProcessKey")


		SDNCAdapterRestV2 sdncAdapterRestV2 = new SDNCAdapterRestV2()
		sdncAdapterRestV2.preProcessRequest(mockExecution)
		


		verify(mockExecution).setVariable("prefix","SDNCREST_")

		verify(mockExecution).setVariable("SDNCREST_SuccessIndicator",false)
		verify(mockExecution).setVariable("SDNCREST_requestType","SDNCServiceRequest")
		verify(mockExecution).setVariable("SDNCAResponse_CORRELATOR","0d883b7f-dd34-4e1b-9ed5-341d33052360-1511808197479")
		verify(mockExecution).setVariable("SDNCREST_sdncAdapterMethod","POST")
		verify(mockExecution).setVariable("SDNCREST_timeout","PT20S")
	}

	@Ignore
	@Test
	public void testPreProcessRequestBadTimeout() {
		// bpTimeout is invalid and "mso.adapters.sdnc.timeout" is undefined

		String sdncAdapterWorkflowRequest = FileUtil.readResourceFile("__files/SDN-ETHERNET-INTERNET/SDNCAdapterRestV1/sdnc_request.json");
		sdncAdapterWorkflowRequest = JsonUtils.addJsonValue(sdncAdapterWorkflowRequest, "SDNCServiceRequest.bpTimeout", "badTimeout")
		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("prefix")).thenReturn("SDNCREST_")
		when(mockExecution.getVariable("mso-request-id")).thenReturn("testMsoRequestId")
		when(mockExecution.getVariable("SDNCREST_Request")).thenReturn(sdncAdapterWorkflowRequest)
		when(mockExecution.getVariable("mso.adapters.po.auth")).thenReturn("3141634BF7E070AA289CF2892C986C0B")
		when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

		when(mockExecution.getVariable("mso.adapters.sdnc.rest.endpoint")).thenReturn("http://localhost:18080/adapters/rest/v1/sdnc/")

		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
		when(mockExecution.getVariable("testProcessKey")).thenReturn("testProcessKey")


		SDNCAdapterRestV2 sdncAdapterRestV2 = new SDNCAdapterRestV2()
		sdncAdapterRestV2.preProcessRequest(mockExecution)
		


		verify(mockExecution).setVariable("prefix","SDNCREST_")

		verify(mockExecution).setVariable("SDNCREST_SuccessIndicator",false)
		verify(mockExecution).setVariable("SDNCREST_requestType","SDNCServiceRequest")
		verify(mockExecution).setVariable("SDNCAResponse_CORRELATOR","0d883b7f-dd34-4e1b-9ed5-341d33052360-1511808197479")
		verify(mockExecution).setVariable("SDNCREST_sdncAdapterMethod","POST")
		verify(mockExecution).setVariable("SDNCREST_timeout","PT10S")
	}
}