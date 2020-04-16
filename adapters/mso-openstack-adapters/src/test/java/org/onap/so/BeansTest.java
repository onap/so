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

package org.onap.so;

import org.junit.Test;
import org.onap.so.openpojo.rules.ToStringTester;
import org.springframework.stereotype.Component;
import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.PojoClassFilter;
import com.openpojo.reflection.filters.FilterEnum;
import com.openpojo.reflection.filters.FilterNonConcrete;
import com.openpojo.reflection.filters.FilterPackageInfo;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.NoPublicFieldsExceptStaticFinalRule;
import com.openpojo.validation.rule.impl.NoStaticExceptFinalRule;
import com.openpojo.validation.rule.impl.SerializableMustHaveSerialVersionUIDRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;


public class BeansTest {

    private final PojoClassFilter filterTestClasses = new FilterTestClasses();

    private final PojoClassFilter enumFilter = new FilterEnum();

    private final PojoClassFilter noNestedClasses = new FilterTestNestedClasses();



    @Test
    public void pojoStructure() {
        test("org.onap.so.adapters.vnf.async.client");
        test("org.onap.so.adapters.network");
        test("org.onap.so.adapters.vnf");
        test("org.onap.so.vdu.utils");
    }

    private void test(String pojoPackage) {
        Validator validator = ValidatorBuilder.create().with(new NoStaticExceptFinalRule())
                .with(new SerializableMustHaveSerialVersionUIDRule()).with(new NoPublicFieldsExceptStaticFinalRule())
                .with(new SetterTester()).with(new GetterTester()).with(new ToStringTester())

                .build();


        validator.validate(pojoPackage, new FilterPackageInfo(), filterTestClasses, enumFilter, new FilterNonConcrete(),
                noNestedClasses, new FilterBeans());
    }

    private static class FilterTestClasses implements PojoClassFilter {
        @Override
        public boolean include(PojoClass pojoClass) {
            return !pojoClass.getSourcePath().contains("/test-classes/");
        }
    }

    private static class FilterTestNestedClasses implements PojoClassFilter {
        @Override
        public boolean include(PojoClass pojoClass) {
            return !pojoClass.isNestedClass();
        }
    }

    private static class FilterBeans implements PojoClassFilter {
        @Override
        public boolean include(PojoClass pojoClass) {
            return pojoClass.getAnnotations().stream().filter(o -> o instanceof Component).count() <= 0;
        }
    }

}
