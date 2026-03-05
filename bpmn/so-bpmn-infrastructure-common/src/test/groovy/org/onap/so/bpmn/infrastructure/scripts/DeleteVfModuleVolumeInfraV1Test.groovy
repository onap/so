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
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner
import org.onap.aai.domain.yang.VolumeGroup
import org.onap.so.bpmn.common.scripts.DeleteAAIVfModule
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.bpmn.core.WorkflowException
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types
import org.onap.aaiclient.client.graphinventory.exceptions.GraphInventoryUriComputationException

import javax.ws.rs.NotFoundException

import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.doNothing
import static org.mockito.Mockito.doThrow
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

class DeleteVfModuleVolumeInfraV1Test extends MsoGroovyTest {

	@Spy
	DeleteVfModuleVolumeInfraV1 deleteVfModuleVolumeInfraV1 ;

	@Captor
	static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

	@Before
	void init() throws IOException {
		super.init("DeleteVfModuleVolumeInfraV1")
		MockitoAnnotations.openMocks(this);
		when(deleteVfModuleVolumeInfraV1.getAAIClient()).thenReturn(client)
	}

	String deleteVnfAdapterRequestXml = """<deleteVolumeGroupRequest>
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

	String dbRequestXml = """<soapenv:Envelope xmlns:req="http://org.onap.so/requestsdb"
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

	String completionRequestXml = """<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                            xmlns:ns="http://org.onap/so/request/types/v1"
                            xmlns="http://org.onap/so/infra/vnf-request/v1">
   <request-info>
      <request-id>TEST-REQUEST-ID-0123</request-id>
      <action>DELETE</action>
      <source>VID</source>
   </request-info>
   <aetgt:status-message>Volume Group has been deleted successfully.</aetgt:status-message>
   <aetgt:mso-bpel-name>BPMN VF Module Volume action: DELETE</aetgt:mso-bpel-name>
</aetgt:MsoCompletionRequest>"""

	String falloutHandlerRequestXml = """<aetgt:FalloutHandlerRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                             xmlns:ns="http://org.onap/so/request/types/v1"
                             xmlns="http://org.onap/so/infra/vnf-request/v1">
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
		when(mockExecution.getVariable("mso.workflow.message.endpoint")).thenReturn('http://localhost:28080/mso/WorkflowMessage')
		when(mockExecution.getVariable("mso.use.qualified.host")).thenReturn('')

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
		when(mockExecution.getVariable("mso.adapters.db.auth")).thenReturn("757A94191D685FD2092AC1490730A4FC")
		when(mockExecution.getVariable("mso.msoKey")).thenReturn("07a7159d3bf51a0e53be7a8f89699be7")

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

	@Test
    void testQueryAAIForVolumeGroup(){
        when(mockExecution.getVariable("DELVfModVol_volumeGroupId")).thenReturn("volumeGroupId1")
        when(mockExecution.getVariable("DELVfModVol_aicCloudRegion")).thenReturn("region1")
        AAIResultWrapper wrapper = mockVolumeGroupWrapper("region1", "volumeGroupId1", "__files/AAI/VolumeGroupWithTenant.json")
        Optional<VolumeGroup> volumeGroupOp = wrapper.asBean(VolumeGroup.class)
        deleteVfModuleVolumeInfraV1.queryAAIForVolumeGroup(mockExecution, true)
        verify(mockExecution).setVariable("DELVfModVol_volumeGroupTenantId", "Tenant123")
    }

    @Test
    void testQueryAAIForVolumeGroupWithVfModule(){
        when(mockExecution.getVariable("DELVfModVol_volumeGroupId")).thenReturn("volumeGroupId1")
        when(mockExecution.getVariable("DELVfModVol_aicCloudRegion")).thenReturn("region1")
        AAIResultWrapper wrapper = mockVolumeGroupWrapper("region1", "volumeGroupId1", "__files/AAI/VolumeGroupWithVfModule.json")
        try {
            deleteVfModuleVolumeInfraV1.queryAAIForVolumeGroup(mockExecution, true)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }
        Mockito.verify(mockExecution, times(3)).setVariable(captor.capture(), captor.capture())
        WorkflowException workflowException = captor.getValue()
        Assert.assertEquals(2500, workflowException.getErrorCode())
        Assert.assertEquals("Volume Group volumeGroupId1 currently in use - found vf-module relationship.", workflowException.getErrorMessage())
    }

    @Test
    void testQueryAAIForVolumeGroupNoTenant(){
        when(mockExecution.getVariable("DELVfModVol_volumeGroupId")).thenReturn("volumeGroupId1")
        when(mockExecution.getVariable("DELVfModVol_aicCloudRegion")).thenReturn("region1")
        AAIResultWrapper wrapper = mockVolumeGroupWrapper("region1", "volumeGroupId1", "__files/AAI/VolumeGroup.json")
        try {
            deleteVfModuleVolumeInfraV1.queryAAIForVolumeGroup(mockExecution, true)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }
        Mockito.verify(mockExecution, times(3)).setVariable(captor.capture(), captor.capture())
        WorkflowException workflowException = captor.getValue()
        Assert.assertEquals(2500, workflowException.getErrorCode())
        Assert.assertEquals( "Could not find Tenant Id element in Volume Group with Volume Group Id volumeGroupId1", workflowException.getErrorMessage())
    }

    @Test
    void testQueryAAIForVolumeGroupNoId(){
        when(mockExecution.getVariable("DELVfModVol_aicCloudRegion")).thenReturn("region1")
        try {
            deleteVfModuleVolumeInfraV1.queryAAIForVolumeGroup(mockExecution, true)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }
        Mockito.verify(mockExecution, times(1)).setVariable(captor.capture(), captor.capture())
        WorkflowException workflowException = captor.getValue()
        Assert.assertEquals(2500, workflowException.getErrorCode())
        Assert.assertEquals("volume-group-id is not provided in the request", workflowException.getErrorMessage())
    }

    @Test
    void testDeleteVolGrpId(){
        VolumeGroup volumeGroup = new VolumeGroup()
        volumeGroup.setVolumeGroupId("volumeGroupId1")
        when(mockExecution.getVariable("DELVfModVol_queryAAIVolGrpResponse")).thenReturn(volumeGroup)
        when(mockExecution.getVariable("DELVfModVol_aicCloudRegion")).thenReturn("region1")
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(CLOUD_OWNER, "region1").volumeGroup("volumeGroupId1"))
        doNothing().when(client).delete(resourceUri)
        deleteVfModuleVolumeInfraV1.deleteVolGrpId(mockExecution, true)
        verify(client).delete(resourceUri)
    }

    @Test
    void testDeleteVolGrpIdNotFound(){
        VolumeGroup volumeGroup = new VolumeGroup()
        volumeGroup.setVolumeGroupId("volumeGroupId1")
        when(mockExecution.getVariable("DELVfModVol_queryAAIVolGrpResponse")).thenReturn(volumeGroup)
        when(mockExecution.getVariable("DELVfModVol_aicCloudRegion")).thenReturn("region1")
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(CLOUD_OWNER, "region1").volumeGroup("volumeGroupId1"))
        doThrow(new NotFoundException("Not Found")).when(client).delete(resourceUri)
        try {
            deleteVfModuleVolumeInfraV1.deleteVolGrpId(mockExecution, true)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }
        Mockito.verify(mockExecution, times(1)).setVariable(captor.capture(), captor.capture())
        WorkflowException workflowException = captor.getValue()
        Assert.assertEquals(2500, workflowException.getErrorCode())
        Assert.assertEquals("Volume group volumeGroupId1 not found for delete in AAI Response code: 404", workflowException.getErrorMessage())
    }

    @Test
    void testDeleteVolGrpIdError(){
        VolumeGroup volumeGroup = new VolumeGroup()
        volumeGroup.setVolumeGroupId("volumeGroupId1")
        when(mockExecution.getVariable("DELVfModVol_queryAAIVolGrpResponse")).thenReturn(volumeGroup)
        when(mockExecution.getVariable("DELVfModVol_aicCloudRegion")).thenReturn("region1")
        AAIResourceUri resourceUri = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.cloudInfrastructure().cloudRegion(CLOUD_OWNER, "region1").volumeGroup("volumeGroupId1"))
        doThrow(new GraphInventoryUriComputationException("Error")).when(client).delete(resourceUri)
        try {
            deleteVfModuleVolumeInfraV1.deleteVolGrpId(mockExecution, true)
        } catch (Exception ex) {
            println " Test End - Handle catch-throw BpmnError()! "
        }
        Mockito.verify(mockExecution, times(1)).setVariable(captor.capture(), captor.capture())
        WorkflowException workflowException = captor.getValue()
        Assert.assertEquals(5000, workflowException.getErrorCode())
        Assert.assertEquals("Received error from A&AI ()", workflowException.getErrorMessage())
    }
}
