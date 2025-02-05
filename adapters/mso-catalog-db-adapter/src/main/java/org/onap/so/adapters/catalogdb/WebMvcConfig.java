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

package org.onap.so.adapters.catalogdb;


import org.onap.so.logging.filter.spring.LoggingInterceptor;
import org.onap.so.logging.filter.spring.StatusLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
<<<<<<< HEAD
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
||||||| parent of 30f7ad6a1 (Runtime issues of jdk17)
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
=======
import org.springframework.context.annotation.FilterType;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
>>>>>>> 30f7ad6a1 (Runtime issues of jdk17)
import org.springframework.web.servlet.handler.MappedInterceptor;

@Configuration
<<<<<<< HEAD
@ComponentScan(basePackages = {"org.onap.logging.filter"})
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Value("${logging.request-status.exclusions:}")
    private String[] excludedPaths = new String[0];
||||||| parent of 30f7ad6a1 (Runtime issues of jdk17)
@ComponentScan(basePackages = {"org.onap.logging.filter"})
public class WebMvcConfig implements WebMvcConfigurer {
=======
@ComponentScan(basePackages = {"org.onap.so.logging.filter"})
/*
 * @ComponentScan(basePackages = "org.onap.so.logging.filter", excludeFilters = {@ComponentScan.Filter(type =
 * FilterType.ASSIGNABLE_TYPE, value = org.onap.logging.filter.spring.SpringScheduledTasksMDCSetupAspect.class)})
 */
public class WebMvcConfig implements WebMvcConfigurer {
>>>>>>> 30f7ad6a1 (Runtime issues of jdk17)

    @Autowired
    private LoggingInterceptor loggingInterceptor;

    @Autowired
    private StatusLoggingInterceptor statusLoggingInterceptor;

    @Bean
    public MappedInterceptor mappedLoggingInterceptor() {
        return excludedPaths != null && excludedPaths.length > 0
                ? new MappedInterceptor(new String[] {"/**"}, excludedPaths, loggingInterceptor)
                : new MappedInterceptor(new String[] {"/**"}, loggingInterceptor);
    }

    @Bean
    public MappedInterceptor mappedStatusLoggingInterceptor() {
        return excludedPaths != null && excludedPaths.length > 0
                ? new MappedInterceptor(new String[] {"/**"}, excludedPaths, statusLoggingInterceptor)
                : new MappedInterceptor(new String[] {"/**"}, statusLoggingInterceptor);
    }
}
