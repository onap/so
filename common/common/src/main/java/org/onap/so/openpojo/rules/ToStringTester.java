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

package org.onap.so.openpojo.rules;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.anything;
import org.hamcrest.Matcher;
import com.openpojo.reflection.PojoClass;
import com.openpojo.validation.affirm.Affirm;
import com.openpojo.validation.test.Tester;
import com.openpojo.validation.utils.ValidationHelper;

public class ToStringTester implements Tester {

    private final Matcher m;

    public ToStringTester() {
        m = anything();
    }

    public ToStringTester(Matcher m) {
        this.m = m;
    }

    @Override
    public void run(PojoClass pojoClass) {
        Class<?> clazz = pojoClass.getClazz();
        if (anyOf(m).matches(clazz)) {
            final Object classInstance = ValidationHelper.getBasicInstance(pojoClass);

            Affirm.affirmFalse("Found default toString output",
                    classInstance.toString().matches(Object.class.getName() + "@" + "\\w+"));
        }

    }

}
