/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.so.client.cds;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

public class VnfCDSRequestProviderTest extends AbstractVnfCDSRequestProviderTest {

    @InjectMocks
    private VnfCDSRequestProvider vnfCDSRequestProvider;

    @Mock
    protected ConfigureInstanceParamsForVnf configureInstanceParamsForVnf;

    @Test
    public void testBuildRequestPayloadAssignActionVnf() throws Exception {
        // given
        setScopeAndAction(VNF_SCOPE, ASSIGN_ACTION);
        ServiceInstance instance = createServiceInstance();

        doReturn(instance).when(extractPojosForBB).extractByKey(buildingBlockExecution,
                ResourceKey.SERVICE_INSTANCE_ID);
        doReturn(createGenericVnf()).when(extractPojosForBB).extractByKey(buildingBlockExecution,
                ResourceKey.GENERIC_VNF_ID);
        // when
        vnfCDSRequestProvider.setExecutionObject(buildingBlockExecution);
        String payload = vnfCDSRequestProvider.buildRequestPayload(ASSIGN_ACTION).get();

        // verify
        ObjectMapper mapper = new ObjectMapper();
        JsonNode payloadJson = mapper.readTree(payload);
        JsonNode requestNode = payloadJson.findValue("configAssign-request");
        JsonNode propertiesNode = payloadJson.findValue("configAssign-properties");

        assertNotNull(payload);
        assertTrue(verfiyJsonFromString(payload));
        assertThat(requestNode.get("resolution-key").asText()).isEqualTo(GENERIC_VNF_NAME);
        assertThat(propertiesNode.get("service-instance-id").asText()).isEqualTo(SERVICE_INSTANCE_ID);
        assertThat(propertiesNode.get("service-model-uuid").asText()).isEqualTo(SERVICE_MODEL_UUID);
        assertThat(propertiesNode.get("vnf-id").asText()).isEqualTo(GENERIC_VNF_ID);
        assertThat(propertiesNode.get("vnf-customization-uuid").asText()).isEqualTo(VNF_MODEL_CUSTOMIZATION_UUID);
    }

    @Test
    public void testBuildRequestPayloadDeployActionVnf() throws Exception {
        // given
        setScopeAndAction(VNF_SCOPE, DEPLOY_ACTION);
        ServiceInstance instance = createServiceInstance();

        doReturn(instance).when(extractPojosForBB).extractByKey(buildingBlockExecution,
                ResourceKey.SERVICE_INSTANCE_ID);
        doReturn(createGenericVnf()).when(extractPojosForBB).extractByKey(buildingBlockExecution,
                ResourceKey.GENERIC_VNF_ID);

        // when
        vnfCDSRequestProvider.setExecutionObject(buildingBlockExecution);
        String payload = vnfCDSRequestProvider.buildRequestPayload(DEPLOY_ACTION).get();

        // verify
        ObjectMapper mapper = new ObjectMapper();
        JsonNode payloadJson = mapper.readTree(payload);
        JsonNode requestNode = payloadJson.findValue("configDeploy-request");
        JsonNode propertiesNode = payloadJson.findValue("configDeploy-properties");

        assertNotNull(payload);
        assertTrue(verfiyJsonFromString(payload));
        assertThat(requestNode.get("resolution-key").asText()).isEqualTo(GENERIC_VNF_NAME);
        assertThat(propertiesNode.get("service-instance-id").asText()).isEqualTo(SERVICE_INSTANCE_ID);
        assertThat(propertiesNode.get("service-model-uuid").asText()).isEqualTo(SERVICE_MODEL_UUID);
        assertThat(propertiesNode.get("vnf-id").asText()).isEqualTo(GENERIC_VNF_ID);
        assertThat(propertiesNode.get("vnf-customization-uuid").asText()).isEqualTo(VNF_MODEL_CUSTOMIZATION_UUID);
    }
}
