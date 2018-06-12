package org.openecomp.mso.bpmn.infrastructure.vfcmodel;

import org.junit.Test;

import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.NoNestedClassRule;
import com.openpojo.validation.rule.impl.NoPublicFieldsRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

public class VfcModelPojoTest {
	private String packageName = "org.openecomp.mso.bpmn.infrastructure.vfcmodel";

	@Test
	public void validate() {
		Validator validator = ValidatorBuilder.create()
				.with(new NoNestedClassRule())
				.with(new NoPublicFieldsRule())
				.with(new SetterTester())
				.with(new GetterTester())
				.build();
		validator.validate(packageName);
	}
}
