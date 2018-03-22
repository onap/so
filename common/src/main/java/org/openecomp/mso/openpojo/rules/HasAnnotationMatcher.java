/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.mso.openpojo.rules;

import static org.hamcrest.CoreMatchers.anything;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class HasAnnotationMatcher<T extends Annotation> extends TypeSafeDiagnosingMatcher<AnnotatedElement> {
	private final Class<T> annotationType;
	private final Matcher<? super T> annotationMatcher;

	public HasAnnotationMatcher(final Class<T> annotationType, final Matcher<? super T> annotationMatcher) {
		this.annotationType = annotationType;
		this.annotationMatcher = annotationMatcher;
	}

	@Override
	protected boolean matchesSafely(final AnnotatedElement item, final Description mismatchDescription) {
		final T annotation = item.getAnnotation(this.annotationType);
		if (annotation == null) {
			mismatchDescription.appendText("does not have annotation ").appendText(this.annotationType.getName());
			return false;
		}

		if (!this.annotationMatcher.matches(annotation)) {
			this.annotationMatcher.describeMismatch(annotation, mismatchDescription);
			return false;
		}

		return true;
	}

	@Override
	public void describeTo(final Description description) {
		// Intentionally left blank.
	}

	public static Matcher<AnnotatedElement> hasAnnotation(final Class<? extends Annotation> annotationType) {
		return hasAnnotation(annotationType, anything(""));
	}

	public static <T extends Annotation> Matcher<AnnotatedElement> hasAnnotation(final Class<T> annotationType, final Matcher<? super T> annotationMatcher) {
		return new HasAnnotationMatcher<T>(annotationType, annotationMatcher);
	}
}
