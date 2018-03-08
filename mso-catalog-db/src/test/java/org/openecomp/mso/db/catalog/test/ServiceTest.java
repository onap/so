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
import org.openecomp.mso.db.catalog.beans.Service;

/**
 */

public class ServiceTest {

    @Test
    public final void recipeDataTest() {
        Service service = new Service();
        service.setCreated(new Timestamp(System.currentTimeMillis()));
        assertTrue(service.getCreated() != null);
        service.setDescription("description");
        assertTrue(service.getDescription().equalsIgnoreCase("description"));

        service.setModelInvariantUUID("action");
        assertTrue(service.getModelInvariantUUID().equalsIgnoreCase("action"));

        service.setModelName("modelName");
        assertTrue(service.getModelName().equalsIgnoreCase("modelName"));

        service.setModelUUID("modelUUID");
        assertTrue(service.getModelUUID().equalsIgnoreCase("modelUUID"));
        service.setModelVersion("modelVersion");
        assertTrue(service.getModelVersion().equalsIgnoreCase("modelVersion"));
        service.setServiceRole("serviceRole");
        assertTrue(service.getServiceRole().equalsIgnoreCase("serviceRole"));
        service.setToscaCsarArtifactUUID("toscaCsarArtifactUUID");
        assertTrue(service.getToscaCsarArtifactUUID().equalsIgnoreCase("toscaCsarArtifactUUID"));

        service.setServiceType("serviceType");
        assertTrue(service.getServiceType().equalsIgnoreCase("serviceType"));
        service.setRecipes(null);
        assertTrue(service.getRecipes() == null);
        service.setServiceResourceCustomizations(null);
        assertTrue(service.getServiceResourceCustomizations() == null);

    }

}
