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

package org.openecomp.mso.openstack.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MsoHeatEnvironmentEntryTest {

    private static final String PARAMETER_NAME = "keyTest";
    private static final String VALUE_NAME = "valueTest";
    private static final String NOT_EXISTING_PARAM = "notExistingParam";
    private static final String RAW_ENTRY_WITH_NO_RESOURCE_REGISTRY = "parameters: {"
            + PARAMETER_NAME + ": " + VALUE_NAME + "}";
    private static final String RAW_ENTRY_WITH_RESOURCE_REGISTRY = "resource_registry: resourceTest";
    private static final String RAW_ENTRY_INVALID = "invalidRawEntry";
    private static final String WHITESPACE = " ";

    @Test
    public void createObjectWithNullStringBuilder() {
        MsoHeatEnvironmentEntry testedObject = new MsoHeatEnvironmentEntry(null);
        assertThat(testedObject.getRawEntry()).isNull();
        assertThat(testedObject.containsParameter(PARAMETER_NAME)).isFalse();
    }

    @Test
    public void toFullString_ResourceRegistryNotPresentInRawEntry() {
        MsoHeatEnvironmentEntry testedObject = new MsoHeatEnvironmentEntry(
                new StringBuilder(RAW_ENTRY_WITH_NO_RESOURCE_REGISTRY));
        assertThat(testedObject.toFullString()).hasToString(createExpectedStringBuilderString());
    }

    @Test
    public void toFullString_ResourceRegistryPresentInRawEntry() {
        MsoHeatEnvironmentEntry testedObject = new MsoHeatEnvironmentEntry(
                new StringBuilder(RAW_ENTRY_WITH_RESOURCE_REGISTRY));
        assertThat(testedObject.toFullString()).hasToString("\n" + RAW_ENTRY_WITH_RESOURCE_REGISTRY);
    }

    @Test
    public void toFullString_ExceptionOccurred() {
        MsoHeatEnvironmentEntry testedObject = new MsoHeatEnvironmentEntry(new StringBuilder(RAW_ENTRY_INVALID));
        assertThat(testedObject.isValid()).isFalse();
        assertThat(testedObject.getErrorString()).isNotNull().isNotEmpty();
    }

    @Test
    public void checkIfContainsTheParameter() {
        MsoHeatEnvironmentEntry testedObject = new MsoHeatEnvironmentEntry
                (new StringBuilder(RAW_ENTRY_WITH_NO_RESOURCE_REGISTRY));
        assertThat(testedObject.containsParameter(PARAMETER_NAME)).isTrue();
        assertThat(testedObject.containsParameter(NOT_EXISTING_PARAM)).isFalse();
    }

    private String createExpectedStringBuilderString() {
        return "parameters:\n" + WHITESPACE + WHITESPACE + WHITESPACE
                + PARAMETER_NAME + ":" + WHITESPACE + WHITESPACE + VALUE_NAME + "\n\n\n";
    }
}
