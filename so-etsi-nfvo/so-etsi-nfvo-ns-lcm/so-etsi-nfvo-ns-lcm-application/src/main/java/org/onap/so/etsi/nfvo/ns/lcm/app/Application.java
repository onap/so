/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Ericsson. All rights reserved.
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
package org.onap.so.etsi.nfvo.ns.lcm.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

/**
 * @author Waqas Ikram (waqas.ikram@est.tech)
 *
 */
@SpringBootApplication(scanBasePackages = {"org.onap.so"})
@ComponentScan(basePackages = {"org.onap"}, nameGenerator = DefaultToShortClassNameBeanNameGenerator.class,
        excludeFilters = {@Filter(type = FilterType.ANNOTATION, classes = SpringBootApplication.class)})
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    /**
     * Entry point for the Spring boot application
     *
     * @param args arguments for the application
     */
    public static void main(final String[] args) {
        new SpringApplication(Application.class).run(args);
        logger.info("SO ETSI NFVO NS LCM Application started!");

    }

}
