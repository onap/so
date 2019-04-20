/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import com.openpojo.reflection.PojoField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HasAnnotationPropertyWithValueMatcher<T extends PojoField> extends TypeSafeDiagnosingMatcher<T> {
    private Logger logger = LoggerFactory.getLogger(HasAnnotationPropertyWithValueMatcher.class);
    private final String attribute;
    private final Matcher<?> annotationMatcher;
    private final Class<? extends Annotation> annotationClass;

    public HasAnnotationPropertyWithValueMatcher(Class<? extends Annotation> clazz, String attribute,
            final Matcher<?> annotationMatcher) {
        this.attribute = attribute;
        this.annotationMatcher = annotationMatcher;
        this.annotationClass = clazz;
    }

    @Override
    protected boolean matchesSafely(T obj, final Description mismatchDescription) {
        final PojoField temp = (PojoField) obj;
        final Method method;
        try {
            Annotation a = temp.getAnnotation(this.annotationClass);
            if (a == null) {
                mismatchDescription.appendText("does not have annotation ")
                        .appendText(this.annotationClass.getSimpleName());
                return false;
            }
            method = a.getClass().getMethod(attribute);
            final Object result = method.invoke(a);
            if (!this.annotationMatcher.matches(result)) {
                this.annotationMatcher.describeMismatch(result, mismatchDescription);
                return false;
            }
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            mismatchDescription.appendText("does not have property ").appendText(attribute);
            logger.debug("Error occured", e);
            return false;
        }
        return true;
    }

    @Override
    public void describeTo(final Description description) {
        // Intentionally left blank.
    }

    public static <T extends PojoField> Matcher<T> hasAnnotationPropertyWithValue(Class<? extends Annotation> clazz,
            String attribute, final Matcher<?> annotationMatcher) {
        return new HasAnnotationPropertyWithValueMatcher<>(clazz, attribute, annotationMatcher);
    }
}
