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

package org.onap.so.asdc;

import org.onap.so.db.request.data.repository.InfraActiveRequestsRepositoryImpl;
import org.onap.so.requestsdb.RequestsDBHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@Profile("test")
@ComponentScan(
        basePackages = {"org.onap.so.asdc", "org.onap.so.security", "org.onap.so.rest.service",
                "org.onap.so.configuration", "org.onap.so.client"},
        excludeFilters = {@Filter(type = FilterType.ANNOTATION, classes = SpringBootApplication.class),
                @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RequestsDBHelper.class),
                @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = InfraActiveRequestsRepositoryImpl.class)})
public class TestApplication {
    public static void main(final String... args) {
        SpringApplication.run(TestApplication.class, args);
        System.getProperties().setProperty("mso.db", "MARIADB");
        System.getProperties().setProperty("server.name", "Springboot");
        System.getProperties().setProperty("mso.config.path", "src/test/resources/");
    }
}
