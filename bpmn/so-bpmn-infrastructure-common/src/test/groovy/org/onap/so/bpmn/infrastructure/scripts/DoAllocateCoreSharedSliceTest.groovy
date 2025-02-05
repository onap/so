/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020  Tech Mahindra
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

package org.onap.so.bpmn.infrastructure.scripts

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertEquals

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.onap.so.bpmn.common.scripts.MsoGroovyTest
import org.mockito.Mockito
import org.onap.aaiclient.client.aai.AAIObjectType
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper
import org.onap.aaiclient.client.aai.entities.uri.AAIResourceUri
import org.onap.aaiclient.client.aai.entities.uri.AAIUriFactory
import org.onap.aaiclient.client.generated.fluentbuilders.AAIFluentTypeBuilder

import static org.mockito.Mockito.spy
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when
import static org.mockito.ArgumentMatchers.eq

import jakarta.ws.rs.NotFoundException

class DoAllocateCoreSharedSliceTest extends MsoGroovyTest {

    DoAllocateCoreSharedSlice allocate = new DoAllocateCoreSharedSlice()

    @Before
    void init() throws IOException {
        super.init("DoAllocateCoreSharedSlice")
    }

    @Captor
    static ArgumentCaptor<ExecutionEntity> captor = ArgumentCaptor.forClass(ExecutionEntity.class)

    @Test
    void testPreProcessRequest(){

    String solutions = """ {
    "NSSIId": "11c0c52a-d748-48aa-86e3-c783cbf5026f",
    "invariantUUID": "8ebba719-f815-47e3-8473-c5f0db801356",
    "NSSIName": "nssi_CN_NSST",
    "UUID": "70e2b55b-8dca-4ff3-8f47-374c2965b731"
     }"""
        String sliceProfile = "{\r\n      \"snssaiList\": [ \r\n        \"001-100001\"\r\n      ],\r\n      \"sliceProfileId\": \"ab9af40f13f721b5f13539d87484098\",\r\n      \"plmnIdList\": [\r\n        \"460-00\",\r\n        \"460-01\"\r\n      ],\r\n      \"perfReq\": {\r\n        \"perfReqEmbbList \": [\r\n          {\r\n            \"activityFactor\": 50\r\n          }\r\n        ]\r\n      },\r\n      \"maxNumberofUEs\": 200, \r\n      \"coverageAreaTAList\": [ \r\n        \"1\",\r\n        \"2\",\r\n        \"3\",\r\n        \"4\"\r\n      ],\r\n      \"latency\": 2,\r\n      \"resourceSharingLevel\": \"non-shared\" \r\n    }"

        setUpBaseMockData()

        when(mockExecution.getVariable("solutions")).thenReturn(solutions)
        when(mockExecution.getVariable("sliceProfile")).thenReturn(sliceProfile)

        allocate.preProcessRequest(mockExecution)

        Mockito.verify(mockExecution, times(1)).setVariable(eq("nssiId"), captor.capture())
        def nssiId = captor.getValue()
        assertEquals("11c0c52a-d748-48aa-86e3-c783cbf5026f", nssiId)

        Mockito.verify(mockExecution, times(1)).setVariable(eq("sNssai"), captor.capture())
        def sNssai = captor.getValue()
        assertEquals("001-100001", sNssai)

        Mockito.verify(mockExecution,times(4)).setVariable(captor.capture() as String, captor.capture())
        List<ExecutionEntity> values = captor.getAllValues()
        assertNotNull(values)
    }

    @Test
    public void testPrepareSOMacroRequestPayload() {

        String json ="{ \"serviceResources\"    : {\r\n\t\"modelInfo\"       : {\r\n\t\t\"modelName\"          : \"MSOTADevInfra_vSAMP10a_Service\",\r\n\t\t\"modelUuid\"          : \"5df8b6de-2083-11e7-93ae-92361f002671\",\r\n\t\t\"modelInvariantUuid\" : \"9647dfc4-2083-11e7-93ae-92361f002671\",\r\n\t\t\"modelVersion\"       : \"1.0\"\r\n\t},\r\n\t\"serviceType\"        : \"PortMirroring\",\r\n\t\"serviceRole\"        : \"InfraRole\",\r\n\t\"environmentContext\" : \"Luna\",\r\n\t\"workloadContext\"    : \"Oxygen\",\r\n\t\"serviceVnfs\": [\r\n\t\r\n\t\t{ \"modelInfo\"                    : {\r\n\t\t\t\"modelName\"              : \"vSAMP10a\",\r\n\t\t\t\"modelUuid\"              : \"ff2ae348-214a-11e7-93ae-92361f002671\",\r\n\t\t\t\"modelInvariantUuid\"     : \"2fff5b20-214b-11e7-93ae-92361f002671\",\r\n\t\t\t\"modelVersion\"           : \"1.0\",\r\n\t\t\t\"modelCustomizationUuid\" : \"68dc9a92-214c-11e7-93ae-92361f002671\",\r\n\t\t\t\"modelInstanceName\"      : \"vSAMP10a 1\"\r\n\t\t\t},\r\n\t\t\"toscaNodeType\"            : \"VF\",\r\n\t\t\"nfFunction\"           \t: null,\r\n\t\t\"nfType\"              \t\t: null,\r\n\t\t\"nfRole\"              \t\t: null,\r\n\t\t\"nfNamingCode\"         \t: null,\r\n\t\t\"multiStageDesign\"\t\t: null,\r\n\t\t\t\"vfModules\": [\r\n\t\t\t\t{\r\n\t\t\t\t\t\"modelInfo\"               : { \r\n\t\t\t\t\t\t\"modelName\"              : \"NetworkFqdnTest4\",\r\n\t\t\t\t\t\t\"modelUuid\"              : \"025606c1-4223-11e7-9252-005056850d2e\",\r\n\t\t\t\t\t\t\"modelInvariantUuid\"     : \"06bd0a18-65c0-4418-83c7-5b0d13cba01a\",\r\n\t\t\t\t\t\t\"modelVersion\"           : \"2.0\",\r\n\t\t\t\t\t\t\"modelCustomizationUuid\" : \"06bd0a18-65c0-4418-83c7-5b0d13cba01a\"\r\n\t\t\t\t\t},\t\t\"isBase\"                 : true,\r\n\t\t\t\t\t\"vfModuleLabel\"          : \"label\",\r\n\t\t\t\t\t\"initialCount\"           : 0,\r\n\t\t\t\t\t\"hasVolumeGroup\"           : true\r\n\t\t\t\t},\r\n\t\t\t\t{\r\n\t\t\t\t\t\"modelInfo\"               : { \r\n\t\t\t\t\t\t\"modelName\"              : \"NetworkFqdnTest3\",\r\n\t\t\t\t\t\t\"modelUuid\"              : \"02560575-4223-11e7-9252-005056850d2e\",\r\n\t\t\t\t\t\t\"modelInvariantUuid\"     : \"06bd0a18-65c0-4418-83c7-5b0d13cba0bb\",\r\n\t\t\t\t\t\t\"modelVersion\"           : \"1.0\",\r\n\t\t\t\t\t\t\"modelCustomizationUuid\" : \"06bd0a18-65c0-4418-83c7-5b0d13cba0bb\"\r\n\t\t\t\t\t},\t\t\"isBase\"                 : true,\r\n\t\t\t\t\t\"vfModuleLabel\"          : \"label\",\r\n\t\t\t\t\t\"initialCount\"           : 0,\r\n\t\t\t\t\t\"hasVolumeGroup\"           : false\r\n\t\t\t\t},\r\n\t\t\t\t{\r\n\t\t\t\t\t\"modelInfo\"               : { \r\n\t\t\t\t\t\t\"modelName\"              : \"NetworkFqdnTest5\",\r\n\t\t\t\t\t\t\"modelUuid\"              : \"025607e4-4223-11e7-9252-005056850d2e\",\r\n\t\t\t\t\t\t\"modelInvariantUuid\"     : \"06bd0a18-65c0-4418-83c7-5b0d14cba01a\",\r\n\t\t\t\t\t\t\"modelVersion\"           : \"1.0\",\r\n\t\t\t\t\t\t\"modelCustomizationUuid\" : \"06bd0a18-65c0-4418-83c7-5b0d14cba01a\"\r\n\t\t\t\t\t},\t\t\"isBase\"                 : false,\r\n\t\t\t\t\t\"vfModuleLabel\"          : \"label\",\r\n\t\t\t\t\t\"initialCount\"           : 0,\r\n\t\t\t\t\t\"hasVolumeGroup\"           : false\r\n\t\t\t\t},\r\n\t\t\t\t{\r\n\t\t\t\t\t\"modelInfo\"               : { \r\n\t\t\t\t\t\t\"modelName\"              : \"vSAMP10aDEV::PCM::module-2\",\r\n\t\t\t\t\t\t\"modelUuid\"              : \"7774b4e4-7d37-11e7-bb31-be2e44b06b34\",\r\n\t\t\t\t\t\t\"modelInvariantUuid\"     : \"93e9c1d2-7d37-11e7-bb31-be2e44b06b34\",\r\n\t\t\t\t\t\t\"modelVersion\"           : \"2\",\r\n\t\t\t\t\t\t\"modelCustomizationUuid\" : \"6728bee8-7d3a-11e7-bb31-be2e44b06b34\"\r\n\t\t\t\t\t},\t\t\"isBase\"                 : false,\r\n\t\t\t\t\t\"vfModuleLabel\"          : \"PCM\",\r\n\t\t\t\t\t\"initialCount\"           : 0,\r\n\t\t\t\t\t\"hasVolumeGroup\"           : true\r\n\t\t\t\t},\r\n\t\t\t\t{\r\n\t\t\t\t\t\"modelInfo\"               : { \r\n\t\t\t\t\t\t\"modelName\"              : \"vSAMP10aDEV::PCM::module-1\",\r\n\t\t\t\t\t\t\"modelUuid\"              : \"066de97e-253e-11e7-93ae-92361f002671\",\r\n\t\t\t\t\t\t\"modelInvariantUuid\"     : \"64efd51a-2544-11e7-93ae-92361f002671\",\r\n\t\t\t\t\t\t\"modelVersion\"           : \"2\",\r\n\t\t\t\t\t\t\"modelCustomizationUuid\" : \"b4ea86b4-253f-11e7-93ae-92361f002671\"\r\n\t\t\t\t\t},\t\t\"isBase\"                 : false,\r\n\t\t\t\t\t\"vfModuleLabel\"          : \"PCM\",\r\n\t\t\t\t\t\"initialCount\"           : 0,\r\n\t\t\t\t\t\"hasVolumeGroup\"           : true\r\n\t\t\t\t},\r\n\t\t\t\t{\r\n\t\t\t\t\t\"modelInfo\"               : { \r\n\t\t\t\t\t\t\"modelName\"              : \"vSAMP10aDEV::base::module-0\",\r\n\t\t\t\t\t\t\"modelUuid\"              : \"20c4431c-246d-11e7-93ae-92361f002671\",\r\n\t\t\t\t\t\t\"modelInvariantUuid\"     : \"78ca26d0-246d-11e7-93ae-92361f002671\",\r\n\t\t\t\t\t\t\"modelVersion\"           : \"2\",\r\n\t\t\t\t\t\t\"modelCustomizationUuid\" : \"cb82ffd8-252a-11e7-93ae-92361f002671\"\r\n\t\t\t\t\t},\t\t\"isBase\"                 : true,\r\n\t\t\t\t\t\"vfModuleLabel\"          : \"base\",\r\n\t\t\t\t\t\"initialCount\"           : 1,\r\n\t\t\t\t\t\"hasVolumeGroup\"           : true\r\n\t\t\t\t},\r\n\t\t\t\t{\r\n\t\t\t\t\t\"modelInfo\"               : { \r\n\t\t\t\t\t\t\"modelName\"              : \"vSAMP10a::base::module-0\",\r\n\t\t\t\t\t\t\"modelUuid\"              : \"02560de2-4223-11e7-9252-005056850d2e\",\r\n\t\t\t\t\t\t\"modelInvariantUuid\"     : null,\r\n\t\t\t\t\t\t\"modelVersion\"           : \"2\",\r\n\t\t\t\t\t\t\"modelCustomizationUuid\" : \"MIGRATED_36e76920-ef30-4793-9979-cbd7d4b2bfc4\"\r\n\t\t\t\t\t},\t\t\"isBase\"                 : true,\r\n\t\t\t\t\t\"vfModuleLabel\"          : \"base\",\r\n\t\t\t\t\t\"initialCount\"           : 1,\r\n\t\t\t\t\t\"hasVolumeGroup\"           : true\r\n\t\t\t\t},\r\n\t\t\t\t{\r\n\t\t\t\t\t\"modelInfo\"               : { \r\n\t\t\t\t\t\t\"modelName\"              : \"base::module-0\",\r\n\t\t\t\t\t\t\"modelUuid\"              : \"02561381-4223-11e7-9252-005056850d2e\",\r\n\t\t\t\t\t\t\"modelInvariantUuid\"     : null,\r\n\t\t\t\t\t\t\"modelVersion\"           : \"1\",\r\n\t\t\t\t\t\t\"modelCustomizationUuid\" : \"MIGRATED_51baae4c-b7c7-4f57-b77e-6e01acca89e5\"\r\n\t\t\t\t\t},\t\t\"isBase\"                 : true,\r\n\t\t\t\t\t\"vfModuleLabel\"          : \"module-0\",\r\n\t\t\t\t\t\"initialCount\"           : 1,\r\n\t\t\t\t\t\"hasVolumeGroup\"           : false\r\n\t\t\t\t},\r\n\t\t\t\t{\r\n\t\t\t\t\t\"modelInfo\"               : { \r\n\t\t\t\t\t\t\"modelName\"              : \"vSAMP10a::PCM::module-1\",\r\n\t\t\t\t\t\t\"modelUuid\"              : \"02560f1b-4223-11e7-9252-005056850d2e\",\r\n\t\t\t\t\t\t\"modelInvariantUuid\"     : null,\r\n\t\t\t\t\t\t\"modelVersion\"           : \"1\",\r\n\t\t\t\t\t\t\"modelCustomizationUuid\" : \"MIGRATED_e9be2ed7-45b6-479c-b06e-9093899f8ce8\"\r\n\t\t\t\t\t},\t\t\"isBase\"                 : true,\r\n\t\t\t\t\t\"vfModuleLabel\"          : \"PCM\",\r\n\t\t\t\t\t\"initialCount\"           : 1,\r\n\t\t\t\t\t\"hasVolumeGroup\"           : true\r\n\t\t\t\t}\r\n\t\t\t]\r\n\t\t}\r\n\t],\r\n\t\"serviceNetworks\": [],\r\n\t\"serviceAllottedResources\": [\r\n\t\t{\r\n\t\t\t\"modelInfo\"       : {\r\n\t\t\t\t\"modelName\"              : \"Tunnel_Xconn\",\r\n\t\t\t\t\"modelUuid\"              : \"f6b7d4c6-e8a4-46e2-81bc-31cad5072842\",\r\n\t\t\t\t\"modelInvariantUuid\"     : \"b7a1b78e-6b6b-4b36-9698-8c9530da14af\",\r\n\t\t\t\t\"modelVersion\"           : \"1.0\",\r\n\t\t\t\t\"modelCustomizationUuid\" : \"5b9bee43-f537-4fb3-9e8b-4de9f714d28a\",\r\n\t\t\t\t\"modelInstanceName\"      : \"Pri_Tunnel_Xconn 9\"\r\n\t\t\t},\r\n\t\t\t\"toscaNodeType\"              : null,\r\n\t\t\t\"allottedResourceType\"              : null,\r\n\t\t\t\"allottedResourceRole\"              : null,\r\n\t\t\t\"providingServiceModelInvariantUuid\"              : null,\r\n\t\t\t\"nfFunction\"              : null,\r\n\t\t\t\"nfType\"              : null,\r\n\t\t\t\"nfRole\"              : null,\r\n\t\t\t\"nfNamingCode\"              : null\r\n\t\t}\r\n\t],\r\n\t\"serviceConfigs\": [\r\n\t\t{\r\n\t\t\t\"modelInfo\"       : {\r\n\t\t\t\t\"modelName\"              : \"Mulder\",\r\n\t\t\t\t\"modelUuid\"              : \"025606c1-4fff-11e7-9252-005056850d2e\",\r\n\t\t\t\t\"modelInvariantUuid\"     : \"025606c1-4eee-11e7-9252-005056850d2e\",\r\n\t\t\t\t\"modelVersion\"           : \"1.0\",\r\n\t\t\t\t\"modelCustomizationUuid\" : \"025606c1-4ddd-11e7-9252-005056850d2e\",\r\n\t\t\t\t\"modelInstanceName\"      : \"X_FILES_001\"\r\n\t\t\t},\r\n\t\t\t\"toscaNodeType\"   : \"Scully\"\r\n\t\t},\r\n\t\t{\r\n\t\t\t\"modelInfo\"       : {\r\n\t\t\t\t\"modelName\"              : \"Krychuk\",\r\n\t\t\t\t\"modelUuid\"              : \"025606c1-5fff-11e7-9252-005056850d2e\",\r\n\t\t\t\t\"modelInvariantUuid\"     : \"025606c1-5eee-11e7-9252-005056850d2e\",\r\n\t\t\t\t\"modelVersion\"           : \"1.0\",\r\n\t\t\t\t\"modelCustomizationUuid\" : \"025606c1-5ddd-11e7-9252-005056850d2e\",\r\n\t\t\t\t\"modelInstanceName\"      : \"X_FILES_002\"\r\n\t\t\t},\r\n\t\t\t\"toscaNodeType\"   : \"Skinner\"\r\n\t\t}\r\n\t]\r\n\t}}\r\n\r\n"
        String sliceProfile = "{\r\n      \"snssaiList\": [ \r\n        \"001-100001\"\r\n      ],\r\n      \"sliceProfileId\": \"ab9af40f13f721b5f13539d87484098\",\r\n      \"plmnIdList\": [\r\n        \"460-00\",\r\n        \"460-01\"\r\n      ],\r\n      \"perfReq\": {\r\n        \"perfReqEmbbList \": [\r\n          {\r\n            \"activityFactor\": 50\r\n          }\r\n        ]\r\n      },\r\n      \"maxNumberofUEs\": 200, \r\n      \"coverageAreaTAList\": [ \r\n        \"1\",\r\n        \"2\",\r\n        \"3\",\r\n        \"4\"\r\n      ],\r\n      \"latency\": 2,\r\n      \"resourceSharingLevel\": \"non-shared\" \r\n    }"
        String vnfs="[{\"toscaNodeType\":\"VF\",\"vfModules\":[{\"initialCount\":0,\"vfModuleLabel\":\"label\",\"modelInfo\":{\"modelInvariantUuid\":\"06bd0a18-65c0-4418-83c7-5b0d13cba01a\",\"modelName\":\"NetworkFqdnTest4\",\"modelVersion\":\"2.0\",\"modelCustomizationUuid\":\"06bd0a18-65c0-4418-83c7-5b0d13cba01a\",\"modelUuid\":\"025606c1-4223-11e7-9252-005056850d2e\"},\"hasVolumeGroup\":true,\"isBase\":true},{\"initialCount\":0,\"vfModuleLabel\":\"label\",\"modelInfo\":{\"modelInvariantUuid\":\"06bd0a18-65c0-4418-83c7-5b0d13cba0bb\",\"modelName\":\"NetworkFqdnTest3\",\"modelVersion\":\"1.0\",\"modelCustomizationUuid\":\"06bd0a18-65c0-4418-83c7-5b0d13cba0bb\",\"modelUuid\":\"02560575-4223-11e7-9252-005056850d2e\"},\"hasVolumeGroup\":false,\"isBase\":true},{\"initialCount\":0,\"vfModuleLabel\":\"label\",\"modelInfo\":{\"modelInvariantUuid\":\"06bd0a18-65c0-4418-83c7-5b0d14cba01a\",\"modelName\":\"NetworkFqdnTest5\",\"modelVersion\":\"1.0\",\"modelCustomizationUuid\":\"06bd0a18-65c0-4418-83c7-5b0d14cba01a\",\"modelUuid\":\"025607e4-4223-11e7-9252-005056850d2e\"},\"hasVolumeGroup\":false,\"isBase\":false},{\"initialCount\":0,\"vfModuleLabel\":\"PCM\",\"modelInfo\":{\"modelInvariantUuid\":\"93e9c1d2-7d37-11e7-bb31-be2e44b06b34\",\"modelName\":\"vSAMP10aDEV::PCM::module-2\",\"modelVersion\":\"2\",\"modelCustomizationUuid\":\"6728bee8-7d3a-11e7-bb31-be2e44b06b34\",\"modelUuid\":\"7774b4e4-7d37-11e7-bb31-be2e44b06b34\"},\"hasVolumeGroup\":true,\"isBase\":false},{\"initialCount\":0,\"vfModuleLabel\":\"PCM\",\"modelInfo\":{\"modelInvariantUuid\":\"64efd51a-2544-11e7-93ae-92361f002671\",\"modelName\":\"vSAMP10aDEV::PCM::module-1\",\"modelVersion\":\"2\",\"modelCustomizationUuid\":\"b4ea86b4-253f-11e7-93ae-92361f002671\",\"modelUuid\":\"066de97e-253e-11e7-93ae-92361f002671\"},\"hasVolumeGroup\":true,\"isBase\":false},{\"initialCount\":1,\"vfModuleLabel\":\"base\",\"modelInfo\":{\"modelInvariantUuid\":\"78ca26d0-246d-11e7-93ae-92361f002671\",\"modelName\":\"vSAMP10aDEV::base::module-0\",\"modelVersion\":\"2\",\"modelCustomizationUuid\":\"cb82ffd8-252a-11e7-93ae-92361f002671\",\"modelUuid\":\"20c4431c-246d-11e7-93ae-92361f002671\"},\"hasVolumeGroup\":true,\"isBase\":true},{\"initialCount\":1,\"vfModuleLabel\":\"base\",\"modelInfo\":{\"modelInvariantUuid\":null,\"modelName\":\"vSAMP10a::base::module-0\",\"modelVersion\":\"2\",\"modelCustomizationUuid\":\"MIGRATED_36e76920-ef30-4793-9979-cbd7d4b2bfc4\",\"modelUuid\":\"02560de2-4223-11e7-9252-005056850d2e\"},\"hasVolumeGroup\":true,\"isBase\":true},{\"initialCount\":1,\"vfModuleLabel\":\"module-0\",\"modelInfo\":{\"modelInvariantUuid\":null,\"modelName\":\"base::module-0\",\"modelVersion\":\"1\",\"modelCustomizationUuid\":\"MIGRATED_51baae4c-b7c7-4f57-b77e-6e01acca89e5\",\"modelUuid\":\"02561381-4223-11e7-9252-005056850d2e\"},\"hasVolumeGroup\":false,\"isBase\":true},{\"initialCount\":1,\"vfModuleLabel\":\"PCM\",\"modelInfo\":{\"modelInvariantUuid\":null,\"modelName\":\"vSAMP10a::PCM::module-1\",\"modelVersion\":\"1\",\"modelCustomizationUuid\":\"MIGRATED_e9be2ed7-45b6-479c-b06e-9093899f8ce8\",\"modelUuid\":\"02560f1b-4223-11e7-9252-005056850d2e\"},\"hasVolumeGroup\":true,\"isBase\":true}],\"modelInfo\":{\"modelInvariantUuid\":\"2fff5b20-214b-11e7-93ae-92361f002671\",\"modelName\":\"vSAMP10a\",\"modelVersion\":\"1.0\",\"modelCustomizationUuid\":\"68dc9a92-214c-11e7-93ae-92361f002671\",\"modelInstanceName\":\"vSAMP10a 1\",\"modelUuid\":\"ff2ae348-214a-11e7-93ae-92361f002671\"},\"nfRole\":null,\"nfType\":null,\"multiStageDesign\":null,\"nfFunction\":null,\"nfNamingCode\":null}]\r\n"
        String service = "{\r\n                \"modelName\"          : \"eMBB-NSST\",\r\n                \"modelUuid\"          : \"ecfa2329-8765-4665-84e0-a32a2ac2be90\",\r\n                \"modelInvariantUuid\" : \"2eeeb5ff-4655-47d8-8aa3-044682246b60\",\r\n                \"modelVersion\"       : \"1.0\"\r\n        }"

        when(mockExecution.getVariable("serviceVnfs")).thenReturn(vnfs)

        when(mockExecution.getVariable("serviceType")).thenReturn("5g")
        when(mockExecution.getVariable("vnfs")).thenReturn(vnfs)
        when(mockExecution.getVariable("serviceModelInfo")).thenReturn(service)
        when(mockExecution.getVariable("sliceProfile")).thenReturn(sliceProfile)
        allocate.prepareSOMacroRequestPayload(mockExecution)

        Mockito.verify(mockExecution, times(1)).setVariable(eq("requestPayload"), captor.capture())
        assertNotNull(captor.getValue())
    }

    @Test
    void testPrepareVnfInstanceParamsJson() {
        List<Map<String, Object>> snssaiList = new ArrayList<>()
        Map<String, Object> snssaiMap = new LinkedHashMap<>()
        snssaiMap.put("snssai", "01-5C83F071")
        snssaiMap.put("status", "activated")
        snssaiList.add(snssaiMap)
        Map<String, Object> snssaiMap1 = new LinkedHashMap<>()
        snssaiMap1.put("snssai", "01-5B179BD4")
        snssaiMap1.put("status", "activated")
        snssaiList.add(snssaiMap1)

        when(mockExecution.getVariable("snssaiAndOrchStatusList")).thenReturn(snssaiList)

        String returnedJsonAsString= allocate.prepareVnfInstanceParamsJson(mockExecution)
        String expectedJsonAsString = """{"sNssai":[{"snssai":"01-5C83F071","status":"activated"},{"snssai":"01-5B179BD4","status":"activated"}]}"""
        assertEquals(expectedJsonAsString, returnedJsonAsString)
    }

    @Test
    void testGetNetworkInstanceWithSPInstanceAssociatedWithNssiId(){

        setUpBaseMockData()
        when(mockExecution.getVariable("serviceType")).thenReturn("5G")

        DoAllocateCoreSharedSlice obj = spy(DoAllocateCoreSharedSlice.class)
        when(obj.getAAIClient()).thenReturn(client)

        AAIResourceUri resourceUri1 = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("5GCustomer").serviceSubscription("5G").serviceInstance("NSSI-C-7Q4-HDBNJ-NSSMF-01-A-ZX"))
        when(client.exists(resourceUri1)).thenReturn(true)
        AAIResultWrapper wrapper1 = new AAIResultWrapper(mockQuerySliceServiceReturn())
        when(client.get(resourceUri1, NotFoundException.class)).thenReturn(wrapper1)

        //networkServiceInstanceId
        when(mockExecution.getVariable("networkServiceInstanceId")).thenReturn("206535e7-77c9-4036-9387-3f1cf57b4379")

        AAIResourceUri resourceUri2 = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("5GCustomer").serviceSubscription("5G").serviceInstance("206535e7-77c9-4036-9387-3f1cf57b4379"))
        when(client.exists(resourceUri2)).thenReturn(true)
        AAIResultWrapper wrapper2 = new AAIResultWrapper(mockQueryNS())
        when(client.get(resourceUri2, NotFoundException.class)).thenReturn(wrapper2)
        //Check Vnf
        when(mockExecution.getVariable("vnfId")).thenReturn("eeb66c6f-36bd-47ad-8294-48f46b1aa912")

        Map<String, Object> spiWithsNssaiAndOrchStatus = new LinkedHashMap<>()
        spiWithsNssaiAndOrchStatus.put("snssai", "01-5C83F071")
        spiWithsNssaiAndOrchStatus.put("status", "activated")
        List <Map<String, Object>> spiWithsNssaiAndOrchStatusList = new ArrayList<>();
        spiWithsNssaiAndOrchStatusList.add(spiWithsNssaiAndOrchStatus)
        //snssaiAndOrchStatusList
        when(mockExecution.getVariable("snssaiAndOrchStatusList")).thenReturn(spiWithsNssaiAndOrchStatusList)

        AAIResourceUri resourceUri3 = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.network().genericVnf(mockExecution.getVariable("vnfId")))
        when(client.exists(resourceUri3)).thenReturn(true)
        AAIResultWrapper wrapper3 = new AAIResultWrapper(mockQueryVnf())
        when(client.get(resourceUri3, NotFoundException.class)).thenReturn(wrapper3)

        //Allotted Resources-1
        AAIResourceUri resourceUri4 = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("5GCustomer").serviceSubscription("5G").serviceInstance("0d3d3cce-46a8-486d-816a-954e71697c4e"))
        when(client.exists(resourceUri4)).thenReturn(true)
        AAIResultWrapper wrapper4 = new AAIResultWrapper(mockServiceProfile1())
        when(client.get(resourceUri4, NotFoundException.class)).thenReturn(wrapper4)

        //Allotted Resources-2
        AAIResourceUri resourceUri5 = AAIUriFactory.createResourceUri(AAIFluentTypeBuilder.business().customer("5GCustomer").serviceSubscription("5G").serviceInstance("1c7046f2-a5a3-4d7f-9da8-388ee641a795"))
        when(client.exists(resourceUri5)).thenReturn(true)
        AAIResultWrapper wrapper5 = new AAIResultWrapper(mockServiceProfile2())
        when(client.get(resourceUri5, NotFoundException.class)).thenReturn(wrapper5)

        obj.getNetworkInstanceAssociatedWithNssiId(mockExecution)

        //networkServiceInstanceId
        Mockito.verify(mockExecution, times(1)).setVariable(eq("networkServiceInstanceId"), captor.capture())
        assertEquals("206535e7-77c9-4036-9387-3f1cf57b4379", captor.getValue())

        Mockito.verify(mockExecution, times(1)).setVariable(eq("networkServiceInstanceName"), captor.capture())
        assertEquals("nsi_DemoEmbb", captor.getValue())

        Mockito.verify(mockExecution, times(1)).setVariable(eq("networkServiceModelInvariantUuid"), captor.capture())
        assertEquals("848c5656-5594-4d41-84bb-7afc7c64765c", captor.getValue())

        Mockito.verify(mockExecution, times(1)).setVariable(eq("owningEntityId"), captor.capture())
        assertEquals("OE-generic", captor.getValue())

        //VnfId
        Mockito.verify(mockExecution, times(1)).setVariable(eq("vnfId"), captor.capture())
        assertEquals("eeb66c6f-36bd-47ad-8294-48f46b1aa912", captor.getValue())

        Mockito.verify(mockExecution, times(1)).setVariable(eq("snssaiAndOrchStatusList"), captor.capture())
        List<Map<String, Object>> snssaiList = new ArrayList<>()
        Map<String, Object> snssaiMap = new LinkedHashMap<>()
        snssaiMap.put("snssai", "01-5C83F071")
        snssaiMap.put("status", "activated")
        snssaiList.add(snssaiMap)

        assertEquals(snssaiList, captor.getValue())

        //Verify Project
        Mockito.verify(mockExecution, times(1)).setVariable(eq("projectName"), captor.capture())
        assertEquals("Project-generic", captor.getValue())

        Mockito.verify(mockExecution, times(1)).setVariable(eq("tenantId"), captor.capture())
        assertEquals("3d5819f1542e4ef9a4ccb0bcb278ca10", captor.getValue())

        Mockito.verify(mockExecution, times(1)).setVariable(eq("cloudOwner"), captor.capture())
        assertEquals("k8scloudowner", captor.getValue())

        Mockito.verify(mockExecution, times(1)).setVariable(eq("lcpCloudRegionId"), captor.capture())
        assertEquals("k8sregion", captor.getValue())

        Mockito.verify(mockExecution, times(1)).setVariable(eq("platformName"), captor.capture())
        assertEquals("test", captor.getValue())

        Mockito.verify(mockExecution, times(1)).setVariable(eq("lineOfBusinessName"), captor.capture())
        assertEquals("LOB-Demonstration", captor.getValue())

    }

    void setUpBaseMockData() {

        String sliceParams ="""{
                    "nsiId": "NSI-M-001-HDBNJ-NSMF-01-A-ZX",
                    "snssaiList": [
                                    "01-5B179BD4"
                                ],
                    "sliceProfileId": "ab9af40f13f721b5f13539d87484098"
                }"""

        when(mockExecution.getVariable("msoRequestId")).thenReturn("5ad89cf9-0569-4a93-4509-d8324321e2be")
        when(mockExecution.getVariable("serviceInstanceID")).thenReturn("NSSI-C-7Q4-HDBNJ-NSSMF-01-A-ZX")
        when(mockExecution.getVariable("nssiId")).thenReturn("NSSI-C-7Q4-HDBNJ-NSSMF-01-A-ZX")
        when(mockExecution.getVariable("nsiId")).thenReturn("NSI-M-001-HDBNJ-NSMF-01-A-ZX")
        when(mockExecution.getVariable("globalSubscriberId")).thenReturn("5GCustomer")
        when(mockExecution.getVariable("subscriptionServiceType")).thenReturn("5G")
        when(mockExecution.getVariable("operationType")).thenReturn("deactivateInstance")
        when(mockExecution.getVariable("jobId")).thenReturn("5ad89cf9-0569-4a93-9999-d8324321e2be")
        when(mockExecution.getVariable("sliceParams")).thenReturn(sliceParams)
    }

    String mockQueryNS() {
        return """
                 {
    "service-instance-id": "206535e7-77c9-4036-9387-3f1cf57b4379",
    "service-instance-name": "nsi_DemoEmbb",
    "environment-context": "General_Revenue-Bearing",
    "workload-context": "Production",
    "model-invariant-id": "848c5656-5594-4d41-84bb-7afc7c64765c",
    "model-version-id": "2de92587-3395-44e8-bb2c-b9529747e580",
    "resource-version": "1599228110527",
    "selflink": "restconf/config/GENERIC-RESOURCE-API:services/service/206535e7-77c9-4036-9387-3f1cf57b4379/service-data/service-topology/",
    "orchestration-status": "Assigned",
    "relationship-list": {
        "relationship": [{
            "related-to": "owning-entity",
            "relationship-label": "org.onap.relationships.inventory.BelongsTo",
            "related-link": "/aai/v19/business/owning-entities/owning-entity/OE-generic",
            "relationship-data": [{
                "relationship-key": "owning-entity.owning-entity-id",
                "relationship-value": "OE-generic"
            }]
        }, {
            "related-to": "generic-vnf",
            "relationship-label": "org.onap.relationships.inventory.ComposedOf",
            "related-link": "/aai/v19/network/generic-vnfs/generic-vnf/eeb66c6f-36bd-47ad-8294-48f46b1aa912",
            "relationship-data": [{
                "relationship-key": "generic-vnf.vnf-id",
                "relationship-value": "eeb66c6f-36bd-47ad-8294-48f46b1aa912"
            }],
            "related-to-property": [{
                "property-key": "generic-vnf.vnf-name",
                "property-value": "vfwuctest 0"
            }]
        }, {
            "related-to": "project",
            "relationship-label": "org.onap.relationships.inventory.Uses",
            "related-link": "/aai/v19/business/projects/project/Project-generic",
            "relationship-data": [{
                "relationship-key": "project.project-name",
                "relationship-value": "Project-generic"
            }]
        }]
    }
}
        """
    }

    String mockQueryVnf() {

        return """
        {
  "vnf-id": "eeb66c6f-36bd-47ad-8294-48f46b1aa912",
  "vnf-name": "vfwuctest 0",
  "vnf-type": "vfwuctest/null",
  "service-id": "a9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb",
  "prov-status": "PREPROV",
  "orchestration-status": "ConfigAssigned",
  "in-maint": false,
  "is-closed-loop-disabled": false,
  "resource-version": "1599228155361",
  "model-invariant-id": "1086e068-c932-4b61-ae3b-2d2eb0cbe3ec",
  "model-version-id": "7fbb28cf-7dfc-447a-892c-4a3130b371d2",
  "model-customization-id": "471b3188-e8f2-470b-9f4d-89e74d45445f",
  "relationship-list": {
    "relationship": [{
      "related-to": "tenant",
      "relationship-label": "org.onap.relationships.inventory.BelongsTo",
      "related-link": "/aai/v19/cloud-infrastructure/cloud-regions/cloud-region/k8scloudowner/k8sregion/tenants/tenant/3d5819f1542e4ef9a4ccb0bcb278ca10",
      "relationship-data": [{
        "relationship-key": "cloud-region.cloud-owner",
        "relationship-value": "k8scloudowner"
      }, {
        "relationship-key": "cloud-region.cloud-region-id",
        "relationship-value": "k8sregion"
      }, {
        "relationship-key": "tenant.tenant-id",
        "relationship-value": "3d5819f1542e4ef9a4ccb0bcb278ca10"
      }],
      "related-to-property": [{
        "property-key": "tenant.tenant-name",
        "property-value": "onap-tm5g-dev"
      }]
    }, {
      "related-to": "cloud-region",
      "relationship-label": "org.onap.relationships.inventory.LocatedIn",
      "related-link": "/aai/v19/cloud-infrastructure/cloud-regions/cloud-region/k8scloudowner/k8sregion",
      "relationship-data": [{
        "relationship-key": "cloud-region.cloud-owner",
        "relationship-value": "k8scloudowner"
      }, {
        "relationship-key": "cloud-region.cloud-region-id",
        "relationship-value": "k8sregion"
      }],
      "related-to-property": [{
        "property-key": "cloud-region.owner-defined-type",
        "property-value": "OwnerType"
      }]
    }, {
      "related-to": "service-instance",
      "relationship-label": "org.onap.relationships.inventory.ComposedOf",
      "related-link": "/aai/v19/business/customers/customer/Demonstration/service-subscriptions/service-subscription/vfw-k8s/service-instances/service-instance/206535e7-77c9-4036-9387-3f1cf57b4379",
      "relationship-data": [{
        "relationship-key": "customer.global-customer-id",
        "relationship-value": "Demonstration"
      }, {
        "relationship-key": "service-subscription.service-type",
        "relationship-value": "vfw-k8s"
      }, {
        "relationship-key": "service-instance.service-instance-id",
        "relationship-value": "206535e7-77c9-4036-9387-3f1cf57b4379"
      }],
      "related-to-property": [{
        "property-key": "service-instance.service-instance-name",
        "property-value": "vfw-0201"
      }]
    }, {
      "related-to": "platform",
      "relationship-label": "org.onap.relationships.inventory.Uses",
      "related-link": "/aai/v19/business/platforms/platform/test",
      "relationship-data": [{
        "relationship-key": "platform.platform-name",
        "relationship-value": "test"
      }]
    }, {
      "related-to": "line-of-business",
      "relationship-label": "org.onap.relationships.inventory.Uses",
      "related-link": "/aai/v19/business/lines-of-business/line-of-business/LOB-Demonstration",
      "relationship-data": [{
        "relationship-key": "line-of-business.line-of-business-name",
        "relationship-value": "LOB-Demonstration"
      }]
    }]
  }
}
        """
    }

    String mockQuerySliceServiceReturn(){
        String expect =
                """{
  "service-instance-id": "NSSI-C-7Q4-HDBNJ-NSSMF-01-A-ZX",
  "service-instance-name": "nssi_DemoEmbb",
  "service-role": "nssi",
  "environment-context": "cn",
  "model-invariant-id": "da575e8e-0863-4172-88b3-b3a9ead67895",
  "model-version-id": "e398c92f-27da-44b9-a717-1dbfc1bdd82e",
  "service-instance-location-id": "39-00",
  "resource-version": "1593525640482",
  "orchestration-status": "activated",
  "relationship-list": {
    "relationship": [{
      "related-to": "service-instance",
      "relationship-label": "org.onap.relationships.inventory.ComposedOf",
      "related-link": "/aai/v19/business/customers/customer/5GCustomer/service-subscriptions/service-subscription/5G/service-instances/service-instance/206535e7-77c9-4036-9387-3f1cf57b4379",
      "relationship-data": [{
        "relationship-key": "customer.global-customer-id",
        "relationship-value": "5GCustomer"
      }, {
        "relationship-key": "service-subscription.service-type",
        "relationship-value": "5G"
      }, {
        "relationship-key": "service-instance.service-instance-id",
        "relationship-value": "206535e7-77c9-4036-9387-3f1cf57b4379"
      }],
      "related-to-property": [{
        "property-key": "service-instance.service-instance-name",
        "property-value": "nsi_DemoEmbb"
      }]
    },
    {
      "related-to": "allotted-resource",
      "relationship-label": "org.onap.relationships.inventory.Uses",
      "related-link": "/aai/v19/business/customers/customer/5GCustomer/service-subscriptions/service-subscription/5G/service-instances/service-instance/0d3d3cce-46a8-486d-816a-954e71697c4e/allotted-resources/allotted-resource/d63c241a-4c0b-4294-b4c3-5a57421a1769",
      "relationship-data": [{
        "relationship-key": "customer.global-customer-id",
        "relationship-value": "5GCustomer"
      }, {
        "relationship-key": "service-subscription.service-type",
        "relationship-value": "5G"
      }, {
        "relationship-key": "service-instance.service-instance-id",
        "relationship-value": "0d3d3cce-46a8-486d-816a-954e71697c4e"
      }, {
        "relationship-key": "allotted-resource.id",
        "relationship-value": "d63c241a-4c0b-4294-b4c3-5a57421a1769"
      }],
      "related-to-property": [{
        "property-key": "allotted-resource.description"
      }, {
        "property-key": "allotted-resource.allotted-resource-name",
        "property-value": "Allotted_DemoEmbb_shared"
      }]
    }, {
      "related-to": "allotted-resource",
      "relationship-label": "org.onap.relationships.inventory.Uses",
      "related-link": "/aai/v19/business/customers/customer/5GCustomer/service-subscriptions/service-subscription/5G/service-instances/service-instance/1c7046f2-a5a3-4d7f-9da8-388ee641a795/allotted-resources/allotted-resource/362e46c2-cd84-45e4-a6c1-77f4ef88328d",
      "relationship-data": [{
        "relationship-key": "customer.global-customer-id",
        "relationship-value": "5GCustomer"
      }, {
        "relationship-key": "service-subscription.service-type",
        "relationship-value": "5G"
      }, {
        "relationship-key": "service-instance.service-instance-id",
        "relationship-value": "1c7046f2-a5a3-4d7f-9da8-388ee641a795"
      }, {
        "relationship-key": "allotted-resource.id",
        "relationship-value": "362e46c2-cd84-45e4-a6c1-77f4ef88328d"
      }],
      "related-to-property": [{
        "property-key": "allotted-resource.description"
      }, {
        "property-key": "allotted-resource.allotted-resource-name",
        "property-value": "Allotted_DemoEmbb"
      }]
    }
    ]
  }
}
                """
        return expect
    }

    String mockServiceProfile1() {
        return """{
  "service-instance-id": "0d3d3cce-46a8-486d-816a-954e71697c4e",
  "service-instance-name": "DemoEmbb2",
  "service-role": "e2esliceprofile-service",
  "environment-context": "01-5C83F071",
  "model-invariant-id": "040b1b40-3120-446b-b8e3-4f21d153d11e",
  "model-version-id": "8b7dabb3-3f27-4555-a9fe-803e862b0292",
  "service-instance-location-id": "39-00",
  "resource-version": "1593511782269",
  "orchestration-status": "activated",
  "relationship-list": {
    "relationship": [{
      "related-to": "service-instance",
      "relationship-label": "org.onap.relationships.inventory.ComposedOf",
      "related-link": "/aai/v19/business/customers/customer/5GCustomer/service-subscriptions/service-subscription/5G/service-instances/service-instance/4b2bdbc0-cf7e-4c50-882a-f660e3ab8520",
      "relationship-data": [{
        "relationship-key": "customer.global-customer-id",
        "relationship-value": "5GCustomer"
      }, {
        "relationship-key": "service-subscription.service-type",
        "relationship-value": "5G"
      }, {
        "relationship-key": "service-instance.service-instance-id",
        "relationship-value": "4b2bdbc0-cf7e-4c50-882a-f660e3ab8520"
      }],
      "related-to-property": [{
        "property-key": "service-instance.service-instance-name",
        "property-value": "DemoEmbb"
      }]
    }]
  },
  "allotted-resources": {
    "allotted-resource": [{
      "id": "362e46c2-cd84-45e4-a6c1-77f4ef88328d",
      "model-invariant-id": "e5941a50-ddb4-4f74-be03-25449ae02ddc",
      "model-version-id": "ab171d60-c2cc-4903-ac1d-c451b647e461",
      "resource-version": "1593511173712",
      "type": "Allotted Resource",
      "allotted-resource-name": "Allotted_DemoEmbb",
      "relationship-list": {
        "relationship": [{
          "related-to": "service-instance",
          "relationship-label": "org.onap.relationships.inventory.Uses",
          "related-link": "/aai/v19/business/customers/customer/5GCustomer/service-subscriptions/service-subscription/5G/service-instances/service-instance/ea107578-9854-4718-8145-7c7febf0de6c",
          "relationship-data": [{
            "relationship-key": "customer.global-customer-id",
            "relationship-value": "5GCustomer"
          }, {
            "relationship-key": "service-subscription.service-type",
            "relationship-value": "5G"
          }, {
            "relationship-key": "service-instance.service-instance-id",
            "relationship-value": "ea107578-9854-4718-8145-7c7febf0de6c"
          }],
          "related-to-property": [{
            "property-key": "service-instance.service-instance-name",
            "property-value": "nsi_DemoEmbb"
          }]
        }]
      }
    }]
  },
  "slice-profiles": {
    "slice-profile": [{
    "profile-id": "31a83df8-5bd0-4df7-a50f-7900476b81a2",
    "latency": 3,
    "max-number-of-UEs": 0,
    "coverage-area-TA-list": "Beijing;Beijing;HaidianDistrict;WanshouluStreet",
    "ue-mobility-level": "stationary",
    "resource-sharing-level": "0",
    "exp-data-rate-UL": 500,
    "exp-data-rate-DL": 2000,
    "activity-factor": 0,
    "e2e-latency": 0,
    "jitter": 0,
    "survival-time": 0,
    "exp-data-rate": 0,
    "payload-size": 0,
    "traffic-density": 0,
    "conn-density": 0,
    "s-nssai": "01-5C83F071",
    "resource-version": "1593525640617"
  }]
  }
}"""
    }

    String mockServiceProfile2() {
        return """{
  "service-instance-id": "1c7046f2-a5a3-4d7f-9da8-388ee641a795",
  "service-instance-name": "DemoEmbb",
  "service-role": "e2esliceprofile-service",
  "environment-context": "01-5B179BD4",
  "model-invariant-id": "040b1b40-3120-446b-b8e3-4f21d153d12e",
  "model-version-id": "8b7dabb3-3f27-4555-a9fe-803e862b0282",
  "service-instance-location-id": "39-00",
  "resource-version": "1593511782169",
  "orchestration-status": "activated",
  "relationship-list": {
    "relationship": [{
      "related-to": "service-instance",
      "relationship-label": "org.onap.relationships.inventory.ComposedOf",
      "related-link": "/aai/v19/business/customers/customer/5GCustomer/service-subscriptions/service-subscription/5G/service-instances/service-instance/4b2bdbc0-cf7e-4c50-882a-f660e3ab8520",
      "relationship-data": [{
        "relationship-key": "customer.global-customer-id",
        "relationship-value": "5GCustomer"
      }, {
        "relationship-key": "service-subscription.service-type",
        "relationship-value": "5G"
      }, {
        "relationship-key": "service-instance.service-instance-id",
        "relationship-value": "4b2bdbc0-cf7e-4c50-882a-f660e3ab8520"
      }],
      "related-to-property": [{
        "property-key": "service-instance.service-instance-name",
        "property-value": "DemoEmbb"
      }]
    }]
  },
  "allotted-resources": {
    "allotted-resource": [{
      "id": "362e46c2-cd84-45e4-a6c1-77f4ef88328d",
      "model-invariant-id": "e5941a50-ddb4-4f74-be03-25449ae02ddc",
      "model-version-id": "ab171d60-c2cc-4903-ac1d-c451b647e461",
      "resource-version": "1593511173712",
      "type": "Allotted Resource",
      "allotted-resource-name": "Allotted_DemoEmbb",
      "relationship-list": {
        "relationship": [{
          "related-to": "service-instance",
          "relationship-label": "org.onap.relationships.inventory.Uses",
          "related-link": "/aai/v19/business/customers/customer/5GCustomer/service-subscriptions/service-subscription/5G/service-instances/service-instance/ea107578-9854-4718-8145-7c7febf0de6c",
          "relationship-data": [{
            "relationship-key": "customer.global-customer-id",
            "relationship-value": "5GCustomer"
          }, {
            "relationship-key": "service-subscription.service-type",
            "relationship-value": "5G"
          }, {
            "relationship-key": "service-instance.service-instance-id",
            "relationship-value": "ea107578-9854-4718-8145-7c7febf0de6c"
          }],
          "related-to-property": [{
            "property-key": "service-instance.service-instance-name",
            "property-value": "nsi_DemoEmbb"
          }]
        }]
      }
    }]
  },
  "slice-profiles": {
    "slice-profile": [{
    "profile-id": "b86df550-9d70-452b-a5a9-eb8823417255",
    "latency": 6,
    "max-number-of-UEs": 0,
    "coverage-area-TA-list": "Beijing;Beijing;HaidianDistrict;WanshouluStreet",
    "ue-mobility-level": "stationary",
    "resource-sharing-level": "0",
    "exp-data-rate-UL": 500,
    "exp-data-rate-DL": 1000,
    "activity-factor": 0,
    "e2e-latency": 0,
    "jitter": 0,
    "survival-time": 0,
    "exp-data-rate": 0,
    "payload-size": 0,
    "traffic-density": 0,
    "conn-density": 0,
    "s-nssai": "01-5B179BD4",
    "resource-version": "1593511356725"
  }]
  }
}"""
    }
}