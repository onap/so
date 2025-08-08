/*
 * ============LICENSE_START======================================================= ONAP : SO
 * ================================================================================ Copyright (C) 2018 TechMahindra
 * ================================================================================ Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.so.openstack.utils;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertNull;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.junit.Test;
import org.onap.so.TestDataSetup;
import org.onap.so.db.catalog.beans.HeatTemplateParam;

public class MsoYamlEditorWithEnvtTest extends TestDataSetup {
    private MsoYamlEditorWithEnvt yaml;
    private static final String PARAMETER_NAME = "keyTest";
    private static final String PARAMETER_VALUE = "{type : paramType}";
    private static final String RESOURCE_NAME = "resourceKey";
    private static final String RESOURCE_VALUE = "resourceValue";
    private static final String RAW_ENTRY_WITH_RESOURCE_REGISTRY =
            "resource_registry: {" + RESOURCE_NAME + " : " + RESOURCE_VALUE + "}";
    private static final String RAW_ENTRY_WITH_NO_RESOURCE_REGISTRY =
            "parameters: {" + PARAMETER_NAME + ": " + PARAMETER_VALUE + "}";

    @Test
    public void getParameterListTest() throws IOException {
        yaml = new MsoYamlEditorWithEnvt(RAW_ENTRY_WITH_NO_RESOURCE_REGISTRY.getBytes());

        MsoHeatEnvironmentParameter expectedHeatParam = mapper.readValue(
                new File(RESOURCE_PATH + "HeatEnvironmentParameter.json"), MsoHeatEnvironmentParameter.class);

        Set<MsoHeatEnvironmentParameter> heatEnvironmentSet = yaml.getParameterListFromEnvt();

        for (MsoHeatEnvironmentParameter heatEnvironment : heatEnvironmentSet) {
            assertThat(heatEnvironment, sameBeanAs(expectedHeatParam));
        }
    }

    @Test
    public void getResourceListFromEnvtTest() {
        yaml = new MsoYamlEditorWithEnvt(RAW_ENTRY_WITH_RESOURCE_REGISTRY.getBytes());

        MsoHeatEnvironmentResource expectedHeatResource = new MsoHeatEnvironmentResource(RESOURCE_NAME, RESOURCE_VALUE);

        Set<MsoHeatEnvironmentResource> heatResourceSet = yaml.getResourceListFromEnvt();

        for (MsoHeatEnvironmentResource heatResource : heatResourceSet) {
            assertThat(heatResource, sameBeanAs(expectedHeatResource));
        }
    }

    @Test
    public void getResourceListFromEnvtExceptionTest() {
        yaml = new MsoYamlEditorWithEnvt();

        Set<MsoHeatEnvironmentResource> heatResourceSet = yaml.getResourceListFromEnvt();

        assertNull(heatResourceSet);
    }

    @Test
    public void getParameterListFromEnvtTest() throws IOException {
        yaml = new MsoYamlEditorWithEnvt(RAW_ENTRY_WITH_NO_RESOURCE_REGISTRY.getBytes());

        HeatTemplateParam expectedHeatParam =
                mapper.readValue(new File(RESOURCE_PATH + "HeatTemplateParamExpected.json"), HeatTemplateParam.class);

        Set<HeatTemplateParam> heatParamSet = yaml.getParameterList();

        for (HeatTemplateParam heatParam : heatParamSet) {
            assertThat(heatParam, sameBeanAs(expectedHeatParam));
        }
    }
}
