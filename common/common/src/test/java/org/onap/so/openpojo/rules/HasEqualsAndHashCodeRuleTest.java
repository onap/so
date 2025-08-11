/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom AG Intellectual Property. All rights reserved.
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
package org.onap.so.openpojo.rules;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.rule.Rule;
import lombok.Data;

public class HasEqualsAndHashCodeRuleTest {

    private Rule rule;

    @Before
    public void setUp() {
        rule = new HasEqualsAndHashCodeRule();
    }

    @Test
    public void testValidEqualsAndHashCode() {
        PojoClass pojoClass = PojoClassFactory.getPojoClass(HasEqualsAndHashCode.class);

        assertDoesNotThrow(() -> rule.evaluate(pojoClass));
    }

    @Test
    public void testEqualsButNoHashCode() {
        PojoClass pojoClass = PojoClassFactory.getPojoClass(HasEqualsButNoHashCode.class);

        AssertionError error = assertThrows(AssertionError.class, () -> rule.evaluate(pojoClass));
        assertTrue(error.getMessage().contains("does not override hashCode"));
    }

    @Test
    public void testHashCodeButNoEquals() {
        PojoClass pojoClass = PojoClassFactory.getPojoClass(HasHashCodeButNoEquals.class);

        AssertionError error = assertThrows(AssertionError.class, () -> rule.evaluate(pojoClass));
        assertTrue(error.getMessage().contains("does not override equals"));
    }

    @Data
    static class HasEqualsAndHashCode {

        @Override
        public boolean equals(final Object other) {
            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this);
        }
    }

    static class HasEqualsButNoHashCode {
        @Override
        public boolean equals(final Object other) {
            return true;
        }
    }

    static class HasHashCodeButNoEquals {
        @Override
        public int hashCode() {
            return Objects.hash(this);
        }
    }
}
