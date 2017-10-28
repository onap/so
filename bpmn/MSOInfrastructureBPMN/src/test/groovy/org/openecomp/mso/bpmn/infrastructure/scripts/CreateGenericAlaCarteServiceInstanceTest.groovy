package org.openecomp.mso.bpmn.infrastructure.scripts

import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.runtime.Execution
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.openecomp.mso.bpmn.common.scripts.MsoUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.core.json.JsonUtils

import static org.junit.Assert.*;
import static org.mockito.Mockito.*

import com.github.tomakehurst.wiremock.junit.WireMockRule

class CreateGenericAlaCarteServiceInstanceTest  {
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8090);
	
		String Prefix="CRENI_"
		def utils = new MsoUtils()
		JsonUtils jsonUtil = new JsonUtils()
		VidUtils vidUtils = new VidUtils()
	

String jsonIncomingRequest =
"""{
  "service": {
    "name": "so_test4",
    "description": "so_test2",
    "serviceDefId": "60c3e96e-0970-4871-b6e0-3b6de7561519",
    "templateId": "592f9437-a9c0-4303-b9f6-c445bb7e9814",
    "parameters": {
      "globalSubscriberId": "123457",
      "subscriberName": "Customer1",
      "serviceType": "voLTE",
      "templateName": "voLTE Service:1.0",
      "resources": [
        {
          "resourceName": "vIMS",
          "resourceDefId": "60c3e96e-0970-4871-b6e0-3b6de7561516",
          "resourceId": "60c3e96e-0970-4871-b6e0-3b6de7561512",
          "nsParameters": {
            "locationConstraints": [
              {
                "vnfProfileId": "zte-vBAS-1.0",
                "locationConstraints": {
                  "vimId": "4050083f-465f-4838-af1e-47a545222ad0"
                }
              },
              {
                "vnfProfileId": "zte-vMME-1.0",
                "locationConstraints": {
                  "vimId": "4050083f-465f-4838-af1e-47a545222ad0"
                }
              }
            ],
            "additionalParamForNs": {}
          }
        },
        {
          "resourceName": "vEPC",
          "resourceDefId": "61c3e96e-0970-4871-b6e0-3b6de7561516",
          "resourceId": "62c3e96e-0970-4871-b6e0-3b6de7561512",
          "nsParameters": {
            "locationConstraints": [
              {
                "vnfProfileId": "zte-CSCF-1.0",
                "locationConstraints": {
                  "vimId": "4050083f-465f-4838-af1e-47a545222ad1"
                }
              }
            ],
            "additionalParamForNs": {}
          }
        },
        {
          "resourceName": "underlayvpn",
          "resourceDefId": "60c3e96e-0970-4871-b6e0-3b6de7561513",
          "resourceId": "60c3e96e-0970-4871-b6e0-3b6de7561514",
          "nsParameters": {
            "locationConstraints": [],
            "additionalParamForNs": {
              "externalDataNetworkName": "Flow_out_net",
              "m6000_mng_ip": "181.18.20.2",
              "externalCompanyFtpDataNetworkName": "Flow_out_net",
              "externalPluginManageNetworkName": "plugin_net_2014",
              "externalManageNetworkName": "mng_net_2017",
              "sfc_data_network": "sfc_data_net_2016",
              "NatIpRange": "210.1.1.10-210.1.1.20",
              "location": "4050083f-465f-4838-af1e-47a545222ad0",
              "sdncontroller": "9b9f02c0-298b-458a-bc9c-be3692e4f35e"
            }
          }
        },
        {
          "resourceName": "overlayvpn",
          "resourceDefId": "60c3e96e-0970-4871-b6e0-3b6de7561517",
          "resourceId": "60c3e96e-0970-4871-b6e0-3b6de7561518",
          "nsParameters": {
            "locationConstraints": [],
            "additionalParamForNs": {
              "externalDataNetworkName": "Flow_out_net",
              "m6000_mng_ip": "181.18.20.2",
              "externalCompanyFtpDataNetworkName": "Flow_out_net",
              "externalPluginManageNetworkName": "plugin_net_2014",
              "externalManageNetworkName": "mng_net_2017",
              "sfc_data_network": "sfc_data_net_2016",
              "NatIpRange": "210.1.1.10-210.1.1.20",
              "location": "4050083f-465f-4838-af1e-47a545222ad0",
              "sdncontroller": "9b9f02c0-298b-458a-bc9c-be3692e4f35e"
            }
          }
        }
      ]
    }
  }
}"""

	    @Before
		public void init()
		{
			MockitoAnnotations.initMocks(this)
			
		}

		public void initializeVariables(Execution mockExecution) {
			
			verify(mockExecution).setVariable(Prefix + "Success", false)
			
			verify(mockExecution).setVariable(Prefix + "CompleteMsoProcessRequest", "")
			verify(mockExecution).setVariable(Prefix + "FalloutHandlerRequest", "")
			verify(mockExecution).setVariable(Prefix + "isSilentSuccess", false)
				
		}
				
		@Test
		//@Ignore  
		public void preProcessRequest() {
			
			println "************ preProcessRequest() ************* " 
			ExecutionEntity mockExecution = mock(ExecutionEntity.class)
			// Initialize prerequisite variables
			when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
			when(mockExecution.getVariable("bpmnRequest")).thenReturn(jsonIncomingRequest)
			
									
			// preProcessRequest(Execution execution)						
			CreateGenericALaCarteServiceInstance createGenericALaCarteServiceInstance = new CreateGenericALaCarteServiceInstance()
            createGenericALaCarteServiceInstance.preProcessRequest(mockExecution)

			verify(mockExecution).getVariable("isDebugLogEnabled")
			verify(mockExecution).setVariable("prefix", Prefix)
			
			initializeVariables(mockExecution)
			//verify(mockExecution).setVariable(Prefix + "Success", false)
							
		}
		}