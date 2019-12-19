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
package org.onap.so.bpmn.servicedecomposition.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ExecuteBuildingBlockTest {
    private static final String RESOURCE_PATH = "src/test/resources/__files/ExecuteBuildingBlock/";
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void should_properlyDeserializeGivenJsonFile() throws IOException {
        // given
        BuildingBlock buildingBlock = new BuildingBlock().setMsoId("msoId").setBpmnFlowName("bbName");
        ExecuteBuildingBlock expectedEBB = new ExecuteBuildingBlock().setBuildingBlock(buildingBlock)
                .setRequestId("requestId").setaLaCarte(true);
        // when
        ExecuteBuildingBlock result =
                mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockExtended.json"),
                        ExecuteBuildingBlock.class);
        BuildingBlock resultBuildingBlock = result.getBuildingBlock();
        // then
        assertThat(resultBuildingBlock.getMsoId()).isEqualTo("msoId");
        assertThat(resultBuildingBlock.getBpmnFlowName()).isEqualTo("bbName");
        assertThat(result.getRequestId()).isEqualTo("requestId");
        assertThat(result.isaLaCarte()).isTrue();
        assertThat(result.getApiVersion()).isNull();
        assertThat(result.getResourceId()).isNull();
        assertThat(result.getRequestAction()).isNull();
        assertThat(result.getVnfType()).isNull();
        assertThat(result.isHoming()).isFalse();
        assertThat(result.getWorkflowResourceIds()).isNull();
        assertThat(result.getRequestDetails()).isNull();
        assertThat(result.getConfigurationResourceKeys()).isNull();
    }
}