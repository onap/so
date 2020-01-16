package org.onap.so.adapters.appc.orchestrator.client.beans;

import org.junit.Test;
import com.openpojo.reflection.filters.FilterPackageInfo;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

public class BeansTest {

    @Test
    public void validateGettersAndSetters() {
        Validator validator = ValidatorBuilder.create().with(new SetterMustExistRule(), new GetterMustExistRule())
                .with(new SetterTester(), new GetterTester()).build();
        validator.validate("org.onap.so.adapters.appc.orchestrator.client.beans", new FilterPackageInfo());
    }
}
