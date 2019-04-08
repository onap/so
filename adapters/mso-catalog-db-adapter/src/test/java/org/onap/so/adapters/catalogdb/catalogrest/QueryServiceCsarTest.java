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

package org.onap.so.adapters.catalogdb.catalogrest;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.onap.so.db.catalog.beans.ToscaCsar;
import org.onap.so.jsonpath.JsonPathUtil;

public class QueryServiceCsarTest {

    private static final String TOSCA_ARTIFACT_UUID = "artUuidTest";
    private static final String TOSCA_NAME = "toscaNameTest";
    private static final String TOSCA_VERSION = "v12";
    private static final String TOSCA_ARTIFACT_CHECKSUM = "tosArtCheck";
    private static final String TOSCA_URL = "tosUrl";
    private static final String TOSCA_DESCRIPTION = "toscaDescr";

    private QueryServiceCsar testedObject;

    @Before
    public void init() {
        testedObject = new QueryServiceCsar(createToscaCsar());
    }

    @Test
    public void convertToJson_successful() {
        String jsonResult = testedObject.JSON2(true, true);
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.artifactUUID")).contains(TOSCA_ARTIFACT_UUID);
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.name")).contains(TOSCA_NAME);
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.version")).contains(TOSCA_VERSION);
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.artifactChecksum"))
                .contains(String.valueOf(TOSCA_ARTIFACT_CHECKSUM));
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.url")).contains(TOSCA_URL);
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.description")).contains(TOSCA_DESCRIPTION);
    }

    private ToscaCsar createToscaCsar() {
        ToscaCsar toscaCsar = new ToscaCsar();
        toscaCsar.setArtifactUUID(TOSCA_ARTIFACT_UUID);
        toscaCsar.setName(TOSCA_NAME);
        toscaCsar.setVersion(TOSCA_VERSION);
        toscaCsar.setArtifactChecksum(TOSCA_ARTIFACT_CHECKSUM);
        toscaCsar.setUrl(TOSCA_URL);
        toscaCsar.setDescription(TOSCA_DESCRIPTION);
        return toscaCsar;
    }

}
