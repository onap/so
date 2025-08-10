/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.flowspecific.tasks;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.bbobjects.L3Network;
import org.onap.so.bpmn.servicedecomposition.entities.ResourceKey;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;

public class UnassignNetworkBBTest extends BaseTaskTest {

    @Mock
    private NetworkBBUtils networkBBUtils;

    @InjectMocks
    private UnassignNetworkBB unassignNetworkBB = new UnassignNetworkBB();

    private final static String JSON_FILE_LOCATION = "src/test/resources/__files/BuildingBlocks/Network/";
    private L3Network network;

    @Before
    public void setup() {
        doThrow(new BpmnError("BPMN Error")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(Exception.class));
        doThrow(new BpmnError("BPMN Error")).when(exceptionUtil)
                .buildAndThrowWorkflowException(any(BuildingBlockExecution.class), eq(7000), any(String.class));
    }

    @Test
    public void checkRelationshipRelatedToTrueTest() throws Exception {
        expectedException.expect(BpmnError.class);
        network = setL3Network();
        network.setNetworkId("testNetworkId1");
        final String aaiResponse = new String(
                Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "unassignNetworkBB_queryAAIResponse_.json")));
        AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(aaiResponse);
        Optional<org.onap.aai.domain.yang.L3Network> l3network =
                aaiResultWrapper.asBean(org.onap.aai.domain.yang.L3Network.class);

        doReturn(network).when(extractPojosForBB).extractByKey(execution, ResourceKey.NETWORK_ID);
        doReturn(aaiResultWrapper).when(aaiNetworkResources).queryNetworkWrapperById(network);

        doReturn(true).when(networkBBUtils).isRelationshipRelatedToExists(any(Optional.class), eq("vf-module"));

        unassignNetworkBB.checkRelationshipRelatedTo(execution, "vf-module");
        assertThat(execution.getVariable("ErrorUnassignNetworkBB"), notNullValue());
    }

    @Test
    public void checkRelationshipRelatedToUnassignNetworkExceptionTest() {
        String msg = "Cannot perform Unassign Network. Network is still related to vf-module";
        expectedException.expect(BpmnError.class);
        doReturn(true).when(networkBBUtils).isRelationshipRelatedToExists(any(Optional.class), eq("vf-module"));
        unassignNetworkBB.checkRelationshipRelatedTo(execution, "vf-module");
        assertEquals(execution.getVariable("ErrorUnassignNetworkBB"), msg);
    }

    @Test
    public void getCloudSdncRegion25Test() {
        CloudRegion cloudRegion = setCloudRegion();
        cloudRegion.setCloudRegionVersion("2.5");
        doReturn("AAIAIC25").when(networkBBUtils).getCloudRegion(execution, SourceSystem.SDNC);
        unassignNetworkBB.getCloudSdncRegion(execution);
        assertEquals("AAIAIC25", execution.getVariable("cloudRegionSdnc"));
    }

    @Test
    public void getCloudSdncRegion30Test() {
        CloudRegion cloudRegion = setCloudRegion();
        cloudRegion.setCloudRegionVersion("3.0");
        gBBInput.setCloudRegion(cloudRegion);
        doReturn(cloudRegion.getLcpCloudRegionId()).when(networkBBUtils).getCloudRegion(execution, SourceSystem.SDNC);
        unassignNetworkBB.getCloudSdncRegion(execution);
        assertEquals(cloudRegion.getLcpCloudRegionId(), execution.getVariable("cloudRegionSdnc"));
    }

    @Test
    public void errorEncounteredTest_rollback() {
        expectedException.expect(BpmnError.class);
        execution.setVariable("ErrorUnassignNetworkBB",
                "Relationship's RelatedTo still exists in AAI, remove the relationship vf-module first.");
        execution.setVariable("isRollbackNeeded", true);
        unassignNetworkBB.errorEncountered(execution);
    }

    @Test
    public void errorEncounteredTest_noRollback() {
        expectedException.expect(BpmnError.class);
        execution.setVariable("ErrorUnassignNetworkBB",
                "Relationship's RelatedTo still exists in AAI, remove the relationship vf-module first.");
        unassignNetworkBB.errorEncountered(execution);
    }
}
