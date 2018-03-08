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
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.openecomp.mso.db.catalog.beans.VfModule;
import org.openecomp.mso.db.catalog.beans.VnfResource;
import org.openecomp.mso.db.catalog.beans.VnfResourceCustomization;

/**
 */

public class VnfResourceTest {

    @Test
    public final void vnfResourceDataTest() {

        VnfResource vnfResource = new VnfResource();
        vnfResource.setCreated(new Timestamp(System.currentTimeMillis()));
        assertTrue(vnfResource.getCreated() != null);
        vnfResource.setDescription("description");
        assertTrue(vnfResource.getDescription().equalsIgnoreCase("description"));

        vnfResource.setAicVersionMax("aicVersionMax");
        assertTrue(vnfResource.getAicVersionMax().equalsIgnoreCase("aicVersionMax"));

        vnfResource.setAicVersionMin("aicVersionMin");
        assertTrue(vnfResource.getAicVersionMin().equalsIgnoreCase("aicVersionMin"));
        vnfResource.setHeatTemplateArtifactUUId("heatTemplateArtifactUUId");
        assertTrue(vnfResource.getHeatTemplateArtifactUUId().equalsIgnoreCase("heatTemplateArtifactUUId"));

        vnfResource.setModelInvariantUuid("modelInvariantUuid");
        assertTrue(vnfResource.getModelInvariantUuid().equalsIgnoreCase("modelInvariantUuid"));
        vnfResource.setModelName("modelName");
        assertTrue(vnfResource.getModelName().equalsIgnoreCase("modelName"));
        vnfResource.setModelUuid("modelUuid");
        assertTrue(vnfResource.getModelUuid().equalsIgnoreCase("modelUuid"));
        vnfResource.setModelVersion("modelVersion");
        assertTrue(vnfResource.getModelVersion().equalsIgnoreCase("modelVersion"));
        vnfResource.setOrchestrationMode("orchestrationMode");
        assertTrue(vnfResource.getOrchestrationMode().equalsIgnoreCase("orchestrationMode"));
        vnfResource.setTemplateId("heatTemplateArtifactUUId");
        assertTrue(vnfResource.getHeatTemplateArtifactUUId().equalsIgnoreCase("heatTemplateArtifactUUId"));
        vnfResource.setModelInvariantUuid("modelInvariantUuid");
        assertTrue(vnfResource.getModelInvariantUuid().equalsIgnoreCase("modelInvariantUuid"));
        Set<VnfResourceCustomization> list = new HashSet<>();
        list.add(new VnfResourceCustomization());
        vnfResource.setVnfResourceCustomizations(list);
        assertTrue(vnfResource.getVfModuleCustomizations() != null);
        Set<VfModule> vfModules = new HashSet<>();
        vfModules.add(new VfModule());
        vnfResource.setVfModules(vfModules);
        assertTrue(vnfResource.getVfModules() != null);
//      assertTrue(vnfResource.toString() != null);

    }

}
