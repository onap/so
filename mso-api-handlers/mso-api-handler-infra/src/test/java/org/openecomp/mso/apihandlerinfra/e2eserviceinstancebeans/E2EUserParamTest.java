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

package org.openecomp.mso.apihandlerinfra.e2eserviceinstancebeans;

import org.junit.Test;

public class E2EUserParamTest {

    // TODO: currently test case is done for coverage
    // later, it should be modified properly.

    E2EUserParam test = new E2EUserParam();

    @Test
    public void getNameTest() throws Exception {
        test.getName();
    }

    @Test
    public void setNameTest() throws Exception {
        test.setName("name");
    }

    @Test
    public void getValueTest() throws Exception {
        test.getValue();
    }

    @Test
    public void setValueTest() throws Exception {
        test.setValue("value");
    }

    @Test
    public void getAdditionalPropertiesTest() throws Exception {
        test.getAdditionalProperties();
    }

    @Test
    public void setAdditionalPropertiesTest() throws Exception {
        test.setAdditionalProperty("name",test);
    }
}
