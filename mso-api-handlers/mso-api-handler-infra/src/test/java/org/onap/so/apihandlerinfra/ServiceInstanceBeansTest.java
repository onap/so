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

package org.onap.so.apihandlerinfra;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.filters.FilterEnum;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;


public class ServiceInstanceBeansTest extends BaseTest {
    List<PojoClass> pojoClasses;

    @Before
    public void setup() {
        pojoClasses = PojoClassFactory.getPojoClassesRecursively("org.onap.so.serviceinstancebeans", new FilterEnum());
    }

    @Test
    public void validateGettersAndSetters() {
        Validator validator = ValidatorBuilder.create().with(new SetterMustExistRule(), new GetterMustExistRule())
                .with(new SetterTester(), new GetterTester()).build();
        validator.validate(pojoClasses);
    }
}

