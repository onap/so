/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
package org.onap.so.monitoring.configuration;

import static org.junit.Assert.assertFalse;

import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;
import org.onap.so.openpojo.rules.ToStringTester;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import com.openpojo.reflection.filters.FilterPackageInfo;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * @author waqas.ikram@ericsson.com
 */
public class PojoClassesTests {

    @Test
    public void test_camunda_module_pojo_classes() throws ClassNotFoundException {
        test("org.onap.so.monitoring.camunda.model");
        assertEqualMethod("org.onap.so.monitoring.camunda.model");
    }

    @Test
    public void test_so_monitoring_pojo_classes() throws ClassNotFoundException {
        test("org.onap.so.monitoring.model");
        assertEqualMethod("org.onap.so.monitoring.model");
    }

    public void assertEqualMethod(final String pojoPackage) throws ClassNotFoundException {
        final Set<BeanDefinition> classes = getBeanDefinition(pojoPackage);
        assertFalse(classes.isEmpty());
        for (final BeanDefinition bean : classes) {
            final Class<?> clazz = Class.forName(bean.getBeanClassName());
            if (!clazz.getName().endsWith("Builder")) {
                EqualsVerifier.forClass(clazz).suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS).verify();
            }
        }
    }

    private Set<BeanDefinition> getBeanDefinition(final String pojoPackage) {
        final ClassPathScanningCandidateComponentProvider provider =
                new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(pojoPackage + ".*")));
        return provider.findCandidateComponents(pojoPackage);
    }

    private void test(final String pojoPackage) {
        final Validator validator = ValidatorBuilder.create().with(new SetterTester()).with(new GetterTester())
                .with(new ToStringTester()).build();
        validator.validate(pojoPackage, new FilterPackageInfo());
    }
}
