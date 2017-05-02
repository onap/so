/*- 
 * ============LICENSE_START======================================================= 
 * OPENECOMP - MSO 
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

package org.openecomp.mso.bpmn.infrastructure.scripts;

import static org.junit.Assert.*
import static org.mockito.Mockito.*

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.openecomp.mso.bpmn.common.scripts.MsoGroovyTest


@RunWith(MockitoJUnitRunner.class)
class DoCreateVfModuleVolumeV1Test extends MsoGroovyTest  {
	
	def volumeRequest =
"""<volume-request xmlns="http://org.openecomp/mso/infra/vnf-request/v1" xmlns:xs="http://www.w3.org/2001/XMLSchema">
	<request-info>
		<test-data-only>123abc</test-data-only> <!-- don't remove this tag. Its used for junit test -->
		<request-id>d8d4fcfa-fd7e-4413-b19d-c95aa67291b8</request-id>
		<action>CREATE_VF_MODULE_VOL</action>
		<source>SoapUI-bns-create-base-vol-1001-1</source>
	</request-info>
	<volume-inputs>
		<vnf-type>Test/vSAMP12</vnf-type>
		<vf-module-model-name>vSAMP12::base::module-0</vf-module-model-name>
		<backout-on-failure>true</backout-on-failure>
		<asdc-service-model-version>2.0</asdc-service-model-version>
		<service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
		<aic-cloud-region>MDTWNJ21</aic-cloud-region>
		<tenant-id>897deadc2b954a6bac6d3c197fb3525e</tenant-id>
		<volume-group-name>MSOTESTVOL101a-vSAMP12_base_vol_module-0</volume-group-name>
		<volume-group-id/>
	</volume-inputs>
	<volume-params>
		<param name="param1">value1</param>"
		<param name="param2">value2</param>"
		<param name="param3">value3</param>"
	</volume-params>	
</volume-request>"""

	def genericVnfResponseXml = """
<generic-vnf xmlns="http://org.openecomp.aai.inventory/v8">
	<vnf-id>TEST-VNF-ID-0123</vnf-id>
	<vnf-name>STMTN5MMSC20</vnf-name>
	<vnf-type>pcrf-capacity</vnf-type>
	<service-id>SDN-MOBILITY</service-id>
	<equipment-role>vPCRF</equipment-role>
	<orchestration-status>pending-create</orchestration-status>
	<in-maint>false</in-maint>
	<is-closed-loop-disabled>false</is-closed-loop-disabled>
	<persona-model-id>introvert</persona-model-id>
	<persona-model-version>2.0</persona-model-version>
	<resource-version>0000020</resource-version>
	<vf-modules>
		<vf-module>
			<vf-module-id>lukewarm</vf-module-id>
			<vf-module-name>PCRF::module-0-0</vf-module-name>
			<persona-model-id>introvert</persona-model-id>
			<persona-model-version>2.0</persona-model-version>
			<is-base-vf-module>true</is-base-vf-module>
			<heat-stack-id>fastburn</heat-stack-id>
			<orchestration-status>pending-create</orchestration-status>
			<resource-version>0000074</resource-version>
		</vf-module>
		<vf-module>
			<vf-module-id>supercool</vf-module-id>
			<vf-module-name>PCRF::module-1-0</vf-module-name>
			<persona-model-id>extrovert</persona-model-id>
			<persona-model-version>2.0</persona-model-version>
			<is-base-vf-module>false</is-base-vf-module>
			<heat-stack-id>slowburn</heat-stack-id>
			<orchestration-status>pending-create</orchestration-status>
			<resource-version>0000075</resource-version>
		</vf-module>
	</vf-modules>
	<relationship-list/>
	<l-interfaces/>
	<lag-interfaces/>
</generic-vnf>
"""		
	def String expectedCreateVnfRequestXml = """<createVolumeGroupRequest>
   <cloudSiteId>MDTWNJ21</cloudSiteId>
   <tenantId>897deadc2b954a6bac6d3c197fb3525e</tenantId>
   <vnfId>TEST-VNF-ID-0123</vnfId>
   <vnfName>STMTN5MMSC20</vnfName>
   <volumeGroupId>test-vol-group-id-123</volumeGroupId>
   <volumeGroupName>MSOTESTVOL101a-vSAMP12_base_vol_module-0</volumeGroupName>
   <vnfType>Test/vSAMP12</vnfType>
   <vnfVersion>2.0</vnfVersion>
   <vfModuleType>vSAMP12::base::module-0</vfModuleType>
   <modelCustomizationUuid/>
   <volumeGroupParams>
      <entry>
         <key>vnf_id</key>
         <value>TEST-VNF-ID-0123</value>
      </entry>
      <entry>
         <key>vnf_name</key>
         <value>STMTN5MMSC20</value>
      </entry>
      <entry>
         <key>vf_module_id</key>
         <value>test-vol-group-id-123</value>
      </entry>
      <entry>
         <key>vf_module_name</key>
         <value>MSOTESTVOL101a-vSAMP12_base_vol_module-0</value>
      </entry>
      <entry>
         <key>param1</key>
         <value>value1</value>
      </entry>
      <entry>
         <key>param2</key>
         <value>value2</value>
      </entry>
      <entry>
         <key>param3</key>
         <value>value3</value>
      </entry>
   </volumeGroupParams>
   <skipAAI>true</skipAAI>
   <backout>true</backout>
   <failIfExists>true</failIfExists>
   <msoRequest>
      <requestId>d8d4fcfa-fd7e-4413-b19d-c95aa67291b8</requestId>
      <serviceInstanceId>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</serviceInstanceId>
   </msoRequest>
   <messageId>111</messageId>
   <notificationUrl>http://localhost:28080/mso/WorkflowMessage/12345678</notificationUrl>
</createVolumeGroupRequest>
"""

    @Before
	public void init()
	{
		MockitoAnnotations.initMocks(this)
		
	}
	
	@Test
	public void TestPreProcessRequest() {
		
		ExecutionEntity mockExecution = setupMock('DoCreateVfModuleVolumeV1')
		
		when(mockExecution.getVariable("DoCreateVfModuleVolumeV1Request")).thenReturn(volumeRequest)
		when(mockExecution.getVariable("vnf-id")).thenReturn('test-vnf-id-123')
		when(mockExecution.getVariable("volume-group-id")).thenReturn('test-volume-group-id-123')
		when(mockExecution.getVariable("mso-request-id")).thenReturn('test-request-id-123')
								
		DoCreateVfModuleVolumeV1 myprocess = new DoCreateVfModuleVolumeV1()
		myprocess.preProcessRequest(mockExecution, 'true')
		
		verify(mockExecution).setVariable('DCVFMODVOLV1_serviceId', 'a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb')
		verify(mockExecution).setVariable('DCVFMODVOLV1_source', 'SoapUI-bns-create-base-vol-1001-1')
		verify(mockExecution, times(7)).setVariable(anyString(), anyString())
	}
	
	@Test
	public void TestPrepareVnfAdapterCreateRequest() {
		
		ExecutionEntity mockExecution = setupMock('DoCreateVfModuleVolumeV1')
		
		when(mockExecution.getVariable("DCVFMODVOLV1_Request")).thenReturn(volumeRequest)
		when(mockExecution.getVariable("DCVFMODVOLV1_requestId")).thenReturn('d8d4fcfa-fd7e-4413-b19d-c95aa67291b8')
		when(mockExecution.getVariable("DCVFMODVOLV1_serviceId")).thenReturn('a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb')
		when(mockExecution.getVariable("DCVFMODVOLV1_AAIQueryGenericVfnResponse")).thenReturn(genericVnfResponseXml)
		when(mockExecution.getVariable("DCVFMODVOLV1_rollbackEnabled")).thenReturn(true)
		when(mockExecution.getVariable("volume-group-id")).thenReturn('test-vol-group-id-123')
		when(mockExecution.getVariable("URN_mso_workflow_message_endpoint")).thenReturn('http://localhost:28080/mso/WorkflowMessage')
		when(mockExecution.getVariable("URN_mso_use_qualified_host")).thenReturn(true)
								
		DoCreateVfModuleVolumeV1 myprocess = new DoCreateVfModuleVolumeV1()
		myprocess.prepareVnfAdapterCreateRequest(mockExecution, 'true')
		
		// Capture the arguments to setVariable
		ArgumentCaptor<String> captor1 = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> captor2 = ArgumentCaptor.forClass(String.class);
		
		verify(mockExecution, times(2)).setVariable(captor1.capture(), captor2.capture())
		
		List<String> arg2List = captor2.getAllValues()
		String createVnfRequestXml = arg2List.get(0)
		
		//replace messageID value because it is random generated
		createVnfRequestXml = createVnfRequestXml.replaceAll("<messageId>(.+?)</messageId>", "<messageId>111</messageId>")
												 .replaceAll("<notificationUrl>(.+?)</notificationUrl>", "<notificationUrl>http://localhost:28080/mso/WorkflowMessage/12345678</notificationUrl>")
		
		Assert.assertEquals(expectedCreateVnfRequestXml.trim(), createVnfRequestXml.trim())
	}

}
