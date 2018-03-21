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

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.anything;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Id;

import org.hamcrest.Matcher;

import com.openpojo.business.annotation.BusinessKey;
import com.openpojo.random.RandomFactory;
import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.PojoField;
import com.openpojo.validation.affirm.Affirm;
import com.openpojo.validation.test.Tester;
import com.openpojo.validation.utils.ValidationHelper;

public class EqualsAndHashCodeTester implements Tester {

	
	private final Matcher m;
	public EqualsAndHashCodeTester() {
		m = anything();
	}
	
	public EqualsAndHashCodeTester(Matcher m) {
		this.m = m;
	}
	@Override
	public void run(PojoClass pojoClass) {
		Class<?> clazz = pojoClass.getClazz();
		if (anyOf(m).matches(clazz)) {
			final Object classInstanceOne = ValidationHelper.getBasicInstance(pojoClass);
			final Object classInstanceTwo = ValidationHelper.getBasicInstance(pojoClass);
			Set<PojoField> identityFields = hasIdOrBusinessKey(pojoClass);
			List<PojoField> otherFields = new ArrayList<>(pojoClass.getPojoFields());
			otherFields.removeAll(identityFields);
			
			for (PojoField field : identityFields) {
				final Object value  = RandomFactory.getRandomValue(field);
	
				field.invokeSetter(classInstanceOne, value);
				field.invokeSetter(classInstanceTwo, value);
			}
			
			for (PojoField field : otherFields) {
				if (field.hasSetter()) {
					final Object valueOne  = RandomFactory.getRandomValue(field);
					final Object valueTwo  = RandomFactory.getRandomValue(field);
					
					field.invokeSetter(classInstanceOne, valueOne);
					field.invokeSetter(classInstanceTwo, valueTwo);
				}
			}
			
			Affirm.affirmTrue("Equals test failed for [" + classInstanceOne.getClass().getName() + "]", classInstanceOne.equals(classInstanceTwo));
			
			Affirm.affirmTrue("Equals test failed for [" + classInstanceOne.getClass().getName() + "]", classInstanceOne.equals(
					classInstanceOne));
			
			Affirm.affirmTrue("HashCode test failed for [" + classInstanceOne.getClass().getName() + "]", classInstanceOne.hashCode() == classInstanceTwo.hashCode());
			
			Affirm.affirmFalse("Expected false for comparison of two unlike objects", classInstanceOne.equals("test"));
		}
	}
	
	
	private Set<PojoField> hasIdOrBusinessKey(PojoClass pojoClass) {
		final Set<PojoField> fields = new HashSet<>();
		
		fields.addAll(pojoClass.getPojoFieldsAnnotatedWith(BusinessKey.class));
		fields.addAll(pojoClass.getPojoFieldsAnnotatedWith(Id.class));
		
		return fields;
		
	}

}
