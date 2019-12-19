package org.onap.so.bpmn.servicedecomposition.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class BuildingBlockTest {
    private static final String RESOURCE_PATH = "src/test/resources/__files/BuildingBlock/";
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void simpleJson_shouldBeProperlyDeserialized() throws IOException {
        // given
        BuildingBlock expectedBuildingBlock = new BuildingBlock.Builder().withMsoId("msoId").withBpmnFlowName("bbName")
                .build();
        // when
        BuildingBlock result = mapper.readValue(new File(RESOURCE_PATH + "BuildingBlockSimple.json"),
                BuildingBlock.class);
        // then
        assertEquals(result.getBpmnFlowName(), expectedBuildingBlock.getBpmnFlowName());
        assertEquals(result.getMsoId(), expectedBuildingBlock.getMsoId());
    }

    @Test
    public void fullJson_shouldBeProperlyDeserialized() throws IOException {
        // given
        BuildingBlock expectedBuildingBlock = new BuildingBlock.Builder().withMsoId("msoId").withBpmnFlowName("bbName")
                .withVirtualLinkKey("sampleVirtualLinkKey").withIsVirtualLink(true).withKey("sampleKey").build();
        // when
        BuildingBlock result = mapper.readValue(new File(RESOURCE_PATH + "BuildingBlockAllParameters.json"),
                BuildingBlock.class);
        // then
        assertEquals(result.getBpmnFlowName(), expectedBuildingBlock.getBpmnFlowName());
        assertEquals(result.getMsoId(), expectedBuildingBlock.getMsoId());
        assertEquals(true, expectedBuildingBlock.getIsVirtualLink());
        assertEquals(result.getVirtualLinkKey(), expectedBuildingBlock.getVirtualLinkKey());
        assertEquals(result.getKey(), expectedBuildingBlock.getKey());

    }
}
