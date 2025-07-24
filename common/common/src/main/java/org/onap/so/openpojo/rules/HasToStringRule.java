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
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import org.hamcrest.Matcher;
import com.openpojo.reflection.PojoClass;
import com.openpojo.validation.affirm.Affirm;
import com.openpojo.validation.rule.Rule;

public class HasToStringRule implements Rule {

    private final Matcher m;

    public HasToStringRule() {
        m = anything();
    }

    public HasToStringRule(Matcher m) {
        this.m = m;
    }

    @Override
    public void evaluate(PojoClass pojoClass) {
        Class<?> clazz = pojoClass.getClazz();
        if (anyOf(m).matches(clazz)) {
            boolean hasToString = false;
            final String name = clazz.getSimpleName();
            final Method[] methods;
            if (clazz.getSuperclass().equals(Object.class)) {
                methods = clazz.getDeclaredMethods();
            } else {
                methods = clazz.getMethods();
            }
            for (Method method : methods) {
                Parameter[] parameters = method.getParameters();
                if ("toString".equals(method.getName()) && String.class.equals(method.getReturnType())
                        && parameters.length == 0) {
                    hasToString = true;
                    break;
                }
            }

            if (!hasToString) {
                Affirm.fail(String.format("[%s] does not override toString", name));
            }
        }
    }

}
