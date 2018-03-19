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

package org.openecomp.mso.cloudify;

import org.junit.Before;
import org.junit.Test;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.PojoClassFilter;
import com.openpojo.reflection.filters.FilterEnum;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

public class BeanMultiTest {

	Validator validator;
	 PojoClassFilter enumFilter;
	 private PojoClassFilter filterTestClasses = new FilterTestClasses();
	 
  @Before
  public void setup(){
	  enumFilter = new FilterEnum();
	   validator = ValidatorBuilder.create()
              .with(new SetterMustExistRule(),
                    new GetterMustExistRule())
              .with(new SetterTester(),
                    new GetterTester())
              .build();
  }
  @Test
  public void validateBeansMsoApihandlerBeans() {
      try{
	  validator.validate("org.openecomp.mso.cloudify.v3.model",enumFilter);
      } catch (AssertionError e) {
	  System.err.println("!!!!!!!!!");
	  e.printStackTrace();
	  System.err.println("!!!!!!!!!");
	  throw e;
      }
  }
  
  private static class FilterTestClasses implements PojoClassFilter {
	    public boolean include(PojoClass pojoClass) {
	      return !pojoClass.getSourcePath().contains("/src/test/java");
	    }
	  }
}