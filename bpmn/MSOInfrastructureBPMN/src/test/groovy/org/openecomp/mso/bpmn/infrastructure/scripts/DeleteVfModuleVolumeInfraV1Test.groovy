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

package org.openecomp.mso.bpmn.infrastructure.scripts

import static org.junit.Assert.*
import static org.mockito.Mockito.*

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.openecomp.mso.bpmn.common.scripts.MsoGroovyTest
import org.openecomp.mso.bpmn.core.WorkflowException


@RunWith(MockitoJUnitRunner.class)
class DeleteVfModuleVolumeInfraV1Test extends MsoGroovyTest {

	def deleteVnfAdapterRequestXml = """<deleteVolumeGroupRequest>
   <cloudSiteId>RDM2WAGPLCP</cloudSiteId>
   <tenantId>fba1bd1e195a404cacb9ce17a9b2b421</tenantId>
   <volumeGroupId>78987</volumeGroupId>
   <volumeGroupStackId/>
   <skipAAI>true</skipAAI>
   <msoRequest>
      <requestId>TEST-REQUEST-ID-0123</requestId>
      <serviceInstanceId>1234</serviceInstanceId>
   </msoRequest>
   <messageId>ebb9ef7b-a6a5-40e6-953e-f868f1767677</messageId>
   <notificationUrl>http://localhost:28080/mso/WorkflowMessage/VNFAResponse/ebb9ef7b-a6a5-40e6-953e-f868f1767677</notificationUrl>
</deleteVolumeGroupRequest>"""
	
	def dbRequestXml = """<soapenv:Envelope xmlns:req="http://org.openecomp.mso/requestsdb"
                  xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
   <soapenv:Header/>
   <soapenv:Body>
      <req:updateInfraRequest>
         <requestId>TEST-REQUEST-ID-0123</requestId>
         <lastModifiedBy>BPMN</lastModifiedBy>
         <statusMessage>VolumeGroup successfully deleted</statusMessage>
         <requestStatus>COMPLETE</requestStatus>
         <progress>100</progress>
         <vnfOutputs/>
      </req:updateInfraRequest>
   </soapenv:Body>
</soapenv:Envelope>"""
	
	def completionRequestXml = """<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
                            xmlns:ns="http://org.openecomp/mso/request/types/v1"
                            xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
   <request-info>
      <request-id>TEST-REQUEST-ID-0123</request-id>
      <action>DELETE</action>
      <source>VID</source>
   </request-info>
   <aetgt:status-message>Volume Group has been deleted successfully.</aetgt:status-message>
   <aetgt:mso-bpel-name>BPMN VF Module Volume action: DELETE</aetgt:mso-bpel-name>
</aetgt:MsoCompletionRequest>"""
	
	def falloutHandlerRequestXml = """<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
                             xmlns:ns="http://org.openecomp/mso/request/types/v1"
                             xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
   <request-info>
      <request-id>TEST-REQUEST-ID-0123</request-id>
      <action>DELETE</action>
      <source>VID</source>
   </request-info>
   <aetgt:WorkflowException>
      <aetgt:ErrorMessage>Unexpected Error</aetgt:ErrorMessage>
      <aetgt:ErrorCode>5000</aetgt:ErrorCode>
   </aetgt:WorkflowException>
</aetgt:FalloutHandlerRequest>"""
	
	@Before
	public void init()
	{
		MockitoAnnotations.initMocks(this)
	}
	
	
	@Test
	public void testPrepareVnfAdapterDeleteRequest() {
		
		ExecutionEntity mockExecution = setupMock('DeleteVfModuleVolumeInfraV1')
		when(mockExecution.getVariable("DELVfModVol_cloudRegion")).thenReturn('RDM2WAGPLCP')
		when(mockExecution.getVariable("DELVfModVol_tenantId")).thenReturn('fba1bd1e195a404cacb9ce17a9b2b421')
		when(mockExecution.getVariable("DELVfModVol_volumeGroupId")).thenReturn('78987')
		when(mockExecution.getVariable("DELVfModVol_volumeGroupHeatStackId")).thenReturn('')
		when(mockExecution.getVariable("DELVfModVol_requestId")).thenReturn('TEST-REQUEST-ID-0123')
		when(mockExecution.getVariable("DELVfModVol_serviceId")).thenReturn('1234')
		when(mockExecution.getVariable("DELVfModVol_messageId")).thenReturn('ebb9ef7b-a6a5-40e6-953e-f868f1767677')
		when(mockExecution.getVariable("URN_mso_workflow_message_endpoint")).thenReturn('http://localhost:28080/mso/WorkflowMessage')
		when(mockExecution.getVariable("URN_mso_use_qualified_host")).thenReturn('')

		DeleteVfModuleVolumeInfraV1 myproc = new DeleteVfModuleVolumeInfraV1()
		myproc.prepareVnfAdapterDeleteRequest(mockExecution, 'true')
		
		verify(mockExecution).setVariable("DELVfModVol_deleteVnfARequest", deleteVnfAdapterRequestXml)

	}
	
	@Test
	//@Ignore
	public void testPrepareDbRequest() {
		
		ExecutionEntity mockExecution = setupMock('DeleteVfModuleVolumeInfraV1')
		when(mockExecution.getVariable("DELVfModVol_requestId")).thenReturn('TEST-REQUEST-ID-0123')
		when(mockExecution.getVariable("DELVfModVol_volumeOutputs")).thenReturn('')
		when(mockExecution.getVariable("URN_mso_adapters_db_auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
		when(mockExecution.getVariable("URN_mso_msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")
		
		DeleteVfModuleVolumeInfraV1 myproc = new DeleteVfModuleVolumeInfraV1()
		myproc.prepareDBRequest(mockExecution, 'true')
		
		verify(mockExecution).setVariable("DELVfModVol_updateInfraRequest", dbRequestXml)
	}

	@Test
	public void testPrepareCompletionHandlerRequest() {
		
		ExecutionEntity mockExecution = setupMock('DeleteVfModuleVolumeInfraV1')
		when(mockExecution.getVariable("mso-request-id")).thenReturn('TEST-REQUEST-ID-0123')
		when(mockExecution.getVariable("DELVfModVol_source")).thenReturn('VID')
		
		DeleteVfModuleVolumeInfraV1 myproc = new DeleteVfModuleVolumeInfraV1()
		myproc.prepareCompletionHandlerRequest(mockExecution, 'true')
		
		verify(mockExecution).setVariable("DELVfModVol_CompleteMsoProcessRequest", completionRequestXml)
	}
	
	@Test
	public void testPrepareFalloutHandler() {
		
		WorkflowException workflowException = new WorkflowException('DeleteVfModuleVolumeInfraV1', 5000, 'Unexpected Error')
		
		ExecutionEntity mockExecution = setupMock('DeleteVfModuleVolumeInfraV1')
		
		when(mockExecution.getVariable("DELVfModVol_requestId")).thenReturn('TEST-REQUEST-ID-0123')
		when(mockExecution.getVariable("WorkflowException")).thenReturn(workflowException)
		when(mockExecution.getVariable("DELVfModVol_source")).thenReturn('VID')
		
		DeleteVfModuleVolumeInfraV1 myproc = new DeleteVfModuleVolumeInfraV1()
		myproc.prepareFalloutHandler(mockExecution, 'true')
		
		verify(mockExecution).setVariable("DELVfModVol_Success", false)
		verify(mockExecution).setVariable("DELVfModVol_FalloutHandlerRequest", falloutHandlerRequestXml)
	}
}
