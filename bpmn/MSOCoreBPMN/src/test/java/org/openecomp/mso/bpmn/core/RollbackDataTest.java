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

package org.openecomp.mso.bpmn.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import org.junit.Test;

public class RollbackDataTest {

    private final static String TYPE_A = "typeA";
    private final static String TYPE_B = "typeB";
    private static final String KEY_1 = "key1";
    private static final String VALUE_1 = "value1";
    private static final String VALUE_2 = "value2";

    @Test
    public void shouldReturnStringRepresentationOfDataInAnyPermutation() throws Exception {
        // given
        RollbackData data = new RollbackData();
        data.put(TYPE_A, KEY_1, VALUE_1);
        data.put(TYPE_A, "key2", "value2");
        data.put(TYPE_B, "key3", "value3");
        // when, then
        assertThat(data.toString()).isIn(
                "[typeB{key3=value3},typeA{key1=value1, key2=value2}]",
                "[typeB{key3=value3},typeA{key2=value2, key1=value1}]",
                "[typeA{key1=value1, key2=value2},typeB{key3=value3}]",
                "[typeA{key2=value2, key1=value1},typeB{key3=value3}]");
    }

    @Test
    public void shouldBeEmptyOnCreation() throws Exception {
        // given
        RollbackData data = new RollbackData();
        // then
        assertThat(data.hasType(TYPE_A)).isFalse();
        assertThat(data.get(TYPE_A, KEY_1)).isNull();
    }

    @Test
    public void shouldHaveTypeAfterPuttingDataOfThatType() throws Exception {
        // given
        RollbackData data = new RollbackData();
        // when
        data.put(TYPE_A, KEY_1, VALUE_1);
        // then
        assertThat(data.hasType(TYPE_A)).isTrue();
        assertThat(data.hasType(TYPE_B)).isFalse();
        assertThat(data.get(TYPE_A, KEY_1)).isEqualTo(VALUE_1);
    }

    @Test
    public void shouldKeepTwoValuesWithSameKeysButDifferentTypes() throws Exception {
        // given
        RollbackData data = new RollbackData();
        // when
        data.put(TYPE_A, KEY_1, VALUE_1);
        data.put(TYPE_B, KEY_1, VALUE_2);
        // then
        assertThat(data.get(TYPE_A, KEY_1)).isEqualTo(VALUE_1);
        assertThat(data.get(TYPE_A)).containsExactly(entry(KEY_1, VALUE_1));
        assertThat(data.get(TYPE_B, KEY_1)).isEqualTo(VALUE_2);
        assertThat(data.get(TYPE_B)).containsExactly(entry(KEY_1, VALUE_2));
    }
}