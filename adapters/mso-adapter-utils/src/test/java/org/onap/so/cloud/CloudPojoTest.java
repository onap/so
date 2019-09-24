/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.so.cloud;

import org.junit.Test;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.CloudifyManager;
import org.onap.so.openpojo.rules.ToStringTester;
import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.EqualsAndHashCodeMatchRule;
import com.openpojo.validation.rule.impl.NoPrimitivesRule;
import com.openpojo.validation.rule.impl.NoPublicFieldsRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

public class CloudPojoTest {
    @Test
    public void pojoStructure() {
        test(PojoClassFactory.getPojoClass(CloudIdentity.class));
        test(PojoClassFactory.getPojoClass(CloudifyManager.class));
        test(PojoClassFactory.getPojoClass(CloudSite.class));
        test(PojoClassFactory.getPojoClass(CloudConfig.class));
    }

    private void test(PojoClass pojoClass) {
        Validator validator = ValidatorBuilder.create()
                .with(new EqualsAndHashCodeMatchRule())
                .with(new NoPrimitivesRule())
                .with(new NoPublicFieldsRule())
                .with(new SetterTester())
                .with(new GetterTester())
                .with(new ToStringTester())
                .build();
        validator.validate(pojoClass);
    }
}
