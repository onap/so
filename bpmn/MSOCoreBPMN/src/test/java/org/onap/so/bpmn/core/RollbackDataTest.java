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

package org.onap.so.bpmn.core;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.Matchers.isIn;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class RollbackDataTest {

    private final static String TYPE_A = "typeA";
    private final static String TYPE_B = "typeB";
    private static final String KEY_1 = "key1";
    private static final String VALUE_1 = "value1";
    private static final String VALUE_2 = "value2";

    @Test
    public void shouldReturnStringRepresentationOfDataInAnyPermutation() {
        // given
        RollbackData data = new RollbackData();
        data.put(TYPE_A, KEY_1, VALUE_1);
        data.put(TYPE_A, "key2", "value2");
        data.put(TYPE_B, "key3", "value3");
        // when, then
        assertThat(data.toString(),
                isIn(Arrays.asList("[typeB{key3=value3},typeA{key1=value1, key2=value2}]",
                        "[typeB{key3=value3},typeA{key2=value2, key1=value1}]",
                        "[typeA{key1=value1, key2=value2},typeB{key3=value3}]",
                        "[typeA{key2=value2, key1=value1},typeB{key3=value3}]")));
    }

    @Test
    public void shouldBeEmptyOnCreation() {
        // given
        RollbackData data = new RollbackData();
        // then
        assertFalse(data.hasType(TYPE_A));
        assertNull(data.get(TYPE_A, KEY_1));
    }

    @Test
    public void shouldHaveTypeAfterPuttingDataOfThatType() {
        // given
        RollbackData data = new RollbackData();
        // when
        data.put(TYPE_A, KEY_1, VALUE_1);
        // then
        assertTrue(data.hasType(TYPE_A));
        assertFalse(data.hasType(TYPE_B));
        assertEquals(VALUE_1, data.get(TYPE_A, KEY_1));
    }

    @Test
    public void shouldKeepTwoValuesWithSameKeysButDifferentTypes() {
        // given
        RollbackData data = new RollbackData();
        // when
        data.put(TYPE_A, KEY_1, VALUE_1);
        data.put(TYPE_B, KEY_1, VALUE_2);
        // then
        assertEquals(VALUE_1, data.get(TYPE_A, KEY_1));
        assertThat(data.get(TYPE_A), is(Collections.singletonMap(KEY_1, VALUE_1)));
        assertEquals(VALUE_2, data.get(TYPE_B, KEY_1));
        assertThat(data.get(TYPE_B), is(Collections.singletonMap(KEY_1, VALUE_2)));
    }
}
