/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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


import org.junit.Test;
import org.onap.so.bpmn.servicedecomposition.entities.BuildingBlock;
import org.onap.so.bpmn.servicedecomposition.entities.ExecuteBuildingBlock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ExecuteBuildingBlockTest {
    @Test
    public void builder_shouldBeProperlyMergedWithExecuteBuildingBlock() {
        // given
        BuildingBlock buildingBlock = new BuildingBlock.Builder().build();
        ExecuteBuildingBlock executeBuildingBlock = new ExecuteBuildingBlock.Builder().withApiVersion("0.0.0")
                .withBuildingBlock(buildingBlock).withHoming(true).withRequestId("888").build();
        ExecuteBuildingBlock.Builder builder = new ExecuteBuildingBlock.Builder().withApiVersion("1.3.4");

        ExecuteBuildingBlock expectedExecuteBuildingBlock = new ExecuteBuildingBlock.Builder().withApiVersion("1.3.4")
                .withBuildingBlock(buildingBlock).withHoming(true).withRequestId("888").build();
        // when
        ExecuteBuildingBlock result = executeBuildingBlock.copyAndChangeExecuteBuildingBlock(builder);
        // then
        assertEquals(expectedExecuteBuildingBlock.getApiVersion(), result.getApiVersion());
        assertEquals(expectedExecuteBuildingBlock.getBuildingBlock(), result.getBuildingBlock());
        assertEquals(expectedExecuteBuildingBlock.isHoming(), result.isHoming());
        assertEquals(expectedExecuteBuildingBlock.getRequestId(), result.getRequestId());
        assertNull(result.getConfigurationResourceKeys());
        assertNull(result.getRequestDetails());
        assertNull(result.isaLaCarte());
        assertNull(result.getWorkflowResourceIds());
        assertNull(result.getVnfType());
        assertNull(result.getRequestAction());
        assertNull(result.getResourceId());
    }

}
