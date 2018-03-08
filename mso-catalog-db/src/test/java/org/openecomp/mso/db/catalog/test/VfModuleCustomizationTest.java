/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.db.catalog.test;

import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import org.junit.Test;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VfModuleCustomization;

/**
 */

public class VfModuleCustomizationTest {

    @Test
    public final void vfModuleCustomizationDataTest() {
        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setCreated(new Timestamp(System.currentTimeMillis()));
        assertTrue(vfModuleCustomization.getCreated() != null);
        vfModuleCustomization.setAvailabilityZoneCount(1);
        assertTrue(vfModuleCustomization.getAvailabilityZoneCount() == 1);
        vfModuleCustomization.hashCode();
        vfModuleCustomization.setVolEnvironmentArtifactUuid("volEnvironmentArtifactUuid");
        assertTrue(
                vfModuleCustomization.getVolEnvironmentArtifactUuid().equalsIgnoreCase("volEnvironmentArtifactUuid"));

        vfModuleCustomization.setHeatEnvironmentArtifactUuid("heatEnvironmentArtifactUuid");
        assertTrue(
                vfModuleCustomization.getHeatEnvironmentArtifactUuid().equalsIgnoreCase("heatEnvironmentArtifactUuid"));

        vfModuleCustomization.setInitialCount(1);
        assertTrue(vfModuleCustomization.getInitialCount() == 1);

        vfModuleCustomization.setLabel("label");
        assertTrue(vfModuleCustomization.getLabel().equalsIgnoreCase("label"));
        vfModuleCustomization.setMaxInstances(2);
        assertTrue(vfModuleCustomization.getMaxInstances() == 2);
        vfModuleCustomization.setMinInstances(1);
        assertTrue(vfModuleCustomization.getMinInstances() == 1);
        vfModuleCustomization.setModelCustomizationUuid("modelCustomizationUuid");
        assertTrue(vfModuleCustomization.getModelCustomizationUuid().equalsIgnoreCase("modelCustomizationUuid"));
        vfModuleCustomization.setVfModule(new VfModule());
        assertTrue(vfModuleCustomization.getVfModule() != null);

//      assertTrue(vfModuleCustomization.toString() == null);

    }

}
