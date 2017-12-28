/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 Huawei Technologies Co., Ltd. All rights reserved.
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

package org.openecomp.mso.adapters.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Map;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.codehaus.jackson.map.DeserializationContext;

public class MapDeserializerTest {

    private static final String MAP_KEY = "keyTest";
    private static final String MAP_VALUE = "valueTest";
    private MapDeserializer testedObject;

    @Test
    public void mapWithProperValuesIsReturned() throws IOException, JSONException {
        JsonParser parser = new ObjectMapper().getJsonFactory().createJsonParser(getJsonAsString());
        testedObject = new MapDeserializer();
        Map<String, String> params = testedObject.deserialize(parser, mock(DeserializationContext.class));
        assertTrue(params.containsKey(MAP_KEY));
        assertEquals(MAP_VALUE, params.get(MAP_KEY));
    }

    private String getJsonAsString() throws JSONException {
        JSONObject child2 = new JSONObject();
        child2.put("key", MAP_KEY);
        child2.put("value", MAP_VALUE);
        JSONObject child1 = new JSONObject();
        child1.put("child2", child2);
        JSONObject parent = new JSONObject();
        parent.put("child1", child1);
        return parent.toString();
    }
}
