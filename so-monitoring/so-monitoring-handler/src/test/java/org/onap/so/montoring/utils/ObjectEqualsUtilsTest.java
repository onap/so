/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
 * 
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.monitoring.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;


/**
 * @author waqas.ikram@ericsson.com
 *
 */
public class ObjectEqualsUtilsTest {

    private static final String VALUE = "Humpty Dumpty Sat On The Wall";

    @Test
    public void test_ObjectEqualsUtils_isEqual_NullValues() {
        assertTrue(ObjectEqualsUtils.isEqual(null, null));
    }

    @Test
    public void test_ObjectEqualsUtils_isEqual_FirstValueNullSecondNotNull() {
        assertFalse(ObjectEqualsUtils.isEqual(null, VALUE));
    }

    @Test
    public void test_ObjectEqualsUtils_isEqual_FirstValueNotNullSecondNull() {
        assertFalse(ObjectEqualsUtils.isEqual(VALUE, null));
    }

    @Test
    public void test_ObjectEqualsUtils_isEqual_NotNullValues() {
        assertTrue(ObjectEqualsUtils.isEqual(VALUE, VALUE));
    }

    @Test
    public void test_ObjectEqualsUtils_isEqual_CollectionValues() {
        final List<Object> firstObject = Arrays.asList(VALUE);
        final List<Object> secondObject = Arrays.asList(VALUE);
        assertTrue(ObjectEqualsUtils.isEqual(firstObject, secondObject));
    }

    @Test
    public void test_ObjectEqualsUtils_isEqual_CollectionAndStringValues() {
        final List<Object> firstObject = Arrays.asList(VALUE);
        assertFalse(ObjectEqualsUtils.isEqual(firstObject, VALUE));
    }
}
