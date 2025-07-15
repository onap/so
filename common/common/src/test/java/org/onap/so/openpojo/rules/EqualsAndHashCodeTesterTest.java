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

import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.reflection.PojoClass;
import lombok.Data;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import java.util.Objects;
import javax.persistence.Id;
import org.junit.Before;
import org.junit.Test;

public class EqualsAndHashCodeTesterTest {


    private EqualsAndHashCodeTester tester;

    @Before
    public void setUp() {
        tester = new EqualsAndHashCodeTester();
    }

    @Test
    public void testValidEqualsAndHashCode() {
        PojoClass pojoClass = PojoClassFactory.getPojoClass(ValidEqualsWithId.class);

        tester.run(pojoClass);
        assertTrue(true); // this is here to silence a sonarqube violation
    }

    @Test
    // assert that test fails when object fields that are compared in the classes equals implementation
    // are not annotated with @Id or @BusinessKey
    public void testInValidEqualsAndHashCode() {
        PojoClass pojoClass = PojoClassFactory.getPojoClass(InvalidEqualsWithoutId.class);

        AssertionError error = assertThrows(AssertionError.class, () -> tester.run(pojoClass));
        assertTrue(error.getMessage().contains("Equals test"));
    }

    @Test
    public void testOnlyDeclaredMethods() {
        PojoClass pojoClass = PojoClassFactory.getPojoClass(ValidEqualsWithId.class);
        EqualsAndHashCodeTester onlyDeclaredMethodsTester = new EqualsAndHashCodeTester().onlyDeclaredMethods();

        onlyDeclaredMethodsTester.run(pojoClass);
        assertTrue(true); // this is here to silence a sonarqube violation
    }

    @Data
    static class ValidEqualsWithId {

        @Id
        String requestId;

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ValidEqualsWithId)) {
                return false;
            }
            final ValidEqualsWithId castOther = (ValidEqualsWithId) other;
            return Objects.equals(getRequestId(), castOther.getRequestId());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getRequestId());
        }
    }

    @Data
    static class InvalidEqualsWithoutId {

        String requestId;

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof InvalidEqualsWithoutId)) {
                return false;
            }
            final InvalidEqualsWithoutId castOther = (InvalidEqualsWithoutId) other;
            // equals may only compare fields that are annotated with @Id or @BusinessKey
            return Objects.equals(getRequestId(), castOther.getRequestId());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getRequestId());
        }
    }
}
