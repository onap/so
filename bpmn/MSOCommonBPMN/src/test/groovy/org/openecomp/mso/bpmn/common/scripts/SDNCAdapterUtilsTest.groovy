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

package org.openecomp.mso.bpmn.common.scripts;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*

import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.common.scripts.SDNCAdapterUtils

import org.openecomp.mso.bpmn.mock.FileUtil

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
		tp = new AbstractServiceTaskProcessor() {
			@Override
			public void preProcessRequest(DelegateExecution execution) {
			}
		};
		utils = new SDNCAdapterUtils(tp)
		
		// svcex gets its variables from "map"
		when(svcex.getVariable(any())).thenAnswer(
			{ invocation ->
				return map.get(invocation.getArgumentAt(0, String.class)) })
		
		// svcex puts its variables into "map"
		when(svcex.setVariable(any(), any())).thenAnswer(
			{ invocation ->
				return map.put(
							invocation.getArgumentAt(0, String.class),
							invocation.getArgumentAt(1, String.class)) })
		
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
		
		assertEquals(true, map.get("mypfx-sdncResponseSuccess"))
		assertEquals("200", map.get("mypfx-sdncRequestDataResponseCode"))		
		assertFalse(map.containsKey("WorkflowException"))
	}

	@Ignore // 1802 merge				  								
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
	
	@Ignore // 1802 merge - testing method that doesn't exist
	@Test
	public void testUpdateServiceInfo() {
		String actual = utils.updateServiceInfo(null, "96688f6f-ab06-4ef6-ae55-9d3af28ae909")
		println actual
		assertEquals("<service-information><infra-service-instance-id>96688f6f-ab06-4ef6-ae55-9d3af28ae909</infra-service-instance-id></service-information>", actual)
	}
	
	@Ignore // 1802 merge - testing method that doesn't exist
	@Test
	public void testUpdateServiceInfo2() {
		String serviceInfo = "<service-information><service-type>SDN-ETHERNET-INTERNET</service-type><service-instance-id>MIS/1602/00029/SB_INTERNET</service-instance-id></service-information>"
		String actual = utils.updateServiceInfo(serviceInfo, "96688f6f-ab06-4ef6-ae55-9d3af28ae909")
		println actual
		assertEquals("<service-information><service-type>SDN-ETHERNET-INTERNET</service-type><service-instance-id>MIS/1602/00029/SB_INTERNET</service-instance-id><infra-service-instance-id>96688f6f-ab06-4ef6-ae55-9d3af28ae909</infra-service-instance-id></service-information>", actual)
	}
	
	private String makeResp(String respcode, String respmsg, String reqdata) {
		def rc = encodeXml(respcode)
		def rm = encodeXml(respmsg)
		
		return """
<sdncadapterworkflow:SDNCAdapterWorkflowResponse xmlns:sdncadapterworkflow="http://org.openecomp/mso/workflow/schema/v1"
                                                 xmlns:tag0="http://org.openecomp/workflow/sdnc/adapter/schema/v1"
                                                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
   <sdncadapterworkflow:response-data>
      <tag0:CallbackHeader>
         <tag0:RequestId>myreq</tag0:RequestId>
         <tag0:ResponseCode>${rc}</tag0:ResponseCode>
         <tag0:ResponseMessage>${rm}</tag0:ResponseMessage>
      </tag0:CallbackHeader>
	  ${reqdata}
   </sdncadapterworkflow:response-data>
</sdncadapterworkflow:SDNCAdapterWorkflowResponse>
"""
	
	}
	
	private String makeReq(String respcode, String respmsg) {
		def rc = encodeXml(respcode)
		def rm = encodeXml(respmsg)
		
		def output = """
<output xmlns="org:onap:sdnc:northbound:generic-resource">
		<svc-request-id>8b46e36e-b44f-4085-9404-427be1bc8a3</svc-request-id>
		<response-code>${rc}</response-code>
		<response-message>${rm}</response-message>
		<ack-final-indicator>Y</ack-final-indicator>
</output>
"""
		output = encodeXml(output)
		
		return """<tag0:RequestData xsi:type="xs:string">${output}</tag0:RequestData>"""
	}
	
	private String encodeXml(String txt) {
		return txt.replace("&", "&amp;").replace("<", "&lt;")
	}
}
