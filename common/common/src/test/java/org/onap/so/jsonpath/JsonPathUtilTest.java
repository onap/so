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

package org.onap.so.jsonpath;

import static org.junit.Assert.assertEquals;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;

public class JsonPathUtilTest {

    private static final String json = "{\"test\" : \"hello\", \"test2\" : {\"nested\" : \"value\"}}";

    @Test
    public void pathExistsTest() {
        assertEquals("test is found", JsonPathUtil.getInstance().pathExists(json, "$.test"), true);
        assertEquals("nothing is not found", JsonPathUtil.getInstance().pathExists(json, "$.nothing"), false);
    }

    @Test
    public void locateResultTest() {
        assertEquals("value of hello is found", Optional.of("hello"),
                JsonPathUtil.getInstance().locateResult(json, "$.test"));
        assertEquals("nothing returns empty", Optional.empty(),
                JsonPathUtil.getInstance().locateResult(json, "$.nothing"));
    }

    @Test
    public void simpleAndComplexValues() {
        assertEquals("json doc found", Optional.of("{\"nested\":\"value\"}"),
                JsonPathUtil.getInstance().locateResult(json, "$.test2"));
        assertEquals("value found", Optional.of("value"),
                JsonPathUtil.getInstance().locateResult(json, "$.test2.nested"));
    }

    @Test
    public void pathListTest() {
        assertEquals(Collections.singletonList("$['test2']['nested']"),
                JsonPathUtil.getInstance().getPathList(json, "$.*.*"));
    }
}
