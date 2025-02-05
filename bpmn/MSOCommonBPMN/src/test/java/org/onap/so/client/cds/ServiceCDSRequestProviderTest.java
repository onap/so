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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ServiceCDSRequestProviderTest extends AbstractVnfCDSRequestProviderTest {

    @InjectMocks
    private ServiceCDSRequestProvider serviceCDSRequestProvider;

    @Mock
    private ConfigureInstanceParamsForService configureInstanceParamsForService;

    @Test
    public void testRequestPayloadForCreateService() throws Exception {
        // given
        setScopeAndAction(SERVICE_SCOPE, SERVICE_ACTION);
        ServiceInstance instance = createServiceInstance();
        buildingBlockExecution.getGeneralBuildingBlock().setServiceInstance(instance);

        // when
        serviceCDSRequestProvider.setExecutionObject(buildingBlockExecution);
        String payload = serviceCDSRequestProvider.buildRequestPayload(SERVICE_ACTION).get();

        // verify
        ObjectMapper mapper = new ObjectMapper();
        JsonNode payloadJson = mapper.readTree(payload);
        JsonNode requestNode = payloadJson.findValue("create-request");
        JsonNode propertiesNode = payloadJson.findValue("create-properties");

        assertNotNull(payload);
        assertTrue(verfiyJsonFromString(payload));
        assertThat(requestNode.get("resolution-key").asText()).isEqualTo(SERVICE_INSTANCE_NAME);
        assertThat(propertiesNode.get("service-instance-id").asText()).isEqualTo(SERVICE_INSTANCE_ID);
        assertThat(propertiesNode.get("service-model-uuid").asText()).isEqualTo(SERVICE_MODEL_UUID);
    }
}
