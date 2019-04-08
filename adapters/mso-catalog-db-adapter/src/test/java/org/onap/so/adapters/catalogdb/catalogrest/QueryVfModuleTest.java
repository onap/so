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
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.onap.so.db.catalog.beans.HeatEnvironment;
import org.onap.so.db.catalog.beans.VfModule;
import org.onap.so.db.catalog.beans.VfModuleCustomization;
import org.onap.so.jsonpath.JsonPathUtil;

public class QueryVfModuleTest {

    private static final String VF_MODULE_MODEL_NAME = "vfModelNameTest";
    private static final String VF_MODEL_UUID = "vfModelUuidTest";
    private static final String VF_MODULE_INVARIANT_UUID = "vfModuleInvUuid";
    private static final String VF_MODULE_VERSION = "vfModuleVerTest";

    private static final String VF_MODEL_CUSTOMIZATION_UUID = "modelCustomizationUuid";
    private static final String VF_MODEL_CUSTOMIZATION_LABEL = "modelCustomizationLabel";
    private static final int VF_MODEL_CUSTOMIZATION_INITIAL_COUNT = 1;

    @Test
    public void convertToJson_successful() {
        QueryVfModule testedObject = new QueryVfModule(createList());
        String jsonResult = testedObject.JSON2(true, false);
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.vfModules[0].modelInfo.modelName"))
                .contains(VF_MODULE_MODEL_NAME);
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.vfModules[0].modelInfo.modelUuid"))
                .contains(VF_MODEL_UUID);
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.vfModules[0].modelInfo.modelInvariantUuid"))
                .contains(VF_MODULE_INVARIANT_UUID);
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.vfModules[0].isBase")).contains("true");
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.vfModules[0].vfModuleLabel"))
                .contains(VF_MODEL_CUSTOMIZATION_LABEL);
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.vfModules[0].initialCount"))
                .contains(String.valueOf(VF_MODEL_CUSTOMIZATION_INITIAL_COUNT));
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.vfModules[0].hasVolumeGroup"))
                .contains("false");
    }

    @Test
    public void convertToJson_successful_hasVolumeGroupIsTrue() {
        QueryVfModule testedObject = new QueryVfModule(createListWithHeatEnvironmentArtifactUuid());
        String jsonResult = testedObject.JSON2(true, false);
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.vfModules[0].hasVolumeGroup"))
                .contains("true");
    }

    private List<VfModuleCustomization> createList() {
        List<VfModuleCustomization> list = new ArrayList<>();

        VfModule vfModule = new VfModule();
        vfModule.setModelName(VF_MODULE_MODEL_NAME);
        vfModule.setModelUUID(VF_MODEL_UUID);
        vfModule.setModelInvariantUUID(VF_MODULE_INVARIANT_UUID);
        vfModule.setModelVersion(VF_MODULE_VERSION);
        vfModule.setIsBase(true);

        VfModuleCustomization vfModuleCustomization = new VfModuleCustomization();
        vfModuleCustomization.setVfModule(vfModule);
        vfModuleCustomization.setModelCustomizationUUID(VF_MODEL_CUSTOMIZATION_UUID);
        vfModuleCustomization.setLabel(VF_MODEL_CUSTOMIZATION_LABEL);
        vfModuleCustomization.setInitialCount(VF_MODEL_CUSTOMIZATION_INITIAL_COUNT);
        list.add(vfModuleCustomization);
        return list;
    }

    private List<VfModuleCustomization> createListWithHeatEnvironmentArtifactUuid() {
        List<VfModuleCustomization> list = createList();
        HeatEnvironment he = new HeatEnvironment();
        he.setArtifactUuid("heatEnvTest");
        list.get(0).setVolumeHeatEnv(he);
        return list;
    }

}
