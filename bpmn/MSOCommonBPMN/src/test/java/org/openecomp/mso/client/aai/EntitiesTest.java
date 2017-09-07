package org.openecomp.mso.client.aai;

import org.junit.Test;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

public class EntitiesTest {

	private String packageName = "org.openecomp.mso.client.aai.entities";

	@Test
	public void validate() {
		Validator validator = ValidatorBuilder.create().with(new SetterMustExistRule(), new GetterMustExistRule())
				.with(new SetterTester(), new GetterTester()).build();
		validator.validate(packageName);
	}
}
