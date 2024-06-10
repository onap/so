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

import jakarta.annotation.PostConstruct;
import org.onap.so.asdc.activity.DeployActivitySpecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"org.onap.so"})
@EnableScheduling
@EnableJpaRepositories("org.onap.so.db.catalog.data.repository")
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final String LOGS_DIR = "logs_dir";

    @Autowired
    DeployActivitySpecs deployActivitySpecs;

    private static void setLogsDir() {
        if (System.getProperty(LOGS_DIR) == null) {
            System.getProperties().setProperty(LOGS_DIR, "./logs/asdc/");
        }
    }

    @PostConstruct
    private void deployActivities() {
        try {
            deployActivitySpecs.deployActivities();
        } catch (Exception e) {
            logger.warn("{} {}", "Exception on deploying activitySpecs: ", e.getMessage());
        }

    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.getProperties().setProperty("mso.db", "MARIADB");

        System.getProperties().setProperty("server.name", "Springboot");
        setLogsDir();
    }
}
