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
package org.onap.so.apihandlerinfra;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.Collection;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class RequestActionMapTest {

    private String inputAction;
    private String expectedMappedAction;

    public RequestActionMapTest(String inputAction, String expectedMappedAction) {
        this.inputAction = inputAction;
        this.expectedMappedAction = expectedMappedAction;
    }

    @Parameterized.Parameters(name = "{index}: getMappedRequestAction({0})={1}")
    public static Collection<Object[]> data() {
        return Arrays
                .asList(new Object[][] {{"CREATE_VF_MODULE", "createInstance"}, {"DELETE_VF_MODULE", "deleteInstance"},
                        {"UPDATE_VF_MODULE", "updateInstance"}, {"CREATE_VF_MODULE_VOL", "createInstance"},
                        {"DELETE_VF_MODULE_VOL", "deleteInstance"}, {"UPDATE_VF_MODULE_VOL", "updateInstance"},
                        {"CREATE", "createInstance"}, {"DELETE", "deleteInstance"}, {"UPDATE", "updateInstance"},
                        {"createInstance", "createInstance"}, {"deleteInstance", "deleteInstance"},
                        {"updateInstance", "updateInstance"}, {"replaceInstance", "replaceInstance"},
                        // Test for key not present in the map (should return null)
                        {"NON_EXISTENT_ACTION", null}});
    }

    @Test
    public void testGetMappedRequestAction() {
        assertEquals(expectedMappedAction, RequestActionMap.getMappedRequestAction(inputAction));
    }
}
