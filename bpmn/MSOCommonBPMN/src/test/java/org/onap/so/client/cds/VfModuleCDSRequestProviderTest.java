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
import org.mockito.Spy;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class VfModuleCDSRequestProviderTest extends AbstractVnfCDSRequestProviderTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Spy
    protected ExtractServiceFromUserParameters extractServiceFromUserParameters;

    @Spy
    @InjectMocks
    protected ConfigureInstanceParamsForVfModule configureInstanceParamsForVfModule;

    @InjectMocks
    private VfModuleCDSRequestProvider vfModuleCDSRequestProvider;


    @Test
    public void testRequestPayloadForConfigDeployVfModule() throws Exception {
        // given
        setScopeAndAction(VF_SCOPE, DEPLOY_ACTION);
        ServiceInstance serviceInstance = createServiceInstance();

        doReturn(serviceInstance).when(extractPojosForBB).extractByKey(buildingBlockExecution,
                ResourceKey.SERVICE_INSTANCE_ID);
        doReturn(createGenericVnf()).when(extractPojosForBB).extractByKey(buildingBlockExecution,
                ResourceKey.GENERIC_VNF_ID);
        doReturn(createVfModule()).when(extractPojosForBB).extractByKey(buildingBlockExecution,
                ResourceKey.VF_MODULE_ID);
        doReturn(getUserParams()).when(extractServiceFromUserParameters).getServiceFromRequestUserParams(anyList());
        doCallRealMethod().when(configureInstanceParamsForVfModule).populateInstanceParams(any(), any(), anyString(),
                anyString(), any());

        // when
        vfModuleCDSRequestProvider.setExecutionObject(buildingBlockExecution);
        String payload = vfModuleCDSRequestProvider.buildRequestPayload(DEPLOY_ACTION).get();

        // verify
        ObjectMapper mapper = new ObjectMapper();
        JsonNode payloadJson = mapper.readTree(payload);
        JsonNode requestNode = payloadJson.findValue("configDeploy-request");
        JsonNode propertiesNode = payloadJson.findValue("configDeploy-properties");

        assertNotNull(payload);
        assertTrue(verfiyJsonFromString(payload));
        assertThat(requestNode.get("resolution-key").asText()).isEqualTo(VF_MODULE_NAME);
        assertThat(propertiesNode.get("service-instance-id").asText()).isEqualTo(SERVICE_INSTANCE_ID);
        assertThat(propertiesNode.get("vf-module-id").asText()).isEqualTo(VF_MODULE_ID);
        assertThat(propertiesNode.get("vf-module-name").asText()).isEqualTo(VF_MODULE_NAME);
        assertThat(propertiesNode.get("vf-module-customization-uuid").asText()).isEqualTo(VF_MODULE_CUSTOMIZATION_UUID);
        assertThat(propertiesNode.get("service-model-uuid").asText()).isEqualTo(SERVICE_MODEL_UUID);
        assertThat(propertiesNode.get("vnf-id").asText()).isEqualTo(GENERIC_VNF_ID);
    }

    @Test
    public void testRequestPayloadForConfigDeployVfModuleWithoutUserParams() throws Exception {
        // given
        setScopeAndActionWithoutUserParams(VF_SCOPE, DEPLOY_ACTION);
        ServiceInstance serviceInstance = createServiceInstance();

        doReturn(serviceInstance).when(extractPojosForBB).extractByKey(buildingBlockExecution,
                ResourceKey.SERVICE_INSTANCE_ID);
        doReturn(createGenericVnf()).when(extractPojosForBB).extractByKey(buildingBlockExecution,
                ResourceKey.GENERIC_VNF_ID);
        doReturn(createVfModule()).when(extractPojosForBB).extractByKey(buildingBlockExecution,
                ResourceKey.VF_MODULE_ID);
        doReturn(getUserParams()).when(extractServiceFromUserParameters).getServiceFromRequestUserParams(anyList());
        doCallRealMethod().when(configureInstanceParamsForVfModule).populateInstanceParams(any(), any(), anyString(),
                anyString(), any());

        vfModuleCDSRequestProvider.setExecutionObject(buildingBlockExecution);
        String payload = vfModuleCDSRequestProvider.buildRequestPayload(DEPLOY_ACTION).get();

        // verify
        ObjectMapper mapper = new ObjectMapper();
        JsonNode payloadJson = mapper.readTree(payload);
        JsonNode requestNode = payloadJson.findValue("configDeploy-request");
        JsonNode propertiesNode = payloadJson.findValue("configDeploy-properties");

        assertNotNull(payload);
        assertTrue(verfiyJsonFromString(payload));
        assertThat(requestNode.get("resolution-key").asText()).isEqualTo(VF_MODULE_NAME);
        assertThat(propertiesNode.get("service-instance-id").asText()).isEqualTo(SERVICE_INSTANCE_ID);
        assertThat(propertiesNode.get("vf-module-id").asText()).isEqualTo(VF_MODULE_ID);
        assertThat(propertiesNode.get("vf-module-name").asText()).isEqualTo(VF_MODULE_NAME);
        assertThat(propertiesNode.get("vf-module-customization-uuid").asText()).isEqualTo(VF_MODULE_CUSTOMIZATION_UUID);
        assertThat(propertiesNode.get("service-model-uuid").asText()).isEqualTo(SERVICE_MODEL_UUID);
        assertThat(propertiesNode.get("vnf-id").asText()).isEqualTo(GENERIC_VNF_ID);
    }
}
