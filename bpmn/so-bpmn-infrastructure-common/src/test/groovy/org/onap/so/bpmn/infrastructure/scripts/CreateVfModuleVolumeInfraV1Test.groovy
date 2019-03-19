/*- 
 * ============LICENSE_START======================================================= 
 * ONAP - SO 
 * ================================================================================ 
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved. 
 * ================================================================================ 
 * Modifications Copyright (c) 2019 Samsung
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
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.Variables
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.mockito.ArgumentCaptor
import org.mockito.MockitoAnnotations
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.onap.so.client.aai.entities.AAIResultWrapper
import org.onap.so.client.aai.entities.uri.AAIResourceUri

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
}"""

    String serviceInstanceRequestXml = """
<service-instance>
               <service-instance-id>MIS/1604/0026/SW_INTERNET</service-instance-id>
               <resource-version>123456789</resource-version>
               <relationship-list>
                  <relationship>
                     <related-to>cvlan-tag</related-to>
                     <related-link>https://aai-ext1.test.com:8443/aai/v2/network/vces/vce/832bace2-3fb0-49e0-a6a4-07c47223c535/port-groups/port-group/slcp1447vbc.ipag/cvlan-tags/cvlan-tag/2003/</related-link>
                     <relationship-data>
                        <relationship-key>cvlan-tag.cvlan-tag</relationship-key>
                        <relationship-value>2003</relationship-value>
                     </relationship-data>
                     <relationship-data>
                        <relationship-key>port-group.interface-id</relationship-key>
                        <relationship-value>slcp1447vbc.ipag</relationship-value>
                     </relationship-data>
                     <relationship-data>
                        <relationship-key>vce.vnf-id</relationship-key>
                        <relationship-value>832bace2-3fb0-49e0-a6a4-07c47223c535</relationship-value>
                     </relationship-data>
                  </relationship>
                  <relationship>
                     <related-to>vce</related-to>
                     <related-link>https://aai-ext1.test.com:8443/aai/v2/network/vces/vce/832bace2-3fb0-49e0-a6a4-07c47223c535/</related-link>
                     <relationship-data>
                        <relationship-key>vce.vnf-id</relationship-key>
                        <relationship-value>832bace2-3fb0-49e0-a6a4-07c47223c535</relationship-value>
                     </relationship-data>
                  </relationship>
               </relationship-list>
            </service-instance>"""


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
    public void init() {
        super.init("CreateVfModuleVolumeInfraV1")
        MockitoAnnotations.initMocks(this)
    }


    @Test
    public void testPreProcessRequest() {

        when(mockExecution.getVariable("prefix")).thenReturn('CVMVINFRAV1_')
        when(mockExecution.getVariable("bpmnRequest")).thenReturn(jsonRequest)
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn('TBD')
        when(mockExecution.getVariable("vnfId")).thenReturn('test-vnf-id')
        when(mockExecution.getVariable("mso-request-id")).thenReturn('1234')
        when(mockExecution.getVariable("mso.rollback")).thenReturn('true')

        CreateVfModuleVolumeInfraV1 createVfModuleVolumeInfraV1 = new CreateVfModuleVolumeInfraV1()
        createVfModuleVolumeInfraV1.preProcessRequest(mockExecution, 'true')

        // Capture the arguments to setVariable
        ArgumentCaptor<String> captor1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> captor2 = ArgumentCaptor.forClass(String.class);

        verify(mockExecution, times(19)).setVariable(captor1.capture(), captor2.capture())

        verify(mockExecution).setVariable(eq("CVMVINFRAV1_SuccessIndicator"), eq(false))
        verify(mockExecution).setVariable(eq("CVMVINFRAV1_syncResponseSent"), eq(false))
        verify(mockExecution).setVariable(eq("prefix"), eq("CVMVINFRAV1_"))
        verify(mockExecution).setVariable(eq("bpmnRequest"), any(VariableMap.class))
        VariableMap request = Variables.createVariables()
        request.putValueTyped('payload', Variables.objectValue(jsonRequest)
                .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
                .create())
        verify(mockExecution).setVariable(eq("CVMVINFRAV1_Request"), eq(request))
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
        when(mockExecution.getVariable("CVMVINFRAV1_Request")).thenReturn(serviceInstanceRequestXml)
        when(createVfModuleVolumeInfraV1.getAAIClient()).thenReturn(client)
        doReturn(true).when(client).exists((AAIResourceUri) isA(AAIResourceUri.class))
        createVfModuleVolumeInfraV1.callRESTQueryAAIServiceInstance(mockExecution, true)
    }

    @Test
    public void testcallRESTQueryAAIServiceInstance_NoData() {
        CreateVfModuleVolumeInfraV1 createVfModuleVolumeInfraV1 = spy(CreateVfModuleVolumeInfraV1.class)
        when(createVfModuleVolumeInfraV1.getAAIClient()).thenReturn(client)
        AAIResultWrapper resultWrapper = new AAIResultWrapper("{}")
        when(client.get((AAIResourceUri) isA(AAIResourceUri.class))).thenReturn(resultWrapper)
        thrown.expect(BpmnError.class)
        createVfModuleVolumeInfraV1.callRESTQueryAAIServiceInstance(mockExecution, true)
    }
}
