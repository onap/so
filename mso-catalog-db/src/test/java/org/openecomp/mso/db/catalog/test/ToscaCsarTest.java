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
import org.openecomp.mso.db.catalog.beans.ToscaCsar;

/**
 */

public class ToscaCsarTest {

    @Test
    public final void toscaCsarDataTest() {
        ToscaCsar toscaCsar = new ToscaCsar();
        toscaCsar.setCreated(new Timestamp(System.currentTimeMillis()));
        assertTrue(toscaCsar.getCreated() != null);
        toscaCsar.setDescription("description");
        assertTrue(toscaCsar.getDescription().equalsIgnoreCase("description"));

        toscaCsar.setArtifactChecksum("artifactChecksum");
        assertTrue(toscaCsar.getArtifactChecksum().equalsIgnoreCase("artifactChecksum"));

        toscaCsar.setArtifactUUID("artifactUUID");
        assertTrue(toscaCsar.getArtifactUUID().equalsIgnoreCase("artifactUUID"));

        toscaCsar.setName("name");
        assertTrue(toscaCsar.getName().equalsIgnoreCase("name"));
        toscaCsar.setUrl("url");
        assertTrue(toscaCsar.getUrl().equalsIgnoreCase("url"));
//      assertTrue(toscaCsar.toString() != null);

    }

}
