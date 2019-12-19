/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2020 Nokia Intellectual Property. All rights reserved.
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
package org.onap.so.bpmn.servicedecomposition.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import java.io.File;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BuildingBlockTest {
    private static final String RESOURCE_PATH = "src/test/resources/__files/BuildingBlock/";
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void simpleJson_shouldBeProperlyDeserialized() throws IOException {
        // given
        BuildingBlock expectedBuildingBlock =
                new BuildingBlock.Builder().withMsoId("msoId").withBpmnFlowName("bbName").build();
        // when
        BuildingBlock result =
                mapper.readValue(new File(RESOURCE_PATH + "BuildingBlockSimple.json"), BuildingBlock.class);
        // then
        assertEquals(expectedBuildingBlock.getBpmnFlowName(), result.getBpmnFlowName());
        assertEquals(expectedBuildingBlock.getMsoId(), result.getMsoId());
        assertNull(result.getKey());
        assertNull(result.getVirtualLinkKey());
        assertNull(result.isVirtualLink());
    }

    @Test
    public void fullJson_shouldBeProperlyDeserialized() throws IOException {
        // given
        BuildingBlock expectedBuildingBlock = new BuildingBlock.Builder().withMsoId("msoId").withBpmnFlowName("bbName")
                .withVirtualLinkKey("sampleVirtualLinkKey").withIsVirtualLink(true).withKey("sampleKey")
                .withBpmnScope("sampleScope").withBpmnAction("sampleAction").build();
        // when
        BuildingBlock result =
                mapper.readValue(new File(RESOURCE_PATH + "BuildingBlockAllParameters.json"), BuildingBlock.class);
        // then
        assertEquals(expectedBuildingBlock.getBpmnFlowName(), result.getBpmnFlowName());
        assertEquals(expectedBuildingBlock.getMsoId(), result.getMsoId());
        assertEquals(true, result.isVirtualLink());
        assertEquals(expectedBuildingBlock.getVirtualLinkKey(), result.getVirtualLinkKey());
        assertEquals(expectedBuildingBlock.getKey(), result.getKey());
        assertEquals(expectedBuildingBlock.getBpmnScope(), result.getBpmnScope());
        assertEquals(expectedBuildingBlock.getBpmnAction(), result.getBpmnAction());
    }

    @Test
    public void builderOfBuildingBlockObject_shouldBeProperlyMergedWithExistingBB() {
        // given
        BuildingBlock expectedBuildingBlock = new BuildingBlock.Builder().withBpmnFlowName("sampleFlowName")
                .withMsoId("msoId").withVirtualLinkKey("sampleVirtualLinkKey").withIsVirtualLink(true)
                .withKey("sampleKey").withBpmnScope("sampleScope").withBpmnAction("sampleAction").build();
        BuildingBlock bbWithMissingBpmnFlowName = new BuildingBlock.Builder().withMsoId("msoId")
                .withVirtualLinkKey("sampleVirtualLinkKey").withIsVirtualLink(true).withKey("sampleKey")
                .withBpmnScope("sampleScope").withBpmnAction("sampleAction").build();

        BuildingBlock.Builder builder = new BuildingBlock.Builder().withBpmnFlowName("sampleFlowName");
        // when
        BuildingBlock result = bbWithMissingBpmnFlowName.copyAndChangeBuildingBlock(builder);
        // then
        assertEquals(expectedBuildingBlock.getBpmnFlowName(), result.getBpmnFlowName());
        assertEquals(expectedBuildingBlock.getMsoId(), result.getMsoId());
        assertEquals(true, result.isVirtualLink());
        assertEquals(expectedBuildingBlock.getVirtualLinkKey(), result.getVirtualLinkKey());
        assertEquals(expectedBuildingBlock.getKey(), result.getKey());
        assertEquals(expectedBuildingBlock.getBpmnScope(), result.getBpmnScope());
        assertEquals(expectedBuildingBlock.getBpmnAction(), result.getBpmnAction());
    }

    @Test
    public void emptyBuilderOfBuildingBlockObject_shouldBeProperlyMergedWithExistingBB() {
        // given
        BuildingBlock buildingBlock = new BuildingBlock.Builder().withMsoId("msoId").withBpmnFlowName("bbName")
                .withVirtualLinkKey("sampleVirtualLinkKey").withIsVirtualLink(true).withKey("sampleKey")
                .withBpmnScope("sampleScope").withBpmnAction("sampleAction").build();

        BuildingBlock.Builder emptyBuilder = new BuildingBlock.Builder();
        // when
        BuildingBlock result = buildingBlock.copyAndChangeBuildingBlock(emptyBuilder);
        // then
        assertEquals(buildingBlock.getBpmnFlowName(), result.getBpmnFlowName());
        assertEquals(buildingBlock.getMsoId(), result.getMsoId());
        assertEquals(true, result.isVirtualLink());
        assertEquals(buildingBlock.getVirtualLinkKey(), result.getVirtualLinkKey());
        assertEquals(buildingBlock.getKey(), result.getKey());
        assertEquals(buildingBlock.getBpmnScope(), result.getBpmnScope());
        assertEquals(buildingBlock.getBpmnAction(), result.getBpmnAction());
    }
}
