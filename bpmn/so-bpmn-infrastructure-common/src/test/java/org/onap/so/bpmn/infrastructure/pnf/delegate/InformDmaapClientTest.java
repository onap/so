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

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.onap.so.bpmn.common.recipe.ResourceInput;
import org.onap.so.bpmn.common.resource.ResourceRequestBuilder;

public class InformDmaapClientTest {
    @Before
    public void setUp() {
        informDmaapClient = new InformDmaapClient();
        dmaapClientTest = new DmaapClientTestImpl();
        informDmaapClient.setDmaapClient(dmaapClientTest);
        delegateExecution = mockDelegateExecution();
    }

    private InformDmaapClient informDmaapClient;

    private DmaapClientTestImpl dmaapClientTest;

    private DelegateExecution delegateExecution;

    private MessageCorrelationBuilder messageCorrelationBuilder;

    @Test
    public void shouldSendListenerToDmaapClient() {
        // when
        informDmaapClient.execute(delegateExecution);
        // then
        assertThat(dmaapClientTest.getPnfCorrelationId()).isEqualTo("testPnfCorrelationId");
        assertThat(dmaapClientTest.getInformConsumer()).isNotNull();
        verifyZeroInteractions(messageCorrelationBuilder);
    }

    @Test
    public void shouldSendListenerToDmaapClientAndSendMessageToCamunda() {
        // when
        informDmaapClient.execute(delegateExecution);
        dmaapClientTest.getInformConsumer().run();
        // then
        assertThat(dmaapClientTest.getPnfCorrelationId()).isEqualTo("testPnfCorrelationId");
        InOrder inOrder = inOrder(messageCorrelationBuilder);
        inOrder.verify(messageCorrelationBuilder).processInstanceBusinessKey("testBusinessKey");
        inOrder.verify(messageCorrelationBuilder).correlateWithResult();
    }

    private ResourceInput getUpdateResInputObj(String modelName) {

        String resourceInput = "{\n" + "\t\"resourceInstanceName\": \"SotnFc-wan-connection_wanconnection-37\",\n"
                + "\t\"resourceInstanceDes\": null,\n" + "\t\"globalSubscriberId\": \"sdwandemo\",\n"
                + "\t\"serviceType\": \"CCVPN\",\n" + "\t\"operationId\": \"df3387b5-4fbf-41bd-82a0-13a955ac178a\",\n"
                + "\t\"serviceModelInfo\": {\n" + "\t\t\"modelName\": \"WanConnectionSvc03\",\n"
                + "\t\t\"modelUuid\": \"198b066c-0771-4157-9594-1824adfdda7e\",\n"
                + "\t\t\"modelInvariantUuid\": \"43fb5165-7d03-4009-8951-a8f45d3f0148\",\n"
                + "\t\t\"modelVersion\": \"1.0\",\n" + "\t\t\"modelCustomizationUuid\": \"\",\n"
                + "\t\t\"modelCustomizationName\": \"\",\n" + "\t\t\"modelInstanceName\": \"\",\n"
                + "\t\t\"modelType\": \"\"\n" + "\t},\n" + "\t\"resourceModelInfo\": {\n" + "\t\t\"modelName\": \""
                + modelName + "\",\n" + "\t\t\"modelUuid\": \"6a0bf88b-343c-415b-88c1-6f73702452c4\",\n"
                + "\t\t\"modelInvariantUuid\": \"50bc3415-2e01-4e50-a9e1-ec9584599bb3\",\n"
                + "\t\t\"modelCustomizationUuid\": \"b205d620-84bd-4058-afa0-e3aeee8bb712\",\n"
                + "\t\t\"modelCustomizationName\": \"\",\n"
                + "\t\t\"modelInstanceName\": \"SotnFc-wan-connection 0\",\n" + "\t\t\"modelType\": \"\"\n" + "\t},\n"
                + "\t\"resourceInstancenUuid\": null,\n"
                + "\t\"resourceParameters\": \"{\\n\\\"locationConstraints\\\":[],\\n\\\"requestInputs\\\":{\\\"sotnfcspecwanconnection0_route-objective-function\\\":null,\\\"sotnfcspecwanconnection0_colorAware\\\":null,\\\"3rdctlspecwanconnection0_thirdPartyAdaptorRpc\\\":null,\\\"sotnfcspecwanconnection0_couplingFlag\\\":null,\\\"sotnfcspecwanconnection0_pbs\\\":null,\\\"3rdctlspecwanconnection0_thirdPartySdncId\\\":null,\\\"sotnfcspecwanconnection0_cbs\\\":null,\\\"3rdctlspecwanconnection0_thirdpartySdncName\\\":null,\\\"sotnfcspecwanconnection0_total-size\\\":null,\\\"3rdctlspecwanconnection0_templateFileName\\\":\\\"sotn_create_zte_template.json\\\",\\\"fcwanconnection0_type\\\":null,\\\"sotnfcspecwanconnection0_cir\\\":null,\\\"fcwanconnection0_uuid\\\":null,\\\"sotnfcspecwanconnection0_diversity-policy\\\":null,\\\"nf_naming\\\":true,\\\"multi_stage_design\\\":false,\\\"availability_zone_max_count\\\":1,\\\"3rdctlspecwanconnection0_restapiUrl\\\":\\\"http://10.80.80.21:8443/restconf/operations/ZTE-API-ConnectivityService:create-connectivity-service\\\",\\\"max_instances\\\":null,\\\"sotnfcspecwanconnection0_reroute\\\":null,\\\"fcwanconnection0_name\\\":null,\\\"sotnfcspecwanconnection0_dualLink\\\":null,\\\"min_instances\\\":null,\\\"sotnfcspecwanconnection0_pir\\\":null,\\\"sotnfcspecwanconnection0_service-type\\\":null}\\n}\",\n"
                + "\t\"operationType\": \"createInstance\",\n"
                + "\t\"serviceInstanceId\": \"ffa07ae4-f820-45af-9439-1416b3bc1d39\",\n"
                + "\t\"requestsInputs\": \"{\\r\\n\\t\\\"service\\\": {\\r\\n\\t\\t\\\"name\\\": \\\"wanconnection-37\\\",\\r\\n\\t\\t\\\"description\\\": \\\"deafe\\\",\\r\\n\\t\\t\\\"serviceInvariantUuid\\\": \\\"43fb5165-7d03-4009-8951-a8f45d3f0148\\\",\\r\\n\\t\\t\\\"serviceUuid\\\": \\\"198b066c-0771-4157-9594-1824adfdda7e\\\",\\r\\n\\t\\t\\\"globalSubscriberId\\\": \\\"sdwandemo\\\",\\r\\n\\t\\t\\\"serviceType\\\": \\\"CCVPN\\\",\\r\\n\\t\\t\\\"parameters\\\": {\\r\\n\\t\\t\\t\\\"resources\\\": [\\r\\n\\t\\t\\t],\\r\\n\\t\\t\\t\\\"requestInputs\\\": {\\r\\n\\t\\t\\t\\t\\\"sotnfcwanconnection0_3rdctlspecwanconnection0_restapiUrl\\\": \\\"http://10.80.80.21:8443/restconf/operations/ZTE-API-ConnectivityService:create-connectivity-service\\\",\\r\\n\\t\\t\\t\\t\\\"sotnfcwanconnection0_3rdctlspecwanconnection0_templateFileName\\\": \\\"sotn_create_zte_template.json\\\",\\r\\n\\t\\t\\t\\t\\\"sdwanfcwanconnection0_3rdctlspecwanconnection0_restapiUrl\\\": \\\"http://10.80.80.21:8443/restconf/operations/ZTE-API-ConnectivityService:create-connectivity-service\\\",\\r\\n\\t\\t\\t\\t\\\"sdwanfcwanconnection0_3rdctlspecwanconnection0_templateFileName\\\": \\\"sdwan_create_zte_template.json\\\",\\\"ont_ont_manufacturer\\\":\\\"huawei\\\",\\\"ont_ont_serial_num\\\":\\\"123\\\"\\r\\n\\t\\t\\t}\\r\\n\\t\\t}\\r\\n\\t}\\r\\n}\"\n"
                + "}";

        ResourceInput resourceInputObj = ResourceRequestBuilder.getJsonObject(resourceInput, ResourceInput.class);
        return resourceInputObj;
    }

    private DelegateExecution mockDelegateExecution() {
        ResourceInput input = getUpdateResInputObj("OLT");
        DelegateExecution delegateExecution = mock(DelegateExecution.class);

        when(delegateExecution.getVariable(eq(ExecutionVariableNames.PNF_CORRELATION_ID)))
                .thenReturn("testPnfCorrelationId");
        when(delegateExecution.getProcessBusinessKey()).thenReturn("testBusinessKey");
        when(delegateExecution.getVariable("resourceInput")).thenReturn(input.toString());
        ProcessEngineServices processEngineServices = mock(ProcessEngineServices.class);
        when(delegateExecution.getProcessEngineServices()).thenReturn(processEngineServices);
        RuntimeService runtimeService = mock(RuntimeService.class);
        when(processEngineServices.getRuntimeService()).thenReturn(runtimeService);
        messageCorrelationBuilder = mock(MessageCorrelationBuilder.class);
        when(runtimeService.createMessageCorrelation(any())).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.processInstanceBusinessKey(any())).thenReturn(messageCorrelationBuilder);

        return delegateExecution;
    }
}
