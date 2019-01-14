/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

import com.github.tomakehurst.wiremock.junit.WireMockRule
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.onap.so.bpmn.common.recipe.ResourceInput
import org.onap.so.bpmn.common.resource.ResourceRequestBuilder

import static org.mockito.Mockito.*
/**
 * Copyright 2018 ZTE Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class CreateSDNCNetworkResourceTest extends GroovyTestCase {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8090)

    String Prefix = "CRESDNCRES_"
    String sdncAdapterWorkflowResponse

    void init() {
        MockitoAnnotations.initMocks(this)
        sdncAdapterWorkflowResponse = """
						<sdncadapterworkflow:SDNCAdapterWorkflowResponse xmlns:sdncadapterworkflow="http://org.onap/so/workflow/schema/v1">
						<sdncadapterworkflow:response-data>
                            <CallbackHeader>
                                <RequestId>726420e0-3962-4bf2-9655-aac82fc7055e</RequestId>
                                <ResponseCode>200</ResponseCode>
                                <ResponseMessage>OK</ResponseMessage>
                            </CallbackHeader>
                            <RequestData xsi:type="xs:string" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">&lt;output xmlns="org:onap:sdnc:northbound:generic-resource"&gt;&lt;response-message&gt;&lt;/response-message&gt;&lt;ack-final-indicator&gt;Y&lt;/ack-final-indicator&gt;&lt;svc-request-id&gt;726420e0-3962-4bf2-9655-aac82fc7055e&lt;/svc-request-id&gt;&lt;network-response-information&gt;&lt;instance-id&gt;9fa732b8-2c54-4eba-926d-464d18c07474&lt;/instance-id&gt;&lt;object-path&gt;restconf/config/GENERIC-RESOURCE-API:services/service/a99deffb-12e2-4656-8cf2-2d2a996d0f52/service-data/networks/network/9fa732b8-2c54-4eba-926d-464d18c07474/network-data/&lt;/object-path&gt;&lt;/network-response-information&gt;&lt;response-code&gt;200&lt;/response-code&gt;&lt;service-response-information&gt;&lt;instance-id&gt;a99deffb-12e2-4656-8cf2-2d2a996d0f52&lt;/instance-id&gt;&lt;/service-response-information&gt;&lt;/output&gt;</RequestData>
                        </sdncadapterworkflow:response-data>
						</sdncadapterworkflow:SDNCAdapterWorkflowResponse>"""

    }

    private ResourceInput getResInputObj(String modelName) {
        String resourceInput = "{\n" +
                "\t\"resourceInstanceName\": \"SotnFc-wan-connection_wanconnection-37\",\n" +
                "\t\"resourceInstanceDes\": null,\n" +
                "\t\"globalSubscriberId\": \"sdwandemo\",\n" +
                "\t\"serviceType\": \"CCVPN\",\n" +
                "\t\"operationId\": \"df3387b5-4fbf-41bd-82a0-13a955ac178a\",\n" +
                "\t\"serviceModelInfo\": {\n" +
                "\t\t\"modelName\": \"WanConnectionSvc03\",\n" +
                "\t\t\"modelUuid\": \"198b066c-0771-4157-9594-1824adfdda7e\",\n" +
                "\t\t\"modelInvariantUuid\": \"43fb5165-7d03-4009-8951-a8f45d3f0148\",\n" +
                "\t\t\"modelVersion\": \"1.0\",\n" +
                "\t\t\"modelCustomizationUuid\": \"\",\n" +
                "\t\t\"modelCustomizationName\": \"\",\n" +
                "\t\t\"modelInstanceName\": \"\",\n" +
                "\t\t\"modelType\": \"\"\n" +
                "\t},\n" +
                "\t\"resourceModelInfo\": {\n" +
                "\t\t\"modelName\": \"" +
                modelName +
                "\",\n" +
                "\t\t\"modelUuid\": \"6a0bf88b-343c-415b-88c1-6f73702452c4\",\n" +
                "\t\t\"modelInvariantUuid\": \"50bc3415-2e01-4e50-a9e1-ec9584599bb3\",\n" +
                "\t\t\"modelCustomizationUuid\": \"b205d620-84bd-4058-afa0-e3aeee8bb712\",\n" +
                "\t\t\"modelCustomizationName\": \"\",\n" +
                "\t\t\"modelInstanceName\": \"SotnFc-wan-connection 0\",\n" +
                "\t\t\"modelType\": \"\"\n" +
                "\t},\n" +
                "\t\"resourceInstancenUuid\": null,\n" +
                "\t\"resourceParameters\": \"{\\n\\\"locationConstraints\\\":[],\\n\\\"requestInputs\\\":{\\\"sotnfcspecwanconnection0_route-objective-function\\\":null,\\\"sotnfcspecwanconnection0_colorAware\\\":null,\\\"3rdctlspecwanconnection0_thirdPartyAdaptorRpc\\\":null,\\\"sotnfcspecwanconnection0_couplingFlag\\\":null,\\\"sotnfcspecwanconnection0_pbs\\\":null,\\\"3rdctlspecwanconnection0_thirdPartySdncId\\\":null,\\\"sotnfcspecwanconnection0_cbs\\\":null,\\\"3rdctlspecwanconnection0_thirdpartySdncName\\\":null,\\\"sotnfcspecwanconnection0_total-size\\\":null,\\\"3rdctlspecwanconnection0_templateFileName\\\":\\\"sotn_create_zte_template.json\\\",\\\"fcwanconnection0_type\\\":null,\\\"sotnfcspecwanconnection0_cir\\\":null,\\\"fcwanconnection0_uuid\\\":null,\\\"sotnfcspecwanconnection0_diversity-policy\\\":null,\\\"nf_naming\\\":true,\\\"multi_stage_design\\\":false,\\\"availability_zone_max_count\\\":1,\\\"3rdctlspecwanconnection0_restapiUrl\\\":\\\"http://10.80.80.21:8443/restconf/operations/ZTE-API-ConnectivityService:create-connectivity-service\\\",\\\"max_instances\\\":null,\\\"sotnfcspecwanconnection0_reroute\\\":null,\\\"fcwanconnection0_name\\\":null,\\\"sotnfcspecwanconnection0_dualLink\\\":null,\\\"min_instances\\\":null,\\\"sotnfcspecwanconnection0_pir\\\":null,\\\"sotnfcspecwanconnection0_service-type\\\":null}\\n}\",\n" +
                "\t\"operationType\": \"createInstance\",\n" +
                "\t\"serviceInstanceId\": \"ffa07ae4-f820-45af-9439-1416b3bc1d39\",\n" +
                "\t\"requestsInputs\": \"{\\r\\n\\t\\\"service\\\": {\\r\\n\\t\\t\\\"name\\\": \\\"wanconnection-37\\\",\\r\\n\\t\\t\\\"description\\\": \\\"deafe\\\",\\r\\n\\t\\t\\\"serviceInvariantUuid\\\": \\\"43fb5165-7d03-4009-8951-a8f45d3f0148\\\",\\r\\n\\t\\t\\\"serviceUuid\\\": \\\"198b066c-0771-4157-9594-1824adfdda7e\\\",\\r\\n\\t\\t\\\"globalSubscriberId\\\": \\\"sdwandemo\\\",\\r\\n\\t\\t\\\"serviceType\\\": \\\"CCVPN\\\",\\r\\n\\t\\t\\\"parameters\\\": {\\r\\n\\t\\t\\t\\\"resources\\\": [\\r\\n\\t\\t\\t],\\r\\n\\t\\t\\t\\\"requestInputs\\\": {\\r\\n\\t\\t\\t\\t\\\"sotnfcwanconnection0_3rdctlspecwanconnection0_restapiUrl\\\": \\\"http://10.80.80.21:8443/restconf/operations/ZTE-API-ConnectivityService:create-connectivity-service\\\",\\r\\n\\t\\t\\t\\t\\\"sotnfcwanconnection0_3rdctlspecwanconnection0_templateFileName\\\": \\\"sotn_create_zte_template.json\\\",\\r\\n\\t\\t\\t\\t\\\"sdwanfcwanconnection0_3rdctlspecwanconnection0_restapiUrl\\\": \\\"http://10.80.80.21:8443/restconf/operations/ZTE-API-ConnectivityService:create-connectivity-service\\\",\\r\\n\\t\\t\\t\\t\\\"sdwanfcwanconnection0_3rdctlspecwanconnection0_templateFileName\\\": \\\"sdwan_create_zte_template.json\\\"\\r\\n\\t\\t\\t}\\r\\n\\t\\t}\\r\\n\\t}\\r\\n}\"\n" +
                "}"
        ResourceInput resourceInputObj = ResourceRequestBuilder.getJsonObject(resourceInput, ResourceInput.class)
        return resourceInputObj
    }

    @Test
    void testAfterCreateSDNCCall() {
        init()
        checkGetInstanceId(getResInputObj("SotnFc-wan-connection"))
        checkGetInstanceId(getResInputObj("SOTNConnectivity"))
        checkGetInstanceId(getResInputObj("SDWANConnectivity"))
    }

    private void checkGetInstanceId(ResourceInput input) {
        ExecutionEntity mockExecution = mock(ExecutionEntity.class)
        when(mockExecution.getVariable(Prefix + "sdncCreateReturnCode")).thenReturn("200")
        when(mockExecution.getVariable(Prefix + "SuccessIndicator")).thenReturn("false")
        when(mockExecution.getVariable("isActivateRequired")).thenReturn("true")
        when(mockExecution.getVariable("CRENWKI_createSDNCResponse")).thenReturn(sdncAdapterWorkflowResponse)
        when(mockExecution.getVariable(Prefix + "resourceInput")).thenReturn(input.toString())
        CreateSDNCNetworkResource createSDNCNetworkResource = new CreateSDNCNetworkResource()
        createSDNCNetworkResource.afterCreateSDNCCall(mockExecution)
        def instanceId = getInstanceId()
        verify(mockExecution).setVariable("networkInstanceId", instanceId)
    }

    private getInstanceId() {
        def response = new XmlSlurper().parseText(sdncAdapterWorkflowResponse)
        def data = response.toString()
        data = data.substring(data.indexOf("<"))
        def resp = new XmlSlurper().parseText(data)
        def instanceId = resp."network-response-information"."instance-id"
        return instanceId
    }
}
