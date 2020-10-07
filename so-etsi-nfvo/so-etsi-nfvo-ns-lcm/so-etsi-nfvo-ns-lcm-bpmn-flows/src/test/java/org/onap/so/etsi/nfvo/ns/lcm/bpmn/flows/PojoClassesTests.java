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
package org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.nsd.FileEntry;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.nsd.NetworkServiceDescriptor;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.nsd.ToscaMetadata;
import org.onap.so.etsi.nfvo.ns.lcm.bpmn.flows.nsd.VirtualNetworkFunction;
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
 *
 */
public class PojoClassesTests {

    @Test
    public void test_nsd_parser_pojo_classes() throws ClassNotFoundException {

        final Validator validator = ValidatorBuilder.create().with(new SetterTester()).with(new GetterTester())
                .with(new ToStringTester()).build();
        validator.validate(FileEntry.class.getPackageName(), new FilterPackageInfo());
    }


    @Test
    public void test_nsd_parser_pojo_classes_equalAndHashMethod() throws ClassNotFoundException {
        final List<Class<?>> classes = Arrays.asList(FileEntry.class, NetworkServiceDescriptor.class,
                ToscaMetadata.class, VirtualNetworkFunction.class);
        for (final Class<?> clazz : classes) {
            EqualsVerifier.forClass(clazz).suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS,
                    Warning.INHERITED_DIRECTLY_FROM_OBJECT).verify();
        }
    }

}
