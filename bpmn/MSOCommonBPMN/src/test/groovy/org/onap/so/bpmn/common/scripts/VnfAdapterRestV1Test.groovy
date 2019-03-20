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
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.internal.debugging.MockitoDebuggerImpl
import org.mockito.runners.MockitoJUnitRunner
import org.onap.so.bpmn.mock.FileUtil

import static org.mockito.Mockito.*
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class VnfAdapterRestV1Test {

	@Before
	public void init()
	{
		MockitoAnnotations.initMocks(this)
	}


	@Test
	public void testPreProcessRequest() {

		String sdncAdapterWorkflowRequest = FileUtil.readResourceFile("__files/vnfAdapterMocks/vnfadapter_request.xml");
		ExecutionEntity mockExecution = mock(ExecutionEntity.class)
		when(mockExecution.getVariable("prefix")).thenReturn("VNFREST_")
		when(mockExecution.getVariable("mso-request-id")).thenReturn("testMsoRequestId")
		when(mockExecution.getVariable("VNFREST_Request")).thenReturn(sdncAdapterWorkflowRequest)
		when(mockExecution.getVariable("mso.adapters.po.auth")).thenReturn("5E12ACACBD552A415E081E29F2C4772F9835792A51C766CCFDD7433DB5220B59969CB2798C")
		when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

		when(mockExecution.getVariable("mso.adapters.vnf.rest.endpoint")).thenReturn("http://localhost:18080/vnfs/rest/v1/vnfs")

		when(mockExecution.getVariable("mso.adapters.sdnc.timeout")).thenReturn("PT5M")

		when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
		when(mockExecution.getVariable("testProcessKey")).thenReturn("testProcessKey")


		VnfAdapterRestV1 vnfAdapterRestV1 = new VnfAdapterRestV1()
		vnfAdapterRestV1.preProcessRequest(mockExecution)

		MockitoDebuggerImpl debugger = new MockitoDebuggerImpl()
		//debugger.printInvocations(mockExecution)


		verify(mockExecution).setVariable("prefix","VNFREST_")

		verify(mockExecution).setVariable("VNFREST_SuccessIndicator",false)
		verify(mockExecution).setVariable("VNFREST_requestType","createVfModuleRequest")
		verify(mockExecution).setVariable("VNFAResponse_CORRELATOR","8892cffa-3514-45d4-b2b0-0fde9a96e323-1511813289000")
		verify(mockExecution).setVariable("VNFREST_vnfAdapterMethod","POST")
		verify(mockExecution).setVariable("VNFREST_notificationUrl","http://localhost:18080/mso/WorkflowMessage/VNFAResponse/8892cffa-3514-45d4-b2b0-0fde9a96e323-1511813289000")
		verify(mockExecution).setVariable("VNFREST_vnfAdapterUrl","http://localhost:18080/vnfs/rest/v1/vnfs/6d2e2469-8708-47c3-a0d4-73fa28a8a50b/vf-modules")

	}

	String rollbackReq = """
<rollbackVolumeGroupRequest>
   <volumeGroupRollback>
      <volumeGroupId>8a07b246-155e-4b08-b56e-76e98a3c2d66</volumeGroupId>
      <volumeGroupStackId>phmaz401me6-vpevre-VOLUMEGROUP/dbd560b6-b03f-4a17-92e7-8942459a60c1</volumeGroupStackId>
      <cloudSiteId>mtrnj1b</cloudSiteId>
      <cloudOwnerId>CloudOwner</cloudOwnerId>
      <tenantId>cfb5e0a790374c9a98a1c0d2044206a7</tenantId>
      <volumeGroupCreated>true</volumeGroupCreated>
      <msoRequest>
         <requestId>1e1a72ca-7300-4ac4-b718-30351f3b6845</requestId>
         <serviceInstanceId>15eb2c68-f771-4030-b185-cff179fdad44</serviceInstanceId>
      </msoRequest>
      <messageId>683ca1ac-2145-4a00-9484-20d48bd701aa</messageId>
   </volumeGroupRollback>
   <skipAAI>true</skipAAI>
   <notificationUrl>http://localhost:8080/mso/WorkflowMessage/VNFAResponse/683ca1ac-2145-4a00-9484-20d48bd701aa</notificationUrl>
</rollbackVolumeGroupRequest>
"""

	@Test
	public void testGetVolumeGroupId() {
		Node root = new XmlParser().parseText(rollbackReq)
		VnfAdapterRestV1 vnfAdapterRestV1 = new VnfAdapterRestV1()
		def volGrpId = vnfAdapterRestV1.getVolumeGroupIdFromRollbackRequest(root)
		assertEquals('8a07b246-155e-4b08-b56e-76e98a3c2d66', volGrpId)
	}


	@Test
	public void testGetMessageId() {
		Node root = new XmlParser().parseText(rollbackReq)

		VnfAdapterRestV1 p = new VnfAdapterRestV1()
		def messageId = p.getMessageIdForVolumeGroupRollback(root)
		assertEquals('683ca1ac-2145-4a00-9484-20d48bd701aa', messageId)
	}

	@Test
	public void testProcessCallback() {

		String sdncAdapterWorkflowRequest = FileUtil.readResourceFile("__files/vnfAdapterMocks/vnfAdapterCallback.xml");
		ExecutionEntity mockExecution = mock(ExecutionEntity.class)

		when(mockExecution.getVariable("VNFAResponse_MESSAGE")).thenReturn(sdncAdapterWorkflowRequest)
		when(mockExecution.getVariable("testProcessKey")).thenReturn("testProcessKey")

		VnfAdapterRestV1 vnfAdapterRestV1 = new VnfAdapterRestV1()
		vnfAdapterRestV1.processCallback(mockExecution)

		verify(mockExecution).setVariable("testProcessKeyResponse" ,sdncAdapterWorkflowRequest)

	}
}
