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

package org.openecomp.mso.bpmn.common;

import static org.openecomp.mso.bpmn.mock.StubResponseAAI.MockGetVolumeGroupById;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit test cases for ConfirmVolumeGroupName.bpmn
 */
public class ConfirmVolumeGroupNameTest extends WorkflowTest {
	/**
	 * Sunny day scenario.
	 * 
	 * @throws Exception
	 */
	@Test
	@Deployment(resources = {
		"subprocess/ConfirmVolumeGroupName.bpmn"
		})
	public void sunnyDay() throws Exception {
				
		logStart();
		MockGetVolumeGroupById("MDTWNJ21", "VOLUME_GROUP_ID_1", "aai-volume-group-id-info.xml");
		
		System.out.println("Before starting process");
		
		RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("ConfirmVolumeGroupName_volumeGroupId", "VOLUME_GROUP_ID_1");
		variables.put("ConfirmVolumeGroupName_volumeGroupName", "VOLUME_GROUP_ID_1_NAME");
		variables.put("ConfirmVolumeGroupName_aicCloudRegion", "MDTWNJ21");
		System.out.println("after setting variables");
		runtimeService.startProcessInstanceByKey("ConfirmVolumeGroupName", variables);
		String response = BPMNUtil.getVariable(processEngineRule, "ConfirmVolumeGroupName", "CVGN_queryVolumeGroupResponse");
		String responseCode = BPMNUtil.getVariable(processEngineRule, "ConfirmVolumeGroupName", "CVGN_queryVolumeGroupResponseCode");
					
		assertEquals("200", responseCode);
		System.out.println(response);
		logEnd();
	}
	
	/**
	 * Rainy day scenario - nonexisting volume group id.
	 * 
	 * @throws Exception
	 */
	@Test
	@Deployment(resources = {
		"subprocess/ConfirmVolumeGroupName.bpmn"
		})
	public void rainyDayNoVolumeGroupId() throws Exception {
				
		logStart();
		MockGetVolumeGroupById("MDTWNJ21", "VOLUME_GROUP_ID_1", "aai-volume-group-id-info.xml");
		
		System.out.println("Before starting process");
		
		RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("ConfirmVolumeGroupName_volumeGroupId", "VOLUME_GROUP_ID_THAT_DOES_NOT_EXIST");
		variables.put("ConfirmVolumeGroupName_volumeGroupName", "cee6d136-e378-4678-a024-2cd15f0ee0cg");
		System.out.println("after setting variables");
		runtimeService.startProcessInstanceByKey("ConfirmVolumeGroupName", variables);
		String response = BPMNUtil.getVariable(processEngineRule, "ConfirmVolumeGroupName", "CVGN_queryVolumeGroupResponse");
		String responseCode = BPMNUtil.getVariable(processEngineRule, "ConfirmVolumeGroupName", "CVGN_queryVolumeGroupResponseCode");
					
		assertEquals("404", responseCode);
		System.out.println(response);
		
		logEnd();
	}
	
	/**
	 * Rainy day scenario - volume group name does not match the name in AAI
	 *
	 * 
	 * @throws Exception
	 */
	@Test
	@Deployment(resources = {
		"subprocess/ConfirmVolumeGroupName.bpmn"
		})
	public void rainyDayNameDoesNotMatch() throws Exception {
				
		logStart();
		MockGetVolumeGroupById("MDTWNJ21", "VOLUME_GROUP_ID_1", "aai-volume-group-id-info.xml");
		
		System.out.println("Before starting process");
		
		RuntimeService runtimeService = processEngineRule.getRuntimeService();				
		Map<String, Object> variables = new HashMap<>();
		variables.put("isDebugLogEnabled","true");
		variables.put("ConfirmVolumeGroupName_volumeGroupId", "VOLUME_GROUP_ID_1");
		variables.put("ConfirmVolumeGroupName_volumeGroupName", "BAD_VOLUME_GROUP_NAME");
		System.out.println("after setting variables");
		runtimeService.startProcessInstanceByKey("ConfirmVolumeGroupName", variables);
		String response = BPMNUtil.getVariable(processEngineRule, "ConfirmVolumeGroupName", "CVGN_queryVolumeGroupResponse");
		String responseCode = BPMNUtil.getVariable(processEngineRule, "ConfirmVolumeGroupName", "CVGN_queryVolumeGroupResponseCode");
					
		assertEquals("404", responseCode);
		System.out.println(response);
		
		logEnd();
	}
}
