/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2019 Huawei Intellectual Property. All rights reserved.
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

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.onap.so.db.catalog.beans.VFCInstanceGroup;
import org.onap.so.db.catalog.beans.VnfcCustomization;
import org.onap.so.db.catalog.beans.VnfcInstanceGroupCustomization;
import org.onap.so.jsonpath.JsonPathUtil;
import java.util.Arrays;
import java.util.List;

public class QueryGroupsTest {

    @Test
    public void convertToJson_successful() {
        QueryGroups queryGroups = new QueryGroups(createList());
        String jsonResult = queryGroups.JSON2(true, false);

        Assertions.assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.groups[0].modelInfo.modelName"))
                .contains("test");
        Assertions
                .assertThat(
                        JsonPathUtil.getInstance().locateResult(jsonResult, "$.groups[0].vnfcs[0].modelInfo.modelName"))
                .contains("test");
    }

    private List<VnfcInstanceGroupCustomization> createList() {

        VnfcCustomization vnfcCustomization = new VnfcCustomization();
        vnfcCustomization.setModelCustomizationUUID("test");
        vnfcCustomization.setModelVersion("test");
        vnfcCustomization.setModelInvariantUUID("test");
        vnfcCustomization.setModelName("test");

        VFCInstanceGroup vfcInstanceGroup = new VFCInstanceGroup();
        vfcInstanceGroup.setModelName("test");
        vfcInstanceGroup.setModelUUID("test");
        vfcInstanceGroup.setModelInvariantUUID("test");
        vfcInstanceGroup.setModelVersion("test");

        VnfcInstanceGroupCustomization vnfcInstanceGroupCustomization = new VnfcInstanceGroupCustomization();
        vnfcInstanceGroupCustomization.setVnfcCustomizations(Arrays.asList(vnfcCustomization));
        vnfcInstanceGroupCustomization.setInstanceGroup(vfcInstanceGroup);


        vfcInstanceGroup.setVnfcInstanceGroupCustomizations(Arrays.asList(vnfcInstanceGroupCustomization));
        return Arrays.asList(vnfcInstanceGroupCustomization);
    }
}
