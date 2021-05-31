/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2021 Bell Canada
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
import org.onap.so.client.exception.PayloadGenerationException;
import org.onap.so.serviceinstancebeans.ModelInfo;
import org.onap.so.serviceinstancebeans.Resources;
import org.onap.so.serviceinstancebeans.Service;
import org.onap.so.serviceinstancebeans.Vnfs;

@RunWith(MockitoJUnitRunner.class)
public class ConfigureInstanceParamsForVnfTest {

    @InjectMocks
    private ConfigureInstanceParamsForVnf configureInstanceParamsForVnf;

    @Mock
    private ExtractServiceFromUserParameters extractServiceFromUserParameters;

    private static final String VNF_1_CUSTOMIZATION_ID = UUID.randomUUID().toString();
    private static final String VNF_2_CUSTOMIZATION_ID = UUID.randomUUID().toString();
    private static final String VNF_1_INSTANCE_NAME = "vnf-instance-1";
    private static final String VNF_2_INSTANCE_NAME = "vnf-instance-2";
    private static final List<Map<String, String>> VNF_1_INSTANCE_PARAMS =
            Arrays.asList(Map.of("param-1", "xyz", "param-2", "123"), Map.of("param-3", "CCC"));
    private static final List<Map<String, String>> VNF_2_INSTANCE_PARAMS =
            Arrays.asList(Map.of("param-1", "abc", "param-2", "999"), Map.of("param-3", "AAA"));


    @Test
    public void testPopulateInstanceParamsByInstanceName() throws Exception {
        Service service = new Service();
        Resources resources = new Resources();
        resources.setVnfs(createVnfs());
        service.setResources(resources);
        when(extractServiceFromUserParameters.getServiceFromRequestUserParams(any())).thenReturn(service);
        JsonObject jsonObject = new JsonObject();

        configureInstanceParamsForVnf.populateInstanceParams(jsonObject, new ArrayList<>(), VNF_2_CUSTOMIZATION_ID,
                VNF_2_INSTANCE_NAME);

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
        configureInstanceParamsForVnf.populateInstanceParams(jsonObject, new ArrayList<>(), VNF_1_CUSTOMIZATION_ID,
                null);

        assertEquals(jsonObject.get("param-1").getAsString(), "xyz");
        assertEquals(jsonObject.get("param-2").getAsString(), "123");
        assertEquals(jsonObject.get("param-3").getAsString(), "CCC");
    }

    private List<Vnfs> createVnfs() {
        Vnfs vnf1 = new Vnfs();
        vnf1.setInstanceName(VNF_1_INSTANCE_NAME);
        ModelInfo modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationId(VNF_1_CUSTOMIZATION_ID);
        vnf1.setModelInfo(modelInfo);
        vnf1.setInstanceParams(VNF_1_INSTANCE_PARAMS);

        Vnfs vnf2 = new Vnfs();
        modelInfo = new ModelInfo();
        modelInfo.setModelCustomizationId(VNF_2_CUSTOMIZATION_ID);
        vnf2.setModelInfo(modelInfo);
        vnf2.setInstanceName(VNF_2_INSTANCE_NAME);
        vnf2.setInstanceParams(VNF_2_INSTANCE_PARAMS);

        return Arrays.asList(vnf1, vnf2);
    }

}
