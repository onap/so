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

import com.google.gson.JsonObject;
import org.junit.Test;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.Resources;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.VfModules;
import org.onap.so.serviceinstancebeans.Vnfs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;

public class ConfigureInstanceParamsForVfModuleTest {

    private static final String TEST_VNF_MODEL_CUSTOMIZATION_UUID = "23ce9ac4-e5dd-11e9-81b4-2a2ae2dbcce4";
    private static final String TEST_VF_MODULE_CUSTOMIZATION_UUID = "23ce9ac4-e5dd-11e9-81b4-2a2ae2dbcce2";
    private static final String TEST_INSTANCE_PARAM_VALUE_1 = "vf-module-1-value";
    private static final String TEST_INSTANCE_PARAM_VALUE_2 = "vf-module-2-value";
    private static final String TEST_INSTANCE_PARAM_KEY_1 = "instance-param-1";
    private static final String TEST_INSTANCE_PARAM_KEY_2 = "instance-param-2";

    @Test
    public void testInstanceParamsForVfModule() throws Exception {
        // given
        List<Map<String, Object>> userParamsFromRequest = createRequestParameters();
        JsonObject jsonObject = new JsonObject();
        ConfigureInstanceParamsForVfModule configureInstanceParamsForVfModule =
                new ConfigureInstanceParamsForVfModule();

        // when
        configureInstanceParamsForVfModule.populateInstanceParams(jsonObject, userParamsFromRequest,
                TEST_VNF_MODEL_CUSTOMIZATION_UUID, TEST_VF_MODULE_CUSTOMIZATION_UUID);

        // verify
        assertEquals(TEST_INSTANCE_PARAM_VALUE_1, jsonObject.get(TEST_INSTANCE_PARAM_KEY_1).getAsString());
        assertEquals(TEST_INSTANCE_PARAM_VALUE_2, jsonObject.get(TEST_INSTANCE_PARAM_KEY_2).getAsString());
    }

    private List<Map<String, Object>> createRequestParameters() {
        List<Map<String, Object>> userParams = new ArrayList<>();
        Map<String, Object> userParamMap = new HashMap<>();
        userParamMap.put("service", getUserParams());
        userParams.add(userParamMap);
        return userParams;
    }

    private Service getUserParams() {
        Service service = new Service();
        Resources resources = new Resources();
        resources.setVnfs(createVnfs());
        service.setResources(resources);
        return service;
    }

    private List<Vnfs> createVnfs() {
        Vnfs searchedVnf = createVnf();
        List<Vnfs> vnfList = new ArrayList<>();
        vnfList.add(searchedVnf);
        return vnfList;
    }

    private Vnfs createVnf() {
        Vnfs vnf = new Vnfs();
        ModelInfo modelInfoForVnf = new ModelInfo();
        modelInfoForVnf.setModelCustomizationId(TEST_VNF_MODEL_CUSTOMIZATION_UUID);
        vnf.setModelInfo(modelInfoForVnf);

        VfModules vfModule = new VfModules();

        ModelInfo modelInfoForVfModule = new ModelInfo();
        modelInfoForVfModule.setModelCustomizationId(TEST_VF_MODULE_CUSTOMIZATION_UUID);

        vfModule.setModelInfo(modelInfoForVfModule);

        // Set instance parameters.
        List<Map<String, String>> instanceParamsListSearchedVfModule = new ArrayList<>();
        Map<String, String> instanceParams = new HashMap<>();
        instanceParams.put("instance-param-1", TEST_INSTANCE_PARAM_VALUE_1);
        instanceParams.put("instance-param-2", TEST_INSTANCE_PARAM_VALUE_2);

        instanceParamsListSearchedVfModule.add(instanceParams);
        vfModule.setInstanceParams(instanceParamsListSearchedVfModule);

        List<VfModules> vfModules = new ArrayList<>();
        vfModules.add(vfModule);

        vnf.setVfModules(vfModules);

        return vnf;
    }
}
