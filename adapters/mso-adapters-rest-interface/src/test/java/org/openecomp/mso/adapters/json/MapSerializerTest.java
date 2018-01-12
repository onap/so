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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;
import org.junit.Test;

public class MapSerializerTest {

    private static final String JSON_FIELD_NAME_1 = "testKey1";
    private static final String JSON_VALUE_1 = "testValue1";
    private static final String JSON_FIELD_NAME_2 = "testKey2";
    private static final String JSON_VALUE_2 = "testValue2";

    @Test
    public void serializationWritesTheProperFieldsToJson() throws Exception {
        JsonGenerator jsonGeneratorMock = mock(JsonGenerator.class);
        MapSerializer testedObject = new MapSerializer();
        testedObject.serialize(prepareMap(), jsonGeneratorMock, mock(SerializerProvider.class));
        verify(jsonGeneratorMock).writeStringField("key", JSON_FIELD_NAME_1);
        verify(jsonGeneratorMock).writeStringField("value", JSON_VALUE_1);
        verify(jsonGeneratorMock).writeStringField("key", JSON_FIELD_NAME_2);
        verify(jsonGeneratorMock).writeStringField("value", JSON_VALUE_2);
    }

    private Map<String, String> prepareMap() {
        Map<String, String> map = new HashMap<>();
        map.put(JSON_FIELD_NAME_1, JSON_VALUE_1);
        map.put(JSON_FIELD_NAME_2, JSON_VALUE_2);
        return map;
    }
}
