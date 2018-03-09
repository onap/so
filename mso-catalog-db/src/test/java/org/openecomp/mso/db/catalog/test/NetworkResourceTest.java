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
import org.openecomp.mso.db.catalog.beans.NetworkResource;

/**
 */

public class NetworkResourceTest {

    @Test
    public final void networkResourceDataTest() {
        NetworkResource networkResource = new NetworkResource();
        networkResource.setAicVersionMax("aicVersionMax");
        assertTrue(networkResource.getAicVersionMax().equalsIgnoreCase("aicVersionMax"));
        networkResource.setAicVersionMin("aicVersionMin");
        assertTrue(networkResource.getAicVersionMin().equalsIgnoreCase("aicVersionMin"));
        networkResource.setCreated(new Timestamp(System.currentTimeMillis()));
        assertTrue(networkResource.getCreated() != null);
        networkResource.setDescription("description");
        assertTrue(networkResource.getDescription().equalsIgnoreCase("description"));
        networkResource.setHeatTemplateArtifactUUID("heatTemplateArtifactUUID");
        assertTrue(networkResource.getHeatTemplateArtifactUUID().equalsIgnoreCase("heatTemplateArtifactUUID"));
        networkResource.setModelInvariantUUID("modelInvariantUUID");
        assertTrue(networkResource.getModelInvariantUUID().equalsIgnoreCase("modelInvariantUUID"));
        networkResource.setModelName("modelName");
        assertTrue(networkResource.getModelName().equalsIgnoreCase("modelName"));
        networkResource.setModelUUID("modelUUID");
        assertTrue(networkResource.getModelUUID().equalsIgnoreCase("modelUUID"));
        networkResource.setModelVersion("modelVersion");
        assertTrue(networkResource.getModelVersion().equalsIgnoreCase("modelVersion"));
        networkResource.setNeutronNetworkType("neutronNetworkType");
        assertTrue(networkResource.getNeutronNetworkType().equalsIgnoreCase("neutronNetworkType"));
        networkResource.setOrchestrationMode("orchestrationMode");
        assertTrue(networkResource.getOrchestrationMode().equalsIgnoreCase("orchestrationMode"));
        networkResource.setToscaNodeType("toscaNodeType");
        assertTrue(networkResource.getToscaNodeType().equalsIgnoreCase("toscaNodeType"));
        networkResource.setVersion("1");
        assertTrue(networkResource.getVersion().equalsIgnoreCase("1"));
//		assertTrue(networkResource.toString() != null);

    }

}
