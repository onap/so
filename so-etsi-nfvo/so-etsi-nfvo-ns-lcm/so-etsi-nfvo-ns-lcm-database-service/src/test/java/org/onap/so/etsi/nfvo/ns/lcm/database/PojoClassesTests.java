/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */
package org.onap.so.etsi.nfvo.ns.lcm.database;

import javax.persistence.Entity;
import javax.persistence.Id;
import org.junit.Test;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoJob;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoJobStatus;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoNfInst;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NfvoNsInst;
import org.onap.so.etsi.nfvo.ns.lcm.database.beans.NsLcmOpOcc;
import org.onap.so.openpojo.rules.ToStringTester;
import com.openpojo.reflection.filters.FilterPackageInfo;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 */
public class PojoClassesTests {

    @Test
    public void test_database_beans() throws ClassNotFoundException {

        final Validator validator = ValidatorBuilder.create().with(new SetterTester()).with(new GetterTester())
                .with(new ToStringTester()).build();
        validator.validate(NfvoNsInst.class.getPackageName(), new FilterPackageInfo());
    }

    @Test
    public void test_database_nfvoJob_equalAndHashMethod() throws ClassNotFoundException {
        EqualsVerifier.forClass(NfvoJob.class)
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.INHERITED_DIRECTLY_FROM_OBJECT)
                .withPrefabValues(NfvoJobStatus.class, new NfvoJobStatus().nfvoJob(new NfvoJob()),
                        new NfvoJobStatus().nfvoJob(new NfvoJob()))
                .withIgnoredAnnotations(Entity.class, Id.class).verify();
    }

    @Test
    public void test_database_nfvoJobStatus_equalAndHashMethod() throws ClassNotFoundException {
        EqualsVerifier.forClass(NfvoJobStatus.class)
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.INHERITED_DIRECTLY_FROM_OBJECT)
                .withPrefabValues(NfvoJob.class, new NfvoJob(), new NfvoJob())
                .withIgnoredAnnotations(Entity.class, Id.class).verify();
    }

    @Test
    public void test_database_nfvoNsInst_equalAndHashMethod() throws ClassNotFoundException {
        EqualsVerifier.forClass(NfvoNsInst.class)
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.INHERITED_DIRECTLY_FROM_OBJECT)
                .withPrefabValues(NfvoNfInst.class, new NfvoNfInst(), new NfvoNfInst())
                .withPrefabValues(NsLcmOpOcc.class, new NsLcmOpOcc(), new NsLcmOpOcc())
                .withIgnoredAnnotations(Entity.class, Id.class).verify();
    }

    @Test
    public void test_database_nfvoNfInst_equalAndHashMethod() throws ClassNotFoundException {
        EqualsVerifier.forClass(NfvoNfInst.class)
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.INHERITED_DIRECTLY_FROM_OBJECT)
                .withPrefabValues(NfvoNsInst.class, new NfvoNsInst(), new NfvoNsInst())
                .withIgnoredAnnotations(Entity.class, Id.class).verify();
    }

    @Test
    public void test_database_nsLcmOpOcc_equalAndHashMethod() throws ClassNotFoundException {
        EqualsVerifier.forClass(NsLcmOpOcc.class)
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.INHERITED_DIRECTLY_FROM_OBJECT)
                .withPrefabValues(NfvoNsInst.class, new NfvoNsInst(), new NfvoNsInst())
                .withIgnoredAnnotations(Entity.class, Id.class).verify();
    }
}
