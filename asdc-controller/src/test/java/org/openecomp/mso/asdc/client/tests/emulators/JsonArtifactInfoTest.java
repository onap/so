/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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

package org.openecomp.mso.asdc.client.tests.emulators;
import org.junit.Test;

import org.openecomp.mso.asdc.client.test.emulators.JsonArtifactInfo;


import java.util.ArrayList;
import java.util.List;

public class JsonArtifactInfoTest {
    JsonArtifactInfo jsonArtifactInfo = new JsonArtifactInfo();

    List<JsonArtifactInfo> artifactList = new ArrayList<>();

    @Test
    public final void addArtifactToUUIDMap()
    {
        jsonArtifactInfo.addArtifactToUUIDMap(artifactList);
    }

    @Test
    public final void setAttribute()
    {
        jsonArtifactInfo.setAttribute("artifactName", "test");
    }


    @Test
    public final void getArtifactDescription()
    {
        final String artifactDescription = jsonArtifactInfo.getArtifactDescription();
        final String artifactName = jsonArtifactInfo.getArtifactName();
        final String artifactChecksumfinal = jsonArtifactInfo.getArtifactChecksum();
        final String artifactChecksum = jsonArtifactInfo.getArtifactChecksum();
        final Integer artifactTimeout = jsonArtifactInfo.getArtifactTimeout();
        final String artifactType =  jsonArtifactInfo.getArtifactType();
        final String artifactURL = jsonArtifactInfo.getArtifactURL();
        final String artifactUUID = jsonArtifactInfo.getArtifactUUID();
        final String artifactVersion = jsonArtifactInfo.getArtifactVersion();
        jsonArtifactInfo.getGeneratedArtifact();
        jsonArtifactInfo.getRelatedArtifacts();

    }


}
