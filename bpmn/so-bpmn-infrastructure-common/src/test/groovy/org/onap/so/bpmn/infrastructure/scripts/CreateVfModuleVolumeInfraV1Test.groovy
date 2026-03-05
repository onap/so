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

import org.camunda.bpm.engine.delegate.BpmnError
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.onap.aai.domain.yang.ResultData
import org.onap.aai.domain.yang.SearchResults
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder.Types

import static org.junit.Assert.assertEquals
import static org.mockito.Mockito.*

class CreateVfModuleVolumeInfraV1Test extends MsoGroovyTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	String jsonRequest = """
{
	"requestDetails": {
		"modelInfo": {
			"modelType": "volumeGroup",
			"modelId": "ff5256d2-5a33-55df-13ab-12abad84e7ff",
			"modelNameVersionId": "fe6478e5-ea33-3346-ac12-ab121484a3fe",
			"modelName": "vSAMP12::base::module-0",
			"modelVersion": "1"
		},
		"cloudConfiguration": {
			"lcpCloudRegionId": "mdt1",
			"tenantId": "88a6ca3ee0394ade9403f075db23167e"
		},
		"requestInfo": {
			"instanceName": "MSOTESTVOL101a-vSAMP12_base_vol_module-0",
			"source": "VID",
			"suppressRollback": false
		},
		"relatedInstanceList": [
			{
				"relatedInstance": {
					"instanceId": "{service-instance-id}",
					"modelInfo": {
						"modelType": "service",
						"modelInvariantUuid": "ff5256d1-5a33-55df-13ab-12abad84e7ff",
						"modelUuid": "fe6478e4-ea33-3346-ac12-ab121484a3fe",
						"modelName": "Test",
						"modelVersion": "2.0"
					}
				}
			}, {
				"relatedInstance": {
					"instanceId": "{vnf-instance-id}",
					"modelInfo": {
						"modelType": "vnf",
						"modelId": "ff5256d1-5a33-55df-13ab-12abad84e7ff",
						"modelNameVersionId": "fe6478e4-ea33-3346-ac12-ab121484a3fe",
						"modelName": "vSAMP12",
						"modelVersion": "1",
						"modelInstanceName": "vSAMP12"
					}
				}
			}
		],
		"requestParameters": {
			"serviceId": "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb",
			"userParams": [
				{"name": "vnfName", "value": "STMTN5MMSC20" },
				{"name": "vnfName2", "value": "US1117MTSNJVBR0246" },
				{"name": "vnfNmInformation", "value": "" },
				{"name": "vnfType", "value": "pcrf-capacity" },
				{"name": "vnfId", "value": "skask" },
				{"name": "vnfStackId", "value": "slowburn" },
				{"name": "vnfStatus", "value": "created" },
				{"name": "aicCloudRegion", "value": "MDTWNJ21" },
				{"name": "availabilityZone", "value": "slcp3-esx-az01" },
				{"name": "oamNetworkName", "value": "VLAN-OAM-1323" },
				{"name": "vmName", "value": "slcp34246vbc246ceb" },
				{"name": "ipagNetworkId", "value": "970cd2b9-7f09-4a12-af47-182ea38ba1f0" },
				{"name": "vpeNetworkId", "value": "545cc2c3-1930-4100-b534-5d82d0e12bb6" }
			]
		}
	}
}
"""
	
	String volumeRequestXml = """<volume-request xmlns="http://www.w3.org/2001/XMLSchema">
   <request-info>
      <action>CREATE_VF_MODULE_VOL</action>
      <source>VID</source>
      <service-instance-id/>
   </request-info>
   <volume-inputs>
      <volume-group-id/>
      <volume-group-name>MSOTESTVOL101a-vSAMP12_base_vol_module-0</volume-group-name>
      <vnf-type>Test/vSAMP12</vnf-type>
      <vf-module-model-name>vSAMP12::base::module-0</vf-module-model-name>
      <asdc-service-model-version>2.0</asdc-service-model-version>
      <aic-cloud-region>mdt1</aic-cloud-region>
      <tenant-id>88a6ca3ee0394ade9403f075db23167e</tenant-id>
      <service-id>a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb</service-id>
      <backout-on-failure>true</backout-on-failure>
      <model-customization-id/>
   </volume-inputs>
   <volume-params>
      <param name="vnf_name">STMTN5MMSC20</param>
      <param name="vnf_name2">US1117MTSNJVBR0246</param>
      <param name="vnf_nm_information"/>
      <param name="vnf_type">pcrf-capacity</param>
      <param name="vnf_id">skask</param>
      <param name="vnf_stack_id">slowburn</param>
      <param name="vnf_status">created</param>
      <param name="aic_cloud_region">MDTWNJ21</param>
      <param name="availability_zone">slcp3-esx-az01</param>
      <param name="oam_network_name">VLAN-OAM-1323</param>
      <param name="vm_name">slcp34246vbc246ceb</param>
      <param name="ipag_network_id">970cd2b9-7f09-4a12-af47-182ea38ba1f0</param>
      <param name="vpe_network_id">545cc2c3-1930-4100-b534-5d82d0e12bb6</param>
   </volume-params>
</volume-request>"""
	 	
	String completeMsoRequestXml = """<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.onap/so/workflow/schema/v1"
                            xmlns:ns="http://org.onap/so/request/types/v1"
                            xmlns="http://org.onap/so/infra/vnf-request/v1">
   <request-info>
      <request-id>1234</request-id>
      <action>CREATE</action>
      <source>VID</source>
   </request-info>
   <aetgt:status-message>Volume Group has been created successfully.</aetgt:status-message>
   <aetgt:mso-bpel-name>BPMN VF Module Volume action: CREATE</aetgt:mso-bpel-name>
</aetgt:MsoCompletionRequest>"""


    @Before
	public void init()
	{
		super.init("CreateVfModuleVolumeInfraV1")
		MockitoAnnotations.initMocks(this)
	}
	

	@Test
	@Ignore
	public void testPreProcessRequest() {
		
		when(mockExecution.getVariable("prefix")).thenReturn('CVMVINFRAV1_')
		when(mockExecution.getVariable("bpmnRequest")).thenReturn(jsonRequest)
		when(mockExecution.getVariable("serviceInstanceId")).thenReturn('')
		when(mockExecution.getVariable("vnfId")).thenReturn('test-vnf-id')
		when(mockExecution.getVariable("mso-request-id")).thenReturn('1234')
		when(mockExecution.getVariable("mso.rollback")).thenReturn('true')
								
		CreateVfModuleVolumeInfraV1 createVfModuleVolumeInfraV1 = new CreateVfModuleVolumeInfraV1()
		createVfModuleVolumeInfraV1.preProcessRequest(mockExecution, 'true')
		
		// Capture the arguments to setVariable
		ArgumentCaptor<String> captor1 = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> captor2 = ArgumentCaptor.forClass(String.class);
		
		verify(mockExecution, times(15)).setVariable(captor1.capture(), captor2.capture())
		
		List<String> arg2List = captor2.getAllValues()
		String volumeRequestActual = arg2List.get(6)
		String isVidRequestActual = arg2List.get(8) 
		
		assertEquals(volumeRequestXml, volumeRequestActual.trim())
		assertEquals('true', isVidRequestActual)
	}
	
	@Test
	public void testPostProcessResponse() {
		
		when(mockExecution.getVariable("dbReturnCode")).thenReturn('000')
		when(mockExecution.getVariable("CVMVINFRAV1_createDBResponse")).thenReturn('')
		when(mockExecution.getVariable("mso-request-id")).thenReturn('1234')
		when(mockExecution.getVariable("CVMVINFRAV1_source")).thenReturn('VID')
								
		CreateVfModuleVolumeInfraV1 createVfModuleVolumeInfraV1 = new CreateVfModuleVolumeInfraV1()
		createVfModuleVolumeInfraV1.postProcessResponse(mockExecution, 'true')
		
		verify(mockExecution).setVariable('CVMVINFRAV1_Success', true)
		verify(mockExecution).setVariable('CVMVINFRAV1_CompleteMsoProcessRequest', completeMsoRequestXml)
	}

	@Test
	public void testcallRESTQueryAAIServiceInstance() {
		CreateVfModuleVolumeInfraV1 createVfModuleVolumeInfraV1 = spy(CreateVfModuleVolumeInfraV1.class)
		when(createVfModuleVolumeInfraV1.getAAIClient()).thenReturn(client)
		AAIResultWrapper resultWrapper = new AAIResultWrapper(SEARCH_RESULT_AAI_WITH_RESULTDATA)
		when(client.get(isA(AAIResourceUri.class))).thenReturn(resultWrapper)
		createVfModuleVolumeInfraV1.callRESTQueryAAIServiceInstance(mockExecution,true)
	}

	@Test
	public void testcallRESTQueryAAIServiceInstance_NoData() {
		CreateVfModuleVolumeInfraV1 createVfModuleVolumeInfraV1 = spy(CreateVfModuleVolumeInfraV1.class)
		when(createVfModuleVolumeInfraV1.getAAIClient()).thenReturn(client)
		AAIResultWrapper resultWrapper = new AAIResultWrapper("{}")
		when(client.get(isA(AAIResourceUri.class))).thenReturn(resultWrapper)
		thrown.expect(BpmnError.class)
		createVfModuleVolumeInfraV1.callRESTQueryAAIServiceInstance(mockExecution,true)
	}
}
