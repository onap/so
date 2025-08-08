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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.onap.aai.domain.yang.L3Network;
import org.onap.so.bpmn.BaseTaskTest;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.aaiclient.client.aai.entities.AAIResultWrapper;

public class NetworkBBUtilsTest extends BaseTaskTest {
    @InjectMocks
    private NetworkBBUtils networkBBUtils = new NetworkBBUtils();

    private final static String JSON_FILE_LOCATION = "src/test/resources/__files/BuildingBlocks/Network/";

    private CloudRegion cloudRegion;

    @Before
    public void before() {
        cloudRegion = setCloudRegion();
    }

    @Test
    public void isRelationshipRelatedToExistsTrueTest() throws Exception {
        final String aaiResponse = new String(
                Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "unassignNetworkBB_queryAAIResponse_.json")));
        AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(aaiResponse);
        Optional<L3Network> l3network = aaiResultWrapper.asBean(L3Network.class);

        boolean isVfModule = networkBBUtils.isRelationshipRelatedToExists(l3network, "vf-module");
        assertTrue(isVfModule);

    }

    @Test
    public void isRelationshipRelatedToExistsFalseTest() throws Exception {
        final String aaiResponse =
                new String(Files.readAllBytes(Paths.get(JSON_FILE_LOCATION + "queryAAIResponse.json")));
        AAIResultWrapper aaiResultWrapper = new AAIResultWrapper(aaiResponse);
        Optional<L3Network> l3network = aaiResultWrapper.asBean(L3Network.class);

        boolean isVfModule = networkBBUtils.isRelationshipRelatedToExists(l3network, "vf-module");
        assertFalse(isVfModule);

    }

    @Test
    public void getCloudRegionSDNC25Test() {
        cloudRegion.setCloudRegionVersion("2.5");

        NetworkBBUtils spyAssign = Mockito.spy(NetworkBBUtils.class);
        String cloudRegionId = spyAssign.getCloudRegion(execution, SourceSystem.SDNC);
        Mockito.verify(spyAssign).getCloudRegion(execution, SourceSystem.SDNC);

        assertEquals("AAIAIC25", cloudRegionId);
    }

    @Test
    public void getCloudRegionSDNC30Test() {
        cloudRegion.setCloudRegionVersion("3.0");

        NetworkBBUtils spyAssign = Mockito.spy(NetworkBBUtils.class);
        String cloudRegionId = spyAssign.getCloudRegion(execution, SourceSystem.SDNC);
        Mockito.verify(spyAssign).getCloudRegion(execution, SourceSystem.SDNC);

        assertEquals(cloudRegion.getLcpCloudRegionId(), cloudRegionId);
    }

    @Test
    public void getCloudRegionPO25Test() {
        cloudRegion.setCloudRegionVersion("2.5");

        NetworkBBUtils spyAssign = Mockito.spy(NetworkBBUtils.class);
        String cloudRegionId = spyAssign.getCloudRegion(execution, SourceSystem.PO);
        Mockito.verify(spyAssign).getCloudRegion(execution, SourceSystem.PO);

        assertEquals(cloudRegion.getLcpCloudRegionId(), cloudRegionId);
    }

    @Test
    public void getCloudRegionPO30Test() {
        cloudRegion.setCloudRegionVersion("3.0");

        NetworkBBUtils spyAssignPO = Mockito.spy(NetworkBBUtils.class);
        String cloudRegionIdPO = spyAssignPO.getCloudRegion(execution, SourceSystem.PO);
        Mockito.verify(spyAssignPO).getCloudRegion(execution, SourceSystem.PO);

        assertEquals(cloudRegion.getLcpCloudRegionId(), cloudRegionIdPO);
    }
}
