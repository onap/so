/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Bell Canada
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

package org.onap.so.client.cds;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.Resources;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.VfModules;
import org.onap.so.serviceinstancebeans.Vnfs;

@RunWith(MockitoJUnitRunner.class)
public class ConfigureInstanceParamsForVfModuleTest {

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

        when(extractServiceFromUserParameters.getServiceFromRequestUserParams(any())).thenReturn(Optional.of(service));
        JsonObject jsonObject = new JsonObject();

        configureInstanceParamsForVfModule.populateInstanceParams(jsonObject, new ArrayList<>(), VNF_CUSTOMIZATION_ID,
                VFMODULE_2_CUSTOMIZATION_ID, VFMODULE_2_INSTANCE_NAME);

        assertEquals("abc", jsonObject.get("param-1").getAsString());
        assertEquals("999", jsonObject.get("param-2").getAsString());
        assertEquals("AAA", jsonObject.get("param-3").getAsString());
    }

    @Test
    public void testPopulateInstanceParamsByCustomizationId() throws Exception {
        Service service = new Service();
        Resources resources = new Resources();
        resources.setVnfs(createVnfs());
        service.setResources(resources);

        when(extractServiceFromUserParameters.getServiceFromRequestUserParams(any())).thenReturn(Optional.of(service));
        JsonObject jsonObject = new JsonObject();

        // No instance name is passed
        configureInstanceParamsForVfModule.populateInstanceParams(jsonObject, new ArrayList<>(), VNF_CUSTOMIZATION_ID,
                VFMODULE_1_CUSTOMIZATION_ID, null);

        assertEquals("xyz", jsonObject.get("param-1").getAsString());
        assertEquals("123", jsonObject.get("param-2").getAsString());
        assertEquals("CCC", jsonObject.get("param-3").getAsString());
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
