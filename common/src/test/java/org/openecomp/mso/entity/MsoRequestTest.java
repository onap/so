package org.openecomp.mso.entity;

import org.junit.Test;
import org.openecomp.mso.openpojo.rules.HasToStringRule;
import org.openecomp.mso.openpojo.rules.ToStringTester;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.PojoClassFilter;
import com.openpojo.reflection.filters.FilterEnum;
import com.openpojo.reflection.filters.FilterPackageInfo;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;


public class MsoRequestTest {

    private PojoClassFilter filterTestClasses = new FilterTestClasses();

    @Test
    public void pojoStructure() {
        test("org.openecomp.mso.entity");
    }

    private void test(String pojoPackage) {
        Validator validator = ValidatorBuilder.create()
                .with(new GetterMustExistRule())
                .with(new SetterMustExistRule())
                .with(new SetterTester())
                .with(new GetterTester())
                .build();
        validator.validate(pojoPackage, new FilterPackageInfo(), new FilterEnum(), filterTestClasses);
    }
    private static class FilterTestClasses implements PojoClassFilter {
        public boolean include(PojoClass pojoClass) {
            return !pojoClass.getSourcePath().contains("/test-classes/");
        }
    }

}

