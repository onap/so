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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;
import org.junit.Test;

public class MapSerializerTest {

    @Test
    public void serializationGeneratesProperJsonOutputFromMap() throws Exception {
        String expectedPartOne = "{\"key\":\"testKey1\",\"value\":\"testValue1\"}";
        String expectedPartTwo = "{\"key\":\"testKey2\",\"value\":\"testValue2\"}";
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                JsonGenerator jsonGenerator = new JsonFactory().createJsonGenerator(out)) {
            MapSerializer testedObject = new MapSerializer();
            testedObject.serialize(prepareMap(), jsonGenerator, mock(SerializerProvider.class));
            assertThat(out.toString()).contains(expectedPartOne).contains(expectedPartTwo);
        }
    }

    private Map<String, String> prepareMap() {
        Map<String, String> map = new HashMap<>();
        map.put("testKey1" , "testValue1");
        map.put("testKey2" , "testValue2");
        return map;
    }

}
