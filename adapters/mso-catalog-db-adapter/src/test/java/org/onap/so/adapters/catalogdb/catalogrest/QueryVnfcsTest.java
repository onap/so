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

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.onap.so.db.catalog.beans.VnfcCustomization;
import org.onap.so.jsonpath.JsonPathUtil;
import java.util.ArrayList;
import java.util.List;

public class QueryVnfcsTest {

    @Test
    public void convertToJson_successful() {
        QueryVnfcs queryVnfcs = new QueryVnfcs(createList());
        String jsonResult = queryVnfcs.JSON2(true, false);
        System.out.println(jsonResult);
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.vnfcs[0].modelInfo.modelName"))
                .contains("model1");
        assertThat(JsonPathUtil.getInstance().locateResult(jsonResult, "$.vnfcs[1].modelInfo.modelName"))
                .contains("model2");

    }

    private List<VnfcCustomization> createList() {
        List<VnfcCustomization> customizations = new ArrayList();

        VnfcCustomization c1 = new VnfcCustomization();
        c1.setModelName("model1");
        c1.setModelUUID("uuid1");
        c1.setModelInvariantUUID("inv1");
        c1.setModelVersion("v1");
        c1.setModelCustomizationUUID("cust1");
        c1.setResourceInput("resourceInput1");

        VnfcCustomization c2 = new VnfcCustomization();
        c2.setModelName("model2");
        c2.setModelUUID("uuid2");
        c2.setModelInvariantUUID("inv2");
        c2.setModelVersion("v2");
        c2.setModelCustomizationUUID("cust2");
        c2.setResourceInput("resourceInput2");

        customizations.add(c1);
        customizations.add(c2);
        return customizations;
    }
}
