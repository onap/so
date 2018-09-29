package org.onap.so.bpmn.common.validation;

import org.onap.so.bpmn.common.DefaultToShortClassNameBeanNameGenerator;
import org.onap.so.client.exception.ExceptionBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ComponentScan(basePackages = {"org.onap.so.bpmn.common.validation"}, basePackageClasses = {ExceptionBuilder.class}, nameGenerator = DefaultToShortClassNameBeanNameGenerator.class)
public class ValidationConfig {

}
