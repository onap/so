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
import static org.hamcrest.CoreMatchers.not;
import org.hamcrest.Matcher;
import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.PojoField;
import com.openpojo.validation.affirm.Affirm;
import com.openpojo.validation.rule.Rule;

public class CustomSetterMustExistRule implements Rule {

    private Matcher[] excludeMatchers = new Matcher[] {not(anything())};
    private Matcher<PojoField>[] includeMatchers = new Matcher[] {anything()};

    public CustomSetterMustExistRule() {}

    @Override
    public void evaluate(final PojoClass pojoClass) {
        for (PojoField fieldEntry : pojoClass.getPojoFields()) {
            if (!anyOf(excludeMatchers).matches(fieldEntry) && anyOf(includeMatchers).matches(fieldEntry)
                    && !fieldEntry.isFinal() && !fieldEntry.hasSetter()) {
                Affirm.fail(String.format("[%s] is missing a setter", fieldEntry));
            }
        }
    }

    public CustomSetterMustExistRule exclude(Matcher... excludeMatchers) {
        this.excludeMatchers = excludeMatchers;
        return this;
    }

    public CustomSetterMustExistRule include(Matcher<PojoField>... includeMatchers) {
        this.includeMatchers = includeMatchers;
        return this;
    }

}
