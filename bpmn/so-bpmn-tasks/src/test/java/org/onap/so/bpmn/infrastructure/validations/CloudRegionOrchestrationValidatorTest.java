/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.bpmn.infrastructure.validations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.bpmn.common.BuildingBlockExecution;
import org.onap.so.bpmn.servicedecomposition.bbobjects.CloudRegion;
import org.onap.so.bpmn.servicedecomposition.entities.GeneralBuildingBlock;

public class CloudRegionOrchestrationValidatorTest {

    private BuildingBlockExecution mockExecution;
    private CloudRegion cloudRegion;

    @Before
    public void setUp() {
        cloudRegion = new CloudRegion();
        cloudRegion.setCloudOwner("CloudOwner");
        cloudRegion.setLcpCloudRegionId("my-region-id");
        GeneralBuildingBlock gbb = new GeneralBuildingBlock();
        gbb.setCloudRegion(cloudRegion);
        mockExecution = mock(BuildingBlockExecution.class);
        doReturn(gbb).when(mockExecution).getGeneralBuildingBlock();
    }

    @Test
    public void validateDisabledTest() {
        cloudRegion.setOrchestrationDisabled(true);
        CloudRegionOrchestrationValidator validation = new CloudRegionOrchestrationValidator();
        Optional<String> result = validation.validate(mockExecution);
        assertEquals(
                "Error: The request has failed due to orchestration currently disabled for the target cloud region my-region-id for cloud owner CloudOwner",
                result.get());
    }

    @Test
    public void validateNotDisabledTest() {
        cloudRegion.setOrchestrationDisabled(false);
        CloudRegionOrchestrationValidator validation = new CloudRegionOrchestrationValidator();
        Optional<String> result = validation.validate(mockExecution);
        assertFalse(result.isPresent());
    }

    @Test
    public void validateDisabledIsNullTest() {
        CloudRegionOrchestrationValidator validation = new CloudRegionOrchestrationValidator();
        Optional<String> result = validation.validate(mockExecution);
        assertFalse(result.isPresent());
    }

    @Test
    public void shouldRunForTest() {
        CloudRegionOrchestrationValidator validator = new CloudRegionOrchestrationValidator();
        assertTrue(validator.shouldRunFor("ActivateNetworkBB"));
        assertTrue(validator.shouldRunFor("AssignNetworkBB"));
        assertTrue(validator.shouldRunFor("CreateNetworkBB"));
        assertTrue(validator.shouldRunFor("DeactivateNetworkBB"));
        assertTrue(validator.shouldRunFor("DeleteNetworkBB"));
        assertTrue(validator.shouldRunFor("UnassignNetworkBB"));
        assertTrue(validator.shouldRunFor("UpdateNetworkBB"));

        assertTrue(validator.shouldRunFor("ActivateVnfBB"));
        assertTrue(validator.shouldRunFor("AssignVnfBB"));
        assertTrue(validator.shouldRunFor("UnassignVnfBB"));
        assertTrue(validator.shouldRunFor("DeactivateVnfBB"));

        assertTrue(validator.shouldRunFor("ActivateVfModuleBB"));
        assertTrue(validator.shouldRunFor("AssignVfModuleBB"));
        assertTrue(validator.shouldRunFor("CreateVfModuleBB"));
        assertTrue(validator.shouldRunFor("DeactivateVfModuleBB"));
        assertTrue(validator.shouldRunFor("DeleteVfModuleBB"));
        assertTrue(validator.shouldRunFor("UnassignVfModuleBB"));

        assertTrue(validator.shouldRunFor("ActivateVolumeGroupBB"));
        assertTrue(validator.shouldRunFor("AssignVolumeGroupBB"));
        assertTrue(validator.shouldRunFor("CreateVolumeGroupBB"));
        assertTrue(validator.shouldRunFor("DeactivateVolumeGroupBB"));
        assertTrue(validator.shouldRunFor("DeleteVolumeGroupBB"));
        assertTrue(validator.shouldRunFor("UnassignVolumeGroupBB"));

        assertTrue(validator.shouldRunFor("ActivateFabricConfigurationBB"));
        assertTrue(validator.shouldRunFor("AssignFabricConfigurationBB"));
        assertTrue(validator.shouldRunFor("UnassignFabricConfigurationBB"));
        assertTrue(validator.shouldRunFor("DeactivateFabricConfigurationBB"));

        assertFalse(validator.shouldRunFor("AssignServiceInstanceBB"));
        assertFalse(validator.shouldRunFor("AAICheckVnfInMaintBB"));
        assertFalse(validator.shouldRunFor("ChangeModelVfModuleBB"));
        assertFalse(validator.shouldRunFor("CreateNetworkCollectionBB"));
    }
}
