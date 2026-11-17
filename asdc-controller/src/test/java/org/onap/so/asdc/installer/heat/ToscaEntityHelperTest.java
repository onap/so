/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.onap.so.asdc.installer.heat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Map;
import org.junit.Test;
import org.onap.sdc.tosca.parser.api.IEntityDetails;
import org.onap.sdc.toscaparser.api.Property;

public class ToscaEntityHelperTest {

    @Test
    public void testNullMapsNullStringAndUnknownTypes() {
        assertNull(ToscaEntityHelper.testNull(null));
        assertNull(ToscaEntityHelper.testNull("NULL"));
        assertEquals("7", ToscaEntityHelper.testNull(7));
        assertEquals("abc", ToscaEntityHelper.testNull("abc"));
        assertEquals("Type not recognized", ToscaEntityHelper.testNull(1.5d));
    }

    @Test
    public void getPropertyInputExtractsInputName() {
        assertEquals("vnf_name", ToscaEntityHelper.getPropertyInput("{get_input=vnf_name}"));
        assertEquals("", ToscaEntityHelper.getPropertyInput("plain"));
        assertEquals("", ToscaEntityHelper.getPropertyInput(null));
    }

    @Test
    public void getLeafPropertyValueReturnsNullForMissingOrNullValue() {
        IEntityDetails entity = mock(IEntityDetails.class);
        Property nullValued = mock(Property.class);
        when(nullValued.getValue()).thenReturn(null);
        Property set = mock(Property.class);
        when(set.getValue()).thenReturn(42);
        when(entity.getProperties()).thenReturn(Map.of("a", nullValued, "b", set));

        assertNull(ToscaEntityHelper.getLeafPropertyValue(entity, "a"));
        assertEquals("42", ToscaEntityHelper.getLeafPropertyValue(entity, "b"));
        assertNull(ToscaEntityHelper.getLeafPropertyValue(entity, "missing"));
    }

    @Test
    public void parseIntegerOrNullParsesNumbersAndMapsEverythingElseToNull() {
        assertEquals(Integer.valueOf(3), ToscaEntityHelper.parseIntegerOrNull("3", "p"));
        assertEquals(Integer.valueOf(0), ToscaEntityHelper.parseIntegerOrNull(" 0 ", "p"));
        assertNull(ToscaEntityHelper.parseIntegerOrNull(null, "p"));
        assertNull(ToscaEntityHelper.parseIntegerOrNull("", "p"));
        assertNull(ToscaEntityHelper.parseIntegerOrNull("  ", "p"));
        assertNull(ToscaEntityHelper.parseIntegerOrNull("UNBOUNDED", "p"));
        assertNull(ToscaEntityHelper.parseIntegerOrNull("{get_input=max_instances}", "p"));
    }
}
