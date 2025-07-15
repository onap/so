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

package org.onap.so.apihandlerinfra.tenantisolationbeans;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.Test;
import org.onap.so.apihandlerinfra.BaseTest;
import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.filters.FilterPackageInfo;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.affirm.Affirm;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

public class TenantIsolationBeansTest extends BaseTest {

    private static final int EXPECTED_CLASS_COUNT = 25;
    private static final String POJO_PACKAGE = "org.onap.so.apihandlerinfra.tenantisolationbeans";

    @Test
    public void ensureExpectedPojoCount() {
        List<PojoClass> pojoClasses = PojoClassFactory.getPojoClasses(POJO_PACKAGE, new FilterPackageInfo());
        Affirm.affirmEquals("Classes added / removed?", EXPECTED_CLASS_COUNT, pojoClasses.size());
        assertTrue(true); // this is here to silence a sonarqube violation
    }

    @Test
    public void testPojoStructureAndBehavior() {
        Validator validator = ValidatorBuilder.create().with(new GetterMustExistRule()).with(new SetterMustExistRule())
                .with(new SetterTester()).with(new GetterTester()).build();

        validator.validate(POJO_PACKAGE, new FilterPackageInfo());
    }

    @Test
    public void testTenantIsolationToString() {
        TenantIsolationRequest request = new TenantIsolationRequest();
        request.setRequestId("requestId");
        request.setRequestScope("scope");
        request.setRequestType("type");
        request.setStartTime("time");
        request.setRequestDetails(new RequestDetails());
        request.setRequestStatus(new RequestStatus());

        assertNotNull(request.toString());
    }

    @Test
    public void testRequestListToString() {
        RequestList list = new RequestList();
        list.setRequest(new Request());
        list.setRequestStatus(new RequestStatus());

        assertNotNull(list.toString());
    }
}
