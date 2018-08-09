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

package org.onap.so.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.onap.so.bpmn.common.scripts.MsoGroovyTest

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.mockito.Mockito.times
import static org.mockito.Mockito.when


@RunWith(MockitoJUnitRunner.class)
class DoCreateVfModuleVolumeV2Test extends MsoGroovyTest {

	@Captor
	static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

	def String volumeRollbackRequest = """
<rollbackVolumeGroupRequest>
   <volumeGroupRollback>
      <volumeGroupId>171907d6-cdf0-4e08-953d-81ee104005a7</volumeGroupId>
      <volumeGroupStackId>{{VOLUMEGROUPSTACKID}}</volumeGroupStackId>
      <tenantId>c2141e3fcae940fcb4797ec9115e5a7a</tenantId>
      <cloudSiteId>mtwnj1a</cloudSiteId>
      <volumeGroupCreated>true</volumeGroupCreated>
      <msoRequest>
         <requestId>230fd6ac-2a39-4be4-9b1e-7b7e1cc039b5</requestId>
         <serviceInstanceId>88c871d6-be09-4982-8490-96b1d243fb34</serviceInstanceId>
      </msoRequest>
      <messageId>9a5a91e8-3b79-463c-81c3-874a78f5b567</messageId>
   </volumeGroupRollback>
   <skipAAI>true</skipAAI>
   <notificationUrl>http://localhost:8080/mso/WorkflowMessage/VNFAResponse/9a5a91e8-3b79-463c-81c3-874a78f5b567</notificationUrl>
</rollbackVolumeGroupRequest>
	"""
	
	def String volumeRollbackRequestWithStackId = """
<rollbackVolumeGroupRequest>
   <volumeGroupRollback>
      <volumeGroupId>171907d6-cdf0-4e08-953d-81ee104005a7</volumeGroupId>
      <volumeGroupStackId>mdt22avrr_volume01/0f1aaae8-efe3-45ce-83e1-bfad01db58d8</volumeGroupStackId>
      <tenantId>c2141e3fcae940fcb4797ec9115e5a7a</tenantId>
      <cloudSiteId>mtwnj1a</cloudSiteId>
      <volumeGroupCreated>true</volumeGroupCreated>
      <msoRequest>
         <requestId>230fd6ac-2a39-4be4-9b1e-7b7e1cc039b5</requestId>
         <serviceInstanceId>88c871d6-be09-4982-8490-96b1d243fb34</serviceInstanceId>
      </msoRequest>
      <messageId>9a5a91e8-3b79-463c-81c3-874a78f5b567</messageId>
   </volumeGroupRollback>
   <skipAAI>true</skipAAI>
   <notificationUrl>http://localhost:8080/mso/WorkflowMessage/VNFAResponse/9a5a91e8-3b79-463c-81c3-874a78f5b567</notificationUrl>
</rollbackVolumeGroupRequest>
	"""
	
	
	
	@Before
	public void init()
	{
		MockitoAnnotations.initMocks(this)
	}
	
	@Test
	public void testBuildRollbackVolumeGroupRequestXml() {
		DoCreateVfModuleVolumeV2 process = new DoCreateVfModuleVolumeV2()
		String xml = process.buildRollbackVolumeGroupRequestXml(
			"171907d6-cdf0-4e08-953d-81ee104005a7", 	// volumeGroupId
			"mtwnj1a", 									// cloudSiteId
			"c2141e3fcae940fcb4797ec9115e5a7a", 		// tenantId
			"230fd6ac-2a39-4be4-9b1e-7b7e1cc039b5", 	// requestId
			"88c871d6-be09-4982-8490-96b1d243fb34", 	// serviceInstanceId
			"9a5a91e8-3b79-463c-81c3-874a78f5b567", 	// messageId
			"http://localhost:8080/mso/WorkflowMessage/VNFAResponse/9a5a91e8-3b79-463c-81c3-874a78f5b567")	// notificationUrl

		assertEquals(volumeRollbackRequest.replaceAll("\\s", ""), xml.replaceAll("\\s", ""))
	}


	@Test
	public void testUpdateRollbackVolumeGroupRequestXml() {
		DoCreateVfModuleVolumeV2 process = new DoCreateVfModuleVolumeV2()
		String updatedXml = process.updateRollbackVolumeGroupRequestXml(volumeRollbackRequest, "mdt22avrr_volume01/0f1aaae8-efe3-45ce-83e1-bfad01db58d8")
		assertEquals(volumeRollbackRequestWithStackId.replaceAll("\\s", ""), updatedXml.replaceAll("\\s", ""))
	}

	@Test
	public void testPrepareVnfAdapterCreateRequest (){
		ExecutionEntity mockExecution = setupMock('DoCreateVfModuleVolumeV2')

		when(mockExecution.getVariable("prefix")).thenReturn('DCVFMODVOLV2_')
		when(mockExecution.getVariable("serviceInstanceId")).thenReturn('')
		when(mockExecution.getVariable("vnfId")).thenReturn('test-vnf-id')
		when(mockExecution.getVariable("mso-request-id")).thenReturn('1234')
		when(mockExecution.getVariable("volumeGroupId")).thenReturn('1234')
		when(mockExecution.getVariable("mso.use.qualified.host")).thenReturn("true")
		when(mockExecution.getVariable("mso.workflow.message.endpoint")).thenReturn("http://localhost:28080/mso/WorkflowMesssage")
		Map vfModuleInputParams = new HashMap()
		vfModuleInputParams.put("param1","value1")
		when(mockExecution.getVariable("vfModuleInputParams")).thenReturn(vfModuleInputParams)
		DoCreateVfModuleVolumeV2 process = new DoCreateVfModuleVolumeV2()
		process.prepareVnfAdapterCreateRequest(mockExecution,"true");
		Mockito.verify(mockExecution,times(2)).setVariable(captor.capture(), captor.capture())
		String DCVFMODVOLV2_createVnfARequest = captor.getValue();
		assertNotNull(DCVFMODVOLV2_createVnfARequest)
	}
}
