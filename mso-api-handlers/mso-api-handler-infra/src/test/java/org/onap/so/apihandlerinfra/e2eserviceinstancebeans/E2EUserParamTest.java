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

package org.onap.so.apihandlerinfra.e2eserviceinstancebeans;

import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;

public class E2EUserParamTest {

    E2EUserParam test = new E2EUserParam();

    @Test
    public void verifyE2EParam() {

        test.setName("name");
        assertEquals(test.getName(), "name");
        test.setValue("value");
        assertEquals(test.getValue(), "value");
        Map<String, Object> additionalProperties = new HashMap<>();
        additionalProperties.put("name", test);
        test.setAdditionalProperty("name", test);
        assertEquals(test.getAdditionalProperties(), additionalProperties);

    }

}
