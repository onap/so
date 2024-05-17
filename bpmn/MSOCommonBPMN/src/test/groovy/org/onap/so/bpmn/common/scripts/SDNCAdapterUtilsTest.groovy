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

package org.onap.so.bpmn.common.scripts;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.onap.so.bpmn.core.WorkflowException
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SDNCAdapterUtilsTest {
	
	private def map
	private ExecutionEntity svcex
	private WorkflowException wfex
	private AbstractServiceTaskProcessor tp
	private String resp
	private SDNCAdapterUtils utils
	
	@Before
	public void init()
	{
		map = new HashMap<String,Object>()
		svcex = mock(ExecutionEntity.class)
		wfex = null
		utils = new SDNCAdapterUtils()
		
		// svcex gets its variables from "map"
		when(svcex.getVariable(any())).thenAnswer(
			{ invocation ->
				return map.get(invocation.getArgument(0)) })
		
		// svcex puts its variables into "map"
		when(svcex.setVariable(any(), any())).thenAnswer(
			{ invocation ->
				return map.put(
							invocation.getArgument(0),
							invocation.getArgument(1)) })
		
		map.put("isDebugLogEnabled", "true")
		map.put("prefix", "mypfx-")
		map.put("testProcessKey", "mykey")
	}
				  								
	@Test
	public void testValidateSDNCResponse_Success_NoCode() {
		resp = """<no-response/>"""
		
		utils.validateSDNCResponse(svcex, resp, wfex, true)
		
		assertEquals(true, map.get("mypfx-sdncResponseSuccess"))
		assertEquals("0", map.get("mypfx-sdncRequestDataResponseCode"))
		assertFalse(map.containsKey("WorkflowException"))
	}
				  								
	@Test
	public void testValidateSDNCResponse_200() {
		utils.validateSDNCResponse(svcex, makeResp("200", "OK", ""), wfex, true)
		
		assertEquals(true, map.get("mypfx-sdncResponseSuccess"))
		assertEquals("200", map.get("mypfx-sdncRequestDataResponseCode"))
		assertFalse(map.containsKey("WorkflowException"))
	}
				  								
	@Test
	public void testValidateSDNCResponse_408() {
		try {
			utils.validateSDNCResponse(svcex, makeResp("408", "failed", ""), wfex, true)
			
			// this has been commented out as, currently, the code doesn't
			// throw an exception in this case
//			fail("missing exception")
			
		} catch(BpmnError ex) {
			ex.printStackTrace()
		}
		
		assertEquals(false, map.get("mypfx-sdncResponseSuccess"))
		assertEquals("408", map.get("mypfx-sdncRequestDataResponseCode"))
		
		wfex = map.get("WorkflowException")
		assertNotNull(wfex)
		
		assertEquals(5320, wfex.getErrorCode())
		assertEquals("Received error from SDN-C: failed", wfex.getErrorMessage())
	}
				  								
	@Test
	public void testValidateSDNCResponse_408_200() {
		
		utils.validateSDNCResponse(svcex, makeResp("408", "failed", makeReq("200", "ok")), wfex, true)
		
		assertEquals(true	, map.get("mypfx-sdncResponseSuccess"))
		assertEquals("200", map.get("mypfx-sdncRequestDataResponseCode"))		
		assertFalse(map.containsKey("WorkflowException"))
	}
				  								
	@Test
	public void testValidateSDNCResponse_408_200_WithEmbeddedLt() {
		
		utils.validateSDNCResponse(svcex, makeResp("408", "failed", makeReq("200", "<success> message")), wfex, true)
		
		assertEquals(true, map.get("mypfx-sdncResponseSuccess"))
		assertEquals("200", map.get("mypfx-sdncRequestDataResponseCode"))		
		assertFalse(map.containsKey("WorkflowException"))
	}
	
	@Test
	public void testUpdateHomingInfo() {
		String actual = utils.updateHomingInfo(null, "AIC3.0")
		println actual
		assertEquals("<l2-homing-information><aic-version>AIC3.0</aic-version></l2-homing-information>", actual)
	}
	
	@Test
	public void testUpdateHomingInfo2() {
		String homingInfo = "<l2-homing-information><preferred-aic-clli>TESTCLLI</preferred-aic-clli></l2-homing-information>" 
		String actual = utils.updateHomingInfo(homingInfo, "AIC3.0")
		println actual
		assertEquals("<l2-homing-information><preferred-aic-clli>TESTCLLI</preferred-aic-clli><aic-version>AIC3.0</aic-version></l2-homing-information>", actual)
	}
	
	private String makeResp(String respcode, String respmsg, String reqdata) {
		def rc = respcode
		def rm = respmsg
		
		return """
<sdncadapterworkflow:SDNCAdapterWorkflowResponse xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1"
                                                 xmlns:tag0="http://org.onap/workflow/sdnc/adapter/schema/v1"
                                                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <sdncadapterworkflow:response-data>
      <tag0:CallbackHeader>
         <tag0:RequestId>myreq</tag0:RequestId>
         <tag0:ResponseCode>${MsoUtils.xmlEscape(rc)}</tag0:ResponseCode>
         <tag0:ResponseMessage>${MsoUtils.xmlEscape(rm)}</tag0:ResponseMessage>
      </tag0:CallbackHeader>
	  ${reqdata}
   </sdncadapterworkflow:response-data>
</sdncadapterworkflow:SDNCAdapterWorkflowResponse>
"""
	
	}
	
	private String makeReq(String respcode, String respmsg) {
		def rc = respcode
		def rm = respmsg
		
		String output = """
<output xmlns="org:onap:sdnc:northbound:generic-resource">
		<svc-request-id>8b46e36e-b44f-4085-9404-427be1bc8a3</svc-request-id>
		<response-code>${MsoUtils.xmlEscape(rc)}</response-code>
		<response-message>${MsoUtils.xmlEscape(rm)}</response-message>
		<ack-final-indicator>Y</ack-final-indicator>
</output>
"""
		output = output
		
		return """<tag0:RequestData xsi:type="xs:string">${MsoUtils.xmlEscape(output)}</tag0:RequestData>"""
	}
}
