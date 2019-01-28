/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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
package org.onap.so.bpmn.common.resource;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.so.BaseTest;
import org.onap.so.bpmn.core.UrnPropertiesReader;
import org.springframework.core.env.Environment;

import static com.github.tomakehurst.wiremock.client.WireMock.*;


public class ResourceRequestBuilderTest extends BaseTest {

    @Test
    public void getResourceInputTest() throws Exception {



        stubFor(get(urlEqualTo("/ecomp/mso/catalog/v2/serviceResources?serviceModelUuid=c3954379-4efe-431c-8258-f84905b158e5"))
                .willReturn(ok("{ \"serviceResources\"    : {\n" +
                        "\t\"modelInfo\"       : {\n" +
                        "\t\t\"modelName\"          : \"demoVFWCL\",\n" +
                        "\t\t\"modelUuid\"          : \"c3954379-4efe-431c-8258-f84905b158e5\",\n" +
                        "\t\t\"modelInvariantUuid\" : \"0cbff61e-3b0a-4eed-97ce-b1b4faa03493\",\n" +
                        "\t\t\"modelVersion\"       : \"1.0\"\n" +
                        "\t},\n" +
                        "\t\"serviceType\"        : \"\",\n" +
                        "\t\"serviceRole\"        : \"\",\n" +
                        "\t\"environmentContext\" : null,\n" +
                        "\t\"resourceOrder\"       : \"res1,res2\",\n" +
                        "\t\"workloadContext\"    : \"Production\",\n" +
                        "\t\"serviceVnfs\": [\n" +
                        "\t\n" +
                        "\t\t{ \"modelInfo\"                    : {\n" +
                        "\t\t\t\"modelName\"              : \"15968a6e-2fe5-41bf-a481\",\n" +
                        "\t\t\t\"modelUuid\"              : \"808abda3-2023-4105-92d2-e62644b61d53\",\n" +
                        "\t\t\t\"modelInvariantUuid\"     : \"6e4ffc7c-497e-4a77-970d-af966e642d31\",\n" +
                        "\t\t\t\"modelVersion\"           : \"1.0\",\n" +
                        "\t\t\t\"modelCustomizationUuid\" : \"a00404d5-d7eb-4c46-b6b6-9cf2d087e545\",\n" +
                        "\t\t\t\"modelInstanceName\"      : \"15968a6e-2fe5-41bf-a481 0\"\n" +
                        "\t\t\t},\n" +
                        "\t\t\"toscaNodeType\"            : \"org.openecomp.resource.vf.15968a6e2fe541bfA481\",\n" +
                        "\t\t\"nfFunction\"           \t: null,\n" +
                        "\"resourceInput\":\"{\\\"a\\\":\\\"b\\\"}\"," +
                        "\t\t\"nfType\"              \t\t: null,\n" +
                        "\t\t\"nfRole\"              \t\t: null,\n" +
                        "\t\t\"nfNamingCode\"         \t: null,\n" +
                        "\t\t\"multiStageDesign\"         : \"false\",\n" +
                        "\t\t\t\"vfModules\": [\n" +
                        "\t\t\t\t{\n" +
                        "\t\t\t\t\t\"modelInfo\"               : { \n" +
                        "\t\t\t\t\t\t\"modelName\"              : \"15968a6e2fe541bfA481..base_vfw..module-0\",\n" +
                        "\t\t\t\t\t\t\"modelUuid\"              : \"ec7fadde-1e5a-42f7-8255-cb19e475ff45\",\n" +
                        "\t\t\t\t\t\t\"modelInvariantUuid\"     : \"61ab8b64-a014-4cf3-8a5a-b5ef388f8819\",\n" +
                        "\t\t\t\t\t\t\"modelVersion\"           : \"1\",\n" +
                        "\t\t\t\t\t\t\"modelCustomizationUuid\" : \"123aff6b-854f-4026-ae1e-cc74a3924576\"\n" +
                        "\t\t\t\t\t},\t\t\"isBase\"                 : true,\n" +
                        "\t\t\t\t\t\"vfModuleLabel\"          : \"base_vfw\",\n" +
                        "\t\t\t\t\t\"initialCount\"           : 1,\n" +
                        "\t\t\t\t\t\"hasVolumeGroup\"           : true\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t]\n" +
                        "\t\t},\n" +
                        "\t\n" +
                        "\t\t{ \"modelInfo\"                    : {\n" +
                        "\t\t\t\"modelName\"              : \"f971106a-248f-4202-9d1f\",\n" +
                        "\t\t\t\"modelUuid\"              : \"4fbc08a4-35ed-4a59-9e47-79975e4add7e\",\n" +
                        "\t\t\t\"modelInvariantUuid\"     : \"c669799e-adf1-46ae-8c70-48b326fe89f3\",\n" +
                        "\t\t\t\"modelVersion\"           : \"1.0\",\n" +
                        "\t\t\t\"modelCustomizationUuid\" : \"e776449e-2b10-45c5-9217-2775c88ca1a0\",\n" +
                        "\t\t\t\"modelInstanceName\"      : \"f971106a-248f-4202-9d1f 0\"\n" +
                        "\t\t\t},\n" +
                        "\t\t\"toscaNodeType\"            : \"org.openecomp.resource.vf.F971106a248f42029d1f\",\n" +
                        "\t\t\"nfFunction\"           \t: null,\n" +
                        "\t\t\"nfType\"              \t\t: null,\n" +
                        "\t\t\"nfRole\"              \t\t: null,\n" +
                        "\"resourceInput\":\"{\\\"a\\\":\\\"key|default_value\\\"}\"," +
                        "\t\t\"nfNamingCode\"         \t: null,\n" +
                        "\t\t\"multiStageDesign\"         : \"false\",\n" +
                        "\t\t\t\"vfModules\": [\n" +
                        "\t\t\t\t{\n" +
                        "\t\t\t\t\t\"modelInfo\"               : { \n" +
                        "\t\t\t\t\t\t\"modelName\"              : \"F971106a248f42029d1f..base_vpkg..module-0\",\n" +
                        "\t\t\t\t\t\t\"modelUuid\"              : \"47d5273a-7456-4786-9035-b3911944cc35\",\n" +
                        "\t\t\t\t\t\t\"modelInvariantUuid\"     : \"0ea3e57e-ac7a-425a-928b-b4aee8806c15\",\n" +
                        "\t\t\t\t\t\t\"modelVersion\"           : \"1\",\n" +
                        "\t\t\t\t\t\t\"modelCustomizationUuid\" : \"9ed9fef6-d3f8-4433-9807-7e23393a16bc\"\n" +
                        "\t\t\t\t\t},\t\t\"isBase\"                 : true,\n" +
                        "\t\t\t\t\t\"vfModuleLabel\"          : \"base_vpkg\",\n" +
                        "\t\t\t\t\t\"initialCount\"           : 1,\n" +
                        "\t\t\t\t\t\"hasVolumeGroup\"           : true\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t]\n" +
                        "\t\t}\n" +
                        "\t],\n" +
                        "\t\"serviceNetworks\": [],\n" +
                        "\t\"serviceAllottedResources\": []\n" +
                        "\t}}")));

//        when(UrnPropertiesReader.getVariable(anyString())).thenReturn("http://localhost:8080");
        HashMap serviceInput = new HashMap();
        serviceInput.put("key", "value");
        Map<String, Object> stringObjectMap = ResourceRequestBuilder.buildResouceRequest("c3954379-4efe-431c-8258-f84905b158e5",
                "e776449e-2b10-45c5-9217-2775c88ca1a0", serviceInput);
        assertEquals(stringObjectMap.get("a"), "value");
    }

    @Test
    public void getResourceInputDefaultValueTest() throws Exception {

        stubFor(get(urlEqualTo("/ecomp/mso/catalog/v2/serviceResources?serviceModelUuid=c3954379-4efe-431c-8258-f84905b158e5"))
                .willReturn(ok("{ \"serviceResources\"    : {\n" +
                        "\t\"modelInfo\"       : {\n" +
                        "\t\t\"modelName\"          : \"demoVFWCL\",\n" +
                        "\t\t\"modelUuid\"          : \"c3954379-4efe-431c-8258-f84905b158e5\",\n" +
                        "\t\t\"modelInvariantUuid\" : \"0cbff61e-3b0a-4eed-97ce-b1b4faa03493\",\n" +
                        "\t\t\"modelVersion\"       : \"1.0\"\n" +
                        "\t},\n" +
                        "\t\"serviceType\"        : \"\",\n" +
                        "\t\"serviceRole\"        : \"\",\n" +
                        "\t\"environmentContext\" : null,\n" +
                        "\t\"workloadContext\"    : \"Production\",\n" +
                        "\t\"serviceVnfs\": [\n" +
                        "\t\n" +
                        "\t\t{ \"modelInfo\"                    : {\n" +
                        "\t\t\t\"modelName\"              : \"15968a6e-2fe5-41bf-a481\",\n" +
                        "\t\t\t\"modelUuid\"              : \"808abda3-2023-4105-92d2-e62644b61d53\",\n" +
                        "\t\t\t\"modelInvariantUuid\"     : \"6e4ffc7c-497e-4a77-970d-af966e642d31\",\n" +
                        "\t\t\t\"modelVersion\"           : \"1.0\",\n" +
                        "\t\t\t\"modelCustomizationUuid\" : \"a00404d5-d7eb-4c46-b6b6-9cf2d087e545\",\n" +
                        "\t\t\t\"modelInstanceName\"      : \"15968a6e-2fe5-41bf-a481 0\"\n" +
                        "\t\t\t},\n" +
                        "\t\t\"toscaNodeType\"            : \"org.openecomp.resource.vf.15968a6e2fe541bfA481\",\n" +
                        "\t\t\"nfFunction\"           \t: null,\n" +
                        "\"resourceInput\":\"{\\\"a\\\":\\\"b\\\"}\"," +
                        "\t\t\"nfType\"              \t\t: null,\n" +
                        "\t\t\"nfRole\"              \t\t: null,\n" +
                        "\t\t\"nfNamingCode\"         \t: null,\n" +
                        "\t\t\"multiStageDesign\"         : \"false\",\n" +
                        "\t\t\t\"vfModules\": [\n" +
                        "\t\t\t\t{\n" +
                        "\t\t\t\t\t\"modelInfo\"               : { \n" +
                        "\t\t\t\t\t\t\"modelName\"              : \"15968a6e2fe541bfA481..base_vfw..module-0\",\n" +
                        "\t\t\t\t\t\t\"modelUuid\"              : \"ec7fadde-1e5a-42f7-8255-cb19e475ff45\",\n" +
                        "\t\t\t\t\t\t\"modelInvariantUuid\"     : \"61ab8b64-a014-4cf3-8a5a-b5ef388f8819\",\n" +
                        "\t\t\t\t\t\t\"modelVersion\"           : \"1\",\n" +
                        "\t\t\t\t\t\t\"modelCustomizationUuid\" : \"123aff6b-854f-4026-ae1e-cc74a3924576\"\n" +
                        "\t\t\t\t\t},\t\t\"isBase\"                 : true,\n" +
                        "\t\t\t\t\t\"vfModuleLabel\"          : \"base_vfw\",\n" +
                        "\t\t\t\t\t\"initialCount\"           : 1,\n" +
                        "\t\t\t\t\t\"hasVolumeGroup\"           : true\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t]\n" +
                        "\t\t},\n" +
                        "\t\n" +
                        "\t\t{ \"modelInfo\"                    : {\n" +
                        "\t\t\t\"modelName\"              : \"f971106a-248f-4202-9d1f\",\n" +
                        "\t\t\t\"modelUuid\"              : \"4fbc08a4-35ed-4a59-9e47-79975e4add7e\",\n" +
                        "\t\t\t\"modelInvariantUuid\"     : \"c669799e-adf1-46ae-8c70-48b326fe89f3\",\n" +
                        "\t\t\t\"modelVersion\"           : \"1.0\",\n" +
                        "\t\t\t\"modelCustomizationUuid\" : \"e776449e-2b10-45c5-9217-2775c88ca1a0\",\n" +
                        "\t\t\t\"modelInstanceName\"      : \"f971106a-248f-4202-9d1f 0\"\n" +
                        "\t\t\t},\n" +
                        "\t\t\"toscaNodeType\"            : \"org.openecomp.resource.vf.F971106a248f42029d1f\",\n" +
                        "\t\t\"nfFunction\"           \t: null,\n" +
                        "\t\t\"nfType\"              \t\t: null,\n" +
                        "\t\t\"nfRole\"              \t\t: null,\n" +
                        "\"resourceInput\":\"{\\\"a\\\":\\\"key|default_value\\\"}\"," +
                        "\t\t\"nfNamingCode\"         \t: null,\n" +
                        "\t\t\"multiStageDesign\"         : \"false\",\n" +
                        "\t\t\t\"vfModules\": [\n" +
                        "\t\t\t\t{\n" +
                        "\t\t\t\t\t\"modelInfo\"               : { \n" +
                        "\t\t\t\t\t\t\"modelName\"              : \"F971106a248f42029d1f..base_vpkg..module-0\",\n" +
                        "\t\t\t\t\t\t\"modelUuid\"              : \"47d5273a-7456-4786-9035-b3911944cc35\",\n" +
                        "\t\t\t\t\t\t\"modelInvariantUuid\"     : \"0ea3e57e-ac7a-425a-928b-b4aee8806c15\",\n" +
                        "\t\t\t\t\t\t\"modelVersion\"           : \"1\",\n" +
                        "\t\t\t\t\t\t\"modelCustomizationUuid\" : \"9ed9fef6-d3f8-4433-9807-7e23393a16bc\"\n" +
                        "\t\t\t\t\t},\t\t\"isBase\"                 : true,\n" +
                        "\t\t\t\t\t\"vfModuleLabel\"          : \"base_vpkg\",\n" +
                        "\t\t\t\t\t\"initialCount\"           : 1,\n" +
                        "\t\t\t\t\t\"hasVolumeGroup\"           : true\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t]\n" +
                        "\t\t}\n" +
                        "\t],\n" +
                        "\t\"serviceNetworks\": [],\n" +
                        "\t\"serviceAllottedResources\": []\n" +
                        "\t}}")));

//        when(UrnPropertiesReader.getVariable(anyString())).thenReturn("http://localhost:8080");
        HashMap serviceInput = new HashMap();
        serviceInput.put("key1", "value");
        Map<String, Object> stringObjectMap = ResourceRequestBuilder.buildResouceRequest("c3954379-4efe-431c-8258-f84905b158e5",
                "e776449e-2b10-45c5-9217-2775c88ca1a0", serviceInput);
        assertEquals(stringObjectMap.get("a"), "default_value");
    }

    @Test
    public void getResourceInputValueNoDefaultTest() throws Exception {

        stubFor(get(urlEqualTo("/ecomp/mso/catalog/v2/serviceResources?serviceModelUuid=c3954379-4efe-431c-8258-f84905b158e5"))
                .willReturn(ok("{ \"serviceResources\"    : {\n" +
                        "\t\"modelInfo\"       : {\n" +
                        "\t\t\"modelName\"          : \"demoVFWCL\",\n" +
                        "\t\t\"modelUuid\"          : \"c3954379-4efe-431c-8258-f84905b158e5\",\n" +
                        "\t\t\"modelInvariantUuid\" : \"0cbff61e-3b0a-4eed-97ce-b1b4faa03493\",\n" +
                        "\t\t\"modelVersion\"       : \"1.0\"\n" +
                        "\t},\n" +
                        "\t\"serviceType\"        : \"\",\n" +
                        "\t\"serviceRole\"        : \"\",\n" +
                        "\t\"environmentContext\" : null,\n" +
                        "\t\"workloadContext\"    : \"Production\",\n" +
                        "\t\"serviceVnfs\": [\n" +
                        "\t\n" +
                        "\t\t{ \"modelInfo\"                    : {\n" +
                        "\t\t\t\"modelName\"              : \"15968a6e-2fe5-41bf-a481\",\n" +
                        "\t\t\t\"modelUuid\"              : \"808abda3-2023-4105-92d2-e62644b61d53\",\n" +
                        "\t\t\t\"modelInvariantUuid\"     : \"6e4ffc7c-497e-4a77-970d-af966e642d31\",\n" +
                        "\t\t\t\"modelVersion\"           : \"1.0\",\n" +
                        "\t\t\t\"modelCustomizationUuid\" : \"a00404d5-d7eb-4c46-b6b6-9cf2d087e545\",\n" +
                        "\t\t\t\"modelInstanceName\"      : \"15968a6e-2fe5-41bf-a481 0\"\n" +
                        "\t\t\t},\n" +
                        "\t\t\"toscaNodeType\"            : \"org.openecomp.resource.vf.15968a6e2fe541bfA481\",\n" +
                        "\t\t\"nfFunction\"           \t: null,\n" +
                        "\"resourceInput\":\"{\\\"a\\\":\\\"b\\\"}\"," +
                        "\t\t\"nfType\"              \t\t: null,\n" +
                        "\t\t\"nfRole\"              \t\t: null,\n" +
                        "\t\t\"nfNamingCode\"         \t: null,\n" +
                        "\t\t\"multiStageDesign\"         : \"false\",\n" +
                        "\t\t\t\"vfModules\": [\n" +
                        "\t\t\t\t{\n" +
                        "\t\t\t\t\t\"modelInfo\"               : { \n" +
                        "\t\t\t\t\t\t\"modelName\"              : \"15968a6e2fe541bfA481..base_vfw..module-0\",\n" +
                        "\t\t\t\t\t\t\"modelUuid\"              : \"ec7fadde-1e5a-42f7-8255-cb19e475ff45\",\n" +
                        "\t\t\t\t\t\t\"modelInvariantUuid\"     : \"61ab8b64-a014-4cf3-8a5a-b5ef388f8819\",\n" +
                        "\t\t\t\t\t\t\"modelVersion\"           : \"1\",\n" +
                        "\t\t\t\t\t\t\"modelCustomizationUuid\" : \"123aff6b-854f-4026-ae1e-cc74a3924576\"\n" +
                        "\t\t\t\t\t},\t\t\"isBase\"                 : true,\n" +
                        "\t\t\t\t\t\"vfModuleLabel\"          : \"base_vfw\",\n" +
                        "\t\t\t\t\t\"initialCount\"           : 1,\n" +
                        "\t\t\t\t\t\"hasVolumeGroup\"           : true\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t]\n" +
                        "\t\t},\n" +
                        "\t\n" +
                        "\t\t{ \"modelInfo\"                    : {\n" +
                        "\t\t\t\"modelName\"              : \"f971106a-248f-4202-9d1f\",\n" +
                        "\t\t\t\"modelUuid\"              : \"4fbc08a4-35ed-4a59-9e47-79975e4add7e\",\n" +
                        "\t\t\t\"modelInvariantUuid\"     : \"c669799e-adf1-46ae-8c70-48b326fe89f3\",\n" +
                        "\t\t\t\"modelVersion\"           : \"1.0\",\n" +
                        "\t\t\t\"modelCustomizationUuid\" : \"e776449e-2b10-45c5-9217-2775c88ca1a0\",\n" +
                        "\t\t\t\"modelInstanceName\"      : \"f971106a-248f-4202-9d1f 0\"\n" +
                        "\t\t\t},\n" +
                        "\t\t\"toscaNodeType\"            : \"org.openecomp.resource.vf.F971106a248f42029d1f\",\n" +
                        "\t\t\"nfFunction\"           \t: null,\n" +
                        "\t\t\"nfType\"              \t\t: null,\n" +
                        "\t\t\"nfRole\"              \t\t: null,\n" +
                        "\"resourceInput\":\"{\\\"a\\\":\\\"value\\\"}\"," +
                        "\t\t\"nfNamingCode\"         \t: null,\n" +
                        "\t\t\"multiStageDesign\"         : \"false\",\n" +
                        "\t\t\t\"vfModules\": [\n" +
                        "\t\t\t\t{\n" +
                        "\t\t\t\t\t\"modelInfo\"               : { \n" +
                        "\t\t\t\t\t\t\"modelName\"              : \"F971106a248f42029d1f..base_vpkg..module-0\",\n" +
                        "\t\t\t\t\t\t\"modelUuid\"              : \"47d5273a-7456-4786-9035-b3911944cc35\",\n" +
                        "\t\t\t\t\t\t\"modelInvariantUuid\"     : \"0ea3e57e-ac7a-425a-928b-b4aee8806c15\",\n" +
                        "\t\t\t\t\t\t\"modelVersion\"           : \"1\",\n" +
                        "\t\t\t\t\t\t\"modelCustomizationUuid\" : \"9ed9fef6-d3f8-4433-9807-7e23393a16bc\"\n" +
                        "\t\t\t\t\t},\t\t\"isBase\"                 : true,\n" +
                        "\t\t\t\t\t\"vfModuleLabel\"          : \"base_vpkg\",\n" +
                        "\t\t\t\t\t\"initialCount\"           : 1,\n" +
                        "\t\t\t\t\t\"hasVolumeGroup\"           : true\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t]\n" +
                        "\t\t}\n" +
                        "\t],\n" +
                        "\t\"serviceNetworks\": [],\n" +
                        "\t\"serviceAllottedResources\": []\n" +
                        "\t}}")));

//        when(UrnPropertiesReader.getVariable(anyString())).thenReturn("http://localhost:8080");
        HashMap serviceInput = new HashMap();
        serviceInput.put("key1", "value");
        Map<String, Object> stringObjectMap = ResourceRequestBuilder.buildResouceRequest("c3954379-4efe-431c-8258-f84905b158e5",
                "e776449e-2b10-45c5-9217-2775c88ca1a0", serviceInput);
        assertEquals(stringObjectMap.get("a"), "value");
    }

    @Test
    public void getResourceSequenceTest() throws Exception {

        stubFor(get(urlEqualTo("/ecomp/mso/catalog/v2/serviceResources?serviceModelUuid=c3954379-4efe-431c-8258-f84905b158e5"))
                .willReturn(ok("{ \"serviceResources\"    : {\n" +
                        "\t\"modelInfo\"       : {\n" +
                        "\t\t\"modelName\"          : \"demoVFWCL\",\n" +
                        "\t\t\"modelUuid\"          : \"c3954379-4efe-431c-8258-f84905b158e5\",\n" +
                        "\t\t\"modelInvariantUuid\" : \"0cbff61e-3b0a-4eed-97ce-b1b4faa03493\",\n" +
                        "\t\t\"modelVersion\"       : \"1.0\"\n" +
                        "\t},\n" +
                        "\t\"serviceType\"        : \"\",\n" +
                        "\t\"serviceRole\"        : \"\",\n" +
                        "\t\"environmentContext\" : null,\n" +
                        "\t\"resourceOrder\"       : \"res1,res2\",\n" +
                        "\t\"workloadContext\"    : \"Production\",\n" +
                        "\t\"serviceVnfs\": [\n" +
                        "\t\n" +
                        "\t\t{ \"modelInfo\"                    : {\n" +
                        "\t\t\t\"modelName\"              : \"15968a6e-2fe5-41bf-a481\",\n" +
                        "\t\t\t\"modelUuid\"              : \"808abda3-2023-4105-92d2-e62644b61d53\",\n" +
                        "\t\t\t\"modelInvariantUuid\"     : \"6e4ffc7c-497e-4a77-970d-af966e642d31\",\n" +
                        "\t\t\t\"modelVersion\"           : \"1.0\",\n" +
                        "\t\t\t\"modelCustomizationUuid\" : \"a00404d5-d7eb-4c46-b6b6-9cf2d087e545\",\n" +
                        "\t\t\t\"modelInstanceName\"      : \"15968a6e-2fe5-41bf-a481 0\"\n" +
                        "\t\t\t},\n" +
                        "\t\t\"toscaNodeType\"            : \"org.openecomp.resource.vf.15968a6e2fe541bfA481\",\n" +
                        "\t\t\"nfFunction\"           \t: null,\n" +
                        "\"resourceInput\":\"{\\\"a\\\":\\\"b\\\"}\"," +
                        "\t\t\"nfType\"              \t\t: null,\n" +
                        "\t\t\"nfRole\"              \t\t: null,\n" +
                        "\t\t\"nfNamingCode\"         \t: null,\n" +
                        "\t\t\"multiStageDesign\"         : \"false\",\n" +
                        "\t\t\t\"vfModules\": [\n" +
                        "\t\t\t\t{\n" +
                        "\t\t\t\t\t\"modelInfo\"               : { \n" +
                        "\t\t\t\t\t\t\"modelName\"              : \"15968a6e2fe541bfA481..base_vfw..module-0\",\n" +
                        "\t\t\t\t\t\t\"modelUuid\"              : \"ec7fadde-1e5a-42f7-8255-cb19e475ff45\",\n" +
                        "\t\t\t\t\t\t\"modelInvariantUuid\"     : \"61ab8b64-a014-4cf3-8a5a-b5ef388f8819\",\n" +
                        "\t\t\t\t\t\t\"modelVersion\"           : \"1\",\n" +
                        "\t\t\t\t\t\t\"modelCustomizationUuid\" : \"123aff6b-854f-4026-ae1e-cc74a3924576\"\n" +
                        "\t\t\t\t\t},\t\t\"isBase\"                 : true,\n" +
                        "\t\t\t\t\t\"vfModuleLabel\"          : \"base_vfw\",\n" +
                        "\t\t\t\t\t\"initialCount\"           : 1,\n" +
                        "\t\t\t\t\t\"hasVolumeGroup\"           : true\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t]\n" +
                        "\t\t},\n" +
                        "\t\n" +
                        "\t\t{ \"modelInfo\"                    : {\n" +
                        "\t\t\t\"modelName\"              : \"f971106a-248f-4202-9d1f\",\n" +
                        "\t\t\t\"modelUuid\"              : \"4fbc08a4-35ed-4a59-9e47-79975e4add7e\",\n" +
                        "\t\t\t\"modelInvariantUuid\"     : \"c669799e-adf1-46ae-8c70-48b326fe89f3\",\n" +
                        "\t\t\t\"modelVersion\"           : \"1.0\",\n" +
                        "\t\t\t\"modelCustomizationUuid\" : \"e776449e-2b10-45c5-9217-2775c88ca1a0\",\n" +
                        "\t\t\t\"modelInstanceName\"      : \"f971106a-248f-4202-9d1f 0\"\n" +
                        "\t\t\t},\n" +
                        "\t\t\"toscaNodeType\"            : \"org.openecomp.resource.vf.F971106a248f42029d1f\",\n" +
                        "\t\t\"nfFunction\"           \t: null,\n" +
                        "\t\t\"nfType\"              \t\t: null,\n" +
                        "\t\t\"nfRole\"              \t\t: null,\n" +
                        "\"resourceInput\":\"{\\\"a\\\":\\\"key|default_value\\\"}\"," +
                        "\t\t\"nfNamingCode\"         \t: null,\n" +
                        "\t\t\"multiStageDesign\"         : \"false\",\n" +
                        "\t\t\t\"vfModules\": [\n" +
                        "\t\t\t\t{\n" +
                        "\t\t\t\t\t\"modelInfo\"               : { \n" +
                        "\t\t\t\t\t\t\"modelName\"              : \"F971106a248f42029d1f..base_vpkg..module-0\",\n" +
                        "\t\t\t\t\t\t\"modelUuid\"              : \"47d5273a-7456-4786-9035-b3911944cc35\",\n" +
                        "\t\t\t\t\t\t\"modelInvariantUuid\"     : \"0ea3e57e-ac7a-425a-928b-b4aee8806c15\",\n" +
                        "\t\t\t\t\t\t\"modelVersion\"           : \"1\",\n" +
                        "\t\t\t\t\t\t\"modelCustomizationUuid\" : \"9ed9fef6-d3f8-4433-9807-7e23393a16bc\"\n" +
                        "\t\t\t\t\t},\t\t\"isBase\"                 : true,\n" +
                        "\t\t\t\t\t\"vfModuleLabel\"          : \"base_vpkg\",\n" +
                        "\t\t\t\t\t\"initialCount\"           : 1,\n" +
                        "\t\t\t\t\t\"hasVolumeGroup\"           : true\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t]\n" +
                        "\t\t}\n" +
                        "\t],\n" +
                        "\t\"serviceNetworks\": [],\n" +
                        "\t\"serviceAllottedResources\": []\n" +
                        "\t}}")));

        List<String> resourceSequence = ResourceRequestBuilder.getResourceSequence("c3954379-4efe-431c-8258-f84905b158e5");
        assertEquals(resourceSequence.size(), 2);
        assertEquals(resourceSequence.get(0), "res1");
        assertEquals(resourceSequence.get(1), "res2");
    }
}