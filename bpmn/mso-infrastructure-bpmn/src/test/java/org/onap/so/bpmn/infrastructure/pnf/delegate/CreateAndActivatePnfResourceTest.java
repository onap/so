/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright 2018 Nokia
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

package org.onap.so.bpmn.infrastructure.pnf.delegate;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_CORRELATION_ID;
import static org.onap.so.bpmn.infrastructure.pnf.delegate.ExecutionVariableNames.PNF_UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.BaseIntegrationTest;
import org.onap.so.bpmn.common.recipe.ResourceInput;
import org.onap.so.bpmn.common.resource.ResourceRequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;


@TestPropertySource(properties = {"pnf.kafka.topicListenerDelayInSeconds=3",
        "pnf.kafka.kafkaBootstrapServers=localhost:9092", "pnf.kafka.pnfReadyTopicName=someTopic",
        "pnf.kafka.pnfUpdateTopicName=someTopic2", "pnf.kafka.consumerGroup=someConsumerGroup",
        "pnf.kafka.consumerId=someConsumerId", "pnf.kafka.consumerIdUpdate=someConsumerIdUpdate"})
public class CreateAndActivatePnfResourceTest extends BaseIntegrationTest {

    private static final String VALID_UUID = UUID.nameUUIDFromBytes("testUuid".getBytes()).toString();
    private static final String SERVICE_INSTANCE_ID = "serviceForInstance";

    private Map<String, Object> variables;

    @Autowired
    private PnfManagementTestImpl pnfManagementTest;

    @Autowired
    private KafkaClientTestImpl kafkaClientTestImpl;

    @Before
    public void setup() {
        pnfManagementTest.reset();
        variables = new HashMap<>();
        variables.put("serviceInstanceId", SERVICE_INSTANCE_ID);
        variables.put(PNF_UUID, VALID_UUID);
    }

    @Test
    public void shouldWaitForMessageFromKafkaAndUpdateAaiEntryWhenAaiEntryExists() {
        // given
        variables.put(PNF_CORRELATION_ID, PnfManagementTestImpl.ID_WITH_ENTRY);
        ResourceInput ri = getUpdateResInputObj("OLT");
        if (ri != null) {
            variables.put("resourceInput", ri.toString());
        } else {
            variables.put("resourceInput", null);
        }
        // when
        ProcessInstance instance =
                runtimeService.startProcessInstanceByKey("CreateAndActivatePnfResource", "businessKey", variables);
        assertThat(instance).isWaitingAt("WaitForKafkaPnfReadyNotification").isWaitingFor("WorkflowMessage");
        kafkaClientTestImpl.sendMessage();

        // then
        assertThat(instance).isEnded().hasPassedInOrder("CreateAndActivatePnf_StartEvent", "CheckInputs",
                "CheckAiiForPnfCorrelationId", "DoesAaiContainInfoAboutPnf", "AaiEntryExists", "InformKafkaClient",
                "WaitForKafkaPnfReadyNotification", "CreateRelationId", "AaiEntryUpdated");
        Assertions.assertThat(pnfManagementTest.getServiceAndPnfRelationMap())
                .containsOnly(MapEntry.entry(SERVICE_INSTANCE_ID, PnfManagementTestImpl.ID_WITH_ENTRY));
    }

    @Test
    public void shouldCreateAaiEntryWaitForMessageFromKafkaAndUpdateAaiEntryWhenNoAaiEntryExists() {
        // given
        variables.put(PNF_CORRELATION_ID, PnfManagementTestImpl.ID_WITHOUT_ENTRY);
        ResourceInput ri = getUpdateResInputObj("OLT");
        if (ri != null) {
            variables.put("resourceInput", ri.toString());
        } else {
            variables.put("resourceInput", null);
        }
        // when
        ProcessInstance instance =
                runtimeService.startProcessInstanceByKey("CreateAndActivatePnfResource", "businessKey", variables);
        assertThat(instance).isWaitingAt("WaitForKafkaPnfReadyNotification").isWaitingFor("WorkflowMessage");
        kafkaClientTestImpl.sendMessage();

        // then
        assertThat(instance).isEnded().hasPassedInOrder("CreateAndActivatePnf_StartEvent", "CheckInputs",
                "CheckAiiForPnfCorrelationId", "DoesAaiContainInfoAboutPnf", "CreatePnfEntryInAai", "AaiEntryExists",
                "InformKafkaClient", "WaitForKafkaPnfReadyNotification", "CreateRelationId", "AaiEntryUpdated");
        Assertions.assertThat(pnfManagementTest.getCreated()).containsOnlyKeys(PnfManagementTestImpl.ID_WITHOUT_ENTRY);
        Assertions.assertThat(pnfManagementTest.getServiceAndPnfRelationMap())
                .containsOnly(MapEntry.entry(SERVICE_INSTANCE_ID, PnfManagementTestImpl.ID_WITHOUT_ENTRY));
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
}
*/
