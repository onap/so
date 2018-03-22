package org.openecomp.mso.bpmn.infrastructure;

import org.junit.Test;

import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.PojoClassFilter;
import com.openpojo.reflection.filters.FilterEnum;
import com.openpojo.reflection.filters.FilterNonConcrete;
import com.openpojo.reflection.filters.FilterPackageInfo;

public class BeansTest {


	private PojoClassFilter filterTestClasses = new FilterTestClasses();
	
	private PojoClassFilter  enumFilter = new FilterEnum();
	

	@Test
	public void pojoStructure() {	
		test("org.openecomp.mso.bpmn.infrastructure.workflow.serviceTask.client.entity");		
	}

	private void test(String pojoPackage) {
		Validator validator = ValidatorBuilder.create()
				.with(new GetterMustExistRule())
				.with(new SetterMustExistRule())
				.with(new SetterTester())
				.with(new GetterTester())
			 
				.with(new SetterTester())
				.with(new GetterTester())	
				
			     
				.build();
		
	
		validator.validate(pojoPackage, new FilterPackageInfo(), filterTestClasses,enumFilter,new FilterNonConcrete());
	}
	private static class FilterTestClasses implements PojoClassFilter {
		public boolean include(PojoClass pojoClass) {
			return !pojoClass.getSourcePath().contains("/test-classes/");
		}
	}
}
