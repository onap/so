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


import org.onap.logging.filter.spring.LoggingInterceptor;
import org.onap.logging.filter.spring.StatusLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.MappedInterceptor;

@Configuration
@ComponentScan(basePackages = {"org.onap.logging.filter"})
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoggingInterceptor loggingInterceptor;

    @Autowired
    private StatusLoggingInterceptor statusLoggingInterceptor;

    @Bean
    public MappedInterceptor mappedLoggingInterceptor() {
        return new MappedInterceptor(new String[] {"/**"}, loggingInterceptor);
    }

    @Bean
    public MappedInterceptor mappedStatusLoggingInterceptor() {
        return new MappedInterceptor(new String[] {"/**"}, statusLoggingInterceptor);
    }
}
