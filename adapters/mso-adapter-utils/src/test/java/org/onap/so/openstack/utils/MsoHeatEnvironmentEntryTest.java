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

package org.onap.so.openstack.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.Test;
import org.onap.so.TestDataSetup;
import org.onap.so.db.catalog.beans.HeatTemplateParam;

public class MsoHeatEnvironmentEntryTest extends TestDataSetup {

    private static final String PARAMETER_NAME = "keyTest";
    private static final String VALUE_NAME = "valueTest";
    private static final String NOT_EXISTING_PARAM = "notExistingParam";
    private static final String RAW_ENTRY_WITH_NO_RESOURCE_REGISTRY =
            "parameters: {" + PARAMETER_NAME + ": " + VALUE_NAME + "}";
    private static final String RAW_ENTRY_WITH_RESOURCE_REGISTRY = "resource_registry: resourceTest";
    private static final String RAW_ENTRY_INVALID = "invalidRawEntry";

    @Test
    public void createObjectWithNullStringBuilder() {
        MsoHeatEnvironmentEntry testedObject = new MsoHeatEnvironmentEntry(null);
        assertThat(testedObject.isValid()).isTrue();
        assertThat(testedObject.getRawEntry()).isNull();
        assertThat(testedObject.containsParameter(PARAMETER_NAME)).isFalse();
    }

    @Test
    public void toFullString_ResourceRegistryNotPresentInRawEntry()
            throws IOException {
        StringBuilder sb = new StringBuilder(RAW_ENTRY_WITH_NO_RESOURCE_REGISTRY);

        MsoHeatEnvironmentEntry testedObject = new MsoHeatEnvironmentEntry(sb);

        HeatTemplateParam heatTemplateParam =
                mapper.readValue(new File(RESOURCE_PATH + "HeatTemplateParam.json"), HeatTemplateParam.class);

        assertThat(testedObject.getRawEntry()).isEqualTo(sb);
        assertThat(testedObject.isValid()).isTrue();
        assertThat(testedObject.containsParameter(PARAMETER_NAME)).isTrue();
        assertThat(testedObject.toString()).doesNotContain(RAW_ENTRY_WITH_RESOURCE_REGISTRY);
        assertTrue(testedObject.containsParameter(PARAMETER_NAME, "dummyAlias"));
        assertTrue(testedObject.containsParameter("dummyName", PARAMETER_NAME));
        assertFalse(testedObject.containsParameter("dummyName", "dummyAlias"));
        assertEquals("parameters:\n   " + PARAMETER_NAME + ":  " + VALUE_NAME + "\n\n\n",
                testedObject.toFullString().toString());
        assertEquals("parameters:\n  " + PARAMETER_NAME + ": " + VALUE_NAME + "\n\n\n",
                testedObject
                        .toFullStringExcludeNonParams(new HashSet<HeatTemplateParam>(Arrays.asList(heatTemplateParam)))
                        .toString());
        assertEquals(1, testedObject.getNumberOfParameters());
        assertFalse(testedObject.hasResources());

        MsoHeatEnvironmentResource heatResource = new MsoHeatEnvironmentResource("resourceName", "resourceValue");
        MsoHeatEnvironmentParameter heatParameter = new MsoHeatEnvironmentParameter("parameterName", "parameterValue");
        testedObject.addResource(heatResource);
        testedObject.addParameter(heatParameter);
        assertEquals(1, testedObject.getNumberOfResources());
        assertEquals(2, testedObject.getNumberOfParameters());

        testedObject.setResources(null);
        testedObject.setParameters(null);
        assertNull(testedObject.getParameters());
        assertNull(testedObject.getResources());
    }

    @Test
    public void toFullString_ExceptionOccurred() {
        StringBuilder sb = new StringBuilder(RAW_ENTRY_INVALID);
        MsoHeatEnvironmentEntry testedObject = new MsoHeatEnvironmentEntry(sb);
        assertThat(testedObject.getRawEntry()).isEqualTo(sb);
        assertThat(testedObject.isValid()).isFalse();
        assertThat(testedObject.getErrorString()).isNotNull().isNotEmpty();
    }

    @Test
    public void checkIfContainsTheParameter() {
        StringBuilder sb = new StringBuilder(RAW_ENTRY_WITH_NO_RESOURCE_REGISTRY);
        MsoHeatEnvironmentEntry testedObject = new MsoHeatEnvironmentEntry(sb);
        assertThat(testedObject.getRawEntry()).isEqualTo(sb);
        assertThat(testedObject.isValid()).isTrue();
        assertThat(testedObject.containsParameter(PARAMETER_NAME)).isTrue();
        assertThat(testedObject.containsParameter(NOT_EXISTING_PARAM)).isFalse();
    }
}
