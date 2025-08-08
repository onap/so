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

package org.onap.so.client.policy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.onap.so.client.CommonObjectMapperProvider;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class CommonObjectMapperProviderTest {

    @Test
    public void shouldSetCorrectMapperProperties() {
        // given
        CommonObjectMapperProvider provider = new CommonObjectMapperProvider();
        // when
        ObjectMapper context = provider.getMapper();
        // then
        assertTrue(context.isEnabled(MapperFeature.USE_ANNOTATIONS));
        assertFalse(context.isEnabled(SerializationFeature.WRAP_ROOT_VALUE));
        assertFalse(context.isEnabled(DeserializationFeature.UNWRAP_ROOT_VALUE));
    }
}
