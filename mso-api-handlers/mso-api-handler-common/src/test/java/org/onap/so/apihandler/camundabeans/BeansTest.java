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

package org.onap.so.apihandler.camundabeans;

import org.junit.Test;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.PojoClassFilter;
import com.openpojo.reflection.filters.FilterEnum;
import com.openpojo.reflection.filters.FilterNonConcrete;
import com.openpojo.reflection.filters.FilterPackageInfo;
import org.onap.so.openpojo.rules.ToStringTester;

public class BeansTest {


    private PojoClassFilter filterTestClasses = new FilterTestClasses();

    private PojoClassFilter enumFilter = new FilterEnum();


    @Test
    public void pojoStructure() {
        test("org.onap.so.apihandler.camundabeans");
    }

    private void test(String pojoPackage) {
        Validator validator = ValidatorBuilder.create()



                .with(new SetterTester()).with(new GetterTester()).with(new ToStringTester())

                .build();


        validator.validate(pojoPackage, new FilterPackageInfo(), filterTestClasses, enumFilter,
                new FilterNonConcrete());
    }

    private static class FilterTestClasses implements PojoClassFilter {
        public boolean include(PojoClass pojoClass) {
            return !pojoClass.getSourcePath().contains("/test-classes/");
        }
    }
}
