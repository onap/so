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
import org.openecomp.mso.bpmn.common.scripts.VidUtils

import static org.junit.Assert.*
import static org.mockito.Mockito.*

import com.github.tomakehurst.wiremock.junit.WireMockRule

class DeleteGenericAlaCarteServiceInstanceTest  {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8090)

    String Prefix="DELSI_"
    def utils = new MsoUtils()
		JsonUtils jsonUtil = new JsonUtils()
		VidUtils vidUtils = new VidUtils()

    String jsonIncomingRequest =
            """{
        "requestDetails": {
        "modelInfo": {
        "modelType": "service",
        "modelInvariantId": "1de901ed-17af-4b03-bc1f-41659cfa27cb",
        "modelVersionId": "ace39141-09ec-4068-b06d-ac6b23bdc6e0",
        "modelName": "demoVLB",
        "modelVersion": "1.0"
        },
        "cloudConfiguration" : {
        "lcpCloudRegionId": "RegionOne",
        "tenantId": "onap"
        },
        "subscriberInfo": {
        "globalSubscriberId": "Demonstration",
        "subscriberName": "Demonstration"
        },
        "requestInfo": {
        "instanceName": "sample-instance-2",
        "productFamilyId": "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb",
        "source": "VID",
        "requestorId":"1234",
        "suppressRollback": "false"
        },
        "requestParameters": {
        "subscriptionServiceType": "vLB"
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
			
			try {
                // preProcessRequest(Execution execution)
                DeleteGenericALaCarteServiceInstance deleteGenericALaCarteServiceInstance = new DeleteGenericALaCarteServiceInstance()
                deleteGenericALaCarteServiceInstance.preProcessRequest(mockExecution)

                verify(mockExecution).getVariable("isDebugLogEnabled")
                verify(mockExecution).setVariable("prefix", Prefix)

                initializeVariables(mockExecution)
                //verify(mockExecution).setVariable(Prefix + "Success", false)
            }catch(Exception e){
                //ignore
            }

		}
}