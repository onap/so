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

package org.openecomp.mso.adapters;

import java.io.IOException;
import java.util.HashMap;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.mso.adapters.json.MapSerializer;

public class AdapterRestInterfaceTest {

	@Test
	public final void mapSerializerTest() {
		MapSerializer mapSerializer = new MapSerializer();
		mapSerializer.isUnwrappingSerializer();
		mapSerializer.toString();
		mapSerializer.unwrappingSerializer();
		JsonGenerator jsonGenerator = Mockito.mock(JsonGenerator.class);
		SerializerProvider serializerProvider = Mockito
				.mock(SerializerProvider.class);
		try {
			mapSerializer.serialize(new HashMap(), jsonGenerator, serializerProvider);
		} catch (IOException e) {
		}
	}

}
