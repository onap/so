/*-
 * ============LICENSE_START=======================================================
 * ONAP - SO
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.so.adapters.requestsdb.application;

import javax.sql.DataSource;
import org.onap.so.logging.filter.base.Constants;
import org.onap.so.logging.filter.base.ONAPComponents;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.jmx.support.RegistrationPolicy;
import org.springframework.scheduling.annotation.EnableScheduling;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;

/**
 * @since Version 1.0
 *
 */
@SpringBootApplication(scanBasePackages = {"org.onap.so", "org.onap.so.logging.filter"})
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "120s")
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
public class MSORequestDBApplication {

    private static final String LOGS_DIR = "logs_dir";

    private static void setLogsDir() {
        if (System.getProperty(LOGS_DIR) == null) {
            System.getProperties().setProperty(LOGS_DIR, "./logs/reqdb/");
        }
    }

    public static void main(String... args) {
        System.setProperty(Constants.Property.PARTNER_NAME, ONAPComponents.REQUEST_DB.toString());
        SpringApplication.run(MSORequestDBApplication.class, args);
        java.security.Security.setProperty("networkaddress.cache.ttl", "10");
        setLogsDir();
    }

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(dataSource);
    }


}
