package org.openecomp.mso.bpmn.infrastructure.scripts

import static org.mockito.Mockito.*

import org.apache.commons.lang3.*
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.runtime.Execution
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.runners.MockitoJUnitRunner
import org.openecomp.mso.bpmn.common.scripts.MsoUtils
import org.openecomp.mso.bpmn.core.WorkflowException
import org.openecomp.mso.bpmn.common.scripts.ExceptionUtil


import com.github.tomakehurst.wiremock.junit.WireMockRule

@RunWith(MockitoJUnitRunner.class)
class SacleCustomE2EServiceInstanceTest{
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8090);

    String Prefix="CRESI_"
    ExceptionUtil exceptionUtil = new ExceptionUtil()
    String globalSubscriberId="test_custormer"
    String requestDescription = "request description for test"
    def utils = new MsoUtils()

    String jsonIncomingRequest = """{"service":{
                                        "serviceType":"example-service-type",
                                        "globalSubscriberId":"test_custormer",
                                        "resources":[
                                            {
                                                "resourceInstanceId":"ns111",
                                                "scaleType":"SCALE_NS",
                                                "scaleNsData":{
                                                    "scaleNsByStepsData":{
                                                        "numberOfSteps":"4",
                                                        "aspectId":"TIC_EDGE_HW",
                                                        "scalingDirection":"UP"
                                                        }
                                                }
                                            },
                                            {
                                                "resourceInstanceId":"ns333",
                                                "scaleType":"SCALE_NS",
                                                "scaleNsData":{
                                                    "scaleNsByStepsData":{
                                                        "numberOfSteps":"4",
                                                        "aspectId":"TIC_EDGE_HW",
                                                        "scalingDirection":"UP"
                                                    }
                                                }
                                            }],
                                        "serviceInstanceName":"XXXX"
                                     },
                                     "operationId":"0a5b1651-c56e-4263-8c26-c8f8a6ef72d8"
                                   }"""

    String xmlMsoCompletionRequest = """<aetgt:MsoCompletionRequest xmlns:aetgt="http://org.openecomp/mso/workflow/schema/v1"
                            xmlns:ns="http://org.openecomp/mso/request/types/v1"
                            xmlns:w1aaan0="http://org.openecomp/mso/infra/vnf-request/v1">
   <w1aaan0:request-info>
      <w1aaan0:request-id>56c881ad-6c9d-4b79-aacc-401e5640b47f</w1aaan0:request-id>
      <w1aaan0:action>SCALE</w1aaan0:action>
      <w1aaan0:source>null</w1aaan0:source>
   </w1aaan0:request-info>
   <status-message>Service Instance was scaled successfully.</status-message>
   <serviceInstanceId>56c881ad-6c9d-4b79-aacc-401e5640b47f</serviceInstanceId>
   <mso-bpel-name>ScaleGenericALaCarteServiceInstance</mso-bpel-name>
</aetgt:MsoCompletionRequest>"""

    String requestInfo = """<request-info xmlns="http://org.openecomp/mso/infra/vnf-request/v1">
					<request-id>56c881ad-6c9d-4b79-aacc-401e5640b47f</request-id>
					<action>SCALE</action>
					<source>null</source>
				   </request-info>"""

    String payload ="""<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                        xmlns:ns="http://org.openecomp.mso/requestsdb">
                        <soapenv:Header/>
                        <soapenv:Body>
                            <ns:updateServiceOperationStatus xmlns:ns="http://org.openecomp.mso/requestsdb">
                            <serviceId>56c881ad-6c9d-4b79-aacc-401e5640b47f</serviceId>
                            <operationId>0a5b1651-c56e-4263-8c26-c8f8a6ef72d8</operationId>
                            <serviceName>XXXX</serviceName>
                            <operationType>SCALE</operationType>
                            <userId></userId>
                            <result>processing</result>
                            <operationContent>Prepare service scaling</operationContent>
                            <progress>0</progress>
                            <reason></reason>
                        </ns:updateServiceOperationStatus>
                    </soapenv:Body>
                </soapenv:Envelope>"""

    @Before
    public void init()
    {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    public void preProcessRequestTest() {
        println "************ preProcessRequest_Payload ************* "
        ExecutionEntity mockExecution = mock(ExecutionEntity.class)

        // Initialize prerequisite variables
        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("bpmnRequest")).thenReturn(jsonIncomingRequest)

        when(mockExecution.getVariable("mso-request-id")).thenReturn("56c881ad-6c9d-4b79-aacc-401e5640b47f")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("56c881ad-6c9d-4b79-aacc-401e5640b47f")

        ScaleCustomE2EServiceInstance scaleCustomE2EServiceInstance = new ScaleCustomE2EServiceInstance()
        scaleCustomE2EServiceInstance.preProcessRequest(mockExecution)

        verify(mockExecution).setVariable("globalSubscriberId", globalSubscriberId)
        verify(mockExecution).setVariable("prefix", Prefix)
        verify(mockExecution).setVariable("requestDescription", requestDescription)
    }

    @Test
    public void sendSyncResponseTest() {
        println "************ sendSyncResponse ************* "
        ExecutionEntity mockExecution = mock(ExecutionEntity.class)

        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("operationId")).thenReturn("3338b250-e995-4782-8936-081b66ba4dbf")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("56c881ad-6c9d-4b79-aacc-401e5640b47f")

        ScaleCustomE2EServiceInstance scaleCustomE2EServiceInstance = new ScaleCustomE2EServiceInstance()
        scaleCustomE2EServiceInstance.sendSyncResponse(mockExecution)

        verify(mockExecution).setVariable("sentSyncResponse", true)
    }

    @Test
    public void prepareCompletionRequestTest() {
        println "************ prepareCompletionRequest ************* "
        ExecutionEntity mockExecution = mock(ExecutionEntity.class)

        when(mockExecution.getVariable("isDebugLogEnabled")).thenReturn("true")
        when(mockExecution.getVariable("msoRequestId")).thenReturn("56c881ad-6c9d-4b79-aacc-401e5640b47f")
        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("56c881ad-6c9d-4b79-aacc-401e5640b47f")

        ScaleCustomE2EServiceInstance scaleCustomE2EServiceInstance = new ScaleCustomE2EServiceInstance()
        scaleCustomE2EServiceInstance.prepareCompletionRequest(mockExecution)

        verify(mockExecution).setVariable("CompleteMsoProcessRequest", xmlMsoCompletionRequest)

    }

    @Test
    public void prepareInitServiceOperationStatusTest() {
        println "************ prepareInitServiceOperationStatus ************* "
        ExecutionEntity mockExecution = mock(ExecutionEntity.class)

        when(mockExecution.getVariable("serviceInstanceId")).thenReturn("56c881ad-6c9d-4b79-aacc-401e5640b47f")
        when(mockExecution.getVariable("serviceInstanceName")).thenReturn("XXXX")
        when(mockExecution.getVariable("operationId")).thenReturn("0a5b1651-c56e-4263-8c26-c8f8a6ef72d8")

        ScaleCustomE2EServiceInstance scaleCustomE2EServiceInstance = new ScaleCustomE2EServiceInstance()
        scaleCustomE2EServiceInstance.prepareInitServiceOperationStatus(mockExecution)

        payload = utils.formatXml(payload)
        verify(mockExecution).setVariable("CVFMI_updateServiceOperStatusRequest", payload)
    }

}
