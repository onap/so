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
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.bpmn.servicedecomposition.bbobjects.ServiceInstance;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.Resources;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.VfModules;
import org.onap.so.serviceinstancebeans.Vnfs;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
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

    @RunWith(MockitoJUnitRunner.class)
    public static class ConfigureInstanceParamsForVfModuleTest {

        @InjectMocks
        private ConfigureInstanceParamsForVfModule configureInstanceParamsForVfModule;

        @Mock
        private ExtractServiceFromUserParameters extractServiceFromUserParameters;

        private static final String VNF_CUSTOMIZATION_ID = UUID.randomUUID().toString();
        private static final String VFMODULE_1_CUSTOMIZATION_ID = UUID.randomUUID().toString();
        private static final String VFMODULE_2_CUSTOMIZATION_ID = UUID.randomUUID().toString();
        private static final String VFMODULE_1_INSTANCE_NAME = "vfmodule-instance-1";
        private static final String VFMODULE_2_INSTANCE_NAME = "vfmodule-instance-2";
        private static final List<Map<String, String>> VFMODULE_1_INSTANCE_PARAMS =
                Arrays.asList(Map.of("param-1", "xyz", "param-2", "123"), Map.of("param-3", "CCC"));
        private static final List<Map<String, String>> VFMODULE_2_INSTANCE_PARAMS =
                Arrays.asList(Map.of("param-1", "abc", "param-2", "999"), Map.of("param-3", "AAA"));


        @Test
        public void testPopulateInstanceParamsByInstanceName() throws Exception {
            Service service = new Service();
            Resources resources = new Resources();
            resources.setVnfs(createVnfs());
            service.setResources(resources);

            when(extractServiceFromUserParameters.getServiceFromRequestUserParams(any())).thenReturn(service);
            JsonObject jsonObject = new JsonObject();

            configureInstanceParamsForVfModule.populateInstanceParams(jsonObject, new ArrayList<>(),
                    VNF_CUSTOMIZATION_ID, VFMODULE_2_CUSTOMIZATION_ID, VFMODULE_2_INSTANCE_NAME);

            assertEquals(jsonObject.get("param-1").getAsString(), "abc");
            assertEquals(jsonObject.get("param-2").getAsString(), "999");
            assertEquals(jsonObject.get("param-3").getAsString(), "AAA");
        }

        @Test
        public void testPopulateInstanceParamsByCustomizationId() throws Exception {
            Service service = new Service();
            Resources resources = new Resources();
            resources.setVnfs(createVnfs());
            service.setResources(resources);

            when(extractServiceFromUserParameters.getServiceFromRequestUserParams(any())).thenReturn(service);
            JsonObject jsonObject = new JsonObject();

            // No instance name is passed
            configureInstanceParamsForVfModule.populateInstanceParams(jsonObject, new ArrayList<>(),
                    VNF_CUSTOMIZATION_ID, VFMODULE_1_CUSTOMIZATION_ID, null);

            assertEquals(jsonObject.get("param-1").getAsString(), "xyz");
            assertEquals(jsonObject.get("param-2").getAsString(), "123");
            assertEquals(jsonObject.get("param-3").getAsString(), "CCC");
        }

        private List<Vnfs> createVnfs() {
            Vnfs vnf1 = new Vnfs();
            ModelInfo modelInfo = new ModelInfo();
            modelInfo.setModelCustomizationId(VNF_CUSTOMIZATION_ID);
            vnf1.setModelInfo(modelInfo);

            VfModules vfModule1 = new VfModules();
            modelInfo = new ModelInfo();
            modelInfo.setModelCustomizationId(VFMODULE_1_CUSTOMIZATION_ID);
            vfModule1.setModelInfo(modelInfo);
            vfModule1.setInstanceName(VFMODULE_1_INSTANCE_NAME);
            vfModule1.setInstanceParams(VFMODULE_1_INSTANCE_PARAMS);

            VfModules vfModule2 = new VfModules();
            modelInfo = new ModelInfo();
            modelInfo.setModelCustomizationId(VFMODULE_2_CUSTOMIZATION_ID);
            vfModule2.setModelInfo(modelInfo);
            vfModule2.setInstanceName(VFMODULE_2_INSTANCE_NAME);
            vfModule2.setInstanceParams(VFMODULE_2_INSTANCE_PARAMS);

            vnf1.setVfModules(Arrays.asList(vfModule1, vfModule2));

            return Arrays.asList(vnf1);
        }

    }
}
