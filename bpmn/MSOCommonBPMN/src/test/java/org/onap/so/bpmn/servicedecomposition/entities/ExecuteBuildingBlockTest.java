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
        // when
        ExecuteBuildingBlock resultEBB = mapper.readValue(new File(RESOURCE_PATH + "ExecuteBuildingBlockExtended.json"),
                ExecuteBuildingBlock.class);
        BuildingBlock resultBB = resultEBB.getBuildingBlock();
        // then
        assertThat(resultBB.getMsoId()).isEqualTo("msoId");
        assertThat(resultBB.getBpmnFlowName()).isEqualTo("bbName");
        assertThat(resultEBB.getRequestId()).isEqualTo("requestId");
        assertThat(resultEBB.isaLaCarte()).isTrue();
        assertThat(resultEBB.getApiVersion()).isNull();
        assertThat(resultEBB.getResourceId()).isNull();
        assertThat(resultEBB.getRequestAction()).isNull();
        assertThat(resultEBB.getVnfType()).isNull();
        assertThat(resultEBB.isHoming()).isFalse();
        assertThat(resultEBB.getWorkflowResourceIds()).isNull();
        assertThat(resultEBB.getRequestDetails()).isNull();
        assertThat(resultEBB.getConfigurationResourceKeys()).isNull();
    }
}
