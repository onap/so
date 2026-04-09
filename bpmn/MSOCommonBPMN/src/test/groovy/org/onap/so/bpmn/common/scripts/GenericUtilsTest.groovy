/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG
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

package org.onap.so.bpmn.common.scripts

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

@RunWith(MockitoJUnitRunner.class)
class GenericUtilsTest {

    @Test
    void testIsBlank_null() {
        assertTrue(GenericUtils.isBlank(null))
    }

    @Test
    void testIsBlank_emptyString() {
        assertTrue(GenericUtils.isBlank(""))
    }

    @Test
    void testIsBlank_literalNull() {
        assertTrue(GenericUtils.isBlank("null"))
    }

    @Test
    void testIsBlank_whitespaceOnly() {
        assertTrue(GenericUtils.isBlank("   "))
    }

    @Test
    void testIsBlank_tabsAndNewlines() {
        assertTrue(GenericUtils.isBlank("\t\n\r "))
    }

    @Test
    void testIsBlank_nonBlankString() {
        assertFalse(GenericUtils.isBlank("hello"))
    }

    @Test
    void testIsBlank_stringWithSpaces() {
        assertFalse(GenericUtils.isBlank(" hello "))
    }

    @Test
    void testIsBlank_singleCharacter() {
        assertFalse(GenericUtils.isBlank("a"))
    }
}
