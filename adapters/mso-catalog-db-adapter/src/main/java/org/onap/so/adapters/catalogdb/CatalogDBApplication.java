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

package org.onap.so.adapters.catalogdb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"org.onap.so.adapters.catalogdb", "org.onap.so.db.catalog.client",
        "org.onap.so.logging.jaxrs.filter", "org.onap.so.logging.spring.interceptor", "org.onap.so.client",
        "org.onap.so.configuration", "org.onap.so.db", "org.onap.so.logging.filter",
        "org.onap.so.logger, org.onap.so.security"})
@EnableJpaRepositories("org.onap.so.db.catalog.data.repository")
@EntityScan("org.onap.so.db.catalog.beans")
@EnableScheduling
public class CatalogDBApplication {

    private static final String LOGS_DIR = "logs_dir";

    private static void setLogsDir() {
        if (System.getProperty(LOGS_DIR) == null) {
            System.getProperties().setProperty(LOGS_DIR, "./logs/catdb/");
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(CatalogDBApplication.class, args);
        java.security.Security.setProperty("networkaddress.cache.ttl", "10");
        System.getProperties().setProperty("mso.db", "MARIADB");
        System.getProperties().setProperty("server.name", "Springboot");
        setLogsDir();
    }
}
